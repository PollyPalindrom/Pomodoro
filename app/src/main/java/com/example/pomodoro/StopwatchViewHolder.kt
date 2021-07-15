package com.example.pomodoro

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.ItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StopwatchViewHolder(
    private val binding: ItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) :
    RecyclerView.ViewHolder(binding.root), LifecycleObserver {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }
        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                binding.startPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                binding.startPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                var listToStop: MutableList<Stopwatch>
                listToStop = listener.stopOtherStopwatches(stopwatch.id) as MutableList<Stopwatch>
                stopOtherTimers(listToStop)
                listener.start(stopwatch.id)

            }
        }
        binding.restartButton.setOnClickListener { listener.reset(stopwatch.id) }
        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    private fun stopOtherTimers(listToStop: List<Stopwatch>) {
        listToStop.forEach {
            listener.stop(it.id, it.currentMs)
        }
    }

    private fun stopTimer(stopwatch: Stopwatch) {
        val drawable = resources.getDrawable(R.drawable.ic_baseline_play_circle_outline_24)
        binding.startPauseButton.setImageDrawable(drawable)

        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    @SuppressLint("ResourceAsColor")
    private fun startTimer(stopwatch: Stopwatch) {
        val drawable = resources.getDrawable(R.drawable.ic_baseline_pause_circle_outline_24)
        binding.startPauseButton.setImageDrawable(drawable)
        binding.stopwatchTimer.setBackgroundColor(0)
        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }


    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(stopwatch.limit, UNIT_TEN_MS) {
            val interval = UNIT_TEN_MS
            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs -= interval
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
            }

            @SuppressLint("ResourceAsColor")
            override fun onFinish() {
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                listener.reset(stopwatch.id)
                binding.stopwatchTimer.setBackgroundColor(R.color.red)
            }
        }
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
        private const val UNIT_TEN_MS = 10L
    }
}