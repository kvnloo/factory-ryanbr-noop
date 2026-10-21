package com.noop.ble.gesture

import com.noop.ble.gesture.OuraGestureStandbyPolicy.Action
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.StringWriter

/** Always-listen decisions, the guided plan and its CSV rows, and frame-loss accounting (pure JVM). */
class OuraGestureStandbyTest {

    private fun next(
        enabled: Boolean = true, hold: Boolean = false, owns: Boolean = false, armed: Boolean = false,
        ready: Boolean = true, backoff: Boolean = true,
    ) = OuraGestureStandbyPolicy.next(enabled, hold, owns, armed, ready, backoff)

    @Test
    fun armsWhenOnAndTheRingIsReady() = assertEquals(Action.ARM, next())

    @Test
    fun reArmsAfterTheCaptureDroppedUnderneath() {
        // Link loss / re-auth / stall clears the capture: the owner no longer owns one and it is not armed.
        assertEquals(Action.ARM, next(owns = false, armed = false))
    }

    @Test
    fun waitsForTheRingAndForTheBackoff() {
        assertEquals(Action.IDLE, next(ready = false))
        assertEquals(Action.IDLE, next(backoff = false))
    }

    @Test
    fun neverStealsTheSetupScreensCapture() = assertEquals(Action.IDLE, next(owns = false, armed = true))

    @Test
    fun standsAsideForTheScreenAndWhenTurnedOff() {
        assertEquals(Action.DISARM, next(hold = true, owns = true, armed = true))
        assertEquals(Action.DISARM, next(enabled = false, owns = true, armed = true))
        assertEquals(Action.IDLE, next(enabled = false, owns = false, armed = true))
        assertEquals(Action.IDLE, next(enabled = false))
    }

    @Test
    fun backoffDoublesToACap() {
        assertEquals(10_000L, OuraGestureStandbyPolicy.backoffMs(1))
        assertEquals(20_000L, OuraGestureStandbyPolicy.backoffMs(2))
        assertEquals(120_000L, OuraGestureStandbyPolicy.backoffMs(9))
    }

    @Test
    fun oneDeliveryPerRefractoryWindow() {
        assertTrue(OuraGestureStandbyPolicy.deliverable(5_000, 0))
        assertFalse(OuraGestureStandbyPolicy.deliverable(6_000, 5_000))
        assertTrue(OuraGestureStandbyPolicy.deliverable(6_500, 5_000))
    }

    @Test
    fun guidedPlanPutsTheDrivingGesturesAndTonightsNegativesFirst() {
        val ids = OuraGuidedPlan.BLOCKS.map { it.id }
        assertEquals(listOf("double-normal", "triple-normal", "neg-handling", "neg-walking"), ids.take(4))
        assertEquals(ids.size, ids.toSet().size)
        // Every tap class at every intensity, rapid singles/doubles/triples at every intensity, five negatives.
        for (c in listOf("single", "double", "triple", "quadruple", "quintuple"))
            for (i in listOf("light", "normal", "firm")) assertTrue("$c-$i", "$c-$i" in ids)
        for (c in listOf("single", "double", "triple"))
            for (i in listOf("light", "normal", "firm")) assertTrue("rapid-$c-$i", "rapid-$c-$i" in ids)
        assertEquals(5, OuraGuidedPlan.BLOCKS.count { it.kind == OuraGuidedPlan.Kind.NEGATIVE })
        assertTrue(OuraGuidedPlan.BLOCKS.filter { it.kind == OuraGuidedPlan.Kind.NEGATIVE }.all { it.durationMs == 60_000L })
        val minutes = OuraGuidedPlan.totalMs() / 60_000.0
        assertTrue("plan is $minutes min", minutes in 10.0..15.0)
        assertTrue(OuraGuidedPlan.totalMs(10) < OuraGuidedPlan.totalMs())
    }

    @Test
    fun guidedCueAndRepOffsets() {
        val triple = OuraGuidedPlan.BLOCKS[1]
        assertEquals("Block 2 of ${OuraGuidedPlan.BLOCKS.size}: triple taps, normal, 8 times, one at each beep. Go.",
            OuraGuidedPlan.cue(1))
        assertEquals(List(8) { it * 3_000L }, OuraGuidedPlan.repOffsets(triple))
        assertEquals(emptyList<Long>(), OuraGuidedPlan.repOffsets(OuraGuidedPlan.BLOCKS[2]))
    }

    @Test
    fun recorderWritesBlockBoundariesAndCues() {
        val w = StringWriter()
        val rec = OuraAcmRecorder(w, "guided", "Oura Ring 4")
        val b = OuraGuidedPlan.BLOCKS[0]
        rec.block(100, "start", 0, b)
        rec.cue(100, 0, 1)
        rec.block(25_000, "end", 0, b)
        rec.block(30_000, "abort", 2, OuraGuidedPlan.BLOCKS[2])
        rec.close()
        assertEquals(
            "# noop-oura-acm v2 label=guided gen=OuraRing4\n" +
                "k,100,start,0,double-normal,TAPS,2,normal,8,3000,24000\n" +
                "c,100,0,1\n" +
                "k,25000,end,0,double-normal,TAPS,2,normal,8,3000,24000\n" +
                "k,30000,abort,2,neg-handling,NEGATIVE,0,-,0,0,60000\n",
            w.toString(),
        )
        assertEquals(1, rec.marks)
    }

    @Test
    fun frameCounterLossAccounting() {
        val wire = byteArrayOf(0x33, 0x0e, 0x32, 0x4e, 0x5e, 0x02, 0x99.toByte(), 0x02,
            0x8c.toByte(), 0x01, 0xfe.toByte(), 0x00, 0x94.toByte(), 0x00, 0xdf.toByte(), 0xfc.toByte())
        assertEquals(0x4e, OuraAcmCodec.sequence(wire))
        assertEquals(null, OuraAcmCodec.sequence(byteArrayOf(0x33, 0x01, 0x00)))
        assertEquals(0, OuraAcmCodec.framesLostBetween(10, 11))
        assertEquals(2, OuraAcmCodec.framesLostBetween(10, 13))
        assertEquals(1, OuraAcmCodec.framesLostBetween(255, 1))
        assertEquals(0, OuraAcmCodec.framesLostBetween(7, 7))
    }
}
