package com.noop.analytics

/*
 * ProspectiveExperimentContract.kt — preregister a local experiment before exposure.
 *
 * Pure, deterministic, DB-free. Faithful Kotlin mirror of
 * StrandAnalytics/ProspectiveExperimentContract.swift.
 *
 * This file intentionally contains only the evidence contract + structural validation:
 * no persistence, scheduling, causal inference, recommendation, or scoring.
 */

enum class ExperimentStatus(val wireValue: String) {
    PLANNED("planned"),
    RUNNING("running"),
    COMPLETED("completed"),
    ABANDONED("abandoned"),
}

enum class ExperimentPredictionDirection(val wireValue: String) {
    INCREASE("increase"),
    DECREASE("decrease"),
    NO_MEANINGFUL_CHANGE("no_meaningful_change"),
}

data class ExperimentWindow(
    val startMs: Long,
    val endMs: Long,
) {
    val durationMs: Long get() = endMs - startMs
}

data class ProspectiveExperimentContract(
    val id: String,
    val title: String,
    val hypothesis: String,
    val factorKey: String,
    val primaryMetricKey: String,
    val baselineWindow: ExperimentWindow,
    val exposureWindow: ExperimentWindow,
    val outcomeWindow: ExperimentWindow,
    val predictedDirection: ExperimentPredictionDirection,
    val minimumCoverage: Double,
    val minimumSamples: Int,
    val falsificationRule: String,
    val createdAtMs: Long,
    val predictionLockedAtMs: Long,
    val analysisRecipeVersion: String,
    val status: ExperimentStatus,
)

enum class ExperimentContractIssue(val wireValue: String) {
    EMPTY_ID("empty_id"),
    EMPTY_TITLE("empty_title"),
    EMPTY_HYPOTHESIS("empty_hypothesis"),
    EMPTY_FACTOR_KEY("empty_factor_key"),
    EMPTY_PRIMARY_METRIC_KEY("empty_primary_metric_key"),
    INVALID_BASELINE_WINDOW("invalid_baseline_window"),
    INVALID_EXPOSURE_WINDOW("invalid_exposure_window"),
    INVALID_OUTCOME_WINDOW("invalid_outcome_window"),
    BASELINE_OVERLAPS_EXPOSURE("baseline_overlaps_exposure"),
    OUTCOME_STARTS_BEFORE_EXPOSURE_ENDS("outcome_starts_before_exposure_ends"),
    INVALID_MINIMUM_COVERAGE("invalid_minimum_coverage"),
    INVALID_MINIMUM_SAMPLES("invalid_minimum_samples"),
    EMPTY_FALSIFICATION_RULE("empty_falsification_rule"),
    EMPTY_ANALYSIS_RECIPE_VERSION("empty_analysis_recipe_version"),
    CREATED_AFTER_PREDICTION_LOCK("created_after_prediction_lock"),
    PREDICTION_LOCKED_AFTER_EXPOSURE_START("prediction_locked_after_exposure_start"),
}

object ProspectiveExperimentValidator {

    fun issues(contract: ProspectiveExperimentContract): List<ExperimentContractIssue> {
        val out = mutableListOf<ExperimentContractIssue>()

        if (contract.id.isBlank()) out += ExperimentContractIssue.EMPTY_ID
        if (contract.title.isBlank()) out += ExperimentContractIssue.EMPTY_TITLE
        if (contract.hypothesis.isBlank()) out += ExperimentContractIssue.EMPTY_HYPOTHESIS
        if (contract.factorKey.isBlank()) out += ExperimentContractIssue.EMPTY_FACTOR_KEY
        if (contract.primaryMetricKey.isBlank()) out += ExperimentContractIssue.EMPTY_PRIMARY_METRIC_KEY

        if (contract.baselineWindow.startMs >= contract.baselineWindow.endMs) {
            out += ExperimentContractIssue.INVALID_BASELINE_WINDOW
        }
        if (contract.exposureWindow.startMs >= contract.exposureWindow.endMs) {
            out += ExperimentContractIssue.INVALID_EXPOSURE_WINDOW
        }
        if (contract.outcomeWindow.startMs >= contract.outcomeWindow.endMs) {
            out += ExperimentContractIssue.INVALID_OUTCOME_WINDOW
        }

        if (contract.baselineWindow.endMs > contract.exposureWindow.startMs) {
            out += ExperimentContractIssue.BASELINE_OVERLAPS_EXPOSURE
        }
        if (contract.outcomeWindow.startMs < contract.exposureWindow.endMs) {
            out += ExperimentContractIssue.OUTCOME_STARTS_BEFORE_EXPOSURE_ENDS
        }

        if (!contract.minimumCoverage.isFinite()
            || contract.minimumCoverage <= 0.0
            || contract.minimumCoverage > 1.0
        ) {
            out += ExperimentContractIssue.INVALID_MINIMUM_COVERAGE
        }
        if (contract.minimumSamples <= 0) {
            out += ExperimentContractIssue.INVALID_MINIMUM_SAMPLES
        }

        if (contract.falsificationRule.isBlank()) {
            out += ExperimentContractIssue.EMPTY_FALSIFICATION_RULE
        }
        if (contract.analysisRecipeVersion.isBlank()) {
            out += ExperimentContractIssue.EMPTY_ANALYSIS_RECIPE_VERSION
        }

        if (contract.createdAtMs > contract.predictionLockedAtMs) {
            out += ExperimentContractIssue.CREATED_AFTER_PREDICTION_LOCK
        }
        if (contract.predictionLockedAtMs > contract.exposureWindow.startMs) {
            out += ExperimentContractIssue.PREDICTION_LOCKED_AFTER_EXPOSURE_START
        }

        return out
    }
}
