package mdk.gsm.graph.transition.walk

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.transition.IGraphTraversal
import mdk.gsm.state.ITransitionGuardState

/**
 * Factory for creating graph walk instances.
 *
 * This factory provides methods for creating stateless graph walks, which are used by the Walker implementation.
 * Unlike traditional traversals, stateless walks do not track visited nodes and do not support backtracking.
 */
internal object GraphWalkFactory {
    /**
     * Builds a stateless graph walk.
     *
     * @param graph The graph to walk through
     * @param startVertex The starting vertex for the walk
     * @return A new [StatelessGraphWalk] instance
     */
    internal fun <V, I, F, A> buildStatelessGraphWalk(
        graph: Graph<V, I, F, A>,
        startVertex: V
    ): IGraphTraversal<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {
        return StatelessGraphWalk(graph, startVertex)
    }
}