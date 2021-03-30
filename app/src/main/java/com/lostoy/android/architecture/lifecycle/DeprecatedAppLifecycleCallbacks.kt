package com.lostoy.android.architecture.lifecycle

import android.app.Activity

/**
 * Bug:
 * 1. 横竖屏切换的时候
 * 2. SDK 中使用在 Activity 创建后初始化
 * 3. 透明 Activity 场景
 */
class DeprecatedAppLifecycleCallbacks: IActivityLifecycleCallbacks, IAppLifeCycleCallbacks {

    private var topActivity: Activity? = null

    override fun onActivityStarted(activity: Activity) {
        if (topActivity == null) {
            onForeground()
        }
        topActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {
        if (topActivity == activity) {
            topActivity = null
            onBackground()
        }
    }

    override fun onForeground() {

    }

    override fun onBackground() {

    }
}