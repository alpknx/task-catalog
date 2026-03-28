package com.example.taskcatalog.service

import com.example.taskcatalog.dto.CreateTaskRequest
import com.example.taskcatalog.dto.UpdateStatusRequest
import com.example.taskcatalog.exception.TaskNotFoundException
import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import java.time.LocalDateTime

class TaskServiceTest {

    private val taskRepository: TaskRepository = mockk()
    private val taskService: TaskService = TaskServiceImpl(taskRepository)

    private val now = LocalDateTime.of(2024, 1, 1, 12, 0)

    private val sampleTask = Task(
        id = 1L,
        title = "Test Task",
        description = "A test task",
        status = TaskStatus.NEW,
        createdAt = now,
        updatedAt = now
    )

    @Test
    fun `createTask returns TaskResponse with status NEW`() {
        every { taskRepository.save(any()) } returns sampleTask

        val result = taskService.createTask(CreateTaskRequest(title = "Test Task", description = "A test task"))

        StepVerifier.create(result)
            .assertNext { response ->
                assertEquals(1L, response.id)
                assertEquals("Test Task", response.title)
                assertEquals("A test task", response.description)
                assertEquals(TaskStatus.NEW, response.status)
            }
            .verifyComplete()
    }

    @Test
    fun `getTaskById found returns correct TaskResponse`() {
        every { taskRepository.findById(1L) } returns sampleTask

        val result = taskService.getTaskById(1L)

        StepVerifier.create(result)
            .assertNext { response ->
                assertEquals(1L, response.id)
                assertEquals("Test Task", response.title)
                assertEquals(TaskStatus.NEW, response.status)
            }
            .verifyComplete()
    }

    @Test
    fun `getTaskById not found throws TaskNotFoundException`() {
        every { taskRepository.findById(99L) } returns null

        val result = taskService.getTaskById(99L)

        StepVerifier.create(result)
            .expectError(TaskNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `updateStatus updates and returns new TaskResponse`() {
        val updatedTask = sampleTask.copy(status = TaskStatus.IN_PROGRESS, updatedAt = now.plusHours(1))
        every { taskRepository.updateStatus(1L, TaskStatus.IN_PROGRESS) } returns true
        every { taskRepository.findById(1L) } returns updatedTask

        val result = taskService.updateStatus(1L, UpdateStatusRequest(status = TaskStatus.IN_PROGRESS))

        StepVerifier.create(result)
            .assertNext { response ->
                assertEquals(1L, response.id)
                assertEquals(TaskStatus.IN_PROGRESS, response.status)
            }
            .verifyComplete()
    }

    @Test
    fun `deleteTask calls repository and completes`() {
        every { taskRepository.deleteById(1L) } returns true

        val result = taskService.deleteTask(1L)

        StepVerifier.create(result)
            .verifyComplete()

        verify { taskRepository.deleteById(1L) }
    }

    @Test
    fun `getTasks with status filter returns correct pagination metadata`() {
        val tasks = listOf(sampleTask, sampleTask.copy(id = 2L, title = "Task 2"))
        every { taskRepository.findAll(0, 10, TaskStatus.NEW) } returns tasks
        every { taskRepository.countAll(TaskStatus.NEW) } returns 12L

        val result = taskService.getTasks(0, 10, TaskStatus.NEW)

        StepVerifier.create(result)
            .assertNext { page ->
                assertEquals(2, page.content.size)
                assertEquals(0, page.page)
                assertEquals(10, page.size)
                assertEquals(12L, page.totalElements)
                assertEquals(2, page.totalPages)
            }
            .verifyComplete()
    }
}
