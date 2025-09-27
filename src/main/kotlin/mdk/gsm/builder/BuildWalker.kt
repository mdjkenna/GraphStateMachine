@file:Suppress("unused")

package mdk.gsm.builder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import mdk.gsm.action.CompletableAction
import mdk.gsm.builder.DispatcherConfig.Companion.toChannel
import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.scope.GraphStateMachineScopeFactory
import mdk.gsm.state.*
import mdk.gsm.state.walker.Walker
import mdk.gsm.state.walker.WalkerDispatcherImplementation
import mdk.gsm.state.walker.WalkerImplementation
import mdk.gsm.state.walker.WalkerStateImplementation

/**
 * Builds a graph based walker with a custom transition guard state and typed per-action arguments.
 *
 * A walker only supports forward movement and does not keep a history of visited states.
 * Use this overload when you need to pass contextual data with actions via
 * [GraphStateMachineAction.NextArgs] of type [A]. The value is delivered to transition guards
 * and stored on the resulting [mdk.gsm.state.TransitionState.args].
 *
 * Overload selection:
 * - Choose this overload if you need both a custom guard state ([F]) and per-action arguments ([A]).
 * - If you do not need per-action arguments, prefer [buildWalker] without [A].
 * - If you also do not need a custom guard state, prefer the simplest [buildWalker] overload.
 *
 * Parameters:
 * - [guardState]: The initial state shared by all transition guards.
 * - [coroutineScope]: Scope used for dispatch; defaults to [GraphStateMachineScopeFactory.newScope].
 * - [dispatcherConfig]: Controls channel capacity/overflow for action dispatching.
 * - [builderFunction]: DSL to declare vertices, edges, and options.
 *
 * Returns: A configured [mdk.gsm.state.walker.Walker].
 *
 * Throws: [IllegalStateException] if the graph/start vertex is not configured.
 *
 * Example:
 * ```kotlin
 * val walker = buildWalkerWithActions<MyVertex, String, Flags, Long>(
 *     guardState = Flags()
 * ) {
 *     buildGraph(startAtVertex = MyVertex.Start) {
 *         // define vertices and edges
 *     }
 * }
 * ```
 *
 * @param V The type of vertices (states). Must implement [IVertex].
 * @param I The type of vertex identifiers.
 * @param F The type of transition guard state. Must implement [ITransitionGuardState].
 * @param A The type of per-action arguments used with [GraphStateMachineAction.NextArgs].
 */
fun <V, I, F, A> buildWalkerWithActions(
    guardState : F,
    coroutineScope : CoroutineScope = GraphStateMachineScopeFactory.newScope(),
    dispatcherConfig: DispatcherConfig<A> = DispatcherConfig(),
    builderFunction : WalkerBuilderScope<V, I, F, A>.() -> Unit
) : Walker<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {
    val graphStateMachineBuilder = GraphStateMachineBuilder<V, I, F, A>()

    val walkerBuilderScope = WalkerBuilderScope(graphStateMachineBuilder)
    builderFunction(walkerBuilderScope)

    graphStateMachineBuilder.transitionGuardState = guardState

    val gsm = graphStateMachineBuilder.buildForWalker()

    val channel : Channel<CompletableAction<V, I, A>> = dispatcherConfig.toChannel()

    return WalkerImplementation(
        WalkerStateImplementation.create(gsm),
        WalkerDispatcherImplementation.create(gsm, coroutineScope, channel)
    )
}

/**
 * Builds a graph-backed walker with a custom transition guard state and no per-action arguments.
 *
 * A walker only supports forward movement and does not keep a history of visited states.
 * Use this overload when you want guards backed by shared state ([F]) but you do not need to pass
 * values with each action.
 *
 * Overload selection:
 * - Choose this overload if you need custom guard state ([F]) but not per-action arguments.
 * - If you need per-action arguments as well, prefer [buildWalkerWithActions].
 * - If you need neither, prefer the simplest [buildWalker] overload.
 *
 * Example:
 * ```kotlin
 * val walker = buildWalker<MyVertex, String, Flags>(
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
 * @param builderFunction The builder scope function for configuring the walker.
 * @return A fully configured [Walker] instance.
 * @throws IllegalStateException If the walker is not configured correctly when attempting to build.
 */
@GsmBuilderScope
fun <V, I, F> buildWalker(
    guardState : F,
    coroutineScope : CoroutineScope = GraphStateMachineScopeFactory.newScope(),
    dispatcherConfig: DispatcherConfig<Nothing> = DispatcherConfig(),
    builderFunction : WalkerBuilderScope<V, I, F, Nothing>.() -> Unit
) : Walker<V, I, F, Nothing>
    where V : IVertex<I>, F : ITransitionGuardState {

    val graphStateMachineBuilder = GraphStateMachineBuilder<V, I, F, Nothing>()

    val walkerBuilderScope = WalkerBuilderScope(graphStateMachineBuilder)
    builderFunction(walkerBuilderScope)

    graphStateMachineBuilder.transitionGuardState = guardState

    val gsm = graphStateMachineBuilder.buildForWalker()

    val channel : Channel<CompletableAction<V, I, Nothing>> = dispatcherConfig.toChannel()

    return WalkerImplementation(
        WalkerStateImplementation.create(gsm),
        WalkerDispatcherImplementation.create(gsm, coroutineScope, channel)
    )
}

