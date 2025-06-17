@file:Suppress("unused")

package mdk.gsm.builder

import mdk.gsm.graph.Edge
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.VertexContainer
import mdk.gsm.state.BeforeVisitHandler
import mdk.gsm.state.ITransitionGuardState

@GsmBuilderScope
class VertexBuilderScope<V, I, F, A> internal constructor(
    private val vertexContainerBuilder: VertexBuilder<V, I, F, A>
) where V : IVertex<I>, F : ITransitionGuardState {

    /**
     * Adds an outgoing edge from the current vertex to a destination vertex.
     * If [autoOrder] is `true` (default), the [Edge.order] value is automatically assigned based on the existing
     * outgoing edge count. If false, then it must be set manually.
     *
     * @param V The type of the vertices (states). Must implement [IVertex].
     * @param I The type of the vertex identifiers.
     * @param F Must implement [ITransitionGuardState].
     * @param autoOrder Automatically assign the edge's order. Defaults to `true`.
     * @param edgeBuilderScope Lambda receiving an [EdgeBuilderScope] to configure the edge.
     */
    fun addEdge(
        autoOrder : Boolean = true,
        edgeBuilderScope : EdgeBuilderScope<V, I, F, A>.() -> Unit
    ) {
        val edgeBuilder = EdgeBuilder<V, I, F, A>(vertexContainerBuilder.stepInstance)

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
        edgeBuilderScope: EdgeBuilderScope<V, I, F, A>.() -> Unit
    ) = addEdge(autoOrder, edgeBuilderScope)

    /**
     * Define the [BeforeVisitHandler] which is invoked just before this vertex becomes published state and can be used to define intermediate states.
     *
     * @see [BeforeVisitHandler]
     * @see [mdk.gsm.state.BeforeVisitScope]
     */
    fun onBeforeVisit(handler: BeforeVisitHandler<V, I, F, A>) {
        vertexContainerBuilder.beforeVisitHandler = handler
    }
}

internal class VertexBuilder<V, I, F, A>(
    internal val stepInstance: V
) where V : IVertex<I>, F : ITransitionGuardState {

    private val adjacent = HashMap<I, Edge<V, I, F, A>>()
    var beforeVisitHandler: BeforeVisitHandler<V, I, F, A>? = null

    val numberOfEdges: Int
        get() = adjacent.size

    fun addOutgoingEdge(edge: Edge<V, I, F, A>) {
        adjacent[edge.to] = edge
    }

    fun build(): VertexContainer<V, I, F, A> {
        val sortedEdges : List<Edge<V, I, F, A>> = adjacent.values.sortedBy {
            it.order
        }

        return VertexContainer(
            vertex = stepInstance,
            adjacentOrdered = sortedEdges,
            beforeVisitHandler = beforeVisitHandler
        )
    }
}