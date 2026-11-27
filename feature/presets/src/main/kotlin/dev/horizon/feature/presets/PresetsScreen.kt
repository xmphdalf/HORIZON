package dev.horizon.feature.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import dev.horizon.core.data.SavedPreset
import dev.horizon.core.design.*
import kotlinx.coroutines.delay

@Composable
fun PresetsScreen(rows: List<SavedPreset>, current: SavedPreset, save: (List<SavedPreset>) -> Unit,
                  apply: (SavedPreset) -> Unit, back: () -> Unit) {
    var edit by rememberSaveable { mutableIntStateOf(-1) }
    var name by rememberSaveable { mutableStateOf("") }
    var deleted by remember { mutableStateOf<Pair<Int, SavedPreset>?>(null) }
    LaunchedEffect(deleted) { if (deleted != null) { delay(8000); deleted = null } }
    androidx.activity.compose.BackHandler(edit >= 0) { edit = -1 }
    if (edit >= 0) {
        val preset = rows.getOrNull(edit) ?: current
        Page("Presets", if (edit < rows.size) "Rename preset" else "Save current as preset", { edit = -1 }) {
            OutlinedTextField(name, { name = it.take(32) }, label = { Text("Preset name") }, singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Ink, unfocusedTextColor = Ink))
            Copy("${preset.kelvin} K · ${preset.dim}% brightness")
            if (rows.withIndex().any { it.index != edit && it.value.name.equals(name.trim(), true) }) Copy("Choose a unique name.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (edit < rows.size) Action("Delete", Modifier.weight(1f)) {
                    deleted = edit to preset; save(rows.filterIndexed { i, _ -> i != edit }); edit = -1
                }
                Action("Save", Modifier.weight(1f), true) {
                    val label = name.trim()
                    if (label.isNotEmpty() && rows.withIndex().none { it.index != edit && it.value.name.equals(label, true) }) {
                        val list = rows.toMutableList()
                        if (edit < list.size) list[edit] = preset.copy(name = label) else list.add(preset.copy(name = label))
                        save(list); edit = -1
                    }
                }
            }
        }
    } else Page("Presets", if (rows.size == 4) "Four ways to look" else "Your presets", back) {
        rows.forEachIndexed { i, p ->
            Row(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(16.dp)).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).clickable { apply(p) }.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).background(if (p.rgb >= 0) Color(p.rgb or 0xff000000.toInt()) else Ember.copy(alpha = .28f),
                        RoundedCornerShape(if (p.name == "Migraine") 4.dp else 10.dp)), contentAlignment = Alignment.Center) {
                        Text(p.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2), color = Ink, fontFamily = Mono)
                    }
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(p.name, color = Ink, fontFamily = Outfit, fontSize = 19.sp)
                        Copy("${p.kelvin} K · ${p.dim}%")
                    }
                }
                Action("Edit") { name = p.name; edit = i }
            }
        }
        Action("+  Save current as preset", Modifier.fillMaxWidth()) { name = ""; edit = rows.size }
        deleted?.let { (i, p) ->
            Action("Undo delete · ${p.name}", Modifier.fillMaxWidth()) {
                if (rows.none { it.name == p.name }) save(rows.toMutableList().apply { add(i.coerceAtMost(size), p) })
                deleted = null
            }
        }
    }
}
