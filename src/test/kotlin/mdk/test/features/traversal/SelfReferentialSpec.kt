package mdk.test.features.traversal

import io.kotest.core.spec.style.BehaviorSpec
import mdk.gsm.builder.buildTraverser
import mdk.gsm.graph.transition.traversal.EdgeTraversalType
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.util.StringVertex
import mdk.test.utils.AssertionUtils.assertTracedPathWithCurrentState

class SelfReferentialSpec : BehaviorSpec({

    var cycleCount = 0
    val transitionGuardState = object : ITransitionGuardState {
        override fun onReset() { cycleCount = 0 }
    }

    Given("A graph state machine with a vertex that references itself and has a transition guard limiting to 7 cycles") {
        val traverser = buildTraverser<StringVertex, String, ITransitionGuardState>(transitionGuardState) {
            val v1 = StringVertex("1")
            val v2 = StringVertex("2")
            setTraversalType(EdgeTraversalType.DFSCyclic)

            buildGraph(v1) {
                v(v1) {
                    e {
                        setTo(v1)
                        setEdgeTransitionGuard {
                            if (cycleCount < 7) {
                                cycleCount++
                                true
                            } else {
                                false
                            }
                        }
                    }

                    e {
                        setTo(v2)
                    }
                }

                v(v2)
            }
        }

        When("Advancing the state machine forward seven times") {
            repeat(7) {
                traverser.dispatch(GraphStateMachineAction.Next)
            }

            Then("The traversal path should contain eight occurrences of vertex '1'") {
                assertTracedPathWithCurrentState(
                    expectedPath = List(8) { "1" },
                    traverser = traverser
                )
            }
        }

        When("Advancing once more after reaching the cycle guard limit") {
            traverser.dispatch(GraphStateMachineAction.Next)

            Then("The traversal should proceed to vertex '2' instead of continuing the cycle") {
                assertTracedPathWithCurrentState(
                    expectedPath = List(8) { "1" } + "2",
                    traverser = traverser
                )
            }
        }

        When("Reversing the state machine six steps backward") {
            repeat(6) {
                traverser.dispatch(GraphStateMachineAction.Previous)
            }

            Then("The path should be reduced to only three occurrences of vertex '1'") {
                assertTracedPathWithCurrentState(
                    expectedPath = List(3) { "1" },
                    traverser = traverser
                )
            }
        }

        When("Manually setting the cycle count to 2 and advancing six more times") {
            cycleCount = 2

            repeat(6) {
                traverser.dispatch(GraphStateMachineAction.Next)
            }

            Then("The path should contain eight '1' vertices followed by vertex '2'") {
                assertTracedPathWithCurrentState(
                    expectedPath = List(8) { "1" } + "2",
                    traverser = traverser
                )
            }
        }
    }
})
