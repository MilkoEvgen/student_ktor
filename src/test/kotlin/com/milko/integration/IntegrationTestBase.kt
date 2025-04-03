package com.milko.integration

import com.milko.module
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import java.sql.DriverManager

abstract class IntegrationTestBase {

    companion object {
        private lateinit var testBuilder: ApplicationTestBuilder

        @BeforeAll
        @JvmStatic
        fun setup(): Unit = runBlocking {
            val flyway = Flyway.configure()
                .dataSource(
                    PostgresContainer.jdbcUrl,
                    PostgresContainer.username,
                    PostgresContainer.password
                )
                .cleanDisabled(false)
                .load()

            flyway.clean()
            flyway.migrate()

            testBuilder = ApplicationTestBuilder().apply {
                environment {
                    config = MapApplicationConfig(
                        "ktor.database.url" to PostgresContainer.jdbcUrl,
                        "ktor.database.user" to PostgresContainer.username,
                        "ktor.database.password" to PostgresContainer.password,
                        "ktor.environment" to "test"
                    )
                }
                application {
                    module()
                }
            }
        }
    }

    protected fun runTestOnceApp(block: suspend ApplicationTestBuilder.() -> Unit) {
        runBlocking {
            testBuilder.block()
        }
    }

    @AfterEach
    fun truncateTables() {
        DriverManager.getConnection(PostgresContainer.jdbcUrl, "test", "test").use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeUpdate("TRUNCATE TABLE teachers, departments, course_student, students, courses")
            }
        }
    }
}



