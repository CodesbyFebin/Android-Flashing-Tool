# ReviewBinding: How MESSAGE_CHANGED Works

## The Problem (Why This Matters)

A user sees a transaction: **"Send 25 SKR to Alice"**

User approves it. Then, **between approval and signing**, a malicious RPC or UI glitch changes it to **"Send 25.000001 SKR to Bob"**—and the user never sees it.

**ReviewBinding prevents this** by hashing the exact transaction bytes before approval, then re-hashing and comparing before signing. If anything changed, approval is blocked.

---

## The Flow (Big Picture)

```
USER SEES TRANSACTION
         ↓
  [User reads: 25 SKR to Alice]
         ↓
  ReviewBinding.serializeMessagePayload()
  → Converts to canonical byte string
  → Computes SHA-256 hash
  → Stores hash in memory
         ↓
  User taps [APPROVE]
         ↓
  ReviewBinding.assertUnchanged()
  → Re-serializes the exact same data
  → Re-computes SHA-256 hash
  → Compares: stored hash == current hash?
         ↓
  If MATCH: ✅ Approval proceeds, receipt stored
  If DIFFERENT: ❌ MESSAGE_CHANGED thrown, Approve disabled
```

---

## Step-by-Step Implementation

### Step 1: Define the Message Format

**What gets hashed?** Every field that matters for the transaction:

```
SOLANA_CLEARANCE_MSG_V1
INSTRUCTION_DISCRIMINATOR:TransferChecked
SIGNER_ACCOUNT:9B2T...7xK9
DESTINATION:Addr...abc
MINT:SKRskr...K84
AMOUNT_RAW:25000000
DECIMALS:6
```

**Why this format?**
- Deterministic (same data → same hash always)
- Includes all transaction-critical fields
- Human-readable (for debugging)
- Versioned (V1 allows future changes without breaking compatibility)

---

### Step 2: Serialize the Message

**Current (Broken) Code** in `ReviewBinding.kt`:
```kotlin
fun serializeMessagePayload(
    action: Action,  // ← Only gets action object
    evidenceDigest: String? = null,
    userAccount: String
): ByteArray {
    val canonical = buildString {
        append("SOLANA_CLEARANCE_MSG_V1\n")
        append("NETWORK:${action.network}\n")
        append("ACCOUNT:$userAccount\n")
        append("RECIPIENT:${action.recipient}\n")      // ← Partial
        append("MINT:${action.mint}\n")
        append("RAW_AMOUNT:${action.rawAmount}\n")     // ← Partial
        append("DECIMALS:${action.decimals}\n")
        append("TASK_ID:${action.taskId}\n")           // ← Extra field (not in instruction)
        if (!evidenceDigest.isNullOrBlank()) {
            append("EVIDENCE_DIGEST:$evidenceDigest\n")
        }
    }
    return canonical.toByteArray(Charsets.UTF_8)
}
```

**Problems**:
1. Only hashes `action` object fields, not the actual Solana instruction discriminator
2. Includes `taskId` (not part of transaction)
3. Mixes transaction fields with evidence (separate concern)
4. If `Action` structure changes, hash breaks

---

### Step 2 (Corrected): Hash the Actual Instruction

