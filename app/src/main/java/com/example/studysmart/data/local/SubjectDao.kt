package com.example.studysmart.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.studysmart.domain.models.Subjects
import kotlinx.coroutines.flow.Flow

/*
Important : Room also supports Coroutine
Diff between suspend and flow
Suspend -> Used for one time events (data needs not to be updated continuously)
Flow -> Used for regular events (data needs to be updated continuously)
 */
@Dao
interface SubjectDao {

    @Upsert
    suspend fun upsertSubject(subject: Subjects)   //Insert new subject if not exist else it will update the subject

    @Query("SELECT COUNT(*) FROM SUBJECTS")
    fun getTotalSubjectCount():Flow<Int>

    @Query("SELECT SUM(goalHrs) FROM SUBJECTS")
    fun getTotalGoalHours():Flow<Float>

    @Query("SELECT * FROM SUBJECTS WHERE subjectId=:subId")
    suspend fun getSubjectById(subId:Int):Subjects?

    @Query("DELETE FROM SUBJECTS WHERE subjectId=:subId")
    suspend fun deleteSubjectById(subId: Int)

    @Query("SELECT * FROM SUBJECTS")
    fun getAllSubjects(): Flow<List<Subjects>>
}