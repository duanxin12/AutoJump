package com.zoom.autojump;

import android.app.Application;

import com.zoom.api.Dispatch;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Dispatch.getDefault().init(this);
    }
}
