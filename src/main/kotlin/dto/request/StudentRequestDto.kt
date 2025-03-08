package com.milko.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class StudentRequestDto(
    val name: String,
    val email: String
)
