package mdk.test.features.action

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import mdk.gsm.builder.buildWalkerWithActions
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.walker.Walker
import mdk.test.utils.TestTransitionGuardState
import mdk.test.utils.TestVertex

class WalkerActionWithArgumentsSpec : BehaviorSpec(
    body = {
        Given("A walker with a vertex that accepts arguments in the Next action") {
            val walker = buildWalkerGraph()

            When("The Next action is dispatched without an argument type") {

                val noArgsActionResult =
                    walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

                Then("Everything works as expected, it just goes next from vertex 1 to vertex 2") {
                    walker.current.value.vertex.id shouldBe "2"
                }

                Then("The first Next action result i.e. the first traversal state result should not have any arguments") {
                    noArgsActionResult.args shouldBe null
                }
            }

            When("The Next action is dispatched with an argument value specifying a particular edge with the traversal guard") {
                val argsActionResult = walker.dispatchAndAwaitResult(
                    GraphStateMachineAction.NextArgs(
                        WalkerTestArgs(WalkerTestArgs.ARGS_GO_TO_4)
                    )
                )

                Then("""
                    The argument is correctly interpreted as only traversing the edge with the specified argument value going from vertex 2 to vertex 4, 
                    and not going to vertex 3 due to the action argument check in the traversal guard.
                    Vertex 4 then autoadvances to vertex 5 due to the action argument in onBeforeVisit and so the current state is vertex 5
                    """) {
                    walker.current.value.vertex.id shouldBe "5"
                }

                Then("The next argument result i.e. the traversal state result has the same argument as the next action which generated the result") {
                    argsActionResult.args shouldBe WalkerTestArgs(WalkerTestArgs.ARGS_GO_TO_4)
                }
            }

            When("A Reset action is received after moving through an intermediate state with an argument value") {
                val resetActionResult = walker.dispatchAndAwaitResult(GraphStateMachineAction.Reset)

                Then("The current state is the start vertex (vertex 1)") {
                    walker.current.value.vertex.id shouldBe "1"
                }

                Then("The reset action result should not have any arguments associated with it") {
                    resetActionResult.args shouldBe null
                }
            }

            When("A next action is received with an argument for vertex 3") {
                // First move to vertex 2
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                
                val nextActionResult = walker.dispatchAndAwaitResult(
                    GraphStateMachineAction.NextArgs(
                        WalkerTestArgs(WalkerTestArgs.ARGS_GO_TO_3)
                    )
                )

                Then("Vertex 4 is not visited unlike previously when the argument was for vertex 4, instead, the current state is for vertex 3") {
                    walker.current.value.vertex.id shouldBe "3"
                }

                Then("The next argument result i.e. the traversal state result has the same argument as the next action which generated the result") {
                    nextActionResult.args shouldBe WalkerTestArgs(WalkerTestArgs.ARGS_GO_TO_3)
                }
            }

            When("A next action is received with an argument for vertex 4, which has absolutely no effect on traversal as it is not relevant to the state machine's construction") {
                val nextActionResult = walker.dispatchAndAwaitResult(
                    GraphStateMachineAction.NextArgs(
                        WalkerTestArgs(WalkerTestArgs.ARGS_GO_TO_4)
                    )
                )

                Then("The current state is vertex 6, as this is the destination of the only outgoing edge that vertex 3 has") {
                    walker.current.value.vertex.id shouldBe "6"
                }

                Then("The next argument result i.e. the traversal state result has the same argument as the next action which generated the result") {
                    nextActionResult.args shouldBe WalkerTestArgs(WalkerTestArgs.ARGS_GO_TO_4)
                }
            }

            When("A next action is received when on a terminal vertex (vertex 6)") {
                val nextActionResult = walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

                Then("The next action result should be out of bounds - beyond last") {
                    nextActionResult.isBeyondLast shouldBe true
                }

                Then("The next action result should not have any arguments associated with it, as none were received when the state machine moved forward into this state originally") {
                    nextActionResult.args shouldBe null
                }
            }
        }
    }
)

data class WalkerTestArgs(val id: Int) {
    companion object {
        const val ARGS_GO_TO_3 = 3
        const val ARGS_GO_TO_4 = 4
    }
}

private fun buildWalkerGraph() : Walker<TestVertex, String, TestTransitionGuardState, WalkerTestArgs> {

    return buildWalkerWithActions(guardState = TestTransitionGuardState()) {
        val v1 = TestVertex("1")
        val v2 = TestVertex("2")
        val v3 = TestVertex("3")
        val v4 = TestVertex("4")
        val v5 = TestVertex("5")
        val v6 = TestVertex("6")

        setExplicitTransitionIntoBounds(true)

        buildGraph(v1) {
            addVertex(v1) {
                addEdge {
                    setTo(v2)
                }
            }

            addVertex(v2) {
                addEdge {
                    setTo(v3)
                    setEdgeTransitionGuard {
                        args != null && args.id == WalkerTestArgs.ARGS_GO_TO_3
                    }
                }

                addEdge {
                    setTo(v4)
                    setEdgeTransitionGuard {
                        args != null && args.id == WalkerTestArgs.ARGS_GO_TO_4
                    }
                }
            }

            addVertex(v4) {
                onBeforeVisit {
                    if (args != null && args.id == WalkerTestArgs.ARGS_GO_TO_4) {
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