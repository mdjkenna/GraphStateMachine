package mdk.test.features.traversal

import io.kotest.core.spec.style.BehaviorSpec
import mdk.gsm.graph.transition.traversal.EdgeTraversalType
import mdk.gsm.state.GraphStateMachineAction
import mdk.test.utils.AssertionUtils
import mdk.test.utils.TestBuilderUtils
import mdk.test.utils.TestTransitionGuardState

class ConditionalPathSelectionSpec : BehaviorSpec({
    val guardState = TestTransitionGuardState()
    val traverser = TestBuilderUtils.build8VertexGraphStateMachine(
        testProgressionFlags = guardState,
        edgeTraversalType = EdgeTraversalType.DFSAcyclic
    )

    Given("A cyclic graph state machine with traversal guard conditions which evaluate to block traversal along a model 8 vertex graph") {

        When("Traversal along particular paths is blocked by traversal guard condition evaluations") {

            guardState.blockedGoingTo2 = true
            guardState.blockedGoingTo7 = true

            Then("Traversal should not occur along the blocked path but instead traverse along the remaining paths which are not blocked") {
                AssertionUtils.assertExpectedPathGoingNextUntilEnd(
                    traverser,
                    listOf("1", "3", "5", "6")
                )

                AssertionUtils.assertTracedPathWithCurrentState(
                    expectedPath = listOf("1", "3", "5", "6"),
                    traverser = traverser
                )
            }
        }

        When("Traversal along a different path is blocked by traversal guard condition evaluation") {
            traverser.dispatchAndAwaitResult(GraphStateMachineAction.Reset)

            guardState.blockedGoingTo3 = true

            Then("The other paths which were previously blocked should still be traversed, however the newly blocked transition is prevented") {

                AssertionUtils.assertExpectedPathGoingNextUntilEnd(
                    expectedPath = listOf("1", "2", "4", "8"),
                    traverser = traverser
                )

                AssertionUtils.assertTracedPathWithCurrentState(
                    expectedPath = listOf("1", "2", "4", "8"),
                    traverser = traverser
                )
            }
        }
    }
})
