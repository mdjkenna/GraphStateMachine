package mdk.gsm.state.walker

import kotlinx.coroutines.flow.StateFlow
import mdk.gsm.graph.IVertex
import mdk.gsm.state.GsmController
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.state.traverser.TraversalState

/**
 * Implementation of the [WalkerState] interface.
 *
 * This class provides read-only access to the walker's current state.
 * Unlike [mdk.gsm.state.traverser.TraverserState], it does not provide access to traversal history as walkers do not maintain history.
 *
 * @param V The type of vertices (states) in the graph. Must implement [IVertex].
 * @param I The type of vertex identifiers used in the graph.
 * @param F The type of traversal guard state, which controls conditional edge traversal. Must implement [mdk.gsm.state.ITransitionGuardState].
 * @param A The type of action arguments that can be passed when dispatching actions.
 */
internal class WalkerStateImpl<V, I, F, A> private constructor(
    val gsm: GsmController<V, I, F, A>
) : WalkerState<V, I, A> where V : IVertex<I>, F : ITransitionGuardState {

    override val current: StateFlow<TraversalState<V, I, A>>
        get() = gsm.stateOut

    companion object {
        internal fun <V, I, F, A> create(
            gsm: GsmController<V, I, F, A>
        ): WalkerStateImpl<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {
            return WalkerStateImpl(gsm)
        }
    }
}