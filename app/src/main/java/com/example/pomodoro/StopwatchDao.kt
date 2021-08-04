package com.example.pomodoro

import androidx.room.*


@Dao
interface StopwatchDao {
    @Query("SELECT * FROM stopwatch")
    fun getAll(): List<Stopwatch?>?

    @Insert(entity = Stopwatch::class, onConflict = OnConflictStrategy.REPLACE)
    fun insert(stopwatches: Stopwatch?)

    @Update
    fun update(stopwatches: Stopwatch?)

    @Delete
    fun delete(stopwatches: Stopwatch?)
}