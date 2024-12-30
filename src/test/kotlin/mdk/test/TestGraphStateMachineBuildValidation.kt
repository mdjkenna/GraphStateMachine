package mdk.test

import mdk.gsm.builder.buildGraphStateMachine
import mdk.gsm.builder.buildGraphStateMachineWithTransitionFlags
import mdk.test.utils.TestEdgeTransitionFlags
import mdk.test.utils.TestVertex
import org.junit.Test
import strikt.api.expectCatching
import strikt.assertions.isFailure
import strikt.assertions.isSuccess

class TestGraphStateMachineBuildValidation {

    @Test
    fun `missing vertices causing dangling edges are detected in the graph`() {

        expectCatching {
            buildGraphStateMachine<TestVertex> {
                buildGraph(TestVertex("1")) {
                    addVertex(TestVertex("1")) {
                        addOutgoingEdge {
                            setTo(TestVertex("2"))
                        }
                    }
                }
            }
        }.isFailure()
    }

    @Test
    fun `adding duplicate vertex identifiers to the graph results in an error`() {

        expectCatching {
            buildGraphStateMachine<TestVertex> {
                buildGraph(TestVertex("1")) {
                    addVertex(TestVertex("1")) {
                        addOutgoingEdge {
                            setTo(TestVertex("2"))
                        }
                    }

                    addVertex(TestVertex("2"))

                    addVertex(TestVertex("1")) {
                        addOutgoingEdge {
                            setTo(TestVertex("3"))
                        }
                    }

                    addVertex(TestVertex("3"))
                }
            }
        }.isFailure()
    }

    @Test
    fun `specifying a transition flag type and not setting results in error`() {
        expectCatching {
            buildGraphStateMachineWithTransitionFlags<TestVertex, TestEdgeTransitionFlags> {
                buildGraph(TestVertex("1")) {
                    addVertex(TestVertex("1")) {
                        addOutgoingEdge {
                            setTo(TestVertex("2"))
                        }
                    }

                    addVertex(TestVertex("2"))
                }
            }
        }.isFailure()
    }

    @Test
    fun `not specifying a transition flag type and not setting results proceeds ok`() {
        expectCatching {
            buildGraphStateMachine<TestVertex> {
                buildGraph(TestVertex("1")) {
                    addVertex(TestVertex("1")) {
                        addOutgoingEdge {
                            setTo(TestVertex("2"))
                        }
                    }

                    addVertex(TestVertex("2"))
                }
            }
        }.isSuccess()
    }
}