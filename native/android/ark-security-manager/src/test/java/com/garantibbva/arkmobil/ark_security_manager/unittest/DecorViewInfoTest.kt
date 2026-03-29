package com.garantibbva.arkmobil.ark_security_manager.unittest

import android.R
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.garantibbva.arkmobil.securitymanager.DecorViewInfo
import com.garantibbva.arkmobil.securitymanager.extensions.activity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DecorViewInfoTest {

    @Test
    fun `activity is resolved from decorView extension`() {
        mockkStatic("com.garantibbva.arkmobil.securitymanager.extensions.ViewExtensionsKt")

        val activity = mockk<Activity>()
        val decorView = mockk<View>()
        every { decorView.activity } returns activity

        val info = DecorViewInfo(
            decorView = decorView,
            isToast = false,
            isApplication = true,
            isDialog = false,
        )

        assertEquals(activity, info.activity)
    }

    @Test
    fun `activity is resolved from contentView when decorView activity is null`() {
        mockkStatic("com.garantibbva.arkmobil.securitymanager.extensions.ViewExtensionsKt")

        val activity = mockk<Activity>()
        val decorView = mockk<View>()
        val contentView = mockk<ViewGroup>()

        every { decorView.activity } returns null
        every { decorView.findViewById<ViewGroup>(R.id.content) } returns contentView
        every { contentView.activity } returns activity

        val info = DecorViewInfo(
            decorView = decorView,
            isToast = true,
            isApplication = false,
            isDialog = true,
        )

        assertEquals(activity, info.activity)
    }

    @Test
    fun `activity is null when neither decorView nor contentView has activity`() {
        mockkStatic("com.garantibbva.arkmobil.securitymanager.extensions.ViewExtensionsKt")

        val decorView = mockk<View>()
        val contentView = mockk<ViewGroup>()

        every { decorView.activity } returns null
        every { decorView.findViewById<ViewGroup>(R.id.content) } returns contentView
        every { contentView.activity } returns null

        val info = DecorViewInfo(
            decorView = decorView,
            isToast = false,
            isApplication = false,
            isDialog = false,
        )

        assertNull(info.activity)
    }

    @Test
    fun `activity is null when no content view`() {
        mockkStatic("com.garantibbva.arkmobil.securitymanager.extensions.ViewExtensionsKt")

        val decorView = mockk<View>()

        every { decorView.activity } returns null
        every { decorView.findViewById<ViewGroup>(R.id.content) } returns null

        val info = DecorViewInfo(
            decorView = decorView,
            isToast = false,
            isApplication = true,
            isDialog = true,
        )

        assertNull(info.activity)
    }
}
