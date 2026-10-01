package com.noop.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

/** Golden validation vectors mirror ExternalOutcomeObservationTests.swift. */
class ExternalOutcomeObservationTest {

    private fun validObservation(
        observedAtMs: Long = 1_000,
        recordedAtMs: Long = 1_100,
        value: Double = 247.0,
        sourceRecordId: String? = "pvt-42",
        sessionId: String? = "session-7",
    ) = ExternalOutcomeObservation(
        id = "obs-001",
        observedAtMs = observedAtMs,
        recordedAtMs = recordedAtMs,
        outcomeKey = "reaction_time_ms",
        value = value,
        unit = "ms",
        sourceId = "local-pvt",
        sourceRecordId = sourceRecordId,
        sessionId = sessionId,
        measurementVersion = "pvt-v1",
    )

    @Test fun valid_observation_has_no_issues() {
        assertEquals(
            emptyList<ExternalOutcomeObservationIssue>(),
            ExternalOutcomeObservationValidator.issues(validObservation()),
        )
    }

    @Test fun recorded_timestamp_cannot_precede_observation() {
        assertEquals(
            listOf(ExternalOutcomeObservationIssue.RECORDED_BEFORE_OBSERVED),
            ExternalOutcomeObservationValidator.issues(
                validObservation(observedAtMs = 1_000, recordedAtMs = 999),
            ),
        )
    }

    @Test fun timestamps_cannot_be_negative() {
        assertEquals(
            listOf(
                ExternalOutcomeObservationIssue.INVALID_OBSERVED_AT,
                ExternalOutcomeObservationIssue.INVALID_RECORDED_AT,
                ExternalOutcomeObservationIssue.RECORDED_BEFORE_OBSERVED,
            ),
            ExternalOutcomeObservationValidator.issues(
                validObservation(observedAtMs = -1, recordedAtMs = -2),
            ),
        )
    }

    @Test fun value_must_be_finite() {
        assertEquals(
            listOf(ExternalOutcomeObservationIssue.INVALID_VALUE),
            ExternalOutcomeObservationValidator.issues(
                validObservation(value = Double.POSITIVE_INFINITY),
            ),
        )
    }

    @Test fun optional_identifiers_must_be_meaningful_when_present() {
        assertEquals(
            listOf(ExternalOutcomeObservationIssue.BLANK_SOURCE_RECORD_ID),
            ExternalOutcomeObservationValidator.issues(validObservation(sourceRecordId = " ")),
        )
        assertEquals(
            listOf(ExternalOutcomeObservationIssue.BLANK_SESSION_ID),
            ExternalOutcomeObservationValidator.issues(validObservation(sessionId = "")),
        )
        assertEquals(
            emptyList<ExternalOutcomeObservationIssue>(),
            ExternalOutcomeObservationValidator.issues(
                validObservation(sourceRecordId = null, sessionId = null),
            ),
        )
    }

    @Test fun required_metadata_cannot_be_blank() {
        val observation = ExternalOutcomeObservation(
            id = " ",
            observedAtMs = 1_000,
            recordedAtMs = 1_100,
            outcomeKey = "",
            value = 1.0,
            unit = " ",
            sourceId = "",
            sourceRecordId = null,
            sessionId = null,
            measurementVersion = " ",
        )
        assertEquals(
            listOf(
                ExternalOutcomeObservationIssue.EMPTY_ID,
                ExternalOutcomeObservationIssue.EMPTY_OUTCOME_KEY,
                ExternalOutcomeObservationIssue.EMPTY_UNIT,
                ExternalOutcomeObservationIssue.EMPTY_SOURCE_ID,
                ExternalOutcomeObservationIssue.EMPTY_MEASUREMENT_VERSION,
            ),
            ExternalOutcomeObservationValidator.issues(observation),
        )
    }

    @Test fun wire_values_are_stable() {
        assertEquals("invalid_value", ExternalOutcomeObservationIssue.INVALID_VALUE.wireValue)
        assertEquals(
            "recorded_before_observed",
            ExternalOutcomeObservationIssue.RECORDED_BEFORE_OBSERVED.wireValue,
        )
    }
}
