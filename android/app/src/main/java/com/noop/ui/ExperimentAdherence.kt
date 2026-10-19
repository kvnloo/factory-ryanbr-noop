package com.noop.ui

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Journal Yes answers measure follow-through even when the wearable has no usable outcome. */
internal object ExperimentAdherence {
    fun fraction(
        yesDays: Set<String>, startedDay: String, today: String, durationDays: Int,
    ): Double {
        val start = runCatching { LocalDate.parse(startedDay) }.getOrNull() ?: return 0.0
        val end = runCatching { LocalDate.parse(today) }.getOrNull() ?: return 0.0
        if (end.isBefore(start)) return 0.0
        val elapsed = (ChronoUnit.DAYS.between(start, end) + 1)
            .coerceAtMost(durationDays.coerceAtLeast(1).toLong())
        val completed = yesDays.count { key ->
            val day = runCatching { LocalDate.parse(key) }.getOrNull()
            day != null && !day.isBefore(start) && ChronoUnit.DAYS.between(start, day) < elapsed
        }
        return completed.toDouble() / elapsed
    }

    /** Prevent late-arriving wearable samples from extending an already-ended plan. */
    fun includes(day: String, startedDay: String, today: String, durationDays: Int): Boolean {
        val start = runCatching { LocalDate.parse(startedDay) }.getOrNull() ?: return false
        val end = runCatching { LocalDate.parse(today) }.getOrNull() ?: return false
        val observed = runCatching { LocalDate.parse(day) }.getOrNull() ?: return false
        return !observed.isBefore(start) && !observed.isAfter(end) &&
            ChronoUnit.DAYS.between(start, observed) < durationDays.coerceAtLeast(1)
    }
}
