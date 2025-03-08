package com.milko.mapper

import com.milko.dto.request.CourseRequestDto
import com.milko.dto.request.TeacherRequestDto
import com.milko.dto.response.CourseResponseDto
import com.milko.dto.response.StudentResponseDto
import com.milko.dto.response.TeacherResponseDto
import com.milko.model.Course
import com.milko.model.Teacher

fun TeacherRequestDto.toTeacher(id: Long? = null): Teacher {
    return Teacher(
        id = id ?: 0,
        name = name,
        department = null
    )
}

fun TeacherRequestDto.toTeacherResponse(id: Long): TeacherResponseDto {
    return TeacherResponseDto(
        id = id,
        name = name,
        department = null
    )
}

fun Teacher.toTeacherResponse(courses: List<CourseResponseDto>?): TeacherResponseDto {
    return TeacherResponseDto(
        id = id,
        name = name,
        department = department?.toDepartmentResponse(),
        courses = courses ?: emptyList()
    )
}
