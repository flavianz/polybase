package ch.flavianz.adapters

import ch.flavianz.core.exception.SchemaValidationException
import ch.flavianz.core.model.CollectionModel
import ch.flavianz.core.model.DataType
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date
import java.util.UUID

class ValidationTest {

    // Minimal concrete PolybaseDatabase just for testing validate()
    private lateinit var db: PolybaseDatabase

    private val userModel = CollectionModel(
        name = "user",
        properties = mapOf(
            "username" to DataType.STRING,
            "age"      to DataType.INT,
            "score"    to DataType.DOUBLE,
            "active"   to DataType.BOOL,
            "joined"   to DataType.DATE,
        )
    )

    @BeforeEach
    fun setup() {
        db = object : PolybaseDatabase {
            override val collections: MutableMap<String, CollectionModel> =
                mutableMapOf("user" to userModel)
            override fun createCollection(collectionModel: CollectionModel) {}
            override fun insert(collectionName: String, data: Map<String, Any>) = mockk<UUID>()
        }
    }

    // --- Happy path ---

    @Test
    fun `valid data passes validation`() {
        assertDoesNotThrow {
            db.validate("user", mapOf(
                "username" to "flavian",
                "age"      to 17,
                "score"    to 9.8,
                "active"   to true,
                "joined"   to Date(),
            ))
        }
    }

    // --- Unknown collection ---

    @Test
    fun `unknown collection throws SchemaValidationException`() {
        val ex = assertThrows<SchemaValidationException> {
            db.validate("nonexistent", mapOf("username" to "flavian"))
        }
        assertTrue(ex.message!!.contains("nonexistent"))
    }

    // --- Missing fields ---

    @Test
    fun `missing required property throws SchemaValidationException`() {
        val ex = assertThrows<SchemaValidationException> {
            db.validate("user", mapOf(
                "username" to "flavian",
                // age, score, active, joined missing
            ))
        }
        assertTrue(ex.message!!.contains("Missing"))
    }

    // --- Wrong types ---

    @Test
    fun `string value for INT field throws SchemaValidationException`() {
        val ex = assertThrows<SchemaValidationException> {
            db.validate("user", mapOf(
                "username" to "flavian",
                "age"      to "seventeen", // wrong
                "score"    to 9.8,
                "active"   to true,
                "joined"   to Date(),
            ))
        }
        assertTrue(ex.message!!.contains("age"))
        assertTrue(ex.message!!.contains("INT"))
    }

    @Test
    fun `int value for STRING field throws SchemaValidationException`() {
        val ex = assertThrows<SchemaValidationException> {
            db.validate("user", mapOf(
                "username" to 123, // wrong
                "age"      to 17,
                "score"    to 9.8,
                "active"   to true,
                "joined"   to Date(),
            ))
        }
        assertTrue(ex.message!!.contains("username"))
        assertTrue(ex.message!!.contains("STRING"))
    }

    @Test
    fun `double value for BOOL field throws SchemaValidationException`() {
        val ex = assertThrows<SchemaValidationException> {
            db.validate("user", mapOf(
                "username" to "flavian",
                "age"      to 17,
                "score"    to 9.8,
                "active"   to 1.0, // wrong
                "joined"   to Date(),
            ))
        }
        assertTrue(ex.message!!.contains("active"))
    }

    // --- Unknown keys ---

    @Test
    fun `unknown property key throws SchemaValidationException`() {
        val ex = assertThrows<SchemaValidationException> {
            db.validate("user", mapOf(
                "username"     to "flavian",
                "age"          to 17,
                "score"        to 9.8,
                "active"       to true,
                "joined"       to Date(),
                "notInSchema"  to "hacker", // unknown
            ))
        }
        assertTrue(ex.message!!.contains("notInSchema"))
    }

    // --- DataType.matches() edge cases ---

    @Test
    fun `LONG does not accept Int`() {
        val model = CollectionModel("item", mapOf("count" to DataType.LONG))
        val localDb = object : PolybaseDatabase {
            override val collections = mutableMapOf("item" to model)
            override fun createCollection(m: CollectionModel) {}
            override fun insert(n: String, d: Map<String, Any>) = mockk<UUID>()
        }
        assertThrows<SchemaValidationException> {
            localDb.validate("item", mapOf("count" to 5)) // Int, not Long
        }
    }

    @Test
    fun `LONG accepts Long`() {
        val model = CollectionModel("item", mapOf("count" to DataType.LONG))
        val localDb = object : PolybaseDatabase {
            override val collections = mutableMapOf("item" to model)
            override fun createCollection(m: CollectionModel) {}
            override fun insert(n: String, d: Map<String, Any>) = mockk<UUID>()
        }
        assertDoesNotThrow {
            localDb.validate("item", mapOf("count" to 5L)) // correct
        }
    }
}