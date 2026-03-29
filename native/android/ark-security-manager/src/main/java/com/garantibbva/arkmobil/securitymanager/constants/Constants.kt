package com.garantibbva.arkmobil.securitymanager.constants

internal object Constants {
    const val CORE_SECURITY_CHECK_FILE = "core_security_check.txt"
    const val CORE_SECURE_STORAGE_ID = "CoreSecureStorageID"
    const val CORE_SIMPLE_STORAGE_ID = "CoreSimpleStorageID"
    const val EXTRA_PARAMETERS = "extraParameters"
    const val INITIAL_DATA = "initialData"
    const val LINK = "link"
    const val FRIDA_IP = "127.0.0.1"
    const val FRIDA_PORTS_1 = 27042
    const val FRIDA_PORTS_2 = 27043
    val ROOT_DETECT_PATHS = listOf(
        "/data/local",
        "/data/local/bin",
        "/data/local/xbin",
        "/data/adb",
        "/sbin",
        "/su/bin",
        "/system",
        "/system/lib",
        "/system/lib64",
        "/system/app",
        "/system/bin",
        "/system/bin/.ext",
        "/system/bin/failsafe",
        "/system/sd/xbin",
        "/system/usr/we-need-root",
        "/system/sd/xbin",
        "/system/xbin",
        "/system/sbin",
    )
    val ROOT_DETECT_OTHER_PATHS = listOf(
        "/sbin/.magisk",
        "/dev/.magisk",
        "/cache/magisk.log",
        "/vendor/bin/busybox",
        "/data/local/tmp/frida-server",
        "/data/local/tmp/re.frida.server",
        "/data/local/tmp/frida",
        "/data/local/tmp/frida-inject",
        "/data/local/tmp/frida-agent-32",
        "/data/local/tmp/frida-agent-64",
        "/system/lib/libfrida-gadget.so",
        "/system/lib64/libfrida-gadget.so",
        "/system/framework/XposedBridge.jar",
        "/system/lib/libxposed_art.so",
        "/system/lib64/libxposed_art.so",
        "/system/xbin/daemonsu",
        "/system/xbin/supolicy",
        "/system/bin/daemonsu",
        "/data/app/de.robv.android.xposed.installer",
        "/usr/libexec/substrate",
        "/Library/MobileSubstrate/MobileSubstrate.dylib",
        "/usr/lib/substitute-inserter.dylib",
        "/usr/lib/libhooker.dylib",
        "/etc/apt",
        "/private/var/lib/apt/",
        "/Applications/Cydia.app",
        "/Applications/FakeCarrier.app",
        "/Applications/Icy.app",
        "/Applications/IntelliScreen.app",
        "/Applications/SBSettings.app",
        "/Applications/RockApp.app",
    )
    val ROOT_DETECT_BINARIES = listOf(
        "magisk", "zygisk", "su", "su2", "superuser.apk", "Superuser.apk",
        "su-backup", ".su", "busybox", "busybox.apk",
    )
    val KNOWN_ROOT_APPS_PACKAGES = arrayOf(
        "com.noshufou.android.su",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.topjohnwu.magisk",
    )
    val ROOT_DETECT_SU_WHICH_CMD = arrayOf("/system/xbin/which", "su")
}
