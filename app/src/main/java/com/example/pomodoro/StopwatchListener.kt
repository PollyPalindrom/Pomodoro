package com.example.pomodoro

import com.example.pomodoro.databinding.ItemBinding

interface StopwatchListener {
    fun start(id: Int)
    fun stop(id: Int, currentMs: Long)
    fun reset(id: Int)
    fun delete(id: Int)
    fun stopOtherStopwatches(id: Int):List<Stopwatch>
}