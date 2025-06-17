package mdk.gsm.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.random.Random

object GraphStateMachineScopeFactory {
    private val GraphStateMachineDispatcher by lazy {
        val singleThreadExecutor = Executors.newSingleThreadExecutor { r ->
            Thread(r, "GraphStateMachine-Thread-${Random.nextInt(1, 900000)}")
        }

        singleThreadExecutor.asCoroutineDispatcher()
    }

    fun newScope(dispatcher : CoroutineDispatcher? = null): CoroutineScope {
        val coroutineDispatcher = dispatcher
            ?: GraphStateMachineDispatcher

        return CoroutineScope(coroutineDispatcher + SupervisorJob())
    }

    object GsmIdentityKey
}