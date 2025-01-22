@file:Suppress("unused")

package mdk.gsm.builder

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.traversal.EdgeTraversalType
import mdk.gsm.graph.traversal.GraphTraversalFactory
import mdk.gsm.state.GraphStateMachine
import mdk.gsm.state.GsmConfig
import mdk.gsm.state.ITraversalGuardState

/**
 * Constructs a `GraphStateMachine` with a custom [ITraversalGuardState].
 *
 * This function provides a DSL-based entry point for building a state machine represented by a directed graph.  The DSL (Domain Specific Language)
 * leverages Kotlin's type-safe builder pattern and function literals with receiver [builderFunction] to define state machine configurations.
 *
 * @param V The type of the vertices (states) in the graph. Must implement [IVertex].
 * @param I The type of the vertex identifiers.
 * @param F The traversal guard state shared across edges. Must implement [ITraversalGuardState].
 * @param builderFunction The builder scope function for configuring the state machine.
 * @return A fully configured `GraphStateMachine` instance.
 * @throws IllegalStateException If the state machine is not configured correctly when attempting to build
 *
 * @see IVertex
 * @see ITraversalGuardState
 * @see GraphStateMachineBuilderScope
 * @see buildGraphStateMachine for building without custom transition flags.
 */
@GsmBuilderScope
fun <V, I, F> buildGraphStateMachine(
    guardState : F,
    builderFunction : GraphStateMachineBuilderScope<V, I, F>.() -> Unit
) : GraphStateMachine<V, I, F>
    where V : IVertex<I>, F : ITraversalGuardState {

    val graphStateMachineBuilder = GraphStateMachineBuilder<V, I, F>()

    val graphStateMachineBuilderScope = GraphStateMachineBuilderScope(graphStateMachineBuilder)
    builderFunction(graphStateMachineBuilderScope)

    graphStateMachineBuilder.traversalGuardState = guardState

    return graphStateMachineBuilder.build()
}

/**
 * Constructs a `GraphStateMachine`.
 *
 * This function provides a DSL-based entry point for building a state machine represented by a directed graph.  The DSL (Domain Specific Language)
 * leverages Kotlin's type-safe builder pattern and function literals with receiver [builderFunction] to define state machine configurations.
 *
 * @param V The type of the vertices (states) in the graph. Must implement [IVertex].
 * @param I The type of the vertex identifiers.
 * @param builderFunction The builder scope function for configuring the state machine.
 * @return A fully configured `GraphStateMachine` instance.
 * @throws IllegalStateException If the state machine is not configured correctly when attempting to build
 *
 * @see IVertex
 * @see ITraversalGuardState
 * @see GraphStateMachineBuilderScope
 * @see buildGraphStateMachine for building without custom transition flags.
 */

@GsmBuilderScope
fun <V, I> buildGraphStateMachine(
    builderFunction : GraphStateMachineBuilderScope<V, I, ITraversalGuardState.None>.() -> Unit
) : GraphStateMachine<V, I, ITraversalGuardState.None> where V : IVertex<I> {

    val graphStateMachineBuilder = GraphStateMachineBuilder<V, I, ITraversalGuardState.None>()
    graphStateMachineBuilder.traversalGuardState = ITraversalGuardState.None

    val graphStateMachineBuilderScope = GraphStateMachineBuilderScope(graphStateMachineBuilder)
    builderFunction(graphStateMachineBuilderScope)

    return graphStateMachineBuilder.build()
}

