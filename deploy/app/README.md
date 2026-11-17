# Vexus Android App

- Package: `com.vexus.app`
- Version: `1.0.0` (versionCode 1)
- Min SDK: 21
- Target SDK: 27
- Entry: WebView wrapper loading `https://vexus.us.com/wap/`
- File: `Vexus.apk`
- Signed with local keystore `vexus.keystore` (kept outside the repository)

## Install

```bash
adb install -r Vexus.apk
```

Or copy the APK to an Android phone and install manually (allow unknown sources).

## Rebuild

Requires:
- Android SDK build-tools (aapt, d8, zipalign, apksigner)
- JDK bundled with Android Studio

```powershell
powershell -ExecutionPolicy Bypass -File build-apk.ps1
```

The output APK is written to `dist/Vexus.apk` in the build workspace.

## Notes

- The app is an Android WebView wrapper. It requires network access.
- iOS IPA builds require macOS + Xcode and are not included here.
