package mdk.test.utils

import mdk.gsm.graph.IVertex
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.state.TransitionState
import mdk.gsm.state.traverser.Traverser

suspend fun <V, I, F, A> Traverser<V, I, F, A>.goNextAndRecordPublishedStatesUntilEnd(): List<TransitionState<V, I, A>>
        where V : IVertex<I>, F : ITransitionGuardState {

    val states = mutableListOf<TransitionState<V, I, A>>()
    var currentState : TransitionState<V, I, A> = current.value

    while (!currentState.isBeyondLast) {
        states.add(currentState)
        currentState = dispatchAndAwaitResult(
            GraphStateMachineAction.Next
        )
    }

    return states
}

suspend fun <V, I, F, A> Traverser<V, I, F, A>.goPreviousAndRecordPublishedStatesUntilStart(): List<TransitionState<V, I, A>>
        where V : IVertex<I>, F : ITransitionGuardState {

    val states = mutableListOf<TransitionState<V, I, A>>()
    var currentState = current.value

    while (!currentState.isBeforeFirst) {
        states.add(currentState)
        currentState = dispatchAndAwaitResult(GraphStateMachineAction.Previous)
    }

    return states
}
