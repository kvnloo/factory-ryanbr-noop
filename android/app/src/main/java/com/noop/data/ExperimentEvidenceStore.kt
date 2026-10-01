package com.noop.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

enum class ExperimentEvidenceWriteResult {
    INSERTED,
    ALREADY_PRESENT,
}

enum class ExperimentEvidenceStoreErrorCode {
    CONTRACT_CONFLICT,
    RECEIPT_CONFLICT,
    MISSING_CONTRACT,
    INVALID_CONTRACT_CHRONOLOGY,
    INVALID_RECEIPT_CHRONOLOGY,
    INVALID_RECEIPT_SHAPE,
}

class ExperimentEvidenceStoreException(
    val code: ExperimentEvidenceStoreErrorCode,
    val recordId: String,
) : IllegalStateException("$code: $recordId")

data class ExperimentContractRecord(
    val id: String,
    val title: String,
    val hypothesis: String,
    val factorKey: String,
    val primaryMetricKey: String,
    val baselineStartMs: Long,
    val baselineEndMs: Long,
    val exposureStartMs: Long,
    val exposureEndMs: Long,
    val outcomeStartMs: Long,
    val outcomeEndMs: Long,
    val predictedDirection: String,
    val minimumCoverage: Double,
    val minimumSamples: Int,
    val falsificationRule: String,
    val createdAtMs: Long,
    val predictionLockedAtMs: Long,
    val analysisRecipeVersion: String,
)

data class PersistedExperimentContractRecord(
    val contract: ExperimentContractRecord,
    val persistedAtMs: Long,
)

data class ExperimentEvidenceReceiptRecord(
    val id: String,
    val contractId: String,
    val analyzedAtMs: Long,
    val baselineSampleCount: Int,
    val outcomeSampleCount: Int,
    val baselineCoverage: Double,
    val outcomeCoverage: Double,
    val effectEstimate: Double?,
    val uncertaintyLower: Double?,
    val uncertaintyUpper: Double?,
    val result: String,
    val sourceIds: List<String>,
    val confounderAnnotations: List<String>,
)

data class PersistedExperimentEvidenceReceiptRecord(
    val receipt: ExperimentEvidenceReceiptRecord,
    val persistedAtMs: Long,
)

/**
 * Append-only evidence DAO.
 *
 * The DAO stamps persistedAtMs itself on first insert. Same id + same evidence is an idempotent
 * retry even later; same id + changed evidence is a hard conflict.
 *
 * persistedAtMs is local-clock provenance, not cryptographic proof against a deliberately
 * backdated operating-system clock.
 */
