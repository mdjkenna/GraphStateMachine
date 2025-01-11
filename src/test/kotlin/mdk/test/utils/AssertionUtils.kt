package mdk.test.utils

import mdk.gsm.graph.IVertex
import mdk.gsm.state.GraphStateMachine
import mdk.gsm.state.IEdgeTransitionFlags
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object AssertionUtils {

    fun <V, I, F> expectPath(
        expectedPath : List<I>,
        graphStateMachine: GraphStateMachine<V, I, F>
    ) where V : IVertex<I>, F : IEdgeTransitionFlags {

        expectThat(graphStateMachine.tracePath().map(IVertex<I>::id))
            .isEqualTo(expectedPath)

        expectThat(graphStateMachine.currentState.vertex.id)
            .isEqualTo(expectedPath.last())
    }
}