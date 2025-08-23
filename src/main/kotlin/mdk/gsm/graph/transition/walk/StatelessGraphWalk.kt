package mdk.gsm.graph.transition.walk

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.VertexContainer
import mdk.gsm.graph.transition.IForwardTransition
import mdk.gsm.graph.transition.IResettable
import mdk.gsm.graph.transition.traverse.TraversalPathNode
import mdk.gsm.state.ITransitionGuardState


internal class StatelessGraphWalk<V, I, F, A>(
    private val graph: Graph<V, I, F, A>,
    private val startVertex: V,
) : IForwardTransition<V, I, F, A>, IResettable<V> where V : IVertex<I>, F : ITransitionGuardState {

    private var currentVertex: V = startVertex
    private var currentPathNode: TraversalPathNode<V, A> = TraversalPathNode(vertex = startVertex)

    /**
     * Moves to the next vertex in the graph based on the traversal guards.
     * Unlike traditional traversal, this implementation doesn't track visited nodes
     * and simply follows the first valid edge it finds.
     */
    override suspend fun moveNext(
        guardState: F,
        autoAdvance: Boolean,
        args: A?
    ): TraversalPathNode<V, A>? {
        val sortedEdges = graph.getOutgoingEdgesSorted(currentVertex).orEmpty()

        for (edge in sortedEdges) {
            if (!edge.canProceed(guardState, args)) {
                continue
            }

            val nextVertex = graph.getVertex(edge.to)
                ?: continue

            val newPathNode = TraversalPathNode(
                vertex = nextVertex,
                left = null,
                args = args
            )

            currentVertex = nextVertex
            currentPathNode = newPathNode

            return newPathNode
        }
        
        return null
    }

    /**
     * Returns the current vertex in the walk.
     */
    override fun currentStep(): V {
        return currentVertex
    }

    /**
     * Resets the walk to the start vertex.
     */
    override fun reset(): V {
        currentVertex = startVertex
        currentPathNode = TraversalPathNode(vertex = startVertex)
        return startVertex
    }

    /**
     * Returns the vertex container for the given ID.
     */
    override fun getVertexContainer(id: I): VertexContainer<V, I, F, A>? {
        return graph.getVertexContainer(id)
    }

    /**
     * Returns the current path node.
     */
    override fun head(): TraversalPathNode<V, A> {
        return currentPathNode
    }
}
