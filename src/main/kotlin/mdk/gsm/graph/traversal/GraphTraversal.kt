package mdk.gsm.graph.traversal

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.state.IEdgeTransitionFlags

internal abstract class GraphTraversal<V, F>(
    val startVertex: V,
    protected val graph: Graph<V, F>
) where V : IVertex, F : IEdgeTransitionFlags {

    protected var pathHead = PathNode(left = null, vertex = startVertex)

    protected val visited: MutableMap<String, Int> = run {
        val map = HashMap<String, Int>()
        map
    }

    abstract fun next(flags: F): V?

    fun currentStep(): V {
        return pathHead.vertex
    }

    fun movePrevious(): V? {
        val left = pathHead.left ?: return null

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