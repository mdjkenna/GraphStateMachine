package mdk.gsm.state

/**
 * Graph traversal can have dead ends depending on the structure of the graph. The [TraversalBounds] class represents the different scenarios
 * that can occur in a [GraphStateMachine] regarding attempts to traverse when at a dead end, from either a [GraphStateMachineAction.Previous] action when at the first addVertex, or a [GraphStateMachineAction.Next] when there are no vertices left.
 * A common situation where this might occur is when using a [GraphStateMachine] to represent a workflow via a directed acyclic graph with a definite ending -
 * in this case the [TraversalBounds] class can be used to represent when the workflow is 'completed'.
 *
 * This can be ignored if it is not relevant for your use case.
 *
 * @see [mdk.gsm.state.TraversalState]
 * @see [mdk.gsm.builder.GraphStateMachineBuilderScope.explicitlyTransitionIntoBounds]
 */
enum class TraversalBounds {
    /**
     * Has not attempted to go beyond a dead end e.g. a [GraphStateMachineAction.Next] action when there are no vertices left. The normal status.
     */
    WithinBounds,

    /**
     * Means there has been a [GraphStateMachineAction.Previous] attempt when already at the first addVertex
     */
    BeforeFirst,

    /**
     * Means there has been a [GraphStateMachineAction.Next] attempt when there are no vertices left
     */
    BeyondLast
}