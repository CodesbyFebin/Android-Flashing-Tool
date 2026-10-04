# October 5–8 Sprint Plan
**Scope**: Three honest screens (Now, Review, Receipts) + demo video + pitch deck  
**Deadline**: Oct 8, 11:59 PM PDT  
**Status**: Code changes required before local build

---

## Day 1: Oct 5 — Build + Core Fixes

### Morning: Local Build Setup
- [ ] Clone to local machine
- [ ] Configure `local.properties` with Android SDK path
- [ ] Run `./gradlew clean assembleDebug`
- [ ] **Gate**: APK builds and installs on device

### Afternoon: Remove Simulation Claims
**Files to edit** (remove false claims):
- `README.md` → Remove references to "50 features," staking, DePIN, Gemini, real MWA
- `app/src/main/AndroidManifest.xml` → Verify permissions match actual scope (no wallet, no network)
- `ReviewBinding.kt` → **Keep**: MESSAGE_CHANGED logic for local hashing. **Fix**: Hash actual transaction bytes, not partial fields
- `SkrStakingService.kt` → Label as "demo data" throughout; remove APY/cooldown simulation as fact
- `MwaWalletService.kt` → Remove or mark as "not integrated"
- `SolanaRpcService.kt` → Mark as "demo only; no actual RPC calls"
- `MainActivity.kt` → Add demo-mode label to UI or splash screen

### Late Afternoon: Scope Lock
Freeze three screens only:
1. **Now**: Demo account, sample balances, **labeled sample cooldown**
2. **Review**: Decode Token `TransferChecked` (25 SKR, 6 decimals, official mint) + `SetAuthority` refusal + tamper test
3. **Receipts**: Local approval storage, copy button

**Remove from scope**:
- ❌ DePinTasksScreen
- ❌ EvidenceCaptureDialog
- ❌ ScanQrDialog
- ❌ ProofScreen advanced features
- ❌ GeminiAuditService
- ❌ Real MWA connection
- ❌ Real RPC calls
- ❌ Staking logic (keep UI, label as demo)

### Tests to Pass
- [ ] Review screen loads without crash
- [ ] Toast/error on missing instruction decoding
- [ ] MESSAGE_CHANGED triggered on tampered amount

---

## Day 2: Oct 6 — Core Fixes + Testing

### Core Fixes (Morning)

**1. Official SKR Validation**
- **File**: `SolanaDecoder.kt` (or new `SkrValidator.kt`)
- **Fix**: Validate exact mint: `SKRskrY7Z43bZ1b9GvPqN648YjY27J79vXmY37VfK84`
- **Fix**: Validate decimals = 6 (not 9)
- **Behavior**: Block other tokens with explanation

```kotlin
val OFFICIAL_SKR_MINT = "SKRskrY7Z43bZ1b9GvPqN648YjY27J79vXmY37VfK84"
val OFFICIAL_SKR_DECIMALS = 6

fun validateSkr(mint: String, decimals: Int): Result<Unit> {
    if (mint != OFFICIAL_SKR_MINT) return Result.failure("Invalid mint")
    if (decimals != OFFICIAL_SKR_DECIMALS) return Result.failure("Invalid decimals")
    return Result.success(Unit)
}
```

**2. Canonical Review Hashing**
- **File**: `ReviewBinding.kt`
- **Current**: Hashes action fields (incomplete)
- **Fix**: Hash actual unsigned Solana message bytes
- **Behavior**: Any byte change → different hash → Approve disabled

