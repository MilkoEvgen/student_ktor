package com.milko.unit

import com.milko.dto.request.DepartmentRequestDto
import com.milko.mapper.toDepartment
import com.milko.mapper.toDepartmentResponse
import com.milko.model.Department
import com.milko.repository.DepartmentRepository
import com.milko.repository.TeacherRepository
import com.milko.service.DepartmentService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DepartmentServiceTest {
    private val departmentRepository = mockk<DepartmentRepository>()
    private val teacherRepository = mockk<TeacherRepository>()
    private val departmentService = DepartmentService(departmentRepository, teacherRepository)

    @BeforeEach
    fun setup() {
        mockkStatic("com.milko.utils.TransactionWrapperKt")

        coEvery<Any?> { com.milko.utils.dbQuery(any()) } coAnswers {
            val block = invocation.args[0] as suspend () -> Any?
            runBlocking { block() }
        }
    }

    @Test
    fun `create should return DepartmentResponseDto`() = runTest {
        val dto = DepartmentRequestDto(name = "Physics")
        val expectedId = 1L

        coEvery { departmentRepository.create(any()) } returns expectedId

        val result = departmentService.create(dto)

        assertEquals(expectedId, result.id)
        assertEquals(dto.name, result.name)

        coVerify(exactly = 1) { departmentRepository.create(any()) }
    }

    @Test
    fun `update should return updated DepartmentResponseDto`() = runTest {
        val departmentId = 1L
        val dto = DepartmentRequestDto(name = "Updated Name")
        val updatedDepartment = dto.toDepartment(departmentId)
        val response = dto.toDepartmentResponse(departmentId)

        coEvery { departmentRepository.throwExceptionIfNotExists(departmentId) } returns Unit
        coEvery { departmentRepository.update(updatedDepartment) } returns true
        coEvery { departmentRepository.findByIdOrThrowExceptionIfNotExists(departmentId) } returns updatedDepartment

        val result = departmentService.update(departmentId, dto)

        assertEquals(response.id, result.id)
        assertEquals(response.name, result.name)

        coVerify { departmentRepository.throwExceptionIfNotExists(departmentId) }
        coVerify { departmentRepository.update(updatedDepartment) }
    }

    @Test
    fun `findById should return DepartmentResponseDto`() = runTest {
        val departmentId = 1L
        val department = Department(id = departmentId, name = "Math", headOfDepartment = null)

        coEvery { departmentRepository.findByIdOrThrowExceptionIfNotExists(departmentId) } returns department

        val result = departmentService.findById(departmentId)

        assertEquals(departmentId, result.id)
        assertEquals("Math", result.name)

        coVerify { departmentRepository.findByIdOrThrowExceptionIfNotExists(departmentId) }
    }

    @Test
    fun `findAll should return list of DepartmentResponseDto`() = runTest {
        val dep1 = Department(1, "Math", null)
        val dep2 = Department(2, "Biology", null)

        coEvery { departmentRepository.findAll() } returns listOf(dep1, dep2)

        val result = departmentService.findAll()

        assertEquals(2, result.size)
        assertEquals("Math", result[0].name)
        assertEquals("Biology", result[1].name)

        coVerify { departmentRepository.findAll() }
    }

    @Test
    fun `delete should call deleteById`() = runTest {
        val departmentId = 1L

        coEvery { departmentRepository.deleteById(departmentId) } returns Unit

        departmentService.delete(departmentId)

        coVerify(exactly = 1) { departmentRepository.deleteById(departmentId) }
    }
}
