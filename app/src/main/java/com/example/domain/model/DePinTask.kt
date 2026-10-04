package com.example.domain.model

enum class DePinTaskStatus(val displayName: String) {
    ASSIGNED("Assigned"),
    IN_PROGRESS("In Progress"),
    PROOF_SUBMITTED("Proof Ready"),
    PAYMENT_PENDING("Review Pending"),
    COMPLETED_SETTLED("Settled & Paid")
}

data class DePinTask(
    val id: String,
    val title: String,
    val network: String, // "Helium 5G", "Grass Network", "Hivemapper", "Render GPU", "SolarGrid"
    val category: String,
    val description: String,
    val rewardAmount: String,
    val rewardToken: String = "SKR",
    val deviceOrNodeId: String,
    val status: DePinTaskStatus = DePinTaskStatus.ASSIGNED,
    val assignedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val actionId: String? = null,
    val receiptId: String? = null,
    val requirements: String = "Site inspection;Hardware logs;Sensor telemetry",
    val attachedFilesCount: Int = 0
) {
    val requirementList: List<String>
        get() = requirements.split(";").filter { it.isNotBlank() }
}
