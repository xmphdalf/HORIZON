package dev.horizon.feature.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import kotlinx.coroutines.delay

private val outfit = FontFamily(Font(dev.horizon.core.design.R.font.outfit))
private val mono = FontFamily(Font(dev.horizon.core.design.R.font.jetbrainsmono))
/** Home and its accessible controls share the same warmth and brightness values. */
@Composable
fun HomeScreen(active: Boolean, kelvin: Int, dim: Int, onChange: (Int, Int, Boolean) -> Unit,
               onToggle: () -> Unit, onCustom: () -> Unit,
               onSchedule: (() -> Unit)? = null, onSettings: (() -> Unit)? = null,
               onMission: (() -> Unit)? = null, missionSeen: Boolean = false,
               shortcuts: List<Triple<String, Int, Int>> = listOf(Triple("Reading", 3400, 82), Triple("Deep Night", 1700, 14)),
               onPreset: ((String) -> Unit)? = null) {
    val k = if (active) kelvin else 4600
    val colors = remember(k) { dev.horizon.core.design.paletteAt(k) }
    val ground = colors.ground
    val ink = colors.ink
    val accent = colors.accent
    var dragging by remember { mutableStateOf(false) }
    val update by rememberUpdatedState(onChange)
    val current by rememberUpdatedState(kelvin to dim)
    val haptic = LocalHapticFeedback.current
    Column(Modifier.fillMaxSize().background(ground).safeDrawingPadding().padding(horizontal = 22.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (onSchedule != null) Text("Schedule", Modifier.clickable(onClick = onSchedule).padding(12.dp), color = ink, fontFamily = outfit)
            if (onSettings != null) Text("Settings", Modifier.clickable(onClick = onSettings).padding(12.dp), color = ink, fontFamily = outfit)
        }
        Column(Modifier.weight(1f).fillMaxWidth().pointerInput(Unit) {
            var rawK = 0f
            var rawDim = 0f
            var detent = 0
            var lastK = current.first
            var lastDim = current.second
            fun finish() { dragging = false; update(lastK, lastDim, true) }
            detectDragGestures(onDragStart = {
                lastK = current.first; lastDim = current.second
                rawK = current.first.toFloat(); rawDim = current.second.toFloat(); dragging = true
            }, onDragEnd = { finish() }, onDragCancel = { finish() }) { change, delta ->
                change.consume()
                rawK = (rawK + delta.y / density * 4.5f / if (abs(delta.x) > abs(delta.y)) 1.4f else 1f)
                    .coerceIn(1600f, 4600f)
                rawDim = (rawDim + delta.x * 92 / size.width / if (abs(delta.y) > abs(delta.x)) 1.4f else 1f)
                    .coerceIn(8f, 100f)
                val snap = when { abs(rawK - 3400) < 60 -> 3400; abs(rawK - 1700) < 60 -> 1700; else -> 0 }
                if (snap != 0 && snap != detent) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                detent = snap
                lastK = if (snap > 0) snap else (rawK / 50).roundToInt() * 50
                lastDim = rawDim.roundToInt()
                update(lastK, lastDim, false)
            }
        }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Arc(k, active, ground, accent)
            Spacer(Modifier.height(36.dp))
            Readout(active, dragging, kelvin, dim, ink, onChange)
        }
        if (onMission != null) Text(if (missionSeen) "◯" else "Free forever. Here’s why →",
            Modifier.align(Alignment.CenterHorizontally).clickable(onClickLabel = "Our mission", onClick = onMission).padding(12.dp),
            color = ink.copy(alpha = .70f), fontFamily = outfit, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            shortcuts.take(2).forEach { (name, k, percent) ->
                Chip(name, accent, ink, Modifier.weight(1f), active && kelvin == k && dim == percent) {
                    if (onPreset == null) onChange(k, percent, true) else onPreset(name)
                }
            }
            Chip("+", accent, ink, Modifier.width(48.dp), false, onCustom)
        }
        Spacer(Modifier.height(16.dp))
        val shape = RoundedCornerShape(20.dp)
        Row(Modifier.fillMaxWidth().height(66.dp).clip(shape).background(accent.copy(alpha = .12f))
            .border(1.dp, accent.copy(alpha = .28f), shape).clickable(onClick = onToggle),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).border(2.dp, accent, CircleShape)
                .background(if (active) accent else Color.Transparent, CircleShape))
            Spacer(Modifier.width(12.dp))
            Text(if (active) "Filter on" else "Turn filter on", color = ink, fontFamily = outfit, fontSize = 16.sp)
        }
        Spacer(Modifier.height(34.dp))
    }
}

