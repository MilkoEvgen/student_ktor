package com.milko.exceptionhandling

import com.milko.exceptions.EntityNotFoundException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<EntityNotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    status = HttpStatusCode.NotFound.value.toString(),
                    error = HttpStatusCode.NotFound.description,
                    message = cause.message ?: "Entity not found",
                    path = call.request.path()
                )
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    status = HttpStatusCode.BadRequest.value.toString(),
                    error = HttpStatusCode.BadRequest.description,
                    message = cause.message ?: "Invalid input",
                    path = call.request.path()
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    status = HttpStatusCode.InternalServerError.value.toString(),
                    error = HttpStatusCode.InternalServerError.description,
                    message = "Internal server error: ${cause.localizedMessage}",
                    path = call.request.path()
                )
            )
        }
    }
}
