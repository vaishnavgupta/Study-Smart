package com.example.studysmart.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    val title:String,
    val description:String,
    val dueDate:Long,
    val priority:Int,
    val isCompleted:Boolean,
    val relatedToSubjects:String,
    val taskSubjectId:Int,
    @PrimaryKey(autoGenerate = true)
    val taskId:Int?=null
)
