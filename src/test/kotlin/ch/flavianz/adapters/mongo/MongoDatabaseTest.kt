package ch.flavianz.adapters.mongo

import ch.flavianz.core.connection.MongoConnection
import ch.flavianz.core.exception.SchemaValidationException
import ch.flavianz.core.model.CollectionModel
import ch.flavianz.core.model.DataType
import com.mongodb.client.MongoCollection
import io.mockk.*
import org.bson.Document
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MongoDatabaseTest {

    private lateinit var db: MongoDatabase
    private val mockMongoDb    = mockk<com.mongodb.client.MongoDatabase>(relaxed = true)
    private val mockCollection = mockk<MongoCollection<Document>>(relaxed = true)
    private val mockConnection = mockk<MongoConnection>(relaxed = true) // relaxed = true here

    @BeforeEach
    fun setup() {
        // Stub the property directly on the mock — no real getter is called
        every { mockConnection.mongoDatabase } returns mockMongoDb
        every { mockMongoDb.getCollection("user") } returns mockCollection
        every { mockMongoDb.runCommand(any()) } returns Document("ok", 1.0)

        db = MongoDatabase(mockConnection)
    }

    private val userModel = CollectionModel(
        name = "user",
        properties = mapOf(
            "username" to DataType.STRING,
            "age"      to DataType.INT,
        )
    )

    @Test
    fun `createCollection registers model`() {
        db.createCollection(userModel)
        assertTrue(db.collections.containsKey("user"))
    }

    @Test
    fun `createCollection calls createCollection on MongoDatabase`() {
        db.createCollection(userModel)
        verify { mockMongoDb.createCollection("user") }
    }

    @Test
    fun `createCollection attaches validator via runCommand`() {
        db.createCollection(userModel)
        verify { mockMongoDb.runCommand(match { (it as Document).getString("collMod") == "user" }) }
    }

    @Test
    fun `insert returns a UUID`() {
        db.createCollection(userModel)
        val id = db.insert("user", mapOf("username" to "flavian", "age" to 17))
        assertNotNull(id)
    }

    @Test
    fun `insert calls insertOne on the collection`() {
        db.createCollection(userModel)
        db.insert("user", mapOf("username" to "flavian", "age" to 17))
        verify { mockCollection.insertOne(any()) }
    }

    @Test
    fun `insert with wrong type throws SchemaValidationException before hitting DB`() {
        db.createCollection(userModel)
        assertThrows<SchemaValidationException> {
            db.insert("user", mapOf("username" to "flavian", "age" to "notAnInt"))
        }
        verify(exactly = 0) { mockCollection.insertOne(any()) }
    }

    @Test
    fun `insert into unregistered collection throws SchemaValidationException`() {
        assertThrows<SchemaValidationException> {
            db.insert("ghost", mapOf("username" to "flavian"))
        }
    }
}