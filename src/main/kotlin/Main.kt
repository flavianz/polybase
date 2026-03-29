package ch.flavianz

import ch.flavianz.core.connection.ConnectionManager
import ch.flavianz.core.connection.MongoConnection
import ch.flavianz.core.connection.PostgresConnection

fun main() {
    val manager = ConnectionManager()

    // Register both connections
    manager.register(
        PostgresConnection(
            host = "localhost",
            port = 5432,
            database = "polybase",
            username = "postgres",
            password = "password"
        )
    )
    manager.register(
        MongoConnection(
            host = "localhost",
            port = 27017,
            database = "polybase"
        )
    )

    // Connect to everything at once
    manager.connectAll()

    // Health check
    val health = manager.healthCheck()
    health.forEach { (name, alive) ->
        println("$name → ${if (alive) "OK" else "UNREACHABLE"}")
    }
    // Use a specific connection directly when you need raw access
    val pg = manager.get<PostgresConnection>("PostgreSQL[polybase]")
    val resultSet = pg.jdbcConnection.createStatement().executeQuery("SELECT version()")
    if (resultSet.next()) println("Postgres version: ${resultSet.getString(1)}")

    val mongo = manager.get<MongoConnection>("MongoDB[polybase]")
    println("MongoDB collections: ${mongo.mongoDatabase.listCollectionNames().toList()}")

    // Clean up
    manager.disconnectAll()
}