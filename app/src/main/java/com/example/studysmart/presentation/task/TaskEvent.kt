package com.example.studysmart.presentation.task

import com.example.studysmart.domain.models.Subjects
import com.example.studysmart.utils.Priority

sealed class TaskEvent {

    data class onTitleChange(val title:String):TaskEvent()

    data class onDescChange(val desc:String):TaskEvent()

    data class onDateChange(val millis: Long?):TaskEvent()

    data class onPriorityChange(val priority:Priority):TaskEvent()

    data class onRelatedToSubjectChange(val subject: Subjects):TaskEvent()

    data object onIsCompleteChange:TaskEvent()

    data object saveTask:TaskEvent()

    data object deleteTask:TaskEvent()

}