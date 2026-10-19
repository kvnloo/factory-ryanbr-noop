package com.noop.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JournalCopySuggestionTest {
    @Test fun previousAnswersAreOnlySuggestionsWhenCurrentDayIsEmpty() {
        val previous = mapOf("read" to true, "caffeine" to false)
        val result = journalCopySuggestion(0L, emptyMap(), previous)
        assertEquals(previous, result)
        assertEquals(false, result["caffeine"])
    }

    @Test fun manualTodayAnswerSuppressesSuggestions() {
        assertTrue(journalCopySuggestion(0L, mapOf("read" to false),
            mapOf("read" to true)).isEmpty())
    }

    @Test fun backfillAndTomorrowNeverOfferACopy() {
        val previous = mapOf("read" to true)
        assertTrue(journalCopySuggestion(1L, emptyMap(), previous).isEmpty())
        assertTrue(journalCopySuggestion(-1L, emptyMap(), previous).isEmpty())
    }

    @Test fun noPreviousLogMeansNoSuggestion() {
        assertTrue(journalCopySuggestion(0L, emptyMap(), emptyMap()).isEmpty())
    }

    @Test fun confirmationDoesNotOverwriteARacingManualAnswer() {
        val candidate = mapOf("read" to true, "late caffeine" to false)
        assertEquals(
            mapOf("late caffeine" to false),
            missingJournalCopyAnswers(candidate, mapOf("read" to false)),
        )
        assertTrue(missingJournalCopyAnswers(candidate, candidate).isEmpty())
    }

    @Test fun importedCurrentDayEvidenceSuppressesCopyOffer() {
        val importedToday = mapOf("read" to true)
        assertTrue(journalCopySuggestion(0L, importedToday, mapOf("read" to false)).isEmpty())
    }

    @Test fun confirmationDoesNotReplaceImportedAnswers() {
        val candidate = mapOf("read" to false, "walk" to true)
        val imported = mapOf("read" to true)
        assertEquals(mapOf("walk" to true), missingJournalCopyAnswers(candidate, imported))
    }
}
