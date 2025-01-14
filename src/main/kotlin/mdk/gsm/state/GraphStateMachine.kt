@file:Suppress("unused")

package mdk.gsm.state

import mdk.gsm.graph.IVertex
import mdk.gsm.graph.traversal.EdgeTraversalType
import mdk.gsm.graph.traversal.IGraphTraversal

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
class GraphStateMachine<out V, I, F> internal constructor(
    private val graphTraversal: IGraphTraversal<V, I, F>,
    private val gsmConfig: GsmConfig,
    val flags: F,
    internal val edgeTraversalType: EdgeTraversalType
) where V : IVertex<I>, F : IEdgeTransitionFlags {

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
     * Traces the path of the traversal from the start addVertex to the current addVertex.
     *
     * @return A list of vertices representing the path from the start addVertex to the current addVertex.
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

                    val next = graphTraversal.moveNext(flags)
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
