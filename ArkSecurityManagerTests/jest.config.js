module.exports = {
    preset: 'react-native',
    testEnvironment: 'node',
    moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx', 'json'],
    transform: {
        '^.+\\.(ts|tsx)$': 'ts-jest',
        '^.+\\.(js|jsx)$': 'babel-jest',
    },
    transformIgnorePatterns: [
        'node_modules/(?!(react-native|@react-native|react-native-ark-security-manager)/)',
    ],
    testMatch: ['**/*.(test|spec).(ts|tsx|js)'],
    testPathIgnorePatterns: ['/node_modules/', '.*\\.example\\.(js|ts)$'],
    moduleNameMapper: {
        '^react-native-ark-security-manager$': '<rootDir>/../src/index.tsx',
    },
    setupFilesAfterEnv: ['<rootDir>/setupTests.ts'],
    collectCoverageFrom: ['../src/**/*.{ts,tsx}', '!../src/**/*.d.ts'],
    coverageThreshold: {
        global: {
            branches: 95,
            functions: 95,
            lines: 95,
            statements: 95,
        },
    },
    coverageDirectory: 'coverage',
    coverageReporters: ['text', 'lcov', 'html'],
    verbose: true,
};
