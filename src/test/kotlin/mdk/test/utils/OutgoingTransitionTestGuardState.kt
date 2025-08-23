package mdk.test.utils

import mdk.gsm.state.ITransitionGuardState

class OutgoingTransitionTestGuardState : ITransitionGuardState {
    @Volatile
    var shouldPreventTransition = false

    override fun onReset() {
        shouldPreventTransition = false
    }
}