```kotlin
fun serializeMessagePayload(
    instruction: DecodedInstruction,  // Full decoded instruction
    userAccount: String
): ByteArray {
    val canonical = buildString {
        append("SOLANA_CLEARANCE_MSG_V1\n")
        append("INSTRUCTION_DISCRIMINATOR:${instruction.discriminator}\n")
        append("ACCOUNT:$userAccount\n")
        append("DESTINATION:${instruction.destination}\n")
        append("MINT:${instruction.mint}\n")
        append("AMOUNT_RAW:${instruction.amountRaw}\n")
        append("DECIMALS:${instruction.decimals}\n")
        // All instruction fields, not partial
    }
    return canonical.toByteArray(Charsets.UTF_8)
}

fun assertUnchanged(currentSerialized: ByteArray, review: ReviewedAction) {
    val currentHash = SHA256Service.hash(currentSerialized)
    if (currentHash != review.messageHash) {
        throw IllegalStateException("MESSAGE_CHANGED")
    }
}
```

**3. Authority Block**
- **File**: `SolanaDecoder.kt`
- **Behavior**: Detect `SetAuthority` instruction → return refusal card
- **UI**: "Authority transfer detected. Blocked for security."

**4. Tamper Test**
- **File**: `ClearScreen.kt` (Review screen)
- **Behavior**: 
  - User approves 25 SKR
  - Behind the scenes, change to 25.000001 SKR
  - Show MESSAGE_CHANGED error
  - Approve button disabled
  - User sees: "Transaction was altered. Please review again."

### Afternoon: Edge Case Testing
- [ ] Test: Disconnect app, reopen → receipt still there
- [ ] Test: Amount = 0 → block with "Invalid amount"
- [ ] Test: Missing mint → block with "Mint not found"
- [ ] Test: Malformed instruction → block with "Unsupported operation"
- [ ] Test: Duplicate approval → local dedup (show "Already approved")

### Late Afternoon: Receipt Persistence
- **File**: `ClearanceDatabase.kt` or `ClearanceRepository.kt`
- **Store locally**: timestamp, amount, mint, account, message hash, approval status
- **UI**: List in Receipts screen with copy button

**Gate**: All six core behaviors work end-to-end.

---

## Day 3: Oct 7 — Demo + Deck

### Morning: Record Demo Video (90 seconds)

**Setup**:
- Device or emulator (1080p+)
- Screen brightness 100%
- Built APK from Oct 6

**Script** (90 seconds total):

1. **(0:00–0:15)** Open app
   - Show Now screen
   - Highlight demo-mode label
   - Show sample account and balances

2. **(0:15–0:30)** Reject unsupported
   - Navigate to Review screen
   - Show SetAuthority request
   - Tap [Reject]
   - Show refusal message

3. **(0:30–0:60)** Approve real request + tamper test
   - Show new 25 SKR TransferChecked request
   - Review decoded: 25 SKR, 6 decimals, official mint, recipient
   - Tap [Approve]
   - Show MESSAGE_CHANGED error (behind-the-scenes tamper)
   - Show Approve button disabled
   - Go back, approve clean transaction
   - Success: receipt displayed

4. **(0:60–0:90)** Show receipt + copy
   - Navigate to Receipts
   - Show stored receipt: amount, account, timestamp, hash
   - Tap [Copy]
   - Show "Copied to clipboard"
   - Label: "Demo approval. Not broadcast."

**Export**:
- MP4, 1920×1080, 90 seconds
- Label on-screen: "DEMO MODE — No wallet connected"
- File: `demo.mp4`

### Afternoon: Create Pitch Deck (10 slides)

**Tools**: Google Slides, Keynote, or PowerPoint

Use slides from earlier outline, but with honest scope:
1. **Clearance** — Demo-mode label visible on first screenshot
2. **The problem** — Three questions (which asset, how much, who gets authority)
3. **First user** — Mobile user reviewing in the app; hypothesis not yet proven
4. **Three screens** — Now (demo), Review (decode + refusal + tamper), Receipts (local copy)
5. **Refusal is a feature** — SetAuthority blocked, wrong mint blocked, missing amount blocked
6. **Official SKR** — 25 SKR, 6 decimals, validated mint, exact checks
7. **Approve only reviewed** — Tamper test: 25 → 25.000001 → MESSAGE_CHANGED → Approve disabled
8. **Receipt provenance** — Outcome, hash, copy button. "Demo approval. Not broadcast."
9. **Demo & validation** — Actual sequence from video; list completed tests
10. **Ship & roadmap** — Implemented (review, refusal, tampering, receipts). Next (real wallet, Oct 9+). The 50 ideas (modular roadmap, not now).

