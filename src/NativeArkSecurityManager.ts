import { TurboModuleRegistry, type TurboModule } from 'react-native';

// ─── Model types ──────────────────────────────────────────────────────────────
// Codegen only supports primitive types and simple object specs via UnsafeObject.
// We use UnsafeObject here and provide typed wrappers in index.ts.

export interface Spec extends TurboModule {
  // ── Detection checks ──────────────────────────────────────────────────────

  /** Returns true if a debugger is currently attached. */
  isDebuggerAttached(): boolean;

  /** Returns true if developer options are enabled (Android only, iOS: always false). */
  isDeveloperOptionsEnabled(): boolean;

  /**
   * Returns true if the device appears to be rooted (Android) or jailbroken (iOS).
   * Heuristic-based — may produce false positives on custom ROMs.
   */
  isDeviceCompromised(): boolean;

  /**
   * Returns true if Frida ports are open (Android only).
   * iOS: always returns false.
   */
  isFridaDetected(): boolean;

  /**
   * Returns true if an SSL bypass tool (Kill Switch, proxy, etc.) is detected.
   * iOS only — Android: always returns false.
   */
  isSSLBypassed(): boolean;

  /**
   * Returns true if the app was installed from a trusted store.
   * Android: checks against known store package names.
   * iOS: checks AppStore / TestFlight / Dev build type.
   */
  isInstalledFromTrustedSource(): boolean;

  // ── Screen protection ─────────────────────────────────────────────────────

  /**
   * Enables or disables FLAG_SECURE (Android) / secure view (iOS).
   * Prevents screenshots and screen recording.
   */
  setScreenSecure(enable: boolean): void;

  /**
   * Applies overview / app switcher protection.
   * Android: Android 12+ blur overlay, older versions fall back to FLAG_SECURE.
   * iOS: no-op.
   */
  applyOverviewProtection(useBlur: boolean): void;

  /**
   * Starts native screenshot and screen-recording monitoring.
   * iOS emits events through NativeEventEmitter.
   * Android: no-op.
   */
  startSecurityEventMonitoring(): void;

  /**
   * Stops native screenshot and screen-recording monitoring.
   * iOS removes registered observers.
   * Android: no-op.
   */
  stopSecurityEventMonitoring(): void;

  /**
   * Returns whether the screen is currently being recorded.
   * iOS uses UIScreen capture state.
   * Android currently always returns false.
   */
  isScreenRecordingActive(): boolean;

  // ── Android-specific ─────────────────────────────────────────────────────

  /**
   * Returns true if the given location appears to be mocked.
   * Android only — iOS: always returns false.
   * @param latitude  Latitude of the location to check.
   * @param longitude Longitude of the location to check.
   * @param isMock    Pass the isMock / isFromMockProvider value from the native Location object.
   */
  isLocationMocked(isMock: boolean): boolean;

  /**
   * Replaces the trusted store list used by isInstalledFromTrustedSource (Android only).
   * @param stores Array of installer package names considered trusted.
   */
  setTrustedStores(stores: string[]): void;

  /**
   * Removes a single store from the trusted store list (Android only).
   * iOS: no-op.
   * @param store The installer package name to remove.
   */
  removeTrustedStore(store: string): void;

  // ── Composite report ──────────────────────────────────────────────────────

  /**
   * Runs all available checks and returns a plain-object report.
   * Returned as UnsafeObject; typed wrapper is in index.ts.
   */
  getSecurityReport(): Object;

  /**
   * Required by NativeEventEmitter.
   */
  addListener(eventName: string): void;

  /**
   * Required by NativeEventEmitter.
   */
  removeListeners(count: number): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('ArkSecurityManager');
