package com.garantibbva.arkmobil.securitymanager.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

val View.activity: Activity?
    get() = context.activity

internal fun ViewGroup.removeExistingByTag(tag: Any) {
    val child = findViewWithTag<View>(tag)
    if (child != null && child.parent === this) {
        removeView(child)
    }
}
