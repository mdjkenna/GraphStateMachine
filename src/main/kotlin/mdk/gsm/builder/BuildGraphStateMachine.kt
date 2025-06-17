@file:Suppress("unused")

package mdk.gsm.builder

import mdk.gsm.graph.Graph
import mdk.gsm.graph.IVertex
import mdk.gsm.graph.transition.traversal.EdgeTraversalType
import mdk.gsm.graph.transition.traversal.GraphTraversalFactory
import mdk.gsm.graph.transition.traversal.TraversalMediator
import mdk.gsm.graph.transition.walk.GraphWalkFactory
import mdk.gsm.state.GsmConfig
import mdk.gsm.state.GsmController
import mdk.gsm.state.ITransitionGuardState

@PublishedApi
internal class GraphStateMachineBuilder<V, I, F, A> @PublishedApi internal constructor() where V : IVertex<I>, F : ITransitionGuardState {
    var graph : Graph<V, I, F, A>? = null
    var startVertex : V? = null
    var transitionGuardState : F? = null
    var traversalType : EdgeTraversalType = EdgeTraversalType.DFSAcyclic
    var explicitlyTransitionIntoBounds : Boolean = false
    var useStatelessWalk : Boolean = false

    @PublishedApi
    internal fun build(): GsmController<V, I, F, A> {
        val _graph = graph
        val _startVertex = startVertex
        val _transitionGuardState = this@GraphStateMachineBuilder.transitionGuardState

        check(_graph != null) {
            "The workflow graph must be defined."
        }

        check(_startVertex != null) {
            "The start vertex must be defined."
        }

        check(_graph.containsVertex(_startVertex)) {
            buildString {
                append("The graph must contain the start vertex. ")
                appendLine()
                append("The start vertex with stepId '${_startVertex.id}' does not exist in the graph.")
            }
        }

        check(_transitionGuardState != null) {
            "The transition guard state must be initialised"
        }

        val graphTraversal = if (useStatelessWalk) {
            GraphWalkFactory.buildStatelessGraphWalk(
                graph = _graph,
                startVertex = _startVertex
            )
        } else {
            GraphTraversalFactory.buildGraphTraversal(
                graph = _graph,
                startVertex = _startVertex,
                traversalType = traversalType
            )
        }

        return GsmController(
            graphTraversalMediator = TraversalMediator(
                graphTraversal = graphTraversal,
                transitionGuardState = _transitionGuardState,
                gsmConfig = GsmConfig(explicitlyTransitionIntoBounds)
            )
        )
    }
}
