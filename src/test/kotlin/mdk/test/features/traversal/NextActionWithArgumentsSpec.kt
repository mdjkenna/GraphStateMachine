package mdk.test.features.traversal

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import mdk.gsm.builder.buildTraverserWithActions
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.traverser.Traverser
import mdk.test.utils.AssertionUtils
import mdk.test.utils.TestTransitionGuardState
import mdk.test.utils.TestVertex

class NextActionWithArgumentsSpec : BehaviorSpec(
    body = {
        Given("A graph state machine with a vertex that accepts arguments in the Next action") {
            val traverser = buildGraph()

            When("The Next action is dispatched without an argument type") {

                val noArgsActionResult =
                    traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)

                Then("Everything works as expected, it just goes next from vertex 1 to vertex 2") {
                    AssertionUtils.assertTracedPathWithCurrentState(
                        listOf("1", "2"),
                        traverser
                    )
                }

                Then("The first Next action result i.e. the first traversal state result should not have any arguments") {
                    noArgsActionResult.args shouldBe null
                }
            }

            When("The Next action is dispatched with an argument value specifying a particular edge with the traversal guard") {
                val argsActionResult = traverser.dispatchAndAwaitResult(
                    GraphStateMachineAction.NextArgs(
                        TestArgs(TestArgs.ARGS_GO_TO_4)
                    )
                )

                Then("""
                    The argument is correctly interpreted as only traversing the edge with the specified argument value going from vertex 2 to vertex 4, 
                    and not going to vertex 3 due to the action argument check in the traversal guard.
                    Vertex 4 then autoadvances to vertex 5 due to the action argument in onBeforeVisit and so the current state is vertex 5
                    """) {
                    AssertionUtils.assertTracedPathWithCurrentState(
                        listOf("1", "2", "4", "5"),
                        traverser
                    )
                }

                Then("The next argument result i.e. the traversal state result has the same argument as the next action which generated the result") {
                    argsActionResult.args shouldBe TestArgs(TestArgs.ARGS_GO_TO_4)
                }
            }

            When("A previous action is received after moving through an intermediate state with an argument value") {
                val previousActionResult = traverser.dispatchAndAwaitResult(GraphStateMachineAction.Previous)

                Then("The current state is vertex 2 which was the vertex that the next action with arguments was received on previously") {
                    AssertionUtils.assertTracedPathWithCurrentState(
                        listOf("1", "2"),
                        traverser
                    )
                }

                Then("The previous action result should not have any arguments associated with it, as none were received when the state machine moved forward into this state originally") {
                    previousActionResult.args shouldBe null
                }
            }

            When("A next action is received with an argument for vertex 3") {
                val nextActionResult = traverser.dispatchAndAwaitResult(
                    GraphStateMachineAction.NextArgs(
                        TestArgs(TestArgs.ARGS_GO_TO_3)
                    )
                )

                Then("Vertex 4 is not visited unlike previously when the argument was for vertex 4, instead, the current state is for vertex 3") {
                    AssertionUtils.assertTracedPathWithCurrentState(
                        listOf("1", "2", "3"),
                        traverser
                    )
                }

                Then("The next argument result i.e. the traversal state result has the same argument as the next action which generated the result") {
                    nextActionResult.args shouldBe TestArgs(TestArgs.ARGS_GO_TO_3)
                }
            }

            When("A next action is received with an argument for vertex 4, which has absolutely no effect on traversal as it is not relevant to the state machine's construction") {
                val nextActionResult = traverser.dispatchAndAwaitResult(
                    GraphStateMachineAction.NextArgs(
                        TestArgs(TestArgs.ARGS_GO_TO_4)
                    )
                )

                Then("The current state is vertex 6, as this is the destination of the only outgoing edge that vertex 3 has") {
                    AssertionUtils.assertTracedPathWithCurrentState(
                        listOf("1", "2", "3", "6"),
                        traverser
                    )
                }

                Then("The next argument result i.e. the traversal state result has the same argument as the next action which generated the result") {
                    nextActionResult.args shouldBe TestArgs(TestArgs.ARGS_GO_TO_4)
                }
            }

            When("A next action is received when on a terminal vertex (vertex 6)") {
                val nextActionResult = traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)

                Then("The next action result should be out of bounds - beyond last") {
                    nextActionResult.isBeyondLast shouldBe true
                }

                Then("The next action result should not have any arguments associated with it, as none were received when the state machine moved forward into this state originally") {
                    nextActionResult.args shouldBe null
                }
            }

            When("A previous action is received when beyond last with a previous in bounds state having received an argument value in the next action with arguments taht generated that state") {
                val previousActionResult = traverser.dispatchAndAwaitResult(GraphStateMachineAction.Previous)

                Then("The current state is now in bounds with the expected path") {
                    AssertionUtils.assertTracedPathWithCurrentState(
                        listOf("1", "2", "3", "6"),
                        traverser
                    )
                }

                Then("The previous action result has the arguments from the previous in bounds state, which was vertex 4 in this case") {
                    previousActionResult.args shouldBe TestArgs(TestArgs.ARGS_GO_TO_4)
                }
            }

            When("A previous action is received when the previous state received arguments as part of the next action that generated that state") {
                val previousActionResult = traverser.dispatchAndAwaitResult(GraphStateMachineAction.Previous)

                Then("The current state is now in bounds with the expected path") {
                    AssertionUtils.assertTracedPathWithCurrentState(
                        listOf("1", "2", "3"),
                        traverser
                    )
                }

                Then("The previous action result has the arguments from the previous in bounds state") {
                    previousActionResult.args shouldBe TestArgs(TestArgs.ARGS_GO_TO_3)
                }
            }
        }
    }
)


data class TestArgs(val id: Int) {
    companion object {
        const val ARGS_GO_TO_3 = 3
        const val ARGS_GO_TO_4 = 4
    }
}

private fun buildGraph() : Traverser<TestVertex, String, TestTransitionGuardState, TestArgs> {

    return buildTraverserWithActions(guardState = TestTransitionGuardState()) {
        val v1 = TestVertex("1")
        val v2 = TestVertex("2")
        val v3 = TestVertex("3")
        val v4 = TestVertex("4")
        val v5 = TestVertex("5")
        val v6 = TestVertex("6")

        setExplicitTransitionIntoBounds(true)

        buildGraph(startAtVertex = v1) {
            addVertex(v1) {
                addEdge {
                    setTo(v2)
                }
            }

            addVertex(v2) {
                addEdge {
                    setTo(v3)
                    setEdgeTransitionGuard {
                        args != null && args.id == TestArgs.ARGS_GO_TO_3
                    }
                }

                addEdge {
                    setTo(v4)
                    setEdgeTransitionGuard {
                        args != null && args.id == TestArgs.ARGS_GO_TO_4
                    }
                }
            }


            addVertex(v4) {
                onBeforeVisit {
                    if (args != null && args.id == TestArgs.ARGS_GO_TO_4) {
                        autoAdvance()
                    }
                }

                addEdge {
                    setTo(v5)
                }
            }

            addVertex(v3) {
                addEdge {
                    setTo(v6)
                }
            }

            addVertex(v5)
            addVertex(v6)
        }

    }
}
