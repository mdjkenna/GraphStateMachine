package mdk.test.utils

import io.kotest.matchers.shouldBe
import mdk.gsm.graph.IVertex
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.state.traverser.Traverser
import mdk.gsm.state.traverser.TraverserState

object AssertionUtils {
    fun <V, I, F, A> assertTracedPathWithCurrentState(
        expectedPath: List<I>,
        traverser: TraverserState<V, I, F, A>
    ) where V : IVertex<I>, F : ITransitionGuardState {
        traverser.tracePath().map { it.id } shouldBe expectedPath
        traverser.current.value.vertex.id shouldBe expectedPath.last()
    }

    fun <V : IVertex<I>, I, F : ITransitionGuardState, A> assertBounds(
        traverser: Traverser<V, I, F, A>,
        within: Boolean,
        beyond: Boolean,
        before: Boolean
    ) {

        val currentState = traverser.current.value
        currentState.isWithinBounds shouldBe within
        currentState.isBeyondLast shouldBe beyond
        currentState.isBeforeFirst shouldBe before
        currentState.isNotBeforeFirst shouldBe !before
        currentState.isNotBeyondLast shouldBe !beyond
    }

    suspend fun <V, I, F, A> assertExpectedPathGoingNextUntilEnd(
        traverser: Traverser<V, I, F, A>,
        expectedPath: List<I>
    ) where V : IVertex<I>, F : ITransitionGuardState {
        traverser.goNextAndRecordPublishedStatesUntilEnd().map { state ->
            state.vertex.id
        } shouldBe expectedPath
    }

    suspend fun <V, I, F, A> assertExpectedPathGoingPreviousUntilStart(
        traverser: Traverser<V, I, F, A>,
        expectedPath : List<I>,
    ) where V : IVertex<I>, F : ITransitionGuardState {
        traverser.goPreviousAndRecordPublishedStatesUntilStart().map { state ->
            state.vertex.id
        } shouldBe expectedPath
    }
}
