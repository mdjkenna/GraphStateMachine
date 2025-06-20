package mdk.gsm.state

/**
 * Graph traversal can have dead ends depending on the structure of the graph. The [TraversalBounds] class represents the different scenarios
 * that can occur, from either a [GraphStateMachineAction.Previous] action when at the first vertex,
 * or a [GraphStateMachineAction.Next] when there are no vertices left to go to.
 *
 * A cyclic graph might retain a status of [TraversalBounds.WithinBounds] indefinitely.
 * A value of this enum forms part of the published [mdk.gsm.state.traverser.TraversalState].
 * It is particularly relevant for DAGs and workflow related use cases.
 *
 * Depending on the implementer's requirements, a [mdk.gsm.state.traverser.TraversalState] with a value other than [WithinBounds] might be treated as null.
 *
 * @see [mdk.gsm.state.traverser.TraversalState]
 */
enum class TraversalBounds {
    /**
     * The normal / default status. Depending on the context
     * Has not attempted to go beyond a dead end e.g., a [GraphStateMachineAction.Next] action when there are no vertices left.
     */
    WithinBounds,

    /**
     * Means there has been a [GraphStateMachineAction.Previous] attempt when already at the first vertex
     */
    BeforeFirst,

    /**
     * Means there has been a [GraphStateMachineAction.Next] attempt when there are no vertices left to go next to in the graph
     */
    BeyondLast
}