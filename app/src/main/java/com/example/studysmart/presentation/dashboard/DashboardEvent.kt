package com.example.studysmart.presentation.dashboard

import androidx.compose.ui.graphics.Color
import com.example.studysmart.domain.models.Session
import com.example.studysmart.domain.models.Task


//for events such as User Actions (changing edit text , clicking)
sealed class DashboardEvent {

    data object saveSubject:DashboardEvent()

    data object deleteSession:DashboardEvent()

    data class subjectCardColorChange(val colors:List<Color>):DashboardEvent()

    data class onSubjectNameChange(val name:String):DashboardEvent()

    data class onSubjectGoalHrsChange(val hrs:String):DashboardEvent()

    data class onTaskIsCompleteChange(val task: Task):DashboardEvent()

    data class onDeleteSessionBtnClick(val session: Session):DashboardEvent()

}