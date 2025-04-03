package com.milko.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class CourseResponseDto(
    val id: Long,
    val title: String,
    val teacher: TeacherResponseDto? = null,
    val students: List<StudentResponseDto> = emptyList()
)
