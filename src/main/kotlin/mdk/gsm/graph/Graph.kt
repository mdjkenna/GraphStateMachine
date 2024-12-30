@file:Suppress("unused")

package mdk.gsm.graph

import mdk.gsm.state.IEdgeTransitionFlags

class Graph<V,  F> internal constructor(
    private val map: Map<String, VertexContainer<V, F>>
) where V : IVertex, F : IEdgeTransitionFlags{

    /**
     * Check if the graph contains this vertex by id
     */
    fun containsVertex(vertex : V) : Boolean {
        return map.containsKey(vertex.id)
    }

    /**
     * Check if the graph contains this vertex id
     */
    fun containsVertexId(vertexId : String) : Boolean {
        return map.containsKey(vertexId)
    }

    /**
     * Get a vertex by id if present
     */
    fun getVertex(id: String) : V? {
        return map[id]?.vertex
    }

    /**
     * Get the outgoing edges of a vertex sorted by their order property i.e. [Edge.order]
     */
    fun getOutgoingEdgesSorted(workflowStep: IVertex) : List<Edge<V, F>>? {
        return map[workflowStep.id]?.adjacentOrdered
    }
}

