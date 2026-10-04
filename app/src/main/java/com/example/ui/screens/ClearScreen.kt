package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Action
import com.example.domain.model.ActionStatus
import com.example.domain.model.Evidence
import com.example.domain.model.Receipt
import com.example.services.TransactionExplainer
import com.example.services.TransactionExplanation
import com.example.ui.ClearanceViewModel
import com.example.ui.theme.*

@Composable
fun ClearScreen(
    viewModel: ClearanceViewModel,
    action: Action,
    onBack: () -> Unit,
    onAuthorized: (Receipt) -> Unit,
    onOpenCaptureEvidence: (String) -> Unit
) {
    BackHandler { onBack() }

    val evidence by viewModel.currentEvidence.collectAsState()
    val review by viewModel.currentReview.collectAsState()
    val securityAudit by viewModel.securityAudit.collectAsState()
    val isAuditing by viewModel.isAuditing.collectAsState()
    val isAuthorizing by viewModel.isAuthorizing.collectAsState()
    val isTamperSimEnabled by viewModel.isTamperSimulationEnabled.collectAsState()
    val explanation by viewModel.activeExplanation.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showRejectConfirmDialog by remember { mutableStateOf(false) }
    var showRawDecoderDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation & Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("clear_screen_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CLEARANCE CARD",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Decoded Consequence & Pre-Signing Authorization",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                StatusBadge(status = action.status)
            }
        }

        // Primary Consequence & Recipient Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, CyanNeon.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = action.taskId,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SolanaPurple.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = action.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolanaPurple
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = action.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = action.description,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = SurfaceBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Transfer Amount
                    Text(
                        text = "PAYMENT TRANSFER CONSEQUENCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = action.formattedAmount,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = CyanNeon
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = action.tokenSymbol,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolanaPurple,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ChipBackground)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "RAW: ${action.rawAmount}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Recipient Address
                    Text(
                        text = "VERIFIED RECIPIENT PUBLIC KEY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CodeBackground)
                            .clickable {
                                clipboardManager.setText(AnnotatedString(action.recipient))
                                Toast.makeText(context, "Recipient address copied", Toast.LENGTH_SHORT).show()
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = action.recipient,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = SolanaGreen,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Dedicated Plain-Language Solana Transaction Explainer Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SolanaPurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SolanaPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DECODED TRANSACTION EXPLANATION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        IconButton(
                            onClick = { showRawDecoderDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Raw Decoder Tool",
                                tint = CyanNeon,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val exp = explanation ?: TransactionExplainer.explainAction(
                        action,
                        evidence,
                        "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU"
                    )

                    // Headline explanation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SolanaPurple.copy(alpha = 0.15f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = exp.headline,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Balance impact
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Balance Change Impact:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Text(
                                text = exp.balanceImpactSummary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Permissions Analysis
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = SolanaGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Delegated Permissions & Authority:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Text(
                                text = exp.permissionsSummary,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Network Fee
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Max Network Gas Fee:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Text(
                                text = exp.maxNetworkFeeSol,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Attached DePIN Evidence Bundle Card (Photo, Video, Document)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Attachment,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ATTACHED EVIDENCE BUNDLE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Button(
                            onClick = { onOpenCaptureEvidence(action.id) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChipBackground),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = if (evidence != null) "Update Proof" else "Attach Proof",
                                fontSize = 11.sp,
                                color = CyanNeon,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (evidence != null) {
                        val ev = evidence!!
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Note
                            Text(
                                text = "\"${ev.note}\"",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )

                            // 1. Photo Hash
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CodeBackground)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Photo Fingerprint:", fontSize = 10.sp, color = TextMuted)
                                }
                                Text(
                                    text = "${ev.photoSha256.take(12)}...${ev.photoSha256.takeLast(6)}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = CyanNeon
                                )
                            }

                            // 2. Video Hash if present
                            if (!ev.videoSha256.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CodeBackground)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = SolanaPurple, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Video Sweep (.mp4):", fontSize = 10.sp, color = TextMuted)
                                    }
                                    Text(
                                        text = "${ev.videoSha256!!.take(12)}...${ev.videoSha256!!.takeLast(6)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = SolanaPurple
                                    )
                                }
                            }

                            // 3. Document / Log Hash if present
                            if (!ev.documentSha256.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CodeBackground)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = SolanaGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(ev.documentName ?: "Diagnostic Report:", fontSize = 10.sp, color = TextMuted)
                                    }
                                    Text(
                                        text = "${ev.documentSha256!!.take(12)}...${ev.documentSha256!!.takeLast(6)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = SolanaGreen
                                    )
                                }
                            }

                            // Combined Manifest SHA-256
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CodeBackground)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "CANONICAL EVIDENCE DIGEST (SHA-256):",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Text(
                                    text = ev.manifestSha256,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = SolanaGreen
                                )
                            }

                            // Keystore attestation
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SolanaGreen.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = SolanaGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Android Keystore ECDSA-secp256r1 Hardware Attested",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolanaGreen
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No physical evidence attached to this work claim. Tap 'Attach Proof' to bind photos, videos, or sensor logs.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Gemini AI Thinking Forensics Panel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, if (securityAudit?.riskScore ?: 0 >= 50) RedDanger.copy(alpha = 0.5f) else CyanNeon.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "AI FORENSIC REASONING",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "gemini-3.1-pro-preview • High Thinking",
                                    fontSize = 10.sp,
                                    color = CyanNeon
                                )
                            }
                        }

                        if (securityAudit != null) {
                            val score = securityAudit!!.riskScore
                            val scoreColor = if (score < 30) SolanaGreen else if (score < 60) AmberWarning else RedDanger
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(scoreColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "RISK: $score / 100",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = scoreColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isAuditing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = CyanNeon,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Evaluating transaction consequences with High Thinking...",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    } else if (securityAudit != null) {
                        val audit = securityAudit!!
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = audit.summary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            // Passed checks
                            audit.checksPassed.forEach { check ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SolanaGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = check, fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            // Warnings
                            audit.warnings.forEach { warning ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = AmberWarning,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = warning, fontSize = 11.sp, color = AmberWarning)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Review Binding & Negative Control (Tamper Simulation)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.dp,
                        if (isTamperSimEnabled) RedDanger else SurfaceBorder,
                        RoundedCornerShape(14.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TAMPER RESISTANCE & REVIEW BINDING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTamperSimEnabled) RedDanger else TextMuted
                            )
                            Text(
                                text = if (isTamperSimEnabled)
                                    "Simulating malicious payload injection (MESSAGE_CHANGED attack)"
                                else
                                    "Message hash locked in memory before wallet signing",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = isTamperSimEnabled,
                            onCheckedChange = { viewModel.toggleTamperSimulation() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RedDanger,
                                checkedTrackColor = RedDanger.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("tamper_simulation_switch")
                        )
                    }

                    if (review != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "LOCKED MESSAGE HASH: ${review!!.messageHash}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = if (isTamperSimEnabled) RedDanger else TextMuted
                        )
                    }
                }
            }
        }

        // Action Buttons: Reject & Authorize
        item {
            if (action.status != ActionStatus.SETTLED) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            viewModel.authorizeCurrentAction { receipt ->
                                onAuthorized(receipt)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("authorize_payment_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTamperSimEnabled) RedDanger else CyanNeon
                        ),
                        enabled = !isAuthorizing
                    ) {
                        if (isAuthorizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ObsidianBackground,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Signing with Seeker Seed Vault...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ObsidianBackground
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ObsidianBackground,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamperSimEnabled) "Test Authorize (Should Trigger MESSAGE_CHANGED)" else "Authorize & Sign with Seed Vault",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ObsidianBackground
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showRejectConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reject_payment_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedDanger.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = RedDanger,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reject Request",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RedDanger
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SolanaGreen.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SolanaGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Payment Settled & Receipt Created",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolanaGreen
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for rejection
    if (showRejectConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRejectConfirmDialog = false },
            title = { Text("Reject Payment Request?") },
            text = { Text("This will mark the task as rejected and abort any wallet signing. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRejectConfirmDialog = false
                        viewModel.rejectCurrentAction { onBack() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
                ) {
                    Text("Confirm Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    // Raw Transaction Decoder Tool Dialog
    if (showRawDecoderDialog) {
        var rawInput by remember { mutableStateOf("{\"recipient\":\"9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM\",\"rawAmount\":\"5000000000\",\"decimals\":9,\"symbol\":\"SKR\"}") }
        var decodedExplanation by remember { mutableStateOf<TransactionExplanation?>(null) }

        AlertDialog(
            onDismissRequest = { showRawDecoderDialog = false },
            title = { Text("Raw Solana Transaction Decoder", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Paste raw instruction JSON or Solana Pay string to analyze:", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = rawInput,
                        onValueChange = { rawInput = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )
                    Button(
                        onClick = {
                            decodedExplanation = TransactionExplainer.decodeArbitraryPayload(rawInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SolanaPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Decode & Explain")
                    }

                    if (decodedExplanation != null) {
                        val d = decodedExplanation!!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CodeBackground)
                                .padding(8.dp)
                        ) {
                            Text(d.headline, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CyanNeon)
                            Text("Impact: ${d.balanceImpactSummary}", fontSize = 10.sp, color = TextPrimary)
                            Text("Permissions: ${d.permissionsSummary}", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRawDecoderDialog = false }) {
                    Text("Done", color = CyanNeon)
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}
