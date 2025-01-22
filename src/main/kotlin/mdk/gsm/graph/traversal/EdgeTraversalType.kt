package mdk.gsm.graph.traversal

/**
 * Represents the type of edge traversal allowed in a graph.
 *
 * The choice of edge traversal type determines how the graph is traversed, including rules about revisiting vertices.
 * This effects how cycles are treated, where cycles refer to a vertex being re-encountered during
 * the exploration of its descendant vertices i.e. a vertex with a path back to itself or a "grey" vertex being encountered
 */
enum class EdgeTraversalType {
    /**
     * Depth-first traversal that avoids cycles
     */
    DFSAcyclic,

    /**
     * Depth-first traversal that permits cycles.
     * In this mode, grey vertices may be revisited if the graph contains cycles.
     */
    DFSCyclic
}