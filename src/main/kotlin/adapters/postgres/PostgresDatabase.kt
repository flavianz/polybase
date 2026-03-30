package ch.flavianz.adapters.postgres

import ch.flavianz.adapters.PolybaseDatabase
import ch.flavianz.core.connection.PostgresConnection
import ch.flavianz.core.model.CollectionModel
import java.util.UUID

class PostgresDatabase(val postgresConnection: PostgresConnection) : PolybaseDatabase {

    override val collections: MutableMap<String, CollectionModel> = mutableMapOf()

    override fun createCollection(collectionModel: CollectionModel) {
        fun sanitize(name: String): String {
            require(name.matches(Regex("[a-zA-Z0-9_]+"))) { "Invalid identifier: $name" }
            return "\"$name\""
        }

        val tableName = sanitize(collectionModel.name)
        val pkName = sanitize("${collectionModel.name}_pk")

        val sqlString = StringBuilder("CREATE TABLE $tableName ($pkName uuid PRIMARY KEY")
        for (property in collectionModel.properties) {
            val colName = sanitize("${property.key}_prop")
            val colType = property.value.toSqlType()
            sqlString.append(", $colName $colType")
        }
        sqlString.append(")")

        postgresConnection.jdbcConnection.createStatement().use { it.executeUpdate(sqlString.toString()) }

        // Register after successful creation
        collections[collectionModel.name] = collectionModel
        println("Registered collection '${collectionModel.name}'")
    }

    override fun insert(collectionName: String, data: Map<String, Any>): UUID {
        validate(collectionName, data) // throws SchemaValidationException if invalid

        fun sanitize(name: String): String {
            require(name.matches(Regex("[a-zA-Z0-9_]+"))) { "Invalid identifier: $name" }
            return "\"$name\""
        }

        val id = UUID.randomUUID()
        val tableName = sanitize(collectionName)
        val pkCol = sanitize("${collectionName}_pk")

        val keys = data.keys.toList()
        val columns = listOf(pkCol) + keys.map { sanitize("${it}_prop") }
        val placeholders = columns.map { "?" }.joinToString(", ")

        val sql = "INSERT INTO $tableName (${columns.joinToString(", ")}) VALUES ($placeholders)"

        postgresConnection.jdbcConnection.prepareStatement(sql).use { statement ->
            statement.setObject(1, id)
            keys.forEachIndexed { i, key -> statement.setObject(i + 2, data[key]) }
            statement.executeUpdate()
        }

        return id
    }
}