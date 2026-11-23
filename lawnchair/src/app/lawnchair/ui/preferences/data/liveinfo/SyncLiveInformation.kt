package app.lawnchair.ui.preferences.data.liveinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import app.lawnchair.preferences2.asState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SyncLiveInformation(
    liveInformationManager: LiveInformationManager = liveInformationManager(),
) {
    val enabled by liveInformationManager.enabled.asState()
    val endpoint by liveInformationManager.endpoint.asState()

    LaunchedEffect(enabled, endpoint) {
        // Fork: no network access except for updates, so announcements are never fetched.
        if (enabled && LIVE_INFORMATION_ALLOWED) {
            getLiveInformation(endpoint)?.let { liveInformation ->
                liveInformationManager.liveInformation.set(liveInformation)
            }
        }
    }
}

private const val LIVE_INFORMATION_ALLOWED = false
