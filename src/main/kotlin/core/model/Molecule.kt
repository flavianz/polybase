package ch.flavianz.core.model

class Molecule {
    val properties = emptyMap<String, DataType>()
    val particles = emptyList<Particle>()
    val atoms = emptyList<Atom>()
    val connections = emptyList<Molecule>()
}