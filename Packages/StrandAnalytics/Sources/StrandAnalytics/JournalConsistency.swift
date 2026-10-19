import Foundation

/// A read-only view of explicit Journal answers for one behaviour.
///
/// The Journal's exact canonical question is the caller's identity key; this helper never
/// renames it, guesses whether Yes is a "good" habit, or infers No from an absent row.
/// Day keys follow the app's local wake-day convention. This is not a health score.
public enum JournalConsistency {
    public struct Summary: Equatable, Sendable {
        public let windowDays: Int
        public let yesDays: Int
        public let noDays: Int
        public let unloggedDays: Int
        public let currentYesStreak: Int
        public let longestYesStreak: Int
        /// Nil means no explicit answer was logged for today, not an implied No.
        public let todayAnswer: Bool?
    }

    /// Derive an auditable 1...90-day view from already-merged answers for one question.
    ///
    /// Later duplicate rows win, matching Journal edit precedence. Rows after today are
    /// excluded from both streaks and the window. A missing today gives yesterday's Yes
    /// streak one day of grace; an explicit No today ends that streak immediately.
    /// All calculations are local, deterministic and side-effect-free.
    public static func summarize(
        dayKeys: [String],
        answeredYes: [Bool],
        today: String,
        windowDays: Int = 7
    ) -> Summary? {
        guard let t = Baselines.isoEpochDay(today) else { return nil }
        let window = Swift.max(1, Swift.min(windowDays, 90))
        var answers: [Int: Bool] = [:]
        for i in 0..<Swift.min(dayKeys.count, answeredYes.count) {
            guard let d = Baselines.isoEpochDay(dayKeys[i]), d <= t else { continue }
            answers[d] = answeredYes[i]
        }

        let start = t - (window - 1)
        let yes = answers.reduce(0) { total, pair in
            total + (pair.key >= start && pair.value ? 1 : 0)
        }
        let no = answers.reduce(0) { total, pair in
            total + (pair.key >= start && !pair.value ? 1 : 0)
        }
        let yesDays = Set(answers.compactMap { $0.value ? $0.key : nil })

        var longest = 0
        for day in yesDays where !yesDays.contains(day - 1) {
            var length = 1
            while yesDays.contains(day + length) { length += 1 }
            longest = Swift.max(longest, length)
        }

        let anchor: Int?
        if answers[t] == true {
            anchor = t
        } else if answers[t] == nil && answers[t - 1] == true {
            anchor = t - 1
        } else {
            anchor = nil
        }
        var current = 0
        if let a = anchor {
            current = 1
            while yesDays.contains(a - current) { current += 1 }
        }
        return Summary(
            windowDays: window,
            yesDays: yes,
            noDays: no,
            unloggedDays: window - yes - no,
            currentYesStreak: current,
            longestYesStreak: longest,
            todayAnswer: answers[t]
        )
    }
}
