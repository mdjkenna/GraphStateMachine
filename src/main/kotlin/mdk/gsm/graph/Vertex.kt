package mdk.gsm.graph

/**
 * A convenience class for a vertex in a graph, implementing the [IVertex] interface.
 */
data class Vertex(
    override val id: String,
) : IVertex

