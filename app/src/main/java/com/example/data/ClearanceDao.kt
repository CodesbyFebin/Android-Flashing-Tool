package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClearanceDao {

    // Actions
    @Query("SELECT * FROM actions ORDER BY createdAt DESC")
    fun getAllActions(): Flow<List<ActionEntity>>

    @Query("SELECT * FROM actions WHERE id = :id LIMIT 1")
    suspend fun getActionById(id: String): ActionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: ActionEntity)

    @Update
    suspend fun updateAction(action: ActionEntity)

    @Query("UPDATE actions SET status = :status WHERE id = :id")
    suspend fun updateActionStatus(id: String, status: String)

    @Query("DELETE FROM actions WHERE id = :id")
    suspend fun deleteAction(id: String)

    // Evidence
    @Query("SELECT * FROM evidence WHERE actionId = :actionId LIMIT 1")
    suspend fun getEvidenceForAction(actionId: String): EvidenceEntity?

    @Query("SELECT * FROM evidence WHERE actionId = :actionId LIMIT 1")
    fun observeEvidenceForAction(actionId: String): Flow<EvidenceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: EvidenceEntity)

    // Receipts
    @Query("SELECT * FROM receipts ORDER BY observedAt DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE actionId = :actionId LIMIT 1")
    suspend fun getReceiptByActionId(actionId: String): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE id = :receiptId LIMIT 1")
    suspend fun getReceiptById(receiptId: String): ReceiptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Query("UPDATE receipts SET settlementStatus = :status WHERE id = :receiptId")
    suspend fun updateReceiptStatus(receiptId: String, status: String)

    // DePIN Tasks
    @Query("SELECT * FROM depin_tasks ORDER BY assignedAt DESC")
    fun getAllDePinTasks(): Flow<List<DePinTaskEntity>>

    @Query("SELECT * FROM depin_tasks WHERE id = :id LIMIT 1")
    suspend fun getDePinTaskById(id: String): DePinTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDePinTask(task: DePinTaskEntity)

    @Update
    suspend fun updateDePinTask(task: DePinTaskEntity)

    @Query("UPDATE depin_tasks SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateDePinTaskCompletion(id: String, status: String, completedAt: Long?)

    @Query("UPDATE depin_tasks SET actionId = :actionId, status = :status WHERE id = :id")
    suspend fun linkDePinTaskAction(id: String, actionId: String, status: String)

    @Query("UPDATE depin_tasks SET receiptId = :receiptId, status = :status WHERE actionId = :actionId")
    suspend fun linkDePinTaskReceiptByActionId(actionId: String, receiptId: String, status: String)
}
