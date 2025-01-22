package mdk.test.utils

import mdk.gsm.builder.buildGraphStateMachine
import mdk.gsm.graph.traversal.EdgeTraversalType
import mdk.gsm.state.GraphStateMachine

object TestBuilderUtils {

    val v1 = TestVertex("1")
    val v2 = TestVertex("2")
    val v3 = TestVertex("3")
    val v4 = TestVertex("4")
    val v5 = SubTestVertex("5")
    val v6  = TestVertex("6")
    val v7  = TestVertex("7")
    val v8  = TestVertex("8")
    val v9  = TestVertex("9")
    val v10 = TestVertex("10")
    val v11 = TestVertex("11")
    val v12 = TestVertex("12")
    val v13 = TestVertex("13")
    val v14 = TestVertex("14")
    val v15 = TestVertex("15")

    fun build8VertexGraphStateMachine(
        testProgressionFlags: TestTraversalGuardState,
        edgeTraversalType: EdgeTraversalType,
        add7to3cycle : Boolean = false,
    ): GraphStateMachine<TestVertex, String, TestTraversalGuardState> {
        return buildGraphStateMachine<TestVertex, String, TestTraversalGuardState>(testProgressionFlags) {
            setTraversalType(edgeTraversalType)

            buildGraph(v1) {

                addVertex(v1) {
                    addEdge {
                        setTo(v2)
                        setEdgeTraversalGate {
                            !guardState.blockedGoingTo2
                        }
                    }

                    addEdge {
                        setTo(v3)
                        setEdgeTraversalGate {
                            !guardState.blockedGoingTo3
                        }
                    }
                }

                addVertex(v2) {
                    addEdge {
                        setTo(v4)
                    }
                }

                addVertex(v3) {
                    addEdge {
                        setTo(v5)
                        setEdgeTraversalGate {
                            !guardState.blockedGoingTo5
                        }
                    }

                    addEdge {
                        setTo(v6)
                    }
                }

                addVertex(v4) {
                    addEdge {
                        setTo(v8)
                    }
                }

                addVertex(v5) {
                    addEdge {
                        setTo(v7)
                        setEdgeTraversalGate {
                            !guardState.blockedGoingTo7 && (from as SubTestVertex).testField // cast for subclass access
                                    && v5.testField // alternatively capturing lambda, these are the two options
                        }
                    }
                }

                addVertex(v6) {
                    addEdge {
                        setTo(v7)
                        setEdgeTraversalGate {
                            !guardState.blockedGoingTo7
                        }
                    }
                }

                addVertex(v7) {
                    addEdge {
                        setTo(v8)
                    }

                    if (add7to3cycle) {
                        addEdge {
                            setTo(v3)
                            setEdgeTraversalGate {
                                !guardState.blockedGoingTo3
                            }
                        }
                    }
                }

                addVertex(v8) {}
            }
        }
    }

    fun build15VertexGraphStateMachine(
        testProgressionFlags: Test15VertexTransitionArgs,
        edgeTraversalType: EdgeTraversalType
    ): GraphStateMachine<TestVertex, String, Test15VertexTransitionArgs> {
        return buildGraphStateMachine<TestVertex, String, Test15VertexTransitionArgs>(testProgressionFlags) {
            setTraversalGuardState(testProgressionFlags)
            setTraversalType(edgeTraversalType)

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
                        setEdgeTraversalGate {
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
                        setEdgeTraversalGate {
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