**Export**: PDF, 16:9, readable from 10 feet

---

## Day 4: Oct 8 — Final Build + Submit

### Morning: Build Release APK

```bash
# Create keystore (one-time, if not already done)
keytool -genkey -v -keystore clearance.keystore \
  -alias clearance \
  -keyalg RSA -keysize 2048 \
  -validity 10000

# Build signed release
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=./clearance.keystore \
  -Pandroid.injected.signing.store.password=PASSWORD \
  -Pandroid.injected.signing.key.alias=clearance \
  -Pandroid.injected.signing.key.password=PASSWORD

# Verify APK
ls -lh app/build/outputs/apk/release/app-release.apk
# Should be < 100MB
```

### Late Morning: Verification
- [ ] APK installs and opens on device
- [ ] Review screen shows demo-mode label
- [ ] Tamper test works (MESSAGE_CHANGED shown)
- [ ] Receipt copy button works
- [ ] Video plays without corruption
- [ ] Pitch deck renders correctly as PDF

### Afternoon: Push to GitHub

```bash
# Commit Oct 6–8 changes
git add .
git commit -m "Clearance Oct 5–8 sprint: honest demo scope

Implemented and tested:
- Now screen: demo account + sample balances
- Review screen: decode official 25 SKR TransferChecked
- Refusal cards: SetAuthority blocked, wrong mint blocked
- MESSAGE_CHANGED: tamper prevention with hash verification
- Receipts screen: local approval storage + copy button

Scope limited to three screens (Now, Review, Receipts).
Demo mode clearly labeled on all UI.
No wallet integration, no RPC, no staking logic in critical path.

Next phase (Oct 9+): Real wallet integration + user validation.

Co-Authored-By: Codes by Febin <codesbyfebin@gmail.com>"

# Push
git remote add origin https://github.com/USERNAME/clearance
git push -u origin main
```

### Late Afternoon: Assemble Submission

```
clearance-submission/
├── app-release.apk              (Signed, verified, < 100MB)
├── demo.mp4                     (90 seconds, 1920×1080, labeled DEMO MODE)
├── pitch-deck.pdf               (10 slides, honest scope)
├── github-link.txt              (Public repository URL)
└── submission-notes.md          (Version hash, date, what's tested)
```

**submission-notes.md**:
```markdown
# Clearance Demo Submission — Oct 8, 2026

## Version
- Commit: [hash from Oct 8]
- APK: app-release.apk
- Built: Oct 8, 2026
- Scope: Three screens (Now, Review, Receipts)

## What's Implemented & Tested
- ✅ Review screen: Decode official 25 SKR TransferChecked
- ✅ Refusal: SetAuthority blocked, wrong mint blocked
- ✅ MESSAGE_CHANGED: Tamper detection with hash verification
- ✅ Receipts: Local approval storage and copy button
- ✅ Demo mode: Clearly labeled on all UI

## What's NOT in This Build
- ❌ Real wallet integration (planned Oct 9+)
- ❌ Live RPC (demo mode only)
- ❌ Staking logic in critical path
- ❌ Gemini AI
- ❌ DePIN integrations
- ❌ 50-feature claims (modular roadmap)

## Demo Video
- Duration: 90 seconds
- Device: [Pixel 6 / Emulator]
- Tested behaviors: reject, review, tamper block, approve, copy receipt

## How to Test Locally
1. `git clone https://github.com/USERNAME/clearance.git`
2. Edit `local.properties` with your Android SDK path
3. `./gradlew assembleDebug`
4. Install APK on Android 7.0+ device
5. Verify: review 25 SKR, trigger tamper test, copy receipt

