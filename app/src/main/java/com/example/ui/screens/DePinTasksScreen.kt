package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Action
import com.example.domain.model.DePinTask
import com.example.domain.model.DePinTaskStatus
import com.example.ui.ClearanceViewModel
import com.example.ui.theme.*

@Composable
fun DePinTasksScreen(
    viewModel: ClearanceViewModel,
    onOpenEvidenceDialog: (taskId: String) -> Unit,
    onTriggerPaymentReview: (Action) -> Unit
) {
    val tasks by viewModel.dePinTasks.collectAsState()
    var selectedFilter by remember { mutableStateOf<DePinTaskStatus?>(null) }
    var taskToInspect by remember { mutableStateOf<DePinTask?>(null) }
    val context = LocalContext.current

    val filteredTasks = remember(tasks, selectedFilter) {
        if (selectedFilter == null) tasks else tasks.filter { it.status == selectedFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DePIN TASK HUB",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Complete physical work, upload proof & trigger Solana clearance",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanNeon.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${tasks.size} TASKS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    }
                }
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ACTIVE NETWORKS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Helium • Grass • Hivemapper", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("EARN POTENTIAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("12.45 SKR", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CyanNeon)
                    }
                }
            }
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All (${tasks.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                        selectedLabelColor = CyanNeon
                    )
                )
                FilterChip(
                    selected = selectedFilter == DePinTaskStatus.ASSIGNED || selectedFilter == DePinTaskStatus.IN_PROGRESS,
                    onClick = { selectedFilter = DePinTaskStatus.ASSIGNED },
                    label = { Text("Assigned", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberWarning.copy(alpha = 0.2f),
                        selectedLabelColor = AmberWarning
                    )
                )
                FilterChip(
                    selected = selectedFilter == DePinTaskStatus.PROOF_SUBMITTED,
                    onClick = { selectedFilter = DePinTaskStatus.PROOF_SUBMITTED },
                    label = { Text("Proof Ready", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SolanaPurple.copy(alpha = 0.2f),
                        selectedLabelColor = SolanaPurple
                    )
                )
                FilterChip(
                    selected = selectedFilter == DePinTaskStatus.COMPLETED_SETTLED,
                    onClick = { selectedFilter = DePinTaskStatus.COMPLETED_SETTLED },
                    label = { Text("Settled", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SolanaGreen.copy(alpha = 0.2f),
                        selectedLabelColor = SolanaGreen
                    )
                )
            }
        }

        // Task Items
        if (filteredTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks found in this category.", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                DePinTaskCard(
                    task = task,
                    onAttachProof = { onOpenEvidenceDialog(task.id) },
                    onMarkComplete = { viewModel.markDePinTaskComplete(task.id) },
                    onTriggerPayment = {
                        viewModel.triggerDePinPaymentWorkflow(task) { action ->
                            onTriggerPaymentReview(action)
                        }
                    },
                    onInspect = { taskToInspect = task }
                )
            }
        }
    }

    // Inspect Details Dialog
    if (taskToInspect != null) {
        val t = taskToInspect!!
        AlertDialog(
            onDismissRequest = { taskToInspect = null },
            title = {
                Text(
                    text = t.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = t.description, fontSize = 13.sp, color = TextSecondary)
                    HorizontalDivider(color = SurfaceBorder)
                    Text(
                        text = "REQUIREMENTS CHECKLIST:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    t.requirementList.forEach { req ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SolanaGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = req, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Reward: ${t.rewardAmount} ${t.rewardToken} • Node: ${t.deviceOrNodeId}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = CyanNeon
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { taskToInspect = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon)
                ) {
                    Text("Close", color = ObsidianBackground)
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

@Composable
fun DePinTaskCard(
    task: DePinTask,
    onAttachProof: () -> Unit,
    onMarkComplete: () -> Unit,
    onTriggerPayment: () -> Unit,
    onInspect: () -> Unit
) {
    val networkColor = when (task.network) {
        "Helium Mobile" -> CyanNeon
        "Grass Network" -> SolanaGreen
        "Hivemapper" -> Color(0xFFFF9900)
        "Render Network" -> SolanaPurple
        else -> CyanNeon
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .clickable { onInspect() }
            .testTag("depin_task_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Network & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(networkColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.network.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = networkColor
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.id,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                DePinStatusChip(status = task.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Description
            Text(
                text = task.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.description,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Requirements Checklist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                task.requirementList.take(2).forEach { req ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ChipBackground)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "✓ $req",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = SurfaceBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REWARD",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        text = "${task.rewardAmount} ${task.rewardToken}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = networkColor
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (task.status) {
                        DePinTaskStatus.ASSIGNED, DePinTaskStatus.IN_PROGRESS -> {
                            OutlinedButton(
                                onClick = onAttachProof,
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Attachment,
                                    contentDescription = null,
                                    tint = SolanaPurple,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload Proof", fontSize = 11.sp, color = TextPrimary)
                            }

                            Button(
                                onClick = onMarkComplete,
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SolanaPurple)
                            ) {
                                Text("Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        DePinTaskStatus.PROOF_SUBMITTED, DePinTaskStatus.PAYMENT_PENDING -> {
                            Button(
                                onClick = onTriggerPayment,
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = ObsidianBackground,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Trigger Clearance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianBackground
                                )
                            }
                        }

                        DePinTaskStatus.COMPLETED_SETTLED -> {
                            OutlinedButton(
                                onClick = onInspect,
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SolanaGreen.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = SolanaGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Settled", fontSize = 11.sp, color = SolanaGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DePinStatusChip(status: DePinTaskStatus) {
    val (bgColor, textColor, label) = when (status) {
        DePinTaskStatus.ASSIGNED -> Triple(AmberWarning.copy(alpha = 0.15f), AmberWarning, "ASSIGNED")
        DePinTaskStatus.IN_PROGRESS -> Triple(CyanNeon.copy(alpha = 0.15f), CyanNeon, "IN PROGRESS")
        DePinTaskStatus.PROOF_SUBMITTED -> Triple(SolanaPurple.copy(alpha = 0.15f), SolanaPurple, "PROOF READY")
        DePinTaskStatus.PAYMENT_PENDING -> Triple(CyanNeon.copy(alpha = 0.2f), CyanNeon, "CLEARANCE READY")
        DePinTaskStatus.COMPLETED_SETTLED -> Triple(SolanaGreen.copy(alpha = 0.15f), SolanaGreen, "SETTLED")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}
