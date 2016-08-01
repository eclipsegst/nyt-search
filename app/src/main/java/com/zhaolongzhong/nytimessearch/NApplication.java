package com.zhaolongzhong.nytimessearch;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class NApplication extends Application {
    private static final String TAG = NApplication.class.getSimpleName();

    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfiguration);
        Realm.getDefaultInstance();
    }

    public static Application getApplication() {
        return application;
    }
}
