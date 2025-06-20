package mdk.gsm.util

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.state.ITransitionGuardState

/**
 * Decoration for vertices in the dot file.
 *
 * @property description Optional description text to display with the vertex ID
 * @property color Optional color for the vertex (in dot format, e.g., "red", "#FF0000")
 */
data class VertexDecoration(
    val description: String? = null,
    val color: String? = null
)

/**
 * Decoration for edges in the dot file.
 *
 * @property description Optional description text to display with the edge
 * @property color Optional color for the edge (in dot format, e.g., "blue", "#0000FF")
 */
data class EdgeDecoration(
    val description: String? = null,
    val color: String? = null
)

/**
 * Decoration for transition guards in the dot file.
 *
 * @property description Description text to display for the transition guard
 */
data class TransitionGuardDecoration(
    val description: String
)

/**
 * Generator for DOT language representation of a Graph State Machine.
 * This allows visualization of the state machine using tools like Graphviz.
 *
 * @param V The type of vertices in the graph. Must implement [IVertex].
 * @param I The type of vertex identifiers.
 * @param F The type of transition guard state. Must implement [ITransitionGuardState].
 * @param A The type of arguments that can be passed with actions.
 */
class GsmDotGenerator<V, I, F, A> where V : IVertex<I>, F : ITransitionGuardState {

    private val vertexDecorations = mutableMapOf<I, VertexDecoration>()
    private val edgeDecorations = mutableMapOf<Pair<I, I>, EdgeDecoration>()
    private val guardDecorations = mutableMapOf<Pair<I, I>, TransitionGuardDecoration>()

    /**
     * Set decoration for a vertex.
     *
     * @param vertexId The ID of the vertex to decorate
     * @param decoration The decoration to apply
     * @return This generator instance for method chaining
     */
    fun decorateVertex(vertexId: I, decoration: VertexDecoration): GsmDotGenerator<V, I, F, A> {
        vertexDecorations[vertexId] = decoration
        return this
    }

    /**
     * Set decoration for an edge.
     *
     * @param fromId The ID of the source vertex
     * @param toId The ID of the destination vertex
     * @param decoration The decoration to apply
     * @return This generator instance for method chaining
     */
    fun decorateEdge(fromId: I, toId: I, decoration: EdgeDecoration): GsmDotGenerator<V, I, F, A> {
        edgeDecorations[Pair(fromId, toId)] = decoration
        return this
    }

    /**
     * Set decoration for a transition guard.
     *
     * @param fromId The ID of the source vertex
     * @param toId The ID of the destination vertex
     * @param decoration The decoration to apply
     * @return This generator instance for method chaining
     */
    fun decorateTransitionGuard(fromId: I, toId: I, decoration: TransitionGuardDecoration): GsmDotGenerator<V, I, F, A> {
        guardDecorations[Pair(fromId, toId)] = decoration
        return this
    }

    /**
     * Generate DOT language representation of the graph.
     *
     * @param graph The graph to visualize
     * @param graphName Optional name for the graph
     * @return String containing the DOT language representation
     */
    fun generateDot(graph: Graph<V, I, F, A>, graphName: String = "GraphStateMachine"): String {
        val sb = StringBuilder()

        // Start digraph
        sb.appendLine("digraph $graphName {")

        // Global graph settings
        sb.appendLine("  node [shape=circle];")
        sb.appendLine("  rankdir=LR;")

        // First pass: define all vertices
        graph.getAllVertices().forEach { vertex ->
            val vertexId = vertex.id

            val decoration = vertexDecorations[vertexId]
            val label = if (decoration?.description != null) {
                "\"${vertexId}\\n${decoration.description}\""
            } else {
                "\"$vertexId\""
            }

            val colorAttr = decoration?.color?.let { "color=\"$it\", fontcolor=\"$it\"" } ?: ""

            sb.appendLine("  \"$vertexId\" [label=$label $colorAttr];")
        }

        // Second pass: define all edges
        graph.getAllVertices().forEach { vertex ->
            val fromId = vertex.id
            val edges = graph.getOutgoingEdgesSorted(vertex) ?: emptyList()

            edges.forEachIndexed { index, edge ->
                val toId = edge.to
                val edgeKey = Pair(fromId, toId)

                val edgeDecoration = edgeDecorations[edgeKey]
                val guardDecoration = guardDecorations[edgeKey]

                val edgeLabel = buildString {
                    append("\"")
                    append("[$index]")
                    if (edgeDecoration?.description != null) {
                        append("\\n${edgeDecoration.description}")
                    }
                    append("\"")
                }

                val colorAttr = edgeDecoration?.color?.let { "color=\"$it\", fontcolor=\"$it\"" } ?: ""

                if (guardDecoration != null) {
                    // Edge with guard label
                    sb.appendLine("  \"$fromId\" -> \"$toId\" [label=$edgeLabel, labeltooltip=\"${guardDecoration.description}\", $colorAttr];")
                } else {
                    // Edge without guard label
                    sb.appendLine("  \"$fromId\" -> \"$toId\" [label=$edgeLabel $colorAttr];")
                }
            }
        }

        // End digraph
        sb.appendLine("}")

        return sb.toString()
    }
}
