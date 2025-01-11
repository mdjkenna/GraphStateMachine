@file:Suppress("unused")

package mdk.gsm.builder

import mdk.gsm.graph.Edge
import mdk.gsm.graph.IVertex
import mdk.gsm.state.IEdgeTransitionFlags

@GsmBuilderScope
class EdgeBuilderScope<V, I, F> internal constructor(
    private val edgeBuilder: EdgeBuilder<V, I, F>
) where V : IVertex<I>, F : IEdgeTransitionFlags {

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
    fun setTo(to: I) {
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
    fun setTransitionHandler(transitionHandler : EdgeTransitionHandler<V, I, F>) {
        edgeBuilder.transitionHandler = transitionHandler
    }
}

internal class EdgeBuilder<V, I, F>(
    private val from : V
) where V : IVertex<I>, F : IEdgeTransitionFlags {
    var order = 0
    var to : I? = null
    var transitionHandler : EdgeTransitionHandler<V, I, F>? = null

    fun build() : Edge<V, I, F> {

        val localTo = to
        check(localTo != null)

        return Edge<V, I, F>(
            order = order,
            from = from,
            to = localTo,
            progressionHandler = transitionHandler
        )
    }
}

typealias EdgeTransitionHandler<V, I, F> = TransitionScope<V, I, F>.() -> Boolean

@GsmBuilderScope
class TransitionScope<V, I, F>(
    val from : V,
    val flags : F
) where V : IVertex<I>, F : IEdgeTransitionFlags
