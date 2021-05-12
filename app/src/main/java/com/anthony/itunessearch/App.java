package com.anthony.itunessearch;

import android.app.Application;

import com.sasiddiqui.itunessearch.BuildConfig;

import timber.log.Timber;

/**
 * Created by shahrukhamd on 04/06/18.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
    }
}