@GsmBuilderScope
class GraphStateMachineBuilderScope<V, I, F> @PublishedApi internal constructor(
    internal val graphStateMachineBuilder: GraphStateMachineBuilder<V, I, F>
) where V : IVertex<I>, F : ITraversalGuardState {

    /**
     * Allows simply setting the graph for cases where one is already created.
     *
     * @param startAtVertex The vertex to start at. Must be a vertex in the graph.
     * @param graph The graph to set.
     */
    fun setWorkflowGraph(startAtVertex : V, graph: Graph<V, I, F>) {
        graphStateMachineBuilder.graph = graph
        graphStateMachineBuilder.startVertex = startAtVertex
    }

    /**
     * Build a graph using the provided scope receiver.
     *
     * @param startAtVertex The vertex to start at. Must be a vertex in the graph when built.
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
     * Sets the traversal type for the graph state machine
     *
     * @see EdgeTraversalType
     */
    fun setTraversalType(edgeTraversalType: EdgeTraversalType) {
        graphStateMachineBuilder.traversalType = edgeTraversalType
    }

    /**
     * Sets the traversal guard state for the entire state machine.
     * Can be ignored if not building a state machine with traversal guards, however must not be null if specified as a type parameter.
     */
    fun setTraversalGuardState(guardState: F) {
        graphStateMachineBuilder.traversalGuardState = guardState
    }

    /**
     * Sets the start vertex for the graph state machine.
     * The start vertex must be set before the graph state machine can be built.
     *
     * @param vertex The vertex to start at. Must be a vertex in the graph.
     */
    fun startAtVertex(vertex: V) {
        graphStateMachineBuilder.startVertex = vertex
    }


    /**
     * *In summary:* The default is `false`, in which case transition behaviour is not impacted and the concept of traversal bounds can be ignored.
     * Depending on your use case, you may want to and can ignore this.
     * If set to `true`, a previous or next action will move the out-of-bounds state back in bounds with an additional action required to go to the previous or next vertex.
     *
     * *In more detail:* Configures the state machine's behavior when reaching out-of-bounds states [mdk.gsm.state.TraversalBounds.BeforeFirst] or [mdk.gsm.state.TraversalBounds.BeyondLast].
     * Out-of-bounds means nowhere else to go in the graph.
     *
     * Being out-of-bounds for the state machine (as flagged by the [mdk.gsm.state.TraversalBounds] property of [mdk.gsm.state.TraversalState])
     * is basically a null state, but with the benefit of knowing the last vertex, and the direction the state was moved in.
     *
     * The current state of the graph state machine is always non-null, but when out of bounds, and [explicitlyTransitionIntoBounds] is `true`,
     * this could arguably be considered a kind of null object representation depending on how you want to interpret it via your use case.
     *
     * Callers can choose to ignore the [mdk.gsm.state.TraversalBounds] and forget about this mechanism, or treat being out of bounds as a distinct state.
     *
     * If [explicitlyTransitionIntoBounds] is `true`, the state machine will treat moving back into bounds as a standalone distinct transition, moving to a
     * valid in-bounds state (*on the same vertex*) upon the next dispatched action.
     *
     * This means the [GraphStateMachine] will stay on the same vertex, but simply move into a traversal state which is in-bounds i.e.
     * only the [mdk.gsm.state.TraversalBounds] property will be updated as being back into bounds: ([mdk.gsm.state.TraversalBounds.WithinBounds]).
     *
     * This can be useful to demarcate a process being "uninitialized" or "finished" for example, such as in the case of a completed workflow (workflows are typically a directed acyclic graph with a definitive 'ending') for instance,
     * allowing callers to respond to the workflow being finished.
     *
     * @param explicitlyTransitionIntoBounds `true` to enable automatic transitions back into a valid state on the same vertex when
     *        out of bounds, `false` to keep the state machine at the out-of-bounds state (default).
     */
    fun explicitlyTransitionIntoBounds(explicitlyTransitionIntoBounds : Boolean) {
        graphStateMachineBuilder.explicitlyTransitionIntoBounds = explicitlyTransitionIntoBounds
    }
}

class GraphStateMachineBuilder<V, I, F> @PublishedApi internal constructor() where V : IVertex<I>, F : ITraversalGuardState {
    var graph : Graph<V, I, F>? = null
    var startVertex : V? = null
    var traversalGuardState : F? = null
    var traversalType : EdgeTraversalType = EdgeTraversalType.DFSAcyclic
    var explicitlyTransitionIntoBounds : Boolean = false

    @PublishedApi
    internal fun build(): GraphStateMachine<V, I, F> {
        val _graph = graph
        val _startVertex = startVertex
        val _traversalGuardState = this@GraphStateMachineBuilder.traversalGuardState

        check(_graph != null) {
            "The workflow graph must be defined."
        }

        check(_startVertex != null) {
            "The start vertex must be defined."
        }

        check(_graph.containsVertex(_startVertex)) {
            buildString {
                append("The graph must contain the start vertex. ")
                appendLine()
                append("The start vertex with stepId '${_startVertex.id}' does not exist in the graph.")
            }
        }

        check(_traversalGuardState != null) {
            "The traversal guard state must be initialised"
        }

        val graphTraversal = GraphTraversalFactory.buildGraphTraversal(
            graph = _graph,
            startVertex = _startVertex,
            traversalType = traversalType
        )

        return GraphStateMachine(
            graphTraversal = graphTraversal,
            gsmConfig = GsmConfig(explicitlyTransitionIntoBounds),
            edgeTraversalType = traversalType,
            traversalGuardState = _traversalGuardState,
        )
    }
}
