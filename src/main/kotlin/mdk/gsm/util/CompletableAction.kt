package mdk.gsm.util

import kotlinx.coroutines.CompletableDeferred
import mdk.gsm.graph.IVertex
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.traverser.TraversalState

internal data class CompletableAction<V, I, A>(
    val action: GraphStateMachineAction<A>,
    val deferred: CompletableDeferred<TraversalState<V, I, A>>
) where V : IVertex<I>