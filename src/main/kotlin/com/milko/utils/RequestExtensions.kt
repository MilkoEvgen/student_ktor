package com.milko.utils

import io.ktor.server.application.*

fun ApplicationCall.getIdOrThrowException(paramName: String = "id"): Long {
    return parameters[paramName]?.toLongOrNull()
        ?: throw IllegalArgumentException("Invalid '$paramName'")
}

