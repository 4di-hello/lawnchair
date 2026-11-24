package app.lawnchair.hotseat

import android.content.res.Configuration
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import com.android.launcher3.Launcher
import com.android.launcher3.Utilities

/**
 * LC: how far the dock has to move up so a navigation bar does not cover its icons.
 *
 * Lawnchair lays out the dock for the navigation mode set in Android. A bar shown later on top
 * of gesture navigation, like Xiaomi's full screen gesture toggle (force_fsg_nav_bar), is not part
 * of that, so the dock is measured against the bar that is actually there.
 */
object NavBarLift {

    // Xiaomi (MIUI/HyperOS): 1 = full screen gestures, 0 = navigation buttons shown.
    const val XIAOMI_SETTING = "force_fsg_nav_bar"

    private const val BUTTONS_HEIGHT_DP = 48f
    private const val MARGIN_DP = 4f

    /**
     * Extra bottom space for the dock and the home screen pages above it, so that both move up
     * together and the pages get smaller rows instead of running under the dock.
     */
    @JvmStatic
    fun forLauncher(launcher: Launcher): Int {
        val grid = launcher.deviceProfile
        if (grid.isVerticalBarLayout) return 0
        val hotseat = launcher.hotseat ?: return 0
        return extraBottom(hotseat, grid.getHotseatLayoutPadding(launcher).bottom)
    }

    /** Extra bottom space for the dock whose icons end [iconsBottomPadding] above its bottom. */
    @JvmStatic
    fun extraBottom(dock: View, iconsBottomPadding: Int): Int {
        if (!Utilities.ATLEAST_R) return 0
        val context = dock.context
        if (context.resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) return 0
        val insets = dock.rootWindowInsets
        val reported = insets?.let {
            maxOf(
                it.getInsets(WindowInsets.Type.navigationBars()).bottom,
                it.getInsets(WindowInsets.Type.systemBars()).bottom,
            )
        } ?: 0
        val xiaomiButtons = if (Settings.Global.getInt(context.contentResolver, XIAOMI_SETTING, -1) == 0) {
            Utilities.dpToPx(BUTTONS_HEIGHT_DP)
        } else {
            0
        }
        val bar = maxOf(reported, xiaomiButtons)
        // Small bars (the gesture handle) already fit below the icons.
        if (bar <= 0) return 0
        return maxOf(0, bar + Utilities.dpToPx(MARGIN_DP) - iconsBottomPadding)
    }
}
