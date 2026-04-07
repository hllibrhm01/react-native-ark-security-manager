import { getTestState } from './testUtils';

const testState = getTestState();

jest.mock('react-native', () => ({
  NativeModules: {
    ArkSecurityManager: testState.nativeModule,
  },
  NativeEventEmitter: testState.nativeEventEmitterCtor,
  Platform: testState.platform,
  TurboModuleRegistry: {
    getEnforcing: testState.turboModuleRegistryGetEnforcing,
  },
}));

const originalConsole = console;

beforeAll(() => {
  globalThis.console = {
    ...originalConsole,
    error: jest.fn(),
    log: jest.fn(),
    warn: jest.fn(),
  };
});

afterAll(() => {
  globalThis.console = originalConsole;
});

beforeEach(() => {
  testState.reset();
});

export {};
