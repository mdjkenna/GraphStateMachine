package mdk.gsm.graph

import mdk.gsm.state.ITraversalGuardState

@PublishedApi
internal class VertexContainer<V, I, F> internal constructor(
    val vertex: V,
    val adjacentOrdered: List<Edge<V, I, F>>
) where V : IVertex<I>, F : ITraversalGuardState