package net.sourceforge.opencamera;

import android.app.Application;
import android.os.Process;
import android.util.Log;

public class OpenCameraApplication extends Application {
    private static final String TAG = "OpenCameraApplication";

    public void onCreate() {
        super.onCreate();
        checkAppReplacingState();
    }

    private void checkAppReplacingState() {
        if (getResources() == null) {
            Log.e(TAG, "app is replacing, kill");
            Process.killProcess(Process.myPid());
        }
    }
}
