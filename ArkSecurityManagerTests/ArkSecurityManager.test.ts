import * as ArkSecurityManager from '../src/index';
import { getTestState } from './testUtils';

const testState = getTestState();

describe('ARK Security Manager', () => {
  describe('Detection checks', () => {
    it('should return the native debugger state', () => {
      testState.nativeModule.isDebuggerAttached.mockReturnValue(true);

      expect(ArkSecurityManager.isDebuggerAttached()).toBe(true);
      expect(testState.nativeModule.isDebuggerAttached).toHaveBeenCalledTimes(
        1
      );
    });

    it('should return the native developer options state', () => {
      testState.nativeModule.isDeveloperOptionsEnabled.mockReturnValue(true);

      expect(ArkSecurityManager.isDeveloperOptionsEnabled()).toBe(true);
      expect(
        testState.nativeModule.isDeveloperOptionsEnabled
      ).toHaveBeenCalledTimes(1);
    });

    it('should return the native device compromise state', () => {
      testState.nativeModule.isDeviceCompromised.mockReturnValue(true);

      expect(ArkSecurityManager.isDeviceCompromised()).toBe(true);
      expect(testState.nativeModule.isDeviceCompromised).toHaveBeenCalledTimes(
        1
      );
    });

    it('should return the native Frida detection state', () => {
      testState.nativeModule.isFridaDetected.mockReturnValue(true);

      expect(ArkSecurityManager.isFridaDetected()).toBe(true);
      expect(testState.nativeModule.isFridaDetected).toHaveBeenCalledTimes(1);
    });

    it('should return the native SSL bypass state', () => {
      testState.nativeModule.isSSLBypassed.mockReturnValue(true);

      expect(ArkSecurityManager.isSSLBypassed()).toBe(true);
      expect(testState.nativeModule.isSSLBypassed).toHaveBeenCalledTimes(1);
    });

    it('should return the native trusted source state', () => {
      testState.nativeModule.isInstalledFromTrustedSource.mockReturnValue(true);

      expect(ArkSecurityManager.isInstalledFromTrustedSource()).toBe(true);
      expect(
        testState.nativeModule.isInstalledFromTrustedSource
      ).toHaveBeenCalledTimes(1);
    });
  });

  describe('Screen protection', () => {
    it('should delegate setScreenSecure enable and disable calls', () => {
      ArkSecurityManager.setScreenSecure(true);
      ArkSecurityManager.setScreenSecure(false);

      expect(testState.nativeModule.setScreenSecure).toHaveBeenNthCalledWith(
        1,
        true
      );
      expect(testState.nativeModule.setScreenSecure).toHaveBeenNthCalledWith(
        2,
        false
      );
    });

    it('should use true as the default overview protection argument', () => {
      ArkSecurityManager.applyOverviewProtection();

      expect(
        testState.nativeModule.applyOverviewProtection
      ).toHaveBeenCalledWith(true);
    });

    it('should forward an explicit overview protection argument', () => {
      ArkSecurityManager.applyOverviewProtection(false);

      expect(
        testState.nativeModule.applyOverviewProtection
      ).toHaveBeenCalledWith(false);
    });
  });

  describe('Android-specific helpers', () => {
    it('should forward mocked location checks', () => {
      testState.nativeModule.isLocationMocked.mockImplementation(
        (isMock) => isMock
      );

      expect(ArkSecurityManager.isLocationMocked(true)).toBe(true);
      expect(ArkSecurityManager.isLocationMocked(false)).toBe(false);
      expect(testState.nativeModule.isLocationMocked).toHaveBeenNthCalledWith(
        1,
        true
      );
      expect(testState.nativeModule.isLocationMocked).toHaveBeenNthCalledWith(
        2,
        false
      );
    });

    it('should delegate trusted store replacement', () => {
      const stores = ['com.android.vending', 'com.huawei.appmarket'];

      ArkSecurityManager.setTrustedStores(stores);

      expect(testState.nativeModule.setTrustedStores).toHaveBeenCalledWith(
        stores
      );
    });

    it('should delegate trusted store removal', () => {
      ArkSecurityManager.removeTrustedStore('com.example.store');

      expect(testState.nativeModule.removeTrustedStore).toHaveBeenCalledWith(
        'com.example.store'
      );
    });
  });

  describe('Composite report', () => {
    it('should return the native security report shape unchanged', () => {
      const report = {
        isDebuggerAttached: true,
        isDeveloperOptionsEnabled: false,
        isDeviceCompromised: true,
        isFridaDetected: false,
        isInstalledFromTrustedSource: true,
        isSSLBypassed: true,
      };

      testState.nativeModule.getSecurityReport.mockReturnValue(report);

      expect(ArkSecurityManager.getSecurityReport()).toEqual(report);
      expect(ArkSecurityManager.getSecurityReport()).toBe(report);
    });
  });
});
