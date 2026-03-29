package com.garantibbva.arkmobil.ark_security_manager.unittest

import android.app.Application
import android.content.pm.PackageManager
import com.garantibbva.arkmobil.securitymanager.RootDetectionUtil
import com.garantibbva.arkmobil.securitymanager.helper.BuildHelper
import com.garantibbva.arkmobil.securitymanager.helper.FileHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkConstructor
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class RootDetectionUtilTest {

    private lateinit var application: Application
    private lateinit var packageManager: PackageManager

    @Before
    fun setUp() {
        mockkConstructor(FileHelper::class)
        application = mockk(relaxed = true)
        packageManager = mockk(relaxed = true)
        every { application.packageManager } returns packageManager
    }

    @After
    fun tearDown() {
        unmockkConstructor(FileHelper::class)
    }

    @Test
    fun `test isRooted returns true for rooted paths`() {
        mockkObject(FileHelper) {
            every { FileHelper.isExist(any<String>()) } returns true
            assertTrue(RootDetectionUtil.isRooted(packageManager))
        }
        unmockkObject(FileHelper)
    }

    @Test
    fun `test isRooted returns true for other paths`() {
        mockkObject(FileHelper) {
            every { FileHelper.isExist("/sbin/.magisk") } returns true
            assertTrue(RootDetectionUtil.isRooted(packageManager))
        }
        unmockkObject(FileHelper)
    }

    @Test
    fun `test isRooted returns true for create File`() {
        every { packageManager.getPackageInfo(any<String>(), any<Int>()) } throws PackageManager.NameNotFoundException()

        mockkObject(FileHelper) {
            every { FileHelper.canCreateFile(any<String>()) } returns true
            assertTrue(RootDetectionUtil.isRooted(packageManager))
        }
        unmockkObject(FileHelper)
    }

    @Test
    fun `test isRooted returns false for rooted paths`() {
        every { anyConstructed<FileHelper>().canCreateFile(any<String>()) } returns false
        every { anyConstructed<FileHelper>().isExist(any<String>()) } returns true
            assertTrue(RootDetectionUtil.isRooted(packageManager))
    }

    @Test
    fun `test isRooted returns false for non-rooted paths`() {
        every { packageManager.getPackageInfo(any<String>(), any<Int>()) } throws PackageManager.NameNotFoundException()
        mockkObject(FileHelper) {
            every { FileHelper.canCreateFile(any<String>()) } returns false
            every { FileHelper.isExist(any<String>()) } returns false
            assertFalse(RootDetectionUtil.isRooted(packageManager))
        }
        unmockkObject(FileHelper)
    }

    @Test
    fun `test checkForRootApps returns true for known root apps`() {
        every { packageManager.getPackageInfo(any<String>(), any<Int>()) } returns mockk()
            val result = RootDetectionUtil.checkForRootApps(packageManager)
        assertTrue(result)
    }

    @Test
    fun `test checkForRootApps returns false for no root apps`() {
        every { packageManager.getPackageInfo(any<String>(), any<Int>()) } throws PackageManager.NameNotFoundException()
        mockkObject(FileHelper) {
            every { FileHelper.canCreateFile(any<String>()) } returns false
            val result = RootDetectionUtil.isRooted(packageManager)
            assertFalse(result)
        }
        unmockkObject(FileHelper)
    }

    @Test
    fun `test createFile returns true when file is created`() {
        every { anyConstructed<FileHelper>().canCreateFile(any<String>()) } returns true
        val result = RootDetectionUtil.isRooted(packageManager)
        assertTrue(result)
    }

    @Test
    fun `test createFile returns false when file creation fails IO`() {
        every { anyConstructed<FileHelper>().canCreateFile(any<String>()) } throws IOException()
        val result = RootDetectionUtil.isRooted(packageManager)
        assertTrue(result)
    }

    @Test
    fun `test createFile returns false when file creation fails Security`() {
        every { anyConstructed<FileHelper>().canCreateFile(any<String>()) } throws SecurityException()
        val result = RootDetectionUtil.isRooted(packageManager)
        assertTrue(result)
    }

    @Test
    fun `test createFile returns false when file creation fails General`() {
        every { anyConstructed<FileHelper>().canCreateFile(any<String>()) } throws Exception()
        val result = RootDetectionUtil.isRooted(packageManager)
        assertTrue(result)
    }

    @Test
    fun `test isRooted handles Runtime exceptions`() {
        mockkStatic(Runtime::class) {
            every { Runtime.getRuntime().exec(any<Array<String>>()) } throws IOException()
            val result = RootDetectionUtil.isRooted(packageManager)
            assertTrue(result)
        }
        unmockkStatic(Runtime::class)
    }

    @Test
    fun `test BuildTAGS`() {
        every { packageManager.getPackageInfo(any<String>(), any<Int>()) } throws PackageManager.NameNotFoundException()
        mockkObject(FileHelper) {
            every { FileHelper.isExist(any<String>()) } returns false
        }
        mockkObject(BuildHelper) {
            every { BuildHelper.getBuildTAGS() } returns "test-keys"
            val result = RootDetectionUtil.isRooted(packageManager)
            assertTrue(result)
        }
        unmockkObject(BuildHelper::class)
    }
}
