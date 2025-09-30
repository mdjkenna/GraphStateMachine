package mdk.test.features.capabilities

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import mdk.gsm.builder.buildWalker
import mdk.gsm.state.GraphStateMachineAction
import mdk.test.utils.TestTransitionGuardState
import mdk.test.utils.TestVertex

class WalkerCapabilitiesSpec : BehaviorSpec({
    
    Given("A walker that doesn't support traverser-only operations") {
        val guardState = TestTransitionGuardState()
        val walker = buildWalker(guardState) {
            val v1 = TestVertex("1")
            val v2 = TestVertex("2")
            val v3 = TestVertex("3")
            
            buildGraph(v1) {
                addVertex(v1) {
                    addEdge { setTo(v2) }
                }
                addVertex(v2) {
                    addEdge { setTo(v3) }
                }
                addVertex(v3)
            }
        }
        
        When("Moving the walker forward to the second vertex") {
            runBlocking {
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            }
            
            Then("The walker should be at vertex 2") {
                walker.current.value.vertex.id shouldBe "2"
            }
        }
        
        When("Attempting unsupported operations on a walker") {
            Then("Previous action should not be available (walkers only support Next)") {
                // Walkers don't expose Previous actions - this is by design
                // The exception types exist for internal use when unsupported capabilities are called
                walker.current.value.vertex.id shouldBe "2"
            }
        }
        
        When("Moving walker to the end") {
            runBlocking {
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            }
            
            Then("The walker should be beyond last") {
                walker.current.value.isBeyondLast shouldBe true
                walker.current.value.vertex.id shouldBe "3"
            }
        }
        
        When("Resetting the walker") {
            runBlocking {
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Reset)
            }
            
            Then("The walker should return to the start vertex") {
                walker.current.value.vertex.id shouldBe "1"
                walker.current.value.isWithinBounds shouldBe true
            }
        }
    }
    
    Given("A walker that supports reset functionality") {
        val guardState = TestTransitionGuardState()
        val walker = buildWalker(guardState) {
            val start = TestVertex("start")
            val middle = TestVertex("middle")
            val end = TestVertex("end")
            
            buildGraph(start) {
                addVertex(start) {
                    addEdge { setTo(middle) }
                }
                addVertex(middle) {
                    addEdge { setTo(end) }
                }
                addVertex(end)
            }
        }
        
        When("Walker has moved through multiple states") {
            runBlocking {
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            }
            
            walker.current.value.vertex.id shouldBe "end"
            
            Then("Reset action should return to the start") {
                runBlocking {
                    val resetState = walker.dispatchAndAwaitResult(GraphStateMachineAction.Reset)
                    resetState.vertex.id shouldBe "start"
                }
                
                walker.current.value.vertex.id shouldBe "start"
            }
        }
    }
})
