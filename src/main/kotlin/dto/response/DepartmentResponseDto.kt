package com.milko.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class DepartmentResponseDto(
    val id: Long,
    val name: String,
    val headOfDepartment: TeacherResponseDto?
)
