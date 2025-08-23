package mdk.test.features.transition.traverse

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import mdk.gsm.builder.buildWalker
import mdk.gsm.scope.GraphStateMachineScopeFactory
import mdk.gsm.state.GraphStateMachineAction
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.state.walker.Walker
import mdk.gsm.util.StringVertex
import mdk.test.utils.OutgoingTransitionTestGuardState

class OutgoingTransitionHandlerSpec : BehaviorSpec({
    given("A graph with vertices that use onOutgoingTransition handlers") {
        val guardState = OutgoingTransitionTestGuardState()
        val transitionAttempts = mutableListOf<String>()
        val publishedStates = mutableListOf<String>()

        val walker = buildOutgoingTransitionTestGraph(
            guardState = guardState,
            transitionAttempts = transitionAttempts
        )

        publishedStates.add(walker.current.value.vertex.id)

        `when`("A NEXT action is received and the current vertex has an onOutgoingTransition handler that prevents traversal") {
            // The START vertex has an onOutgoingTransition handler that prevents traversal when a condition is met
            guardState.shouldPreventTransition = true

            val result = walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            publishedStates.add(result.vertex.id)

            then("The traversal is prevented and the state remains the same") {
                transitionAttempts shouldContainExactly listOf(ids.START)
                publishedStates shouldContainExactly listOf(ids.START, ids.START)
                walker.current.value.vertex.id shouldBe ids.START
            }
        }

        `when`("A NEXT action is received and the current vertex has an onOutgoingTransition handler that allows traversal") {
            // Now allow the traversal
            guardState.shouldPreventTransition = false

            val result = walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            publishedStates.add(result.vertex.id)

            then("The traversal occurs normally") {
                transitionAttempts shouldContainExactly listOf(ids.START, ids.START)
                publishedStates shouldContainExactly listOf(ids.START, ids.START, ids.MIDDLE)
                walker.current.value.vertex.id shouldBe ids.MIDDLE
            }
        }

        `when`("Another NEXT action is received and the current vertex has no onOutgoingTransition handler") {
            // The MIDDLE vertex has no onOutgoingTransition handler, so traversal should always occur
            val result = walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            publishedStates.add(result.vertex.id)

            then("The traversal occurs normally") {
                transitionAttempts shouldContainExactly listOf(ids.START, ids.START)
                publishedStates shouldContainExactly listOf(ids.START, ids.START, ids.MIDDLE, ids.END)
                walker.current.value.vertex.id shouldBe ids.END
            }
        }
    }
}) {
    companion object {
        object ids {
            const val START = "start"
            const val MIDDLE = "middle"
            const val END = "end"
        }

        fun <F : ITransitionGuardState> buildOutgoingTransitionTestGraph(
            guardState: F,
            scope: CoroutineScope = GraphStateMachineScopeFactory.newScope(),
            transitionAttempts: MutableList<String>
        ): Walker<StringVertex, String, F, Nothing> {
            return buildWalker(guardState, scope) {
                val start = StringVertex(ids.START)
                val middle = StringVertex(ids.MIDDLE)
                val end = StringVertex(ids.END)

                buildGraph(start) {
                    addVertex(start) {
                        // Add an onOutgoingTransition handler that prevents traversal when a condition is met
                        onOutgoingTransition {
                            transitionAttempts.add(vertex.id)

                            // Access the guard state to check if traversal should be prevented
                            if ((guardState as OutgoingTransitionTestGuardState).shouldPreventTransition) {
                                noTransition()
                            }
                        }

                        addEdge {
                            setTo(middle)
                        }
                    }

                    addVertex(middle) {
                        // No onOutgoingTransition handler, so traversal will always occur
                        addEdge {
                            setTo(end)
                        }
                    }

                    addVertex(end) {
                        // End vertex has no outgoing edges
                    }
                }
            }
        }
    }
}