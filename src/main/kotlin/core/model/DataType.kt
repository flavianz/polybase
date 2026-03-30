package ch.flavianz.core.model

enum class DataType {
    STRING,
    INT,
    LONG,
    DOUBLE,
    BOOL,
    DATE,
    BYTES;

    fun matches(value: Any): Boolean = when (this) {
        STRING -> value is String
        INT    -> value is Int
        LONG   -> value is Long
        DOUBLE -> value is Double
        BOOL   -> value is Boolean
        DATE   -> value is java.util.Date || value is java.time.LocalDateTime
        BYTES  -> value is ByteArray
    }

    fun toSqlType(): String = when (this) {
        STRING -> "TEXT"
        INT    -> "INTEGER"
        LONG   -> "BIGINT"
        DOUBLE -> "DOUBLE PRECISION"
        BOOL   -> "BOOLEAN"
        DATE   -> "TIMESTAMP"
        BYTES  -> "BYTEA"
    }

    fun toBsonType(): String = when (this) {
        STRING -> "string"
        INT    -> "int"
        LONG   -> "long"
        DOUBLE -> "double"
        BOOL   -> "bool"
        DATE   -> "date"
        BYTES  -> "binData"
    }
}