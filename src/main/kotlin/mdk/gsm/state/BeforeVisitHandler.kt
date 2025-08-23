package mdk.gsm.state

import mdk.gsm.builder.GsmBuilderScope
import mdk.gsm.graph.IVertex

/**
 * Provides context and control capabilities to vertex pre-visit handlers.
 *
 * This scope is automatically created and passed to a [BeforeVisitHandler] function when the state machine
 * is about to traversal to a vertex. It provides access to:
 *
 * 1. The vertex that is about to be visited
 * 2. The shared traversal guard state
 * 3. Any arguments passed with the current action
 *
 * Additionally, it provides control over the traversal process through the [autoAdvance] method,
 * which allows the handler to mark the vertex as an intermediate state that should not be published
 * as the current state of the state machine.
 *
 * Common use cases for BeforeVisitScope include:
 * - Performing setup operations before a vertex becomes the current state
 * - Validating preconditions for entering a state
 * - Implementing intermediate vertices that perform operations but aren't exposed to observers
 * - Creating automatic transitions through certain vertices based on runtime conditions
 *
 * @property vertex The vertex that is about to be visited (become the current state).
 * @property guardState The shared traversal guard state for the entire state machine.
 * @property args Any arguments passed with the current action. May be null if no arguments were provided.
 *
 * @param V The type of vertices in the graph. Must implement [mdk.gsm.graph.IVertex].
 * @param I The type of vertex identifiers.
 * @param F The type of traversal guard state. Must implement [ITransitionGuardState].
 * @param A The type of arguments that can be passed with actions.
 *
 * @see BeforeVisitHandler
 * @see mdk.gsm.graph.VertexContainer
 */
@GsmBuilderScope
class BeforeVisitScope<V, I, F, A>(
    val vertex: V,
    val guardState: F,
    val args : A?
) where V : IVertex<I>, F : ITransitionGuardState {

    internal var autoAdvanceTrigger = false

    /**
     * Signals that the state machine should automatically advance to the next state
     * without publishing this vertex as the current state.
     *
     * When called, the handler's vertex becomes an 'intermediate state'.
     * Intermediate states are 'in-between' states that are not published,
     * functioning as 'effects' that are explicitly represented in the state machine but never perceived by observers.
     *
     * Note: Intermediate states are skipped over during previous actions.
     *
     * @see BeforeVisitHandler
     */
    fun autoAdvance() {
        autoAdvanceTrigger = true
    }
}

/**
 * This handler can be associated with a specific vertex during graph construction.
 * It is function that executes custom logic immediately before a vertex is visited in the graph traversal.
 *
 * The handler is invoked when the state machine is about to traversal to a new vertex, but before
 * that vertex is published as the current state. It receives a [BeforeVisitScope] as its receiver,
 * providing access to the vertex being visited, the shared traversal guard state, and any arguments
 * passed with the current action.
 *
 *
 * @param V The type of vertices in the graph. Must implement [IVertex].
 * @param I The type of vertex identifiers.
 * @param F The type of traversal guard state. Must implement [ITransitionGuardState].
 * @param A The type of arguments that can be passed with actions.
 *
 * @see BeforeVisitScope
 * @see mdk.gsm.builder.VertexBuilderScope.onBeforeVisit
 */
typealias BeforeVisitHandler<V, I, F, A> = suspend BeforeVisitScope<V, I, F, A>.() -> Unit
