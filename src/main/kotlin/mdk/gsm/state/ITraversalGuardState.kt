package mdk.gsm.state

/**
 * This is used in the edge building function [mdk.gsm.builder.EdgeBuilderScope.setEdgeTraversalGate]
 *
 * Holds state that is processed as part of determining whether to traverse an edge or not.
 */
interface ITraversalGuardState {

    object None : ITraversalGuardState {
        override fun onReset() = Unit
    }

    /**
     * Called if a [GraphStateMachineAction.Reset] action is received by the state machine.
     * Use this to reset your [ITraversalGuardState] implementation if required.
     */
    fun onReset()
}
