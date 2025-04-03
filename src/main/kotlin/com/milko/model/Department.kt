package com.milko.model

import kotlinx.serialization.Serializable

@Serializable
data class Department (
    val id: Long,
    val name: String,
    val headOfDepartment: Teacher?
)
