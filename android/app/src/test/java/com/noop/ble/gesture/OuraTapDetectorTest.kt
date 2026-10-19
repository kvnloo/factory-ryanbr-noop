package com.noop.ble.gesture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Synthetic impulse-only tests; none of these are calibrated Gen 3 BLE captures. */
class OuraTapDetectorTest {
    private fun still(detector: OuraTapDetector, ms: Long) {
        detector.sample(ms, 1024, 0, 0)
    }

    private fun tap(detector: OuraTapDetector, ms: Long) {
        still(detector, ms)
        detector.sample(ms + 20, 1800, 0, 0)
        still(detector, ms + 40)
    }

    @Test fun aDoubleTapWaitsForTheQuietWindow() {
        val detector = OuraTapDetector()
        still(detector, 0)
        tap(detector, 100)
        tap(detector, 340)
        assertNull(detector.flush(800))
        assertEquals(OuraTapDetector.Gesture.DOUBLE_TAP, detector.flush(900))
        assertNull(detector.flush(1_000))
    }

    @Test fun aThirdOrFourthTapDoesNotPrematurelyEmitDouble() {
        val detector = OuraTapDetector()
        still(detector, 0)
        tap(detector, 100)
        tap(detector, 330)
        tap(detector, 560)
        assertEquals(OuraTapDetector.Gesture.TRIPLE_TAP, detector.flush(1_200))

        detector.reset()
        still(detector, 0)
        tap(detector, 100)
        tap(detector, 330)
        tap(detector, 560)
        tap(detector, 790)
        assertEquals(OuraTapDetector.Gesture.QUADRUPLE_TAP, detector.flush(1_350))
    }

    @Test fun singlesRapidBounceAndQuietMovementDoNotTrigger() {
        val detector = OuraTapDetector()
        still(detector, 0)
        tap(detector, 100)
        tap(detector, 170) // 70 ms spacing is below the 120 ms refractory window
        assertNull(detector.flush(900))
        detector.reset()
        still(detector, 0)
        for (ms in 20L..1_000L step 20) {
            detector.sample(ms, 1024 + (ms % 80).toInt(), 0, 0)
        }
        assertNull(detector.flush(2_000))
    }

    @Test fun emittedGestureIsCooldownGated() {
        val detector = OuraTapDetector()
        still(detector, 0)
        tap(detector, 100)
        tap(detector, 340)
        assertEquals(OuraTapDetector.Gesture.DOUBLE_TAP, detector.flush(900))
        tap(detector, 1_200)
        tap(detector, 1_480)
        assertNull(detector.flush(2_050))
        tap(detector, 2_800)
        tap(detector, 3_030)
        assertEquals(OuraTapDetector.Gesture.DOUBLE_TAP, detector.flush(3_600))
    }

    @Test fun resetOrClockRollbackDiscardsAnUnfinishedBurst() {
        val detector = OuraTapDetector()
        still(detector, 0)
        tap(detector, 100)
        detector.reset() // e.g., BLE disconnect
        assertNull(detector.flush(1_000))
        still(detector, 1_200)
        tap(detector, 1_300)
        detector.sample(900, 1024, 0, 0) // monotonic clock regression
        assertNull(detector.flush(2_100))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidCalibrationIsRejected() {
        OuraTapDetector(thresholdCounts = -1.0)
    }
}
