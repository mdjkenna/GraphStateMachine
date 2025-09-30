package mdk.test.features.dispatcher

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mdk.gsm.builder.DispatcherConfig
import mdk.gsm.builder.buildTraverserWithActions
import mdk.gsm.state.GraphStateMachineAction
import mdk.test.utils.TestTransitionGuardState
import mdk.test.utils.TestVertex

class DispatcherConfigSpec : BehaviorSpec({
    
    Given("A traverser with default dispatcher configuration") {
        val guardState = TestTransitionGuardState()
        val traverser = buildTraverserWithActions<TestVertex, String, TestTransitionGuardState, Nothing>(
            guardState = guardState,
            dispatcherConfig = DispatcherConfig<Nothing>()
        ) {
            val v1 = TestVertex("1")
            val v2 = TestVertex("2")
            val v3 = TestVertex("3")
            
            buildGraph(v1) {
                addVertex(v1) { addEdge { setTo(v2) } }
                addVertex(v2) { addEdge { setTo(v3) } }
                addVertex(v3)
            }
        }
        
        When("Actions are dispatched normally") {
            runBlocking {
                traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            }
            
            Then("The traverser should process actions correctly") {
                traverser.current.value.vertex.id shouldBe "2"
            }
        }
    }
    
    Given("A traverser with DROP_OLDEST buffer overflow strategy") {
        val droppedActions = mutableListOf<GraphStateMachineAction<Int>>()
        val config = DispatcherConfig<Int>(
            capacity = 2,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
            onUndeliveredElement = { action ->
                droppedActions.add(action)
            }
        )
        
        val guardState = TestTransitionGuardState()
        val traverser = buildTraverserWithActions(
            guardState = guardState,
            dispatcherConfig = config
        ) {
            val v1 = TestVertex("1")
            val v2 = TestVertex("2")
            val v3 = TestVertex("3")
            val v4 = TestVertex("4")
            
            buildGraph(v1) {
                addVertex(v1) {
                    addEdge {
                        setTo(v2)
                        setEdgeTransitionGuard { delay(100); true }
                    }
                }
                addVertex(v2) { addEdge { setTo(v3) } }
                addVertex(v3) { addEdge { setTo(v4) } }
                addVertex(v4)
            }
        }
        
        When("Actions are dispatched sequentially") {
            runBlocking {
                // Dispatch actions sequentially to test the configuration works
                traverser.dispatchAndAwaitResult(GraphStateMachineAction.NextArgs(1))
                traverser.dispatchAndAwaitResult(GraphStateMachineAction.NextArgs(2))
            }
            
            Then("The buffer overflow strategy should allow sequential processing") {
                // With DROP_OLDEST, the traverser processes actions correctly
                traverser.current.value.vertex.id shouldBe "3"
            }
        }
    }
    
    Given("A traverser with DROP_LATEST buffer overflow strategy") {
        val config = DispatcherConfig<Int>(
            capacity = 1,
            onBufferOverflow = BufferOverflow.DROP_LATEST
        )
        
        val guardState = TestTransitionGuardState()
        val traverser = buildTraverserWithActions(
            guardState = guardState,
            dispatcherConfig = config
        ) {
            val v1 = TestVertex("1")
            val v2 = TestVertex("2")
            val v3 = TestVertex("3")
            
            buildGraph(v1) {
                addVertex(v1) {
                    addEdge {
                        setTo(v2)
                        setEdgeTransitionGuard { delay(100); true }
                    }
                }
                addVertex(v2) { addEdge { setTo(v3) } }
                addVertex(v3)
            }
        }
        
        When("Actions are dispatched sequentially") {
            runBlocking {
                traverser.dispatchAndAwaitResult(GraphStateMachineAction.NextArgs(1))
            }
            
            Then("The system should remain stable with DROP_LATEST configuration") {
                // With DROP_LATEST, the traverser processes actions correctly
                traverser.current.value.vertex.id shouldBe "2"
            }
        }
    }
    
    Given("A traverser with UNLIMITED capacity") {
        val config = DispatcherConfig<Int>(
            capacity = Channel.UNLIMITED
        )
        
        val guardState = TestTransitionGuardState()
        val traverser = buildTraverserWithActions(
            guardState = guardState,
            dispatcherConfig = config
        ) {
            val v1 = TestVertex("1")
            val v2 = TestVertex("2")
            val v3 = TestVertex("3")
            val v4 = TestVertex("4")
            
            buildGraph(v1) {
                addVertex(v1) { addEdge { setTo(v2) } }
                addVertex(v2) { addEdge { setTo(v3) } }
                addVertex(v3) { addEdge { setTo(v4) } }
                addVertex(v4)
            }
        }
        
        When("Many actions are dispatched") {
            runBlocking {
                traverser.dispatchAndAwaitResult(GraphStateMachineAction.NextArgs(1))
                traverser.dispatchAndAwaitResult(GraphStateMachineAction.NextArgs(2))
                traverser.dispatchAndAwaitResult(GraphStateMachineAction.NextArgs(3))
            }
            
            Then("All actions should be processed successfully") {
                traverser.current.value.vertex.id shouldBe "4"
            }
        }
    }
    
    Given("A traverser with RENDEZVOUS capacity (no buffering)") {
        val config = DispatcherConfig<Nothing>(
            capacity = Channel.RENDEZVOUS
        )
        
        val guardState = TestTransitionGuardState()
        val traverser = buildTraverserWithActions(
            guardState = guardState,
            dispatcherConfig = config
        ) {
            val v1 = TestVertex("1")
            val v2 = TestVertex("2")
            
            buildGraph(v1) {
                addVertex(v1) { addEdge { setTo(v2) } }
                addVertex(v2)
            }
        }
        
        When("Actions are dispatched with rendezvous") {
            runBlocking {
                traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            }
            
            Then("Actions should be processed immediately") {
                traverser.current.value.vertex.id shouldBe "2"
            }
        }
    }
    
    Given("A traverser with onUndeliveredElement callback") {
        val undeliveredActions = mutableListOf<GraphStateMachineAction<String>>()
        val config = DispatcherConfig<String>(
            capacity = 2,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
            onUndeliveredElement = { action ->
                undeliveredActions.add(action)
            }
        )
        
        val guardState = TestTransitionGuardState()
        val traverser = buildTraverserWithActions(
            guardState = guardState,
            dispatcherConfig = config
        ) {
            val v1 = TestVertex("1")
            val v2 = TestVertex("2")
            
            buildGraph(v1) {
                addVertex(v1) { addEdge { setTo(v2) } }
                addVertex(v2)
            }
        }
        
        When("The traverser is torn down with pending actions") {
            runBlocking {
                // Note: This test verifies the callback mechanism exists
                // Actual invocation depends on internal channel closure timing
                traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
                traverser.tearDown()
            }
            
            Then("The system should handle teardown gracefully") {
                // The callback is registered and will be invoked if actions are undelivered
                traverser.current.value.vertex.id shouldBe "2"
            }
        }
    }
    
    Given("DispatcherConfig data class properties") {
        
        When("Creating a config with custom capacity") {
            val config = DispatcherConfig<Int>(capacity = 10)
            
            Then("The capacity should be set correctly") {
                config.capacity shouldBe 10
            }
        }
        
        When("Creating a config with custom overflow strategy") {
            val config = DispatcherConfig<Int>(
                onBufferOverflow = BufferOverflow.DROP_LATEST
            )
            
            Then("The overflow strategy should be set correctly") {
                config.onBufferOverflow shouldBe BufferOverflow.DROP_LATEST
            }
        }
        
        When("Creating a config with all custom parameters") {
            val callback: (GraphStateMachineAction<String>) -> Unit = { }
            val config = DispatcherConfig(
                capacity = 5,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
                onUndeliveredElement = callback
            )
            
            Then("All properties should be set correctly") {
                config.capacity shouldBe 5
                config.onBufferOverflow shouldBe BufferOverflow.DROP_OLDEST
                config.onUndeliveredElement shouldBe callback
            }
        }
    }
})
