package mdk.gsm.graph.traversal

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.state.ITraversalGuardState

internal class AcyclicDfsTraversal<V, I, F>(
    private val graph: Graph<V, I, F>,
    private val startVertex: V
) : IGraphTraversal<V, I, F> where V : IVertex<I>, F : ITraversalGuardState {

    private var pathHead = PathNode(left = null, vertex = startVertex)

    private val visited: HashSet<I> = HashSet()

    override fun moveNext(guardState: F): V? {
        visited.add(pathHead.vertex.id)

        val nextAdjacentStep = nextVertexOrNull(pathHead.vertex, guardState)
        if (nextAdjacentStep != null) {
            pathHead = PathNode(left = pathHead, vertex = nextAdjacentStep)
            return nextAdjacentStep
        } else {
            var current = pathHead.left
            while (current != null) {
                val next = nextVertexOrNull(current.vertex, guardState)
                if (next == null) {
                    current = current.left
                } else {
                    pathHead = PathNode(left = pathHead, vertex = next)
                    return next
                }
            }
        }

        return null
    }

    private fun nextVertexOrNull(vertex : V, flags: F): V? {
        val sortedEdges = graph.getOutgoingEdgesSorted(vertex)

        if (sortedEdges == null || sortedEdges.isEmpty()) {
            return null
        }

        for (i in sortedEdges.indices) {
            val edge = sortedEdges[i]

            if (visited.contains(edge.to)) {
                continue
            }

            if (!edge.canProceed(flags)) {
                continue
            }

            return graph.getVertex(edge.to)
        }

        return null
    }

    override fun currentStep(): V {
        return pathHead.vertex
    }

    override fun movePrevious(): V? {
        val left = pathHead.left ?: return null
        visited.remove(pathHead.vertex.id)
        visited.remove(left.vertex.id)
        pathHead = left

        return left.vertex
    }

    override fun reset(): V {
        visited.clear()
        pathHead = PathNode(left = null, vertex = startVertex)

        return startVertex
    }

    override fun tracePath(): List<V> {
        val path = ArrayList<V>()
        var current: PathNode<V>? = pathHead
        while (current != null) {
            path.add(current.vertex)
            current = current.left
        }
        path.reverse()
        return path
    }
}