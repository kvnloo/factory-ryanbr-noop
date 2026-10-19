package com.noop.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TwoMinuteHabitTest {
    @Test fun buildsARegularJournalQuestion() {
        assertEquals(
            "After brushing my teeth, did you read one page for two minutes?",
            twoMinuteHabitQuestion(" brushing my  teeth. ", " read one page! "),
        )
    }

    @Test fun normalizesRepeatedUnicodeWhitespaceWithoutChangingMeaning() {
        assertEquals(
            twoMinuteHabitQuestion("after dinner", "walk outside"),
            twoMinuteHabitQuestion("  after\u00a0\u00a0dinner  ", " walk\n outside. "),
        )
    }

    @Test fun requiresTwoNonemptyParts() {
        assertNull(twoMinuteHabitQuestion("", "walk"))
        assertNull(twoMinuteHabitQuestion("brush teeth", " \n "))
        assertNull(twoMinuteHabitQuestion("!", "?"))
    }

    @Test fun rejectsOversizedQuestionsRatherThanPersistingThem() {
        assertNull(twoMinuteHabitQuestion("a".repeat(81), "read"))
        assertNull(twoMinuteHabitQuestion("brush", "b".repeat(81)))
        assertEquals(193, twoMinuteHabitQuestion("a".repeat(80), "b".repeat(80))?.length)
    }

    /** Locks the Android side of Swift's explicit whitespace table.
     * These pairs are byte-identical to TwoMinuteHabitTests.swift.
     */
    @Test fun unicodeWhitespaceBoundariesMatchSwiftCanonical() {
        val examples = listOf(
            "brush\u001Cteeth" to "After brush teeth, did you read for two minutes?",
            "brush\u001Fteeth" to "After brush teeth, did you read for two minutes?",
            "brush\u0085teeth" to "After brush\u0085teeth, did you read for two minutes?",
            "brush\u200Bteeth" to "After brush\u200Bteeth, did you read for two minutes?",
            "brush\u2007teeth" to "After brush teeth, did you read for two minutes?",
            "brush\u00A0teeth" to "After brush teeth, did you read for two minutes?",
            "\uFEFFbrush" to "After \uFEFFbrush, did you read for two minutes?",
        )
        for ((cue, expected) in examples) {
            assertEquals(expected, twoMinuteHabitQuestion(cue, "read"))
        }
        assertNull(twoMinuteHabitQuestion("\u001D", "read"))
    }

}
