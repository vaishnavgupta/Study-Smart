package com.example.studysmart.presentation.dashboard

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysmart.domain.models.Session
import com.example.studysmart.domain.models.Subjects
import com.example.studysmart.domain.models.Task
import com.example.studysmart.domain.repository.SessionRepository
import com.example.studysmart.domain.repository.SubjectRepository
import com.example.studysmart.domain.repository.TaskRepository
import com.example.studysmart.utils.SnackBarEvent
import com.example.studysmart.utils.toHours
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardScreenViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val sessionRepository:SessionRepository,
    private val taskRepository:TaskRepository
) :ViewModel(){

    private val _state= MutableStateFlow(DashboardState())

    val state= combine(
        _state,
        subjectRepository.getTotalSubjectCount(),
        subjectRepository.getTotalGoalHours(),
        subjectRepository.getAllSubjects(),
        sessionRepository.getTotalSessionsDuration()
    ){ _state,subCount,goalHrs,allSubs,sessDurt->
        _state.copy(
            totalSubjectCount = subCount,
            totalGoalStudyHours = goalHrs,
            totalStudiedHours = sessDurt.toHours(),   //extension function
            subjectList = allSubs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )

    //We are using different stateIn because stateIn does not accept more than 5 values
    val tasks:StateFlow<List<Task>> = taskRepository.getAllUpcomingTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentSessions:StateFlow<List<Session>> = sessionRepository.getRecentFiveSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _snackbarEvenFlow= MutableSharedFlow<SnackBarEvent>()  //because it does not hold any value
    val snackbarEventFlow=_snackbarEvenFlow.asSharedFlow()

    fun manageEventChanges(event: DashboardEvent){
        when(event){
            //does deleteSubject Work
            DashboardEvent.deleteSession -> deleteSessionFromDb()
            is DashboardEvent.onDeleteSessionBtnClick -> {
                _state.update {
                    it.copy(
                        session = event.session
                    )
                }
            }
            is DashboardEvent.onSubjectGoalHrsChange -> {
                _state.update {
                    it.copy(
                        goalStudyHours = event.hrs
                    )
                }
            }
            is DashboardEvent.onSubjectNameChange -> {
                _state.update {
                    it.copy(
                        subjectName = event.name
                    )
                }
            }
            is DashboardEvent.onTaskIsCompleteChange -> {updateTsk(event.task)}
            DashboardEvent.saveSubject -> saveSubj()
            is DashboardEvent.subjectCardColorChange -> {
                _state.update {
                    it.copy(
                        subjectCardColors = event.colors
                    )
                }
            }
        }
    }

    private fun updateTsk(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.upsertTask(
                    task = task.copy(
                        isCompleted = !task.isCompleted
                    )
                )
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Saved in completed tasks successfully."
                    )
                )
            }catch(e:Exception){
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Failed to complete task.${e.message}.",
                        snackBarDuration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

    private fun deleteSessionFromDb(){
        viewModelScope.launch {
            try {
                state.value.session?.let {
                    sessionRepository.deleteSession(it)
                    _snackbarEvenFlow.emit(
                        SnackBarEvent.ShowSnackbar(
                            msg = "Study Session Deleted Successfully.",
                        )
                    )
                }
            }catch (e:Exception){
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Failed to delete Study Session. ${e.message}.",
                        snackBarDuration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

    private fun saveSubj() {
        viewModelScope.launch {
            try {
                subjectRepository.upsertSubject(
                    subject = Subjects(
                        name = state.value.subjectName,
                        goalHrs = state.value.goalStudyHours.toFloatOrNull()?:1f,
                        colors = state.value.subjectCardColors.map { it.toArgb() }
                    )
                )
                _state.update {
                    it.copy(
                        subjectName = "",
                        goalStudyHours = "",
                        subjectCardColors = Subjects.subjectCardColors.random()
                    )
                }
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Subject added successfully."
                    )
                )
            }catch (e:Exception){
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Unable to add subject.${e.message}",SnackbarDuration.Long
                    )
                )
            }
        }
    }
}