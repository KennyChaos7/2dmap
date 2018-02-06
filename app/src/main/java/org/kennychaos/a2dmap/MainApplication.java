package org.kennychaos.a2dmap;

import android.app.Application;

import org.xutils.x;

/**
 * Created by Kenny on 18-2-6.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(false);
    }
}
