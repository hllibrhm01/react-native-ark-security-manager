# react-native-ark-security-manager

React Native SDK for ARK Security Manager — wraps the native Android and iOS security SDKs and exposes a unified JavaScript API. Supports both **Legacy Bridge** and **New Architecture (TurboModules)**.

## Features

| Check                                    | Android | iOS |
| ---------------------------------------- | :-----: | :-: |
| Debugger attached                        |   ✅    | ✅  |
| Developer options enabled                |   ✅    |  —  |
| Root / Jailbreak detection               |   ✅    | ✅  |
| Frida detection                          |   ✅    |  —  |
| SSL bypass detection                     |    —    | ✅  |
| Trusted install source                   |   ✅    | ✅  |
| Screen secure (FLAG_SECURE / SecureView) |   ✅    | ✅  |
| Overview / recents protection            |   ✅    |  —  |
| Screenshot event monitoring              |    —    | ✅  |
| Screen-recording event monitoring        |    —    | ✅  |
| Mock location check                      |   ✅    |  —  |

## Installation

```sh
npm install react-native-ark-security-manager
# or
yarn add react-native-ark-security-manager
```

### Native SDK setup (local)

This package expects the native SDKs to be present under `native/`:

```
native/
├── android/
│   └── ark-security-manager/   ← from ark-mobile-android-module
└── ios/
    └── Modules/
        └── ARKSecurityManager/ ← from ark-mobile-ios-module
```

Clone or symlink:

```sh
# Android
git clone https://github.com/hllibrhm01/ark-mobile-android-module native/android-repo
ln -s ../android-repo/ark-security-manager native/android/ark-security-manager

# iOS
git clone https://github.com/hllibrhm01/ark-mobile-ios-module native/ios-repo
ln -s ../ios-repo/Modules native/ios/Modules
```

### iOS — Podfile

```ruby
pod install
```

The podspec uses a local path dependency for `ARKSecurityManager` automatically.

### Android — settings.gradle

The `android/settings.gradle` in this package registers `:ark-security-manager` as a Gradle subproject automatically via RN autolinking.

## Usage

```ts
import {
  addSecurityEventListener,
  applyOverviewProtection,
  isDebuggerAttached,
  isDeviceCompromised,
  setScreenSecure,
  startSecurityEventMonitoring,
  stopSecurityEventMonitoring,
  getSecurityReport,
} from 'react-native-ark-security-manager';

// Single check
if (isDeviceCompromised()) {
  console.warn('Device is rooted/jailbroken!');
}

// Full report at startup
const report = getSecurityReport();
console.log(report);
// {
//   isDebuggerAttached: false,
//   isDeveloperOptionsEnabled: false,
//   isDeviceCompromised: false,
//   isFridaDetected: false,
//   isSSLBypassed: false,
//   isInstalledFromTrustedSource: true,
// }

// Screen protection
setScreenSecure(true);

// Android overview protection
applyOverviewProtection(true);

// iOS native events
startSecurityEventMonitoring();
const subscription = addSecurityEventListener((event) => {
  console.log(event);
});

stopSecurityEventMonitoring();
subscription.remove();
```

## API

| Function                             | Returns              | Description                                        |
| ------------------------------------ | -------------------- | -------------------------------------------------- |
| `isDebuggerAttached()`               | `boolean`            | Debugger attached?                                 |
| `isDeveloperOptionsEnabled()`        | `boolean`            | Dev options on? (Android)                          |
| `isDeviceCompromised()`              | `boolean`            | Rooted / jailbroken?                               |
| `isFridaDetected()`                  | `boolean`            | Frida ports open? (Android)                        |
| `isSSLBypassed()`                    | `boolean`            | SSL bypass detected? (iOS)                         |
| `isInstalledFromTrustedSource()`     | `boolean`            | From a trusted store?                              |
| `setScreenSecure(enable)`            | `void`               | Enable/disable screenshot protection               |
| `applyOverviewProtection(useBlur)`   | `void`               | Apply Android overview blur / FLAG_SECURE fallback |
| `startSecurityEventMonitoring()`     | `void`               | Start iOS screenshot and recording observers       |
| `stopSecurityEventMonitoring()`      | `void`               | Stop iOS screenshot and recording observers        |
| `isScreenRecordingActive()`          | `boolean`            | Current iOS screen-recording state                 |
| `addSecurityEventListener(listener)` | `{ remove(): void }` | Subscribe to iOS native security events            |
| `isLocationMocked(isMock)`           | `boolean`            | Location mocked? (Android)                         |
| `setTrustedStores(stores)`           | `void`               | Override trusted store list (Android)              |
| `getSecurityReport()`                | `SecurityReport`     | All checks in one call                             |

## Native Event Payloads

`addSecurityEventListener()` emits the following payloads on iOS:

```ts
type SecurityEvent =
  | {
      type: 'screenshotTaken';
      platform: 'ios';
      timestamp: number;
    }
  | {
      type: 'screenRecordingChanged';
      platform: 'ios';
      timestamp: number;
      isRecording: boolean;
    };
```

## Example App

The example app now includes:

- A full Android Security Tests parity section with explicit buttons for debugger, developer options, device compromise, Frida, trusted source, `setScreenSecure(true)`, `setScreenSecure(false)` and overview protection.
- An iOS monitoring section that exposes the combined screen-secure behavior and streams screenshot / screen-recording events through the React Native bridge.
- Manual action output and native event logs so bridge behavior is observable without attaching a debugger.

## License

MIT
