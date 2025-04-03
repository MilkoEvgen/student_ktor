package com.milko.repository

import com.milko.dao.StudentDAO
import com.milko.dsl.CourseStudent
import com.milko.dsl.Courses
import com.milko.dsl.Students
import com.milko.exceptions.EntityNotFoundException
import com.milko.model.Student
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class StudentRepository {

    suspend fun create(student: Student): Long {
        return newSuspendedTransaction {
            StudentDAO.new {
                name = student.name
                email = student.email
            }.id.value
        }
    }

    suspend fun update(student: Student): Boolean {
        return newSuspendedTransaction {
            val existingStudent = StudentDAO.findById(student.id) ?: return@newSuspendedTransaction false
            existingStudent.apply {
                name = student.name
                email = student.email
            }
            true
        }
    }

    suspend fun findByIdOrThrowExceptionIfNotExists(studentId: Long): Student {
        return newSuspendedTransaction {
            StudentDAO.findById(studentId)?.let {
                Student(it.id.value, it.name, it.email)
            } ?: throw EntityNotFoundException("Student with id $studentId not found")
        }
    }

    suspend fun getStudentsByCourseId(courseId: Long): List<Student> {
        return newSuspendedTransaction {
            (CourseStudent innerJoin Students)
                .select { CourseStudent.courseId eq courseId }
                .map { row ->
                    Student(
                        id = row[Students.id].value,
                        name = row[Students.name],
                        email = row[Students.email]
                    )
                }
        }
    }

    suspend fun getStudentsByCoursesIds(courseIds: List<Long>): Map<Long, List<Student>> {
        return newSuspendedTransaction {
            (CourseStudent innerJoin Courses innerJoin Students)
                .select { CourseStudent.courseId inList courseIds }
                .map { row ->
                    val courseId = row[CourseStudent.courseId]
                    val student = Student(
                        id = row[Students.id].value,
                        name = row[Students.name],
                        email = row[Students.email]
                    )
                    courseId to student
                }
                .groupBy({ it.first }, { it.second })
        }
    }

    suspend fun findAll(): List<Student> {
        return newSuspendedTransaction {
            StudentDAO.all().map {
                Student(it.id.value, it.name, it.email)
            }
        }
    }

    suspend fun throwExceptionIfNotExists(studentId: Long) {
        newSuspendedTransaction {
            val exists = Students.select { Students.id eq studentId }
                .limit(1)
                .empty()
                .not()

            if (!exists) {
                throw EntityNotFoundException("Student with id $studentId not found")
            }
        }
    }

    suspend fun deleteById(studentId: Long) {
        newSuspendedTransaction {
            StudentDAO.findById(studentId)?.delete()
        }
    }

}

