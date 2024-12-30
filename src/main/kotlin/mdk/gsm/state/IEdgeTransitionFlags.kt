package mdk.gsm.state

/**
 * This is used in the edge building function [mdk.gsm.builder.EdgeBuilderScope.setTransitionHandler]
 *
 * Holds state that is processed as part of determining whether to traverse an edge or not.
 */
interface IEdgeTransitionFlags {

    object None : IEdgeTransitionFlags
}
