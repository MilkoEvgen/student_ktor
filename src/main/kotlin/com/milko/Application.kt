package com.milko

import com.milko.com.milko.configureRouting
import com.milko.exceptionhandling.configureExceptionHandling
import com.milko.repository.CourseRepository
import com.milko.repository.DepartmentRepository
import com.milko.repository.StudentRepository
import com.milko.repository.TeacherRepository
import com.milko.service.CourseService
import com.milko.service.DepartmentService
import com.milko.service.StudentService
import com.milko.service.TeacherService
import com.milko.utils.DatabaseFactory
import com.milko.utils.FlywayMigrations
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val dataSource = DatabaseFactory.init(environment)
    FlywayMigrations.runMigrations(dataSource)

    configureExceptionHandling()
    install(ContentNegotiation) {
        json()
    }

    val courseRepository = CourseRepository()
    val studentRepository = StudentRepository()
    val teacherRepository = TeacherRepository()
    val departmentRepository = DepartmentRepository()

    val studentService = StudentService(studentRepository, courseRepository)
    val courseService = CourseService(courseRepository, studentRepository, teacherRepository)
    val teacherService = TeacherService(teacherRepository, courseRepository)
    val departmentService = DepartmentService(departmentRepository, teacherRepository)

    configureRouting(studentService, courseService, teacherService, departmentService)
}
