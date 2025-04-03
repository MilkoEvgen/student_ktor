package com.milko.integration

import com.milko.dto.request.DepartmentRequestDto
import com.milko.dto.request.TeacherRequestDto
import com.milko.dto.response.DepartmentResponseDto
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

class DepartmentRoutesTest : IntegrationTestBase() {

    @Test
    fun `POST - should create department and return 201`() = runTestOnceApp {
        val requestBody = DepartmentRequestDto("Math Dept")
        val response = client.post("/api/v1/departments") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(requestBody))
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val createdDepartment = Json.decodeFromString<DepartmentResponseDto>(response.bodyAsText())
        assertEquals("Math Dept", createdDepartment.name)
        assertTrue(createdDepartment.id > 0)
    }

    @Test
    fun `GET all - should return 200 and empty list if no departments`() = runTestOnceApp {
        val response = client.get("/api/v1/departments")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        println("GET all empty: $body")
        assertTrue(body.contains("[]"))
    }

    @Test
    fun `GET all - should return 200 and list of 1 if we created 1 department`() = runTestOnceApp {
        val createResp = client.post("/api/v1/departments") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(DepartmentRequestDto("Science Dept")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)

        val getAllResp = client.get("/api/v1/departments")
        assertEquals(HttpStatusCode.OK, getAllResp.status)

        val body = getAllResp.bodyAsText()
        println("GET all body: $body")

        val depts = Json.decodeFromString<List<DepartmentResponseDto>>(body)
        assertEquals(1, depts.size)
        assertEquals("Science Dept", depts[0].name)
    }

    @Test
    fun `GET by id - should return 200 and correct department`() = runTestOnceApp {
        val createResp = client.post("/api/v1/departments") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(DepartmentRequestDto("Arts Dept")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)

        val createdDepartment = Json.decodeFromString<DepartmentResponseDto>(createResp.bodyAsText())
        val deptId = createdDepartment.id

        val getResp = client.get("/api/v1/departments/$deptId")
        assertEquals(HttpStatusCode.OK, getResp.status)

        val fetchedDept = Json.decodeFromString<DepartmentResponseDto>(getResp.bodyAsText())
        assertEquals(deptId, fetchedDept.id)
        assertEquals("Arts Dept", fetchedDept.name)
    }

    @Test
    fun `GET by id - should return 404 if no such department`() = runTestOnceApp {
        val response = client.get("/api/v1/departments/999")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val body = response.bodyAsText()
        println("GET 999 dept body: $body")

        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("404", errorResponse.status)
        assertEquals("Not Found", errorResponse.error)
        assertTrue(errorResponse.message.contains("not found", ignoreCase = true))
        assertEquals("/api/v1/departments/999", errorResponse.path)
    }

    @Test
    fun `PATCH - should update department name and return 200`() = runTestOnceApp {
        val createResp = client.post("/api/v1/departments") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(DepartmentRequestDto("Initial Dept")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)
        val created = Json.decodeFromString<DepartmentResponseDto>(createResp.bodyAsText())

        val patchResp = client.patch("/api/v1/departments/${created.id}") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(DepartmentRequestDto("Updated Dept")))
        }
        assertEquals(HttpStatusCode.OK, patchResp.status)

        val updated = Json.decodeFromString<DepartmentResponseDto>(patchResp.bodyAsText())
        assertEquals("Updated Dept", updated.name)
    }

    @Test
    fun `PATCH - should return 404 if trying to update non-existing department`() = runTestOnceApp {
        val response = client.patch("/api/v1/departments/999") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(DepartmentRequestDto("No Dept")))
        }
        assertEquals(HttpStatusCode.NotFound, response.status)

        val body = response.bodyAsText()
        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("404", errorResponse.status)
        assertEquals("Not Found", errorResponse.error)
        assertTrue(errorResponse.message.contains("not found", ignoreCase = true))
        assertEquals("/api/v1/departments/999", errorResponse.path)
    }

    @Test
    fun `DELETE - should delete existing department and return 204`() = runTestOnceApp {
        val createResp = client.post("/api/v1/departments") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(DepartmentRequestDto("ToBeDeleted")))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)
        val created = Json.decodeFromString<DepartmentResponseDto>(createResp.bodyAsText())
        val deptId = created.id

        val deleteResp = client.delete("/api/v1/departments/$deptId")
        assertEquals(HttpStatusCode.NoContent, deleteResp.status)

        val getResp = client.get("/api/v1/departments/$deptId")
        assertEquals(HttpStatusCode.NotFound, getResp.status)
    }

    @Test
    fun `DELETE - should return 204 if no such department`() = runTestOnceApp {
        val resp = client.delete("/api/v1/departments/999")
        assertEquals(HttpStatusCode.NoContent, resp.status)
    }

    @Test
    fun `POST teacher - should set teacher to department and return 201`() = runTestOnceApp {
        val createDeptResp = client.post("/api/v1/departments") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(DepartmentRequestDto("History Dept")))
        }
        assertEquals(HttpStatusCode.Created, createDeptResp.status)
        val createdDept = Json.decodeFromString<DepartmentResponseDto>(createDeptResp.bodyAsText())
        val deptId = createdDept.id

        val createTeacherResp = client.post("/api/v1/teachers") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(TeacherRequestDto("John")))
        }
        assertEquals(HttpStatusCode.Created, createTeacherResp.status)
        val createdTeacher = Json.decodeFromString<TeacherResponseDto>(createTeacherResp.bodyAsText())
        val teacherId = createdTeacher.id

        val postResp = client.post("/api/v1/departments/$deptId/teacher/$teacherId")
        assertEquals(HttpStatusCode.Created, postResp.status)

        val updatedDept = Json.decodeFromString<DepartmentResponseDto>(postResp.bodyAsText())
        assertEquals(deptId, updatedDept.id)
    }

    @Test
    fun `POST teacher - should return 404 if no such department`() = runTestOnceApp {
        val resp = client.post("/api/v1/departments/999/teacher/1")
        assertEquals(HttpStatusCode.NotFound, resp.status)

        val body = resp.bodyAsText()
        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("404", errorResponse.status)
        assertEquals("Not Found", errorResponse.error)
        assertTrue(errorResponse.message.contains("not found", ignoreCase = true))
        assertEquals("/api/v1/departments/999/teacher/1", errorResponse.path)
    }
}
