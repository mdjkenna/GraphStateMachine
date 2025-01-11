package mdk.gsm.graph

import mdk.gsm.builder.EdgeTransitionHandler
import mdk.gsm.builder.TransitionScope
import mdk.gsm.state.IEdgeTransitionFlags

/**
 * Represents an edge in a graph.
 *
 * @property order The order of the edge.
 * @property from The vertex the edge is coming from.
 * @property to The id of the vertex the edge is going to.
 */
class Edge<out V, I, F> internal constructor(
    val order: Int,
    val from: V,
    val to: I,
    private val progressionHandler: EdgeTransitionHandler<V, I, F>?
) where F : IEdgeTransitionFlags, V : IVertex<I> {

    internal fun canProceed(flags: F): Boolean {
        val ph = progressionHandler
        if (ph == null) return true

        val scope = TransitionScope(from, flags)
        return ph.invoke(scope)
    }
}