/**
 * Builds a walker without custom guard state and without per-action arguments (the simplest overload).
 *
 * A walker supports forward-only movement and does not keep a history of visited states. Use this when your
 * transitions depend solely on the graph structure. A no-op guard state is used internally and the action
 * argument type is [Nothing].
 *
 * Overload selection:
 * - Choose this overload if you need neither custom guard state nor per-action arguments.
 * - If you need guard state, prefer [buildWalker] with [F].
 * - If you also need per-action arguments, prefer [buildWalkerWithActions].
 *
 * Example:
 * ```kotlin
 * val walker = buildWalker<MyVertex, String> {
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
 * @param builderFunction The builder scope function for configuring the walker.
 * @return A fully configured [Walker] instance.
 * @throws IllegalStateException If the walker is not configured correctly when attempting to build.
 */
@GsmBuilderScope
fun <V, I> buildWalker(
    scope : CoroutineScope = GraphStateMachineScopeFactory.newScope(),
    dispatcherConfig: DispatcherConfig<Nothing> = DispatcherConfig(),
    builderFunction : WalkerBuilderScope<V, I, ITransitionGuardState, Nothing>.() -> Unit
) : Walker<V, I, ITransitionGuardState, Nothing> where V : IVertex<I> {

    val graphStateMachineBuilder = GraphStateMachineBuilder<V, I, ITransitionGuardState, Nothing>()
    graphStateMachineBuilder.transitionGuardState = NoTransitionGuardState

    val walkerBuilderScope = WalkerBuilderScope(graphStateMachineBuilder)
    builderFunction(walkerBuilderScope)

    val gsm = graphStateMachineBuilder.buildForWalker()

    val channel : Channel<CompletableAction<V, I, Nothing>> = dispatcherConfig.toChannel()

    return WalkerImplementation(
        WalkerStateImplementation.create(gsm),
        WalkerDispatcherImplementation.create(gsm, scope, channel)
    )
}

/**
 * Builder scope class for configuring a walker.
 *
 * This class provides a DSL (Domain Specific Language) for configuring a walker.
 * It exposes methods for setting up the graph, defining the start vertex, and other walker properties.
 * Unlike [TraverserBuilderScope], it does not expose methods for configuring traversal behavior,
 * as walkers only support forward movement.
 *
 * Instances of this class are created by the [buildWalker] and [buildWalkerWithActions]
 * functions and passed to the builder function provided to those functions.
 *
 * @param V The type of the vertices (states) in the graph. Must implement [IVertex].
 * @param I The type of the vertex identifiers.
 * @param F The traversal guard state shared across edges. Must implement [ITransitionGuardState].
 * @param A The type of arguments that can be passed with actions to influence traversal decisions.
 *
 * @see buildWalker
 * @see buildWalkerWithActions
 * @see GraphStateMachineBuilder
 */
@GsmBuilderScope
class WalkerBuilderScope<V, I, F, A> @PublishedApi internal constructor(
    internal val graphStateMachineBuilder: GraphStateMachineBuilder<V, I, F, A>
) where V : IVertex<I>, F : ITransitionGuardState {

    /**
     * Assigns an already-built [Graph] and sets the start vertex for the walker.
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
     * @param graph The graph to assign to this walker.
     */
    fun setWorkflowGraph(startAtVertex : V, graph: Graph<V, I, F, A>) {
        graphStateMachineBuilder.graph = graph
        graphStateMachineBuilder.startVertex = startAtVertex
    }

    /**
     * Builds and assigns a graph for the walker using the provided DSL.
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
     * Sets the traversal guard state for the entire walker.
     * Can be ignored if not building a walker with traversal guards, however, must not be null if specified as a type parameter.
     */
    fun setTraversalGuardState(guardState: F) {
        graphStateMachineBuilder.transitionGuardState = guardState
    }

    /**
     * Sets the start vertex for the walker.
     * The start vertex must be set before the walker can be built.
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
     * Being out-of-bounds (having nowhere else to go) for the walker (as flagged by the [mdk.gsm.state.TransitionBounds] property of [mdk.gsm.state.TransitionState])
     * is basically a null state, but with the benefit of knowing the last vertex, and the direction the state was moved in.
     *
     * The current state of the walker is always non-null, but when out of bounds, and [setExplicitTransitionIntoBounds] is `true`,
     * this could arguably be considered a kind of null object representation depending on how you want to interpret it via your use case.
     *
     * Callers can choose to ignore the [mdk.gsm.state.TransitionBounds] and forget about this mechanism, or treat being out of bounds as a distinct state.
     *
     * If [setExplicitTransitionIntoBounds] is `true`, the walker will treat moving back into bounds as a standalone distinct traversal, moving to a
     * valid in-bounds state (*on the same vertex*) upon the next dispatched action.
     *
     * This means the [GsmController] will stay on the same vertex, but simply move into a traversal state which is in-bounds i.e.
     * only the [mdk.gsm.state.TransitionBounds] property will be updated as being back into bounds: ([mdk.gsm.state.TransitionBounds.WithinBounds]).
     *
     * This can be useful to demarcate a process being "uninitialized" or "finished" for example, such as in the case of a completed workflow (workflows are typically a directed acyclic graph with a definitive 'ending') for instance,
     * allowing callers to respond to the workflow being finished.
     *
     * @param explicitlyTransitionIntoBounds `true` to enable automatic transitions back into a valid state on the same vertex when
     *        out of bounds, `false` to keep the walker at the out-of-bounds state (default).
     */
    fun setExplicitTransitionIntoBounds(explicitlyTransitionIntoBounds : Boolean) {
        graphStateMachineBuilder.explicitlyTransitionIntoBounds = explicitlyTransitionIntoBounds
    }
}

/**
 * Extension function for [GraphStateMachineBuilder] to build a GsmController with a stateless graph walk.
 */
@PublishedApi
internal fun <V, I, F, A> GraphStateMachineBuilder<V, I, F, A>.buildForWalker(): GsmController<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {
    useStatelessWalk = true

    return build()
}
