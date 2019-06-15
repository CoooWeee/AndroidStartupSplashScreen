package com.coooweee.splash;

import android.annotation.SuppressLint;
import android.app.Application;

/**
 * <pre>
 *      author : GA
 *      time   : 15/06/2019
 *      desc   : Use this in manifest (android:name="com.coooweee.splash.App").
 * </pre>
 */

public class App extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Application app = null;


    /**
     * Remembering application 'context'
     */
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }


    /**
     * Get Application
     *
     * @return applications
     */
    public static Application getApp() {
        if (app == null) {
            throw new NullPointerException("should init first ! add android:name='com.coooweee.splash.App' to Manifest");
        }
        return app;
    }
}
