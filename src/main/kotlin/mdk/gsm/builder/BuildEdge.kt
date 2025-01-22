@file:Suppress("unused")

package mdk.gsm.builder

import mdk.gsm.graph.Edge
import mdk.gsm.graph.IVertex
import mdk.gsm.state.ITraversalGuardState
import mdk.gsm.state.TraversalGuard

@GsmBuilderScope
class EdgeBuilderScope<V, I, F> internal constructor(
    private val edgeBuilder: EdgeBuilder<V, I, F>
) where V : IVertex<I>, F : ITraversalGuardState {

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
     * Sets the [mdk.gsm.state.TraversalGuard] for this edge, controlling
     * whether the state machine can traverse this edge at runtime.
     *
     * The gate returns true for open and false for closed.
     *
     * Note the same gate can be evaluated multiple times, including as part of DFS backtracking after reaching a dead end.
     *
     * The [mdk.gsm.state.TraversalGuard] is a lambda with receiver, receiving a [mdk.gsm.state.TraversalGuardScope] as its receiver.
     * It should return `true` if the transition is allowed, and `false` otherwise.  This allows you to
     * implement complex conditional state transition logic.
     *
     * @param traversalGuard The function controlling transition across this edge. Receives a
     *   [mdk.gsm.state.TraversalGuardScope] and returns `true` to allow the transition, `false` to prevent it.
     */
    fun setEdgeTraversalGate(traversalGuard : TraversalGuard<V, I, F>) {
        edgeBuilder.traversalGuard = traversalGuard
    }
}

internal class EdgeBuilder<V, I, F>(
    private val from : V
) where V : IVertex<I>, F : ITraversalGuardState {
    var order = 0
    var to : I? = null
    var traversalGuard : TraversalGuard<V, I, F>? = null

    fun build() : Edge<V, I, F> {

        val localTo = to
        check(localTo != null)

        return Edge<V, I, F>(
            order = order,
            from = from,
            to = localTo,
            traversalGuard = traversalGuard
        )
    }
}