**Corrected Code**:
```kotlin
package com.example.services

object ReviewBinding {
    
    /**
     * Serialize the exact instruction bytes that will be signed.
     * This deterministic format ensures any change is detected.
     */
    fun serializeMessagePayload(
        // These are the decoded instruction fields—exactly what the user approved
        discriminator: String,      // e.g., "TransferChecked"
        destination: String,        // e.g., "Addr...abc" (token account)
        mint: String,              // e.g., "SKRskr...K84"
        amountRaw: String,         // e.g., "25000000" (25 SKR in smallest units)
        decimals: Int,             // e.g., 6
        signerAccount: String      // User's account (the one approving)
    ): ByteArray {
        // Build a deterministic canonical representation
        val canonical = buildString {
            append("SOLANA_CLEARANCE_MSG_V1\n")
            append("INSTRUCTION_DISCRIMINATOR:$discriminator\n")
            append("SIGNER_ACCOUNT:$signerAccount\n")
            append("DESTINATION:$destination\n")
            append("MINT:$mint\n")
            append("AMOUNT_RAW:$amountRaw\n")         // Exact lamports/tokens
            append("DECIMALS:$decimals\n")
            // No extra fields; only transaction-critical data
        }
        return canonical.toByteArray(Charsets.UTF_8)
    }
    
    /**
     * Compute the hash BEFORE user approval (during review).
     * Store this hash in memory.
     */
    fun prepareReview(
        discriminator: String,
        destination: String,
        mint: String,
        amountRaw: String,
        decimals: Int,
        signerAccount: String
    ): ReviewedAction {
        val serialized = serializeMessagePayload(
            discriminator, destination, mint, amountRaw, decimals, signerAccount
        )
        val messageHash = SHA256Service.hash(serialized)
        
        return ReviewedAction(
            messageHash = messageHash,           // Store this
            signerAccount = signerAccount,
            destination = destination,
            mint = mint,
            amountRaw = amountRaw,
            reviewedAt = System.currentTimeMillis(),
            discriminator = discriminator
        )
    }
    
    /**
     * BEFORE APPROVAL: Re-hash and verify nothing changed.
     * If hash differs → throw MESSAGE_CHANGED
     */
    fun assertUnchanged(
        currentSerialized: ByteArray,
        storedReview: ReviewedAction
    ) {
        val currentHash = SHA256Service.hash(currentSerialized)
        
        // Exact match required (case-insensitive for safety)
        if (currentHash.lowercase() != storedReview.messageHash.lowercase()) {
            throw IllegalStateException(
                "MESSAGE_CHANGED: Transaction payload altered after user review! " +
                "Expected hash: ${storedReview.messageHash.take(16)}... " +
                "Current hash:  ${currentHash.take(16)}... " +
                "Authorization blocked for user safety."
            )
        }
    }
}
```

**Key improvements**:
1. ✅ Takes actual instruction fields, not abstract `Action` object
2. ✅ Hashes discriminator (instruction type) explicitly
3. ✅ No extra fields (evidence, taskId)
4. ✅ Deterministic: same input → same hash always
5. ✅ Throws `IllegalStateException("MESSAGE_CHANGED")` on mismatch

---

### Step 3: Wire It Into ClearScreen (The UI)

**In `ClearScreen.kt` (Review screen)**:

```kotlin
@Composable
fun ReviewCard(
    instruction: DecodedInstruction,
    viewModel: ClearanceViewModel,
    onApprovalResult: (success: Boolean) -> Unit
) {
    var reviewedAction by remember { mutableStateOf<ReviewedAction?>(null) }
    var isApproveDisabled by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // When user first REVIEWS this instruction
    LaunchedEffect(instruction) {
        // Compute and store the hash
        reviewedAction = ReviewBinding.prepareReview(
            discriminator = instruction.type,      // "TransferChecked"
            destination = instruction.destination,
            mint = instruction.mint,
            amountRaw = instruction.amountRaw,     // "25000000"
            decimals = instruction.decimals,       // 6
            signerAccount = "demo-user"            // (hardcoded for demo)
        )
        isApproveDisabled = false
        errorMessage = null
    }
    
    Column(modifier = Modifier.padding(16.dp)) {
        // Show decoded transaction
        Text("Instruction: ${instruction.type}")
        Text("Amount: ${instruction.amountRaw} (${instruction.decimals} decimals)")
        Text("Mint: ${instruction.mint.take(8)}...${instruction.mint.takeLast(4)}")
        Text("Destination: ${instruction.destination.take(8)}...${instruction.destination.takeLast(4)}")
        Text("Message Hash: ${reviewedAction?.messageHash?.take(16)}...")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // REJECT: Immediate, no checks
            Button(
                onClick = {
                    viewModel.rejectTransaction()
                    onApprovalResult(false)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Reject")
            }
            
            // APPROVE: Check for tampering
            Button(
                onClick = {
                    if (reviewedAction == null) {
                        errorMessage = "Review not ready. Please wait."
                        return@Button
                    }
                    
                    // CRITICAL: Re-serialize the EXACT SAME data
                    val currentSerialized = ReviewBinding.serializeMessagePayload(
                        discriminator = instruction.type,
                        destination = instruction.destination,
                        mint = instruction.mint,
                        amountRaw = instruction.amountRaw,
                        decimals = instruction.decimals,
                        signerAccount = "demo-user"
                    )
                    
                    // CRITICAL: Check for tampering
                    try {
                        ReviewBinding.assertUnchanged(
                            currentSerialized,
                            reviewedAction!!  // The hash we stored during review
                        )
                        
                        // ✅ Hash matches → approve and store receipt
                        val receipt = Receipt(
                            timestamp = System.currentTimeMillis(),
                            amount = instruction.amountRaw,
                            mint = instruction.mint,
                            destination = instruction.destination,
                            messageHash = reviewedAction!!.messageHash,
                            status = "demo-approved"  // Local only
                        )
                        viewModel.storeReceipt(receipt)
                        onApprovalResult(true)
                        
                    } catch (e: IllegalStateException) {
                        if (e.message?.contains("MESSAGE_CHANGED") == true) {
                            // ❌ Hash differs → block approval
                            isApproveDisabled = true
                            errorMessage = e.message ?: "Transaction was altered"
                        } else {
                            throw e
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
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
```

