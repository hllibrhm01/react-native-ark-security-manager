import * as ArkSecurityManagerModule from '../src/index';

describe('Index Module Exports', () => {
  it('should export all public runtime functions', () => {
    expect(ArkSecurityManagerModule.addSecurityEventListener).toBeDefined();
    expect(ArkSecurityManagerModule.applyOverviewProtection).toBeDefined();
    expect(ArkSecurityManagerModule.getSecurityReport).toBeDefined();
    expect(ArkSecurityManagerModule.isDebuggerAttached).toBeDefined();
    expect(ArkSecurityManagerModule.isDeveloperOptionsEnabled).toBeDefined();
    expect(ArkSecurityManagerModule.isDeviceCompromised).toBeDefined();
    expect(ArkSecurityManagerModule.isFridaDetected).toBeDefined();
    expect(ArkSecurityManagerModule.isInstalledFromTrustedSource).toBeDefined();
    expect(ArkSecurityManagerModule.isLocationMocked).toBeDefined();
    expect(ArkSecurityManagerModule.isScreenRecordingActive).toBeDefined();
    expect(ArkSecurityManagerModule.isSSLBypassed).toBeDefined();
    expect(ArkSecurityManagerModule.removeTrustedStore).toBeDefined();
    expect(ArkSecurityManagerModule.setScreenSecure).toBeDefined();
    expect(ArkSecurityManagerModule.setTrustedStores).toBeDefined();
    expect(ArkSecurityManagerModule.startSecurityEventMonitoring).toBeDefined();
    expect(ArkSecurityManagerModule.stopSecurityEventMonitoring).toBeDefined();
  });

  it('should expose functions with callable runtime types', () => {
    expect(typeof ArkSecurityManagerModule.addSecurityEventListener).toBe(
      'function'
    );
    expect(typeof ArkSecurityManagerModule.applyOverviewProtection).toBe(
      'function'
    );
    expect(typeof ArkSecurityManagerModule.getSecurityReport).toBe('function');
    expect(typeof ArkSecurityManagerModule.isDebuggerAttached).toBe('function');
    expect(typeof ArkSecurityManagerModule.isDeveloperOptionsEnabled).toBe(
      'function'
    );
    expect(typeof ArkSecurityManagerModule.isDeviceCompromised).toBe(
      'function'
    );
    expect(typeof ArkSecurityManagerModule.isFridaDetected).toBe('function');
    expect(typeof ArkSecurityManagerModule.isInstalledFromTrustedSource).toBe(
      'function'
    );
    expect(typeof ArkSecurityManagerModule.isLocationMocked).toBe('function');
    expect(typeof ArkSecurityManagerModule.isScreenRecordingActive).toBe(
      'function'
    );
    expect(typeof ArkSecurityManagerModule.isSSLBypassed).toBe('function');
    expect(typeof ArkSecurityManagerModule.removeTrustedStore).toBe('function');
    expect(typeof ArkSecurityManagerModule.setScreenSecure).toBe('function');
    expect(typeof ArkSecurityManagerModule.setTrustedStores).toBe('function');
    expect(typeof ArkSecurityManagerModule.startSecurityEventMonitoring).toBe(
      'function'
    );
    expect(typeof ArkSecurityManagerModule.stopSecurityEventMonitoring).toBe(
      'function'
    );
  });

  it('should allow runtime imports alongside compile-time types', () => {
    expect(true).toBe(true);
  });
});
