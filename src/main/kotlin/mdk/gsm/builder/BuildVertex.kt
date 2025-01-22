@file:Suppress("unused")

package mdk.gsm.builder

import mdk.gsm.graph.Edge
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.VertexContainer
import mdk.gsm.state.ITraversalGuardState

@GsmBuilderScope
class VertexBuilderScope<V, I, F> internal constructor(
    private val vertexContainerBuilder: VertexBuilder<V, I, F>
) where V : IVertex<I>, F : ITraversalGuardState {

    /**
     * Adds an outgoing edge from the current vertex to a destination vertex. The edge is configured
     * using the provided [edgeBuilderScope] lambda.
     *
     * If [autoOrder] is `true` (default), the [Edge.order] value is automatically assigned based on the existing
     * outgoing edge count. If false then it must be set manually.
     *
     * @param V The type of the vertices (states). Must implement [IVertex].
     * @param I The type of the vertex identifiers.
     * @param F Must implement [ITraversalGuardState].
     * @param autoOrder Automatically assign the edge's order. Defaults to `true`.
     * @param edgeBuilderScope Lambda receiving an [EdgeBuilderScope] to configure the edge.
     */
    fun addEdge(
        autoOrder : Boolean = true,
        edgeBuilderScope : EdgeBuilderScope<V, I, F>.() -> Unit
    ) {
        val edgeBuilder = EdgeBuilder<V, I, F>(vertexContainerBuilder.stepInstance)

        edgeBuilderScope(EdgeBuilderScope(edgeBuilder))
        if (autoOrder) {
            edgeBuilder.order = vertexContainerBuilder.numberOfEdges
        }

        vertexContainerBuilder.addOutgoingEdge(
            edgeBuilder.build()
        )
    }

    /**
     * Shorthand for [addEdge]
     *
     * @see addEdge
     */
    fun e(
        autoOrder: Boolean = true,
        edgeBuilderScope: EdgeBuilderScope<V, I, F>.() -> Unit
    ) = addEdge(autoOrder, edgeBuilderScope)
}

internal class VertexBuilder<V, I, F>(
    internal val stepInstance: V
) where V : IVertex<I>, F : ITraversalGuardState {

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