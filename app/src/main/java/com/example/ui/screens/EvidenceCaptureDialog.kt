package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.security.SHA256Service
import com.example.ui.theme.*
import java.io.ByteArrayOutputStream

@Composable
fun EvidenceCaptureDialog(
    actionId: String,
    onDismiss: () -> Unit,
    onSaveEvidence: (
        actionId: String,
        photoPath: String,
        photoBytes: ByteArray,
        videoPath: String?,
        videoBytes: ByteArray?,
        documentPath: String?,
        documentBytes: ByteArray?,
        documentName: String?,
        note: String
    ) -> Unit
) {
    var note by remember { mutableStateOf("Site inspection complete; antenna mounted and verified via RF spectrum scan.") }
    var attachVideo by remember { mutableStateOf(true) }
    var attachDocument by remember { mutableStateOf(true) }
    var selectedSensorMode by remember { mutableStateOf("RF Spectrum + 5G Microcell") }
    var selectedDocType by remember { mutableStateOf("rf_spectrum_telemetry.csv") }

    // Generate simulated sensor/photo capture bytes
    val photoBytes = remember(selectedSensorMode, note) {
        val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = AndroidColor.DKGRAY
        }
        canvas.drawRect(0f, 0f, 400f, 300f, paint)
        paint.color = AndroidColor.CYAN
        paint.textSize = 20f
        canvas.drawText("Clearance Telemetry: $selectedSensorMode", 20f, 150f, paint)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        stream.toByteArray()
    }

    val videoBytes = remember(attachVideo) {
        if (attachVideo) {
            "CLEARANCE_SIMULATED_VIDEO_STREAM_ANTENNA_SWEEP_FRAME_DATA_1080P".toByteArray(Charsets.UTF_8)
        } else null
    }

    val documentBytes = remember(attachDocument, selectedDocType) {
        if (attachDocument) {
            "timestamp,rssi,snr,frequency,uptime\n1700000000,-72,18.4,915.2,99.98%".toByteArray(Charsets.UTF_8)
        } else null
    }

    val photoSha256 = remember(photoBytes) { SHA256Service.hash(photoBytes) }
    val videoSha256 = remember(videoBytes) { videoBytes?.let { SHA256Service.hash(it) } }
    val docSha256 = remember(documentBytes) { documentBytes?.let { SHA256Service.hash(it) } }

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
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
                            text = "ATTACH EVIDENCE BUNDLE",
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

                Text(
                    text = "Upload photos, videos, and diagnostic logs to bind tamper-proof cryptographic proofs to this settlement.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                // 1. Photo Artifact
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CodeBackground)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Photo Evidence: $selectedSensorMode",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SolanaGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ATTACHED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SolanaGreen)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SHA-256: ${photoSha256.take(24)}...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = CyanNeon
                    )
                }

                // 2. Video Artifact Toggle
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CodeBackground)
                        .clickable { attachVideo = !attachVideo }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = SolanaPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Panoramic Site Sweep Video (.mp4)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Checkbox(
                            checked = attachVideo,
                            onCheckedChange = { attachVideo = it },
                            colors = CheckboxDefaults.colors(checkedColor = SolanaPurple)
                        )
                    }
                    if (attachVideo && videoSha256 != null) {
                        Text(
                            text = "SHA-256: ${videoSha256.take(24)}...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = SolanaPurple
                        )
                    }
                }

                // 3. Document / Diagnostic Log Toggle
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CodeBackground)
                        .clickable { attachDocument = !attachDocument }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = SolanaGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Diagnostic Report ($selectedDocType)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Checkbox(
                            checked = attachDocument,
                            onCheckedChange = { attachDocument = it },
                            colors = CheckboxDefaults.colors(checkedColor = SolanaGreen)
                        )
                    }
                    if (attachDocument && docSha256 != null) {
                        Text(
                            text = "SHA-256: ${docSha256.take(24)}...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = SolanaGreen
                        )
                    }
                }

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Inspector Verification Note", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("evidence_note_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 2
                )

                // Hardware Attestation
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SolanaPurple.copy(alpha = 0.15f))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = SolanaPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Seals combined multi-file digest with Android Keystore TEE key",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                // Submit Button
                Button(
                    onClick = {
                        val videoPath = if (attachVideo) "file://evidence/site_panoramic_sweep.mp4" else null
                        val docPath = if (attachDocument) "file://evidence/$selectedDocType" else null
                        onSaveEvidence(
                            actionId,
                            "file://evidence/sensor_capture_${System.currentTimeMillis()}.jpg",
                            photoBytes,
                            videoPath,
                            videoBytes,
                            docPath,
                            documentBytes,
                            if (attachDocument) selectedDocType else null,
                            note
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_evidence_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = ObsidianBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Seal Evidence & Bind Manifest",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ObsidianBackground
                    )
                }
            }
        }
    }
}
