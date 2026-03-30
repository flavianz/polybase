// ch/flavianz/adapters/PolybaseDatabase.kt
package ch.flavianz.adapters

import ch.flavianz.core.model.CollectionModel
import ch.flavianz.core.model.DataType
import ch.flavianz.core.exception.SchemaValidationException
import java.util.UUID

interface PolybaseDatabase {
    val collections: Map<String, CollectionModel>

    fun createCollection(collectionModel: CollectionModel)
    fun insert(collectionName: String, data: Map<String, Any>): UUID

    /**
     * Validates [data] against the registered schema for [collectionName].
     * Throws [SchemaValidationException] if:
     *  - the collection was never registered
     *  - a required property is missing
     *  - a value's runtime type doesn't match the declared DataType
     *  - an unknown key is present
     */
    fun validate(collectionName: String, data: Map<String, Any>) {
        val model = collections[collectionName]
            ?: throw SchemaValidationException("Unknown collection: '$collectionName'")

        // Reject unknown keys
        val unknownKeys = data.keys - model.properties.keys
        if (unknownKeys.isNotEmpty())
            throw SchemaValidationException("Unknown properties in '$collectionName': $unknownKeys")

        // Check every declared property
        for ((key, dataType) in model.properties) {
            val value = data[key]
                ?: throw SchemaValidationException("Missing required property '$key' in '$collectionName'")

            if (!dataType.matches(value))
                throw SchemaValidationException(
                    "Type mismatch for '$key' in '$collectionName': " +
                            "expected $dataType but got ${value::class.simpleName}"
                )
        }
    }
}