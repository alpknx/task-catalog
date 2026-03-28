package com.example.taskcatalog.exception

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.core.codec.DecodingException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException::class)
    fun handleTaskNotFound(ex: TaskNotFoundException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            mapOf("error" to "Task not found", "id" to ex.taskId)
        )
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(ex: WebExchangeBindException): ResponseEntity<Map<String, Any>> {
        val errors = ex.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return ResponseEntity.badRequest().body(
            mapOf("error" to "Validation failed", "fields" to errors)
        )
    }

    @ExceptionHandler(DecodingException::class)
    fun handleDecodingException(ex: DecodingException): ResponseEntity<Map<String, Any>> {
        val cause = ex.cause
        if (cause is InvalidFormatException && cause.targetType.isEnum) {
            return ResponseEntity.badRequest().body(
                mapOf("error" to "Invalid status value")
            )
        }
        return ResponseEntity.badRequest().body(
            mapOf("error" to "Invalid request body")
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            mapOf("error" to "Internal server error")
        )
    }
}
