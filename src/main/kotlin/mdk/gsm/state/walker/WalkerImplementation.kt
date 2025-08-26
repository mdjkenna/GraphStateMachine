package mdk.gsm.state.walker

import kotlinx.coroutines.flow.StateFlow
import mdk.gsm.graph.IVertex
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.state.TransitionState

/**
 * Implementation of the [Walker] interface.
 *
 * This class combines a [WalkerState] and a [WalkerDispatcher] to provide a unified API for
 * both dispatching actions to and reading state from the walker.
 *
 * @param V The type of vertices (states) in the graph. Must implement [IVertex].
 * @param I The type of vertex identifiers used in the graph.
 * @param F The type of traversal guard state, which controls conditional edge traversal. Must implement [mdk.gsm.state.ITransitionGuardState].
 * @param A The type of action arguments that can be passed when dispatching actions.
 */
internal class WalkerImplementation<V, I, F, A> (
    override val walkerState: WalkerState<V, I, A>,
    override val walkerDispatcher: WalkerDispatcher<V, I, F, A>
) : Walker<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {

    override val current: StateFlow<TransitionState<V, I, A>>
        get() = walkerState.current

    override fun launchDispatch(action: GraphStateMachineAction.Next) {
        walkerDispatcher.launchDispatch(action)
    }

    override fun launchDispatch(action: GraphStateMachineAction.NextArgs<A>) {
        walkerDispatcher.launchDispatch(action)
    }

    override fun launchDispatch(action: GraphStateMachineAction.Reset) {
        walkerDispatcher.launchDispatch(action)
    }

    override suspend fun dispatch(action: GraphStateMachineAction.Next) {
        walkerDispatcher.dispatch(action)
    }

    override suspend fun dispatch(action: GraphStateMachineAction.NextArgs<A>) {
        walkerDispatcher.dispatch(action)
    }

    override suspend fun dispatch(action: GraphStateMachineAction.Reset) {
        walkerDispatcher.dispatch(action)
    }

    override suspend fun dispatchAndAwaitResult(action: GraphStateMachineAction.Next): TransitionState<V, I, A> {
        return walkerDispatcher.dispatchAndAwaitResult(action)
    }

    override suspend fun dispatchAndAwaitResult(action: GraphStateMachineAction.NextArgs<A>): TransitionState<V, I, A> {
        return walkerDispatcher.dispatchAndAwaitResult(action)
    }

    override suspend fun dispatchAndAwaitResult(action: GraphStateMachineAction.Reset): TransitionState<V, I, A> {
        return walkerDispatcher.dispatchAndAwaitResult(action)
    }

    override fun tearDown() {
        walkerDispatcher.tearDown()
    }
}