@Dao
abstract class ExperimentEvidenceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertContractRaw(row: ExperimentContractEntity): Long

    @Query("SELECT * FROM experimentContract WHERE id = :id")
    protected abstract suspend fun experimentContractEntity(id: String): ExperimentContractEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertReceiptRaw(row: ExperimentEvidenceReceiptEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertReceiptSourcesRaw(rows: List<ExperimentReceiptSourceEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertReceiptConfoundersRaw(rows: List<ExperimentReceiptConfounderEntity>)

    @Query("SELECT * FROM experimentReceipt WHERE id = :id")
    protected abstract suspend fun receiptMain(id: String): ExperimentEvidenceReceiptEntity?

    @Query("SELECT sourceId FROM experimentReceiptSource WHERE receiptId = :id ORDER BY ordinal ASC")
    protected abstract suspend fun receiptSourceIds(id: String): List<String>

    @Query("SELECT annotation FROM experimentReceiptConfounder WHERE receiptId = :id ORDER BY ordinal ASC")
    protected abstract suspend fun receiptConfounders(id: String): List<String>

    @Transaction
    open suspend fun experimentContract(id: String): PersistedExperimentContractRecord? {
        val row = experimentContractEntity(id) ?: return null
        return PersistedExperimentContractRecord(
            contract = row.toEvidenceRecord(),
            persistedAtMs = row.persistedAtMs,
        )
    }

    @Transaction
    open suspend fun persistLockedExperimentContract(
        record: ExperimentContractRecord,
    ): ExperimentEvidenceWriteResult {
        val existing = experimentContractEntity(record.id)
        if (existing != null) {
            if (existing.toEvidenceRecord() == record) {
                return ExperimentEvidenceWriteResult.ALREADY_PRESENT
            }
            throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.CONTRACT_CONFLICT,
                record.id,
            )
        }

        val persistedAtMs = System.currentTimeMillis()
        if (!validContractChronology(record, persistedAtMs)) {
            throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.INVALID_CONTRACT_CHRONOLOGY,
                record.id,
            )
        }

        val inserted = insertContractRaw(record.toEntity(persistedAtMs))
        if (inserted != -1L) return ExperimentEvidenceWriteResult.INSERTED

        val raced = experimentContractEntity(record.id)
        if (raced?.toEvidenceRecord() == record) return ExperimentEvidenceWriteResult.ALREADY_PRESENT
        throw ExperimentEvidenceStoreException(
            ExperimentEvidenceStoreErrorCode.CONTRACT_CONFLICT,
            record.id,
        )
    }

    @Transaction
    open suspend fun experimentEvidenceReceipt(id: String): PersistedExperimentEvidenceReceiptRecord? {
        val main = receiptMain(id) ?: return null
        return PersistedExperimentEvidenceReceiptRecord(
            receipt = main.toEvidenceRecord(
                sourceIds = receiptSourceIds(id),
                confounderAnnotations = receiptConfounders(id),
            ),
            persistedAtMs = main.persistedAtMs,
        )
    }

    @Transaction
    open suspend fun persistExperimentEvidenceReceipt(
        record: ExperimentEvidenceReceiptRecord,
    ): ExperimentEvidenceWriteResult {
        if ((record.uncertaintyLower == null) != (record.uncertaintyUpper == null)) {
            throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.INVALID_RECEIPT_SHAPE,
                record.id,
            )
        }

        val existingMain = receiptMain(record.id)
        if (existingMain != null) {
            val existing = existingMain.toEvidenceRecord(
                sourceIds = receiptSourceIds(record.id),
                confounderAnnotations = receiptConfounders(record.id),
            )
            if (existing == record) return ExperimentEvidenceWriteResult.ALREADY_PRESENT
            throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.RECEIPT_CONFLICT,
                record.id,
            )
        }

        val contract = experimentContractEntity(record.contractId)
            ?: throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.MISSING_CONTRACT,
                record.contractId,
            )

        val persistedAtMs = System.currentTimeMillis()
        if (record.analyzedAtMs < contract.outcomeEndMs || persistedAtMs < record.analyzedAtMs) {
            throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.INVALID_RECEIPT_CHRONOLOGY,
                record.id,
            )
        }

        val inserted = insertReceiptRaw(record.toEntity(persistedAtMs))
        if (inserted == -1L) {
            val racedMain = receiptMain(record.id)
            val raced = racedMain?.toEvidenceRecord(
                sourceIds = receiptSourceIds(record.id),
                confounderAnnotations = receiptConfounders(record.id),
            )
            if (raced == record) return ExperimentEvidenceWriteResult.ALREADY_PRESENT
            throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.RECEIPT_CONFLICT,
                record.id,
            )
        }

        if (record.sourceIds.isNotEmpty()) {
            insertReceiptSourcesRaw(
                record.sourceIds.mapIndexed { ordinal, sourceId ->
                    ExperimentReceiptSourceEntity(record.id, ordinal, sourceId)
                },
            )
        }
        if (record.confounderAnnotations.isNotEmpty()) {
            insertReceiptConfoundersRaw(
                record.confounderAnnotations.mapIndexed { ordinal, annotation ->
                    ExperimentReceiptConfounderEntity(record.id, ordinal, annotation)
                },
            )
        }
        return ExperimentEvidenceWriteResult.INSERTED
    }

    private fun validContractChronology(
        record: ExperimentContractRecord,
        persistedAtMs: Long,
    ): Boolean =
        record.baselineStartMs < record.baselineEndMs &&
            record.exposureStartMs < record.exposureEndMs &&
            record.outcomeStartMs < record.outcomeEndMs &&
            record.baselineEndMs <= record.exposureStartMs &&
            record.outcomeStartMs >= record.exposureEndMs &&
            record.createdAtMs <= record.predictionLockedAtMs &&
            record.predictionLockedAtMs <= persistedAtMs &&
            persistedAtMs <= record.exposureStartMs
}

