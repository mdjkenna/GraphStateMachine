package mdk.gsm.state.traverser

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import mdk.gsm.action.CompletableAction
import mdk.gsm.graph.IVertex
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.GsmController
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.state.TransitionState

internal class TraverserDispatcherImplementation<V, I, F, A> private constructor(
    private val scope: CoroutineScope,
    private val actionChannel: Channel<CompletableAction<V, I, A>>
) : TraverserDispatcher<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {

    override fun launchDispatch(action: GraphStateMachineAction<A>) {
        scope.launch {
            dispatch(action)
        }
    }

    override suspend fun dispatch(action: GraphStateMachineAction<A>) {
        actionChannel.send(CompletableAction(action, CompletableDeferred()))
    }

    override suspend fun dispatchAndAwaitResult(action: GraphStateMachineAction<A>) : TransitionState<V, I, A> {
        val completableAction = CompletableAction<V, I, A>(action, CompletableDeferred())
        dispatchInternal(completableAction)

        return completableAction.deferred.await()
    }

    private suspend fun dispatchInternal(completableAction: CompletableAction<V, I, A>) {
        actionChannel.send(completableAction)
    }

    override fun tearDown() {
        scope.cancel()
    }

    private suspend inline fun withAction(
        crossinline block: suspend () -> Unit
    ) {
        block()
    }

    companion object {
        internal fun <V, I, F, A> create(
            gsm: GsmController<V, I, F, A>,
            singleThreadedScope: CoroutineScope,
            actionChannel: Channel<CompletableAction<V, I, A>>,
        ) : TraverserDispatcherImplementation<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {

            val gsmDispatcherImpl: TraverserDispatcherImplementation<V, I, F, A> = TraverserDispatcherImplementation(
                singleThreadedScope,
                actionChannel
            )

            singleThreadedScope.launch {
                for (action in actionChannel) {
                    gsmDispatcherImpl.withAction() {
                        gsm.dispatch(action)
                    }
                }
            }

            return gsmDispatcherImpl
        }
    }
}
