package app.lawnchair.util

import android.animation.ValueAnimator
import android.content.Context
import android.provider.Settings
import android.util.Log
import app.lawnchair.preferences2.PreferenceManager2
import app.lawnchair.preferences2.firstCached

/**
 * Fork: speed of the launcher's own animations (app drawer, folders, opening and closing apps
 * when Lawnchair runs them), on top of Android's animation scale. Android applies its scale to
 * every app; this changes it only for the launcher's process.
 */
object AnimationSpeed {

    private const val TAG = "AnimationSpeed"

    @JvmStatic
    fun apply(context: Context) {
        val prefs = PreferenceManager2.getInstance(context)
        val speed = prefs.animationSpeed.firstCached(prefs).coerceIn(0.25f, 4f)
        val system = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        // Animations turned off in Android stay off.
        val scale = if (system == 0f) 0f else system / speed
        if (ValueAnimator.getDurationScale() == scale) return
        try {
            ValueAnimator::class.java.getMethod("setDurationScale", Float::class.javaPrimitiveType).invoke(null, scale)
        } catch (e: Exception) {
            Log.w(TAG, "Could not set the animation speed", e)
        }
    }
}
