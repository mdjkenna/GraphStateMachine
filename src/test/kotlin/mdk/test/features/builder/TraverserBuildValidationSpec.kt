package mdk.test.features.builder

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import mdk.gsm.builder.buildTraverser
import mdk.gsm.util.IntVertex
import mdk.test.utils.TestVertex

class TraverserBuildValidationSpec : BehaviorSpec({

    Given("A graph state machine builder") {

        When("Building a graph with missing vertices causing dangling edges") {
            Then("The build should fail with an appropriate error") {
                shouldThrow<IllegalStateException> {
                    buildTraverser {
                        buildGraph(TestVertex("1")) {
                            addVertex(TestVertex("1")) {
                                addEdge {
                                    setTo(TestVertex("2"))
                                    setTo("2")
                                }
                            }
                        }
                    }
                }
            }
        }

        When("Adding duplicate vertex identifiers to the graph") {
            Then("The build should fail with a duplicate vertex error") {
                shouldThrow<IllegalStateException> {
                    buildTraverser {
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
                }
            }
        }

        When("Not specifying a traversal flag type and not setting flags") {
            Then("The build should succeed without errors") {
                val result = runCatching {
                    buildTraverser {
                        buildGraph(TestVertex("1")) {
                            addVertex(TestVertex("1")) {
                                addEdge {
                                    setTo(TestVertex("2"))
                                }
                            }

                            addVertex(TestVertex("2"))
                        }
                    }
                }
                result.isSuccess shouldBe true
            }
        }

        When("Not setting a graph at all") {
            Then("The build should fail with an appropriate error") {
                shouldThrow<IllegalStateException> {
                    buildTraverser<TestVertex, String> {}
                }
            }
        }

        When("Setting a correct start vertex and then changing to a non-existent vertex") {
            Then("The build should fail with a vertex not found error") {
                shouldThrow<IllegalStateException> {
                    buildTraverser {
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
                }
            }
        }
    }
})
