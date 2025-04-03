package com.milko.integration

import com.milko.dto.request.CourseRequestDto
import com.milko.dto.request.StudentRequestDto
import com.milko.dto.response.CourseResponseDto
import com.milko.dto.response.StudentResponseDto
import com.milko.exceptionhandling.ErrorResponse
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StudentRoutesTest : IntegrationTestBase() {

    @Test
    fun `POST - should create student and return 201`() = runTestOnceApp {
        val resp = client.post("/api/v1/students") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(StudentRequestDto("Alice", "alice@example.com")))
        }
        assertEquals(HttpStatusCode.Created, resp.status)
        val created = Json.decodeFromString<StudentResponseDto>(resp.bodyAsText())
        assertEquals("Alice", created.name)
        assertEquals("alice@example.com", created.email)
    }

    @Test
    fun `POST - should fail with 500 if student name already exists`() = runTestOnceApp {
        val requestBody = StudentRequestDto("Alice", "alice@example.com")
        val firstResponse = client.post("/api/v1/students") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(requestBody))
        }
        assertEquals(HttpStatusCode.Created, firstResponse.status)

        val secondResponse = client.post("/api/v1/students") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(requestBody))
        }
        assertEquals(HttpStatusCode.InternalServerError, secondResponse.status)

        val responseBody = secondResponse.bodyAsText()

        val errorResponse = Json.decodeFromString<ErrorResponse>(responseBody)
        assertEquals("500", errorResponse.status)
        assertEquals("Internal Server Error", errorResponse.error)
        assertTrue(errorResponse.message.contains("duplicate key") || errorResponse.message.isNotBlank())
        assertEquals("/api/v1/students", errorResponse.path)
    }

    @Test
    fun `GET by id - should return 200 and correct student`() = runTestOnceApp {
        val createResp = client.post("/api/v1/students") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(StudentRequestDto("Bob", "bob@example.com")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)
        val createdStudent = Json.decodeFromString<StudentResponseDto>(createResp.bodyAsText())
        val studentId = createdStudent.id

        val getResp = client.get("/api/v1/students/$studentId")
        assertEquals(HttpStatusCode.OK, getResp.status)
        val fetched = Json.decodeFromString<StudentResponseDto>(getResp.bodyAsText())
        assertEquals("Bob", fetched.name)
        assertEquals("bob@example.com", fetched.email)
    }

    @Test
    fun `GET by id - should return 404 if no such student`() = runTestOnceApp {
        val resp = client.get("/api/v1/students/999")
        assertEquals(HttpStatusCode.NotFound, resp.status)
        val error = Json.decodeFromString<ErrorResponse>(resp.bodyAsText())
        assertEquals("404", error.status)
        assertEquals("/api/v1/students/999", error.path)
    }

    @Test
    fun `GET all - should return 200 and empty list if no students`() = runTestOnceApp {
        val resp = client.get("/api/v1/students")
        assertEquals(HttpStatusCode.OK, resp.status)
        val body = resp.bodyAsText()
        assertTrue(body.contains("[]"))
    }

    @Test
    fun `GET all - should return 200 and list of 1 if we created 1 student`() = runTestOnceApp {
        val createResp = client.post("/api/v1/students") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(StudentRequestDto("Carol", "carol@example.com")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)

        val getAllResp = client.get("/api/v1/students")
        assertEquals(HttpStatusCode.OK, getAllResp.status)
        val students = Json.decodeFromString<List<StudentResponseDto>>(getAllResp.bodyAsText())
        assertEquals(1, students.size)
        assertEquals("Carol", students[0].name)
    }

    @Test
    fun `GET courses - should return 200 and empty list if student has no courses`() = runTestOnceApp {
        val createResp = client.post("/api/v1/students") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(StudentRequestDto("Dan", "dan@example.com")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)
        val student = Json.decodeFromString<StudentResponseDto>(createResp.bodyAsText())

        val resp = client.get("/api/v1/students/${student.id}/courses")
        assertEquals(HttpStatusCode.OK, resp.status)
        val courses = Json.decodeFromString<List<CourseResponseDto>>(resp.bodyAsText())
        assertTrue(courses.isEmpty())
    }

    @Test
    fun `PATCH - should update student and return 200`() = runTestOnceApp {
        val createResp = client.post("/api/v1/students") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(StudentRequestDto("Eve", "eve@example.com")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)
        val student = Json.decodeFromString<StudentResponseDto>(createResp.bodyAsText())

        val patchResp = client.patch("/api/v1/students/${student.id}") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(StudentRequestDto("Eve Updated", "eve2@example.com")))
        }
        assertEquals(HttpStatusCode.OK, patchResp.status)
        val updated = Json.decodeFromString<StudentResponseDto>(patchResp.bodyAsText())
        assertEquals("Eve Updated", updated.name)
        assertEquals("eve2@example.com", updated.email)
    }

    @Test
    fun `PATCH - should return 404 if trying to update non-existing student`() = runTestOnceApp {
        val resp = client.patch("/api/v1/students/999") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(StudentRequestDto("Ghost", "ghost@example.com")))
        }
        assertEquals(HttpStatusCode.NotFound, resp.status)
        val body = resp.bodyAsText()
        val error = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("404", error.status)
        assertTrue(error.message.contains("not found", true))
    }

    @Test
    fun `DELETE - should delete existing student and return 204`() = runTestOnceApp {
        val createResp = client.post("/api/v1/students") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(StudentRequestDto("Frank", "frank@example.com")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)
        val student = Json.decodeFromString<StudentResponseDto>(createResp.bodyAsText())

        val deleteResp = client.delete("/api/v1/students/${student.id}")
        assertEquals(HttpStatusCode.NoContent, deleteResp.status)

        val getResp = client.get("/api/v1/students/${student.id}")
        assertEquals(HttpStatusCode.NotFound, getResp.status)
    }

    @Test
    fun `DELETE - should return 204 if no such student`() = runTestOnceApp {
        val resp = client.delete("/api/v1/students/999")
        assertEquals(HttpStatusCode.NoContent, resp.status)
    }

    @Test
    fun `POST student-courses - should add course to student and return 200`() = runTestOnceApp {
        val studentResp = client.post("/api/v1/students") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(StudentRequestDto("Gina", "gina@example.com")))
        }
        assertEquals(HttpStatusCode.Created, studentResp.status)
        val student = Json.decodeFromString<StudentResponseDto>(studentResp.bodyAsText())

        val courseResp = client.post("/api/v1/courses") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(CourseRequestDto("Algorithms")))
        }
        assertEquals(HttpStatusCode.Created, courseResp.status)
        val course = Json.decodeFromString<CourseResponseDto>(courseResp.bodyAsText())

        val postResp = client.post("/api/v1/students/${student.id}/courses/${course.id}")
        assertEquals(HttpStatusCode.OK, postResp.status)
        val updated = Json.decodeFromString<StudentResponseDto>(postResp.bodyAsText())
        assertEquals("Gina", updated.name)
        assertEquals("gina@example.com", updated.email)
    }

    @Test
    fun `POST student-courses - should return 404 if student not found`() = runTestOnceApp {
        val courseResp = client.post("/api/v1/courses") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(CourseRequestDto("Data Structures")))
        }
        assertEquals(HttpStatusCode.Created, courseResp.status)
        val course = Json.decodeFromString<CourseResponseDto>(courseResp.bodyAsText())

        val resp = client.post("/api/v1/students/999/courses/${course.id}")
        assertEquals(HttpStatusCode.NotFound, resp.status)
        val error = Json.decodeFromString<ErrorResponse>(resp.bodyAsText())
        assertEquals("404", error.status)
    }
}
