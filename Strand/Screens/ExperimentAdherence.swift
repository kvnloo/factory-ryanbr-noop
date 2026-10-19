import Foundation

/// Habit follow-through comes from explicit Journal Yes answers, never from the
/// availability of a biometric outcome sample. Dates are local-day keys.
enum ExperimentAdherence {
    static func fraction(
        yesDays: Set<String>, startedDay: String, today: String, durationDays: Int
    ) -> Double {
        guard let start = dayNumber(startedDay), let end = dayNumber(today), end >= start else {
            return 0
        }
        let elapsed = min(max(1, durationDays), end - start + 1)
        let completed = yesDays.compactMap(dayNumber).filter { day in
            day >= start && day - start < elapsed
        }.count
        return Double(completed) / Double(elapsed)
    }

    private static func dayNumber(_ key: String) -> Int? {
        let parts = key.split(separator: "-", omittingEmptySubsequences: false)
        guard parts.count == 3, parts[0].count == 4, parts[1].count == 2,
              parts[2].count == 2, let year = Int(parts[0]), let month = Int(parts[1]),
              let day = Int(parts[2]) else { return nil }
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(secondsFromGMT: 0)!
        guard let date = calendar.date(from: DateComponents(year: year, month: month, day: day)) else {
            return nil
        }
        let resolved = calendar.dateComponents([.year, .month, .day], from: date)
        guard resolved.year == year, resolved.month == month, resolved.day == day else { return nil }
        return calendar.ordinality(of: .day, in: .era, for: date)
    }
}
