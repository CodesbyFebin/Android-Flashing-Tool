#!/bin/bash
# Linux/macOS build script for Android Flashing Tool

set -e

PROJECT_NAME="Android Flashing Tool"
PROJECT_SLUG="android-flashing-tool"
VERSION="1.0.0"

echo "🚀 $PROJECT_NAME Build System"
echo "=================================="

# Check Python
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is required"
    exit 1
fi

PYTHON_VERSION=$(python3 -c 'import sys; print(f"{sys.version_info.major}.{sys.version_info.minor}")')
echo "✅ Python $PYTHON_VERSION found"

# Check PyInstaller
if ! python3 -c "import PyInstaller" 2>/dev/null; then
    echo "❌ PyInstaller not installed"
    echo "   Install with: pip install pyinstaller"
    exit 1
fi

# Create directories
mkdir -p build dist

# Install dependencies
echo ""
echo "📦 Installing dependencies..."
pip install -q -r requirements.txt

# Build executable
echo ""
echo "🏗️  Building executable..."

python3 -m PyInstaller \
    --onefile \
    --windowed \
    --name "$PROJECT_SLUG" \
    --distpath dist \
    --buildpath build/pyinstaller \
    --specpath build \
    --add-data "web:web" \
    --hidden-import=cryptography \
    --hidden-import=aiohttp \
    launcher.py

echo "✅ Executable built: dist/$PROJECT_SLUG"

# Platform-specific tasks
SYSTEM=$(uname -s)

if [ "$SYSTEM" = "Darwin" ]; then
    echo ""
    echo "📦 Creating macOS DMG..."

    if command -v create-dmg &> /dev/null; then
        DMG_FILE="dist/${PROJECT_SLUG}-${VERSION}.dmg"
        create-dmg \
            --volname "$PROJECT_NAME" \
            --window-pos 200 120 \
            --window-size 600 400 \
            --icon-size 100 \
            "$DMG_FILE" \
            "dist/$PROJECT_SLUG"
        echo "✅ DMG created: $DMG_FILE"
    else
        echo "⚠️  create-dmg not found. Skipping DMG creation."
        echo "   Install with: brew install create-dmg"
    fi
fi

if [ "$SYSTEM" = "Linux" ]; then
    echo ""
    echo "📦 Creating Linux AppImage..."

    if command -v appimage-builder &> /dev/null; then
        appimage-builder \
            --appdir "dist/AppDir" \
            --output appimage
        echo "✅ AppImage created"
    else
        echo "⚠️  appimage-builder not found. Skipping AppImage creation."
        echo "   Install with: pip install appimage-builder"
    fi
fi

echo ""
echo "=================================="
echo "✅ Build complete!"
echo "📁 Output: ./dist/"
echo "=================================="
