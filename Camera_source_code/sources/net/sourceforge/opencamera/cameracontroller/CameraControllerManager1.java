package net.sourceforge.opencamera.cameracontroller;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;

public class CameraControllerManager1 extends CameraControllerManager {
    private static final String TAG = "CControllerManager1";

    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    public boolean isFrontFacing(int i) {
        boolean z = false;
        try {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == 1) {
                z = true;
            }
            return z;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }
}
