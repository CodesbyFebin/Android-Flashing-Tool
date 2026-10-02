#!/usr/bin/env python3
"""
Build configuration for Android Flashing Tool
Generates standalone executables for Windows, Linux, and macOS
"""

import os
import sys
import platform
import subprocess
import shutil
from pathlib import Path

class BuildConfig:
    """Cross-platform build configuration"""

    # Project metadata
    PROJECT_NAME = "Android Flashing Tool"
    PROJECT_SLUG = "android-flashing-tool"
    VERSION = "1.0.0"
    DESCRIPTION = "Safe, offline firmware flashing for Android devices"
    AUTHOR = "CodesbyFebin"
    AUTHOR_EMAIL = "codesbyfebin@gmail.com"
    GITHUB_REPO = "https://github.com/CodesbyFebin/Android-Flashing-Tool"

    # Paths
    ROOT_DIR = Path(__file__).parent
    BUILD_DIR = ROOT_DIR / "build"
    DIST_DIR = ROOT_DIR / "dist"
    WEB_DIR = ROOT_DIR / "web"
    RUNTIME_FILE = ROOT_DIR / "runtime.py"

    # Platform detection
    SYSTEM = platform.system()  # Windows, Linux, Darwin
    MACHINE = platform.machine()  # x86_64, arm64, etc.

    # PyInstaller options
    PYINSTALLER_OPTS = [
        "--onefile",  # Single executable
        "--windowed",  # No console window
        "--name", PROJECT_SLUG,
        "--icon", "launcher_icon.ico",
        f"--version-file={PROJECT_SLUG}.version",
        "--add-data", f"{WEB_DIR}{os.pathsep}web",
        "--hidden-import=cryptography",
        "--hidden-import=aiohttp",
        "--collect-all=cryptography",
    ]

    @classmethod
    def get_output_name(cls):
        """Get platform-specific output filename"""
        if cls.SYSTEM == "Windows":
            return f"{cls.PROJECT_SLUG}-{cls.VERSION}-windows-x64.exe"
        elif cls.SYSTEM == "Darwin":
            return f"{cls.PROJECT_SLUG}-{cls.VERSION}-macos-{cls.MACHINE}.dmg"
        elif cls.SYSTEM == "Linux":
            return f"{cls.PROJECT_SLUG}-{cls.VERSION}-linux-{cls.MACHINE}.AppImage"
        return f"{cls.PROJECT_SLUG}-{cls.VERSION}-unknown"

    @classmethod
    def get_install_dir(cls):
        """Get platform-specific installation directory"""
        if cls.SYSTEM == "Windows":
            return "C:\\Program Files\\Android Flashing Tool"
        elif cls.SYSTEM == "Darwin":
            return "/Applications/Android Flashing Tool.app"
        elif cls.SYSTEM == "Linux":
            return "/opt/android-flashing-tool"
        return "/opt/android-flashing-tool"

    @classmethod
    def ensure_dirs(cls):
        """Ensure required directories exist"""
        cls.BUILD_DIR.mkdir(exist_ok=True)
        cls.DIST_DIR.mkdir(exist_ok=True)


def setup_environment():
    """Setup build environment"""
    print("🔧 Setting up build environment...")

    # Check Python version
    if sys.version_info < (3, 10):
        print("❌ Python 3.10+ required")
        sys.exit(1)

    # Check for PyInstaller
    try:
        import PyInstaller
        print(f"✅ PyInstaller {PyInstaller.__version__}")
    except ImportError:
        print("❌ PyInstaller not installed")
        print("Install with: pip install pyinstaller")
        sys.exit(1)

    # Platform-specific requirements
    if BuildConfig.SYSTEM == "Windows":
        check_windows_requirements()
    elif BuildConfig.SYSTEM == "Darwin":
        check_macos_requirements()
    elif BuildConfig.SYSTEM == "Linux":
        check_linux_requirements()


def check_windows_requirements():
    """Check Windows build requirements"""
    print("📋 Windows build requirements:")
    print("  - Python 3.10+")
    print("  - NSIS (for installer): https://nsis.sourceforge.io/")
    print("  - Visual C++ Build Tools (optional, for native extensions)")


def check_macos_requirements():
    """Check macOS build requirements"""
    print("📋 macOS build requirements:")
    print("  - Python 3.10+")
    print("  - Xcode Command Line Tools: xcode-select --install")
    print("  - create-dmg: brew install create-dmg")


def check_linux_requirements():
    """Check Linux build requirements"""
    print("📋 Linux build requirements:")
    print("  - Python 3.10+")
    print("  - appimage-builder: pip install appimage-builder")
    print("  - FUSE: sudo apt-get install libfuse2 (runtime requirement)")


def build_executable():
    """Build executable using PyInstaller"""
    print(f"\n🏗️  Building {BuildConfig.PROJECT_SLUG} for {BuildConfig.SYSTEM}...")

    BuildConfig.ensure_dirs()

    # Change to project directory
    os.chdir(BuildConfig.ROOT_DIR)

    # Build command
    cmd = [
        "pyinstaller",
        *BuildConfig.PYINSTALLER_OPTS,
        "--distpath", str(BuildConfig.DIST_DIR),
        "--buildpath", str(BuildConfig.BUILD_DIR / "pyinstaller"),
        "--specpath", str(BuildConfig.BUILD_DIR),
        str(BuildConfig.RUNTIME_FILE),
    ]

    print(f"📦 Command: {' '.join(cmd)}")
    result = subprocess.run(cmd)

    if result.returncode != 0:
        print("❌ Build failed")
        sys.exit(1)

    print("✅ Build successful")
    return BuildConfig.DIST_DIR / BuildConfig.PROJECT_SLUG


