# Smriti Counter (Android Widget App)

Track your seconds in soul consciousness (*"I am a soul"*) and daily Brahma Kumaris **Swaman** practice.

## What's Included
- **6 Home-Screen Widget Options (Dark & Light Themes):**
  - **2x2 Minimal (Dark / Light):** Shows only the count number. Tap anywhere to +1.
  - **4x4 Standard (Dark / Light):** Shows `SMRITI`, active Swaman, count & seconds, live focus timer (`Start / Stop`), and optional streak.
  - **6x6 Extended (Dark / Light):** Adds date, dual progress (count + seconds), progress bar, `-1 Undo`, `Reset`, `Bindu` meditation screen, and `History`.
- **Seconds-in-Consciousness Tracking:** Live widget timer (`Start / Stop`) + distraction-free **Point-of-Light (Bindu)** meditation screen.
- **Swaman Practice:** Built-in & custom Swamans, daily auto-rotation, and per-day Swaman history.
- **Spiritual Chart & Insights:** Automatic **Amrit Vela**, **Daytime**, and **Evening** breakdown, dual targets (count + seconds), 7-day summary, and one-tap **Copy Chart** export.
- **Traffic Control Reminders:** Optional hourly/custom-interval gentle reminder notifications.
- **Fixed Haptic & Bell Chime:** Uses `VibrationAttributes` (Android 12+/13+ compatible) and a synthesized soft bell chime via `AudioTrack`.

## How to Build the APK on GitHub
1. Upload the `app` folder and root files (`build.gradle.kts`, `gradle.properties`, `settings.gradle.kts`, `README.md`) to your GitHub repository.
2. Add `.github/workflows/build-apk.yml` in GitHub.
3. Open **Actions** -> **Build Android APK**, then download **SmritiCounter-APK** from **Artifacts**.
