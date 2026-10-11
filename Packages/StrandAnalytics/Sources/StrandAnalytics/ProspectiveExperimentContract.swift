import Foundation

// ProspectiveExperimentContract.swift — preregister a local experiment before exposure.
//
// Pure, deterministic, DB-free. This is intentionally only an evidence contract:
// it references an existing factor/intervention key and metric key, declares the
// windows and prediction up front, and validates that the prediction was locked
// before exposure began.
//
// No scheduling, scoring, causal inference, recommendation, or persistence lives here.

public enum ExperimentStatus: String, Codable, CaseIterable, Sendable {
    case planned
    case running
    case completed
    case abandoned
}

public enum ExperimentPredictionDirection: String, Codable, CaseIterable, Sendable {
    case increase
    case decrease
    case noMeaningfulChange = "no_meaningful_change"
}

public struct ExperimentWindow: Codable, Equatable, Sendable {
    public let startMs: Int64
    public let endMs: Int64

    public init(startMs: Int64, endMs: Int64) {
        self.startMs = startMs
        self.endMs = endMs
    }

    public var durationMs: Int64 { endMs - startMs }
}

public struct ProspectiveExperimentContract: Codable, Equatable, Sendable {
    public let id: String
    public let title: String
    public let hypothesis: String
    public let factorKey: String
    public let primaryMetricKey: String
    public let baselineWindow: ExperimentWindow
    public let exposureWindow: ExperimentWindow
    public let outcomeWindow: ExperimentWindow
    public let predictedDirection: ExperimentPredictionDirection
    public let minimumCoverage: Double
    public let minimumSamples: Int
    public let falsificationRule: String
    public let createdAtMs: Int64
    public let predictionLockedAtMs: Int64
    public let analysisRecipeVersion: String
    public let status: ExperimentStatus

    public init(
        id: String,
        title: String,
        hypothesis: String,
        factorKey: String,
        primaryMetricKey: String,
        baselineWindow: ExperimentWindow,
        exposureWindow: ExperimentWindow,
        outcomeWindow: ExperimentWindow,
        predictedDirection: ExperimentPredictionDirection,
        minimumCoverage: Double,
        minimumSamples: Int,
        falsificationRule: String,
        createdAtMs: Int64,
        predictionLockedAtMs: Int64,
        analysisRecipeVersion: String,
        status: ExperimentStatus
    ) {
        self.id = id
        self.title = title
        self.hypothesis = hypothesis
        self.factorKey = factorKey
        self.primaryMetricKey = primaryMetricKey
        self.baselineWindow = baselineWindow
        self.exposureWindow = exposureWindow
        self.outcomeWindow = outcomeWindow
        self.predictedDirection = predictedDirection
        self.minimumCoverage = minimumCoverage
        self.minimumSamples = minimumSamples
        self.falsificationRule = falsificationRule
        self.createdAtMs = createdAtMs
        self.predictionLockedAtMs = predictionLockedAtMs
        self.analysisRecipeVersion = analysisRecipeVersion
        self.status = status
    }
}

public enum ExperimentContractIssue: String, Codable, CaseIterable, Sendable {
    case emptyID = "empty_id"
    case emptyTitle = "empty_title"
    case emptyHypothesis = "empty_hypothesis"
    case emptyFactorKey = "empty_factor_key"
    case emptyPrimaryMetricKey = "empty_primary_metric_key"
    case invalidBaselineWindow = "invalid_baseline_window"
    case invalidExposureWindow = "invalid_exposure_window"
    case invalidOutcomeWindow = "invalid_outcome_window"
    case baselineOverlapsExposure = "baseline_overlaps_exposure"
    case outcomeStartsBeforeExposureEnds = "outcome_starts_before_exposure_ends"
    case invalidMinimumCoverage = "invalid_minimum_coverage"
    case invalidMinimumSamples = "invalid_minimum_samples"
    case emptyFalsificationRule = "empty_falsification_rule"
    case emptyAnalysisRecipeVersion = "empty_analysis_recipe_version"
    case createdAfterPredictionLock = "created_after_prediction_lock"
    case predictionLockedAfterExposureStart = "prediction_locked_after_exposure_start"
}

public enum ProspectiveExperimentValidator {

    public static func issues(_ contract: ProspectiveExperimentContract) -> [ExperimentContractIssue] {
        var out: [ExperimentContractIssue] = []

        if blank(contract.id) { out.append(.emptyID) }
        if blank(contract.title) { out.append(.emptyTitle) }
        if blank(contract.hypothesis) { out.append(.emptyHypothesis) }
        if blank(contract.factorKey) { out.append(.emptyFactorKey) }
        if blank(contract.primaryMetricKey) { out.append(.emptyPrimaryMetricKey) }

        if contract.baselineWindow.startMs >= contract.baselineWindow.endMs {
            out.append(.invalidBaselineWindow)
        }
        if contract.exposureWindow.startMs >= contract.exposureWindow.endMs {
            out.append(.invalidExposureWindow)
        }
        if contract.outcomeWindow.startMs >= contract.outcomeWindow.endMs {
            out.append(.invalidOutcomeWindow)
        }

        if contract.baselineWindow.endMs > contract.exposureWindow.startMs {
            out.append(.baselineOverlapsExposure)
        }
        if contract.outcomeWindow.startMs < contract.exposureWindow.endMs {
            out.append(.outcomeStartsBeforeExposureEnds)
        }

        if !contract.minimumCoverage.isFinite
            || contract.minimumCoverage <= 0
            || contract.minimumCoverage > 1 {
            out.append(.invalidMinimumCoverage)
        }
        if contract.minimumSamples <= 0 {
            out.append(.invalidMinimumSamples)
        }

        if blank(contract.falsificationRule) { out.append(.emptyFalsificationRule) }
        if blank(contract.analysisRecipeVersion) { out.append(.emptyAnalysisRecipeVersion) }

        if contract.createdAtMs > contract.predictionLockedAtMs {
            out.append(.createdAfterPredictionLock)
        }
        if contract.predictionLockedAtMs > contract.exposureWindow.startMs {
            out.append(.predictionLockedAfterExposureStart)
        }

        return out
    }

    private static func blank(_ value: String) -> Bool {
        value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }
}
