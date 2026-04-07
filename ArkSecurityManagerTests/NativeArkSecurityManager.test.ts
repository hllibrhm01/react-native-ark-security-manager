import NativeArkSecurityManager from '../src/NativeArkSecurityManager';
import { getTestState } from './testUtils';

const testState = getTestState();

describe('NativeArkSecurityManager', () => {
  it('should resolve to the mocked ArkSecurityManager TurboModule', () => {
    expect(NativeArkSecurityManager).toBe(testState.nativeModule);
  });

  it('should expose all expected native bridge methods', () => {
    expect(typeof NativeArkSecurityManager.isDebuggerAttached).toBe('function');
    expect(typeof NativeArkSecurityManager.isDeveloperOptionsEnabled).toBe(
      'function'
    );
    expect(typeof NativeArkSecurityManager.isDeviceCompromised).toBe(
      'function'
    );
    expect(typeof NativeArkSecurityManager.isFridaDetected).toBe('function');
    expect(typeof NativeArkSecurityManager.isSSLBypassed).toBe('function');
    expect(typeof NativeArkSecurityManager.isInstalledFromTrustedSource).toBe(
      'function'
    );
    expect(typeof NativeArkSecurityManager.setScreenSecure).toBe('function');
    expect(typeof NativeArkSecurityManager.applyOverviewProtection).toBe(
      'function'
    );
    expect(typeof NativeArkSecurityManager.startSecurityEventMonitoring).toBe(
      'function'
    );
    expect(typeof NativeArkSecurityManager.stopSecurityEventMonitoring).toBe(
      'function'
    );
    expect(typeof NativeArkSecurityManager.isScreenRecordingActive).toBe(
      'function'
    );
    expect(typeof NativeArkSecurityManager.isLocationMocked).toBe('function');
    expect(typeof NativeArkSecurityManager.setTrustedStores).toBe('function');
    expect(typeof NativeArkSecurityManager.removeTrustedStore).toBe('function');
    expect(typeof NativeArkSecurityManager.getSecurityReport).toBe('function');
  });

  it('should execute native methods directly on the resolved TurboModule', () => {
    testState.nativeModule.isDebuggerAttached.mockReturnValue(true);

    expect(NativeArkSecurityManager.isDebuggerAttached()).toBe(true);
    expect(testState.nativeModule.isDebuggerAttached).toHaveBeenCalledTimes(1);

    NativeArkSecurityManager.setTrustedStores(['com.app.store']);

    expect(testState.nativeModule.setTrustedStores).toHaveBeenCalledWith([
      'com.app.store',
    ]);
  });

  it('should forward report objects without reshaping them', () => {
    const report = {
      isDebuggerAttached: true,
      isDeveloperOptionsEnabled: true,
      isDeviceCompromised: false,
      isFridaDetected: true,
      isInstalledFromTrustedSource: true,
      isSSLBypassed: false,
    };

    testState.nativeModule.getSecurityReport.mockReturnValue(report);

    expect(NativeArkSecurityManager.getSecurityReport()).toBe(report);
  });
});
