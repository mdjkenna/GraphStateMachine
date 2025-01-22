package mdk.test.utils

import mdk.gsm.graph.IVertex
import mdk.gsm.state.GraphStateMachine
import mdk.gsm.state.ITraversalGuardState
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.map

object AssertionUtils {
    fun <V, I, F> expectPath(
        expectedPath : List<I>,
        graphStateMachine: GraphStateMachine<V, I, F>,
        messageOnFail : String = ""
    ) where V : IVertex<I>, F : ITraversalGuardState {
        expectThat(graphStateMachine.tracePath()).describedAs(messageOnFail)
            .map(IVertex<I>::id)
            .isEqualTo(expectedPath)

        expectThat(graphStateMachine.currentState.vertex.id)
            .isEqualTo(expectedPath.last())
    }

    inline fun withMessageOnFail(
        crossinline messageOnFail : () -> String,
        crossinline test : () -> Unit
    ) {

        val testResult = runCatching {
            test()
        }

        if (testResult.isFailure) {
            throw Throwable(messageOnFail(), testResult.exceptionOrNull())
        }
    }
}