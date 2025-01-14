@file:Suppress("unused")

package mdk.gsm.builder

import mdk.gsm.graph.Edge
import mdk.gsm.graph.IVertex
import mdk.gsm.state.IEdgeTransitionFlags
import mdk.gsm.state.TraversalGate

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
     * Sets the target addVertex of the edge.
     * @param to The step id of the target addVertex of the edge.
     */
    fun setTo(to: I) {
        edgeBuilder.to = to
    }

    /**
     * Sets the target addVertex of the edge.
     * @param to The step instance of the target addVertex of the edge.
     */
    fun setTo(to : V) {
        edgeBuilder.to = to.id
    }

    /**
     * Sets the [mdk.gsm.state.TraversalGate] for this edge, controlling
     * whether the state machine can traverse this edge at runtime.
     *
     * The [mdk.gsm.state.TraversalGate] is a lambda with receiver, receiving a [mdk.gsm.state.TraversalGateScope] as its receiver.
     * It should return `true` if the transition is allowed, and `false` otherwise.  This allows you to
     * implement complex conditional state transition logic.
     *
     * @param traversalGate The function controlling transition across this edge. Receives a
     *   [mdk.gsm.state.TraversalGateScope] and returns `true` to allow the transition, `false` to prevent it.
     */
    fun setStateTransitionGate(traversalGate : TraversalGate<V, I, F>) {
        edgeBuilder.traversalGate = traversalGate
    }
}

internal class EdgeBuilder<V, I, F>(
    private val from : V
) where V : IVertex<I>, F : IEdgeTransitionFlags {
    var order = 0
    var to : I? = null
    var traversalGate : TraversalGate<V, I, F>? = null

    fun build() : Edge<V, I, F> {

        val localTo = to
        check(localTo != null)

        return Edge<V, I, F>(
            order = order,
            from = from,
            to = localTo,
            traversalGate = traversalGate
        )
    }
}

