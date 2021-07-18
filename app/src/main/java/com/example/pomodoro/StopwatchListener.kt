package com.example.pomodoro

import android.os.CountDownTimer
import com.example.pomodoro.databinding.ItemBinding

interface StopwatchListener {
    fun start(id: Int, itemBinding: ItemBinding)
    fun stop(id: Int, currentMs: Long)
    fun reset(id: Int, itemBinding: ItemBinding)
    fun delete(id: Int)
    fun stopOtherStopwatches(id: Int): List<Stopwatch>
    fun getTimer(): CountDownTimer?
    fun setTimer(stopwatch: Stopwatch, itemBinding: ItemBinding)
    fun setText(stopwatch: Stopwatch, binding: ItemBinding)
}