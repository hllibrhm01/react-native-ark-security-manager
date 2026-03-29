package com.garantibbva.arkmobil.securitymanager.helper

import androidx.annotation.VisibleForTesting
import com.garantibbva.arkmobil.securitymanager.constants.Constants
import java.io.File

/**
 * FileHelper is a utility object for basic file operations.
 *
 * Provides methods to:
 * - Check if a file exists at a given path.
 * - Attempt to create a temporary file (named "deneme.txt") in a given directory and delete it immediately after creation.
 *
 * Usage examples:
 *
 * val exists = FileHelper.isExist("/path/to/file.txt")
 * val canCreate = FileHelper.canCreateFile("/path/to/directory")
 *
 * All methods are static and can be accessed directly from the object.
 */
object FileHelper {

    fun isExist(filePath: String): Boolean {
        return File(filePath).exists()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun canCreateFile(filePath: String): Boolean {
        return try {
            val accessFile = File(filePath, Constants.CORE_SECURITY_CHECK_FILE)
            val isCreated = accessFile.createNewFile()
            return isCreated.also {
                accessFile.delete()
            }
        } catch (e: Exception) {
            false
        }
    }
}
