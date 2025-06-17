@file:Suppress("unused")

package mdk.gsm.builder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import mdk.gsm.builder.DispatcherConfig.Companion.toChannel
import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.transition.traversal.EdgeTraversalType
import mdk.gsm.state.*
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.state.NoTransitionGuardState
import mdk.gsm.state.traverser.Traverser
import mdk.gsm.state.traverser.TraverserDispatcherImplementation
import mdk.gsm.state.traverser.TraverserImplementation
import mdk.gsm.state.traverser.TraverserStateImplementation
import mdk.gsm.util.CompletableAction
import mdk.gsm.util.GraphStateMachineScopeFactory


/**
 * Constructs a `Traverser` with a custom [ITransitionGuardState] and support for action arguments.
 *
 * This function provides a DSL-based entry point for building a traverser represented by a directed graph.
 *
 * It allows passing custom arguments of type [A] with actions, which can be used by transition guards to make
 * conditional decisions about edge traversal.
 *
 * @param V The type of the vertices (states) in the graph. Must implement [IVertex].
 * @param I The type of the vertex identifiers.
 * @param F The transition guard state shared across edges. Must implement [ITransitionGuardState].
 * @param A The type of arguments that can be passed with actions to influence traversal decisions.
 * @param guardState The initial state for transition guards, shared across all edges.
 * @param coroutineScope The coroutine scope used for dispatching actions. Defaults to [GraphStateMachineScopeFactory.newScope].
 * @param builderFunction The builder scope function for configuring the traverser.
 * @return A fully configured [mdk.gsm.state.traverser.Traverser] instance.
 * @throws IllegalStateException If the traverser is not configured correctly when attempting to build.
 *
 * @see IVertex
 * @see ITransitionGuardState
 * @see TraverserBuilderScope
 * @see GraphStateMachineAction.NextArgs
 */
fun <V, I, F, A> buildTraverserWithActions(
    guardState : F,
    coroutineScope : CoroutineScope = GraphStateMachineScopeFactory.newScope(),
    dispatcherConfig: DispatcherConfig<A> = DispatcherConfig(),
    builderFunction : TraverserBuilderScope<V, I, F, A>.() -> Unit
) : Traverser<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {
    val graphStateMachineBuilder = GraphStateMachineBuilder<V, I, F, A>()

    val traverserBuilderScope = TraverserBuilderScope(graphStateMachineBuilder)
    builderFunction(traverserBuilderScope)

    graphStateMachineBuilder.transitionGuardState = guardState

    val gsm = graphStateMachineBuilder.build()

    val channel : Channel<CompletableAction<V, I, A>> = dispatcherConfig.toChannel()

    return TraverserImplementation(
        TraverserStateImplementation.create(gsm),
        TraverserDispatcherImplementation.create(gsm, coroutineScope, channel)
    )
}

/**
 * Constructs a `Traverser` with a custom [ITransitionGuardState].
 * This function provides a DSL-based entry point for building a traverser represented by a directed graph.
 *
 * @param V The type of the vertices (states) in the graph. Must implement [IVertex].
 * @param I The type of the vertex identifiers.
 * @param F The traversal guard state shared across edges. Must implement [ITransitionGuardState].
 * @param guardState The initial state for traversal guards, shared across all edges.
 * @param coroutineScope The coroutine scope used for dispatching actions. Defaults to [GraphStateMachineScopeFactory.newScope].
 * @param dispatcherConfig Configuration for the dispatcher channel. Controls buffering and overflow behavior.
 * @param builderFunction The builder scope function for configuring the traverser.
 * @return A fully configured [Traverser] instance.
 * @throws IllegalStateException If the traverser is not configured correctly when attempting to build.
 *
 * @see IVertex
 * @see ITransitionGuardState
 * @see TraverserBuilderScope
 * @see buildTraverser For building without custom traversal guard state.
 * @see buildTraverserWithActions For building with custom action argument types.
 */
