package com.example.taskcatalog.service

import com.example.taskcatalog.dto.*
import com.example.taskcatalog.exception.TaskNotFoundException
import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.repository.TaskRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.LocalDateTime
import kotlin.math.ceil

@Service
class TaskServiceImpl(private val taskRepository: TaskRepository) : TaskService {

    override fun createTask(request: CreateTaskRequest): Mono<TaskResponse> {
        return Mono.fromCallable {
            val now = LocalDateTime.now()
            val task = Task(
                id = 0,
                title = request.title,
                description = request.description,
                status = TaskStatus.NEW,
                createdAt = now,
                updatedAt = now
            )
            TaskResponse.from(taskRepository.save(task))
        }.subscribeOn(Schedulers.boundedElastic())
    }

    override fun getTaskById(id: Long): Mono<TaskResponse> {
        return Mono.fromCallable {
            taskRepository.findById(id) ?: throw TaskNotFoundException(id)
        }.subscribeOn(Schedulers.boundedElastic())
            .map { TaskResponse.from(it) }
    }

    override fun getTasks(page: Int, size: Int, status: TaskStatus?): Mono<PageResponse<TaskResponse>> {
        return Mono.fromCallable {
            val tasks = taskRepository.findAll(page, size, status)
            val total = taskRepository.countAll(status)
            val totalPages = if (total == 0L) 0 else ceil(total.toDouble() / size).toInt()
            PageResponse(
                content = tasks.map { TaskResponse.from(it) },
                page = page,
                size = size,
                totalElements = total,
                totalPages = totalPages
            )
        }.subscribeOn(Schedulers.boundedElastic())
    }

    override fun updateStatus(id: Long, request: UpdateStatusRequest): Mono<TaskResponse> {
        return Mono.fromCallable {
            val updated = taskRepository.updateStatus(id, request.status)
            if (!updated) throw TaskNotFoundException(id)
            taskRepository.findById(id) ?: throw TaskNotFoundException(id)
        }.subscribeOn(Schedulers.boundedElastic())
            .map { TaskResponse.from(it) }
    }

    override fun deleteTask(id: Long): Mono<Void> {
        return Mono.fromCallable {
            taskRepository.deleteById(id)
        }.subscribeOn(Schedulers.boundedElastic())
            .then()
    }
}