## Next Milestone
Real wallet integration via Mobile Wallet Adapter + user validation.
Roadmap at github.com/USERNAME/clearance/wiki
```

### Evening: Submit (Before 11:59 PM PDT)

1. Visit https://solanamobile.com/hackathon
2. Upload:
   - app-release.apk
   - demo.mp4
   - pitch-deck.pdf
   - GitHub link
3. **Receive confirmation email**
4. ✅ Done

---

## Code Changes Summary

| File | Change | Rationale |
|------|--------|-----------|
| `README.md` | Remove staking, DePIN, 50 features; focus on three screens | Honest scope |
| `ReviewBinding.kt` | Hash full instruction bytes, not partial fields | Real tamper detection |
| `SolanaDecoder.kt` | Validate official SKR mint (6 decimals); block others | Prevent wrong tokens |
| `MainActivity.kt` | Add "DEMO MODE" label to splash/home | Transparent labeling |
| `ClearScreen.kt` | Implement SetAuthority refusal; add tamper test | Core features |
| `ProofScreen.kt` | → `ReceiptsScreen.kt`: Local storage only, copy button | Honest scope |
| `SkrStakingService.kt` | Label all data as "demo"; remove as fact | Reduce simulation claims |
| `SolanaRpcService.kt` | Mark all calls as "demo only"; no real network | Prevent false settlement |
| `MwaWalletService.kt` | Disable or clearly mark as "not integrated" | No fake wallet |
| `build.gradle.kts` | Remove Gemini, network permissions if unused | Clean dependencies |

---

## Testing Checklist

### Oct 6 Builds
- [ ] `./gradlew assembleDebug` → BUILD SUCCESSFUL
- [ ] APK installs: `./gradlew installDebug`
- [ ] App opens without crash
- [ ] Review screen loads

### Oct 6 Functional Tests
- [ ] Decode 25 SKR TransferChecked → shows amount, mint, account
- [ ] Reject SetAuthority → "Authority transfer detected. Blocked."
- [ ] Tamper (25 → 25.000001) → MESSAGE_CHANGED → Approve disabled
- [ ] Approve clean 25 SKR → receipt stored
- [ ] Receipt persists after app restart
- [ ] Copy receipt → clipboard ready

### Oct 7 Media Tests
- [ ] Video: 90 seconds ±5s, 1080p, no artifacts
- [ ] Video: DEMO MODE label visible
- [ ] Video: All six test steps shown
- [ ] Deck: All 10 slides readable, PDF exports cleanly

### Oct 8 Final Tests
- [ ] Release APK builds and installs
- [ ] All functional tests pass on release build
- [ ] GitHub: repository is public and accessible
- [ ] GitHub: source commit matches APK build date
- [ ] Video: plays without stuttering on web
- [ ] Deck: renders correctly as PDF

---

## Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| Build fails locally | Keep Oct 5 morning for full setup; debug early |
| Tamper test not working | Test hash logic in isolation (unit test) before integration |
| Video doesn't match behavior | Record on Oct 6 after fixes; re-record on Oct 7 if needed |
| GitHub access issues | Test push early (Oct 7 afternoon); use PAT if SSH fails |
| Submission portal closes early | Submit by Oct 8, 6:00 PM PDT (6-hour buffer) |

---

## Success Criteria

- ✅ APK builds, installs, opens
- ✅ Review screen decodes official 25 SKR
- ✅ Tamper detection prevents approval
- ✅ Receipt persists and copies
- ✅ Video shows real behavior (not mocked)
- ✅ Pitch deck is honest about scope
- ✅ GitHub source is public
- ✅ Submitted before deadline

---

**Owner**: Codes by Febin  
**Sprint Lead**: Corrected scope, honest claims  
**Deadline**: Oct 8, 11:59 PM PDT  
**Next**: Oct 9, real wallet integration begins
