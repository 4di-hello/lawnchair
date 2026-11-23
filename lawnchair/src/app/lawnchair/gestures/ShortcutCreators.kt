package app.lawnchair.gestures

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.PinItemRequest
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import app.lawnchair.gestures.config.GestureHandlerConfig
import app.lawnchair.gestures.handlers.OpenAppTarget
import app.lawnchair.util.toBitmap
import com.android.launcher3.pm.PinRequestHelper
import com.android.launcher3.pm.UserCache

/** Fork: an app's shortcut setup (ACTION_CREATE_SHORTCUT), e.g. Tasker's task shortcut. */
data class ShortcutCreator(
    val label: String,
    val icon: Bitmap,
    val info: LauncherActivityInfo,
)

/**
 * Fork: shortcuts made through an app's shortcut setup, like adding a shortcut to the home
 * screen. This is how Tasker tasks and similar actions become gesture targets.
 */
object ShortcutCreators {

    private const val TAG = "ShortcutCreators"

    /** All apps' shortcut setups. Must not be called on the main thread. */
    fun load(context: Context): List<ShortcutCreator> {
        val launcherApps = context.getSystemService(LauncherApps::class.java) ?: return emptyList()
        val density = context.resources.displayMetrics.densityDpi
        return UserCache.INSTANCE.get(context).userProfiles.flatMap { user ->
            try {
                launcherApps.getShortcutConfigActivityList(null, user)
            } catch (e: Exception) {
                Log.w(TAG, "Could not list shortcut setups", e)
                emptyList()
            }
        }.filter { it.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.O }
            .map { info ->
                ShortcutCreator(
                    label = context.packageManager.getUserBadgedLabel(info.label, info.user).toString(),
                    icon = info.getBadgedIcon(density).toBitmap(),
                    info = info,
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    /** Turns what a shortcut setup returned into a gesture, or null if it returned nothing usable. */
    fun configFromResult(context: Context, creator: ShortcutCreator, data: Intent?): GestureHandlerConfig? {
        if (data == null) return null
        val request = PinRequestHelper.getPinItemRequest(data)
        if (request != null) {
            if (request.requestType != PinItemRequest.REQUEST_TYPE_SHORTCUT) return null
            val shortcut = request.shortcutInfo ?: return null
            // Accepting pins the shortcut for this launcher, so it can be started later.
            if (!request.isValid || !request.accept()) return null
            val label = (shortcut.longLabel ?: shortcut.shortLabel)?.toString()
            return GestureHandlerConfig.OpenApp(
                appName = if (label.isNullOrEmpty()) creator.label else label,
                target = OpenAppTarget.Shortcut(
                    intent = Intent(Intent.ACTION_MAIN).setPackage(shortcut.`package`),
                    user = shortcut.userHandle,
                    packageName = shortcut.`package`,
                    id = shortcut.id,
                ),
            )
        }
        @Suppress("DEPRECATION")
        val intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT) as? Intent ?: return null
        @Suppress("DEPRECATION")
        val name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
        return GestureHandlerConfig.OpenApp(
            appName = if (name.isNullOrEmpty()) creator.label else name,
            target = OpenAppTarget.LegacyShortcut(intent),
        )
    }
}
