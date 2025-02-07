package com.example.studysmart.data.dependencyInjc

import android.app.Application
import androidx.room.Room
import com.example.studysmart.data.local.AppDatabase
import com.example.studysmart.data.local.SessionDao
import com.example.studysmart.data.local.SubjectDao
import com.example.studysmart.data.local.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


// DependencyInjection File
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    //For Room Database
    @Provides
    @Singleton
    fun provideDatabase(
        application: Application
    ):AppDatabase{
        return Room.databaseBuilder(
            application,   //for context we use application parameter
            AppDatabase::class.java,
            "StudySmart.db"
        )
            .build()
    }

    @Provides
    @Singleton
    fun provideSubjectDao(database:AppDatabase):SubjectDao{
        return database.subjectDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database:AppDatabase):TaskDao{
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database:AppDatabase):SessionDao{
        return database.sessionDao()
    }

}