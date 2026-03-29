package com.garantibbva.arkmobil.securitymanager.helper

import android.os.Build
/**
 * Utility wrapper object to access build configuration details.
 * Currently provides the SDK version of the device. (For testing)
 */
internal object BuildHelper {
    fun getVersionSDKInt(): Int {
        return Build.VERSION.SDK_INT
    }
    fun getBuildTAGS(): String? {
        return Build.TAGS
    }
}
