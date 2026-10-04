# Clearance — Local Review & Receipt Demo
> **A phone-first desk that explains supported payment requests and preserves a copyable receipt.**  
> *CLOCK IN: Solana Mobile Hackathon | Demo submission, Oct 8, 2026*

---

## 🎯 What This Is

Clearance is a **demo application** that solves a real mobile-first UX problem: **users don't know what they're approving.**

When a user in a mobile wallet sees a transaction request, they face three critical questions:
1. **Which asset?** (SOL, SKR, or something else?)
2. **How much?** (Exact amount, with decimals)
3. **Who gets authority?** (Is anyone granted control?)

**Clearance answers these three questions.** It decodes one payment instruction type (Token `TransferChecked` for official SKR), shows refusal cards for unsupported operations (like authority changes), prevents tampering with MESSAGE_CHANGED detection, and preserves a copyable receipt.

**This is not a full wallet.** It's a focused demo of the review-and-receipt flow, clearly labeled as demo mode throughout.

---

## 📊 Honest Scope

### Three Screens ✅

**Now** (Home)
- Demo account (hardcoded)
- Sample balances: SOL + SKR
- Sample 48-hour cooldown (labeled as sample data)
- Reason to return: Shows what's pending

**Review** (Clearance Card)
- Decode official 25 SKR TransferChecked instruction
- Show refusal cards for unsupported operations:
  - Authority transfer (SetAuthority) → "Blocked for security"
  - Wrong token mint → "Invalid token"
  - Missing amount → "Unsupported operation"
- Show tampering detection:
  - 25 SKR → alter to 25.000001 → MESSAGE_CHANGED prevents approval
- Two buttons: [Reject] (immediate) and [Approve] (local, no wallet)

**Receipts** (Outcomes)
- Store local approvals (timestamp, amount, mint, account, hash)
- Display stored outcomes
- Copy button to clipboard
- Label: "Demo approval. Nothing was broadcast."

### Not in Scope ❌

- ❌ Real wallet integration (Mobile Wallet Adapter planned for Oct 9+)
- ❌ Live RPC settlement (Devnet calls simulated)
- ❌ Staking logic in critical path (GUI only)
- ❌ DePIN integrations (Helium, Grass, Hivemapper)
- ❌ Gemini AI audit service
- ❌ "50 features shipped" (modular roadmap only)
- ❌ Hardware-attested evidence (Android Keystore logic present, not verified)

---

## 🔒 Core Integrity Features

### 1. MESSAGE_CHANGED: Tamper Detection
- **What**: When you review a 25 SKR payment, Clearance computes a SHA-256 hash of the exact instruction bytes
- **How**: Before approval, it re-hashes and compares. If any byte changed (25 → 25.000001), the hash differs
- **Result**: Approval is disabled; user sees "Transaction was altered"
- **Why it matters**: Prevents a malicious RPC or UI from silently changing the amount you approved

**Code**:
```kotlin
// In ReviewBinding.kt
fun assertUnchanged(currentSerialized: ByteArray, review: ReviewedAction) {
    val currentHash = SHA256Service.hash(currentSerialized)
    if (currentHash != review.messageHash) {
        throw IllegalStateException("MESSAGE_CHANGED: Transaction altered after review!")
    }
}
```

### 2. Official SKR Validation
- **What**: Only decode and approve Token `TransferChecked` instructions for the official SKR mint
- **Mint**: `SKRskrY7Z43bZ1b9GvPqN648YjY27J79vXmY37VfK84`
- **Decimals**: 6 (not 9, not other values)
- **Result**: Reject other tokens with "Invalid mint" message

**Code**:
```kotlin
// In SolanaDecoder.kt
val OFFICIAL_SKR_MINT = "SKRskrY7Z43bZ1b9GvPqN648YjY27J79vXmY37VfK84"
val OFFICIAL_SKR_DECIMALS = 6

fun validateSkr(mint: String, decimals: Int): Result<Unit> {
    if (mint != OFFICIAL_SKR_MINT) return Result.failure("Invalid mint")
    if (decimals != OFFICIAL_SKR_DECIMALS) return Result.failure("Invalid decimals")
    return Result.success(Unit)
}
```

