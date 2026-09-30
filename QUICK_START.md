# Start the upgraded application

1. Extract the ZIP into a folder on the computer connected to your Android device.
2. Install Python 3.10 or newer and the official Android SDK Platform-Tools. Ensure `adb` and `fastboot` are on PATH.
3. Launch:

| System | Launcher |
|---|---|
| Windows | Double-click `start.bat` |
| macOS | Run `sh start.command` in this folder |
| Linux | Run `sh start.sh` in this folder |

The launcher creates a Python environment, installs its dependency, starts the local API and opens the application at http://127.0.0.1:8765. First setup requires internet to install dependencies. After setup, the UI/runtime do not require cloud services.

The initial mode is inspection-only. Device writes require explicit runtime configuration and a trusted signed package; see README.md. Do not attempt to use ordinary firmware archives, Odin packages, or unsigned images with this guided release.

The finished reference image is in `reference/final-design-reference.png`. It is a design reference, not evidence that hardware was connected or flashed.
