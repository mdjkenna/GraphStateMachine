@file:Suppress("unused")

package mdk.gsm.builder

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.traversal.EdgeTraversalType
import mdk.gsm.graph.traversal.GraphTraversalFactory
import mdk.gsm.state.GraphStateMachine
import mdk.gsm.state.GsmConfig
import mdk.gsm.state.IEdgeTransitionFlags

/**
 * Constructs a `GraphStateMachine` with custom transition flags, enabling fine-grained control over state transitions.
 * This function provides a DSL-based entry point for building a state machine represented by a directed graph.  The DSL (Domain Specific Language)
 * leverages Kotlin's type-safe builder pattern and function literals with receiver [builderFunction] to define state machine configurations.
 *
 * Transition flags, represented by the type parameter `F`, enable injecting custom logic during transitions. This allows for complex
 * behavior such as conditional transitions and side effects. The [F] type must implement `IEdgeTransitionFlags`.
 * For simpler state machines without custom flags, consider using [buildGraphStateMachine] instead.
 *
 * @param V The type of the vertices (states). Must implement {@link IVertex}.
 * @param I The type of the addVertex identifiers.
 * @param F The type of the edge transition flags. Must implement {@link IEdgeTransitionFlags}.
 * @param builderFunction The builder scope function for configuring the state machine.
 * @return A fully configured `GraphStateMachine` instance.
 * @throws IllegalStateException If the state machine is not configured correctly when attempting to build
 *
 * @see IVertex
 * @see IEdgeTransitionFlags
 * @see GraphStateMachineBuilderScope
 * @see buildGraphStateMachine for building without custom transition flags.
 */
@GsmBuilderScope
inline fun <reified V, reified I, reified F> buildGraphStateMachineWithTransitionFlags(
    crossinline builderFunction : GraphStateMachineBuilderScope<V, I, F>.() -> Unit
) : GraphStateMachine<V, I, F>
    where V : IVertex<I>, F : IEdgeTransitionFlags {

    val graphStateMachineBuilder = GraphStateMachineBuilder<V, I, F>()

    val graphStateMachineBuilderScope = GraphStateMachineBuilderScope(graphStateMachineBuilder)
    builderFunction(graphStateMachineBuilderScope)

    if (graphStateMachineBuilder.edgeTransitionFlags == null) {
        if (F::class.isInstance(IEdgeTransitionFlags.None)) {
            graphStateMachineBuilder.edgeTransitionFlags = IEdgeTransitionFlags.None as F
        }
    }

    checkNotNull(graphStateMachineBuilder.edgeTransitionFlags) {
        "The transition flags must be initialised on the graph state machine"
    }

    return graphStateMachineBuilder.build()
}

/**
 * Builds a graph state machine using the provided scope functions.
 * This is shorthand for calling [buildGraphStateMachineWithTransitionFlags] with [IEdgeTransitionFlags.None].
 *
 * @return A configured graph state machine.
 */

@GsmBuilderScope
inline fun <reified V, reified I> buildGraphStateMachine(
    crossinline scopeFun : GraphStateMachineBuilderScope<V, I, IEdgeTransitionFlags.None>.() -> Unit
) : GraphStateMachine<V, I, IEdgeTransitionFlags.None> where V : IVertex<I> {

    val graphStateMachineBuilder = GraphStateMachineBuilder<V, I, IEdgeTransitionFlags.None>()
    graphStateMachineBuilder.edgeTransitionFlags = IEdgeTransitionFlags.None

    val graphStateMachineBuilderScope = GraphStateMachineBuilderScope(graphStateMachineBuilder)
    scopeFun(graphStateMachineBuilderScope)

    return graphStateMachineBuilder.build()
}

