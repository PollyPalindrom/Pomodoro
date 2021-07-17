package com.example.pomodoro

import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Parcelable
import android.os.PersistableBundle
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.databinding.ItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.LineNumberInputStream

class MainActivity : AppCompatActivity(), StopwatchListener {
    private val stopwatches = mutableListOf<Stopwatch>()
    private lateinit var binding: ActivityMainBinding
    private val stopwatchAdapter = StopwatchAdapter(this)
    private var nextId = 0
    private var timers = mutableListOf<CountDownTimer?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }
        binding.addNewStopwatchButton.setOnClickListener {
            stopwatches.add(
                Stopwatch(
                    nextId++,
                    binding.minutes.text.toString().toLong() * 60L * 1000L,
                    binding.minutes.text.toString().toLong() * 60L * 1000L,
                    false
                )
            )
            var timer: CountDownTimer? = null
            timers.add(timer)
            stopwatchAdapter.submitList(stopwatches.toList())
        }

    }

    override fun start(id: Int) {
        changeStopwatch(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }

    override fun reset(id: Int) {
        changeStopwatch(id, stopwatches.find { it.id == id }?.limit, false)
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    override fun stopOtherStopwatches(id: Int): List<Stopwatch> {
        var listToStop: MutableList<Stopwatch> = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id != id && it.isStarted == true) listToStop.add(it)
        }
        return listToStop
    }

    override fun getTimer(id: Int): CountDownTimer? {
        return timers[id]
    }

    override fun setTimer(id: Int, newTimer: CountDownTimer?) {
        timers[id] = newTimer
    }

    override fun setText(stopwatch: Stopwatch, itemBinding: ItemBinding) {

        if (!itemBinding.blinkingIndicator.isInvisible) itemBinding.stopwatchTimer.text =
            stopwatch.currentMs.displayTime()
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(
                    Stopwatch(
                        it.id,
                        currentMs ?: it.currentMs,
                        it.limit,
                        isStarted
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
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
        val ms = this % 1000 / 10
        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}:${displaySlot(ms)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {
        private const val START_TIME = "00:00:00:00"
        private const val LIST_STATE_KEY = "LIST_STATE_KEY"
        private const val UNIT_TEN_MS = 10L
    }

}