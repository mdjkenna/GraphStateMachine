package mdk.test

import mdk.gsm.builder.buildGraphStateMachine
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.traversal.EdgeTraversalType
import mdk.gsm.state.GraphStateMachine
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.util.StringVertex
import mdk.test.utils.AssertionUtils
import mdk.test.utils.TestBuilderUtils
import mdk.test.utils.TestBuilderUtils.v1
import mdk.test.utils.TestBuilderUtils.v2
import mdk.test.utils.TestBuilderUtils.v3
import mdk.test.utils.TestBuilderUtils.v4
import mdk.test.utils.TestBuilderUtils.v5
import mdk.test.utils.TestBuilderUtils.v6
import mdk.test.utils.TestBuilderUtils.v7
import mdk.test.utils.TestBuilderUtils.v8
import mdk.test.utils.TestTraversalGuardState
import org.junit.Assert
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test

@RunWith(Parameterized::class)
class TestAcyclicGraphBidirectionalTraversal (
    private val parameters: Parameters,
) {

    val graphStateMachine : GraphStateMachine<IVertex<String>, String, TestTraversalGuardState>
        get() = parameters.graphStateMachine

    @Before
    fun before() {
        graphStateMachine.dispatch(GraphStateMachineAction.Reset)
    }

    @Test
    fun `staggered actions in both directions supported and produce equivalent paths`() {
        val forwardUpdated = ArrayList<IVertex<String>>()
        val expectedForwardPath = parameters.expectedForwardPath
        val expectedPathSize = expectedForwardPath.size

        do {
            forwardUpdated.add(graphStateMachine.currentState.vertex)
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        } while (graphStateMachine.currentState.isWithinBounds)

        val forwardTraced = graphStateMachine.tracePath().map {
            it.id
        }

        Assert.assertEquals(expectedForwardPath, forwardTraced)
        Assert.assertEquals(expectedForwardPath, forwardUpdated.map { it.id })

        val backwardsUpdated = ArrayList<IVertex<String>>()
        do {
            backwardsUpdated.add(graphStateMachine.currentState.vertex)
            graphStateMachine.dispatch(GraphStateMachineAction.Previous)
        } while (graphStateMachine.currentState.isWithinBounds)

        Assert.assertEquals(expectedForwardPath.reversed(), backwardsUpdated.map { it.id })

        forwardUpdated.clear()

        do {
            forwardUpdated.add(graphStateMachine.currentState.vertex)
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        } while (graphStateMachine.currentState.isWithinBounds)

        expectThat(forwardUpdated.map(IVertex<String>::id)) {
            isEqualTo(expectedForwardPath)
        }

        graphStateMachine.dispatch(GraphStateMachineAction.Reset)

        val lastIndex = expectedPathSize.dec()
        for (i in 0 until expectedPathSize.dec()) {
            val next1Index = if (i < lastIndex) {
                i + 1
            } else {
                i
            }

            val next2Index = if (i < lastIndex - 1) {
                next1Index + 1
            } else {
                next1Index
            }

            val prevIndex = if (i < lastIndex - 1) {
                next1Index
            } else {
                i
            }

            val messageOnFail = ": In the scenario: i : $i, size: ${expectedPathSize}, next 1 index : $next1Index, next 2 index : $next2Index , prev index : $prevIndex"
            AssertionUtils.expectPath(
                expectedForwardPath.slice(0..i),
                graphStateMachine,
                messageOnFail
            )

            graphStateMachine.dispatch(GraphStateMachineAction.Next)
            AssertionUtils.expectPath(
                expectedForwardPath.slice(0..next1Index),
                graphStateMachine,
                messageOnFail
            )

            graphStateMachine.dispatch(GraphStateMachineAction.Next)
            AssertionUtils.expectPath(
                expectedForwardPath.slice(0..next2Index),
                graphStateMachine,
                messageOnFail
            )

            graphStateMachine.dispatch(GraphStateMachineAction.Previous)
            AssertionUtils.expectPath(
                expectedForwardPath.slice(0..prevIndex),
                graphStateMachine,
                messageOnFail
            )
        }

        graphStateMachine.dispatch(GraphStateMachineAction.Next)
        AssertionUtils.expectPath(
            expectedForwardPath,
            graphStateMachine,
        )

        for (i in expectedPathSize.dec() downTo 1) {
            val prev1Index =  if (i > 1) {
                i - 1
            } else {
                0
            }

            val prev2Index = if (i >= 2) {
                prev1Index - 1
            } else {
                0
            }

            val nextIndex = if (i > 1) {
                prev1Index
            } else {
                i
            }

            val messageOnFail = ": In the scenario: i : $i, size: ${expectedPathSize}, previous 1 index : $prev1Index, previous 2 index : $prev2Index , next index : $nextIndex"

            AssertionUtils.expectPath(
                expectedForwardPath.slice(0..i),
                graphStateMachine,
                messageOnFail
            )

            graphStateMachine.dispatch(GraphStateMachineAction.Previous)
            AssertionUtils.expectPath(
                expectedForwardPath.slice(0..prev1Index),
                graphStateMachine,
                messageOnFail
            )

            graphStateMachine.dispatch(GraphStateMachineAction.Previous)
            if (i > 1) {
                AssertionUtils.expectPath(
                    expectedForwardPath.slice(0..prev2Index),
                    graphStateMachine,
                    messageOnFail
                )
            }

            graphStateMachine.dispatch(GraphStateMachineAction.Next)
            AssertionUtils.expectPath(
                expectedForwardPath.slice(0..nextIndex),
                graphStateMachine,
                messageOnFail
            )
        }
    }

    data class Parameters(
        val title : String,
        val graphStateMachine : GraphStateMachine<IVertex<String>, String, TestTraversalGuardState>,
        val expectedForwardPath: List<String>,
    ) {
        override fun toString(): String {
            return title
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    parameters1(title = "11 Vertex DAG Acyclic", EdgeTraversalType.DFSAcyclic)
                ),
                arrayOf(
                    parameters1(title = "11 Vertex DAG Cyclic", EdgeTraversalType.DFSCyclic)
                ),
                arrayOf(
                    parameters2(title = "8 Vertex DAG Forward Acyclic", EdgeTraversalType.DFSAcyclic)
                ),
                arrayOf(
                    parameters2(title = "8 Vertex DAG Forward Cyclic", EdgeTraversalType.DFSCyclic)
                ),
            )
        }

        private fun parameters2(title: String, edgeTraversalType: EdgeTraversalType) : Parameters {
            val graph = TestBuilderUtils.build8VertexGraphStateMachine(TestTraversalGuardState(), edgeTraversalType)
            return Parameters(
                title,
                graph,
                listOf(v1, v2, v4, v8, v3, v5, v7, v6).map(
                    IVertex<String>::id
                )
            )
        }

        private fun parameters1(title: String, edgeTraversalType: EdgeTraversalType): Parameters {
            val graphStateMachine = buildGraphStateMachine<IVertex<String>, String, TestTraversalGuardState>(TestTraversalGuardState()) {
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
                        addEdge {
                            setTo(step6)
                        }
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

            return Parameters(
                title,
                graphStateMachine,
                listOf("1", "2A", "3A", "4A", "5", "7", "3B", "4B", "6", "2B", "3C")
            )
        }
    }
}