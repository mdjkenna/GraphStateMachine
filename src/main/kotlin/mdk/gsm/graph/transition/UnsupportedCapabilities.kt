package mdk.gsm.graph.transition

import mdk.gsm.graph.IVertex
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.util.GsmException

internal class UnsupportedPrevious<V, I, F, A> : IPreviousTransition<V, I, F, A>
        where V : IVertex<I>, F : ITransitionGuardState {
    override suspend fun movePrevious() = throw GsmException.PreviousActionUnsupported()
}

internal class UnsupportedResettable<V> : IResettable<V> {
    override fun reset(): V = throw GsmException.ResetActionUnsupported()
}

internal class UnsupportedPathTraceable<V> : IPathTraceable<V> {
    override fun tracePath(): List<V> = throw GsmException.TracePathUnsupported()
}

