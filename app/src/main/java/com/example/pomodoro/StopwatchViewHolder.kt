package com.example.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import androidx.core.view.isInvisible
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.R.drawable
import com.example.pomodoro.databinding.ItemBinding

class StopwatchViewHolder(
    private val binding: ItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) :
    RecyclerView.ViewHolder(binding.root), LifecycleObserver {

    fun bind(stopwatch: Stopwatch) {
        if (!stopwatch.isStarted) {
            stopAnimation()
        } else {
            startAnimation()
        }
        binding.customView.setPeriod(stopwatch.limit)
        binding.customView.setCurrent(stopwatch.currentMs)
        listener.setText(stopwatch, binding)
        initButtonsListeners(stopwatch)
    }


    private fun initButtonsListeners(stopwatch: Stopwatch) {

        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                if (adapterPosition >= 0) {
                    stopAnimation()
                    listener.stop(adapterPosition, stopwatch, stopwatch.currentMs)
                }

            } else {
                if (adapterPosition >= 0) {
                    startAnimation()
                    listener.stopOtherStopwatches()
                    listener.start(adapterPosition, stopwatch, binding)
                }

            }
        }
        binding.restartButton.setOnClickListener {
            if (adapterPosition >= 0) {
                stopwatch.currentMs = stopwatch.limit
                listener.reset(adapterPosition, stopwatch, binding)
                stopAnimation()
            }
        }
        binding.deleteButton.setOnClickListener {
            if (adapterPosition >= 0) listener.delete(
                adapterPosition,
                stopwatch
            )
        }
    }

    private fun startAnimation() {
        binding.startPauseButton.setImageResource(drawable.ic_baseline_pause_circle_outline_24)
        if (binding.blinkingIndicator.isInvisible) {
            binding.blinkingIndicator.isInvisible = false
            (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
        }
    }

    fun stopAnimation() {
        binding.startPauseButton.setImageResource(drawable.ic_baseline_play_circle_outline_24)
        if (!binding.blinkingIndicator.isInvisible) {
            binding.blinkingIndicator.isInvisible = true
            (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        }
    }

}