package com.milko.model

import kotlinx.serialization.Serializable

@Serializable
data class Teacher(
    val id: Long,
    val name: String,
    val department: Department?
)
