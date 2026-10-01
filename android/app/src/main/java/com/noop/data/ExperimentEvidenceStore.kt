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

data class ExperimentEvidenceReceiptRecord(
    val receipt: ExperimentEvidenceReceiptEntity,
    val sourceIds: List<String>,
    val confounderAnnotations: List<String>,
)

/**
 * Append-only evidence DAO.
 *
 * Raw inserts are protected so normal callers cannot bypass exact-idempotency:
 * the same id + exact same payload is a no-op, while the same id + changed evidence throws.
 *
 * persistedAtMs is local-clock provenance, not cryptographic proof against a deliberately
 * backdated system clock.
 */
@Dao
abstract class ExperimentEvidenceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertContractRaw(row: ExperimentContractEntity): Long

    @Query("SELECT * FROM experimentContract WHERE id = :id")
    abstract suspend fun experimentContract(id: String): ExperimentContractEntity?

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
    open suspend fun persistLockedExperimentContract(
        row: ExperimentContractEntity,
    ): ExperimentEvidenceWriteResult {
        if (!validContractChronology(row)) {
            throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.INVALID_CONTRACT_CHRONOLOGY,
                row.id,
            )
        }

        val existing = experimentContract(row.id)
        if (existing != null) {
            if (existing == row) return ExperimentEvidenceWriteResult.ALREADY_PRESENT
            throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.CONTRACT_CONFLICT,
                row.id,
            )
        }

        val inserted = insertContractRaw(row)
        if (inserted != -1L) return ExperimentEvidenceWriteResult.INSERTED

        val raced = experimentContract(row.id)
        if (raced == row) return ExperimentEvidenceWriteResult.ALREADY_PRESENT
        throw ExperimentEvidenceStoreException(
            ExperimentEvidenceStoreErrorCode.CONTRACT_CONFLICT,
            row.id,
        )
    }

    @Transaction
    open suspend fun experimentEvidenceReceipt(id: String): ExperimentEvidenceReceiptRecord? {
        val main = receiptMain(id) ?: return null
        return ExperimentEvidenceReceiptRecord(
            receipt = main,
            sourceIds = receiptSourceIds(id),
            confounderAnnotations = receiptConfounders(id),
        )
    }

    @Transaction
    open suspend fun persistExperimentEvidenceReceipt(
        row: ExperimentEvidenceReceiptEntity,
        sourceIds: List<String>,
        confounderAnnotations: List<String>,
    ): ExperimentEvidenceWriteResult {
        if ((row.uncertaintyLower == null) != (row.uncertaintyUpper == null)) {
            throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.INVALID_RECEIPT_SHAPE,
                row.id,
            )
        }

        val contract = experimentContract(row.contractId)
            ?: throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.MISSING_CONTRACT,
                row.contractId,
            )

        if (row.analyzedAtMs < contract.outcomeEndMs || row.persistedAtMs < row.analyzedAtMs) {
            throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.INVALID_RECEIPT_CHRONOLOGY,
                row.id,
            )
        }

        val existingMain = receiptMain(row.id)
        if (existingMain != null) {
            val existing = ExperimentEvidenceReceiptRecord(
                receipt = existingMain,
                sourceIds = receiptSourceIds(row.id),
                confounderAnnotations = receiptConfounders(row.id),
            )
            val incoming = ExperimentEvidenceReceiptRecord(
                receipt = row,
                sourceIds = sourceIds,
                confounderAnnotations = confounderAnnotations,
            )
            if (existing == incoming) return ExperimentEvidenceWriteResult.ALREADY_PRESENT
            throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.RECEIPT_CONFLICT,
                row.id,
            )
        }

        val inserted = insertReceiptRaw(row)
        if (inserted == -1L) {
            val racedMain = receiptMain(row.id)
            val raced = racedMain?.let {
                ExperimentEvidenceReceiptRecord(
                    receipt = it,
                    sourceIds = receiptSourceIds(row.id),
                    confounderAnnotations = receiptConfounders(row.id),
                )
            }
            val incoming = ExperimentEvidenceReceiptRecord(row, sourceIds, confounderAnnotations)
            if (raced == incoming) return ExperimentEvidenceWriteResult.ALREADY_PRESENT
            throw ExperimentEvidenceStoreException(
                ExperimentEvidenceStoreErrorCode.RECEIPT_CONFLICT,
                row.id,
            )
        }

        if (sourceIds.isNotEmpty()) {
            insertReceiptSourcesRaw(
                sourceIds.mapIndexed { ordinal, sourceId ->
                    ExperimentReceiptSourceEntity(row.id, ordinal, sourceId)
                },
            )
        }
        if (confounderAnnotations.isNotEmpty()) {
            insertReceiptConfoundersRaw(
                confounderAnnotations.mapIndexed { ordinal, annotation ->
                    ExperimentReceiptConfounderEntity(row.id, ordinal, annotation)
                },
            )
        }
        return ExperimentEvidenceWriteResult.INSERTED
    }

    private fun validContractChronology(row: ExperimentContractEntity): Boolean =
        row.baselineStartMs < row.baselineEndMs &&
            row.exposureStartMs < row.exposureEndMs &&
            row.outcomeStartMs < row.outcomeEndMs &&
            row.baselineEndMs <= row.exposureStartMs &&
            row.outcomeStartMs >= row.exposureEndMs &&
            row.createdAtMs <= row.predictionLockedAtMs &&
            row.predictionLockedAtMs <= row.persistedAtMs &&
            row.persistedAtMs <= row.exposureStartMs
}
