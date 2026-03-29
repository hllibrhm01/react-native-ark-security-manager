package com.garantibbva.arkmobil.ark_security_manager.unittest
import com.garantibbva.arkmobil.securitymanager.helper.FileHelper
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class FileHelperTest {
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        tempDir = createTempDir()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testIsExist_whenFileExists_returnsTrue() {
        val file = File(tempDir, "test.txt")
        file.createNewFile()
        assertTrue(FileHelper.isExist(file.absolutePath))
    }

    @Test
    fun testIsExist_whenFileDoesNotExist_returnsFalse() {
        val file = File(tempDir, "not_exist.txt")
        assertFalse(FileHelper.isExist(file.absolutePath))
    }

    @Test
    fun testCanCreateFile_success() {
        val result = FileHelper.canCreateFile(tempDir.absolutePath)
        val createdFile = File(tempDir, "deneme.txt")
        assertTrue(result)
        assertFalse(createdFile.exists())
    }

    @Test
    fun testCanCreateFile_whenIOException_returnsFalse() {
        val fakeDir = File(tempDir, "not_a_dir/invalid")
        val result = FileHelper.canCreateFile(fakeDir.absolutePath)
        assertFalse(result)
    }
}
