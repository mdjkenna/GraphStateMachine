package mdk.test.utils

import mdk.gsm.state.IEdgeTransitionFlags

class TestEdgeTransitionFlags : IEdgeTransitionFlags {
    var blockedGoingTo2 = false
    var blockedGoingTo3 = false
    var blockedGoingTo7 = false
    var blockedGoingTo5 = false

    fun reset() {
        blockedGoingTo2 = false
        blockedGoingTo3 = false
        blockedGoingTo7 = false
        blockedGoingTo5 = false
    }
}

class Test15VertexTransitionFlags : IEdgeTransitionFlags{
    var blockedFrom3To2 = false
    var blockedFrom8To9 = false
}