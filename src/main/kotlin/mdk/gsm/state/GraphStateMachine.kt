@file:Suppress("unused")

package mdk.gsm.state

import mdk.gsm.graph.IVertex
import mdk.gsm.graph.traversal.EdgeTraversalType
import mdk.gsm.graph.traversal.GraphTraversal

/**
 * A class representing a graph-based state machine.
 * Possible transitions between states are modelled as relationships in a directed graph.
 *
 * The use of a directed graph allows structural limitations to be placed on which state transitions are possible using directed edges when the graph is created.
 * Transitions are also controlled via flags: [IEdgeTransitionFlags] that determine the eligibility of a transition (or edge traversal).
 *
 * Using [IEdgeTransitionFlags] the possible transitions are reduced to a subset depending on runtime conditions of the state machine.
 *
 * @property flags The edge progression flags which are passed to each edge to determine if traversal can occur.
 */
class GraphStateMachine<V, F> internal constructor(
    private val graphTraversal: GraphTraversal<V, F>,
    val flags: F,
    internal val edgeTraversalType: EdgeTraversalType
) where V : IVertex, F : IEdgeTransitionFlags {

    var currentState = TraversalState<V>(graphTraversal.currentStep())
        private set

    private var onStateUpdatedListener : ((TraversalState<V>) -> Unit)? = null

    /**
     * Sets a listener for state updates as a result of a dispatched action
     */
    fun setOnStateUpdatedListener(
        readCurrent: Boolean = true,
        listener: (TraversalState<V>) -> Unit
    ) {
        onStateUpdatedListener = listener

        if (readCurrent) {
            listener(currentState)
        }
    }

    /**
     * Clears the listener set by [setOnStateUpdatedListener]
     */
    fun clearOnStateUpdatedListener() {
        onStateUpdatedListener = null
    }

    /**
     * Traces the path of the traversal from the start vertex to the current vertex.
     *
     * @return A list of vertices representing the path from the start vertex to the current vertex.
     */
    fun tracePath(): List<V> {
        return graphTraversal.tracePath()
    }

    /**
     * Dispatches an action to the state machine.
     * This will update the state machines current state and cause the state update listener to be called with the result of the action, if set.
     *
     * @param graphStateMachineAction The action to dispatch.
     */
    fun dispatch(graphStateMachineAction: GraphStateMachineAction) {

        when (graphStateMachineAction) {
            GraphStateMachineAction.Next -> {
                val next = graphTraversal.next(flags)

                updateProgress { current ->
                    if (next != null) {
                        TraversalState(next)
                    } else {
                        current.copy(traversalBounds = TraversalBounds.BeyondLast)
                    }
                }
            }

            GraphStateMachineAction.Previous -> {
                val previousStep = graphTraversal.movePrevious()

                updateProgress { current ->
                    if (previousStep != null) {
                        TraversalState(previousStep)
                    } else {
                        current.copy(traversalBounds = TraversalBounds.BeforeFirst)
                    }
                }
            }

            GraphStateMachineAction.Reset -> {
                updateProgress {
                    TraversalState(graphTraversal.reset())
                }
            }
        }
    }

    private inline fun updateProgress(
        crossinline update : (current : TraversalState<V>) -> TraversalState<V>
    ) {
        val newProgress = update(currentState)
        currentState = newProgress
        onStateUpdatedListener?.invoke(newProgress)
    }
}

