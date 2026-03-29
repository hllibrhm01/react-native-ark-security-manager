# react-native-ark-security-manager

React Native SDK for ARK Security Manager — wraps the native Android and iOS security SDKs and exposes a unified JavaScript API. Supports both **Legacy Bridge** and **New Architecture (TurboModules)**.

## Features

| Check | Android | iOS |
|---|:---:|:---:|
| Debugger attached | ✅ | ✅ |
| Developer options enabled | ✅ | — |
| Root / Jailbreak detection | ✅ | ✅ |
| Frida detection | ✅ | — |
| SSL bypass detection | — | ✅ |
| Trusted install source | ✅ | ✅ |
| Screen secure (FLAG_SECURE / SecureView) | ✅ | ✅ |
| Mock location check | ✅ | — |

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
  isDebuggerAttached,
  isDeviceCompromised,
  isSSLBypassed,
  setScreenSecure,
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
```

## API

| Function | Returns | Description |
|---|---|---|
| `isDebuggerAttached()` | `boolean` | Debugger attached? |
| `isDeveloperOptionsEnabled()` | `boolean` | Dev options on? (Android) |
| `isDeviceCompromised()` | `boolean` | Rooted / jailbroken? |
| `isFridaDetected()` | `boolean` | Frida ports open? (Android) |
| `isSSLBypassed()` | `boolean` | SSL bypass detected? (iOS) |
| `isInstalledFromTrustedSource()` | `boolean` | From a trusted store? |
| `setScreenSecure(enable)` | `void` | Enable/disable screenshot protection |
| `isLocationMocked(isMock)` | `boolean` | Location mocked? (Android) |
| `setTrustedStores(stores)` | `void` | Override trusted store list (Android) |
| `getSecurityReport()` | `SecurityReport` | All checks in one call |

## License

MIT
