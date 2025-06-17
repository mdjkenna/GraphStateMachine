package mdk.gsm.graph.transition.traversal

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.transition.IGraphTraversal
import mdk.gsm.state.ITransitionGuardState

@PublishedApi
internal object GraphTraversalFactory {

    fun <V, I, F, A> buildGraphTraversal(
        graph: Graph<V, I, F, A>,
        startVertex: V,
        traversalType: EdgeTraversalType
    ) : IGraphTraversal<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {
        return if (traversalType == EdgeTraversalType.DFSCyclic) {
            CyclicDfsGraphTraversal(graph, startVertex)
        } else {
            AcyclicDfsTraversal(graph, startVertex)
        }
    }
}