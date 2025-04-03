package com.milko.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class StudentResponseDto(
    val id: Long,
    val name: String,
    val email: String,
    val courses: List<CourseResponseDto> = emptyList()
)