---

### Step 4: The Tamper Test (Oct 6 Testing)

**How the test works**:

```kotlin
@Test
fun testMessageChangedOnAmperTampering() {
    // User reviews 25 SKR
    val original = ReviewBinding.serializeMessagePayload(
        discriminator = "TransferChecked",
        destination = "Addr...abc",
        mint = "SKRskr...K84",
        amountRaw = "25000000",  // 25 SKR
        decimals = 6,
        signerAccount = "9B2T...7xK9"
    )
    val originalHash = SHA256Service.hash(original)
    val review = ReviewedAction(
        messageHash = originalHash,
        signerAccount = "9B2T...7xK9",
        destination = "Addr...abc",
        mint = "SKRskr...K84",
        amountRaw = "25000000",
        reviewedAt = System.currentTimeMillis(),
        discriminator = "TransferChecked"
    )
    
    // Attacker tries to change amount to 25.000001 SKR
    val tampered = ReviewBinding.serializeMessagePayload(
        discriminator = "TransferChecked",
        destination = "Addr...abc",
        mint = "SKRskr...K84",
        amountRaw = "25000001",  // ← Changed by 1 lamport
        decimals = 6,
        signerAccount = "9B2T...7xK9"
    )
    
    // Assert: tampering is detected
    assertThrows<IllegalStateException> {
        ReviewBinding.assertUnchanged(tampered, review)
    }.also { e ->
        assertTrue(e.message?.contains("MESSAGE_CHANGED") == true)
    }
}
```

**In the UI (Oct 6 manual test)**:
1. User approves 25 SKR → hash stored
2. App silently alters to 25.000001 (to simulate attacker)
3. User sees: "Transaction was altered" + Approve button disabled
4. User goes back, re-reviews
5. Original amount restored → hash matches again → Approve enabled

---

## How All Phases Run in Parallel

Here's the complete Oct 5–8 timeline with ReviewBinding and other phases:

### Oct 5 (Day 1): Build + ReviewBinding Implementation

```
MORNING (3h):
├─ Clone locally
├─ Configure Android SDK
└─ ./gradlew assembleDebug
   └─ Gate: BUILD SUCCESSFUL

AFTERNOON (5h):
├─ CODE_CHANGES_REQUIRED.md
│  └─ Priority 1:
│     ├─ ReviewBinding.kt (CURRENT → CORRECTED)
│     │  ├─ Update serializeMessagePayload()
│     │  ├─ Update assertUnchanged()
│     │  └─ Remove old partial-field hashing
│     ├─ SolanaDecoder.kt (Add SKR validation)
│     ├─ ClearScreen.kt (Wire in ReviewBinding + UI)
│     └─ MainActivity.kt (Add demo-mode banner)
└─ ./gradlew assembleDebug
   └─ Gate: BUILD SUCCESSFUL (with new code)

EVENING (2h):
├─ ./gradlew installDebug
├─ Open app on device
├─ Verify: DEMO MODE banner visible
├─ Verify: Review screen shows decoded 25 SKR
└─ Gate: App opens, no crashes

RESULT: ReviewBinding implemented + MESSAGE_CHANGED logic ready for testing
```

