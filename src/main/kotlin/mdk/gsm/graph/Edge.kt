package mdk.gsm.graph

import mdk.gsm.state.ITraversalGuardState
import mdk.gsm.state.TraversalGuard
import mdk.gsm.state.TraversalGuardScope

/**
 * Represents a directed edge within the graph of a [mdk.gsm.state.GraphStateMachine], connecting a source vertex [from] to a destination vertex [to].
 * Edges define the possible transitions between states in the state machine.
 *
 * Edges are defined as outgoing from a vertex, and outgoing edges are traversed according to their [order] value,
 * used to prioritize transitions when multiple edges originate from the same vertex.
 * Lower [order] values are prioritized.
 *
 * The [traversalGuard] acts as a dynamic runtime condition. If the [TraversalGuard] function returns `true`, the transition is allowed;
 * otherwise, it's blocked. If no [traversalGuard] has been set then traversal is never blocked.
 *
 * @param V The type of the vertices (states). Must implement [IVertex].
 * @param I The type of the vertex identifiers.
 * @param F The type of the edge transition flags. Must implement [ITraversalGuardState].
 * @param order The priority of this edge during traversal. Lower values are evaluated first.
 * @param from The source vertex of this edge.
 * @param to The identifier of the destination vertex.
 * @param traversalGuard An optional function that controls whether this edge can be traversed at runtime.
 */
class Edge<out V, I, F> internal constructor(
    val order: Int,
    val from: V,
    val to: I,
    private val traversalGuard: TraversalGuard<V, I, F>?
) where F : ITraversalGuardState, V : IVertex<I> {

    internal fun canProceed(flags: F): Boolean {
        return if (traversalGuard == null) {
            true
        } else {
            val scope = TraversalGuardScope(from, flags)
            traversalGuard.invoke(scope)
        }
    }
}