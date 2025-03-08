package com.milko.dsl

import org.jetbrains.exposed.sql.Table

object CourseStudent : Table("course_student") {
    val studentId = long("student_id").references(Students.id)
    val courseId = long("course_id").references(Courses.id)

    override val primaryKey = PrimaryKey(studentId, courseId)
}
