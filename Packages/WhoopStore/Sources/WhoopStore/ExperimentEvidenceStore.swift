import Foundation
import GRDB

// ExperimentEvidenceStore.swift — append-only persistence for prospective experiment evidence.
//
// WhoopStore deliberately does not import StrandAnalytics: StrandAnalytics already depends on
// WhoopStore. These storage DTOs mirror only durable primitive fields.
//
// Audit boundary:
// - the store itself stamps persistedAtMs on first insert
// - a locked contract is INSERT-only by id
// - a receipt is INSERT-only by id
// - replaying the exact same evidence is an idempotent no-op
// - reusing an id with different evidence throws
//
// persistedAtMs is local-clock provenance, not cryptographic proof against a deliberately
// backdated operating-system clock.

public enum ExperimentEvidenceWriteResult: Equatable, Sendable {
    case inserted
    case alreadyPresent
}

public enum ExperimentEvidenceStoreError: Error, Equatable, Sendable {
    case contractConflict(id: String)
    case receiptConflict(id: String)
    case missingContract(id: String)
    case invalidContractChronology(id: String)
    case invalidReceiptChronology(id: String)
    case invalidReceiptShape(id: String)
}

public struct ExperimentContractRecord: Equatable, Codable, Sendable {
    public let id: String
    public let title: String
    public let hypothesis: String
    public let factorKey: String
    public let primaryMetricKey: String
    public let baselineStartMs: Int64
    public let baselineEndMs: Int64
    public let exposureStartMs: Int64
    public let exposureEndMs: Int64
    public let outcomeStartMs: Int64
    public let outcomeEndMs: Int64
    public let predictedDirection: String
    public let minimumCoverage: Double
    public let minimumSamples: Int
    public let falsificationRule: String
    public let createdAtMs: Int64
    public let predictionLockedAtMs: Int64
    public let analysisRecipeVersion: String

    public init(
        id: String,
        title: String,
        hypothesis: String,
        factorKey: String,
        primaryMetricKey: String,
        baselineStartMs: Int64,
        baselineEndMs: Int64,
        exposureStartMs: Int64,
        exposureEndMs: Int64,
        outcomeStartMs: Int64,
        outcomeEndMs: Int64,
        predictedDirection: String,
        minimumCoverage: Double,
        minimumSamples: Int,
        falsificationRule: String,
        createdAtMs: Int64,
        predictionLockedAtMs: Int64,
        analysisRecipeVersion: String
    ) {
        self.id = id
        self.title = title
        self.hypothesis = hypothesis
        self.factorKey = factorKey
        self.primaryMetricKey = primaryMetricKey
        self.baselineStartMs = baselineStartMs
        self.baselineEndMs = baselineEndMs
        self.exposureStartMs = exposureStartMs
        self.exposureEndMs = exposureEndMs
        self.outcomeStartMs = outcomeStartMs
        self.outcomeEndMs = outcomeEndMs
        self.predictedDirection = predictedDirection
        self.minimumCoverage = minimumCoverage
        self.minimumSamples = minimumSamples
        self.falsificationRule = falsificationRule
        self.createdAtMs = createdAtMs
        self.predictionLockedAtMs = predictionLockedAtMs
        self.analysisRecipeVersion = analysisRecipeVersion
    }
}

public struct PersistedExperimentContractRecord: Equatable, Codable, Sendable {
    public let contract: ExperimentContractRecord
    public let persistedAtMs: Int64

    public init(contract: ExperimentContractRecord, persistedAtMs: Int64) {
        self.contract = contract
        self.persistedAtMs = persistedAtMs
    }
}

public struct ExperimentEvidenceReceiptRecord: Equatable, Codable, Sendable {
    public let id: String
    public let contractId: String
    public let analyzedAtMs: Int64
    public let baselineSampleCount: Int
    public let outcomeSampleCount: Int
    public let baselineCoverage: Double
    public let outcomeCoverage: Double
    public let effectEstimate: Double?
    public let uncertaintyLower: Double?
    public let uncertaintyUpper: Double?
    public let result: String
    public let sourceIds: [String]
    public let confounderAnnotations: [String]

    public init(
        id: String,
        contractId: String,
        analyzedAtMs: Int64,
        baselineSampleCount: Int,
        outcomeSampleCount: Int,
        baselineCoverage: Double,
        outcomeCoverage: Double,
        effectEstimate: Double?,
        uncertaintyLower: Double?,
        uncertaintyUpper: Double?,
        result: String,
        sourceIds: [String],
        confounderAnnotations: [String]
    ) {
        self.id = id
        self.contractId = contractId
        self.analyzedAtMs = analyzedAtMs
        self.baselineSampleCount = baselineSampleCount
        self.outcomeSampleCount = outcomeSampleCount
        self.baselineCoverage = baselineCoverage
        self.outcomeCoverage = outcomeCoverage
        self.effectEstimate = effectEstimate
        self.uncertaintyLower = uncertaintyLower
        self.uncertaintyUpper = uncertaintyUpper
        self.result = result
        self.sourceIds = sourceIds
        self.confounderAnnotations = confounderAnnotations
    }
}

