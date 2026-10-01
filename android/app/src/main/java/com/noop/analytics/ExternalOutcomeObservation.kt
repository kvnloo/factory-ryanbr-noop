package com.noop.analytics

/*
 * ExternalOutcomeObservation.kt — one local outcome NOOP does not natively own.
 *
 * Pure, deterministic, DB-free. Faithful Kotlin mirror of
 * StrandAnalytics/ExternalOutcomeObservation.swift.
 *
 * This models only what the daily metricSeries shape cannot preserve: multiple
 * timestamped observations per day/session plus source + measurement-version provenance.
 */

data class ExternalOutcomeObservation(
    val id: String,
    val observedAtMs: Long,
    val recordedAtMs: Long,
    val outcomeKey: String,
    val value: Double,
    val unit: String,
    val sourceId: String,
    val sourceRecordId: String?,
    val sessionId: String?,
    val measurementVersion: String,
)

enum class ExternalOutcomeObservationIssue(val wireValue: String) {
    EMPTY_ID("empty_id"),
    INVALID_OBSERVED_AT("invalid_observed_at"),
    INVALID_RECORDED_AT("invalid_recorded_at"),
    RECORDED_BEFORE_OBSERVED("recorded_before_observed"),
    EMPTY_OUTCOME_KEY("empty_outcome_key"),
    INVALID_VALUE("invalid_value"),
    EMPTY_UNIT("empty_unit"),
    EMPTY_SOURCE_ID("empty_source_id"),
    BLANK_SOURCE_RECORD_ID("blank_source_record_id"),
    BLANK_SESSION_ID("blank_session_id"),
    EMPTY_MEASUREMENT_VERSION("empty_measurement_version"),
}

object ExternalOutcomeObservationValidator {

    fun issues(observation: ExternalOutcomeObservation): List<ExternalOutcomeObservationIssue> {
        val out = mutableListOf<ExternalOutcomeObservationIssue>()

        if (observation.id.isBlank()) out += ExternalOutcomeObservationIssue.EMPTY_ID
        if (observation.observedAtMs < 0) out += ExternalOutcomeObservationIssue.INVALID_OBSERVED_AT
        if (observation.recordedAtMs < 0) out += ExternalOutcomeObservationIssue.INVALID_RECORDED_AT
        if (observation.recordedAtMs < observation.observedAtMs) {
            out += ExternalOutcomeObservationIssue.RECORDED_BEFORE_OBSERVED
        }
        if (observation.outcomeKey.isBlank()) out += ExternalOutcomeObservationIssue.EMPTY_OUTCOME_KEY
        if (!observation.value.isFinite()) out += ExternalOutcomeObservationIssue.INVALID_VALUE
        if (observation.unit.isBlank()) out += ExternalOutcomeObservationIssue.EMPTY_UNIT
        if (observation.sourceId.isBlank()) out += ExternalOutcomeObservationIssue.EMPTY_SOURCE_ID

        if (observation.sourceRecordId != null && observation.sourceRecordId.isBlank()) {
            out += ExternalOutcomeObservationIssue.BLANK_SOURCE_RECORD_ID
        }
        if (observation.sessionId != null && observation.sessionId.isBlank()) {
            out += ExternalOutcomeObservationIssue.BLANK_SESSION_ID
        }
        if (observation.measurementVersion.isBlank()) {
            out += ExternalOutcomeObservationIssue.EMPTY_MEASUREMENT_VERSION
        }

        return out
    }
}
