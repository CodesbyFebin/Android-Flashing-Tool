# Android Flashing Tool — local device operations

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
