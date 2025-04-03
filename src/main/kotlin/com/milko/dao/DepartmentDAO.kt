package com.milko.dao

import com.milko.dsl.Departments
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class DepartmentDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DepartmentDAO>(Departments)

    var name by Departments.name
    var headOfDepartment by TeacherDAO.optionalReferencedOn(Departments.headOfDepartmentId)
}