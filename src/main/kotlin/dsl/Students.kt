package com.milko.dsl

import org.jetbrains.exposed.dao.id.LongIdTable

object Students : LongIdTable("students") {
    val name = text("name")
    val email = text("email")
}