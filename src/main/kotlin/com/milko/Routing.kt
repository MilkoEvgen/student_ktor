package com.milko.com.milko

import com.milko.rest.courseRoutes
import com.milko.rest.departmentRoutes
import com.milko.rest.studentRoutes
import com.milko.rest.teacherRoutes
import com.milko.service.CourseService
import com.milko.service.DepartmentService
import com.milko.service.StudentService
import com.milko.service.TeacherService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(studentService: StudentService,
                                 courseService: CourseService,
                                 teacherService: TeacherService,
                                 departmentService: DepartmentService) {
    routing {
        studentRoutes(studentService)
        courseRoutes(courseService)
        teacherRoutes(teacherService)
        departmentRoutes(departmentService)
    }
}
