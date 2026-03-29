package com.garantibbva.arkmobil.securitymanager

import android.app.Activity
import android.app.Application
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager.LayoutParams
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import com.garantibbva.arkmobil.securitymanager.extensions.removeExistingByTag
import hideKeyboard
import java.net.InetSocketAddress
import java.net.Socket

object ArkSecurityViewHelper {
    const val TAG_OVERLAY = "secureViewOverlay"
    const val TAG_BLUR = "secureViewBlur"
    const val DEFAULT_BLUR_RADIUS = 50f
    const val DEFAULT_OVERLAY_COLOR = Color.BLACK

    @RequiresApi(Build.VERSION_CODES.S)
    @JvmStatic
    fun setOverviewScreenSecureView(
        activity: Activity,
        overlayView: View? = null,
        useBlur: Boolean = true,
        @ColorInt overlayColor: Int = DEFAULT_OVERLAY_COLOR,
    ) {
        val root = activity.window.decorView.rootView as? ViewGroup
        val application = activity.application

        val overlay = overlayView ?: View(activity).apply {
            tag = TAG_OVERLAY
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setBackgroundColor(overlayColor)
        }

        var isStartingNewActivity = false

        val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityStarted(startedActivity: Activity) {
                if (startedActivity !== activity) {
                    isStartingNewActivity = true
                }
            }

            override fun onActivityDestroyed(destroyedActivity: Activity) {
                if (destroyedActivity === activity) {
                    application.unregisterActivityLifecycleCallbacks(this)
                } else {
                    isStartingNewActivity = false
                }
            }

            override fun onActivityCreated(a: Activity, savedInstanceState: android.os.Bundle?) = Unit
            override fun onActivityResumed(a: Activity) = Unit
            override fun onActivityPaused(a: Activity) = Unit
            override fun onActivityStopped(a: Activity) = Unit
            override fun onActivitySaveInstanceState(a: Activity, outState: android.os.Bundle) = Unit
        }

        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            val decorInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getDecorViewInfos()
            } else {
                null
            }

            val isCurrentActivityDecor = decorInfo?.let {
                it.activity === activity &&
                    it.isDialog.not() &&
                    it.isToast.not() &&
                    it.isApplication.not()
            } ?: true

            if (!hasFocus && !isStartingNewActivity && isCurrentActivityDecor) {
                if (activity is androidx.appcompat.app.AppCompatActivity) activity.hideKeyboard()
                if (useBlur) {
                    applyBlurView(activity) { blurView ->
                        root?.removeExistingByTag(TAG_OVERLAY)
                        root?.removeExistingByTag(TAG_BLUR)
                        root?.addView(blurView)
                    }
                } else {
                    if (overlay.parent == null) {
                        root?.addView(overlay)
                    }
                }
            } else {
                if (useBlur) {
                    root?.removeExistingByTag(TAG_BLUR)
                } else {
                    root?.removeExistingByTag(TAG_OVERLAY)
                }
            }
        }

        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        activity.window.decorView.viewTreeObserver
            .addOnWindowFocusChangeListener(focusListener)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @JvmStatic
    internal fun applyBlurView(
        activity: Activity,
        callback: (ImageView) -> Unit = {},
    ) {
        val view: View = activity.window.decorView.rootView as? ViewGroup ?: return

        view.post {
            val w = view.width
            val h = view.height
            if (w <= 0 || h <= 0) return@post

            val scaleFactor = 0.25f
            val bitmap = createBitmap((w * scaleFactor).toInt(), (h * scaleFactor).toInt())
            val canvas = Canvas(bitmap)
            canvas.scale(scaleFactor, scaleFactor)
            view.draw(canvas)

            val renderEffect = RenderEffect.createBlurEffect(
                DEFAULT_BLUR_RADIUS,
                DEFAULT_BLUR_RADIUS,
                Shader.TileMode.CLAMP,
            )
            val bitmapDrawable = bitmap.toDrawable(activity.resources)

            val imageView = ImageView(activity).apply {
                tag = TAG_BLUR
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageDrawable(bitmapDrawable)
                setRenderEffect(renderEffect)
            }

            callback(imageView)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    internal fun getDecorViewInfos(): DecorViewInfo? {
        val dialogTypes = setOf(
            LayoutParams.TYPE_APPLICATION_PANEL,
            LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
            LayoutParams.TYPE_APPLICATION_SUB_PANEL,
            LayoutParams.TYPE_APPLICATION_OVERLAY,
            LayoutParams.TYPE_APPLICATION_MEDIA,
            LayoutParams.TYPE_APPLICATION,
        )

        val windowViews = android.view.inspector.WindowInspector.getGlobalWindowViews()
        return windowViews.map { view ->

            val isToast = view.context.javaClass.name.contains(
                "Toast",
                ignoreCase = true,
            ) || (view.layoutParams is LayoutParams && (view.layoutParams as LayoutParams).type == LayoutParams.TYPE_TOAST)
            val isApplication = view.context === view.context.applicationContext
            val layoutParams = view.layoutParams
            val isDialog = if (layoutParams is LayoutParams) {
                layoutParams.type in dialogTypes
            } else {
                false
            }

            DecorViewInfo(
                decorView = view,
                isToast = isToast,
                isApplication = isApplication,
                isDialog = isDialog,
            )
        }.lastOrNull()
    }
}
