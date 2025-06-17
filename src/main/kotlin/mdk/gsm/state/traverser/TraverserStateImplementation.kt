package mdk.gsm.state.traverser

import kotlinx.coroutines.flow.StateFlow
import mdk.gsm.graph.IVertex
import mdk.gsm.state.GsmController
import mdk.gsm.state.ITransitionGuardState


internal class TraverserStateImplementation<V, I, F, A> private constructor(
    val gsm: GsmController<V, I, F, A>
) : TraverserState<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {

    override val current: StateFlow<TraversalState<V, I, A>>
        get() = gsm.stateOut

    override fun tracePath(): List<V> {
        return gsm.tracePath()
    }

    companion object {
        internal fun <V, I, F, A> create(
            gsm: GsmController<V, I, F, A>
        ): TraverserStateImplementation<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {
            return TraverserStateImplementation(gsm)
        }
    }
}
