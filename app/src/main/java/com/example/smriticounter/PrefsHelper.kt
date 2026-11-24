package com.example.smriticounter

import android.content.Context
import android.os.SystemClock
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayRecord(
    val date: String,
    val count: Int,
    val seconds: Int,
    val amritCount: Int,
    val amritSec: Int,
    val dayCount: Int,
    val daySec: Int,
    val eveCount: Int,
    val eveSec: Int,
    val swaman: String
)

object PrefsHelper {
    private const val PREFS_NAME = "smriti_prefs_v2"
    private const val KEY_TARGET = "daily_target"
    private const val KEY_SECONDS_TARGET = "daily_seconds_target"
    private const val KEY_DAILY_LOG = "daily_log_v2"
    private const val KEY_VIBRATION = "pref_vibration"
    private const val KEY_TAP_CHIME = "pref_tap_chime"
    private const val KEY_SOUND = "pref_sound"
    private const val KEY_STREAK_VISIBLE = "pref_streak_visible"
    private const val KEY_ROTATE_SWAMAN = "pref_rotate_swaman"
    private const val KEY_TRAFFIC_CONTROL = "pref_traffic_control"
    private const val KEY_ACTIVE_SWAMAN = "active_swaman"
    private const val KEY_CUSTOM_SWAMANS = "custom_swamans"
    private const val KEY_TIMER_RUNNING = "timer_running"
    private const val KEY_TIMER_START_ELAPSED = "timer_start_elapsed"

    val DEFAULT_SWAMANS = listOf(
        "I am a peaceful soul",
        "I am a point of light",
        "I am a master almighty authority",
        "I am a pure, loving soul",
        "I am a carefree emperor",
        "I am Baba's companion"
    )

    fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun todayFriendly(): String = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())

    private fun currentTimeWindow(): String {
        val cal = Calendar.getInstance()
        val mins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        return when {
            mins in 210..359 -> "amrit" // 03:30 - 05:59
            mins in 360..1079 -> "day"  // 06:00 - 17:59
            else -> "eve"               // 18:00 - 03:29
        }
    }

    fun getTarget(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_TARGET, 108).coerceAtLeast(1)

    fun setTarget(context: Context, target: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_TARGET, target.coerceAtLeast(1)).apply()
    }

    fun getSecondsTarget(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_SECONDS_TARGET, 600).coerceAtLeast(1)

    fun setSecondsTarget(context: Context, sec: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_SECONDS_TARGET, sec.coerceAtLeast(1)).apply()
    }

    fun isVibrationEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_VIBRATION, true)

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_VIBRATION, enabled).apply()
    }

    fun isTapChimeEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_TAP_CHIME, false)

    fun setTapChimeEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_TAP_CHIME, enabled).apply()
    }

    fun isSoundEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_SOUND, true)

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_SOUND, enabled).apply()
    }

    fun isStreakVisible(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_STREAK_VISIBLE, true)

    fun setStreakVisible(context: Context, visible: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_STREAK_VISIBLE, visible).apply()
    }

    fun isAutoRotateSwaman(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ROTATE_SWAMAN, false)

    fun setAutoRotateSwaman(context: Context, rotate: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_ROTATE_SWAMAN, rotate).apply()
    }

    fun isTrafficControlEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_TRAFFIC_CONTROL, false)

    fun setTrafficControlEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_TRAFFIC_CONTROL, enabled).apply()
    }

    fun getAllSwamans(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_CUSTOM_SWAMANS, "[]") ?: "[]"
        val list = DEFAULT_SWAMANS.toMutableList()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val s = arr.optString(i).trim()
                if (s.isNotEmpty() && !list.contains(s)) list.add(s)
            }
        } catch (_: Exception) {}
        return list
    }

    fun addCustomSwaman(context: Context, swaman: String) {
        val clean = swaman.trim()
        if (clean.isEmpty()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val arr = try {
            JSONArray(prefs.getString(KEY_CUSTOM_SWAMANS, "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }
        arr.put(clean)
        prefs.edit().putString(KEY_CUSTOM_SWAMANS, arr.toString()).putString(KEY_ACTIVE_SWAMAN, clean).apply()
    }

    fun getActiveSwaman(context: Context): String {
        val all = getAllSwamans(context)
        if (isAutoRotateSwaman(context) && all.isNotEmpty()) {
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            return all[dayOfYear % all.size]
        }
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_ACTIVE_SWAMAN, null)
        return saved ?: all.first()
    }

    fun setActiveSwaman(context: Context, swaman: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_ACTIVE_SWAMAN, swaman).apply()
    }

    // Timer state
    fun isTimerRunning(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val running = prefs.getBoolean(KEY_TIMER_RUNNING, false)
        if (!running) return false
        val start = prefs.getLong(KEY_TIMER_START_ELAPSED, 0L)
        if (start <= 0L || SystemClock.elapsedRealtime() < start) {
            prefs.edit().putBoolean(KEY_TIMER_RUNNING, false).apply()
            return false
        }
        return true
    }

    fun getTimerStartElapsed(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_TIMER_START_ELAPSED, SystemClock.elapsedRealtime())
    }

    fun toggleTimer(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (isTimerRunning(context)) {
            val start = getTimerStartElapsed(context)
            val elapsedSec = ((SystemClock.elapsedRealtime() - start) / 1000L).toInt().coerceAtLeast(1)
            prefs.edit().putBoolean(KEY_TIMER_RUNNING, false).apply()
            addSecondsToday(context, elapsedSec)
            false
        } else {
            prefs.edit()
                .putBoolean(KEY_TIMER_RUNNING, true)
                .putLong(KEY_TIMER_START_ELAPSED, SystemClock.elapsedRealtime())
                .apply()
            true
        }
    }

    private fun readLog(context: Context): JSONObject {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_DAILY_LOG, "{}") ?: "{}"
        return try { JSONObject(raw) } catch (_: Exception) { JSONObject() }
    }

    private fun writeLog(context: Context, json: JSONObject) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_DAILY_LOG, json.toString()).apply()
    }

    private fun getOrCreateDayObj(log: JSONObject, key: String, swaman: String): JSONObject {
        val obj = log.optJSONObject(key) ?: JSONObject()
        if (!obj.has("swaman")) obj.put("swaman", swaman)
        return obj
    }

    fun getTodayRecord(context: Context): DayRecord {
        val key = todayKey()
        val obj = readLog(context).optJSONObject(key)
        return parseDayRecord(key, obj, getActiveSwaman(context))
    }

    private fun parseDayRecord(date: String, obj: JSONObject?, fallbackSwaman: String): DayRecord {
        if (obj == null) {
            return DayRecord(date, 0, 0, 0, 0, 0, 0, 0, 0, fallbackSwaman)
        }
        return DayRecord(
            date = date,
            count = obj.optInt("count", 0),
            seconds = obj.optInt("seconds", 0),
            amritCount = obj.optInt("amritCount", 0),
            amritSec = obj.optInt("amritSec", 0),
            dayCount = obj.optInt("dayCount", 0),
            daySec = obj.optInt("daySec", 0),
            eveCount = obj.optInt("eveCount", 0),
            eveSec = obj.optInt("eveSec", 0),
            swaman = obj.optString("swaman", fallbackSwaman)
        )
    }

    fun incrementToday(context: Context): Int {
        val log = readLog(context)
        val key = todayKey()
        val swaman = getActiveSwaman(context)
        val obj = getOrCreateDayObj(log, key, swaman)
        val next = obj.optInt("count", 0) + 1
        obj.put("count", next)
        obj.put("swaman", swaman)
        when (currentTimeWindow()) {
            "amrit" -> obj.put("amritCount", obj.optInt("amritCount", 0) + 1)
            "day" -> obj.put("dayCount", obj.optInt("dayCount", 0) + 1)
            else -> obj.put("eveCount", obj.optInt("eveCount", 0) + 1)
        }
        log.put(key, obj)
        writeLog(context, log)
        return next
    }

    fun addSecondsToday(context: Context, sec: Int): Int {
        if (sec <= 0) return getTodayRecord(context).seconds
        val log = readLog(context)
        val key = todayKey()
        val swaman = getActiveSwaman(context)
        val obj = getOrCreateDayObj(log, key, swaman)
        val next = obj.optInt("seconds", 0) + sec
        obj.put("seconds", next)
        obj.put("swaman", swaman)
        when (currentTimeWindow()) {
            "amrit" -> obj.put("amritSec", obj.optInt("amritSec", 0) + sec)
            "day" -> obj.put("daySec", obj.optInt("daySec", 0) + sec)
            else -> obj.put("eveSec", obj.optInt("eveSec", 0) + sec)
        }
        log.put(key, obj)
        writeLog(context, log)
        return next
    }

    fun decrementToday(context: Context) {
        val log = readLog(context)
        val key = todayKey()
        val obj = log.optJSONObject(key) ?: return
        val next = (obj.optInt("count", 0) - 1).coerceAtLeast(0)
        obj.put("count", next)
        log.put(key, obj)
        writeLog(context, log)
    }

    fun resetToday(context: Context) {
        val log = readLog(context)
        log.remove(todayKey())
        writeLog(context, log)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_TIMER_RUNNING, false).apply()
    }

    fun getStreak(context: Context): Int {
        val log = readLog(context)
        val target = getTarget(context)
        val secTarget = getSecondsTarget(context)
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        var streak = 0

        fun met(key: String): Boolean {
            val obj = log.optJSONObject(key) ?: return false
            return obj.optInt("count", 0) >= target || obj.optInt("seconds", 0) >= secTarget
        }

        if (met(fmt.format(cal.time))) streak += 1
        for (i in 1..365) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            if (met(fmt.format(cal.time))) streak += 1 else break
        }
        return streak
    }

    fun getHistoryEntries(context: Context): List<DayRecord> {
        val log = readLog(context)
        val swaman = getActiveSwaman(context)
        val result = mutableListOf<DayRecord>()
        val keys = log.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            result.add(parseDayRecord(k, log.optJSONObject(k), swaman))
        }
        return result.sortedByDescending { it.date }
    }
}
