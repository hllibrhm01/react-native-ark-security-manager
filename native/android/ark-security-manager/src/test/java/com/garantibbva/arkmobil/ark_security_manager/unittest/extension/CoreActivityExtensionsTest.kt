package com.garantibbva.arkmobil.ark_security_manager.unittest.extension

import android.content.Context
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import applySecureView
import clearSecureView
import com.garantibbva.arkmobil.securitymanager.ArkSecurityManager
import hideKeyboard
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import isKeyboardVisible
import keyboardVisibilityListener
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import showKeyboard

@RunWith(RobolectricTestRunner::class)
class CoreActivityExtensionsTest {

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `applySecureView calls ArkSecurityManager setWindowSecureFlag`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        mockkObject(ArkSecurityManager)
        coreActivity.applySecureView()
        verify { ArkSecurityManager.setWindowSecureFlag(coreActivity) }
    }

    @Test
    fun `clearSecureView calls ArkSecurityManager setWindowSecureFlag`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        mockkObject(ArkSecurityManager)
        coreActivity.clearSecureView()
        verify { ArkSecurityManager.clearWindowSecureFlag(coreActivity) }
    }

    @Test
    fun `showKeyboard calls showSoftInput on currentFocus`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        val imm = mockk<InputMethodManager>(relaxed = true)
        val view = mockk<View>(relaxed = true)
        val window = mockk<Window>(relaxed = true)
        every { coreActivity.getSystemService(Context.INPUT_METHOD_SERVICE) } returns imm
        every { coreActivity.currentFocus } returns view
        every { coreActivity.window } returns window
        every { window.decorView } returns view
        coreActivity.showKeyboard()
        verify { imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT) }
    }

    @Test
    fun `showKeyboard calls showSoftInput on decorView when currentFocus is null`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        val imm = mockk<InputMethodManager>(relaxed = true)
        val decorView = mockk<View>(relaxed = true)
        val window = mockk<Window>(relaxed = true)
        every { coreActivity.getSystemService(Context.INPUT_METHOD_SERVICE) } returns imm
        every { coreActivity.currentFocus } returns null
        every { coreActivity.window } returns window
        every { window.decorView } returns decorView
        coreActivity.showKeyboard()
        verify { imm.showSoftInput(decorView, InputMethodManager.SHOW_IMPLICIT) }
    }

    @Test
    fun `hideKeyboard calls hideSoftInputFromWindow on currentFocus window token`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        val imm = mockk<InputMethodManager>(relaxed = true)
        every { coreActivity.getSystemService(Context.INPUT_METHOD_SERVICE) } returns imm
        coreActivity.hideKeyboard()
        verify { imm.hideSoftInputFromWindow(any(), 0) }
    }

    @Test
    fun `hideKeyboard calls nothing on currentFocus window token`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        val imm = mockk<InputMethodManager>(relaxed = true)
        every { coreActivity.getSystemService(Context.INPUT_METHOD_SERVICE) } returns null
        coreActivity.hideKeyboard()
        verify(exactly = 0) { imm.hideSoftInputFromWindow(any(), 0) }
    }

    @Test
    fun `isKeyboardVisible returns true when keyboard is visible`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        val window = mockk<Window>(relaxed = true)
        val decorView = mockk<View>(relaxed = true)
        val insets = mockk<WindowInsetsCompat>(relaxed = true)

        every { coreActivity.window } returns window
        every { window.decorView } returns decorView
        mockkStatic(ViewCompat::class)
        every { ViewCompat.getRootWindowInsets(decorView) } returns insets
        every { insets.isVisible(WindowInsetsCompat.Type.ime()) } returns true

        val result = coreActivity.isKeyboardVisible()

        assert(result == true)
    }

    @Test
    fun `isKeyboardVisible returns false when keyboard is not visible`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        val window = mockk<Window>(relaxed = true)
        val decorView = mockk<View>(relaxed = true)
        val insets = mockk<WindowInsetsCompat>(relaxed = true)

        every { coreActivity.window } returns window
        every { window.decorView } returns decorView
        mockkStatic(ViewCompat::class)
        every { ViewCompat.getRootWindowInsets(decorView) } returns insets
        every { insets.isVisible(WindowInsetsCompat.Type.ime()) } returns false

        val result = coreActivity.isKeyboardVisible()

        assert(result == false)
    }

    @Test
    fun `isKeyboardVisible returns null when insets are null`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        val window = mockk<Window>(relaxed = true)
        val decorView = mockk<View>(relaxed = true)

        every { coreActivity.window } returns window
        every { window.decorView } returns decorView
        mockkStatic(ViewCompat::class)
        every { ViewCompat.getRootWindowInsets(decorView) } returns null

        val result = coreActivity.isKeyboardVisible()

        assert(result == null)
    }

    @Test
    fun `keyboardVisibilityListener sets listener and invokes callback when keyboard becomes visible`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        val window = mockk<Window>(relaxed = true)
        val decorView = mockk<View>(relaxed = true)
        val insets = mockk<WindowInsetsCompat>(relaxed = true)
        var callbackInvoked = false
        var keyboardVisible: Boolean? = null

        every { coreActivity.window } returns window
        every { window.decorView } returns decorView
        every { insets.isVisible(WindowInsetsCompat.Type.ime()) } returns true

        mockkStatic(ViewCompat::class)
        every {
            ViewCompat.setOnApplyWindowInsetsListener(decorView, any())
        } answers {
            val listener = secondArg<OnApplyWindowInsetsListener>()
            listener.onApplyWindowInsets(decorView, insets)
        }
        every { ViewCompat.onApplyWindowInsets(decorView, insets) } returns insets

        coreActivity.keyboardVisibilityListener { isVisible ->
            callbackInvoked = true
            keyboardVisible = isVisible
        }

        assert(callbackInvoked)
        assert(keyboardVisible == true)
        verify { ViewCompat.setOnApplyWindowInsetsListener(decorView, any()) }
    }

    @Test
    fun `keyboardVisibilityListener sets listener and invokes callback when keyboard becomes hidden`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        val window = mockk<Window>(relaxed = true)
        val decorView = mockk<View>(relaxed = true)
        val insets = mockk<WindowInsetsCompat>(relaxed = true)
        var callbackInvoked = false
        var keyboardVisible: Boolean? = null

        every { coreActivity.window } returns window
        every { window.decorView } returns decorView
        every { insets.isVisible(WindowInsetsCompat.Type.ime()) } returns false

        mockkStatic(ViewCompat::class)
        every {
            ViewCompat.setOnApplyWindowInsetsListener(decorView, any())
        } answers {
            val listener = secondArg<OnApplyWindowInsetsListener>()
            listener.onApplyWindowInsets(decorView, insets)
        }
        every { ViewCompat.onApplyWindowInsets(decorView, insets) } returns insets

        coreActivity.keyboardVisibilityListener { isVisible ->
            callbackInvoked = true
            keyboardVisible = isVisible
        }

        assert(callbackInvoked)
        assert(keyboardVisible == false)
        verify { ViewCompat.setOnApplyWindowInsetsListener(decorView, any()) }
    }

    @Test
    fun `keyboardVisibilityListener handles multiple callback invocations`() {
        val coreActivity = mockk<AppCompatActivity>(relaxed = true)
        val window = mockk<Window>(relaxed = true)
        val decorView = mockk<View>(relaxed = true)
        val insets = mockk<WindowInsetsCompat>(relaxed = true)
        val callbackResults = mutableListOf<Boolean>()

        every { coreActivity.window } returns window
        every { window.decorView } returns decorView
        every { insets.isVisible(WindowInsetsCompat.Type.ime()) } returnsMany listOf(true, false, true)

        mockkStatic(ViewCompat::class)
        var capturedListener: OnApplyWindowInsetsListener? = null
        every {
            ViewCompat.setOnApplyWindowInsetsListener(decorView, any())
        } answers {
            capturedListener = secondArg()
        }
        every { ViewCompat.onApplyWindowInsets(decorView, insets) } returns insets

        coreActivity.keyboardVisibilityListener { isVisible ->
            callbackResults.add(isVisible)
        }

        // Simulate multiple insets changes
        capturedListener?.onApplyWindowInsets(decorView, insets)
        capturedListener?.onApplyWindowInsets(decorView, insets)
        capturedListener?.onApplyWindowInsets(decorView, insets)

        assert(callbackResults.size == 3)
        assert(callbackResults[0])
        assert(!callbackResults[1])
        assert(callbackResults[2])
    }
}
