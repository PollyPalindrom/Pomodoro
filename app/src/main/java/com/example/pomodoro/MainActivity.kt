package com.example.pomodoro

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.databinding.ItemBinding


class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding
    private val stopwatchAdapter = StopwatchAdapter(this)
    private var timer: CountDownTimer? = null
    private var timeOverSound: MediaPlayer? = null
    private var instance: MainActivity? = null
    private var stopwatchId: Int = 0
    private var stopwatchDao: StopwatchDao? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        instance = this
        val database =
            Room.databaseBuilder(this, AppDatabase::class.java, "stopwatches")
                .allowMainThreadQueries()
                .build()
        stopwatchDao = database.stopwatchDao()
        if (stopwatchDao?.getAll()?.size != 0) {
            stopwatchAdapter.setStopwatches(stopwatchDao?.getAll() as List<Stopwatch>)
        }
        timeOverSound = MediaPlayer.create(this, R.raw.zvukgonga)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.addNewStopwatchButton.setOnClickListener {
            if (checkNumber(binding.minutes.text.toString())) {

                stopwatchAdapter.addStopwatch(
                    Stopwatch(
                        0,
                        binding.minutes.text.toString().toLong() * 60L * 1000L,
                        isStarted = false,
                        shouldBeRestarted = false
                    )
                )
                stopwatchId++
            } else {
                Toast.makeText(
                    this.applicationContext,
                    "Wrong input :3 Max value is 24:00:00 in minutes. Min value is 00:01:00 in minutes",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        val touchHelper = ItemTouchHelper(SimpleItemTouchHelperCallback(stopwatchAdapter))
        touchHelper.attachToRecyclerView(binding.recycler)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (stopwatchAdapter.timerIsStarted() == true) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, stopwatchAdapter.getCurrentTimerTime())
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    override fun onStop() {
        if (stopwatchDao?.getAll()?.size == 0) {
            stopwatchAdapter.getStopwatches().forEach { stopwatchDao?.insert(it) }
        } else {
            stopwatchDao?.getAll()?.forEach {
                if (it != null) {
                    if (!checkList(stopwatchAdapter.getStopwatches(), it.id)) {
                        stopwatchDao?.delete(it)
                    }
                }
            }
            stopwatchAdapter.getStopwatches().forEach { stopwatch ->
                if (checkList(
                        stopwatchDao?.getAll() as List<Stopwatch>,
                        stopwatch.id
                    )
                ) stopwatchDao?.update(stopwatch)
                else stopwatchDao?.insert(stopwatch)
            }

        }
        super.onStop()
    }

    private fun checkList(list: List<Stopwatch>, id: Int): Boolean {
        var temp: Boolean = false
        list.forEach { if (it.id == id) temp = true }
        return temp
    }

    private fun checkNumber(numberToCompare: String): Boolean {
        var sum: Long = 0
        try {
            numberToCompare.toLong()
        } catch (e: NumberFormatException) {
            return false
        }
        if (numberToCompare == "") return false
        if (numberToCompare.toLong() <= 0L) return false
        if (numberToCompare.toLong() <= 1440L) return true
        if (numberToCompare.toLong() > 1440L) return false
        else {
            for (i in numberToCompare.indices) {
                sum = sum * 10L + numberToCompare[i].toInt()
                if (sum > 1440L) return false
            }
            return true
        }
    }

    override fun start(position: Int, stopwatch: Stopwatch, itemBinding: ItemBinding) {
        stopwatchAdapter.changeStopwatch(
            position, null,
            isStarted = true,
            shouldBeRestarted = false
        )
        timer?.cancel()
        if (stopwatch.currentMs == 0L) stopwatch.currentMs = stopwatch.limit
        setTimer(position, stopwatch, itemBinding)
        timer?.start()
        if (!itemBinding.blinkingIndicator.isInvisible) {//работает каждый раз, когда стартует таймер
            itemBinding.customView.setPeriod((stopwatch.limit))
        }
    }


    override fun stop(position: Int, stopwatch: Stopwatch, currentMs: Long) {
        stopwatchAdapter.changeStopwatch(
            position, currentMs,
            isStarted = false,
            shouldBeRestarted = false
        )
        timer?.cancel()
    }

    override fun reset(position: Int, stopwatch: Stopwatch, itemBinding: ItemBinding) {
        stopwatch.shouldBeRestarted = false
        stopwatch.currentMs = 0L
        setText(stopwatch, itemBinding)
        if (stopwatch.isStarted) timer?.cancel()
        stopwatchAdapter.changeStopwatch(
            position, 0,
            isStarted = false,
            shouldBeRestarted = false
        )
        itemBinding.customView.setPeriod(0)
        itemBinding.customView.setCurrent(0)
    }

    override fun delete(position: Int, stopwatch: Stopwatch) {
        if (stopwatch.isStarted) {
            timer?.cancel()
            timer = null
        }
        stopwatchAdapter.deleteStopwatch(position)
    }

    override fun stopOtherStopwatches() {
        stopwatchAdapter.stopOtherTimers()
        timer?.cancel()
    }

    override fun getMyTimer(): CountDownTimer? {
        return timer
    }

    override fun setTimer(position: Int, stopwatch: Stopwatch, itemBinding: ItemBinding) {
        timer = object : CountDownTimer(stopwatch.currentMs, UNIT_TEN_MS) {
            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs = millisUntilFinished
                setText(stopwatch, itemBinding)
//                stopwatchAdapter.notifyDataSetChanged()

                if (!itemBinding.blinkingIndicator.isInvisible) itemBinding.customView.setCurrent(
                    stopwatch.currentMs
                )
            }

            override fun onFinish() {
                timeOverSound?.start()
                setText(stopwatch, itemBinding)
                stopwatch.shouldBeRestarted = true
                stopwatchAdapter.changeStopwatch(
                    position, stopwatch.limit,
                    isStarted = false,
                    shouldBeRestarted = true
                )
                timer?.cancel()
            }
        }
    }


    override fun setText(stopwatch: Stopwatch, binding: ItemBinding) {
        if (stopwatch.currentMs == 0L) {
            binding.stopwatchTimer.text =
                stopwatch.limit.displayTime()
        } else if (!binding.blinkingIndicator.isInvisible || !stopwatch.isStarted)
            binding.stopwatchTimer.text =
                stopwatch.currentMs.displayTime()
    }

    override fun getViewHolder(position: Int): RecyclerView.ViewHolder? {
        return binding.recycler.findViewHolderForAdapterPosition(position)
    }

    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return START_TIME
        }
        val h = this / 1000L / 3600L
        val m = this / 1000L % 3600L / 60L
        val s = this / 1000L % 60L
        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0L) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {
        private const val START_TIME = "00:00:00"
        private const val COMMAND_START = "COMMAND_START"
        private const val COMMAND_STOP = "COMMAND_STOP"
        private const val COMMAND_ID = "COMMAND_ID"
        private const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME_MS"
        private const val UNIT_TEN_MS = 1000L
    }

}