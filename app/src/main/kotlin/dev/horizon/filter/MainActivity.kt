package dev.horizon.filter

import android.Manifest
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Process
import android.widget.EditText
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import dev.horizon.core.data.*
import dev.horizon.feature.schedule.ScheduleScreen
import dev.horizon.feature.presets.PresetsScreen
import dev.horizon.feature.settings.AdvancedScreen
import dev.horizon.feature.permission.PermissionScreen
import dev.horizon.feature.mission.ScienceScreen
import dev.horizon.feature.mission.MissionCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.horizon.feature.home.HomeScreen
import dev.horizon.surface.overlay.FilterService

/** Hosts the manual controls and requests special access only after a user starts the filter. */
class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("manual_filter", Context.MODE_PRIVATE) }
    private var service by mutableStateOf<FilterService?>(null)
    private var kelvin by mutableIntStateOf(3400)
    private var dim by mutableIntStateOf(82)
    private var starting = false
    private var granting = false
    private var showTips by mutableStateOf(false)
    private val tipsDelegate = lazy { PlayTips(this) { visible -> service?.checkoutVisible(visible) } }
    private val tips by tipsDelegate
    private var permissionOrigin = "Home"
    private var page by mutableStateOf("Home")
    private var revision by mutableIntStateOf(0)
    private val schedule by lazy { Schedule(this) }
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "kelvin") kelvin = prefs.getInt("kelvin", 3400)
        if (key == "dim") dim = prefs.getInt("dim", 82)
        revision++
    }
    private val alarmGrant = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (schedule.allowed()) setSchedule(true)
    }
    private val locate = registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) locateCity()
    }

    private var bound = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            service = (binder as FilterService.LocalBinder).service
        }
        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            starting = false
        }
    }
    private val grant = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        granting = false
        if (canStart()) {
            page = permissionOrigin
            if (page == "Schedule") scheduleAction("schedule", 0) else if (page == "Home") maybeNotifyThenStart()
        } else page = "Permissions"
    }
    private val notifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        granting = false
        startFilter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kelvin = prefs.getInt("kelvin", 3400)
        dim = prefs.getInt("dim", 82)
        if (intent.action == FILTER_OFF) { control(this, FILTER_OFF); finish(); return }
        if (intent.action == "android.service.quicksettings.action.QS_TILE_PREFERENCES") {
            val presets = readPresets(prefs)
            val recent = org.json.JSONArray(prefs.getString("recent_presets", "[]"))
            val names = (List(recent.length()) { recent.getString(it) } + presets.map { it.name }).distinct()
            val cycle = names.mapNotNull { name -> presets.find { it.name == name } }.take(3)
            if (cycle.isNotEmpty() && canStart()) {
                val index = cycle.indexOfFirst { it.kelvin == kelvin && it.dim == dim }
                val next = cycle[(index + 1) % cycle.size]
                prefs.edit { putInt("kelvin", next.kelvin); putInt("dim", next.dim); putInt("rgb", next.rgb) }
                control(this, "APPLY")
                finish(); return
            }
        }
        page = savedInstanceState?.getString("page") ?: "Home"
        permissionOrigin = savedInstanceState?.getString("permissionOrigin") ?: "Home"
        showTips = savedInstanceState?.getBoolean("tips") ?: false
        prefs.registerOnSharedPreferenceChangeListener(listener)
        bound = bindService(Intent(this, FilterService::class.java), connection, Context.BIND_AUTO_CREATE)
        setContent {
            val active = service?.active?.collectAsState()?.value == true
            androidx.compose.runtime.LaunchedEffect(active) { if (active) starting = false }
            BackHandler(page != "Home") { back() }
            val rows = remember(revision) { readPresets(prefs) }
            dev.horizon.core.design.HorizonTheme(if (active) kelvin else 4600) {
            when (page) {
                "Schedule" -> ScheduleScreen(prefs.getBoolean("schedule", false) && schedule.allowed(), prefs.getBoolean("solar", false),
                    prefs.getBoolean("invert", false), timeLabel(true) to timeLabel(false),
                    prefs.getString("city", "Gandhinagar").orEmpty(), prefs.getInt("ramp", 0), schedule.selectedDays(), ::scheduleAction, ::back)
                "Presets" -> PresetsScreen(rows, SavedPreset("", kelvin, dim, prefs.getInt("rgb", -1)),
                    { savePresets(prefs, it) }, ::applyPreset, ::back)
                "Settings" -> AdvancedScreen(prefs.getBoolean("deep", true), prefs.getBoolean("restore", false),
                    prefs.getInt("rgb", -1), prefs.getStringSet("excluded", emptySet()).orEmpty().map { it to appLabel(it) },
                    hasUsageAccess(), ::advancedAction, { id -> exclusion(id, false) }, ::back)
                "Permissions" -> PermissionScreen(Settings.canDrawOverlays(this), Settings.System.canWrite(this), ::requestSpecial, ::back)
                "Science" -> ScienceScreen(::back)
                else -> HomeScreen(active, kelvin, dim, ::change, ::toggle, { page = "Presets" },
                    { page = "Schedule" }, { page = "Settings" }, {
                        page = "Mission"; prefs.edit { putBoolean("mission_seen", true) }
                    }, prefs.getBoolean("mission_seen", false), rows.take(2).map { Triple(it.name, it.kelvin, it.dim) },
                    { name -> rows.firstOrNull { it.name == name }?.let(::applyPreset) },
                    missionCard = if (page == "Mission") { { MissionCard(::back) { showTips = true; tips.open() } } } else null)
            }
            if (showTips) dev.horizon.feature.mission.TipSheet(tips.prices, tips.message, tips.busy, tips.received,
                tips::purchase, tips::open) { showTips = false; page = "Home" }
            }
        }
    }

    private fun change(k: Int, percent: Int, commit: Boolean) {
        kelvin = k
        dim = percent
        if (commit) prefs.edit { putInt("kelvin", k).putInt("dim", percent) }
        if (service?.active?.value == true) service?.update(k, percent, commit)
        else if (!starting && (commit || canStart())) enable()
    }

    private fun toggle() {
        if (service?.active?.value == true) service?.disable() else enable()
    }

    private fun canStart() = Settings.canDrawOverlays(this) && Settings.System.canWrite(this)

    private fun enable() {
        if (granting || starting) return
        if (canStart()) maybeNotifyThenStart() else openPermissions()
    }

    private fun openPermissions() { permissionOrigin = page; page = "Permissions" }

    private fun requestSpecial() {
        val action = when {
            !Settings.canDrawOverlays(this) -> Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            !Settings.System.canWrite(this) -> Settings.ACTION_MANAGE_WRITE_SETTINGS
            else -> { back(); return }
        }
        granting = true
        grant.launch(Intent(action, "package:$packageName".toUri()))
    }

    private fun maybeNotifyThenStart() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED && !prefs.getBoolean("asked_notifications", false)
        ) {
            granting = true
            prefs.edit { putBoolean("asked_notifications", true) }
            notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else startFilter()
    }

    private fun startFilter() {
        if (!canStart()) return
        starting = true
        startForegroundService(Intent(this, FilterService::class.java).putExtra("kelvin", kelvin).putExtra("dim", dim))
        // Permit a retry if Android revoked a grant while starting the service.
        window.decorView.postDelayed({ starting = false }, 1000)
    }

    private fun back() { page = when (page) { "Science" -> "Settings"; "Permissions" -> permissionOrigin; else -> "Home" } }

    private fun applyPreset(preset: SavedPreset) {
        val recent = org.json.JSONArray(prefs.getString("recent_presets", "[]"))
        val names = listOf(preset.name) + List(recent.length()) { recent.getString(it) }
        prefs.edit {
            putInt("rgb", preset.rgb)
            putString("recent_presets", org.json.JSONArray(names.distinct().take(3)).toString())
        }
        page = "Home"
        change(preset.kelvin, preset.dim, true)
    }

    private fun timeLabel(start: Boolean): String = schedule.event(LocalDate.now(), start)
        ?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "No sun event"

    private fun setSchedule(enabled: Boolean) {
        prefs.edit { putBoolean("schedule", enabled) }
        schedule.arm()
    }

    private fun setNumber(key: String, value: Int) {
        prefs.edit { putInt(key, value) }
        schedule.arm()
    }

    private fun scheduleAction(action: String, value: Int) {
        val solar = prefs.getBoolean("solar", false)
        val invert = prefs.getBoolean("invert", false)
        val evening = (action == "start" || action == "offsetStart") != invert
        val key = if (solar) { if (evening) "sunset_offset" else "sunrise_offset" }
            else if (evening) "evening" else "morning"
        when (action) {
            "solar", "invert" -> {
                prefs.edit { putBoolean(action, if (action == "solar") value == 1 else !invert) }; schedule.arm()
            }
            "ramp" -> {
                val minutes = intArrayOf(0, 5, 15, 20, 30, 45, 60)
                AlertDialog.Builder(this).setTitle("Ramp over").setItems(minutes.map { "$it min" }.toTypedArray()) { _, i ->
                    setNumber("ramp", minutes[i])
                }.show()
            }
            "schedule" -> when {
                prefs.getBoolean("schedule", false) && schedule.allowed() -> setSchedule(false)
                !canStart() -> openPermissions()
                Build.VERSION.SDK_INT >= 31 && !schedule.allowed() -> alarmGrant.launch(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, "package:$packageName".toUri()))
                else -> setSchedule(true)
            }
            "start", "end" -> if (solar) {
                val offsets = (-12..12).map { it * 5 }
                AlertDialog.Builder(this).setTitle("Solar offset").setItems(offsets.map { "$it min" }.toTypedArray()) { _, i ->
                    setNumber(key, offsets[i])
                }.show()
            } else {
                val v = prefs.getInt(key, if (evening) 1320 else 450)
                TimePickerDialog(this, { _, hour, minute -> setNumber(key, hour * 60 + minute) }, v / 60, v % 60, true).show()
            }
            "offsetStart", "offsetEnd" -> {
                val v = prefs.getInt(key, if (solar) 0 else if (evening) 1320 else 450) + value
                setNumber(key, if (solar) v.coerceIn(-120, 120) else Math.floorMod(v, 1440))
            }
            "days" -> {
                val days = schedule.selectedDays().toMutableSet()
                val labels = java.time.DayOfWeek.values().map { it.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()) }
                AlertDialog.Builder(this).setTitle("Days after shift").setMultiChoiceItems(labels.toTypedArray(),
                    BooleanArray(7) { it + 1 in days }) { _, i, checked -> if (checked) days.add(i + 1) else days.remove(i + 1) }
                    .setNegativeButton("Cancel", null).setPositiveButton("Save") { _, _ ->
                        prefs.edit { putStringSet("days", days.map(Int::toString).toSet()) }
                        schedule.arm()
                    }.show()
            }
            "city" -> chooseCity()
        }
    }

    private fun chooseCity() {
        val options = cities.keys.toList() + listOf("Use approximate location", "Enter latitude, longitude")
        AlertDialog.Builder(this).setTitle("Sunrise and sunset").setItems(options.toTypedArray()) { _, i ->
            when (i) {
                cities.size -> locate.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                cities.size + 1 -> {
                    val field = EditText(this).apply { hint = "23.22, 72.64" }
                    val dialog = AlertDialog.Builder(this).setTitle("Coordinates").setView(field)
                        .setNegativeButton("Cancel", null).setPositiveButton("Save", null).create()
                    dialog.show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val v = field.text.toString()
                        if (coords(v) == null) field.error = "Enter latitude, longitude"
                        else { saveCity(v); dialog.dismiss() }
                    }
                }
                else -> saveCity(options[i])
            }
        }.show()
    }

    private fun saveCity(city: String) {
        prefs.edit { putString("city", city) }; schedule.arm()
    }

    private fun locateCity() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        val manager = requireNotNull(getSystemService(LocationManager::class.java))
        val location = manager.getProviders(true).mapNotNull { provider ->
            try { manager.getLastKnownLocation(provider) } catch (_: SecurityException) { null }
        }.maxByOrNull { it.time }
        if (location == null) {
            AlertDialog.Builder(this).setMessage("No recent location is available. Choose a city or enter coordinates.")
                .setPositiveButton("Choose city") { _, _ -> chooseCity() }.setNegativeButton("Cancel", null).show()
        } else {
            val nearest = cities.minBy { (_, point) ->
                val distance = FloatArray(1)
                Location.distanceBetween(location.latitude, location.longitude, point.first, point.second, distance)
                distance[0]
            }.key
            AlertDialog.Builder(this).setTitle("Use $nearest?")
                .setMessage("This is the nearest bundled city. Horizon saves only the city name and computes each sunrise and sunset offline.")
                .setPositiveButton("Use city") { _, _ -> saveCity(nearest) }
                .setNegativeButton("Choose another") { _, _ -> chooseCity() }.show()
        }
    }

    private fun advancedAction(action: String, value: Int) {
        when (action) {
            "deep", "restore" -> prefs.edit { putBoolean(action, !prefs.getBoolean(action, action == "deep")) }
            "rgb" -> prefs.edit { putInt("rgb", value) }
            "science" -> page = "Science"
            "permission" -> openPermissions()
            "apps" -> if (hasUsageAccess()) chooseApp() else startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        if (service?.active?.value == true && action in setOf("deep", "rgb")) service?.update(kelvin, dim, true)
    }

    @Suppress("DEPRECATION")
    private fun hasUsageAccess(): Boolean {
        val ops = requireNotNull(getSystemService(AppOpsManager::class.java))
        val mode = ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    @Suppress("DEPRECATION")
    private fun appLabel(id: String) = try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(id, 0)).toString()
    } catch (_: android.content.pm.PackageManager.NameNotFoundException) { id }

    @Suppress("DEPRECATION")
    private fun chooseApp() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = packageManager.queryIntentActivities(intent, 0).map { it.activityInfo.packageName }.distinct()
            .filter { it != packageName }.map { it to appLabel(it) }.sortedBy { it.second.lowercase() }
        AlertDialog.Builder(this).setTitle("Turn filter off in").setItems(apps.map { it.second }.toTypedArray()) { _, i ->
            exclusion(apps[i].first, true)
        }.show()
    }

    private fun exclusion(id: String, add: Boolean) {
        val current = prefs.getStringSet("excluded", emptySet()).orEmpty()
        prefs.edit { putStringSet("excluded", if (add) current + id else current - id) }
        service?.rulesChanged()
    }

    override fun onResume() { super.onResume(); revision++
        if (tipsDelegate.isInitialized() || showTips || getSharedPreferences("play_support", MODE_PRIVATE).getBoolean("outstanding", false)) tips.resume(showTips) }
    override fun onSaveInstanceState(outState: Bundle) { super.onSaveInstanceState(outState); outState.putString("page", page); outState.putBoolean("tips", showTips); outState.putString("permissionOrigin", permissionOrigin) }

    override fun onDestroy() {
        if (tipsDelegate.isInitialized()) tips.close()
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
        if (bound) unbindService(connection)
        super.onDestroy()
    }
}
