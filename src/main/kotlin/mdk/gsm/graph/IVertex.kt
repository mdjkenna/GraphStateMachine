package mdk.gsm.graph

/**
 * Represents a vertex in a graph. Implement this interface to make a class usable as a vertex.
 *
 * @param I The type of the vertex ID, which should be completely immutable. The [id] must be unique within the graph. Common [id] types include `String`, `Int`, `Long`, etc.
 *            See the provided convenience interfaces like [IStringVertex], [IIntVertex], etc., for common [id] types.
 */
interface IVertex<I> {
    val id : I
}

