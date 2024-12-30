package mdk.gsm.graph.traversal

/**
 * The type of edge traversal to use when traversing the graph.
 *
 * Generally it is desired to avoid cycles, so use an Acyclic option unless there is a specific need to support cycles.
 * The default option used is: [EdgeTraversalType.RetrogradeAcyclic].
 *
 * There are two situations affected by the traversal type currently:
 * - An edge is not traversed due to a transition handler returning false, but later becomes a traversal candidate while its source vertex is still active
 * - The graph contains cycles
 *
 * If neither of the above situations are encountered, then there is currently no difference between the traversal types.
 */
enum class EdgeTraversalType {
    /**
     * Looks "forwards" and "backwards" on a source vertex for potential edges to traverse.
     * Each traversal attempt rechecks all non-visited edges of gray vertices for traversal.
     * This option will make an edge which was previously not a candidate for traversal be checked again for traversal.
     *
     * Cycles are ignored.
     */
    RetrogradeAcyclic,

    /**
     * Only looks "forwards" in terms of potential edges to traverse.
     * Each traversal attempt records an edge index, and every subsequent traversal attempt will only consider edges after that index.
     * Unlike [EdgeTraversalType.RetrogradeAcyclic], edges before the current edge are not rechecked for traversal.
     *
     * Cycles are ignored.
     */
    ForwardAcyclic,

    /**
     * Cycles are not ignored.
     *
     * Similar to [EdgeTraversalType.ForwardAcyclic], except this option will not ignore cycles but instead transition through them.
     * Arriving at a gray vertex will reset the traversal to the first edge of that vertex.
     * This means when cycles are encountered, they can potentially go on forever until they are prevented in a transition handler.
     * This can be desirable behaviour for certain situations, but should be used with caution.
     */
    ForwardCyclic
}