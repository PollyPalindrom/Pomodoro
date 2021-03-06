package com.example.pomodoro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class ForegroundService : Service() {
    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null
    private var job: Job? = null
    private var currentTime: Long = 0L
    private var timeOverSound: MediaPlayer? = null
    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Simple Timer")
            .setGroup("Timer").setGroupSummary(false).setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(getPendingIntent())
            .setSilent(true).setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
    }

    override fun onCreate() {
        super.onCreate()
        timeOverSound = MediaPlayer.create(this, R.raw.zvukgonga)
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processCommand(intent)
        return START_REDELIVER_INTENT

    }

    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> {
                val startTime =
                    intent?.extras?.getLong(STARTED_TIMER_TIME_MS) ?: return
                commandStart(startTime)
            }
            COMMAND_STOP -> commandStop()
            INVALID -> return
        }

    }

    private fun commandStop() {
        if (!isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStop()")
        try {
            job?.cancel()
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    private fun commandStart(startTime: Long) {
        if (isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStart()")
        Log.i("TAG", startTime.toString())
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer(startTime)
        } finally {
            isServiceStarted = true
        }
    }

    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("content")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, channelName, importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("TAG", "moveToStartedState(): Running on Android O or higher")
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            Log.d("TAG", "moveToStartedState(): Running on Android N or lower")
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    private fun continueTimer(startTime: Long) {
        currentTime = startTime
        job = GlobalScope.launch(Dispatchers.Main) {
            while (true) {

                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(
                        currentTime.displayTime()
                    )
                )
                currentTime -= INTERVAL
                delay(INTERVAL)
                if (currentTime <= 0L) {
                    notificationManager?.notify(
                        NOTIFICATION_ID,
                        getNotification(
                            "Timer is finished!!!"
                        )
                    )
                    timeOverSound?.start()
                    delay(6 * INTERVAL)
                    try {
                        job?.cancel()
                        stopForeground(true)
                        stopSelf()
                    } finally {
                        isServiceStarted = false
                    }
                    break
                }
            }
        }

    }

    private fun getPendingIntent(): PendingIntent? {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun Long.displayTime(): String {
        if (this <= 0L) {
            return START_TIME
        }
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {
        private const val START_TIME = "00:00:00"
        private const val INVALID = "INVALID"
        private const val COMMAND_START = "COMMAND_START"
        private const val COMMAND_STOP = "COMMAND_STOP"
        private const val COMMAND_ID = "COMMAND_ID"
        private const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME_MS"
        private const val CHANNEL_ID = "Channel_ID"
        private const val NOTIFICATION_ID = 777
        private const val INTERVAL = 1000L
    }
}