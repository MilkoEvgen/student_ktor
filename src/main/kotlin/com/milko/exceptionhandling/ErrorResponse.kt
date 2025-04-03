package com.milko.exceptionhandling

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class ErrorResponse(
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val status: String,
    val error: String,
    val message: String,
    val path: String
)
