# START HERE: Oct 5–8 Sprint Plan
**Date**: Oct 4, 2026  
**Deadline**: Oct 8, 11:59 PM PDT  
**Status**: Ready for local build

---

## 🎯 One-Sentence Summary

**Build a three-screen Android demo that honestly shows review, refusal, and tamper prevention—not a full wallet with 50 features.**

---

## 📂 What to Read (In This Order)

| Document | Purpose | Time |
|----------|---------|------|
| **This file** | Overview and entry point | 5 min |
| `OCT5-8_SPRINT.md` | Day-by-day breakdown with gates | 10 min |
| `README_CORRECTED.md` | What the app actually does | 10 min |
| `CODE_CHANGES_REQUIRED.md` | Exact code changes needed | 30 min |

---

## 🚀 Quick Start (Oct 5 Morning)

**On your local machine:**

```bash
# 1. Get the code
git clone https://github.com/YOUR_USERNAME/clearance.git
cd clearance

# 2. Configure Android SDK (edit this file)
# local.properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk    # macOS
# OR
sdk.dir=~/Android/Sdk                                 # Linux

# 3. Build
./gradlew clean
./gradlew assembleDebug

# Expected output: BUILD SUCCESSFUL in XXs
# APK: app/build/outputs/apk/debug/app-debug.apk
```

**If build succeeds**: ✅ You're ready for Priority 1 code changes  
**If build fails**: Debug environment (SDK path, JDK version), then retry

---

## 🎬 What Gets Built (Oct 5–8)

### Three Screens Only ✅

**Now** (Home)
- Demo account
- Sample balances
- Sample cooldown
- **All labeled**: "DEMO MODE"

**Review** (Clearance Card)
- Decode official 25 SKR TransferChecked instruction
- Show refusal cards (SetAuthority blocked, wrong mint blocked)
- **MESSAGE_CHANGED test**: Alter amount → approve button disabled
- Two buttons: [Reject] and [Approve]

**Receipts** (Outcomes)
- Store local approvals
- Copy button
- **Label**: "Demo approval. Nothing was broadcast."

### What's Removed ❌

- ❌ Real wallet (Mobile Wallet Adapter)
- ❌ Live RPC (Devnet settlement)
- ❌ Staking logic in critical path
- ❌ DePIN integrations
- ❌ Gemini AI
- ❌ "50 features shipped" claims

---

## 📅 4-Day Sprint

### Day 1: Oct 5 (8–12 hours)
**Morning**: Set up local build  
**Afternoon**: Apply Priority 1 code changes (ReviewBinding, SolanaDecoder, ClearScreen)  
**Evening**: Build, install, verify demo-mode banner appears  
**Gate**: `./gradlew assembleDebug` succeeds; APK installs

### Day 2: Oct 6 (6–8 hours)
**Morning**: Apply Priority 2 changes (label simulations)  
**Midday**: Run unit tests  
**Afternoon**: Test six core scenarios (reject, approve, tamper, copy, restart, persistence)  
**Gate**: All six tests pass; MESSAGE_CHANGED triggered on tampering

### Day 3: Oct 7 (4–6 hours)
**Morning**: Record demo video (90 seconds)  
**Afternoon**: Create pitch deck (10 slides)  
**Evening**: Verify video and deck quality  
**Gate**: Demo footage matches actual app behavior

### Day 4: Oct 8 (2–3 hours)
**Morning**: Build release APK  
**Afternoon**: Verify GitHub is public, video plays, deck exports  
**Evening**: Submit before 11:59 PM PDT  
**Gate**: Submitted ✅

---

## 🛠️ Three Types of Code Changes

### Type 1: Core Integrity (REQUIRED)
**Files**: `ReviewBinding.kt`, `SolanaDecoder.kt`, `ClearScreen.kt`, `MainActivity.kt`  
**Changes**: MESSAGE_CHANGED hashing, official SKR validation, refusal cards, demo label  
**Impact**: Without these, tamper detection doesn't work and app makes false claims  
**When**: Oct 5 afternoon (Priority 1)

### Type 2: Simulation Labels (STRONGLY RECOMMENDED)
**Files**: `SkrStakingService.kt`, `SolanaRpcService.kt`, `MwaWalletService.kt`, `GeminiAuditService.kt`  
**Changes**: Label all hardcoded values as "demo"; disable real integrations  
**Impact**: Without these, judges see simulated data as production  
**When**: Oct 6 morning (Priority 2)

