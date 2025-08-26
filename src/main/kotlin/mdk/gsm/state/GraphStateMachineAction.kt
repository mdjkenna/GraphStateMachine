package mdk.gsm.state

/**
 * Represents actions that can be dispatched to a graph state machine.
 *
 * Actions are processed by the graph state machine in the order they are received.
 * with each action causing a new [mdk.gsm.state.traverser.TraversalState] to be published and potentially a traversal to a new vertex.
 *
 * @param A The type of arguments that can be passed with certain actions (specifically [NextArgs]).
 *          This allows for passing contextual data during state transitions.
 *
 * @see mdk.gsm.state.traverser.TraversalState For the resulting state after an action is processed
 */
sealed class GraphStateMachineAction<out A> {

    /**
     * Action to move to the next vertex in the graph with additional arguments of type [A].
     *
     * The argument [A] is made available to any [TransitionGuard] functions.
     * The argument is forms part of the published state since it is stored in the resulting [mdk.gsm.state.traverser.TraversalState] and accessible via [mdk.gsm.state.traverser.TraversalState.args].
     * An example use case would be specifying an identifier to perform an action on to as loading an entity from a database.
     *
     * Note that use of arguments is completely optional and can be bypassed using the regular [Next] action.
     *
     * @property args The arguments to pass with the next action, available to traversal guards and stored in the resulting state
     */
    class NextArgs<A>(
        val args: A
    ) : GraphStateMachineAction<A>()

    /**
     * Action for forward progression of the state machine without supplying any arguments.
     */
    data object Next : GraphStateMachineAction<Nothing>()

    /**
     * Action for backwards progression of the state machine without supplying any arguments.
     * No arguments are available on any previous action as moving backwards through historic states is unconditional.
     */
    data object Previous : GraphStateMachineAction<Nothing>()

    /**
     * Action to reset the state machine to its initial state, clearing all traversal history.
     * Note this causes: [ITransitionGuardState.onReset] to be called.
     *
     * This can allow the same state machine instance to be reused, as long as custom mutations performed by the implementers code are completely re-initialised in the
     * [ITransitionGuardState.onReset] implementation.
     */
    data object Reset : GraphStateMachineAction<Nothing>()
}
