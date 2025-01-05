package mdk.test

import mdk.gsm.graph.traversal.EdgeTraversalType
import mdk.gsm.state.GraphStateMachine
import mdk.gsm.state.GraphStateMachineAction
import mdk.test.utils.AssertionUtils.expectPath
import mdk.test.utils.TestBuilderUtils
import mdk.test.utils.TestBuilderUtils.v2
import mdk.test.utils.TestBuilderUtils.v3
import mdk.test.utils.TestBuilderUtils.v5
import mdk.test.utils.TestBuilderUtils.v6
import mdk.test.utils.TestEdgeTransitionFlags
import mdk.test.utils.TestVertex
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

@RunWith(Parameterized::class)
class TestAcyclicGraphTraversal(
    val conditionalTraversalTestParameters: ConditionalTraversalTestParameters
) {

    class ConditionalTraversalTestParameters(
        val graphStateMachine: GraphStateMachine<TestVertex, TestEdgeTransitionFlags>,
        val edgeTraversalType: EdgeTraversalType
    ) {
        override fun toString(): String {
            return "ConditionalTraversalTestParameters(edgeTraversalType=$edgeTraversalType)"
        }
    }


    @Before
    fun resetParameters() {
        conditionalTraversalTestParameters.graphStateMachine.dispatch(
            GraphStateMachineAction.Reset
        )

        conditionalTraversalTestParameters.graphStateMachine.flags.reset()
    }

    @Test
    fun `graph edges are visited conditionally as expected according to the edge traversal type`() {
        val testProgressionFlags = conditionalTraversalTestParameters.graphStateMachine.flags
        val graphStateMachine = conditionalTraversalTestParameters.graphStateMachine

        testProgressionFlags.blockedGoingTo2 = true
        testProgressionFlags.blockedGoingTo7 = true

        do {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        } while (graphStateMachine.currentState.isWithinBounds)

        expectPath(
            expectedPath = listOf("1", "3", "5", "6"),
            graphStateMachine = graphStateMachine
        )

        graphStateMachine.dispatch(GraphStateMachineAction.Reset)

        testProgressionFlags.blockedGoingTo3 = true
        testProgressionFlags.blockedGoingTo7 = false
        testProgressionFlags.blockedGoingTo2 = false

        do {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        } while (graphStateMachine.currentState.isWithinBounds)

        expectPath(
            expectedPath = listOf("1", "2", "4", "8"),
            graphStateMachine = graphStateMachine
        )
    }

    @Test
    fun `different traversal types show expected differences in behaviour surrounding edge conditional evaluation`() {
        val testProgressionFlags = conditionalTraversalTestParameters.graphStateMachine.flags
        val graphStateMachine = conditionalTraversalTestParameters.graphStateMachine
        testProgressionFlags.blockedGoingTo2 = true
        testProgressionFlags.blockedGoingTo7 = true

        for (step in listOf(v3, v5, v6)) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
            val nextStep = graphStateMachine.currentState.vertex
            expectThat(nextStep.id).isEqualTo(step.id)
        }

        testProgressionFlags.blockedGoingTo2 = false
        graphStateMachine.dispatch(GraphStateMachineAction.Next)
        var expectedNextStep = when (conditionalTraversalTestParameters.edgeTraversalType) {
            EdgeTraversalType.RetrogradeAcyclic -> v2
            else -> v6
        }

        expectThat(expectedNextStep.id)
            .isEqualTo(
                graphStateMachine.currentState.vertex.id
            )

        graphStateMachine.dispatch(GraphStateMachineAction.Reset)
        testProgressionFlags.blockedGoingTo2 = true
        testProgressionFlags.blockedGoingTo5 = true
        testProgressionFlags.blockedGoingTo7 = true

        for (step in listOf(v3, v6)) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
            val nextStep = graphStateMachine.currentState.vertex
            expectThat(nextStep.id)
                .isEqualTo(step.id)
        }

        repeat(10) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)

            expectThat(graphStateMachine.currentState.vertex.id)
                .isEqualTo(v6.id)

            expectThat(graphStateMachine.currentState.isNotBeforeFirst)
                .isTrue()

            expectThat(graphStateMachine.currentState.isNotBeyondLast)
                .isFalse()
        }
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: test with {0}")
        fun data(): Collection<Array<Any>> {

            return buildList {
                EdgeTraversalType.entries.forEach { edgeTraversalType ->
                    add(
                        arrayOf(
                            ConditionalTraversalTestParameters(
                                TestBuilderUtils.build8VertexGraphStateMachine(
                                    testProgressionFlags = TestEdgeTransitionFlags(),
                                    edgeTraversalType = edgeTraversalType
                                ),
                                edgeTraversalType
                            )
                        )
                    )
                }
            }
        }
    }
}

