# Clearance Hackathon Submission Checklist
**Target**: CLOCK IN: Solana Mobile Hackathon  
**Deadline**: October 8, 2026, 11:59 PM PST  
**Status**: In Progress

---

## Phase 1: Local Build & Verification ✅

### Environment Setup
- [ ] Install Android Studio (latest)
- [ ] Install Android SDK 36 (API level 36)
- [ ] Install Android Build-Tools 36.x
- [ ] Configure `local.properties` with Android SDK path
- [ ] Verify Java: `java -version` → JDK 17+
- [ ] Verify Gradle wrapper: `./gradlew --version` → 9.3.1

### Code Compilation
- [ ] Run `./gradlew clean`
- [ ] Run `./gradlew assembleDebug` → Check `app/build/outputs/apk/debug/app-debug.apk` exists
- [ ] Fix any compilation errors
- [ ] Run `./gradlew testDebugUnitTest` → All tests pass ✅

### Code Quality
- [ ] Run `./gradlew spotlessApply` (format code)
- [ ] Remove dead code and unused imports
- [ ] Verify no hardcoded API keys or secrets in source
- [ ] Review comments for clarity (no TODOs left)
- [ ] Verify package structure is clean (`com.example.*`)

---

## Phase 2: Release Build & Device Testing

### Release APK Generation
- [ ] Create or acquire Android keystore for signing
- [ ] Run `./gradlew assembleRelease` (with signing config)
- [ ] Verify APK: `app/build/outputs/apk/release/app-release.apk`
- [ ] Check APK size < 100MB
- [ ] Test installation on Android device (API 24+)

### Device Testing
- [ ] Install APK on physical Android device
- [ ] Test MWA connect (if Seed Vault available)
- [ ] Test DePIN task flow (QR scan, evidence capture)
- [ ] Test Clearance Card review (decode transaction)
- [ ] Test cooldown clock display
- [ ] Test season claims calendar
- [ ] Test receipt generation and verification
- [ ] No crashes during 5-minute usage

### Emulator Testing (Fallback)
- [ ] Launch Android Emulator (API 30+)
- [ ] Install APK: `./gradlew installDebug`
- [ ] Test core functionality (app-only, no wallet connection)
- [ ] Verify UI renders correctly at 1080p

---

## Phase 3: Demo Video Production (3 minutes)

### Pre-Production
- [ ] Script written (see DEMO_SCRIPT.md)
- [ ] Device setup: Pixel 6 or similar (1080p+)
- [ ] Screen recording tool ready (built-in Android screen record or OBS)
- [ ] Good lighting and stable camera mount

### Recording Segments (30s each, total 3 min)
1. **App Launch & MWA Connect** (30s)
   - [ ] Tap app icon → Clearance loads
   - [ ] Tap [Connect Wallet] → show wallet address
   - [ ] Display SOL and SKR balances
   - [ ] Show account name and network (Devnet)

2. **DePIN Task Hub** (30s)
   - [ ] Navigate to TASKS tab
   - [ ] Show available tasks (Helium, Grass, Hivemapper)
   - [ ] Highlight reward amounts in SKR
   - [ ] Tap [Start Task]

3. **Evidence Capture** (30s)
   - [ ] Camera opens → capture photo
   - [ ] Add notes (work description)
   - [ ] Tap QR scanner → demo scanning
   - [ ] Show captured evidence summary

4. **Clearance Card Review** (45s)
   - [ ] Navigate to CLEAR tab
   - [ ] Display decoded transaction (recipient, amount, program)
   - [ ] Show decoded data (stakes, unstakes, transfers)
   - [ ] Highlight authority warnings
   - [ ] Demo MESSAGE_CHANGED warning (if possible)
   - [ ] Show [REJECT] and [APPROVE] buttons

5. **Approval & Receipt** (30s)
   - [ ] Tap [APPROVE]
   - [ ] Show signature generation
   - [ ] Navigate to PROOFS tab
   - [ ] Display receipt with settlement status
   - [ ] Show signature verification checkmark

6. **Cooldown Clock & Season Claims** (15s)
   - [ ] Navigate to NOW tab
   - [ ] Highlight 48-hour cooldown countdown
   - [ ] Show Season claims calendar
   - [ ] Display APY and claim deadlines

### Post-Production
- [ ] Export as MP4 (1920×1080 or better)
- [ ] File size < 500MB
- [ ] No background music required
- [ ] Optional: Light narration explaining key features
- [ ] Save as: `demo.mp4` in submission folder

---

## Phase 4: Pitch Deck Creation (10 slides, PDF)

### Slide Content
- [ ] Slide 1: Title slide with tagline "Know What You Approve"
- [ ] Slide 2: Problem statement (blind signing, DePIN verification)
- [ ] Slide 3: Solution overview (Clearance Card, Review Binding, TEE)
- [ ] Slide 4: 50-feature matrix (MVP validation)
- [ ] Slide 5: Architecture diagram (MVVM, Jetpack Compose, Firebase)
- [ ] Slide 6: Security model (MESSAGE_CHANGED, TEE, mint validation)
- [ ] Slide 7: Demo highlights (key UI interactions)
- [ ] Slide 8: Market opportunity (SKR integration, DePIN scale)
- [ ] Slide 9: Timeline (Oct 8 submission, Season 1 roadmap)
- [ ] Slide 10: Call-to-action (GitHub link, open source)

