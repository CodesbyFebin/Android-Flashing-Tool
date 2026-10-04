# Clearance Demo Video Script (3 Minutes)
**Target Duration**: 180 seconds  
**Format**: MP4, 1920×1080 (or better), no audio compression  
**Style**: Continuous walkthrough with smooth transitions, minimal cuts

---

## Segment 1: App Launch & MWA Wallet Connect (0:00-0:30)

### Setup
- Device: Pixel 6+ or similar (landscape or portrait, consistent)
- Screen brightness: 100%
- Font size: Android Large or XL (for readability)
- Wallet: Seed Vault installed with test account pre-loaded

### Script
**Visual Flow** (no narration required, but optional):
1. (0:00) Tap Clearance app icon on home screen
2. (0:03) App logo/splash screen appears
3. (0:05) Main screen loads, showing tab navigation: `TASKS | NOW | CLEAR | PROOFS`
4. (0:08) Tap blue [Connect Wallet] button
5. (0:10) Mobile Wallet Adapter dialog appears: "Clearance wants to connect to Solana"
6. (0:12) Tap [Authorize] in wallet (Seed Vault)
7. (0:15) Wallet returns to app with connected state
8. (0:18) Display connected account:
   - Wallet address: `9B2T...7xK9` (masked)
   - Network: **Devnet**
9. (0:22) Show balance section:
   - **SOL**: 2.5 SOL
   - **SKR (Liquid)**: 1,000 SKR
   - **SKR (Staked)**: 500 SKR
   - **SKR (Cooling)**: 200 SKR (48h countdown active)
10. (0:30) Hold on balance display

**Optional Narration** (if desired):
> "Clearance connects securely to Solana Mobile Seeker wallet. You always see your exact balances: liquid SOL and SKR, staked positions, and any SKR currently cooling down after unstaking."

---

## Segment 2: DePIN Task Hub (0:30-1:00)

### Setup
- Ensure TASKS tab visible in navigation

### Script
**Visual Flow**:
1. (0:30) Tap TASKS tab
2. (0:33) Task list appears, showing:
   - **Task 1**: "Helium Network Hotspot Check-In"
     - Reward: 15 SKR
     - Status: Available
   - **Task 2**: "Grass Node Bandwidth Proof"
     - Reward: 12 SKR
     - Status: Available
   - **Task 3**: "Hivemapper Photo Route Verification"
     - Reward: 20 SKR
     - Status: Available
3. (0:42) Swipe or scroll to show more tasks
4. (0:48) Tap on **Task 1** (Helium)
5. (0:50) Task detail screen opens:
   - Task ID (QR code shown)
   - Description: "Verify hotspot uptime on Devnet"
   - Reward amount: 15 SKR
   - Estimated duration: 5-10 minutes
   - Status: In Progress
6. (0:55) Show [Start Task] or [Continue] button
7. (1:00) Hold on task detail

**Optional Narration** (if desired):
> "The TASKS tab is your DePIN hub. Choose from available tasks across Helium, Grass, and Hivemapper. Each task shows the exact SKR reward, duration, and status. You earn real SKR for completed work, verifiable on-chain."

---

## Segment 3: Evidence Capture (1:00-1:30)

### Setup
- Ensure camera permissions are enabled
- Pre-position device for comfortable photo capture

### Script
**Visual Flow**:
1. (1:00) Tap [Capture Evidence] button (or similar)
2. (1:03) Camera app opens in-app or via native camera
3. (1:05) Frame a simple scene (desk, document, or test pattern)
4. (1:08) Tap shutter button → photo captured
5. (1:10) Return to task screen with photo thumbnail visible
6. (1:12) Tap [Add Notes] field
7. (1:14) Keyboard opens, type sample note:
   - "Helium hotspot online, signal strength -87dBm, last report 2:15 PM UTC"
8. (1:20) Show note text in field
9. (1:22) Tap [QR Scanner] button (or camera icon for QR)
10. (1:24) QR scanner opens
11. (1:26) Point at pre-generated QR code (or sample QR)
12. (1:28) QR decoded → Task ID confirmed in field
13. (1:30) Evidence summary screen shows:
    - Photo thumbnail
    - Note text
    - QR task ID
    - SHA-256 digest (preview): `3a7f2e...`

