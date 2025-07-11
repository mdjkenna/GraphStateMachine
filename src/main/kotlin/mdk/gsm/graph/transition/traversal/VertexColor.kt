package mdk.gsm.graph.transition.traversal

internal object VertexColor {

    const val GRAY = 0
    const val BLACK = 1

    fun isBlack(vtxState : Int) : Boolean {
        return vtxState == BLACK
    }
}
