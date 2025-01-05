package mdk.test

import mdk.gsm.graph.traversal.EdgeTraversalType
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.TraversalState
import mdk.test.utils.TestBuilderUtils
import mdk.test.utils.TestEdgeTransitionFlags
import mdk.test.utils.TestVertex
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class TestGraphStateMachineStateUpdatedListener {

    @Test
    fun `the on state updated listener should receive state updates including the current state when set unless specified otherwise`() {

        val updatedStates = ArrayList<TraversalState<TestVertex>>()

        val graphStateMachine = TestBuilderUtils.build8VertexGraphStateMachine(
            testProgressionFlags = TestEdgeTransitionFlags(),
            edgeTraversalType = EdgeTraversalType.ForwardCyclic,
        )

        graphStateMachine.setOnStateUpdatedListener { traversalState ->
            updatedStates.add(traversalState)
        }

        repeat(4) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        }

        expectThat(updatedStates) {
            get { size }.isEqualTo(5)
            get { get(0).vertex.id }.isEqualTo("1")
            get { get(1).vertex.id }.isEqualTo("2")
            get { get(2).vertex.id }.isEqualTo("4")
            get { get(3).vertex.id }.isEqualTo("8")
            get { get(4).vertex.id }.isEqualTo("3")
        }

        graphStateMachine.clearOnStateUpdatedListener()
        graphStateMachine.dispatch(GraphStateMachineAction.Reset)

        expectThat(updatedStates) {
            get { size }.isEqualTo(5)
            get { get(4).vertex.id }.isEqualTo("3")
        }

        updatedStates.clear()
        graphStateMachine.setOnStateUpdatedListener(readCurrent = false) { traversalStates ->
            updatedStates.add(traversalStates)
        }

        repeat(4) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        }

        expectThat(updatedStates) {
            get { size }.isEqualTo(4)
            get { get(0).vertex.id }.isEqualTo("2")
            get { get(1).vertex.id }.isEqualTo("4")
            get { get(2).vertex.id }.isEqualTo("8")
            get { get(3).vertex.id }.isEqualTo("3")
        }

    }
}