@file:Suppress("unused")

package mdk.gsm.state

import kotlinx.coroutines.flow.MutableStateFlow
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.transition.traversal.TraversalMediator
import mdk.gsm.state.traverser.TraversalState
import mdk.gsm.util.CompletableAction

internal class GsmController<V, I, F, A> internal constructor(
    private val graphTraversalMediator: TraversalMediator<V, I, F, A>,
) where V : IVertex<I>, F : ITransitionGuardState {

    val stateOut = MutableStateFlow<TraversalState<V, I, A>>(
        graphTraversalMediator.initialReadStartVertex()
    )

    fun tracePath(): List<V> {
        return graphTraversalMediator.tracePath()
    }

    suspend fun dispatch(completableAction: CompletableAction<V, I, A>) {

        when (val action = completableAction.action) {
            GraphStateMachineAction.Next -> {
                updateState(completableAction) {
                    graphTraversalMediator.handleNext()
                }
            }

            GraphStateMachineAction.Previous -> {
                updateState(completableAction) {
                   graphTraversalMediator.handlePrevious()
                }
            }

            GraphStateMachineAction.Reset -> {
                updateState(completableAction) {
                   graphTraversalMediator.handleReset()
                }
            }

            is GraphStateMachineAction.NextArgs<A> -> {
                updateState(completableAction) {
                    graphTraversalMediator.handleNext(action.args)
                }
            }
        }
    }

    private suspend inline fun updateState(
        completableAction: CompletableAction<V, I, A>,
        crossinline update : suspend () -> TraversalState<V, I, A>
    ) {
        val newState = update()
        stateOut.value = newState

        completableAction.deferred.complete(newState)
    }
}


internal class GsmConfig(
    val explicitlyMoveIntoBounds : Boolean
)
