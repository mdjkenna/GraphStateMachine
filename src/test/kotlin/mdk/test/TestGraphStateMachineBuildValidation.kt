package mdk.test

import mdk.gsm.builder.buildGraphStateMachine
import mdk.gsm.util.IntVertex
import mdk.test.utils.TestVertex
import org.junit.Test
import strikt.api.expectCatching
import strikt.assertions.isFailure
import strikt.assertions.isSuccess

class TestGraphStateMachineBuildValidation {

    @Test
    fun `missing vertices causing dangling edges are detected in the graph`() {

        expectCatching {
            buildGraphStateMachine<TestVertex, String> {
                buildGraph(TestVertex("1")) {

                    addVertex(TestVertex("1")) {
                        addEdge {
                            setTo(TestVertex("2"))
                            setTo("2")
                        }
                    }
                }
            }
        }.isFailure()
    }

    @Test
    fun `adding duplicate vertex identifiers to the graph results in an error`() {

        expectCatching {
            buildGraphStateMachine<TestVertex, String> {
                buildGraph(TestVertex("1")) {
                    addVertex(TestVertex("1")) {
                        addEdge {
                            setTo(TestVertex("2"))
                        }
                    }

                    addVertex(TestVertex("2"))

                    addVertex(TestVertex("1")) {
                        addEdge {
                            setTo(TestVertex("3"))
                        }
                    }

                    addVertex(TestVertex("3"))
                }
            }
        }.isFailure()
    }

    @Test
    fun `not specifying a transition flag type and not setting results proceeds ok`() {
        expectCatching {
            buildGraphStateMachine<TestVertex, String> {
                buildGraph(TestVertex("1")) {
                    addVertex(TestVertex("1")) {
                        addEdge {
                            setTo(TestVertex("2"))
                        }
                    }

                    addVertex(TestVertex("2"))
                }
            }
        }.isSuccess()
    }

    @Test
    fun `not setting a graph results in an error`() {
        expectCatching {
            buildGraphStateMachine<TestVertex, String> {}
        }.isFailure()
    }

    @Test
    fun `setting a correct start vertex and the changing to a non existent vertex results in an error`() {
        expectCatching {
            buildGraphStateMachine<IntVertex, Int> {
                buildGraph(IntVertex(1)) {
                    addVertex(IntVertex(1)) {
                        addEdge {
                            setTo(IntVertex(2))
                        }
                    }

                    addVertex(IntVertex(2))

                }

                startAtVertex(IntVertex(3))
            }
        }.isFailure()
    }
}