package com.milko.utils

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> dbQuery(block: suspend () -> T): T {
    return newSuspendedTransaction { block() }
}