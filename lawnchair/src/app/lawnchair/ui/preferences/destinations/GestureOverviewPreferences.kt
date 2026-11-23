package app.lawnchair.ui.preferences.destinations

import android.content.Context
import android.content.pm.LauncherApps
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lawnchair.preferences2.PreferenceManager2
import app.lawnchair.preferences2.StoredIconGesture
import app.lawnchair.preferences2.preferenceManager2
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.AppGesturePreference
import app.lawnchair.ui.preferences.components.ItemGesturePreference
import app.lawnchair.ui.preferences.components.controls.ClickablePreference
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.layout.PreferenceLayout
import app.lawnchair.ui.preferences.components.layout.PreferenceTemplate
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherSettings.Favorites
import com.android.launcher3.R
import com.android.launcher3.util.ComponentKey
import kotlin.coroutines.resume
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/** Fork: name of a home screen item and where it is, for the gesture overview. */
private data class HomeItemName(val title: String, val place: Int)

/**
 * Fork: lists every swipe gesture set on apps, home screen icons and folders, grouped by
 * what it belongs to. Gestures can be changed or removed here; gestures of items that no
 * longer exist can be cleaned up.
 */
@Composable
fun GestureOverviewPreferences(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val prefs = preferenceManager2()
    val scope = rememberCoroutineScope()
    val gestures by remember { prefs.storedIconGestures() }.collectAsStateWithLifecycle(initialValue = null)
    val items by produceState<Map<Int, HomeItemName>?>(initialValue = null) { value = loadHomeItems(context) }

    PreferenceLayout(
        label = stringResource(R.string.gesture_overview_label),
        backArrowVisible = !LocalIsExpandedScreen.current,
        modifier = modifier,
    ) {
        val all = gestures ?: return@PreferenceLayout
        val names = items ?: return@PreferenceLayout
        if (all.isEmpty()) {
            PreferenceGroup {
                PreferenceTemplate(title = { Text(text = stringResource(R.string.gesture_overview_empty)) })
            }
            return@PreferenceLayout
        }

        val (itemGestures, appGestures) = all.partition { it.itemId != null }
        val orphans = itemGestures.filter { it.itemId !in names }

        appGestures.groupBy { it.appKey!! }
            .map { (key, list) -> appLabel(context, key) to list }
            .sortedBy { it.first.lowercase() }
            .forEach { (label, list) ->
                PreferenceGroup(heading = stringResource(R.string.gesture_overview_app, label)) {
                    list.sortedBy { it.type.ordinal }.forEach { gesture ->
                        GestureRow(gesture, onRemove = { scope.launch { prefs.removeStoredIconGesture(gesture.prefKey) } }) {
                            AppGesturePreference(
                                cmp = gesture.appKey!!,
                                gestureType = gesture.type,
                                label = stringResource(gesture.type.labelResId),
                            )
                        }
                    }
                }
            }

        itemGestures.filter { it.itemId in names }
            .groupBy { it.itemId!! }
            .map { (id, list) -> names.getValue(id) to (id to list) }
            .sortedBy { it.first.title.lowercase() }
            .forEach { (name, entry) ->
                val (id, list) = entry
                PreferenceGroup(heading = "${name.title.ifEmpty { "–" }} · ${stringResource(name.place)}") {
                    list.sortedBy { it.type.ordinal }.forEach { gesture ->
                        GestureRow(gesture, onRemove = { scope.launch { prefs.removeStoredIconGesture(gesture.prefKey) } }) {
                            ItemGesturePreference(
                                itemId = id,
                                fallback = null,
                                gestureType = gesture.type,
                                label = stringResource(gesture.type.labelResId),
                            )
                        }
                    }
                }
            }

        if (orphans.isNotEmpty()) {
            PreferenceGroup(heading = stringResource(R.string.gesture_overview_orphans)) {
                ClickablePreference(
                    label = stringResource(R.string.gesture_overview_remove_orphans, orphans.size),
                    onClick = {
                        scope.launch { orphans.forEach { prefs.removeStoredIconGesture(it.prefKey) } }
                    },
                )
            }
        }
    }
}

@Composable
private fun GestureRow(
    gesture: StoredIconGesture,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) { content() }
        IconButton(onClick = onRemove, modifier = Modifier.padding(end = 8.dp)) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = stringResource(R.string.gesture_overview_remove, stringResource(gesture.type.labelResId)),
            )
        }
    }
}

private fun appLabel(context: Context, key: ComponentKey): String = runCatching {
    context.getSystemService(LauncherApps::class.java)
        ?.getActivityList(key.componentName.packageName, key.user)
        ?.firstOrNull { it.componentName == key.componentName }
        ?.label?.toString()
}.getOrNull() ?: key.componentName.packageName

/** Titles of the home screen items by id, read from the launcher's model. */
private suspend fun loadHomeItems(context: Context): Map<Int, HomeItemName> = suspendCancellableCoroutine { continuation ->
    val prefs = PreferenceManager2.getInstance(context)
    LauncherAppState.getInstance(context).model.loadAsync { dataModel ->
        val result = if (dataModel == null) {
            emptyMap()
        } else {
            synchronized(dataModel) {
                dataModel.itemsIdMap.associate { info ->
                    val place = when {
                        info.itemType == Favorites.ITEM_TYPE_FOLDER -> R.string.gesture_overview_place_folder
                        info.container == Favorites.CONTAINER_HOTSEAT -> R.string.gesture_overview_place_dock
                        info.container >= 0 -> R.string.gesture_overview_place_in_folder
                        else -> R.string.gesture_overview_place_home
                    }
                    val title = prefs.getItemTitleCached(info.id) ?: info.title?.toString().orEmpty()
                    info.id to HomeItemName(title, place)
                }
            }
        }
        if (continuation.isActive) continuation.resume(result)
    }
}
