package dev.horizon.surface.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.input.InputManager
import android.os.Binder
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews
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
    private val prefs by lazy { getSharedPreferences("manual_filter", Context.MODE_PRIVATE) }
    private val handler = Handler(Looper.getMainLooper())
    private var undoView: View? = null
    private val finishUndo = Runnable { dismissUndo(); if (!running.value) stopSelf() }
    private var kelvinValue = 3400
    private var dimValue = 82
    private var inCheckout = false
    private var pausedUntil = 0L
    private var rampStart = 0L
    private var rampDuration = 0L
    private var foreground = ""
    private var lastQuery = 0L
    private var cap = 1f
    private val tick = Runnable { reconcile() }

    /** Reevaluate an exclusion immediately when the user edits Settings. */
    fun rulesChanged() { if (running.value) reconcile() }

    /** Hide the overlay and return physical brightness while Google Play owns checkout. */
    fun checkoutVisible(visible: Boolean) {
        inCheckout = visible
        rulesChanged()
    }

    private fun reconcile() {
        val excluded = prefs.getStringSet("excluded", emptySet()).orEmpty()
        val now = System.currentTimeMillis()
        if (excluded.isNotEmpty() && requireNotNull(getSystemService(PowerManager::class.java)).isInteractive) {
            val events = requireNotNull(getSystemService(UsageStatsManager::class.java))
                .queryEvents(if (lastQuery == 0L) now - 60000L else lastQuery, now)
            lastQuery = now
            if (events != null) {
                val event = UsageEvents.Event()
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == 1) foreground = event.packageName
                }
            }
        }
        val hidden = inCheckout || pausedUntil > now || foreground in excluded
        val view = layer ?: return
        if (hidden && view.visibility == View.VISIBLE) {
            view.visibility = View.INVISIBLE
            if (Settings.System.canWrite(this)) brightness.restore()
        } else if (!hidden) {
            val returning = view.visibility != View.VISIBLE
            if (returning) { if (!brightness.capture()) { disable(); return }; view.visibility = View.VISIBLE }
            if (returning || rampDuration > 0) update(kelvinValue, dimValue, true)
        }
        handler.removeCallbacks(tick)
        val next = when {
            excluded.isNotEmpty() -> 2000L
            rampDuration > 0 -> 15000L
            pausedUntil > now -> pausedUntil - now
            else -> return
        }
        handler.postDelayed(tick, next)
    }

    private fun publish() {
        prefs.edit().putBoolean("filter_running", running.value).apply()
        sendBroadcast(Intent().setClassName(packageName, "dev.horizon.filter.ControlsChanged"))
    }

    private val params = WindowManager.LayoutParams(
        -1, -1, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Fill the display behind transparent bars, including the camera cutout.
            setFitInsetsTypes(0)
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
    }

    /** Activity access is local to the application process. */
    inner class LocalBinder : Binder() {
        /** The binder and all callers share the Android main thread. */
        val service get() = this@FilterService
    }

    override fun onBind(intent: Intent) = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        if (prefs.getBoolean("filter_running", false)) publish()
        if (Settings.System.canWrite(this)) brightness.restore()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == STOP && running.value) {
            clear()
            stopForeground(STOP_FOREGROUND_REMOVE)
            offerUndo()
            return START_NOT_STICKY
        }
        dismissUndo()
        if (intent?.action == STOP || intent?.action == "SCHEDULE_END" || intent == null) {
            disable()
            return START_NOT_STICKY
        }
        kelvinValue = intent.getIntExtra("kelvin", prefs.getInt("kelvin", 3400))
        dimValue = intent.getIntExtra("dim", prefs.getInt("dim", 82))
        when (intent.action) {
            "LESS" -> dimValue = (dimValue - 10).coerceAtLeast(8)
            "MORE" -> dimValue = (dimValue + 10).coerceAtMost(100)
            "PAUSE" -> pausedUntil = System.currentTimeMillis() + 3600000L
            else -> pausedUntil = 0
        }
        rampDuration = intent.getIntExtra("ramp", 0).toLong() * 60000
        rampStart = SystemClock.elapsedRealtime()
        startForeground(1, notification())
        if (!Settings.canDrawOverlays(this) || !Settings.System.canWrite(this)) {
            disable()
            return START_NOT_STICKY
        }
        try {
            if (brightness.capture()) {
                attach(kelvinValue, dimValue)
                if (running.value) reconcile()
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
        kelvinValue = kelvin
        dimValue = dim
        if (commit) prefs.edit().putInt("kelvin", kelvin).putInt("dim", dim).apply()
        val progress = if (rampDuration == 0L) 1f else ((SystemClock.elapsedRealtime() - rampStart).toFloat() / rampDuration).coerceIn(0f, 1f)
        if (progress >= 1f) rampDuration = 0L
        val warmth = ((6500 - kelvin) / 5500f).coerceIn(0f, 1f)
        val darkness = 1f - dim.coerceIn(8, 100) / 100f
        val alpha = (0.38f * warmth + 0.40f * darkness) .times(progress).coerceAtMost(cap)
        val intensity = 1f - darkness * 0.86f
        val rgb = prefs.getInt("rgb", -1)
        val base = if (rgb == -1) Color.rgb(255, (210 - 118 * warmth).toInt(), 0) else rgb
        view.setBackgroundColor(Color.rgb((Color.red(base) * intensity).toInt(), (Color.green(base) * intensity).toInt(),
            (Color.blue(base) * intensity).toInt()))
        try {
            if (alpha != params.alpha) {
                params.alpha = alpha
                windows.updateViewLayout(view, params)
            }
            if (commit && view.visibility == View.VISIBLE) {
                if (prefs.getBoolean("deep", true)) {
                    if (!brightness.capture() || !brightness.apply((100 + (dim - 100) * progress).toInt().coerceIn(8, 100))) { disable(); return }
                } else brightness.restore()
                requireNotNull(getSystemService(NotificationManager::class.java)).notify(1, notification())
                publish()
            }
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

    private fun dismissUndo() {
        handler.removeCallbacks(finishUndo)
        undoView?.let { if (it.isAttachedToWindow) windows.removeViewImmediate(it) }
        undoView = null
    }

    private fun offerUndo() {
        val dp = resources.displayMetrics.density
        val strip = android.widget.TextView(this).apply {
            setText(R.string.undo_off)
            setTextColor(Color.rgb(25, 20, 16))
            gravity = android.view.Gravity.CENTER
            setPadding((24 * dp).toInt(), (12 * dp).toInt(), (24 * dp).toInt(), (12 * dp).toInt())
            setBackgroundResource(R.drawable.escape_key)
            setOnClickListener {
                dismissUndo()
                startForegroundService(Intent(this@FilterService, FilterService::class.java))
            }
        }
        val layout = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL; y = (64 * dp).toInt() }
        try {
            windows.addView(strip, layout)
            undoView = strip
            handler.postDelayed(finishUndo, 8000)
        } catch (_: SecurityException) { stopSelf() } catch (_: WindowManager.BadTokenException) { stopSelf() }
    }

    private fun clear() {
        dismissUndo()
        handler.removeCallbacks(tick)
        pausedUntil = 0
        rampDuration = 0
        layer?.let { if (it.isAttachedToWindow) windows.removeViewImmediate(it) }
        layer = null
        running.value = false
        publish()
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
            .setContentTitle("Filter on · ${kelvinValue}K · ${dimValue}%")
            .setContentText(if (pausedUntil > System.currentTimeMillis()) "Paused for one hour" else "Horizon")
            .setOnlyAlertOnce(true).setOngoing(true)
            .addAction(Notification.Action.Builder(null, "Turn off", stop).build())
        val controls = RemoteViews(packageName, R.layout.filter_controls)
        controls.setTextViewText(R.id.status, "${kelvinValue} K · ${dimValue}%")
        controls.setOnClickPendingIntent(R.id.stop, stop)
        for ((command, id) in listOf("LESS" to R.id.less, "MORE" to R.id.more, "PAUSE" to R.id.pause)) {
            val action = PendingIntent.getService(this, 0, Intent(this, FilterService::class.java).setAction(command),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            controls.setOnClickPendingIntent(id, action)
        }
        builder.setCustomBigContentView(controls)
        packageManager.getLaunchIntentForPackage(packageName)?.let {
            builder.setContentIntent(PendingIntent.getActivity(this, 1, it, PendingIntent.FLAG_IMMUTABLE))
        }
        return builder.build()
    }

    private companion object {
        const val STOP = "dev.horizon.filter.STOP"
    }
}
