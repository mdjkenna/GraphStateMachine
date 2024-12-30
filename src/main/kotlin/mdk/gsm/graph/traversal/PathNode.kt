package mdk.gsm.graph.traversal

import mdk.gsm.graph.IVertex

internal class PathNode<N>(
    val left: PathNode<N>?,
    val vertex: N
) where N : IVertex {
    override fun toString(): String {
        return "PathNode(vertex=${vertex.id}, left=${left?.vertex?.id})"
    }
}