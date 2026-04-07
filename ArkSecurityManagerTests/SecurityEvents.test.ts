import {
  addSecurityEventListener,
  isScreenRecordingActive,
  startSecurityEventMonitoring,
  stopSecurityEventMonitoring,
} from '../src/index';
import { getTestState } from './testUtils';

const SECURITY_EVENT_NAME = 'ArkSecurityManager:securityEvent';
const testState = getTestState();

describe('Security event monitoring', () => {
  it('should delegate monitoring start and stop to the native module', () => {
    startSecurityEventMonitoring();
    stopSecurityEventMonitoring();

    expect(
      testState.nativeModule.startSecurityEventMonitoring
    ).toHaveBeenCalledTimes(1);
    expect(
      testState.nativeModule.stopSecurityEventMonitoring
    ).toHaveBeenCalledTimes(1);
  });

  it('should return the native screen recording state', () => {
    testState.nativeModule.isScreenRecordingActive.mockReturnValue(true);

    expect(isScreenRecordingActive()).toBe(true);
    expect(
      testState.nativeModule.isScreenRecordingActive
    ).toHaveBeenCalledTimes(1);
  });

  it('should subscribe to screenshot events and forward payloads unchanged', () => {
    const listener = jest.fn();
    const subscription = addSecurityEventListener(listener);
    const emitter = testState.getEmitterInstance();
    const screenshotEvent = {
      type: 'screenshotTaken' as const,
      platform: 'ios' as const,
      timestamp: Date.now(),
    };

    expect(emitter.addListener).toHaveBeenCalledWith(
      SECURITY_EVENT_NAME,
      expect.any(Function)
    );

    testState.emit(SECURITY_EVENT_NAME, screenshotEvent);

    expect(listener).toHaveBeenCalledWith(screenshotEvent);

    subscription.remove();
  });

  it('should subscribe to screen recording events and forward payloads unchanged', () => {
    const listener = jest.fn();
    addSecurityEventListener(listener);
    const screenRecordingEvent = {
      type: 'screenRecordingChanged' as const,
      platform: 'ios' as const,
      timestamp: Date.now(),
      isRecording: true,
    };

    testState.emit(SECURITY_EVENT_NAME, screenRecordingEvent);

    expect(listener).toHaveBeenCalledWith(screenRecordingEvent);
  });

  it('should stop forwarding events after subscription removal', () => {
    const listener = jest.fn();
    const subscription = addSecurityEventListener(listener);
    const firstEvent = {
      type: 'screenshotTaken' as const,
      platform: 'ios' as const,
      timestamp: Date.now(),
    };
    const secondEvent = {
      type: 'screenRecordingChanged' as const,
      platform: 'ios' as const,
      timestamp: Date.now() + 1,
      isRecording: false,
    };

    testState.emit(SECURITY_EVENT_NAME, firstEvent);
    subscription.remove();
    testState.emit(SECURITY_EVENT_NAME, secondEvent);

    expect(listener).toHaveBeenCalledTimes(1);
    expect(listener).toHaveBeenCalledWith(firstEvent);
  });
});
