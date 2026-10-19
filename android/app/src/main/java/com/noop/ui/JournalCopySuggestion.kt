package com.noop.ui

/**
 * Previous-day answers are never current-day evidence without an explicit user action.
 * In particular, a missing answer must not be silently copied as an observed Yes or No.
 */
internal fun journalCopySuggestion(
    selectedDayOffset: Long,
    selectedAnswers: Map<String, Boolean>,
    previousAnswers: Map<String, Boolean>,
): Map<String, Boolean> =
    if (selectedDayOffset == 0L && selectedAnswers.isEmpty()) previousAnswers.toMap()
    else emptyMap()

/** A delayed tap cannot overwrite a new answer entered while the suggestion was showing. */
internal fun missingJournalCopyAnswers(
    suggested: Map<String, Boolean>,
    alreadyLogged: Map<String, Boolean>,
): Map<String, Boolean> = suggested.filterKeys { it !in alreadyLogged }