**Optional Narration** (if desired):
> "Capture evidence for your work: photos, notes, and task QR codes. Clearance hashes the evidence into a SHA-256 fingerprint and will anchor it on-chain, creating an immutable record of what you did and when."

---

## Segment 4: Clearance Card Review (1:30-2:15)

### Setup
- Navigate to CLEAR tab
- Have a pre-loaded transaction ready (or mock transaction)

### Script
**Visual Flow**:
1. (1:30) Tap CLEAR tab
2. (1:33) Clearance Card screen loads, showing a transaction preview:
   - **Title**: "Review Transaction"
   - **Recipient**: `DePINVault...3x9K` (masked)
   - **Amount**: 15 SKR
   - **Network**: Devnet
   - **Program**: Solana Token Program (verified ✅)
3. (1:42) Scroll down on card to reveal decoded details:
   - Instruction type: `TransferChecked`
   - Token mint: `SKRskrY...YfK84` (matches official SKR) ✅
   - Authority: No delegation changes
   - Warnings: None (green checkmark)
4. (1:50) Highlight a key field: **"No Authority Changes Detected"**
5. (1:54) Scroll further to show signature summary
6. (2:00) Tap [DEMO MESSAGE_CHANGED] button (if available):
   - Error dialog appears: **"MESSAGE_CHANGED: Transaction altered after user review!"**
   - Show dismissal of dialog
7. (2:06) Return to normal card state
8. (2:08) Show two action buttons at bottom:
   - [REJECT] button (red)
   - [APPROVE] button (blue/green)
9. (2:12) Tap [REJECT] to demo single-tap rejection
10. (2:15) Hold on rejection confirmation

**Optional Narration** (if desired):
> "The Clearance Card breaks down every transaction into human language. You see the exact amount, recipient, and any warnings. Review Binding ensures if anyone tries to alter this transaction after you review it, Clearance halts execution immediately. You can reject any transaction with one tap—no second prompt."

---

## Segment 5: Approval & Receipt (2:15-2:45)

### Setup
- Return to CLEAR tab (or continue from rejection screen)
- Prepare to show approval flow

### Script
**Visual Flow**:
1. (2:15) Go back to Clearance Card (or show a fresh transaction)
2. (2:18) Tap [APPROVE] button
3. (2:20) Approval confirmation dialog:
   - Message: "Confirm approval to sign with Seed Vault?"
   - [Cancel] and [Confirm] buttons
4. (2:24) Tap [Confirm]
5. (2:26) Mobile Wallet Adapter dialog appears (Seed Vault sign request)
6. (2:28) Tap [Sign] in wallet
7. (2:30) Transaction signed, returns to app
8. (2:32) Navigate to PROOFS tab (or auto-navigate)
9. (2:35) PROOFS screen shows receipt:
   - **Receipt ID**: `receipt_001`
   - **Status**: ✅ Confirmed (or "Finalized on Devnet")
   - **Amount**: 15 SKR
   - **Recipient**: `DePINVault...3x9K`
   - **Timestamp**: `Oct 4, 2026 - 11:42 AM UTC`
   - **Signature**: `3a7f2e1d...c8f9` (truncated)
   - **Verification**: ✅ Signature Valid
10. (2:42) Tap [Export JSON] button (if available)
11. (2:44) Show export confirmation
12. (2:45) Hold on receipt

**Optional Narration** (if desired):
> "Approve the transaction via your Seed Vault. The signature is verified cryptographically. Your receipt shows the exact settlement details and verification checksum. Export receipts as JSON for record-keeping or third-party verification."

---

## Segment 6: Cooldown Clock & Season Claims (2:45-3:00)

### Setup
- Navigate to NOW tab
- Ensure 48-hour cooldown is visible

### Script
**Visual Flow**:
1. (2:45) Tap NOW tab (Home/Dashboard)
2. (2:48) Dashboard loads with top section showing:
   - **Active Cooldown Clock**: `47h 52m remaining` (red/urgent color)
   - **Reason**: "SKR unstaking cooldown (48h epoch lock)"
   - **Subtitle**: "Rewards stopped immediately upon unstaking"
3. (2:55) Scroll down to reveal Season Claims section:
   - **Season 1 Rewards Available**: `25 SKR`
   - **Claim Deadline**: `45 days remaining` (green checkmark)
   - **Fee Floor**: `0.015 SOL required`
   - **Status**: `✅ Claimable (you have 2.5 SOL)`
