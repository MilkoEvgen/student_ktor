package com.milko.repository

import com.milko.dao.TeacherDAO
import com.milko.dsl.Departments
import com.milko.dsl.Teachers
import com.milko.exceptions.EntityNotFoundException
import com.milko.model.Department
import com.milko.model.Teacher
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class TeacherRepository {

    suspend fun create(teacher: Teacher): Long {
        return newSuspendedTransaction {
            TeacherDAO.new {
                name = teacher.name
            }.id.value
        }
    }

    suspend fun update(teacher: Teacher): Boolean {
        return newSuspendedTransaction {
            val existingTeacher = TeacherDAO.findById(teacher.id) ?: return@newSuspendedTransaction false
            existingTeacher.apply {
                name = teacher.name
            }
            true
        }
    }

    suspend fun findByIdOrThrowExceptionIfNotExists(teacherId: Long): Teacher {
        return newSuspendedTransaction {
            (Teachers leftJoin Departments)
                .select { Teachers.id eq teacherId }
                .singleOrNull()
                ?.let { row ->
                    Teacher(
                        id = row[Teachers.id].value,
                        name = row[Teachers.name],
                        department = row[Departments.id]?.let {
                            Department(
                                id = it.value,
                                name = row[Departments.name],
                                headOfDepartment = null
                            )
                        }
                    )
                } ?: throw EntityNotFoundException("Teacher with id $teacherId not found")
        }
    }

    suspend fun findAll(): List<Teacher> {
        return newSuspendedTransaction {
            (Teachers leftJoin Departments)
                .selectAll()
                .map { row ->
                    Teacher(
                        id = row[Teachers.id].value,
                        name = row[Teachers.name],
                        department = row[Departments.id]?.let {
                            Department(
                                id = it.value,
                                name = row[Departments.name],
                                headOfDepartment = null
                            )
                        }
                    )
                }
        }
    }

    suspend fun throwExceptionIfNotExists(teacherId: Long) {
        newSuspendedTransaction {
            val exists = Teachers.select { Teachers.id eq teacherId }
                .limit(1)
                .empty()
                .not()

            if (!exists) {
                throw EntityNotFoundException("Teacher with id $teacherId not found")
            }
        }
    }

    suspend fun deleteById(teacherId: Long) {
        newSuspendedTransaction {
            TeacherDAO.findById(teacherId)?.delete()
        }
    }
}