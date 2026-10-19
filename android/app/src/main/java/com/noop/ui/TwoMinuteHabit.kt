package com.noop.ui

/**
 * Converts a user-entered cue and tiny action to a *normal* custom Journal question.
 * The question is the existing Journal catalog identity, not an external habit ID.
 * Empty, oversized or ambiguous inputs fail closed without creating data.
 *
 * A Yes/No check-in is recorded only if the person explicitly taps a Journal chip.
 */
internal fun twoMinuteHabitQuestion(cueInput: String, actionInput: String): String? {
    val cue = cleanHabitPart(cueInput)
    val action = cleanHabitPart(actionInput)
    if (cue.isEmpty() || action.isEmpty() || cue.length > 80 || action.length > 80) return null
    return "After $cue, did you $action for two minutes?"
}

private fun cleanHabitPart(input: String): String {
    val compact = buildString {
        var spacePending = false
        for (c in input.trim()) {
            if (c.isWhitespace()) {
                if (isNotEmpty()) spacePending = true
            } else {
                if (spacePending) append(' ')
                append(c)
                spacePending = false
            }
        }
    }
    return compact.trimEnd(' ', '.', ',', ':', ';', '?', '!')
}
