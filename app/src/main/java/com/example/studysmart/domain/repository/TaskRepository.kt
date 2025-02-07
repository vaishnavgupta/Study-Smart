package com.example.studysmart.domain.repository

import com.example.studysmart.domain.models.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun upsertTask(task: Task)

    suspend fun deleteTaskByTaskId(taskId:Int)

    suspend fun getTaskByTaskId(taskId:Int): Task?

    fun getUpcomingTaskForSubject(subId:Int): Flow<List<Task>>

    fun getCompletedTaskForSubject(subId:Int): Flow<List<Task>>

    fun getAllUpcomingTasks(): Flow<List<Task>>
}