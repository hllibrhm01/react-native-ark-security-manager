type PlatformOS = 'android' | 'ios';
type EventHandler = (event: unknown) => void;

export interface MockEventSubscription {
  remove: jest.Mock<void, []>;
}

export interface MockNativeEventEmitterInstance {
  addListener: jest.Mock<MockEventSubscription, [string, EventHandler]>;
  removeAllListeners: jest.Mock<void, [string?]>;
}

export interface ArkSecurityManagerNativeModuleMock {
  isDebuggerAttached: jest.Mock<boolean, []>;
  isDeveloperOptionsEnabled: jest.Mock<boolean, []>;
  isDeviceCompromised: jest.Mock<boolean, []>;
  isFridaDetected: jest.Mock<boolean, []>;
  isSSLBypassed: jest.Mock<boolean, []>;
  isInstalledFromTrustedSource: jest.Mock<boolean, []>;
  setScreenSecure: jest.Mock<void, [boolean]>;
  applyOverviewProtection: jest.Mock<void, [boolean]>;
  startSecurityEventMonitoring: jest.Mock<void, []>;
  stopSecurityEventMonitoring: jest.Mock<void, []>;
  isScreenRecordingActive: jest.Mock<boolean, []>;
  isLocationMocked: jest.Mock<boolean, [boolean]>;
  setTrustedStores: jest.Mock<void, [string[]]>;
  removeTrustedStore: jest.Mock<void, [string]>;
  getSecurityReport: jest.Mock<Record<string, boolean>, []>;
  addListener: jest.Mock<void, [string]>;
  removeListeners: jest.Mock<void, [number]>;
}

export interface ArkSecurityManagerTestState {
  nativeModule: ArkSecurityManagerNativeModuleMock;
  platform: {
    OS: PlatformOS;
    select: jest.Mock<unknown, [Record<string, unknown>]>;
  };
  nativeEventEmitterCtor: jest.Mock<MockNativeEventEmitterInstance, [unknown?]>;
  turboModuleRegistryGetEnforcing: jest.Mock<
    ArkSecurityManagerNativeModuleMock,
    [string]
  >;
  emit: (eventName: string, payload: unknown) => void;
  getEmitterInstance: () => MockNativeEventEmitterInstance;
  setPlatform: (os: PlatformOS) => void;
  reset: () => void;
}

declare global {
  var __ARK_SECURITY_MANAGER_TEST_STATE__:
    | ArkSecurityManagerTestState
    | undefined;
}

function createDefaultSecurityReport(): Record<string, boolean> {
  return {
    isDebuggerAttached: false,
    isDeveloperOptionsEnabled: false,
    isDeviceCompromised: false,
    isFridaDetected: false,
    isInstalledFromTrustedSource: false,
    isSSLBypassed: false,
  };
}

function createNativeModule(): ArkSecurityManagerNativeModuleMock {
  return {
    isDebuggerAttached: jest.fn<boolean, []>(),
    isDeveloperOptionsEnabled: jest.fn<boolean, []>(),
    isDeviceCompromised: jest.fn<boolean, []>(),
    isFridaDetected: jest.fn<boolean, []>(),
    isSSLBypassed: jest.fn<boolean, []>(),
    isInstalledFromTrustedSource: jest.fn<boolean, []>(),
    setScreenSecure: jest.fn<void, [boolean]>(),
    applyOverviewProtection: jest.fn<void, [boolean]>(),
    startSecurityEventMonitoring: jest.fn<void, []>(),
    stopSecurityEventMonitoring: jest.fn<void, []>(),
    isScreenRecordingActive: jest.fn<boolean, []>(),
    isLocationMocked: jest.fn<boolean, [boolean]>(),
    setTrustedStores: jest.fn<void, [string[]]>(),
    removeTrustedStore: jest.fn<void, [string]>(),
    getSecurityReport: jest.fn<Record<string, boolean>, []>(),
    addListener: jest.fn<void, [string]>(),
    removeListeners: jest.fn<void, [number]>(),
  };
}

