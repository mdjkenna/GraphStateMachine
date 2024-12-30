package mdk.gsm.graph

/**
 * To make a class usable in a graph, it must implement this interface.
 * @property id The unique identifier of the vertex. It is vital this is unique within the graph.
 */
interface IVertex {
    val id : String
}