package com.lostoy.android.architecture;

import android.app.Application;

import com.didichuxing.doraemonkit.DoraemonKit;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DoraemonKit.install(this);
    }
}
