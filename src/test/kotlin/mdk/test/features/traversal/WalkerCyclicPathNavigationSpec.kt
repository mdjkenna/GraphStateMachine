package mdk.test.features.traversal

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import mdk.gsm.builder.buildWalker
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.walker.Walker
import mdk.test.utils.Test15VertexTransitionArgs
import mdk.test.utils.TestVertex

class WalkerCyclicPathNavigationSpec : BehaviorSpec({

    Given("A walker with a graph containing multiple cyclic paths") {

        val transitionGuardState = Test15VertexTransitionArgs()
        val walker = build15VertexWalker(
            transitionGuardState = transitionGuardState
        )

        When("The walker has multiple NEXT actions dispatched that take it through a cyclic path which is unblocked") {
            // First cycle: 1 -> 2 -> 4 -> 6 -> 3 -> 2 (repeat)

            // Move to vertex 2
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 4
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 6
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 3
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 2 (completing the cycle)
            val result = walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            Then("The walker should have navigated through the cycle and returned to vertex 2") {
                result.vertex.id shouldBe "2"
            }
        }

        When("The walker continues through the cycle multiple times") {
            // Continue from vertex 2 to vertex 4
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 6
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 3
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 2 (completing the cycle again)
            val result = walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            Then("The walker should have navigated through the cycle again and returned to vertex 2") {
                result.vertex.id shouldBe "2"
            }
        }

        When("A traversal guard breaks the cycle's infinite loop") {
            transitionGuardState.blockedFrom3To2 = true

            // Continue from vertex 2 to vertex 4
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 6
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 3
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Try to move to vertex 2, but it's blocked, so move to vertex 7 instead
            val result = walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            Then("The walker should have broken out of the cycle and moved to vertex 7") {
                result.vertex.id shouldBe "7"
            }
        }

        When("The walker navigates to another cycle") {
            // Continue from vertex 7 to vertex 8
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 9
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 11
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 12
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 14
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 5
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            // Move to vertex 8 (completing the second cycle)
            val result = walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            Then("The walker should have navigated through the second cycle and returned to vertex 8") {
                result.vertex.id shouldBe "8"
            }
        }

        When("A traversal guard breaks the second cycle") {
            transitionGuardState.blockedFrom8To9 = true

            // Try to move to vertex 9, but it's blocked, so move to vertex 10 instead
            val result = walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            Then("The walker should have broken out of the second cycle and moved to vertex 10") {
                result.vertex.id shouldBe "10"
            }
        }

        When("The walker is reset") {
            val result = walker.dispatchAndAwaitResult(GraphStateMachineAction.Reset)

            Then("The walker should return to the start vertex") {
                result.vertex.id shouldBe "1"
            }
        }
    }
}) {
    companion object {
        fun build15VertexWalker(
            transitionGuardState: Test15VertexTransitionArgs
        ): Walker<TestVertex, String, Test15VertexTransitionArgs, Nothing> {
            return buildWalker(transitionGuardState) {
                val v1 = TestVertex("1")
                val v2 = TestVertex("2")
                val v3 = TestVertex("3")
                val v4 = TestVertex("4")
                val v5 = TestVertex("5")
                val v6 = TestVertex("6")
                val v7 = TestVertex("7")
                val v8 = TestVertex("8")
                val v9 = TestVertex("9")
                val v10 = TestVertex("10")
                val v11 = TestVertex("11")
                val v12 = TestVertex("12")
                val v13 = TestVertex("13")
                val v14 = TestVertex("14")
                val v15 = TestVertex("15")

                buildGraph(v1) {
                    addVertex(v1) {
                        addEdge {
                            setTo(v2)
                        }
                        addEdge {
                            setTo(v3)
                        }
                    }

                    addVertex(v2) {
                        addEdge {
                            setTo(v4)
                        }
                    }

                    addVertex(v3) {
                        addEdge {
                            setTo(v2)
                            setEdgeTransitionGuard {
                                !guardState.blockedFrom3To2
                            }
                        }

                        addEdge {
                            setTo(v7)
                        }

                        addEdge {
                            setTo(v5)
                        }
                    }

                    addVertex(v4) {
                        addEdge {
                            setTo(v6)
                        }
                    }

                    addVertex(v5) {
                        addEdge {
                            setTo(v8)
                        }
                    }

                    addVertex(v6) {
                        addEdge {
                            setTo(v3)
                        }
                        addEdge {
                            setTo(v8)
                        }
                    }

                    addVertex(v7) {
                        addEdge {
                            setTo(v8)
                        }
                    }

                    addVertex(v8) {
                        addEdge {
                            setTo(v9)
                            setEdgeTransitionGuard {
                                !guardState.blockedFrom8To9
                            }
                        }
                        addEdge {
                            setTo(v10)
                        }
                    }

                    addVertex(v9) {
                        addEdge {
                            setTo(v11)
                        }
                    }

                    addVertex(v10) {
                        addEdge {
                            setTo(v11)
                        }
                    }

                    addVertex(v11) {
                        addEdge {
                            setTo(v12)
                        }
                        addEdge {
                            setTo(v13)
                        }
                    }

                    addVertex(v12) {
                        addEdge {
                            setTo(v14)
                        }
                    }

                    addVertex(v13) {
                        addEdge {
                            setTo(v14)
                        }
                    }

                    addVertex(v14) {
                        addEdge {
                            setTo(v5)
                        }
                        addEdge {
                            setTo(v15)
                        }
                    }

                    addVertex(v15) {
                        addEdge {
                            setTo(v2)
                        }
                    }
                }
            }
        }
    }
}
