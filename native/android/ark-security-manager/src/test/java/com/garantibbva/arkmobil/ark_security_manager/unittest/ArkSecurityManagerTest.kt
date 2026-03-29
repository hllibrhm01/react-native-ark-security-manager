package com.garantibbva.arkmobil.ark_security_manager.unittest

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.InstallSourceInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RenderEffect
import android.location.Location
import android.os.Build
import android.os.Debug
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.inspector.WindowInspector
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.garantibbva.arkmobil.securitymanager.ArkSecurityManager
import com.garantibbva.arkmobil.securitymanager.ArkSecurityManager.applyBlurView
import com.garantibbva.arkmobil.securitymanager.ArkSecurityManager.setOverviewScreenSecureView
import com.garantibbva.arkmobil.securitymanager.RootDetectionUtil
import com.garantibbva.arkmobil.securitymanager.helper.BuildHelper
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkConstructor
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config
import java.net.Socket

class ArkSecurityManagerTest {

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var packageManager: PackageManager
    private lateinit var activity: Activity
    private lateinit var window: Window
    private lateinit var decorView: View
    private lateinit var rootView: ViewGroup
    private lateinit var viewTreeObserver: ViewTreeObserver

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        application = mockk(relaxed = true)
        packageManager = mockk(relaxed = true)
        activity = mockk(relaxed = true)
        window = mockk(relaxed = true)
        decorView = mockk(relaxed = true)
        rootView = mockk(relaxed = true)
        viewTreeObserver = mockk(relaxed = true)

