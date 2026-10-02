@echo off
REM Windows build script for Android Flashing Tool

setlocal enabledelayedexpansion

set PROJECT_NAME=Android Flashing Tool
set PROJECT_SLUG=android-flashing-tool
set VERSION=1.0.0

echo.
echo 🚀 %PROJECT_NAME% Build System
echo ==================================

REM Check Python
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Python is required
    echo    Download from: https://www.python.org/downloads/
    exit /b 1
)

for /f "tokens=2" %%i in ('python --version 2^>^&1') do set PYTHON_VERSION=%%i
echo ✅ Python %PYTHON_VERSION% found

REM Check PyInstaller
python -c "import PyInstaller" >nul 2>&1
if errorlevel 1 (
    echo ❌ PyInstaller not installed
    echo    Install with: pip install pyinstaller
    exit /b 1
)

REM Create directories
if not exist build mkdir build
if not exist dist mkdir dist

REM Install dependencies
echo.
echo 📦 Installing dependencies...
pip install -q -r requirements.txt

REM Build executable
echo.
echo 🏗️  Building executable...

python -m PyInstaller ^
    --onefile ^
    --windowed ^
    --name %PROJECT_SLUG% ^
    --distpath dist ^
    --buildpath build\pyinstaller ^
    --specpath build ^
    --add-data "web;web" ^
    --hidden-import=cryptography ^
    --hidden-import=aiohttp ^
    launcher.py

if errorlevel 1 (
    echo ❌ Build failed
    exit /b 1
)

echo ✅ Executable built: dist\%PROJECT_SLUG%.exe

REM Create ZIP portable version
echo.
echo 📦 Creating portable ZIP...

cd dist
powershell -Command "Compress-Archive -Path '%PROJECT_SLUG%.exe' -DestinationPath '%PROJECT_SLUG%-%VERSION%-portable.zip' -Force"
cd ..

echo ✅ Portable ZIP created: dist\%PROJECT_SLUG%-%VERSION%-portable.zip

REM Check for NSIS
where makensis >nul 2>&1
if errorlevel 1 (
    echo.
    echo ⚠️  NSIS not found. Skipping installer creation.
    echo    Install from: https://nsis.sourceforge.io/
) else (
    echo.
    echo 📦 Creating Windows installer...

    REM Note: NSIS script generation would go here
    echo ⚠️  NSIS installer creation not yet implemented
)

echo.
echo ==================================
echo ✅ Build complete!
echo 📁 Output: .\dist\
echo ==================================
echo.
pause
