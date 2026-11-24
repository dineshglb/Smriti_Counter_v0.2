package com.example.smriticounter

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews

object WidgetUpdater {
    const val ACTION_INCREMENT = "com.example.smriticounter.ACTION_INCREMENT"
    const val ACTION_TOGGLE_TIMER = "com.example.smriticounter.ACTION_TOGGLE_TIMER"
    const val ACTION_UNDO = "com.example.smriticounter.ACTION_UNDO"
    const val ACTION_RESET = "com.example.smriticounter.ACTION_RESET"

    fun handleIncrement(context: Context) {
        val newCount = PrefsHelper.incrementToday(context)
        val target = PrefsHelper.getTarget(context)

        if (PrefsHelper.isVibrationEnabled(context)) {
            FeedbackHelper.vibrateTap(context)
        }
        if (PrefsHelper.isSoundEnabled(context) && newCount == target) {
            FeedbackHelper.playSoftChime(isTargetChime = true)
        } else if (PrefsHelper.isTapChimeEnabled(context)) {
            FeedbackHelper.playSoftChime(isTargetChime = false)
        }
        updateAllWidgets(context)
    }

    fun handleToggleTimer(context: Context) {
        PrefsHelper.toggleTimer(context)
        if (PrefsHelper.isVibrationEnabled(context)) {
            FeedbackHelper.vibrateTap(context)
        }
        updateAllWidgets(context)
    }

    fun handleUndo(context: Context) {
        PrefsHelper.decrementToday(context)
        updateAllWidgets(context)
    }

    fun handleReset(context: Context) {
        PrefsHelper.resetToday(context)
        updateAllWidgets(context)
    }

