package mdk.gsm.graph.traversal

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.state.IEdgeTransitionFlags

internal class ForwardGraphTraversal<V, F>(
    graph: Graph<V, F>,
    startVertex: V,
    val skipCycles : Boolean = true
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
        val vtxState = visited.computeIfAbsent(this.id) {
            VtxState.GRAY_INITIAL
        }

        if (VtxState.isBlack(vtxState)) {
            return null
        }

        for (i in VtxState.currentEdgeOrZero(vtxState) until sortedEdges.size) {
            val nextEdge = sortedEdges[i]

            val toVtx = visited[nextEdge.to]

            if (skipCycles) {
                if (!VtxState.isWhite(toVtx)) {
                    continue
                }
            } else {
                if (VtxState.isBlack(toVtx)) {
                    continue
                }
            }

            if (!nextEdge.canProceed(flags)) {
                continue
            }

            visited[this.id] = i + 1

            return graph.getVertex(nextEdge.to)
        }

        visited[this.id] = VtxState.BLACK

        return null
    }
}
