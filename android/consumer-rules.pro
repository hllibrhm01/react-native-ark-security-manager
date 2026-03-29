# ProGuard / R8 consumer rules for react-native-ark-security-manager
# These rules are merged into the consuming app's ProGuard config automatically.

# Keep the native ARK Security Manager SDK classes
-keep class com.garantibbva.arkmobil.securitymanager.** { *; }
-dontwarn com.garantibbva.arkmobil.securitymanager.**

# Keep the RN module classes
-keep class com.arksecuritymanager.** { *; }
-dontwarn com.arksecuritymanager.**
