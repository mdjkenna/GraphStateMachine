package mdk.gsm.graph

import mdk.gsm.state.BeforeVisitHandler
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.state.OutgoingTransitionHandler

@PublishedApi
internal class VertexContainer<V, I, F, A> internal constructor(
    val vertex: V,
    val adjacentOrdered: List<Edge<V, I, F, A>>,
    val beforeVisitHandler: BeforeVisitHandler<V, I, F, A>?,
    val outgoingTransitionHandler: OutgoingTransitionHandler<V, I, F, A>? = null
) where V : IVertex<I>, F : ITransitionGuardState
