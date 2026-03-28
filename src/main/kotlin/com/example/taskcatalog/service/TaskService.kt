package com.example.taskcatalog.service

import com.example.taskcatalog.dto.*
import com.example.taskcatalog.model.TaskStatus
import reactor.core.publisher.Mono

interface TaskService {
    fun createTask(request: CreateTaskRequest): Mono<TaskResponse>
    fun getTaskById(id: Long): Mono<TaskResponse>
    fun getTasks(page: Int, size: Int, status: TaskStatus?): Mono<PageResponse<TaskResponse>>
    fun updateStatus(id: Long, request: UpdateStatusRequest): Mono<TaskResponse>
    fun deleteTask(id: Long): Mono<Void>
}
