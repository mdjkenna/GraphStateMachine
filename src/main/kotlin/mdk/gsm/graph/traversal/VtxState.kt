package mdk.gsm.graph.traversal

internal object VtxState {

    const val GRAY_INITIAL = 0
    const val WHITE = -1
    const val BLACK = -2

    fun isWhite(vtxState : Int?): Boolean {
        return vtxState == null || vtxState == WHITE
    }

    fun isBlack(vtxState : Int?) : Boolean {
        return if (vtxState == null) {
            false
        } else {
            (vtxState <= BLACK)
        }
    }

    fun currentEdgeOrZero(vtxState: Int?) : Int {
        return if (vtxState == null || vtxState < 0) {
            0
        } else {
            vtxState
        }
    }
}