### Type 3: Scope Reduction (OPTIONAL)
**Files**: `MainScreen.kt` (remove TASKS tab), `ProofScreen.kt` → `ReceiptsScreen.kt`  
**Changes**: Hide out-of-scope screens; simplify remaining ones  
**Impact**: Cleaner scope, but not critical if Priority 1 and 2 are done  
**When**: Oct 6 (Priority 3)

**Minimum viable**: Do Priority 1 only. Priority 2 strongly recommended. Priority 3 optional.

---

## 🔍 How to Verify Each Day

### Oct 5 Evening
```bash
./gradlew assembleDebug
# Looks for: BUILD SUCCESSFUL
./gradlew installDebug
# Looks for: App opens, DEMO MODE banner visible at top
```

### Oct 6 Evening
```bash
./gradlew testDebugUnitTest
# Looks for: All tests pass
# Manual tests:
# 1. Reject SetAuthority → refusal card
# 2. Approve 25 SKR → receipt stored
# 3. Tamper amount → MESSAGE_CHANGED → approve disabled
# 4. Copy receipt → clipboard
# 5. Kill app, reopen → receipt still there
# 6. Every screen says "DEMO MODE"
```

### Oct 7 Evening
- Demo video plays smoothly (1080p, 90 seconds)
- Video shows DEMO MODE label
- All six test steps visible in video
- Pitch deck: all 10 slides, readable, PDF exports

### Oct 8 Evening
- APK size < 100MB
- GitHub: public and accessible
- Video plays without corruption
- Submit confirmation email received

---

## 📖 Documentation Map

```
clearance/
├── START_HERE.md                      ← You are here
├── OCT5-8_SPRINT.md                   Detailed 4-day plan with gates
├── README_CORRECTED.md                What the app actually does
├── CODE_CHANGES_REQUIRED.md           Exact file-by-file changes
├── app/                               Source code
├── build.gradle.kts                   Dependencies
├── local.properties                   Android SDK path (YOUR CONFIG)
├── gradlew                            Gradle wrapper script
└── gradle/wrapper/                    Gradle 9.3.1
```

---

## ❓ Common Questions

**Q: Can I skip Priority 2 code changes?**  
A: Priority 1 is required. Priority 2 is strongly recommended (it prevents judges seeing fake production claims). Priority 3 is optional.

**Q: What if my local build fails?**  
A: Debug environment setup (SDK path, JDK version). See OCT5-8_SPRINT.md "Risk Mitigation" section.

**Q: Do I need a real Solana wallet?**  
A: No. This is a demo. All approvals are local. Real wallet integration is Oct 9+ work.

**Q: Can I integrate real wallet during Oct 5–8?**  
A: Not recommended. Stay focused on the three screens + MESSAGE_CHANGED. Wallet integration is Oct 9+.

**Q: What if I miss the Oct 8 deadline?**  
A: Hackathon submission window may be flexible; check the portal. But Oct 8, 11:59 PM PDT is the stated deadline—submit early if possible (6-hour buffer recommended).

---

## 🎯 Success Criteria

| Criterion | Status |
|-----------|--------|
| APK builds locally | [ ] |
| Message tamper detection works | [ ] |
| Official SKR validation works | [ ] |
| Refusal cards shown | [ ] |
| Receipt copy works | [ ] |
| Demo video recorded (90 sec, 1080p) | [ ] |
| Pitch deck created (10 slides, PDF) | [ ] |
| Source on public GitHub | [ ] |
| Submitted to hackathon | [ ] |

---

## 🔗 Key Resources

**Local Build**:
- Android Studio: https://developer.android.com/studio
- Gradle: https://gradle.org

**Solana (Future Integration)**:
- Solana Mobile: https://solanamobile.com/developers
- Mobile Wallet Adapter: https://github.com/solana-mobile/mobile-wallet-adapter

**Hackathon**:
- Portal: https://solanamobile.com/hackathon
- Deadline: Oct 8, 11:59 PM PDT

---

## 📞 Next Steps

1. **Now**: Read `OCT5-8_SPRINT.md` (10 min)
2. **Oct 5 morning**: Clone locally, configure Android SDK, build
3. **Oct 5 afternoon**: Read `CODE_CHANGES_REQUIRED.md`, apply Priority 1 changes
4. **Oct 5 evening**: Verify build and test
5. **Oct 6**: Apply Priority 2, run tests, verify all six scenarios
6. **Oct 7**: Record demo video, create pitch deck
7. **Oct 8**: Build release APK, submit

---

**You've got 4 days to build an honest, working demo.**  
**Start with local build on Oct 5. Everything else flows from there.**

Good luck! 🚀
