package com.example.studysmart.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.studysmart.domain.models.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Upsert
    suspend fun upsertTask(task:Task)

    @Query("DELETE FROM TASK WHERE taskId=:taskId")
    suspend fun deleteTaskByTaskId(taskId:Int)

    @Query("DELETE FROM TASK WHERE taskSubjectId=:subId")
    suspend fun deleteTaskBySubjectId(subId:Int)

    @Query("SELECT * FROM TASK WHERE taskId=:taskId")
    suspend fun getTaskByTaskId(taskId:Int):Task?

    @Query("SELECT * FROM TASK WHERE taskSubjectId=:subId")
    fun getTaskForSubject(subId:Int):Flow<List<Task>>

    @Query("SELECT * FROM TASK")
    fun getAllTasks():Flow<List<Task>>

}