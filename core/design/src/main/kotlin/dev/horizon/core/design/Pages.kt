package dev.horizon.core.design

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlin.math.*
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*

private val grounds = listOf(Color(0xff08090b), Color(0xff100c09), Color(0xff140b05), Color(0xff170a02))
private val accents = listOf(Color(0xff6e7c8c), Color(0xfff2a65a), Color(0xffde8f41), Color(0xffc97a34))
private val inks = listOf(Color(0xffe9ecee), Color(0xffe5d2bc), Color(0xffdec198), Color(0xffd3a876))

private fun ramp(samples: List<Color>, k: Int): Color {
    val stops = intArrayOf(4600, 3400, 2400, 1700)
    val i = when { k >= 3400 -> 0; k >= 2400 -> 1; else -> 2 }
    val t = ((stops[i] - k).toFloat() / (stops[i] - stops[i + 1])).coerceIn(0f, 1f)
    if (t == 0f) return samples[i]
    if (t == 1f) return samples[i + 1]
    val a = samples[i].convert(ColorSpaces.Oklab)
    val b = samples[i + 1].convert(ColorSpaces.Oklab)
    val h = atan2(a.blue, a.green)
    val delta = atan2(b.blue, b.green) - h
    val angle = h + t * atan2(sin(delta), cos(delta))
    val c = hypot(a.green, a.blue) * (1 - t) + hypot(b.green, b.blue) * t
    return Color(a.red + (b.red - a.red) * t, c * cos(angle), c * sin(angle), 1f, ColorSpaces.Oklab)
        .convert(ColorSpaces.Srgb)
}

public data class HorizonColors(public val ground: Color, public val surface: Color, public val ink: Color, public val muted: Color, public val accent: Color)
private val palette = staticCompositionLocalOf { paletteAt(3400) }

public fun paletteAt(kelvin: Int): HorizonColors = HorizonColors(
    ramp(grounds, kelvin), ramp(listOf(Color(0xff101216), Color(0xff191310), Color(0xff1e120a), Color(0xff231004)), kelvin),
    ramp(inks, kelvin), ramp(inks, kelvin).copy(alpha = .74f), ramp(accents, kelvin),
)

@Composable
public fun HorizonTheme(kelvin: Int, content: @Composable () -> Unit) {
    val colors = remember(kelvin) { paletteAt(kelvin) }
    CompositionLocalProvider(palette provides colors) {
        MaterialTheme(colorScheme = darkColorScheme(primary = colors.accent, surface = colors.surface,
            onSurface = colors.ink, onSurfaceVariant = colors.muted, background = colors.ground), content = content)
    }
}

public val Ground: Color @Composable get() = palette.current.ground
public val Surface: Color @Composable get() = palette.current.surface
public val Ink: Color @Composable get() = palette.current.ink
public val Muted: Color @Composable get() = palette.current.muted
public val Ember: Color @Composable get() = palette.current.accent
public val Outfit: FontFamily = FontFamily(Font(R.font.outfit))
public val Mono: FontFamily = FontFamily(Font(R.font.jetbrainsmono))

@Composable
public fun Page(section: String, title: String, back: () -> Unit, body: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Ground).safeDrawingPadding()
        .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("‹", Modifier.clickable(onClickLabel = "Back", onClick = back).padding(10.dp), color = Muted, fontSize = 26.sp)
            Text(section.uppercase(), color = Muted, fontFamily = Mono, fontSize = 11.sp, letterSpacing = 2.sp)
        }
        Text(title, color = Ink, fontFamily = Outfit, fontWeight = FontWeight.Light, fontSize = 30.sp)
        body()
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
public fun Copy(text: String) {
    Text(text, color = Muted, fontFamily = Outfit, fontSize = 15.sp, lineHeight = 22.sp)
}

@Composable
public fun Action(text: String, modifier: Modifier = Modifier, chosen: Boolean = false, enabled: Boolean = true, click: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(modifier.defaultMinSize(minHeight = 50.dp).clip(shape).background(if (chosen) Ember.copy(alpha = .13f) else Surface)
        .border(1.dp, Ember.copy(alpha = if (chosen) .40f else .14f), shape)
        .clickable(enabled = enabled, onClick = click).padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(text, color = if (!enabled) Muted.copy(alpha = .45f) else if (chosen) Ember else Ink, fontFamily = Outfit, fontSize = 15.sp)
    }
}

@Composable
public fun Setting(label: String, value: String, click: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = click).heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = Ink, fontFamily = Outfit, fontSize = 16.sp)
        Text(value, color = Ember, fontFamily = Mono, fontSize = 13.sp)
    }
    HorizontalDivider(color = Ember.copy(alpha = .12f))
}
