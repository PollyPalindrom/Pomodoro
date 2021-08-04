package com.example.pomodoro

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Stopwatch(
    var currentMs: Long,
    var limit: Long,
    var isStarted: Boolean,
    var shouldBeRestarted: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}