package mdk.gsm.graph.transition.traverse

/**
 * Represents the type of edge traversal strategy used in a graph-based state machine.
 *
 * The choice of an edge traversal type determines how the graph is traversed, including rules about revisiting vertices.
 * This affects how cycles are treated, where cycles refer to a vertex being re-encountered during
 * the exploration of its descendant vertices (i.e., a vertex with a path back to itself).
 *
 * In graph traversal terminology:
 * - A "white" vertex has not been discovered yet
 * - A "grey" vertex has been discovered but not fully explored
 * - A "black" vertex has been fully explored
 *
 * The traversal type is specified when building a graph state machine using
 * [mdk.gsm.builder.TraverserBuilderScope.setTraversalType].
 *
 * @see mdk.gsm.graph.transition.TransitionFactory
 * @see mdk.gsm.builder.TraverserBuilderScope.setTraversalType
 */
enum class EdgeTraversalType {
    /**
     * Depth-first traversal that avoids cycles by preventing revisits to vertices that are currently being explored.
     *
     * This traversal type is suitable for:
     * - Directed acyclic graphs (DAGs)
     * - Workflows with no loops
     * - Ensuring each vertex is visited at most once during a traversal
     *
     * When a cycle is detected, the traversal will not follow that path, effectively treating the graph as acyclic.
     */
    DFSAcyclic,

    /**
     * Depth-first traversal that permits cycles, allowing vertices to be revisited.
     *
     * This traversal type is suitable for:
     * - Graphs with intentional cycles
     * - State machines that need to revisit states
     * - Interactive workflows where users may need to return to previous steps
     *
     * In this mode, "grey" vertices (those currently being explored) may be revisited if the graph contains cycles,
     * enabling traversal through loops in the graph structure.
     */
    DFSCyclic
}
