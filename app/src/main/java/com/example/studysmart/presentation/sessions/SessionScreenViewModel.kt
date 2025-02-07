package com.example.studysmart.presentation.sessions

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysmart.domain.models.Session
import com.example.studysmart.domain.repository.SessionRepository
import com.example.studysmart.domain.repository.SubjectRepository
import com.example.studysmart.utils.SnackBarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class SessionScreenViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val subjectRepository: SubjectRepository
):ViewModel() {

    private val _state = MutableStateFlow(SessionState())
    val state = combine(
        _state,
        subjectRepository.getAllSubjects(),
        sessionRepository.getAllSessions(),
    ){ _state,subjects,sessions ->
        _state.copy(
            subjects = subjects,
            recentSessions = sessions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionState()
    )

    private val _snackbarEvenFlow= MutableSharedFlow<SnackBarEvent>()  //because it does not hold any value
    val snackbarEventFlow=_snackbarEvenFlow.asSharedFlow()

    fun manageEventChanges(event: SessionEvent){
        when(event){
            SessionEvent.NotifyToUpdateSubId -> notifyToUpdtSubId()
            SessionEvent.DeleteSession -> deleteSessionFromDb()
            is SessionEvent.OnDeleteSessionBtnClick -> {
                _state.update {
                    it.copy(
                        session = event.session
                    )
                }
            }
            is SessionEvent.OnRelatedSubjectChange -> {
                _state.update {
                    it.copy(
                        relatedToSubject = event.subject.name,
                        subjectId = event.subject.subjectId
                    )
                }
            }
            is SessionEvent.SaveSession -> insertSessionInDb(event.duration)
            is SessionEvent.UpdateSubjectAndRelatedSession -> {
                _state.update {
                    it.copy(
                        relatedToSubject = event.relatedToSubject,
                        subjectId = event.subjectId
                    )
                }
            }
        }
    }

    private fun notifyToUpdtSubId() {
        viewModelScope.launch {
            if(state.value.subjectId==null || state.value.relatedToSubject==null){
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Select a Subject to start study session.",
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

    private fun insertSessionInDb(duration: Long) {
        // duration must be more than 36 seconds

        viewModelScope.launch {
            if (duration < 36){
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Study Session must be not less than 36 seconds.",
                        snackBarDuration = SnackbarDuration.Long
                    )
                )
                return@launch
            }
            try {
                val st = state.value
                sessionRepository.insertSession(
                    Session(
                        sessionSubjectId = st.subjectId ?: -1,
                        relatedToSub = st.relatedToSubject ?: "No_Subject",
                        date = Instant.now().toEpochMilli(),
                        duration = duration,
                    )
                )
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Study Session Saved Successfully.",
                    )
                )
            }catch (e:Exception){
                _snackbarEvenFlow.emit(
                    SnackBarEvent.ShowSnackbar(
                        msg = "Failed to add Study Session. ${e.message}.",
                        snackBarDuration = SnackbarDuration.Long
                    )
                )
            }
        }
    }
}