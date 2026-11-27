package dev.horizon.core.design

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*

public val Ground: Color = Color(0xff100c09)
public val Surface: Color = Color(0xff191310)
public val Ink: Color = Color(0xffe5d2bc)
public val Muted: Color = Color(0xffbfaa92)
public val Ember: Color = Color(0xfff2a65a)
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
public fun Action(text: String, modifier: Modifier = Modifier, chosen: Boolean = false, click: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(modifier.defaultMinSize(minHeight = 50.dp).clip(shape).background(if (chosen) Ember.copy(alpha = .13f) else Surface)
        .border(1.dp, Ember.copy(alpha = if (chosen) .40f else .14f), shape)
        .clickable(onClick = click).padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(text, color = if (chosen) Ember else Ink, fontFamily = Outfit, fontSize = 15.sp)
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