4. (3:00) Tap [Claim Now] button or hold on Season Claims

**Optional Narration** (if desired):
> "The home screen shows your open cooldowns and pending claims. Your 48-hour SKR cooldown from unstaking is displayed prominently—rewards stopped immediately, and your tokens will be available to re-stake in 47 hours. Your Season 1 rewards are claimable, and you have plenty of SOL to cover the 0.015 SOL claim fee."

---

## Video Production Tips

### Recording Tools
- **Built-in Android**: Settings → About → Developer Options → Enable screen record
- **Third-party**: OBS Studio (free, multi-platform)
- **Resolution**: 1920×1080 (Full HD) minimum
- **Frame rate**: 30 or 60 fps

### Best Practices
1. **Frame rate**: Consistent 30fps (smoother than 24fps, lower file size than 60fps)
2. **Lighting**: Well-lit room, no shadows on screen
3. **Audio**: If narrating, use good microphone; background noise filtering OK
4. **Timing**: Rehearse segments to fit 30-second blocks; use clock marks
5. **Smooth transitions**: Wait 1-2 seconds at end of each segment before transitioning
6. **Zoom**: Do not use pinch-zoom unless showcasing specific UI detail
7. **Colors**: Ensure high contrast for readability (especially text)

### Editing (Optional)
- **Trim excess footage**: Cut the start/end of each segment if needed
- **Audio**: Add optional background music (royalty-free) at low volume
- **Subtitles**: Optional captions for accessibility
- **Title card**: Optional opening slide with "Clearance - Know What You Approve"
- **Closing card**: Optional closing slide with GitHub link

### File Export
- **Format**: MP4 (H.264 codec recommended)
- **Resolution**: 1920×1080 or better
- **Bitrate**: 5-10 Mbps (balance quality and file size)
- **Target file size**: < 500MB
- **Duration**: Aim for 2:55-3:05 (exactly 3 minutes)

### Verification Before Submission
1. Play video in VLC or web browser → verify playback quality
2. Check sound levels (if narrated)
3. Verify all key UI elements are visible and readable
4. Confirm video duration is correct
5. Check file size
6. Upload to cloud storage (Google Drive, Dropbox) as backup

---

## Backup Plan (If Live Demo Not Possible)

If you cannot record a live demo on device:

1. **Android Emulator Recording**: Start Android Emulator, record interactions, export video
2. **Screen Capture**: Use Android Studio's screen record feature
3. **Mock Screenshots**: Create a video slideshow using screenshots + transitions (less ideal, but acceptable)

**Note**: Live device recording is strongly preferred to show smooth interactions and real-time behavior.

---

## Timing Reference

| Timestamp | Segment | Duration |
|-----------|---------|----------|
| 0:00–0:30 | App Launch & MWA Connect | 30s |
| 0:30–1:00 | DePIN Task Hub | 30s |
| 1:00–1:30 | Evidence Capture | 30s |
| 1:30–2:15 | Clearance Card Review | 45s |
| 2:15–2:45 | Approval & Receipt | 30s |
| 2:45–3:00 | Cooldown Clock & Season Claims | 15s |
| **Total** | | **180s (3 min)** |

---

## Optional Enhancements (If Time Allows)

1. **Voice Narration**: Record soft narration explaining each feature (see "Optional Narration" above)
2. **Opening Card**: "Clearance — Know What You Approve" (2s slide)
3. **Closing Card**: GitHub link and "Submit to CLOCK IN: Solana Mobile Hackathon" (2s slide)
4. **Slow-motion**: Play transaction approval in slow-motion to emphasize security
5. **Zoom/Pan**: Highlight specific UI details with subtle zoom
6. **Music**: Royalty-free background music at 30-40% volume (optional)
7. **Captions**: Add text labels for key features (optional for accessibility)

---

## Contact & Resources

- **Demo Video Format Guide**: https://solanamobile.com/hackathon/submission-guide
- **Screen Recording Help**: https://support.google.com/android/answer/9075928
- **OBS Studio**: https://obsproject.com
- **Video Compression**: https://handbrake.fr

---

**Script Version**: 1.0  
**Last Updated**: Oct 4, 2026  
**Creator**: Codes by Febin for Clearance Hackathon Submission
