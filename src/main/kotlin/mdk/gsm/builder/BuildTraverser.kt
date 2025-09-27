@file:Suppress("unused")

package mdk.gsm.builder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import mdk.gsm.action.CompletableAction
import mdk.gsm.builder.DispatcherConfig.Companion.toChannel
import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.transition.traverse.EdgeTraversalType
import mdk.gsm.scope.GraphStateMachineScopeFactory
import mdk.gsm.state.*
import mdk.gsm.state.traverser.Traverser
import mdk.gsm.state.traverser.TraverserDispatcherImplementation
import mdk.gsm.state.traverser.TraverserImplementation
import mdk.gsm.state.traverser.TraverserStateImplementation


/**
 * Builds a graph-backed traverser with a custom transition guard state and typed per-action arguments.
 *
 * Use this overload when you need to pass contextual data with an action via
 * [GraphStateMachineAction.NextArgs] of type [A]. The value is delivered to transition guards
 * and stored on the resulting [mdk.gsm.state.TransitionState.args]. The traverser supports
 * forward and backward navigation and maintains transition history.
 *
 * Overload selection:
 * - Choose this overload if you need both a custom guard state ([F]) and per-action arguments ([A]).
 * - If you do not need per-action arguments, prefer [buildTraverser] without [A].
 * - If you also do not need a custom guard state, prefer the simplest [buildTraverser] overload.
 *
 * Parameters:
 * - [guardState]: The initial state shared by all transition guards.
 * - [coroutineScope]: Scope used for dispatch; defaults to [GraphStateMachineScopeFactory.newScope].
 * - [dispatcherConfig]: Controls channel capacity/overflow for action dispatching.
 * - [builderFunction]: DSL to declare vertices, edges, and options.
 *
 * Returns: A configured [mdk.gsm.state.traverser.Traverser].
 *
 * Throws: [IllegalStateException] if the graph/start vertex is not configured.
 *
 * Example:
 * ```kotlin
 * val traverser = buildTraverserWithActions<MyVertex, String, Flags, Long>(
 *     guardState = Flags()
 * ) {
 *     buildGraph(startAtVertex = MyVertex.Start) {
 *         // define vertices and edges
 *     }
 * }
 * // Later: dispatch an action with contextual arguments
 * // traverser.dispatcher.launchDispatch(GraphStateMachineAction.NextArgs(42L))
 * ```
 *
 * @param V The type of vertices (states). Must implement [IVertex].
 * @param I The type of vertex identifiers.
 * @param F The type of transition guard state. Must implement [ITransitionGuardState].
 * @param A The type of per-action arguments used with [GraphStateMachineAction.NextArgs].
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
 * Builds a graph-backed traverser with a custom transition guard state and no per-action arguments.
 *
 * Use this overload when you want guards backed by shared state ([F]), but you do not need to pass
 * values with each action. The traverser supports forward and backward navigation and maintains
 * transition history.
 *
 * Overload selection:
 * - Choose this overload if you need custom guard state ([F]) but not per-action arguments.
 * - If you need per-action arguments as well, prefer [buildTraverserWithActions].
 * - If you need neither, prefer the simplest [buildTraverser] overload.
 *
 * Example:
 * ```kotlin
 * val traverser = buildTraverser<MyVertex, String, Flags>(
 *     guardState = Flags()
 * ) {
 *     buildGraph(startAtVertex = MyVertex.Start) {
 *         // define vertices and edges
 *     }
 * }
 * ```
 *
 * @param V The type of the vertices (states) in the graph. Must implement [IVertex].
 * @param I The type of the vertex identifiers.
 * @param F The transition guard state shared across edges. Must implement [ITransitionGuardState].
 * @param guardState The initial state for transition guards, shared across all edges.
 * @param coroutineScope The coroutine scope used for dispatching actions. Defaults to [GraphStateMachineScopeFactory.newScope].
 * @param dispatcherConfig Configuration for the dispatcher channel. Controls buffering and overflow behavior.
 * @param builderFunction The builder scope function for configuring the traverser.
 * @return A fully configured [Traverser] instance.
 * @throws IllegalStateException If the traverser is not configured correctly when attempting to build.
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
 * Builds a traverser without custom guard state and without per-action arguments (the simplest overload).
 *
 * Use this when your transitions depend only on the graph structure and you do not require guard state
 * or passing values with actions. The traverser supports forward and backward navigation and maintains
 * transition history. A no-op guard state is used internally and the action argument type is [Nothing].
 *
 * Overload selection:
 * - Choose this overload if you need neither custom guard state nor per-action arguments.
 * - If you need guard state, prefer [buildTraverser] with [F].
 * - If you also need per-action arguments, prefer [buildTraverserWithActions].
 *
 * Example:
 * ```kotlin
 * val traverser = buildTraverser<MyVertex, String> {
 *     buildGraph(startAtVertex = MyVertex.Start) {
 *         // define vertices and edges
 *     }
 * }
 * ```
 *
 * @param V The type of the vertices (states) in the graph. Must implement [IVertex].
 * @param I The type of the vertex identifiers.
 * @param scope The coroutine scope used for dispatching actions. Defaults to [GraphStateMachineScopeFactory.newScope].
 * @param dispatcherConfig Configuration for the dispatcher channel. Controls buffering and overflow behavior.
 * @param builderFunction The builder scope function for configuring the traverser.
 * @return A fully configured [Traverser] instance.
 * @throws IllegalStateException If the traverser is not configured correctly when attempting to build.
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
     * Assigns an already-built [Graph] and sets the start vertex for the traverser.
     *
     * Use this when you have constructed a graph separately (e.g., via [buildGraphOnly]) and
     * want to reuse it. The [startAtVertex] should exist in the provided [graph].
     *
     * Example:
     * ```kotlin
     * val graph = buildGraphOnly<MyVertex, String, Flags, Nothing> { /* ... */ }
     * setWorkflowGraph(startAtVertex = MyVertex.Start, graph = graph)
     * ```
     *
     * @param startAtVertex The start vertex. Must exist in [graph].
     * @param graph The graph to assign to this traverser.
     */
    fun setWorkflowGraph(startAtVertex : V, graph: Graph<V, I, F, A>) {
        graphStateMachineBuilder.graph = graph
        graphStateMachineBuilder.startVertex = startAtVertex
    }

    /**
     * Builds and assigns a graph for the traverser using the provided DSL.
     *
     * This creates an internal [GraphBuilder] and invokes [scopeConsumer] with a [GraphBuilderScope]
     * to declare vertices and edges. The resulting graph is validated (no duplicate vertex ids and
     * no dangling edges) and set on the builder, and [startAtVertex] becomes the start vertex.
     *
     * Example:
     * ```kotlin
     * buildGraph(startAtVertex = MyVertex.Start) {
     *     addVertex(MyVertex.Start) { addEdge { setTo(MyVertex.Next) } }
     *     addVertex(MyVertex.Next)
     * }
     * ```
     *
     * @param startAtVertex The vertex to start at. Must exist in the graph after building.
     * @param scopeConsumer A DSL that configures the graph via [GraphBuilderScope].
     */
    fun buildGraph(startAtVertex : V, scopeConsumer : GraphBuilderScope<V, I, F, A>.() -> Unit) {
        val graphGraphBuilder = GraphBuilder<V, I, F, A>()
        val graphBuilderScope = GraphBuilderScope(graphGraphBuilder)
        scopeConsumer(graphBuilderScope)
        graphStateMachineBuilder.graph = graphGraphBuilder.build()
        graphStateMachineBuilder.startVertex = startAtVertex
    }

    /**
     * Shorthand to build and assign a graph with the same semantics as [buildGraph].
     *
     * Accepts the same parameters and builds the graph using a [GraphBuilderScope], then sets
     * [startAtVertex] as the start vertex.
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
     * The default is `false`, in which case traversal behaviour is not impacted and the concept of traversal bounds can be ignored.
     * This can be safely ignored unless the implementer requires distinct states representing nowhere else to go via [TransitionBounds] that need to be traversed over.
     *
     * Being out-of-bounds (having nowhere else to go) for the traverser (as flagged by the [mdk.gsm.state.TransitionBounds] property of [mdk.gsm.state.TransitionState])
     * is basically a null state, but with the benefit of knowing the last vertex, and the direction the state was moved in.
     *
     * The current state of the traverser is always non-null, but when out of bounds, and [setExplicitTransitionIntoBounds] is `true`,
     * this could arguably be considered a kind of null object representation depending on how you want to interpret it via your use case.
     *
     * Callers can choose to ignore the [mdk.gsm.state.TransitionBounds] and forget about this mechanism, or treat being out of bounds as a distinct state.
     *
     * If [setExplicitTransitionIntoBounds] is `true`, the traverser will treat moving back into bounds as a standalone distinct traversal, moving to a
     * valid in-bounds state (*on the same vertex*) upon the next dispatched action.
     *
     * This means the [GsmController] will stay on the same vertex, but simply move into a traversal state which is in-bounds i.e.
     * only the [mdk.gsm.state.TransitionBounds] property will be updated as being back into bounds: ([mdk.gsm.state.TransitionBounds.WithinBounds]).
     *
     * This can be useful to demarcate a process being "uninitialized" or "finished" for example, such as in the case of a completed workflow (workflows are typically a directed acyclic graph with a definitive 'ending') for instance,
     * allowing callers to respond to the workflow being finished.
     *
     * @param explicitlyTransitionIntoBounds `true` to enable automatic transitions back into a valid state on the same vertex when
     *        out of bounds, `false` to keep the traverser at the out-of-bounds state (default).
     */
    fun setExplicitTransitionIntoBounds(explicitlyTransitionIntoBounds : Boolean) {
        graphStateMachineBuilder.explicitlyTransitionIntoBounds = explicitlyTransitionIntoBounds
    }
}
