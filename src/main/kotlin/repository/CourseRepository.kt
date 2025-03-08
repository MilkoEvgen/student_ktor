package com.milko.repository

import com.milko.dao.CourseDAO
import com.milko.dao.StudentDAO
import com.milko.dsl.CourseStudent
import com.milko.dsl.Courses
import com.milko.dsl.Teachers
import com.milko.exceptions.EntityNotFoundException
import com.milko.model.Course
import com.milko.model.Student
import com.milko.model.Teacher
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class CourseRepository {
    suspend fun create(course: Course): Long {
        return newSuspendedTransaction {
            CourseDAO.new {
                title = course.title
                teacher = null
            }.id.value
        }
    }

    suspend fun update(course: Course): Boolean {
        return newSuspendedTransaction {
            val existingCourse = CourseDAO.findById(course.id) ?: return@newSuspendedTransaction false
            existingCourse.apply {
                title = course.title
            }
            true
        }
    }

    suspend fun findAll(): List<Course> {
        return newSuspendedTransaction {
            (Courses leftJoin Teachers)
                .selectAll()
                .map { row ->
                    Course(
                        id = row[Courses.id].value,
                        title = row[Courses.title],
                        teacher = row[Teachers.id]?.let {
                            Teacher(
                                id = it.value,
                                name = row[Teachers.name],
                                department = null
                            )
                        }
                    )
                }
        }
    }

    suspend fun getCoursesByTeacherId(teacherId: Long): List<Course> {
        return newSuspendedTransaction {
            (Courses innerJoin Teachers)
                .select { Courses.teacherId eq teacherId }
                .map { row ->
                    Course(
                        id = row[Courses.id].value,
                        title = row[Courses.title],
                        teacher = null
                    )
                }
        }
    }

    suspend fun getCoursesByTeacherIds(teacherIds: List<Long>): Map<Long, List<Course>> {
        return newSuspendedTransaction {
            (Courses innerJoin Teachers)
                .select { Courses.teacherId inList teacherIds }
                .map { row ->
                    val teacherId = row[Teachers.id].value
                    val course = Course(
                        id = row[Courses.id].value,
                        title = row[Courses.title],
                        teacher = Teacher(
                            id = teacherId,
                            name = row[Teachers.name],
                            department = null
                        )
                    )
                    teacherId to course
                }
                .groupBy({ it.first }, { it.second })
        }
    }


    suspend fun getCoursesByStudentId(studentId: Long): List<Course> {
        return newSuspendedTransaction {
            (CourseStudent innerJoin Courses leftJoin Teachers)
                .select { CourseStudent.studentId eq studentId }
                .map { row ->
                    Course(
                        id = row[Courses.id].value,
                        title = row[Courses.title],
                        teacher = row[Teachers.id]?.let {
                            Teacher(
                                id = it.value,
                                name = row[Teachers.name],
                                department = null
                            )
                        }
                    )
                }
        }
    }

    suspend fun getCoursesByStudentIds(studentIds: List<Long>): Map<Long, List<Course>> {
        return newSuspendedTransaction {
            (CourseStudent innerJoin Courses leftJoin Teachers)
                .select { CourseStudent.studentId inList studentIds }
                .map { row ->
                    val studentId = row[CourseStudent.studentId]
                    val course = Course(
                        id = row[Courses.id].value,
                        title = row[Courses.title],
                        teacher = Teacher(
                            id = row[Teachers.id].value,
                            name = row[Teachers.name],
                            department = null
                        )
                    )
                    studentId to course
                }
                .groupBy({ it.first }, { it.second })
        }
    }

    suspend fun findByIdOrThrowExceptionIfNotExists(courseId: Long): Course {
        return newSuspendedTransaction {
            (Courses leftJoin Teachers)
                .select { Courses.id eq courseId }
                .mapNotNull { row ->
                    Course(
                        id = row[Courses.id].value,
                        title = row[Courses.title],
                        teacher = row[Teachers.id]?.let {
                            Teacher(
                                id = it.value,
                                name = row[Teachers.name],
                                department = null
                            )
                        }
                    )
                }
                .singleOrNull() ?: throw EntityNotFoundException("Course with id $courseId not found")
        }
    }

    suspend fun throwExceptionIfNotExists(courseId: Long) {
        newSuspendedTransaction {
            val exists = Courses.select { Courses.id eq courseId }
                .limit(1)
                .empty()
                .not()

            if (!exists) {
                throw EntityNotFoundException("Course with id $courseId not found")
            }
        }
    }

    suspend fun deleteById(courseId: Long) {
        newSuspendedTransaction {
            CourseDAO.findById(courseId)?.delete()
        }
    }

}
