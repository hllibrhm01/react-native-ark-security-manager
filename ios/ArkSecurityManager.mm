#import "ArkSecurityManager.h"
#import <React/RCTBridgeModule.h>

// Import the Swift implementation. CocoaPods generates this header from the
// pod target's PRODUCT_MODULE_NAME, which is set to RNArkSecurityManager to
// avoid colliding with the ARKSecurityManager SDK dependency.
#import "RNArkSecurityManager-Swift.h"

static NSString *const ArkSecurityManagerSecurityEventName = @"ArkSecurityManager:securityEvent";

@interface ArkSecurityManager ()

@property (nonatomic, assign) BOOL hasListeners;

@end

@implementation ArkSecurityManager

// ── Module registration ────────────────────────────────────────────────────
// RCT_EXPORT_MODULE registers the module for the Legacy Bridge.
// The New Architecture uses the Codegen-generated TurboModule binding below.
RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup {
  return YES;
}

- (NSArray<NSString *> *)supportedEvents {
  return @[ ArkSecurityManagerSecurityEventName ];
}

- (void)startObserving {
  self.hasListeners = YES;
}

- (void)stopObserving {
  self.hasListeners = NO;
}

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
  [ArkSecurityManagerImpl setScreenSecure:enable];
}

- (void)applyOverviewProtection:(BOOL)useBlur {
  [ArkSecurityManagerImpl applyOverviewProtection:useBlur];
}

- (void)startSecurityEventMonitoring {
  __weak ArkSecurityManager *weakSelf = self;
  [ArkSecurityManagerImpl startSecurityEventMonitoring:^(NSDictionary *event) {
    ArkSecurityManager *strongSelf = weakSelf;
    if (strongSelf != nil && strongSelf.hasListeners) {
      [strongSelf sendEventWithName:ArkSecurityManagerSecurityEventName body:event];
    }
  }];
}

- (void)stopSecurityEventMonitoring {
  [ArkSecurityManagerImpl stopSecurityEventMonitoring];
}

- (NSNumber *)isScreenRecordingActive {
  return @([ArkSecurityManagerImpl isScreenRecordingActive]);
}

// ── Android-specific stubs ─────────────────────────────────────────────────

- (NSNumber *)isLocationMocked:(BOOL)isMock {
  // Location mocking check is Android-only; always false on iOS.
  return @NO;
}

- (void)setTrustedStores:(NSArray<NSString *> *)stores {
  // Trusted store list is Android-only; no-op on iOS.
}

- (void)removeTrustedStore:(NSString *)store {
  // Trusted store management is Android-only; no-op on iOS.
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
