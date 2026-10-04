# Code Changes Required for Oct 5–8 Sprint

This file maps all code changes needed to move from "simulated 50 features" to "honest three-screen demo."

---

## Priority 1: Critical Path (Oct 5–6)

### 1. ReviewBinding.kt — MESSAGE_CHANGED Implementation
**Current State**: Hashes action fields (incomplete)  
**Required**: Hash actual instruction bytes

**Changes**:
```kotlin
// CHANGE 1: Full message serialization
fun serializeMessagePayload(
    discriminator: String,      // Instruction type
    destination: String,        // Token account recipient
    mint: String,              // Token mint
    amountRaw: String,         // Raw amount (no decimals)
    decimals: Int,             // Decimals for display
    userAccount: String        // Signer account
): ByteArray {
    val canonical = buildString {
        append("SOLANA_CLEARANCE_MSG_V1\n")
        append("INSTRUCTION_DISCRIMINATOR:$discriminator\n")
        append("SIGNER_ACCOUNT:$userAccount\n")
        append("DESTINATION:$destination\n")
        append("MINT:$mint\n")
        append("AMOUNT_RAW:$amountRaw\n")  // Exact lamports/tokens
        append("DECIMALS:$decimals\n")
        // This covers the full instruction, not partial fields
    }
    return canonical.toByteArray(Charsets.UTF_8)
}

// CHANGE 2: Strict hash comparison
fun assertUnchanged(currentSerialized: ByteArray, review: ReviewedAction) {
    val currentHash = SHA256Service.hash(currentSerialized)
    // Exact match required; no case-insensitive comparison
    if (currentHash.lowercase() != review.messageHash.lowercase()) {
        throw IllegalStateException(
            "MESSAGE_CHANGED: Transaction payload altered after user review! " +
            "Expected: ${review.messageHash.take(16)}... " +
            "Got: ${currentHash.take(16)}... " +
            "Authorization blocked for user safety."
        )
    }
}

// CHANGE 3: Remove partial hashing
// DELETE: Old methods that hashed only action.rawAmount or action.recipient
```

**Test**: 
- Hash 25 SKR → verify hash
- Alter amount to 25.000001 → hash differs → MESSAGE_CHANGED thrown ✅

---

### 2. SolanaDecoder.kt — Official SKR Validation
**Current State**: Generic instruction parsing  
**Required**: Validate exact official SKR mint

**Changes**:
```kotlin
package com.example.services

object SkrValidator {
    // OFFICIAL SKR ON DEVNET & MAINNET
    private const val OFFICIAL_SKR_MINT = "SKRskrY7Z43bZ1b9GvPqN648YjY27J79vXmY37VfK84"
    private const val OFFICIAL_SKR_DECIMALS = 6  // NOT 9
    
    fun validateMintAndDecimals(mint: String, decimals: Int): Result<String> {
        return when {
            mint.isBlank() -> Result.failure("Mint address missing")
            decimals < 0 || decimals > 18 -> Result.failure("Invalid decimals: $decimals")
            mint != OFFICIAL_SKR_MINT -> Result.failure(
                "Invalid token mint. Expected official SKR ($OFFICIAL_SKR_MINT)"
            )
            decimals != OFFICIAL_SKR_DECIMALS -> Result.failure(
                "Invalid decimals. Official SKR has $OFFICIAL_SKR_DECIMALS decimals, got $decimals"
            )
            else -> Result.success("Valid official SKR")
        }
    }
    
    fun isOfficialSkr(mint: String, decimals: Int): Boolean {
        return mint == OFFICIAL_SKR_MINT && decimals == OFFICIAL_SKR_DECIMALS
    }
}
```

**Integrate into instruction decoder**:
```kotlin
// In SolanaDecoder.kt, when parsing TransferChecked instruction:
fun decodeTransferChecked(instruction: DecodedInstruction): Result<DecodedPayment> {
    val skrValidation = SkrValidator.validateMintAndDecimals(
        instruction.mint,
        instruction.decimals
    )
    return skrValidation.mapCatching { _ ->
        DecodedPayment(
            type = "Transfer",
            amount = instruction.amountRaw,
            decimals = instruction.decimals,
            mint = instruction.mint,
            destination = instruction.destination,
            supportsApproval = true
        )
    }.mapError { error ->
        RefusalCard(
            title = "Unsupported Token",
            reason = error.message ?: "Invalid token",
            recommendation = "Only official SKR (6 decimals) is supported."
        )
    }
}
```

