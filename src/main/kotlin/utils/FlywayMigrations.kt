package com.milko.utils

import org.flywaydb.core.Flyway
import javax.sql.DataSource

object FlywayMigrations {
    fun runMigrations(dataSource: DataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .load()
            .migrate()
    }
}
