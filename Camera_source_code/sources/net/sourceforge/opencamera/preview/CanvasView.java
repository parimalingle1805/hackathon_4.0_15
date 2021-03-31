package net.sourceforge.opencamera.preview;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.View;

public class CanvasView extends View {
    private static final String TAG = "CanvasView";
    /* access modifiers changed from: private */
    public final Handler handler = new Handler();
    private final int[] measure_spec = new int[2];
    private final Preview preview;
    private final Runnable tick;

    CanvasView(Context context, final Preview preview2) {
        super(context);
        this.preview = preview2;
        this.tick = new Runnable() {
            public void run() {
                preview2.test_ticker_called = true;
                CanvasView.this.invalidate();
                CanvasView.this.handler.postDelayed(this, preview2.getFrameRate());
            }
        };
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

    /* access modifiers changed from: 0000 */
    public void onPause() {
        this.handler.removeCallbacks(this.tick);
    }

    /* access modifiers changed from: 0000 */
    public void onResume() {
        this.tick.run();
    }
}
