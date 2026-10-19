package com.noop.ui

import com.noop.data.JournalEntry
import org.junit.Assert.assertEquals
import org.junit.Test

/** Regression: opening a fresh day must not manufacture a journal observation. */
class JournalExplicitDayAnswersTest {
    private fun row(day: String, question: String, yes: Boolean) =
        JournalEntry(JOURNAL_DEVICE_ID, day, question, yes)

    @Test fun yesterdayIsNotImplicitlyCarriedForward() {
        val yesterday = listOf(
            row("2026-10-18", "Did you read before bed?", true),
            row("2026-10-18", "Did you drink any alcohol?", false),
        )
        assertEquals(emptyMap<String, Boolean>(), explicitJournalDayAnswers(yesterday, "2026-10-19"))
        assertEquals(
            mapOf("Did you read before bed?" to true, "Did you drink any alcohol?" to false),
            explicitJournalDayAnswers(yesterday, "2026-10-18"),
        )
    }

    @Test fun explicitNoRemainsDifferentFromMissing() {
        val native = listOf(
            row("2026-10-19", "Did you read before bed?", false),
            row("2026-10-18", "Did you drink any alcohol?", true),
        )
        assertEquals(
            mapOf("Did you read before bed?" to false),
            explicitJournalDayAnswers(native, "2026-10-19"),
        )
    }

    @Test fun latestSameDayEditWinsWithoutModifyingOtherDays() {
        val rows = listOf(
            row("2026-10-19", "Did you read before bed?", false),
            row("2026-10-18", "Did you read before bed?", true),
            row("2026-10-19", "Did you read before bed?", true),
        )
        assertEquals(
            mapOf("Did you read before bed?" to true),
            explicitJournalDayAnswers(rows, "2026-10-19"),
        )
    }
}
