package ch.flavianz.adapters.mongo

import ch.flavianz.adapters.PolybaseDatabase
import ch.flavianz.core.connection.MongoConnection
import ch.flavianz.core.model.CollectionModel
import org.bson.Document
import org.bson.BsonBinary
import org.bson.BsonBinarySubType
import java.util.UUID

class MongoDatabase(val mongoConnection: MongoConnection) : PolybaseDatabase {

    override val collections: MutableMap<String, CollectionModel> = mutableMapOf()

    override fun createCollection(collectionModel: CollectionModel) {
        val db = mongoConnection.mongoDatabase
        db.createCollection(collectionModel.name)

        if (collectionModel.properties.isNotEmpty()) {
            val properties = Document()
            properties["${collectionModel.name}_pk"] = Document("bsonType", "binData")
            for ((key, dataType) in collectionModel.properties) {
                properties["${key}_prop"] = Document("bsonType", dataType.toBsonType())
            }

            val command = Document("collMod", collectionModel.name)
                .append("validator", Document("\$jsonSchema",
                    Document("bsonType", "object")
                        .append("required", listOf("${collectionModel.name}_pk"))
                        .append("properties", properties)
                ))
                .append("validationLevel", "moderate")
                .append("validationAction", "error")

            db.runCommand(command)
        }

        // Register after successful creation
        collections[collectionModel.name] = collectionModel
        println("Registered collection '${collectionModel.name}'")
    }

    override fun insert(collectionName: String, data: Map<String, Any>): UUID {
        validate(collectionName, data) // throws SchemaValidationException if invalid

        val id = UUID.randomUUID()
        val document = Document()

        document["${collectionName}_pk"] = BsonBinary(BsonBinarySubType.UUID_STANDARD, id.toByteArray())
        for ((key, value) in data) {
            document["${key}_prop"] = value
        }

        mongoConnection.mongoDatabase.getCollection(collectionName).insertOne(document)
        return id
    }

    private fun UUID.toByteArray(): ByteArray {
        val msb = mostSignificantBits
        val lsb = leastSignificantBits
        return ByteArray(16) { i ->
            when {
                i < 8 -> (msb ushr (56 - i * 8)).toByte()
                else  -> (lsb ushr (56 - (i - 8) * 8)).toByte()
            }
        }
    }
}