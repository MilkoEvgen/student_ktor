package com.milko.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

object DatabaseFactory {
    fun init(environment: ApplicationEnvironment): DataSource {
        val dbConfig = environment.config
            .config("ktor")
            .config("database")

        val url = dbConfig.property("url").getString()
        val user = dbConfig.property("user").getString()
        val dbPassword = dbConfig.property("password").getString()

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = url
            driverClassName = "org.postgresql.Driver"
            username = user
            password = dbPassword
            maximumPoolSize = 20
        }

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)
        return dataSource
    }
}

