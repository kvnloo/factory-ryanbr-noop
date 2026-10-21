package com.noop.ble.gesture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.StringWriter

/** The debug recorder's CSV and the detector's per-excursion diagnostics (synthetic samples only). */
class OuraAcmRecorderTest {

    @Test
    fun writesHeaderFramesMarksGesturesAndPeaks() {
        val w = StringWriter()
        val rec = OuraAcmRecorder(w, "normal", "Oura Ring 4")
        val wire = byteArrayOf(0x33, 0x0e, 0x32, 0x4e, 0x5e, 0x02, 0x99.toByte(), 0x02,
            0x8c.toByte(), 0x01, 0xfe.toByte(), 0x00, 0x94.toByte(), 0x00, 0xdf.toByte(), 0xfc.toByte())
        rec.frame(1000, wire, OuraAcmCodec.decode(wire))
        rec.frame(1040, byteArrayOf(0x33, 0x01, 0x00), emptyList())
        rec.mark(1500, "normal")
        rec.gesture(1600, "DOUBLE_TAP")
        rec.peak(OuraTapDetector.Peak(1200, 412.7, 380.0, true, 230, 2, false))
        rec.close()
        rec.mark(9999, "after-close")   // ignored
        assertEquals(
            "# noop-oura-acm v1 label=normal gen=OuraRing4\n" +
                "f,1000,330e324e5e0299028c01fe009400dffc,606,665,396,254,148,-801\n" +
                "f,1040,330100\n" +
                "m,1500,normal\n" +
                "g,1600,DOUBLE_TAP\n" +
                "p,1200,412,380,1,230,2,0\n",
            w.toString(),
        )
        assertEquals(2, rec.frames)
        assertEquals(1, rec.undecoded)
        assertEquals(1, rec.marks)
    }

    @Test
    fun detectorReportsCountedAndSubThresholdPeaksWithoutChangingDecisions() {
        val peaks = ArrayList<OuraTapDetector.Peak>()
        val observed = OuraTapDetector(observer = { peaks.add(it) })
        val plain = OuraTapDetector()
        fun feed(ms: Long, x: Int) {
            assertEquals(plain.sample(ms, x, 0, 0), observed.sample(ms, x, 0, 0))
        }
        feed(0, 1000)
        feed(20, 1000)
        feed(40, 1250)     // deviation 250: above half the threshold (190), below 380
        feed(60, 1000)
        feed(100, 1500)    // deviation 500: impulse 1
        feed(120, 1000)
        feed(340, 1500)    // impulse 2, spacing 240
        feed(360, 1000)
        assertEquals(3, peaks.size)
        assertFalse(peaks[0].counted)
        assertEquals(250.0, peaks[0].deviation, 1.0)
        assertTrue(peaks[1].counted)
        assertEquals(-1L, peaks[1].spacingMs)
        assertEquals(1, peaks[1].impulsesAfter)
        assertTrue(peaks[2].counted)
        assertEquals(240L, peaks[2].spacingMs)
        assertEquals(2, peaks[2].impulsesAfter)
        assertEquals(plain.flush(900), observed.flush(900))
    }
}
