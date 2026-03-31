#import "ArkSecurityManager.h"
#import <React/RCTBridgeModule.h>

// Import the Swift implementation. CocoaPods generates this header from the
// pod target's PRODUCT_MODULE_NAME, which is set to RNArkSecurityManager to
// avoid colliding with the ARKSecurityManager SDK dependency.
#import "RNArkSecurityManager-Swift.h"

@implementation ArkSecurityManager

// ── Module registration ────────────────────────────────────────────────────
// RCT_EXPORT_MODULE registers the module for the Legacy Bridge.
// The New Architecture uses the Codegen-generated TurboModule binding below.
RCT_EXPORT_MODULE()

// ── Detection checks ───────────────────────────────────────────────────────

- (NSNumber *)isDebuggerAttached {
  return @([ArkSecurityManagerImpl isDebuggerAttached]);
}

- (NSNumber *)isDeveloperOptionsEnabled {
  // Developer options are an Android concept; always false on iOS.
  return @NO;
}

- (NSNumber *)isDeviceCompromised {
  return @([ArkSecurityManagerImpl isDeviceCompromised]);
}

- (NSNumber *)isFridaDetected {
  // Frida port scanning is Android-only; always false on iOS.
  return @NO;
}

- (NSNumber *)isSSLBypassed {
  return @([ArkSecurityManagerImpl isSSLBypassed]);
}

- (NSNumber *)isInstalledFromTrustedSource {
  return @([ArkSecurityManagerImpl isInstalledFromTrustedSource]);
}

// ── Screen protection ──────────────────────────────────────────────────────

- (void)setScreenSecure:(BOOL)enable {
  dispatch_async(dispatch_get_main_queue(), ^{
    [ArkSecurityManagerImpl setScreenSecure:enable];
  });
}

// ── Android-specific stubs ─────────────────────────────────────────────────

- (NSNumber *)isLocationMocked:(BOOL)isMock {
  // Location mocking check is Android-only; always false on iOS.
  return @NO;
}

- (void)setTrustedStores:(NSArray<NSString *> *)stores {
  // Trusted store list is Android-only; no-op on iOS.
}

// ── Composite report ───────────────────────────────────────────────────────

- (NSDictionary *)getSecurityReport {
  return [ArkSecurityManagerImpl getSecurityReport];
}

// ── New Architecture (TurboModule) binding ─────────────────────────────────

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params {
  return std::make_shared<facebook::react::NativeArkSecurityManagerSpecJSI>(params);
}

@end
