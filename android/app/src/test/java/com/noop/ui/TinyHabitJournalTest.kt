package com.noop.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TinyHabitJournalTest {
    @Test
    fun habitStacksAnExistingJournalQuestionOnACue() {
        val prompt = "Tiny habit: After I brush my teeth, did I read for two minutes?"
        assertEquals(prompt, TinyHabitJournal.question("brush my teeth", "read"))
        assertEquals(prompt, TinyHabitJournal.question(" brush\u00a0my\n teeth ", "  read  "))
        assertTrue(TinyHabitJournal.isQuestion(prompt))
        assertFalse(TinyHabitJournal.isQuestion("Did you read before bed?"))
    }

    @Test
    fun invalidOrUnboundedInputCannotBecomeAJournalKey() {
        assertNull(TinyHabitJournal.question("", "read"))
        assertNull(TinyHabitJournal.question("brush my teeth", " "))
        assertNull(TinyHabitJournal.question("a".repeat(81), "read"))
        assertNull(TinyHabitJournal.question("brush", "r\u0000ead"))
        assertEquals(80, TinyHabitJournal.question("a".repeat(80), "read")!!
            .substringAfter("After I ").substringBefore(", did I").length)
    }

    @Test
    fun yesNoAndMissingStayDistinctAndUseTheExistingWakeDayKey() {
        // Existing Journal storage uses nullable map lookup: true = done, false = skipped,
        // absent = no response. Do NOT convert absent to No in experiments or streaks.
        val question = TinyHabitJournal.question("wake up", "stretch")!!
        val answers: Map<String, Boolean> = mapOf(question to false)
        assertEquals(false, answers[question])
        assertNull(emptyMap<String, Boolean>()[question])
        assertEquals(true, mapOf(question to true)[question])
        // Today\'s action is logged for tomorrow\'s morning outcome, not today\'s.
        assertEquals("2026-10-20", journalDayKey(-1, LocalDate.of(2026, 10, 19)))
    }
}
