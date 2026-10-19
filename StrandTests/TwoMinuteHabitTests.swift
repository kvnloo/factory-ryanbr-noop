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
}
