package com.noop.ui

/** Two-minute habit prompts are ordinary Journal yes/no questions, not a second scoring store. */
internal object TinyHabitJournal {
    private const val PREFIX = "Tiny habit: After I "
    private const val SUFFIX = " for two minutes?"
    private const val MAX_PART_LENGTH = 80

    fun question(cue: String, action: String): String? {
        val normalizedCue = normalizePart(cue) ?: return null
        val normalizedAction = normalizePart(action) ?: return null
        return "$PREFIX$normalizedCue, did I $normalizedAction$SUFFIX"
    }

    fun isQuestion(question: String): Boolean =
        question.startsWith(PREFIX) && question.endsWith(SUFFIX)

    private fun normalizePart(input: String): String? {
        val value = buildString {
            var space = true
            for (character in input) {
                if (character.isWhitespace()) {
                    if (!space) append(' ')
                    space = true
                } else {
                    append(character)
                    space = false
                }
            }
        }.trim()
        return value.takeIf { it.isNotEmpty() && it.length <= MAX_PART_LENGTH &&
            it.none { character -> character.isISOControl() } }
    }
}
