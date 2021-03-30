package com.lostoy.android.architecture.lifecycle

interface IAppLifeCycleCallbacks {

    fun onForeground() = Unit
    fun onBackground() = Unit
}