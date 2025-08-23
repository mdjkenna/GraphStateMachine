package mdk.gsm.scope

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.*
import java.util.concurrent.Executors

object GraphStateMachineScopeFactory {
    private val GraphStateMachineDispatcher by lazy {
        val singleThreadExecutor = Executors.newSingleThreadExecutor { r ->
            Thread(r, "GraphStateMachine-Thread-${UUID.randomUUID().toString()}")
        }

        singleThreadExecutor.asCoroutineDispatcher()
    }

    fun newScope(dispatcher : CoroutineDispatcher? = null): CoroutineScope {
        val coroutineDispatcher = dispatcher
            ?: GraphStateMachineDispatcher

        return CoroutineScope(coroutineDispatcher + SupervisorJob())
    }
}