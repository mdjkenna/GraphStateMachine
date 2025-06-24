package mdk.gsm.dot.decorations

import mdk.gsm.dot.DotDefaults

/**
 * Decoration for vertices in the dot file.
 *
 * @property description Optional description text to display with the vertex ID
 * @property fillColor Optional fill color for the vertex (in dot format, e.g., "red", "#FF0000")
 * @property borderColor Optional border color for the vertex (in dot format, e.g., "black", "#000000")
 * @property textColor Optional text color for the vertex content (in dot format, e.g., "white", "#FFFFFF")
 * @property style Optional style for the vertex (e.g., "filled", "dashed", "dotted")
 * @property penWidth Optional thickness of the vertex border
 */
data class VertexDecoration(
    val description: String? = null,
    val fillColor: String? = null,
    val borderColor: String = DotDefaults.DEFAULT_VERTEX_BORDER_COLOR,
    val textColor: String = DotDefaults.DEFAULT_VERTEX_TEXT_COLOR,
    val style: String = DotDefaults.DEFAULT_VERTEX_STYLE,
    val penWidth: Double = DotDefaults.DEFAULT_VERTEX_PEN_WIDTH
)