package mdk.test

import mdk.gsm.graph.traversal.EdgeTraversalType
import mdk.gsm.state.GraphStateMachineAction
import mdk.test.utils.AssertionUtils
import mdk.test.utils.Test15VertexTransitionFlags
import mdk.test.utils.TestBuilderUtils
import mdk.test.utils.TestEdgeTransitionFlags
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class TestCyclicGraphTraversal {

    @Test
    fun `graph with a cycle is traversed according to the edge traversal type as expected`() {

        val graphStateMachine = TestBuilderUtils.build8VertexGraphStateMachine(
            testProgressionFlags = TestEdgeTransitionFlags(),
            edgeTraversalType = EdgeTraversalType.ForwardCyclic,
            add7to3cycle = true
        )

        val testProgressionFlags = graphStateMachine.flags

        repeat(20) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        }

        var expectedPath = "1, 2, 4, 8, 3, 5, 7, 3, 5, 7, 3, 5, 7, 3, 5, 7, 3, 5, 7, 3, 5".split(", ")
        AssertionUtils.expectPath(
            expectedPath = expectedPath,
            graphStateMachine = graphStateMachine
        )

        testProgressionFlags.blockedGoingTo3 = true

        repeat(2) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        }

        expectedPath += listOf("7", "6")
        AssertionUtils.expectPath(
            expectedPath = expectedPath,
            graphStateMachine = graphStateMachine
        )

        repeat(20) {
            graphStateMachine.dispatch(GraphStateMachineAction.Previous)
        }

        AssertionUtils.expectPath(
            expectedPath = expectedPath.slice(0..2),
            graphStateMachine = graphStateMachine
        )

        repeat(3) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        }

        AssertionUtils.expectPath(
            expectedPath = expectedPath.slice(0..3),
            graphStateMachine = graphStateMachine
        )
    }

    @Test
    fun `large graph with multiple cycles traversed as expected`() {
        val graphStateMachine = TestBuilderUtils.build15VertexGraphStateMachine(
            testProgressionFlags = Test15VertexTransitionFlags(),
            edgeTraversalType = EdgeTraversalType.ForwardCyclic,
        )

        var expected = "1, 2, 4, 6, 3, 2, 4, 6, 3, 2, 4, 6, 3".split(", ")

        repeat(12) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        }

        AssertionUtils.expectPath(
            expectedPath = expected,
            graphStateMachine = graphStateMachine
        )

        graphStateMachine.flags.blockedFrom3To2 = true

        repeat(14) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        }

        expected += ("7, 8, 9, 11, 12, 14, 5, 8, 9, 11, 12, 14, 5, 8".split(", "))

        AssertionUtils.expectPath(
            expectedPath = expected,
            graphStateMachine = graphStateMachine
        )

        graphStateMachine.flags.blockedFrom8To9 = true

        repeat(2) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        }

        expected += "10, 11".split(", ")

        AssertionUtils.expectPath(
            expectedPath = expected,
            graphStateMachine = graphStateMachine
        )

        for (i in expected.lastIndex downTo 0) {
            AssertionUtils.expectPath(
                expectedPath = expected.slice(0..i),
                graphStateMachine = graphStateMachine
            )

            expectThat(graphStateMachine.currentState.isWithinBounds)
                .isTrue()

            graphStateMachine.dispatch(GraphStateMachineAction.Previous)
        }

        expectThat(graphStateMachine.currentState.isWithinBounds)
            .isFalse()
    }
}