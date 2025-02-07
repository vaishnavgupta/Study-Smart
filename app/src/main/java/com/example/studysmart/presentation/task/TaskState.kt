package com.example.studysmart.presentation.task

import com.example.studysmart.domain.models.Subjects
import com.example.studysmart.utils.Priority

data class TaskState(
    val taskTitle:String="",
    val taskDescription:String="",
    val subjects:List<Subjects> = emptyList(),
    val isTaskComplete:Boolean=false,
    val priority: Priority=Priority.MEDIUM,
    val subjectId:Int? = null,
    val currentTaskId:Int?=null,
    val dueDate:Long?=null,
    val relatedToSubject:String?=null
    )
