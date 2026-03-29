package ch.flavianz.core.model

class CollectionModel(val name: String) {
    val properties = emptyMap<String, DataType>()
    val particles = emptyList<ParticleModel>()
    val atoms = emptyList<AtomModel>()
    val connections = emptyList<CollectionModel>()
}