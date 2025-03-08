package com.milko.mapper

import com.milko.dto.request.StudentRequestDto
import com.milko.dto.response.CourseResponseDto
import com.milko.dto.response.StudentResponseDto
import com.milko.model.Student


fun StudentRequestDto.toStudent(id: Long? = null): Student {
    return Student(
        id = id ?: 0,
        name = this.name,
        email = this.email
    )
}

fun StudentRequestDto.toStudentResponse(id: Long): StudentResponseDto {
    return StudentResponseDto(
        id = id,
        name = this.name,
        email = this.email,
        courses = emptyList()
    )
}

fun Student.toStudentResponse(courses: List<CourseResponseDto>?): StudentResponseDto {
    return StudentResponseDto(
        id = this.id,
        name = this.name,
        email = this.email,
        courses = courses ?: emptyList()
    )
}