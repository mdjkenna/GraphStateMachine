@file:Suppress("unused")

package mdk.gsm.graph

import mdk.gsm.state.ITraversalGuardState

/**
 * Represents a graph data structure used for state machine transitions.  This graph
 * stores vertices and their associated outgoing edges, enabling traversal for state
 * progression. Vertices are identified by unique IDs of type `I`.
 *
 * The `Graph` class facilitates efficient lookups of vertices and their connected
 * edges. Defining all _possible_ state transitions, the [Graph] class forms the main scaffolding for the state machine's navigation logic.
 *
 * The [Graph] class is immutable if the vertex implementations are immutable.
 *
 * @see mdk.gsm.builder.buildGraphOnly
 * @see mdk.gsm.builder.GraphStateMachineBuilderScope.buildGraph
 * @param V The type of vertices stored in this graph. Must implement [IVertex].
 * @param I The type of the vertex ID. Must correspond to the type parameter of [IVertex] implemented by [V].
 * @param F The type of flags used for edge transitions. Must implement [ITraversalGuardState].
 */
class Graph<V, I, F> internal constructor(
    private val map: Map<I, VertexContainer<V, I, F>>
) where V : IVertex<I>, F : ITraversalGuardState {

    fun containsVertex(vertex: V): Boolean {
        return map.containsKey(vertex.id)
    }

    fun containsVertexId(vertexId: I): Boolean {
        return map.containsKey(vertexId)
    }

    fun getVertex(id: I): V? {
        return map[id]?.vertex
    }

    fun getOutgoingEdgesSorted(vertex: V): List<Edge<V, I, F>>? {
        return map[vertex.id]?.adjacentOrdered
    }
}



