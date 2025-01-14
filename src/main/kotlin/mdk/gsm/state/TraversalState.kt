package mdk.gsm.state

import mdk.gsm.graph.IVertex

/**
 * This class essentially holds the current state of the graph state machine for callers.
 * It represents the current status of the [GraphStateMachine] (which always has a non-null state)
 * This class can determine whether the progress has gone as far as it can - or the traversal is 'finished' - having met a dead end
 *
 * @property vertex The current step in the traversal
 * @property traversalBounds A status which gives visibility on whether the graph has reached a dead end in terms of moving next or previous
 */
data class TraversalState<out V, I>(
    val vertex : V,
    val traversalBounds: TraversalBounds = TraversalBounds.WithinBounds
) where V : IVertex<I> {

    val isWithinBounds : Boolean get() = traversalBounds == TraversalBounds.WithinBounds

    val isBeforeFirst : Boolean get() = traversalBounds == TraversalBounds.BeforeFirst

    val isBeyondLast : Boolean get() = traversalBounds == TraversalBounds.BeyondLast

    val isNotBeforeFirst : Boolean get() = !isBeforeFirst

    val isNotBeyondLast : Boolean get() = !isBeyondLast
}

