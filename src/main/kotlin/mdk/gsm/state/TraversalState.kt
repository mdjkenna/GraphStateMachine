package mdk.gsm.state

import mdk.gsm.graph.IVertex

/**
 * This class essentially holds the current state of the graph state machine for callers.
 * It represents the current status of the [GraphStateMachine] (which always has a non-null state)
 * This class can determine whether the progress has gone as far as it can - or the traversal is 'finished' - having met a dead end
 *
 * @property value The current step in the traversal
 * @property hasMore Whether there are more steps to traverse.
 * Calling next while at a terminal vertex will cause this to be false.
 * @property hasPrevious Whether there are previous steps to traverse.
 * Attempting to move before the first step will cause this to be false.
 */
data class TraversalState<out V>(
    val value : V,
    val hasMore: Boolean = true,
    val hasPrevious : Boolean = true
) where V : IVertex