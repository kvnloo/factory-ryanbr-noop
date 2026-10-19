package com.noop.ble.gesture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Derived packet fixture only; a valid decode does not prove live hardware. */
class OuraAcmCodecTest {
    private fun fromHex(hex: String): ByteArray =
        hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    @Test fun signedLittleEndianAxesAndTwoSamples() {
        assertEquals(
            listOf(OuraAcmCodec.Axes(-2, 2, -3), OuraAcmCodec.Axes(4, 5, 6)),
            OuraAcmCodec.decode(fromHex("330c3201feff0200fdff040005000600")),
        )
    }

    @Test fun acceptsSingleSample() {
        assertEquals(
            listOf(OuraAcmCodec.Axes(1, 2, 3)),
            OuraAcmCodec.decode(fromHex("33063200010002000300")),
        )
    }

    @Test fun rejectsUnknownTagRateSizeAndLength() {
        val valid = fromHex("330c3201feff0200fdff040005000600")
        assertTrue(OuraAcmCodec.decode(byteArrayOf()).isEmpty())
        assertTrue(OuraAcmCodec.decode(valid.copyOf(8)).isEmpty())
        assertTrue(OuraAcmCodec.decode(valid.copyOf(10)).isEmpty())
        assertTrue(OuraAcmCodec.decode(valid.copyOf().apply { set(0, 0x44) }).isEmpty())
        assertTrue(OuraAcmCodec.decode(valid.copyOf().apply { set(1, 0x08) }).isEmpty())
        assertTrue(OuraAcmCodec.decode(valid.copyOf().apply { set(2, 0x10) }).isEmpty())
    }

    @Test fun boundedEnableAndOffPackets() {
        assertEquals(listOf(6, 7, 32, 0, 0, 0, 1, 0, 0),
            OuraAcmCodec.enableForSeconds(1).toList())
        assertEquals(listOf(6, 7, 32, 0, 0, 0, 2, 0, 0),
            OuraAcmCodec.enableForSeconds(61).toList())
        assertEquals(listOf(6, 7, 32, 0, 0, 0, 5, 0, 0),
            OuraAcmCodec.enableForSeconds(300).toList())
        assertEquals(listOf(6, 4, 0, 0, 0, 0), OuraAcmCodec.stop().toList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun refusesZeroDuration() { OuraAcmCodec.enableForSeconds(0) }

    @Test(expected = IllegalArgumentException::class)
    fun refusesUnboundedDuration() { OuraAcmCodec.enableForSeconds(301) }
}
