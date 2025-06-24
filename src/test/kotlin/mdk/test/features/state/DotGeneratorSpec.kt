package mdk.test.features.state

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import mdk.gsm.builder.buildGraphOnly
import mdk.gsm.dot.DotConfig
import mdk.gsm.dot.DotDefaults
import mdk.gsm.dot.DotGenerator
import mdk.gsm.dot.decorations.EdgeDecoration
import mdk.gsm.dot.decorations.TransitionGuardDecoration
import mdk.gsm.dot.decorations.VertexDecoration
import mdk.gsm.graph.IVertex
import mdk.gsm.state.ITransitionGuardState

class DotGeneratorSpec : BehaviorSpec({

    given("A graph only") {
        data class StringVertex(override val id: String) : IVertex<String>

        class SimpleGuardState : ITransitionGuardState {
            override fun onReset() {}
        }

        val graph = buildGraphOnly<StringVertex, String, SimpleGuardState, Unit> {

            v(StringVertex("A")) {
                addEdge {
                    setTo("B")
                }
                addEdge {
                    setTo("C")
                }
            }

            v(StringVertex("B")) {
                addEdge {
                    setTo("D")
                }
            }

            v(StringVertex("C")) {
                addEdge {
                    setTo("D")
                }
            }

            v(StringVertex("D"))
        }

        `when`("Generating a DOT representation without decorations") {
            val dotGenerator = DotGenerator<StringVertex, String, SimpleGuardState, Unit>(
                DotConfig(showEdgeIndices = true)
            )
            val dotContent = dotGenerator.generateDot(graph)

            then("The DOT content should contain all vertices and edges with default styling") {
                println("[DEBUG_LOG] DOT content without decorations:\n$dotContent")

                // Check default vertex styling
                dotContent shouldContain "\"A\" [label=\"A\""
                dotContent shouldContain "style=\"${DotDefaults.DEFAULT_VERTEX_STYLE}\""
                dotContent shouldContain "fillcolor=\"${DotDefaults.DEFAULT_VERTEX_FILL_COLOR}\""
                dotContent shouldContain "color=\"${DotDefaults.DEFAULT_VERTEX_BORDER_COLOR}\""
                dotContent shouldContain "fontcolor=\"${DotDefaults.DEFAULT_VERTEX_TEXT_COLOR}\""
                dotContent shouldContain "penwidth=${DotDefaults.DEFAULT_VERTEX_PEN_WIDTH}"

                // Check other vertices
                dotContent shouldContain "\"B\" [label=\"B\""
                dotContent shouldContain "\"C\" [label=\"C\""
                dotContent shouldContain "\"D\" [label=\"D\""

                // Check edges
                dotContent shouldContain "\"A\" -> \"B\" [label=\"[0]\", color=\"${DotDefaults.DEFAULT_EDGE_COLOR}\", fontcolor=\"${DotDefaults.DEFAULT_EDGE_COLOR}\", penwidth=${DotDefaults.DEFAULT_EDGE_PEN_WIDTH}];"
                dotContent shouldContain "\"A\" -> \"C\" [label=\"[1]\", color=\"${DotDefaults.DEFAULT_EDGE_COLOR}\", fontcolor=\"${DotDefaults.DEFAULT_EDGE_COLOR}\", penwidth=${DotDefaults.DEFAULT_EDGE_PEN_WIDTH}];"
                dotContent shouldContain "\"B\" -> \"D\" [label=\"[0]\", color=\"${DotDefaults.DEFAULT_EDGE_COLOR}\", fontcolor=\"${DotDefaults.DEFAULT_EDGE_COLOR}\", penwidth=${DotDefaults.DEFAULT_EDGE_PEN_WIDTH}];"
                dotContent shouldContain "\"C\" -> \"D\" [label=\"[0]\", color=\"${DotDefaults.DEFAULT_EDGE_COLOR}\", fontcolor=\"${DotDefaults.DEFAULT_EDGE_COLOR}\", penwidth=${DotDefaults.DEFAULT_EDGE_PEN_WIDTH}];"
            }
        }

        `when`("Generating a DOT representation with basic decorations") {
            val dotGenerator = DotGenerator<StringVertex, String, SimpleGuardState, Unit>(
                DotConfig(showEdgeIndices = true)
            )
                .decorateVertex("A", VertexDecoration(description = "Start Node", fillColor = "blue"))
                .decorateVertex("D", VertexDecoration(description = "End Node", fillColor = "red"))
                .decorateEdge("A", "B", EdgeDecoration(description = "Path 1", color = "green"))
                .decorateEdge("A", "C", EdgeDecoration(description = "Path 2", color = "orange"))
                .decorateTransitionGuard("B", "D", TransitionGuardDecoration(description = "Guard condition"))

            val dotContent = dotGenerator.generateDot(graph, "DecoratedGraph")

            then("The DOT content should contain all decorations") {
                // Check vertex A with decoration
                dotContent shouldContain "\"A\" [label=\"A\\nStart Node\""
                dotContent shouldContain "style=\"filled\""
                dotContent shouldContain "fillcolor=\"blue\""
                dotContent shouldContain "color=\"${DotDefaults.DEFAULT_VERTEX_BORDER_COLOR}\""
                dotContent shouldContain "fontcolor=\"${DotDefaults.DEFAULT_VERTEX_TEXT_COLOR}\""

                dotContent shouldContain "\"D\" [label=\"D\\nEnd Node\""
                dotContent shouldContain "fillcolor=\"red\""

                dotContent shouldContain "\"A\" -> \"B\" [label=\"[0]\\nPath 1\", color=\"green\", fontcolor=\"green\", penwidth=${DotDefaults.DEFAULT_EDGE_PEN_WIDTH}];"
                dotContent shouldContain "\"A\" -> \"C\" [label=\"[1]\\nPath 2\", color=\"orange\", fontcolor=\"orange\", penwidth=${DotDefaults.DEFAULT_EDGE_PEN_WIDTH}];"

                dotContent shouldContain "\"B\" -> \"D\" [label=\"[0]\", labeltooltip=\"Guard condition\", color=\"${DotDefaults.DEFAULT_EDGE_COLOR}\", fontcolor=\"${DotDefaults.DEFAULT_EDGE_COLOR}\", penwidth=${DotDefaults.DEFAULT_EDGE_PEN_WIDTH}];"
            }
        }

        `when`("Generating a DOT representation with custom vertex styling") {
            // Create a custom config with different default vertex decoration
            val customConfig = DotConfig(
                defaultVertexDecoration = VertexDecoration(
                    fillColor = "#336699",
                    borderColor = "#FF0000",
                    textColor = "#FFFF00",
                    style = "filled,dashed",
                    penWidth = 3.0
                )
            )

            val customVertexDecoration = VertexDecoration(
                description = "Custom Styled",
                fillColor = "purple",
                borderColor = "green",
                textColor = "orange",
                style = "filled,bold",
                penWidth = 4.0
            )

            val dotGenerator = DotGenerator<StringVertex, String, SimpleGuardState, Unit>(customConfig)
                .decorateVertex("A", customVertexDecoration)

            val dotContent = dotGenerator.generateDot(graph, "CustomStyledGraph")

            then("The DOT content should contain custom styling") {
                dotContent shouldContain "\"A\" [label=\"A\\nCustom Styled\""
                dotContent shouldContain "style=\"filled,bold\""
                dotContent shouldContain "fillcolor=\"purple\""
                dotContent shouldContain "color=\"green\""
                dotContent shouldContain "fontcolor=\"orange\""
                dotContent shouldContain "penwidth=4.0"

                dotContent shouldContain "\"B\" [label=\"B\""
                dotContent shouldContain "style=\"filled,dashed\""
                dotContent shouldContain "fillcolor=\"#336699\""
                dotContent shouldContain "color=\"#FF0000\""
                dotContent shouldContain "fontcolor=\"#FFFF00\""
                dotContent shouldContain "penwidth=3.0"
            }
        }
    }
})
