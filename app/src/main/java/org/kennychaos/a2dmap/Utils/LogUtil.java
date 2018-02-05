package org.kennychaos.a2dmap.Utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 * Created by Kenny on 18-2-5.
 */

public class LogUtil {
    public final static int LOG_LEVEL_VERBOSE = 0;
    public final static int LOG_LEVEL_INFO = 1;
    public final static int LOG_LEVEL_ERROR = 2;

    private Activity activity = null;

    LogUtil(Activity activity)
    {
        this.activity = activity;
    }

    public void show(String log_msg,int log_level)
    {
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            String TAG = "=====" + activity.getLocalClassName().toLowerCase() + "===== ";
            switch (log_level) {
                case LOG_LEVEL_VERBOSE:
                    Log.v(TAG,log_msg);
                    break;
                case LOG_LEVEL_INFO:
                    Log.i(TAG,log_msg);
                    break;
                case LOG_LEVEL_ERROR:
                    Log.e(TAG,log_msg);
                    break;
            }
        }
    }
}
