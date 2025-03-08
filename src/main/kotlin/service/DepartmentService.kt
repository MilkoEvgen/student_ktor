package com.milko.service

import com.milko.dsl.Departments
import com.milko.dto.request.DepartmentRequestDto
import com.milko.dto.response.DepartmentResponseDto
import com.milko.mapper.toDepartment
import com.milko.mapper.toDepartmentResponse
import com.milko.repository.DepartmentRepository
import com.milko.repository.TeacherRepository
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class DepartmentService(
    private val departmentRepository: DepartmentRepository,
    private val teacherRepository: TeacherRepository
) {

    suspend fun create(departmentRequestDto: DepartmentRequestDto): DepartmentResponseDto {
        return newSuspendedTransaction {
            val department = departmentRequestDto.toDepartment()
            val departmentId = departmentRepository.create(department)
            departmentRequestDto.toDepartmentResponse(departmentId)
        }
    }

    suspend fun update(id: Long, departmentRequestDto: DepartmentRequestDto): DepartmentResponseDto {
        return newSuspendedTransaction {
            departmentRepository.throwExceptionIfNotExists(id)
            val department = departmentRequestDto.toDepartment(id)
            departmentRepository.update(department)
            findById(id)
        }
    }

    suspend fun findById(departmentId: Long): DepartmentResponseDto {
        return newSuspendedTransaction {
            val department = departmentRepository.findByIdOrThrowExceptionIfNotExists(departmentId)
            department.toDepartmentResponse()
        }
    }

    suspend fun findAll(): List<DepartmentResponseDto> {
        return newSuspendedTransaction {
            val departments = departmentRepository.findAll()

            departments.map { department ->
                department.toDepartmentResponse()
            }
        }
    }

    suspend fun delete(departmentId: Long) {
        return newSuspendedTransaction {
            departmentRepository.deleteById(departmentId)
        }
    }

    suspend fun setTeacherToDepartment(departmentId: Long, teacherId: Long): DepartmentResponseDto {
        return newSuspendedTransaction {
            departmentRepository.throwExceptionIfNotExists(departmentId)
            teacherRepository.throwExceptionIfNotExists(teacherId)

            Departments.update({ Departments.id eq departmentId}) {
                it[headOfDepartmentId] = teacherId
            }

            return@newSuspendedTransaction findById(departmentId)
        }
    }
}
