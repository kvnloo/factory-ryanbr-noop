import XCTest
@testable import WhoopStore

final class ExperimentEvidenceStoreTests: XCTestCase {
    private func contract(id: String = "exp-1") -> ExperimentContractRecord {
        ExperimentContractRecord(
            id: id,
            title: "Earlier caffeine cutoff",
            hypothesis: "Earlier cutoff improves sleep efficiency.",
            factorKey: "caffeine_timing",
            primaryMetricKey: "sleep_efficiency",
            baselineStartMs: 100,
            baselineEndMs: 900,
            exposureStartMs: 2_000,
            exposureEndMs: 2_100,
            outcomeStartMs: 2_100,
            outcomeEndMs: 3_000,
            predictedDirection: "increase",
            minimumCoverage: 0.8,
            minimumSamples: 5,
            falsificationRule: "Fail if effect is not positive at adequate coverage.",
            createdAtMs: 1_000,
            predictionLockedAtMs: 1_400,
            analysisRecipeVersion: "sleep-eff-v1"
        )
    }

    private func receipt(
        id: String = "receipt-1",
        contractId: String = "exp-1",
        analyzedAtMs: Int64 = 3_100,
        effect: Double? = 0.03,
        sources: [String] = ["journal", "metric-series"]
    ) -> ExperimentEvidenceReceiptRecord {
        ExperimentEvidenceReceiptRecord(
            id: id,
            contractId: contractId,
            analyzedAtMs: analyzedAtMs,
            baselineSampleCount: 8,
            outcomeSampleCount: 8,
            baselineCoverage: 0.9,
            outcomeCoverage: 0.9,
            effectEstimate: effect,
            uncertaintyLower: 0.01,
            uncertaintyUpper: 0.05,
            result: "supports",
            sourceIds: sources,
            confounderAnnotations: ["travel"]
        )
    }

    func testMigrationCreatesEvidenceTablesAndReceiptIndex() async throws {
        let store = try await WhoopStore.inMemory()
        let tables = try await store.tableNames()
        for table in [
            "experimentContract",
            "experimentReceipt",
            "experimentReceiptSource",
            "experimentReceiptConfounder",
        ] {
            XCTAssertTrue(tables.contains(table), "missing \(table)")
        }
        XCTAssertEqual(try await store.primaryKeyColumns("experimentContract"), ["id"])
        XCTAssertEqual(try await store.primaryKeyColumns("experimentReceipt"), ["id"])
        XCTAssertEqual(
            try await store.primaryKeyColumns("experimentReceiptSource"),
            ["receiptId", "ordinal"]
        )
        XCTAssertTrue(
            try await store.indexNamesForTest(table: "experimentReceipt")
                .contains("idx_experimentReceipt_contract")
        )
    }

    func testLockedContractIsInsertOnlyAndExactReplayKeepsFirstPersistenceStamp() async throws {
        let store = try await WhoopStore.inMemory()
        let original = contract()

        XCTAssertEqual(
            try await store.persistLockedExperimentContractForTest(original, persistedAtMs: 1_500),
            .inserted
        )
        XCTAssertEqual(
            try await store.persistLockedExperimentContractForTest(original, persistedAtMs: 1_900),
            .alreadyPresent
        )

        let stored = try await store.experimentContract(id: original.id)
        XCTAssertEqual(stored?.contract, original)
        XCTAssertEqual(stored?.persistedAtMs, 1_500)

        let changed = ExperimentContractRecord(
            id: original.id,
            title: "Rewritten after looking",
            hypothesis: original.hypothesis,
            factorKey: original.factorKey,
            primaryMetricKey: original.primaryMetricKey,
            baselineStartMs: original.baselineStartMs,
            baselineEndMs: original.baselineEndMs,
            exposureStartMs: original.exposureStartMs,
            exposureEndMs: original.exposureEndMs,
            outcomeStartMs: original.outcomeStartMs,
            outcomeEndMs: original.outcomeEndMs,
            predictedDirection: original.predictedDirection,
            minimumCoverage: original.minimumCoverage,
            minimumSamples: original.minimumSamples,
            falsificationRule: original.falsificationRule,
            createdAtMs: original.createdAtMs,
            predictionLockedAtMs: original.predictionLockedAtMs,
            analysisRecipeVersion: original.analysisRecipeVersion
        )

        do {
            _ = try await store.persistLockedExperimentContractForTest(changed, persistedAtMs: 1_600)
            XCTFail("same id with changed evidence must fail")
        } catch {
            XCTAssertEqual(error as? ExperimentEvidenceStoreError, .contractConflict(id: original.id))
        }
        XCTAssertEqual((try await store.experimentContract(id: original.id))?.contract, original)
    }

