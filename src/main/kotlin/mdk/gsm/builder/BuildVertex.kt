package mdk.gsm.builder

import mdk.gsm.graph.Edge
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.VertexContainer
import mdk.gsm.state.IEdgeTransitionFlags

@GsmBuilderScope
class VertexBuilderScope<V, I, F> internal constructor(
    private val vertexContainerBuilder: VertexBuilder<V, I, F>
) where V : IVertex<I>, F : IEdgeTransitionFlags {

    /**
     * Adds an outgoing edge to the vertex.
     * @param autoOrder Automatically set the order of the edge to the current number of edges.
     * @param scopeConsumer Used to configure the edge.
     */
    fun addOutgoingEdge(
        autoOrder : Boolean = true,
        scopeConsumer : EdgeBuilderScope<V, I, F>.() -> Unit
    ) {
        val edgeBuilder = EdgeBuilder<V, I, F>(vertexContainerBuilder.stepInstance)

        scopeConsumer(EdgeBuilderScope(edgeBuilder))
        if (autoOrder) {
            edgeBuilder.order = vertexContainerBuilder.numberOfEdges
        }

        vertexContainerBuilder.addOutgoingEdge(
            edgeBuilder.build()
        )
    }
}

internal class VertexBuilder<V, I, F>(
    internal val stepInstance: V
) where V : IVertex<I>, F : IEdgeTransitionFlags {

    private val adjacent = HashMap<I, Edge<V, I, F>>()

    val numberOfEdges: Int
        get() = adjacent.size

    fun addOutgoingEdge(edge: Edge<V, I, F>) {
        adjacent[edge.to] = edge
    }

    fun build(): VertexContainer<V, I, F> {
        val sortedEdges : List<Edge<V, I, F>> = adjacent.values.sortedBy {
            it.order
        }

        return VertexContainer(
            vertex = stepInstance,
            adjacentOrdered = sortedEdges
        )
    }
}