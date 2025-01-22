package mdk.gsm.graph

/**
 * Represents a vertex in the graph of a [mdk.gsm.state.GraphStateMachine].
 * Implement this interface to make a class usable as a vertex.
 * Note the [id] property should have an immutable type and if it is a user defined type, then care should be taken to check if equality and hashcode generation should be value based.
 * Typically, data classes would be recommended for user defined [id] types.
 *
 * @param I The type of the vertex ID, which should be completely immutable. The [id] must be unique within the graph.
 * @property id The unique identifier for the vertex of type [I]
 */
interface IVertex<I> {
    val id : I
}

