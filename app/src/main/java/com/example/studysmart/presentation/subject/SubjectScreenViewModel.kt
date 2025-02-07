package com.example.studysmart.presentation.subject

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysmart.domain.models.Subjects
import com.example.studysmart.domain.models.Task
import com.example.studysmart.domain.repository.SessionRepository
import com.example.studysmart.domain.repository.SubjectRepository
import com.example.studysmart.domain.repository.TaskRepository
import com.example.studysmart.presentation.navArgs
import com.example.studysmart.utils.SnackBarEvent
import com.example.studysmart.utils.toHours
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SubjectScreenViewModel@Inject constructor(
    private val subjectRepository:SubjectRepository,
    private val taskRepository:TaskRepository,
    private val sessionRepository: SessionRepository,
    saveStateHandle:SavedStateHandle        //for getting navArgs from previous screen

) :ViewModel(){

    private val navArgs:SubjectScreenNavArgs = saveStateHandle.navArgs()

    private val _state = MutableStateFlow(SubjectState())

    val state= combine(
        _state,
        taskRepository.getUpcomingTaskForSubject(navArgs.subjectId),
        taskRepository.getCompletedTaskForSubject(navArgs.subjectId),
        sessionRepository.getRecentTenSessionsForSubject(navArgs.subjectId),
        sessionRepository.getTotalSessionsDurationBySubject(navArgs.subjectId)
    ){ _state,upcomingTasks,completedTasks,recentTenSess,totalSessDur->
        _state.copy(
            upcomingTasks=upcomingTasks,
            completedTasks = completedTasks,
            recentSessions = recentTenSess,
            studiedHours = totalSessDur.toHours()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubjectState()
    )

    private val _snackbarEvenFlow= MutableSharedFlow<SnackBarEvent>()  //because it does not hold any value
    val snackbarEventFlow=_snackbarEvenFlow.asSharedFlow()

    init {
        fetchSubject()    //calls automatically when SubjectScreenViewModel is created
    }


    fun manageEventChanges(event: SubjectEvent){
        when(event){
            SubjectEvent.deleteSession -> deleteSessionFromDb()
            SubjectEvent.deleteSubject -> deleteSub()
            is SubjectEvent.onDeleteSessionBtnClick -> {
                _state.update {
                    it.copy(session = event.session)
                }
            }
            is SubjectEvent.onSubjectGoalHrsChange -> {
                _state.update {
                    it.copy(goalStudyHours = event.hrs)
                }
            }
            is SubjectEvent.onSubjectNameChange -> {
                _state.update {
                    it.copy(subjectName = event.name)
                }
            }
            is SubjectEvent.onTaskIsCompleteChange -> updateTsk(event.task)
            is SubjectEvent.subjectCardColorChange -> {
                _state.update {
                    it.copy(subjectCardColor = event.colors)
                }
            }
            SubjectEvent.updateSubject -> updateSub()
            SubjectEvent.updateProgress -> updateProgressBar()
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

    private fun updateTsk(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.upsertTask(
                    task = task.copy(
                        isCompleted = !task.isCompleted
                    )
                )
                if(task.isCompleted){
                    _snackbarEvenFlow.emit(
                        SnackBarEvent.ShowSnackbar(
                            msg = "Saved in upcoming tasks."
                        )
                    )
                }else{
                    _snackbarEvenFlow.emit(
                        SnackBarEvent.ShowSnackbar(
                            msg = "Saved in completed tasks."
                        )
                    )
                }
            }catch(e:Exception){
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Failed to update task.${e.message}.",
                        snackBarDuration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

    private fun deleteSub() {
        viewModelScope.launch {
            try {
                val subID=state.value.currentSubjectId
                if(subID!=null){
                    withContext(Dispatchers.IO){
                        subjectRepository.deleteSubjectById(subId = subID)
                    }
                    _snackbarEvenFlow.emit(
                        SnackBarEvent.ShowSnackbar(
                            msg = "Subject deleted successfully."
                        )
                    )
                    _snackbarEvenFlow.emit(SnackBarEvent.navigateUp)
                }
                else{
                    _snackbarEvenFlow.emit(
                        SnackBarEvent.ShowSnackbar(
                            msg = "No subject to be deleted."
                        )
                    )
                }
            }catch (e:Exception){
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Failed to delete subject.${e.message}",
                        snackBarDuration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

    private fun updateProgressBar() {
        val goalHrs=state.value.goalStudyHours.toFloatOrNull()?:1f
        _state.update {
            it.copy(
                progress = (state.value.studiedHours/goalHrs).coerceIn(0f,1f)
            )
        }
    }

    private fun updateSub() {
        viewModelScope.launch {
            try{
                subjectRepository.upsertSubject(
                    Subjects(
                        name = state.value.subjectName,
                        subjectId = state.value.currentSubjectId,
                        goalHrs = state.value.goalStudyHours.toFloatOrNull()?:1f,
                        colors = state.value.subjectCardColor.map { it.toArgb() }
                    )
                )
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Subject updated successfully."
                    )
                )
            }catch (e:Exception){
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Failed to update subject.${e.message}",
                        snackBarDuration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

    private fun fetchSubject(){
        viewModelScope.launch {
            subjectRepository.getSubjectById(navArgs.subjectId)?.let { subj->
                _state.update {
                    it.copy(
                        subjectName = subj.name,
                        goalStudyHours = subj.goalHrs.toString(),
                        subjectCardColor = subj.colors.map { Color(it) },
                        currentSubjectId = subj.subjectId
                    )
                }
            }
        }
    }

}

