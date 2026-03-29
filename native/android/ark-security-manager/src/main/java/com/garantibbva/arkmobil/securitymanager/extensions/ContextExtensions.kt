package com.garantibbva.arkmobil.securitymanager.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

val Context.activity: Activity?
    get() {
        var context: Context? = this
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }
