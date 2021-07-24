package com.example.pomodoro

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.databinding.ItemBinding

class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {
    private val stopwatches = mutableListOf<Stopwatch>()
    private lateinit var binding: ActivityMainBinding
    private val stopwatchAdapter = StopwatchAdapter(this)
    private var nextId = 0
    private var timer: CountDownTimer? = null
    private var timeOverSound: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timeOverSound = MediaPlayer.create(this, R.raw.zvukgonga)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }
        binding.addNewStopwatchButton.setOnClickListener {
            if (checkNumber(binding.minutes.text.toString())) {
                stopwatches.add(
                    Stopwatch(
                        nextId++,
                        binding.minutes.text.toString().toLong() * 60L * 1000L,
                        binding.minutes.text.toString().toLong() * 60L * 1000L,
                        false,
                        false
                    )
                )
                stopwatchAdapter.submitList(stopwatches.toList())
            } else {
                Toast.makeText(
                    this.applicationContext,
                    "Wrong input :3 Max value is 24:00:00 in minutes. Min value is 00:01:00 in minutes",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    private fun getCurrentTimerTime(): Long? {
        return stopwatches.find { it.isStarted }?.currentMs
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (stopwatches.find { it.isStarted }?.isStarted == true) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            println(getCurrentTimerTime())
            startIntent.putExtra(STARTED_TIMER_TIME_MS, getCurrentTimerTime())
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    private fun checkNumber(numberToCompare: String): Boolean {
        var sum: Long = 0
        try {
            numberToCompare.toLong()
        } catch (e: NumberFormatException) {
            return false
        }
        if (numberToCompare == "") return false
        if (numberToCompare.toLong() <= 0L) return false
        if (numberToCompare.toLong() <= 1440L) return true
        if (numberToCompare.toLong() > 1440L) return false
        else {
            for (i in numberToCompare.indices) {
                sum = sum * 10L + numberToCompare[i].toInt()
                if (sum > 1440L) return false
            }
            return true
        }
    }

    override fun start(id: Int, itemBinding: ItemBinding) {
        changeStopwatch(id, null, true, false)
        timer?.cancel()
        stopwatches.find { it.id == id }?.let { setTimer(it, itemBinding) }
        timer?.start()
        stopwatches.find { it.id == id }?.limit?.let { itemBinding.customView.setPeriod(it) }
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false, false)
        timer?.cancel()
    }

    override fun reset(id: Int, itemBinding: ItemBinding) {
        itemBinding.background.setBackgroundColor(resources.getColor(R.color.transparent))
        stopwatches.find { it.id == id }?.shouldBeRestarted = false
        stopwatches.find { it.id == id }?.let { setText(it.id, itemBinding) }
        changeStopwatch(id, stopwatches.find { it.id == id }?.limit, false, false)
        timer?.cancel()
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        timer?.cancel()
        timer = null
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    override fun stopOtherStopwatches(id: Int): List<Stopwatch> {
        val listToStop: MutableList<Stopwatch> = mutableListOf()
        stopwatches.forEach {
            if (it.id != id && it.isStarted) listToStop.add(it)
        }
        return listToStop
    }

    override fun getTimer(): CountDownTimer? {
        return timer
    }

    override fun setTimer(stopwatch: Stopwatch, itemBinding: ItemBinding) {
        timer = object : CountDownTimer(stopwatch.currentMs, UNIT_TEN_MS) {
            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs = millisUntilFinished
                setText(stopwatch.id, itemBinding)
            }

            override fun onFinish() {
                timeOverSound?.start()
                setText(stopwatch.id, itemBinding)
                stopwatch.shouldBeRestarted = true
                changeStopwatch(
                    stopwatch.id,
                    stopwatches.find { it.id == stopwatch.id }?.limit,
                    false,
                    true
                )
                setColor(stopwatch, itemBinding)
                timer?.cancel()
            }
        }
    }

    private fun setColor(stopwatch: Stopwatch, itemBinding: ItemBinding) {
        if (stopwatch.shouldBeRestarted || stopwatch.currentMs <= 0L) itemBinding.background.setBackgroundColor(
            resources.getColor(
                R.color.red
            )
        )
        else itemBinding.background.setBackgroundColor(resources.getColor(R.color.transparent))
    }

    override fun setText(id: Int, binding: ItemBinding) {
        if (stopwatches.find { it.id == id }?.limit == stopwatches.find { it.id == id }?.currentMs) {
            binding.stopwatchTimer.text =
                stopwatches.find { it.id == id }?.limit?.displayTime()
        }
        if (!binding.blinkingIndicator.isInvisible) {
            binding.stopwatchTimer.text =
                stopwatches.find { it.id == id }?.currentMs?.displayTime()
            stopwatches.find { it.id == id }?.currentMs?.let { binding.customView.setCurrent(it) }
        }
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean, restart: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(
                    Stopwatch(
                        it.id,
                        currentMs ?: it.currentMs,
                        it.limit,
                        isStarted,
                        restart
                    )
                )
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return START_TIME
        }
        val h = this / 1000L / 3600L
        val m = this / 1000L % 3600L / 60L
        val s = this / 1000L % 60L
        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0L) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {
        private const val START_TIME = "00:00:00"
        private const val COMMAND_START = "COMMAND_START"
        private const val COMMAND_STOP = "COMMAND_STOP"
        private const val COMMAND_ID = "COMMAND_ID"
        private const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME_MS"
        private const val UNIT_TEN_MS = 1000L
    }

}