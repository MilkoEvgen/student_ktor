package com.milko.repository

import com.milko.dao.CourseDAO
import com.milko.dao.DepartmentDAO
import com.milko.dsl.Courses
import com.milko.dsl.Departments
import com.milko.dsl.Teachers
import com.milko.exceptions.EntityNotFoundException
import com.milko.model.Course
import com.milko.model.Department
import com.milko.model.Teacher
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class DepartmentRepository {

    suspend fun create(department: Department): Long {
        return newSuspendedTransaction {
            DepartmentDAO.new {
                name = department.name
                headOfDepartment = null
            }.id.value
        }
    }

    suspend fun update(department: Department): Boolean {
        return newSuspendedTransaction {
            val existingDepartment = DepartmentDAO.findById(department.id) ?: return@newSuspendedTransaction false
            existingDepartment.apply {
                name = department.name
            }
            true
        }
    }

    suspend fun findByIdOrThrowExceptionIfNotExists(departmentId: Long): Department {
        return newSuspendedTransaction {
            (Departments leftJoin Teachers)
                .select { Departments.id eq departmentId }
                .singleOrNull()
                ?.let { row ->
                    Department(
                        id = row[Departments.id].value,
                        name = row[Departments.name],
                        headOfDepartment = row[Teachers.id]?.let { teacherId ->
                            Teacher(
                                id = teacherId.value,
                                name = row[Teachers.name],
                                department = null
                            )
                        }
                    )
                } ?: throw EntityNotFoundException("Department with id $departmentId not found")
        }
    }


    suspend fun findAll(): List<Department> {
        return newSuspendedTransaction {
            (Departments leftJoin Teachers)
                .selectAll()
                .map { row ->
                    Department(
                        id = row[Departments.id].value,
                        name = row[Departments.name],
                        headOfDepartment = row[Teachers.id]?.let {
                            Teacher(
                                id = it.value,
                                name = row[Teachers.name],
                                department = null
                            )
                        }
                    )
                }
        }
    }

    suspend fun throwExceptionIfNotExists(departmentId: Long) {
        newSuspendedTransaction {
            val exists = Departments.select { Departments.id eq departmentId }
                .limit(1)
                .empty()
                .not()

            if (!exists) {
                throw EntityNotFoundException("Department with id $departmentId not found")
            }
        }
    }

    suspend fun deleteById(departmentId: Long) {
        newSuspendedTransaction {
            DepartmentDAO.findById(departmentId)?.delete()
        }
    }
}