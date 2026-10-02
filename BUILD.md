# Building Android Flashing Tool

Complete guide for building standalone applications for Windows, macOS, and Linux.

## Overview

The Android Flashing Tool can be built into standalone executables for all major platforms:
- **Windows:** `.exe` (with optional NSIS installer)
- **macOS:** Universal `.dmg` (Intel + Apple Silicon)
- **Linux:** `.AppImage` (runs on all distributions)

## Prerequisites

### All Platforms
- **Python 3.10 or higher**
- **pip** (Python package manager)
- **git** (for version control)

### Windows
```bash
# Install Python from: https://www.python.org/downloads/
# Make sure to check "Add Python to PATH" during installation

# Install build tools
pip install pyinstaller

# Optional: For NSIS installer
# Download from: https://nsis.sourceforge.io/
```

### macOS
```bash
# Install Xcode Command Line Tools
xcode-select --install

# Install Homebrew (if not installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install build tools
pip install pyinstaller

# Optional: For DMG creation
brew install create-dmg
```

### Linux
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install python3.10 python3-pip build-essential libfuse2

# Fedora/RHEL
sudo dnf install python3 python3-pip gcc

# Install build tools
pip install pyinstaller

# Optional: For AppImage creation
pip install appimage-builder
```

---

## Quick Start

### Windows
```bash
# Double-click build.bat
# Or from PowerShell:
.\build.bat

# Output: dist\android-flashing-tool.exe
```

### macOS/Linux
```bash
# Make build script executable
chmod +x build.sh

# Run build script
./build.sh

# Output: dist/android-flashing-tool
```

---

## Detailed Build Instructions

### Step 1: Setup Development Environment

```bash
# Clone the repository
git clone https://github.com/CodesbyFebin/Android-Flashing-Tool.git
cd Android-Flashing-Tool

# Create virtual environment (recommended)
python3 -m venv venv

# Activate virtual environment
# Windows:
venv\Scripts\activate
# macOS/Linux:
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt
pip install pyinstaller
```

### Step 2: Build Executable

#### Option A: Using Build Script

**Windows:**
```bash
.\build.bat
```

**macOS/Linux:**
```bash
./build.sh
```

#### Option B: Using Python Build Config

```bash
python build_config.py
```

#### Option C: Manual PyInstaller Command

```bash
pyinstaller \
    --onefile \
    --windowed \
    --name android-flashing-tool \
    --add-data "web:web" \
    --hidden-import=cryptography \
    --hidden-import=aiohttp \
    launcher.py
```

### Step 3: Locate Output

Built files appear in `dist/`:

```
dist/
├── android-flashing-tool          # Linux executable
├── android-flashing-tool.exe       # Windows executable
├── android-flashing-tool.dmg       # macOS disk image
└── android-flashing-tool-portable.zip  # Windows portable ZIP
```

---

## Platform-Specific Details

### Windows Build

#### Executable (`.exe`)
- Single-file executable
- No installation required
- Double-click to run
- Size: ~60-80 MB

#### Portable ZIP
- Compress executable with all dependencies
- Extract anywhere and run
- No admin rights needed

#### Installer (NSIS)
```bash
# Prerequisites
# 1. Install NSIS from: https://nsis.sourceforge.io/
# 2. Run build script

.\build.bat
```

**Installation:**
- Standard Windows installer wizard
- Start menu shortcuts
- Desktop shortcut
- Add/Remove Programs entry
- Automatic updates possible

### macOS Build

#### Disk Image (`.dmg`)
- Professional app distribution format
- Drag-and-drop installation to Applications
- Universal binary (Intel + Apple Silicon)
- Code signing recommended for distribution

**Requirements:**
```bash
brew install create-dmg
```

**Build:**
```bash
./build.sh
# Creates: dist/android-flashing-tool-1.0.0.dmg
```

**Installation:**
1. Double-click `.dmg` file
2. Drag app to Applications folder
3. Run from Applications or Spotlight

#### Code Signing (Optional)
```bash
# For App Store distribution
codesign -s - dist/android-flashing-tool

