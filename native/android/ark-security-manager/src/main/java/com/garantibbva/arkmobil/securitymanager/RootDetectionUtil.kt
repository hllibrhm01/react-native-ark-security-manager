package com.garantibbva.arkmobil.securitymanager

import android.app.Application
import android.content.pm.PackageManager
import androidx.annotation.VisibleForTesting
import com.garantibbva.arkmobil.securitymanager.constants.Constants
import com.garantibbva.arkmobil.securitymanager.helper.BuildHelper
import com.garantibbva.arkmobil.securitymanager.helper.FileHelper

/**
 * RootDetectionUtil provides utility methods to detect if the device is rooted.
 *
 * Main features:
 * - Checks for the presence of root binaries and root management apps.
 * - Attempts to create files in common root directories to detect root access.
 * - Provides helper methods for testing root detection logic.
 *
 * Usage examples:
 *
 * val isRooted = RootDetectionUtil.isRooted(context.packageManager)
 *
 * All methods are static and can be accessed directly from the object.
 */
internal object RootDetectionUtil {
    private val paths = Constants.ROOT_DETECT_PATHS
    private val otherPaths = Constants.ROOT_DETECT_OTHER_PATHS
    private val binaries = Constants.ROOT_DETECT_BINARIES
    private val knownRootAppsPackages = Constants.KNOWN_ROOT_APPS_PACKAGES

    fun isRooted(packageManager: PackageManager): Boolean {
        paths.forEach { path ->
            binaries.forEach { bin ->
                val isExist = FileHelper.isExist("$path/$bin")
                if (isExist) {
                    return true
                }
            }
        }
        otherPaths.forEach { path ->
            if (FileHelper.isExist(path)) {
                return true
            }
        }

        try {
            val process = Runtime.getRuntime().exec(Constants.ROOT_DETECT_SU_WHICH_CMD)
            val result = process.inputStream.bufferedReader().readLine() != null
            process.destroy()
            if (result) return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (BuildHelper.getBuildTAGS()?.contains("test-keys") == true) {
            return true
        }
        checkForRootApps(packageManager).run {
            if (this) {
                return true
            }
        }
        return createFile(paths)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun checkForRootApps(packageManager: PackageManager): Boolean {
        for (packageName in knownRootAppsPackages) {
            try {
                packageManager.getPackageInfo(packageName, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {

            }
        }
        return false
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun createFile(paths: List<String>): Boolean {
        var isCreated = false
        paths.forEach { path ->
            if (FileHelper.canCreateFile(path)) {
                isCreated = true
            }
        }
        return isCreated
    }
}
