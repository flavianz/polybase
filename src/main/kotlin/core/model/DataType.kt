package ch.flavianz.core.model

enum class DataType {
    STRING,
    INT,
    FLOAT,
    BOOL;

    fun toSqlType(): String {
        return when(this) {
            STRING -> "text"
            INT -> "int"
            FLOAT -> "float"
            BOOL -> "bool"
        }
    }
}