package mdk.test.features.traversal

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import mdk.gsm.builder.buildTraverser
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.transition.traversal.EdgeTraversalType
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.state.traverser.Traverser
import mdk.gsm.util.StringVertex
import mdk.test.utils.TestVertex
import mdk.test.utils.AssertionUtils
import mdk.test.utils.AssertionUtils.assertTracedPathWithCurrentState
import mdk.test.utils.TestBuilderUtils
import mdk.test.utils.TestBuilderUtils.v1
import mdk.test.utils.TestBuilderUtils.v2
import mdk.test.utils.TestBuilderUtils.v3
import mdk.test.utils.TestBuilderUtils.v4
import mdk.test.utils.TestBuilderUtils.v5
import mdk.test.utils.TestBuilderUtils.v6
import mdk.test.utils.TestBuilderUtils.v7
import mdk.test.utils.TestBuilderUtils.v8
import mdk.test.utils.TestTransitionGuardState

class GraphBidirectionalTraversalEquivalenceSpec : BehaviorSpec({

    Given("A graph state machine which is subjected to traversal in both next and previous directions") {

        withData(
            listOf<TestParameters>(
                createParam1("11 Vertex DAG Acyclic", EdgeTraversalType.DFSAcyclic),
                createParam1("11 Vertex DAG Cyclic", EdgeTraversalType.DFSCyclic),
                createParam2("8 Vertex DAG Forward Acyclic", EdgeTraversalType.DFSAcyclic),
                createParam2("8 Vertex DAG Forward Cyclic", EdgeTraversalType.DFSCyclic)
            )
        ) { param: TestParameters ->

            When("Traversing the entire graph forward") {
                Then("the traversal should visit all vertices in the expected order") {
                    AssertionUtils.assertExpectedPathGoingNextUntilEnd(
                        param.traverser,
                        param.expectedForwardPath
                    )
                }
            }

            When("Traversing the entire graph backward once the end is reached after traversing forwards") {
                Then("the traversal should visit all vertices in reverse order") {
                    AssertionUtils.assertExpectedPathGoingPreviousUntilStart(
                        param.traverser,
                        param.expectedForwardPath.reversed()
                    )
                }
            }

            When("Performing staggered navigation with Next-Next-Previous pattern after traversing all the way previous from the end") {
                Then("Each step should visit the correct sequence of vertices") {
                    val expectedPathSize = param.expectedForwardPath.size
                    val expectedForwardPath = param.expectedForwardPath

                    param.traverser.dispatchAndAwaitResult(GraphStateMachineAction.Reset)

                    val lastIndex = expectedPathSize.dec()
                    for (i in 0 until expectedPathSize.dec()) {
                        val next1Index = if (i < lastIndex) i + 1 else i
                        val next2Index = if (i < lastIndex - 1) next1Index + 1 else next1Index
                        val prevIndex = if (i < lastIndex - 1) next1Index else i

                        assertTracedPathWithCurrentState(
                            expectedForwardPath.slice(0..i),
                            param.traverser
                        )

                        param.traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                        assertTracedPathWithCurrentState(
                            expectedForwardPath.slice(0..next1Index),
                            param.traverser,

                        )

                        param.traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                        assertTracedPathWithCurrentState(
                            expectedForwardPath.slice(0..next2Index),
                            param.traverser,

                        )

                        param.traverser.dispatchAndAwaitResult(GraphStateMachineAction.Previous)
                        assertTracedPathWithCurrentState(
                            expectedForwardPath.slice(0..prevIndex),
                            param.traverser,
                        )
                    }
                }
            }

            When("Performing staggered backward navigation with Previous-Previous-Next pattern") {
                Then("Each step should visit the correct sequence of vertices") {
                    val expectedPathSize = param.expectedForwardPath.size
                    val expectedForwardPath = param.expectedForwardPath

                    param.traverser.dispatchAndAwaitResult(GraphStateMachineAction.Reset)
                    var nextCount = 0
                    do {
                        param.traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                        nextCount++
                    } while (param.traverser.current.value.isWithinBounds && nextCount < expectedPathSize)

                    for (i in expectedPathSize.dec() downTo 1) {
                        val prev1Index = if (i > 1) i - 1 else 0
                        val prev2Index = if (i >= 2) prev1Index - 1 else 0
                        val nextIndex = if (i > 1) prev1Index else i

                        assertTracedPathWithCurrentState(
                            expectedForwardPath.slice(0..i),
                            param.traverser,

                        )

                        param.traverser.dispatchAndAwaitResult(GraphStateMachineAction.Previous)
                        assertTracedPathWithCurrentState(
                            expectedForwardPath.slice(0..prev1Index),
                            param.traverser,

                        )

                        param.traverser.dispatchAndAwaitResult(GraphStateMachineAction.Previous)
                        if (i > 1) {
                            assertTracedPathWithCurrentState(
                                expectedForwardPath.slice(0..prev2Index),
                                param.traverser,

                            )
                        }

                        param.traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                        assertTracedPathWithCurrentState(
                            expectedForwardPath.slice(0..nextIndex),
                            param.traverser,

                        )
                    }
                }
            }
        }
    }
}) {
    companion object {
        data class TestParameters(
            val title: String,
            val traverser: Traverser<out IVertex<String>, String, out ITransitionGuardState, Nothing>,
            val expectedForwardPath: List<String>
        )

        private fun createParam2(title: String, edgeTraversalType: EdgeTraversalType): TestParameters {
            val traverser = TestBuilderUtils.build8VertexGraphStateMachine(TestTransitionGuardState(), edgeTraversalType)

            return TestParameters(
                title,
                traverser,
                listOf(v1, v2, v4, v8, v3, v5, v7, v6).map(IVertex<String>::id)
            )
        }

        private fun createParam1(title: String, edgeTraversalType: EdgeTraversalType): TestParameters {
            val traverser = buildTraverser(
                TestTransitionGuardState()
            ) {
                val step1 = StringVertex("1")
                setTraversalType(edgeTraversalType)

                buildGraph(step1) {
                    val step2a = StringVertex("2A")
                    val step2b = StringVertex("2B")
                    val step3a = StringVertex("3A")
                    val step3b = StringVertex("3B")
                    val step3c = StringVertex("3C")
                    val step4a = StringVertex("4A")
                    val step4b = StringVertex("4B")
                    val step5 = StringVertex("5")
                    val step6 = StringVertex("6")
                    val step7 = StringVertex("7")

                    addVertex(step1) {
                        addEdge { setTo(step2a) }
                        addEdge { setTo(step2b) }
                    }

                    addVertex(step2a) {
                        addEdge { setTo(step3a) }
                        addEdge { setTo(step3b) }
                    }

                    addVertex(step2b) {
                        addEdge { setTo(step3c) }
                    }

                    addVertex(step3a) {
                        addEdge { setTo(step4a) }
                    }

                    addVertex(step3b) {
                        addEdge { setTo(step4b) }
                    }

                    addVertex(step3c) {
                        addEdge { setTo(step4b) }
                    }

                    addVertex(step4a) {
                        addEdge { setTo(step5) }
                    }

                    addVertex(step4b) {
                        addEdge { setTo(step6) }
                    }

                    addVertex(step5) {
                        addEdge { setTo(step7) }
                    }

                    addVertex(step6) {
                        addEdge { setTo(step7) }
                    }

                    addVertex(step7) {}
                }
            }

            return TestParameters(
                title,
                traverser,
                listOf("1", "2A", "3A", "4A", "5", "7", "3B", "4B", "6", "2B", "3C")
            )
        }
    }
}
