package mdk.gsm.graph

import mdk.gsm.state.IEdgeTransitionFlags

@PublishedApi
internal class VertexContainer<V, F> internal constructor(
    val vertex: V,
    val adjacentOrdered: List<Edge<V, F>>
) where V : IVertex, F : IEdgeTransitionFlags