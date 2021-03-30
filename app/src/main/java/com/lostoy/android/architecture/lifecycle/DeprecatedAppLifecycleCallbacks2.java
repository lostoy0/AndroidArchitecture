package com.lostoy.android.architecture.lifecycle;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;

/**
 * Bug:
 * 1. 每次 Configurations 变化的时候会导致前后台切换回调各重走一次
 * 2. 若是在 SDK 中使用 SDK 延迟初始化，若是有 Activity 已经创建导致 SDK 中的 ActivityLifecycleCallbacks 收到的回调是不全的，
 * 这时候也会导致前后台切换判断混乱
 */
public class DeprecatedAppLifecycleCallbacks2 implements ActivityLifecycleCallbacks {

    private static int resumed;
    private static int paused;
    private static int started;
    private static int stopped;
    private static boolean inForeground;

    public DeprecatedAppLifecycleCallbacks2() {
    }

    public static boolean isAppVisible() {
        return started > stopped;
    }

    public static boolean isAppInForeground() {
        return resumed > paused;
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    public void onActivityStarted(Activity activity) {
        ++started;
    }

    public void onActivityResumed(Activity activity) {
        ++resumed;
        if (!inForeground && isAppInForeground()) {
            inForeground = true;
            this.onEnterForeground(activity);
        }
    }

    public void onActivityPaused(Activity activity) {
        ++paused;
    }

    public void onActivityStopped(Activity activity) {
        ++stopped;
        if (inForeground && !isAppVisible()) {
            inForeground = false;
            this.onEnterBackground(activity);
        }
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityDestroyed(Activity activity) {
    }

    protected void onEnterForeground(Activity activity) {
        Log.e("_raymond", "onForeground ------------");
    }

    protected void onEnterBackground(Activity activity) {
        Log.e("_raymond", "------------ onForeground");
    }
}
