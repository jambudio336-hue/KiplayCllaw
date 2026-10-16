# K1pL4y_Agent.pro

Android AI agent project based on the CellClaw source currently present in this repository.

> **Development status:** experimental. The GitHub Actions workflow builds a debug APK for testing; it does not mean every feature has been tested on a physical device.

## Build an APK on GitHub

1. Open the **Actions** tab.
2. Select **K1pL4y Agent Android APK**.
3. Press **Run workflow** on branch `master`.
4. Wait for the workflow to finish successfully.
5. Open the completed run and download the `K1pL4y_Agent-pro-debug` artifact ZIP.
6. Extract the ZIP to get `app-debug.apk`, then install it on your Android device.

A push affecting app or Gradle files on `master` also starts a build automatically.

## Requirements

- Android 8.0 (API 26) or newer, according to the current Gradle configuration.
- An AI provider API key configured in the app. API usage may cost money; check provider pricing before use.
- Accessibility and other permissions must be enabled by the device owner. Grant only permissions needed for features you choose to use.

## Safety

This app can interact with device UI and may request sensitive permissions. Keep human confirmation enabled for messages, account changes, purchases, deletion, and other consequential actions. Never share OTPs, passwords, or banking credentials with an agent.

## Source and license

This repository currently contains source derived from CellClaw. Before distributing modified APKs publicly or commercially, verify the upstream repository's license and comply with its terms. A public GitHub repository alone does not grant permission to redistribute copyrighted code. Keep upstream attribution and notices as required by the applicable license.

## Current build scope

The initial workflow runs unit tests and builds an Android **debug** APK, then uploads it as a GitHub Actions artifact. Release signing, package ID rebranding, and device-level testing are separate follow-up tasks.