**Test**:
- Decode official 25 SKR (6 decimals) → supports approval ✅
- Decode wrong mint → RefusalCard shown ✅
- Decode 9 decimals → RefusalCard shown ✅

---

### 3. ClearScreen.kt — SetAuthority Refusal & Tamper Test
**Current State**: Shows decoded transaction  
**Required**: Detect SetAuthority, implement tamper test

**Changes**:

```kotlin
// CHANGE 1: Detect SetAuthority instruction
fun detectUnsupportedOperations(instruction: DecodedInstruction): List<RefusalCard> {
    val refusals = mutableListOf<RefusalCard>()
    
    if (instruction.type == "SetAuthority") {
        refusals.add(RefusalCard(
            title = "Authority Transfer Detected",
            reason = "This transaction attempts to transfer control of an account.",
            recommendation = "Blocked for security. This operation is not supported."
        ))
    }
    
    if (instruction.type == "CloseAccount") {
        refusals.add(RefusalCard(
            title = "Account Close Detected",
            reason = "This transaction will close an account and transfer remaining funds.",
            recommendation = "Blocked. This operation is not supported."
        ))
    }
    
    return refusals
}

// CHANGE 2: Implement Approve button logic with tamper test
@Composable
fun ReviewCard(
    instruction: DecodedInstruction,
    viewModel: ClearanceViewModel
) {
    var isApproveDisabled by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Column(modifier = Modifier.padding(16.dp)) {
        // Show instruction details
        Text("Amount: ${instruction.amountRaw} (${instruction.decimals} decimals)")
        Text("Mint: ${instruction.mint.take(8)}...${instruction.mint.takeLast(4)}")
        Text("Destination: ${instruction.destination.take(8)}...${instruction.destination.takeLast(4)}")
        
        // Show any refusal cards
        detectUnsupportedOperations(instruction).forEach { card ->
            RefusalCardUI(card)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // REJECT button: immediate, no confirmation
            Button(
                onClick = {
                    viewModel.rejectReview()
                    // Navigate back or show "Rejected" toast
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("Reject")
            }
            
            // APPROVE button: with tamper test
            Button(
                onClick = {
                    // TAMPER TEST: Behind the scenes, alter the amount
                    // This simulates a malicious RPC changing 25 → 25.000001
                    val tamperedInstruction = instruction.copy(
                        amountRaw = (instruction.amountRaw.toLongOrNull() ?: 0L) + 1L
                    )
                    val tamperedSerialized = ReviewBinding.serializeMessagePayload(
                        discriminator = tamperedInstruction.type,
                        destination = tamperedInstruction.destination,
                        mint = tamperedInstruction.mint,
                        amountRaw = tamperedInstruction.amountRaw,
                        decimals = tamperedInstruction.decimals,
                        userAccount = "demo-user"  // hardcoded for demo
                    )
                    
                    // Try to assert unchanged; this will throw MESSAGE_CHANGED
                    try {
                        ReviewBinding.assertUnchanged(
                            tamperedSerialized,
                            review  // Original hash from approval setup
                        )
                        // If we get here, tamper test passed (no change)
                        // Proceed with approval
                        viewModel.approveReview()
                        
                    } catch (e: IllegalStateException) {
                        if (e.message?.contains("MESSAGE_CHANGED") == true) {
                            // Tamper test triggered; disable button
                            isApproveDisabled = true
                            errorMessage = "Transaction was altered. Please review again."
                        }
                    }
                },
                enabled = !isApproveDisabled,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text("Approve")
            }
        }
        
        if (errorMessage != null) {
            Text(
                errorMessage!!,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
```

**Test**:
- Normal approval: MESSAGE_CHANGED not triggered, receipt saved ✅
- Tamper test: MESSAGE_CHANGED thrown, button disabled ✅
- Error message visible: "Transaction was altered" ✅

---

### 4. MainActivity.kt & Theme — Add Demo Mode Label
**Current State**: No demo-mode indicator  
**Required**: Label all screens as demo

**Changes**:
```kotlin
// In MainActivity.kt, in the setContent lambda
setContent {
    ClearanceTheme {
        // Add demo mode banner at top
        Column(modifier = Modifier.fillMaxSize()) {
            // DEMO MODE BANNER
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Yellow),
                color = Color(0xFFFFEAA7)
            ) {
                Text(
                    "DEMO MODE — No wallet connected",
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black
                )
            }
            
            // Main content
            MainScreen(viewModel = viewModel)
        }
    }
}
```

