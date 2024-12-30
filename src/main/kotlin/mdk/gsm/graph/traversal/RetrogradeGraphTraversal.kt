package mdk.gsm.graph.traversal

import mdk.gsm.graph.IVertex
import mdk.gsm.graph.Graph
import mdk.gsm.state.IEdgeTransitionFlags

internal class RetrogradeGraphTraversal<V, F>(
    graph: Graph<V, F>,
    startVertex: V
) : GraphTraversal<V, F>(startVertex, graph) where V : IVertex, F : IEdgeTransitionFlags {

    override fun next(flags: F): V? {

        visited[pathHead.vertex.id] = VtxState.GRAY_INITIAL

        val nextAdjacentStep = pathHead.vertex.nextVertexOrNull(flags)
        if (nextAdjacentStep != null) {
            pathHead = PathNode(left = pathHead, vertex = nextAdjacentStep)
            return nextAdjacentStep
        } else {
            var current = pathHead.left
            while (current != null) {
                val next = current.vertex.nextVertexOrNull(flags)
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

    private fun V.nextVertexOrNull(
        flags : F
    ): V? {
        val sortedEdges = graph.getOutgoingEdgesSorted(this) ?: emptyList()
        sortedEdges.ifEmpty { null }
            ?: return null

        val nextEdge = sortedEdges.firstOrNull { edge ->
            var result = !visited.contains(edge.to)
            if (result) {
                val to = graph.getVertex(edge.to)
                result = to != null && edge.canProceed(flags)
            }

            result
        }

        return if (nextEdge != null) {
            graph.getVertex(nextEdge.to)
        } else {
            null
        }
    }
}

