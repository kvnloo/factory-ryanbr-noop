package com.noop.analytics

/*
 * ExperimentEvidenceReceipt.kt — auditable result for one prospective experiment.
 *
 * Pure, deterministic, DB-free. Faithful Kotlin mirror of
 * StrandAnalytics/ExperimentEvidenceReceipt.swift.
 *
 * The receipt carries the P0 contract snapshot supplied by the caller. This pure layer
 * validates structure but cannot prove the snapshot was actually persisted before exposure;
 * that audit guarantee belongs to the persistence layer.
 */

enum class ExperimentEvidenceResult(val wireValue: String) {
    SUPPORTS("supports"),
    CONTRADICTS("contradicts"),
    INCONCLUSIVE("inconclusive"),
}

data class ExperimentUncertaintyInterval(
    val lower: Double,
    val upper: Double,
)

data class ExperimentEvidenceReceipt(
    val id: String,
    val contract: ProspectiveExperimentContract,
    val analyzedAtMs: Long,
    val sourceIds: List<String>,
    val baselineSampleCount: Int,
    val outcomeSampleCount: Int,
    val baselineCoverage: Double,
    val outcomeCoverage: Double,
    val effectEstimate: Double?,
    val uncertainty: ExperimentUncertaintyInterval?,
    val confounderAnnotations: List<String>,
    val result: ExperimentEvidenceResult,
) {
    val lagMs: Long
        get() = contract.outcomeWindow.startMs - contract.exposureWindow.endMs

    val baselineMissingness: Double
        get() = 1.0 - baselineCoverage

    val outcomeMissingness: Double
        get() = 1.0 - outcomeCoverage
}

enum class ExperimentEvidenceReceiptIssue(val wireValue: String) {
    EMPTY_ID("empty_id"),
    INVALID_CONTRACT_SNAPSHOT("invalid_contract_snapshot"),
    ANALYZED_BEFORE_OUTCOME_END("analyzed_before_outcome_end"),
    EMPTY_SOURCE_IDS("empty_source_ids"),
    BLANK_SOURCE_ID("blank_source_id"),
    DUPLICATE_SOURCE_ID("duplicate_source_id"),
    INVALID_BASELINE_SAMPLE_COUNT("invalid_baseline_sample_count"),
    INVALID_OUTCOME_SAMPLE_COUNT("invalid_outcome_sample_count"),
    INVALID_BASELINE_COVERAGE("invalid_baseline_coverage"),
    INVALID_OUTCOME_COVERAGE("invalid_outcome_coverage"),
    INVALID_EFFECT_ESTIMATE("invalid_effect_estimate"),
    INVALID_UNCERTAINTY_INTERVAL("invalid_uncertainty_interval"),
    EFFECT_OUTSIDE_UNCERTAINTY("effect_outside_uncertainty"),
    BLANK_CONFOUNDER_ANNOTATION("blank_confounder_annotation"),
    CONCLUSIVE_WITHOUT_EFFECT("conclusive_without_effect"),
    CONCLUSIVE_WITHOUT_UNCERTAINTY("conclusive_without_uncertainty"),
    CONCLUSIVE_BELOW_COVERAGE_GATE("conclusive_below_coverage_gate"),
    CONCLUSIVE_BELOW_SAMPLE_GATE("conclusive_below_sample_gate"),
}

object ExperimentEvidenceReceiptValidator {

    fun issues(receipt: ExperimentEvidenceReceipt): List<ExperimentEvidenceReceiptIssue> {
        val out = mutableListOf<ExperimentEvidenceReceiptIssue>()

        if (receipt.id.isBlank()) out += ExperimentEvidenceReceiptIssue.EMPTY_ID
        if (ProspectiveExperimentValidator.issues(receipt.contract).isNotEmpty()) {
            out += ExperimentEvidenceReceiptIssue.INVALID_CONTRACT_SNAPSHOT
        }
        if (receipt.analyzedAtMs < receipt.contract.outcomeWindow.endMs) {
            out += ExperimentEvidenceReceiptIssue.ANALYZED_BEFORE_OUTCOME_END
        }

        if (receipt.sourceIds.isEmpty()) {
            out += ExperimentEvidenceReceiptIssue.EMPTY_SOURCE_IDS
        } else {
            if (receipt.sourceIds.any { it.isBlank() }) {
                out += ExperimentEvidenceReceiptIssue.BLANK_SOURCE_ID
            }
            if (receipt.sourceIds.toSet().size != receipt.sourceIds.size) {
                out += ExperimentEvidenceReceiptIssue.DUPLICATE_SOURCE_ID
            }
        }

        if (receipt.baselineSampleCount < 0) {
            out += ExperimentEvidenceReceiptIssue.INVALID_BASELINE_SAMPLE_COUNT
        }
        if (receipt.outcomeSampleCount < 0) {
            out += ExperimentEvidenceReceiptIssue.INVALID_OUTCOME_SAMPLE_COUNT
        }

        if (!validObservedCoverage(receipt.baselineCoverage)) {
            out += ExperimentEvidenceReceiptIssue.INVALID_BASELINE_COVERAGE
        }
        if (!validObservedCoverage(receipt.outcomeCoverage)) {
            out += ExperimentEvidenceReceiptIssue.INVALID_OUTCOME_COVERAGE
        }

        val effect = receipt.effectEstimate
        if (effect != null && !effect.isFinite()) {
            out += ExperimentEvidenceReceiptIssue.INVALID_EFFECT_ESTIMATE
        }

        val interval = receipt.uncertainty
        if (interval != null) {
            if (!interval.lower.isFinite() || !interval.upper.isFinite() || interval.lower > interval.upper) {
                out += ExperimentEvidenceReceiptIssue.INVALID_UNCERTAINTY_INTERVAL
            } else if (effect != null && effect.isFinite() &&
                (effect < interval.lower || effect > interval.upper)
            ) {
                out += ExperimentEvidenceReceiptIssue.EFFECT_OUTSIDE_UNCERTAINTY
            }
        }

        if (receipt.confounderAnnotations.any { it.isBlank() }) {
            out += ExperimentEvidenceReceiptIssue.BLANK_CONFOUNDER_ANNOTATION
        }

        if (receipt.result != ExperimentEvidenceResult.INCONCLUSIVE) {
            if (effect == null) {
                out += ExperimentEvidenceReceiptIssue.CONCLUSIVE_WITHOUT_EFFECT
            }
            if (interval == null) {
                out += ExperimentEvidenceReceiptIssue.CONCLUSIVE_WITHOUT_UNCERTAINTY
            }

            val coverageGate = receipt.contract.minimumCoverage
            if (receipt.baselineCoverage < coverageGate || receipt.outcomeCoverage < coverageGate) {
                out += ExperimentEvidenceReceiptIssue.CONCLUSIVE_BELOW_COVERAGE_GATE
            }

            val sampleGate = receipt.contract.minimumSamples
            if (receipt.baselineSampleCount < sampleGate || receipt.outcomeSampleCount < sampleGate) {
                out += ExperimentEvidenceReceiptIssue.CONCLUSIVE_BELOW_SAMPLE_GATE
            }
        }

        return out
    }

    private fun validObservedCoverage(value: Double): Boolean =
        value.isFinite() && value >= 0.0 && value <= 1.0
}
