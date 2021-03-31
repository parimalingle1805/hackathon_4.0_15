package net.sourceforge.opencamera.preview.camerasurface;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.cameracontroller.CameraControllerException;
import net.sourceforge.opencamera.preview.Preview;

public class MySurfaceView extends SurfaceView implements CameraSurface {
    private static final String TAG = "MySurfaceView";
    /* access modifiers changed from: private */
    public final Handler handler = new Handler();
    private final int[] measure_spec = new int[2];
    private final Preview preview;
    private final Runnable tick;

    public View getView() {
        return this;
    }

    public MySurfaceView(Context context, final Preview preview2) {
        super(context);
        this.preview = preview2;
        getHolder().addCallback(preview2);
        getHolder().setType(3);
        this.tick = new Runnable() {
            public void run() {
                preview2.test_ticker_called = true;
                MySurfaceView.this.invalidate();
                MySurfaceView.this.handler.postDelayed(this, preview2.getFrameRate());
            }
        };
    }

    public void setPreviewDisplay(CameraController cameraController) {
        try {
            cameraController.setPreviewDisplay(getHolder());
        } catch (CameraControllerException e) {
            e.printStackTrace();
        }
    }

    public void setVideoRecorder(MediaRecorder mediaRecorder) {
        mediaRecorder.setPreviewDisplay(getHolder().getSurface());
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.preview.touchEvent(motionEvent);
    }

    public void onDraw(Canvas canvas) {
        this.preview.draw(canvas);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        this.preview.getMeasureSpec(this.measure_spec, i, i2);
        int[] iArr = this.measure_spec;
        super.onMeasure(iArr[0], iArr[1]);
    }

    public void setTransform(Matrix matrix) {
        throw new RuntimeException();
    }

    public void onPause() {
        this.handler.removeCallbacks(this.tick);
    }

    public void onResume() {
        this.tick.run();
    }
}
