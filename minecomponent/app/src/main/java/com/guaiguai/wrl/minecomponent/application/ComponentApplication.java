package com.guaiguai.wrl.minecomponent.application;

import android.app.Application;

/**
 * Created by WRL on 2017/5/3.
 */
public class ComponentApplication extends Application {

    private static ComponentApplication application = null;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static ComponentApplication getInstance () {
        return application;
    }
}