        every { activity.application } returns application
        every { activity.window } returns window
        every { window.decorView } returns decorView
        every { decorView.rootView } returns rootView
        every { decorView.viewTreeObserver } returns viewTreeObserver
        every { application.packageManager } returns packageManager
    }

    @Test
    fun `test isDebuggerConnected`() {
        mockkStatic("android.os.Debug")
        every { Debug.isDebuggerConnected() } returns true
        assertTrue(ArkSecurityManager.isDebuggerConnected)
        unmockkStatic("android.os.Debug")
    }

    @Test
    fun `test isDeveloperOptionsEnabled returns true`() {
        mockkStatic(Settings.Global::class)
        every {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            )
        } returns 1

        assertTrue(ArkSecurityManager.isDeveloperOptionsEnabled(context))

        unmockkStatic(Settings.Global::class)
    }

    @Test
    fun `test isRooted returns true`() {
        mockkObject(RootDetectionUtil)
        every { RootDetectionUtil.isRooted(packageManager) } returns true

        assertTrue(ArkSecurityManager.isRooted(application))

        unmockkObject(RootDetectionUtil)
    }

    @Test
    fun `test isRooted returns false`() {
        mockkObject(RootDetectionUtil)
        every { RootDetectionUtil.isRooted(packageManager) } throws Exception()

        assertFalse(ArkSecurityManager.isRooted(application))

        unmockkObject(RootDetectionUtil)
    }

    @Test
    fun `test checkFridaPortOpen returns true`() {
        mockkConstructor(Socket::class)
        every { anyConstructed<Socket>().connect(ofType(java.net.InetSocketAddress::class), any()) } just Runs
        every { anyConstructed<Socket>().close() } just Runs

        assertTrue(ArkSecurityManager.checkFridaPortOpen())

        unmockkConstructor(Socket::class)
    }

    @Test
    fun `test checkFridaPortOpen returns false`() {
        mockkConstructor(Socket::class)
        every { anyConstructed<Socket>().connect(ofType(java.net.InetSocketAddress::class), any()) } throws Exception()
        every { anyConstructed<Socket>().close() } just Runs

        assertFalse(ArkSecurityManager.checkFridaPortOpen())

        unmockkConstructor(Socket::class)
    }

    @Test
    fun `test setWindowSecureFlag`() {
        mockkObject(BuildHelper) {
            every { BuildHelper.getVersionSDKInt() } returns 33
            ArkSecurityManager.setWindowSecureFlag(activity)
            verify { window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) }
            verify { window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED) }
            verify { window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD) }
            verify { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
            verify { activity.setShowWhenLocked(false) }
            verify { activity.setRecentsScreenshotEnabled(false) }
        }
    }

    @Test
    fun `test setWindowSecureFlag Low Version`() {
        mockkObject(BuildHelper) {
            every { BuildHelper.getVersionSDKInt() } returns 26
            ArkSecurityManager.setWindowSecureFlag(activity)
            verify { window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) }
            verify { window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED) }
            verify { window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD) }
            verify { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
        }
    }

    @Test
    fun `test clearWindowSecureFlag`() {
        mockkObject(BuildHelper) {
            every { BuildHelper.getVersionSDKInt() } returns 33
            ArkSecurityManager.clearWindowSecureFlag(activity)
            verify { window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
            verify { activity.setRecentsScreenshotEnabled(true) }
        }
    }

    @Test
    fun `test clearWindowSecureFlag Low Version`() {
        mockkObject(BuildHelper) {
            every { BuildHelper.getVersionSDKInt() } returns 26
            ArkSecurityManager.clearWindowSecureFlag(activity)
            verify { window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
        }
    }

    @Test
    fun `isLocationMocked returns true when isMock true on Android S and above`() {
        mockkObject(BuildHelper)
        every { BuildHelper.getVersionSDKInt() } returns Build.VERSION_CODES.S
        val location = mockk<Location>(relaxed = true)
        every { location.isMock } returns true
        val result = ArkSecurityManager.isLocationMocked(location)
        assertTrue(result)
        unmockkObject(BuildHelper)
    }

    @Test
    fun `isLocationMocked returns true when isFromMockProvider true below Android S`() {
        mockkObject(BuildHelper)
        every { BuildHelper.getVersionSDKInt() } returns Build.VERSION_CODES.R
        val location = mockk<Location>(relaxed = true)
        every { location.isFromMockProvider } returns true
        val result = ArkSecurityManager.isLocationMocked(location)
        assertTrue(result)
        unmockkObject(BuildHelper)
    }

    @Test
    @Config(sdk = [31])
    fun `applyBlurView does not call callback when view has zero width`() {
        val mockRootView = mockk<View>(relaxed = true)
        every { decorView.rootView } returns mockRootView
        every { mockRootView.width } returns 0
        every { mockRootView.height } returns 1920
        every { mockRootView.post(any()) } answers {
            val runnable = firstArg<Runnable>()
            runnable.run()
            true
        }

        var callbackCalled = false
        applyBlurView(activity) {
            callbackCalled = true
        }

        assertFalse("Callback çağrılmamalı", callbackCalled)
    }

    @Test
    @Config(sdk = [31])
    fun `applyBlurView does not call callback when view has zero height`() {
        val mockRootView = mockk<View>(relaxed = true)
        every { decorView.rootView } returns mockRootView
        every { mockRootView.width } returns 1080
        every { mockRootView.height } returns 0
        every { mockRootView.post(any()) } answers {
            val runnable = firstArg<Runnable>()
            runnable.run()
            true
        }

        var callbackCalled = false
        applyBlurView(activity) {
            callbackCalled = true
        }

        assertFalse("Callback çağrılmamalı", callbackCalled)
    }

    @Test
    fun `setOverviewScreenSecureView lifecycle callbacks with same activity are ignored`() {
        mockkConstructor(View::class)
        every { anyConstructed<View>().tag = any() } just Runs
        every { anyConstructed<View>().setBackgroundColor(any()) } just Runs

        mockkConstructor(ViewGroup.LayoutParams::class)
        every { anyConstructed<View>().layoutParams = any() } just Runs

        val lifecycleCallbackSlot = slot<Application.ActivityLifecycleCallbacks>()
        every { application.registerActivityLifecycleCallbacks(capture(lifecycleCallbackSlot)) } just Runs

        every { rootView.addView(any()) } just Runs
        every { rootView.removeView(any()) } just Runs
        every { viewTreeObserver.addOnWindowFocusChangeListener(any()) } just Runs

        setOverviewScreenSecureView(activity, null)

        val lifecycleCallback = lifecycleCallbackSlot.captured

        lifecycleCallback.onActivityStarted(activity)
        lifecycleCallback.onActivityDestroyed(activity)
        lifecycleCallback.onActivityCreated(activity, null)
        lifecycleCallback.onActivityResumed(activity)
        lifecycleCallback.onActivityPaused(activity)
        lifecycleCallback.onActivityStopped(activity)
        lifecycleCallback.onActivitySaveInstanceState(activity, mockk())

        verify(exactly = 0) { rootView.addView(any()) }

        unmockkConstructor(View::class)
        unmockkConstructor(ViewGroup.LayoutParams::class)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `setOverviewScreenSecureView with custom overlayColor and blurRadius`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        val coreWindow = mockk<Window>(relaxed = true)
        val coreDecorView = mockk<View>(relaxed = true)
        val coreRootView = mockk<ViewGroup>(relaxed = true)
        val coreViewTreeObserver = mockk<ViewTreeObserver>(relaxed = true)

        every { coreActivity.application } returns application
        every { coreActivity.window } returns coreWindow
        every { coreWindow.decorView } returns coreDecorView
        every { coreDecorView.rootView } returns coreRootView
        every { coreDecorView.viewTreeObserver } returns coreViewTreeObserver
        every { coreActivity.lifecycle } returns mockk(relaxed = true)

        every { coreRootView.width } returns 1080
        every { coreRootView.height } returns 1920
        every { coreRootView.findViewWithTag<View>(any()) } returns null
        every { application.registerActivityLifecycleCallbacks(any()) } just Runs
        every { coreViewTreeObserver.addOnWindowFocusChangeListener(any()) } just Runs

        setOverviewScreenSecureView(
            coreActivity,
            useBlur = false,
            overlayColor = Color.RED,
        )

        verify { application.registerActivityLifecycleCallbacks(any()) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `clearOverviewScreenSecureView removes views and unregisters callbacks`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        val coreWindow = mockk<Window>(relaxed = true)
        val coreDecorView = mockk<View>(relaxed = true)
        val coreRootView = mockk<ViewGroup>(relaxed = true)
        val coreViewTreeObserver = mockk<ViewTreeObserver>(relaxed = true)

        val mockLifecycleCallbacks = mockk<Application.ActivityLifecycleCallbacks>(relaxed = true)
        val mockFocusListener = mockk<ViewTreeObserver.OnWindowFocusChangeListener>(relaxed = true)

        every { coreActivity.application } returns application
        every { coreActivity.window } returns coreWindow
        every { coreWindow.decorView } returns coreDecorView
        every { coreDecorView.rootView } returns coreRootView
        every { coreDecorView.viewTreeObserver } returns coreViewTreeObserver

        every { coreRootView.findViewWithTag<View>("secureViewOverlay") } returns mockk(relaxed = true)
        every { coreRootView.findViewWithTag<View>("secureViewBlur") } returns mockk(relaxed = true)
        every { coreRootView.removeView(any()) } just Runs
        every { application.unregisterActivityLifecycleCallbacks(mockLifecycleCallbacks) } just Runs
        every { coreViewTreeObserver.removeOnWindowFocusChangeListener(mockFocusListener) } just Runs
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `clearOverviewScreenSecureView with non-CoreActivity does not crash`() {
        val regularActivity = mockk<Activity>(relaxed = true)
        val regularWindow = mockk<Window>(relaxed = true)
        val regularDecorView = mockk<View>(relaxed = true)
        val regularRootView = mockk<ViewGroup>(relaxed = true)

        every { regularActivity.application } returns application
        every { regularActivity.window } returns regularWindow
        every { regularWindow.decorView } returns regularDecorView
        every { regularDecorView.rootView } returns regularRootView
        every { regularRootView.findViewWithTag<View>(any()) } returns null

        verify(exactly = 0) { application.unregisterActivityLifecycleCallbacks(any()) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `applyBlurView creates ImageView with correct properties and calls callback`() {
        val mockRootView = mockk<ViewGroup>(relaxed = true)
        every { decorView.rootView } returns mockRootView
        every { mockRootView.width } returns 1080
        every { mockRootView.height } returns 1920
        every { mockRootView.draw(any()) } just Runs
        every { mockRootView.post(any()) } answers {
            firstArg<Runnable>().run()
            true
        }

        mockkStatic(Bitmap::class)
        val mockBitmap = mockk<Bitmap>(relaxed = true)
        every { Bitmap.createBitmap(1080, 1920, any()) } returns mockBitmap

        mockkConstructor(Canvas::class)
        every { anyConstructed<Canvas>().drawColor(any()) } just Runs

        mockkStatic(RenderEffect::class)
        val mockRenderEffect = mockk<RenderEffect>(relaxed = true)
        every { RenderEffect.createBlurEffect(50f, 50f, any()) } returns mockRenderEffect

        mockkConstructor(ImageView::class)
        val capturedTag = slot<String>()
        every { anyConstructed<ImageView>().tag = capture(capturedTag) } just Runs
        every { anyConstructed<ImageView>().layoutParams = any() } just Runs
        every { anyConstructed<ImageView>().scaleType = any() } just Runs
        every { anyConstructed<ImageView>().setImageDrawable(any()) } just Runs
        every { anyConstructed<ImageView>().setRenderEffect(any()) } just Runs

        every { activity.resources } returns mockk(relaxed = true)

        var callbackInvoked = false
        var capturedImageView: ImageView? = null
        applyBlurView(activity) { imageView ->
            callbackInvoked = true
            capturedImageView = imageView
        }

        assertTrue("Callback should be invoked", callbackInvoked)
        assertNotNull("ImageView should be created", capturedImageView)
        assertTrue("Tag should be captured", capturedTag.isCaptured)
        assertEquals("secureViewBlur", capturedTag.captured)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `applyBlurView with custom blur radius creates blur effect correctly`() {
        val mockRootView = mockk<ViewGroup>(relaxed = true)
        every { decorView.rootView } returns mockRootView
        every { mockRootView.width } returns 800
        every { mockRootView.height } returns 1200
        every { mockRootView.draw(any()) } just Runs
        every { mockRootView.post(any()) } answers {
            firstArg<Runnable>().run()
            true
        }

        mockkStatic(Bitmap::class)
        val mockBitmap = mockk<Bitmap>(relaxed = true)
        every { Bitmap.createBitmap(800, 1200, any()) } returns mockBitmap

        mockkConstructor(Canvas::class)
        every { anyConstructed<Canvas>().drawColor(any()) } just Runs

        mockkStatic(RenderEffect::class)
        val mockRenderEffect = mockk<RenderEffect>(relaxed = true)
        every { RenderEffect.createBlurEffect(any(), any(), any()) } returns mockRenderEffect

        mockkConstructor(ImageView::class)
        every { anyConstructed<ImageView>().tag = any() } just Runs
        every { anyConstructed<ImageView>().layoutParams = any() } just Runs
        every { anyConstructed<ImageView>().scaleType = any() } just Runs
        every { anyConstructed<ImageView>().setImageDrawable(any()) } just Runs
        every { anyConstructed<ImageView>().setRenderEffect(any()) } just Runs

        every { activity.resources } returns mockk(relaxed = true)

        var callbackInvoked = false
        applyBlurView(activity) {
            callbackInvoked = true
        }

        assertTrue("Callback should be invoked", callbackInvoked)
        verify { RenderEffect.createBlurEffect(any(), any(), any()) }

        unmockkConstructor(ImageView::class)
        unmockkConstructor(Canvas::class)
        unmockkStatic(Bitmap::class)
        unmockkStatic(RenderEffect::class)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `applyBlurView does not call callback when rootView is not ViewGroup`() {
        val mockNonViewGroupRoot = mockk<View>(relaxed = true)
        every { decorView.rootView } returns mockNonViewGroupRoot

        var callbackCalled = false
        applyBlurView(activity) {
            callbackCalled = true
        }

        assertFalse("Callback should not be called", callbackCalled)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `applyBlurView does not call callback when post is not executed`() {
        val mockRootView = mockk<ViewGroup>(relaxed = true)
        every { decorView.rootView } returns mockRootView
        every { mockRootView.width } returns 1080
        every { mockRootView.height } returns 1920
        every { mockRootView.post(any()) } returns false

        var callbackCalled = false
        applyBlurView(activity) {
            callbackCalled = true
        }

        assertFalse("Callback should not be called when post fails", callbackCalled)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `applyBlurView handles edge case with minimum dimensions`() {
        val mockRootView = mockk<ViewGroup>(relaxed = true)
        every { decorView.rootView } returns mockRootView
        every { mockRootView.width } returns 1
        every { mockRootView.height } returns 1
        every { mockRootView.draw(any()) } just Runs
        every { mockRootView.post(any()) } answers {
            firstArg<Runnable>().run()
            true
        }

        mockkStatic(Bitmap::class)
        val mockBitmap = mockk<Bitmap>(relaxed = true)
        every { Bitmap.createBitmap(1, 1, any()) } returns mockBitmap

        mockkConstructor(Canvas::class)
        every { anyConstructed<Canvas>().drawColor(any()) } just Runs

        mockkStatic(RenderEffect::class)
        val mockRenderEffect = mockk<RenderEffect>(relaxed = true)
        every { RenderEffect.createBlurEffect(any(), any(), any()) } returns mockRenderEffect

        mockkConstructor(ImageView::class)
        every { anyConstructed<ImageView>().tag = any() } just Runs
        every { anyConstructed<ImageView>().layoutParams = any() } just Runs
        every { anyConstructed<ImageView>().scaleType = any() } just Runs
        every { anyConstructed<ImageView>().setImageDrawable(any()) } just Runs
        every { anyConstructed<ImageView>().setRenderEffect(any()) } just Runs
        every { activity.resources } returns mockk(relaxed = true)

        var callbackInvoked = false
        applyBlurView(activity) {
            callbackInvoked = true
        }

        assertTrue("Callback should be invoked with minimum dimensions", callbackInvoked)

        unmockkConstructor(ImageView::class)
        unmockkConstructor(Canvas::class)
        unmockkStatic(Bitmap::class)
        unmockkStatic(RenderEffect::class)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `applyBlurView sets correct scaleType to CENTER_CROP`() {
        val mockRootView = mockk<ViewGroup>(relaxed = true)
        every { decorView.rootView } returns mockRootView
        every { mockRootView.width } returns 1080
        every { mockRootView.height } returns 1920
        every { mockRootView.draw(any()) } just Runs
        every { mockRootView.post(any()) } answers {
            firstArg<Runnable>().run()
            true
        }

        mockkStatic(Bitmap::class)
        val mockBitmap = mockk<Bitmap>(relaxed = true)
        every { Bitmap.createBitmap(any<Int>(), any<Int>(), any()) } returns mockBitmap

        mockkConstructor(Canvas::class)
        mockkStatic(RenderEffect::class)
        every { RenderEffect.createBlurEffect(any(), any(), any()) } returns mockk(relaxed = true)

        mockkConstructor(ImageView::class)
        val capturedScaleType = slot<ImageView.ScaleType>()
        every { anyConstructed<ImageView>().tag = any() } just Runs
        every { anyConstructed<ImageView>().layoutParams = any() } just Runs
        every { anyConstructed<ImageView>().scaleType = capture(capturedScaleType) } just Runs
        every { anyConstructed<ImageView>().setImageDrawable(any()) } just Runs
        every { anyConstructed<ImageView>().setRenderEffect(any()) } just Runs
        every { activity.resources } returns mockk(relaxed = true)

        applyBlurView(activity) { }

        assertTrue("ScaleType should be captured", capturedScaleType.isCaptured)
        assertEquals(ImageView.ScaleType.CENTER_CROP, capturedScaleType.captured)

        unmockkConstructor(ImageView::class)
        unmockkConstructor(Canvas::class)
        unmockkStatic(Bitmap::class)
        unmockkStatic(RenderEffect::class)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDecorViewInfos returns null when no views exist`() {
        mockkStatic("android.view.inspector.WindowInspector")

        every { WindowInspector.getGlobalWindowViews() } returns emptyList()

        val result = ArkSecurityManager.getDecorViewInfos()

        assertNull(result)

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDecorViewInfos returns DecorViewInfo with application context`() {
        mockkStatic("android.view.inspector.WindowInspector")

        val view = mockk<View>(relaxed = true)
        val appCtx = mockk<Context>(relaxed = true)
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION
        }
        every { view.context } returns appCtx
        every { appCtx.applicationContext } returns appCtx
        every { view.layoutParams } returns lp

        every { WindowInspector.getGlobalWindowViews() } returns listOf(view)

        val info = ArkSecurityManager.getDecorViewInfos()

        assertNotNull(info)
        assertEquals(view, info?.decorView)
        assertTrue(info!!.isApplication)
        assertFalse(info.isToast)
        assertTrue(info.isDialog)

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDecorViewInfos detects TYPE_APPLICATION as dialog`() {
        mockkStatic("android.view.inspector.WindowInspector")

        val view = mockk<View>(relaxed = true)
        val ctx = mockk<Context>(relaxed = true)
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION
        }

        every { view.context } returns ctx
        every { ctx.applicationContext } returns mockk()
        every { view.layoutParams } returns lp

        every { WindowInspector.getGlobalWindowViews() } returns listOf(view)

        val info = ArkSecurityManager.getDecorViewInfos()

        assertNotNull(info)
        assertTrue(info!!.isDialog)

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDecorViewInfos detects TYPE_APPLICATION_PANEL as dialog`() {
        mockkStatic("android.view.inspector.WindowInspector")

        val view = mockk<View>(relaxed = true)
        val ctx = mockk<Context>(relaxed = true)
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
        }

        every { view.context } returns ctx
        every { ctx.applicationContext } returns ctx
        every { view.layoutParams } returns lp

        every { WindowInspector.getGlobalWindowViews() } returns listOf(view)

        val info = ArkSecurityManager.getDecorViewInfos()

        assertNotNull(info)
        assertTrue(info!!.isDialog)

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDecorViewInfos detects TYPE_APPLICATION_ATTACHED_DIALOG as dialog`() {
        mockkStatic("android.view.inspector.WindowInspector")

        val view = mockk<View>(relaxed = true)
        val ctx = mockk<Context>(relaxed = true)
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
        }

        every { view.context } returns ctx
        every { ctx.applicationContext } returns mockk()
        every { view.layoutParams } returns lp

        every { WindowInspector.getGlobalWindowViews() } returns listOf(view)

        val info = ArkSecurityManager.getDecorViewInfos()

        assertNotNull(info)
        assertTrue(info!!.isDialog)

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDecorViewInfos detects TYPE_APPLICATION_SUB_PANEL as dialog`() {
        mockkStatic("android.view.inspector.WindowInspector")

        val view = mockk<View>(relaxed = true)
        val ctx = mockk<Context>(relaxed = true)
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL
        }

        every { view.context } returns ctx
        every { ctx.applicationContext } returns mockk()
        every { view.layoutParams } returns lp

        every { WindowInspector.getGlobalWindowViews() } returns listOf(view)

        val info = ArkSecurityManager.getDecorViewInfos()

        assertNotNull(info)
        assertTrue(info!!.isDialog)

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDecorViewInfos detects TYPE_APPLICATION_OVERLAY as dialog`() {
        mockkStatic("android.view.inspector.WindowInspector")

        val view = mockk<View>(relaxed = true)
        val ctx = mockk<Context>(relaxed = true)
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }

        every { view.context } returns ctx
        every { ctx.applicationContext } returns mockk()
        every { view.layoutParams } returns lp

        every { WindowInspector.getGlobalWindowViews() } returns listOf(view)

        val info = ArkSecurityManager.getDecorViewInfos()

        assertNotNull(info)
        assertTrue(info!!.isDialog)

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDecorViewInfos detects TYPE_APPLICATION_MEDIA as dialog`() {
        mockkStatic("android.view.inspector.WindowInspector")

        val view = mockk<View>(relaxed = true)
        val ctx = mockk<Application>(relaxed = true)
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA
        }

        every { view.context } returns ctx
        every { ctx.applicationContext } returns ctx
        every { view.layoutParams } returns lp

        every { WindowInspector.getGlobalWindowViews() } returns listOf(view)

        val info = ArkSecurityManager.getDecorViewInfos()

        assertNotNull(info)
        assertTrue(info!!.isDialog)

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDecorViewInfos returns false for isDialog with non-dialog type`() {
        mockkStatic("android.view.inspector.WindowInspector")

        val view = mockk<View>(relaxed = true)
        val ctx = mockk<Context>(relaxed = true)
        val lp = WindowManager.LayoutParams()

        every { view.context } returns ctx
        every { ctx.applicationContext } returns ctx
        every { view.layoutParams } returns lp

        every { WindowInspector.getGlobalWindowViews() } returns listOf(view)

        val info = ArkSecurityManager.getDecorViewInfos()

        assertNotNull(info)
        assertFalse(info!!.isDialog)

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDecorViewInfos handles layoutParams not being WindowManager LayoutParams`() {
        mockkStatic("android.view.inspector.WindowInspector")

        val view = mockk<View>(relaxed = true)
        val ctx = mockk<Context>(relaxed = true)
        val lp = WindowManager.LayoutParams()

        every { view.context } returns ctx
        every { ctx.applicationContext } returns mockk()
        every { view.layoutParams } returns lp

        every { WindowInspector.getGlobalWindowViews() } returns listOf(view)

        val info = ArkSecurityManager.getDecorViewInfos()

        assertNotNull(info)
        assertFalse(info!!.isDialog)

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDecorViewInfos returns last view when multiple views exist`() {
        mockkStatic("android.view.inspector.WindowInspector")

        val view1 = mockk<View>(relaxed = true)
        val view2 = mockk<View>(relaxed = true)
        val ctx1 = mockk<Context>(relaxed = true)
        val ctx2 = mockk<Context>(relaxed = true)
        val lp1 = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION
        }
        val lp2 = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_TOAST
        }

        every { view1.context } returns ctx1
        every { view2.context } returns ctx2
        every { ctx1.applicationContext } returns mockk()
        every { ctx2.applicationContext } returns mockk()
        every { view1.layoutParams } returns lp1
        every { view2.layoutParams } returns lp2

        every { WindowInspector.getGlobalWindowViews() } returns listOf(view1, view2)

        val info = ArkSecurityManager.getDecorViewInfos()

        assertNotNull(info)
        assertEquals(view2, info?.decorView)
        assertTrue(info!!.isToast)

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDecorViewInfos detects toast with case insensitive name check`() {
        mockkStatic("android.view.inspector.WindowInspector")

        val view = mockk<View>(relaxed = true)
        val ctx = mockk<Context>(relaxed = true)
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_TOAST
        }

        every { view.context } returns ctx
        every { ctx.applicationContext } returns mockk()
        every { view.layoutParams } returns lp

        every { WindowInspector.getGlobalWindowViews() } returns listOf(view)

        val info = ArkSecurityManager.getDecorViewInfos()

        assertNotNull(info)
        assertTrue(info!!.isToast)

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDecorViewInfos detects toast with partial name match`() {
        mockkStatic("android.view.inspector.WindowInspector")

        val view = mockk<View>(relaxed = true)
        val ctx = mockk<Context>(relaxed = true)
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_TOAST
        }

        every { view.context } returns ctx
        every { ctx.applicationContext } returns ctx
        every { view.layoutParams } returns lp

        every { WindowInspector.getGlobalWindowViews() } returns listOf(view)

        val info = ArkSecurityManager.getDecorViewInfos()

        assertNotNull(info)
        assertTrue(info!!.isToast)

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `focusListener adds overlay when focus lost with useBlur false`() {
        val regularActivity = mockk<Activity>(relaxed = true)
        val regularWindow = mockk<Window>(relaxed = true)
        val regularDecorView = mockk<View>(relaxed = true)
        val regularRootView = mockk<ViewGroup>(relaxed = true)
        val vto = mockk<ViewTreeObserver>(relaxed = true)
        val overlay = mockk<View>(relaxed = true)

        every { regularActivity.application } returns application
        every { regularActivity.window } returns regularWindow
        every { regularWindow.decorView } returns regularDecorView
        every { regularDecorView.rootView } returns regularRootView
        every { regularDecorView.viewTreeObserver } returns vto
        every { overlay.parent } returns null
        every { regularRootView.findViewWithTag<View>(any()) } returns null
        every { regularRootView.removeView(any()) } just Runs
        every { regularRootView.addView(any()) } just Runs
        every { application.registerActivityLifecycleCallbacks(any()) } just Runs

        val focusListenerSlot = slot<ViewTreeObserver.OnWindowFocusChangeListener>()
        every { vto.addOnWindowFocusChangeListener(capture(focusListenerSlot)) } just Runs

        mockkStatic("android.view.inspector.WindowInspector")
        every { WindowInspector.getGlobalWindowViews() } returns emptyList()

        setOverviewScreenSecureView(regularActivity, overlay, useBlur = false)

        assertTrue(focusListenerSlot.isCaptured)
        focusListenerSlot.captured.onWindowFocusChanged(false)

        verify { regularRootView.addView(overlay) }

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `focusListener removes blur when focus gained with useBlur true`() {
        val regularActivity = mockk<Activity>(relaxed = true)
        val regularWindow = mockk<Window>(relaxed = true)
        val regularDecorView = mockk<View>(relaxed = true)
        val regularRootView = mockk<ViewGroup>(relaxed = true)
        val vto = mockk<ViewTreeObserver>(relaxed = true)
        val blurView = mockk<View>(relaxed = true)

        every { regularActivity.application } returns application
        every { regularActivity.window } returns regularWindow
        every { regularWindow.decorView } returns regularDecorView
        every { regularDecorView.rootView } returns regularRootView
        every { regularDecorView.viewTreeObserver } returns vto
        every { regularRootView.findViewWithTag<View>("secureViewBlur") } returns blurView
        every { blurView.parent } returns regularRootView
        every { regularRootView.removeView(blurView) } just Runs
        every { application.registerActivityLifecycleCallbacks(any()) } just Runs

        val focusListenerSlot = slot<ViewTreeObserver.OnWindowFocusChangeListener>()
        every { vto.addOnWindowFocusChangeListener(capture(focusListenerSlot)) } just Runs

        setOverviewScreenSecureView(regularActivity, null, useBlur = true)

        assertTrue(focusListenerSlot.isCaptured)
        focusListenerSlot.captured.onWindowFocusChanged(true)

        verify { regularRootView.removeView(blurView) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `focusListener removes overlay when focus gained with useBlur false`() {
        val regularActivity = mockk<Activity>(relaxed = true)
        val regularWindow = mockk<Window>(relaxed = true)
        val regularDecorView = mockk<View>(relaxed = true)
        val regularRootView = mockk<ViewGroup>(relaxed = true)
        val vto = mockk<ViewTreeObserver>(relaxed = true)
        val overlayView = mockk<View>(relaxed = true)

        every { regularActivity.application } returns application
        every { regularActivity.window } returns regularWindow
        every { regularWindow.decorView } returns regularDecorView
        every { regularDecorView.rootView } returns regularRootView
        every { regularDecorView.viewTreeObserver } returns vto
        every { regularRootView.findViewWithTag<View>("secureViewOverlay") } returns overlayView
        every { overlayView.parent } returns regularRootView
        every { regularRootView.removeView(overlayView) } just Runs
        every { application.registerActivityLifecycleCallbacks(any()) } just Runs

        val focusListenerSlot = slot<ViewTreeObserver.OnWindowFocusChangeListener>()
        every { vto.addOnWindowFocusChangeListener(capture(focusListenerSlot)) } just Runs

        setOverviewScreenSecureView(regularActivity, null, useBlur = false)

        assertTrue(focusListenerSlot.isCaptured)
        focusListenerSlot.captured.onWindowFocusChanged(true)

        verify { regularRootView.removeView(overlayView) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `focusListener does not add view when isStartingNewActivity is true`() {
        val regularActivity = mockk<Activity>(relaxed = true)
        val newActivity = mockk<Activity>(relaxed = true)
        val regularWindow = mockk<Window>(relaxed = true)
        val regularDecorView = mockk<View>(relaxed = true)
        val regularRootView = mockk<ViewGroup>(relaxed = true)
        val vto = mockk<ViewTreeObserver>(relaxed = true)

        every { regularActivity.application } returns application
        every { regularActivity.window } returns regularWindow
        every { regularWindow.decorView } returns regularDecorView
        every { regularDecorView.rootView } returns regularRootView
        every { regularDecorView.viewTreeObserver } returns vto
        every { regularRootView.findViewWithTag<View>(any()) } returns null
        every { regularRootView.removeView(any()) } just Runs
        every { regularRootView.addView(any()) } just Runs

        val lifecycleSlot = slot<Application.ActivityLifecycleCallbacks>()
        every { application.registerActivityLifecycleCallbacks(capture(lifecycleSlot)) } just Runs

        val focusListenerSlot = slot<ViewTreeObserver.OnWindowFocusChangeListener>()
        every { vto.addOnWindowFocusChangeListener(capture(focusListenerSlot)) } just Runs

        mockkStatic("android.view.inspector.WindowInspector")
        every { WindowInspector.getGlobalWindowViews() } returns emptyList()

        setOverviewScreenSecureView(regularActivity, null, useBlur = false)

        assertTrue(lifecycleSlot.isCaptured)
        lifecycleSlot.captured.onActivityStarted(newActivity)

        assertTrue(focusListenerSlot.isCaptured)
        focusListenerSlot.captured.onWindowFocusChanged(false)

        verify(exactly = 0) { regularRootView.addView(any()) }

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `focusListener does not add overlay when parent is not null`() {
        val regularActivity = mockk<Activity>(relaxed = true)
        val regularWindow = mockk<Window>(relaxed = true)
        val regularDecorView = mockk<View>(relaxed = true)
        val regularRootView = mockk<ViewGroup>(relaxed = true)
        val vto = mockk<ViewTreeObserver>(relaxed = true)
        val overlay = mockk<View>(relaxed = true)

        every { regularActivity.application } returns application
        every { regularActivity.window } returns regularWindow
        every { regularWindow.decorView } returns regularDecorView
        every { regularDecorView.rootView } returns regularRootView
        every { regularDecorView.viewTreeObserver } returns vto
        every { overlay.parent } returns mockk<ViewGroup>()
        every { regularRootView.findViewWithTag<View>(any()) } returns null
        every { regularRootView.removeView(any()) } just Runs
        every { regularRootView.addView(any()) } just Runs
        every { application.registerActivityLifecycleCallbacks(any()) } just Runs

        val focusListenerSlot = slot<ViewTreeObserver.OnWindowFocusChangeListener>()
        every { vto.addOnWindowFocusChangeListener(capture(focusListenerSlot)) } just Runs

        mockkStatic("android.view.inspector.WindowInspector")
        every { WindowInspector.getGlobalWindowViews() } returns emptyList()

        setOverviewScreenSecureView(regularActivity, overlay, useBlur = false)

        assertTrue(focusListenerSlot.isCaptured)
        focusListenerSlot.captured.onWindowFocusChanged(false)

        verify(exactly = 0) { regularRootView.addView(overlay) }

        unmockkStatic("android.view.inspector.WindowInspector")
    }

    @Test
    fun `isInstalledFromTrustedStore returns true for trusted installer`() {
        val pm = mockk<PackageManager>(relaxed = true)
        ArkSecurityManager.setTrustedStores(listOf("com.android.vending"))
        every { context.packageManager } returns pm
        every { context.packageName } returns "com.example.app"
        every { pm.getInstallerPackageName("com.example.app") } returns "com.android.vending"
        assertTrue(ArkSecurityManager.isInstalledFromTrustedStore(context))
    }

    @Test
    fun `isInstalledFromTrustedStore returns false for untrusted installer`() {
        val pm = mockk<PackageManager>(relaxed = true)
        mockkObject(ArkSecurityManager)

        every { context.packageManager } returns pm
        every { context.packageName } returns "com.example.app"
        every { pm.getInstallerPackageName("com.example.app") } returns "com.unknown.store"
        assertFalse(ArkSecurityManager.isInstalledFromTrustedStore(context))
        unmockkObject(ArkSecurityManager)
    }

    @Test
    fun `isInstalledFromTrustedStore returns false when installer is null`() {
        val pm = mockk<PackageManager>(relaxed = true)
        mockkObject(ArkSecurityManager)

        every { context.packageManager } returns pm
        every { context.packageName } returns "com.example.app"
        every { pm.getInstallerPackageName("com.example.app") } returns null
        assertFalse(ArkSecurityManager.isInstalledFromTrustedStore(context))
        unmockkObject(ArkSecurityManager)
    }

    @Test
    fun `setTrustedStores replaces trusted store list`() {
        val pm = mockk<PackageManager>(relaxed = true)
        mockkObject(ArkSecurityManager)

        every { context.packageManager } returns pm
        every { context.packageName } returns "com.example.app"
        every { pm.getInstallerPackageName("com.example.app") } returns "custom.store"
        ArkSecurityManager.setTrustedStores(listOf("custom.store"))
        assertTrue(ArkSecurityManager.isInstalledFromTrustedStore(context))
        unmockkObject(ArkSecurityManager)
    }

    @Test
    fun `removeTrustedStore removes store from trusted list`() {
        val pm = mockk<PackageManager>(relaxed = true)
        mockkObject(ArkSecurityManager)

        every { context.packageManager } returns pm
        every { context.packageName } returns "com.example.app"
        every { pm.getInstallerPackageName("com.example.app") } returns "com.android.vending"
        // Remove the default trusted store
        ArkSecurityManager.removeTrustedStore("com.android.vending")
        assertFalse(ArkSecurityManager.isInstalledFromTrustedStore(context))
        unmockkObject(ArkSecurityManager)
    }

    @Test
    fun `isInstalledFromTrustedStore uses getInstallSourceInfo on R+ and returns true for trusted installer`() {
        mockkObject(BuildHelper) {
            every { BuildHelper.getVersionSDKInt() } returns Build.VERSION_CODES.R
            val pm = mockk<PackageManager>(relaxed = true)
            ArkSecurityManager.setTrustedStores(listOf("trusted.store"))
            val installSourceInfo = mockk<InstallSourceInfo>(relaxed = true)
            every { context.packageManager } returns pm
            every { context.packageName } returns "com.example.app"
            every { pm.getInstallSourceInfo("com.example.app") } returns installSourceInfo
            every { installSourceInfo.installingPackageName } returns "trusted.store"
            assertTrue(ArkSecurityManager.isInstalledFromTrustedStore(context))
        }
    }

    @Test
    fun `isInstalledFromTrustedStore returns false and covers catch block when exception thrown`() {
        val pm = mockk<PackageManager>(relaxed = true)
        ArkSecurityManager.setTrustedStores(listOf("trusted.store"))
        every { context.packageManager } returns pm
        every { context.packageName } returns "com.example.app"
        every { pm.getInstallSourceInfo("com.example.app") } throws PackageManager.NameNotFoundException()
        every { pm.getInstallerPackageName("com.example.app") } throws PackageManager.NameNotFoundException()
        assertFalse(ArkSecurityManager.isInstalledFromTrustedStore(context))
    }
}
