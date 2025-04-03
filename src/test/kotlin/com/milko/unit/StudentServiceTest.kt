package com.milko.unit

import com.milko.dto.request.StudentRequestDto
import com.milko.mapper.toStudent
import com.milko.model.Course
import com.milko.model.Student
import com.milko.repository.CourseRepository
import com.milko.repository.StudentRepository
import com.milko.service.StudentService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StudentServiceTest {

    private val studentRepository = mockk<StudentRepository>()
    private val courseRepository = mockk<CourseRepository>()
    private val studentService = StudentService(studentRepository, courseRepository)

    @BeforeEach
    fun setup() {
        mockkStatic("com.milko.utils.TransactionWrapperKt")

        coEvery<Any?> { com.milko.utils.dbQuery(any()) } coAnswers {
            val block = invocation.args[0] as suspend () -> Any?
            runBlocking { block() }
        }
    }

    @Test
    fun `create should return StudentResponseDto`() = runTest {
        val dto = StudentRequestDto(name = "Alice", email = "alice@example.com")
        val expectedId = 1L

        coEvery { studentRepository.create(any()) } returns expectedId

        val result = studentService.create(dto)

        assertEquals(expectedId, result.id)
        assertEquals(dto.name, result.name)
        assertEquals(dto.email, result.email)

        coVerify { studentRepository.create(any()) }
    }

    @Test
    fun `update should return updated StudentResponseDto`() = runTest {
        val id = 1L
        val dto = StudentRequestDto(name = "Updated", email = "updated@example.com")
        val updated = dto.toStudent(id)

        coEvery { studentRepository.throwExceptionIfNotExists(id) } returns Unit
        coEvery { studentRepository.update(updated) } returns true
        coEvery { studentRepository.findByIdOrThrowExceptionIfNotExists(id) } returns updated
        coEvery { courseRepository.getCoursesByStudentId(id) } returns emptyList()

        val result = studentService.update(id, dto)

        assertEquals(id, result.id)
        assertEquals("Updated", result.name)
        assertEquals("updated@example.com", result.email)

        coVerify { studentRepository.throwExceptionIfNotExists(id) }
        coVerify { studentRepository.update(updated) }
    }

    @Test
    fun `findById should return student with courses`() = runTest {
        val id = 1L
        val student = Student(id, "Bob", "bob@mail.com")
        val course = Course(id = 10, title = "Math", teacher = null)

        coEvery { studentRepository.findByIdOrThrowExceptionIfNotExists(id) } returns student
        coEvery { courseRepository.getCoursesByStudentId(id) } returns listOf(course)

        val result = studentService.findById(id)

        assertEquals(id, result.id)
        assertEquals(1, result.courses.size)
        assertEquals("Math", result.courses.first().title)
    }

    @Test
    fun `findAll should return list of students with their courses`() = runTest {
        val s1 = Student(1, "A", "a@a.com")
        val s2 = Student(2, "B", "b@b.com")
        val c1 = Course(11, "Java", null)
        val c2 = Course(22, "Kotlin", null)

        coEvery { studentRepository.findAll() } returns listOf(s1, s2)
        coEvery { courseRepository.getCoursesByStudentIds(listOf(1, 2)) } returns mapOf(
            1L to listOf(c1),
            2L to listOf(c2)
        )

        val result = studentService.findAll()

        assertEquals(2, result.size)
        assertEquals("Java", result[0].courses.first().title)
        assertEquals("Kotlin", result[1].courses.first().title)
    }

    @Test
    fun `delete should call deleteById`() = runTest {
        val id = 1L
        coEvery { studentRepository.deleteById(id) } returns Unit

        studentService.delete(id)

        coVerify { studentRepository.deleteById(id) }
    }
}
