package com.lostoy.android.architecture;

import android.app.Application;

import com.didichuxing.doraemonkit.DoraemonKit;
import com.lostoy.android.architecture.lifecycle.AppLifecycleCallbacks;
import com.lostoy.android.architecture.lifecycle.DeprecatedAppLifecycleCallbacks2;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DoraemonKit.install(this);
        registerActivityLifecycleCallbacks(new AppLifecycleCallbacks());
    }
}
