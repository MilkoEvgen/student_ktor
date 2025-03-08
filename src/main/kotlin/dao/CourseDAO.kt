package com.milko.dao

import com.milko.dsl.Courses
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CourseDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CourseDAO>(Courses)

    var title by Courses.title
    var teacher by TeacherDAO.optionalReferencedOn(Courses.teacherId)
}