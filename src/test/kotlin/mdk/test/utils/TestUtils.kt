package mdk.test.utils

import mdk.gsm.graph.IVertex
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.traverser.Traverser
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.state.traverser.TraversalState

suspend fun <V, I, F, A> Traverser<V, I, F, A>.goNextAndRecordPublishedStatesUntilEnd(): List<TraversalState<V, I, A>>
        where V : IVertex<I>, F : ITransitionGuardState {

    val states = mutableListOf<TraversalState<V, I, A>>()
    var currentState : TraversalState<V, I, A> = current.value

    while (!currentState.isBeyondLast) {
        states.add(currentState)
        currentState = dispatchAndAwaitResult(
            GraphStateMachineAction.Next
        )
    }

    return states
}

suspend fun <V, I, F, A> Traverser<V, I, F, A>.goPreviousAndRecordPublishedStatesUntilStart(): List<TraversalState<V, I, A>>
        where V : IVertex<I>, F : ITransitionGuardState {

    val states = mutableListOf<TraversalState<V, I, A>>()
    var currentState = current.value

    while (!currentState.isBeforeFirst) {
        states.add(currentState)
        currentState = dispatchAndAwaitResult(GraphStateMachineAction.Previous)
    }

    return states
}
