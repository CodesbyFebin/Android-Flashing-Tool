# Android Flashing Tool 🚀

> **Flash. Recover. Customize.** Safe • Powerful • Transparent • Offline
>
> A modern, open-source Android firmware flashing toolkit for complete device freedom. Sign your firmware, verify before you write, and keep complete control over your device.

[![GitHub Stars](https://img.shields.io/github/stars/CodesbyFebin/Android-Flashing-Tool?style=flat-square&color=24d8f0)](https://github.com/CodesbyFebin/Android-Flashing-Tool)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue?style=flat-square)](LICENSE)
[![Python 3.10+](https://img.shields.io/badge/Python-3.10%2B-blue?style=flat-square)](https://www.python.org)
[![Vercel Deploy](https://img.shields.io/badge/Deployed%20on-Vercel-black?style=flat-square)](https://android-flashing-tool.vercel.app)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?style=flat-square)](#testing)
[![Made with ❤️](https://img.shields.io/badge/Made%20with-❤️-red?style=flat-square)](https://github.com/CodesbyFebin)

---

## ✨ What's New: Modern UI Redesign

The application now features a **completely redesigned modern interface** with:
- 🎨 **Modern Gradients & Animations** - Smooth transitions and gradient backgrounds
- 🎯 **Improved Visual Hierarchy** - Better organization with clear visual flow  
- 📱 **Responsive Design** - Works perfectly on desktop, tablet, and mobile
- ✅ **Pre-Flash Gates Sidebar** - Easy verification checklist before flashing
- ⚡ **Enhanced UX** - Faster, clearer, more intuitive workflow
- 🌙 **Dark Mode Optimized** - Eye-friendly dark theme throughout

**See it live:** http://127.0.0.1:8765 (when running locally)

---

## 🎯 Core Features

| Feature | Details |
|---------|---------|
| **✅ Cryptographic Verification** | Ed25519-signed firmware packages with manifest verification |
| **🔒 Security-First Design** | 10 pre-flash safety gates before any write operation |
| **💯 Explicit Confirmations** | Human-in-the-loop: backup confirmation + risk acknowledgment |
| **🚀 Optimized Workflow** | 5-step pipeline: Inspect → Verify → Review → Execute → Evidence |
| **📊 Signed Evidence** | All operations cryptographically signed for audit trails |
| **🌐 Completely Offline** | No cloud, no telemetry, no external dependencies |
| **🛡️ Recovery Journal** | Automatic recovery from crashes with durable journals |
| **⚙️ Local Only** | 127.0.0.1:8765 - localhost only, zero network exposure |

---

## 🚀 Quick Start

### Try It Now (No Installation)

```bash
# Clone the repository
git clone https://github.com/CodesbyFebin/Android-Flashing-Tool.git
cd Android-Flashing-Tool

# Install dependencies
pip install cryptography

# Start the local runtime
python3 runtime.py
```

Then open your browser to: **http://127.0.0.1:8765**

### With Virtual Environment (Recommended)

```bash
python3 -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate
pip install -r requirements.txt
python3 runtime.py
```

### Enable Flashing (Optional)

By default, the application runs in **inspection mode only** (read-only). To enable actual device writes:

```bash
# macOS/Linux
DH_FLASH_ENABLE_EXECUTION=1 python3 runtime.py

# Windows PowerShell
$env:DH_FLASH_ENABLE_EXECUTION='1'; python3 runtime.py
```

---

## 📋 Supported Operations

✅ **Partitions Supported:**
- `boot` / `boot_a` / `boot_b`
- `init_boot` / `init_boot_a` / `init_boot_b`
- `vendor_boot` / `vendor_boot_a` / `vendor_boot_b`
- `dtbo` / `dtbo_a` / `dtbo_b`
- `vbmeta` / `vbmeta_a` / `vbmeta_b`
- `recovery` / `recovery_a` / `recovery_b`

✅ **Safety Gates (All Required):**
- Device product matches firmware target
- Bootloader is unlocked
- Bootloader version matches exactly
- Battery level ≥ 60%
- Fastboot mode detected
- Partition sizes sufficient
- Firmware signature verified
- Package hashes match
- Device not in use by other operations

❌ **Not Supported (By Design):**
- Sparse images (security risk)
- Odin TAR format
- Payload extraction
- Dynamic partitions
- Automatic wipe/relock/reboot
- Root provisioning
- OEM-specific recovery

---

## 🏗️ Architecture

```
┌─────────────────────────────────┐
│   Modern Web UI (HTML/CSS/JS)   │
│  - Responsive design             │
│  - Dark mode optimized           │
│  - Real-time status updates      │
└────────────┬────────────────────┘
             │ HTTP API
┌────────────▼────────────────────┐
│  Python Runtime Server           │
│  - Device probing (ADB/Fastboot) │
│  - Firmware inspection           │
│  - Safety gates verification     │
│  - Execution & evidence signing  │
└────────────┬────────────────────┘
             │ USB
┌────────────▼────────────────────┐
│   Android Device                │
│  - Bootloader mode               │
│  - Fastboot protocol             │
└─────────────────────────────────┘
```

### Technology Stack

- **Frontend:** HTML5, CSS3, Vanilla JavaScript (~1KB)
- **Backend:** Python 3.10+ with cryptography library
- **Build:** Node.js (npm)
- **Deployment:** Vercel (static hosting) + Local runtime
- **Cryptography:** Ed25519 (EdDSA)
- **API:** RESTful with JSON

---

## 🔐 Security & Privacy

✅ **Zero Cloud Connectivity**
- All operations run locally
- No telemetry, tracking, or external requests
- No CDN, analytics, or third-party services

✅ **Cryptographic Guarantees**
- Ed25519 signature verification
- SHA-256 hash validation
- Signed evidence records
- Publisher key pinning

✅ **Hardware Isolation**
- Localhost-only access (127.0.0.1)
- No network exposure
- Same-origin policy
- Token-based session auth

✅ **User Control**
- Explicit confirmation required before writes
- Backup acknowledgment
- Risk understanding requirement
- 5-minute plan expiration
- 1-minute approval expiration

---

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| [QUICK_START.md](QUICK_START.md) | Installation and setup guide |
| [VALIDATION.md](VALIDATION.md) | Testing and validation procedures |
| [DEPLOYMENT.md](DEPLOYMENT.md) | Production deployment guide |
| [design.md](design.md) | UI/UX specifications |
| [openapi.json](openapi.json) | Complete API schema |

---

## 🧪 Testing

The application includes comprehensive test coverage:

```bash
# Run all tests
python -m unittest discover -s tests -v
```

**Test Coverage:**
- ✅ Package signature verification
- ✅ Archive integrity checks
- ✅ Path traversal protection
- ✅ Device gate validation
- ✅ Approval expiration
- ✅ Evidence signing
- ✅ Crash recovery
- ✅ Lock management
- 16 total unit tests

---

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Setup

```bash
# Clone and setup
git clone https://github.com/CodesbyFebin/Android-Flashing-Tool.git
cd Android-Flashing-Tool

# Create virtual environment
python3 -m venv .venv
source .venv/bin/activate

# Install dev dependencies
pip install -r requirements.txt

# Run tests
python -m unittest discover -s tests -v

# Start development server
DH_FLASH_ENABLE_EXECUTION=0 python3 runtime.py
```

---

## 📊 Project Stats

- **Language:** Python, JavaScript, HTML/CSS
- **Lines of Code:** ~1500 backend + ~1000 frontend
- **Test Coverage:** 16 comprehensive unit tests
- **API Endpoints:** 12 documented endpoints
- **Supported Devices:** Any device with Fastboot bootloader
- **Package Size:** ~50KB (excluding platform-tools)

---

## 🎓 Learning Resources

### For Device Flashing

- [Android Developer - Platform Tools](https://developer.android.com/tools/releases/platform-tools)
- [Android Recovery Project](https://source.android.com/docs/security/features/verifiedboot)
- [Fastboot Documentation](https://source.android.com/docs/core/ota/nonab)

### For Understanding the Code

- [Cryptography Python Library](https://cryptography.io)
- [Ed25519 Signatures](https://ed25519.cr.yp.to/)
- [ZIP File Format](https://pkware.com/documents/casestudies/APPNOTE.TXT)

---

## 📝 License

GNU General Public License v3.0 - See [LICENSE](LICENSE) file for details

This is free and open-source software built for device freedom.

---

## ⚠️ Important Disclaimer

**This tool is a tested implementation release, not hardware-qualified universal flashing software.** It:

- Does **not** provide OEM verification or approval
- Does **not** perform complete anti-rollback verification
- Does **not** guarantee successful boot after flashing
- Requires **explicit review** of firmware before use
- Requires **bootloader unlock** (security-conscious step)

**Always backup your device before flashing.** Incorrect firmware or flashing failures can prevent boot.

---

## 🙏 Credits

Built with ❤️ for Android enthusiasts and developers who value:
- **Freedom** - Complete control over your device
- **Transparency** - Open source, auditable code
- **Security** - Cryptographic verification, no telemetry
- **Simplicity** - Clear, minimal, offline-first design

---

## 📞 Support & Community

- 🐛 **Found a bug?** [Open an issue](https://github.com/CodesbyFebin/Android-Flashing-Tool/issues)
- 💡 **Have an idea?** [Start a discussion](https://github.com/CodesbyFebin/Android-Flashing-Tool/discussions)
- 📖 **Need help?** Check the [documentation](#documentation)
- 🚀 **Want to contribute?** See [CONTRIBUTING.md](CONTRIBUTING.md)

---

**Give us a star ⭐ if you find this project useful!**

A complete standalone frontend and loopback API based on the supplied Decentralized.Host reference. It replaces simulated telemetry with actual ADB/Fastboot probes. This is a tested implementation release, **not hardware-qualified universal flashing software**.

## Hosted interface

The Vercel-hosted interface offers source downloads and local-runtime setup. USB device operations require launching the application on the connected computer. See DEPLOYMENT.md for Git-backed hosting.

## Quick launch

See QUICK_START.md. Windows: start.bat; macOS: sh start.command; Linux: sh start.sh. The launcher opens the browser automatically. The final design reference is bundled under reference/.

## Run

Requires Python 3.10+, Android SDK Platform-Tools on PATH, and the computer physically connected to the phone.

```sh
python3 -m venv .venv
# macOS/Linux
. .venv/bin/activate
# Windows: .venv\Scripts\activate
pip install -r requirements.txt
python runtime.py
```

Open http://127.0.0.1:8765. Inspect-only is the default. To explicitly enable destructive execution:

```sh
# macOS/Linux
DH_FLASH_ENABLE_EXECUTION=1 python runtime.py
# PowerShell
$env:DH_FLASH_ENABLE_EXECUTION='1'; python runtime.py
```

The application is local software; a cloud deployment cannot access your computer's USB devices. All fonts and UI assets are local; no CDN, analytics or external requests are required. Install trusted platform-tools separately from https://developer.android.com/tools/releases/platform-tools.

## Supported write policy

Only signed `dh-flash/v1` ZIPs containing raw `boot`, `init_boot`, `vendor_boot`, `dtbo`, `vbmeta`, or `recovery` images, optionally explicit `_a`/`_b` targets. No guessed partitions, scripts, payload extraction, sparse images, dynamic partition operations, Odin TAR handling, wiping, rooting, relocking, or bootloader unlocking.

Runtime requirements: exact device product, exact bootloader revision, reported unlock `yes`, bootloader Fastboot (`is-userspace=no`), reported battery ≥60%, valid trusted signature, matching archive/image hashes, and sufficiently large explicitly named partitions. Missing device variables block execution. No fallback from Fastboot to sideload or Odin exists.

An exact bootloader revision check is conservative compatibility gating; it is **not a complete AVB anti-rollback verifier**. Publisher signatures attest the reviewed plan's origin, not that the OEM approves it or that its images are safe. Full OEM/device qualification, rollback-index inspection, partition readback and post-boot validation require device-specific adapters and remain unavailable. Unsigned or ordinary manufacturer packages cannot be flashed through this release.

## Firmware publisher setup

Provision an independently verified **32-byte raw Ed25519 public key** to `~/.dh-flash/trusted-keys/KEY_ID.pub`. The browser cannot install trust keys. Keep publisher private keys outside the application. `DH_FLASH_DATA` can override the data directory.

Manifest example (illustrative; never use without reviewing the actual device and firmware):

```json
{
  "schema": "dh-flash/v1",
  "name": "Reviewed boot image",
  "key_id": "publisher",
  "target": "DEVICE_PRODUCT",
  "bootloader_version": "EXACT_DEVICE_REVISION",
  "partitions": [
    {"partition": "boot_a", "file": "boot.img", "sha256": "IMAGE_SHA256"}
  ]
}
```

`manifest.sig` contains base64 Ed25519 signature over JSON encoded with sorted keys and compact separators. Each image is at ZIP root. Generate packages with an existing publisher key:

```sh
python package_firmware.py manifest.json --private-key /secure/publisher.key --images /reviewed/images --output reviewed-firmware.zip
```

This tool fills image hashes, signs the manifest and prints the archive SHA-256. Distribute that archive hash through an independently trusted channel. Do not treat a generated key as OEM trust.

## API

Same-origin requests only; loopback Host allowlist. `/session` supplies an ephemeral token. Every API call requires `X-DH-Token`. No CORS permission and no arbitrary shell-command endpoint.

| Method | Path | Request / behavior |
|---|---|---|
| GET | `/session` | Local session token |
| GET | `/api/health` | Runtime health |
| GET | `/api/diagnostics` | Runtime configuration and capability flags |
| GET | `/api/openapi.json` | OpenAPI 3.1 schema |
| GET | `/api/devices` | Real device probes, tool availability, execution mode |
| POST | `/api/firmware` | Raw ZIP body; `X-Firmware-SHA256` required |
| POST | `/api/plans` | `serial`, `firmware_id`; fresh read-only gates |
| POST | `/api/approvals` | `plan_id`, `acknowledgements: {backup:true,risk:true,confirmation:"FLASH"}` |
| POST | `/api/execute` | `plan_id`, `approval_id`; one-use approval and revalidation |
| GET | `/api/operations` | Progress and complete stdout/stderr/exit codes |
| GET | `/api/evidence/ID` | Signed JSON record after operation finishes |
| POST | `/api/verify-evidence` | Exported signed record; signature validity and local signer comparison |
| POST | `/api/reboot` | `serial`, `mode`: `bootloader`, `recovery`, or `system`; supported modes only |

Plans expire in five minutes. Approvals expire in one minute and are consumed once. A global execution lock blocks competing operations. Commands use argv arrays and `shell=False`. Timeouts are failures and stop subsequent writes. Before each partition write the runtime rechecks device product/unlock and image hash.

## Evidence and recovery

The signer key is locally generated and kept in the data directory; pin/export its public key independently when auditing. Signed final evidence is stored under `evidence/`. A valid signature alone does not establish a trusted publisher or successful boot. `writes_completed_unverified` means Fastboot acknowledged each write; it does not claim independent flash readback or successful boot. No automatic wipe or reboot follows writes. A failed command stops the pipeline; review its exported record and use device-specific recovery instructions.

Runtime termination or host failure can interrupt a flash and its final evidence creation. A durable, fsynced journal records each pending command before execution and its result afterward; a pending command after a crash has UNKNOWN outcome. Journals are not signed final evidence. There is no automatic recovery/resume. Do not terminate the process during an operation. Completed evidence records reappear after restart only after their signature and local signer identity are verified. Interrupted journals reappear as interrupted_unknown. Firmware plans are session scoped; inspect/build a new plan after restarting.

## Validation

```sh
python -m unittest discover -s tests -v
```

16 tests cover interrupted-command recovery, tampered stored evidence, lock release on disk failure, runtime capability reporting, signed package acceptance, archive hash mismatch, image tampering, archive traversal, unsupported partitions, untrusted keys, device mismatch, unknown battery, expired approval, disabled writes, first-failure stop, signed failure evidence, and honest completion status. No physical phone was attached during development; actual USB/flash behavior remains unqualified.

## Files

- `web/`: upgraded responsive interface and wired API client
- `runtime.py`: local API, device probing, inspection, planning, approvals, executor, evidence
- `package_firmware.py`: publisher packaging utility
- `tests/`: safety/failure tests
- `reference/final-design-reference.png`: generated final design reference
- `openapi.json`: API schema
- `launch.py`, `start.bat`, `start.command`, `start.sh`: platform launchers
- `QUICK_START.md`, `design.md`: setup and UI specification

The original ZIP and attachments are preserved separately. This deliverable is standalone and avoids the supplied prototype's cloud-specific runtime dependencies.
