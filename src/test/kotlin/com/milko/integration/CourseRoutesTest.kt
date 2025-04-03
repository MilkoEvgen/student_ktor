package com.milko.integration

import com.milko.dto.request.CourseRequestDto
import com.milko.dto.request.TeacherRequestDto
import com.milko.dto.response.CourseResponseDto
import com.milko.dto.response.TeacherResponseDto
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

class CourseRoutesTest : IntegrationTestBase() {

    @Test
    fun `POST - should create course and return 201`() = runTestOnceApp {
        val requestBody = CourseRequestDto(title = "Math")
        val response = client.post("/api/v1/courses") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(requestBody))
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `POST - should fail with 500 if course title already exists`() = runTestOnceApp {
        val requestBody = CourseRequestDto(title = "Math")
        val firstResponse = client.post("/api/v1/courses") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(requestBody))
        }
        assertEquals(HttpStatusCode.Created, firstResponse.status)

        val secondResponse = client.post("/api/v1/courses") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(requestBody))
        }
        assertEquals(HttpStatusCode.InternalServerError, secondResponse.status)

        val responseBody = secondResponse.bodyAsText()

        val errorResponse = Json.decodeFromString<ErrorResponse>(responseBody)
        assertEquals("500", errorResponse.status)
        assertEquals("Internal Server Error", errorResponse.error)
        assertTrue(errorResponse.message.contains("duplicate key") || errorResponse.message.isNotBlank())
        assertEquals("/api/v1/courses", errorResponse.path)
    }

    @Test
    fun `GET all - should return 200 and empty list if no courses`() = runTestOnceApp {
        val response = client.get("/api/v1/courses")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        assertTrue(body.contains("[]"))
    }

    @Test
    fun `GET all - should return 200 and list of 1 if we created 1 course`() = runTestOnceApp {
        val requestBody = CourseRequestDto(title = "Physics")
        val postResp = client.post("/api/v1/courses") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(requestBody))
        }
        assertEquals(HttpStatusCode.Created, postResp.status)

        val getAllResp = client.get("/api/v1/courses")
        assertEquals(HttpStatusCode.OK, getAllResp.status)
        val body = getAllResp.bodyAsText()

        val courses = Json.decodeFromString<List<CourseResponseDto>>(body)
        assertEquals(1, courses.size)
        assertEquals("Physics", courses[0].title)
        assertTrue(body.contains("Physics"))
    }

    @Test
    fun `GET by id - should return 200 and correct course`() = runTestOnceApp {
        val requestBody = CourseRequestDto(title = "Biology")
        val createResp = client.post("/api/v1/courses") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(requestBody))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)

        val createdCourse = Json.decodeFromString<CourseResponseDto>(createResp.bodyAsText())
        val courseId = createdCourse.id

        val getResp = client.get("/api/v1/courses/$courseId")
        assertEquals(HttpStatusCode.OK, getResp.status)

        val courseJson = getResp.bodyAsText()
        assertTrue(courseJson.contains("Biology"))
    }

    @Test
    fun `GET by id - should return 404 if no such course`() = runTestOnceApp {
        val response = client.get("/api/v1/courses/999")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val body = response.bodyAsText()

        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("404", errorResponse.status)
        assertEquals("Not Found", errorResponse.error)
        assertTrue(errorResponse.message.contains("not found", ignoreCase = true))
        assertEquals("/api/v1/courses/999", errorResponse.path)
    }

    @Test
    fun `PATCH - should update course title and return 200`() = runTestOnceApp {
        val createResp = client.post("/api/v1/courses") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(CourseRequestDto("Chemistry")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)
        val createdCourse = Json.decodeFromString<CourseResponseDto>(createResp.bodyAsText())
        val courseId = createdCourse.id

        val patchResp = client.patch("/api/v1/courses/$courseId") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(CourseRequestDto("Organic Chemistry")))
        }
        assertEquals(HttpStatusCode.OK, patchResp.status)
        val patchedBody = patchResp.bodyAsText()
        assertTrue(patchedBody.contains("Organic Chemistry"))
    }

    @Test
    fun `PATCH - should return 404 if trying to update non-existing course`() = runTestOnceApp {
        val response = client.patch("/api/v1/courses/999") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(CourseRequestDto("Philosophy")))
        }
        assertEquals(HttpStatusCode.NotFound, response.status)

        val body = response.bodyAsText()

        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("404", errorResponse.status)
        assertEquals("Not Found", errorResponse.error)
        assertTrue(errorResponse.message.contains("not found", ignoreCase = true))
        assertEquals("/api/v1/courses/999", errorResponse.path)
    }


    @Test
    fun `DELETE - should delete existing course and return 204`() = runTestOnceApp {
        val createResp = client.post("/api/v1/courses") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(CourseRequestDto("History")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)
        val createdCourse = Json.decodeFromString<CourseResponseDto>(createResp.bodyAsText())
        val courseId = createdCourse.id

        val deleteResp = client.delete("/api/v1/courses/$courseId")
        assertEquals(HttpStatusCode.NoContent, deleteResp.status)

        val getResp = client.get("/api/v1/courses/$courseId")
        assertEquals(HttpStatusCode.NotFound, getResp.status)
    }

    @Test
    fun `DELETE - should return 200 if no such course`() = runTestOnceApp {
        val courseId = 999
        val deleteResp = client.delete("/api/v1/courses/$courseId")
        assertEquals(HttpStatusCode.NoContent, deleteResp.status)
    }

    @Test
    fun `POST teacher - should set teacher for existing course and return 200`() = runTestOnceApp {
        val createCourseResp = client.post("/api/v1/courses") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(CourseRequestDto("Music")))
        }
        assertEquals(HttpStatusCode.Created, createCourseResp.status)
        val createdCourse = Json.decodeFromString<CourseResponseDto>(createCourseResp.bodyAsText())
        val courseId = createdCourse.id

        val createTeacherResp = client.post("/api/v1/teachers") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(TeacherRequestDto("John")))
        }

        assertEquals(HttpStatusCode.Created, createTeacherResp.status)
        val createdTeacher = Json.decodeFromString<TeacherResponseDto>(createTeacherResp.bodyAsText())
        val teacherId = createdTeacher.id

        val postResp = client.post("/api/v1/courses/$courseId/teacher/$teacherId")
        assertEquals(HttpStatusCode.OK, postResp.status)
        val body = postResp.bodyAsText()
        assertTrue(body.contains("Music"))
    }

    @Test
    fun `POST teacher - should return 404 if course not found`() = runTestOnceApp {
        val response = client.post("/api/v1/courses/999/teacher/1")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val body = response.bodyAsText()

        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("404", errorResponse.status)
        assertEquals("Not Found", errorResponse.error)
        assertTrue(errorResponse.message.contains("not found", ignoreCase = true))
        assertEquals("/api/v1/courses/999/teacher/1", errorResponse.path)
    }
}
