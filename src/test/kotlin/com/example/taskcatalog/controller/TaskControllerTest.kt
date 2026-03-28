package com.example.taskcatalog.controller

import com.example.taskcatalog.dto.PageResponse
import com.example.taskcatalog.dto.TaskResponse
import com.example.taskcatalog.exception.GlobalExceptionHandler
import com.example.taskcatalog.exception.TaskNotFoundException
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.service.TaskService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@WebFluxTest(TaskController::class)
@Import(GlobalExceptionHandler::class)
class TaskControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var taskService: TaskService

    private val now = LocalDateTime.of(2024, 1, 1, 12, 0)

    private val sampleResponse = TaskResponse(
        id = 1L,
        title = "Test Task",
        description = "A test task",
        status = TaskStatus.NEW,
        createdAt = now,
        updatedAt = now
    )

    @Test
    fun `POST api tasks returns 201 Created`() {
        every { taskService.createTask(any()) } returns Mono.just(sampleResponse)

        webTestClient.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"title": "Test Task", "description": "A test task"}""")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.title").isEqualTo("Test Task")
            .jsonPath("$.status").isEqualTo("NEW")
    }

    @Test
    fun `POST api tasks with blank title returns 400`() {
        webTestClient.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"title": "", "description": "desc"}""")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `GET api tasks id exists returns 200`() {
        every { taskService.getTaskById(1L) } returns Mono.just(sampleResponse)

        webTestClient.get().uri("/api/tasks/1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.title").isEqualTo("Test Task")
    }

    @Test
    fun `GET api tasks id missing returns 404`() {
        every { taskService.getTaskById(99L) } returns Mono.error(TaskNotFoundException(99L))

        webTestClient.get().uri("/api/tasks/99")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.error").isEqualTo("Task not found")
            .jsonPath("$.id").isEqualTo(99)
    }

    @Test
    fun `PATCH api tasks id status returns 200`() {
        val updatedResponse = sampleResponse.copy(status = TaskStatus.DONE)
        every { taskService.updateStatus(1L, any()) } returns Mono.just(updatedResponse)

        webTestClient.patch().uri("/api/tasks/1/status")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"status": "DONE"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("DONE")
    }

    @Test
    fun `DELETE api tasks id returns 204`() {
        every { taskService.deleteTask(1L) } returns Mono.empty()

        webTestClient.delete().uri("/api/tasks/1")
            .exchange()
            .expectStatus().isNoContent
    }
}
