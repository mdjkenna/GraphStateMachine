@file:Suppress("unused")

package mdk.gsm.builder

import mdk.gsm.graph.IVertex
import mdk.gsm.graph.VertexContainer
import mdk.gsm.graph.Graph
import mdk.gsm.state.IEdgeTransitionFlags

@DslMarker
internal annotation class GsmBuilderScope

/**
 * Build a workflow graph
 * Each step added to the workflow must have a unique step id
 */
@GsmBuilderScope
fun <V, F> buildGraphOnly(
    scopeConsumer : GraphBuilderScope<V, F>.() -> Unit
) : Graph<V, F> where V : IVertex, F : IEdgeTransitionFlags {
    val graphBuilder = GraphBuilder<V, F>()
    val graphBuilderScope = GraphBuilderScope(graphBuilder)
    scopeConsumer(graphBuilderScope)

    return graphBuilder.build()
}

@GsmBuilderScope
class GraphBuilderScope<V, F> internal constructor(
    private val workflowGraphGraphBuilder: GraphBuilder<V, F>
) where V : IVertex, F : IEdgeTransitionFlags
{
    /**
     * Add a vertex to the graph
     *
     * @param vertex The vertex to add
     * @param scopeConsumer The scope consumer to build the vertex
     */
    fun addVertex(
        vertex: V,
        scopeConsumer: VertexBuilderScope<V, F>.() -> Unit = {}
    ) {
        val vertexContainerBuilder = VertexBuilder<V, F>(vertex)
        val vertexBuilderScope = VertexBuilderScope(vertexContainerBuilder)
        scopeConsumer(vertexBuilderScope)
        workflowGraphGraphBuilder.add(vertexContainerBuilder.build())
    }
}

internal class GraphBuilder<V, F> where V : IVertex, F : IEdgeTransitionFlags {
    private val map = HashMap<String, VertexContainer<V, F>>()

    fun add(vertexContainer: VertexContainer<V, F>) {
        val existingValue = map.put(vertexContainer.vertex.id, vertexContainer)
        check(existingValue == null) {
            "A vertex with the id ${vertexContainer.vertex.id} already exists in the graph."
        }
    }

    fun build() : Graph<V, F> {
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