### Design & Format
- [ ] Professional color scheme (Solana purple/green optional)
- [ ] Consistent font and layout across slides
- [ ] High-quality screenshots from app
- [ ] Clear, readable text (min 18pt)
- [ ] Diagrams for technical components
- [ ] One key metric/stat per slide
- [ ] Export as PDF: `pitch-deck.pdf`

---

## Phase 5: GitHub Repository Setup

### Repository Creation
- [ ] Create public repository on GitHub (github.com/username/clearance)
- [ ] Clone to local machine or initialize in existing directory
- [ ] Verify `.gitignore` includes: `.gradle/`, `build/`, `*.apk`, `local.properties`, `google-services.json`

### Git Commits
- [ ] Review all files: `git status`
- [ ] Add all files: `git add .`
- [ ] Create initial commit:
  ```
  git commit -m "Initial Clearance submission for CLOCK IN Solana Mobile Hackathon

  MVP: 24+ features shipped
  - Core 9 features: MWA connect, Clearance Card, SKR decoder, cooldown clock
  - Safety 15+: Authority warnings, mint validation, fee floor checks
  - Review Binding MESSAGE_CHANGED tamper detection
  - 48-hour cooldown + season claims calendar
  - Hardware-attested DePIN evidence (Android Keystore TEE)
  - Firebase Gemini 3.1 Pro AI audit service
  
  Co-Authored-By: Codes by Febin <codesbyfebin@gmail.com>
  Co-Authored-By: Claude Haiku 4.5 <noreply@anthropic.com>"
  ```
- [ ] Push to GitHub: `git remote add origin <url> && git push -u origin main`
- [ ] Verify repository is public (Settings → Visibility → Public)
- [ ] Add GitHub URL to submission notes

### Repository Quality
- [ ] README.md is comprehensive and up-to-date ✅
- [ ] BUILD_STATUS.md documents feature completion ✅
- [ ] Clean commit history (no merge conflicts, linear progression)
- [ ] No sensitive files (keys, credentials) in git history
- [ ] Verified that repository is searchable on GitHub

---

## Phase 6: Final Submission Package

### File Organization
```
clearance-submission/
├── app-release.apk                 (Signed release APK)
├── demo.mp4                        (3-minute demo video)
├── pitch-deck.pdf                  (10-slide presentation)
├── github-repository-link.txt      (URL)
├── submission-notes.md             (Optional: feature highlights)
└── README.md                       (Quick start guide)
```

### Submission Verification
- [ ] APK file exists and is signed
- [ ] APK size < 100MB
- [ ] Demo video is exactly 3 minutes (or close)
- [ ] Demo video is MP4 format
- [ ] Pitch deck is PDF (10 slides)
- [ ] GitHub link is public and accessible
- [ ] All files in submission folder

### Submission to Hackathon
- [ ] Visit https://solanamobile.com/hackathon
- [ ] Create account or log in
- [ ] Navigate to submission form
- [ ] Upload APK file
- [ ] Upload demo video
- [ ] Upload pitch deck
- [ ] Paste GitHub repository link
- [ ] Review submission preview
- [ ] Confirm and submit before Oct 8, 11:59 PM PST
- [ ] Receive confirmation email

---

## Phase 7: Post-Submission

### Follow-Up
- [ ] Monitor email for hackathon updates
- [ ] Check GitHub for any issues reported
- [ ] Prepare for potential final presentation (if selected)
- [ ] Document lessons learned for future improvements

### Nice-to-Have (Time Permitting)
- [ ] Record supplementary technical deep-dive video (5 min)
- [ ] Create blog post explaining Review Binding pattern
- [ ] Set up CI/CD pipeline (GitHub Actions)
- [ ] Add unit test coverage badges to README
- [ ] Create API documentation for services

---

## Timeline

| Phase | Task | Deadline |
|-------|------|----------|
| 1 | Local build, compilation, unit tests | Oct 5 |
| 2 | Release APK, device testing | Oct 5 |
| 3 | Demo video recording & editing | Oct 6 |
| 4 | Pitch deck creation | Oct 6 |
| 5 | GitHub repo setup & push | Oct 7 |
| 6 | Final submission package assembly | Oct 7 |
| 7 | Submit to hackathon | Oct 8 before 11:59 PM PST |

---

## Key Contacts & Resources

- **Hackathon Portal**: https://solanamobile.com/hackathon
- **Solana Mobile Docs**: https://solanamobile.com/developers
- **GitHub Issues**: Report bugs during submission prep
- **Android Studio Help**: https://developer.android.com/studio/intro
- **Firebase Docs**: https://firebase.google.com/docs

---

## Notes

- **Confidentiality**: Do not share APK publicly before submission (GitHub repos are OK)
- **Intellectual Property**: Ensure all code is original or properly attributed
- **Testing**: Test on at least one device (emulator OK as fallback)
- **Backup**: Keep local copy of all submission files

---

**Submission Package Owner**: Codes by Febin  
**Contact**: codesbyfebin@gmail.com  
**Last Updated**: Oct 4, 2026
