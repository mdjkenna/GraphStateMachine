package mdk.gsm.graph.transition

import mdk.gsm.graph.IVertex
import mdk.gsm.state.ITransitionGuardState

internal data class TransitionCapabilities<V, I, F, A>(
    val forward: IForwardTransition<V, I, F, A>,
    val previous: IPreviousTransition<V, I, F, A>,
    val resettable: IResettable<V>,
    val pathTraceable: IPathTraceable<V>
) where V : IVertex<I>, F : ITransitionGuardState

