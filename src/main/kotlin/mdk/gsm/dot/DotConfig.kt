package mdk.gsm.dot

import mdk.gsm.dot.decorations.EdgeDecoration
import mdk.gsm.dot.decorations.VertexDecoration

/**
 * Configuration for DOT graph layout and appearance.
 *
 * @property rankDir Direction of graph layout: "TB" (top to bottom), "LR" (left to right),
 *                   "BT" (bottom to top), or "RL" (right to left)
 * @property defaultVertexDecoration Default decoration for vertices if not specified individually
 * @property defaultEdgeDecoration Default decoration for edges if not specified individually
 * @property showEdgeIndices Whether to show indices on edges
 * @property layout Optional layout engine to use (e.g., "dot", "neato", "fdp", "sfdp", "circo", "twopi")
 * @property ratio Aspect ratio setting for the graph
 */
data class DotConfig(
    val rankDir: String = DotDefaults.DEFAULT_RANK_DIR,
    val defaultVertexDecoration: VertexDecoration = VertexDecoration(
        fillColor = DotDefaults.DEFAULT_VERTEX_FILL_COLOR
    ),
    val defaultEdgeDecoration: EdgeDecoration = EdgeDecoration(
        color = DotDefaults.DEFAULT_EDGE_COLOR,
        penWidth = DotDefaults.DEFAULT_EDGE_PEN_WIDTH
    ),
    val showEdgeIndices: Boolean = false,
    val layout: String? = null,
    val ratio: String = DotDefaults.DEFAULT_RATIO
)