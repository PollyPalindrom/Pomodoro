package com.example.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import androidx.core.view.isInvisible
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.R.color
import com.example.pomodoro.R.drawable
import com.example.pomodoro.databinding.ItemBinding

class StopwatchViewHolder(
    private val binding: ItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) :
    RecyclerView.ViewHolder(binding.root), LifecycleObserver {
    fun bind(stopwatch: Stopwatch) {
        listener.setText(stopwatch, binding)
        if (!stopwatch.isStarted) {
            stopAnimation()
        } else {
            startAnimation()

        }
        binding.customView.setPeriod(stopwatch.limit)
        binding.customView.setCurrent(stopwatch.currentMs)
        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {

        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                stopAnimation()
                listener.stop(stopwatch.id, stopwatch.currentMs)

            } else {
                startAnimation()
                val listToStop: MutableList<Stopwatch> =
                    listener.stopOtherStopwatches(stopwatch.id) as MutableList<Stopwatch>
                stopOtherTimers(listToStop)
                listener.start(stopwatch.id, binding)

            }
        }
        binding.restartButton.setOnClickListener {
            stopwatch.currentMs = stopwatch.limit
            listener.reset(stopwatch.id, binding)
            stopAnimation()
        }
        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    private fun startAnimation() {
        binding.stopwatchTimer.setBackgroundColor(resources.getColor(color.transparent))
        binding.startPauseButton.setImageResource(drawable.ic_baseline_pause_circle_outline_24)
        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopAnimation() {
        binding.startPauseButton.setImageResource(drawable.ic_baseline_play_circle_outline_24)
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun stopOtherTimers(listToStop: List<Stopwatch>) {
        listToStop.forEach {
            listener.stop(it.id, it.currentMs)
        }
    }
}