def create_installer(executable_path):
    """Create platform-specific installer"""
    if BuildConfig.SYSTEM == "Windows":
        create_windows_installer(executable_path)
    elif BuildConfig.SYSTEM == "Darwin":
        create_macos_dmg(executable_path)
    elif BuildConfig.SYSTEM == "Linux":
        create_linux_appimage(executable_path)


def create_windows_installer(executable_path):
    """Create Windows NSIS installer"""
    print("\n📦 Creating Windows installer...")

    nsis_script = BuildConfig.BUILD_DIR / f"{BuildConfig.PROJECT_SLUG}.nsi"

    nsi_content = f"""
; Android Flashing Tool Installer
!include "MUI2.nsh"

Name "{BuildConfig.PROJECT_NAME} {BuildConfig.VERSION}"
OutFile "${{OUTDIR}}/{BuildConfig.PROJECT_SLUG}-{BuildConfig.VERSION}-installer.exe"
InstallDir "$PROGRAMFILES\\Android Flashing Tool"

; MUI Settings
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_LANGUAGE "English"

; Installer sections
Section "Install"
  SetOutPath "$INSTDIR"
  File "{executable_path}"

  ; Create shortcuts
  CreateDirectory "$SMPROGRAMS\\Android Flashing Tool"
  CreateShortcut "$SMPROGRAMS\\Android Flashing Tool\\Android Flashing Tool.lnk" "$INSTDIR\\{BuildConfig.PROJECT_SLUG}.exe"
  CreateShortcut "$DESKTOP\\Android Flashing Tool.lnk" "$INSTDIR\\{BuildConfig.PROJECT_SLUG}.exe"
  CreateShortcut "$SMPROGRAMS\\Android Flashing Tool\\Uninstall.lnk" "$INSTDIR\\Uninstall.exe"

  ; Create uninstaller
  WriteUninstaller "$INSTDIR\\Uninstall.exe"

  ; Register with Windows
  WriteRegStr HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{BuildConfig.PROJECT_SLUG}" "DisplayName" "{BuildConfig.PROJECT_NAME}"
  WriteRegStr HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{BuildConfig.PROJECT_SLUG}" "UninstallString" "$INSTDIR\\Uninstall.exe"
  WriteRegStr HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{BuildConfig.PROJECT_SLUG}" "DisplayVersion" "{BuildConfig.VERSION}"
SectionEnd

Section "Uninstall"
  Delete "$INSTDIR\\{BuildConfig.PROJECT_SLUG}.exe"
  Delete "$INSTDIR\\Uninstall.exe"
  RMDir "$INSTDIR"

  Delete "$DESKTOP\\Android Flashing Tool.lnk"
  Delete "$SMPROGRAMS\\Android Flashing Tool\\Android Flashing Tool.lnk"
  Delete "$SMPROGRAMS\\Android Flashing Tool\\Uninstall.lnk"
  RMDir "$SMPROGRAMS\\Android Flashing Tool"
SectionEnd
"""

    nsis_script.write_text(nsi_content)

    # Check for NSIS
    try:
        subprocess.run(["makensis", str(nsis_script)], check=True)
        print("✅ Windows installer created")
    except FileNotFoundError:
        print("⚠️  NSIS not found. Skipping installer creation.")
        print("   Install NSIS from: https://nsis.sourceforge.io/")


def create_macos_dmg(executable_path):
    """Create macOS DMG installer"""
    print("\n📦 Creating macOS DMG...")

    try:
        dmg_file = BuildConfig.DIST_DIR / f"{BuildConfig.PROJECT_SLUG}-{BuildConfig.VERSION}.dmg"

        # Check for create-dmg
        subprocess.run([
            "create-dmg",
            "--volname", BuildConfig.PROJECT_NAME,
            "--window-pos", "200", "120",
            "--window-size", "600", "400",
            "--icon-size", "100",
            str(dmg_file),
            str(executable_path),
        ], check=True)

        print(f"✅ macOS DMG created: {dmg_file}")
    except FileNotFoundError:
        print("⚠️  create-dmg not found")
        print("   Install with: brew install create-dmg")


def create_linux_appimage(executable_path):
    """Create Linux AppImage"""
    print("\n📦 Creating Linux AppImage...")

    try:
        subprocess.run([
            "appimage-builder",
            "--appdir", str(BuildConfig.DIST_DIR / "AppDir"),
            "--output", "appimage",
        ], check=True)
        print("✅ Linux AppImage created")
    except FileNotFoundError:
        print("⚠️  appimage-builder not found")
        print("   Install with: pip install appimage-builder")


def create_portable_zip(executable_path):
    """Create portable ZIP for Windows"""
    print("\n📦 Creating portable ZIP...")

    zip_path = BuildConfig.DIST_DIR / f"{BuildConfig.PROJECT_SLUG}-{BuildConfig.VERSION}-portable.zip"

    shutil.make_archive(
        str(zip_path.with_suffix("")),
        'zip',
        str(BuildConfig.DIST_DIR),
        executable_path.name
    )

    print(f"✅ Portable ZIP created: {zip_path}")


def main():
    """Main build function"""
    print("=" * 60)
    print(f"🚀 {BuildConfig.PROJECT_NAME} Build System")
    print(f"   Version: {BuildConfig.VERSION}")
    print(f"   Platform: {BuildConfig.SYSTEM} ({BuildConfig.MACHINE})")
    print("=" * 60)

    setup_environment()

    # Build executable
    executable = build_executable()

    # Create installers
    create_installer(executable)

    # Create portable version
    if BuildConfig.SYSTEM == "Windows":
        create_portable_zip(executable)

    print("\n" + "=" * 60)
    print("✅ Build complete!")
    print(f"📁 Output: {BuildConfig.DIST_DIR}")
    print("=" * 60)


if __name__ == "__main__":
    main()
