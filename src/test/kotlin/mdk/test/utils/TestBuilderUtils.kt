package mdk.test.utils

import mdk.gsm.builder.buildGraphStateMachineWithTransitionFlags
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
        testProgressionFlags: TestEdgeTransitionFlags,
        edgeTraversalType: EdgeTraversalType,
        add7to3cycle : Boolean = false,
    ): GraphStateMachine<TestVertex, TestEdgeTransitionFlags> {
        return buildGraphStateMachineWithTransitionFlags<TestVertex, TestEdgeTransitionFlags> {
            setEdgeTransitionFlags(testProgressionFlags)
            setTraversalType(edgeTraversalType)

            buildGraph(v1) {

                addVertex(v1) {
                    addOutgoingEdge {
                        setTo(v2)
                        setTransitionHandler {
                            !flags.blockedGoingTo2
                        }
                    }

                    addOutgoingEdge {
                        setTo(v3)
                        setTransitionHandler {
                            !flags.blockedGoingTo3
                        }
                    }
                }

                addVertex(v2) {
                    addOutgoingEdge {
                        setTo(v4)
                    }
                }

                addVertex(v3) {
                    addOutgoingEdge {
                        setTo(v5)
                        setTransitionHandler {
                            !flags.blockedGoingTo5
                        }
                    }

                    addOutgoingEdge {
                        setTo(v6)
                    }
                }

                addVertex(v4) {
                    addOutgoingEdge {
                        setTo(v8)
                    }
                }

                addVertex(v5) {
                    addOutgoingEdge {
                        setTo(v7)
                        setTransitionHandler {
                            !flags.blockedGoingTo7 && (from as SubTestVertex).testField // cast for subclass access
                                    && v5.testField // alternatively capturing lambda, these are the two options
                        }
                    }
                }

                addVertex(v6) {
                    addOutgoingEdge {
                        setTo(v7)
                        setTransitionHandler {
                            !flags.blockedGoingTo7
                        }
                    }
                }

                addVertex(v7) {
                    addOutgoingEdge {
                        setTo(v8)
                    }

                    if (add7to3cycle) {
                        addOutgoingEdge {
                            setTo(v3)
                            setTransitionHandler {
                                !flags.blockedGoingTo3
                            }
                        }
                    }
                }

                addVertex(v8) {}
            }
        }
    }

    fun build15VertexGraphStateMachine(
        testProgressionFlags: Test15VertexTransitionFlags,
        edgeTraversalType: EdgeTraversalType
    ): GraphStateMachine<TestVertex, Test15VertexTransitionFlags> {
        return buildGraphStateMachineWithTransitionFlags<TestVertex, Test15VertexTransitionFlags> {
            setEdgeTransitionFlags(testProgressionFlags)
            setTraversalType(edgeTraversalType)

            buildGraph(v1) {

                addVertex(v1) {
                    addOutgoingEdge {
                        setTo(v2)
                    }
                    addOutgoingEdge {
                        setTo(v3)
                    }
                }

                addVertex(v2) {
                    addOutgoingEdge {
                        setTo(v4)
                    }
                }

                addVertex(v3) {
                    addOutgoingEdge {
                        setTo(v2)
                        setTransitionHandler {
                            !flags.blockedFrom3To2
                        }
                    }

                    addOutgoingEdge {
                        setTo(v7)
                    }

                    addOutgoingEdge {
                        setTo(v5)
                    }
                }

                addVertex(v4) {
                    addOutgoingEdge {
                        setTo(v6)
                    }
                }

                addVertex(v5) {
                    addOutgoingEdge {
                        setTo(v8)
                    }
                }

                addVertex(v6) {
                    addOutgoingEdge {
                        setTo(v3)
                    }
                    addOutgoingEdge {
                        setTo(v8)
                    }
                }

                addVertex(v7) {
                    addOutgoingEdge {
                        setTo(v8)
                    }
                }

                addVertex(v8) {
                    addOutgoingEdge {
                        setTo(v9)
                        setTransitionHandler {
                            !flags.blockedFrom8To9
                        }
                    }
                    addOutgoingEdge {
                        setTo(v10)
                    }
                }

                addVertex(v9) {
                    addOutgoingEdge {
                        setTo(v11)
                    }
                }

                addVertex(v10) {
                    addOutgoingEdge {
                        setTo(v11)
                    }
                }

                addVertex(v11) {
                    addOutgoingEdge {
                        setTo(v12)
                    }
                    addOutgoingEdge {
                        setTo(v13)
                    }
                }

                addVertex(v12) {
                    addOutgoingEdge {
                        setTo(v14)
                    }
                }

                addVertex(v13) {
                    addOutgoingEdge {
                        setTo(v14)
                    }
                }

                addVertex(v14) {
                    addOutgoingEdge {
                        setTo(v5)
                    }
                    addOutgoingEdge {
                        setTo(v15)
                    }
                }

                addVertex(v15) {
                    addOutgoingEdge {
                        setTo(v2)
                    }
                }
            }
        }
    }
}

