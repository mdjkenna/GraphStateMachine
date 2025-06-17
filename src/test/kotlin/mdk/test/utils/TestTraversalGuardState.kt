package mdk.test.utils

import mdk.gsm.state.ITransitionGuardState

class Test15VertexTransitionArgs : ITransitionGuardState {
    @Volatile
    var blockedFrom3To2 = false

    @Volatile
    var blockedFrom8To9 = false

    override fun onReset() {
        blockedFrom3To2 = false
        blockedFrom8To9 = false
    }
}