function applyNativeModuleDefaults(
  nativeModule: ArkSecurityManagerNativeModuleMock
) {
  nativeModule.isDebuggerAttached.mockReset().mockReturnValue(false);
  nativeModule.isDeveloperOptionsEnabled.mockReset().mockReturnValue(false);
  nativeModule.isDeviceCompromised.mockReset().mockReturnValue(false);
  nativeModule.isFridaDetected.mockReset().mockReturnValue(false);
  nativeModule.isSSLBypassed.mockReset().mockReturnValue(false);
  nativeModule.isInstalledFromTrustedSource.mockReset().mockReturnValue(false);
  nativeModule.setScreenSecure.mockReset();
  nativeModule.applyOverviewProtection.mockReset();
  nativeModule.startSecurityEventMonitoring.mockReset();
  nativeModule.stopSecurityEventMonitoring.mockReset();
  nativeModule.isScreenRecordingActive.mockReset().mockReturnValue(false);
  nativeModule.isLocationMocked
    .mockReset()
    .mockImplementation((isMock) => isMock);
  nativeModule.setTrustedStores.mockReset();
  nativeModule.removeTrustedStore.mockReset();
  nativeModule.getSecurityReport
    .mockReset()
    .mockImplementation(() => createDefaultSecurityReport());
  nativeModule.addListener.mockReset();
  nativeModule.removeListeners.mockReset();
}

function createEventEmitterInstance(
  listeners: Map<string, Set<EventHandler>>
): MockNativeEventEmitterInstance {
  return {
    addListener: jest.fn((eventName: string, handler: EventHandler) => {
      const handlers = listeners.get(eventName) ?? new Set<EventHandler>();
      handlers.add(handler);
      listeners.set(eventName, handlers);

      return {
        remove: jest.fn(() => {
          handlers.delete(handler);
          if (handlers.size === 0) {
            listeners.delete(eventName);
          }
        }),
      };
    }),
    removeAllListeners: jest.fn((eventName?: string) => {
      if (eventName) {
        listeners.delete(eventName);
        return;
      }

      listeners.clear();
    }),
  };
}

function createTestState(): ArkSecurityManagerTestState {
  const listeners = new Map<string, Set<EventHandler>>();
  const emitterInstances: MockNativeEventEmitterInstance[] = [];
  const nativeModule = createNativeModule();

  const platform = {
    OS: 'ios' as PlatformOS,
    select: jest.fn<unknown, [Record<string, unknown>]>(
      (options: Record<string, unknown>) => options.ios ?? options.default
    ),
  };

  const testState = {
    nativeModule,
    platform,
    nativeEventEmitterCtor: jest.fn<
      MockNativeEventEmitterInstance,
      [unknown?]
    >(),
    turboModuleRegistryGetEnforcing: jest.fn<
      ArkSecurityManagerNativeModuleMock,
      [string]
    >(),
    emit(eventName: string, payload: unknown) {
      const handlers = listeners.get(eventName);
      if (!handlers) {
        return;
      }

      Array.from(handlers).forEach((handler) => {
        handler(payload);
      });
    },
    getEmitterInstance() {
      const instance = emitterInstances[emitterInstances.length - 1];

      if (!instance) {
        throw new Error('No NativeEventEmitter instance has been created yet.');
      }

      return instance;
    },
    setPlatform(os: PlatformOS) {
      platform.OS = os;
      platform.select
        .mockReset()
        .mockImplementation(
          (options: Record<string, unknown>) => options[os] ?? options.default
        );
    },
    reset() {
      applyNativeModuleDefaults(nativeModule);
      listeners.clear();
      testState.nativeEventEmitterCtor.mockClear();
      testState.turboModuleRegistryGetEnforcing.mockClear();
      emitterInstances.forEach((instance) => {
        instance.addListener.mockClear();
        instance.removeAllListeners.mockClear();
      });
      testState.setPlatform('ios');
    },
  } satisfies ArkSecurityManagerTestState;

  testState.nativeEventEmitterCtor.mockImplementation(() => {
    const instance = createEventEmitterInstance(listeners);
    emitterInstances.push(instance);
    return instance;
  });

  testState.turboModuleRegistryGetEnforcing.mockImplementation(
    (moduleName: string) => {
      if (moduleName !== 'ArkSecurityManager') {
        throw new Error(`Unexpected TurboModule request: ${moduleName}`);
      }

      return nativeModule;
    }
  );

  testState.reset();

  return testState;
}

export function getTestState(): ArkSecurityManagerTestState {
  if (!globalThis.__ARK_SECURITY_MANAGER_TEST_STATE__) {
    globalThis.__ARK_SECURITY_MANAGER_TEST_STATE__ = createTestState();
  }

  return globalThis.__ARK_SECURITY_MANAGER_TEST_STATE__;
}