@Composable
private fun Chip(label: String, accent: Color, ink: Color, modifier: Modifier, selected: Boolean, click: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(modifier.height(48.dp).clip(shape).background(accent.copy(alpha = if (selected) .14f else 0f))
        .border(1.dp, accent.copy(alpha = if (selected) .32f else .16f), shape)
        .clickable(onClickLabel = if (label == "+") "Custom preset" else label, onClick = click),
        contentAlignment = Alignment.Center) { Text(label, color = ink, fontFamily = outfit, fontSize = 14.sp) }
}

@Composable
private fun Arc(k: Int, active: Boolean, ground: Color, accent: Color) {
    Canvas(Modifier.widthIn(max = 280.dp).fillMaxWidth().aspectRatio(300f / 170)) {
        val u = size.width / 300
        val bounds = Size(260 * u, 260 * u)
        val start = Offset(20 * u, 20 * u)
        val fraction = ((4600 - k) / 3000f).coerceIn(0f, 1f)
        drawArc(accent.copy(alpha = .24f), 180f, 180f, false, start, bounds, style = Stroke(2 * u, cap = StrokeCap.Round))
        drawArc(accent, 180f, 180 * fraction, false, start, bounds, style = Stroke(4 * u, cap = StrokeCap.Round))
        val angle = Math.PI * (1 + fraction)
        val knob = Offset((150 + 130 * cos(angle)).toFloat() * u, (150 + 130 * sin(angle)).toFloat() * u)
        drawCircle(if (active) ground else accent, 6 * u, knob)
        if (active) drawCircle(accent, 6 * u, knob, style = Stroke(2.5f * u))
    }
}

@Composable
private fun Readout(active: Boolean, dragging: Boolean, k: Int, dim: Int, ink: Color, update: (Int, Int, Boolean) -> Unit) {
    var faded by remember { mutableStateOf(false) }
    LaunchedEffect(k, dim, dragging, active) { faded = false; if (active && !dragging) { delay(500); faded = true } }
    val alpha by animateFloatAsState(if (faded) .22f else 1f, tween(600), label = "Readout")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val value = buildAnnotatedString {
            if (active) {
                append(k.toString())
                withStyle(SpanStyle(fontSize = 26.sp, color = ink.copy(alpha = .55f))) { append(" K") }
            } else append("Off")
        }
        Text(value, Modifier.graphicsLayer { this.alpha = alpha }.semantics {
            contentDescription = "Warmth"; progressBarRangeInfo = ProgressBarRangeInfo(k.toFloat(), 1600f..4600f)
            setProgress { update((it / 50).roundToInt().coerceIn(32, 92) * 50, dim, true); true }
        }, color = ink, fontFamily = outfit, fontWeight = FontWeight.ExtraLight, letterSpacing = (-3).sp, maxLines = 1, fontSize = if (active) 96.sp else 68.sp)
        Text(if (active) "$dim % BRIGHTNESS" else "Drag up to warm the screen. Drag left to dim past the system minimum.",
            Modifier.widthIn(max = 230.dp).semantics {
                contentDescription = "Brightness"; progressBarRangeInfo = ProgressBarRangeInfo(dim.toFloat(), 8f..100f)
                setProgress { update(k, it.roundToInt().coerceIn(8, 100), true); true }
            }, color = ink.copy(alpha = .75f), fontFamily = if (active) mono else outfit,
            fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

@Preview
@Composable
private fun OffPreview() = HomeScreen(false, 3400, 82, { _, _, _ -> }, {}, {})
@Preview
@Composable
private fun ReadingPreview() = HomeScreen(true, 3400, 82, { _, _, _ -> }, {}, {})
@Preview
@Composable
private fun NightPreview() = HomeScreen(true, 1700, 14, { _, _, _ -> }, {}, {})
@Preview(fontScale = 2f)
@Composable
private fun LargePreview() = HomeScreen(true, 3400, 82, { _, _, _ -> }, {}, {})
