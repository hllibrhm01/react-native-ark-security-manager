import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.garantibbva.arkmobil.securitymanager.ArkSecurityManager
import com.garantibbva.arkmobil.securitymanager.helper.BuildHelper

fun AppCompatActivity.applySecureView() {
    ArkSecurityManager.setWindowSecureFlag(this)
}

fun AppCompatActivity.clearSecureView() {
    ArkSecurityManager.clearWindowSecureFlag(this)
}

fun AppCompatActivity.showKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    currentFocus?.let { view ->
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    } ?: run {
        val view = window.decorView
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun AppCompatActivity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    val view = window.decorView
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
}

fun AppCompatActivity.isKeyboardVisible(): Boolean? {
    val insets = ViewCompat.getRootWindowInsets(window.decorView)
    val isKeyboardVisible = insets?.isVisible(WindowInsetsCompat.Type.ime())
    return isKeyboardVisible
}

fun AppCompatActivity.keyboardVisibilityListener(callback: (Boolean) -> Unit) {
    val rootView = window.decorView
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
        val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
        callback(isKeyboardVisible)
        ViewCompat.onApplyWindowInsets(v, insets)
    }
}

@SuppressLint("NewApi")
fun AppCompatActivity.applyOverviewProtection(useBlur: Boolean = true, overlayView: View? = null) {
        if (BuildHelper.getVersionSDKInt() >= Build.VERSION_CODES.S) {
        ArkSecurityManager.setOverviewScreenSecureView(this, overlayView, useBlur)
    } else {
        applySecureView()
    }
}
