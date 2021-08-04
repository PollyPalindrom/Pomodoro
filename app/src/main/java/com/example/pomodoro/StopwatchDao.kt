package com.example.pomodoro

import androidx.room.*


@Dao
interface StopwatchDao {
    @Query("SELECT * FROM stopwatch")
    fun getAll(): List<Stopwatch?>?

    @Query("SELECT * FROM stopwatch WHERE id = :id")
    fun getById(id: Long): Stopwatch?

    @Insert(entity=Stopwatch::class,onConflict = OnConflictStrategy.REPLACE)
    fun insert(stopwatches: Stopwatch?)

    @Update
    fun update(stopwatches: Stopwatch?)

    @Delete
    fun delete(stopwatches: Stopwatch?)
}