package com.example.pomodoro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.ItemBinding
import java.util.*

class StopwatchAdapter(private val listener: StopwatchListener) :
    RecyclerView.Adapter<StopwatchViewHolder>(), ItemTouchHelperAdapter {

    private var stopwatches = mutableListOf<Stopwatch>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopwatchViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemBinding.inflate(layoutInflater, parent, false)
        return StopwatchViewHolder(binding, listener, binding.root.context.resources)
    }

    fun setStopwatches(stopwatchList: List<Stopwatch>) {
        stopwatches = stopwatchList as MutableList<Stopwatch>
    }

    fun getCurrentTimerTime(): Long? {
        return stopwatches.find { it.isStarted }?.currentMs
    }

    fun timerIsStarted(): Boolean? {
        return stopwatches.find { it.isStarted }?.isStarted
    }

    fun stopOtherTimers(): List<Stopwatch> {
        stopwatches.forEachIndexed { index, stopwatch ->
            if (stopwatch.isStarted) {
                (listener.getViewHolder(index) as StopwatchViewHolder).stopAnimation()
                stopwatch.isStarted = false
            }
        }
        return stopwatches
    }

    fun changeStopwatch(
        position: Int,
        currentMs: Long?,
        isStarted: Boolean,
        shouldBeRestarted: Boolean
    ) {
        if (currentMs != null) {
            stopwatches[position].currentMs = currentMs
        }
        stopwatches[position].isStarted = isStarted
        stopwatches[position].shouldBeRestarted = shouldBeRestarted
    }

    fun addStopwatch(stopwatch: Stopwatch) {
        stopwatches.add(stopwatch)
        notifyItemInserted(stopwatches.size - 1)
    }

    fun deleteStopwatch(position: Int) {
        if (position >= 0) {
            stopwatches.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getStopwatches(): MutableList<Stopwatch> {
        return stopwatches
    }

    override fun onBindViewHolder(holder: StopwatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun getItem(position: Int): Stopwatch {
        return stopwatches[position]
    }

    override fun getItemCount(): Int {
        return stopwatches.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(stopwatches, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(stopwatches, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        deleteStopwatch(position)
    }
}