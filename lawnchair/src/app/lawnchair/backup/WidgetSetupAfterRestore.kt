package app.lawnchair.backup

import android.content.Context
import androidx.core.content.edit
import com.android.launcher3.Launcher
import com.android.launcher3.widget.PendingAppWidgetHostView

/**
 * Fork: after a backup has been restored, widgets come back at their position and size but
 * usually need to be set up again. Instead of making the user tap every placeholder, this opens
 * the setup of one widget each time the home screen comes back, until all have been offered once.
 */
object WidgetSetupAfterRestore {

    // Kept outside the backed up preferences so a restore can't overwrite the flag.
    private const val PREFS_NAME = "fork_widget_setup_after_restore"
    private const val KEY_PENDING = "pending"
    private const val DELAY_MS = 800L

    private val offeredWidgetIds = mutableSetOf<Int>()

    fun markRestored(context: Context) {
        offeredWidgetIds.clear()
        prefs(context).edit { putBoolean(KEY_PENDING, true) }
    }

    fun onResume(launcher: Launcher) {
        if (!prefs(launcher).getBoolean(KEY_PENDING, false)) return
        launcher.workspace.postDelayed({ setUpNextWidget(launcher) }, DELAY_MS)
    }

    private fun setUpNextWidget(launcher: Launcher) {
        if (launcher.isDestroyed || !launcher.hasBeenResumed() || launcher.isWorkspaceLoading) return
        val next = launcher.workspace.mapOverItems { _, view ->
            view is PendingAppWidgetHostView &&
                view.isReadyForClickSetup &&
                view.appWidgetId !in offeredWidgetIds
        } as? PendingAppWidgetHostView
        if (next == null) {
            prefs(launcher).edit { putBoolean(KEY_PENDING, false) }
            return
        }
        offeredWidgetIds += next.appWidgetId
        // Same as tapping the "tap to set up" placeholder.
        next.onClick(next)
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
