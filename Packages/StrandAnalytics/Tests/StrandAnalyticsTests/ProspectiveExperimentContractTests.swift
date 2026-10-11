import Foundation
import XCTest
@testable import StrandAnalytics

final class ProspectiveExperimentContractTests: XCTestCase {

    private func validContract(
        minimumCoverage: Double = 0.8,
        minimumSamples: Int = 5,
        createdAtMs: Int64 = 100,
        predictionLockedAtMs: Int64 = 150,
        baselineWindow: ExperimentWindow = .init(startMs: 500, endMs: 1_500),
        exposureWindow: ExperimentWindow = .init(startMs: 2_000, endMs: 2_100),
        outcomeWindow: ExperimentWindow = .init(startMs: 2_100, endMs: 3_000)
    ) -> ProspectiveExperimentContract {
        ProspectiveExperimentContract(
            id: "exp-001",
            title: "Late caffeine and sleep efficiency",
            hypothesis: "Reporting late caffeine is associated with lower sleep efficiency.",
            factorKey: "Did you have caffeine late in the day?",
            primaryMetricKey: "sleep_efficiency",
            baselineWindow: baselineWindow,
            exposureWindow: exposureWindow,
            outcomeWindow: outcomeWindow,
            predictedDirection: .decrease,
            minimumCoverage: minimumCoverage,
            minimumSamples: minimumSamples,
            falsificationRule: "Fail if the prespecified effect is not positive at adequate coverage.",
            createdAtMs: createdAtMs,
            predictionLockedAtMs: predictionLockedAtMs,
            analysisRecipeVersion: "sleep-efficiency-v1",
            status: .planned
        )
    }

    func testValidContractHasNoIssues() {
        XCTAssertEqual(ProspectiveExperimentValidator.issues(validContract()), [])
    }

    func testPredictionMustBeLockedBeforeExposureStarts() {
        let contract = validContract(predictionLockedAtMs: 2_001)
        XCTAssertEqual(
            ProspectiveExperimentValidator.issues(contract),
            [.predictionLockedAfterExposureStart]
        )
    }

    func testCreatedTimestampCannotFollowPredictionLock() {
        let contract = validContract(createdAtMs: 151, predictionLockedAtMs: 150)
        XCTAssertEqual(
            ProspectiveExperimentValidator.issues(contract),
            [.createdAfterPredictionLock]
        )
    }

    func testCoverageAndSampleGatesMustBeUsable() {
        XCTAssertEqual(
            ProspectiveExperimentValidator.issues(validContract(minimumCoverage: 0)),
            [.invalidMinimumCoverage]
        )
        XCTAssertEqual(
            ProspectiveExperimentValidator.issues(validContract(minimumCoverage: 1.01)),
            [.invalidMinimumCoverage]
        )
        XCTAssertEqual(
            ProspectiveExperimentValidator.issues(validContract(minimumCoverage: .infinity)),
            [.invalidMinimumCoverage]
        )
        XCTAssertEqual(
            ProspectiveExperimentValidator.issues(validContract(minimumSamples: 0)),
            [.invalidMinimumSamples]
        )
    }

    func testWindowOrderingIssuesAreStable() {
        let contract = validContract(
            baselineWindow: .init(startMs: 1_000, endMs: 2_050),
            exposureWindow: .init(startMs: 2_000, endMs: 2_100),
            outcomeWindow: .init(startMs: 2_050, endMs: 3_000)
        )
        XCTAssertEqual(
            ProspectiveExperimentValidator.issues(contract),
            [.baselineOverlapsExposure, .outcomeStartsBeforeExposureEnds]
        )
    }

    func testInvalidWindowsAreRejected() {
        let contract = validContract(
            baselineWindow: .init(startMs: 1_000, endMs: 1_000),
            exposureWindow: .init(startMs: 2_100, endMs: 2_000),
            outcomeWindow: .init(startMs: 3_000, endMs: 3_000)
        )
        XCTAssertEqual(
            ProspectiveExperimentValidator.issues(contract),
            [.invalidBaselineWindow, .invalidExposureWindow, .invalidOutcomeWindow]
        )
    }

    // Structural counterexample: repeated late-caffeine observations over two weeks
    // produce nightly outcomes before the 14-day exposure window has ended.
    // P0 is a single-exposure contract, not a multi-night study scheduler.
    func testRepeatedExposureWithNightlyOutcomesIsOutOfScope() {
        let contract = validContract(
            exposureWindow: .init(startMs: 1_791_417_600_000, endMs: 1_792_627_200_000),
            outcomeWindow: .init(startMs: 1_791_532_800_000, endMs: 1_792_746_000_000)
        )
        XCTAssertEqual(
            ProspectiveExperimentValidator.issues(contract),
            [.outcomeStartsBeforeExposureEnds]
        )
    }

    func testWireValuesAreStable() {
        XCTAssertEqual(ExperimentPredictionDirection.noMeaningfulChange.rawValue, "no_meaningful_change")
        XCTAssertEqual(ExperimentContractIssue.emptyPrimaryMetricKey.rawValue, "empty_primary_metric_key")
        XCTAssertEqual(ExperimentContractIssue.predictionLockedAfterExposureStart.rawValue,
                       "prediction_locked_after_exposure_start")
    }

    func testCodableRoundTripPreservesContract() throws {
        let original = validContract()
        let data = try JSONEncoder().encode(original)
        let decoded = try JSONDecoder().decode(ProspectiveExperimentContract.self, from: data)
        XCTAssertEqual(decoded, original)
    }
}
