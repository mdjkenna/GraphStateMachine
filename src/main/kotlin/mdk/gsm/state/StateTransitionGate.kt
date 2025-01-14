package mdk.gsm.state

import mdk.gsm.builder.GsmBuilderScope
import mdk.gsm.graph.IVertex

/**
 * Represents a function that acts as a gate for edge exploration i.e. state transitions, within a [mdk.gsm.state.GraphStateMachine].
 *
 * A [TraversalGate] function is associated with an [mdk.gsm.graph.Edge] and is evaluated at runtime to determine whether the transition
 * along that edge is allowed as part of the edge exploration that causes state transitions.
 *
 * The gate should return `true` to allow the transition, and `false` to prevent it.
 *
 * @param <V> The type of the vertices (states). Must implement [IVertex].
 * @param <I> The type of the vertex identifiers.
 * @param <F> The type of the edge transition flags. Must implement [IEdgeTransitionFlags].
 */
typealias TraversalGate<V, I, F> =
        TraversalGateScope<V, I, F>.() -> Boolean

/**
 * Provides data used by a [TraversalGate] function to determine whether a state transition is permitted.
 * This scope provides access to data associated with an attempted edge traversal.
 *
 * @param <V> The type of the vertices (states). Must implement [IVertex].
 * @param <I> The type of the vertex identifiers.
 * @param <F> The type of the edge transition flags. Must implement [IEdgeTransitionFlags].
 */
@GsmBuilderScope
class TraversalGateScope<V, I, F>(
    val from : V,
    val flags : F
) where V : IVertex<I>, F : IEdgeTransitionFlags