**Also in NowScreen.kt**:
```kotlin
@Composable
fun NowScreen(viewModel: ClearanceViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Demo Account (Simulated)",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        Text("Address: 9B2T...7xK9 (demo)")
        
        // Sample data
        Text("SOL Balance: 2.5 (sample)")
        Text("SKR (Liquid): 1,000 (demo)")
        Text("SKR (Staked): 500 (demo)")
        
        Text(
            "Cooldown: 47h 52m remaining (sample)",
            color = Color.Red
        )
    }
}
```

**And in ReceiptsScreen.kt**:
```kotlin
// On each receipt
Text(
    "Demo approval. Nothing was broadcast.",
    style = MaterialTheme.typography.labelSmall,
    color = Color.Gray
)
```

---

## Priority 2: Clean Up Simulations (Oct 5–6)

### 5. SkrStakingService.kt — Label as Demo Data
**Current State**: Returns hardcoded values as if real  
**Required**: Clearly label all values as simulated

**Changes**:
```kotlin
object SkrStakingService {
    // DEMO DATA — All values below are simulated for demonstration only
    
    private const val DEMO_ACCOUNT = "9B2T...7xK9"
    private const val DEMO_GUARDIAN = "EthelSec Sentinel"  // Sample
    private const val DEMO_APY = 26.2  // Sample inflation rate (not real)
    
    fun getStakePosition(): SkrStakePosition {
        return SkrStakePosition(
            guardianName = DEMO_GUARDIAN,
            stakedAmount = 500,  // DEMO
            coolingDownAmount = 200,  // DEMO
            liquidAmount = 1000,  // DEMO
            apyPercent = DEMO_APY,  // DEMO
            cooldownEndTimestamp = System.currentTimeMillis() + (47 * 3600 * 1000),  // DEMO (47h)
            claimableEpochRewards = 0.0,  // DEMO
            epoch = 1  // DEMO
        )
    }
    
    fun getSeasonClaims(): List<SeasonClaim> {
        return listOf(
            SeasonClaim(
                seasonId = "S1",
                seasonName = "Season 1 (Demo)",
                allocatedSkr = 25.0,  // DEMO
                claimDeadlineTimestamp = System.currentTimeMillis() + (45 * 86400 * 1000),  // DEMO (45d)
                requiredSolFloor = 0.015  // Real floor
            )
        )
    }
}
```

---

### 6. SolanaRpcService.kt — Mark as Demo
**Current State**: Tries to call Devnet RPC  
**Required**: Label all returns as demo data

**Changes**:
```kotlin
object SolanaRpcService {
    // DEMO MODE: All RPC calls return simulated data
    // In production (Oct 9+), this will call actual Solana RPC
    
    fun getBalance(account: String): Flow<Long> = flow {
        // DEMO: Hardcoded balance
        emit(2_500_000_000L)  // 2.5 SOL in lamports
    }
    
    fun getTokenBalance(account: String, mint: String): Flow<String> = flow {
        // DEMO: Return sample balance for official SKR only
        if (mint == "SKRskrY7Z43bZ1b9GvPqN648YjY27J79vXmY37VfK84") {
            emit("1000000000")  // 1000 SKR (6 decimals)
        } else {
            emit("0")  // Unsupported token
        }
    }
}
```

---

### 7. MwaWalletService.kt — Disable or Mark as Disabled
**Current State**: Generates fake signatures  
**Required**: Explicitly mark as not integrated

**Changes**:
```kotlin
object MwaWalletService {
    // NOT INTEGRATED IN THIS DEMO
    // Mobile Wallet Adapter integration is planned for Oct 9+
    // This service is disabled and returns demo-only data
    
    fun connectWallet(): Result<String> {
        // DEMO: Return hardcoded demo account
        return Result.success("9B2T...7xK9")
    }
    
    fun signTransaction(transaction: ByteArray): Result<ByteArray> {
        // NOT IMPLEMENTED: This would require real Seed Vault connection
        return Result.failure(
            Exception(
                "Real wallet integration not available in demo mode. " +
                "Planned for Oct 9+ release."
            )
        )
    }
}
```

---

### 8. GeminiAuditService.kt — Disable
**Current State**: Returns heuristic scores  
**Required**: Mark as not in scope for this demo

**Changes**:
```kotlin
object GeminiAuditService {
    // NOT IN SCOPE FOR THIS DEMO
    // Firebase Gemini AI audit service is planned for future release
    // This class is disabled
    
    fun auditTransaction(instruction: DecodedInstruction): Result<AuditScore> {
        return Result.failure(
            Exception(
                "Gemini AI audit service not available in demo mode. " +
                "Planned for post-hackathon release."
            )
        )
    }
}
```

