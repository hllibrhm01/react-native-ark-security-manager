import { useEffect, useState } from 'react';
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
  getSecurityReport,
  isDebuggerAttached,
  isDeveloperOptionsEnabled,
  isDeviceCompromised,
  isFridaDetected,
  isInstalledFromTrustedSource,
  isSSLBypassed,
  setScreenSecure,
  type SecurityReport,
} from 'react-native-ark-security-manager';

export default function App() {
  const [report, setReport] = useState<SecurityReport | null>(null);
  const [screenSecure, setScreenSecureState] = useState(false);
  const [loading, setLoading] = useState(false);

  const runChecks = () => {
    setLoading(true);
    try {
      setReport(getSecurityReport());
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    runChecks();
  }, []);

  const toggleScreenSecure = () => {
    const next = !screenSecure;
    setScreenSecure(next);
    setScreenSecureState(next);
  };

  return (
    <SafeAreaView style={styles.safe}>
      <ScrollView contentContainerStyle={styles.container}>
        <Text style={styles.title}>ARK Security Manager</Text>
        <Text style={styles.subtitle}>Platform: {Platform.OS}</Text>

        {loading && <ActivityIndicator style={styles.loader} />}

        {report && (
          <View style={styles.card}>
            <Row label="Debugger attached"          value={report.isDebuggerAttached} />
            <Row label="Developer options"          value={report.isDeveloperOptionsEnabled} androidOnly />
            <Row label="Device compromised"         value={report.isDeviceCompromised} danger />
            <Row label="Frida detected"             value={report.isFridaDetected} androidOnly danger />
            <Row label="SSL bypass"                 value={report.isSSLBypassed} iosOnly danger />
            <Row label="Trusted install source"     value={report.isInstalledFromTrustedSource} invert />
          </View>
        )}

        <TouchableOpacity style={styles.button} onPress={runChecks}>
          <Text style={styles.buttonText}>Run checks</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.button, screenSecure && styles.buttonActive]}
          onPress={toggleScreenSecure}
        >
          <Text style={styles.buttonText}>
            Screen secure: {screenSecure ? 'ON' : 'OFF'}
          </Text>
        </TouchableOpacity>

        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Individual checks</Text>
          <Text style={styles.mono}>isDebuggerAttached()      → {String(isDebuggerAttached())}</Text>
          <Text style={styles.mono}>isDeveloperOptionsEnabled → {String(isDeveloperOptionsEnabled())}</Text>
          <Text style={styles.mono}>isDeviceCompromised()     → {String(isDeviceCompromised())}</Text>
          <Text style={styles.mono}>isFridaDetected()         → {String(isFridaDetected())}</Text>
          <Text style={styles.mono}>isSSLBypassed()           → {String(isSSLBypassed())}</Text>
          <Text style={styles.mono}>isInstalledFromTrusted()  → {String(isInstalledFromTrustedSource())}</Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

type RowProps = {
  label: string;
  value: boolean;
  danger?: boolean;
  invert?: boolean;
  androidOnly?: boolean;
  iosOnly?: boolean;
};

function Row({ label, value, danger, invert, androidOnly, iosOnly }: RowProps) {
  const platformLabel =
    androidOnly ? ' (Android)' : iosOnly ? ' (iOS)' : '';
  const isAlert = danger ? value : invert ? !value : false;

  return (
    <View style={styles.row}>
      <Text style={styles.rowLabel}>
        {label}
        <Text style={styles.platform}>{platformLabel}</Text>
      </Text>
      <Text style={[styles.rowValue, isAlert && styles.rowAlert]}>
        {value ? 'YES' : 'NO'}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: '#0f0f0f' },
  container: { padding: 24, gap: 16 },
  title: { fontSize: 22, fontWeight: '700', color: '#fff', textAlign: 'center' },
  subtitle: { fontSize: 13, color: '#888', textAlign: 'center', marginTop: 4 },
  loader: { marginVertical: 12 },
  card: {
    backgroundColor: '#1a1a1a',
    borderRadius: 12,
    padding: 16,
    gap: 10,
  },
  sectionTitle: { fontSize: 13, color: '#888', marginBottom: 4, fontWeight: '600' },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  rowLabel: { fontSize: 14, color: '#ccc', flex: 1 },
  platform: { fontSize: 11, color: '#555' },
  rowValue: { fontSize: 14, fontWeight: '700', color: '#4caf50' },
  rowAlert: { color: '#f44336' },
  button: {
    backgroundColor: '#1e1e1e',
    borderRadius: 10,
    padding: 14,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#333',
  },
  buttonActive: { borderColor: '#4caf50' },
  buttonText: { color: '#fff', fontWeight: '600', fontSize: 15 },
  mono: { fontSize: 12, color: '#aaa', fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace' },
});
