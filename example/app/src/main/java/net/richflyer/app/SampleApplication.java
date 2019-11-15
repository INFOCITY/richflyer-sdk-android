package net.richflyer.app;

import androidx.multidex.MultiDexApplication;

public class SampleApplication extends MultiDexApplication {

    private static SampleApplication application = null;

    @Override
    public void onCreate() {
        super.onCreate();


        application = this;
    }

    public static SampleApplication getApplication() {
        return application;
    }
}
