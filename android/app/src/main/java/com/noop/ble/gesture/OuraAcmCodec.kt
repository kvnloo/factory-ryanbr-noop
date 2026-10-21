package com.noop.ble.gesture

/**
 * Candidate Oura Gen 3 realtime 0x33 accelerometer wire format, independently
 * re-derived from protocol facts and an offline fixture. Pure decoder + command
 * builder only: no GATT writes, no background collection, no app-key handling.
 * Golden real-ring packets and stream behavior remain unverified.
 */
object OuraAcmCodec {
    data class Axes(val x: Int, val y: Int, val z: Int)

    private const val ACCEL_TAG = 0x33
    private const val RATE_50_HZ = 0x32

    /**
     * Expected wire frame: 0x33, payload-length (6 or 12), 50 Hz, one header
     * byte of unknown purpose, followed by one or two signed i16-LE xyz triples.
     * Reject unknown framing/rates rather than guessing at timestamps or axes.
     */
    fun decode(frame: ByteArray): List<Axes> {
        if (frame.size != 10 && frame.size != 16) return emptyList()
        if ((frame[0].toInt() and 0xff) != ACCEL_TAG) return emptyList()
        if ((frame[1].toInt() and 0xff) != frame.size - 4) return emptyList()
        if ((frame[2].toInt() and 0xff) != RATE_50_HZ) return emptyList()

        fun signedAxis(offset: Int): Int =
            ((frame[offset].toInt() and 0xff) or (frame[offset + 1].toInt() shl 8))
                .toShort().toInt()

        return (4 until frame.size step 6).map {
            Axes(signedAxis(it), signedAxis(it + 2), signedAxis(it + 4))
        }
    }

    /**
     * Split realtime ACM frames off the front of one notification. An ACM frame's length byte counts only
     * its xyz payload (6 or 12), so a frame is `len + 4` bytes on the wire, not the `2 + len` of the
     * ring's other outer frames. Parsed as an outer frame it loses its last two bytes and [decode]
     * rejects it. Returns the frames and the offset where any remaining non-ACM bytes begin.
     */
    fun splitLeadingFrames(bytes: ByteArray): Pair<List<ByteArray>, Int> {
        val frames = ArrayList<ByteArray>(1)
        var i = 0
        while (i + 2 <= bytes.size && (bytes[i].toInt() and 0xff) == ACCEL_TAG) {
            val len = bytes[i + 1].toInt() and 0xff
            if (len != 6 && len != 12) break
            val total = len + 4
            if (i + total > bytes.size) break
            frames.add(bytes.copyOfRange(i, i + total))
            i += total
        }
        return frames to i
    }

    /** Opt-in bounded request candidate. Never write unless user explicitly arms. */
    fun enableForSeconds(seconds: Int): IntArray {
        require(seconds in 1..300) { "Capture must be 1-300 seconds" }
        val minutes = (seconds + 59) / 60
        return intArrayOf(0x06, 0x07, 0x20, 0, 0, 0,
            minutes and 0xff, (minutes shr 8) and 0xff, 0)
    }

    /** Mandatory realtime-OFF candidate for the transport's finally block. */
    fun stop(): IntArray = intArrayOf(0x06, 0x04, 0, 0, 0, 0)
}
