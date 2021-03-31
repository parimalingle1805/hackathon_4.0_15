package net.sourceforge.opencamera.preview.camerasurface;

import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.view.View;
import net.sourceforge.opencamera.cameracontroller.CameraController;

public interface CameraSurface {
    View getView();

    void onPause();

    void onResume();

    void setPreviewDisplay(CameraController cameraController);

    void setTransform(Matrix matrix);

    void setVideoRecorder(MediaRecorder mediaRecorder);
}
