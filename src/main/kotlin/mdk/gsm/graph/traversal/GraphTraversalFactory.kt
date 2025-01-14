package mdk.gsm.graph.traversal

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.state.IEdgeTransitionFlags

internal object GraphTraversalFactory {
    fun <V, I, F> buildGraphTraversal(
        graph: Graph<V, I, F>,
        startVertex: V,
        isCyclic : Boolean
    ) : IGraphTraversal<V, I, F> where V : IVertex<I>, F : IEdgeTransitionFlags {
        return if (isCyclic) {
            CyclicGraphTraversal(graph, startVertex)
        } else {
            AcyclicTraversal(graph, startVertex)
        }
    }
}