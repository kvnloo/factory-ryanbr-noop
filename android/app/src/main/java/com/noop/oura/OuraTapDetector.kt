package com.noop.oura

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Experimental, platform-pure motion-impulse classifier for Oura live-ACM samples.
 * Not a BLE decoder or a contact sensor. A time-boxed sensor session must be
 * explicitly armed and calibrated before any result may control an assistant.
 * Times use one monotonic clock (Android elapsedRealtime, in milliseconds).
 */
class OuraTapDetector(
    private val thresholdCounts: Double = 380.0,
    private val minSpacingMs: Long = 120,
    private val maxSpacingMs: Long = 430,
    private val quietMs: Long = 500,
    private val cooldownMs: Long = 1_600,
) {
    enum class Gesture { DOUBLE_TAP, TRIPLE_TAP, QUADRUPLE_TAP }

    private var gravityMagnitude: Double? = null
    private var elevated = false
    private var lastSampleMs = -1L
    private var lastImpulseMs = -1L
    private var impulses = 0
    private var blockedUntilMs = -1L

    init {
        require(thresholdCounts > 0 && thresholdCounts.isFinite())
        require(minSpacingMs > 0 && maxSpacingMs > minSpacingMs)
        require(quietMs > maxSpacingMs && cooldownMs > quietMs)
    }

    /** Feed raw accelerometer XYZ *after* the unverified BLE packet framing is validated. */
    fun sample(elapsedMs: Long, x: Int, y: Int, z: Int): Gesture? {
        if (elapsedMs < 0 || (lastSampleMs >= 0 && elapsedMs < lastSampleMs)) {
            reset()
            return null
        }
        if (lastSampleMs >= 0 && elapsedMs - lastSampleMs > 2_000) reset()
        val completed = flush(elapsedMs)
        lastSampleMs = elapsedMs
        val magnitude = sqrt(x.toDouble() * x + y.toDouble() * y + z.toDouble() * z)
        val prior = gravityMagnitude
        if (prior == null) {
            gravityMagnitude = magnitude
            return completed
        }
        val deviation = abs(magnitude - prior)
        if (deviation < thresholdCounts * 0.45) elevated = false
        if (deviation >= thresholdCounts && !elevated) {
            elevated = true
            if (elapsedMs >= blockedUntilMs) {
                if (lastImpulseMs < 0 || elapsedMs - lastImpulseMs > maxSpacingMs) {
                    impulses = 1
                    lastImpulseMs = elapsedMs
                } else if (elapsedMs - lastImpulseMs >= minSpacingMs) {
                    impulses++
                    lastImpulseMs = elapsedMs
                }
            }
        }
        // Prevent a tap spike from pulling the estimated gravity vector toward it.
        if (deviation < thresholdCounts) gravityMagnitude = prior + (magnitude - prior) * 0.06
        return completed
    }

    /** Wait for quiet before deciding double versus triple or quadruple tap. */
    fun flush(elapsedMs: Long): Gesture? {
        if (elapsedMs < 0 || lastImpulseMs < 0 || elapsedMs < lastImpulseMs ||
            elapsedMs - lastImpulseMs < quietMs) return null
        val detected = when (impulses) {
            2 -> Gesture.DOUBLE_TAP
            3 -> Gesture.TRIPLE_TAP
            4 -> Gesture.QUADRUPLE_TAP
            else -> null
        }
        impulses = 0
        lastImpulseMs = -1L
        if (detected != null) blockedUntilMs = elapsedMs + cooldownMs
        return detected
    }

    /** Reset on disconnect, Bluetooth shutdown, capture expiry, or user disarm. */
    fun reset() {
        gravityMagnitude = null
        elevated = false
        lastSampleMs = -1L
        lastImpulseMs = -1L
        impulses = 0
        blockedUntilMs = -1L
    }
}
