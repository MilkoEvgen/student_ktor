package com.milko.rest

import com.milko.dto.request.TeacherRequestDto
import com.milko.service.TeacherService
import com.milko.utils.getIdOrThrowException
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.teacherRoutes(service: TeacherService) {

    route("/api/v1/teachers") {

        post {
            val teacherRequestDto = call.receive<TeacherRequestDto>()
            val teacherResponseDto = service.create(teacherRequestDto)
            call.respond(HttpStatusCode.Created, teacherResponseDto)
        }

        get {
            val teachers = service.findAll()
            call.respond(HttpStatusCode.OK, teachers)
        }

        get("/{id}") {
            val teacherId = call.getIdOrThrowException()
            val teacherDto = service.findById(teacherId)
            call.respond(HttpStatusCode.OK, teacherDto)
        }

        patch("/{id}") {
            val teacherId = call.getIdOrThrowException()
            val teacherRequestDto = call.receive<TeacherRequestDto>()
            val updatedTeacher = service.update(teacherId, teacherRequestDto)
            call.respond(HttpStatusCode.OK, updatedTeacher)
        }

        delete("/{id}") {
            val teacherId = call.getIdOrThrowException()
            service.delete(teacherId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
