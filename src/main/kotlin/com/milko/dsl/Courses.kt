package com.milko.dsl

import org.jetbrains.exposed.dao.id.LongIdTable

object Courses : LongIdTable("courses") {
    val title = text("title")
    val teacherId = reference("teacher_id", Teachers.id).nullable()
}