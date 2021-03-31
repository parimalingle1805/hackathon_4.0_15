package net.sourceforge.opencamera.cameracontroller;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

public class CameraControllerManager2 extends CameraControllerManager {
    private static final String TAG = "CControllerManager2";
    private final Context context;

    public CameraControllerManager2(Context context2) {
        this.context = context2;
    }

    public int getNumberOfCameras() {
        try {
            return ((CameraManager) this.context.getSystemService("camera")).getCameraIdList().length;
        } catch (Throwable th) {
            th.printStackTrace();
            return 0;
        }
    }

    public boolean isFrontFacing(int i) {
        CameraManager cameraManager = (CameraManager) this.context.getSystemService("camera");
        boolean z = false;
        try {
            if (((Integer) cameraManager.getCameraCharacteristics(cameraManager.getCameraIdList()[i]).get(CameraCharacteristics.LENS_FACING)).intValue() == 0) {
                z = true;
            }
            return z;
        } catch (Throwable th) {
            th.printStackTrace();
            return false;
        }
    }

    static boolean isHardwareLevelSupported(CameraCharacteristics cameraCharacteristics, int i) {
        int intValue = ((Integer) cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)).intValue();
        boolean z = true;
        if (intValue == 2) {
            if (i != intValue) {
                z = false;
            }
            return z;
        }
        if (intValue == 4) {
            intValue = 0;
        }
        if (i == 4) {
            i = 0;
        }
        if (i > intValue) {
            z = false;
        }
        return z;
    }

    public boolean allowCamera2Support(int i) {
        CameraManager cameraManager = (CameraManager) this.context.getSystemService("camera");
        try {
            return isHardwareLevelSupported(cameraManager.getCameraCharacteristics(cameraManager.getCameraIdList()[i]), 0);
        } catch (Throwable th) {
            th.printStackTrace();
            return false;
        }
    }
}
