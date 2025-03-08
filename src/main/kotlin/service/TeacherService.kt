package com.milko.service

import com.milko.dto.request.TeacherRequestDto
import com.milko.dto.response.TeacherResponseDto
import com.milko.mapper.*
import com.milko.repository.CourseRepository
import com.milko.repository.TeacherRepository
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class TeacherService(
    private val teacherRepository: TeacherRepository,
    private val courseRepository: CourseRepository
) {

    suspend fun create(teacherRequestDto: TeacherRequestDto): TeacherResponseDto {
        return newSuspendedTransaction {
            val teacher = teacherRequestDto.toTeacher()
            val teacherId = teacherRepository.create(teacher)
            teacherRequestDto.toTeacherResponse(teacherId)
        }
    }

    suspend fun update(id: Long, teacherRequestDto: TeacherRequestDto): TeacherResponseDto {
        return newSuspendedTransaction {
            teacherRepository.throwExceptionIfNotExists(id)
            val teacher = teacherRequestDto.toTeacher(id)
            teacherRepository.update(teacher)
            findById(id)
        }
    }

    suspend fun findById(teacherId: Long): TeacherResponseDto {
        return newSuspendedTransaction {
            val teacher = teacherRepository.findByIdOrThrowExceptionIfNotExists(teacherId)
            val courses = courseRepository.getCoursesByTeacherId(teacherId)
            val courseResponseDtos = courses.map { course -> course.toCourseResponse(null) }
            teacher.toTeacherResponse(courseResponseDtos)
        }
    }

    suspend fun findAll(): List<TeacherResponseDto> {
        return newSuspendedTransaction {
            val teachers = teacherRepository.findAll()
            val teacherIds = teachers.map { it.id }
            val coursesByTeachers = courseRepository.getCoursesByTeacherIds(teacherIds)

            teachers.map { teacher ->
                val courses = coursesByTeachers[teacher.id] ?: emptyList()
                teacher.toTeacherResponse(courses.map { it.toCourseResponse(null) })
            }
        }
    }

    suspend fun delete(teacherId: Long) {
        return newSuspendedTransaction {
            teacherRepository.deleteById(teacherId)
        }
    }
}
