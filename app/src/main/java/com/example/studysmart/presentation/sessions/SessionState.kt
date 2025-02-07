package com.example.studysmart.presentation.sessions

import com.example.studysmart.domain.models.Session
import com.example.studysmart.domain.models.Subjects

data class SessionState(
    val subjects:List<Subjects> = emptyList(),
    val relatedToSubject:String? = null,
    val recentSessions:List<Session> = emptyList(),
    val subjectId:Int? = null,
    val session:Session? = null
)
