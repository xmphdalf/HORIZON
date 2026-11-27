package dev.horizon.feature.permission

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import dev.horizon.core.design.*

@Composable
fun PermissionScreen(overlay: Boolean, write: Boolean, proceed: () -> Unit, back: () -> Unit) {
    Page("Permissions", if (!overlay) "Display over other apps" else "Modify system settings", back) {
        Copy("Android will ask whether Horizon can draw over other apps. That is how the warm layer reaches the screen you are reading.")
        Copy("The layer cannot read, tap or record the screen. Horizon saves your brightness before dimming and restores it when you turn the filter off.")
        Text("No internet permission at all.", color = Ink, fontFamily = Outfit, fontSize = 21.sp)
        Copy("No accounts, analytics or ads. You can revoke access in Android settings at any time.")
        Text("PERMISSIONS REQUESTED", fontFamily = Mono, color = Muted, fontSize = 11.sp)
        Copy("Display over other apps · " + if (overlay) "Allowed" else "Required")
        Copy("Modify system settings · " + if (write) "Allowed" else "Required")
        Copy("A foreground service keeps the tint active. Notifications add a quick off action. Alarms, boot restore, approximate location and usage access support optional features.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Action("Not now", Modifier.weight(1f), click = back)
            Action("Open settings", Modifier.weight(1f), click = proceed)
        }
    }
}
