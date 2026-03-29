package com.garantibbva.arkmobil.securitymanager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Debug.isDebuggerConnected
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION
import android.view.inspector.WindowInspector
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.garantibbva.arkmobil.securitymanager.constants.Constants.FRIDA_IP
import com.garantibbva.arkmobil.securitymanager.constants.Constants.FRIDA_PORTS_1
import com.garantibbva.arkmobil.securitymanager.constants.Constants.FRIDA_PORTS_2
import com.garantibbva.arkmobil.securitymanager.extensions.removeExistingByTag
import com.garantibbva.arkmobil.securitymanager.helper.BuildHelper
import hideKeyboard
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Collections
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.createBitmap

/**
 * ArkSecurityManager is a utility object that provides security-related checks for the application.
 *
 * Main features:
 * - Detects if the device is running in debug mode or if a debugger is attached.
 * - Checks if developer options are enabled on the device.
 * - Detects if the device is rooted using RootDetectionUtil.
 *   - NOTE: The root detection method may produce false positives on some devices or custom ROMs, as it relies on heuristics such as the presence of binaries, system properties, and installed packages.
 *     Devices with unlocked bootloaders, custom recoveries, or certain manufacturer modifications may be flagged as rooted even if they are not compromised.
 * - Checks if the Frida debugging tool ports are open (commonly used for reverse engineering).
 *
 * Usage examples:
 *
 * val isDevOptionsEnabled = ArkSecurityManager.isDeveloperOptionsEnabled(context)
 * val isDeviceRooted = ArkSecurityManager.isRooted(application)
 * val isFridaPortOpen = ArkSecurityManager.checkFridaPortOpen()
 *
 * All methods are static and can be accessed directly from the object.
 */
object ArkSecurityManager {
    private val TRUSTED_STORE_LIST = listOf(
        "com.android.vending",
        "com.google.android.feedback",
        "com.amazon.venezia",
        "com.sec.android.app.samsungapps",
        "com.huawei.appmarket",
        "com.miui.packageinstaller",
        "com.xiaomi.market",
        "com.xiaomi.mipicks",
        "com.oppo.market",
        "com.bbk.appstore",
        "com.vivo.appstore",
        "com.heytap.market",
    )
    private val trustedStoreList: MutableList<String> = Collections.synchronizedList(TRUSTED_STORE_LIST.toMutableList())

    val isDebuggerConnected
        get() = isDebuggerConnected()

    fun isDeveloperOptionsEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            ) == 1
        } catch (e: Exception) {
            Log.i("","Developer Options Not Enabled : $e")
            false
        }
    }

    /**
     * Checks if the device is rooted.
     *
     * Limitations:
     * - May produce false positives on some devices, especially those with custom ROMs, unlocked bootloaders, or certain manufacturer modifications.
     * - Relies on heuristics such as the presence of binaries, system properties, and installed packages.
     */
    fun isRooted(application: Application): Boolean {
        return try {
            RootDetectionUtil.isRooted(application.packageManager)
        } catch (e: Exception) {
            false
        }
    }

    fun checkFridaPortOpen(timeoutMs: Int = 500): Boolean {
        return listOf(FRIDA_PORTS_1, FRIDA_PORTS_2).any { port ->
            Socket().use { socket ->
                try {
                    socket.connect(InetSocketAddress(FRIDA_IP, port), timeoutMs)
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    fun isLocationMocked(location: Location): Boolean {
        val locationIsMocked =
            if (BuildHelper.getVersionSDKInt() >= Build.VERSION_CODES.S) {
                location.isMock
            } else {
                location.isFromMockProvider
            }
        return locationIsMocked
    }

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    fun setWindowSecureFlag(activity: Activity) {
        activity.apply {
            window.apply {
                addFlags(LayoutParams.FLAG_SECURE)
                clearFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                clearFlags(LayoutParams.FLAG_DISMISS_KEYGUARD)
                clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            if (BuildHelper.getVersionSDKInt() >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(false)
            }
            if (BuildHelper.getVersionSDKInt() >= Build.VERSION_CODES.TIRAMISU) {
                setRecentsScreenshotEnabled(false)
            }
        }
    }

    @SuppressLint("NewApi")
    fun clearWindowSecureFlag(activity: Activity) {
        activity.apply {
            window.clearFlags(LayoutParams.FLAG_SECURE)
            if (BuildHelper.getVersionSDKInt() >= Build.VERSION_CODES.TIRAMISU) {
                setRecentsScreenshotEnabled(true)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @JvmStatic
    fun setOverviewScreenSecureView(
        activity: Activity,
        overlayView: View? = null,
        useBlur: Boolean = true,
        @ColorInt overlayColor: Int = ArkSecurityViewHelper.DEFAULT_OVERLAY_COLOR,
    ) {
        ArkSecurityViewHelper.setOverviewScreenSecureView(activity, overlayView, useBlur, overlayColor)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @JvmStatic
    internal fun applyBlurView(
        activity: Activity,
        callback: (ImageView) -> Unit = {},
    ) {
        ArkSecurityViewHelper.applyBlurView(activity, callback)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    internal fun getDecorViewInfos(): DecorViewInfo? {
        return ArkSecurityViewHelper.getDecorViewInfos()
    }

    @SuppressLint("NewApi")
    fun isInstalledFromTrustedStore(context: Context): Boolean {
        val packageManager = context.packageManager
        val packageName: String = context.packageName
        val installer: String? = try {
            if (BuildHelper.getVersionSDKInt() >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageName)
                    .installingPackageName
            } else {
                packageManager.getInstallerPackageName(packageName)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        return installer != null && trustedStoreList.contains(installer)
    }

    fun setTrustedStores(stores: List<String>) {
        if (stores.isNotEmpty()) {
            synchronized(trustedStoreList) {
                trustedStoreList.clear()
                trustedStoreList.addAll(stores)
            }
        }
    }

    fun removeTrustedStore(store: String) {
        trustedStoreList.remove(store)
    }
}
