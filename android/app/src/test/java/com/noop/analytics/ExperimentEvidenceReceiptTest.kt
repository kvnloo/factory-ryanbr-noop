package com.noop.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

/** Golden validation vectors mirror ExperimentEvidenceReceiptTests.swift. */
class ExperimentEvidenceReceiptTest {

    private fun validContract(
        minimumCoverage: Double = 0.8,
        minimumSamples: Int = 5,
        outcomeWindow: ExperimentWindow = ExperimentWindow(2_100, 3_000),
    ) = ProspectiveExperimentContract(
        id = "exp-001",
        title = "Earlier caffeine cutoff",
        hypothesis = "Earlier caffeine cutoff improves next-night sleep efficiency.",
        factorKey = "caffeine_timing",
        primaryMetricKey = "sleep_efficiency",
        baselineWindow = ExperimentWindow(500, 1_500),
        exposureWindow = ExperimentWindow(2_000, 2_100),
        outcomeWindow = outcomeWindow,
        predictedDirection = ExperimentPredictionDirection.INCREASE,
        minimumCoverage = minimumCoverage,
        minimumSamples = minimumSamples,
        falsificationRule = "Fail if the prespecified effect is not positive at adequate coverage.",
        createdAtMs = 100,
        predictionLockedAtMs = 150,
        analysisRecipeVersion = "sleep-efficiency-v1",
        status = ExperimentStatus.PLANNED,
    )

    private fun validReceipt(
        contract: ProspectiveExperimentContract = validContract(),
        analyzedAtMs: Long = 3_100,
        sourceIds: List<String> = listOf("journal", "metric-series"),
        baselineSampleCount: Int = 8,
        outcomeSampleCount: Int = 8,
        baselineCoverage: Double = 0.9,
        outcomeCoverage: Double = 0.9,
        effectEstimate: Double? = 0.03,
        uncertainty: ExperimentUncertaintyInterval? = ExperimentUncertaintyInterval(0.01, 0.05),
        confounderAnnotations: List<String> = listOf("travel"),
        result: ExperimentEvidenceResult = ExperimentEvidenceResult.SUPPORTS,
    ) = ExperimentEvidenceReceipt(
        id = "receipt-001",
        contract = contract,
        analyzedAtMs = analyzedAtMs,
        sourceIds = sourceIds,
        baselineSampleCount = baselineSampleCount,
        outcomeSampleCount = outcomeSampleCount,
        baselineCoverage = baselineCoverage,
        outcomeCoverage = outcomeCoverage,
        effectEstimate = effectEstimate,
        uncertainty = uncertainty,
        confounderAnnotations = confounderAnnotations,
        result = result,
    )

    @Test fun valid_conclusive_receipt_has_no_issues() {
        assertEquals(emptyList<ExperimentEvidenceReceiptIssue>(), ExperimentEvidenceReceiptValidator.issues(validReceipt()))
    }

    @Test fun analysis_must_wait_for_outcome_window_to_close() {
        assertEquals(
            listOf(ExperimentEvidenceReceiptIssue.ANALYZED_BEFORE_OUTCOME_END),
            ExperimentEvidenceReceiptValidator.issues(validReceipt(analyzedAtMs = 2_999)),
        )
    }

    @Test fun source_provenance_must_be_present_nonblank_and_unique() {
        assertEquals(
            listOf(ExperimentEvidenceReceiptIssue.EMPTY_SOURCE_IDS),
            ExperimentEvidenceReceiptValidator.issues(validReceipt(sourceIds = emptyList())),
        )
        assertEquals(
            listOf(ExperimentEvidenceReceiptIssue.BLANK_SOURCE_ID),
            ExperimentEvidenceReceiptValidator.issues(validReceipt(sourceIds = listOf("journal", " "))),
        )
        assertEquals(
            listOf(ExperimentEvidenceReceiptIssue.DUPLICATE_SOURCE_ID),
            ExperimentEvidenceReceiptValidator.issues(validReceipt(sourceIds = listOf("journal", "journal"))),
        )
    }

