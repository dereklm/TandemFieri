package com.gmail.dleemcewen.tandemfieri;

import android.app.Application;

import com.beardedhen.androidbootstrap.TypefaceProvider;

public class CustomApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();

        // setup default typefaces
        TypefaceProvider.registerDefaultIconSets();
    }
}