    fun updateAllWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)

        for (id in manager.getAppWidgetIds(ComponentName(context, SmritiWidget2x2Dark::class.java))) {
            update2x2(context, manager, id, R.layout.smriti_widget_2x2_dark, SmritiWidget2x2Dark::class.java, 210)
        }
        for (id in manager.getAppWidgetIds(ComponentName(context, SmritiWidget2x2Light::class.java))) {
            update2x2(context, manager, id, R.layout.smriti_widget_2x2_light, SmritiWidget2x2Light::class.java, 220)
        }
        for (id in manager.getAppWidgetIds(ComponentName(context, SmritiWidget4x4Dark::class.java))) {
            update4x4(context, manager, id, R.layout.smriti_widget_4x4_dark, SmritiWidget4x4Dark::class.java, 410)
        }
        for (id in manager.getAppWidgetIds(ComponentName(context, SmritiWidget4x4Light::class.java))) {
            update4x4(context, manager, id, R.layout.smriti_widget_4x4_light, SmritiWidget4x4Light::class.java, 420)
        }
        for (id in manager.getAppWidgetIds(ComponentName(context, SmritiWidget6x6Dark::class.java))) {
            update6x6(context, manager, id, R.layout.smriti_widget_6x6_dark, SmritiWidget6x6Dark::class.java, 610)
        }
        for (id in manager.getAppWidgetIds(ComponentName(context, SmritiWidget6x6Light::class.java))) {
            update6x6(context, manager, id, R.layout.smriti_widget_6x6_light, SmritiWidget6x6Light::class.java, 620)
        }
    }

    private fun broadcastIntent(context: Context, cls: Class<*>, actionStr: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, cls).apply { action = actionStr }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun update2x2(context: Context, manager: AppWidgetManager, widgetId: Int, layoutId: Int, cls: Class<*>, reqBase: Int) {
        val rec = PrefsHelper.getTodayRecord(context)
        val views = RemoteViews(context.packageName, layoutId)
        views.setTextViewText(R.id.tv_widget_count_2x2, rec.count.toString())
        views.setOnClickPendingIntent(R.id.widget_root_2x2, broadcastIntent(context, cls, ACTION_INCREMENT, reqBase + 1))
        manager.updateAppWidget(widgetId, views)
    }

    fun update4x4(context: Context, manager: AppWidgetManager, widgetId: Int, layoutId: Int, cls: Class<*>, reqBase: Int) {
        val rec = PrefsHelper.getTodayRecord(context)
        val target = PrefsHelper.getTarget(context)
        val swaman = PrefsHelper.getActiveSwaman(context)
        val showStreak = PrefsHelper.isStreakVisible(context)
        val streak = PrefsHelper.getStreak(context)
        val running = PrefsHelper.isTimerRunning(context)

        val views = RemoteViews(context.packageName, layoutId)
        views.setTextViewText(R.id.tv_widget_swaman_4x4, swaman)
        views.setTextViewText(R.id.tv_widget_count_4x4, rec.count.toString())
        views.setTextViewText(R.id.tv_widget_target_4x4, "of $target  •  ${rec.seconds}s")
        views.setTextViewText(
            R.id.tv_widget_status_4x4,
            if (rec.count >= target) "Target reached! Tap to +1" else "Tap tile to +1"
        )

        views.setViewVisibility(R.id.tv_widget_streak_4x4, if (showStreak) View.VISIBLE else View.GONE)
        views.setTextViewText(R.id.tv_widget_streak_4x4, if (streak == 1) "1-day streak" else "$streak-day streak")

        views.setViewVisibility(R.id.chrono_widget_4x4, if (running) View.VISIBLE else View.GONE)
        views.setChronometer(R.id.chrono_widget_4x4, PrefsHelper.getTimerStartElapsed(context), "%s", running)
        views.setTextViewText(R.id.btn_widget_timer_4x4, if (running) "Stop & Save" else "Start Timer")

        views.setOnClickPendingIntent(R.id.widget_root_4x4, broadcastIntent(context, cls, ACTION_INCREMENT, reqBase + 1))
        views.setOnClickPendingIntent(R.id.btn_widget_timer_4x4, broadcastIntent(context, cls, ACTION_TOGGLE_TIMER, reqBase + 2))
        manager.updateAppWidget(widgetId, views)
    }

    fun update6x6(context: Context, manager: AppWidgetManager, widgetId: Int, layoutId: Int, cls: Class<*>, reqBase: Int) {
        val rec = PrefsHelper.getTodayRecord(context)
        val target = PrefsHelper.getTarget(context)
        val secTarget = PrefsHelper.getSecondsTarget(context)
        val swaman = PrefsHelper.getActiveSwaman(context)
        val showStreak = PrefsHelper.isStreakVisible(context)
        val streak = PrefsHelper.getStreak(context)
        val running = PrefsHelper.isTimerRunning(context)

        val views = RemoteViews(context.packageName, layoutId)
        views.setTextViewText(R.id.tv_widget_date_6x6, PrefsHelper.todayFriendly())
        views.setTextViewText(R.id.tv_widget_swaman_6x6, swaman)
        views.setTextViewText(R.id.tv_widget_count_6x6, rec.count.toString())
        views.setTextViewText(R.id.tv_widget_target_6x6, "of $target  •  ${rec.seconds}s / ${secTarget}s")
        views.setProgressBar(R.id.pb_widget_6x6, target, rec.count.coerceAtMost(target), false)
        views.setTextViewText(
            R.id.tv_widget_status_6x6,
            if (rec.count >= target) "Target reached! Tap center to +1" else "Tap center to +1"
        )

        views.setViewVisibility(R.id.tv_widget_streak_6x6, if (showStreak) View.VISIBLE else View.GONE)
        views.setTextViewText(R.id.tv_widget_streak_6x6, if (streak == 1) "1-day streak" else "$streak-day streak")

        views.setViewVisibility(R.id.chrono_widget_6x6, if (running) View.VISIBLE else View.GONE)
        views.setChronometer(R.id.chrono_widget_6x6, PrefsHelper.getTimerStartElapsed(context), "%s", running)
        views.setTextViewText(R.id.btn_widget_timer_6x6, if (running) "Stop & Save" else "Start")

        views.setOnClickPendingIntent(R.id.widget_tap_area_6x6, broadcastIntent(context, cls, ACTION_INCREMENT, reqBase + 1))
        views.setOnClickPendingIntent(R.id.btn_widget_timer_6x6, broadcastIntent(context, cls, ACTION_TOGGLE_TIMER, reqBase + 2))
        views.setOnClickPendingIntent(R.id.btn_widget_undo_6x6, broadcastIntent(context, cls, ACTION_UNDO, reqBase + 3))
        views.setOnClickPendingIntent(R.id.btn_widget_reset_6x6, broadcastIntent(context, cls, ACTION_RESET, reqBase + 4))

        val binduPending = PendingIntent.getActivity(
            context, reqBase + 5, Intent(context, BinduActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_bindu_6x6, binduPending)

        val openPending = PendingIntent.getActivity(
            context, reqBase + 6, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_open_6x6, openPending)

        manager.updateAppWidget(widgetId, views)
    }
}
