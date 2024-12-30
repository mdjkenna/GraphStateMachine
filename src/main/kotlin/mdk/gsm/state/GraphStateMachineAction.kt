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
     * Represents the action of clearing all existing state and starting from the beginning, as if newly created.
     * Note edge transition flags are not automatically reset, the caller must do this if necessary.
     */
    data object Reset : GraphStateMachineAction()
}