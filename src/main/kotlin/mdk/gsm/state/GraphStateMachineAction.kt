package mdk.gsm.state

sealed class GraphStateMachineAction {
    /**
     * Moves state to the next vertex in the graph if available
     */
    data object Next : GraphStateMachineAction()

    /**
     * Moves state to the previous vertex in the graph unconditionally
     */
    data object Previous : GraphStateMachineAction()

    /**
     * Represents the action of clearing all existing state and starting from the beginning,
     * as if newly created. Allows for reuse.
     *
     * @see [ITraversalGuardState.onReset]
     */
    data object Reset : GraphStateMachineAction()
}