package com.example.pomodoro

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.databinding.ItemBinding

class MainActivity : AppCompatActivity(), StopwatchListener {
    private val stopwatches = mutableListOf<Stopwatch>()
    private lateinit var binding: ActivityMainBinding
    private val stopwatchAdapter = StopwatchAdapter(this)
    private var nextId = 0
    private var timer: CountDownTimer? = null
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
            stopwatchAdapter.submitList(stopwatches.toList())
        }

    }

    override fun start(id: Int, itemBinding: ItemBinding) {
        changeStopwatch(id, null, true)
        timer?.cancel()
        setTimer(stopwatches[id], itemBinding)
        timer?.start()
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
        timer?.cancel()
    }

    override fun reset(id: Int, itemBinding: ItemBinding) {
        setText(stopwatches[id],itemBinding)
        changeStopwatch(id, stopwatches[id].limit, false)
        timer?.cancel()
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
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
                setText(stopwatch, itemBinding)
            }

            override fun onFinish() {
                itemBinding.stopwatchTimer.setBackgroundColor(resources.getColor(R.color.red))
                setText(stopwatch, itemBinding)
                reset(stopwatch.id, itemBinding)
            }
        }
    }

    override fun setText(stopwatch: Stopwatch, binding: ItemBinding) {

        if (!binding.blinkingIndicator.isInvisible)
            binding.stopwatchTimer.text =
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
        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {
        private const val START_TIME = "00:00:00"
        private const val UNIT_TEN_MS = 1000L
    }

}