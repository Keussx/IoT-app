package com.ksopha.thanetearth;

import android.app.Application;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Kelvin Sopha on 31/03/18.
 */

public class Start extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name("thanet.earth")
                .schemaVersion(0)
                .build();
        Realm.setDefaultConfiguration(realmConfig);
    }
}
