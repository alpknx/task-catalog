package com.example.taskcatalog.exception

class TaskNotFoundException(val taskId: Long) : RuntimeException("Task not found with id: $taskId")
