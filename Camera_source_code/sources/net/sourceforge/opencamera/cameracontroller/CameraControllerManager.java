package net.sourceforge.opencamera.cameracontroller;

public abstract class CameraControllerManager {
    public abstract int getNumberOfCameras();

    public abstract boolean isFrontFacing(int i);
}