@GsmBuilderScope
fun <V, I, F> buildTraverser(
    guardState : F,
    coroutineScope : CoroutineScope = GraphStateMachineScopeFactory.newScope(),
    dispatcherConfig: DispatcherConfig<Nothing> = DispatcherConfig(),
    builderFunction : TraverserBuilderScope<V, I, F, Nothing>.() -> Unit
) : Traverser<V, I, F, Nothing>
    where V : IVertex<I>, F : ITransitionGuardState {

    val graphStateMachineBuilder = GraphStateMachineBuilder<V, I, F, Nothing>()

    val traverserBuilderScope = TraverserBuilderScope(graphStateMachineBuilder)
    builderFunction(traverserBuilderScope)

    graphStateMachineBuilder.transitionGuardState = guardState

    val gsm = graphStateMachineBuilder.build()

    val channel : Channel<CompletableAction<V, I, Nothing>> = dispatcherConfig.toChannel()

    return TraverserImplementation(
        TraverserStateImplementation.create(gsm),
        TraverserDispatcherImplementation.create(gsm, coroutineScope, channel)
    )
}

/**
 * This is the simplest way to create a traverser when you don't need custom traversal guard state
 * or specialized action argument types.
 *
 * @param V The type of the vertices (states) in the graph. Must implement [IVertex].
 * @param I The type of the vertex identifiers.
 * @param scope The coroutine scope used for dispatching actions. Defaults to [GraphStateMachineScopeFactory.newScope].
 * @param dispatcherConfig Configuration for the dispatcher channel. Controls buffering and overflow behavior.
 * @param builderFunction The builder scope function for configuring the traverser.
 * @return A fully configured [Traverser] instance.
 * @throws IllegalStateException If the traverser is not configured correctly when attempting to build.
 *
 * @see IVertex
 * @see TraverserBuilderScope
 * @see buildTraverser For building with custom traversal guard state.
 * @see buildTraverserWithActions For building with custom action argument types.
 */

@GsmBuilderScope
fun <V, I> buildTraverser(
    scope : CoroutineScope = GraphStateMachineScopeFactory.newScope(),
    dispatcherConfig: DispatcherConfig<Nothing> = DispatcherConfig(),
    builderFunction : TraverserBuilderScope<V, I, ITransitionGuardState, Nothing>.() -> Unit
) : Traverser<V, I, ITransitionGuardState, Nothing> where V : IVertex<I> {

    val graphStateMachineBuilder = GraphStateMachineBuilder<V, I, ITransitionGuardState, Nothing>()
    graphStateMachineBuilder.transitionGuardState = NoTransitionGuardState

    val traverserBuilderScope = TraverserBuilderScope(graphStateMachineBuilder)
    builderFunction(traverserBuilderScope)

    val gsm = graphStateMachineBuilder.build()

    val channel : Channel<CompletableAction<V, I, Nothing>> = dispatcherConfig.toChannel()

    return TraverserImplementation(
        TraverserStateImplementation.create(gsm),
        TraverserDispatcherImplementation.create(gsm, scope, channel)
    )
}

/**
 * Builder scope class for configuring a traverser.
 *
 * This class provides a DSL (Domain Specific Language) for configuring a traverser.
 * It exposes methods for setting up the graph, defining the start vertex, configuring traversal behavior,
 * and other traverser properties.
 *
 * Instances of this class are created by the [buildTraverser] and [buildTraverserWithActions]
 * functions and passed to the builder function provided to those functions.
 *
 * @param V The type of the vertices (states) in the graph. Must implement [IVertex].
 * @param I The type of the vertex identifiers.
 * @param F The traversal guard state shared across edges. Must implement [ITransitionGuardState].
 * @param A The type of arguments that can be passed with actions to influence traversal decisions.
 *
 * @see buildTraverser
 * @see buildTraverserWithActions
 * @see GraphStateMachineBuilder
 */
