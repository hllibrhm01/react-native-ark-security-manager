# Changelog

## 0.0.1 (27-03-2026)

### Changes

### SDK initialization
- The SDK exposes a lightweight initialization step to prepare internal state; call this at app startup (for example, a single `initialize` entry-point to set up context and configuration).
- 
### Added
- Initial public API for security checks and protections:
    - Debugger detection (`isDebuggerConnected`).
    - Developer options detection (`isDeveloperOptionsEnabled`).
    - Root detection utilities (`isRooted`).
    - Frida port scanner (`checkFridaPortOpen`).
    - Window protection helpers (`setWindowSecureFlag`, `clearWindowSecureFlag`).
    - Overview/recents protection helper (`setOverviewScreenSecureView`) with SDK-gated fallback.
    - Installer-source verification (`isInstalledFromTrustedStore`).

### Notes
- No breaking changes in this initial release.
- Overview/recents protection should be guarded by Android S+ checks when used.
- Network/frida checks run off the main thread — call from a background dispatcher.