@GsmBuilderScope
class GraphStateMachineBuilderScope<V, I, F> @PublishedApi internal constructor(
    internal val graphStateMachineBuilder: GraphStateMachineBuilder<V, I, F>
) where V : IVertex<I>, F : IEdgeTransitionFlags {

    /**
     * Allows simply setting the graph for cases where one is already created.
     *
     * @param startAtVertex The addVertex to start at. Must be a addVertex in the graph.
     * @param graph The graph to set.
     */
    fun setWorkflowGraph(startAtVertex : V, graph: Graph<V, I, F>) {
        graphStateMachineBuilder.graph = graph
        graphStateMachineBuilder.startVertex = startAtVertex
    }

    /**
     * Builds a graph using the provided scope consumer.
     *
     * @param startAtVertex The addVertex to start at. Must be a addVertex in the graph.
     * @param scopeConsumer The scope consumer to build the graph.
     */
    fun buildGraph(startAtVertex : V, scopeConsumer : GraphBuilderScope<V, I, F>.() -> Unit) {
        val graphGraphBuilder = GraphBuilder<V, I, F>()
        val graphBuilderScope = GraphBuilderScope(graphGraphBuilder)
        scopeConsumer(graphBuilderScope)
        graphStateMachineBuilder.graph = graphGraphBuilder.build()
        graphStateMachineBuilder.startVertex = startAtVertex
    }

    /**
     * Sets the traversal type for the graph state machine which will determine edge exploration behaviour after dynamic state changes
     *
     * @see EdgeTraversalType
     */
    fun setTraversalType(edgeTraversalType: EdgeTraversalType) {
        graphStateMachineBuilder.traversalType = edgeTraversalType
    }

    /**
     * Sets the progression flags for the graph state machine.
     * This edge progression flags instance is shared across all edges in the graph.
     */
    fun setEdgeTransitionFlags(flags: F) {
        graphStateMachineBuilder.edgeTransitionFlags = flags
    }

    /**
     * Sets the start addVertex for the graph state machine.
     * The start addVertex must be set before the graph state machine can be built.
     *
     * @param vertex The addVertex to start at. Must be a addVertex in the graph.
     */
    fun startAtVertex(vertex: V) {
        graphStateMachineBuilder.startVertex = vertex
    }


    /**
     * *In summary:* The default is `false`, in which case transition behaviour is not impacted and the concept of traversal bounds can be ignored.
     * For the default of `false` reaching the end and then moving previous will simply transition to the previous addVertex.
     * If set to true, a previous action will move the out-of-bounds state back in bounds with an additional previous action required to go to the previous addVertex.
     *
     * *In more detail:* Configures the state machine's behavior when reaching out-of-bounds states [mdk.gsm.state.TraversalBounds.BeforeFirst] or [mdk.gsm.state.TraversalBounds.BeyondLast].
     * Out-of-bounds means nowhere else to go in the graph.
     *
     * Being out-of-bounds for the state machine (as flagged by the [mdk.gsm.state.TraversalBounds] property of [mdk.gsm.state.TraversalState])
     * is basically a null state, but with the benefit of knowing the last addVertex, and the direction the state was moved in.
     *
     * The current state of the graph state machine is always non-null, but when out of bounds,
     * this could arguably be considered a kind of null object representation depending on how you want to interpret it via your use case.
     *
     * Callers can choose to ignore the [mdk.gsm.state.TraversalBounds] and forget about this mechanism, or treat being out of bounds as a distinct state.
     *
     * If [explicitlyTransitionIntoBounds] is `true`, the state machine will treat moving back into bounds as a standalone distinct transition, moving to a
     * valid in-bounds state (*on the same addVertex*) upon the next dispatched action.
     *
     * This means the [GraphStateMachine] will stay on the same addVertex, but simply move into a traversal state which is in-bounds i.e.
     * only the [mdk.gsm.state.TraversalBounds] property will be updated as being back into bounds: ([mdk.gsm.state.TraversalBounds.WithinBounds]).
     *
     * This can be useful to demarcate a process being "uninitialized" or "finished" for example, such as in the case of a completed workflow (workflows are typically a directed acyclic graph with a definitive 'ending') for instance,
     * allowing callers to respond to the workflow being finished.
     *
     * @param explicitlyTransitionIntoBounds `true` to enable automatic transitions back into a valid state on the same addVertex when
     *        out of bounds, `false` to keep the state machine at the out-of-bounds state (default).
     */
    fun explicitlyTransitionIntoBounds(explicitlyTransitionIntoBounds : Boolean) {
        graphStateMachineBuilder.explicitlyTransitionIntoBounds = explicitlyTransitionIntoBounds
    }
}

class GraphStateMachineBuilder<V, I, F> @PublishedApi internal constructor() where V : IVertex<I>, F : IEdgeTransitionFlags {
    var graph : Graph<V, I, F>? = null
    var startVertex : V? = null
    var edgeTransitionFlags : F? = null
    var traversalType : EdgeTraversalType = EdgeTraversalType.RetrogradeAcyclic
    var explicitlyTransitionIntoBounds : Boolean = false

    @PublishedApi
    internal fun build(): GraphStateMachine<V, I, F> {
        val _graph = graph
        val _startVertex = startVertex
        val _edgeTransitionFlags = edgeTransitionFlags

        check(_graph != null) {
            "The workflow graph must be defined."
        }

        check(_startVertex != null) {
            "The start addVertex must be defined."
        }

        check(_graph.containsVertex(_startVertex)) {
            buildString {
                append("The graph must contain the start addVertex. ")
                appendLine()
                append("The start addVertex with stepId '${_startVertex.id}' does not exist in the graph.")
            }
        }

        check(_edgeTransitionFlags != null) {
            "The transition flags must be initialised"
        }

        val graphTraversal = GraphTraversalFactory.buildGraphTraversal(
            graph = _graph,
            startVertex = _startVertex,
            isCyclic = traversalType == EdgeTraversalType.ForwardCyclic
        )

        return GraphStateMachine(
            graphTraversal = graphTraversal,
            gsmConfig = GsmConfig(explicitlyTransitionIntoBounds),
            edgeTraversalType = traversalType,
            flags = _edgeTransitionFlags,
        )
    }
}
