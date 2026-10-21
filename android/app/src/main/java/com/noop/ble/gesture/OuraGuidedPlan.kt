package com.noop.ble.gesture

/**
 * The guided labelled-recording session for tuning [OuraTapDetector]: ordered blocks of cued taps and
 * negative activities, so a recording carries exact ground truth instead of hand-pressed marks.
 *
 * Ordered by importance, so a session stopped early is still useful: the two gestures that drive z0live
 * (double, triple) at normal strength and the two negatives that caused tonight's false triggers
 * (handling the phone, walking) come first; light/firm variants, the other negatives, and the classes
 * recorded only for rejection or future gestures (single, quadruple, quintuple, rapid) follow.
 *
 * Pure (no Android), so the plan, its length and its cue text are JVM-tested.
 */
object OuraGuidedPlan {
    enum class Kind { TAPS, RAPID, NEGATIVE }

    /**
     * One block. [taps] is the taps per repetition (0 for a negative). TAPS blocks cue [reps] repetitions
     * [periodMs] apart; RAPID and NEGATIVE blocks run for [durationMs] with a cue only at start and end.
     */
    data class Block(
        val id: String,
        val kind: Kind,
        val taps: Int,
        val intensity: String,
        val reps: Int,
        val periodMs: Long,
        val durationMs: Long,
        val spoken: String,
    )

    /** Spoken/announced before a block's first cue; the ring hand should be still when cues start. */
    const val LEAD_IN_MS = 4_000L
    /** Quiet gap after a block before the next announcement. */
    const val GAP_MS = 2_000L

    private val NAMES = mapOf(1 to "single", 2 to "double", 3 to "triple", 4 to "quadruple", 5 to "quintuple")
    private val PERIOD = mapOf(1 to 2_500L, 2 to 3_000L, 3 to 3_000L, 4 to 3_500L, 5 to 4_000L)

    /** Eight cued reps at normal strength, six at light and firm (keeps the plan near 15 minutes). */
    private fun taps(n: Int, intensity: String, reps: Int = if (intensity == "normal") 8 else 6): Block {
        val name = NAMES.getValue(n)
        val period = PERIOD.getValue(n)
        return Block("$name-$intensity", Kind.TAPS, n, intensity, reps, period, reps * period,
            "$name taps, $intensity, $reps times, one at each beep")
    }

    private fun rapid(n: Int, intensity: String): Block {
        val name = if (n == 1) "single taps" else "${NAMES.getValue(n)}s"
        return Block("rapid-${NAMES.getValue(n)}-$intensity", Kind.RAPID, n, intensity, 0, 0, 10_000L,
            "rapid $name, $intensity, as fast as you can for 10 seconds")
    }

    private fun negative(id: String, spoken: String) =
        Block("neg-$id", Kind.NEGATIVE, 0, "-", 0, 0, 60_000L, "$spoken for one minute, no ring taps")

    val BLOCKS: List<Block> = listOf(
        taps(2, "normal"),
        taps(3, "normal"),
        negative("handling", "handle the phone, put it in your pocket and take it out"),
        negative("walking", "walk around"),
        taps(2, "firm"),
        taps(2, "light"),
        taps(3, "firm"),
        taps(3, "light"),
        negative("typing", "type on the phone"),
        negative("talking", "talk with your hands"),
        taps(1, "normal"),
        taps(4, "normal"),
        taps(5, "normal"),
        rapid(1, "normal"),
        rapid(2, "normal"),
        rapid(3, "normal"),
        negative("rest", "rest your hand, stay still"),
        taps(1, "light"),
        taps(1, "firm"),
        taps(4, "light"),
        taps(4, "firm"),
        taps(5, "light"),
        taps(5, "firm"),
        rapid(1, "light"),
        rapid(1, "firm"),
        rapid(2, "light"),
        rapid(2, "firm"),
        rapid(3, "light"),
        rapid(3, "firm"),
    )

    /** Wall time of the whole plan from [from], including lead-ins and gaps. */
    fun totalMs(from: Int = 0): Long = BLOCKS.drop(from).sumOf { LEAD_IN_MS + it.durationMs + GAP_MS }

    /** What is announced (and spoken) before block [index]. */
    fun cue(index: Int): String = "Block ${index + 1} of ${BLOCKS.size}: ${BLOCKS[index].spoken}. Go."

    /** Offsets (ms after the block starts) of each repetition's beep; empty for RAPID and NEGATIVE. */
    fun repOffsets(block: Block): List<Long> =
        if (block.kind != Kind.TAPS) emptyList() else List(block.reps) { it * block.periodMs }
}