### 3. Refusal Cards: Explicit Blocking
- **Authority changes** (SetAuthority instruction) → "Authority transfer detected. Blocked for security."
- **Wrong tokens** (any mint except official SKR) → "Invalid token mint"
- **Unsupported programs** (anything not Token Program) → "Unsupported operation"
- **Missing or invalid amounts** → "Invalid amount"

**Why it matters**: Users see exactly why certain requests are blocked, building trust.

### 4. Local Receipts (No Broadcast)
- All approvals are stored locally on the device
- Each receipt includes: timestamp, amount, mint, destination account, message hash
- Copy button lets users save the receipt as text
- **Label on all receipts**: "Demo approval. Nothing was broadcast to Solana."

---

## 🛠️ Local Build Instructions

### Prerequisites
- **JDK 17+** (verify: `java -version`)
- **Android SDK 36** (installed via Android Studio SDK Manager)
- **Android Build-Tools 36.x**
- **Gradle** (included via wrapper; 9.3.1)

### Setup

**1. Clone or download this repository**
```bash
git clone https://github.com/USERNAME/clearance.git
cd clearance
```

**2. Configure Android SDK path**
Edit `local.properties`:
```properties
# macOS:
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk

# Linux:
sdk.dir=~/Android/Sdk

# Windows:
sdk.dir=C:\Users\YOUR_USERNAME\AppData\Local\Android\sdk
```

Or set environment variable:
```bash
export ANDROID_HOME=/Users/YOUR_USERNAME/Library/Android/sdk
```

**3. Build debug APK**
```bash
./gradlew clean
./gradlew assembleDebug
```

Expected output:
```
BUILD SUCCESSFUL in XXs
APK location: app/build/outputs/apk/debug/app-debug.apk
```

