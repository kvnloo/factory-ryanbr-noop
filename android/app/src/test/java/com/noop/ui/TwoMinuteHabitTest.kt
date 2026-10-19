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
        assertEquals(123, twoMinuteHabitQuestion("a".repeat(80), "b".repeat(80))?.length)
    }
}
