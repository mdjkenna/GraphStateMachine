package mdk.gsm.graph.transition.traverse

/**
 * Edge exploration strategy used by the traverser when planning transitions across the graph.
 *
 * This setting controls whether depth-first exploration may revisit vertices (allow cycles) or
 * avoids them (treat the graph as acyclic) when evaluating possible transitions.
 *
 * Specify the type via [mdk.gsm.builder.TraverserBuilderScope.setTraversalType].
 *
 * @see mdk.gsm.graph.transition.TransitionFactory
 * @see mdk.gsm.builder.TraverserBuilderScope.setTraversalType
 */
enum class EdgeTraversalType {
    /**
     * Depth-first exploration that avoids cycles by not revisiting vertices on the current path (stack).
     *
     * Suitable for:
     * - Directed acyclic graphs (DAGs)
     * - Workflows with no loops
     * - Ensuring each vertex is considered at most once during an exploration
     *
     * When a cycle is detected, that path is not followed, effectively treating the graph as acyclic.
     */
    DFSAcyclic,

    /**
     * Depth-first exploration that permits cycles, allowing vertices to be revisited.
     *
     * Suitable for:
     * - Graphs with intentional cycles
     * - State machines that need to revisit states
     * - Interactive workflows where returning to previous steps is required
     *
     * In this mode, vertices on the current path may be revisited when the structure contains cycles,
     * enabling transitions through loops.
     */
    DFSCyclic
}
