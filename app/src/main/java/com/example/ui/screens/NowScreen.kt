package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import com.example.domain.model.ClaimStatus
import com.example.domain.model.SeasonClaim
import com.example.ui.ClearanceViewModel
import com.example.ui.theme.*

@Composable
fun NowScreen(
    viewModel: ClearanceViewModel,
    onSelectAction: (Action) -> Unit,
    onOpenScanQr: () -> Unit,
    onOpenCaptureEvidence: (String) -> Unit
) {
    val wallet by viewModel.walletState.collectAsState()
    val solBalance by viewModel.solBalance.collectAsState()
    val skrBalance by viewModel.skrBalance.collectAsState()
    val actions by viewModel.actions.collectAsState()
    val slot by viewModel.networkSlot.collectAsState()
    val stakePosition by viewModel.stakePosition.collectAsState()
    val seasonClaims by viewModel.seasonClaims.collectAsState()

    var selectedFilter by remember { mutableStateOf<ActionStatus?>(null) }
    var showUnstakeDialog by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val filteredActions = remember(actions, selectedFilter) {
        if (selectedFilter == null) actions else actions.filter { it.status == selectedFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header & Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CLEARANCE",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SolanaPurple.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SOLANA DEVNET",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                        }
                    }
                    Text(
                        text = "Know what you approve. Prove what happened.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshNetworkState() },
                    modifier = Modifier.testTag("refresh_network_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Devnet",
                        tint = CyanNeon
                    )
                }
            }
        }

        // Wallet & Devnet Balances Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp)),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(SolanaGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = wallet?.walletName ?: "Seeker Seed Vault",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        val addr = wallet?.address ?: "7xKX...gAsU"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ChipBackground)
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(addr))
                                    Toast.makeText(context, "Wallet address copied", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${addr.take(4)}...${addr.takeLast(4)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = CyanNeon
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy address",
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Balances Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "LIQUID SKR BALANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = skrBalance,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CyanNeon
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SKR",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolanaPurple,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "SOL BALANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = solBalance,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SOL",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolanaGreen,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = SurfaceBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = SolanaGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Slot #$slot • ~2,840 TPS",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = { viewModel.requestFaucetAirdrop() },
                            colors = ButtonDefaults.buttonColors(containerColor = ChipBackground),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("faucet_airdrop_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+1.0 SOL Faucet",
                                fontSize = 11.sp,
                                color = CyanNeon,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 48-Hour Cooldown Clock & SKR Staking Hub Card (Feature 5, 11, 12, 13, 17, 18, 45, 47)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, SolanaPurple.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = SolanaPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SKR 48-HOUR COOLDOWN & STAKING",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SolanaGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "26.2% APY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SolanaGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Three-part SKR balance breakdown (Feature 47)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CodeBackground)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("LIQUID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text("${stakePosition.liquidAmount} SKR", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("STAKED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text("${stakePosition.stakedAmount} SKR", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SolanaGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("COOLING DOWN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text("${stakePosition.coolingDownAmount} SKR", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Delegated Guardian Information
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DELEGATED GUARDIAN (EPOCH #${stakePosition.epoch})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Text(
                                text = stakePosition.guardianName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanNeon
                            )
                        }
                        Text(
                            text = "Inflation: 10% TGE, -25%/yr, floor 2%",
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SurfaceBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Cooldown clock status and action button
                    if (stakePosition.isCooldownActive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    progress = { 0.60f },
                                    modifier = Modifier.size(24.dp),
                                    color = AmberWarning,
                                    strokeWidth = 3.dp,
                                    trackColor = AmberWarning.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${stakePosition.hoursRemainingInCooldown}h remaining in 48-hour cooldown",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberWarning
                                    )
                                    Text(
                                        text = "Unstaked tokens sit in escrow until epoch boundary",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else if (stakePosition.isWithdrawReady) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Cooldown Complete!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolanaGreen
                                )
                                Text(
                                    text = "${stakePosition.coolingDownAmount} SKR ready to withdraw",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Button(
                                onClick = { viewModel.withdrawCooldown() },
                                colors = ButtonDefaults.buttonColors(containerColor = SolanaGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Withdraw Now", color = ObsidianBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All tokens actively earning yield",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Button(
                                onClick = { showUnstakeDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ChipBackground),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Unstake SKR", color = AmberWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Seed Vault Season Claims Calendar Card (Feature 31, 32)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
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
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SEED VAULT SEASON CLAIMS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "90-Day Expiry • Floor >= 0.015 SOL",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    seasonClaims.forEach { claim ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CodeBackground)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = claim.seasonName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (claim.status == ClaimStatus.CLAIMED) "Claimed to wallet" else if (claim.status == ClaimStatus.EXPIRED) "Expired" else "${claim.daysRemaining} days remaining",
                                    fontSize = 10.sp,
                                    color = if (claim.isUrgent) AmberWarning else TextSecondary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "+${claim.allocatedSkr} SKR",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (claim.status == ClaimStatus.CLAIMABLE_NOW) CyanNeon else TextMuted
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (claim.status == ClaimStatus.CLAIMABLE_NOW) {
                                    Button(
                                        onClick = { viewModel.claimSeasonAllocation(claim.seasonId) },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Claim", color = ObsidianBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text(
                                        text = claim.status.displayName,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Triggers
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ACTIONS & INTEGRATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenScanQr,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("scan_task_qr_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Task QR",
                            tint = ObsidianBackground,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scan Task",
                            color = ObsidianBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenScanQr,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("new_depin_claim_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddLocationAlt,
                            contentDescription = "Presets",
                            tint = SolanaPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DePIN Presets",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Action Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All (${actions.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                        selectedLabelColor = CyanNeon
                    )
                )
                FilterChip(
                    selected = selectedFilter == ActionStatus.DRAFT,
                    onClick = { selectedFilter = ActionStatus.DRAFT },
                    label = { Text("Pending Review", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberWarning.copy(alpha = 0.2f),
                        selectedLabelColor = AmberWarning
                    )
                )
                FilterChip(
                    selected = selectedFilter == ActionStatus.SETTLED,
                    onClick = { selectedFilter = ActionStatus.SETTLED },
                    label = { Text("Settled", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SolanaGreen.copy(alpha = 0.2f),
                        selectedLabelColor = SolanaGreen
                    )
                )
            }
        }

        // Action Feed List
        if (filteredActions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No pending clearance tasks",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        } else {
            items(filteredActions, key = { it.id }) { action ->
                ActionFeedCard(
                    action = action,
                    onReview = { onSelectAction(action) },
                    onAttachEvidence = { onOpenCaptureEvidence(action.id) }
                )
            }
        }
    }

    // Unstake Dialog (Feature 16, 17: Partial Unstake Default & Epoch Warning)
    if (showUnstakeDialog) {
        var unstakeAmount by remember { mutableStateOf("10.00") }
        var isFullUnstake by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showUnstakeDialog = false },
            title = {
                Text(
                    text = "Unstake SKR Preview",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "WARNING: Unstaking terminates yield immediately for this epoch. Tokens enter a strict 48-hour cooldown period before withdrawal.",
                        fontSize = 11.sp,
                        color = AmberWarning
                    )
                    OutlinedTextField(
                        value = if (isFullUnstake) stakePosition.stakedAmount else unstakeAmount,
                        onValueChange = { if (!isFullUnstake) unstakeAmount = it },
                        label = { Text("Amount to Unstake (SKR)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isFullUnstake,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isFullUnstake,
                            onCheckedChange = { isFullUnstake = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Full Unstake (All ${stakePosition.stakedAmount} SKR)", fontSize = 12.sp, color = TextPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = if (isFullUnstake) stakePosition.stakedAmount.toDoubleOrNull() ?: 0.0 else unstakeAmount.toDoubleOrNull() ?: 0.0
                        viewModel.stageUnstake(amt, isFullUnstake)
                        showUnstakeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberWarning)
                ) {
                    Text("Confirm Unstake", color = ObsidianBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnstakeDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

@Composable
fun ActionFeedCard(
    action: Action,
    onReview: () -> Unit,
    onAttachEvidence: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .clickable { onReview() }
            .testTag("action_card_${action.id}"),
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ChipBackground)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = action.taskId,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = action.category,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                StatusBadge(status = action.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = action.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = action.description,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = SurfaceBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recipient: ${action.recipient.take(4)}...${action.recipient.takeLast(4)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${action.formattedAmount} ${action.tokenSymbol}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (action.tokenSymbol == "SKR") CyanNeon else SolanaGreen
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (action.status == ActionStatus.DRAFT || action.status == ActionStatus.REVIEWED) {
                        OutlinedButton(
                            onClick = onAttachEvidence,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("attach_evidence_${action.id}"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Add Evidence",
                                tint = SolanaPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Evidence",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }

                        Button(
                            onClick = onReview,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("review_action_${action.id}"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon)
                        ) {
                            Text(
                                text = "Review & Clear",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ObsidianBackground
                            )
                        }
                    } else if (action.status == ActionStatus.SETTLED) {
                        OutlinedButton(
                            onClick = onReview,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("inspect_settled_${action.id}"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SolanaGreen.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = SolanaGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Receipt",
                                fontSize = 11.sp,
                                color = SolanaGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: ActionStatus) {
    val (bgColor, textColor, label) = when (status) {
        ActionStatus.DRAFT -> Triple(AmberWarning.copy(alpha = 0.15f), AmberWarning, "DRAFT")
        ActionStatus.REVIEWED -> Triple(CyanNeon.copy(alpha = 0.15f), CyanNeon, "REVIEWED")
        ActionStatus.AUTHORIZED -> Triple(SolanaPurple.copy(alpha = 0.15f), SolanaPurple, "AUTHORIZED")
        ActionStatus.SETTLED -> Triple(SolanaGreen.copy(alpha = 0.15f), SolanaGreen, "SETTLED")
        ActionStatus.REJECTED -> Triple(RedDanger.copy(alpha = 0.15f), RedDanger, "REJECTED")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}