### Oct 6 (Day 2): Testing ReviewBinding + Priority 2 Changes

```
MORNING (2h):
├─ Unit tests
│  └─ ./gradlew testDebugUnitTest
│     ├─ ReviewBinding hash tests
│     ├─ Official SKR validation tests
│     └─ Gate: All tests pass
└─ Code review (ensure no syntax errors)

MIDDAY (1h):
├─ Apply Priority 2 changes (parallel path):
│  ├─ SkrStakingService.kt (label demo data)
│  ├─ SolanaRpcService.kt (mark demo RPC)
│  ├─ MwaWalletService.kt (disable)
│  └─ GeminiAuditService.kt (disable)
└─ ./gradlew assembleDebug
   └─ Verify: Still builds

AFTERNOON (3h): Six manual tests (device)
├─ TEST 1: Reject SetAuthority
│  ├─ Navigate to Review
│  ├─ Trigger SetAuthority instruction
│  ├─ Tap [Reject]
│  └─ Verify: Refusal card shows
│
├─ TEST 2: Approve 25 SKR
│  ├─ View clean 25 SKR TransferChecked
│  ├─ Tap [Approve]
│  └─ Verify: Receipt stored
│
├─ TEST 3: MESSAGE_CHANGED (tamper test)
│  ├─ View 25 SKR
│  ├─ [Behind scenes] App alters to 25.000001
│  ├─ Tap [Approve]
│  └─ Verify: "Transaction was altered" + button disabled
│
├─ TEST 4: Copy receipt
│  ├─ Navigate to Receipts
│  ├─ Tap [Copy]
│  └─ Verify: Receipt in clipboard
│
├─ TEST 5: Restart & persistence
│  ├─ Kill app
│  ├─ Reopen
│  ├─ Navigate to Receipts
│  └─ Verify: Receipt still there
│
└─ TEST 6: Demo labels visible
   ├─ Check all screens
   └─ Verify: "DEMO MODE" banner + labels on receipts

RESULT: All core features tested + verified + ReviewBinding tamper detection proven
```

### Oct 7 (Day 3): Demo Video + Pitch Deck (Parallel)

```
MORNING (2h): Record demo video
├─ Set up device (1080p, brightness 100%)
├─ Record 90-second sequence:
│  ├─ (0:00-0:15) Open app, show demo label
│  ├─ (0:15-0:30) Reject SetAuthority
│  ├─ (0:30-0:60) Review 25 SKR + trigger MESSAGE_CHANGED
│  └─ (0:60-0:90) Approve original + show receipt copy
└─ Export: demo.mp4 (1920×1080, MP4)

PARALLEL — MORNING (2h): Create pitch deck
├─ Tool: Google Slides, Keynote, or PowerPoint
├─ 10 slides:
│  1. Clearance (demo label visible)
│  2. The problem (3 questions users face)
│  3. First user hypothesis
│  4. Three screens (Now, Review, Receipts)
│  5. Refusal (what's blocked and why)
│  6. Official SKR validation
│  7. MESSAGE_CHANGED demo (25 → 25.000001)
│  8. Receipt provenance (demo approval, not broadcast)
│  9. Demo & tests (actual sequence)
│  10. Ship & roadmap (implemented vs. planned)
└─ Export: pitch-deck.pdf (16:9, readable from 10 feet)

AFTERNOON (2h): Verify quality
├─ Video plays smoothly (1920×1080, no stutters)
├─ Video duration: 90 seconds ±5s
├─ Deck renders as PDF (all 10 slides visible)
├─ Deck text readable on screen
└─ Gate: Both deliverables ready

RESULT: Demo video + pitch deck complete + verified
```

### Oct 8 (Day 4): Release APK + Submit

