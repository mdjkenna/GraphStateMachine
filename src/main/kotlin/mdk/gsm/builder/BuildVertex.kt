package mdk.gsm.builder

import mdk.gsm.graph.*
import mdk.gsm.state.IEdgeTransitionFlags

@GsmBuilderScope
class VertexBuilderScope<V, F> internal constructor(
    private val vertexContainerBuilder: VertexBuilder<V, F>
) where V : IVertex, F : IEdgeTransitionFlags {

    /**
     * Adds an outgoing edge to the vertex.
     * @param autoOrder Automatically set the order of the edge to the current number of edges.
     * @param scopeConsumer Used to configure the edge.
     */
    fun addOutgoingEdge(
        autoOrder : Boolean = true,
        scopeConsumer : EdgeBuilderScope<V, F>.() -> Unit
    ) {
        val edgeBuilder = EdgeBuilder<V, F>(vertexContainerBuilder.stepInstance)
        if (autoOrder) {
            edgeBuilder.order = vertexContainerBuilder.numberOfEdges
        }

        scopeConsumer(EdgeBuilderScope(edgeBuilder))
        vertexContainerBuilder.addOutgoingEdge(
            edgeBuilder.build()
        )
    }
}

internal class VertexBuilder<V, F>(
    internal val stepInstance: V
) where V : IVertex, F : IEdgeTransitionFlags {

    private val adjacent = HashMap<String, Edge<V, F>>()

    val numberOfEdges: Int
        get() = adjacent.size

    fun addOutgoingEdge(edge: Edge<V, F>) {
        adjacent[edge.to] = edge
    }

    fun build(): VertexContainer<V, F> {
        val sortedEdges : List<Edge<V, F>> = adjacent.values.sortedBy {
            it.order
        }

        return VertexContainer(
            vertex = stepInstance,
            adjacentOrdered = sortedEdges
        )
    }
}