package ch.flavianz.adapters

import ch.flavianz.core.model.CollectionModel

interface PolybaseDatabase {
    fun createCollection(collectionModel: CollectionModel)
}