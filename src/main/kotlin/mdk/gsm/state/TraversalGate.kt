package mdk.gsm.state

import mdk.gsm.builder.GsmBuilderScope
import mdk.gsm.graph.IVertex

/**
 * Represents a function that acts as a guard for edge exploration i.e. state transitions, within a [mdk.gsm.state.GraphStateMachine].
 *
 * A [TraversalGuard] function is associated with an [mdk.gsm.graph.Edge] and is evaluated at runtime to determine whether the transition
 * along that edge is allowed as part of the edge exploration that causes state transitions.
 *
 * @return `true` to allow the transition, and `false` to prevent it
 *
 * @param <V> The type of the vertices (states). Must implement [IVertex].
 * @param <I> The type of the vertex identifiers.
 * @param <F> The type of Traversal Guard State. Must implement [ITraversalGuardState].
 */
typealias TraversalGuard<V, I, F> =
        TraversalGuardScope<V, I, F>.() -> Boolean

/**
 * Provides data used by a [TraversalGuard] function to determine whether a state transition is permitted.
 * This scope provides access to data associated with an attempted edge traversal.
 *
 * @property from The vertex the current edge is outgoing from
 * @property guardState The traversal guard state for the state machine. Note this is a single instance for the entire state machine. It can be used to communicate across edges to dynamically control state transitions.
 *
 * @param <V> The type of the vertices (states). Must implement [IVertex].
 * @param <I> The type of the vertex identifiers.
 * @param <F> The type of Traversal Guard State. Must implement [ITraversalGuardState].
 */
@GsmBuilderScope
class TraversalGuardScope<V, I, F>(
    val from : V,
    val guardState : F
) where V : IVertex<I>, F : ITraversalGuardState