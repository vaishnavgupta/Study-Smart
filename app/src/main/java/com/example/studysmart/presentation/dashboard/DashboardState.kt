package com.example.studysmart.presentation.dashboard

import androidx.compose.ui.graphics.Color
import com.example.studysmart.domain.models.Session
import com.example.studysmart.domain.models.Subjects

//contains all the states of the screen

data class DashboardState(
    val totalSubjectCount:Int=0,
    val totalStudiedHours:Float=0f,
    val totalGoalStudyHours:Float=0f,
    val subjectList:List<Subjects> = emptyList(),
    //for Add Subject dialog
    val subjectName:String="",
    val goalStudyHours:String="",
    val subjectCardColors:List<Color> = Subjects.subjectCardColors.random(),
    val session: Session?=null
)
