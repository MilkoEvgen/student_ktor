package com.milko.integration

import com.milko.dto.request.TeacherRequestDto
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

class TeacherRoutesTest : IntegrationTestBase() {

    @Test
    fun `POST - should create teacher and return 201`() = runTestOnceApp {
        val resp = client.post("/api/v1/teachers") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(TeacherRequestDto("John")))
        }
        assertEquals(HttpStatusCode.Created, resp.status)
        val teacher = Json.decodeFromString<TeacherResponseDto>(resp.bodyAsText())
        assertEquals("John", teacher.name)
    }

    @Test
    fun `GET all - should return 200 and empty list if no teachers`() = runTestOnceApp {
        val resp = client.get("/api/v1/teachers")
        assertEquals(HttpStatusCode.OK, resp.status)
        val body = resp.bodyAsText()
        assertTrue(body.contains("[]"))
    }

    @Test
    fun `GET all - should return 200 and list of 1 if we created 1 teacher`() = runTestOnceApp {
        val createResp = client.post("/api/v1/teachers") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(TeacherRequestDto("Kate")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)

        val getAllResp = client.get("/api/v1/teachers")
        assertEquals(HttpStatusCode.OK, getAllResp.status)
        val teachers = Json.decodeFromString<List<TeacherResponseDto>>(getAllResp.bodyAsText())
        assertEquals(1, teachers.size)
        assertEquals("Kate", teachers[0].name)
    }

    @Test
    fun `GET by id - should return 200 and correct teacher`() = runTestOnceApp {
        val createResp = client.post("/api/v1/teachers") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(TeacherRequestDto("Mike")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)
        val created = Json.decodeFromString<TeacherResponseDto>(createResp.bodyAsText())
        val teacherId = created.id

        val getResp = client.get("/api/v1/teachers/$teacherId")
        assertEquals(HttpStatusCode.OK, getResp.status)
        val fetched = Json.decodeFromString<TeacherResponseDto>(getResp.bodyAsText())
        assertEquals("Mike", fetched.name)
        assertEquals(teacherId, fetched.id)
    }

    @Test
    fun `GET by id - should return 404 if teacher not found`() = runTestOnceApp {
        val resp = client.get("/api/v1/teachers/999")
        assertEquals(HttpStatusCode.NotFound, resp.status)
        val error = Json.decodeFromString<ErrorResponse>(resp.bodyAsText())
        assertEquals("404", error.status)
        assertEquals("/api/v1/teachers/999", error.path)
    }

    @Test
    fun `PATCH - should update teacher and return 200`() = runTestOnceApp {
        val createResp = client.post("/api/v1/teachers") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(TeacherRequestDto("Lily")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)
        val teacher = Json.decodeFromString<TeacherResponseDto>(createResp.bodyAsText())

        val patchResp = client.patch("/api/v1/teachers/${teacher.id}") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(TeacherRequestDto("Lily Updated")))
        }
        assertEquals(HttpStatusCode.OK, patchResp.status)
        val updated = Json.decodeFromString<TeacherResponseDto>(patchResp.bodyAsText())
        assertEquals("Lily Updated", updated.name)
    }

    @Test
    fun `PATCH - should return 404 if teacher not found`() = runTestOnceApp {
        val resp = client.patch("/api/v1/teachers/999") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(TeacherRequestDto("Ghost")))
        }
        assertEquals(HttpStatusCode.NotFound, resp.status)
        val error = Json.decodeFromString<ErrorResponse>(resp.bodyAsText())
        assertEquals("404", error.status)
    }

    @Test
    fun `DELETE - should delete existing teacher and return 204`() = runTestOnceApp {
        val createResp = client.post("/api/v1/teachers") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(TeacherRequestDto("Tony")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)
        val teacher = Json.decodeFromString<TeacherResponseDto>(createResp.bodyAsText())

        val deleteResp = client.delete("/api/v1/teachers/${teacher.id}")
        assertEquals(HttpStatusCode.NoContent, deleteResp.status)

        val getResp = client.get("/api/v1/teachers/${teacher.id}")
        assertEquals(HttpStatusCode.NotFound, getResp.status)
    }

    @Test
    fun `DELETE - should return 204 if teacher not exist`() = runTestOnceApp {
        val resp = client.delete("/api/v1/teachers/999")
        assertEquals(HttpStatusCode.NoContent, resp.status)
    }
}
