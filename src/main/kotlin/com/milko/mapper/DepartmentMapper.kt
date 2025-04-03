package com.milko.mapper

import com.milko.dto.request.DepartmentRequestDto
import com.milko.dto.response.DepartmentResponseDto
import com.milko.model.Department

fun DepartmentRequestDto.toDepartment(id: Long? = null): Department {
    return Department(
        id = id ?: 0,
        name = name,
        headOfDepartment = null
    )
}

fun DepartmentRequestDto.toDepartmentResponse(id: Long? = null): DepartmentResponseDto {
    return DepartmentResponseDto(
        id = id ?: 0,
        name = name,
        headOfDepartment = null
    )
}

fun Department.toDepartmentResponse(): DepartmentResponseDto {
    return DepartmentResponseDto(
        id = id,
        name = name,
        headOfDepartment = headOfDepartment?.toTeacherResponse(null)
    )
}