private fun ExperimentContractRecord.toEntity(persistedAtMs: Long) = ExperimentContractEntity(
    id = id,
    title = title,
    hypothesis = hypothesis,
    factorKey = factorKey,
    primaryMetricKey = primaryMetricKey,
    baselineStartMs = baselineStartMs,
    baselineEndMs = baselineEndMs,
    exposureStartMs = exposureStartMs,
    exposureEndMs = exposureEndMs,
    outcomeStartMs = outcomeStartMs,
    outcomeEndMs = outcomeEndMs,
    predictedDirection = predictedDirection,
    minimumCoverage = minimumCoverage,
    minimumSamples = minimumSamples,
    falsificationRule = falsificationRule,
    createdAtMs = createdAtMs,
    predictionLockedAtMs = predictionLockedAtMs,
    analysisRecipeVersion = analysisRecipeVersion,
    persistedAtMs = persistedAtMs,
)

private fun ExperimentContractEntity.toEvidenceRecord() = ExperimentContractRecord(
    id = id,
    title = title,
    hypothesis = hypothesis,
    factorKey = factorKey,
    primaryMetricKey = primaryMetricKey,
    baselineStartMs = baselineStartMs,
    baselineEndMs = baselineEndMs,
    exposureStartMs = exposureStartMs,
    exposureEndMs = exposureEndMs,
    outcomeStartMs = outcomeStartMs,
    outcomeEndMs = outcomeEndMs,
    predictedDirection = predictedDirection,
    minimumCoverage = minimumCoverage,
    minimumSamples = minimumSamples,
    falsificationRule = falsificationRule,
    createdAtMs = createdAtMs,
    predictionLockedAtMs = predictionLockedAtMs,
    analysisRecipeVersion = analysisRecipeVersion,
)

private fun ExperimentEvidenceReceiptRecord.toEntity(
    persistedAtMs: Long,
) = ExperimentEvidenceReceiptEntity(
    id = id,
    contractId = contractId,
    analyzedAtMs = analyzedAtMs,
    persistedAtMs = persistedAtMs,
    baselineSampleCount = baselineSampleCount,
    outcomeSampleCount = outcomeSampleCount,
    baselineCoverage = baselineCoverage,
    outcomeCoverage = outcomeCoverage,
    effectEstimate = effectEstimate,
    uncertaintyLower = uncertaintyLower,
    uncertaintyUpper = uncertaintyUpper,
    result = result,
)

private fun ExperimentEvidenceReceiptEntity.toEvidenceRecord(
    sourceIds: List<String>,
    confounderAnnotations: List<String>,
) = ExperimentEvidenceReceiptRecord(
    id = id,
    contractId = contractId,
    analyzedAtMs = analyzedAtMs,
    baselineSampleCount = baselineSampleCount,
    outcomeSampleCount = outcomeSampleCount,
    baselineCoverage = baselineCoverage,
    outcomeCoverage = outcomeCoverage,
    effectEstimate = effectEstimate,
    uncertaintyLower = uncertaintyLower,
    uncertaintyUpper = uncertaintyUpper,
    result = result,
    sourceIds = sourceIds,
    confounderAnnotations = confounderAnnotations,
)
