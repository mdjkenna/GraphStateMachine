@file:Suppress("unused")

package mdk.gsm.builder

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.VertexContainer
import mdk.gsm.state.ITraversalGuardState

@DslMarker
internal annotation class GsmBuilderScope

/**
 * Build a workflow graph
 * Each step added to the workflow must have a unique step id
 */
@GsmBuilderScope
fun <V, I, F> buildGraphOnly(
    scopeConsumer : GraphBuilderScope<V, I, F>.() -> Unit
) : Graph<V, I, F> where V : IVertex<I>, F : ITraversalGuardState {
    val graphBuilder = GraphBuilder<V, I, F>()
    val graphBuilderScope = GraphBuilderScope(graphBuilder)
    scopeConsumer(graphBuilderScope)

    return graphBuilder.build()
}

@GsmBuilderScope
class GraphBuilderScope<V, I, F> internal constructor(
    private val workflowGraphGraphBuilder: GraphBuilder<V, I, F>
) where V : IVertex<I>, F : ITraversalGuardState
{
    /**
     * Add a vertex to the graph
     *
     * @param vertex The vertex to add
     * @param scopeConsumer The scope consumer to build the vertex
     */
    fun addVertex(
        vertex: V,
        scopeConsumer: VertexBuilderScope<V, I, F>.() -> Unit = {}
    ) {
        val vertexContainerBuilder = VertexBuilder<V, I, F>(vertex)
        val vertexBuilderScope = VertexBuilderScope(vertexContainerBuilder)
        scopeConsumer(vertexBuilderScope)

        workflowGraphGraphBuilder.add(vertexContainerBuilder.build())
    }

    /**
     * Shorthand for [addVertex]
     *
     * @see addVertex
     */
    fun v(
        vertex: V,
        scopeConsumer: VertexBuilderScope<V, I, F>.() -> Unit = {}
    ) = addVertex(vertex, scopeConsumer)
}

internal class GraphBuilder<V, I, F> where V : IVertex<I>, F : ITraversalGuardState {
    private val map = HashMap<I, VertexContainer<V, I, F>>()

    fun add(vertexContainer: VertexContainer<V, I, F>) {
        val existingValue = map.put(vertexContainer.vertex.id, vertexContainer)
        check(existingValue == null) {
            "A vertex with the id ${vertexContainer.vertex.id} already exists in the graph."
        }
    }

    fun build() : Graph<V, I, F> {
        assertNoDanglingEdges()

        return Graph(map)
    }

    private fun assertNoDanglingEdges() {
        map.forEach { _, vertexContainer ->
            vertexContainer.adjacentOrdered.forEach { edge ->
                checkNotNull(map[edge.to]) {
                    "The vertex pointed to by the edge from ${vertexContainer.vertex.id} to ${edge.to} is missing"
                }
            }
        }
    }
}