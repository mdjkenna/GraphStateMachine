package mdk.gsm.graph.traversal

import mdk.gsm.graph.IVertex
import mdk.gsm.state.ITraversalGuardState

internal interface IGraphTraversal<V, I, F> where V : IVertex<I>, F : ITraversalGuardState {
    fun moveNext(guardState: F) : V?
    fun currentStep(): V
    fun movePrevious(): V?
    fun reset(): V
    fun tracePath(): List<V>
}


