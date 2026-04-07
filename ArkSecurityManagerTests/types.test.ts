import type {
  SecurityEvent,
  SecurityEventSubscription,
  SecurityReport,
} from '../src/index';

describe('Security Manager Types', () => {
  it('should accept a complete SecurityReport shape', () => {
    const report: SecurityReport = {
      isDebuggerAttached: false,
      isDeveloperOptionsEnabled: false,
      isDeviceCompromised: false,
      isFridaDetected: false,
      isInstalledFromTrustedSource: true,
      isSSLBypassed: false,
    };

    expect(report).toEqual({
      isDebuggerAttached: false,
      isDeveloperOptionsEnabled: false,
      isDeviceCompromised: false,
      isFridaDetected: false,
      isInstalledFromTrustedSource: true,
      isSSLBypassed: false,
    });
  });

  it('should accept screenshotTaken event payloads', () => {
    const event: SecurityEvent = {
      type: 'screenshotTaken',
      platform: 'ios',
      timestamp: Date.now(),
    };

    expect(event.type).toBe('screenshotTaken');
    expect(event.platform).toBe('ios');
    expect('isRecording' in event).toBe(false);
  });

  it('should accept screenRecordingChanged event payloads', () => {
    const event: SecurityEvent = {
      type: 'screenRecordingChanged',
      platform: 'ios',
      timestamp: Date.now(),
      isRecording: true,
    };

    expect(event.type).toBe('screenRecordingChanged');
    expect(event.isRecording).toBe(true);
  });

  it('should model subscriptions with a remove function', () => {
    const subscription: SecurityEventSubscription = {
      remove: jest.fn(),
    };

    subscription.remove();

    expect(subscription.remove).toHaveBeenCalledTimes(1);
  });
});
