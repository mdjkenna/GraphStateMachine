@file:Suppress("unused")

package mdk.gsm.builder

import mdk.gsm.graph.Edge
import mdk.gsm.graph.IVertex
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.state.TransitionGuard

@GsmBuilderScope
class EdgeBuilderScope<V, I, F, A> internal constructor(
    private val edgeBuilder: EdgeBuilder<V, I, F, A>
) where V : IVertex<I>, F : ITransitionGuardState {

    /**
     * Sets the order in which the edge is traversed based on being sorted ascending.
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
     * Sets the [TransitionGuard] for this edge.
     * @see TransitionGuard
     *
     * @param transitionGuard The function controlling transition across this edge. Receives a
     *   [mdk.gsm.state.TransitionGuardScope] and returns `true` to allow the transition, `false` to prevent it.
     */
    fun setEdgeTransitionGuard(transitionGuard : TransitionGuard<V, I, F, A>) {
        edgeBuilder.transitionGuard = transitionGuard
    }
}

internal class EdgeBuilder<V, I, F, A>(
    private val from : V
) where V : IVertex<I>, F : ITransitionGuardState {
    var order = 0
    var to : I? = null
    var transitionGuard : TransitionGuard<V, I, F, A>? = null

    fun build() : Edge<V, I, F, A> {

        val localTo = to
        check(localTo != null)

        return Edge<V, I, F, A>(
            order = order,
            from = from,
            to = localTo,
            transitionGuard = transitionGuard
        )
    }
}
