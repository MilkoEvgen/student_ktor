package com.milko.service

import com.milko.dsl.Courses
import com.milko.dto.request.CourseRequestDto
import com.milko.dto.response.CourseResponseDto
import com.milko.mapper.toCourse
import com.milko.mapper.toCourseResponse
import com.milko.mapper.toStudentResponse
import com.milko.repository.CourseRepository
import com.milko.repository.StudentRepository
import com.milko.repository.TeacherRepository
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class CourseService(
    private val courseRepository: CourseRepository,
    private val studentRepository: StudentRepository,
    private val teacherRepository: TeacherRepository
) {
    suspend fun create(courseRequestDto: CourseRequestDto): CourseResponseDto{
        val course = courseRequestDto.toCourse()
        val courseId = courseRepository.create(course)
        val courseResponse = courseRequestDto.toCourseResponse(courseId)
        return courseResponse
    }

    suspend fun update(id: Long, courseRequestDto: CourseRequestDto): CourseResponseDto{
        courseRepository.throwExceptionIfNotExists(id)
        val course = courseRequestDto.toCourse(id)
        courseRepository.update(course)
        return findById(id)
    }

    suspend fun findById(courseId: Long): CourseResponseDto{
        val course = courseRepository.findByIdOrThrowExceptionIfNotExists(courseId)
        val students = studentRepository.getStudentsByCourseId(courseId)
        val studentResponseDtos = students.map { student -> student.toStudentResponse(null) }
        return course.toCourseResponse(studentResponseDtos)
    }

    suspend fun findAll(): List<CourseResponseDto> {
        val courses = courseRepository.findAll()
        val courseIds = courses.map { it.id }
        val studentsByCourses = studentRepository.getStudentsByCoursesIds(courseIds)

        return courses.map { course ->
            val students = studentsByCourses[course.id] ?: emptyList()
            course.toCourseResponse(students.map { it.toStudentResponse(null) })
        }
    }

    suspend fun delete(id: Long){
        courseRepository.deleteById(id)
    }

    suspend fun setTeacherToCourse(courseId: Long, teacherId: Long): CourseResponseDto{
        return newSuspendedTransaction {
            courseRepository.throwExceptionIfNotExists(courseId)
            teacherRepository.throwExceptionIfNotExists(teacherId)

            Courses.update({Courses.id eq courseId}) {
                it[Courses.teacherId] = teacherId
            }

            return@newSuspendedTransaction findById(courseId)
        }
    }
}