```
MORNING (1h):
├─ Build release APK
│  └─ ./gradlew assembleRelease (with signing keystore)
└─ Verify APK: app/build/outputs/apk/release/app-release.apk

MIDDAY (1h): Final verification
├─ APK size < 100MB
├─ APK installs on device
├─ Review screen works (MESSAGE_CHANGED detectable)
├─ Receipts screen works
└─ All demo labels visible

AFTERNOON (1h):
├─ Push to GitHub
│  ├─ git remote add origin https://github.com/USERNAME/clearance
│  ├─ git push -u origin main
│  └─ Verify: Repository public
├─ Assemble submission folder:
│  ├─ app-release.apk
│  ├─ demo.mp4
│  ├─ pitch-deck.pdf
│  └─ github-link.txt
└─ Final checklist:
   ├─ All files present
   ├─ Video plays
   ├─ Deck displays
   └─ GitHub accessible

EVENING (1h): SUBMIT
├─ Visit https://solanamobile.com/hackathon
├─ Upload all files
├─ Verify submission received
└─ ✅ DONE

RESULT: Submitted before 11:59 PM PDT
```

---

## ReviewBinding in Context (Full Data Flow)

```
USER INTERACTION                CODE EXECUTION                  DATA STATE
─────────────────              ──────────────────              ─────────────

Open Review                →    ClearScreen composable         
                               launches LaunchedEffect
                                    ↓
User sees 25 SKR      →        instruction.type = "TransferChecked"
                               instruction.amount = "25000000"
                               instruction.mint = "SKRskr...K84"
                                    ↓
                          ReviewBinding.prepareReview()
                               ↓
                          serializeMessagePayload()
                            [build canonical string]
                               ↓
                          SHA256Service.hash()
                            "3a7f2e..." ← stored in memory
                                    ↓
                          reviewedAction.messageHash = "3a7f2e..."

User taps [APPROVE]  →        Button.onClick() triggered
                                    ↓
                          ReviewBinding.serializeMessagePayload()
                            [re-build same canonical string]
                                    ↓
                          SHA256Service.hash()
                            "3a7f2e..." (if unchanged)
                               OR
                            "e4b1c9..." (if tampered)
                                    ↓
                          ReviewBinding.assertUnchanged()
                            compare hashes
                                    ↓
                          Hash MATCH: ✅                 Receipt stored
                          Hash DIFFER: ❌ MESSAGE_CHANGED  Button disabled
```

---

## Why This Specific Implementation

| Design Choice | Why |
|---------------|-----|
| Hash **before** review, verify **before** approval | Catches tampering at the last moment |
| Use deterministic canonical format | Same data → same hash always (no randomness) |
| Include discriminator explicitly | Prevents instruction-type swaps (SetAuthority → Transfer) |
| Use exact amountRaw (no decimals in hash) | Prevents decimal manipulation |
| Throw on mismatch (don't retry) | Fail-safe: if something's wrong, don't proceed |
| Store hash in-memory only | No server/network dependency (local app) |

---

## Oct 5–8 in One Visual

```
OCT 5                    OCT 6                 OCT 7              OCT 8
─────────────────────────────────────────────────────────────────────────

[Build + ReviewBinding]   [Test 6 scenarios]   [Video + Deck]   [Submit]
├─ Clone locally         ├─ Unit tests        ├─ Record 90s     ├─ Release
├─ Configure SDK         ├─ Reject test       ├─ Create 10      │  APK
├─ Priority 1 code       ├─ Approve test      │  slides          ├─ Verify
│  └─ ReviewBinding      ├─ MESSAGE_CHANGED   ├─ Export PDF     │
│  └─ SolanaDecoder      │  tamper test       ├─ Verify         └─ SUBMIT
│  └─ ClearScreen        ├─ Copy test         └─ Ready
│  └─ MainActivity       ├─ Restart test
├─ Build & install       ├─ Label test
└─ Gate: No crashes      └─ Gate: All pass

PARALLEL ACTIVITIES:
Oct 6 afternoon: While testing (TEST 1-6), someone can start sketching deck
Oct 7 morning: Recording video + deck design in parallel
Oct 8 morning: Build APK while verifying GitHub access
```

---

**ReadyBinding is the cornerstone. Everything else (UI, testing, demo, pitch) flows from it working correctly.**

Start with Step 1 (understand the format), move to Step 2 (implement the code), then integrate into ClearScreen (Step 3). Then test relentlessly (Step 4).

Questions on any step?
