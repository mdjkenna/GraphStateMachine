package mdk.gsm.state.walker

import mdk.gsm.graph.IVertex
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.state.traverser.TraversalState
import org.jetbrains.annotations.ApiStatus

/**
 * Primary interface for interacting with a graph-based walker. This interface combines the capabilities
 * of both [WalkerDispatcher] and [WalkerState], providing a unified API for both
 * dispatching actions to and reading state from the walker.
 *
 * The walker represents states as vertices in a directed graph, with edges representing
 * possible transitions between states. The walker can be traversed by dispatching actions,
 * which cause transitions along the edges of the graph according to the defined traversal rules.
 * Unlike a traverser, a walker only supports forward movement and does not maintain a history
 * of visited states.
 *
 * This interface is typically obtained by using the builder function in [mdk.gsm.builder]:
 * - [mdk.gsm.builder.buildWalker]
 *
 * @param V The type of vertices (states) in the graph. Must implement [IVertex].
 * @param I The type of vertex identifiers used in the graph.
 * @param F The type of traversal guard state, which controls conditional edge traversal. Must implement [mdk.gsm.state.ITransitionGuardState].
 * @param A The type of action arguments that can be passed when dispatching actions.
 *
 * @see WalkerDispatcher For dispatching actions to the walker
 * @see WalkerState For reading the current state
 * @see mdk.gsm.builder.buildWalker
 */
interface Walker<V, I, F, A> : WalkerState<V, I, A>
        where V : IVertex<I>, F : ITransitionGuardState
{
    /**
     * Provides read-only access to the walker's current state.
     * This property allows accessing the walker's data without the ability to modify it.
     */
    val walkerState : WalkerState<V, I, A>

    /**
     * Provides the ability to dispatch actions to the walker, causing state transitions.
     * This property allows modifying the walker's state through controlled actions.
     */
    val walkerDispatcher : WalkerDispatcher<V, I, F, A>

    /**
     * Asynchronously dispatches a Next action to the walker without waiting for it to complete.
     *
     * This method launches a coroutine to dispatch the action and immediately returns, allowing
     * the caller to continue execution while the action is processed in the background.
     */
    fun launchDispatch(action: GraphStateMachineAction.Next)

    /**
     * Asynchronously dispatches a NextArgs action to the walker without waiting for it to complete.
     *
     * This method launches a coroutine to dispatch the action and immediately returns, allowing
     * the caller to continue execution while the action is processed in the background.
     *
     * @param action The [GraphStateMachineAction.NextArgs] to dispatch to the walker
     */
    fun launchDispatch(action: GraphStateMachineAction.NextArgs<A>)

    /**
     * Asynchronously dispatches a Reset action to the walker without waiting for it to complete.
     *
     * This method launches a coroutine to dispatch the action and immediately returns, allowing
     * the caller to continue execution while the action is processed in the background.
     */
    fun launchDispatch(action: GraphStateMachineAction.Reset)

    /**
     * Suspends the current coroutine and dispatches a Next action to the walker.
     *
     * This method suspends until the action is received by the walker, but does not
     * wait for the action to be fully processed or for the state transition to complete.
     */
    suspend fun dispatch(action: GraphStateMachineAction.Next)

    /**
     * Suspends the current coroutine and dispatches a NextArgs action to the walker.
     *
     * This method suspends until the action is received by the walker, but does not
     * wait for the action to be fully processed or for the state transition to complete.
     *
     * @param action The [GraphStateMachineAction.NextArgs] to dispatch to the walker
     */
    suspend fun dispatch(action: GraphStateMachineAction.NextArgs<A>)

    /**
     * Suspends the current coroutine and dispatches a Reset action to the walker.
     *
     * This method suspends until the action is received by the walker, but does not
     * wait for the action to be fully processed or for the state transition to complete.
     */
    suspend fun dispatch(action: GraphStateMachineAction.Reset)

    /**
     * Dispatches a Next action to the walker and awaits the resulting state.
     *
     * This method suspends until the action is fully processed and the state transition is complete,
     * then returns the new state of the walker.
     *
     * @return The [TraversalState] representing the new state after the action is processed
     */
    suspend fun dispatchAndAwaitResult(action: GraphStateMachineAction.Next): TraversalState<V, I, A>

    /**
     * Dispatches a NextArgs action to the walker and awaits the resulting state.
     *
     * This method suspends until the action is fully processed and the state transition is complete,
     * then returns the new state of the walker.
     *
     * @param action The [GraphStateMachineAction.NextArgs] to dispatch to the walker
     * @return The [TraversalState] representing the new state after the action is processed
     */
    suspend fun dispatchAndAwaitResult(action: GraphStateMachineAction.NextArgs<A>): TraversalState<V, I, A>

    /**
     * Dispatches a Reset action to the walker and awaits the resulting state.
     *
     * This method suspends until the action is fully processed and the state transition is complete,
     * then returns the new state of the walker.
     *
     * @return The [TraversalState] representing the new state after the action is processed
     */
    suspend fun dispatchAndAwaitResult(action: GraphStateMachineAction.Reset): TraversalState<V, I, A>

    /**
     * Suspends until all previously dispatched actions have been fully consumed and the queue is empty.
     * This has niche uses such as throttling actions.
     *
     * Note: This API is experimental and may change in future releases.
     * This method does not wait for the last action in the queue to finish processing.
     */
    @ApiStatus.Experimental
    suspend fun awaitNoDispatchedActions()

    /**
     * Tears down the walker, cancelling all associated jobs.
     * This is irreversible, and the walker can not be used afterwards.
     *
     * Note this will directly cancel the walker's coroutine scope.
     * Be mindful of this if you have used your own.
     *
     * This method should be called when the walker is no longer needed to ensure
     * proper cleanup of resources, such as coroutines and channels.
     */
    fun tearDown()

    /**
     * Component function that enables destructuring the walker to get the state component.
     * This allows for convenient access to the state in destructuring declarations.
     * 
     * @return The [WalkerState] component of this walker
     */
    operator fun component1(): WalkerState<V, I, A> = walkerState

    /**
     * Component function that enables destructuring the walker to get the dispatcher component.
     * This allows for convenient access to the dispatcher in destructuring declarations.
     * 
     * @return The [WalkerDispatcher] component of this walker
     */
    operator fun component2(): WalkerDispatcher<V, I, F, A> = walkerDispatcher
}
