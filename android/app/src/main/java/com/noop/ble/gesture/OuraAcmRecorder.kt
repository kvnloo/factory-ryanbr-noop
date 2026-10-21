package com.noop.ble.gesture

import java.io.BufferedWriter
import java.io.File
import java.io.Writer

/**
 * Debug-only raw motion recorder for tuning [OuraTapDetector]. It is off by default: it exists only
 * while the user has turned recording on in the gesture setup screen AND armed one bounded capture.
 * It writes to app-private storage, never to the network, and holds accelerometer bytes only (no key,
 * serial, address or health data).
 *
 * One CSV per capture. Times are the phone's monotonic clock (`elapsedRealtime`, ms) at arrival.
 *   `# noop-oura-acm v1 label=<label> gen=<generation>`
 *   `f,<ms>,<raw hex of the whole 0x33 frame>,<x1>,<y1>,<z1>[,<x2>,<y2>,<z2>]` (no axes = codec rejected it)
 *   `m,<ms>,<label>`   a ground-truth mark (the user pressed MARK / volume-down)
 *   `g,<ms>,<gesture>` what the current detector emitted
 *   `p,<ms>,<deviation>,<threshold>,<counted 0/1>,<spacing ms or -1>,<impulses after>,<blocked 0/1>`
 *      one detector excursion ([OuraTapDetector.Peak]); `<ms>` is the detector's sample clock
 *
 * Thread-safe: frames arrive on the GATT binder thread, marks on main.
 */
class OuraAcmRecorder(private val out: Writer, label: String, generation: String) {
    var frames = 0
        private set
    var undecoded = 0
        private set
    var marks = 0
        private set
    private var closed = false

    init {
        out.write("# noop-oura-acm v1 label=${clean(label)} gen=${clean(generation)}\n")
    }

    @Synchronized
    fun frame(elapsedMs: Long, wire: ByteArray, axes: List<OuraAcmCodec.Axes>) {
        if (closed) return
        frames++
        if (axes.isEmpty()) undecoded++
        val sb = StringBuilder(64)
        sb.append("f,").append(elapsedMs).append(',')
        for (b in wire) sb.append("%02x".format(b.toInt() and 0xff))
        for (a in axes) sb.append(',').append(a.x).append(',').append(a.y).append(',').append(a.z)
        out.write(sb.append('\n').toString())
    }

    @Synchronized
    fun mark(elapsedMs: Long, label: String) {
        if (closed) return
        marks++
        out.write("m,$elapsedMs,${clean(label)}\n")
    }

    @Synchronized
    fun gesture(elapsedMs: Long, gesture: String) {
        if (closed) return
        out.write("g,$elapsedMs,${clean(gesture)}\n")
    }

    @Synchronized
    fun peak(p: OuraTapDetector.Peak) {
        if (closed) return
        out.write("p,${p.atMs},${p.deviation.toInt()},${p.thresholdCounts.toInt()},${if (p.counted) 1 else 0}," +
            "${p.spacingMs},${p.impulsesAfter},${if (p.blocked) 1 else 0}\n")
    }

    @Synchronized
    fun close() {
        if (closed) return
        closed = true
        runCatching { out.flush() }
        runCatching { out.close() }
    }

    companion object {
        private fun clean(s: String) = s.filter { it.isLetterOrDigit() || it == '_' || it == '-' }.take(32)

        /** Open a new recording under `<filesDir>/diagnostics/oura-acm/`. */
        fun open(filesDir: File, label: String, generation: String, wallMs: Long): Pair<OuraAcmRecorder, File> {
            val dir = File(filesDir, "diagnostics/oura-acm").apply { mkdirs() }
            val file = File(dir, "acm-$wallMs-${clean(label)}.csv")
            val writer: BufferedWriter = file.bufferedWriter()
            return OuraAcmRecorder(writer, label, generation) to file
        }
    }
}
