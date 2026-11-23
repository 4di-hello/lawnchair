package app.lawnchair.ui.preferences.destinations

import android.app.Activity
import android.content.Intent
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
import app.lawnchair.gestures.AppShortcutEntry
import app.lawnchair.gestures.AppShortcuts
import app.lawnchair.ui.preferences.components.AppItem
import app.lawnchair.ui.preferences.components.AppItemPlaceholder
import app.lawnchair.ui.preferences.components.layout.PreferenceLazyColumn
import app.lawnchair.ui.preferences.components.layout.PreferenceSearchScaffold
import app.lawnchair.ui.preferences.components.layout.PreferenceTemplate
import app.lawnchair.ui.preferences.components.layout.preferenceGroupItems
import app.lawnchair.util.kotlinxJson
import com.android.launcher3.R
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

            list.isEmpty() -> PreferenceLazyColumn(padding) {
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
                val rows = remember(groups, expanded.toList(), search) {
                    groups.flatMap { group ->
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

private sealed interface ShortcutRow {
    val key: String

    data class Header(val groupKey: String, val entry: AppShortcutEntry) : ShortcutRow {
        override val key = "app:$groupKey"
    }

    data class Child(val entry: AppShortcutEntry) : ShortcutRow {
        override val key = "shortcut:${groupKey(entry)}/${entry.target.id}"
    }
}
