package com.example.studysmart.presentation.subject

import androidx.compose.ui.graphics.Color
import com.example.studysmart.domain.models.Session
import com.example.studysmart.domain.models.Subjects
import com.example.studysmart.domain.models.Task

data class SubjectState (
    val currentSubjectId:Int?=null,
    val subjectName:String="",
    val goalStudyHours:String="",
    val subjectCardColor:List<Color> = Subjects.subjectCardColors.random(),
    val studiedHours:Float=0f,
    val progress:Float=0f,
    val recentSessions:List<Session> = emptyList(),
    val completedTasks:List<Task> = emptyList(),
    val upcomingTasks:List<Task> = emptyList(),
    val session:Session?=null,

)
