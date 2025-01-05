package mdk.test.utils

import mdk.gsm.graph.IVertex
import mdk.gsm.state.GraphStateMachine
import mdk.gsm.state.IEdgeTransitionFlags
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object AssertionUtils {

    fun <V, F> expectPath(
        expectedPath : List<String>,
        graphStateMachine: GraphStateMachine<V, F>
    ) where V : IVertex, F : IEdgeTransitionFlags {

        expectThat(graphStateMachine.tracePath().map(IVertex::id))
            .isEqualTo(expectedPath)

        expectThat(graphStateMachine.currentState.vertex.id)
            .isEqualTo(expectedPath.last())
    }
}