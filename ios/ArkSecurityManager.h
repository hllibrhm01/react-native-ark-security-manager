#import <ArkSecurityManagerSpec/ArkSecurityManagerSpec.h>
#import <React/RCTEventEmitter.h>

/// ObjC bridge class that forwards all calls to the Swift implementation.
/// Both Legacy Bridge and New Architecture (TurboModule) are supported:
///  - Legacy: RCT_EXPORT_MODULE in the .mm file registers the module.
///  - New Arch: getTurboModule: returns the Codegen-generated JSI binding.
@interface ArkSecurityManager : RCTEventEmitter <NativeArkSecurityManagerSpec>

@end
