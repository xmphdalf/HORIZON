package dev.horizon.filter

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.RemoteViews
import dev.horizon.surface.overlay.FilterService

internal const val FILTER_OFF = "dev.horizon.filter.STOP"

internal fun control(context: Context, action: String) {
    val p = context.getSharedPreferences("manual_filter", Context.MODE_PRIVATE)
    val on = p.getBoolean("filter_running", false)
    val stop = action == FILTER_OFF || (action == "TOGGLE" && on)
    val intent = Intent(context, FilterService::class.java)
    if (stop) {
        context.startService(intent.setAction(FILTER_OFF))
    } else if (Settings.canDrawOverlays(context) && Settings.System.canWrite(context)) {
        context.startForegroundService(intent.setAction(action).putExtra("kelvin", p.getInt("kelvin", 3400)).putExtra("dim", p.getInt("dim", 82)))
    } else {
        context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/** Framework tile owns its state whenever Android asks it to listen. */
class FilterTile : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        val p = getSharedPreferences("manual_filter", Context.MODE_PRIVATE)
        val on = p.getBoolean("filter_running", false)
        qsTile?.let {
            it.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            it.label = "Filter"
            if (Build.VERSION.SDK_INT >= 29) it.subtitle = if (on) "${p.getInt("kelvin", 3400)} K" else "Off"
            it.updateTile()
        }
    }
    override fun onClick() { super.onClick(); control(this, "TOGGLE") }
}

/** Both widget sizes use the service's committed settings. */
open class FilterWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val p = context.getSharedPreferences("manual_filter", Context.MODE_PRIVATE)
        val on = p.getBoolean("filter_running", false)
        val wide = this is WideWidget
        val view = RemoteViews(context.packageName, if (wide) R.layout.widget_wide else R.layout.widget_square)
        view.setTextViewText(R.id.value, if (on) "${p.getInt("kelvin", 3400)} K" else "Off")
        view.setTextViewText(R.id.detail, if (on) "${p.getInt("dim", 82)}% · ON" else "HORIZON")
        fun wire(id: Int, action: String) {
            val command = Intent(context, javaClass).setAction(action)
            view.setOnClickPendingIntent(id, PendingIntent.getBroadcast(context, id, command, PendingIntent.FLAG_IMMUTABLE))
        }
        wire(R.id.power, if (on) FILTER_OFF else "TOGGLE")
        view.setTextViewText(R.id.power, if (on) "Turn off" else "Turn on")
        if (wide) { wire(R.id.less, "LESS"); wire(R.id.more, "MORE") }
        manager.updateAppWidget(ids, view)
    }
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in setOf(FILTER_OFF, "TOGGLE", "LESS", "MORE")) control(context, intent.action.orEmpty())
        else super.onReceive(context, intent)
    }
}
class WideWidget : FilterWidget()

internal fun refreshControls(context: Context) {
    val manager = AppWidgetManager.getInstance(context)
    listOf(FilterWidget(), WideWidget()).forEach { w ->
        val ids = manager.getAppWidgetIds(ComponentName(context, w.javaClass))
        if (ids.isNotEmpty()) w.onUpdate(context, manager, ids)
    }
    TileService.requestListeningState(context, ComponentName(context, FilterTile::class.java))
}

class ControlsChanged : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { refreshControls(context) }
}
