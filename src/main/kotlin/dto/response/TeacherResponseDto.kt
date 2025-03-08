package com.milko.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class TeacherResponseDto(
    val id: Long,
    val name: String,
    val department: DepartmentResponseDto?,
    val courses: List<CourseResponseDto> = emptyList()
)
