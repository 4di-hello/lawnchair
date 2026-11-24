package app.lawnchair.gestures

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import app.lawnchair.gestures.type.GestureType
import app.lawnchair.preferences2.PreferenceManager2
import app.lawnchair.preferences2.firstCached
import app.lawnchair.theme.color.ColorOption

/**
 * LC: small marks next to an icon or folder, one on each side that has a swipe gesture, so you
 * can see at a glance where swiping does something. Can be turned off in the home screen
 * settings.
 */
object GestureIndicator {

    private const val LENGTH_FACTOR = 0.24f
    private const val ALPHA = 200

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(60, 0, 0, 0)
    }
    private val rect = RectF()

    @JvmStatic
    fun isEnabled(view: View): Boolean {
        val prefs = PreferenceManager2.getInstance(view.context)
        return prefs.showGestureIndicator.firstCached(prefs)
    }

    /**
     * Draws the marks for [gestures] around [iconBounds], in the color picked in the settings.
     * Automatic uses [color], the label color, so the marks stay readable on light and dark
     * wallpapers like the labels.
     */
    @JvmStatic
    fun draw(view: View, canvas: Canvas, iconBounds: Rect, gestures: Set<GestureType>, color: Int) {
        if (gestures.isEmpty()) return
        val density = view.resources.displayMetrics.density
        val thickness = 2.5f * density
        val gap = 2f * density
        val radius = thickness / 2
        val length = iconBounds.width() * LENGTH_FACTOR
        val prefs = PreferenceManager2.getInstance(view.context)
        val option = prefs.gestureIndicatorColor.firstCached(prefs)
        paint.color = if (option == ColorOption.Default) color else option.colorPreferenceEntry.lightColor(view.context)
        paint.alpha = ALPHA
        val cx = iconBounds.exactCenterX()
        val cy = iconBounds.exactCenterY()
        for (type in gestures) {
            when (type) {
                GestureType.SWIPE_UP -> rect.set(
                    cx - length / 2,
                    iconBounds.top - gap - thickness,
                    cx + length / 2,
                    iconBounds.top - gap,
                )

                GestureType.SWIPE_DOWN -> rect.set(
                    cx - length / 2,
                    iconBounds.bottom + gap,
                    cx + length / 2,
                    iconBounds.bottom + gap + thickness,
                )

                GestureType.SWIPE_LEFT -> rect.set(
                    iconBounds.left - gap - thickness,
                    cy - length / 2,
                    iconBounds.left - gap,
                    cy + length / 2,
                )

                GestureType.SWIPE_RIGHT -> rect.set(
                    iconBounds.right + gap,
                    cy - length / 2,
                    iconBounds.right + gap + thickness,
                    cy + length / 2,
                )

                else -> continue
            }
            rect.offset(0f, density / 2)
            canvas.drawRoundRect(rect, radius, radius, shadowPaint)
            rect.offset(0f, -density / 2)
            canvas.drawRoundRect(rect, radius, radius, paint)
        }
    }
}
