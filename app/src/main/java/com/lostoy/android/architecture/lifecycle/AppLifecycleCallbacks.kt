package com.lostoy.android.architecture.lifecycle

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log

@SuppressLint("NewApi")
class AppLifecycleCallbacks : IActivityLifecycleCallbacks, IAppLifeCycleCallbacks {

    private var activityRefCount = 0
    private val activityCounter: MutableMap<Int, Int> = mutableMapOf()
    private var isChangingConfigurations = false

    override fun onActivityStarted(activity: Activity) {
        activityRefCount ++
        increment(activity)
        if (activityRefCount == 1 && !isChangingConfigurations) {
            onForeground()
        }
    }

    override fun onActivityPaused(activity: Activity) {
        isChangingConfigurations = activity.isChangingConfigurations
    }

    override fun onActivityStopped(activity: Activity) {
        if (activityCounter[activity.hashCode()] ?: 0 > 0) {
            activityRefCount --
            decrement(activity)
        }
        if (activityRefCount == 0 && !isChangingConfigurations) {
            onBackground()
        }
    }

    private fun increment(activity: Activity) {
        val key = activity.hashCode()
        activityCounter[key] = activityCounter[key] ?: 0 + 1
    }

    private fun decrement(activity: Activity) {
        val key = activity.hashCode()
        activityCounter[key] = activityCounter[key] ?: 0 - 1
    }

    override fun onForeground() {

    }

    override fun onBackground() {

    }
}