package com.milko.unit

import com.milko.dto.request.TeacherRequestDto
import com.milko.mapper.toTeacher
import com.milko.model.Course
import com.milko.model.Teacher
import com.milko.repository.CourseRepository
import com.milko.repository.TeacherRepository
import com.milko.service.TeacherService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TeacherServiceTest {

    private val teacherRepository = mockk<TeacherRepository>()
    private val courseRepository = mockk<CourseRepository>()
    private val teacherService = TeacherService(teacherRepository, courseRepository)

    @BeforeEach
    fun setup() {
        mockkStatic("com.milko.utils.TransactionWrapperKt")

        coEvery<Any?> { com.milko.utils.dbQuery(any()) } coAnswers {
            val block = invocation.args[0] as suspend () -> Any?
            runBlocking { block() }
        }
    }

    @Test
    fun `create should return TeacherResponseDto`() = runTest {
        val dto = TeacherRequestDto(name = "Alan")
        val expectedId = 1L

        coEvery { teacherRepository.create(any()) } returns expectedId

        val result = teacherService.create(dto)

        assertEquals(expectedId, result.id)
        assertEquals(dto.name, result.name)

        coVerify { teacherRepository.create(any()) }
    }

    @Test
    fun `update should return updated TeacherResponseDto`() = runTest {
        val teacherId = 1L
        val dto = TeacherRequestDto(name = "Updated")
        val updated = dto.toTeacher(teacherId)

        coEvery { teacherRepository.throwExceptionIfNotExists(teacherId) } returns Unit
        coEvery { teacherRepository.update(updated) } returns true
        coEvery { teacherRepository.findByIdOrThrowExceptionIfNotExists(teacherId) } returns updated
        coEvery { courseRepository.getCoursesByTeacherId(teacherId) } returns emptyList()

        val result = teacherService.update(teacherId, dto)

        assertEquals(teacherId, result.id)
        assertEquals(dto.name, result.name)
        coVerify { teacherRepository.update(updated) }
    }

    @Test
    fun `findById should return teacher with courses`() = runTest {
        val teacherId = 1L
        val teacher = Teacher(id = teacherId, name = "Bob", department = null)
        val course = Course(id = 10, title = "Math", teacher = teacher)

        coEvery { teacherRepository.findByIdOrThrowExceptionIfNotExists(teacherId) } returns teacher
        coEvery { courseRepository.getCoursesByTeacherId(teacherId) } returns listOf(course)

        val result = teacherService.findById(teacherId)

        assertEquals(teacherId, result.id)
        assertEquals("Math", result.courses.first().title)
    }

    @Test
    fun `findAll should return list of teachers with their courses`() = runTest {
        val t1 = Teacher(1, "John", null)
        val t2 = Teacher(2, "Jane", null)
        val c1 = Course(101, "Spring", t1)
        val c2 = Course(102, "Kotlin", t2)

        coEvery { teacherRepository.findAll() } returns listOf(t1, t2)
        coEvery { courseRepository.getCoursesByTeacherIds(listOf(1, 2)) } returns mapOf(
            1L to listOf(c1),
            2L to listOf(c2)
        )

        val result = teacherService.findAll()

        assertEquals(2, result.size)
        assertEquals("Spring", result[0].courses.first().title)
        assertEquals("Kotlin", result[1].courses.first().title)
    }

    @Test
    fun `delete should call deleteById`() = runTest {
        val teacherId = 1L
        coEvery { teacherRepository.deleteById(teacherId) } returns Unit

        teacherService.delete(teacherId)

        coVerify(exactly = 1) { teacherRepository.deleteById(teacherId) }
    }
}
