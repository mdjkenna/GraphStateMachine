package mdk.test.utils

import mdk.gsm.state.ITraversalGuardState

class TestTraversalGuardState : ITraversalGuardState {
    var blockedGoingTo2 = false
    var blockedGoingTo3 = false
    var blockedGoingTo7 = false
    var blockedGoingTo5 = false

    override fun onReset() {
        blockedGoingTo2 = false
        blockedGoingTo3 = false
        blockedGoingTo7 = false
        blockedGoingTo5 = false
    }
}

class Test15VertexTransitionArgs : ITraversalGuardState{
    var blockedFrom3To2 = false
    var blockedFrom8To9 = false

    override fun onReset() {
        blockedFrom3To2 = false
        blockedFrom8To9 = false
    }
}