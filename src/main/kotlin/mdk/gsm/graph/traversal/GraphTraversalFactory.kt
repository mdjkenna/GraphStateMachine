package mdk.gsm.graph.traversal

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.state.IEdgeTransitionFlags

internal object GraphTraversalFactory {
    fun <V, I, F> buildGraphTraversal(
        graph: Graph<V, I, F>,
        startVertex: V,
        traversalType: EdgeTraversalType
    ) : GraphTraversal<V, I, F> where V : IVertex<I>, F : IEdgeTransitionFlags {
        return when(traversalType) {
            EdgeTraversalType.RetrogradeAcyclic -> RetrogradeGraphTraversal(graph, startVertex)
            EdgeTraversalType.ForwardAcyclic -> ForwardGraphTraversal(graph, startVertex)
            EdgeTraversalType.ForwardCyclic -> ForwardGraphTraversal(graph, startVertex, skipCycles = false)
        }
    }
}