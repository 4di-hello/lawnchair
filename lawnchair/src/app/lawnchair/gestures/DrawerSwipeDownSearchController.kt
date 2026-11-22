package app.lawnchair.gestures

import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import app.lawnchair.LawnchairLauncher
import app.lawnchair.preferences2.PreferenceManager2
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.LauncherState
import com.android.launcher3.util.TouchController
import com.patrykmichalik.opto.core.onEach
import kotlin.math.abs

/**
 * LC fork: in the app drawer, a downward swipe that would otherwise close the drawer
 * focuses the search field and opens the keyboard instead (optional).
 */
class DrawerSwipeDownSearchController(
    private val launcher: LawnchairLauncher,
) : TouchController {

    private val touchSlop = ViewConfiguration.get(launcher).scaledTouchSlop
    private var enabled = false
    private var tracking = false
    private var downX = 0f
    private var downY = 0f

    init {
        PreferenceManager2.getInstance(launcher).swipeDownInDrawerOpensSearch
            .onEach(launchIn = launcher.lifecycleScope) { enabled = it }
    }

    override fun onControllerInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                tracking = enabled && canStart(ev)
                downX = ev.x
                downY = ev.y
            }

            MotionEvent.ACTION_MOVE -> if (tracking) {
                val dx = ev.x - downX
                val dy = ev.y - downY
                if (dy > touchSlop && dy > abs(dx) * 2) {
                    tracking = false
                    focusSearch()
                    return true
                }
                if (dy < -touchSlop || abs(dx) > touchSlop) {
                    tracking = false
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> tracking = false
        }
        return false
    }

    // Swallow the rest of the gesture once it has opened the search.
    override fun onControllerTouchEvent(ev: MotionEvent): Boolean = true

    private fun canStart(ev: MotionEvent): Boolean {
        if (!launcher.isInState(LauncherState.ALL_APPS)) return false
        if (AbstractFloatingView.getTopOpenView(launcher) != null) return false
        if (isKeyboardVisible()) return false
        val appsView = launcher.appsView ?: return false
        // Only where a downward swipe would move the drawer, i.e. the list is at its top.
        return appsView.shouldContainerScroll(ev)
    }

    private fun isKeyboardVisible(): Boolean {
        val insets = launcher.rootView.rootWindowInsets ?: return false
        return WindowInsetsCompat.toWindowInsetsCompat(insets).isVisible(WindowInsetsCompat.Type.ime())
    }

    private fun focusSearch() {
        launcher.appsView.searchUiManager.editText?.showKeyboard()
    }
}
