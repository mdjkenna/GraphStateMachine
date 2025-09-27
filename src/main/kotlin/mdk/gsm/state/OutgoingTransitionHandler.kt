package mdk.gsm.state

import mdk.gsm.builder.GsmBuilderScope
import mdk.gsm.graph.IVertex

/**
 * @property args Any arguments passed with the current action. May be null if no arguments were provided.
 * @property guardState The shared traversal guard state for the entire state machine.
 * @property vertex The current vertex from which transitions are being considered.
 *
 * @param V The type of vertices in the graph. Must implement [mdk.gsm.graph.IVertex].
 * @param I The type of vertex identifiers.
 * @param F The type of traversal guard state. Must implement [ITransitionGuardState].
 * @param A The type of arguments that can be passed with actions.
 *
 * @see OutgoingTransitionHandler
 */
@GsmBuilderScope
class OutgoingTransitionScope<V, I, F, A>(
    val args: A?,
    val guardState: F,
    val vertex: V
) where V : IVertex<I>, F : ITransitionGuardState {

    internal var noChange = false

    /**
     * Signals that the state machine should not traverse at all,
     * effectively keeping the current vertex as the state.
     *
     * When called, the state machine will skip exploring any outgoing edges
     * and remain in the current state.
     *
     * @see OutgoingTransitionHandler
     */
    fun noTransition() {
        noChange = true
    }
}

/**
 * This handler can be associated with a specific vertex during graph construction.
 * It is a function that executes custom logic before any outgoing transitions are explored.
{{ ... }}
 * The handler is invoked when the state machine is about to consider transitions from the current vertex.
 * It receives an [OutgoingTransitionScope] as its receiver, providing access to the current vertex,
 * the shared traversal guard state, and any arguments passed with the current action.
 *
 * @param V The type of vertices in the graph. Must implement [IVertex].
 * @param I The type of vertex identifiers.
 * @param F The type of traversal guard state. Must implement [ITransitionGuardState].
 * @param A The type of arguments that can be passed with actions.
 *
 * @see OutgoingTransitionScope
 * @see mdk.gsm.builder.VertexBuilderScope.onOutgoingTransition
 */
typealias OutgoingTransitionHandler<V, I, F, A> = suspend OutgoingTransitionScope<V, I, F, A>.() -> Unit