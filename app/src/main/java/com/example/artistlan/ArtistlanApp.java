package com.example.artistlan;

import android.app.Application;
import android.content.Context;

public class ArtistlanApp extends Application {

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return appContext;
    }
}
