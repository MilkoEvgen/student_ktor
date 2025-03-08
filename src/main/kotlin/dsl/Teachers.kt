package com.milko.dsl

import org.jetbrains.exposed.dao.id.LongIdTable

object Teachers : LongIdTable("teachers") {
    val name = text("name")
}