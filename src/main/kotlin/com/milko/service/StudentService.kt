package com.milko.service

import com.milko.dsl.CourseStudent
import com.milko.dto.request.StudentRequestDto
import com.milko.dto.response.CourseResponseDto
import com.milko.dto.response.StudentResponseDto
import com.milko.mapper.toCourseResponse
import com.milko.mapper.toStudent
import com.milko.mapper.toStudentResponse
import com.milko.repository.CourseRepository
import com.milko.repository.StudentRepository
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class StudentService(
    private val studentRepository: StudentRepository,
    private val courseRepository: CourseRepository
) {

     suspend fun create(studentRequestDto: StudentRequestDto): StudentResponseDto {
        val student = studentRequestDto.toStudent()
        val studentId = studentRepository.create(student)
        val studentResponse = studentRequestDto.toStudentResponse(studentId)
        return studentResponse
    }

    suspend fun update(id: Long, studentRequestDto: StudentRequestDto): StudentResponseDto {
        studentRepository.throwExceptionIfNotExists(id)
        val student = studentRequestDto.toStudent(id)
        studentRepository.update(student)
        return findById(id)
    }

    suspend fun findById(studentId: Long): StudentResponseDto {
        val student = studentRepository.findByIdOrThrowExceptionIfNotExists(studentId)
        val courses = courseRepository.getCoursesByStudentId(studentId)
        val courseResponseDtos = courses.map { course -> course.toCourseResponse(null) }
        return student.toStudentResponse(courseResponseDtos)
    }

    suspend fun findAll(): List<StudentResponseDto> {
        val students = studentRepository.findAll()
        val studentIds = students.map { it.id }
        val coursesByStudent = courseRepository.getCoursesByStudentIds(studentIds)

        return students.map { student ->
            val courses = coursesByStudent[student.id] ?: emptyList()
            student.toStudentResponse(courses.map { it.toCourseResponse(null) })
        }
    }

    suspend fun findAllCoursesByStudentId(id: Long): List<CourseResponseDto> {
        val courses = courseRepository.getCoursesByStudentId(id)
        return courses.map { it.toCourseResponse(null) }
    }

    suspend fun delete(id: Long) {
        studentRepository.deleteById(id)
    }

    suspend fun addCourseToStudent(studentId: Long, courseId: Long): StudentResponseDto {
        return newSuspendedTransaction {
            studentRepository.throwExceptionIfNotExists(studentId)
            courseRepository.throwExceptionIfNotExists(courseId)

            val existingCourseStudent = CourseStudent.select {
                (CourseStudent.studentId eq studentId) and (CourseStudent.courseId eq courseId)
            }.singleOrNull()

            if (existingCourseStudent != null) {
                throw IllegalArgumentException("Student is already enrolled in this course")
            }

            CourseStudent.insert {
                it[CourseStudent.studentId] = studentId
                it[CourseStudent.courseId] = courseId
            }

            return@newSuspendedTransaction findById(studentId)
        }
    }

}




