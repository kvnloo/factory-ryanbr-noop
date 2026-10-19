import XCTest
import WhoopStore
import StrandAnalytics
@testable import Strand

/// Uses the real migrated GRDB tables, not in-memory dictionaries masquerading as storage.
/// All values are synthetic: this proves a data path, not an intervention or health effect.
final class PersistedBehaviorRecipeEvidenceTests: XCTestCase {
    func testTwoRecipesRoundTripThroughSQLiteWithoutInventedControls() async throws {
        let store = try await WhoopStore.inMemory()
        let days = (1...12).map { String(format: "2026-09-%02d", $0) }
        let yesDays = Array(days.prefix(5))
        let noDays = Array(days.dropFirst(5).prefix(5))
        let unansweredDays = Array(days.suffix(2))

        let recipes: [(factor: String, metric: String, withValues: [Double],
                       withoutValues: [Double], unansweredValues: [Double],
                       meanWith: Double, meanWithout: Double)] = [
            ("Did you have caffeine late in the day?", "sleep_efficiency",
             [82, 80, 84, 78, 81], [87, 89, 85, 86, 88], [10, 99], 81, 87),
            ("Did you view a screen in bed?", "sleep_performance",
             [60, 62, 61, 59, 63], [73, 71, 75, 72, 74], [0, 100], 61, 73),
        ]

        for recipe in recipes {
            XCTAssertTrue(JournalCatalogStore.starterQuestions.contains(recipe.factor))
            XCTAssertEqual(MetricCatalog.metric(key: recipe.metric, source: "my-whoop")?.key,
                           recipe.metric)

            let imported = yesDays.map {
                JournalEntry(day: $0, question: recipe.factor, answeredYes: true, notes: nil)
            } + [
                // Imported Yes is stale; native No must win for this natural key.
                JournalEntry(day: noDays[0], question: recipe.factor, answeredYes: true, notes: nil)
            ]
            let native = noDays.map {
                JournalEntry(day: $0, question: recipe.factor, answeredYes: false, notes: nil)
            }
            try await store.upsertJournal(imported, deviceId: "my-whoop")
            try await store.upsertJournal(native, deviceId: "noop-journal")

            let samples = zip(days, recipe.withValues + recipe.withoutValues + recipe.unansweredValues)
                .map { MetricPoint(day: $0.0, key: recipe.metric, value: $0.1) }
            try await store.upsertMetricSeries(samples, deviceId: "my-whoop")

            // Read back through production storage before invoking production merge/analytics.
            let importedRead = try await store.journalEntries(
                deviceId: "my-whoop", from: days[0], to: days[11])
            let nativeRead = try await store.journalEntries(
                deviceId: "noop-journal", from: days[0], to: days[11])
            let metricRead = try await store.metricSeries(
                deviceId: "my-whoop", key: recipe.metric, from: days[0], to: days[11])

            XCTAssertEqual(importedRead.count, 6)
            XCTAssertEqual(nativeRead.count, 5)
            XCTAssertEqual(metricRead.count, 12)

            let merged = Repository.mergeJournal(imported: importedRead, native: nativeRead)
            let withDays = Set(merged.filter { $0.answeredYes }.map(\.day))
            let withoutDays = Set(merged.filter { !$0.answeredYes }.map(\.day))
            XCTAssertEqual(merged.count, 10)
            XCTAssertEqual(withDays, Set(yesDays))
            XCTAssertEqual(withoutDays, Set(noDays))
            XCTAssertTrue(Set(unansweredDays).isDisjoint(with: withDays.union(withoutDays)))

            let values = Dictionary(uniqueKeysWithValues: metricRead.map { ($0.day, $0.value) })
            let effect = try XCTUnwrap(BehaviorInsights.effect(
                behaviorDays: withDays, controlDays: withoutDays, outcomeByDay: values,
                behavior: recipe.factor, outcome: recipe.metric))
            XCTAssertEqual(effect.nWith, 5)
            XCTAssertEqual(effect.nWithout, 5)
            XCTAssertEqual(effect.meanWith, recipe.meanWith, accuracy: 1e-9)
            XCTAssertEqual(effect.meanWithout, recipe.meanWithout, accuracy: 1e-9)
            XCTAssertEqual(effect.delta, recipe.meanWith - recipe.meanWithout, accuracy: 1e-9)
        }
    }
}
