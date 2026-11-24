package app.lawnchair.ui.preferences.destinations

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import app.lawnchair.gestures.AppShortcutEntry
import app.lawnchair.gestures.AppShortcuts
import app.lawnchair.gestures.ShortcutCreator
import app.lawnchair.gestures.ShortcutCreators
import app.lawnchair.ui.preferences.components.AppItem
import app.lawnchair.ui.preferences.components.AppItemPlaceholder
import app.lawnchair.ui.preferences.components.layout.PreferenceLazyColumn
import app.lawnchair.ui.preferences.components.layout.PreferenceSearchScaffold
import app.lawnchair.ui.preferences.components.layout.PreferenceTemplate
import app.lawnchair.ui.preferences.components.layout.preferenceGroupItems
import app.lawnchair.util.kotlinxJson
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.MSDLPlayerWrapper
import com.google.android.msdl.data.model.MSDLToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lists the app shortcuts of all apps this launcher may start (for example a pinned chat or a
 * "new message" action) so that one can be bound to a gesture. Grouped by app, each app can be
 * expanded to show its shortcuts; the search filters apps and shortcuts by name.
 */
@Composable
fun PickShortcutForGesture(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as Activity
    val msdlPlayer = MSDLPlayerWrapper.INSTANCE.get(context)
    val state = rememberLazyListState()
    // Apps whose shortcuts are shown. All apps start collapsed, so the list stays short.
    val expanded = remember { mutableStateListOf<String>() }
    var query by rememberSaveable { mutableStateOf("") }

    val shortcuts by produceState<List<AppShortcutEntry>?>(initialValue = null) {
        value = withContext(Dispatchers.IO) { AppShortcuts.load(context) }
    }
    // Fork: apps' shortcut setups, e.g. Tasker's task shortcut
    val creators by produceState(initialValue = emptyList<ShortcutCreator>()) {
        value = withContext(Dispatchers.IO) { ShortcutCreators.load(context) }
    }
    // Name of the app whose shortcut setup is open; saved in case Android recreates this screen.
    var pendingCreator by rememberSaveable { mutableStateOf<String?>(null) }
    fun onCreated(result: ActivityResult) {
        val creatorLabel = pendingCreator ?: return
        pendingCreator = null
        if (result.resultCode != Activity.RESULT_OK) return
        val config = ShortcutCreators.configFromResult(context, creatorLabel, result.data)
        if (config == null) {
            Toast.makeText(context, R.string.pick_shortcut_create_failed, Toast.LENGTH_SHORT).show()
            return
        }
        activity.setResult(Activity.RESULT_OK, Intent().putExtra("config", kotlinxJson.encodeToString(config)))
        activity.finish()
    }

    // Like the launcher's own widget and shortcut picker: apps of this profile are started
    // directly; for other profiles Android hands out an IntentSender, which must be allowed to
    // start the app's activity (blocked silently on Android 14 and later otherwise).
    val createInProfile = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult(), ::onCreated)
    val createInOtherProfile = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult(), ::onCreated)

    fun onCreate(creator: ShortcutCreator) {
        pendingCreator = creator.label
        try {
            if (creator.info.user == Process.myUserHandle()) {
                createInProfile.launch(Intent(Intent.ACTION_CREATE_SHORTCUT).setComponent(creator.info.componentName))
            } else {
                val sender = context.getSystemService(LauncherApps::class.java)!!.getShortcutConfigActivityIntent(creator.info)!!
                createInOtherProfile.launch(IntentSenderRequest.Builder(sender).build(), AllowBackgroundStart)
            }
        } catch (e: Exception) {
            pendingCreator = null
            Toast.makeText(context, R.string.pick_shortcut_create_failed, Toast.LENGTH_SHORT).show()
        }
    }

    fun onSelect(entry: AppShortcutEntry) {
        val configString = kotlinxJson.encodeToString(entry.toGestureConfig())
        activity.setResult(Activity.RESULT_OK, Intent().putExtra("config", configString))
        activity.finish()
    }

    PreferenceSearchScaffold(
        value = query,
        onValueChange = { query = it },
        modifier = modifier,
        placeholder = {
            Text(
                text = stringResource(id = R.string.pick_shortcut_search),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) { padding ->
        val list = shortcuts
        when {
            list == null -> PreferenceLazyColumn(padding, enabled = false) {
                preferenceGroupItems(count = 20, isFirstChild = true) {
                    AppItemPlaceholder()
                }
            }

            list.isEmpty() && creators.isEmpty() -> PreferenceLazyColumn(padding) {
                preferenceGroupItems(count = 1, isFirstChild = true) {
                    PreferenceTemplate(
                        title = { Text(text = stringResource(id = R.string.pick_shortcut_for_gesture_empty)) },
                    )
                }
            }

            else -> {
                val groups = remember(list) {
                    list.groupBy { it.target.packageName to it.target.user }.values.toList()
                }
                val search = query.trim()
                val rows = remember(groups, creators, expanded.toList(), search) {
                    // Fork: shortcut setups first, as one group that opens on a tap.
                    val shownCreators = if (search.isEmpty()) {
                        creators
                    } else {
                        creators.filter { it.label.contains(search, ignoreCase = true) }
                    }
                    val creatorRows = when {
                        shownCreators.isEmpty() -> emptyList()

                        search.isNotEmpty() || CREATORS_KEY in expanded ->
                            listOf(ShortcutRow.CreatorsHeader) + shownCreators.map { ShortcutRow.Creator(it) }

                        else -> listOf(ShortcutRow.CreatorsHeader)
                    }
                    creatorRows + groups.flatMap { group ->
                        val first = group.first()
                        val key = groupKey(first)
                        val header = ShortcutRow.Header(key, first)
                        when {
                            // While searching, apps open by themselves: an app that matches shows
                            // all its shortcuts, otherwise only the shortcuts that match.
                            search.isNotEmpty() -> {
                                val children = if (first.appLabel.contains(search, ignoreCase = true)) {
                                    group
                                } else {
                                    group.filter { it.shortcutLabel.contains(search, ignoreCase = true) }
                                }
                                if (children.isEmpty()) {
                                    emptyList()
                                } else {
                                    listOf(header) + children.map { ShortcutRow.Child(it) }
                                }
                            }

                            key in expanded -> listOf(header) + group.map { ShortcutRow.Child(it) }

                            else -> listOf(header)
                        }
                    }
                }
                PreferenceLazyColumn(padding, state = state) {
                    if (rows.isEmpty()) {
                        preferenceGroupItems(count = 1, isFirstChild = true) {
                            PreferenceTemplate(
                                title = { Text(text = stringResource(id = R.string.pick_shortcut_no_results)) },
                            )
                        }
                    }
                    preferenceGroupItems(
                        items = rows,
                        isFirstChild = true,
                        key = { _, row -> row.key },
                    ) { _, row ->
                        when (row) {
                            is ShortcutRow.Header -> {
                                val isExpanded = search.isNotEmpty() || row.groupKey in expanded
                                val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "arrow")
                                AppItem(
                                    label = row.entry.appLabel,
                                    icon = row.entry.appIcon,
                                    onClick = {
                                        msdlPlayer.playToken(MSDLToken.TAP_LOW_EMPHASIS)
                                        if (row.groupKey in expanded) {
                                            expanded.remove(row.groupKey)
                                        } else {
                                            expanded.add(row.groupKey)
                                        }
                                    },
                                    endWidget = {
                                        Icon(
                                            imageVector = Icons.Rounded.KeyboardArrowDown,
                                            contentDescription = stringResource(
                                                if (isExpanded) {
                                                    R.string.pick_shortcut_collapse
                                                } else {
                                                    R.string.pick_shortcut_expand
                                                },
                                            ),
                                            modifier = Modifier.rotate(rotation),
                                        )
                                    },
                                )
                            }

                            is ShortcutRow.CreatorsHeader -> {
                                val isExpanded = search.isNotEmpty() || CREATORS_KEY in expanded
                                val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "arrow")
                                PreferenceTemplate(
                                    title = { Text(text = stringResource(id = R.string.pick_shortcut_create)) },
                                    description = { Text(text = stringResource(id = R.string.pick_shortcut_create_description)) },
                                    onClick = {
                                        msdlPlayer.playToken(MSDLToken.TAP_LOW_EMPHASIS)
                                        if (CREATORS_KEY in expanded) {
                                            expanded.remove(CREATORS_KEY)
                                        } else {
                                            expanded.add(CREATORS_KEY)
                                        }
                                    },
                                    endWidget = {
                                        Icon(
                                            imageVector = Icons.Rounded.KeyboardArrowDown,
                                            contentDescription = null,
                                            modifier = Modifier.rotate(rotation),
                                        )
                                    },
                                )
                            }

                            is ShortcutRow.Creator -> AppItem(
                                label = row.creator.label,
                                icon = row.creator.icon,
                                onClick = {
                                    msdlPlayer.playToken(MSDLToken.TAP_MEDIUM_EMPHASIS)
                                    onCreate(row.creator)
                                },
                                modifier = Modifier.padding(start = 24.dp),
                            )

                            is ShortcutRow.Child -> AppItem(
                                label = row.entry.shortcutLabel,
                                icon = row.entry.icon,
                                onClick = {
                                    msdlPlayer.playToken(MSDLToken.TAP_MEDIUM_EMPHASIS)
                                    onSelect(row.entry)
                                },
                                modifier = Modifier.padding(start = 24.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun groupKey(entry: AppShortcutEntry) = "${entry.target.packageName}/${entry.target.user.hashCode()}"

private const val CREATORS_KEY = "creators"

private sealed interface ShortcutRow {
    val key: String

    data object CreatorsHeader : ShortcutRow {
        override val key = "creators"
    }

    data class Creator(val creator: ShortcutCreator) : ShortcutRow {
        override val key = "creator:${creator.info.componentName.flattenToString()}/${creator.info.user.hashCode()}"
    }

    data class Header(val groupKey: String, val entry: AppShortcutEntry) : ShortcutRow {
        override val key = "app:$groupKey"
    }

    data class Child(val entry: AppShortcutEntry) : ShortcutRow {
        override val key = "shortcut:${groupKey(entry)}/${entry.target.id}"
    }
}

/** Fork: lets an IntentSender from Android start the app's shortcut setup (Android 14+). */
private object AllowBackgroundStart : ActivityOptionsCompat() {
    override fun toBundle(): Bundle? = Utilities.allowBGLaunch(ActivityOptions.makeBasic()).toBundle()
}
