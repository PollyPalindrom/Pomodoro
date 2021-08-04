package com.example.pomodoro

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Stopwatch(
    @PrimaryKey var id:Int,
    var currentMs: Long,
    var limit: Long,
    var isStarted: Boolean,
    var shouldBeRestarted: Boolean
)