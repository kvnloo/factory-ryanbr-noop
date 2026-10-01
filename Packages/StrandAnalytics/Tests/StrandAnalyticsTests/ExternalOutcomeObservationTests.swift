import Foundation
import XCTest
@testable import StrandAnalytics

final class ExternalOutcomeObservationTests: XCTestCase {

    private func validObservation(
        observedAtMs: Int64 = 1_000,
        recordedAtMs: Int64 = 1_100,
        value: Double = 247,
        sourceRecordId: String? = "pvt-42",
        sessionId: String? = "session-7"
    ) -> ExternalOutcomeObservation {
        ExternalOutcomeObservation(
            id: "obs-001",
            observedAtMs: observedAtMs,
            recordedAtMs: recordedAtMs,
            outcomeKey: "reaction_time_ms",
            value: value,
            unit: "ms",
            sourceId: "local-pvt",
            sourceRecordId: sourceRecordId,
            sessionId: sessionId,
            measurementVersion: "pvt-v1"
        )
    }

    func testValidObservationHasNoIssues() {
        XCTAssertEqual(ExternalOutcomeObservationValidator.issues(validObservation()), [])
    }

    func testRecordedTimestampCannotPrecedeObservation() {
        XCTAssertEqual(
            ExternalOutcomeObservationValidator.issues(
                validObservation(observedAtMs: 1_000, recordedAtMs: 999)
            ),
            [.recordedBeforeObserved]
        )
    }

    func testTimestampsCannotBeNegative() {
        XCTAssertEqual(
            ExternalOutcomeObservationValidator.issues(
                validObservation(observedAtMs: -1, recordedAtMs: -2)
            ),
            [.invalidObservedAt, .invalidRecordedAt, .recordedBeforeObserved]
        )
    }

    func testValueMustBeFinite() {
        XCTAssertEqual(
            ExternalOutcomeObservationValidator.issues(validObservation(value: .infinity)),
            [.invalidValue]
        )
    }

    func testOptionalIdentifiersMustBeMeaningfulWhenPresent() {
        XCTAssertEqual(
            ExternalOutcomeObservationValidator.issues(validObservation(sourceRecordId: " ")),
            [.blankSourceRecordID]
        )
        XCTAssertEqual(
            ExternalOutcomeObservationValidator.issues(validObservation(sessionId: "")),
            [.blankSessionID]
        )
        XCTAssertEqual(
            ExternalOutcomeObservationValidator.issues(
                validObservation(sourceRecordId: nil, sessionId: nil)
            ),
            []
        )
    }

    func testRequiredMetadataCannotBeBlank() {
        let observation = ExternalOutcomeObservation(
            id: " ",
            observedAtMs: 1_000,
            recordedAtMs: 1_100,
            outcomeKey: "",
            value: 1,
            unit: " ",
            sourceId: "",
            sourceRecordId: nil,
            sessionId: nil,
            measurementVersion: " "
        )
        XCTAssertEqual(
            ExternalOutcomeObservationValidator.issues(observation),
            [.emptyID, .emptyOutcomeKey, .emptyUnit, .emptySourceID, .emptyMeasurementVersion]
        )
    }

    func testWireValuesAreStable() {
        XCTAssertEqual(ExternalOutcomeObservationIssue.invalidValue.rawValue, "invalid_value")
        XCTAssertEqual(
            ExternalOutcomeObservationIssue.recordedBeforeObserved.rawValue,
            "recorded_before_observed"
        )
    }

    func testCodableRoundTripPreservesObservation() throws {
        let original = validObservation()
        let data = try JSONEncoder().encode(original)
        let decoded = try JSONDecoder().decode(ExternalOutcomeObservation.self, from: data)
        XCTAssertEqual(decoded, original)
    }
}
