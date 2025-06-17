package mdk.gsm.state.traverser

import kotlinx.coroutines.flow.StateFlow
import mdk.gsm.graph.IVertex
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.ITransitionGuardState

internal class TraverserImplementation<V, I, F, A> (
    override val traverserState: TraverserState<V, I, F, A>,
    override val traverserDispatcher: TraverserDispatcher<V, I, F, A>
) : Traverser<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {

    override val current: StateFlow<TraversalState<V, I, A>>
        get() = traverserState.current

    override fun tracePath(): List<V> {
        return traverserState.tracePath()
    }

    override fun launchDispatch(action: GraphStateMachineAction<A>) {
        traverserDispatcher.launchDispatch(action)
    }

    override suspend fun dispatch(action: GraphStateMachineAction<A>) {
        traverserDispatcher.dispatch(action)
    }

    override suspend fun dispatchAndAwaitResult(action: GraphStateMachineAction<A>): TraversalState<V, I, A> {
        return traverserDispatcher.dispatchAndAwaitResult(action)
    }

    override suspend fun awaitNoDispatchedActions() {
        traverserDispatcher.awaitNoDispatchedActions()
    }

    override fun tearDown() {
        traverserDispatcher.tearDown()
    }
}
