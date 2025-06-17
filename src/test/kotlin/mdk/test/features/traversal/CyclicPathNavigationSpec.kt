package mdk.test.features.traversal

import io.kotest.core.spec.style.BehaviorSpec
import mdk.gsm.graph.transition.traversal.EdgeTraversalType
import mdk.gsm.state.GraphStateMachineAction
import mdk.test.utils.AssertionUtils.assertTracedPathWithCurrentState
import mdk.test.utils.Test15VertexTransitionArgs
import mdk.test.utils.TestBuilderUtils

class CyclicPathNavigationSpec : BehaviorSpec({

    Given("A state machine with a graph containing multiple cyclic paths and cyclic DFS traversal") {

        val transitionGuardState = Test15VertexTransitionArgs()
        val traverser = TestBuilderUtils.build15VertexGraphStateMachine(
            transitionGuardState = transitionGuardState,
            edgeTraversalType = EdgeTraversalType.DFSCyclic,
        )

        var expectedPath = "1, 2, 4, 6, 3, 2, 4, 6, 3, 2, 4, 6, 3".split(", ")

        When("The state machine has multiple NEXT actions dispatched that take it to a vertex with the first edge exploring a cyclic path which is unblocked") {
            repeat(12) {
                traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            }

            Then("The traced path should contain multiple loops around the cycle") {
                assertTracedPathWithCurrentState(
                    expectedPath = expectedPath,
                    traverser = traverser
                )
            }
        }

        When("A traversal guard breaks the cycle's infinite loop") {
            transitionGuardState.blockedFrom3To2 = true

            repeat(14) {
                traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            }

            Then("GSM transitions across the edge immediately after the blocked one and continues through the graph to another cycle") {
                expectedPath += ("7, 8, 9, 11, 12, 14, 5, 8, 9, 11, 12, 14, 5, 8".split(", "))

                assertTracedPathWithCurrentState(
                    expectedPath = expectedPath,
                    traverser = traverser
                )
            }
        }

        When("A traversal guard brakes the second loop encountered after the first loop was exited") {

            transitionGuardState.blockedFrom8To9 = true

            repeat(2) {
                traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            }


            Then("The state machines transitions skip the blocked edge and traverses the edge immediately after") {
                expectedPath += "10, 11".split(", ")

                assertTracedPathWithCurrentState(
                    expectedPath = expectedPath,
                    traverser = traverser
                )
            }
        }

        When("There are a series of PREVIOUS actions received by the state machine") {

            for (i in expectedPath.lastIndex downTo 0) {
                Then("The previous state is becomes the current one, with the last element removed from the traced path: $i") {
                    assertTracedPathWithCurrentState(
                        expectedPath = expectedPath.slice(0..i),
                        traverser = traverser
                    )

                    traverser.dispatchAndAwaitResult(GraphStateMachineAction.Previous)
                }
            }
        }
    }
})
