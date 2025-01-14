package mdk.gsm.graph

import mdk.gsm.state.IEdgeTransitionFlags
import mdk.gsm.state.TraversalGate
import mdk.gsm.state.TraversalGateScope

/**
 * Represents a directed edge within the graph of a [mdk.gsm.state.GraphStateMachine], connecting a source addVertex [from] to a destination addVertex [to].
 * Edges define the possible transitions between states in the state machine.
 *
 * Edges are defined as outgoing from a addVertex, and outgoing edges are traversed according to their [order] value,
 * used to prioritize transitions when multiple edges originate from the same addVertex.
 * Lower [order] values are prioritized.
 *
 * The [traversalGate] acts as a dynamic runtime condition. If the [TraversalGate] function returns `true`, the transition is allowed;
 * otherwise, it's blocked. If no [traversalGate] has been set then traversal is never blocked.
 *
 * @param V The type of the vertices (states). Must implement [IVertex].
 * @param I The type of the addVertex identifiers.
 * @param F The type of the edge transition flags. Must implement [IEdgeTransitionFlags].
 * @param order The priority of this edge during traversal. Lower values are evaluated first.
 * @param from The source addVertex of this edge.
 * @param to The identifier of the destination addVertex.
 * @param traversalGate An optional function that controls whether this edge can be traversed at runtime.
 */
class Edge<out V, I, F> internal constructor(
    val order: Int,
    val from: V,
    val to: I,
    private val traversalGate: TraversalGate<V, I, F>?
) where F : IEdgeTransitionFlags, V : IVertex<I> {

    internal fun canProceed(flags: F): Boolean {
        return if (traversalGate == null) {
            true
        } else {
            val scope = TraversalGateScope(from, flags)
            traversalGate.invoke(scope)
        }
    }
}