package dev.horizon.core.data

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

public data class SavedPreset(public val name: String, public val kelvin: Int, public val dim: Int, public val rgb: Int = -1)

public fun readPresets(prefs: SharedPreferences): List<SavedPreset> {
    val stored = prefs.getString("presets", null)
    if (stored != null) {
        val rows = JSONArray(stored)
        return List(rows.length()) { i ->
            val row = rows.getJSONObject(i)
            SavedPreset(row.getString("name"), row.getInt("k"), row.getInt("dim"), row.optInt("rgb", -1))
        }
    }
    return listOf(SavedPreset("Reading", 3400, 82), SavedPreset("Deep Night", 1700, 14),
        SavedPreset("Migraine", 1000, 8, 0xff2008), SavedPreset("Custom", prefs.getInt("custom_k", 2400), prefs.getInt("custom_dim", 55)))
}

public fun savePresets(prefs: SharedPreferences, presets: List<SavedPreset>) {
    val rows = JSONArray()
    presets.forEach { rows.put(JSONObject().put("name", it.name).put("k", it.kelvin).put("dim", it.dim).put("rgb", it.rgb)) }
    prefs.edit().putString("presets", rows.toString()).apply()
}
