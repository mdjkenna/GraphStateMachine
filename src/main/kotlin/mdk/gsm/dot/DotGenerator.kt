package mdk.gsm.dot

import mdk.gsm.dot.decorations.EdgeDecoration
import mdk.gsm.dot.decorations.TransitionGuardDecoration
import mdk.gsm.dot.decorations.VertexDecoration
import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.state.ITransitionGuardState

/**
 * Generator for DOT language representation of a Graph State Machine.
 * This allows visualization of the state machine using tools like Graphviz.
 *
 * @param V The type of vertices in the graph. Must implement [IVertex].
 * @param I The type of vertex identifiers.
 * @param F The type of transition guard state. Must implement [ITransitionGuardState].
 * @param A The type of arguments that can be passed with actions.
 * @param config Configuration for DOT graph layout and appearance.
 */
class DotGenerator<V, I, F, A>(
    private val config: DotConfig = DotConfig()
) where V : IVertex<I>, F : ITransitionGuardState {

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
    fun decorateVertex(vertexId: I, decoration: VertexDecoration): DotGenerator<V, I, F, A> {
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
    fun decorateEdge(fromId: I, toId: I, decoration: EdgeDecoration): DotGenerator<V, I, F, A> {
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
    fun decorateTransitionGuard(fromId: I, toId: I, decoration: TransitionGuardDecoration): DotGenerator<V, I, F, A> {
        guardDecorations[Pair(fromId, toId)] = decoration
        return this
    }

    /**
     * Generate DOT language representation of the graph.
     *
     * @param graph The graph to visualize
     * @param graphName Optional name for the graph
     * @param dotConfig Optional configuration to override the default config
     * @return String containing the DOT language representation
     */
    fun generateDot(
        graph: Graph<V, I, F, A>, 
        graphName: String = "GraphStateMachine",
        dotConfig: DotConfig = config
    ): String {
        val sb = StringBuilder()

        sb.appendLine("digraph $graphName {")

        sb.appendLine("  node [shape=circle];")
        sb.appendLine("  edge [headclip=true, tailclip=true, arrowsize=0.8];")
        sb.appendLine("  rankdir=${dotConfig.rankDir};")
        sb.appendLine("  ratio=${dotConfig.ratio};")

        dotConfig.layout?.let { layout ->
            sb.appendLine("  layout=$layout;")
        }

        graph.getAllVertices().forEach { vertex ->
            val vertexId = vertex.id

            val decoration = vertexDecorations[vertexId]
            val label = if (decoration?.description != null) {
                "\"${vertexId}\\n${decoration.description}\""
            } else {
                "\"$vertexId\""
            }

            val vertexStyle = decoration?.style ?: dotConfig.defaultVertexDecoration.style
            val fillColor = decoration?.fillColor ?: dotConfig.defaultVertexDecoration.fillColor
            val borderColor = decoration?.borderColor ?: dotConfig.defaultVertexDecoration.borderColor
            val textColor = decoration?.textColor ?: dotConfig.defaultVertexDecoration.textColor
            val penWidth = decoration?.penWidth ?: dotConfig.defaultVertexDecoration.penWidth

            val colorAttr = "style=\"$vertexStyle\", fillcolor=\"$fillColor\", color=\"$borderColor\", fontcolor=\"$textColor\", penwidth=$penWidth"

            sb.appendLine("  \"$vertexId\" [label=$label, $colorAttr];")
        }

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
                    if (dotConfig.showEdgeIndices) {
                        append("[$index]")
                    }
                    if (edgeDecoration?.description != null) {
                        if (dotConfig.showEdgeIndices) {
                            append("\\n")
                        }
                        append("${edgeDecoration.description}")
                    }
                    append("\"")
                }

                val color = edgeDecoration?.color ?: dotConfig.defaultEdgeDecoration.color
                val penWidth = edgeDecoration?.penWidth ?: dotConfig.defaultEdgeDecoration.penWidth
                val fontColor = edgeDecoration?.fontColor ?: color
                val styleAttr = edgeDecoration?.style?.let { "style=\"$it\", " } ?: ""
                val colorAttr = "${styleAttr}color=\"$color\", fontcolor=\"$fontColor\", penwidth=$penWidth"

                if (guardDecoration != null) {
                    sb.appendLine("  \"$fromId\" -> \"$toId\" [label=$edgeLabel, labeltooltip=\"${guardDecoration.description}\", $colorAttr];")
                } else {
                    sb.appendLine("  \"$fromId\" -> \"$toId\" [label=$edgeLabel, $colorAttr];")
                }
            }
        }

        sb.appendLine("}")

        return sb.toString()
    }
}
