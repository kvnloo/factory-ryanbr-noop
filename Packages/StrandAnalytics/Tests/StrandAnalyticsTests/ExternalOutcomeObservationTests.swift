import Foundation
import XCTest
@testable import StrandAnalytics

final class ExternalOutcomeObservationTests: XCTestCase {

    private func validObservation(
        observedAtMs: Int64 = 1_000,
        ingestedAtMs: Int64 = 1_100,
        value: Double = 247,
        sourceRecordId: String? = "pvt-42",
        sessionId: String? = "session-7"
    ) -> ExternalOutcomeObservation {
        ExternalOutcomeObservation(
            id: "obs-001",
            observedAtMs: observedAtMs,
            ingestedAtMs: ingestedAtMs,
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

    func testClockSkewDoesNotInvalidateObservation() {
        XCTAssertEqual(
            ExternalOutcomeObservationValidator.issues(
                validObservation(observedAtMs: 1_200, ingestedAtMs: 1_100)
            ),
            []
        )
    }

    func testTimestampsCannotBeNegative() {
        XCTAssertEqual(
            ExternalOutcomeObservationValidator.issues(
                validObservation(observedAtMs: -1, ingestedAtMs: -2)
            ),
            [.invalidObservedAt, .invalidIngestedAt]
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
            ingestedAtMs: 1_100,
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
            ExternalOutcomeObservationIssue.invalidIngestedAt.rawValue,
            "invalid_ingested_at"
        )
    }

    func testCodableRoundTripPreservesObservation() throws {
        let original = validObservation()
        let data = try JSONEncoder().encode(original)
        let decoded = try JSONDecoder().decode(ExternalOutcomeObservation.self, from: data)
        XCTAssertEqual(decoded, original)
    }
}
