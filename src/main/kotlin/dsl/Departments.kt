package com.milko.dsl

import org.jetbrains.exposed.dao.id.LongIdTable

object Departments : LongIdTable("departments"){
    val name = text("name")
    val headOfDepartmentId = reference("head_of_department_id", Teachers.id).nullable()
}
