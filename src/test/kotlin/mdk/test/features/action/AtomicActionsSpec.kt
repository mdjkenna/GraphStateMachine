package mdk.test.features.action

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import mdk.gsm.builder.DispatcherConfig
import mdk.gsm.builder.buildTraverserWithActions
import mdk.gsm.builder.buildWalkerWithActions
import mdk.gsm.state.GraphStateMachineAction
import mdk.test.utils.TestTransitionGuardState
import mdk.test.utils.TestVertex

// Note: This test file tests atomic action processing with delays, so inline graph building is required
class AtomicActionsSpec : BehaviorSpec(
    body = {
        Given("A traverser with delayed traversal guards testing action atomicity") {
            val traverser = buildTraverserWithActions<TestVertex, String, TestTransitionGuardState, Int>(guardState = TestTransitionGuardState()) {
                val v1 = TestVertex("1")
                val v2 = TestVertex("2")
                val v3 = TestVertex("3")
                val v5 = TestVertex("5")

                buildGraph(startAtVertex = v1) {
                    addVertex(v1) {
                        addEdge {
                            setTo(v2)
                            setEdgeTransitionGuard {
                                delay(500)
                                true
                            }
                        }
                    }
                    addVertex(v2) {
                        addEdge {
                            setTo(v3)
                            setEdgeTransitionGuard {
                                delay(500)
                                true
                            }
                        }
                    }
                    addVertex(v3) {
                        addEdge {
                            setTo(v5)
                            setEdgeTransitionGuard {
                                delay(500)
                                true
                            }
                        }
                    }
                    addVertex(v5)
                }
            }

            When("Multiple numbered actions are dispatched in sequence") {
                traverser.launchDispatch(GraphStateMachineAction.NextArgs(1))
                traverser.launchDispatch(GraphStateMachineAction.NextArgs(2))
                traverser.launchDispatch(GraphStateMachineAction.NextArgs(3))

                withTimeout(3000) {
                    while (traverser.current.value.vertex.id != "5") {
                        delay(10)
                    }
                }

                Then("Actions are processed atomically with proper vertex transitions and argument passing") {
                    val tracedPath = traverser.tracePath()
                    tracedPath.map { it.id } shouldBe listOf("1", "2", "3", "5")

                    traverser.current.value.vertex.id shouldBe "5"
                    traverser.current.value.args shouldBe 3
                }
            }
        }

        Given("A traverser with multiple delays for testing sequential dispatch") {
            val traverser = buildTraverserWithActions<TestVertex, String, TestTransitionGuardState, Int>(guardState = TestTransitionGuardState()) {
                val v1 = TestVertex("1")
                val v2 = TestVertex("2")
                val v3 = TestVertex("3")
                val v5 = TestVertex("5")

                buildGraph(startAtVertex = v1) {
                    addVertex(v1) {
                        addEdge {
                            setTo(v2)
                            setEdgeTransitionGuard {
                                delay(500)
                                true
                            }
                        }
                    }
                    addVertex(v2) {
                        addEdge {
                            setTo(v3)
                            setEdgeTransitionGuard {
                                delay(500)
                                true
                            }
                        }
                    }
                    addVertex(v3) {
                        addEdge {
                            setTo(v5)
                            setEdgeTransitionGuard {
                                delay(500)
                                true
                            }
                        }
                    }
                    addVertex(v5)
                }
            }

            When("Actions are dispatched with result awaited") {
                val result1 = traverser.dispatchAndAwaitResult(GraphStateMachineAction.NextArgs(10))
                result1.vertex.id shouldBe "2"
                result1.args shouldBe 10

                val result2 = traverser.dispatchAndAwaitResult(GraphStateMachineAction.NextArgs(20))
                result2.vertex.id shouldBe "3"
                result2.args shouldBe 20

                val result3 = traverser.dispatchAndAwaitResult(GraphStateMachineAction.NextArgs(30))
                result3.vertex.id shouldBe "5"
                result3.args shouldBe 30

                Then("The sequence of transitions is as expected") {
                    val tracedPath = traverser.tracePath().map { it.id }
                    tracedPath shouldBe listOf("1", "2", "3", "5")

                    traverser.current.value.vertex.id shouldBe "5"
                    traverser.current.value.args shouldBe 30
                }
            }
        }

        Given("A walker with a conflated channel for testing conflation") {
            val dispatcherConfig = DispatcherConfig<Int>(capacity = Channel.CONFLATED)
            val walker = buildWalkerWithActions(
                guardState = TestTransitionGuardState(),
                dispatcherConfig = dispatcherConfig
            ) {
                val v1 = TestVertex("1")
                val v2 = TestVertex("2")
                val v3 = TestVertex("3")
                val v5 = TestVertex("5")

                buildGraph(startAtVertex = v1) {
                    addVertex(v1) {
                        addEdge {
                            setTo(v2)
                            setEdgeTransitionGuard { true }
                        }
                    }
                    addVertex(v2) {
                        addEdge {
                            setTo(v3)
                            setEdgeTransitionGuard { true }
                        }
                    }
                    addVertex(v3) {
                        addEdge {
                            setTo(v5)
                            setEdgeTransitionGuard { true }
                        }
                    }
                    addVertex(v5)
                }
            }

            When("Multiple actions are dispatched using the conditionally suspending `dispatch` method") {
                suspend fun dispatchSequence() {
                    walker.dispatch(GraphStateMachineAction.NextArgs(100))
                    delay(1)
                    walker.current.value.vertex.id shouldBe "2"
                    walker.current.value.args shouldBe 100

                    walker.dispatch(GraphStateMachineAction.NextArgs(200))
                    delay(1)
                    walker.current.value.vertex.id shouldBe "3"
                    walker.current.value.args shouldBe 200

                    walker.dispatch(GraphStateMachineAction.NextArgs(300))
                    delay(1)
                    walker.current.value.vertex.id shouldBe "5"
                    walker.current.value.args shouldBe 300
                }

                dispatchSequence()

                Then("The updated state is readable soon after dispatching given that the channel was conflated - the action dispatch suspended") {
                    walker.current.value.vertex.id shouldBe "5"
                    walker.current.value.args shouldBe 300
                }
            }
        }
    }
)
