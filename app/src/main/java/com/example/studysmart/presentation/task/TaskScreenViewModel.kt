package com.example.studysmart.presentation.task

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studysmart.domain.models.Task
import com.example.studysmart.domain.repository.SubjectRepository
import com.example.studysmart.domain.repository.TaskRepository
import com.example.studysmart.presentation.navArgs
import com.example.studysmart.utils.Priority
import com.example.studysmart.utils.SnackBarEvent
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
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class TaskScreenViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    saveStateHandle: SavedStateHandle        //for getting navArgs from previous screen
):ViewModel(){

    private val _state=MutableStateFlow(TaskState())
    private val navArgs: TaskScreenNavArgs = saveStateHandle.navArgs()

    val state= combine(
        _state,
        subjectRepository.getAllSubjects()
    ){ _state,subjectList->
        _state.copy(
            subjects = subjectList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskState()
    )

    private val _snackbarEvenFlow= MutableSharedFlow<SnackBarEvent>()  //because it does not hold any value
    val snackbarEventFlow=_snackbarEvenFlow.asSharedFlow()

    init {
        fetchTaskDetails()
        fetchSubjectDetails()
    }

    fun manageEventChanges(event:TaskEvent){
        when(event){
            TaskEvent.deleteTask -> deleteTsk()
            is TaskEvent.onDateChange -> {
                _state.update {
                    it.copy(
                        dueDate = event.millis
                    )
                }
            }
            is TaskEvent.onDescChange -> {
                _state.update {
                    it.copy(
                        taskDescription = event.desc
                    )
                }
            }
            TaskEvent.onIsCompleteChange -> {
                //updating with its inverse value
                _state.update {
                    it.copy(
                        isTaskComplete = !state.value.isTaskComplete
                    )
                }
            }
            is TaskEvent.onPriorityChange -> {
                _state.update {
                    it.copy(
                        priority = event.priority
                    )
                }
            }
            is TaskEvent.onRelatedToSubjectChange -> {
                _state.update {
                    it.copy(
                        relatedToSubject = event.subject.name,
                        subjectId = event.subject.subjectId
                    )
                }
            }
            is TaskEvent.onTitleChange -> {
                _state.update {
                    it.copy(
                        taskTitle = event.title
                    )
                }
            }
            TaskEvent.saveTask -> saveThisTask()
        }
    }

    private fun deleteTsk() {
        viewModelScope.launch {
            try{
                val currTaskId=state.value.currentTaskId
                if(currTaskId!=null){
                    withContext(Dispatchers.IO){
                        taskRepository.deleteTaskByTaskId(currTaskId)
                    }
                    _snackbarEvenFlow.emit(
                        SnackBarEvent.ShowSnackbar(
                            msg = "Task deleted successfully."
                        )
                    )
                    _snackbarEvenFlow.emit(SnackBarEvent.navigateUp)
                }
                else{
                    _snackbarEvenFlow.emit(
                        SnackBarEvent.ShowSnackbar(
                            msg = "No task to be deleted.",
                            snackBarDuration = SnackbarDuration.Long
                        )
                    )
                }
            }catch (e:Exception){
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Failed to delete task.${e.message}",
                        snackBarDuration = SnackbarDuration.Long
                    )
                )
            }

        }
    }

    private fun saveThisTask() {
        viewModelScope.launch {
            val st=state.value
            if(st.relatedToSubject==null || st.subjectId==null) {
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Please select related to Subject."
                        , snackBarDuration = SnackbarDuration.Long
                    )
                )
                return@launch
            }
            try {
                taskRepository.upsertTask(
                    Task(
                        title = st.taskTitle,
                        description = state.value.taskDescription,
                        dueDate = state.value.dueDate ?: Instant.now().toEpochMilli(),    //takes current date
                        priority = state.value.priority.value,
                        isCompleted = state.value.isTaskComplete,
                        relatedToSubjects = st.relatedToSubject,
                        taskSubjectId = st.subjectId,
                        taskId = state.value.currentTaskId,
                    )
                )
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Task added successfully."
                    )
                )
                _snackbarEvenFlow.emit(SnackBarEvent.navigateUp)
            }catch (e:Exception){
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Failed to add Task. ${e.message}"
                        , snackBarDuration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

    private fun fetchTaskDetails(){
        viewModelScope.launch {
            navArgs.taskId?.let {
                taskRepository.getTaskByTaskId(it)?.let{ task->
                    _state.update {
                        it.copy(
                            taskTitle = task.title,
                            taskDescription = task.description,
                            dueDate = task.dueDate,
                            relatedToSubject = task.relatedToSubjects,
                            isTaskComplete = task.isCompleted,
                            priority = Priority.fromIntToPriority(task.priority),
                            currentTaskId = task.taskId,
                            subjectId = task.taskSubjectId
                        )
                    }
                }
            }
        }
    }

    private fun fetchSubjectDetails(){
        viewModelScope.launch {
            navArgs.subjectId?.let {
                subjectRepository.getSubjectById(subId = it)?.let { subject ->
                    _state.update {
                        it.copy(
                            relatedToSubject = subject.name,
                            subjectId = subject.subjectId
                        )
                    }
                }
            }
        }
    }
}