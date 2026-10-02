# Security Policy

## Reporting a Vulnerability

**DO NOT** create a public GitHub issue for security vulnerabilities. Public issues allow attackers to see and potentially exploit discovered vulnerabilities before they can be fixed.

### How to Report

If you discover a security vulnerability in Android Flashing Tool, please email security concerns to: **codesbyfebin@gmail.com**

Please include in your report:
1. Description of the vulnerability
2. Steps to reproduce (if applicable)
3. Affected versions
4. Potential impact
5. Suggested fix (if you have one)

### Response Timeline

We will:
- **Acknowledge** receipt of your report within 48 hours
- **Provide** an initial assessment within 1 week
- **Fix** and release a patch within 30 days of confirmation (or provide a timeline if longer is needed)
- **Credit** you in release notes if you wish

### Responsible Disclosure

We ask that you:
- Do not share the vulnerability publicly before a fix is released
- Do not attempt to access others' devices or data
- Do not perform testing on systems without permission
- Provide reasonable time for us to develop and release a fix

## Security Considerations

### What This Tool Is Designed For

✅ **Security Features:**
- Ed25519 cryptographic signature verification
- SHA-256 hash validation
- Signed operation evidence
- No cloud connectivity or telemetry
- Localhost-only access (no network exposure)
- Explicit user confirmations required
- 10-point pre-flash safety gates
- Crash recovery with journaling
- Transparent, auditable code

### What This Tool Does NOT Provide

❌ **Not Provided:**
- OEM verification or approval
- Post-boot verification
- Full anti-rollback verification  
- Automatic device backup
- Device root provisioning
- Automatic recovery from boot failures
- Hardware security module integration

### Prerequisites for Safe Use

Users should understand:
1. **Device Knowledge**: Know your device model, bootloader version, and current firmware
2. **Backup First**: Always backup critical data before flashing
3. **Bootloader Unlock**: This requires consciously unlocking your bootloader
4. **Firmware Review**: Carefully review and understand firmware before flashing
5. **Risk Acknowledgment**: Understand that incorrect firmware can prevent boot
6. **Offline Verification**: Download and verify firmware through trusted channels independently

## Cryptographic Security

### Signature Verification

- **Algorithm**: Ed25519 (EdDSA)
- **Key Size**: 256-bit private keys
- **Signature Format**: Base64-encoded binary signatures
- **Manifest**: JSON with canonical form (sorted keys, compact separators)

### Hash Validation

- **Algorithm**: SHA-256
- **Validation**: All partition images verified against declared hashes
- **Archive Integrity**: ZIP file hash validated before extraction

### Key Management

- **Publisher Keys**: Stored in `~/.dh-flash/trusted-keys/`
- **Signer Key**: Locally generated Ed25519 key
- **No Rotation**: Current implementation does not support key rotation
- **Manual Installation**: Publisher keys must be installed manually outside the application

## Implementation Security

### Input Validation

- ZIP file structure validation (no path traversal)
- JSON size limits (64KB per request)
- Firmware size limits (4GB max)
- Partition name validation (regex: `boot|init_boot|vendor_boot|dtbo|vbmeta|recovery(_[ab])?`)

### Safe Defaults

- **Inspection Mode Only**: Execution disabled by default (`DH_FLASH_ENABLE_EXECUTION=0`)
- **Localhost Only**: 127.0.0.1:8765 only, no network exposure
- **Session Tokens**: Ephemeral tokens required for API access
- **Approval Expiration**: Plans expire in 5 minutes, approvals in 1 minute
- **First-Failure Abort**: Stops operation on first error

### Atomicity & Recovery

- **Atomic Writes**: Firmware extraction to temporary directory first
- **Fsync**: Evidence records flushed and synced to disk
- **Recovery Journal**: Tracks pending operations for crash recovery
- **No Resume**: Failed operations must be reviewed and restarted manually

## Dependencies

### Python Libraries
- **cryptography**: 50.0.2+ - Cryptographic operations
- **Built-in only**: subprocess, json, zipfile, hashlib, pathlib, threading

### Runtime Requirements
- **Python**: 3.10+
- **ADB/Fastboot**: From Android SDK Platform-Tools
- **No Network**: Project has zero external dependencies at runtime

### Verification

Dependencies are minimal and specified in `requirements.txt`:
```
cryptography>=50.0.2
```

## Supported & Unsupported Operations

### Explicitly Supported
- ✅ Raw image flashing for: boot, init_boot, vendor_boot, dtbo, vbmeta, recovery
- ✅ Optional `_a`/`_b` slot selection
- ✅ Signed firmware package verification
- ✅ Partition size validation
- ✅ Device property verification

### Explicitly NOT Supported (by design)
- ❌ Sparse images (security risk)
- ❌ Payload extraction
- ❌ Dynamic partitions
- ❌ Odin TAR format
- ❌ Automatic wipe/relock/reboot
- ❌ Root provisioning
- ❌ OEM-specific recovery

## Testing & Validation

The project includes 16 comprehensive unit tests covering:
- ✅ Package signature verification
- ✅ Archive integrity checks
- ✅ Path traversal protection
- ✅ Device gate validation
- ✅ Approval expiration
- ✅ Evidence signing
- ✅ Crash recovery
- ✅ Lock management
- ✅ Tampered image detection
- ✅ Untrusted signer detection

**Note**: Tests run without physical devices attached. Actual USB/Fastboot behavior remains untested.

## Known Limitations

1. **Anti-Rollback**: Does not perform full AVB anti-rollback verification
2. **Boot Verification**: Does not verify successful device boot after flashing
3. **OEM Approval**: Does not establish OEM trust or verification
4. **Device Recovery**: Does not provide automatic recovery from boot failures
5. **Concurrent Operations**: Not tested with multiple parallel devices

## Security Audit

This is a **tested implementation**, not hardware-qualified universal flashing software. Users requiring:
- Strict compliance verification
- Third-party security audit
- OEM approval
- Production deployment certification

Should consider engaging professional security services for independent review.

## Contact

For security inquiries or responsible disclosure:
- **Email**: codesbyfebin@gmail.com
- **GitHub Issues**: Do NOT create public issues for security vulnerabilities
- **Response Time**: We aim to respond within 48 hours

---

**Last Updated**: October 2, 2026  
**Policy Version**: 1.0

This security policy will be updated as the project evolves. Check back regularly for updates.
