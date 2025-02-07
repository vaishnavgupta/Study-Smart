package com.example.studysmart.data.repository

import com.example.studysmart.data.local.TaskDao
import com.example.studysmart.domain.models.Task
import com.example.studysmart.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

//@Inject constructor specifies that we will provide the value of taskDao at some point
// DaggerHilt will help us to do so

class TaskRepositoryImpl @Inject constructor(
    private val taskDao:TaskDao
):TaskRepository {
    override suspend fun upsertTask(task: Task) {
        taskDao.upsertTask(task)
    }

    override suspend fun deleteTaskByTaskId(taskId: Int) {
        taskDao.deleteTaskByTaskId(taskId)
    }

    override suspend fun getTaskByTaskId(taskId: Int): Task? {
        return taskDao.getTaskByTaskId(taskId)
    }

    override fun getUpcomingTaskForSubject(subId: Int): Flow<List<Task>> {
        return taskDao.getTaskForSubject(subId)
            .map { tasks -> tasks.filter { it.isCompleted.not() } }
            .map { tasks -> sortTasks(tasks) }
    }

    override fun getCompletedTaskForSubject(subId: Int): Flow<List<Task>> {
        return taskDao.getTaskForSubject(subId)
            .map { tasks -> tasks.filter { it.isCompleted } }
            .map { tasks -> sortTasks(tasks) }
    }

    override fun getAllUpcomingTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
            .map { tasks->tasks.filter { it.isCompleted.not() } }
            .map { tasks -> sortTasks(tasks) }
    }

    private fun sortTasks(tasks: List<Task>): List<Task> {
        return tasks.sortedWith(compareBy<Task> { it.dueDate }.thenByDescending { it.priority })
    }

}