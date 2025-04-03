package com.milko.rest

import com.milko.dto.request.CourseRequestDto
import com.milko.service.CourseService
import com.milko.utils.getIdOrThrowException
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.courseRoutes(service: CourseService){

    route("/api/v1/courses"){
        post{
            val courseRequestDto = call.receive<CourseRequestDto>()
            val courseResponseDto = service.create(courseRequestDto)
            call.respond(HttpStatusCode.Created, courseResponseDto)
        }

        get("/{id}") {
            val courseId = call.getIdOrThrowException()
            val courseDto = service.findById(courseId)
            call.respond(HttpStatusCode.OK, courseDto)
        }

        get {
            val courses = service.findAll()
            call.respond(HttpStatusCode.OK, courses)
        }

        patch("/{id}") {
            val courseId = call.getIdOrThrowException()
            val courseRequestDto = call.receive<CourseRequestDto>()
            val updatedCourse = service.update(courseId, courseRequestDto)
            call.respond(HttpStatusCode.OK, updatedCourse)
        }

        delete("/{id}") {
            val courseId = call.getIdOrThrowException()
            service.delete(courseId)
            call.respond(HttpStatusCode.NoContent)
        }

        post("{courseId}/teacher/{teacherId}") {
            val courseId = call.getIdOrThrowException("courseId")
            val teacherId = call.getIdOrThrowException("teacherId")
            val updatedCourse = service.setTeacherToCourse(courseId, teacherId)
            call.respond(HttpStatusCode.OK, updatedCourse)
        }

    }
}