import NativeArkSecurityManager from './NativeArkSecurityManager';

// ─── Types ────────────────────────────────────────────────────────────────────

export interface SecurityReport {
  isDebuggerAttached: boolean;
  isDeveloperOptionsEnabled: boolean;
  isDeviceCompromised: boolean;
  isFridaDetected: boolean;
  isSSLBypassed: boolean;
  isInstalledFromTrustedSource: boolean;
}

// ─── API ─────────────────────────────────────────────────────────────────────

/**
 * Returns true if a debugger is currently attached.
 * Works on both Android and iOS.
 */
export function isDebuggerAttached(): boolean {
  return NativeArkSecurityManager.isDebuggerAttached();
}

/**
 * Returns true if developer options are enabled on the device.
 * Android only — always returns false on iOS.
 */
export function isDeveloperOptionsEnabled(): boolean {
  return NativeArkSecurityManager.isDeveloperOptionsEnabled();
}

/**
 * Returns true if the device appears to be rooted (Android) or jailbroken (iOS).
 */
export function isDeviceCompromised(): boolean {
  return NativeArkSecurityManager.isDeviceCompromised();
}

/**
 * Returns true if Frida debugging ports are open.
 * Android only — always returns false on iOS.
 */
export function isFridaDetected(): boolean {
  return NativeArkSecurityManager.isFridaDetected();
}

/**
 * Returns true if an SSL bypass tool is detected (Kill Switch, proxy, etc.).
 * iOS only — always returns false on Android.
 */
export function isSSLBypassed(): boolean {
  return NativeArkSecurityManager.isSSLBypassed();
}

/**
 * Returns true if the app was installed from a trusted source.
 * Android: checks known store package names.
 * iOS: validates AppStore / TestFlight distribution.
 */
export function isInstalledFromTrustedSource(): boolean {
  return NativeArkSecurityManager.isInstalledFromTrustedSource();
}

/**
 * Enables or disables screen security (FLAG_SECURE on Android, SecureView on iOS).
 * When enabled, prevents screenshots and screen recording system-wide for the app.
 */
export function setScreenSecure(enable: boolean): void {
  NativeArkSecurityManager.setScreenSecure(enable);
}

/**
 * Returns true if the given location is mocked.
 * Android only — always returns false on iOS.
 * @param isMock Pass the `isMock` / `isFromMockProvider` boolean from your native location object.
 */
export function isLocationMocked(isMock: boolean): boolean {
  return NativeArkSecurityManager.isLocationMocked(isMock);
}

/**
 * Replaces the trusted store list used for installer verification.
 * Android only — no-op on iOS.
 * @param stores Array of installer package names to consider trusted.
 */
export function setTrustedStores(stores: string[]): void {
  NativeArkSecurityManager.setTrustedStores(stores);
}

/**
 * Runs all available security checks and returns a typed report.
 * Convenient for a single-call security gate at app startup.
 */
export function getSecurityReport(): SecurityReport {
  return NativeArkSecurityManager.getSecurityReport() as SecurityReport;
}
