package mdk.test

import mdk.gsm.builder.buildGraphStateMachineWithTransitionFlags
import mdk.gsm.graph.traversal.EdgeTraversalType
import mdk.gsm.state.GraphStateMachine
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.IEdgeTransitionFlags
import mdk.gsm.util.StringVertex
import org.junit.Assert
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test

@RunWith(Parameterized::class)
class TestGraphBidirectionalTraversal (
    private val parameters: Parameters,
) {

    val graphStateMachine : GraphStateMachine<StringVertex, String, IEdgeTransitionFlags.None>
        get() = parameters.graphStateMachine

    @Test
    fun `moving forwards then backwards should produce paths that mirror one another when graph is acyclic and without conditions`() {
        val forwardUpdated = ArrayList<StringVertex>()

        do {
            forwardUpdated.add(graphStateMachine.currentState.vertex)
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        } while (graphStateMachine.currentState.isWithinBounds)

        val forwardTraced = graphStateMachine.tracePath().map {
            it.id
        }

        Assert.assertEquals(parameters.expectedForwardPath, forwardTraced)
        Assert.assertEquals(parameters.expectedForwardPath, forwardUpdated.map { it.id })

        val backwardsUpdated = ArrayList<StringVertex>()
        do {
            backwardsUpdated.add(graphStateMachine.currentState.vertex)
            graphStateMachine.dispatch(GraphStateMachineAction.Previous)
        } while (graphStateMachine.currentState.isWithinBounds)

        Assert.assertEquals(parameters.expectedForwardPath.reversed(), backwardsUpdated.map { it.id })

        forwardUpdated.clear()

        do {
            forwardUpdated.add(graphStateMachine.currentState.vertex)
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        } while (graphStateMachine.currentState.isWithinBounds)

        expectThat(forwardUpdated.map(StringVertex::id)) {
            isEqualTo(parameters.expectedForwardPath)
        }
    }

    data class Parameters(
        val graphStateMachine : GraphStateMachine<StringVertex, String, IEdgeTransitionFlags.None>,
        val expectedForwardPath: List<String>
    )

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(parameters1(EdgeTraversalType.RetrogradeAcyclic)),
                arrayOf(parameters1(EdgeTraversalType.ForwardAcyclic)),
                arrayOf(parameters1(EdgeTraversalType.ForwardCyclic)),
                arrayOf(parameters2(EdgeTraversalType.RetrogradeAcyclic)),
                arrayOf(parameters2(EdgeTraversalType.ForwardAcyclic)),
                arrayOf(parameters2(EdgeTraversalType.ForwardCyclic)),
            )
        }

        private fun parameters2(edgeTransitionType: EdgeTraversalType) : Parameters {
            val step1 = StringVertex("1")
            val step2a = StringVertex("2A")
            val step2b = StringVertex("2B")
            val step3 = StringVertex("3")
            val step3a = StringVertex("3A")
            val step3b = StringVertex("3B")
            val step4 = StringVertex("4")
            val step5 = StringVertex("5")

            val graph = buildGraphStateMachineWithTransitionFlags<StringVertex, String, IEdgeTransitionFlags.None> {
                setTraversalType(edgeTransitionType)

                buildGraph(step1) {

                    addVertex(step1) {
                        addOutgoingEdge { setTo("2A") }
                        addOutgoingEdge { setTo("2B") }
                    }

                    addVertex(step2a) {
                        addOutgoingEdge { setTo("3") }
                    }
                    addVertex(step2b) {
                        addOutgoingEdge { setTo("3A") }
                        addOutgoingEdge { setTo("3B") }
                    }
                    addVertex(step3) {
                        addOutgoingEdge { setTo("5") }
                    }
                    addVertex(step3a) {
                        addOutgoingEdge { setTo("4") }
                    }
                    addVertex(step3b) {
                        addOutgoingEdge { setTo("4") }
                    }
                    addVertex(step4) {
                        addOutgoingEdge { setTo("5") }
                    }
                    addVertex(step5) {}
                }
            }

            return Parameters(
                graph,
                listOf(step1, step2a, step3, step5, step2b, step3a, step4, step3b).map(
                    StringVertex::id
                )
            )

        }

        private fun parameters1(edgeTraversalType: EdgeTraversalType): Parameters {
            val graphStateMachine = buildGraphStateMachineWithTransitionFlags<StringVertex, String, IEdgeTransitionFlags.None> {
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
                        addOutgoingEdge { setTo(step2a) }
                        addOutgoingEdge { setTo(step2b) }
                    }

                    addVertex(step2a) {
                        addOutgoingEdge { setTo(step3a) }
                        addOutgoingEdge { setTo(step3b) }
                    }

                    addVertex(step2b) {
                        addOutgoingEdge { setTo(step3c) }
                    }

                    addVertex(step3a) {
                        addOutgoingEdge { setTo(step4a) }
                    }

                    addVertex(step3b) {
                        addOutgoingEdge { setTo(step4b) }
                    }

                    addVertex(step3c) {
                        addOutgoingEdge { setTo(step4b) }
                    }

                    addVertex(step4a) {
                        addOutgoingEdge { setTo(step5) }
                    }

                    addVertex(step4b) {
                        addOutgoingEdge {
                            setTo(step6)
                        }
                    }

                    addVertex(step5) {
                        addOutgoingEdge { setTo(step7) }
                    }

                    addVertex(step6) {
                        addOutgoingEdge { setTo(step7) }
                    }

                    addVertex(step7) {}
                }
            }

            return Parameters(
                graphStateMachine,
                listOf("1", "2A", "3A", "4A", "5", "7", "3B", "4B", "6", "2B", "3C")
            )
        }
    }
}