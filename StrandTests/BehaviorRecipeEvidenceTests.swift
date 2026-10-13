import XCTest
import WhoopStore
import StrandAnalytics
@testable import Strand

/// Fork-only integration witnesses for two existing NOOP Journal -> metric recipes.
///
/// These are synthetic observations, NOT results from people, devices or a
/// preregistered experiment. They test existing production contracts without
/// introducing an experiment store, new metric key or causal claim.
final class BehaviorRecipeEvidenceTests: XCTestCase {
    private let days = (1...12).map { String(format: "2026-09-%02d", $0) }

    /// Exercise the exact starter question, metric descriptor, imported/native
    /// journal merge and the pure outcome comparison together. Days with a
    /// metric but no explicit Journal answer must NOT become No controls.
    private func compare(
        factor: String,
        metricKey: String,
        yesValues: [Double],
        noValues: [Double],
        missingValues: [Double]
    ) throws -> BehaviorEffect {
        XCTAssertTrue(JournalCatalogStore.starterQuestions.contains(factor))
        XCTAssertEqual(MetricCatalog.metric(key: metricKey, source: "my-whoop")?.key, metricKey)
        XCTAssertEqual(yesValues.count, 5)
        XCTAssertEqual(noValues.count, 5)
        XCTAssertEqual(missingValues.count, 2)

        let yesDays = Array(days.prefix(5))
        let noDays = Array(days.dropFirst(5).prefix(5))
        let unknownDays = Array(days.suffix(2))

        let imported = yesDays.map {
            JournalEntry(day: $0, question: factor, answeredYes: true, notes: nil)
        } + [
            // A stale imported Yes must not override the actual native No.
            JournalEntry(day: noDays[0], question: factor, answeredYes: true, notes: nil)
        ]
        let native = noDays.map {
            JournalEntry(day: $0, question: factor, answeredYes: false, notes: nil)
        }
        let merged = Repository.mergeJournal(imported: imported, native: native)
        XCTAssertEqual(merged.count, 10)

        let yes = Set(merged.filter { $0.question == factor && $0.answeredYes }.map(\.day))
        let no = Set(merged.filter { $0.question == factor && !$0.answeredYes }.map(\.day))
        XCTAssertEqual(yes, Set(yesDays))
        XCTAssertEqual(no, Set(noDays))
        XCTAssertTrue(Set(unknownDays).isDisjoint(with: yes.union(no)))

        let values = Dictionary(uniqueKeysWithValues:
            zip(yesDays + noDays + unknownDays, yesValues + noValues + missingValues))
        let direct = try XCTUnwrap(
            BehaviorInsights.effect(
                behaviorDays: yes,
                controlDays: no,
                outcomeByDay: values,
                behavior: factor,
                outcome: metricKey
            )
        )
        // Exercise the same lag-selection entry point the Insights Hub uses,
        // not only the direct day-0 helper. On these synthetic fixtures the
        // prespecified same-day association is stronger than lags +1/+2.
        let ranked = EffectRanker.rank(
            behaviors: [factor: yes],
            controls: [factor: no],
            outcomeByDay: values,
            outcome: metricKey
        )
        XCTAssertEqual(ranked.count, 1)
        XCTAssertEqual(ranked.first?.lag, 0)
        XCTAssertEqual(ranked.first?.effect.delta ?? .nan, direct.delta, accuracy: 1e-9)
        return direct
    }

    func testLateCaffeineAndSleepEfficiencyUseExistingPaths() throws {
        let result = try compare(
            factor: "Did you have caffeine late in the day?",
            metricKey: "sleep_efficiency",
            yesValues: [82, 80, 84, 78, 81],
            noValues: [87, 89, 85, 86, 88],
            missingValues: [10, 99]
        )
        XCTAssertEqual(result.nWith, 5)
        XCTAssertEqual(result.nWithout, 5)
        XCTAssertEqual(result.meanWith, 81, accuracy: 1e-9)
        XCTAssertEqual(result.meanWithout, 87, accuracy: 1e-9)
        XCTAssertEqual(result.delta, -6, accuracy: 1e-9)
    }

    func testScreenInBedAndSleepPerformanceUseExistingPaths() throws {
        let result = try compare(
            factor: "Did you view a screen in bed?",
            metricKey: "sleep_performance",
            yesValues: [60, 62, 61, 59, 63],
            noValues: [73, 71, 75, 72, 74],
            missingValues: [0, 100]
        )
        XCTAssertEqual(result.nWith, 5)
        XCTAssertEqual(result.nWithout, 5)
        XCTAssertEqual(result.meanWith, 61, accuracy: 1e-9)
        XCTAssertEqual(result.meanWithout, 73, accuracy: 1e-9)
        XCTAssertEqual(result.delta, -12, accuracy: 1e-9)
    }
}
