package mdk.test.features.builder

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import mdk.gsm.builder.buildTraverser
import mdk.gsm.builder.buildWalker
import mdk.gsm.state.GraphStateMachineAction
import mdk.test.utils.TestVertex

// Note: This test file tests the builder DSL methods themselves, so inline graph building is required
class BuilderScopeMethodsSpec : BehaviorSpec({
    
    Given("A graph builder using various edge configuration methods") {
        
        When("Building edges with explicit order using setOrder method") {
            val traverser = buildTraverser {
                val v1 = TestVertex("1")
                val v2 = TestVertex("2")
                val v3 = TestVertex("3")
                val v4 = TestVertex("4")
                
                buildGraph(v1) {
                    addVertex(v1) {
                        // Add edges with explicit ordering
                        addEdge(autoOrder = false) {
                            setOrder(10)
                            setTo(v3)
                        }
                        addEdge(autoOrder = false) {
                            setOrder(5)
                            setTo(v2)
                        }
                        addEdge(autoOrder = false) {
                            setOrder(15)
                            setTo(v4)
                        }
                    }
                    addVertex(v2)
                    addVertex(v3)
                    addVertex(v4)
                }
            }
            
            Then("Edges should be traversed in the order specified, not declaration order") {
                runBlocking {
                    // First edge by order should be v2 (order=5)
                    traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    traverser.current.value.vertex.id shouldBe "2"
                }
            }
        }
        
        When("Building edges using setTo with vertex ID directly") {
            val walker = buildWalker<TestVertex, String> {
                val start = TestVertex("start")
                val end = TestVertex("end")
                
                buildGraph(start) {
                    addVertex(start) {
                        addEdge {
                            // Use ID directly instead of vertex instance
                            setTo("end")
                        }
                    }
                    addVertex(end)
                }
            }
            
            Then("The edge should correctly target the vertex by its ID") {
                runBlocking {
                    walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    walker.current.value.vertex.id shouldBe "end"
                }
            }
        }
        
        When("Building edges using setTo with vertex instance") {
            val walker = buildWalker {
                val start = TestVertex("start")
                val middle = TestVertex("middle")
                val end = TestVertex("end")
                
                buildGraph(start) {
                    addVertex(start) {
                        addEdge {
                            // Use vertex instance
                            setTo(middle)
                        }
                    }
                    addVertex(middle) {
                        addEdge {
                            setTo(end)
                        }
                    }
                    addVertex(end)
                }
            }
            
            Then("The edges should correctly link using vertex instances") {
                runBlocking {
                    walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    walker.current.value.vertex.id shouldBe "middle"
                    
                    walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    walker.current.value.vertex.id shouldBe "end"
                }
            }
        }
    }
    
    Given("A graph builder using vertex shorthand methods") {
        
        When("Using shorthand v() method with single edge e() method") {
            val traverser = buildTraverser<TestVertex, String> {
                val v1 = TestVertex("1")
                val v2 = TestVertex("2")
                val v3 = TestVertex("3")
                
                buildGraph(v1) {
                    v(v1) { 
                        e { setTo(v2) }
                    }
                    v(v2) { 
                        e { setTo(v3) }
                    }
                    v(v3)
                }
            }
            
            Then("The graph should be built correctly using shorthand") {
                runBlocking {
                    traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    traverser.current.value.vertex.id shouldBe "2"
                    
                    traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    traverser.current.value.vertex.id shouldBe "3"
                }
            }
        }
        
        When("Using v() shorthand with multiple edges") {
            val walker = buildWalker<TestVertex, String> {
                val start = TestVertex("start")
                val pathA = TestVertex("pathA")
                val pathB = TestVertex("pathB")
                
                buildGraph(start) {
                    v(start) {
                        e { setTo(pathA) }
                        e { setTo(pathB) }
                    }
                    v(pathA)
                    v(pathB)
                }
            }
            
            Then("The first edge should be selected by default traversal") {
                runBlocking {
                    walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    // First edge goes to pathA
                    walker.current.value.vertex.id shouldBe "pathA"
                }
            }
        }
    }
    
    Given("A graph builder with complex edge transition guards") {
        
        When("Edges have transition guards with state checks") {
            var allowPath1 = true
            val traverser = buildTraverser<TestVertex, String> {
                val start = TestVertex("start")
                val path1 = TestVertex("path1")
                val path2 = TestVertex("path2")
                
                buildGraph(start) {
                    addVertex(start) {
                        addEdge {
                            setTo(path1)
                            setEdgeTransitionGuard {
                                allowPath1
                            }
                        }
                        addEdge {
                            setTo(path2)
                            setEdgeTransitionGuard {
                                !allowPath1
                            }
                        }
                    }
                    addVertex(path1)
                    addVertex(path2)
                }
            }
            
            Then("Path selection should be controlled by transition guards") {
                runBlocking {
                    traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    traverser.current.value.vertex.id shouldBe "path1"
                    
                    traverser.dispatchAndAwaitResult(GraphStateMachineAction.Previous)
                    allowPath1 = false
                    
                    traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    traverser.current.value.vertex.id shouldBe "path2"
                }
            }
        }
    }
    
    Given("A walker builder with specific configuration options") {
        
        When("Building a walker with setExplicitTransitionIntoBounds enabled") {
            val walker = buildWalker<TestVertex, String> {
                val v1 = TestVertex("1")
                val v2 = TestVertex("2")
                
                setExplicitTransitionIntoBounds(true)
                
                buildGraph(v1) {
                    v(v1) { e { setTo(v2) } }
                    v(v2)
                }
            }
            
            Then("The walker should support explicit bounds transitions") {
                runBlocking {
                    walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    walker.current.value.vertex.id shouldBe "2"
                    
                    // Move beyond last
                    walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    walker.current.value.isBeyondLast shouldBe true
                    walker.current.value.isWithinBounds shouldBe false
                }
            }
        }
        
        When("Building a walker without explicit transition bounds") {
            val walker = buildWalker {
                val v1 = TestVertex("1")
                val v2 = TestVertex("2")
                
                setExplicitTransitionIntoBounds(false)
                
                buildGraph(v1) {
                    v(v1) { e { setTo(v2) } }
                    v(v2)
                }
            }
            
            Then("The walker should handle bounds differently") {
                walker.current.value.vertex.id shouldBe "1"
                walker.current.value.isWithinBounds shouldBe true
            }
        }
    }
    
    Given("A graph with vertices having onBeforeVisit handlers") {
        
        When("Building vertices with onBeforeVisit callbacks") {
            val visitedVertices = mutableListOf<String>()
            val walker = buildWalker {
                val v1 = TestVertex("1")
                val v2 = TestVertex("2")
                val v3 = TestVertex("3")
                
                buildGraph(v1) {
                    addVertex(v1) {
                        onBeforeVisit {
                            visitedVertices.add("v1")
                        }
                        addEdge { setTo(v2) }
                    }
                    addVertex(v2) {
                        onBeforeVisit {
                            visitedVertices.add("v2")
                        }
                        addEdge { setTo(v3) }
                    }
                    addVertex(v3) {
                        onBeforeVisit {
                            visitedVertices.add("v3")
                        }
                    }
                }
            }
            
            Then("The onBeforeVisit handlers should be called during traversal") {
                runBlocking {
                    // Start vertex doesn't trigger onBeforeVisit
                    visitedVertices.size shouldBe 0
                    
                    walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    visitedVertices shouldBe listOf("v2")
                    
                    walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    visitedVertices shouldBe listOf("v2", "v3")
                }
            }
        }
    }
    
    Given("A graph with vertices having onOutgoingTransition handlers") {
        
        When("Building vertices with onOutgoingTransition callbacks") {
            val transitionLog = mutableListOf<String>()
            val traverser = buildTraverser {
                val v1 = TestVertex("1")
                val v2 = TestVertex("2")
                val v3 = TestVertex("3")
                
                buildGraph(v1) {
                    addVertex(v1) {
                        onOutgoingTransition {
                            transitionLog.add("leaving-v1")
                        }
                        addEdge { setTo(v2) }
                    }
                    addVertex(v2) {
                        onOutgoingTransition {
                            transitionLog.add("leaving-v2")
                        }
                        addEdge { setTo(v3) }
                    }
                    addVertex(v3)
                }
            }
            
            Then("The onOutgoingTransition handlers should be called when leaving vertices") {
                runBlocking {
                    traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    transitionLog shouldBe listOf("leaving-v1")
                    
                    traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    transitionLog shouldBe listOf("leaving-v1", "leaving-v2")
                }
            }
        }
    }
})
