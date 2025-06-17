package mdk.gsm.graph.transition.walk

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.VertexContainer
import mdk.gsm.graph.transition.IGraphTraversal
import mdk.gsm.graph.transition.traversal.TraversalPathNode
import mdk.gsm.state.ITransitionGuardState

/**
 * Implements a stateless walk through a graph.
 *
 * Unlike traditional traversal implementations, StatelessGraphWalk:
 * - Does not track visited nodes
 * - Does not support backtracking (movePrevious)
 * - Simply follows edges based on traversal guards
 *
 * It still supports:
 * - Arguments
 * - Traversal guards
 * - Before visit handlers
 *
 * @param graph The graph to walk through
 * @param startVertex The starting vertex for the walk
 */
internal class StatelessGraphWalk<V, I, F, A>(
    private val graph: Graph<V, I, F, A>,
    private val startVertex: V,
) : IGraphTraversal<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {

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

            val nextVertex = graph.getVertex(edge.to) ?: continue

            // Create a new path node
            val oldPathNode = if (autoAdvance && !currentPathNode.isIntermediate) {
                currentPathNode.copy(isIntermediate = true)
            } else {
                currentPathNode
            }

            val newPathNode = TraversalPathNode(
                vertex = nextVertex,
                left = oldPathNode,
                args = args
            )

            // Update current state
            currentVertex = nextVertex
            currentPathNode = newPathNode

            return newPathNode
        }

        // No valid edge found
        return null
    }

    /**
     * Returns the current vertex in the walk.
     */
    override fun currentStep(): V {
        return currentVertex
    }

    /**
     * This operation is not supported in a stateless walk.
     * Always returns null as we don't support backtracking.
     */
    override fun movePrevious(): TraversalPathNode<V, A>? {
        // Not supported in stateless walk
        return null
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
     * Returns the current path as a list.
     * Note: Since this is a stateless walk, we only track the current path node
     * and its immediate predecessor for the purpose of supporting autoAdvance.
     */
    override fun tracePath(): List<V> {
        val path = ArrayList<V>()
        var current: TraversalPathNode<V, A>? = currentPathNode

        while (current != null) {
            path.add(current.vertex)
            current = current.left
        }

        path.reverse()
        return path
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