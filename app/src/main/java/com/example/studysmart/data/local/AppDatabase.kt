package com.example.studysmart.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.studysmart.domain.models.Session
import com.example.studysmart.domain.models.Subjects
import com.example.studysmart.domain.models.Task

@Database(
    entities = [Subjects::class,Task::class,Session::class],
    version = 1
)

@TypeConverters(ColorListConvertor::class)
abstract class AppDatabase:RoomDatabase(){

    abstract fun subjectDao():SubjectDao
    abstract fun taskDao():TaskDao
    abstract fun sessionDao():SessionDao
}