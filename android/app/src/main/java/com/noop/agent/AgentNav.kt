package com.noop.agent

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * Process-local hand-off from an agent `open_screen` (or its notification tap) to the composed NOOP
 * shell. AppRoot collects [pending], navigates, then calls [landed] so the agent gets a real receipt
 * instead of "dispatched".
 */
object AgentNav {
    data class Request(val id: Long, val screen: String)

    private val ids = AtomicLong(0)
    private val state = MutableStateFlow<Request?>(null)
    val pending: StateFlow<Request?> = state
    @Volatile private var latch: Pair<Long, CountDownLatch>? = null

    const val EXTRA_SCREEN = "com.noop.agent.SCREEN"

    fun request(screen: String): Request {
        require(screen in AgentPolicy.SCREENS)
        val r = Request(ids.incrementAndGet(), screen)
        latch = r.id to CountDownLatch(1)
        state.value = r
        return r
    }

    /** Called by the shell after it navigated for [id]. */
    fun landed(id: Long) {
        if (state.value?.id == id) state.value = null
        latch?.takeIf { it.first == id }?.second?.countDown()
    }

    fun awaitLanded(id: Long, timeoutMs: Long): Boolean =
        latch?.takeIf { it.first == id }?.second?.await(timeoutMs, TimeUnit.MILLISECONDS) ?: false
}
