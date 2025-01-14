package mdk.gsm.graph.traversal

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.state.IEdgeTransitionFlags

internal interface IGraphTraversal<V, I, F> where V : IVertex<I>, F : IEdgeTransitionFlags {
    fun moveNext(flags: F) : V?
    fun currentStep(): V
    fun movePrevious(): V?
    fun reset(): V
    fun tracePath(): List<V>
}

internal class CyclicGraphTraversal<V, I, F>(
    private val graph: Graph<V, I, F>,
    private val startVertex: V,
)  : IGraphTraversal<V, I, F> where V : IVertex<I>, F : IEdgeTransitionFlags {

    private var pathHead = PathNode(left = null, vertex = startVertex)

    private val visited: HashMap<I, Int> = HashMap()

    override fun moveNext(flags: F): V? {
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

    private fun V.nextVertexOrNull(flags: F): V? {
        val sortedEdges = graph.getOutgoingEdgesSorted(this).orEmpty()

        val vtxState = visited.computeIfAbsent(this.id) { VtxState.GRAY_INITIAL }

        for (i in VtxState.currentEdgeOrZero(vtxState) until sortedEdges.size) {
            val nextEdge = sortedEdges[i]
            val toVtxState = visited[nextEdge.to]

            if (VtxState.isBlack(toVtxState)) {
                continue
            }

            if (!nextEdge.canProceed(flags)){
                continue
            }


            return graph.getVertex(nextEdge.to)
        }

        visited[this.id] = VtxState.BLACK
        return null
    }

    override fun currentStep(): V {
        return pathHead.vertex
    }

    override fun movePrevious(): V? {
        val left = pathHead.left
            ?: return null

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


internal class AcyclicTraversal<V, I, F>(
    private val graph: Graph<V, I, F>,
    private val startVertex: V
) : IGraphTraversal<V, I, F> where V : IVertex<I>, F : IEdgeTransitionFlags {

    private var pathHead = PathNode(left = null, vertex = startVertex)

    private val visited: HashMap<I, Int> = HashMap()

    override fun moveNext(flags: F): V? {
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

    private fun V.nextVertexOrNull(flags: F): V? {
        val sortedEdges = graph.getOutgoingEdgesSorted(this) ?: return null

        val nextEdge = sortedEdges.firstOrNull { edge ->
            !visited.contains(edge.to) && edge.canProceed(flags)
        }

        return nextEdge?.let { graph.getVertex(it.to) }
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


internal abstract class GraphTraversal<V, I, F>(
    val startVertex: V,
    protected val graph: Graph<V, I, F>
) where V : IVertex<I>, F : IEdgeTransitionFlags {

    protected var pathHead = PathNode(left = null, vertex = startVertex)

    protected val visited: HashMap<I, Int> = HashMap<I, Int>()

    abstract fun moveNext(flags: F): V?

    fun currentStep(): V {
        return pathHead.vertex
    }

    open fun movePrevious(): V? {
        val left = pathHead.left ?: return null

        visited.remove(pathHead.vertex.id)
        visited.remove(left.vertex.id)
        pathHead = left
        return left.vertex
    }

    fun reset() : V {
        visited.clear()
        pathHead = PathNode(left = null, vertex = startVertex)

        return startVertex
    }

    fun tracePath(): List<V> {
        val path = ArrayList<V>()

        var current : PathNode<V>? = pathHead
        while (current != null) {
            path.add(current.vertex)
            current = current.left
        }

        path.reverse()

        return path
    }

}