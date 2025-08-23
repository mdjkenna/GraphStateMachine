package mdk.gsm.graph.transition

import mdk.gsm.graph.IVertex
import mdk.gsm.graph.VertexContainer
import mdk.gsm.graph.transition.traverse.TraversalPathNode
import mdk.gsm.state.ITransitionGuardState

internal interface IForwardTransition<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {
    suspend fun moveNext(guardState : F, autoAdvance: Boolean, args: A?): TraversalPathNode<V, A>?

    fun currentStep(): V

    fun getVertexContainer(id: I): VertexContainer<V, I, F, A>?

    fun head(): TraversalPathNode<V, A>
}

internal interface IPreviousTransition<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {
    suspend fun movePrevious(): TraversalPathNode<V, A>?
}

internal interface IResettable<V> {
    fun reset(): V
}

internal interface IPathTraceable<V> {
    fun tracePath(): List<V>
}

