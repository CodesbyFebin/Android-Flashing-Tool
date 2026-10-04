package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Action
import com.example.domain.model.ActionStatus
import com.example.ui.screens.ClearScreen
import com.example.ui.screens.DePinTasksScreen
import com.example.ui.screens.EvidenceCaptureDialog
import com.example.ui.screens.NowScreen
import com.example.ui.screens.ProofScreen
import com.example.ui.screens.ScanQrDialog
import com.example.ui.theme.*

enum class ClearanceTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TASKS("Tasks", Icons.Default.Engineering),
    NOW("Now", Icons.Default.FlashOn),
    CLEAR("Clear", Icons.Default.Shield),
    PROOFS("Proofs", Icons.Default.Verified)
}

@Composable
fun MainScreen(viewModel: ClearanceViewModel) {
    var currentTab by remember { mutableStateOf(ClearanceTab.TASKS) }
    val selectedAction by viewModel.selectedAction.collectAsState()
    val notification by viewModel.uiNotification.collectAsState()
    val actions by viewModel.actions.collectAsState()

    var showScanQrDialog by remember { mutableStateOf(false) }
    var captureEvidenceActionId by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notification) {
        notification?.let {
            snackbarHostState.showSnackbar(
                message = it.message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearNotification()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceElevated,
                    contentColor = TextPrimary,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextSecondary,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                ClearanceTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) CyanNeon else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyanNeon else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CyanNeon.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBackground)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ClearanceTab.TASKS -> {
                    DePinTasksScreen(
                        viewModel = viewModel,
                        onOpenEvidenceDialog = { taskId ->
                            captureEvidenceActionId = taskId
                        },
                        onTriggerPaymentReview = { action ->
                            currentTab = ClearanceTab.CLEAR
                        }
                    )
                }

                ClearanceTab.NOW -> {
                    NowScreen(
                        viewModel = viewModel,
                        onSelectAction = { action ->
                            viewModel.selectActionForReview(action)
                            currentTab = ClearanceTab.CLEAR
                        },
                        onOpenScanQr = { showScanQrDialog = true },
                        onOpenCaptureEvidence = { actionId ->
                            captureEvidenceActionId = actionId
                        }
                    )
                }

                ClearanceTab.CLEAR -> {
                    val activeAction = selectedAction ?: actions.firstOrNull { it.status != ActionStatus.SETTLED } ?: actions.firstOrNull()
                    if (activeAction != null) {
                        ClearScreen(
                            viewModel = viewModel,
                            action = activeAction,
                            onBack = { currentTab = ClearanceTab.TASKS },
                            onAuthorized = { receipt ->
                                currentTab = ClearanceTab.PROOFS
                            },
                            onOpenCaptureEvidence = { actionId ->
                                captureEvidenceActionId = actionId
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(
                                text = "Select a DePIN task or payment on the Tasks or Now screen to inspect its Clearance Card.",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                ClearanceTab.PROOFS -> {
                    ProofScreen(viewModel = viewModel)
                }
            }

            // Evidence Capture Dialog (Photos, Videos, Documents)
            if (captureEvidenceActionId != null) {
                EvidenceCaptureDialog(
                    actionId = captureEvidenceActionId!!,
                    onDismiss = { captureEvidenceActionId = null },
                    onSaveEvidence = { id, photoPath, photoBytes, videoPath, videoBytes, docPath, docBytes, docName, note ->
                        viewModel.attachMultiEvidenceToAction(
                            actionId = id,
                            photoPath = photoPath,
                            photoBytes = photoBytes,
                            videoPath = videoPath,
                            videoBytes = videoBytes,
                            documentPath = docPath,
                            documentBytes = docBytes,
                            documentName = docName,
                            note = note
                        )
                    }
                )
            }

            // Scan QR Dialog
            if (showScanQrDialog) {
                ScanQrDialog(
                    viewModel = viewModel,
                    onDismiss = { showScanQrDialog = false }
                )
            }
        }
    }
}
