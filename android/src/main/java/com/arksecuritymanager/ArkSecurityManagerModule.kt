package com.arksecuritymanager

import android.app.Application
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.garantibbva.arkmobil.securitymanager.ArkSecurityManager

/**
 * React Native bridge module for ARK Security Manager.
 *
 * Wraps [ArkSecurityManager] from the native Android SDK and exposes its
 * functionality to JavaScript via both the Legacy Bridge and the New Architecture
 * (TurboModule / Codegen).
 *
 * The generated [NativeArkSecurityManagerSpec] base class handles the New Architecture
 * plumbing automatically; no extra code is needed for backward compatibility.
 */
class ArkSecurityManagerModule(private val reactContext: ReactApplicationContext) :
  NativeArkSecurityManagerSpec(reactContext) {

  companion object {
    const val NAME = NativeArkSecurityManagerSpec.NAME
  }

  // ── Detection checks ────────────────────────────────────────────────────

  override fun isDebuggerAttached(): Boolean =
    ArkSecurityManager.isDebuggerConnected

  override fun isDeveloperOptionsEnabled(): Boolean =
    ArkSecurityManager.isDeveloperOptionsEnabled(reactContext)

  override fun isDeviceCompromised(): Boolean {
    val app = reactContext.applicationContext as? Application ?: return false
    return ArkSecurityManager.isRooted(app)
  }

  override fun isFridaDetected(): Boolean =
    ArkSecurityManager.checkFridaPortOpen()

  /** SSL bypass detection is iOS-only; always returns false on Android. */
  override fun isSSLBypassed(): Boolean = false

  override fun isInstalledFromTrustedSource(): Boolean =
    ArkSecurityManager.isInstalledFromTrustedStore(reactContext)

  // ── Screen protection ───────────────────────────────────────────────────

  override fun setScreenSecure(enable: Boolean) {
    val activity = currentActivity ?: return
    activity.runOnUiThread {
      if (enable) {
        ArkSecurityManager.setWindowSecureFlag(activity)
      } else {
        ArkSecurityManager.clearWindowSecureFlag(activity)
      }
    }
  }

  // ── Android-specific ───────────────────────────────────────────────────

  override fun isLocationMocked(isMock: Boolean): Boolean = isMock

  override fun setTrustedStores(stores: ReadableArray) {
    val list = (0 until stores.size()).mapNotNull { stores.getString(it) }
    ArkSecurityManager.setTrustedStores(list)
  }

  // ── Composite report ───────────────────────────────────────────────────

  override fun getSecurityReport(): com.facebook.react.bridge.WritableMap {
    val app = reactContext.applicationContext as? Application
    val map = Arguments.createMap()
    map.putBoolean("isDebuggerAttached", ArkSecurityManager.isDebuggerConnected)
    map.putBoolean("isDeveloperOptionsEnabled", ArkSecurityManager.isDeveloperOptionsEnabled(reactContext))
    map.putBoolean("isDeviceCompromised", if (app != null) ArkSecurityManager.isRooted(app) else false)
    map.putBoolean("isFridaDetected", ArkSecurityManager.checkFridaPortOpen())
    map.putBoolean("isSSLBypassed", false)
    map.putBoolean("isInstalledFromTrustedSource", ArkSecurityManager.isInstalledFromTrustedStore(reactContext))
    return map
  }
}
