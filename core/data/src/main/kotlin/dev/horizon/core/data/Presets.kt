package dev.horizon.core.data

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

public data class SavedPreset(public val name: String, public val kelvin: Int, public val dim: Int, public val rgb: Int = -1)

private fun refreshedPreset(preset: SavedPreset): SavedPreset = when (preset) {
    SavedPreset("Reading", 3400, 82) -> preset.copy(dim = 92)
    SavedPreset("Deep Night", 1700, 14) -> preset.copy(dim = 55)
    SavedPreset("Migraine", 1000, 8, 0xff2008) -> preset.copy(dim = 48)
    else -> preset
}

public fun readPresets(prefs: SharedPreferences): List<SavedPreset> {
    val stored = prefs.getString("presets", null)
    if (stored != null) {
        val rows = JSONArray(stored)
        return List(rows.length()) { i ->
            val row = rows.getJSONObject(i)
            SavedPreset(row.getString("name"), row.getInt("k"), row.getInt("dim"), row.optInt("rgb", -1)).let { if (prefs.getInt("preset_version", 1) < 2) refreshedPreset(it) else it }
        }
    }
    return listOf(SavedPreset("Reading", 3400, 82), SavedPreset("Deep Night", 1700, 14),
        SavedPreset("Migraine", 1000, 8, 0xff2008), SavedPreset("Custom", prefs.getInt("custom_k", 2400), prefs.getInt("custom_dim", 75))).map(::refreshedPreset)
}

public fun savePresets(prefs: SharedPreferences, presets: List<SavedPreset>) {
    val rows = JSONArray()
    presets.forEach { rows.put(JSONObject().put("name", it.name).put("k", it.kelvin).put("dim", it.dim).put("rgb", it.rgb)) }
    prefs.edit().putInt("preset_version", 2).putString("presets", rows.toString()).apply()
}