    func testFirstContractInsertMustHappenBeforeExposure() async throws {
        let store = try await WhoopStore.inMemory()
        do {
            _ = try await store.persistLockedExperimentContractForTest(
                contract(),
                persistedAtMs: 2_001
            )
            XCTFail("late preregistration must fail")
        } catch {
            XCTAssertEqual(
                error as? ExperimentEvidenceStoreError,
                .invalidContractChronology(id: "exp-1")
            )
        }
    }

    func testReceiptRequiresStoredContractAndClosedOutcomeWindow() async throws {
        let store = try await WhoopStore.inMemory()

        do {
            _ = try await store.persistExperimentEvidenceReceiptForTest(
                receipt(),
                persistedAtMs: 3_200
            )
            XCTFail("orphan receipt must fail")
        } catch {
            XCTAssertEqual(error as? ExperimentEvidenceStoreError, .missingContract(id: "exp-1"))
        }

        _ = try await store.persistLockedExperimentContractForTest(contract(), persistedAtMs: 1_500)

        do {
            _ = try await store.persistExperimentEvidenceReceiptForTest(
                receipt(id: "early", analyzedAtMs: 2_999),
                persistedAtMs: 3_100
            )
            XCTFail("analysis before outcome close must fail")
        } catch {
            XCTAssertEqual(
                error as? ExperimentEvidenceStoreError,
                .invalidReceiptChronology(id: "early")
            )
        }
    }

    func testReceiptIsInsertOnlyAndChildProvenanceRoundTripsInOrder() async throws {
        let store = try await WhoopStore.inMemory()
        _ = try await store.persistLockedExperimentContractForTest(contract(), persistedAtMs: 1_500)
        let original = receipt()

        XCTAssertEqual(
            try await store.persistExperimentEvidenceReceiptForTest(original, persistedAtMs: 3_200),
            .inserted
        )
        XCTAssertEqual(
            try await store.persistExperimentEvidenceReceiptForTest(original, persistedAtMs: 3_500),
            .alreadyPresent
        )

        let stored = try await store.experimentEvidenceReceipt(id: original.id)
        XCTAssertEqual(stored?.receipt, original)
        XCTAssertEqual(stored?.persistedAtMs, 3_200)

        do {
            _ = try await store.persistExperimentEvidenceReceiptForTest(
                receipt(effect: 0.04),
                persistedAtMs: 3_300
            )
            XCTFail("same receipt id with changed evidence must fail")
        } catch {
            XCTAssertEqual(error as? ExperimentEvidenceStoreError, .receiptConflict(id: original.id))
        }

        do {
            _ = try await store.persistExperimentEvidenceReceiptForTest(
                receipt(sources: ["metric-series", "journal"]),
                persistedAtMs: 3_300
            )
            XCTFail("reordered provenance is a different receipt")
        } catch {
            XCTAssertEqual(error as? ExperimentEvidenceStoreError, .receiptConflict(id: original.id))
        }
    }

    func testReceiptUncertaintyMustBeAllOrNothing() async throws {
        let store = try await WhoopStore.inMemory()
        _ = try await store.persistLockedExperimentContractForTest(contract(), persistedAtMs: 1_500)

        let malformed = ExperimentEvidenceReceiptRecord(
            id: "bad-shape",
            contractId: "exp-1",
            analyzedAtMs: 3_100,
            baselineSampleCount: 8,
            outcomeSampleCount: 8,
            baselineCoverage: 0.9,
            outcomeCoverage: 0.9,
            effectEstimate: nil,
            uncertaintyLower: 0.01,
            uncertaintyUpper: nil,
            result: "inconclusive",
            sourceIds: ["metric-series"],
            confounderAnnotations: []
        )

        do {
            _ = try await store.persistExperimentEvidenceReceiptForTest(
                malformed,
                persistedAtMs: 3_200
            )
            XCTFail("half an interval must fail")
        } catch {
            XCTAssertEqual(
                error as? ExperimentEvidenceStoreError,
                .invalidReceiptShape(id: "bad-shape")
            )
        }
    }
}
