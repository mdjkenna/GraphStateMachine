@file:Suppress("unused")

package mdk.gsm.state

import mdk.gsm.graph.IVertex
import mdk.gsm.graph.traversal.EdgeTraversalType
import mdk.gsm.graph.traversal.IGraphTraversal

/**
 * A class representing a graph-based state machine.
 * Possible transitions between states are modelled as relationships in a directed graph.
 *
 * @property traversalGuardState The edge progression flags which are passed to each edge to determine if traversal can occur.
 */
class GraphStateMachine<out V, I, F> internal constructor(
    private val graphTraversal: IGraphTraversal<V, I, F>,
    private val gsmConfig: GsmConfig,
    val traversalGuardState: F,
    internal val edgeTraversalType: EdgeTraversalType
) where V : IVertex<I>, F : ITraversalGuardState {

    val currentState : TraversalState<V, I> get() = _currentState
    private var _currentState : TraversalState<V, I> = TraversalState<V, I>(graphTraversal.currentStep())

    private var onStateUpdatedListener : ((TraversalState<V, I>) -> Unit)? = null

    /**
     * Sets a listener for state updates as a result of a dispatched action
     */
    fun setOnStateUpdatedListener(
        readCurrent: Boolean = true,
        listener: (TraversalState<V, I>) -> Unit
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
                updateState { current ->
                    if (gsmConfig.explicitlyMoveIntoBounds && current.traversalBounds != TraversalBounds.WithinBounds) {
                        current.copy(traversalBounds = TraversalBounds.WithinBounds)
                    }

                    val next = graphTraversal.moveNext(traversalGuardState)
                    if (next != null) {
                        TraversalState(next)
                    } else {
                        current.copy(traversalBounds = TraversalBounds.BeyondLast)
                    }
                }
            }

            GraphStateMachineAction.Previous -> {

                updateState { current ->
                    val previousStep = graphTraversal.movePrevious()
                    if (previousStep != null) {
                        TraversalState(previousStep)
                    } else {
                        current.copy(traversalBounds = TraversalBounds.BeforeFirst)
                    }
                }
            }

            GraphStateMachineAction.Reset -> {
                updateStateUnconditionally {
                    traversalGuardState.onReset()
                    TraversalState(graphTraversal.reset())
                }
            }
        }
    }

    private inline fun updateStateUnconditionally(
        crossinline update : (current : TraversalState<V, I>) -> TraversalState<V, I>
    ) {

        val newProgress = update(currentState)
        _currentState = newProgress
        onStateUpdatedListener?.invoke(newProgress)
    }

    private inline fun updateState(
        crossinline update : (current : TraversalState<V, I>) -> TraversalState<V, I>
    ) {
        val newState = if (gsmConfig.explicitlyMoveIntoBounds && currentState.traversalBounds != TraversalBounds.WithinBounds) {
            currentState.copy(traversalBounds = TraversalBounds.WithinBounds)
        } else {
            update(currentState)
        }

        _currentState = newState
        onStateUpdatedListener?.invoke(newState)
    }
}


internal class GsmConfig(
    val explicitlyMoveIntoBounds : Boolean
)
