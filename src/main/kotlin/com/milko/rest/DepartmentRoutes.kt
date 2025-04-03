package com.milko.rest

import com.milko.dto.request.DepartmentRequestDto
import com.milko.service.DepartmentService
import com.milko.utils.getIdOrThrowException
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.departmentRoutes(service: DepartmentService) {

    route("/api/v1/departments") {

        post {
            val departmentRequestDto = call.receive<DepartmentRequestDto>()
            val departmentResponseDto = service.create(departmentRequestDto)
            call.respond(HttpStatusCode.Created, departmentResponseDto)
        }

        get {
            val departments = service.findAll()
            call.respond(HttpStatusCode.OK, departments)
        }

        get("/{id}") {
            val departmentId = call.getIdOrThrowException()
            val departmentDto = service.findById(departmentId)
            call.respond(HttpStatusCode.OK, departmentDto)
        }

        patch("/{id}") {
            val departmentId = call.getIdOrThrowException()
            val departmentRequestDto = call.receive<DepartmentRequestDto>()
            val updatedDepartment = service.update(departmentId, departmentRequestDto)
            call.respond(HttpStatusCode.OK, updatedDepartment)
        }

        delete("/{id}") {
            val departmentId = call.getIdOrThrowException()
            service.delete(departmentId)
            call.respond(HttpStatusCode.NoContent)
        }

        post("/{departmentId}/teacher/{teacherId}") {
            val departmentId = call.getIdOrThrowException("departmentId")
            val teacherId = call.getIdOrThrowException("teacherId")
            val updatedDepartment = service.setTeacherToDepartment(departmentId, teacherId)
            call.respond(HttpStatusCode.Created, updatedDepartment)
        }
    }
}
