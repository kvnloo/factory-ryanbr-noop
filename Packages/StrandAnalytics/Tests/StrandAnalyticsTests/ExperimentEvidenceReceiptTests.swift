import Foundation
import XCTest
@testable import StrandAnalytics

final class ExperimentEvidenceReceiptTests: XCTestCase {

    private func validContract(
        minimumCoverage: Double = 0.8,
        minimumSamples: Int = 5,
        outcomeWindow: ExperimentWindow = .init(startMs: 2_100, endMs: 3_000)
    ) -> ProspectiveExperimentContract {
        ProspectiveExperimentContract(
            id: "exp-001",
            title: "Earlier caffeine cutoff",
            hypothesis: "Earlier caffeine cutoff improves next-night sleep efficiency.",
            factorKey: "caffeine_timing",
            primaryMetricKey: "sleep_efficiency",
            baselineWindow: .init(startMs: 500, endMs: 1_500),
            exposureWindow: .init(startMs: 2_000, endMs: 2_100),
            outcomeWindow: outcomeWindow,
            predictedDirection: .increase,
            minimumCoverage: minimumCoverage,
            minimumSamples: minimumSamples,
            falsificationRule: "Fail if the prespecified effect is not positive at adequate coverage.",
            createdAtMs: 100,
            predictionLockedAtMs: 150,
            analysisRecipeVersion: "sleep-efficiency-v1",
            status: .planned
        )
    }

    private func validReceipt(
        contract: ProspectiveExperimentContract? = nil,
        analyzedAtMs: Int64 = 3_100,
        sourceIds: [String] = ["journal", "metric-series"],
        baselineSampleCount: Int = 8,
        outcomeSampleCount: Int = 8,
        baselineCoverage: Double = 0.9,
        outcomeCoverage: Double = 0.9,
        effectEstimate: Double? = 0.03,
        uncertainty: ExperimentUncertaintyInterval? = .init(lower: 0.01, upper: 0.05),
        confounderAnnotations: [String] = ["travel"],
        result: ExperimentEvidenceResult = .supports
    ) -> ExperimentEvidenceReceipt {
        ExperimentEvidenceReceipt(
            id: "receipt-001",
            contract: contract ?? validContract(),
            analyzedAtMs: analyzedAtMs,
            sourceIds: sourceIds,
            baselineSampleCount: baselineSampleCount,
            outcomeSampleCount: outcomeSampleCount,
            baselineCoverage: baselineCoverage,
            outcomeCoverage: outcomeCoverage,
            effectEstimate: effectEstimate,
            uncertainty: uncertainty,
            confounderAnnotations: confounderAnnotations,
            result: result
        )
    }

    func testValidConclusiveReceiptHasNoIssues() {
        XCTAssertEqual(ExperimentEvidenceReceiptValidator.issues(validReceipt()), [])
    }

    func testAnalysisMustWaitForOutcomeWindowToClose() {
        XCTAssertEqual(
            ExperimentEvidenceReceiptValidator.issues(validReceipt(analyzedAtMs: 2_999)),
            [.analyzedBeforeOutcomeEnd]
        )
    }

    func testSourceProvenanceMustBePresentNonblankAndUnique() {
        XCTAssertEqual(
            ExperimentEvidenceReceiptValidator.issues(validReceipt(sourceIds: [])),
            [.emptySourceIDs]
        )
        XCTAssertEqual(
            ExperimentEvidenceReceiptValidator.issues(validReceipt(sourceIds: ["journal", " "])),
            [.blankSourceID]
        )
        XCTAssertEqual(
            ExperimentEvidenceReceiptValidator.issues(validReceipt(sourceIds: ["journal", "journal"])),
            [.duplicateSourceID]
        )
    }

    func testObservedCoverageMustBeFiniteAndBetweenZeroAndOne() {
        XCTAssertEqual(
            ExperimentEvidenceReceiptValidator.issues(validReceipt(baselineCoverage: -0.01)),
            [.invalidBaselineCoverage, .conclusiveBelowCoverageGate]
        )
        XCTAssertEqual(
            ExperimentEvidenceReceiptValidator.issues(validReceipt(outcomeCoverage: 1.01)),
            [.invalidOutcomeCoverage]
        )
        XCTAssertEqual(
            ExperimentEvidenceReceiptValidator.issues(validReceipt(baselineCoverage: .infinity)),
            [.invalidBaselineCoverage]
        )
    }

    func testConclusiveResultRequiresEffectUncertaintyAndEvidenceGates() {
        XCTAssertEqual(
            ExperimentEvidenceReceiptValidator.issues(
                validReceipt(
                    baselineSampleCount: 4,
                    baselineCoverage: 0.7,
                    effectEstimate: nil,
                    uncertainty: nil
                )
            ),
            [
                .conclusiveWithoutEffect,
                .conclusiveWithoutUncertainty,
                .conclusiveBelowCoverageGate,
                .conclusiveBelowSampleGate,
            ]
        )
    }

    func testInconclusiveReceiptMayRemainBelowEvidenceGates() {
        XCTAssertEqual(
            ExperimentEvidenceReceiptValidator.issues(
                validReceipt(
                    baselineSampleCount: 0,
                    outcomeSampleCount: 0,
                    baselineCoverage: 0,
                    outcomeCoverage: 0,
                    effectEstimate: nil,
                    uncertainty: nil,
                    result: .inconclusive
                )
            ),
            []
        )
    }

    func testEffectAndUncertaintyMustBeFiniteAndCoherent() {
        XCTAssertEqual(
            ExperimentEvidenceReceiptValidator.issues(validReceipt(effectEstimate: .infinity)),
            [.invalidEffectEstimate]
        )
        XCTAssertEqual(
            ExperimentEvidenceReceiptValidator.issues(
                validReceipt(uncertainty: .init(lower: 0.05, upper: 0.01))
            ),
            [.invalidUncertaintyInterval]
        )
        XCTAssertEqual(
            ExperimentEvidenceReceiptValidator.issues(
                validReceipt(uncertainty: .init(lower: 0.04, upper: 0.05))
            ),
            [.effectOutsideUncertainty]
        )
    }

    func testInvalidContractCannotBeHiddenInsideReceipt() {
        let invalid = validContract(outcomeWindow: .init(startMs: 2_050, endMs: 3_000))
        XCTAssertEqual(
            ExperimentEvidenceReceiptValidator.issues(validReceipt(contract: invalid)),
            [.invalidContractSnapshot]
        )
    }

    func testDerivedLagMissingnessAndWireValuesAreStable() {
        let receipt = validReceipt()
        XCTAssertEqual(receipt.lagMs, 0)
        XCTAssertEqual(receipt.baselineMissingness, 0.1, accuracy: 0.000_001)
        XCTAssertEqual(receipt.outcomeMissingness, 0.1, accuracy: 0.000_001)
        XCTAssertEqual(ExperimentEvidenceResult.inconclusive.rawValue, "inconclusive")
        XCTAssertEqual(
            ExperimentEvidenceReceiptIssue.conclusiveBelowCoverageGate.rawValue,
            "conclusive_below_coverage_gate"
        )
    }

    func testCodableRoundTripPreservesReceipt() throws {
        let original = validReceipt()
        let data = try JSONEncoder().encode(original)
        let decoded = try JSONDecoder().decode(ExperimentEvidenceReceipt.self, from: data)
        XCTAssertEqual(decoded, original)
    }
}