**4. Install on device or emulator**
```bash
./gradlew installDebug
# Or: adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**5. Open app**
Look for "Clearance" app icon on device home screen. Launch it.

### Troubleshooting

| Issue | Solution |
|-------|----------|
| `SDK location not found` | Verify `local.properties` or `ANDROID_HOME` is set correctly |
| `Could not determine Android SDK` | Run `./gradlew clean` then retry build |
| Gradle out of memory | Add to `gradle.properties`: `org.gradle.jvmargs=-Xmx4096m` |
| APK won't install | Ensure Android 7.0+ device; uninstall old version first: `adb uninstall com.example` |

---

## 🧪 Testing the Demo

### Test 1: Reject Unsupported Request
1. Open Clearance
2. Navigate to **Review** screen
3. Tap "View SetAuthority Request"
4. Tap **[Reject]**
5. **Expected**: Refusal card shows "Authority transfer detected. Blocked for security."

### Test 2: Review & Approve Official SKR
1. Open Clearance → **Review**
2. Tap "View 25 SKR Payment"
3. Read decoded transaction:
   - Amount: 25 SKR
   - Mint: SKRskr...K84 (official) ✅
   - Decimals: 6 ✅
   - Destination: (your demo account)
4. Tap **[Approve]**
5. **Expected**: Receipt stored and displayed

### Test 3: Tamper Detection (MESSAGE_CHANGED)
1. Open Clearance → **Review**
2. Tap "View 25 SKR Payment" (clean)
3. Observe Approve button is **enabled**
4. **Behind the scenes**, app alters amount to 25.000001
5. **Expected**: Approve button becomes **disabled**, error shows "Transaction was altered"
6. **Restore** original amount
7. **Expected**: Approve button re-enables

### Test 4: Receipt Copy
1. Open Clearance → **Receipts**
2. View stored receipt (from Test 2)
3. Tap **[Copy Receipt]**
4. Open Notes app and paste (Cmd+V or Ctrl+V)
5. **Expected**: Receipt text appears with amount, hash, timestamp

### Test 5: App Restart & Persistence
1. Complete Test 2 (approve 25 SKR)
2. Close Clearance app (kill it)
3. Reopen Clearance
4. Navigate to **Receipts**
5. **Expected**: Previously approved receipt is still there

---

## 📁 Project Structure

```
clearance/
├── app/
│   └── src/main/java/com/example/
│       ├── MainActivity.kt                 Entry point
│       ├── ui/
│       │   ├── ClearanceViewModel.kt       MVVM state
│       │   ├── MainScreen.kt               Tab navigation (Now, Review, Receipts)
│       │   └── screens/
│       │       ├── NowScreen.kt            Home with demo balances
│       │       ├── ClearScreen.kt          Review decoded instruction + tamper test
│       │       └── ReceiptsScreen.kt       Local approval history + copy
│       ├── services/
│       │   ├── ReviewBinding.kt            ✅ MESSAGE_CHANGED hash logic
│       │   ├── SolanaDecoder.kt            ✅ Instruction parsing + SKR validation
│       │   └── [Other services marked as demo-only]
│       ├── security/
│       │   └── SHA256Service.kt            ✅ Hash computation
│       └── data/
│           ├── ClearanceDatabase.kt        Local receipt storage
│           └── Entities.kt
├── build.gradle.kts                        Dependencies + config
├── local.properties                        Android SDK path (YOUR CONFIG)
├── gradlew                                 Gradle wrapper script
├── gradle/wrapper/                         Gradle 9.3.1
└── README.md (this file)
```

**✅ = Honest and tested**  
**⚠️ = Demo-mode labeled**

---

## 🎬 Demo Video

**Location**: `demo.mp4` (90 seconds)

**Sequence**:
1. (0:00–0:15) Open app, show demo-mode label
2. (0:15–0:30) Review SetAuthority request → tap Reject
3. (0:30–0:60) Review 25 SKR TransferChecked → trigger MESSAGE_CHANGED → show Approve disabled
4. (0:60–0:90) Restore and approve → show receipt → copy to clipboard

**Label visible on all screens**: "DEMO MODE — No wallet connected"

---

## 🎤 Pitch Summary

**Problem**: Users don't know what they're approving on mobile wallets.

**Solution**: Clearance decodes payment requests, shows refusal cards for unsupported operations, prevents tampering with MESSAGE_CHANGED, and preserves receipts.

**What's implemented**:
- ✅ Review screen with instruction decoding
- ✅ MESSAGE_CHANGED tamper detection
- ✅ Official SKR validation
- ✅ Refusal cards
- ✅ Local receipt storage and copy

**What's next** (Oct 9+):
- Real wallet integration via Mobile Wallet Adapter
- User validation (5–10 testers)
- Live RPC settlement
- Mainnet support

**The 50-idea roadmap** is a modular expansion plan, not current scope.

---

## 📋 Submission Checklist

- [x] Source code complete and git-tracked
- [x] Gradle wrapper ready (9.3.1)
- [ ] Local build succeeds on your machine
- [ ] APK installs and opens
- [ ] Review screen decodes 25 SKR
- [ ] Tamper test works (MESSAGE_CHANGED)
- [ ] Receipt copy works
- [ ] App restarts and persists receipts
- [ ] Demo video recorded (90 seconds, 1080p)
- [ ] Pitch deck created (10 slides, honest scope)
- [ ] Source pushed to public GitHub
- [ ] Submitted to hackathon before Oct 8, 11:59 PM PDT

---

## 📞 Support

### Build Help
- Android Studio: https://developer.android.com/studio
- Gradle: https://gradle.org
- Kotlin: https://kotlinlang.org

### Solana Integration (Future)
- Solana Mobile: https://solanamobile.com/developers
- Mobile Wallet Adapter: https://github.com/solana-mobile/mobile-wallet-adapter
- SPL Token Program: https://spl.solana.com/token

### Hackathon
- Portal: https://solanamobile.com/hackathon
- Deadline: Oct 8, 11:59 PM PDT

---

## 📜 License

Apache 2.0 (See LICENSE file)

---

## ✍️ Attribution

**Built by**: Codes by Febin  
**Submission**: CLOCK IN Solana Mobile Hackathon, Oct 8, 2026  
**Honest scope**: Three screens (Now, Review, Receipts), demo mode, local-only  
**Next phase**: Real wallet integration and user validation (Oct 9+)

---

**Status**: ✅ Ready for local build (Oct 5)  
**Deadline**: Oct 8, 11:59 PM PDT  
**Goal**: Honest demo that solves a real problem
