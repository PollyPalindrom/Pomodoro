package com.example.pomodoro

import android.os.CountDownTimer
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.ItemBinding

interface StopwatchListener {
    fun start(position: Int, stopwatch: Stopwatch, itemBinding: ItemBinding)
    fun stop(position: Int, stopwatch: Stopwatch, currentMs: Long)
    fun reset(position: Int, stopwatch: Stopwatch, itemBinding: ItemBinding)
    fun delete(position: Int, stopwatch: Stopwatch)
    fun stopOtherStopwatches()
    fun getMyTimer(): CountDownTimer?
    fun setTimer(position: Int, stopwatch: Stopwatch, itemBinding: ItemBinding)
    fun setText(stopwatch: Stopwatch, binding: ItemBinding)
    fun getViewHolder(position: Int):RecyclerView.ViewHolder?
}