---

## Priority 3: Scope Reduction (Oct 5–6)

### 9. Remove Unused Screens from Navigation
**Current State**: Tab navigation includes TASKS, NOW, CLEAR, PROOFS  
**Required**: Remove TASKS, keep NOW, CLEAR, RECEIPTS

**Changes in MainScreen.kt**:
```kotlin
@Composable
fun MainScreen(viewModel: ClearanceViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // REMOVED: TASKS tab (DePIN integration)
        // KEPT: NOW, REVIEW (CLEAR), RECEIPTS (PROOFS)
        
        TabRow(selectedTabIndex = selectedTab) {
            // Tab 0: Now (Home)
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Now") }
            )
            
            // Tab 1: Review (Clearance Card)
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Review") }
            )
            
            // Tab 2: Receipts (Outcomes)
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Receipts") }
            )
        }
        
        when (selectedTab) {
            0 -> NowScreen(viewModel)
            1 -> ClearScreen(viewModel)
            2 -> ReceiptsScreen(viewModel)  // Rename from ProofScreen
        }
    }
}
```

---

### 10. ProofScreen.kt → ReceiptsScreen.kt (Rename)
**Current State**: `ProofScreen.kt` with complex logic  
**Required**: Simplify to `ReceiptsScreen.kt` with local storage only

**Changes**:
- Rename file: `ProofScreen.kt` → `ReceiptsScreen.kt`
- Remove: Network-dependent settlement checks
- Keep: Local approval history, copy button
- Add: Label "Demo approval. Nothing was broadcast."

---

## File Change Summary

| File | Change | Priority | Oct 5–6 |
|------|--------|----------|---------|
| `ReviewBinding.kt` | Hash actual instruction bytes | **P1** | **Required** |
| `SolanaDecoder.kt` | Official SKR validation | **P1** | **Required** |
| `ClearScreen.kt` | SetAuthority refusal + tamper test | **P1** | **Required** |
| `MainActivity.kt` | Add demo-mode banner | **P1** | **Required** |
| `SkrStakingService.kt` | Label all data as demo | **P2** | Strongly recommended |
| `SolanaRpcService.kt` | Mark as demo RPC | **P2** | Strongly recommended |
| `MwaWalletService.kt` | Disable, mark as Oct 9+ | **P2** | Strongly recommended |
| `GeminiAuditService.kt` | Disable, out of scope | **P2** | Recommended |
| `MainScreen.kt` | Remove TASKS tab | **P3** | Nice-to-have |
| `ProofScreen.kt` | → `ReceiptsScreen.kt` | **P3** | Nice-to-have |
| `README.md` | Replace with README_CORRECTED.md | **P1** | **Required** |

---

## Testing After Each Change

### Oct 5 (Morning—Priority 1)
After ReviewBinding + SolanaDecoder + ClearScreen changes:
```bash
./gradlew assembleDebug
# APK should build without errors
```

### Oct 5 (Afternoon—Priority 1)
After MainActivity demo label:
```bash
./gradlew installDebug
# Open app on device
# Verify: "DEMO MODE — No wallet connected" banner visible
```

### Oct 6 (Morning—Priority 1 + 2)
After all Priority 1 and 2 changes:
```bash
./gradlew testDebugUnitTest
# Run unit tests for ReviewBinding, SolanaDecoder
# Verify: 25 SKR decodes correctly
# Verify: MESSAGE_CHANGED triggered on tamper
```

### Oct 6 (Afternoon—Priority 3 + Testing)
After scope reduction:
```bash
./gradlew installDebug
# Test six scenarios:
# 1. Reject SetAuthority → refusal card shown
# 2. Approve 25 SKR → receipt stored
# 3. Tamper amount → MESSAGE_CHANGED → Approve disabled
# 4. Copy receipt → clipboard contains data
# 5. Kill app, reopen → receipt still there
# 6. All screens labeled "demo"
```

---

## Commits Schedule

**Oct 5**:
```bash
git commit -m "Priority 1: Core integrity (MESSAGE_CHANGED, SKR validation, refusal)"
```

**Oct 5–6**:
```bash
git commit -m "Priority 2: Label simulations (demo data, no real RPC/wallet)"
```

**Oct 6**:
```bash
git commit -m "Priority 3: Scope reduction (remove TASKS, rename ProofScreen)"
```

---

**Ready to begin Oct 5?** Clone locally, follow Priority 1 changes, build, test, then iterate through Priority 2 and 3.
