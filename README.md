# K1pL4y_Agent.pro

Android AI agent project based on the CellClaw source currently present in this repository.

> **Development status:** experimental. CI builds are not a substitute for testing on a physical Android device. Self-improvement currently means learning from recent tool outcomes; it does not retrain the AI model or rewrite the APK by itself.

## Build an APK on GitHub

1. Open the **Actions** tab.
2. Select **K1pL4y Agent Android APK**.
3. Press **Run workflow** on branch `master`.
4. Wait for the workflow to finish successfully.
5. Open the completed run and download the `K1pL4y_Agent-pro-debug` artifact ZIP.
6. Extract the ZIP to get `app-debug.apk`, then install it on your Android device.

A push to `master` starts the debug and test-release workflows automatically. Check the **Releases** page for a test APK when the release workflow finishes successfully; use the Actions artifact if a Release asset is not yet available.

## Requirements

- Android 8.0 (API 26) or newer, according to the current Gradle configuration.
- An AI provider API key configured in the app. API usage may cost money; check provider pricing before use.
- Accessibility and other permissions must be enabled by the device owner. Grant only permissions needed for features you choose to use.

## Advanced agent capabilities

The system prompt now includes a structured problem-solving protocol for planning, comparing approaches, inspecting project context, verifying results, and reporting uncertainty. Bundled specialist skills provide repeatable workflows for:

- Full-stack engineering across web, Android, and backend projects
- Secure code review and application security
- Authorized penetration testing with scope, evidence, and remediation
- Defensive incident response
- Evidence-based research and technical comparisons

Skills are loaded into the agent context from bundled assets and user-created local skills. These instructions improve the agent's workflow but do not make every model an expert automatically; results depend on the configured model, available tools, network access, and actual test coverage. Security testing must stay within explicitly authorized scope.

## Termux coding integration

The `termux.exec` tool can run coding, build, and test commands in Termux and return their output. It requires Termux version 0.109 or newer, the app installed as `com.termux`, and the following one-time setup:

1. In Termux, edit `~/.termux/termux.properties` and set `allow-external-apps = true`, then restart Termux.
2. In Android Settings → Apps → K1pL4y_Agent.pro → Permissions / Additional permissions, grant **Run commands in Termux environment** if Android exposes that permission.
3. In the agent, approve each `termux.exec` request. The tool is configured to ask for approval even in the Full Auto profile.
4. Start with a harmless command such as `pwd` or `python --version` before asking it to modify a project.

Commands run with the Termux user's privileges, so inspect commands before approving them. A timeout does not guarantee that Termux killed the underlying process. If Termux is missing, the permission is not granted, or external apps are disabled, the integration will return an error.

## Safety

This app can interact with device UI and may request sensitive permissions. Keep human confirmation enabled for messages, account changes, purchases, deletion, and other consequential actions. Never share OTPs, passwords, or banking credentials with an agent.

## Source and license

This repository currently contains source derived from CellClaw. Before distributing modified APKs publicly or commercially, verify the upstream repository's license and comply with its terms. A public GitHub repository alone does not grant permission to redistribute copyrighted code. Keep upstream attribution and notices as required by the applicable license.

## Current build scope

CI runs unit tests and builds debug and test-release APKs. The test-release build uses a debug signing key and is not suitable for production or Play Store distribution. The Android application ID remains `com.cellclaw`; the launcher label is `K1pL4y_Agent.pro`. Device-level testing and upstream license verification are still required.
