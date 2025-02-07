package com.example.studysmart.presentation.sessions

import com.example.studysmart.domain.models.Session
import com.example.studysmart.domain.models.Subjects

sealed class SessionEvent {
    data class OnRelatedSubjectChange(val subject: Subjects):SessionEvent()

    data class SaveSession(val duration:Long):SessionEvent()

    data class OnDeleteSessionBtnClick(val session: Session):SessionEvent()

    data object DeleteSession:SessionEvent()

    data object NotifyToUpdateSubId:SessionEvent()

    data class UpdateSubjectAndRelatedSession(
        val subjectId:Int?,
        val relatedToSubject:String?
    ) : SessionEvent()

}