@GsmBuilderScope
class TraverserBuilderScope<V, I, F, A> @PublishedApi internal constructor(
    internal val graphStateMachineBuilder: GraphStateMachineBuilder<V, I, F, A>
) where V : IVertex<I>, F : ITransitionGuardState {

    /**
     * Allows simply setting the graph for cases where one is already created.
     *
     * @param startAtVertex The vertex to start at. Must be a vertex in the graph.
     * @param graph The graph to set.
     */
    fun setWorkflowGraph(startAtVertex : V, graph: Graph<V, I, F, A>) {
        graphStateMachineBuilder.graph = graph
        graphStateMachineBuilder.startVertex = startAtVertex
    }

    /**
     * Build a graph using the provided scope receiver.
     *
     * @param startAtVertex The vertex to start at. Must be a vertex in the graph when built.
     * @param scopeConsumer The scope consumer to build the graph.
     */
    fun buildGraph(startAtVertex : V, scopeConsumer : GraphBuilderScope<V, I, F, A>.() -> Unit) {
        val graphGraphBuilder = GraphBuilder<V, I, F, A>()
        val graphBuilderScope = GraphBuilderScope(graphGraphBuilder)
        scopeConsumer(graphBuilderScope)
        graphStateMachineBuilder.graph = graphGraphBuilder.build()
        graphStateMachineBuilder.startVertex = startAtVertex
    }

    /**
     * Shorthand for [buildGraph]
     */
    fun g(startAtVertex : V, scopeConsumer : GraphBuilderScope<V, I, F, A>.() -> Unit) {
        buildGraph(startAtVertex, scopeConsumer)
    }

    /**
     * Sets the traversal type for the traverser
     *
     * @see EdgeTraversalType
     */
    fun setTraversalType(edgeTraversalType: EdgeTraversalType) {
        graphStateMachineBuilder.traversalType = edgeTraversalType
    }

    /**
     * Sets the traversal guard state for the entire traverser.
     * Can be ignored if not building a traverser with traversal guards, however, must not be null if specified as a type parameter.
     */
    fun setTransitionGuardState(guardState: F) {
        graphStateMachineBuilder.transitionGuardState = guardState
    }

    /**
     * Sets the start vertex for the traverser.
     * The start vertex must be set before the traverser can be built.
     *
     * @param vertex The vertex to start at. Must be a vertex in the graph.
     */
    fun startAtVertex(vertex: V) {
        graphStateMachineBuilder.startVertex = vertex
    }


    /**
     * The default is `false`, in which case transition behaviour is not impacted and the concept of traversal bounds can be ignored.
     * This can be safely ignored unless the implementer requires distinct states representing nowhere else to go via [TraversalBounds] that need to be traversed over.
     *
     * Being out-of-bounds (having nowhere else to go) for the traverser (as flagged by the [mdk.gsm.state.TraversalBounds] property of [mdk.gsm.state.traverser.TraversalState])
     * is basically a null state, but with the benefit of knowing the last vertex, and the direction the state was moved in.
     *
     * The current state of the traverser is always non-null, but when out of bounds, and [setExplicitTransitionIntoBounds] is `true`,
     * this could arguably be considered a kind of null object representation depending on how you want to interpret it via your use case.
     *
     * Callers can choose to ignore the [mdk.gsm.state.TraversalBounds] and forget about this mechanism, or treat being out of bounds as a distinct state.
     *
     * If [setExplicitTransitionIntoBounds] is `true`, the traverser will treat moving back into bounds as a standalone distinct transition, moving to a
     * valid in-bounds state (*on the same vertex*) upon the next dispatched action.
     *
     * This means the [GsmController] will stay on the same vertex, but simply move into a traversal state which is in-bounds i.e.
     * only the [mdk.gsm.state.TraversalBounds] property will be updated as being back into bounds: ([mdk.gsm.state.TraversalBounds.WithinBounds]).
     *
     * This can be useful to demarcate a process being "uninitialized" or "finished" for example, such as in the case of a completed workflow (workflows are typically a directed acyclic graph with a definitive 'ending') for instance,
     * allowing callers to respond to the workflow being finished.
     *
     * @param setExplicitTransitionIntoBounds `true` to enable automatic transitions back into a valid state on the same vertex when
     *        out of bounds, `false` to keep the traverser at the out-of-bounds state (default).
     */
    fun setExplicitTransitionIntoBounds(explicitlyTransitionIntoBounds : Boolean) {
        graphStateMachineBuilder.explicitlyTransitionIntoBounds = explicitlyTransitionIntoBounds
    }
}
