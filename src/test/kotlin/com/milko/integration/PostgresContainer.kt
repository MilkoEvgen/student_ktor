package com.milko.integration

import org.testcontainers.containers.PostgreSQLContainer

object PostgresContainer : PostgreSQLContainer<Nothing>("postgres:16") {
    init {
        withDatabaseName("test_db")
        withUsername("test")
        withPassword("test")
        start()
    }
}