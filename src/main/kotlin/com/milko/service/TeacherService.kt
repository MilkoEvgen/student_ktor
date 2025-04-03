package com.milko.service

import com.milko.dto.request.TeacherRequestDto
import com.milko.dto.response.TeacherResponseDto
import com.milko.mapper.toCourseResponse
import com.milko.mapper.toTeacher
import com.milko.mapper.toTeacherResponse
import com.milko.repository.CourseRepository
import com.milko.repository.TeacherRepository

class TeacherService(
    private val teacherRepository: TeacherRepository,
    private val courseRepository: CourseRepository
) {

    suspend fun create(teacherRequestDto: TeacherRequestDto): TeacherResponseDto {
        return com.milko.utils.dbQuery {
            val teacher = teacherRequestDto.toTeacher()
            val teacherId = teacherRepository.create(teacher)
            teacherRequestDto.toTeacherResponse(teacherId)
        }
    }

    suspend fun update(id: Long, teacherRequestDto: TeacherRequestDto): TeacherResponseDto {
        return com.milko.utils.dbQuery {
            teacherRepository.throwExceptionIfNotExists(id)
            val teacher = teacherRequestDto.toTeacher(id)
            teacherRepository.update(teacher)
            findById(id)
        }
    }

    suspend fun findById(teacherId: Long): TeacherResponseDto {
        return com.milko.utils.dbQuery {
            val teacher = teacherRepository.findByIdOrThrowExceptionIfNotExists(teacherId)
            val courses = courseRepository.getCoursesByTeacherId(teacherId)
            val courseResponseDtos = courses.map { course -> course.toCourseResponse(null) }
            teacher.toTeacherResponse(courseResponseDtos)
        }
    }

    suspend fun findAll(): List<TeacherResponseDto> {
        return com.milko.utils.dbQuery {
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
        return com.milko.utils.dbQuery {
            teacherRepository.deleteById(teacherId)
        }
    }
}
