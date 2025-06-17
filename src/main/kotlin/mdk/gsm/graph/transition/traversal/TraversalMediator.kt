package mdk.gsm.graph.transition.traversal

import mdk.gsm.graph.IVertex
import mdk.gsm.graph.transition.IGraphTraversal
import mdk.gsm.state.*
import mdk.gsm.state.traverser.TraversalState

internal class TraversalMediator<V, I, F, A>(
    private val graphTraversal: IGraphTraversal<V, I, F, A>,
    private val transitionGuardState: F,
    private val gsmConfig: GsmConfig
) where V : IVertex<I>, F : ITransitionGuardState {

    var currentBounds = TraversalBounds.WithinBounds
    var currentArgs: A? = null

    private suspend inline fun returnTraversalState(
        crossinline traversalStateProvider : suspend () -> TraversalState<V, I, A>
    ) : TraversalState<V, I, A> {
        val traversalState = traversalStateProvider()
        currentBounds = traversalState.traversalBounds
        currentArgs = traversalState.args

        return traversalState
    }

    fun initialReadStartVertex() : TraversalState<V, I, A> {
        return TraversalState(
            vertex = graphTraversal.currentStep(),
            traversalBounds = currentBounds,
            args = null
        )
    }

    suspend fun handleNext(
        args : A? = null
    ): TraversalState<V, I, A> = returnTraversalState {

        if (gsmConfig.explicitlyMoveIntoBounds && currentBounds == TraversalBounds.BeforeFirst) {
            return@returnTraversalState TraversalState(
                vertex = graphTraversal.currentStep(),
                traversalBounds = TraversalBounds.WithinBounds,
                args = args
            )
        }

        var autoAdvanceIteration = -1
        var result : TraversalState<V, I, A>?

        while (true) {
            autoAdvanceIteration++
            val traversalNode = graphTraversal.moveNext(
                guardState = transitionGuardState,
                autoAdvance = autoAdvanceIteration > 0,
                args = args
            )

            if (traversalNode == null) {
                result = TraversalState(
                    vertex = graphTraversal.currentStep(),
                    traversalBounds = TraversalBounds.BeyondLast,
                    args = args
                )
                break
            }

            val nextVertex = traversalNode.vertex
            val handler = graphTraversal.getVertexContainer(nextVertex.id)
                ?.beforeVisitHandler

            if (handler != null) {
                val scope = BeforeVisitScope(
                    vertex = nextVertex,
                    guardState = transitionGuardState,
                    args = args
                )

                handler.invoke(scope)
                if (scope.autoAdvanceTrigger) {
                    continue
                }
            }

            result = TraversalState(
                vertex = nextVertex,
                traversalBounds = TraversalBounds.WithinBounds,
                args = args
            )

            break
        }

        return@returnTraversalState result
    }

    suspend fun handlePrevious(): TraversalState<V, I, A> = returnTraversalState {
        if (gsmConfig.explicitlyMoveIntoBounds && currentBounds == TraversalBounds.BeyondLast) {
            return@returnTraversalState TraversalState(
                vertex = graphTraversal.currentStep(),
                traversalBounds = TraversalBounds.WithinBounds,
                args = graphTraversal.head().args
            )
        }

        val previous = graphTraversal.movePrevious()
        if (previous == null) {
            TraversalState(
                vertex = graphTraversal.currentStep(),
                traversalBounds = TraversalBounds.BeforeFirst,
                args = null
            )
        } else {
            TraversalState(
                vertex = previous.vertex,
                traversalBounds = TraversalBounds.WithinBounds,
                args = previous.args
            )
        }
    }

    fun handleReset() : TraversalState<V, I, A> {
        transitionGuardState.onReset()

        return TraversalState(
            vertex = graphTraversal.reset(),
            traversalBounds = TraversalBounds.WithinBounds,
            args = null
        )
    }

    fun tracePath(): List<V> {
        return graphTraversal.tracePath()
    }
}
