package com.example.studysmart.domain.repository

import com.example.studysmart.domain.models.Subjects
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {

    suspend fun upsertSubject(subject: Subjects)   //Insert new subject if not exist else it will update the subject

    fun getTotalSubjectCount(): Flow<Int>

    fun getTotalGoalHours(): Flow<Float>

    suspend fun getSubjectById(subId:Int): Subjects?

    suspend fun deleteSubjectById(subId: Int)

    fun getAllSubjects(): Flow<List<Subjects>>

}