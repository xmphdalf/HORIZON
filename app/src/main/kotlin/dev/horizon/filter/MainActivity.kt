package dev.horizon.filter

import android.Manifest
import android.app.AlertDialog
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
        if (Settings.canDrawOverlays(this) && Settings.System.canWrite(this)) maybeNotifyThenStart()
    }
    private val notifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        granting = false
        startFilter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kelvin = prefs.getInt("kelvin", 3400)
        dim = prefs.getInt("dim", 82)
        bound = bindService(Intent(this, FilterService::class.java), connection, Context.BIND_AUTO_CREATE)
        setContent {
            val active = service?.active?.collectAsState()?.value == true
            androidx.compose.runtime.LaunchedEffect(active) { if (active) starting = false }
            HomeScreen(active, kelvin, dim, ::change, ::toggle, ::customPreset)
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
        when {
            !Settings.canDrawOverlays(this) -> explain("Display over other apps",
                "Horizon uses a touch-through layer to tint your screen.", Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            !Settings.System.canWrite(this) -> explain("Modify system settings",
                "Horizon saves your brightness before dimming and restores it when you turn the filter off.",
                Settings.ACTION_MANAGE_WRITE_SETTINGS)
            else -> maybeNotifyThenStart()
        }
    }

    private fun explain(title: String, reason: String, action: String) {
        granting = true
        AlertDialog.Builder(this).setTitle(title).setMessage(reason)
            .setNegativeButton("Decline") { _, _ -> granting = false }
            .setOnCancelListener { granting = false }
            .setPositiveButton("Open settings") { _, _ ->
                grant.launch(Intent(action, "package:$packageName".toUri()))
            }.show()
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

    private fun customPreset() {
        val dialog = AlertDialog.Builder(this).setTitle("Custom preset")
            .setMessage("Save the current ${kelvin} K and $dim% brightness.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                prefs.edit { putInt("custom_k", kelvin).putInt("custom_dim", dim) }
            }
        if (prefs.contains("custom_k")) dialog.setNeutralButton("Apply saved") { _, _ ->
            change(prefs.getInt("custom_k", kelvin), prefs.getInt("custom_dim", dim), true)
        }
        dialog.show()
    }

    override fun onDestroy() {
        if (bound) unbindService(connection)
        super.onDestroy()
    }
}
