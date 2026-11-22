package app.lawnchair.ui.popup

import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.lawnchair.LawnchairLauncher
import app.lawnchair.gestures.type.GestureType
import app.lawnchair.launcherNullable
import app.lawnchair.ui.preferences.components.ItemGesturePreference
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.views.ComposeBottomSheet
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.DropTarget
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_DESKTOP
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_HOTSEAT
import com.android.launcher3.R
import com.android.launcher3.dragndrop.DragOptions
import com.android.launcher3.folder.FolderIcon
import com.android.launcher3.logging.StatsLogManager
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.views.OptionsPopupView

/**
 * LC: Long-pressing a home screen folder shows a small menu to set the folder's swipe gestures.
 * Like the app shortcut popup, it closes as soon as the folder is dragged.
 */
object FolderGesturesPopup {

    @JvmStatic
    fun startLongPressAction(folderIcon: FolderIcon): DragOptions.PreDragCondition? {
        val launcher = folderIcon.context.launcherNullable ?: return null
        val info = folderIcon.mInfo ?: return null
        if (info.id == ItemInfo.NO_ID) return null
        if (info.container != CONTAINER_DESKTOP && info.container != CONTAINER_HOTSEAT) return null

        val bounds = Rect()
        launcher.dragLayer.getDescendantRectRelativeToSelf(folderIcon, bounds)
        val itemId = info.id
        val title = info.title?.toString().orEmpty()
        val popup = OptionsPopupView.show<LawnchairLauncher>(
            launcher,
            RectF(bounds),
            listOf(
                OptionsPopupView.OptionItem(
                    launcher,
                    R.string.gestures_label,
                    R.drawable.ic_gestures,
                    StatsLogManager.LauncherEvent.IGNORE,
                ) {
                    showFolderGestures(launcher, itemId, title)
                    true
                },
            ),
            true,
        ) ?: return null

        val threshold = launcher.resources.getDimensionPixelSize(R.dimen.deep_shortcuts_start_drag_threshold)
        return object : DragOptions.PreDragCondition {
            override fun shouldStartDrag(distanceDragged: Double) = distanceDragged > threshold

            override fun onPreDragStart(dragObject: DropTarget.DragObject) = Unit

            override fun onPreDragEnd(dragObject: DropTarget.DragObject, dragStarted: Boolean) {
                if (dragStarted) {
                    popup.close(true)
                } else {
                    // Long press released without dragging: keep the menu, show the folder again.
                    folderIcon.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showFolderGestures(launcher: LawnchairLauncher, itemId: Int, title: String) {
        AbstractFloatingView.closeAllOpenViews(launcher)
        ComposeBottomSheet.show(
            context = launcher,
            contentPaddings = PaddingValues(bottom = 64.dp),
        ) {
            FolderGesturesSheet(itemId = itemId, title = title)
        }
    }
}

@Composable
private fun FolderGesturesSheet(
    itemId: Int,
    title: String,
    modifier: Modifier = Modifier,
) {
    val heading = stringResource(id = R.string.gestures_label)
    PreferenceGroup(
        heading = if (title.isEmpty()) heading else "$heading · $title",
        modifier = modifier.padding(top = 16.dp),
    ) {
        GestureType.entries.forEach { gestureType ->
            ItemGesturePreference(
                itemId = itemId,
                fallback = null,
                gestureType = gestureType,
                label = stringResource(id = gestureType.labelResId),
            )
        }
    }
}
