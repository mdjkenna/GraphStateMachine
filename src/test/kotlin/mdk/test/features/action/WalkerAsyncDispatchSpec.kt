package mdk.test.features.action

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import mdk.gsm.state.GraphStateMachineAction
import mdk.test.scenarios.GraphScenarios
import mdk.test.utils.TestTransitionGuardState

class WalkerAsyncDispatchSpec : BehaviorSpec({
    
    Given("A 4-vertex linear walker for testing dispatch operations") {
        val guardState = TestTransitionGuardState()
        val walker = GraphScenarios.linearFourVertexTraverser(guardState)
        
        When("dispatchAndAwaitResult is called with a Next action") {
            runBlocking {
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            }
            
            Then("The action should be processed and walker should move") {
                walker.current.value.vertex.id shouldBe "2"
            }
        }
        
        When("Multiple Next actions are dispatched sequentially") {
            runBlocking {
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            }
            
            Then("All actions should be processed in order") {
                walker.current.value.vertex.id shouldBe "4"
            }
        }
        
        When("Reset action is dispatched") {
            runBlocking {
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Reset)
            }
            
            Then("The walker should reset to the start vertex") {
                walker.current.value.vertex.id shouldBe "1"
                walker.current.value.isWithinBounds shouldBe true
            }
        }
        
        When("Next is called again after reset") {
            runBlocking {
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            }
            
            Then("The walker should move forward from the start") {
                walker.current.value.vertex.id shouldBe "2"
            }
        }
    }
    
    Given("A walker with action arguments for testing NextArgs dispatch") {
        // Inline since this is a specialized arg-based graph
        val guardState = TestTransitionGuardState()
        val walker = mdk.gsm.builder.buildWalkerWithActions<mdk.test.utils.TestVertex, String, TestTransitionGuardState, Int>(guardState) {
            val v1 = mdk.test.utils.TestVertex("1")
            val v2 = mdk.test.utils.TestVertex("2")
            val v3 = mdk.test.utils.TestVertex("3")
            val v4 = mdk.test.utils.TestVertex("4")
            
            buildGraph(v1) {
                addVertex(v1) {
                    addEdge {
                        setTo(v2)
                        setEdgeTransitionGuard { args != null && args == 100 }
                    }
                    addEdge {
                        setTo(v3)
                        setEdgeTransitionGuard { args != null && args == 200 }
                    }
                }
                addVertex(v2) {
                    addEdge { setTo(v4) }
                }
                addVertex(v3) {
                    addEdge { setTo(v4) }
                }
                addVertex(v4)
            }
        }
        
        When("dispatchAndAwaitResult is called with NextArgs action containing argument 100") {
            runBlocking {
                walker.dispatchAndAwaitResult(GraphStateMachineAction.NextArgs(100))
            }
            
            Then("The walker should take the path to vertex 2 based on the argument") {
                walker.current.value.vertex.id shouldBe "2"
                walker.current.value.args shouldBe 100
            }
        }
        
        When("Reset is called to go back to start") {
            runBlocking {
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Reset)
            }
            
            Then("Walker resets to start vertex") {
                walker.current.value.vertex.id shouldBe "1"
            }
        }
        
        When("dispatchAndAwaitResult is called with NextArgs action containing argument 200") {
            runBlocking {
                walker.dispatchAndAwaitResult(GraphStateMachineAction.NextArgs(200))
            }
            
            Then("The walker should take the alternate path to vertex 3 based on the argument") {
                walker.current.value.vertex.id shouldBe "3"
                walker.current.value.args shouldBe 200
            }
        }
    }
    
    Given("A simple walker for testing component destructuring") {
        val guardState = TestTransitionGuardState()
        val start = mdk.test.utils.TestVertex("start")
        val end = mdk.test.utils.TestVertex("end")
        val walker = GraphScenarios.conditionalWithArgsWalker<Nothing>(guardState, { true }, start, end)
        
        When("The walker is destructured into state and dispatcher components") {
            val (state, dispatcher) = walker
            
            Then("The state component should provide read access to current state") {
                state.current.value.vertex.id shouldBe "start"
            }
            
            Then("The dispatcher component should allow dispatching actions") {
                runBlocking {
                    dispatcher.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                    state.current.value.vertex.id shouldBe "end"
                }
            }
        }
        
        When("Using walkerState property directly") {
            val walkerState = walker.walkerState
            
            Then("It should provide access to the current state") {
                walkerState.current.value.vertex.id shouldBe "end"
            }
        }
        
        When("Using walkerDispatcher property directly") {
            val walkerDispatcher = walker.walkerDispatcher
            
            Then("It should allow resetting the walker") {
                runBlocking {
                    walkerDispatcher.dispatchAndAwaitResult(GraphStateMachineAction.Reset)
                    walker.current.value.vertex.id shouldBe "start"
                }
            }
        }
    }
    
    Given("A simple walker for testing tearDown functionality") {
        val guardState = TestTransitionGuardState()
        val walker = GraphScenarios.linearThreeVertexWalker(guardState)
        
        When("The walker is used normally") {
            runBlocking {
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            }
            
            Then("It should function correctly") {
                walker.current.value.vertex.id shouldBe "2"
            }
        }
        
        When("tearDown is called on the walker") {
            walker.tearDown()
            
            Then("The walker should be shut down") {
                // After teardown, the walker's coroutine scope is cancelled
                // The current state should still be readable
                walker.current.value.vertex.id shouldBe "2"
            }
        }
    }
})
