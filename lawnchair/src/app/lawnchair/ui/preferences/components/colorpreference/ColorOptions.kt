package app.lawnchair.ui.preferences.components.colorpreference

import android.graphics.Color
import androidx.compose.ui.res.stringResource
import app.lawnchair.theme.color.ColorOption
import com.android.launcher3.R

val staticColors: List<ColorPreferenceEntry<ColorOption>> = sequenceOf(
    ColorOption.CustomColor(0xFFF32020),
    ColorOption.CustomColor(0xFFF20D69),
    ColorOption.CustomColor(0xFF7452FF),
    ColorOption.CustomColor(0xFF2C41C9),
    ColorOption.LawnchairBlue,
    ColorOption.CustomColor(0xFF00BAD6),
    ColorOption.CustomColor(0xFF00A399),
    ColorOption.CustomColor(0xFF47B84F),
    ColorOption.CustomColor(0xFFFFBB00),
    ColorOption.CustomColor(0xFFFF9800),
    ColorOption.CustomColor(0xFF7C5445),
    ColorOption.CustomColor(0xFF67818E),
).map(ColorOption::colorPreferenceEntry).toList()

val dynamicColors: List<ColorPreferenceEntry<ColorOption>> =
    sequenceOf(ColorOption.SystemAccent, ColorOption.WallpaperPrimary)
        .filter(ColorOption::isSupported)
        .map(ColorOption::colorPreferenceEntry)
        .toList()

val dynamicColorsWithDefault: List<ColorPreferenceEntry<ColorOption>> =
    (dynamicColors.asSequence() + ColorOption.Default.colorPreferenceEntry).toList()

/**
 * LC: choices for the swipe gesture marks: automatic (the label color of each icon), white,
 * black and the system accent. Any other color can be picked as a custom color.
 */
val gestureIndicatorColors: List<ColorPreferenceEntry<ColorOption>> = listOfNotNull(
    ColorPreferenceEntry<ColorOption>(
        ColorOption.Default,
        { stringResource(id = R.string.gesture_indicator_color_auto) },
        { Color.GRAY },
    ),
    ColorPreferenceEntry<ColorOption>(
        ColorOption.CustomColor(Color.WHITE),
        { stringResource(id = R.string.gesture_indicator_color_white) },
        { Color.WHITE },
        { Color.WHITE },
    ),
    ColorPreferenceEntry<ColorOption>(
        ColorOption.CustomColor(Color.BLACK),
        { stringResource(id = R.string.gesture_indicator_color_black) },
        { Color.BLACK },
        { Color.BLACK },
    ),
    ColorOption.SystemAccent.colorPreferenceEntry.takeIf { ColorOption.SystemAccent.isSupported },
)
