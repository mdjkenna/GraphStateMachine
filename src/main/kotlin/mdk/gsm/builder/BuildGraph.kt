@file:Suppress("unused")

package mdk.gsm.builder

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.VertexContainer
import mdk.gsm.state.ITransitionGuardState

@DslMarker
internal annotation class GsmBuilderScope

/**
 * Build a graph only
 */
@GsmBuilderScope
fun <V, I, F, A> buildGraphOnly(
    scopeConsumer : GraphBuilderScope<V, I, F, A>.() -> Unit
) : Graph<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {
    val graphBuilder = GraphBuilder<V, I, F, A>()
    val graphBuilderScope = GraphBuilderScope(graphBuilder)
    scopeConsumer(graphBuilderScope)

    return graphBuilder.build()
}

@GsmBuilderScope
class GraphBuilderScope<V, I, F, A> internal constructor(
    private val workflowGraphGraphBuilder: GraphBuilder<V, I, F, A>
) where V : IVertex<I>, F : ITransitionGuardState
{
    /**
     * Add a vertex to the graph.
     * It must have a unique ID or an error will be thrown.
     *
     * @param vertex The vertex to add
     */
    fun addVertex(
        vertex: V,
        scopeConsumer: VertexBuilderScope<V, I, F, A>.() -> Unit = {}
    ) {
        val vertexContainerBuilder = VertexBuilder<V, I, F, A>(vertex)
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
        scopeConsumer: VertexBuilderScope<V, I, F, A>.() -> Unit = {}
    ) = addVertex(vertex, scopeConsumer)
}

internal class GraphBuilder<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {
    private val map = HashMap<I, VertexContainer<V, I, F, A>>()

    fun add(vertexContainer: VertexContainer<V, I, F, A>) {
        val existingValue = map.put(vertexContainer.vertex.id, vertexContainer)
        check(existingValue == null) {
            "A vertex with the id ${vertexContainer.vertex.id} already exists in the graph."
        }
    }

    fun build() : Graph<V, I, F, A> {
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