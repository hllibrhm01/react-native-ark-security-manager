package com.garantibbva.arkmobil.securitymanager

//noinspection SuspiciousImport
import android.R
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.garantibbva.arkmobil.securitymanager.extensions.activity

data class DecorViewInfo(
    val decorView: View,
    val isToast: Boolean,
    val isApplication: Boolean,
    val isDialog: Boolean,
) {
    private val contentView: ViewGroup? by lazy { decorView.findViewById(R.id.content) }
    val activity: Activity? by lazy { decorView.activity ?: contentView?.activity }
}
