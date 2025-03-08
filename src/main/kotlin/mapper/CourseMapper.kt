package com.milko.mapper

import com.milko.dto.request.CourseRequestDto
import com.milko.dto.request.StudentRequestDto
import com.milko.dto.response.CourseResponseDto
import com.milko.dto.response.StudentResponseDto
import com.milko.model.Course
import com.milko.model.Student


fun CourseRequestDto.toCourse(id: Long? = null): Course {
    return Course(
        id = id ?: 0,
        title = title,
        teacher = null
    )
}

fun CourseRequestDto.toCourseResponse(id: Long): CourseResponseDto {
    return CourseResponseDto(
        id = id,
        title = title,
        teacher = null
    )
}

fun Course.toCourseResponse(students: List<StudentResponseDto>?): CourseResponseDto {
    return CourseResponseDto(
        id = id,
        title = title,
        teacher = teacher?.toTeacherResponse(null),
        students = students ?: emptyList()
    )
}