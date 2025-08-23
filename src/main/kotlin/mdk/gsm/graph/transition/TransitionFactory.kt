package mdk.gsm.graph.transition

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.transition.traverse.AcyclicDfsTraversal
import mdk.gsm.graph.transition.traverse.CyclicDfsGraphTraversal
import mdk.gsm.graph.transition.traverse.EdgeTraversalType
import mdk.gsm.graph.transition.walk.StatelessGraphWalk
import mdk.gsm.state.ITransitionGuardState

internal object TransitionFactory {
    fun <V, I, F, A> create(
        graph: Graph<V, I, F, A>,
        startVertex: V,
        useStatelessWalk: Boolean,
        traversalType: EdgeTraversalType = EdgeTraversalType.DFSAcyclic
    ): TransitionCapabilities<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {
        return if (useStatelessWalk) {
            val engine = StatelessGraphWalk<V, I, F, A>(graph, startVertex)
            TransitionCapabilities(
                forward = engine,
                previous = UnsupportedPrevious(),
                resettable = engine,
                pathTraceable = UnsupportedPathTraceable()
            )
        } else if (traversalType == EdgeTraversalType.DFSCyclic) {
            val engine = CyclicDfsGraphTraversal<V, I, F, A>(graph, startVertex)
            TransitionCapabilities(
                forward = engine,
                previous = engine,
                resettable = engine,
                pathTraceable = engine
            )
        } else {
            val engine = AcyclicDfsTraversal<V, I, F, A>(graph, startVertex)
            TransitionCapabilities(
                forward = engine,
                previous = engine,
                resettable = engine,
                pathTraceable = engine
            )
        }
    }
}