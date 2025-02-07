package com.example.studysmart.presentation.sessions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.studysmart.utils.Constant.ACTION_SERVICE_CANCEL
import com.example.studysmart.utils.Constant.ACTION_SERVICE_START
import com.example.studysmart.utils.Constant.ACTION_SERVICE_STOP
import com.example.studysmart.utils.Constant.NOTIFICATION_CHANNEL_ID
import com.example.studysmart.utils.Constant.NOTIFICATION_CHANNEL_NAME
import com.example.studysmart.utils.Constant.NOTIFICATION_ID
import com.example.studysmart.utils.toStringWithPad
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// Service class are used when we want our app to perform long running tasks
// without the need of UI
// eg music / timer / fetching data

@AndroidEntryPoint   // because using di
class SessionTimerService:Service() {

    @Inject
    lateinit var notificationBuilder:NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager


    private val binder=StudySessionTimerBinder()

    private lateinit var timer: Timer
    var duration: Duration = Duration.ZERO
        private set
    var seconds = mutableStateOf("00")
        private set
    var minutes = mutableStateOf("00")
        private set
    var hours = mutableStateOf("00")
        private set
    var currentTimerState= mutableStateOf(TimerState.IDLE)
        private set
    var subjectId = mutableStateOf<Int?>(null)

    // when services depends on Component of app
    // (eg here we are getting data from Service and show it in SessionScreen)
    override fun onBind(p0: Intent?)  = binder

    // service that are independent of any Component
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action.let {
            when(it){
                ACTION_SERVICE_START -> {
                    startForegroundService()
                    startTimer { h, m, s ->
                        updateNotificationWithNewValues(h,m,s)
                    }
                }
                ACTION_SERVICE_STOP -> {
                    stopTimer()
                }
                ACTION_SERVICE_CANCEL -> {
                    stopTimer()
                    cancelTimer()
                    stopForegroundService()
                }

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(){
        createNotificationChannel()
        startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }

    private fun stopForegroundService(){
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel(){
        val ntfChnl = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        //getting notification manager using Dagger-Hilt
        notificationManager.createNotificationChannel(ntfChnl)
    }

    private fun updateNotificationWithNewValues(
        hours:String,
        minutes:String,
        seconds:String
    ){
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.setContentText(
                "$hours:$minutes:$seconds"
            )
                .build()
        )
    }

    private fun startTimer(
        onTick:(h:String,m:String,s:String) -> Unit
    ){
        currentTimerState.value=TimerState.STARTED
        timer = fixedRateTimer(
            initialDelay = 1000L,
            period = 1000L
        ){
            duration  = duration.plus(1.seconds)
            updateTimeUnits()
            onTick(hours.value,minutes.value,seconds.value)
        }

    }

    private fun stopTimer(){
        if(this::timer.isInitialized){
            timer.cancel()
        }
        currentTimerState.value=TimerState.STOPPED
    }

    private fun cancelTimer(){
        duration = Duration.ZERO
        updateTimeUnits()
        currentTimerState.value=TimerState.IDLE
    }

    private fun updateTimeUnits(
    ){
        duration.toComponents { hours, minutes, seconds, _ ->
            this@SessionTimerService.hours.value=hours.toInt().toStringWithPad()
            this@SessionTimerService.minutes.value=minutes.toStringWithPad()
            this@SessionTimerService.seconds.value=seconds.toStringWithPad()
        }
    }

    inner class StudySessionTimerBinder:Binder(){
        fun getTimerService():SessionTimerService = this@SessionTimerService
    }

}
enum class TimerState{
    IDLE,STARTED,STOPPED
}