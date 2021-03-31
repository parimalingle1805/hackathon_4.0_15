package net.sourceforge.opencamera.preview.camerasurface;

import android.content.Context;
import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.cameracontroller.CameraControllerException;
import net.sourceforge.opencamera.preview.Preview;

public class MyTextureView extends TextureView implements CameraSurface {
    private static final String TAG = "MyTextureView";
    private final int[] measure_spec = new int[2];
    private final Preview preview;

    public View getView() {
        return this;
    }

    public void onPause() {
    }

    public void onResume() {
    }

    public void setVideoRecorder(MediaRecorder mediaRecorder) {
    }

    public MyTextureView(Context context, Preview preview2) {
        super(context);
        this.preview = preview2;
        setSurfaceTextureListener(preview2);
    }

    public void setPreviewDisplay(CameraController cameraController) {
        try {
            cameraController.setPreviewTexture(this);
        } catch (CameraControllerException e) {
            e.printStackTrace();
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.preview.touchEvent(motionEvent);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        this.preview.getMeasureSpec(this.measure_spec, i, i2);
        int[] iArr = this.measure_spec;
        super.onMeasure(iArr[0], iArr[1]);
    }

    public void setTransform(Matrix matrix) {
        super.setTransform(matrix);
    }
}
