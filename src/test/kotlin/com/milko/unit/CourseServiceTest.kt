package com.milko.unit

import com.milko.dto.request.CourseRequestDto
import com.milko.mapper.toCourse
import com.milko.mapper.toCourseResponse
import com.milko.model.Course
import com.milko.model.Student
import com.milko.repository.CourseRepository
import com.milko.repository.StudentRepository
import com.milko.repository.TeacherRepository
import com.milko.service.CourseService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class CourseServiceTest {
    private val courseRepository = mockk<CourseRepository>()
    private val studentRepository = mockk<StudentRepository>()
    private val teacherRepository = mockk<TeacherRepository>()
    private val courseService = CourseService(courseRepository, studentRepository, teacherRepository)

    @Test
    fun `create should return CourseResponseDto`() = runTest {
        val dto = CourseRequestDto(title = "Math")
        val expectedId = 42L

        coEvery { courseRepository.create(any()) } returns expectedId

        val result = courseService.create(dto)

        assertEquals(expectedId, result.id)
        assertEquals(dto.title, result.title)

        coVerify(exactly = 1) { courseRepository.create(any()) }
    }

    @Test
    fun `update should return updated CourseResponseDto`() = runTest {
        val courseId = 1L
        val dto = CourseRequestDto(title = "Updated Title")
        val updatedCourse = dto.toCourse(courseId)
        val response = dto.toCourseResponse(courseId)

        coEvery { courseRepository.throwExceptionIfNotExists(courseId) } returns Unit
        coEvery { courseRepository.update(updatedCourse) } returns true
        coEvery { courseRepository.findByIdOrThrowExceptionIfNotExists(courseId) } returns updatedCourse
        coEvery { studentRepository.getStudentsByCourseId(courseId) } returns emptyList()

        val result = courseService.update(courseId, dto)

        assertEquals(response.id, result.id)
        assertEquals(response.title, result.title)

        coVerify { courseRepository.throwExceptionIfNotExists(courseId) }
        coVerify { courseRepository.update(updatedCourse) }
    }

    @Test
    fun `findById should return course with student responses`() = runTest {
        val courseId = 1L
        val course = Course(id = courseId, title = "Math", teacher = null)
        val student = Student(id = 10L, name = "John", email = "email@student.com")

        coEvery { courseRepository.findByIdOrThrowExceptionIfNotExists(courseId) } returns course
        coEvery { studentRepository.getStudentsByCourseId(courseId) } returns listOf(student)

        val result = courseService.findById(courseId)

        assertEquals(courseId, result.id)
        assertEquals("Math", result.title)
        assertEquals(1, result.students.size)
        assertEquals("John", result.students.first().name)

        coVerify { courseRepository.findByIdOrThrowExceptionIfNotExists(courseId) }
        coVerify { studentRepository.getStudentsByCourseId(courseId) }
    }

    @Test
    fun `findAll should return list of courses with their students`() = runTest {
        val course1 = Course(id = 1, title = "Math", teacher = null)
        val course2 = Course(id = 2, title = "Science", teacher = null)

        val student1 = Student(id = 10, name = "A", email = "email1@student.com")
        val student2 = Student(id = 11, name = "B", email = "email2@student.com")

        coEvery { courseRepository.findAll() } returns listOf(course1, course2)
        coEvery { studentRepository.getStudentsByCoursesIds(listOf(1, 2)) } returns mapOf(
            1L to listOf(student1),
            2L to listOf(student2)
        )

        val result = courseService.findAll()

        assertEquals(2, result.size)
        assertEquals("Math", result[0].title)
        assertEquals("Science", result[1].title)
        assertEquals("A", result[0].students.first().name)
        assertEquals("B", result[1].students.first().name)

        coVerify { courseRepository.findAll() }
        coVerify { studentRepository.getStudentsByCoursesIds(any()) }
    }

    @Test
    fun `deleteById should delete course by id`() = runTest {
        val courseId = 1L

        coEvery { courseRepository.deleteById(courseId) } returns Unit

        courseService.delete(courseId)

        coVerify(exactly = 1) { courseRepository.deleteById(courseId) }
    }

}