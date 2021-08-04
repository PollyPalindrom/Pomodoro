package com.example.pomodoro

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Stopwatch::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stopwatchDao(): StopwatchDao?
}