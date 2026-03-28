package com.example.taskcatalog.repository

import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus

interface TaskRepository {
    fun save(task: Task): Task
    fun findById(id: Long): Task?
    fun findAll(page: Int, size: Int, status: TaskStatus?): List<Task>
    fun countAll(status: TaskStatus?): Long
    fun updateStatus(id: Long, status: TaskStatus): Boolean
    fun deleteById(id: Long): Boolean
}
