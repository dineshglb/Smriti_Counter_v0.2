package com.example.smriticounter

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

class SmritiWidget2x2Light : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            WidgetUpdater.update2x2(context, appWidgetManager, id, R.layout.smriti_widget_2x2_light, SmritiWidget2x2Light::class.java, 220)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            WidgetUpdater.ACTION_INCREMENT -> WidgetUpdater.handleIncrement(context)
            WidgetUpdater.ACTION_TOGGLE_TIMER -> WidgetUpdater.handleToggleTimer(context)
            WidgetUpdater.ACTION_UNDO -> WidgetUpdater.handleUndo(context)
            WidgetUpdater.ACTION_RESET -> WidgetUpdater.handleReset(context)
        }
    }
}
