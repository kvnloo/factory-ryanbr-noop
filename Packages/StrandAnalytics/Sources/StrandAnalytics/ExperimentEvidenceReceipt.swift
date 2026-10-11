import Foundation

// ExperimentEvidenceReceipt.swift — auditable result for one prospective experiment.
//
// Pure, deterministic, DB-free. A receipt carries the P0 contract snapshot supplied by the caller,
// then records only what was observed during analysis: provenance, coverage, sample counts,
// effect/uncertainty, confounder annotations, and the evidence classification.
//
// Structural validation cannot prove that snapshot was actually persisted before exposure;
// that audit guarantee belongs to the persistence layer.
//
// No causal inference, recommendation, scheduling, persistence, or hidden score lives here.

public enum ExperimentEvidenceResult: String, Codable, CaseIterable, Sendable {
    case supports
    case contradicts
    case inconclusive
}

public struct ExperimentUncertaintyInterval: Codable, Equatable, Sendable {
    public let lower: Double
    public let upper: Double

    public init(lower: Double, upper: Double) {
        self.lower = lower
        self.upper = upper
    }
}

public struct ExperimentEvidenceReceipt: Codable, Equatable, Sendable {
    public let id: String
    public let contract: ProspectiveExperimentContract
    public let analyzedAtMs: Int64
    public let sourceIds: [String]
    public let baselineSampleCount: Int
    public let outcomeSampleCount: Int
    public let baselineCoverage: Double
    public let outcomeCoverage: Double
    public let effectEstimate: Double?
    public let uncertainty: ExperimentUncertaintyInterval?
    public let confounderAnnotations: [String]
    public let result: ExperimentEvidenceResult

    public init(
        id: String,
        contract: ProspectiveExperimentContract,
        analyzedAtMs: Int64,
        sourceIds: [String],
        baselineSampleCount: Int,
        outcomeSampleCount: Int,
        baselineCoverage: Double,
        outcomeCoverage: Double,
        effectEstimate: Double?,
        uncertainty: ExperimentUncertaintyInterval?,
        confounderAnnotations: [String],
        result: ExperimentEvidenceResult
    ) {
        self.id = id
        self.contract = contract
        self.analyzedAtMs = analyzedAtMs
        self.sourceIds = sourceIds
        self.baselineSampleCount = baselineSampleCount
        self.outcomeSampleCount = outcomeSampleCount
        self.baselineCoverage = baselineCoverage
        self.outcomeCoverage = outcomeCoverage
        self.effectEstimate = effectEstimate
        self.uncertainty = uncertainty
        self.confounderAnnotations = confounderAnnotations
        self.result = result
    }

    /// Prespecified lag between the end of exposure and start of the outcome window.
    public var lagMs: Int64 {
        contract.outcomeWindow.startMs - contract.exposureWindow.endMs
    }

    public var baselineMissingness: Double { 1 - baselineCoverage }
    public var outcomeMissingness: Double { 1 - outcomeCoverage }
}

public enum ExperimentEvidenceReceiptIssue: String, Codable, CaseIterable, Sendable {
    case emptyID = "empty_id"
    case invalidContractSnapshot = "invalid_contract_snapshot"
    case analyzedBeforeOutcomeEnd = "analyzed_before_outcome_end"
    case emptySourceIDs = "empty_source_ids"
    case blankSourceID = "blank_source_id"
    case duplicateSourceID = "duplicate_source_id"
    case invalidBaselineSampleCount = "invalid_baseline_sample_count"
    case invalidOutcomeSampleCount = "invalid_outcome_sample_count"
    case invalidBaselineCoverage = "invalid_baseline_coverage"
    case invalidOutcomeCoverage = "invalid_outcome_coverage"
    case invalidEffectEstimate = "invalid_effect_estimate"
    case invalidUncertaintyInterval = "invalid_uncertainty_interval"
    case effectOutsideUncertainty = "effect_outside_uncertainty"
    case blankConfounderAnnotation = "blank_confounder_annotation"
    case conclusiveWithoutEffect = "conclusive_without_effect"
    case conclusiveWithoutUncertainty = "conclusive_without_uncertainty"
    case conclusiveBelowCoverageGate = "conclusive_below_coverage_gate"
    case conclusiveBelowSampleGate = "conclusive_below_sample_gate"
}

public enum ExperimentEvidenceReceiptValidator {

    public static func issues(_ receipt: ExperimentEvidenceReceipt) -> [ExperimentEvidenceReceiptIssue] {
        var out: [ExperimentEvidenceReceiptIssue] = []

        if blank(receipt.id) { out.append(.emptyID) }
        if !ProspectiveExperimentValidator.issues(receipt.contract).isEmpty {
            out.append(.invalidContractSnapshot)
        }
        if receipt.analyzedAtMs < receipt.contract.outcomeWindow.endMs {
            out.append(.analyzedBeforeOutcomeEnd)
        }

        if receipt.sourceIds.isEmpty {
            out.append(.emptySourceIDs)
        } else {
            if receipt.sourceIds.contains(where: blank) {
                out.append(.blankSourceID)
            }
            if Set(receipt.sourceIds).count != receipt.sourceIds.count {
                out.append(.duplicateSourceID)
            }
        }

        if receipt.baselineSampleCount < 0 { out.append(.invalidBaselineSampleCount) }
        if receipt.outcomeSampleCount < 0 { out.append(.invalidOutcomeSampleCount) }

        if !validObservedCoverage(receipt.baselineCoverage) {
            out.append(.invalidBaselineCoverage)
        }
        if !validObservedCoverage(receipt.outcomeCoverage) {
            out.append(.invalidOutcomeCoverage)
        }

        if let effect = receipt.effectEstimate, !effect.isFinite {
            out.append(.invalidEffectEstimate)
        }

        if let interval = receipt.uncertainty {
            if !interval.lower.isFinite || !interval.upper.isFinite || interval.lower > interval.upper {
                out.append(.invalidUncertaintyInterval)
            } else if let effect = receipt.effectEstimate,
                      effect.isFinite,
                      (effect < interval.lower || effect > interval.upper) {
                out.append(.effectOutsideUncertainty)
            }
        }

        if receipt.confounderAnnotations.contains(where: blank) {
            out.append(.blankConfounderAnnotation)
        }

        if receipt.result != .inconclusive {
            if receipt.effectEstimate == nil {
                out.append(.conclusiveWithoutEffect)
            }
            if receipt.uncertainty == nil {
                out.append(.conclusiveWithoutUncertainty)
            }

            let coverageGate = receipt.contract.minimumCoverage
            if receipt.baselineCoverage < coverageGate || receipt.outcomeCoverage < coverageGate {
                out.append(.conclusiveBelowCoverageGate)
            }

            let sampleGate = receipt.contract.minimumSamples
            if receipt.baselineSampleCount < sampleGate || receipt.outcomeSampleCount < sampleGate {
                out.append(.conclusiveBelowSampleGate)
            }
        }

        return out
    }

    private static func validObservedCoverage(_ value: Double) -> Bool {
        value.isFinite && value >= 0 && value <= 1
    }

    private static func blank(_ value: String) -> Bool {
        value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }
}
