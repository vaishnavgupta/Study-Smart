package com.example.studysmart.presentation.sessions

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.example.studysmart.MainActivity
import com.example.studysmart.utils.Constant.CLICK_REQ_CODE

object SessionServiceHelper {

    fun clickPendingIntent(context: Context):PendingIntent{
        val deepLinkIntent=Intent(
            Intent.ACTION_VIEW,
            "study_smart://sessionScreen".toUri(),
            context,
            MainActivity::class.java
        )
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(
                CLICK_REQ_CODE,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    fun triggerForegroundService(context: Context, action :String){
        Intent(context,SessionTimerService::class.java).apply {
            this.action=action
            context.startService(this)
        }

    }
}