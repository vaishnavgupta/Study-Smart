package com.example.studysmart.data.repository

import com.example.studysmart.data.local.SessionDao
import com.example.studysmart.data.local.SubjectDao
import com.example.studysmart.data.local.TaskDao
import com.example.studysmart.domain.models.Subjects
import com.example.studysmart.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubjectRepositoryImpl@Inject constructor(
    private val subjectDao: SubjectDao,
    private val taskDao:TaskDao,
    private val sessionDao: SessionDao
):SubjectRepository {

    override suspend fun upsertSubject(subject: Subjects) {
        subjectDao.upsertSubject(subject)
    }

    override fun getTotalSubjectCount(): Flow<Int> {
        return subjectDao.getTotalSubjectCount()
    }

    override fun getTotalGoalHours(): Flow<Float> {
        return subjectDao.getTotalGoalHours()
    }

    override suspend fun getSubjectById(subId: Int): Subjects? {
        return subjectDao.getSubjectById(subId)
    }

    override suspend fun deleteSubjectById(subId: Int) {
        //Deleting related sessions and tasks
        sessionDao.deleteSessionsBySubjectId(subId)
        taskDao.deleteTaskBySubjectId(subId)
        subjectDao.deleteSubjectById(subId)
    }

    override fun getAllSubjects(): Flow<List<Subjects>> {
        return subjectDao.getAllSubjects()
    }
}