# Verify signature
codesign -v dist/android-flashing-tool
```

### Linux Build

#### AppImage (`.AppImage`)
- Standalone executable
- Works on all Linux distributions
- No dependencies to install
- Similar to portable `.exe` on Windows

**Requirements:**
```bash
pip install appimage-builder
```

**Build:**
```bash
./build.sh
# Creates: dist/android-flashing-tool-x86_64.AppImage
```

**Installation:**
```bash
# Make executable
chmod +x dist/android-flashing-tool-*.AppImage

# Run directly
./dist/android-flashing-tool-*.AppImage

# Or move to PATH
sudo mv dist/android-flashing-tool-*.AppImage /usr/local/bin/
android-flashing-tool
```

#### Traditional Package (Optional)
For distribution through package managers (apt, dnf, etc.):
- Create `.deb` for Ubuntu/Debian
- Create `.rpm` for Fedora/CentOS
- Use FPM: `pip install fpm`

---

## Customization

### Change App Icon

1. **Replace icon files:**
   - Windows: `launcher_icon.ico`
   - macOS: `launcher_icon.icns`
   - Linux: `launcher_icon.png`

2. **Update build config:**
```python
PYINSTALLER_OPTS = [
    ...
    "--icon", "path/to/icon",
    ...
]
```

### Change Branding

Edit `build_config.py`:
```python
PROJECT_NAME = "Your App Name"
PROJECT_SLUG = "your-app-slug"
VERSION = "1.0.0"
AUTHOR = "Your Name"
AUTHOR_EMAIL = "your@email.com"
```

### Add Resources

Add resources to bundle:
```python
PYINSTALLER_OPTS = [
    ...
    "--add-data", "path/to/resource:dest_path",
    ...
]
```

---

## Troubleshooting

### "PyInstaller not found"
```bash
pip install --upgrade pyinstaller
```

### "Web files not found"
Ensure web directory exists and add-data path is correct:
```python
--add-data "web:web"
```

### File too large
Enable UPX compression:
```bash
pip install upx
# Then add to PyInstaller: --upx-dir=/path/to/upx
```

### App won't start
Check console output for errors:
```bash
# Run with Python directly
python launcher.py

# Check logs in build directory
cat build/warnings.txt
```

### Platform-specific crashes

**macOS "can't be opened":**
```bash
# Grant permission
xattr -d com.apple.quarantine app.app

# Or open from Finder using Ctrl+Click → Open
```

**Linux "permission denied":**
```bash
chmod +x application
./application
```

**Windows SmartScreen warning:**
- Normal for new unsigned executables
- Click "More info" → "Run anyway"
- Or code sign the executable

---

## Distribution

### GitHub Releases

1. Build all platforms
2. Create GitHub Release
3. Upload built files:
   - `android-flashing-tool.exe`
   - `android-flashing-tool-portable.zip`
   - `android-flashing-tool.dmg`
   - `android-flashing-tool-x86_64.AppImage`

### Installation Scripts

**Linux (one-liner):**
```bash
sudo curl -L https://releases.github.com/.../app -o /usr/local/bin/app && chmod +x /usr/local/bin/app
```

**macOS (Homebrew):**
```bash
brew tap username/app
brew install app
```

**Windows (Scoop):**
```bash
scoop bucket add username https://github.com/username/scoop-app
scoop install app
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build Releases

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ${{ matrix.os }}
    strategy:
      matrix:
        os: [ubuntu-latest, windows-latest, macos-latest]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: '3.12'
      - run: |
          pip install -r requirements.txt
          pip install pyinstaller
      - run: ./build.sh
        if: runner.os != 'Windows'
      - run: .\build.bat
        if: runner.os == 'Windows'
      - uses: actions/upload-artifact@v3
        with:
          path: dist/*
```

---

## Performance Tips

1. **Reduce bundle size:**
   - Use `--onefile` (single executable is faster to run)
   - Strip debug symbols with `--strip`
   - Use UPX compression for Windows

2. **Faster builds:**
   - Use cached virtual environment
   - Avoid rebuilding unchanged parts
   - Use parallel compilation

3. **Better user experience:**
   - Show splash screen while loading
   - Pre-load heavy dependencies
   - Cache web assets

---

## Support

For issues:
1. Check troubleshooting section above
2. Review PyInstaller documentation: https://pyinstaller.org/
3. Open GitHub issue: https://github.com/CodesbyFebin/Android-Flashing-Tool/issues

---

**Last Updated:** October 2, 2026
**Build System Version:** 1.0.0
