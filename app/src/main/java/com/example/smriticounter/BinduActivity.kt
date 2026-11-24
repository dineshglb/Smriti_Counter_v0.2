package com.example.smriticounter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BinduActivity : AppCompatActivity() {

    private var startElapsed = 0L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tvTimer: TextView

    private val ticker = object : Runnable {
        override fun run() {
            val sec = ((SystemClock.elapsedRealtime() - startElapsed) / 1000L).toInt().coerceAtLeast(0)
            val m = sec / 60
            val s = sec % 60
            tvTimer.text = String.format("%02d:%02d", m, s)
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bindu)

        tvTimer = findViewById(R.id.tv_bindu_timer)
        findViewById<TextView>(R.id.tv_bindu_swaman).text = PrefsHelper.getActiveSwaman(this)

        startElapsed = SystemClock.elapsedRealtime()
        handler.post(ticker)

        findViewById<Button>(R.id.btn_bindu_done).setOnClickListener {
            val sec = ((SystemClock.elapsedRealtime() - startElapsed) / 1000L).toInt().coerceAtLeast(1)
            PrefsHelper.addSecondsToday(this, sec)
            WidgetUpdater.updateAllWidgets(this)
            Toast.makeText(this, "Added ${sec}s in soul consciousness", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(ticker)
    }
}
