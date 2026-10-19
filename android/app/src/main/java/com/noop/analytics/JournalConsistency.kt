package com.noop.analytics

/**
 * Read-only counts and Yes-streaks for one canonical Journal question.
 *
 * Missing answers are not No, and a Yes is not necessarily a "good" habit
 * (e.g. "Did you drink alcohol?"). The caller supplies the merged Journal rows
 * for one question with its local wake-day keys. No scoring or storage writes.
 * Mirrors StrandAnalytics/JournalConsistency.swift.
 */
object JournalConsistency {
    data class Summary(
        val windowDays: Int,
        val yesDays: Int,
        val noDays: Int,
        val unloggedDays: Int,
        val currentYesStreak: Int,
        val longestYesStreak: Int,
        val todayAnswer: Boolean?,
    )

    /**
     * Later duplicate rows win; tomorrow/future rows do not count today.
     * A missing today preserves yesterday's Yes streak, but an explicit No
     * ends the current streak. Window clamps to 1..90 local civil days.
     */
    fun summarize(
        dayKeys: List<String>,
        answeredYes: List<Boolean>,
        today: String,
        windowDays: Int = 7,
    ): Summary? {
        val t = Baselines.isoEpochDay(today) ?: return null
        val window = windowDays.coerceIn(1, 90)
        val answers = HashMap<Int, Boolean>()
        for (i in 0 until minOf(dayKeys.size, answeredYes.size)) {
            val d = Baselines.isoEpochDay(dayKeys[i]) ?: continue
            if (d <= t) answers[d] = answeredYes[i]
        }

        val start = t - (window - 1)
        val yes = answers.count { (day, answer) -> day >= start && answer }
        val no = answers.count { (day, answer) -> day >= start && !answer }
        val yesDays = answers.filterValues { it }.keys

        var longest = 0
        for (day in yesDays) {
            if (day - 1 !in yesDays) {
                var length = 1
                while (day + length in yesDays) length++
                longest = maxOf(longest, length)
            }
        }

        val anchor = when {
            answers[t] == true -> t
            answers[t] == null && answers[t - 1] == true -> t - 1
            else -> null
        }
        var current = 0
        if (anchor != null) {
            current = 1
            while (anchor - current in yesDays) current++
        }
        return Summary(
            windowDays = window,
            yesDays = yes,
            noDays = no,
            unloggedDays = window - yes - no,
            currentYesStreak = current,
            longestYesStreak = longest,
            todayAnswer = answers[t],
        )
    }
}
