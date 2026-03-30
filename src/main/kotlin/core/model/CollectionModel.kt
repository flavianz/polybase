package ch.flavianz.core.model

data class CollectionModel(val name: String,
                      val properties: Map<String, DataType> = emptyMap(),
                      val particles: List<ParticleModel> = emptyList(),
                      val atoms: List<AtomModel> = emptyList(),
                      val connections: List<CollectionModel> = emptyList())