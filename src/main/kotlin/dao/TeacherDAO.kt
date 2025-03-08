package com.milko.dao

import com.milko.dsl.Teachers
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TeacherDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<TeacherDAO>(Teachers)

    var name by Teachers.name
}