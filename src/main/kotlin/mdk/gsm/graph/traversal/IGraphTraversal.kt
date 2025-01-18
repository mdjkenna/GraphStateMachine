package mdk.gsm.graph.traversal

import mdk.gsm.graph.IVertex
import mdk.gsm.state.IEdgeTransitionFlags

internal interface IGraphTraversal<V, I, F> where V : IVertex<I>, F : IEdgeTransitionFlags {
    fun moveNext(flags: F) : V?
    fun currentStep(): V
    fun movePrevious(): V?
    fun reset(): V
    fun tracePath(): List<V>
}


