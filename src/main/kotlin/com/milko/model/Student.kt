package com.milko.model

import kotlinx.serialization.Serializable

@Serializable
data class Student(
    var id: Long,
    val name: String,
    val email: String
)
