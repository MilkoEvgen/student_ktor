package com.milko.dao

import com.milko.dsl.Students
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class StudentDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<StudentDAO>(Students)

    var name by Students.name
    var email by Students.email
}