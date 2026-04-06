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
  private static var screenshotObserver: NSObjectProtocol?
  private static var screenRecordingObserver: NSObjectProtocol?
  private static var securityEventHandler: (([String: Any]) -> Void)?
  private static var isMonitoringSecurityEvents = false
  private static var hasAppliedScreenshotProtection = false

  /// Tag used to identify blocker views added by screen protection.
  private static let blockerViewTag = 0xA2C5EC

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
      guard let window = ArkSecurityManagerImpl.keyWindow() else { return }
      if enable {
        if !ArkSecurityManagerImpl.hasAppliedScreenshotProtection {
          ArkSecurityManagerImpl.sdk.preventFromScreenshot(view: window)
          ArkSecurityManagerImpl.hasAppliedScreenshotProtection = true
        }
        ArkSecurityManagerImpl.sdk.preventFromScreenRecording(hapticWarning: false) {
          let blocker = UIView()
          blocker.backgroundColor = .black
          blocker.tag = ArkSecurityManagerImpl.blockerViewTag
          return blocker
        }
        ArkSecurityManagerImpl.sdk.preventFromAppSwitcher {
          let blocker = UIView()
          blocker.backgroundColor = .black
          blocker.tag = ArkSecurityManagerImpl.blockerViewTag
          return blocker
        }
      } else {
        // Remove blocker views added by screen recording and app switcher protection.
        ArkSecurityManagerImpl.removeBlockerViews(from: window)
        ArkSecurityManagerImpl.hasAppliedScreenshotProtection = false
      }
    }
  }

  @objc public static func applyOverviewProtection(_ useBlur: Bool) {
    _ = useBlur
    // Android-only feature. Kept as a no-op on iOS for a shared JS API.
  }

  @objc public static func startSecurityEventMonitoring(
    _ handler: @escaping ([String: Any]) -> Void
  ) {
    DispatchQueue.main.async {
      ArkSecurityManagerImpl.securityEventHandler = handler

      if !ArkSecurityManagerImpl.isMonitoringSecurityEvents {
        ArkSecurityManagerImpl.screenshotObserver = NotificationCenter.default.addObserver(
          forName: UIApplication.userDidTakeScreenshotNotification,
          object: nil,
          queue: .main
        ) { _ in
          ArkSecurityManagerImpl.emitSecurityEvent(
            type: "screenshotTaken"
          )
        }

        ArkSecurityManagerImpl.screenRecordingObserver = NotificationCenter.default.addObserver(
          forName: UIScreen.capturedDidChangeNotification,
          object: nil,
          queue: .main
        ) { _ in
          ArkSecurityManagerImpl.emitScreenRecordingEvent()
        }

        ArkSecurityManagerImpl.isMonitoringSecurityEvents = true
      }

      ArkSecurityManagerImpl.emitScreenRecordingEvent()
    }
  }

  @objc public static func stopSecurityEventMonitoring() {
    DispatchQueue.main.async {
      if let observer = ArkSecurityManagerImpl.screenshotObserver {
        NotificationCenter.default.removeObserver(observer)
        ArkSecurityManagerImpl.screenshotObserver = nil
      }

      if let observer = ArkSecurityManagerImpl.screenRecordingObserver {
        NotificationCenter.default.removeObserver(observer)
        ArkSecurityManagerImpl.screenRecordingObserver = nil
      }

      ArkSecurityManagerImpl.securityEventHandler = nil
      ArkSecurityManagerImpl.isMonitoringSecurityEvents = false
    }
  }

  @objc public static func isScreenRecordingActive() -> Bool {
    guard Thread.isMainThread else {
      return DispatchQueue.main.sync {
        return ArkSecurityManagerImpl.isScreenRecordingActive()
      }
    }
    if let windowScene = UIApplication.shared.connectedScenes
      .compactMap({ $0 as? UIWindowScene })
      .first(where: { $0.activationState == .foregroundActive }) {
      return windowScene.screen.isCaptured
    }

    return false
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

  // MARK: - Private helpers

  private static func removeBlockerViews(from window: UIWindow) {
    for subview in window.subviews where subview.tag == blockerViewTag {
      subview.removeFromSuperview()
    }
    for subview in window.subviews {
      removeBlockerViewsRecursive(from: subview)
    }
  }

  private static func removeBlockerViewsRecursive(from view: UIView) {
    for subview in view.subviews {
      if subview.tag == blockerViewTag {
        subview.removeFromSuperview()
      } else {
        removeBlockerViewsRecursive(from: subview)
      }
    }
  }

  private static func emitSecurityEvent(
    type: String,
    extra: [String: Any] = [:]
  ) {
    var payload: [String: Any] = [
      "type": type,
      "platform": "ios",
      "timestamp": Int(Date().timeIntervalSince1970 * 1000),
    ]

    extra.forEach { key, value in
      payload[key] = value
    }

    ArkSecurityManagerImpl.securityEventHandler?(payload)
  }

  private static func emitScreenRecordingEvent() {
    ArkSecurityManagerImpl.emitSecurityEvent(
      type: "screenRecordingChanged",
      extra: ["isRecording": ArkSecurityManagerImpl.isScreenRecordingActive()]
    )
  }

  private static func keyWindow() -> UIWindow? {
    UIApplication.shared.connectedScenes
      .compactMap { $0 as? UIWindowScene }
      .flatMap { $0.windows }
      .first(where: \.isKeyWindow)
  }
}
