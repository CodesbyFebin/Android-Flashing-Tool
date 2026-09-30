# Vercel deployment

Import https://github.com/CodesbyFebin/Android-Flashing-Tool into Vercel under the desired team. Use repository root, framework Other, build command `npm run build`, output directory `public`. No environment variables are required for the hosted interface.

`vercel.json` supplies these settings. Connect production branch `main` for automatic future deployments. `.github/workflows/verify.yml` runs application checks on pushes and pull requests.

Vercel serves the web interface, setup instructions and source-download links. It does not run the USB flashing runtime. The hosted interface blocks firmware writes and device controls, and directs the user to http://127.0.0.1:8765 after launching the application locally. It never bypasses local-runtime origin checks or calls ADB on the hosting server.

For local USB operations, follow QUICK_START.md. `python runtime.py` serves the same interface on loopback with the authenticated API. Actual writes require the documented explicit runtime setting and valid signed firmware/device gates.
