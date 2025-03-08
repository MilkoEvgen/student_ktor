package com.milko.model

import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: Long,
    val title: String,
    val teacher: Teacher?
)