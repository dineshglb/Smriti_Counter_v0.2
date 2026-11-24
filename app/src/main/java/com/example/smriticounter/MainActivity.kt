package com.example.smriticounter

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvTodaySummary: TextView
    private lateinit var tvTimerLive: TextView
    private lateinit var btnToggleTimer: Button
    private lateinit var spinnerSwaman: Spinner
    private lateinit var etCustomSwaman: EditText
    private lateinit var etTarget: EditText
    private lateinit var etSecondsTarget: EditText
    private lateinit var tvTodayBreakdown: TextView
    private lateinit var tvWeeklySummary: TextView
    private lateinit var tvHistory: TextView

    private lateinit var switchVibration: Switch
    private lateinit var switchTapChime: Switch
    private lateinit var switchSound: Switch
    private lateinit var switchStreak: Switch
    private lateinit var switchRotateSwaman: Switch
    private lateinit var switchTrafficControl: Switch

    private val handler = Handler(Looper.getMainLooper())
    private var swamanList = listOf<String>()

    private val timerTicker = object : Runnable {
        override fun run() {
            updateTimerLabel()
            if (PrefsHelper.isTimerRunning(this@MainActivity)) {
                handler.postDelayed(this, 1000L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTodaySummary = findViewById(R.id.tv_today_summary)
        tvTimerLive = findViewById(R.id.tv_timer_live)
        btnToggleTimer = findViewById(R.id.btn_toggle_timer)
        spinnerSwaman = findViewById(R.id.spinner_swaman)
        etCustomSwaman = findViewById(R.id.et_custom_swaman)
        etTarget = findViewById(R.id.et_target)
        etSecondsTarget = findViewById(R.id.et_seconds_target)
        tvTodayBreakdown = findViewById(R.id.tv_today_breakdown)
        tvWeeklySummary = findViewById(R.id.tv_weekly_summary)
        tvHistory = findViewById(R.id.tv_history)

        switchVibration = findViewById(R.id.switch_vibration)
        switchTapChime = findViewById(R.id.switch_tap_chime)
        switchSound = findViewById(R.id.switch_sound)
        switchStreak = findViewById(R.id.switch_streak)
        switchRotateSwaman = findViewById(R.id.switch_rotate_swaman)
        switchTrafficControl = findViewById(R.id.switch_traffic_control)

        btnToggleTimer.setOnClickListener {
            val running = PrefsHelper.toggleTimer(this)
            WidgetUpdater.updateAllWidgets(this)
            refreshUI()
            if (running) handler.post(timerTicker)
        }

        findViewById<Button>(R.id.btn_open_bindu).setOnClickListener {
            startActivity(Intent(this, BinduActivity::class.java))
        }

        findViewById<Button>(R.id.btn_add_swaman).setOnClickListener {
            val text = etCustomSwaman.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            PrefsHelper.addCustomSwaman(this, text)
            etCustomSwaman.setText("")
            setupSwamanSpinner()
            WidgetUpdater.updateAllWidgets(this)
            Toast.makeText(this, "Swaman added", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_preset_27).setOnClickListener { etTarget.setText("27") }
        findViewById<Button>(R.id.btn_preset_54).setOnClickListener { etTarget.setText("54") }
        findViewById<Button>(R.id.btn_preset_108).setOnClickListener { etTarget.setText("108") }
        findViewById<Button>(R.id.btn_preset_1008).setOnClickListener { etTarget.setText("1008") }

        findViewById<Button>(R.id.btn_save_target).setOnClickListener {
            val cVal = etTarget.text.toString().toIntOrNull()
            val sVal = etSecondsTarget.text.toString().toIntOrNull()
            if (cVal == null || cVal < 1 || sVal == null || sVal < 1) {
                Toast.makeText(this, "Enter valid targets (1 or more)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            PrefsHelper.setTarget(this, cVal)
            PrefsHelper.setSecondsTarget(this, sVal)
            WidgetUpdater.updateAllWidgets(this)
            refreshUI()
            Toast.makeText(this, "Targets saved", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_undo).setOnClickListener {
            PrefsHelper.decrementToday(this)
            WidgetUpdater.updateAllWidgets(this)
            refreshUI()
        }

        findViewById<Button>(R.id.btn_reset).setOnClickListener {
            PrefsHelper.resetToday(this)
            WidgetUpdater.updateAllWidgets(this)
            refreshUI()
        }

        findViewById<Button>(R.id.btn_export_chart).setOnClickListener {
            val entries = PrefsHelper.getHistoryEntries(this).take(7)
            val summary = buildString {
                appendLine("Smriti Counter - Spiritual Chart")
                entries.forEach { r ->
                    appendLine("${r.date}: ${r.count} taps, ${r.seconds}s | Swaman: ${r.swaman}")
                }
            }
            val cb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cb.setPrimaryClip(ClipData.newPlainText("Smriti Chart", summary))
            Toast.makeText(this, "Chart copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        setupSwitches()
        setupSwamanSpinner()
    }

    private fun setupSwitches() {
        switchVibration.isChecked = PrefsHelper.isVibrationEnabled(this)
        switchTapChime.isChecked = PrefsHelper.isTapChimeEnabled(this)
        switchSound.isChecked = PrefsHelper.isSoundEnabled(this)
        switchStreak.isChecked = PrefsHelper.isStreakVisible(this)
        switchRotateSwaman.isChecked = PrefsHelper.isAutoRotateSwaman(this)
        switchTrafficControl.isChecked = PrefsHelper.isTrafficControlEnabled(this)

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.setVibrationEnabled(this, isChecked)
            if (isChecked) FeedbackHelper.vibrateTap(this)
        }
        switchTapChime.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.setTapChimeEnabled(this, isChecked)
            if (isChecked) FeedbackHelper.playSoftChime(isTargetChime = false)
        }
        switchSound.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.setSoundEnabled(this, isChecked)
            if (isChecked) FeedbackHelper.playSoftChime(isTargetChime = true)
        }
        switchStreak.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.setStreakVisible(this, isChecked)
            WidgetUpdater.updateAllWidgets(this)
        }
        switchRotateSwaman.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.setAutoRotateSwaman(this, isChecked)
            setupSwamanSpinner()
            WidgetUpdater.updateAllWidgets(this)
        }
        switchTrafficControl.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                }
            }
            PrefsHelper.setTrafficControlEnabled(this, isChecked)
            TrafficControlReceiver.schedule(this, isChecked)
        }
    }

    private fun setupSwamanSpinner() {
        swamanList = PrefsHelper.getAllSwamans(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, swamanList)
        spinnerSwaman.adapter = adapter
        val active = PrefsHelper.getActiveSwaman(this)
        val idx = swamanList.indexOf(active).coerceAtLeast(0)
        spinnerSwaman.setSelection(idx)

        spinnerSwaman.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val chosen = swamanList[position]
                if (chosen != PrefsHelper.getActiveSwaman(this@MainActivity)) {
                    PrefsHelper.setActiveSwaman(this@MainActivity, chosen)
                    WidgetUpdater.updateAllWidgets(this@MainActivity)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
        if (PrefsHelper.isTimerRunning(this)) {
            handler.post(timerTicker)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timerTicker)
    }

    private fun updateTimerLabel() {
        val rec = PrefsHelper.getTodayRecord(this)
        val running = PrefsHelper.isTimerRunning(this)
        if (running) {
            val liveSec = ((SystemClock.elapsedRealtime() - PrefsHelper.getTimerStartElapsed(this)) / 1000L).toInt().coerceAtLeast(0)
            tvTimerLive.text = "Running: ${liveSec}s  •  Saved today: ${rec.seconds}s"
            btnToggleTimer.text = "Stop & Save"
        } else {
            tvTimerLive.text = "Timer stopped  •  Saved today: ${rec.seconds}s"
            btnToggleTimer.text = "Start Timer"
        }
    }

    private fun refreshUI() {
        val rec = PrefsHelper.getTodayRecord(this)
        val target = PrefsHelper.getTarget(this)
        val secTarget = PrefsHelper.getSecondsTarget(this)
        val streak = PrefsHelper.getStreak(this)

        tvTodaySummary.text = "Today: ${rec.count} / $target  •  ${rec.seconds}s / ${secTarget}s  •  ${streak}d streak"
        etTarget.setText(target.toString())
        etSecondsTarget.setText(secTarget.toString())
        updateTimerLabel()

        tvTodayBreakdown.text =
            "Amrit Vela: ${rec.amritCount} (${rec.amritSec}s)  •  Day: ${rec.dayCount} (${rec.daySec}s)  •  Eve: ${rec.eveCount} (${rec.eveSec}s)"

        val entries = PrefsHelper.getHistoryEntries(this)
        val last7 = entries.take(7)
        val sumCount = last7.sumOf { it.count }
        val sumSec = last7.sumOf { it.seconds }
        tvWeeklySummary.text = "Last 7 Days Total: $sumCount taps  •  ${sumSec}s in consciousness"

        if (entries.isEmpty()) {
            tvHistory.text = "No history yet. Tap a widget to start counting!"
        } else {
            tvHistory.text = entries.joinToString("\n\n") { r ->
                val mark = if (r.count >= target || r.seconds >= secTarget) " ✓" else ""
                "${r.date} — ${r.count}/$target • ${r.seconds}s$mark\nSwaman: ${r.swaman}"
            }
        }
    }
}
