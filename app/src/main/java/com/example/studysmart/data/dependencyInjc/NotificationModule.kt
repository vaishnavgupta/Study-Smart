package com.example.studysmart.data.dependencyInjc

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.studysmart.R
import com.example.studysmart.presentation.sessions.SessionServiceHelper
import com.example.studysmart.utils.Constant.NOTIFICATION_CHANNEL_ID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
class NotificationModule {

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context
    ):NotificationCompat.Builder{
        return NotificationCompat
            .Builder(context,NOTIFICATION_CHANNEL_ID)
            .setContentTitle("StudySmart Study Session")
            .setContentText("00:00:00")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(false)
            .setContentIntent(SessionServiceHelper.clickPendingIntent(context))
    }

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ):NotificationManager{
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }





}