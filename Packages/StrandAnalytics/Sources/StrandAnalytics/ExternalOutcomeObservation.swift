import Foundation

// ExternalOutcomeObservation.swift — one local outcome NOOP does not natively own.
//
// Pure, deterministic, DB-free. This intentionally models only the semantics that
// metricSeries cannot represent: multiple timestamped observations per day/session,
// explicit source identity, and the measurement protocol/version that produced the value.
//
// No persistence, import transport, scoring, recommendation, or causal interpretation lives here.

public struct ExternalOutcomeObservation: Codable, Equatable, Sendable {
    public let id: String
    public let observedAtMs: Int64
    public let recordedAtMs: Int64
    public let outcomeKey: String
    public let value: Double
    public let unit: String
    public let sourceId: String
    public let sourceRecordId: String?
    public let sessionId: String?
    public let measurementVersion: String

    public init(
        id: String,
        observedAtMs: Int64,
        recordedAtMs: Int64,
        outcomeKey: String,
        value: Double,
        unit: String,
        sourceId: String,
        sourceRecordId: String?,
        sessionId: String?,
        measurementVersion: String
    ) {
        self.id = id
        self.observedAtMs = observedAtMs
        self.recordedAtMs = recordedAtMs
        self.outcomeKey = outcomeKey
        self.value = value
        self.unit = unit
        self.sourceId = sourceId
        self.sourceRecordId = sourceRecordId
        self.sessionId = sessionId
        self.measurementVersion = measurementVersion
    }
}

public enum ExternalOutcomeObservationIssue: String, Codable, CaseIterable, Sendable {
    case emptyID = "empty_id"
    case invalidObservedAt = "invalid_observed_at"
    case invalidRecordedAt = "invalid_recorded_at"
    case recordedBeforeObserved = "recorded_before_observed"
    case emptyOutcomeKey = "empty_outcome_key"
    case invalidValue = "invalid_value"
    case emptyUnit = "empty_unit"
    case emptySourceID = "empty_source_id"
    case blankSourceRecordID = "blank_source_record_id"
    case blankSessionID = "blank_session_id"
    case emptyMeasurementVersion = "empty_measurement_version"
}

public enum ExternalOutcomeObservationValidator {

    public static func issues(_ observation: ExternalOutcomeObservation) -> [ExternalOutcomeObservationIssue] {
        var out: [ExternalOutcomeObservationIssue] = []

        if blank(observation.id) { out.append(.emptyID) }
        if observation.observedAtMs < 0 { out.append(.invalidObservedAt) }
        if observation.recordedAtMs < 0 { out.append(.invalidRecordedAt) }
        if observation.recordedAtMs < observation.observedAtMs {
            out.append(.recordedBeforeObserved)
        }
        if blank(observation.outcomeKey) { out.append(.emptyOutcomeKey) }
        if !observation.value.isFinite { out.append(.invalidValue) }
        if blank(observation.unit) { out.append(.emptyUnit) }
        if blank(observation.sourceId) { out.append(.emptySourceID) }

        if let sourceRecordId = observation.sourceRecordId, blank(sourceRecordId) {
            out.append(.blankSourceRecordID)
        }
        if let sessionId = observation.sessionId, blank(sessionId) {
            out.append(.blankSessionID)
        }
        if blank(observation.measurementVersion) {
            out.append(.emptyMeasurementVersion)
        }

        return out
    }

    private static func blank(_ value: String) -> Bool {
        value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }
}
