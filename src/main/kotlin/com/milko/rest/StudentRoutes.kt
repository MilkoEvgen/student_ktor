package com.milko.rest

import com.milko.dto.request.StudentRequestDto
import com.milko.service.StudentService
import com.milko.utils.getIdOrThrowException
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.studentRoutes(service: StudentService) {

    route("/api/v1/students") {

        post {
            val studentRequestDto = call.receive<StudentRequestDto>()
            val studentResponseDto = service.create(studentRequestDto)
            call.respond(HttpStatusCode.Created, studentResponseDto)
        }

        get("/{id}") {
            val studentId = call.getIdOrThrowException()
            val studentDto = service.findById(studentId)
            call.respond(HttpStatusCode.OK, studentDto)
        }

        get {
            val students = service.findAll()
            call.respond(HttpStatusCode.OK, students)
        }

        get("/{id}/courses") {
            val studentId = call.getIdOrThrowException()

            val courses = service.findAllCoursesByStudentId(studentId)
            call.respond(HttpStatusCode.OK, courses)
        }

        patch("/{id}") {
            val studentId = call.getIdOrThrowException()

            val studentRequestDto = call.receive<StudentRequestDto>()
            val updatedStudent = service.update(studentId, studentRequestDto)
            call.respond(HttpStatusCode.OK, updatedStudent)
        }

        delete("/{id}") {
            val studentId = call.getIdOrThrowException()

            service.delete(studentId)
            call.respond(HttpStatusCode.NoContent)
        }

        post("/{studentId}/courses/{courseId}") {
            val studentId = call.getIdOrThrowException("studentId")
            val courseId = call.getIdOrThrowException("courseId")

            val updatedStudent = service.addCourseToStudent(studentId, courseId)
            call.respond(HttpStatusCode.OK, updatedStudent)
        }
    }
}
