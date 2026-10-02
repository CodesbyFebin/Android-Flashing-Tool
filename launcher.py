#!/usr/bin/env python3
"""
Launcher for Android Flashing Tool
Handles platform detection and opens the web interface
"""

import os
import sys
import webbrowser
import time
import subprocess
import threading
from pathlib import Path

def get_resource_path(filename):
    """Get path to bundled resources (works with PyInstaller)"""
    if hasattr(sys, '_MEIPASS'):
        return os.path.join(sys._MEIPASS, filename)
    return os.path.join(os.path.dirname(__file__), filename)


def check_fastboot():
    """Check if fastboot/adb are available"""
    try:
        subprocess.run(
            ["fastboot", "--version"],
            capture_output=True,
            timeout=2
        )
        return True
    except (FileNotFoundError, subprocess.TimeoutExpired):
        return False


def launch_browser(url, delay=2):
    """Launch web browser after delay"""
    time.sleep(delay)
    webbrowser.open(url)


def main():
    """Main launcher function"""
    print("🚀 Android Flashing Tool")
    print("=" * 60)

    # Check fastboot
    print("📱 Checking for fastboot/adb...")
    if not check_fastboot():
        print("⚠️  WARNING: fastboot not found in PATH")
        print("   Make sure Android SDK Platform-Tools is installed")
        print("   Download: https://developer.android.com/tools/releases/platform-tools")
        print()
    else:
        print("✅ fastboot found")

    # Start runtime
    print("🔧 Starting local runtime...")

    try:
        # Import and run runtime
        from runtime import main as runtime_main

        # Start runtime in background thread
        runtime_thread = threading.Thread(target=runtime_main, daemon=True)
        runtime_thread.start()

        # Open browser
        print("🌐 Opening browser...")
        browser_thread = threading.Thread(
            target=launch_browser,
            args=("http://127.0.0.1:8765",),
            daemon=True
        )
        browser_thread.start()

        print("=" * 60)
        print("✅ Application running")
        print("🌐 Open browser to: http://127.0.0.1:8765")
        print()
        print("Press Ctrl+C to stop")
        print("=" * 60)
        print()

        # Keep running
        runtime_thread.join()

    except KeyboardInterrupt:
        print("\n\n👋 Shutting down...")
        sys.exit(0)
    except Exception as e:
        print(f"❌ Error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
