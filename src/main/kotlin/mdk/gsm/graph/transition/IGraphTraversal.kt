package mdk.gsm.graph.transition

import mdk.gsm.graph.IVertex
import mdk.gsm.graph.VertexContainer
import mdk.gsm.graph.transition.traversal.TraversalPathNode
import mdk.gsm.state.ITransitionGuardState

internal interface IGraphTraversal<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {
    suspend fun moveNext(guardState: F, autoAdvance: Boolean, args: A?): TraversalPathNode<V, A>?
    fun currentStep(): V
    fun movePrevious(): TraversalPathNode<V, A>?
    fun reset(): V
    fun tracePath(): List<V>
    fun getVertexContainer(id: I): VertexContainer<V, I, F, A>?
    fun head() : TraversalPathNode<V, A>
}