package mdk.gsm.dot.decorations

import mdk.gsm.dot.DotDefaults

/**
 * Decoration for edges in the dot file.
 *
 * @property description Optional description text to display with the edge
 * @property color Optional color for the edge (in dot format, e.g., "blue", "#0000FF")
 * @property penWidth Optional thickness of the edge
 * @property style Optional style for the edge (e.g., "solid", "dashed", "dotted")
 * @property fontColor Optional color for the edge label text
 */
data class EdgeDecoration(
    val description: String? = null,
    val color: String? = null,
    val penWidth: Double = DotDefaults.DEFAULT_EDGE_PEN_WIDTH,
    val style: String? = null,
    val fontColor: String? = null
)