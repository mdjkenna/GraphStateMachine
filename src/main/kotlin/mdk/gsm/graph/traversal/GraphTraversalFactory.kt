package mdk.gsm.graph.traversal

import mdk.gsm.graph.IVertex
import mdk.gsm.graph.Graph
import mdk.gsm.state.IEdgeTransitionFlags

internal object GraphTraversalFactory {
    fun <V, F> buildGraphTraversal(
        graph: Graph<V, F>,
        startVertex: V,
        traversalType: EdgeTraversalType
    ) : GraphTraversal<V, F> where V : IVertex, F : IEdgeTransitionFlags {
        return when(traversalType) {
            EdgeTraversalType.RetrogradeAcyclic -> RetrogradeGraphTraversal(graph, startVertex)
            EdgeTraversalType.ForwardAcyclic -> ForwardGraphTraversal(graph, startVertex)
            EdgeTraversalType.ForwardCyclic -> ForwardGraphTraversal(graph, startVertex, skipCycles = false)
        }
    }
}