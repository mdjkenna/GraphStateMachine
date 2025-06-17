package mdk.gsm.graph.transition.traversal

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.VertexContainer
import mdk.gsm.graph.transition.IGraphTraversal
import mdk.gsm.state.ITransitionGuardState

internal class AcyclicDfsTraversal<V, I, F, A>(
    private val graph: Graph<V, I, F, A>,
    private val startVertex: V
) : IGraphTraversal<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {

    private var traversalPath = TraversalPath<V, I, A>(startVertex)

    private val visited: HashSet<I> = HashSet()

    override fun getVertexContainer(id: I): VertexContainer<V, I, F, A>? {
        return graph.getVertexContainer(id)
    }

    override fun head() : TraversalPathNode<V, A> {
        return traversalPath.pathHead
    }

    override suspend fun moveNext(
        guardState: F,
        autoAdvance: Boolean,
        args: A?
    ): TraversalPathNode<V, A>? {
        visited.add(traversalPath.currentVertex.id)

        val nextAdjacentStep = nextVertexOrNull(traversalPath.currentVertex, guardState, args)
        if (nextAdjacentStep != null) {
            return traversalPath.appendPathHead(
                vertex = nextAdjacentStep,
                isAutoAdvancing = autoAdvance,
                args = args
            )
        } else {
            var current = traversalPath.pathHead.left
            while (current != null) {
                val next = nextVertexOrNull(current.vertex, guardState, args)
                if (next == null) {
                    current = current.left
                } else {
                    return traversalPath.appendPathHead(
                        vertex = next,
                        isAutoAdvancing = autoAdvance,
                        args = args
                    )
                }
            }
        }

        return null
    }

    private suspend fun nextVertexOrNull(vertex : V, flags: F, args: A?): V? {
        val sortedEdges = graph.getOutgoingEdgesSorted(vertex)

        if (sortedEdges == null || sortedEdges.isEmpty()) {
            return null
        }

        for (i in sortedEdges.indices) {
            val edge = sortedEdges[i]

            if (visited.contains(edge.to)) {
                continue
            }

            if (!edge.canProceed(flags, args)) {
                continue
            }

            return graph.getVertex(edge.to)
        }

        return null
    }

    override fun currentStep(): V {
        return traversalPath.currentVertex
    }

    override fun movePrevious(): TraversalPathNode<V, A>? {
        var left = traversalPath.pathHead.left
            ?: return null

        visited.remove(traversalPath.currentVertex.id)

        while (true) {
            visited.remove(left.vertex.id)
            if (left.isIntermediate && left.left != null) {
                left = left.left
            } else {
                break
            }
        }

        traversalPath.setPathHead(left)

        return left
    }

    override fun reset(): V {
        visited.clear()
        traversalPath.reset(startVertex)

        return startVertex
    }

    override fun tracePath(): List<V> {
       return traversalPath.tracePath()
    }
}