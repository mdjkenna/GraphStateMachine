package mdk.gsm.state.traverser

import mdk.gsm.graph.IVertex
import mdk.gsm.state.TraversalBounds

/**
 * Represents the current state of a graph traversal in the graph state machine.
 * 
 * This class encapsulates the current position within the graph traversal, including the current vertex,
 * boundary status, and any arguments passed with the action that caused this state to be published.
 * 
 * The traversal state can be used to determine whether the traversal has reached a boundary condition
 * (such as the beginning or end of a directed acyclic graph), which is particularly useful for
 * workflow-like applications where there is a definite start and end.
 *
 * @param V The vertex type, which must implement [IVertex] with identifier type [I]
 * @param I The type of the vertex identifier
 * @param A The type of arguments that can be passed during traversal actions
 * 
 * @property vertex The current vertex in the traversal
 * @property traversalBounds Indicates whether the traversal has reached a boundary condition
 * @property args Optional arguments associated with the current traversal state, typically provided during a [mdk.gsm.state.GraphStateMachineAction.NextArgs] action
 * 
 * @see mdk.gsm.state.TraversalBounds
 * @see mdk.gsm.state.GsmController
 * @see mdk.gsm.state.GraphStateMachineAction
 */
data class TraversalState<out V, I, out A>(
    val vertex : V,
    val traversalBounds: TraversalBounds = TraversalBounds.WithinBounds,
    val args : A?
) where V : IVertex<I> {

    /**
     * Indicates whether the traversal is within the normal bounds of the graph.
     * Returns true when [traversalBounds] is [TraversalBounds.WithinBounds].
     */
    val isWithinBounds : Boolean get() = traversalBounds == TraversalBounds.WithinBounds

    /**
     * Indicates whether the traversal has attempted to go before the first vertex.
     * Returns true when [traversalBounds] is [TraversalBounds.BeforeFirst].
     */
    val isBeforeFirst : Boolean get() = traversalBounds == TraversalBounds.BeforeFirst

    /**
     * Indicates whether the traversal has attempted to go beyond the last vertex.
     * Returns true when [traversalBounds] is [TraversalBounds.BeyondLast].
     */
    val isBeyondLast : Boolean get() = traversalBounds == TraversalBounds.BeyondLast

    /**
     * Convenience property that returns true when not before the first vertex.
     * Equivalent to `!isBeforeFirst`.
     */
    val isNotBeforeFirst : Boolean get() = !isBeforeFirst

    /**
     * Convenience property that returns true when not beyond the last vertex.
     * Equivalent to `!isBeyondLast`.
     */
    val isNotBeyondLast : Boolean get() = !isBeyondLast
}

data class WalkState<out V, I, out A> (
    val vertex : V,
    val walkBounds: WalkBounds,
    val args : A?
)


enum class WalkBounds {
    Within,
    Beyond,
}