public struct PersistedExperimentEvidenceReceiptRecord: Equatable, Codable, Sendable {
    public let receipt: ExperimentEvidenceReceiptRecord
    public let persistedAtMs: Int64

    public init(receipt: ExperimentEvidenceReceiptRecord, persistedAtMs: Int64) {
        self.receipt = receipt
        self.persistedAtMs = persistedAtMs
    }
}

extension WhoopStore {
    public func persistLockedExperimentContract(
        _ record: ExperimentContractRecord
    ) async throws -> ExperimentEvidenceWriteResult {
        try persistLockedExperimentContractForTest(
            record,
            persistedAtMs: Self.experimentEvidenceWallClockMs()
        )
    }

    /// Internal seam for deterministic tests. Production callers cannot supply the persistence stamp.
    func persistLockedExperimentContractForTest(
        _ record: ExperimentContractRecord,
        persistedAtMs: Int64
    ) throws -> ExperimentEvidenceWriteResult {
        try syncWrite { db in
            if let existing = try Self.fetchExperimentContract(id: record.id, in: db) {
                guard existing.contract == record else {
                    throw ExperimentEvidenceStoreError.contractConflict(id: record.id)
                }
                return .alreadyPresent
            }

            guard Self.validExperimentContractChronology(record, persistedAtMs: persistedAtMs) else {
                throw ExperimentEvidenceStoreError.invalidContractChronology(id: record.id)
            }

            try db.execute(sql: """
                INSERT INTO experimentContract (
                    id, title, hypothesis, factorKey, primaryMetricKey,
                    baselineStartMs, baselineEndMs, exposureStartMs, exposureEndMs,
                    outcomeStartMs, outcomeEndMs, predictedDirection,
                    minimumCoverage, minimumSamples, falsificationRule,
                    createdAtMs, predictionLockedAtMs, analysisRecipeVersion, persistedAtMs
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, arguments: [
                    record.id, record.title, record.hypothesis, record.factorKey, record.primaryMetricKey,
                    record.baselineStartMs, record.baselineEndMs,
                    record.exposureStartMs, record.exposureEndMs,
                    record.outcomeStartMs, record.outcomeEndMs,
                    record.predictedDirection, record.minimumCoverage, record.minimumSamples,
                    record.falsificationRule, record.createdAtMs, record.predictionLockedAtMs,
                    record.analysisRecipeVersion, persistedAtMs,
                ])
            return .inserted
        }
    }

    public func experimentContract(id: String) async throws -> PersistedExperimentContractRecord? {
        try syncRead { db in try Self.fetchExperimentContract(id: id, in: db) }
    }

    public func persistExperimentEvidenceReceipt(
        _ record: ExperimentEvidenceReceiptRecord
    ) async throws -> ExperimentEvidenceWriteResult {
        try persistExperimentEvidenceReceiptForTest(
            record,
            persistedAtMs: Self.experimentEvidenceWallClockMs()
        )
    }

    /// Internal seam for deterministic tests. Production callers cannot supply the persistence stamp.
    func persistExperimentEvidenceReceiptForTest(
        _ record: ExperimentEvidenceReceiptRecord,
        persistedAtMs: Int64
    ) throws -> ExperimentEvidenceWriteResult {
        guard (record.uncertaintyLower == nil) == (record.uncertaintyUpper == nil) else {
            throw ExperimentEvidenceStoreError.invalidReceiptShape(id: record.id)
        }

        return try syncWrite { db in
            guard let contract = try Self.fetchExperimentContract(id: record.contractId, in: db) else {
                throw ExperimentEvidenceStoreError.missingContract(id: record.contractId)
            }

            if let existing = try Self.fetchExperimentReceipt(id: record.id, in: db) {
                guard existing.receipt == record else {
                    throw ExperimentEvidenceStoreError.receiptConflict(id: record.id)
                }
                return .alreadyPresent
            }

            guard record.analyzedAtMs >= contract.contract.outcomeEndMs,
                  persistedAtMs >= record.analyzedAtMs else {
                throw ExperimentEvidenceStoreError.invalidReceiptChronology(id: record.id)
            }

            try db.execute(sql: """
                INSERT INTO experimentReceipt (
                    id, contractId, analyzedAtMs, persistedAtMs,
                    baselineSampleCount, outcomeSampleCount,
                    baselineCoverage, outcomeCoverage,
                    effectEstimate, uncertaintyLower, uncertaintyUpper, result
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, arguments: [
                    record.id, record.contractId, record.analyzedAtMs, persistedAtMs,
                    record.baselineSampleCount, record.outcomeSampleCount,
                    record.baselineCoverage, record.outcomeCoverage,
                    record.effectEstimate, record.uncertaintyLower, record.uncertaintyUpper,
                    record.result,
                ])

            for (ordinal, sourceId) in record.sourceIds.enumerated() {
                try db.execute(
                    sql: """
                        INSERT INTO experimentReceiptSource (receiptId, ordinal, sourceId)
                        VALUES (?, ?, ?)
                        """,
                    arguments: [record.id, ordinal, sourceId]
                )
            }
            for (ordinal, annotation) in record.confounderAnnotations.enumerated() {
                try db.execute(
                    sql: """
                        INSERT INTO experimentReceiptConfounder (receiptId, ordinal, annotation)
                        VALUES (?, ?, ?)
                        """,
                    arguments: [record.id, ordinal, annotation]
                )
            }
            return .inserted
        }
    }

    public func experimentEvidenceReceipt(id: String) async throws -> PersistedExperimentEvidenceReceiptRecord? {
        try syncRead { db in try Self.fetchExperimentReceipt(id: id, in: db) }
    }

    private static func validExperimentContractChronology(
        _ r: ExperimentContractRecord,
        persistedAtMs: Int64
    ) -> Bool {
        r.baselineStartMs < r.baselineEndMs
            && r.exposureStartMs < r.exposureEndMs
            && r.outcomeStartMs < r.outcomeEndMs
            && r.baselineEndMs <= r.exposureStartMs
            && r.outcomeStartMs >= r.exposureEndMs
            && r.createdAtMs <= r.predictionLockedAtMs
            && r.predictionLockedAtMs <= persistedAtMs
            && persistedAtMs <= r.exposureStartMs
    }

    private static func experimentEvidenceWallClockMs() -> Int64 {
        Int64((Date().timeIntervalSince1970 * 1_000).rounded(.down))
    }

    private static func fetchExperimentContract(
        id: String,
        in db: Database
    ) throws -> PersistedExperimentContractRecord? {
        guard let row = try Row.fetchOne(
            db,
            sql: "SELECT * FROM experimentContract WHERE id = ?",
            arguments: [id]
        ) else { return nil }

        let contract = ExperimentContractRecord(
            id: row["id"],
            title: row["title"],
            hypothesis: row["hypothesis"],
            factorKey: row["factorKey"],
            primaryMetricKey: row["primaryMetricKey"],
            baselineStartMs: row["baselineStartMs"],
            baselineEndMs: row["baselineEndMs"],
            exposureStartMs: row["exposureStartMs"],
            exposureEndMs: row["exposureEndMs"],
            outcomeStartMs: row["outcomeStartMs"],
            outcomeEndMs: row["outcomeEndMs"],
            predictedDirection: row["predictedDirection"],
            minimumCoverage: row["minimumCoverage"],
            minimumSamples: row["minimumSamples"],
            falsificationRule: row["falsificationRule"],
            createdAtMs: row["createdAtMs"],
            predictionLockedAtMs: row["predictionLockedAtMs"],
            analysisRecipeVersion: row["analysisRecipeVersion"]
        )
        return PersistedExperimentContractRecord(
            contract: contract,
            persistedAtMs: row["persistedAtMs"]
        )
    }

    private static func fetchExperimentReceipt(
        id: String,
        in db: Database
    ) throws -> PersistedExperimentEvidenceReceiptRecord? {
        guard let row = try Row.fetchOne(
            db,
            sql: "SELECT * FROM experimentReceipt WHERE id = ?",
            arguments: [id]
        ) else { return nil }

        let sourceIds = try String.fetchAll(
            db,
            sql: """
                SELECT sourceId FROM experimentReceiptSource
                WHERE receiptId = ? ORDER BY ordinal ASC
                """,
            arguments: [id]
        )
        let confounders = try String.fetchAll(
            db,
            sql: """
                SELECT annotation FROM experimentReceiptConfounder
                WHERE receiptId = ? ORDER BY ordinal ASC
                """,
            arguments: [id]
        )

        let receipt = ExperimentEvidenceReceiptRecord(
            id: row["id"],
            contractId: row["contractId"],
            analyzedAtMs: row["analyzedAtMs"],
            baselineSampleCount: row["baselineSampleCount"],
            outcomeSampleCount: row["outcomeSampleCount"],
            baselineCoverage: row["baselineCoverage"],
            outcomeCoverage: row["outcomeCoverage"],
            effectEstimate: row["effectEstimate"],
            uncertaintyLower: row["uncertaintyLower"],
            uncertaintyUpper: row["uncertaintyUpper"],
            result: row["result"],
            sourceIds: sourceIds,
            confounderAnnotations: confounders
        )
        return PersistedExperimentEvidenceReceiptRecord(
            receipt: receipt,
            persistedAtMs: row["persistedAtMs"]
        )
    }
}
