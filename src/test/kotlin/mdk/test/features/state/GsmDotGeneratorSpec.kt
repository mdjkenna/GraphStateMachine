package mdk.test.features.state

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import mdk.gsm.builder.buildGraphOnly
import mdk.gsm.graph.IVertex
import mdk.gsm.state.ITransitionGuardState
import mdk.gsm.util.EdgeDecoration
import mdk.gsm.util.GsmDotGenerator
import mdk.gsm.util.TransitionGuardDecoration
import mdk.gsm.util.VertexDecoration

class GsmDotGeneratorSpec : BehaviorSpec({

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
            val dotGenerator = GsmDotGenerator<StringVertex, String, SimpleGuardState, Unit>()
            val dotContent = dotGenerator.generateDot(graph)

            then("The DOT content should contain all vertices and edges") {
                println("[DEBUG_LOG] DOT content without decorations:\n$dotContent")

                dotContent shouldContain "\"A\" [label=\"A\" ];"
                dotContent shouldContain "\"B\" [label=\"B\" ];"
                dotContent shouldContain "\"C\" [label=\"C\" ];"
                dotContent shouldContain "\"D\" [label=\"D\" ];"

                dotContent shouldContain "\"A\" -> \"B\" [label=\"[0]\" ];"
                dotContent shouldContain "\"A\" -> \"C\" [label=\"[1]\" ];"
                dotContent shouldContain "\"B\" -> \"D\" [label=\"[0]\" ];"
                dotContent shouldContain "\"C\" -> \"D\" [label=\"[0]\" ];"
            }
        }

        `when`("Generating a DOT representation with decorations") {
            val dotGenerator = GsmDotGenerator<StringVertex, String, SimpleGuardState, Unit>()
                .decorateVertex("A", VertexDecoration("Start Node", "blue"))
                .decorateVertex("D", VertexDecoration("End Node", "red"))
                .decorateEdge("A", "B", EdgeDecoration("Path 1", "green"))
                .decorateEdge("A", "C", EdgeDecoration("Path 2", "orange"))
                .decorateTransitionGuard("B", "D", TransitionGuardDecoration("Guard condition"))

            val dotContent = dotGenerator.generateDot(graph, "DecoratedGraph")

            then("The DOT content should contain all decorations") {

                dotContent shouldContain "\"A\" [label=\"A\\nStart Node\" color=\"blue\", fontcolor=\"blue\"];"
                dotContent shouldContain "\"D\" [label=\"D\\nEnd Node\" color=\"red\", fontcolor=\"red\"];"

                dotContent shouldContain "\"A\" -> \"B\" [label=\"[0]\\nPath 1\" color=\"green\", fontcolor=\"green\"];"
                dotContent shouldContain "\"A\" -> \"C\" [label=\"[1]\\nPath 2\" color=\"orange\", fontcolor=\"orange\"];"

                dotContent shouldContain "labeltooltip=\"Guard condition\""
            }
        }
    }
})
