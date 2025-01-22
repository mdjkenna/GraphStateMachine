package mdk.gsm.graph.traversal

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.state.ITraversalGuardState

internal object GraphTraversalFactory {
    fun <V, I, F> buildGraphTraversal(
        graph: Graph<V, I, F>,
        startVertex: V,
        traversalType: EdgeTraversalType
    ) : IGraphTraversal<V, I, F> where V : IVertex<I>, F : ITraversalGuardState {
        return if (traversalType == EdgeTraversalType.DFSCyclic) {
            CyclicDfsGraphTraversal(graph, startVertex)
        } else {
            AcyclicDfsTraversal(graph, startVertex)
        }
    }
}