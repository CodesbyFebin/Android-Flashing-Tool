# Clearance — Verifiable Payments & DePIN Evidence on Solana Mobile Seeker

> **"Know what you approve. Prove what happened."**  
> *Built for the **CLOCK IN: Solana Mobile Hackathon** (Sep 8 – Oct 8, 2026)*

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20Seeker-purple.svg)](https://solanamobile.com)
[![Solana](https://img.shields.io/badge/network-Solana%20Devnet%20%2F%20Mainnet-14F195.svg)](https://solana.com)
[![Seed Vault](https://img.shields.io/badge/security-Seed%20Vault%20%2B%20TEE-00FFA3.svg)](https://solanamobile.com/developers)

---

## 1. Executive Summary

On Solana, wallet signatures are often treated as black boxes. When users interact with dApps or sign stake and payment instructions, they are confronted with unreadable byte arrays or generic approve modals. This opacity leads to catastrophic blind approvals, accidental full unstakes that forfeit entire epoch rewards, silent 48-hour token lockups, and malicious `SetAuthority` or token drainer injections. Furthermore, in the growing **DePIN** sector (Helium, Grass, Hivemapper, Render), physical work is performed with zero verifiable cryptographic binding to on-chain payouts.

**Clearance** is the mobile-native command center for **Solana Mobile Seeker**. It sits between intention and signature:
1. **Decodes Every Transaction**: Translates complex instruction bytes into human-readable **Clearance Cards** detailing exact balances, recipient addresses, and delegated permissions.
2. **Eliminates Blind Signing**: Enforces in-memory **Review Binding**—if any parameter is altered between review and wallet signing, Clearance halts execution immediately with `IllegalStateException("MESSAGE_CHANGED")`.
3. **Hardware-Attested DePIN Evidence**: Captures physical proof (photos, panoramic 360° video sweeps, and diagnostic telemetry logs), computes SHA-256 fingerprints, generates a canonical 2D manifest, signs it inside the **Android Keystore TEE (`secp256r1`)**, and permanently anchors the digest on-chain via the Solana Memo Program.
4. **48-Hour Cooldown & Season Claims Engine**: Live countdown clock for SKR unstaking, epoch reward forfeit warnings, 0.015 SOL floor validation, and 90-day claim expiration tracking.

---

## 2. The 50 Problem-Solving Features Table

Clearance systematically addresses the top 50 user pain points across Solana DeFi, SKR Staking, and DePIN operations:

| # | Feature | Pain Point It Kills | Implementation Status |
|---|---|---|---|
| **1** | MWA Connect, Address, SOL + SKR Balances | *"Which account am I even on?"* | **Shipped** (Seed Vault connection & live Devnet RPC balances) |
| **2** | Clearance Card Before Every Signature | Blind approving arbitrary bytecode | **Shipped** (Consequence card before Seeker signing) |
| **3** | SKR Stake / Unstake / Withdraw Decoder | Unreadable Stake Program instructions | **Shipped** (Instruction-level discriminator decoding) |
| **4** | "Rewards Stop Immediately" Warning | Unstaking under the false belief yield continues | **Shipped** (Prominent epoch forfeit alert) |
| **5** | 48-Hour Cooldown Clock | *"Where is my SKR?"* panic after unstaking | **Shipped** (Live countdown clock on Home screen) |
| **6** | Single-Tap Reject Without Second Prompt | Panic-approving accidental requests | **Shipped** (Immediate [Reject Request] abort path) |
| **7** | Memo Receipt Sealed by Real SKR Transfer | Screenshots easily forged / not verifiable | **Shipped** (Memo program embedding evidence SHA-256) |
| **8** | Official-Mint Check | Counterfeit SKR tokens draining wallets | **Shipped** (Mint validation against official SKR address) |
| **9** | Review Binding (`MESSAGE_CHANGED` Defense) | Payload altered post-approval by malicious RPC | **Shipped** (In-memory SHA-256 pre-signing lock) |
| **10** | Unknown-Program Alert | Interacting with unverified drainer programs | **Shipped** (Flags unverified programs on Clearance Card) |
| **11** | Named Delegated Guardian | Staking portals hiding validator identity | **Shipped** (Displays *EthelSec Sentinel*, *Helium Guardian*) |
| **12** | APY Next to Cooldown | Misleading 26% APY headlines hiding 2-day lock | **Shipped** (26.2% APY paired with 48h clock) |
| **13** | Inflation Trajectory Note | Assuming current yields are permanent | **Shipped** (*"10% at TGE, -25%/yr, floor 2%"* notice) |
| **14** | Locked vs Unlocked SKR Gate | Staking failing cryptically on cooling tokens | **Shipped** (Pre-validates liquid vs staked balance) |
| **16** | Unstake Preview Card | Accidentally liquidating entire staking position | **Shipped** (Preview displays amount, epoch penalty, time) |
| **17** | Partial Unstake Default | Fat-finger full exit | **Shipped** (Defaults to partial unstake with 2-tap full exit) |
| **18** | One-Tap Cooldown Withdraw | Tokens sitting forgotten in limbo after 48h | **Shipped** (One-tap [Withdraw Now] appears at 0h) |
| **20** | Unstake + Redirect Drainer Detection | Unstake coupled with unauthorized recipient swap | **Shipped** (Detects multi-instruction drainer patterns) |
| **21** | Authority Change Callout | `SetAuthority` concealed inside a claim | **Shipped** (Highlighted in prominent red hazard banner) |
| **22** | Token-Account Close / Burn Warning | "Dust cleanup" that burns active positions | **Shipped** (Explicitly warns on close/burn instructions) |
| **24** | SOL Leave-Behind Check | Account drained to zero lamports / dead account | **Shipped** (Enforces rent-exempt minimum floor) |
| **25** | Unlimited Delegation Warning | Single signature granting infinite pull | **Shipped** (Flags infinite token approvals) |
| **26** | dApp Identity on Clearance Card | Generic approval sheet saying "App" | **Shipped** (Name, URI, verified status badge) |
| **31** | Seed Vault Season Claim Calendar | Claims expiring silently in activity logs | **Shipped** (90-day countdown with urgency flags) |
| **32** | 0.015 SOL Fee Floor Check | Claims failing due to lack of fee lamports | **Shipped** (Verifies balance >= 0.015 SOL before claim) |
| **45** | Home Screen is the Open Cooldown | Having no reason to reopen the app tomorrow | **Shipped** (Cooldown clock & pending claims front & center) |
| **47** | Three-Part SKR Balance Breakdown | One blob balance causing confusion | **Shipped** (Liquid vs Staked vs Cooling Down) |
| **49** | Independent Verifier & Receipt Export | Needing to prove settlement to third parties | **Shipped** (On-device SHA-256 verifier & JSON export) |

---

## 3. System Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                        CLEARANCE APPLICATION                           │
├─────────────────┬──────────────────┬─────────────────┬─────────────────┤
│     TASKS       │       NOW        │      CLEAR      │     PROOFS      │
│  (DePIN Hub)    │   (Dashboard)    │ (Clearance Card)│  (Verification) │
├─────────────────┴──────────────────┴─────────────────┴─────────────────┤
│                     ClearanceViewModel (MVVM)                          │
├─────────────────┬──────────────────┬─────────────────┬─────────────────┤
│ SkrStaking      │ Transaction      │ ReviewBinding   │ GeminiAudit     │
│ Service (48h)   │ Explainer        │ Engine (Tamper) │ Service (AI)    │
├─────────────────┴──────────────────┴─────────────────┴─────────────────┤
│                             DATA LAYER                                 │
│    • ClearanceRepository (Flows)       • ClearanceDatabase (Room v2)   │
│    • SolanaRpcService (Live Devnet)    • KeystoreService (TEE Hardware)│
└────────────────────────────────────────────────────────────────────────┘
```

### Core Components:
- **`com.example.services.ReviewBinding`**: Serializes transaction parameters in memory, computes a pre-signing SHA-256 digest, and enforces `assertUnchanged`. Halts any mutated payload.
- **`com.example.security.KeystoreService`**: Uses the hardware-backed `AndroidKeyStore` to generate `secp256r1` ECDSA keys inside the device's Trusted Execution Environment (TEE), providing verifiable hardware attestation signatures.
- **`com.example.services.TransactionExplainer`**: Deterministically parses instruction discriminators (SPL Token `TransferChecked` 12, `Transfer` 3, System Program, Memo) to produce human-readable balance impacts and authority evaluations.
- **`com.example.services.SkrStakingService`**: Manages the 48-hour epoch cooldown, Guardian delegations, 26.2% APY inflation math, and 90-day Seed Vault claim windows with 0.015 SOL floor checks.
- **`com.example.ai.GeminiAuditService`**: Deep security forensics utilizing `gemini-3.1-pro-preview` with `thinkingLevel = ThinkingLevel.HIGH` ("high").

---

## 4. $10,000 SKR Integration Prize Alignment

Clearance makes SKR central to the application experience:
1. **Universal DePIN Settlement & Reward Vault**: DePIN tasks across Helium, Grass, and Hivemapper clear directly in SKR.
2. **48-Hour Staking Cooldown Engine**: Real-time tracking of staked vs liquid vs cooling SKR with live APY calculation (26.2%).
3. **On-Chain Evidence Anchoring**: Each completed task seals a verifiable transaction with real SKR transfers and an embedded SHA-256 evidence digest in the Solana Memo instruction.
4. **Guardian Staking Governance**: Compare and delegate SKR to active Guardians (EthelSec Sentinel, Helium Foundation, Seeker Genesis Community).
5. **Seed Vault Season Claim Calendar**: Tracks 90-day expiration windows and ensures users maintain the 0.015 SOL threshold required to claim SKR allocations.

---

## 5. Local Build & Testing Instructions

### System Prerequisites
- **JDK 17+** (Kotlin 2.2.10 requires JDK 11+; JDK 17+ recommended)
- **Android SDK 36** (or 34+; API level 36 is the target for Android 15)
- **Android NDK** (optional, if using native code)
- **Gradle 9.3.1** (included via gradle wrapper; no manual installation needed)
- **Git** (for cloning and version control)

### Step 1: Install Android SDK & Studio
1. Download [Android Studio](https://developer.android.com/studio) (includes SDK Manager)
2. Open Android Studio → SDK Manager (Tools → SDK Manager)
3. Install:
   - **Android SDK Platform 36** (latest / Android 15)
   - **Android SDK Build-Tools 36.x** (latest)
   - **Android Emulator** (optional, for testing without a physical device)
   - **Android SDK Platform-Tools** (adb, fastboot)
4. Note your SDK installation path:
   - **macOS**: `/Users/USERNAME/Library/Android/sdk`
   - **Linux**: `~/Android/Sdk`
   - **Windows**: `C:\Users\USERNAME\AppData\Local\Android\sdk`

### Step 2: Configure SDK Path
Edit `local.properties` in the project root and set your SDK path:
```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
```
Or set the `ANDROID_HOME` environment variable:
```bash
export ANDROID_HOME=/Users/YOUR_USERNAME/Library/Android/sdk
```

### Step 3: Verify Compilation
From the project root:
```bash
./gradlew assembleDebug
```
**Success**: APK generated at `app/build/outputs/apk/debug/app-debug.apk`

### Step 4: Run Unit Tests
```bash
./gradlew testDebugUnitTest
```
All unit and Robolectric tests execute locally on the JVM (no emulator required):
- **ReviewBinding** SHA-256 tamper detection
- **SkrStakingService** cooldown calculation
- **SolanaDecoder** instruction parsing
- **TransactionExplainer** human-readable card generation

### Step 5: Build Release APK
Requires a keystore (for production signing):
```bash
./gradlew assembleRelease -Pandroid.injected.signing.store.file=/path/to/keystore \
  -Pandroid.injected.signing.store.password=PASSWORD \
  -Pandroid.injected.signing.key.alias=ALIAS \
  -Pandroid.injected.signing.key.password=KEY_PASSWORD
```
**Output**: `app/build/outputs/apk/release/app-release.apk`

### Step 6: Test on Device/Emulator
1. Connect an Android 7.0+ device via USB or start the Android Emulator
2. Enable USB Debugging (Settings → Developer Options → USB Debugging)
3. Install the APK:
   ```bash
   ./gradlew installDebug
   ```
4. Launch the app from the device or via:
   ```bash
   adb shell am start -n com.example/.MainActivity
   ```

### Step 7: Set Up Firebase Credentials (Optional, for Gemini AI)
1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with package name `com.example`
3. Download `google-services.json` and place in `app/`
4. Ensure Firebase dependencies are correctly configured in `build.gradle.kts`

### Troubleshooting
| Issue | Solution |
|-------|----------|
| `SDK location not found` | Verify `local.properties` has `sdk.dir=` set correctly or `ANDROID_HOME` is exported |
| `Could not determine Android SDK` | Run `./gradlew clean` then `./gradlew assembleDebug` |
| `Gradle daemon out of memory` | Add `org.gradle.jvmargs=-Xmx4096m` to `gradle.properties` |
| `Kotlin version mismatch` | Ensure JDK 17+ is in use: `java -version` |
| `Firebase initialization fails` | Place valid `google-services.json` in `app/` directory |

---

## 6. Firebase Gemini AI Configuration

The `GeminiAuditService` uses Google's Gemini 3.1 Pro with Thinking mode for deep security audits. To enable:

1. **Firebase Console Setup**:
   - Create Firebase project
   - Enable Vertex AI API
   - Generate API key or use Application Default Credentials (ADC)

2. **local.properties** (Alternative to google-services.json):
   ```properties
   gemini.api.key=YOUR_GEMINI_API_KEY
   ```

3. **In-App Credential Flow**:
   - App reads `google-services.json` automatically
   - Gemini service initializes via Firebase SDK
   - Thinking mode set to HIGH for maximum reasoning depth

---

## 7. Submission Deliverables Checklist

### ✅ Code & Build
- [ ] Clone this repository locally
- [ ] Set `local.properties` with Android SDK path
- [ ] Run `./gradlew assembleDebug` → Verify `app/build/outputs/apk/debug/app-debug.apk` exists
- [ ] Run `./gradlew testDebugUnitTest` → All tests pass
- [ ] Commit and push to public GitHub repository

### ✅ APK Release Build
- [ ] Generate release APK: `./gradlew assembleRelease`
- [ ] Verify APK size < 100MB
- [ ] Test on physical Android device (API 24+)

### ✅ Demo Video (3 minutes)
Record continuous walkthrough showing:
1. **App Launch & MWA Connect** (30s) → Display connected wallet address and SOL/SKR balances
2. **DePIN Task Tab** (30s) → Show available tasks (Helium, Grass, Hivemapper)
3. **Evidence Capture** (30s) → Take photo, add notes, demonstrate QR scanner
4. **Clearance Card Review** (45s) → Display decoded transaction, show MESSAGE_CHANGED warning example
5. **Approve & Settlement** (30s) → Approve transaction, show receipt with signature verification
6. **Cooldown Clock & Season Claims** (15s) → Highlight 48h countdown and claim calendar

**Format**: MP4, 1920×1080 or better, under 500MB, no background music (optional narration OK)

### ✅ Pitch Deck (10 slides, PDF)
1. **Title Slide**: "Clearance — Know What You Approve"
2. **Problem**: Blind signing, catastrophic token loss, unverified DePIN payouts
3. **Solution**: Clearance Card UI + Review Binding + Hardware TEE attestation
4. **Core Features**: 50-feature matrix, MVP validation, SKR integration
5. **Architecture**: MVVM, Jetpack Compose, Solana RPC, Firebase Gemini AI
6. **Security Model**: MESSAGE_CHANGED, TEE signing, mint validation, fee floor checks
7. **Demo Highlight**: Live app walkthrough with key interactions
8. **Market Opportunity**: $10K SKR prize, DePIN expansion, blind-signing problem scale
9. **Timeline & Roadmap**: Oct 8 submission, Season 1 claim calendar, future expansion
10. **Call-to-Action**: "Empower users to sign safely. Visit github.com/CodesbyFebin/android-flashing-tool"

### ✅ GitHub Repository
1. Initialize git: `git init`
2. Add all files: `git add .`
3. Commit with message:
   ```
   Initial Clearance submission for CLOCK IN Solana Mobile Hackathon
   - MVP: 24+ features shipped (core 9 + day 30+ safety 15+)
   - Review Binding MESSAGE_CHANGED tamper detection
   - 48-hour SKR cooldown clock + season claims calendar
   - Hardware-attested DePIN evidence via Android Keystore TEE
   - Gemini 3.1 Pro AI audit service
   ```
4. Create public repository on GitHub
5. Push: `git remote add origin https://github.com/YOUR_USERNAME/clearance && git push -u origin main`
6. Make repository public (GitHub Settings → Visibility → Public)

### ✅ Final Submission Package
Prepare in a folder: `clearance-submission/`
```
clearance-submission/
├── app-release.apk              (Release APK, signed)
├── demo.mp4                      (3-minute demo video)
├── pitch-deck.pdf                (10-slide presentation)
├── github-link.txt               (Public repo URL)
└── README.txt                    (Submission notes)
```

---

## 8. Submission Instructions

**Deadline**: October 8, 2026, 11:59 PM PST

1. Prepare all deliverables (APK, video, deck, repo)
2. Visit: https://solanamobile.com/hackathon
3. Log in or create account
4. Upload submission package:
   - APK file (app-release.apk)
   - Demo video (demo.mp4)
   - Pitch deck (pitch-deck.pdf)
   - GitHub repository link
5. Verify submission received
6. Monitor for hackathon results (Nov 2026)

---

## 9. Support & Resources

- **Android Studio**: https://developer.android.com/studio
- **Solana Mobile Docs**: https://solanamobile.com/developers
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **Firebase Gemini**: https://firebase.google.com/docs/vertexai
- **Solana Web3.js**: https://solana-labs.github.io/solana-web3.js/
- **Mobile Wallet Adapter**: https://github.com/solana-mobile/mobile-wallet-adapter
