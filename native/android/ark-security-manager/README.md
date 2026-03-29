# ark-security-manager

## Project Setup

Add the dependency:

```groovy
implementation 'com.garantibbva.arkmobil:ark-security-manager:0.0.1-alpha.1'
```

## Introduction

`ark-security-manager` is an Android security SDK providing runtime checks such as debugger/ADB/Frida detection, root checks, screen protection (FLAG_SECURE and overview blur), and installer/source verification.

## Features

- **Debugger detection:** `isDebuggerConnected` — detects whether a debugger is attached at runtime.
- **Developer options check:** reports whether developer options are enabled on the device.
- **Root detection:** heuristic checks based on binaries, known root apps, and `su` presence.
- **Frida detection:** tests whether common Frida ports are open on the device (local loopback).
- **Location mock detection:** detects whether a `Location` instance appears to be mocked.
- **Screen protection:** `FLAG_SECURE` handling and overview blur/overlay support on Android S+.
- **Installer verification:** checks which installer package installed the app and compares it to a trusted list.

## Limitations

- Root and tool detection are heuristic-based and can produce false positives/negatives on some custom ROMs or OEM-modified devices.
- Frida/port scanning inspects the local loopback (127.0.0.1) and does not detect remote attacks directly.
- Some features require newer Android versions (overview blur requires Android S / API 31+).

Minimum recommended Android: `minSdkVersion 24`.

ProGuard / R8 rules:

```proguard
-keep class com.garantibbva.arkmobil.securitymanager.** { *; }
-dontwarn com.garantibbva.arkmobil.securitymanager.**
```

Required permissions: the SDK itself does not request permissions. For location/mock detection, your app must manage `ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION` and provide the `Location` object.

## Usage

### Basic checks

```kotlin
val isDebugger = ArkSecurityManager.isDebuggerConnected
val isDevOptions = ArkSecurityManager.isDeveloperOptionsEnabled(context)
val isRooted = ArkSecurityManager.isRooted(application)
val isFridaOpen = ArkSecurityManager.checkFridaPortOpen()
```

### Window / Overview protection

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ArkSecurityManager.setWindowSecureFlag(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ArkSecurityManager.setOverviewScreenSecureView(this, overlayView = null, useBlur = true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ArkSecurityManager.clearWindowSecureFlag(this)
    }
}
```

### Location / mock detection

```kotlin
fun onLocationReceived(location: Location) {
    if (ArkSecurityManager.isLocationMocked(location)) {
        // handle suspicious location
    }
}
```

### Trusted installer stores

```kotlin
ArkSecurityManager.setTrustedStores(listOf("com.android.vending", "com.example.store"))
ArkSecurityManager.removeTrustedStore("com.example.oldstore")
```

## Examples

### 1) Lock app at startup if rooted or Frida detected

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val isRooted = ArkSecurityManager.isRooted(this)
        val isFrida = ArkSecurityManager.checkFridaPortOpen()
        if (isRooted || isFrida) {
            val intent = Intent(this, SecurityLockedActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }
}
```

### 2) Warn user during login if developer options or debugger detected

```kotlin
fun attemptLogin(context: Context) {
    if (ArkSecurityManager.isDeveloperOptionsEnabled(context) || ArkSecurityManager.isDebuggerConnected) {
        AlertDialog.Builder(context)
            .setTitle("Security Warning")
            .setMessage("Developer options or a debugger is detected. Proceeding may not be safe.")
            .setPositiveButton("Continue") { _, _ -> performLogin() }
            .setNegativeButton("Cancel", null)
            .show()
        return
    }
    performLogin()
}
```

### 3) FLAG_SECURE + overview blur for a sensitive Activity

```kotlin
class SensitiveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensitive)
        ArkSecurityManager.setWindowSecureFlag(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ArkSecurityManager.setOverviewScreenSecureView(this, overlayView = null, useBlur = true)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        ArkSecurityManager.clearWindowSecureFlag(this)
    }
}
```

### 4) Trusted store check in an update/entitlement flow

```kotlin
fun checkInstallerAndProceed(context: Context) {
    val fromTrustedStore = ArkSecurityManager.isInstalledFromTrustedStore(context)
    if (fromTrustedStore.not()) {
        // restrict features or show a warning
    } else {
        proceedWithUpdateOrEntitlement()
    }
}
```

## Notes

- The SDK exposes high-level helpers while encapsulating lower-level checks.
- Integrate these checks into your app flow mindful of UX, logging, and reporting policies.
