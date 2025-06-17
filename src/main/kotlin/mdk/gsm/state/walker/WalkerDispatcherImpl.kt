package mdk.gsm.state.walker

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mdk.gsm.graph.IVertex
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.GsmController
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.state.traverser.TraversalState
import mdk.gsm.util.CompletableAction
import org.jetbrains.annotations.ApiStatus

/**
 * Implementation of the [WalkerDispatcher] interface.
 *
 * This class provides methods to dispatch forward-only actions to the walker.
 * Unlike [mdk.gsm.state.traverser.TraverserDispatcher], it only supports Next, NextArgs, and Reset actions.
 *
 * @param V The type of vertices (states) in the graph. Must implement [IVertex].
 * @param I The type of vertex identifiers used in the graph.
 * @param F The type of traversal guard state, which controls conditional edge traversal. Must implement [mdk.gsm.state.ITransitionGuardState].
 * @param A The type of action arguments that can be passed when dispatching actions.
 */
internal class WalkerDispatcherImpl<V, I, F, A> private constructor(
    private val scope: CoroutineScope,
    private val actionChannel: Channel<CompletableAction<V, I, A>>
) : WalkerDispatcher<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {

    private val currentAction =
        MutableStateFlow<CompletableAction<V, I, A>?>(null)

    override fun launchDispatch(action: GraphStateMachineAction.Next) {
        scope.launch {
            dispatch(action)
        }
    }

    override fun launchDispatch(action: GraphStateMachineAction.NextArgs<A>) {
        scope.launch {
            dispatch(action)
        }
    }

    override fun launchDispatch(action: GraphStateMachineAction.Reset) {
        scope.launch {
            dispatch(action)
        }
    }

    override suspend fun dispatch(action: GraphStateMachineAction.Next) {
        actionChannel.send(CompletableAction(action, CompletableDeferred()))
    }

    override suspend fun dispatch(action: GraphStateMachineAction.NextArgs<A>) {
        actionChannel.send(CompletableAction(action, CompletableDeferred()))
    }

    override suspend fun dispatch(action: GraphStateMachineAction.Reset) {
        actionChannel.send(CompletableAction(action, CompletableDeferred()))
    }

    override suspend fun dispatchAndAwaitResult(action: GraphStateMachineAction.Next): TraversalState<V, I, A> {
        val completableAction = CompletableAction<V, I, A>(action, CompletableDeferred())
        dispatchInternal(completableAction)
        return completableAction.deferred.await()
    }

    override suspend fun dispatchAndAwaitResult(action: GraphStateMachineAction.NextArgs<A>): TraversalState<V, I, A> {
        val completableAction = CompletableAction<V, I, A>(action, CompletableDeferred())
        dispatchInternal(completableAction)
        return completableAction.deferred.await()
    }

    override suspend fun dispatchAndAwaitResult(action: GraphStateMachineAction.Reset): TraversalState<V, I, A> {
        val completableAction = CompletableAction<V, I, A>(action, CompletableDeferred())
        dispatchInternal(completableAction)
        return completableAction.deferred.await()
    }

    private suspend fun dispatchInternal(completableAction: CompletableAction<V, I, A>) {
        actionChannel.send(completableAction)
    }

    @ApiStatus.Experimental
    override suspend fun awaitNoDispatchedActions() {
        currentAction.filter { it == null }
            .first()
    }

    override fun tearDown() {
        scope.cancel()
    }

    private suspend inline fun withAction(action: CompletableAction<V, I, A>, block: suspend () -> Unit) {
        currentAction.value = action
        try {
            block()
        } finally {
            currentAction.value = null
        }
    }

    companion object {
        internal fun <V, I, F, A> create(
            gsm: GsmController<V, I, F, A>,
            singleThreadedScope: CoroutineScope,
            actionChannel: Channel<CompletableAction<V, I, A>> = Channel(Channel.UNLIMITED)
        ): WalkerDispatcherImpl<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {

            val walkerDispatcherImpl: WalkerDispatcherImpl<V, I, F, A> = WalkerDispatcherImpl(
                singleThreadedScope,
                actionChannel
            )

            singleThreadedScope.launch {
                for (action in actionChannel) {
                    walkerDispatcherImpl.withAction(action) {
                        gsm.dispatch(action)
                    }
                }
            }

            return walkerDispatcherImpl
        }
    }
}
