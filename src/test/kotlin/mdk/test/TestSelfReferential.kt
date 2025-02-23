package mdk.test

import mdk.gsm.builder.buildGraphStateMachine
import mdk.gsm.graph.traversal.EdgeTraversalType
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.ITraversalGuardState
import mdk.gsm.util.StringVertex
import mdk.test.utils.AssertionUtils
import org.junit.Test

class TestSelfReferential {
    @Test
    fun `self referential edge creates cycle and traversal guards work as expected`() {
        var cycleCount = 0
        val traversalGuardState = object : ITraversalGuardState {
            override fun onReset() { cycleCount = 0 }
        }

        val graphStateMachine = buildGraphStateMachine<StringVertex, String, ITraversalGuardState>(traversalGuardState) {
            val v1 = StringVertex("1")
            val v2 = StringVertex("2")
            setTraversalType(EdgeTraversalType.DFSCyclic)

            buildGraph(v1) {
                v(v1) {
                    e {
                        setTo(v1)
                        setEdgeTraversalGate {
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

        repeat(7) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        }

        AssertionUtils.expectPath(
            expectedPath = List(8) { "1" },
            graphStateMachine = graphStateMachine
        )

        repeat(1) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        }

        AssertionUtils.expectPath(
            expectedPath = List(8) { "1" } + "2",
            graphStateMachine = graphStateMachine
        )

        repeat(6) {
            graphStateMachine.dispatch(GraphStateMachineAction.Previous)
        }

        AssertionUtils.expectPath(
            List(3) { "1" },
            graphStateMachine
        )

        cycleCount = 2

        repeat(6) {
            graphStateMachine.dispatch(GraphStateMachineAction.Next)
        }

        AssertionUtils.expectPath(
            expectedPath = List(8) { "1" } + "2",
            graphStateMachine = graphStateMachine
        )
    }
}

