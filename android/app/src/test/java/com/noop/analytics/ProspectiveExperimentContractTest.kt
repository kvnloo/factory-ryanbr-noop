package com.noop.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Golden validation vectors mirror ProspectiveExperimentContractTests.swift.
 */
class ProspectiveExperimentContractTest {

    private fun validContract(
        minimumCoverage: Double = 0.8,
        minimumSamples: Int = 5,
        createdAtMs: Long = 100,
        predictionLockedAtMs: Long = 150,
        baselineWindow: ExperimentWindow = ExperimentWindow(500, 1_500),
        exposureWindow: ExperimentWindow = ExperimentWindow(2_000, 2_100),
        outcomeWindow: ExperimentWindow = ExperimentWindow(2_100, 3_000),
    ) = ProspectiveExperimentContract(
        id = "exp-001",
        title = "Earlier caffeine cutoff",
        hypothesis = "Earlier caffeine cutoff improves next-night sleep efficiency.",
        factorKey = "caffeine_timing",
        primaryMetricKey = "sleep_efficiency",
        baselineWindow = baselineWindow,
        exposureWindow = exposureWindow,
        outcomeWindow = outcomeWindow,
        predictedDirection = ExperimentPredictionDirection.INCREASE,
        minimumCoverage = minimumCoverage,
        minimumSamples = minimumSamples,
        falsificationRule = "Fail if the prespecified effect is not positive at adequate coverage.",
        createdAtMs = createdAtMs,
        predictionLockedAtMs = predictionLockedAtMs,
        analysisRecipeVersion = "sleep-efficiency-v1",
        status = ExperimentStatus.PLANNED,
    )

    @Test fun valid_contract_has_no_issues() {
        assertEquals(emptyList<ExperimentContractIssue>(), ProspectiveExperimentValidator.issues(validContract()))
    }

    @Test fun prediction_must_be_locked_before_exposure_starts() {
        assertEquals(
            listOf(ExperimentContractIssue.PREDICTION_LOCKED_AFTER_EXPOSURE_START),
            ProspectiveExperimentValidator.issues(validContract(predictionLockedAtMs = 2_001)),
        )
    }

    @Test fun created_timestamp_cannot_follow_prediction_lock() {
        assertEquals(
            listOf(ExperimentContractIssue.CREATED_AFTER_PREDICTION_LOCK),
            ProspectiveExperimentValidator.issues(validContract(createdAtMs = 151, predictionLockedAtMs = 150)),
        )
    }

    @Test fun coverage_and_sample_gates_must_be_usable() {
        assertEquals(
            listOf(ExperimentContractIssue.INVALID_MINIMUM_COVERAGE),
            ProspectiveExperimentValidator.issues(validContract(minimumCoverage = 0.0)),
        )
        assertEquals(
            listOf(ExperimentContractIssue.INVALID_MINIMUM_COVERAGE),
            ProspectiveExperimentValidator.issues(validContract(minimumCoverage = 1.01)),
        )
        assertEquals(
            listOf(ExperimentContractIssue.INVALID_MINIMUM_COVERAGE),
            ProspectiveExperimentValidator.issues(validContract(minimumCoverage = Double.POSITIVE_INFINITY)),
        )
        assertEquals(
            listOf(ExperimentContractIssue.INVALID_MINIMUM_SAMPLES),
            ProspectiveExperimentValidator.issues(validContract(minimumSamples = 0)),
        )
    }

    @Test fun window_ordering_issues_are_stable() {
        assertEquals(
            listOf(
                ExperimentContractIssue.BASELINE_OVERLAPS_EXPOSURE,
                ExperimentContractIssue.OUTCOME_STARTS_BEFORE_EXPOSURE_ENDS,
            ),
            ProspectiveExperimentValidator.issues(
                validContract(
                    baselineWindow = ExperimentWindow(1_000, 2_050),
                    exposureWindow = ExperimentWindow(2_000, 2_100),
                    outcomeWindow = ExperimentWindow(2_050, 3_000),
                ),
            ),
        )
    }

    @Test fun invalid_windows_are_rejected() {
        assertEquals(
            listOf(
                ExperimentContractIssue.INVALID_BASELINE_WINDOW,
                ExperimentContractIssue.INVALID_EXPOSURE_WINDOW,
                ExperimentContractIssue.INVALID_OUTCOME_WINDOW,
            ),
            ProspectiveExperimentValidator.issues(
                validContract(
                    baselineWindow = ExperimentWindow(1_000, 1_000),
                    exposureWindow = ExperimentWindow(2_100, 2_000),
                    outcomeWindow = ExperimentWindow(3_000, 3_000),
                ),
            ),
        )
    }

    @Test fun wire_values_are_stable() {
        assertEquals("no_meaningful_change", ExperimentPredictionDirection.NO_MEANINGFUL_CHANGE.wireValue)
        assertEquals("empty_primary_metric_key", ExperimentContractIssue.EMPTY_PRIMARY_METRIC_KEY.wireValue)
        assertEquals(
            "prediction_locked_after_exposure_start",
            ExperimentContractIssue.PREDICTION_LOCKED_AFTER_EXPOSURE_START.wireValue,
        )
    }
}