    @Test fun observed_coverage_must_be_finite_and_between_zero_and_one() {
        assertEquals(
            listOf(
                ExperimentEvidenceReceiptIssue.INVALID_BASELINE_COVERAGE,
                ExperimentEvidenceReceiptIssue.CONCLUSIVE_BELOW_COVERAGE_GATE,
            ),
            ExperimentEvidenceReceiptValidator.issues(validReceipt(baselineCoverage = -0.01)),
        )
        assertEquals(
            listOf(ExperimentEvidenceReceiptIssue.INVALID_OUTCOME_COVERAGE),
            ExperimentEvidenceReceiptValidator.issues(validReceipt(outcomeCoverage = 1.01)),
        )
        assertEquals(
            listOf(ExperimentEvidenceReceiptIssue.INVALID_BASELINE_COVERAGE),
            ExperimentEvidenceReceiptValidator.issues(validReceipt(baselineCoverage = Double.POSITIVE_INFINITY)),
        )
    }

    @Test fun conclusive_result_requires_effect_uncertainty_and_evidence_gates() {
        assertEquals(
            listOf(
                ExperimentEvidenceReceiptIssue.CONCLUSIVE_WITHOUT_EFFECT,
                ExperimentEvidenceReceiptIssue.CONCLUSIVE_WITHOUT_UNCERTAINTY,
                ExperimentEvidenceReceiptIssue.CONCLUSIVE_BELOW_COVERAGE_GATE,
                ExperimentEvidenceReceiptIssue.CONCLUSIVE_BELOW_SAMPLE_GATE,
            ),
            ExperimentEvidenceReceiptValidator.issues(
                validReceipt(
                    baselineSampleCount = 4,
                    baselineCoverage = 0.7,
                    effectEstimate = null,
                    uncertainty = null,
                ),
            ),
        )
    }

    @Test fun inconclusive_receipt_may_remain_below_evidence_gates() {
        assertEquals(
            emptyList<ExperimentEvidenceReceiptIssue>(),
            ExperimentEvidenceReceiptValidator.issues(
                validReceipt(
                    baselineSampleCount = 0,
                    outcomeSampleCount = 0,
                    baselineCoverage = 0.0,
                    outcomeCoverage = 0.0,
                    effectEstimate = null,
                    uncertainty = null,
                    result = ExperimentEvidenceResult.INCONCLUSIVE,
                ),
            ),
        )
    }

    @Test fun effect_and_uncertainty_must_be_finite_and_coherent() {
        assertEquals(
            listOf(ExperimentEvidenceReceiptIssue.INVALID_EFFECT_ESTIMATE),
            ExperimentEvidenceReceiptValidator.issues(validReceipt(effectEstimate = Double.POSITIVE_INFINITY)),
        )
        assertEquals(
            listOf(ExperimentEvidenceReceiptIssue.INVALID_UNCERTAINTY_INTERVAL),
            ExperimentEvidenceReceiptValidator.issues(
                validReceipt(uncertainty = ExperimentUncertaintyInterval(0.05, 0.01)),
            ),
        )
        assertEquals(
            listOf(ExperimentEvidenceReceiptIssue.EFFECT_OUTSIDE_UNCERTAINTY),
            ExperimentEvidenceReceiptValidator.issues(
                validReceipt(uncertainty = ExperimentUncertaintyInterval(0.04, 0.05)),
            ),
        )
    }

    @Test fun invalid_contract_cannot_be_hidden_inside_receipt() {
        val invalid = validContract(outcomeWindow = ExperimentWindow(2_050, 3_000))
        assertEquals(
            listOf(ExperimentEvidenceReceiptIssue.INVALID_CONTRACT_SNAPSHOT),
            ExperimentEvidenceReceiptValidator.issues(validReceipt(contract = invalid)),
        )
    }

    @Test fun derived_lag_missingness_and_wire_values_are_stable() {
        val receipt = validReceipt()
        assertEquals(0L, receipt.lagMs)
        assertEquals(0.1, receipt.baselineMissingness, 0.000_001)
        assertEquals(0.1, receipt.outcomeMissingness, 0.000_001)
        assertEquals("inconclusive", ExperimentEvidenceResult.INCONCLUSIVE.wireValue)
        assertEquals(
            "conclusive_below_coverage_gate",
            ExperimentEvidenceReceiptIssue.CONCLUSIVE_BELOW_COVERAGE_GATE.wireValue,
        )
    }
}
