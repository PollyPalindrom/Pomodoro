package com.example.pomodoro

import android.os.CountDownTimer
import com.example.pomodoro.databinding.ItemBinding

interface StopwatchListener {
    fun start(id: Int)
    fun stop(id: Int, currentMs: Long)
    fun reset(id: Int)
    fun delete(id: Int)
    fun stopOtherStopwatches(id: Int): List<Stopwatch>
    fun getTimer(id: Int): CountDownTimer?
    fun setTimer(id: Int, newTimer: CountDownTimer?)
    fun setText(stopwatch: Stopwatch, binding: ItemBinding)
}