package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Receipt
import com.example.domain.model.SettlementStatus
import com.example.ui.ClearanceViewModel
import com.example.ui.theme.*

@Composable
fun ProofScreen(
    viewModel: ClearanceViewModel
) {
    val receipts by viewModel.receipts.collectAsState()
    val selectedReceipt by viewModel.selectedReceipt.collectAsState()
    val verificationResult by viewModel.independentVerificationResult.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "VERIFIABLE RECEIPTS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Cryptographic proof connecting DePIN work to Solana settlement",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SolanaGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${receipts.size} SETTLED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolanaGreen
                        )
                    }
                }
            }
        }

        // Selected Receipt Full Inspector
        if (selectedReceipt != null) {
            val r = selectedReceipt!!
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, SolanaGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
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
                                text = "INSPECTING RECEIPT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolanaGreen,
                                letterSpacing = 1.sp
                            )
                            IconButton(
                                onClick = { viewModel.selectReceiptForInspection(r) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = r.title.ifEmpty { r.taskId },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Task ID: ${r.taskId}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = CyanNeon
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = SurfaceBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Transaction Signature
                        Text(
                            text = "SOLANA TRANSACTION SIGNATURE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CodeBackground)
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(r.transactionSignature))
                                    Toast.makeText(context, "Transaction signature copied", Toast.LENGTH_SHORT).show()
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = r.transactionSignature,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = CyanNeon,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Evidence Digest
                        Text(
                            text = "CANONICAL EVIDENCE DIGEST (SHA-256)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CodeBackground)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = r.evidenceDigest,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = SolanaPurple
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Telemetry Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "BLOCK SLOT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Text(
                                    text = "#${r.blockSlot}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                            Column {
                                Text(
                                    text = "NETWORK FEE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Text(
                                    text = "${r.feeLamports} Lamports",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "STATUS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Text(
                                    text = r.settlementStatus.name,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolanaGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Independent Cryptographic Verifier Button
                        Button(
                            onClick = { viewModel.runIndependentReceiptVerification(r) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("run_independent_verification_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SolanaGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = ObsidianBackground,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verify Cryptographic Integrity",
                                color = ObsidianBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Verification Result Banner
                        if (verificationResult != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val isVerified = verificationResult!!
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isVerified) SolanaGreen.copy(alpha = 0.15f) else RedDanger.copy(alpha = 0.15f))
                                    .padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isVerified) SolanaGreen else RedDanger,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isVerified) "100% CRYPTOGRAPHIC MATCH" else "VERIFICATION FAILED",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isVerified) SolanaGreen else RedDanger
                                    )
                                    Text(
                                        text = if (isVerified)
                                            "Canonical JSON hash equals on-chain evidence digest. No tampering detected."
                                        else
                                            "Digest discrepancy detected. Data integrity compromised.",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Export / Share Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "CLEARANCE VERIFIED RECEIPT\n" +
                                            "Task: ${r.taskId}\n" +
                                            "Amount: ${r.amountFormatted} ${r.tokenSymbol}\n" +
                                            "Signature: ${r.transactionSignature}\n" +
                                            "Evidence Digest: ${r.evidenceDigest}\n" +
                                            "Status: ${r.settlementStatus}\n" +
                                            "Solscan: ${r.explorerUrl}"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export", fontSize = 11.sp, color = TextPrimary)
                            }

                            Button(
                                onClick = {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(r.explorerUrl))
                                    try {
                                        context.startActivity(browserIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "URL: ${r.explorerUrl}", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ChipBackground),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Explorer",
                                    tint = CyanNeon,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Solscan", fontSize = 11.sp, color = CyanNeon)
                            }
                        }
                    }
                }
            }
        }

        // Receipts List
        if (receipts.isEmpty()) {
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
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No settlement receipts yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Authorize a task on the Now screen to generate permanent cryptographic receipts.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(receipts, key = { it.id }) { receipt ->
                ReceiptItemCard(
                    receipt = receipt,
                    isSelected = selectedReceipt?.id == receipt.id,
                    onClick = { viewModel.selectReceiptForInspection(receipt) }
                )
            }
        }
    }
}

@Composable
fun ReceiptItemCard(
    receipt: Receipt,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isSelected) SolanaGreen else SurfaceBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .testTag("receipt_card_${receipt.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = receipt.taskId.ifEmpty { receipt.actionId.take(12) },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SolanaGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = receipt.settlementStatus.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolanaGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = receipt.title.ifEmpty { "Settled Solana Transaction" },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sig: ${receipt.transactionSignature.take(8)}...${receipt.transactionSignature.takeLast(6)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "${receipt.amountFormatted} ${receipt.tokenSymbol}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyanNeon
                )
            }
        }
    }
}
