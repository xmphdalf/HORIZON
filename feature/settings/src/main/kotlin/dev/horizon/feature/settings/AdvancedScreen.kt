package dev.horizon.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import dev.horizon.core.design.*
import kotlin.math.roundToInt

@Composable
fun AdvancedScreen(deep: Boolean, restore: Boolean, rgb: Int, excluded: List<Pair<String,String>>, usage: Boolean,
                   action: (String, Int) -> Unit, remove: (String) -> Unit, back: () -> Unit) {
    Page("Settings", "Advanced", back) {
        Setting("Dim below system minimum", if (deep) "On" else "Off") { action("deep", 0) }
        Setting("Restore after reboot", if (restore) "On" else "Off") { action("restore", 0) }
        Copy("Android controls the keyboard and system icons above the tint.")
        Setting("Per-channel colour", if (rgb < 0) "Temperature" else "Reset") { action("rgb", -1) }
        listOf("R", "G", "B").forEachIndexed { index, channel ->
            val shift = (2 - index) * 8
            val color = if (rgb < 0) 0xff9e2e else rgb
            val level = color shr shift and 255
            Text("$channel   ${(level * 100 / 255)}%", color = Ink, fontFamily = Mono, fontSize = 13.sp)
            Slider(level.toFloat(), { action("rgb", (color and (255 shl shift).inv()) or (it.roundToInt() shl shift)) },
                valueRange = 0f..255f, colors = SliderDefaults.colors(thumbColor = Ember, activeTrackColor = Ember))
        }
        Text("PER-APP RULES", color = Muted, fontFamily = Mono, fontSize = 11.sp)
        excluded.forEach { (id, name) -> Setting(name, "Filter off · Remove") { remove(id) } }
        Action(if (usage) "+ Add app" else "Allow usage access", Modifier.fillMaxWidth()) { action("apps", 0) }
        Setting("About the science", "›") { action("science", 0) }
        Setting("Permissions", "›") { action("permission", 0) }
    }
}
