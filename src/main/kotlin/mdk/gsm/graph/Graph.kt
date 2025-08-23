@file:Suppress("unused")

package mdk.gsm.graph

import mdk.gsm.state.ITransitionGuardState

/**
 * Represents a graph data structure used for state machine transitions.  This graph
 * stores vertices and their associated outgoing edges, enabling traversal for state
 * progression. Vertices are identified by unique IDs of type `I`.
 *
 * The `Graph` class facilitates efficient lookups of vertices and their connected
 * edges. Defining all _possible_ state transitions, the [Graph] class forms the main scaffolding for the state machine's traversal logic.
 *
 * The [Graph] class is immutable if the vertex implementations are immutable.
 *
 * @param V The type of vertices stored in this graph. Must implement [IVertex].
 * @param I The type of the vertex ID. Must correspond to the type parameter of [IVertex] implemented by [V].
 * @param F The type of edge traversal guard used for edge transitions. Must implement [ITransitionGuardState].
 * @param A The type of action argument
 */
class Graph<V, I, F, A> internal constructor(
    private val map: Map<I, VertexContainer<V, I, F, A>>
) where V : IVertex<I>, F : ITransitionGuardState {

    fun containsVertex(vertex: V): Boolean {
        return map.containsKey(vertex.id)
    }

    fun containsVertexId(vertexId: I): Boolean {
        return map.containsKey(vertexId)
    }

    fun getVertex(id: I): V? {
        return map[id]?.vertex
    }

    fun getOutgoingEdgesSorted(vertex: V): List<Edge<V, I, F, A>>? {
        return map[vertex.id]?.adjacentOrdered
    }

    internal fun getVertexContainer(id: I): VertexContainer<V, I, F, A>? {
        return map[id]
    }


    internal fun getAllVertices(): List<V> {
        return map.values.map { it.vertex }
    }

    internal fun getAllEdges(): List<Edge<V, I, F, A>> {
        return map.values.flatMap { it.adjacentOrdered }
    }
}
