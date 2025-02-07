package com.example.studysmart.presentation.subject

import androidx.compose.ui.graphics.Color
import com.example.studysmart.domain.models.Session
import com.example.studysmart.domain.models.Task

sealed class SubjectEvent {

    data object updateSubject:SubjectEvent()

    data object deleteSubject:SubjectEvent()

    data object deleteSession:SubjectEvent()

    data object updateProgress:SubjectEvent()

    data class subjectCardColorChange(val colors:List<Color>): SubjectEvent()

    data class onSubjectNameChange(val name:String): SubjectEvent()

    data class onSubjectGoalHrsChange(val hrs:String): SubjectEvent()

    data class onTaskIsCompleteChange(val task: Task): SubjectEvent()

    data class onDeleteSessionBtnClick(val session: Session): SubjectEvent()
}