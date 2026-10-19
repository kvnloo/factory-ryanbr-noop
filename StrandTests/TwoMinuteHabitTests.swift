import XCTest
@testable import Strand

/// Identical portable vectors to Android TwoMinuteHabitTest. This never
/// writes an answer: a user must still tap the existing Journal Yes/No chip.
final class TwoMinuteHabitTests: XCTestCase {
    func testBuildsAnExistingJournalQuestionShape() {
        XCTAssertEqual(
            twoMinuteHabitQuestion(" brushing my  teeth. ", " read one page! "),
            "After brushing my teeth, did you read one page for two minutes?"
        )
    }

    func testUnicodeWhitespaceNormalizesWithoutInventingChanges() {
        XCTAssertEqual(
            twoMinuteHabitQuestion("after dinner", "walk outside"),
            twoMinuteHabitQuestion("  after\u{00a0}\u{00a0}dinner  ", " walk\n outside. ")
        )
        XCTAssertEqual(
            twoMinuteHabitQuestion(" brushing   my", "  read   one  page"),
            "After brushing my, did you read one page for two minutes?"
        )
    }

    func testMissingOrPunctuationOnlyInputCannotMakeAnItem() {
        XCTAssertNil(twoMinuteHabitQuestion("", "walk"))
        XCTAssertNil(twoMinuteHabitQuestion("brush teeth", " \n "))
        XCTAssertNil(twoMinuteHabitQuestion("!", "?"))
    }

    func testUTF16LengthGateMatchesKotlinEvenForEmoji() {
        XCTAssertNil(twoMinuteHabitQuestion(String(repeating: "a", count: 81), "read"))
        XCTAssertNil(twoMinuteHabitQuestion("brush", String(repeating: "b", count: 81)))
        XCTAssertNil(twoMinuteHabitQuestion(String(repeating: "🙃", count: 41), "read"))
        XCTAssertEqual(
            twoMinuteHabitQuestion(String(repeating: "a", count: 80),
                                   String(repeating: "b", count: 80))?.utf16.count,
            193
        )
    }

    @MainActor
    func testHabitQuestionUsesExistingPersistentJournalCatalogWithoutDuplicates() throws {
        let suite = "TwoMinuteHabitTests.\(UUID().uuidString)"
        let defaults = try XCTUnwrap(UserDefaults(suiteName: suite))
        defer { defaults.removePersistentDomain(forName: suite) }

        let question = try XCTUnwrap(twoMinuteHabitQuestion("brush teeth", "read one page"))
        let catalog = JournalCatalogStore(defaults: defaults)
        catalog.addCustom(question, kind: .bool, group: .behaviour)
        // A second spelling must not create a second habit or rewrite the
        // canonical evidence key. The built-in Journal store owns the backup.
        catalog.addCustom(question.uppercased(), kind: .numeric(unitLabel: "mg"), group: .other)
        XCTAssertEqual(catalog.items.count, 1)
        XCTAssertEqual(catalog.items[0].canonical, question)
        XCTAssertEqual(catalog.items[0].group, .behaviour)
        XCTAssertEqual(catalog.items[0].kind, .bool)
        XCTAssertTrue(catalog.items[0].custom)
        XCTAssertNotNil(defaults.data(forKey: JournalCatalogBackupKeys.items))

        let reloaded = JournalCatalogStore(defaults: defaults)
        XCTAssertEqual(reloaded.items, catalog.items)
        // Hidden customs remain discoverable by the composer's duplicate guard,
        // but are correctly absent from the normal day-check-in rows.
        reloaded.items[0].hidden = true
        XCTAssertTrue(reloaded.resolvedItems(imported: []).allSatisfy {
            JournalCatalogStore.norm($0.canonical) != JournalCatalogStore.norm(question)
        })
        let hidden = reloaded.resolvedItems(imported: [], includeHidden: true)
            .filter { JournalCatalogStore.norm($0.canonical) == JournalCatalogStore.norm(question) }
        XCTAssertEqual(hidden.count, 1)
        XCTAssertTrue(try XCTUnwrap(hidden.first).hidden)
    }

}
