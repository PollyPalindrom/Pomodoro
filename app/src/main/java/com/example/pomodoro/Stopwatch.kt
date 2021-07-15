package com.example.pomodoro

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    var limit: Long,
    var startTime: Long,
    var isStarted: Boolean
)