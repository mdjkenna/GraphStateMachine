@file:Suppress("unused")

package mdk.gsm.builder

import mdk.gsm.graph.Edge
import mdk.gsm.graph.IVertex
import mdk.gsm.state.IEdgeTransitionFlags

@GsmBuilderScope
class EdgeBuilderScope<V, F> internal constructor(
    private val edgeBuilder: EdgeBuilder<V, F>
) where V : IVertex, F : IEdgeTransitionFlags {

    /**
     * Sets the order in which the edge is checked for traversal.
     * @param order The order of the edge.
     */
    fun setOrder(order: Int) {
        edgeBuilder.order = order
    }

    /**
     * Sets the target vertex of the edge.
     * @param to The step id of the target vertex of the edge.
     */
    fun setTo(to: String) {
        edgeBuilder.to = to
    }

    /**
     * Sets the target vertex of the edge.
     * @param to The step instance of the target vertex of the edge.
     */
    fun setTo(to : V) {
        edgeBuilder.to = to.id
    }

    /**
     * Sets the progression handler for the edge.
     * @param transitionHandler The progression handler for the edge.
     */
    fun setTransitionHandler(transitionHandler : EdgeTransitionHandler<V, F>) {
        edgeBuilder.transitionHandler = transitionHandler
    }
}

internal class EdgeBuilder<V, F>(
    private val from : V
) where V : IVertex, F : IEdgeTransitionFlags {
    var order = 0
    var to : String = ""
    var transitionHandler : EdgeTransitionHandler<V, F>? = null

    fun build() : Edge<V, F> {
        return Edge<V, F>(
            order = order,
            from = from,
            to = to,
            progressionHandler = transitionHandler
        )
    }
}

typealias EdgeTransitionHandler<V, F> = TransitionScope<V, F>.() -> Boolean

@GsmBuilderScope
class TransitionScope<V, F>(
    val from : V,
    val flags : F
) where V : IVertex, F : IEdgeTransitionFlags
