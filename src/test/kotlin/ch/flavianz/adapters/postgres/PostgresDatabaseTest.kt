package ch.flavianz.adapters.postgres

import ch.flavianz.core.connection.PostgresConnection
import ch.flavianz.core.exception.SchemaValidationException
import ch.flavianz.core.model.CollectionModel
import ch.flavianz.core.model.DataType
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

class PostgresDatabaseTest {

    private lateinit var db: PostgresDatabase
    private val mockJdbc       = mockk<Connection>(relaxed = true)
    private val mockStatement  = mockk<Statement>(relaxed = true)
    private val mockPrepared   = mockk<PreparedStatement>(relaxed = true)
    private val mockConnection = mockk<PostgresConnection>()

    private val userModel = CollectionModel(
        name = "user",
        properties = mapOf(
            "username" to DataType.STRING,
            "age"      to DataType.INT,
        )
    )

    @BeforeEach
    fun setup() {
        every { mockConnection.jdbcConnection } returns mockJdbc
        every { mockJdbc.createStatement() } returns mockStatement
        every { mockJdbc.prepareStatement(any()) } returns mockPrepared
        every { mockStatement.executeUpdate(any()) } returns 1
        every { mockPrepared.executeUpdate() } returns 1

        db = PostgresDatabase(mockConnection)
    }

    @Test
    fun `createCollection registers model`() {
        db.createCollection(userModel)
        assertTrue(db.collections.containsKey("user"))
    }

    @Test
    fun `createCollection executes CREATE TABLE`() {
        db.createCollection(userModel)
        verify { mockStatement.executeUpdate(match { it.startsWith("CREATE TABLE") }) }
    }

    @Test
    fun `insert returns a UUID`() {
        db.createCollection(userModel)
        val id = db.insert("user", mapOf("username" to "flavian", "age" to 17))
        assertNotNull(id)
    }

    @Test
    fun `insert calls prepareStatement and executeUpdate`() {
        db.createCollection(userModel)
        db.insert("user", mapOf("username" to "flavian", "age" to 17))
        verify { mockJdbc.prepareStatement(match { it.startsWith("INSERT INTO") }) }
        verify { mockPrepared.executeUpdate() }
    }

    @Test
    fun `insert with wrong type throws SchemaValidationException before hitting DB`() {
        db.createCollection(userModel)
        assertThrows<SchemaValidationException> {
            db.insert("user", mapOf("username" to "flavian", "age" to "notAnInt"))
        }
        // prepareStatement should never have been called
        verify(exactly = 0) { mockJdbc.prepareStatement(any()) }
    }

    @Test
    fun `insert into unregistered collection throws SchemaValidationException`() {
        assertThrows<SchemaValidationException> {
            db.insert("ghost", mapOf("username" to "flavian"))
        }
    }
}