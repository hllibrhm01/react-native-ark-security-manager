import { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Platform,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import {
  addSecurityEventListener,
  applyOverviewProtection,
  getSecurityReport,
  isDebuggerAttached,
  isDeveloperOptionsEnabled,
  isDeviceCompromised,
  isFridaDetected,
  isInstalledFromTrustedSource,
  isScreenRecordingActive,
  isSSLBypassed,
  removeTrustedStore,
  setScreenSecure,
  startSecurityEventMonitoring,
  stopSecurityEventMonitoring,
  type SecurityEvent,
  type SecurityReport,
} from 'react-native-ark-security-manager';

const MAX_EVENT_LOGS = 8;
const isAndroid = Platform.OS === 'android';
const isIOS = Platform.OS === 'ios';

type CheckResult = { value: boolean; timestamp: number };
type CheckResults = Record<string, CheckResult>;
type ActionLog = { key: string; message: string; timestamp: number };

export default function App() {
  const [report, setReport] = useState<SecurityReport | null>(null);
  const [screenSecure, setScreenSecureState] = useState(false);
  const [loading, setLoading] = useState(false);
  const [checkResults, setCheckResults] = useState<CheckResults>({});
  const [actionLogs, setActionLogs] = useState<ActionLog[]>([]);
  const [securityEvents, setSecurityEvents] = useState<SecurityEvent[]>([]);
  const [monitoringActive, setMonitoringActive] = useState(false);
  const [screenRecordingActive, setScreenRecordingState] = useState(false);

  const runChecks = () => {
    setLoading(true);
    try {
      setReport(getSecurityReport());
    } finally {
      setLoading(false);
    }
  };

  const addLog = useCallback((key: string, message: string) => {
    setActionLogs((prev) => [{ key, message, timestamp: Date.now() }, ...prev].slice(0, 20));
  }, []);

  const runSingleCheck = useCallback(
    (key: string, value: boolean) => {
      setCheckResults((prev) => ({ ...prev, [key]: { value, timestamp: Date.now() } }));
    },
    []
  );

  const handleSetScreenSecure = (enable: boolean) => {
    setScreenSecure(enable);
    setScreenSecureState(enable);
    addLog(
      'screenSecure',
      enable ? 'Screen secure enabled' : 'Screen secure disabled'
    );
  };

  const handleOverviewProtection = () => {
    applyOverviewProtection(true);
    addLog(
      'overviewProtection',
      'Overview protection applied (blur on Android 12+, FLAG_SECURE fallback).'
    );
  };

  const startMonitoring = () => {
    startSecurityEventMonitoring();
    setMonitoringActive(true);
    setScreenRecordingState(isScreenRecordingActive());
    addLog('monitoring', 'Screenshot and screen recording monitoring started.');
  };

  const stopMonitoring = () => {
    stopSecurityEventMonitoring();
    setMonitoringActive(false);
    addLog('monitoring', 'Monitoring stopped.');
  };

  useEffect(() => {
    runChecks();
    setScreenRecordingState(isScreenRecordingActive());

    const subscription = addSecurityEventListener((event) => {
      setSecurityEvents((current) => [event, ...current].slice(0, MAX_EVENT_LOGS));
      if (event.type === 'screenRecordingChanged') {
        setScreenRecordingState(event.isRecording);
      }
    });

    if (Platform.OS === 'ios') {
      startMonitoring();
    }

    return () => {
      subscription.remove();
      stopSecurityEventMonitoring();
    };
  }, []);

  return (
    <SafeAreaView style={styles.safe}>
      <ScrollView contentContainerStyle={styles.container}>
        <Text style={styles.title}>ARK Security Manager</Text>
        <Text style={styles.subtitle}>Platform: {Platform.OS}</Text>

        {loading && <ActivityIndicator style={styles.loader} />}

        {/* ── Security Report ───────────────────────────────────── */}
        {report && (
          <View style={styles.card}>
            <Text style={styles.sectionTitle}>Security Report</Text>
            <Row label="Debugger attached" value={report.isDebuggerAttached} />
            {isAndroid && <Row label="Developer options" value={report.isDeveloperOptionsEnabled} />}
            <Row label="Device compromised" value={report.isDeviceCompromised} danger />
            {isAndroid && <Row label="Frida detected" value={report.isFridaDetected} danger />}
            {isIOS && <Row label="SSL bypass" value={report.isSSLBypassed} danger />}
            <Row label="Trusted install source" value={report.isInstalledFromTrustedSource} invert />
            <ActionButton label="Refresh Report" onPress={runChecks} compact />
          </View>
        )}

        {/* ── Security Tests ────────────────────────────────────── */}
        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Security Tests</Text>
          <CheckButton
            label="Debugger"
            result={checkResults['debugger']}
            onPress={() => runSingleCheck('debugger', isDebuggerAttached())}
          />
          {isAndroid && (
            <CheckButton
              label="Developer Options"
              result={checkResults['developerOptions']}
              onPress={() => runSingleCheck('developerOptions', isDeveloperOptionsEnabled())}
            />
          )}
          <CheckButton
            label={isIOS ? 'Jailbreak' : 'Root'}
            result={checkResults['deviceCompromised']}
            onPress={() => runSingleCheck('deviceCompromised', isDeviceCompromised())}
            danger
          />
          {isAndroid && (
            <CheckButton
              label="Frida"
              result={checkResults['frida']}
              onPress={() => runSingleCheck('frida', isFridaDetected())}
              danger
            />
          )}
          {isIOS && (
            <CheckButton
              label="SSL Bypass"
              result={checkResults['sslBypass']}
              onPress={() => runSingleCheck('sslBypass', isSSLBypassed())}
              danger
            />
          )}
          <CheckButton
            label="Trusted Source"
            result={checkResults['trustedSource']}
            onPress={() => runSingleCheck('trustedSource', isInstalledFromTrustedSource())}
            invert
          />
        </View>

        {/* ── Screen Protection ─────────────────────────────────── */}
        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Screen Protection</Text>
          <View style={styles.buttonRow}>
            <ActionButton
              label="Enable Secure"
              onPress={() => handleSetScreenSecure(true)}
              active={screenSecure}
              flex
            />
            <ActionButton
              label="Disable Secure"
              onPress={() => handleSetScreenSecure(false)}
              flex
            />
          </View>
          {isAndroid && (
            <ActionButton label="Apply Overview Protection" onPress={handleOverviewProtection} />
          )}
        </View>

        {/* ── Trusted Store Management (Android) ────────────────── */}
        {isAndroid && (
          <View style={styles.card}>
            <Text style={styles.sectionTitle}>Trusted Store Management</Text>
            <ActionButton
              label="Remove Test Store"
              onPress={() => {
                removeTrustedStore('com.example.store');
                addLog('removeTrustedStore', 'Removed com.example.store from trusted stores list.');
              }}
            />
          </View>
        )}

        {/* ── iOS Monitoring ────────────────────────────────────── */}
        {isIOS && (
          <View style={styles.card}>
            <Text style={styles.sectionTitle}>Monitoring</Text>
            <View style={styles.statusGrid}>
              <StatusPill label="Monitoring" value={monitoringActive ? 'ON' : 'OFF'} active={monitoringActive} />
              <StatusPill
                label="Recording"
                value={screenRecordingActive ? 'ACTIVE' : 'IDLE'}
                active={screenRecordingActive}
              />
            </View>
            <View style={styles.buttonRow}>
              <ActionButton label="Start" onPress={startMonitoring} active={monitoringActive} flex />
              <ActionButton label="Stop" onPress={stopMonitoring} flex />
            </View>
            <ActionButton
              label="Refresh Recording State"
              onPress={() => {
                const active = isScreenRecordingActive();
                setScreenRecordingState(active);
                addLog('screenRecording', `Screen recording active: ${String(active)}`);
              }}
              compact
            />
          </View>
        )}

        {/* ── Action Log ────────────────────────────────────────── */}
        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Action Log</Text>
          {actionLogs.length === 0 ? (
            <Text style={styles.emptyText}>No actions executed yet.</Text>
          ) : (
            actionLogs.map((log) => (
              <View key={`${log.key}-${log.timestamp}`} style={styles.logRow}>
                <Text style={styles.logTime}>{new Date(log.timestamp).toLocaleTimeString()}</Text>
                <Text style={styles.logValue}>{log.message}</Text>
              </View>
            ))
          )}
        </View>

        {/* ── Native Security Events (iOS) ──────────────────────── */}
        {isIOS && (
          <View style={styles.card}>
            <Text style={styles.sectionTitle}>Native Security Events</Text>
            {securityEvents.length === 0 ? (
              <Text style={styles.emptyText}>
                No events yet. Take a screenshot or start screen recording after monitoring begins.
              </Text>
            ) : (
              securityEvents.map((event, index) => (
                <View key={`${event.type}-${event.timestamp}-${index}`} style={styles.eventCard}>
                  <Text style={styles.eventTitle}>{event.type}</Text>
                  <Text style={styles.eventMeta}>
                    {new Date(event.timestamp).toLocaleTimeString()}
                  </Text>
                  {'isRecording' in event && (
                    <Text style={styles.eventMeta}>isRecording: {String(event.isRecording)}</Text>
                  )}
                </View>
              ))
            )}
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

// ── Components ──────────────────────────────────────────────────────────────

type RowProps = {
  label: string;
  value: boolean;
  danger?: boolean;
  invert?: boolean;
};

function Row({ label, value, danger, invert }: RowProps) {
  const isAlert = danger ? value : invert ? !value : false;
  return (
    <View style={styles.row}>
      <Text style={styles.rowLabel}>{label}</Text>
      <Text style={[styles.rowValue, isAlert && styles.rowAlert]}>{value ? 'YES' : 'NO'}</Text>
    </View>
  );
}

type CheckButtonProps = {
  label: string;
  result?: CheckResult;
  onPress: () => void;
  danger?: boolean;
  invert?: boolean;
};

function CheckButton({ label, result, onPress, danger, invert }: CheckButtonProps) {
  const hasResult = result !== undefined;
  const isAlert = hasResult && (danger ? result.value : invert ? !result.value : false);
  const isSafe = hasResult && !isAlert;

  return (
    <TouchableOpacity
      accessibilityRole="button"
      style={[
        styles.checkButton,
        hasResult && (isAlert ? styles.checkButtonDanger : styles.checkButtonSafe),
      ]}
      onPress={onPress}
    >
      <Text style={styles.checkButtonLabel}>{label}</Text>
      {hasResult ? (
        <View style={[styles.resultBadge, isAlert ? styles.badgeDanger : styles.badgeSafe]}>
          <Text style={[styles.resultBadgeText, isSafe && styles.resultBadgeTextSafe]}>
            {result.value ? 'TRUE' : 'FALSE'}
          </Text>
        </View>
      ) : (
        <Text style={styles.checkButtonHint}>TAP</Text>
      )}
    </TouchableOpacity>
  );
}

type ActionButtonProps = {
  label: string;
  onPress: () => void;
  active?: boolean;
  compact?: boolean;
  flex?: boolean;
};

function ActionButton({ label, onPress, active, compact, flex }: ActionButtonProps) {
  return (
    <TouchableOpacity
      accessibilityRole="button"
      style={[
        styles.button,
        active && styles.buttonActive,
        compact && styles.buttonCompact,
        flex && styles.buttonFlex,
      ]}
      onPress={onPress}
    >
      <Text style={[styles.buttonText, compact && styles.buttonTextCompact]}>{label}</Text>
    </TouchableOpacity>
  );
}

type StatusPillProps = {
  label: string;
  value: string;
  active?: boolean;
};

function StatusPill({ label, value, active }: StatusPillProps) {
  return (
    <View style={[styles.statusPill, active && styles.statusPillActive]}>
      <Text style={styles.statusLabel}>{label}</Text>
      <Text style={styles.statusValue}>{value}</Text>
    </View>
  );
}

// ── Styles ──────────────────────────────────────────────────────────────────

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: '#0f0f0f' },
  container: { padding: 20, gap: 14 },
  title: { fontSize: 22, fontWeight: '700', color: '#fff', textAlign: 'center' },
  subtitle: { fontSize: 13, color: '#888', textAlign: 'center', marginTop: 2 },
  loader: { marginVertical: 12 },
  card: {
    backgroundColor: '#1a1a1a',
    borderRadius: 12,
    padding: 14,
    gap: 8,
  },
  sectionTitle: {
    fontSize: 12,
    color: '#666',
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 0.8,
    marginBottom: 2,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 4,
  },
  rowLabel: { fontSize: 14, color: '#ccc', flex: 1 },
  rowValue: { fontSize: 14, fontWeight: '700', color: '#4caf50' },
  rowAlert: { color: '#f44336' },

  // Check buttons with inline results
  checkButton: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#222',
    borderRadius: 10,
    padding: 14,
    borderWidth: 1,
    borderColor: '#333',
  },
  checkButtonSafe: { borderColor: '#2e7d32' },
  checkButtonDanger: { borderColor: '#c62828' },
  checkButtonLabel: { color: '#fff', fontWeight: '600', fontSize: 15 },
  checkButtonHint: {
    color: '#555',
    fontSize: 11,
    fontWeight: '700',
    letterSpacing: 1,
  },
  resultBadge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 6,
  },
  badgeSafe: { backgroundColor: '#1b5e20' },
  badgeDanger: { backgroundColor: '#b71c1c' },
  resultBadgeText: { color: '#fff', fontSize: 12, fontWeight: '800', letterSpacing: 0.5 },
  resultBadgeTextSafe: { color: '#c8e6c9' },

  // Action buttons
  buttonRow: { flexDirection: 'row', gap: 8 },
  button: {
    backgroundColor: '#1e1e1e',
    borderRadius: 10,
    padding: 14,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#333',
  },
  buttonActive: { borderColor: '#4caf50' },
  buttonCompact: { padding: 10 },
  buttonFlex: { flex: 1 },
  buttonText: { color: '#fff', fontWeight: '600', fontSize: 15 },
  buttonTextCompact: { fontSize: 13 },

  // Status pills
  statusGrid: { flexDirection: 'row', gap: 10, flexWrap: 'wrap' },
  statusPill: {
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 8,
    backgroundColor: '#111',
    borderWidth: 1,
    borderColor: '#333',
  },
  statusPillActive: { borderColor: '#4caf50' },
  statusLabel: { color: '#787878', fontSize: 11, marginBottom: 2 },
  statusValue: { color: '#fff', fontWeight: '700' },

  // Log and events
  emptyText: { fontSize: 13, color: '#555' },
  logRow: {
    flexDirection: 'row',
    gap: 8,
    paddingVertical: 4,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: '#2a2a2a',
    alignItems: 'center',
  },
  logTime: {
    color: '#555',
    fontSize: 11,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
  },
  logValue: { color: '#bbb', fontSize: 13, flex: 1 },
  eventCard: {
    backgroundColor: '#111',
    borderRadius: 10,
    padding: 12,
    gap: 4,
    borderWidth: 1,
    borderColor: '#242424',
  },
  eventTitle: { color: '#fff', fontWeight: '700', fontSize: 14 },
  eventMeta: {
    color: '#a5a5a5',
    fontSize: 12,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
  },
});
