import UIKit
import ARKSecurityManager

/// Swift implementation of the ARK Security Manager RN module.
///
/// This class is called from the ObjC bridge (`ArkSecurityManager.mm`) so it must
/// be annotated `@objc` and inherit from `NSObject`. All methods are `@objc` as well
/// so they are accessible from ObjC without a bridging header.
///
/// The actual security logic is delegated to `ArkSecurityManager` from the native SDK.
@objc(ArkSecurityManagerImpl)
public class ArkSecurityManagerImpl: NSObject {

  private static let sdk = ArkSecurityManager()

  // MARK: - Detection checks

  @objc public static func isDebuggerAttached() -> Bool {
    sdk.getDebugInfo().isDebuggerAttached
  }

  /// Developer options are an Android concept; always returns false.
  @objc public static func isDeveloperOptionsEnabled() -> Bool { false }

  @objc public static func isDeviceCompromised() -> Bool {
    sdk.getJailBreakInfo().isJailbroken
  }

  /// Frida port scanning is Android-only; always returns false.
  @objc public static func isFridaDetected() -> Bool { false }

  @objc public static func isSSLBypassed() -> Bool {
    sdk.getSSLProtectionInfo().hasSSLBypass
  }

  @objc public static func isInstalledFromTrustedSource() -> Bool {
    let info = sdk.getAppValidationInfo()
    // Consider AppStore and TestFlight as trusted sources.
    return info.isAppStore || info.isTestFlight
  }

  // MARK: - Screen protection

  @objc public static func setScreenSecure(_ enable: Bool) {
    DispatchQueue.main.async {
      guard let window = UIApplication.shared.windows.first else { return }
      if enable {
        sdk.preventFromScreenshot(view: window)
        sdk.preventFromScreenRecording(hapticWarning: false) {
          let blocker = UIView()
          blocker.backgroundColor = .black
          return blocker
        }
        sdk.preventFromAppSwitcher {
          let blocker = UIView()
          blocker.backgroundColor = .black
          return blocker
        }
      }
    }
    // Disabling screen secure: no built-in "undo" in the SDK.
    // Callers should manage this at the screen level.
  }

  // MARK: - Composite report

  @objc public static func getSecurityReport() -> [String: Any] {
    let jailbreak = sdk.getJailBreakInfo()
    let debug     = sdk.getDebugInfo()
    let ssl       = sdk.getSSLProtectionInfo()
    let app       = sdk.getAppValidationInfo()

    return [
      "isDebuggerAttached"        : debug.isDebuggerAttached,
      "isDeveloperOptionsEnabled" : false,
      "isDeviceCompromised"       : jailbreak.isJailbroken,
      "isFridaDetected"           : false,
      "isSSLBypassed"             : ssl.hasSSLBypass,
      "isInstalledFromTrustedSource": app.isAppStore || app.isTestFlight,
    ]
  }
}
