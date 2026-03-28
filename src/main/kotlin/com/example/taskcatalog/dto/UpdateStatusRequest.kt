package com.example.taskcatalog.dto

import com.example.taskcatalog.model.TaskStatus

data class UpdateStatusRequest(
    val status: TaskStatus
)
