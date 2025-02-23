package mdk.test

import mdk.gsm.builder.buildGraphStateMachine
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.util.LongVertex
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@RunWith(Parameterized::class)
class TestExplicitlySteppingIntoBounds(
    private val explicitTransitionIntoBounds: Boolean?
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(true, false, null)
    }

    @Test
    fun `explicitly stepping into bounds behaves as expected`() {
        val graphStateMachine = buildGraphStateMachine<LongVertex, Long> {
            val v1 = LongVertex(1L)
            val v2 = LongVertex(2L)

            if (explicitTransitionIntoBounds != null) {
                setExplicitTransitionIntoBounds(explicitTransitionIntoBounds)
            }

            buildGraph(v1) {
                v(v1) { e { setTo(v2) } }
                v(v2)
            }
        }

        fun assertBounds(
            within: Boolean,
            beyond: Boolean,
            before: Boolean
        ) {
            expectThat(graphStateMachine.currentState) {
                get { isWithinBounds }.isEqualTo(within)
                get { isBeyondLast }.isEqualTo(beyond)
                get { isBeforeFirst }.isEqualTo(before)
                get { isNotBeforeFirst }.isEqualTo(!before)
                get { isNotBeyondLast }.isEqualTo(!beyond)
            }
        }

        assertBounds(
            within = true,
            beyond = false,
            before = false
        )

        graphStateMachine.dispatch(GraphStateMachineAction.Next)
        assertBounds(
            within = true,
            beyond = false,
            before = false
        )

        graphStateMachine.dispatch(GraphStateMachineAction.Next)
        assertBounds(
            within = false,
            beyond = true,
            before = false
        )

        graphStateMachine.dispatch(GraphStateMachineAction.Previous)
        expectThat(graphStateMachine.currentState) {
            get { isWithinBounds }.isEqualTo(true)
            get { isBeyondLast }.isEqualTo(false)
            get { vertex.id }.isEqualTo(if (explicitTransitionIntoBounds == true) 2L else 1L)
        }

        graphStateMachine.dispatch(GraphStateMachineAction.Previous)
        graphStateMachine.dispatch(GraphStateMachineAction.Previous)
        assertBounds(
            within = false,
            beyond = false,
            before = true
        )

        graphStateMachine.dispatch(GraphStateMachineAction.Next)
        expectThat(graphStateMachine.currentState.vertex.id)
            .isEqualTo(if (explicitTransitionIntoBounds == true) 1L else 2L)
    }
}