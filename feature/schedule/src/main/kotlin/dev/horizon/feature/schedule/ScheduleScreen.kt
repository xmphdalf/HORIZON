package dev.horizon.feature.schedule

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.*
import dev.horizon.core.design.*
import kotlin.math.*

@Composable
fun ScheduleScreen(enabled: Boolean, solar: Boolean, inverse: Boolean, times: Pair<String, String>, city: String, ramp: Int,
                   action: (String, Int) -> Unit, back: () -> Unit) {
    val title = when { inverse -> "Inverted · night shift"; solar -> "Follows the sun"; else -> "Fixed times" }
    Page("Schedule", title, back) {
        val adjust by rememberUpdatedState(action)
        val muted = Muted
        val ember = Ember
        val ground = Ground
        Canvas(Modifier.fillMaxWidth().aspectRatio(1.85f).pointerInput(solar, inverse) {
            var left = true
            var motion = 0f
            detectDragGestures(onDragStart = { left = it.x < size.width / 2; motion = 0f }) { change, delta ->
                change.consume(); motion += delta.x
                val steps = (motion / 10.dp.toPx()).toInt()
                if (steps != 0) { adjust(if (left) "offsetStart" else "offsetEnd", steps * 5); motion -= steps * 10.dp.toPx() }
            }
        }) {
            val radius = minOf(size.width * .43f, size.height * .85f)
            val origin = Offset(center.x - radius, size.height - radius * 1.12f)
            val bounds = Size(radius * 2, radius * 2)
            drawArc(muted.copy(alpha = .24f), 180f, 180f, false, origin, bounds, style = Stroke(2.dp.toPx()))
            fun progress(text: String, fallback: Float): Float {
                val parts = text.split(":")
                val minutes = (parts.getOrNull(0)?.toIntOrNull() ?: return fallback) * 60 + (parts.getOrNull(1)?.toIntOrNull() ?: return fallback)
                return ((minutes - (if (inverse) 300 else 1080) + 1440) % 1440 / 900f).coerceIn(0f, 1f)
            }
            val start = progress(times.first, .2f)
            val end = progress(times.second, .8f)
            drawArc(ember, 180 + start * 180, (end - start).coerceAtLeast(0f) * 180, false, origin, bounds, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
            listOf(start, end).forEach {
                val angle = Math.PI * (1 + it)
                val point = Offset(center.x + radius * cos(angle).toFloat(), origin.y + radius + radius * sin(angle).toFloat())
                drawCircle(ground, 9.dp.toPx(), point); drawCircle(ember, 9.dp.toPx(), point, style = Stroke(2.dp.toPx()))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Action(times.first + " · on", Modifier.weight(1f)) { action("start", 0) }
            Action(times.second + " · off", Modifier.weight(1f)) { action("end", 0) }
        }
        Copy(if (solar) "Drag an anchor along the arc to offset it from sunset or sunrise." else "Choose when your filter turns on and off each day.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Action("Sun-linked", Modifier.weight(1f), solar) { action("solar", 1) }
            Action("Fixed times", Modifier.weight(1f), !solar) { action("solar", 0) }
        }
        if (solar) Setting("Location", city) { action("city", 0) }
        Setting("Ramp over", "$ramp min") { action("ramp", 0) }
        Setting("Invert schedule", if (inverse) "On" else "Off") { action("invert", 0) }
        Action(if (enabled) "Schedule on" else "Enable schedule", Modifier.fillMaxWidth(), enabled) { action("schedule", 0) }
    }
}
