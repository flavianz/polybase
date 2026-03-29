package ch.flavianz.adapters.postgres

import ch.flavianz.adapters.PolybaseDatabase
import ch.flavianz.core.connection.PostgresConnection
import ch.flavianz.core.model.CollectionModel

class PostgresDatabase(val postgresConnection: PostgresConnection) : PolybaseDatabase {
    override fun createCollection(collectionModel: CollectionModel) {
        val sqlString = StringBuilder("CREATE TABLE ? (? uuid primary key, ")
        for (property in collectionModel.properties) {
            sqlString.append("? ")
        }
        val statement = postgresConnection.jdbcConnection.prepareStatement(")")

    }
}