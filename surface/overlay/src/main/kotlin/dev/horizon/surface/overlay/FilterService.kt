package dev.horizon.surface.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.input.InputManager
import android.os.Binder
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Owns the single touch-through window independently of the Home activity. */
class FilterService : Service() {
    private val windows by lazy { requireNotNull(getSystemService(WindowManager::class.java)) }
    private val brightness by lazy { BrightnessSession(this) }
    private val running = MutableStateFlow(false)
    /** Changes only after the window is attached or removed. */
    val active = running.asStateFlow()
    private var layer: View? = null
    private var cap = 1f
    private val params = WindowManager.LayoutParams(
        -1, -1, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT,
    )

    /** Activity access is local to the application process. */
    inner class LocalBinder : Binder() {
        /** The binder and all callers share the Android main thread. */
        val service get() = this@FilterService
    }

    override fun onBind(intent: Intent) = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        if (Settings.System.canWrite(this)) brightness.restore()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == STOP || intent == null) {
            disable()
            return START_NOT_STICKY
        }
        startForeground(1, notification())
        if (!Settings.canDrawOverlays(this) || !Settings.System.canWrite(this)) {
            disable()
            return START_NOT_STICKY
        }
        try {
            if (brightness.capture()) {
                attach(intent.getIntExtra("kelvin", 3400), intent.getIntExtra("dim", 82))
            } else {
                disable()
            }
        } catch (_: SecurityException) {
            disable()
        } catch (_: WindowManager.BadTokenException) {
            disable()
        }
        return START_NOT_STICKY
    }

    private fun attach(kelvin: Int, dim: Int) {
        if (Build.VERSION.SDK_INT >= 31) cap = requireNotNull(getSystemService(InputManager::class.java)).maximumObscuringOpacityForTouch
        if (layer == null) {
            params.alpha = 0f
            val view = View(this)
            windows.addView(view, params)
            layer = view
        }
        running.value = true
        update(kelvin, dim, true)
    }

    /** Writes system brightness only on commit. Drag samples update the existing window. */
    fun update(kelvin: Int, dim: Int, commit: Boolean) {
        val view = layer ?: return
        val warmth = ((6500 - kelvin) / 5500f).coerceIn(0f, 1f)
        val darkness = 1f - dim.coerceIn(8, 100) / 100f
        val alpha = (0.38f * warmth + 0.40f * darkness).coerceAtMost(cap)
        val intensity = 1f - darkness * 0.86f
        view.setBackgroundColor(Color.rgb((255 * intensity).toInt(), ((210 - 118 * warmth) * intensity).toInt(), 0))
        try {
            if (alpha != params.alpha) {
                params.alpha = alpha
                windows.updateViewLayout(view, params)
            }
            if (commit && !brightness.apply(dim.coerceIn(8, 100))) disable()
        } catch (_: SecurityException) {
            disable()
        }
    }

    /** Removes the tint before restoring settings or dismissing the notification. */
    fun disable() {
        clear()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun clear() {
        layer?.let { if (it.isAttachedToWindow) windows.removeViewImmediate(it) }
        layer = null
        running.value = false
        if (Settings.System.canWrite(this)) brightness.restore()
    }

    override fun onDestroy() {
        clear()
        super.onDestroy()
    }

    private fun notification(): Notification {
        val channel = "filter"
        requireNotNull(getSystemService(NotificationManager::class.java))
            .createNotificationChannel(NotificationChannel(channel, "Filter controls", NotificationManager.IMPORTANCE_LOW))
        val stop = PendingIntent.getService(this, 0, Intent(this, FilterService::class.java).setAction(STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = Notification.Builder(this, channel).setSmallIcon(R.drawable.filter_status)
            .setContentTitle("Horizon").setContentText("Screen filter is on").setOngoing(true)
            .addAction(Notification.Action.Builder(null, "Turn off", stop).build())
        packageManager.getLaunchIntentForPackage(packageName)?.let {
            builder.setContentIntent(PendingIntent.getActivity(this, 1, it, PendingIntent.FLAG_IMMUTABLE))
        }
        return builder.build()
    }

    private companion object {
        const val STOP = "dev.horizon.filter.STOP"
    }
}
