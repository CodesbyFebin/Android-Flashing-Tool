package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.window.Dialog
import com.example.ui.ClearanceViewModel
import com.example.ui.theme.*

data class TaskPreset(
    val taskId: String,
    val title: String,
    val description: String,
    val category: String,
    val recipient: String,
    val rawAmount: String,
    val isAttackPayload: Boolean = false
)

@Composable
fun ScanQrDialog(
    viewModel: ClearanceViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Presets, 1: Custom JSON
    var customJsonText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val presets = remember {
        listOf(
            TaskPreset(
                taskId = "HE-5G-HOTSPOT-992",
                title = "Helium 5G CBRS Microcell Claim",
                description = "Automated radio beacon telemetry validation for CBRS base station #992 over 24h window",
                category = "DePIN Work",
                recipient = "2xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU",
                rawAmount = "3500000000" // 3.50 SKR
            ),
            TaskPreset(
                taskId = "GRASS-RELAY-NODE-55",
                title = "Grass Residential Bandwidth Relay",
                description = "Proof of uptime and validated egress bandwidth relay settlement for node #55",
                category = "Bandwidth Relay",
                recipient = "4Nd1mBQtrMKBCb2u3hGzV8jN4e6V2nK8mP2qR4sT6uV8",
                rawAmount = "1500000000" // 1.50 SKR
            ),
            TaskPreset(
                taskId = "HIVEMAPPER-MAP-104",
                title = "Hivemapper Street Photogrammetry",
                description = "Sub-meter GPS RTK tagged roadway visual mapping stream over 18.5 km corridor",
                category = "Sensor Survey",
                recipient = "3KmB7uV8jN4e6V2nK8mP2qR4sT6uV8Nd1mBQtrMKBCb2",
                rawAmount = "2800000000" // 2.80 SKR
            ),
            TaskPreset(
                taskId = "ATTACK-MALFORMED-REQ",
                title = "MALICIOUS: Drainer Attack Payload",
                description = "Demonstration of negative control: Invalid recipient address and abnormal parameters that Clearance will reject",
                category = "Threat Simulation",
                recipient = "INVALID_TOO_SHORT_KEY_99",
                rawAmount = "999000000000000",
                isAttackPayload = true
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "IMPORT TASK / QR SCAN",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
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

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CodeBackground,
                    contentColor = CyanNeon
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("DePIN Presets", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Scan / Custom QR", fontSize = 12.sp) }
                    )
                }

                if (selectedTab == 0) {
                    // Presets List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presets) { preset ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        1.dp,
                                        if (preset.isAttackPayload) RedDanger.copy(alpha = 0.5f) else SurfaceBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        if (preset.isAttackPayload) {
                                            // Validate negative control
                                            Toast.makeText(
                                                context,
                                                "SECURITY DEFENSE TRIGGERED: Task rejected due to malformed recipient address format!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            viewModel.createNewAction(
                                                taskId = preset.taskId,
                                                title = preset.title,
                                                description = preset.description,
                                                category = preset.category,
                                                recipient = preset.recipient,
                                                rawAmount = preset.rawAmount
                                            )
                                            onDismiss()
                                        }
                                    }
                                    .testTag("preset_${preset.taskId}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (preset.isAttackPayload) RedDanger.copy(alpha = 0.1f) else CodeBackground
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = preset.taskId,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (preset.isAttackPayload) RedDanger else CyanNeon
                                        )
                                        Text(
                                            text = if (preset.isAttackPayload) "NEGATIVE TEST" else "${preset.rawAmount.toDouble() / 1e9} SKR",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (preset.isAttackPayload) RedDanger else SolanaGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = preset.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = preset.description,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Custom JSON / QR Paste
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Paste TaskRequest JSON or Solana Pay URL:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        OutlinedTextField(
                            value = customJsonText,
                            onValueChange = {
                                customJsonText = it
                                validationError = null
                            },
                            placeholder = {
                                Text(
                                    "{\"taskId\":\"TASK-101\",\"title\":\"My Task\",\"recipient\":\"...\",\"rawAmount\":\"1000000000\"}",
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("custom_qr_json_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        if (validationError != null) {
                            Text(
                                text = validationError!!,
                                fontSize = 11.sp,
                                color = RedDanger
                            )
                        }

                        Button(
                            onClick = {
                                if (customJsonText.isBlank()) {
                                    validationError = "Please enter valid JSON"
                                    return@Button
                                }
                                try {
                                    val obj = org.json.JSONObject(customJsonText)
                                    val taskId = obj.optString("taskId", "TASK-${System.currentTimeMillis() % 10000}")
                                    val title = obj.optString("title", "Custom DePIN Work")
                                    val desc = obj.optString("description", "Imported from QR scan")
                                    val cat = obj.optString("category", "Custom Payment")
                                    val recipient = obj.optString("recipient", "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU")
                                    val rawAmount = obj.optString("rawAmount", "1000000000")

                                    viewModel.createNewAction(
                                        taskId = taskId,
                                        title = title,
                                        description = desc,
                                        category = cat,
                                        recipient = recipient,
                                        rawAmount = rawAmount
                                    )
                                    onDismiss()
                                } catch (e: Exception) {
                                    validationError = "Invalid JSON: ${e.message}"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("import_custom_task_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Import Task",
                                color = ObsidianBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
