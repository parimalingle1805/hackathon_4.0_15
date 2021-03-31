package net.sourceforge.opencamera.preview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RSInvalidStateException;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.support.p000v4.app.NotificationManagerCompat;
import android.support.p000v4.content.ContextCompat;
import android.support.p000v4.view.ViewCompat;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.sourceforge.opencamera.BuildConfig;
import net.sourceforge.opencamera.C0316R;
import net.sourceforge.opencamera.ScriptC_histogram_compute;
import net.sourceforge.opencamera.TakePhoto;
import net.sourceforge.opencamera.ToastBoxer;
import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.cameracontroller.CameraController.Area;
import net.sourceforge.opencamera.cameracontroller.CameraController.AutoFocusCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController.BurstType;
import net.sourceforge.opencamera.cameracontroller.CameraController.ContinuousFocusMoveCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController.ErrorCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController.Face;
import net.sourceforge.opencamera.cameracontroller.CameraController.PictureCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController.Size;
import net.sourceforge.opencamera.cameracontroller.CameraController1;
import net.sourceforge.opencamera.cameracontroller.CameraController2;
import net.sourceforge.opencamera.cameracontroller.CameraControllerException;
import net.sourceforge.opencamera.cameracontroller.CameraControllerManager;
import net.sourceforge.opencamera.cameracontroller.CameraControllerManager1;
import net.sourceforge.opencamera.cameracontroller.CameraControllerManager2;
import net.sourceforge.opencamera.cameracontroller.RawImage;
import net.sourceforge.opencamera.preview.ApplicationInterface.NRModePref;
import net.sourceforge.opencamera.preview.ApplicationInterface.NoFreeStorageException;
import net.sourceforge.opencamera.preview.ApplicationInterface.RawPref;
import net.sourceforge.opencamera.preview.VideoQualityHandler.Dimension2D;
import net.sourceforge.opencamera.preview.camerasurface.CameraSurface;
import net.sourceforge.opencamera.preview.camerasurface.MySurfaceView;
import net.sourceforge.opencamera.preview.camerasurface.MyTextureView;

public class Preview implements Callback, SurfaceTextureListener {
    private static final int FOCUS_DONE = 3;
    private static final int FOCUS_FAILED = 2;
    private static final int FOCUS_SUCCESS = 1;
    private static final int FOCUS_WAITING = 0;
    private static final int PHASE_NORMAL = 0;
    private static final int PHASE_PREVIEW_PAUSED = 3;
    private static final int PHASE_TAKING_PHOTO = 2;
    private static final int PHASE_TIMER = 1;
    private static final String TAG = "Preview";
    private static final long min_safe_restart_video_time = 1000;
    private static final float sensor_alpha = 0.8f;
    /* access modifiers changed from: private */
    public final AccessibilityManager accessibility_manager;
    /* access modifiers changed from: private */
    public RotatedTextView active_fake_toast;
    private List<String> antibanding;
    private boolean app_is_paused;
    /* access modifiers changed from: private */
    public final ApplicationInterface applicationInterface;
    private double aspect_ratio;
    private boolean autofocus_in_continuous_mode;
    private final Timer batteryCheckVideoTimer;
    /* access modifiers changed from: private */
    public TimerTask batteryCheckVideoTimerTask;
    /* access modifiers changed from: private */
    public final IntentFilter battery_ifilter;
    private final Timer beepTimer;
    /* access modifiers changed from: private */
    public TimerTask beepTimerTask;
    private final float[] cameraRotation;
    /* access modifiers changed from: private */
    public final CameraSurface cameraSurface;
    /* access modifiers changed from: private */
    public CameraController camera_controller;
    private final CameraControllerManager camera_controller_manager;
    private boolean camera_controller_supports_zoom;
    /* access modifiers changed from: private */
    public CameraOpenState camera_open_state;
    private final Matrix camera_to_preview_matrix = new Matrix();
    private boolean can_disable_shutter_sound;
    private CanvasView canvasView;
    private float capture_rate_factor;
    /* access modifiers changed from: private */
    public CloseCameraTask close_camera_task;
    private List<String> color_effects;
    /* access modifiers changed from: private */
    public boolean continuous_focus_move_is_started;
    public volatile int count_cameraAutoFocus;
    public volatile int count_cameraContinuousFocusMoving;
    public volatile int count_cameraStartPreview;
    public volatile int count_cameraTakePicture;
    private int current_flash_index;
    private int current_focus_index;
    private int current_orientation;
    private int current_rotation;
    private int current_size_index;
    private final DecimalFormat decimal_format_1dp;
    private final DecimalFormat decimal_format_2dp;
    private final float[] deviceInclination;
    private final float[] deviceRotation;
    private List<String> edge_modes;
    private float exposure_step;
    private List<String> exposures;
    /* access modifiers changed from: private */
    public final RectF face_rect;
    /* access modifiers changed from: private */
    public Face[] faces_detected;
    /* access modifiers changed from: private */
    public final Handler fake_toast_handler;
    private final Timer flashVideoTimer;
    /* access modifiers changed from: private */
    public TimerTask flashVideoTimerTask;
    private final ToastBoxer flash_toast;
    private long focus_complete_time;
    /* access modifiers changed from: private */
    public Bitmap focus_peaking_bitmap;
    /* access modifiers changed from: private */
    public Bitmap focus_peaking_bitmap_buffer;
    private int focus_screen_x;
    private int focus_screen_y;
    private long focus_started_time;
    private int focus_success;
    private final ToastBoxer focus_toast;
    private final float[] geo_direction;
    private final float[] geomagnetic;
    private final GestureDetector gestureDetector;
    private final float[] gravity;
    private boolean has_aspect_ratio;
    private boolean has_capture_rate_factor;
    private boolean has_focus_area;
    private boolean has_geo_direction;
    private boolean has_geomagnetic;
    private boolean has_gravity;
    private boolean has_level_angle;
    private boolean has_permissions;
    private boolean has_pitch_angle;
    private boolean has_surface;
    /* access modifiers changed from: private */
    public boolean has_zoom;
    /* access modifiers changed from: private */
    public int[] histogram;
    /* access modifiers changed from: private */
    public ScriptC_histogram_compute histogramScript;
    /* access modifiers changed from: private */
    public HistogramType histogram_type = HistogramType.HISTOGRAM_TYPE_VALUE;
    private boolean is_exposure_lock_supported;
    private boolean is_exposure_locked;
    /* access modifiers changed from: private */
    public boolean is_preview_started;
    private boolean is_test;
    private boolean is_video;
    private boolean is_white_balance_lock_supported;
    private boolean is_white_balance_locked;
    private List<String> isos;
    private long last_histogram_time_ms;
    private long last_preview_bitmap_time_ms;
    /* access modifiers changed from: private */
    public Toast last_toast;
    /* access modifiers changed from: private */
    public long last_toast_time_ms;
    private double level_angle;
    private int max_expo_bracketing_n_images;
    private int max_exposure;
    private long max_exposure_time;
    private int max_iso;
    private int max_num_focus_areas;
    private int max_temperature;
    private int max_zoom_factor;
    private int min_exposure;
    private long min_exposure_time;
    private int min_iso;
    private int min_temperature;
    private float minimum_focus_distance;
    private double natural_level_angle;
    private final float[] new_geo_direction;
    private List<String> noise_reduction_modes;
    /* access modifiers changed from: private */
    public AsyncTask<Void, Void, CameraController> open_camera_task;
    private OrientationEventListener orientationEventListener;
    private double orig_level_angle;
    private final ToastBoxer pause_video_toast;
    /* access modifiers changed from: private */
    public volatile int phase;
    private double pitch_angle;
    /* access modifiers changed from: private */
    public Bitmap preview_bitmap;
    private int preview_h;
    private double preview_targetRatio;
    private final Matrix preview_to_camera_matrix = new Matrix();
    private int preview_w;
    /* access modifiers changed from: private */
    public RefreshPreviewBitmapTask refreshPreviewBitmapTask;
    /* access modifiers changed from: private */
    public int remaining_repeat_photos;
    private int remaining_restart_video;
    private final Handler reset_continuous_focus_handler;
    /* access modifiers changed from: private */
    public Runnable reset_continuous_focus_runnable;
    /* access modifiers changed from: private */

    /* renamed from: rs */
    public RenderScript f19rs;
    private final ScaleGestureDetector scaleGestureDetector;
    private List<String> scene_modes;
    private String set_flash_value_after_autofocus;
    private boolean set_preview_size;
    private boolean set_textureview_size;
    private List<Size> sizes;
    private boolean successfully_focused;
    private long successfully_focused_time;
    private List<String> supported_flash_values;
    private List<String> supported_focus_values;
    private List<Size> supported_preview_sizes;
    private boolean supports_burst;
    private boolean supports_expo_bracketing;
    private boolean supports_exposure_time;
    private boolean supports_face_detection;
    private boolean supports_focus_bracketing;
    private boolean supports_iso_range;
    private boolean supports_photo_video_recording;
    private boolean supports_raw;
    private boolean supports_tonemap_curve;
    private boolean supports_video;
    private boolean supports_video_high_speed;
    private boolean supports_video_stabilization;
    private boolean supports_white_balance_temperature;
    private final Timer takePictureTimer;
    /* access modifiers changed from: private */
    public TimerTask takePictureTimerTask;
    private boolean take_photo_after_autofocus;
    private long take_photo_time;
    private final ToastBoxer take_photo_toast;
    public volatile boolean test_fail_open_camera;
    public volatile boolean test_ticker_called;
    public volatile boolean test_video_failure;
    private int textureview_h;
    private int textureview_w;
    private int tonemap_max_curve_points;
    private float touch_orig_x;
    private float touch_orig_y;
    private boolean touch_was_multitouch;
    /* access modifiers changed from: private */
    public int ui_rotation;
    /* access modifiers changed from: private */
    public final boolean using_android_l;
    private boolean using_face_detection;
    private VideoFileInfo videoFileInfo;
    private long video_accumulated_time;
    private boolean video_high_speed;
    private final VideoQualityHandler video_quality_handler;
    private volatile MediaRecorder video_recorder;
    private boolean video_recorder_is_paused;
    private boolean video_restart_on_max_filesize;
    private long video_start_time;
    private volatile boolean video_start_time_set;
    private float view_angle_x;
    private float view_angle_y;
    /* access modifiers changed from: private */
    public boolean want_focus_peaking;
    private boolean want_histogram;
    private boolean want_preview_bitmap;
    /* access modifiers changed from: private */
    public boolean want_zebra_stripes;
    private List<String> white_balances;
    /* access modifiers changed from: private */
    public Bitmap zebra_stripes_bitmap;
    /* access modifiers changed from: private */
    public Bitmap zebra_stripes_bitmap_buffer;
    /* access modifiers changed from: private */
    public int zebra_stripes_threshold;
    private List<Integer> zoom_ratios;

    /* renamed from: net.sourceforge.opencamera.preview.Preview$23 */
    static /* synthetic */ class C036023 {

        /* renamed from: $SwitchMap$net$sourceforge$opencamera$preview$Preview$FaceLocation */
        static final /* synthetic */ int[] f20xe8f629e0 = new int[FaceLocation.values().length];

        /* renamed from: $SwitchMap$net$sourceforge$opencamera$preview$Preview$HistogramType */
        static final /* synthetic */ int[] f21xe5e4bd0 = new int[HistogramType.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(20:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|28) */
        /* JADX WARNING: Can't wrap try/catch for region: R(22:0|1|2|3|(2:5|6)|7|(2:9|10)|11|13|14|15|17|18|19|20|21|22|23|24|25|26|28) */
        /* JADX WARNING: Can't wrap try/catch for region: R(23:0|1|2|3|5|6|7|(2:9|10)|11|13|14|15|17|18|19|20|21|22|23|24|25|26|28) */
        /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x0048 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0052 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x005c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0066 */
        static {
            /*
                net.sourceforge.opencamera.preview.Preview$HistogramType[] r0 = net.sourceforge.opencamera.preview.Preview.HistogramType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f21xe5e4bd0 = r0
                r0 = 1
                int[] r1 = f21xe5e4bd0     // Catch:{ NoSuchFieldError -> 0x0014 }
                net.sourceforge.opencamera.preview.Preview$HistogramType r2 = net.sourceforge.opencamera.preview.Preview.HistogramType.HISTOGRAM_TYPE_LUMINANCE     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r1[r2] = r0     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                r1 = 2
                int[] r2 = f21xe5e4bd0     // Catch:{ NoSuchFieldError -> 0x001f }
                net.sourceforge.opencamera.preview.Preview$HistogramType r3 = net.sourceforge.opencamera.preview.Preview.HistogramType.HISTOGRAM_TYPE_VALUE     // Catch:{ NoSuchFieldError -> 0x001f }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2[r3] = r1     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                r2 = 3
                int[] r3 = f21xe5e4bd0     // Catch:{ NoSuchFieldError -> 0x002a }
                net.sourceforge.opencamera.preview.Preview$HistogramType r4 = net.sourceforge.opencamera.preview.Preview.HistogramType.HISTOGRAM_TYPE_INTENSITY     // Catch:{ NoSuchFieldError -> 0x002a }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x002a }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x002a }
            L_0x002a:
                r3 = 4
                int[] r4 = f21xe5e4bd0     // Catch:{ NoSuchFieldError -> 0x0035 }
                net.sourceforge.opencamera.preview.Preview$HistogramType r5 = net.sourceforge.opencamera.preview.Preview.HistogramType.HISTOGRAM_TYPE_LIGHTNESS     // Catch:{ NoSuchFieldError -> 0x0035 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0035 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0035 }
            L_0x0035:
                net.sourceforge.opencamera.preview.Preview$FaceLocation[] r4 = net.sourceforge.opencamera.preview.Preview.FaceLocation.values()
                int r4 = r4.length
                int[] r4 = new int[r4]
                f20xe8f629e0 = r4
                int[] r4 = f20xe8f629e0     // Catch:{ NoSuchFieldError -> 0x0048 }
                net.sourceforge.opencamera.preview.Preview$FaceLocation r5 = net.sourceforge.opencamera.preview.Preview.FaceLocation.FACELOCATION_CENTRE     // Catch:{ NoSuchFieldError -> 0x0048 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0048 }
                r4[r5] = r0     // Catch:{ NoSuchFieldError -> 0x0048 }
            L_0x0048:
                int[] r0 = f20xe8f629e0     // Catch:{ NoSuchFieldError -> 0x0052 }
                net.sourceforge.opencamera.preview.Preview$FaceLocation r4 = net.sourceforge.opencamera.preview.Preview.FaceLocation.FACELOCATION_LEFT     // Catch:{ NoSuchFieldError -> 0x0052 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0052 }
                r0[r4] = r1     // Catch:{ NoSuchFieldError -> 0x0052 }
            L_0x0052:
                int[] r0 = f20xe8f629e0     // Catch:{ NoSuchFieldError -> 0x005c }
                net.sourceforge.opencamera.preview.Preview$FaceLocation r1 = net.sourceforge.opencamera.preview.Preview.FaceLocation.FACELOCATION_RIGHT     // Catch:{ NoSuchFieldError -> 0x005c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x005c }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x005c }
            L_0x005c:
                int[] r0 = f20xe8f629e0     // Catch:{ NoSuchFieldError -> 0x0066 }
                net.sourceforge.opencamera.preview.Preview$FaceLocation r1 = net.sourceforge.opencamera.preview.Preview.FaceLocation.FACELOCATION_TOP     // Catch:{ NoSuchFieldError -> 0x0066 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0066 }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x0066 }
            L_0x0066:
                int[] r0 = f20xe8f629e0     // Catch:{ NoSuchFieldError -> 0x0071 }
                net.sourceforge.opencamera.preview.Preview$FaceLocation r1 = net.sourceforge.opencamera.preview.Preview.FaceLocation.FACELOCATION_BOTTOM     // Catch:{ NoSuchFieldError -> 0x0071 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0071 }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0071 }
            L_0x0071:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.preview.Preview.C036023.<clinit>():void");
        }
    }

    enum CameraOpenState {
        CAMERAOPENSTATE_CLOSED,
        CAMERAOPENSTATE_OPENING,
        CAMERAOPENSTATE_OPENED,
        CAMERAOPENSTATE_CLOSING
    }

    private interface CloseCameraCallback {
        void onClosed();
    }

    private class CloseCameraTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "CloseCameraTask";
        final CameraController camera_controller_local;
        final CloseCameraCallback closeCameraCallback;
        boolean reopen;

        CloseCameraTask(CameraController cameraController, CloseCameraCallback closeCameraCallback2) {
            this.camera_controller_local = cameraController;
            this.closeCameraCallback = closeCameraCallback2;
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voidArr) {
            this.camera_controller_local.stopPreview();
            this.camera_controller_local.release();
            return null;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void voidR) {
            Preview.this.camera_open_state = CameraOpenState.CAMERAOPENSTATE_CLOSED;
            Preview.this.close_camera_task = null;
            CloseCameraCallback closeCameraCallback2 = this.closeCameraCallback;
            if (closeCameraCallback2 != null) {
                closeCameraCallback2.onClosed();
            }
            if (this.reopen) {
                Preview.this.openCamera();
            }
        }
    }

    private class DoubleTapListener extends SimpleOnGestureListener {
        private DoubleTapListener() {
        }

        public boolean onDoubleTap(MotionEvent motionEvent) {
            return Preview.this.onDoubleTap();
        }
    }

    enum FaceLocation {
        FACELOCATION_UNSET,
        FACELOCATION_UNKNOWN,
        FACELOCATION_LEFT,
        FACELOCATION_RIGHT,
        FACELOCATION_TOP,
        FACELOCATION_BOTTOM,
        FACELOCATION_CENTRE
    }

    public enum HistogramType {
        HISTOGRAM_TYPE_RGB,
        HISTOGRAM_TYPE_LUMINANCE,
        HISTOGRAM_TYPE_VALUE,
        HISTOGRAM_TYPE_INTENSITY,
        HISTOGRAM_TYPE_LIGHTNESS
    }

    private static class RefreshPreviewBitmapTask extends AsyncTask<Void, Void, RefreshPreviewBitmapTaskResult> {
        private static final String TAG = "RefreshPreviewBmTask";
        private final WeakReference<Bitmap> focus_peaking_bitmap_bufferReference;
        private final WeakReference<ScriptC_histogram_compute> histogramScriptReference;
        private final WeakReference<Preview> previewReference;
        private final WeakReference<Bitmap> preview_bitmapReference;
        private final boolean update_histogram;
        private final WeakReference<Bitmap> zebra_stripes_bitmap_bufferReference;

        RefreshPreviewBitmapTask(Preview preview, boolean z) {
            this.previewReference = new WeakReference<>(preview);
            this.preview_bitmapReference = new WeakReference<>(preview.preview_bitmap);
            this.zebra_stripes_bitmap_bufferReference = new WeakReference<>(preview.zebra_stripes_bitmap_buffer);
            this.focus_peaking_bitmap_bufferReference = new WeakReference<>(preview.focus_peaking_bitmap_buffer);
            this.update_histogram = z;
            if (preview.f19rs == null) {
                preview.f19rs = RenderScript.create(preview.getContext());
            }
            if (preview.histogramScript == null) {
                preview.histogramScript = new ScriptC_histogram_compute(preview.f19rs);
            }
            this.histogramScriptReference = new WeakReference<>(preview.histogramScript);
        }

        private static int[] computeHistogram(Allocation allocation, RenderScript renderScript, ScriptC_histogram_compute scriptC_histogram_compute, HistogramType histogramType) {
            if (histogramType == HistogramType.HISTOGRAM_TYPE_RGB) {
                Allocation createSized = Allocation.createSized(renderScript, Element.I32(renderScript), 256);
                Allocation createSized2 = Allocation.createSized(renderScript, Element.I32(renderScript), 256);
                Allocation createSized3 = Allocation.createSized(renderScript, Element.I32(renderScript), 256);
                scriptC_histogram_compute.bind_histogram_r(createSized);
                scriptC_histogram_compute.bind_histogram_g(createSized2);
                scriptC_histogram_compute.bind_histogram_b(createSized3);
                scriptC_histogram_compute.invoke_init_histogram_rgb();
                scriptC_histogram_compute.forEach_histogram_compute_rgb(allocation);
                int[] iArr = new int[768];
                int[] iArr2 = new int[256];
                createSized.copyTo(iArr2);
                int i = 0;
                int i2 = 0;
                int i3 = 0;
                while (i2 < 256) {
                    int i4 = i3 + 1;
                    iArr[i3] = iArr2[i2];
                    i2++;
                    i3 = i4;
                }
                createSized2.copyTo(iArr2);
                int i5 = 0;
                while (i5 < 256) {
                    int i6 = i3 + 1;
                    iArr[i3] = iArr2[i5];
                    i5++;
                    i3 = i6;
                }
                createSized3.copyTo(iArr2);
                while (i < 256) {
                    int i7 = i3 + 1;
                    iArr[i3] = iArr2[i];
                    i++;
                    i3 = i7;
                }
                createSized.destroy();
                createSized2.destroy();
                createSized3.destroy();
                return iArr;
            }
            Allocation createSized4 = Allocation.createSized(renderScript, Element.I32(renderScript), 256);
            scriptC_histogram_compute.bind_histogram(createSized4);
            scriptC_histogram_compute.invoke_init_histogram();
            int i8 = C036023.f21xe5e4bd0[histogramType.ordinal()];
            if (i8 == 1) {
                scriptC_histogram_compute.forEach_histogram_compute_by_luminance(allocation);
            } else if (i8 == 2) {
                scriptC_histogram_compute.forEach_histogram_compute_by_value(allocation);
            } else if (i8 == 3) {
                scriptC_histogram_compute.forEach_histogram_compute_by_intensity(allocation);
            } else if (i8 == 4) {
                scriptC_histogram_compute.forEach_histogram_compute_by_lightness(allocation);
            }
            int[] iArr3 = new int[256];
            createSized4.copyTo(iArr3);
            createSized4.destroy();
            return iArr3;
        }

        /* access modifiers changed from: protected */
        public RefreshPreviewBitmapTaskResult doInBackground(Void... voidArr) {
            Preview preview = (Preview) this.previewReference.get();
            if (preview == null) {
                return null;
            }
            ScriptC_histogram_compute scriptC_histogram_compute = (ScriptC_histogram_compute) this.histogramScriptReference.get();
            if (scriptC_histogram_compute == null) {
                return null;
            }
            Bitmap bitmap = (Bitmap) this.preview_bitmapReference.get();
            if (bitmap == null) {
                return null;
            }
            Bitmap bitmap2 = (Bitmap) this.zebra_stripes_bitmap_bufferReference.get();
            Bitmap bitmap3 = (Bitmap) this.focus_peaking_bitmap_bufferReference.get();
            Activity activity = (Activity) preview.getContext();
            if (activity == null || activity.isFinishing()) {
                return null;
            }
            RefreshPreviewBitmapTaskResult refreshPreviewBitmapTaskResult = new RefreshPreviewBitmapTaskResult();
            try {
                ((TextureView) preview.cameraSurface).getBitmap(bitmap);
                Allocation createFromBitmap = Allocation.createFromBitmap(preview.f19rs, bitmap);
                if (this.update_histogram) {
                    refreshPreviewBitmapTaskResult.new_histogram = computeHistogram(createFromBitmap, preview.f19rs, scriptC_histogram_compute, preview.histogram_type);
                }
                if (preview.want_zebra_stripes && bitmap2 != null) {
                    Allocation createFromBitmap2 = Allocation.createFromBitmap(preview.f19rs, bitmap2);
                    scriptC_histogram_compute.set_zebra_stripes_threshold(preview.zebra_stripes_threshold);
                    scriptC_histogram_compute.set_zebra_stripes_width(bitmap2.getWidth() / 20);
                    scriptC_histogram_compute.forEach_generate_zebra_stripes(createFromBitmap, createFromBitmap2);
                    createFromBitmap2.copyTo(bitmap2);
                    createFromBitmap2.destroy();
                    int access$6200 = preview.getDisplayRotationDegrees();
                    Matrix matrix = new Matrix();
                    matrix.postRotate((float) (-access$6200));
                    refreshPreviewBitmapTaskResult.new_zebra_stripes_bitmap = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth(), bitmap2.getHeight(), matrix, false);
                }
                if (preview.want_focus_peaking && bitmap3 != null) {
                    Allocation createFromBitmap3 = Allocation.createFromBitmap(preview.f19rs, bitmap3);
                    scriptC_histogram_compute.set_bitmap(createFromBitmap);
                    scriptC_histogram_compute.forEach_generate_focus_peaking(createFromBitmap, createFromBitmap3);
                    Allocation createTyped = Allocation.createTyped(preview.f19rs, Type.createXY(preview.f19rs, Element.RGBA_8888(preview.f19rs), bitmap3.getWidth(), bitmap3.getHeight()));
                    scriptC_histogram_compute.set_bitmap(createFromBitmap3);
                    scriptC_histogram_compute.forEach_generate_focus_peaking_filtered(createFromBitmap3, createTyped);
                    createFromBitmap3.destroy();
                    createTyped.copyTo(bitmap3);
                    createTyped.destroy();
                    int access$62002 = preview.getDisplayRotationDegrees();
                    Matrix matrix2 = new Matrix();
                    matrix2.postRotate((float) (-access$62002));
                    refreshPreviewBitmapTaskResult.new_focus_peaking_bitmap = Bitmap.createBitmap(bitmap3, 0, 0, bitmap3.getWidth(), bitmap3.getHeight(), matrix2, false);
                }
                createFromBitmap.destroy();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RSInvalidStateException e2) {
                e2.printStackTrace();
            }
            return refreshPreviewBitmapTaskResult;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(RefreshPreviewBitmapTaskResult refreshPreviewBitmapTaskResult) {
            Preview preview = (Preview) this.previewReference.get();
            if (preview != null) {
                Activity activity = (Activity) preview.getContext();
                if (activity != null && !activity.isFinishing() && refreshPreviewBitmapTaskResult != null) {
                    if (refreshPreviewBitmapTaskResult.new_histogram != null) {
                        preview.histogram = refreshPreviewBitmapTaskResult.new_histogram;
                    }
                    if (preview.zebra_stripes_bitmap != null) {
                        preview.zebra_stripes_bitmap.recycle();
                    }
                    preview.zebra_stripes_bitmap = refreshPreviewBitmapTaskResult.new_zebra_stripes_bitmap;
                    if (preview.focus_peaking_bitmap != null) {
                        preview.focus_peaking_bitmap.recycle();
                    }
                    preview.focus_peaking_bitmap = refreshPreviewBitmapTaskResult.new_focus_peaking_bitmap;
                    preview.refreshPreviewBitmapTask = null;
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onCancelled() {
            Preview preview = (Preview) this.previewReference.get();
            if (preview != null) {
                preview.refreshPreviewBitmapTask = null;
            }
        }
    }

    private static class RefreshPreviewBitmapTaskResult {
        Bitmap new_focus_peaking_bitmap;
        int[] new_histogram;
        Bitmap new_zebra_stripes_bitmap;

        private RefreshPreviewBitmapTaskResult() {
        }
    }

    private class RotatedTextView extends View {
        private final Rect bounds = new Rect();
        private String[] lines;
        private int offset_y;
        private final Paint paint = new Paint(1);
        private final RectF rect = new RectF();
        private final Rect sub_bounds = new Rect();

        RotatedTextView(String str, int i, Context context) {
            super(context);
            this.lines = str.split("\n");
            this.offset_y = i;
        }

        /* access modifiers changed from: 0000 */
        public void setText(String str) {
            this.lines = str.split("\n");
        }

        /* access modifiers changed from: 0000 */
        public void setOffsetY(int i) {
            this.offset_y = i;
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            String[] strArr;
            float f = Preview.this.getResources().getDisplayMetrics().density;
            float f2 = (14.0f * f) + 0.5f;
            this.paint.setTextSize(f2);
            this.paint.setShadowLayer(1.0f, 0.0f, 1.0f, ViewCompat.MEASURED_STATE_MASK);
            boolean z = true;
            for (String str : this.lines) {
                this.paint.getTextBounds(str, 0, str.length(), this.sub_bounds);
                if (z) {
                    this.bounds.set(this.sub_bounds);
                    z = false;
                } else {
                    this.bounds.top = Math.min(this.sub_bounds.top, this.bounds.top);
                    this.bounds.bottom = Math.max(this.sub_bounds.bottom, this.bounds.bottom);
                    this.bounds.left = Math.min(this.sub_bounds.left, this.bounds.left);
                    this.bounds.right = Math.max(this.sub_bounds.right, this.bounds.right);
                }
            }
            this.paint.getTextBounds("Ap", 0, 2, this.sub_bounds);
            this.bounds.top = this.sub_bounds.top;
            this.bounds.bottom = this.sub_bounds.bottom;
            int i = this.bounds.bottom - this.bounds.top;
            this.bounds.bottom += ((this.lines.length - 1) * i) / 2;
            this.bounds.top -= ((this.lines.length - 1) * i) / 2;
            int i2 = (int) f2;
            canvas.save();
            canvas.rotate((float) Preview.this.ui_rotation, ((float) canvas.getWidth()) / 2.0f, ((float) canvas.getHeight()) / 2.0f);
            float f3 = (float) i2;
            this.rect.left = (((((float) canvas.getWidth()) / 2.0f) - (((float) this.bounds.width()) / 2.0f)) + ((float) this.bounds.left)) - f3;
            this.rect.top = (((((float) canvas.getHeight()) / 2.0f) + ((float) this.bounds.top)) - f3) + ((float) this.offset_y);
            this.rect.right = ((((float) canvas.getWidth()) / 2.0f) - (((float) this.bounds.width()) / 2.0f)) + ((float) this.bounds.right) + f3;
            this.rect.bottom = (((float) canvas.getHeight()) / 2.0f) + ((float) this.bounds.bottom) + f3 + ((float) this.offset_y);
            this.paint.setStyle(Style.FILL);
            this.paint.setColor(Color.rgb(50, 50, 50));
            float f4 = (f * 24.0f) + 0.5f;
            canvas.drawRoundRect(this.rect, f4, f4, this.paint);
            this.paint.setColor(-1);
            int height = (canvas.getHeight() / 2) + this.offset_y;
            String[] strArr2 = this.lines;
            int length = height - (((strArr2.length - 1) * i) / 2);
            for (String drawText : strArr2) {
                canvas.drawText(drawText, (((float) canvas.getWidth()) / 2.0f) - (((float) this.bounds.width()) / 2.0f), (float) length, this.paint);
                length += i;
            }
            canvas.restore();
        }
    }

    private class ScaleListener extends SimpleOnScaleGestureListener {
        private ScaleListener() {
        }

        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            if (Preview.this.camera_controller != null && Preview.this.has_zoom) {
                Preview.this.scaleZoom(scaleGestureDetector.getScaleFactor());
            }
            return true;
        }
    }

    private static class VideoFileInfo {
        /* access modifiers changed from: private */
        public final String video_filename;
        /* access modifiers changed from: private */
        public final int video_method;
        /* access modifiers changed from: private */
        public final ParcelFileDescriptor video_pfd_saf;
        /* access modifiers changed from: private */
        public final Uri video_uri;

        VideoFileInfo() {
            this.video_method = 0;
            this.video_uri = null;
            this.video_filename = null;
            this.video_pfd_saf = null;
        }

        VideoFileInfo(int i, Uri uri, String str, ParcelFileDescriptor parcelFileDescriptor) {
            this.video_method = i;
            this.video_uri = uri;
            this.video_filename = str;
            this.video_pfd_saf = parcelFileDescriptor;
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
    }

    public Preview(ApplicationInterface applicationInterface2, ViewGroup viewGroup) {
        boolean z = true;
        this.app_is_paused = true;
        this.camera_open_state = CameraOpenState.CAMERAOPENSTATE_CLOSED;
        this.has_permissions = true;
        this.videoFileInfo = new VideoFileInfo();
        this.phase = 0;
        this.takePictureTimer = new Timer();
        this.beepTimer = new Timer();
        this.flashVideoTimer = new Timer();
        this.battery_ifilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        this.batteryCheckVideoTimer = new Timer();
        this.current_flash_index = -1;
        this.current_focus_index = -1;
        this.current_size_index = -1;
        this.capture_rate_factor = 1.0f;
        this.video_quality_handler = new VideoQualityHandler();
        this.flash_toast = new ToastBoxer();
        this.focus_toast = new ToastBoxer();
        this.take_photo_toast = new ToastBoxer();
        this.pause_video_toast = new ToastBoxer();
        this.face_rect = new RectF();
        this.focus_complete_time = -1;
        this.focus_started_time = -1;
        this.focus_success = 3;
        this.set_flash_value_after_autofocus = BuildConfig.FLAVOR;
        this.successfully_focused_time = -1;
        this.gravity = new float[3];
        this.geomagnetic = new float[3];
        this.deviceRotation = new float[9];
        this.cameraRotation = new float[9];
        this.deviceInclination = new float[9];
        this.geo_direction = new float[3];
        this.new_geo_direction = new float[3];
        this.decimal_format_1dp = new DecimalFormat("#.#");
        this.decimal_format_2dp = new DecimalFormat("#.##");
        this.reset_continuous_focus_handler = new Handler();
        this.fake_toast_handler = new Handler();
        this.active_fake_toast = null;
        this.applicationInterface = applicationInterface2;
        Activity activity = (Activity) getContext();
        if (!(activity.getIntent() == null || activity.getIntent().getExtras() == null)) {
            this.is_test = activity.getIntent().getExtras().getBoolean("test_project");
        }
        if (VERSION.SDK_INT < 21 || !applicationInterface2.useCamera2()) {
            z = false;
        }
        this.using_android_l = z;
        if (this.using_android_l) {
            this.cameraSurface = new MyTextureView(getContext(), this);
            this.canvasView = new CanvasView(getContext(), this);
            this.camera_controller_manager = new CameraControllerManager2(getContext());
        } else {
            this.cameraSurface = new MySurfaceView(getContext(), this);
            this.camera_controller_manager = new CameraControllerManager1();
        }
        this.gestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener());
        this.gestureDetector.setOnDoubleTapListener(new DoubleTapListener());
        this.scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        this.accessibility_manager = (AccessibilityManager) activity.getSystemService("accessibility");
        viewGroup.addView(this.cameraSurface.getView());
        CanvasView canvasView2 = this.canvasView;
        if (canvasView2 != null) {
            viewGroup.addView(canvasView2);
        }
    }

    /* access modifiers changed from: private */
    public Resources getResources() {
        return this.cameraSurface.getView().getResources();
    }

    public View getView() {
        return this.cameraSurface.getView();
    }

    private void calculateCameraToPreviewMatrix() {
        if (this.camera_controller != null) {
            this.camera_to_preview_matrix.reset();
            float f = -1.0f;
            if (!this.using_android_l) {
                boolean isFrontFacing = this.camera_controller.isFrontFacing();
                Matrix matrix = this.camera_to_preview_matrix;
                if (!isFrontFacing) {
                    f = 1.0f;
                }
                matrix.setScale(f, 1.0f);
                this.camera_to_preview_matrix.postRotate((float) this.camera_controller.getDisplayOrientation());
            } else {
                boolean isFrontFacing2 = this.camera_controller.isFrontFacing();
                Matrix matrix2 = this.camera_to_preview_matrix;
                if (!isFrontFacing2) {
                    f = 1.0f;
                }
                matrix2.setScale(1.0f, f);
                this.camera_to_preview_matrix.postRotate((float) (((this.camera_controller.getCameraOrientation() - getDisplayRotationDegrees()) + 360) % 360));
            }
            this.camera_to_preview_matrix.postScale(((float) this.cameraSurface.getView().getWidth()) / 2000.0f, ((float) this.cameraSurface.getView().getHeight()) / 2000.0f);
            this.camera_to_preview_matrix.postTranslate(((float) this.cameraSurface.getView().getWidth()) / 2.0f, ((float) this.cameraSurface.getView().getHeight()) / 2.0f);
        }
    }

    private void calculatePreviewToCameraMatrix() {
        if (this.camera_controller != null) {
            calculateCameraToPreviewMatrix();
            this.camera_to_preview_matrix.invert(this.preview_to_camera_matrix);
        }
    }

    /* access modifiers changed from: private */
    public Matrix getCameraToPreviewMatrix() {
        calculateCameraToPreviewMatrix();
        return this.camera_to_preview_matrix;
    }

    private ArrayList<Area> getAreas(float f, float f2) {
        float[] fArr = {f, f2};
        calculatePreviewToCameraMatrix();
        this.preview_to_camera_matrix.mapPoints(fArr);
        float f3 = fArr[0];
        float f4 = fArr[1];
        Rect rect = new Rect();
        int i = (int) f3;
        rect.left = i - 50;
        rect.right = i + 50;
        int i2 = (int) f4;
        rect.top = i2 - 50;
        rect.bottom = i2 + 50;
        if (rect.left < -1000) {
            rect.left = NotificationManagerCompat.IMPORTANCE_UNSPECIFIED;
            rect.right = rect.left + 100;
        } else if (rect.right > 1000) {
            rect.right = 1000;
            rect.left = rect.right - 100;
        }
        if (rect.top < -1000) {
            rect.top = NotificationManagerCompat.IMPORTANCE_UNSPECIFIED;
            rect.bottom = rect.top + 100;
        } else if (rect.bottom > 1000) {
            rect.bottom = 1000;
            rect.top = rect.bottom - 100;
        }
        ArrayList<Area> arrayList = new ArrayList<>();
        arrayList.add(new Area(rect, 1000));
        return arrayList;
    }

    public boolean touchEvent(MotionEvent motionEvent) {
        if (this.gestureDetector.onTouchEvent(motionEvent)) {
            return true;
        }
        this.scaleGestureDetector.onTouchEvent(motionEvent);
        if (this.camera_controller == null) {
            return true;
        }
        this.applicationInterface.touchEvent(motionEvent);
        if (motionEvent.getPointerCount() != 1) {
            this.touch_was_multitouch = true;
            return true;
        } else if (motionEvent.getAction() != 1) {
            if (motionEvent.getAction() == 0 && motionEvent.getPointerCount() == 1) {
                this.touch_was_multitouch = false;
                if (motionEvent.getAction() == 0) {
                    this.touch_orig_x = motionEvent.getX();
                    this.touch_orig_y = motionEvent.getY();
                }
            }
            return true;
        } else if (this.touch_was_multitouch) {
            return true;
        } else {
            if (!this.is_video && isTakingPhotoOrOnTimer()) {
                return true;
            }
            float x = motionEvent.getX();
            float f = x - this.touch_orig_x;
            float y = motionEvent.getY() - this.touch_orig_y;
            float f2 = (f * f) + (y * y);
            float f3 = (getResources().getDisplayMetrics().density * 31.0f) + 0.5f;
            if (f2 > f3 * f3) {
                return true;
            }
            if (!this.is_video) {
                startCameraPreview();
            }
            cancelAutoFocus();
            if (this.camera_controller != null && !this.using_face_detection) {
                this.has_focus_area = false;
                if (this.camera_controller.setFocusAndMeteringArea(getAreas(motionEvent.getX(), motionEvent.getY()))) {
                    this.has_focus_area = true;
                    this.focus_screen_x = (int) motionEvent.getX();
                    this.focus_screen_y = (int) motionEvent.getY();
                }
            }
            if (this.is_video || !this.applicationInterface.getTouchCapturePref()) {
                tryAutoFocus(false, true);
                return true;
            }
            takePicturePressed(false, false);
            return true;
        }
    }

    public boolean onDoubleTap() {
        if (!this.is_video && this.applicationInterface.getDoubleTapCapturePref()) {
            takePicturePressed(false, false);
        }
        return true;
    }

    public void clearFocusAreas() {
        CameraController cameraController = this.camera_controller;
        if (cameraController != null) {
            cameraController.clearFocusAndMetering();
            this.has_focus_area = false;
            this.focus_success = 3;
            this.successfully_focused = false;
        }
    }

    public void getMeasureSpec(int[] iArr, int i, int i2) {
        if (!hasAspectRatio()) {
            iArr[0] = i;
            iArr[1] = i2;
            return;
        }
        double aspectRatio = getAspectRatio();
        int paddingLeft = this.cameraSurface.getView().getPaddingLeft() + this.cameraSurface.getView().getPaddingRight();
        int paddingTop = this.cameraSurface.getView().getPaddingTop() + this.cameraSurface.getView().getPaddingBottom();
        int size = MeasureSpec.getSize(i) - paddingLeft;
        int size2 = MeasureSpec.getSize(i2) - paddingTop;
        boolean z = size > size2;
        int i3 = z ? size : size2;
        if (z) {
            size = size2;
        }
        double d = (double) i3;
        double d2 = (double) size;
        Double.isNaN(d2);
        double d3 = d2 * aspectRatio;
        if (d > d3) {
            i3 = (int) d3;
        } else {
            Double.isNaN(d);
            size = (int) (d / aspectRatio);
        }
        if (z) {
            int i4 = i3;
            i3 = size;
            size = i4;
        }
        int i5 = i3 + paddingTop;
        iArr[0] = MeasureSpec.makeMeasureSpec(size + paddingLeft, 1073741824);
        iArr[1] = MeasureSpec.makeMeasureSpec(i5, 1073741824);
    }

    private void mySurfaceCreated() {
        this.has_surface = true;
        openCamera();
    }

    private void mySurfaceDestroyed() {
        this.has_surface = false;
        closeCamera(false, null);
    }

    private void mySurfaceChanged() {
        if (this.camera_controller == null) {
        }
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mySurfaceCreated();
        this.cameraSurface.getView().setWillNotDraw(false);
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mySurfaceDestroyed();
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        if (surfaceHolder.getSurface() != null) {
            mySurfaceChanged();
        }
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        this.set_textureview_size = true;
        this.textureview_w = i;
        this.textureview_h = i2;
        mySurfaceCreated();
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        this.set_textureview_size = false;
        this.textureview_w = 0;
        this.textureview_h = 0;
        mySurfaceDestroyed();
        return true;
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        this.set_textureview_size = true;
        this.textureview_w = i;
        this.textureview_h = i2;
        mySurfaceChanged();
        configureTransform();
        recreatePreviewBitmap();
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        refreshPreviewBitmap();
    }

    private void configureTransform() {
        if (this.camera_controller != null && this.set_preview_size && this.set_textureview_size) {
            int displayRotation = getDisplayRotation();
            Matrix matrix = new Matrix();
            RectF rectF = new RectF(0.0f, 0.0f, (float) this.textureview_w, (float) this.textureview_h);
            RectF rectF2 = new RectF(0.0f, 0.0f, (float) this.preview_h, (float) this.preview_w);
            float centerX = rectF.centerX();
            float centerY = rectF.centerY();
            if (1 == displayRotation || 3 == displayRotation) {
                rectF2.offset(centerX - rectF2.centerX(), centerY - rectF2.centerY());
                matrix.setRectToRect(rectF, rectF2, ScaleToFit.FILL);
                float max = Math.max(((float) this.textureview_h) / ((float) this.preview_h), ((float) this.textureview_w) / ((float) this.preview_w));
                matrix.postScale(max, max, centerX, centerY);
                matrix.postRotate((float) ((displayRotation - 2) * 90), centerX, centerY);
            }
            this.cameraSurface.setTransform(matrix);
        }
    }

    public void stopVideo(boolean z) {
        if (this.video_recorder != null) {
            this.applicationInterface.stoppingVideo();
            TimerTask timerTask = this.flashVideoTimerTask;
            if (timerTask != null) {
                timerTask.cancel();
                this.flashVideoTimerTask = null;
            }
            TimerTask timerTask2 = this.batteryCheckVideoTimerTask;
            if (timerTask2 != null) {
                timerTask2.cancel();
                this.batteryCheckVideoTimerTask = null;
            }
            if (!z) {
                this.remaining_restart_video = 0;
            }
            if (this.video_recorder != null) {
                this.video_recorder.setOnErrorListener(null);
                this.video_recorder.setOnInfoListener(null);
                try {
                    this.video_recorder.stop();
                } catch (RuntimeException unused) {
                    if (this.videoFileInfo.video_method == 1) {
                        if (this.videoFileInfo.video_uri != null) {
                            try {
                                DocumentsContract.deleteDocument(getContext().getContentResolver(), this.videoFileInfo.video_uri);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (this.videoFileInfo.video_method == 0 && this.videoFileInfo.video_filename != null) {
                        new File(this.videoFileInfo.video_filename).delete();
                    }
                    this.videoFileInfo = new VideoFileInfo();
                    if (!this.video_start_time_set || System.currentTimeMillis() - this.video_start_time > 2000) {
                        this.applicationInterface.onVideoRecordStopError(getVideoProfile());
                    }
                }
                videoRecordingStopped();
            }
        }
    }

    private void videoRecordingStopped() {
        this.video_recorder.reset();
        this.video_recorder.release();
        this.video_recorder = null;
        this.video_recorder_is_paused = false;
        this.applicationInterface.cameraInOperation(false, true);
        reconnectCamera(false);
        this.applicationInterface.stoppedVideo(this.videoFileInfo.video_method, this.videoFileInfo.video_uri, this.videoFileInfo.video_filename);
        this.videoFileInfo = new VideoFileInfo();
    }

    /* access modifiers changed from: private */
    public Context getContext() {
        return this.applicationInterface.getContext();
    }

    /* access modifiers changed from: private */
    public void restartVideo(boolean z) {
        String str;
        if (this.video_recorder != null) {
            if (z) {
                this.video_accumulated_time += System.currentTimeMillis() - this.video_start_time;
            } else {
                this.video_accumulated_time = 0;
            }
            stopVideo(true);
            if (z) {
                long videoMaxDurationPref = this.applicationInterface.getVideoMaxDurationPref();
                if (videoMaxDurationPref > 0 && videoMaxDurationPref - this.video_accumulated_time < min_safe_restart_video_time) {
                    z = false;
                }
            }
            if (!z && this.remaining_restart_video <= 0) {
                return;
            }
            if (this.is_video) {
                if (!z) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.remaining_restart_video);
                    sb.append(" ");
                    sb.append(getContext().getResources().getString(C0316R.string.repeats_to_go));
                    str = sb.toString();
                } else {
                    str = null;
                }
                takePicture(z, false, false);
                if (!z) {
                    showToast((ToastBoxer) null, str);
                    this.remaining_restart_video--;
                    return;
                }
                return;
            }
            this.remaining_restart_video = 0;
        }
    }

    private void reconnectCamera(boolean z) {
        CameraController cameraController = this.camera_controller;
        if (cameraController != null) {
            try {
                cameraController.reconnect();
                setPreviewPaused(false);
            } catch (CameraControllerException e) {
                e.printStackTrace();
                this.applicationInterface.onFailedReconnectError();
                closeCamera(false, null);
            }
            try {
                tryAutoFocus(false, false);
            } catch (RuntimeException e2) {
                e2.printStackTrace();
                this.is_preview_started = false;
                if (!z) {
                    this.applicationInterface.onVideoRecordStopError(getVideoProfile());
                }
                this.camera_controller.release();
                this.camera_controller = null;
                this.camera_open_state = CameraOpenState.CAMERAOPENSTATE_CLOSED;
                openCamera();
            }
        }
    }

    private void closeCamera(boolean z, CloseCameraCallback closeCameraCallback) {
        removePendingContinuousFocusReset();
        this.has_focus_area = false;
        this.focus_success = 3;
        this.focus_started_time = -1;
        synchronized (this) {
            this.take_photo_after_autofocus = false;
        }
        this.set_flash_value_after_autofocus = BuildConfig.FLAVOR;
        this.successfully_focused = false;
        this.preview_targetRatio = 0.0d;
        if (this.continuous_focus_move_is_started) {
            this.continuous_focus_move_is_started = false;
            this.applicationInterface.onContinuousFocusMove(false);
        }
        this.applicationInterface.cameraClosed();
        cancelTimer();
        cancelRepeat();
        if (this.camera_controller != null) {
            if (this.video_recorder != null) {
                stopVideo(false);
            }
            updateFocusForVideo();
            if (this.camera_controller != null) {
                pausePreview(false);
                CameraController cameraController = this.camera_controller;
                this.camera_controller = null;
                if (z) {
                    this.camera_open_state = CameraOpenState.CAMERAOPENSTATE_CLOSING;
                    this.close_camera_task = new CloseCameraTask(cameraController, closeCameraCallback);
                    this.close_camera_task.execute(new Void[0]);
                } else {
                    cameraController.stopPreview();
                    cameraController.release();
                    this.camera_open_state = CameraOpenState.CAMERAOPENSTATE_CLOSED;
                }
            }
        } else if (closeCameraCallback != null) {
            closeCameraCallback.onClosed();
        }
        OrientationEventListener orientationEventListener2 = this.orientationEventListener;
        if (orientationEventListener2 != null) {
            orientationEventListener2.disable();
            this.orientationEventListener = null;
        }
    }

    public void cancelTimer() {
        if (isOnTimer()) {
            this.takePictureTimerTask.cancel();
            this.takePictureTimerTask = null;
            TimerTask timerTask = this.beepTimerTask;
            if (timerTask != null) {
                timerTask.cancel();
                this.beepTimerTask = null;
            }
            this.phase = 0;
        }
    }

    public void cancelRepeat() {
        this.remaining_repeat_photos = 0;
    }

    public void pausePreview(boolean z) {
        if (this.camera_controller != null) {
            updateFocusForVideo();
            setPreviewPaused(false);
            if (z) {
                this.camera_controller.stopPreview();
            }
            this.phase = 0;
            this.is_preview_started = false;
        }
    }

    /* access modifiers changed from: private */
    public void openCamera() {
        if (this.camera_open_state != CameraOpenState.CAMERAOPENSTATE_OPENING) {
            if (this.camera_open_state == CameraOpenState.CAMERAOPENSTATE_CLOSING) {
                Log.d(TAG, "tried to open camera while camera is still closing in background thread");
                return;
            }
            this.is_preview_started = false;
            this.set_preview_size = false;
            this.preview_w = 0;
            this.preview_h = 0;
            this.has_focus_area = false;
            this.focus_success = 3;
            this.focus_started_time = -1;
            synchronized (this) {
                this.take_photo_after_autofocus = false;
            }
            this.set_flash_value_after_autofocus = BuildConfig.FLAVOR;
            this.successfully_focused = false;
            this.preview_targetRatio = 0.0d;
            this.scene_modes = null;
            this.camera_controller_supports_zoom = false;
            this.has_zoom = false;
            this.max_zoom_factor = 0;
            this.minimum_focus_distance = 0.0f;
            this.zoom_ratios = null;
            this.faces_detected = null;
            this.supports_face_detection = false;
            this.using_face_detection = false;
            this.supports_video_stabilization = false;
            this.supports_photo_video_recording = false;
            this.can_disable_shutter_sound = false;
            this.tonemap_max_curve_points = 0;
            this.supports_tonemap_curve = false;
            this.color_effects = null;
            this.white_balances = null;
            this.antibanding = null;
            this.edge_modes = null;
            this.noise_reduction_modes = null;
            this.isos = null;
            this.supports_white_balance_temperature = false;
            this.min_temperature = 0;
            this.max_temperature = 0;
            this.supports_iso_range = false;
            this.min_iso = 0;
            this.max_iso = 0;
            this.supports_exposure_time = false;
            this.min_exposure_time = 0;
            this.max_exposure_time = 0;
            this.exposures = null;
            this.min_exposure = 0;
            this.max_exposure = 0;
            this.exposure_step = 0.0f;
            this.supports_expo_bracketing = false;
            this.max_expo_bracketing_n_images = 0;
            this.supports_focus_bracketing = false;
            this.supports_burst = false;
            this.supports_raw = false;
            this.view_angle_x = 55.0f;
            this.view_angle_y = 43.0f;
            this.sizes = null;
            this.current_size_index = -1;
            this.has_capture_rate_factor = false;
            this.capture_rate_factor = 1.0f;
            this.video_high_speed = false;
            boolean z = true;
            this.supports_video = true;
            this.supports_video_high_speed = false;
            this.video_quality_handler.resetCurrentQuality();
            this.supported_flash_values = null;
            this.current_flash_index = -1;
            this.supported_focus_values = null;
            this.current_focus_index = -1;
            this.max_num_focus_areas = 0;
            this.applicationInterface.cameraInOperation(false, false);
            if (this.is_video) {
                this.applicationInterface.cameraInOperation(false, true);
            }
            if (this.has_surface && !this.app_is_paused) {
                if (VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(getContext(), "android.permission.CAMERA") != 0) {
                        this.has_permissions = false;
                        this.applicationInterface.requestCameraPermission();
                        return;
                    } else if (this.applicationInterface.needsStoragePermission() && ContextCompat.checkSelfPermission(getContext(), "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                        this.has_permissions = false;
                        this.applicationInterface.requestStoragePermission();
                        return;
                    }
                }
                this.has_permissions = true;
                this.camera_open_state = CameraOpenState.CAMERAOPENSTATE_OPENING;
                final int cameraIdPref = this.applicationInterface.getCameraIdPref();
                if (cameraIdPref < 0 || cameraIdPref >= this.camera_controller_manager.getNumberOfCameras()) {
                    this.applicationInterface.setCameraIdPref(0);
                    cameraIdPref = 0;
                }
                if (VERSION.SDK_INT < 23) {
                    z = false;
                }
                if (z) {
                    this.open_camera_task = new AsyncTask<Void, Void, CameraController>() {
                        private static final String TAG = "Preview/openCamera";

                        /* access modifiers changed from: protected */
                        public CameraController doInBackground(Void... voidArr) {
                            return Preview.this.openCameraCore(cameraIdPref);
                        }

                        /* access modifiers changed from: protected */
                        public void onPostExecute(CameraController cameraController) {
                            Preview.this.camera_controller = cameraController;
                            Preview.this.cameraOpened();
                            Preview.this.camera_open_state = CameraOpenState.CAMERAOPENSTATE_OPENED;
                            Preview.this.open_camera_task = null;
                        }

                        /* access modifiers changed from: protected */
                        public void onCancelled(CameraController cameraController) {
                            if (cameraController != null) {
                                cameraController.release();
                            }
                            Preview.this.camera_open_state = CameraOpenState.CAMERAOPENSTATE_OPENED;
                            Preview.this.open_camera_task = null;
                        }
                    }.execute(new Void[0]);
                } else {
                    this.camera_controller = openCameraCore(cameraIdPref);
                    cameraOpened();
                    this.camera_open_state = CameraOpenState.CAMERAOPENSTATE_OPENED;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public CameraController openCameraCore(int i) {
        try {
            if (!this.test_fail_open_camera) {
                C03552 r0 = new ErrorCallback() {
                    public void onError() {
                        if (Preview.this.camera_controller != null) {
                            Preview.this.camera_controller = null;
                            Preview.this.camera_open_state = CameraOpenState.CAMERAOPENSTATE_CLOSED;
                            Preview.this.applicationInterface.onCameraError();
                        }
                    }
                };
                if (!this.using_android_l) {
                    return new CameraController1(i, r0);
                }
                CameraController2 cameraController2 = new CameraController2(getContext(), i, new ErrorCallback() {
                    public void onError() {
                        Preview.this.applicationInterface.onFailedStartPreview();
                    }
                }, r0);
                if (!this.applicationInterface.useCamera2FakeFlash()) {
                    return cameraController2;
                }
                cameraController2.setUseCamera2FakeFlash(true);
                return cameraController2;
            }
            throw new CameraControllerException();
        } catch (CameraControllerException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void cameraOpened() {
        boolean z;
        if (this.camera_controller != null) {
            Activity activity = (Activity) getContext();
            if (activity.getIntent() == null || activity.getIntent().getExtras() == null) {
                z = false;
            } else {
                Bundle extras = activity.getIntent().getExtras();
                String str = TakePhoto.TAKE_PHOTO;
                z = extras.getBoolean(str);
                activity.getIntent().removeExtra(str);
            }
            setCameraDisplayOrientation();
            if (this.orientationEventListener == null) {
                this.orientationEventListener = new OrientationEventListener(activity) {
                    public void onOrientationChanged(int i) {
                        Preview.this.onOrientationChanged(i);
                    }
                };
                this.orientationEventListener.enable();
            }
            this.cameraSurface.setPreviewDisplay(this.camera_controller);
            setupCamera(z);
            if (this.using_android_l) {
                configureTransform();
            }
        }
    }

    public void retryOpenCamera() {
        if (this.camera_controller == null) {
            openCamera();
        }
    }

    public void reopenCamera() {
        closeCamera(true, new CloseCameraCallback() {
            public void onClosed() {
                Preview.this.openCamera();
            }
        });
    }

    public boolean hasPermissions() {
        return this.has_permissions;
    }

    public boolean isOpeningCamera() {
        return this.camera_open_state == CameraOpenState.CAMERAOPENSTATE_OPENING;
    }

    public boolean openCameraAttempted() {
        return this.camera_open_state == CameraOpenState.CAMERAOPENSTATE_OPENED;
    }

    public boolean openCameraFailed() {
        return this.camera_open_state == CameraOpenState.CAMERAOPENSTATE_OPENED && this.camera_controller == null;
    }

    public void setupCamera(boolean z) {
        if (this.camera_controller != null) {
            boolean z2 = !z && this.applicationInterface.getStartupFocusPref();
            updateFocusForVideo();
            try {
                setupCameraParameters();
                boolean isVideoPref = this.applicationInterface.isVideoPref();
                if (isVideoPref && !this.supports_video) {
                    isVideoPref = false;
                }
                if (isVideoPref != this.is_video) {
                    switchVideo(true, false);
                }
                updateFlashForVideo();
                if (z && this.is_video) {
                    switchVideo(true, true);
                }
                if (this.is_video) {
                    boolean z3 = this.supports_tonemap_curve && this.applicationInterface.useVideoLogProfile();
                    this.camera_controller.setLogProfile(z3, z3 ? this.applicationInterface.getVideoLogProfileStrength() : 0.0f);
                }
                this.camera_controller.setVideoHighSpeed(this.is_video && this.video_high_speed);
                if (z2 && this.using_android_l && this.camera_controller.supportsAutoFocus()) {
                    this.set_flash_value_after_autofocus = BuildConfig.FLAVOR;
                    String flashValue = this.camera_controller.getFlashValue();
                    if (flashValue.length() > 0) {
                        String str = "flash_off";
                        if (!flashValue.equals(str) && !flashValue.equals("flash_torch")) {
                            this.set_flash_value_after_autofocus = flashValue;
                            this.camera_controller.setFlashValue(str);
                        }
                    }
                }
                if (!this.supports_raw || this.applicationInterface.getRawPref() == RawPref.RAWPREF_JPEG_ONLY) {
                    this.camera_controller.setRaw(false, 0);
                } else {
                    this.camera_controller.setRaw(true, this.applicationInterface.getMaxRawImages());
                }
                setupBurstMode();
                if (this.camera_controller.isBurstOrExpo()) {
                    Size currentPictureSize = getCurrentPictureSize();
                    if (currentPictureSize != null && !currentPictureSize.supports_burst) {
                        Size size = null;
                        for (int i = 0; i < this.sizes.size(); i++) {
                            Size size2 = (Size) this.sizes.get(i);
                            if (size2.supports_burst && size2.width * size2.height <= currentPictureSize.width * currentPictureSize.height && (size == null || size2.width * size2.height > size.width * size.height)) {
                                this.current_size_index = i;
                                size = size2;
                            }
                        }
                        if (size == null) {
                            String str2 = TAG;
                            Log.e(str2, "can't find burst-supporting picture size smaller than the current picture size");
                            for (int i2 = 0; i2 < this.sizes.size(); i2++) {
                                Size size3 = (Size) this.sizes.get(i2);
                                if (size3.supports_burst && (size == null || size3.width * size3.height > size.width * size.height)) {
                                    this.current_size_index = i2;
                                    size = size3;
                                }
                            }
                            if (size == null) {
                                Log.e(str2, "can't find burst-supporting picture size");
                            }
                        }
                    }
                }
                this.camera_controller.setOptimiseAEForDRO(this.applicationInterface.getOptimiseAEForDROPref());
                setPreviewSize();
                startCameraPreview();
                if (this.has_zoom && this.applicationInterface.getZoomPref() != 0) {
                    zoomTo(this.applicationInterface.getZoomPref());
                } else if (this.camera_controller_supports_zoom && !this.has_zoom) {
                    this.camera_controller.setZoom(0);
                }
                this.applicationInterface.cameraSetup();
                if (z) {
                    String currentFocusValue = getCurrentFocusValue();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            Preview.this.takePicture(false, false, false);
                        }
                    }, (long) ((currentFocusValue == null || !currentFocusValue.equals("focus_mode_continuous_picture")) ? 500 : 1500));
                }
                if (z2) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            Preview.this.tryAutoFocus(true, false);
                        }
                    }, 500);
                }
            } catch (CameraControllerException e) {
                e.printStackTrace();
                this.applicationInterface.onCameraError();
                closeCamera(false, null);
            }
        }
    }

    public void setupBurstMode() {
        if (this.supports_expo_bracketing && this.applicationInterface.isExpoBracketingPref()) {
            this.camera_controller.setBurstType(BurstType.BURSTTYPE_EXPO);
            this.camera_controller.setExpoBracketingNImages(this.applicationInterface.getExpoBracketingNImagesPref());
            this.camera_controller.setExpoBracketingStops(this.applicationInterface.getExpoBracketingStopsPref());
        } else if (this.supports_focus_bracketing && this.applicationInterface.isFocusBracketingPref()) {
            this.camera_controller.setBurstType(BurstType.BURSTTYPE_FOCUS);
            this.camera_controller.setFocusBracketingNImages(this.applicationInterface.getFocusBracketingNImagesPref());
            this.camera_controller.setFocusBracketingAddInfinity(this.applicationInterface.getFocusBracketingAddInfinityPref());
        } else if (!this.supports_burst || !this.applicationInterface.isCameraBurstPref()) {
            this.camera_controller.setBurstType(BurstType.BURSTTYPE_NONE);
        } else {
            boolean z = false;
            if (!this.applicationInterface.getBurstForNoiseReduction()) {
                this.camera_controller.setBurstType(BurstType.BURSTTYPE_NORMAL);
                this.camera_controller.setBurstForNoiseReduction(false, false);
                this.camera_controller.setBurstNImages(this.applicationInterface.getBurstNImages());
            } else if (this.supports_exposure_time) {
                NRModePref nRModePref = this.applicationInterface.getNRModePref();
                this.camera_controller.setBurstType(BurstType.BURSTTYPE_NORMAL);
                CameraController cameraController = this.camera_controller;
                if (nRModePref == NRModePref.NRMODE_LOW_LIGHT) {
                    z = true;
                }
                cameraController.setBurstForNoiseReduction(true, z);
            } else {
                this.camera_controller.setBurstType(BurstType.BURSTTYPE_NONE);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:105:0x02a9  */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x02e1  */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x030f  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x033a  */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x0394  */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x03c3  */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x03cf  */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x03da  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x04fb  */
    /* JADX WARNING: Removed duplicated region for block: B:216:0x0533  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x054a  */
    /* JADX WARNING: Removed duplicated region for block: B:223:0x054c  */
    /* JADX WARNING: Removed duplicated region for block: B:228:0x056d  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x03b9 A[EDGE_INSN: B:246:0x03b9->B:156:0x03b9 ?: BREAK  
    EDGE_INSN: B:246:0x03b9->B:156:0x03b9 ?: BREAK  
    EDGE_INSN: B:246:0x03b9->B:156:0x03b9 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x03b9 A[EDGE_INSN: B:246:0x03b9->B:156:0x03b9 ?: BREAK  
    EDGE_INSN: B:246:0x03b9->B:156:0x03b9 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0203  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0246  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x025e A[LOOP:0: B:88:0x025a->B:90:0x025e, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0279  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setupCameraParameters() throws net.sourceforge.opencamera.cameracontroller.CameraControllerException {
        /*
            r14 = this;
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            java.lang.String r0 = r0.getSceneModePref()
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r14.camera_controller
            net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues r0 = r1.setSceneMode(r0)
            if (r0 == 0) goto L_0x001a
            java.util.List<java.lang.String> r1 = r0.values
            r14.scene_modes = r1
            net.sourceforge.opencamera.preview.ApplicationInterface r1 = r14.applicationInterface
            java.lang.String r0 = r0.selected_value
            r1.setSceneModePref(r0)
            goto L_0x001f
        L_0x001a:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            r0.clearSceneModePref()
        L_0x001f:
            net.sourceforge.opencamera.cameracontroller.CameraController r0 = r14.camera_controller
            net.sourceforge.opencamera.cameracontroller.CameraController$CameraFeatures r0 = r0.getCameraFeatures()
            boolean r1 = r0.is_zoom_supported
            r14.camera_controller_supports_zoom = r1
            boolean r1 = r0.is_zoom_supported
            r2 = 1
            r3 = 0
            if (r1 == 0) goto L_0x0039
            net.sourceforge.opencamera.preview.ApplicationInterface r1 = r14.applicationInterface
            boolean r1 = r1.allowZoom()
            if (r1 == 0) goto L_0x0039
            r1 = 1
            goto L_0x003a
        L_0x0039:
            r1 = 0
        L_0x003a:
            r14.has_zoom = r1
            boolean r1 = r14.has_zoom
            r4 = 0
            if (r1 == 0) goto L_0x004a
            int r1 = r0.max_zoom
            r14.max_zoom_factor = r1
            java.util.List<java.lang.Integer> r1 = r0.zoom_ratios
            r14.zoom_ratios = r1
            goto L_0x004e
        L_0x004a:
            r14.max_zoom_factor = r3
            r14.zoom_ratios = r4
        L_0x004e:
            float r1 = r0.minimum_focus_distance
            r14.minimum_focus_distance = r1
            boolean r1 = r0.supports_face_detection
            r14.supports_face_detection = r1
            java.util.List<net.sourceforge.opencamera.cameracontroller.CameraController$Size> r1 = r0.picture_sizes
            r14.sizes = r1
            java.util.List<java.lang.String> r1 = r0.supported_flash_values
            r14.supported_flash_values = r1
            java.util.List<java.lang.String> r1 = r0.supported_focus_values
            r14.supported_focus_values = r1
            int r1 = r0.max_num_focus_areas
            r14.max_num_focus_areas = r1
            boolean r1 = r0.is_exposure_lock_supported
            r14.is_exposure_lock_supported = r1
            boolean r1 = r0.is_white_balance_lock_supported
            r14.is_white_balance_lock_supported = r1
            boolean r1 = r0.is_video_stabilization_supported
            r14.supports_video_stabilization = r1
            boolean r1 = r0.is_photo_video_recording_supported
            r14.supports_photo_video_recording = r1
            boolean r1 = r0.can_disable_shutter_sound
            r14.can_disable_shutter_sound = r1
            int r1 = r0.tonemap_max_curve_points
            r14.tonemap_max_curve_points = r1
            boolean r1 = r0.supports_tonemap_curve
            r14.supports_tonemap_curve = r1
            boolean r1 = r0.supports_white_balance_temperature
            r14.supports_white_balance_temperature = r1
            int r1 = r0.min_temperature
            r14.min_temperature = r1
            int r1 = r0.max_temperature
            r14.max_temperature = r1
            boolean r1 = r0.supports_iso_range
            r14.supports_iso_range = r1
            int r1 = r0.min_iso
            r14.min_iso = r1
            int r1 = r0.max_iso
            r14.max_iso = r1
            boolean r1 = r0.supports_exposure_time
            r14.supports_exposure_time = r1
            long r5 = r0.min_exposure_time
            r14.min_exposure_time = r5
            long r5 = r0.max_exposure_time
            r14.max_exposure_time = r5
            int r1 = r0.min_exposure
            r14.min_exposure = r1
            int r1 = r0.max_exposure
            r14.max_exposure = r1
            float r1 = r0.exposure_step
            r14.exposure_step = r1
            boolean r1 = r0.supports_expo_bracketing
            r14.supports_expo_bracketing = r1
            int r1 = r0.max_expo_bracketing_n_images
            r14.max_expo_bracketing_n_images = r1
            boolean r1 = r0.supports_focus_bracketing
            r14.supports_focus_bracketing = r1
            boolean r1 = r0.supports_burst
            r14.supports_burst = r1
            boolean r1 = r0.supports_raw
            r14.supports_raw = r1
            float r1 = r0.view_angle_x
            r14.view_angle_x = r1
            float r1 = r0.view_angle_y
            r14.view_angle_y = r1
            java.util.List<net.sourceforge.opencamera.cameracontroller.CameraController$Size> r1 = r0.video_sizes_high_speed
            if (r1 == 0) goto L_0x00dc
            java.util.List<net.sourceforge.opencamera.cameracontroller.CameraController$Size> r1 = r0.video_sizes_high_speed
            int r1 = r1.size()
            if (r1 <= 0) goto L_0x00dc
            r1 = 1
            goto L_0x00dd
        L_0x00dc:
            r1 = 0
        L_0x00dd:
            r14.supports_video_high_speed = r1
            net.sourceforge.opencamera.preview.VideoQualityHandler r1 = r14.video_quality_handler
            java.util.List<net.sourceforge.opencamera.cameracontroller.CameraController$Size> r5 = r0.video_sizes
            r1.setVideoSizes(r5)
            net.sourceforge.opencamera.preview.VideoQualityHandler r1 = r14.video_quality_handler
            java.util.List<net.sourceforge.opencamera.cameracontroller.CameraController$Size> r5 = r0.video_sizes_high_speed
            r1.setVideoSizesHighSpeed(r5)
            java.util.List<net.sourceforge.opencamera.cameracontroller.CameraController$Size> r0 = r0.preview_sizes
            r14.supported_preview_sizes = r0
            r14.faces_detected = r4
            boolean r0 = r14.supports_face_detection
            if (r0 == 0) goto L_0x0100
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            boolean r0 = r0.getFaceDetectionPref()
            r14.using_face_detection = r0
            goto L_0x0102
        L_0x0100:
            r14.using_face_detection = r3
        L_0x0102:
            boolean r0 = r14.using_face_detection
            if (r0 == 0) goto L_0x0110
            net.sourceforge.opencamera.cameracontroller.CameraController r0 = r14.camera_controller
            net.sourceforge.opencamera.preview.Preview$1MyFaceDetectionListener r1 = new net.sourceforge.opencamera.preview.Preview$1MyFaceDetectionListener
            r1.<init>()
            r0.setFaceDetectionListener(r1)
        L_0x0110:
            boolean r0 = r14.supports_video_stabilization
            if (r0 == 0) goto L_0x011f
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            boolean r0 = r0.getVideoStabilizationPref()
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r14.camera_controller
            r1.setVideoStabilization(r0)
        L_0x011f:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            java.lang.String r0 = r0.getColorEffectPref()
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r14.camera_controller
            net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues r0 = r1.setColorEffect(r0)
            if (r0 == 0) goto L_0x0139
            java.util.List<java.lang.String> r1 = r0.values
            r14.color_effects = r1
            net.sourceforge.opencamera.preview.ApplicationInterface r1 = r14.applicationInterface
            java.lang.String r0 = r0.selected_value
            r1.setColorEffectPref(r0)
            goto L_0x013e
        L_0x0139:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            r0.clearColorEffectPref()
        L_0x013e:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            java.lang.String r0 = r0.getWhiteBalancePref()
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r14.camera_controller
            net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues r0 = r1.setWhiteBalance(r0)
            if (r0 == 0) goto L_0x0171
            java.util.List<java.lang.String> r1 = r0.values
            r14.white_balances = r1
            net.sourceforge.opencamera.preview.ApplicationInterface r1 = r14.applicationInterface
            java.lang.String r5 = r0.selected_value
            r1.setWhiteBalancePref(r5)
            java.lang.String r0 = r0.selected_value
            java.lang.String r1 = "manual"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0176
            boolean r0 = r14.supports_white_balance_temperature
            if (r0 == 0) goto L_0x0176
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            int r0 = r0.getWhiteBalanceTemperaturePref()
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r14.camera_controller
            r1.setWhiteBalanceTemperature(r0)
            goto L_0x0176
        L_0x0171:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            r0.clearWhiteBalancePref()
        L_0x0176:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            java.lang.String r0 = r0.getAntiBandingPref()
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r14.camera_controller
            net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues r0 = r1.setAntiBanding(r0)
            if (r0 == 0) goto L_0x0188
            java.util.List<java.lang.String> r0 = r0.values
            r14.antibanding = r0
        L_0x0188:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            java.lang.String r0 = r0.getEdgeModePref()
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r14.camera_controller
            net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues r0 = r1.setEdgeMode(r0)
            if (r0 == 0) goto L_0x019a
            java.util.List<java.lang.String> r0 = r0.values
            r14.edge_modes = r0
        L_0x019a:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            java.lang.String r0 = r0.getCameraNoiseReductionModePref()
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r14.camera_controller
            net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues r0 = r1.setNoiseReductionMode(r0)
            if (r0 == 0) goto L_0x01ac
            java.util.List<java.lang.String> r0 = r0.values
            r14.noise_reduction_modes = r0
        L_0x01ac:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            java.lang.String r0 = r0.getISOPref()
            boolean r1 = r14.supports_iso_range
            java.lang.String r5 = "auto"
            if (r1 == 0) goto L_0x01e0
            r14.isos = r4
            boolean r1 = r0.equals(r5)
            if (r1 == 0) goto L_0x01c6
            net.sourceforge.opencamera.cameracontroller.CameraController r0 = r14.camera_controller
            r0.setManualISO(r3, r3)
            goto L_0x0200
        L_0x01c6:
            int r1 = r14.parseManualISOValue(r0)
            if (r1 < 0) goto L_0x01d3
            net.sourceforge.opencamera.cameracontroller.CameraController r5 = r14.camera_controller
            r5.setManualISO(r2, r1)
            r1 = 1
            goto L_0x01da
        L_0x01d3:
            net.sourceforge.opencamera.cameracontroller.CameraController r0 = r14.camera_controller
            r0.setManualISO(r3, r3)
            r0 = r5
            r1 = 0
        L_0x01da:
            net.sourceforge.opencamera.preview.ApplicationInterface r5 = r14.applicationInterface
            r5.setISOPref(r0)
            goto L_0x0201
        L_0x01e0:
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r14.camera_controller
            net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues r0 = r1.setISO(r0)
            if (r0 == 0) goto L_0x01fb
            java.util.List<java.lang.String> r1 = r0.values
            r14.isos = r1
            java.lang.String r1 = r0.selected_value
            boolean r1 = r1.equals(r5)
            r1 = r1 ^ r2
            net.sourceforge.opencamera.preview.ApplicationInterface r5 = r14.applicationInterface
            java.lang.String r0 = r0.selected_value
            r5.setISOPref(r0)
            goto L_0x0201
        L_0x01fb:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            r0.clearISOPref()
        L_0x0200:
            r1 = 0
        L_0x0201:
            if (r1 == 0) goto L_0x0240
            boolean r0 = r14.supports_exposure_time
            if (r0 == 0) goto L_0x0231
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            long r5 = r0.getExposureTimePref()
            long r7 = r14.getMinimumExposureTime()
            int r0 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r0 >= 0) goto L_0x021a
            long r5 = r14.getMinimumExposureTime()
            goto L_0x0226
        L_0x021a:
            long r7 = r14.getMaximumExposureTime()
            int r0 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r0 <= 0) goto L_0x0226
            long r5 = r14.getMaximumExposureTime()
        L_0x0226:
            net.sourceforge.opencamera.cameracontroller.CameraController r0 = r14.camera_controller
            r0.setExposureTime(r5)
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            r0.setExposureTimePref(r5)
            goto L_0x0236
        L_0x0231:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            r0.clearExposureTimePref()
        L_0x0236:
            boolean r0 = r14.using_android_l
            if (r0 == 0) goto L_0x0240
            java.util.List<java.lang.String> r0 = r14.supported_flash_values
            if (r0 == 0) goto L_0x0240
            r14.supported_flash_values = r4
        L_0x0240:
            r14.exposures = r4
            int r0 = r14.min_exposure
            if (r0 != 0) goto L_0x0251
            int r0 = r14.max_exposure
            if (r0 == 0) goto L_0x024b
            goto L_0x0251
        L_0x024b:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            r0.clearExposureCompensationPref()
            goto L_0x029e
        L_0x0251:
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r14.exposures = r0
            int r0 = r14.min_exposure
        L_0x025a:
            int r5 = r14.max_exposure
            if (r0 > r5) goto L_0x0277
            java.util.List<java.lang.String> r5 = r14.exposures
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = ""
            r6.append(r7)
            r6.append(r0)
            java.lang.String r6 = r6.toString()
            r5.add(r6)
            int r0 = r0 + 1
            goto L_0x025a
        L_0x0277:
            if (r1 != 0) goto L_0x029e
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            int r0 = r0.getExposureCompensationPref()
            int r5 = r14.min_exposure
            if (r0 < r5) goto L_0x0287
            int r5 = r14.max_exposure
            if (r0 <= r5) goto L_0x0294
        L_0x0287:
            int r0 = r14.min_exposure
            if (r0 > 0) goto L_0x0292
            int r0 = r14.max_exposure
            if (r0 >= 0) goto L_0x0290
            goto L_0x0292
        L_0x0290:
            r0 = 0
            goto L_0x0294
        L_0x0292:
            int r0 = r14.min_exposure
        L_0x0294:
            net.sourceforge.opencamera.cameracontroller.CameraController r5 = r14.camera_controller
            r5.setExposureCompensation(r0)
            net.sourceforge.opencamera.preview.ApplicationInterface r5 = r14.applicationInterface
            r5.setExposureCompensationPref(r0)
        L_0x029e:
            r0 = -1
            r14.current_size_index = r0
            net.sourceforge.opencamera.preview.ApplicationInterface r5 = r14.applicationInterface
            android.util.Pair r5 = r5.getCameraResolutionPref()
            if (r5 == 0) goto L_0x02dd
            java.lang.Object r6 = r5.first
            java.lang.Integer r6 = (java.lang.Integer) r6
            int r6 = r6.intValue()
            java.lang.Object r5 = r5.second
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r7 = 0
        L_0x02ba:
            java.util.List<net.sourceforge.opencamera.cameracontroller.CameraController$Size> r8 = r14.sizes
            int r8 = r8.size()
            if (r7 >= r8) goto L_0x02db
            int r8 = r14.current_size_index
            if (r8 != r0) goto L_0x02db
            java.util.List<net.sourceforge.opencamera.cameracontroller.CameraController$Size> r8 = r14.sizes
            java.lang.Object r8 = r8.get(r7)
            net.sourceforge.opencamera.cameracontroller.CameraController$Size r8 = (net.sourceforge.opencamera.cameracontroller.CameraController.Size) r8
            int r9 = r8.width
            if (r9 != r6) goto L_0x02d8
            int r8 = r8.height
            if (r8 != r5) goto L_0x02d8
            r14.current_size_index = r7
        L_0x02d8:
            int r7 = r7 + 1
            goto L_0x02ba
        L_0x02db:
            int r5 = r14.current_size_index
        L_0x02dd:
            int r5 = r14.current_size_index
            if (r5 != r0) goto L_0x0309
            r6 = r4
            r5 = 0
        L_0x02e3:
            java.util.List<net.sourceforge.opencamera.cameracontroller.CameraController$Size> r7 = r14.sizes
            int r7 = r7.size()
            if (r5 >= r7) goto L_0x0309
            java.util.List<net.sourceforge.opencamera.cameracontroller.CameraController$Size> r7 = r14.sizes
            java.lang.Object r7 = r7.get(r5)
            net.sourceforge.opencamera.cameracontroller.CameraController$Size r7 = (net.sourceforge.opencamera.cameracontroller.CameraController.Size) r7
            if (r6 == 0) goto L_0x0303
            int r8 = r7.width
            int r9 = r7.height
            int r8 = r8 * r9
            int r9 = r6.width
            int r10 = r6.height
            int r9 = r9 * r10
            if (r8 <= r9) goto L_0x0306
        L_0x0303:
            r14.current_size_index = r5
            r6 = r7
        L_0x0306:
            int r5 = r5 + 1
            goto L_0x02e3
        L_0x0309:
            net.sourceforge.opencamera.cameracontroller.CameraController$Size r5 = r14.getCurrentPictureSize()
            if (r5 == 0) goto L_0x0318
            net.sourceforge.opencamera.preview.ApplicationInterface r6 = r14.applicationInterface
            int r7 = r5.width
            int r5 = r5.height
            r6.setCameraResolutionPref(r7, r5)
        L_0x0318:
            net.sourceforge.opencamera.preview.ApplicationInterface r5 = r14.applicationInterface
            int r5 = r5.getImageQualityPref()
            net.sourceforge.opencamera.cameracontroller.CameraController r6 = r14.camera_controller
            r6.setJpegQuality(r5)
            r14.initialiseVideoSizes()
            r14.initialiseVideoQuality()
            net.sourceforge.opencamera.preview.ApplicationInterface r5 = r14.applicationInterface
            java.lang.String r5 = r5.getVideoQualityPref()
            net.sourceforge.opencamera.preview.VideoQualityHandler r6 = r14.video_quality_handler
            r6.setCurrentVideoQualityIndex(r0)
            int r6 = r5.length()
            if (r6 <= 0) goto L_0x036e
            r6 = 0
        L_0x033b:
            net.sourceforge.opencamera.preview.VideoQualityHandler r7 = r14.video_quality_handler
            java.util.List r7 = r7.getSupportedVideoQuality()
            int r7 = r7.size()
            if (r6 >= r7) goto L_0x0369
            net.sourceforge.opencamera.preview.VideoQualityHandler r7 = r14.video_quality_handler
            int r7 = r7.getCurrentVideoQualityIndex()
            if (r7 != r0) goto L_0x0369
            net.sourceforge.opencamera.preview.VideoQualityHandler r7 = r14.video_quality_handler
            java.util.List r7 = r7.getSupportedVideoQuality()
            java.lang.Object r7 = r7.get(r6)
            java.lang.String r7 = (java.lang.String) r7
            boolean r7 = r7.equals(r5)
            if (r7 == 0) goto L_0x0366
            net.sourceforge.opencamera.preview.VideoQualityHandler r7 = r14.video_quality_handler
            r7.setCurrentVideoQualityIndex(r6)
        L_0x0366:
            int r6 = r6 + 1
            goto L_0x033b
        L_0x0369:
            net.sourceforge.opencamera.preview.VideoQualityHandler r5 = r14.video_quality_handler
            r5.getCurrentVideoQualityIndex()
        L_0x036e:
            net.sourceforge.opencamera.preview.VideoQualityHandler r5 = r14.video_quality_handler
            int r5 = r5.getCurrentVideoQualityIndex()
            if (r5 != r0) goto L_0x03b9
            net.sourceforge.opencamera.preview.VideoQualityHandler r5 = r14.video_quality_handler
            java.util.List r5 = r5.getSupportedVideoQuality()
            int r5 = r5.size()
            if (r5 <= 0) goto L_0x03b9
            net.sourceforge.opencamera.preview.VideoQualityHandler r5 = r14.video_quality_handler
            r5.setCurrentVideoQualityIndex(r3)
            r5 = 0
        L_0x0388:
            net.sourceforge.opencamera.preview.VideoQualityHandler r6 = r14.video_quality_handler
            java.util.List r6 = r6.getSupportedVideoQuality()
            int r6 = r6.size()
            if (r5 >= r6) goto L_0x03b9
            net.sourceforge.opencamera.preview.VideoQualityHandler r6 = r14.video_quality_handler
            java.util.List r6 = r6.getSupportedVideoQuality()
            java.lang.Object r6 = r6.get(r5)
            java.lang.String r6 = (java.lang.String) r6
            android.media.CamcorderProfile r6 = r14.getCamcorderProfile(r6)
            int r7 = r6.videoFrameWidth
            r8 = 1920(0x780, float:2.69E-42)
            if (r7 != r8) goto L_0x03b6
            int r6 = r6.videoFrameHeight
            r7 = 1080(0x438, float:1.513E-42)
            if (r6 != r7) goto L_0x03b6
            net.sourceforge.opencamera.preview.VideoQualityHandler r6 = r14.video_quality_handler
            r6.setCurrentVideoQualityIndex(r5)
            goto L_0x03b9
        L_0x03b6:
            int r5 = r5 + 1
            goto L_0x0388
        L_0x03b9:
            net.sourceforge.opencamera.preview.VideoQualityHandler r5 = r14.video_quality_handler
            int r5 = r5.getCurrentVideoQualityIndex()
            java.lang.String r6 = "Preview"
            if (r5 == r0) goto L_0x03cf
            net.sourceforge.opencamera.preview.ApplicationInterface r5 = r14.applicationInterface
            net.sourceforge.opencamera.preview.VideoQualityHandler r7 = r14.video_quality_handler
            java.lang.String r7 = r7.getCurrentVideoQuality()
            r5.setVideoQualityPref(r7)
            goto L_0x03d6
        L_0x03cf:
            java.lang.String r5 = "no video qualities found"
            android.util.Log.e(r6, r5)
            r14.supports_video = r3
        L_0x03d6:
            boolean r5 = r14.supports_video
            if (r5 == 0) goto L_0x04e2
            net.sourceforge.opencamera.preview.ApplicationInterface r5 = r14.applicationInterface
            float r5 = r5.getVideoCaptureRateFactor()
            r14.capture_rate_factor = r5
            float r5 = r14.capture_rate_factor
            r7 = 1065353216(0x3f800000, float:1.0)
            float r5 = r5 - r7
            float r5 = java.lang.Math.abs(r5)
            r7 = 925353388(0x3727c5ac, float:1.0E-5)
            int r5 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r5 <= 0) goto L_0x03f4
            r5 = 1
            goto L_0x03f5
        L_0x03f4:
            r5 = 0
        L_0x03f5:
            r14.has_capture_rate_factor = r5
            r14.video_high_speed = r3
            boolean r5 = r14.supports_video_high_speed
            if (r5 == 0) goto L_0x04e2
            net.sourceforge.opencamera.preview.VideoProfile r5 = r14.getVideoProfile()
            net.sourceforge.opencamera.preview.VideoQualityHandler r7 = r14.video_quality_handler
            int r8 = r5.videoFrameWidth
            int r9 = r5.videoFrameHeight
            double r10 = r5.videoCaptureRate
            net.sourceforge.opencamera.cameracontroller.CameraController$Size r7 = r7.findVideoSizeForFrameRate(r8, r9, r10)
            java.lang.String r8 = " at fps "
            java.lang.String r9 = " x "
            if (r7 != 0) goto L_0x04b3
            net.sourceforge.opencamera.preview.VideoQualityHandler r10 = r14.video_quality_handler
            java.util.List r10 = r10.getSupportedVideoSizesHighSpeed()
            if (r10 == 0) goto L_0x04b3
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r10 = "can't find match for capture rate: "
            r7.append(r10)
            double r10 = r5.videoCaptureRate
            r7.append(r10)
            java.lang.String r10 = " and video size: "
            r7.append(r10)
            int r10 = r5.videoFrameWidth
            r7.append(r10)
            r7.append(r9)
            int r10 = r5.videoFrameHeight
            r7.append(r10)
            r7.append(r8)
            double r10 = r5.videoCaptureRate
            r7.append(r10)
            java.lang.String r7 = r7.toString()
            android.util.Log.e(r6, r7)
            net.sourceforge.opencamera.preview.VideoQualityHandler r7 = r14.video_quality_handler
            net.sourceforge.opencamera.cameracontroller.CameraController$Size r7 = r7.getMaxSupportedVideoSizeHighSpeed()
            int r10 = r7.width
            r5.videoFrameWidth = r10
            int r10 = r7.height
            r5.videoFrameHeight = r10
            net.sourceforge.opencamera.preview.VideoQualityHandler r10 = r14.video_quality_handler
            java.util.List r10 = r10.getSupportedVideoSizesHighSpeed()
            double r11 = r5.videoCaptureRate
            net.sourceforge.opencamera.cameracontroller.CameraController$Size r7 = net.sourceforge.opencamera.cameracontroller.CameraController.CameraFeatures.findSize(r10, r7, r11, r3)
            if (r7 == 0) goto L_0x04b3
            net.sourceforge.opencamera.preview.VideoQualityHandler r10 = r14.video_quality_handler
            r10.setCurrentVideoQualityIndex(r0)
            r10 = 0
        L_0x046d:
            net.sourceforge.opencamera.preview.VideoQualityHandler r11 = r14.video_quality_handler
            java.util.List r11 = r11.getSupportedVideoQuality()
            int r11 = r11.size()
            if (r10 >= r11) goto L_0x049e
            net.sourceforge.opencamera.preview.VideoQualityHandler r11 = r14.video_quality_handler
            java.util.List r11 = r11.getSupportedVideoQuality()
            java.lang.Object r11 = r11.get(r10)
            java.lang.String r11 = (java.lang.String) r11
            android.media.CamcorderProfile r11 = r14.getCamcorderProfile(r11)
            int r12 = r11.videoFrameWidth
            int r13 = r5.videoFrameWidth
            if (r12 != r13) goto L_0x049b
            int r11 = r11.videoFrameHeight
            int r12 = r5.videoFrameHeight
            if (r11 != r12) goto L_0x049b
            net.sourceforge.opencamera.preview.VideoQualityHandler r11 = r14.video_quality_handler
            r11.setCurrentVideoQualityIndex(r10)
            goto L_0x049e
        L_0x049b:
            int r10 = r10 + 1
            goto L_0x046d
        L_0x049e:
            net.sourceforge.opencamera.preview.VideoQualityHandler r10 = r14.video_quality_handler
            int r10 = r10.getCurrentVideoQualityIndex()
            if (r10 == r0) goto L_0x04b2
            net.sourceforge.opencamera.preview.ApplicationInterface r10 = r14.applicationInterface
            net.sourceforge.opencamera.preview.VideoQualityHandler r11 = r14.video_quality_handler
            java.lang.String r11 = r11.getCurrentVideoQuality()
            r10.setVideoQualityPref(r11)
            goto L_0x04b3
        L_0x04b2:
            r7 = r4
        L_0x04b3:
            if (r7 != 0) goto L_0x04dc
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r10 = "fps not supported for this video size: "
            r7.append(r10)
            int r10 = r5.videoFrameWidth
            r7.append(r10)
            r7.append(r9)
            int r9 = r5.videoFrameHeight
            r7.append(r9)
            r7.append(r8)
            double r8 = r5.videoCaptureRate
            r7.append(r8)
            java.lang.String r5 = r7.toString()
            android.util.Log.e(r6, r5)
            goto L_0x04e2
        L_0x04dc:
            boolean r5 = r7.high_speed
            if (r5 == 0) goto L_0x04e2
            r14.video_high_speed = r2
        L_0x04e2:
            boolean r5 = r14.is_video
            if (r5 == 0) goto L_0x04f5
            boolean r5 = r14.video_high_speed
            if (r5 == 0) goto L_0x04f5
            boolean r5 = r14.supports_iso_range
            if (r5 == 0) goto L_0x04f5
            if (r1 == 0) goto L_0x04f5
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r14.camera_controller
            r1.setManualISO(r3, r3)
        L_0x04f5:
            r14.current_flash_index = r0
            java.util.List<java.lang.String> r1 = r14.supported_flash_values
            if (r1 == 0) goto L_0x052b
            int r1 = r1.size()
            if (r1 <= r2) goto L_0x052b
            net.sourceforge.opencamera.preview.ApplicationInterface r1 = r14.applicationInterface
            java.lang.String r1 = r1.getFlashPref()
            int r5 = r1.length()
            if (r5 <= 0) goto L_0x0517
            boolean r1 = r14.updateFlash(r1, r3)
            if (r1 != 0) goto L_0x052d
            r14.updateFlash(r3, r2)
            goto L_0x052d
        L_0x0517:
            java.util.List<java.lang.String> r1 = r14.supported_flash_values
            java.lang.String r5 = "flash_auto"
            boolean r1 = r1.contains(r5)
            if (r1 == 0) goto L_0x0525
            r14.updateFlash(r5, r2)
            goto L_0x052d
        L_0x0525:
            java.lang.String r1 = "flash_off"
            r14.updateFlash(r1, r2)
            goto L_0x052d
        L_0x052b:
            r14.supported_flash_values = r4
        L_0x052d:
            r14.current_focus_index = r0
            java.util.List<java.lang.String> r0 = r14.supported_focus_values
            if (r0 == 0) goto L_0x053d
            int r0 = r0.size()
            if (r0 <= r2) goto L_0x053d
            r14.setFocusPref(r2)
            goto L_0x053f
        L_0x053d:
            r14.supported_focus_values = r4
        L_0x053f:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            float r0 = r0.getFocusDistancePref(r3)
            r1 = 0
            int r4 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r4 >= 0) goto L_0x054c
            r0 = 0
            goto L_0x0553
        L_0x054c:
            float r4 = r14.minimum_focus_distance
            int r5 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1))
            if (r5 <= 0) goto L_0x0553
            r0 = r4
        L_0x0553:
            net.sourceforge.opencamera.cameracontroller.CameraController r4 = r14.camera_controller
            r4.setFocusDistance(r0)
            net.sourceforge.opencamera.cameracontroller.CameraController r4 = r14.camera_controller
            r4.setFocusBracketingSourceDistance(r0)
            net.sourceforge.opencamera.preview.ApplicationInterface r4 = r14.applicationInterface
            r4.setFocusDistancePref(r0, r3)
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            float r0 = r0.getFocusDistancePref(r2)
            int r4 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r4 >= 0) goto L_0x056d
            goto L_0x0575
        L_0x056d:
            float r1 = r14.minimum_focus_distance
            int r4 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r4 <= 0) goto L_0x0574
            goto L_0x0575
        L_0x0574:
            r1 = r0
        L_0x0575:
            net.sourceforge.opencamera.cameracontroller.CameraController r0 = r14.camera_controller
            r0.setFocusBracketingTargetDistance(r1)
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            r0.setFocusDistancePref(r1, r2)
            r14.is_exposure_locked = r3
            r14.is_white_balance_locked = r3
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.preview.Preview.setupCameraParameters():void");
    }

    private void setPreviewSize() {
        Size size;
        if (this.camera_controller != null) {
            if (this.is_preview_started) {
                Log.e(TAG, "setPreviewSize() shouldn't be called when preview is running");
                return;
            }
            if (!this.using_android_l) {
                cancelAutoFocus();
            }
            if (this.is_video) {
                VideoProfile videoProfile = getVideoProfile();
                if (this.video_high_speed) {
                    size = new Size(videoProfile.videoFrameWidth, videoProfile.videoFrameHeight);
                } else {
                    double d = (double) videoProfile.videoFrameWidth;
                    double d2 = (double) videoProfile.videoFrameHeight;
                    Double.isNaN(d);
                    Double.isNaN(d2);
                    size = getOptimalVideoPictureSize(this.sizes, d / d2);
                }
            } else {
                size = getCurrentPictureSize();
            }
            if (size != null) {
                this.camera_controller.setPictureSize(size.width, size.height);
            }
            List<Size> list = this.supported_preview_sizes;
            if (list != null && list.size() > 0) {
                Size optimalPreviewSize = getOptimalPreviewSize(this.supported_preview_sizes);
                this.camera_controller.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
                this.set_preview_size = true;
                this.preview_w = optimalPreviewSize.width;
                this.preview_h = optimalPreviewSize.height;
                double d3 = (double) optimalPreviewSize.width;
                double d4 = (double) optimalPreviewSize.height;
                Double.isNaN(d3);
                Double.isNaN(d4);
                setAspectRatio(d3 / d4);
            }
        }
    }

    private void initialiseVideoSizes() {
        if (this.camera_controller != null) {
            this.video_quality_handler.sortVideoSizes();
        }
    }

    private void initialiseVideoQuality() {
        int cameraId = this.camera_controller.getCameraId();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        if (CamcorderProfile.hasProfile(cameraId, 1)) {
            CamcorderProfile camcorderProfile = CamcorderProfile.get(cameraId, 1);
            arrayList.add(Integer.valueOf(1));
            arrayList2.add(new Dimension2D(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight));
        }
        if (VERSION.SDK_INT >= 21 && CamcorderProfile.hasProfile(cameraId, 8)) {
            CamcorderProfile camcorderProfile2 = CamcorderProfile.get(cameraId, 8);
            arrayList.add(Integer.valueOf(8));
            arrayList2.add(new Dimension2D(camcorderProfile2.videoFrameWidth, camcorderProfile2.videoFrameHeight));
        }
        if (CamcorderProfile.hasProfile(cameraId, 6)) {
            CamcorderProfile camcorderProfile3 = CamcorderProfile.get(cameraId, 6);
            arrayList.add(Integer.valueOf(6));
            arrayList2.add(new Dimension2D(camcorderProfile3.videoFrameWidth, camcorderProfile3.videoFrameHeight));
        }
        if (CamcorderProfile.hasProfile(cameraId, 5)) {
            CamcorderProfile camcorderProfile4 = CamcorderProfile.get(cameraId, 5);
            arrayList.add(Integer.valueOf(5));
            arrayList2.add(new Dimension2D(camcorderProfile4.videoFrameWidth, camcorderProfile4.videoFrameHeight));
        }
        if (CamcorderProfile.hasProfile(cameraId, 4)) {
            CamcorderProfile camcorderProfile5 = CamcorderProfile.get(cameraId, 4);
            arrayList.add(Integer.valueOf(4));
            arrayList2.add(new Dimension2D(camcorderProfile5.videoFrameWidth, camcorderProfile5.videoFrameHeight));
        }
        if (CamcorderProfile.hasProfile(cameraId, 3)) {
            CamcorderProfile camcorderProfile6 = CamcorderProfile.get(cameraId, 3);
            arrayList.add(Integer.valueOf(3));
            arrayList2.add(new Dimension2D(camcorderProfile6.videoFrameWidth, camcorderProfile6.videoFrameHeight));
        }
        if (CamcorderProfile.hasProfile(cameraId, 7)) {
            CamcorderProfile camcorderProfile7 = CamcorderProfile.get(cameraId, 7);
            arrayList.add(Integer.valueOf(7));
            arrayList2.add(new Dimension2D(camcorderProfile7.videoFrameWidth, camcorderProfile7.videoFrameHeight));
        }
        if (CamcorderProfile.hasProfile(cameraId, 2)) {
            CamcorderProfile camcorderProfile8 = CamcorderProfile.get(cameraId, 2);
            arrayList.add(Integer.valueOf(2));
            arrayList2.add(new Dimension2D(camcorderProfile8.videoFrameWidth, camcorderProfile8.videoFrameHeight));
        }
        if (CamcorderProfile.hasProfile(cameraId, 0)) {
            CamcorderProfile camcorderProfile9 = CamcorderProfile.get(cameraId, 0);
            arrayList.add(Integer.valueOf(0));
            arrayList2.add(new Dimension2D(camcorderProfile9.videoFrameWidth, camcorderProfile9.videoFrameHeight));
        }
        this.video_quality_handler.initialiseVideoQualityFromProfiles(arrayList, arrayList2);
    }

    private CamcorderProfile getCamcorderProfile(String str) {
        CameraController cameraController = this.camera_controller;
        if (cameraController == null) {
            return CamcorderProfile.get(0, 1);
        }
        int cameraId = cameraController.getCameraId();
        CamcorderProfile camcorderProfile = CamcorderProfile.get(cameraId, 1);
        try {
            int indexOf = str.indexOf(95);
            camcorderProfile = CamcorderProfile.get(cameraId, Integer.parseInt(indexOf != -1 ? str.substring(0, indexOf) : str));
            if (indexOf != -1) {
                int i = indexOf + 1;
                if (i < str.length()) {
                    String substring = str.substring(i);
                    if (substring.charAt(0) == 'r' && substring.length() >= 4) {
                        int indexOf2 = substring.indexOf(120);
                        if (indexOf2 != -1) {
                            String substring2 = substring.substring(1, indexOf2);
                            String substring3 = substring.substring(indexOf2 + 1);
                            int parseInt = Integer.parseInt(substring2);
                            int parseInt2 = Integer.parseInt(substring3);
                            camcorderProfile.videoFrameWidth = parseInt;
                            camcorderProfile.videoFrameHeight = parseInt2;
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return camcorderProfile;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:109:0x01ed, code lost:
        if (r0.equals("preference_video_output_format_default") != false) goto L_0x0205;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public net.sourceforge.opencamera.preview.VideoProfile getVideoProfile() {
        /*
            r14 = this;
            net.sourceforge.opencamera.cameracontroller.CameraController r0 = r14.camera_controller
            java.lang.String r1 = "Preview"
            if (r0 != 0) goto L_0x0011
            net.sourceforge.opencamera.preview.VideoProfile r0 = new net.sourceforge.opencamera.preview.VideoProfile
            r0.<init>()
            java.lang.String r2 = "camera not opened! returning default video profile for QUALITY_HIGH"
            android.util.Log.e(r1, r2)
            return r0
        L_0x0011:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            boolean r0 = r0.getRecordAudioPref()
            net.sourceforge.opencamera.preview.ApplicationInterface r2 = r14.applicationInterface
            java.lang.String r2 = r2.getRecordAudioChannelsPref()
            net.sourceforge.opencamera.preview.ApplicationInterface r3 = r14.applicationInterface
            java.lang.String r3 = r3.getVideoFPSPref()
            net.sourceforge.opencamera.preview.ApplicationInterface r4 = r14.applicationInterface
            java.lang.String r4 = r4.getVideoBitratePref()
            net.sourceforge.opencamera.preview.ApplicationInterface r5 = r14.applicationInterface
            boolean r5 = r5.getForce4KPref()
            net.sourceforge.opencamera.cameracontroller.CameraController r6 = r14.camera_controller
            int r6 = r6.getCameraId()
            r7 = -1
            r8 = 1
            if (r5 == 0) goto L_0x005a
            boolean r5 = r14.video_high_speed
            if (r5 != 0) goto L_0x005a
            android.media.CamcorderProfile r5 = android.media.CamcorderProfile.get(r6, r8)
            r6 = 3840(0xf00, float:5.381E-42)
            r5.videoFrameWidth = r6
            r6 = 2160(0x870, float:3.027E-42)
            r5.videoFrameHeight = r6
            int r6 = r5.videoBitRate
            double r9 = (double) r6
            r11 = 4613487458278336102(0x4006666666666666, double:2.8)
            java.lang.Double.isNaN(r9)
            double r9 = r9 * r11
            int r6 = (int) r9
            r5.videoBitRate = r6
            goto L_0x006e
        L_0x005a:
            net.sourceforge.opencamera.preview.VideoQualityHandler r5 = r14.video_quality_handler
            int r5 = r5.getCurrentVideoQualityIndex()
            if (r5 == r7) goto L_0x006d
            net.sourceforge.opencamera.preview.VideoQualityHandler r5 = r14.video_quality_handler
            java.lang.String r5 = r5.getCurrentVideoQuality()
            android.media.CamcorderProfile r5 = r14.getCamcorderProfile(r5)
            goto L_0x006e
        L_0x006d:
            r5 = 0
        L_0x006e:
            net.sourceforge.opencamera.preview.VideoProfile r6 = new net.sourceforge.opencamera.preview.VideoProfile
            if (r5 == 0) goto L_0x0076
            r6.<init>(r5)
            goto L_0x0079
        L_0x0076:
            r6.<init>()
        L_0x0079:
            java.lang.String r5 = "default"
            boolean r9 = r3.equals(r5)
            if (r9 != 0) goto L_0x008c
            int r3 = java.lang.Integer.parseInt(r3)     // Catch:{ NumberFormatException -> 0x008b }
            r6.videoFrameRate = r3     // Catch:{ NumberFormatException -> 0x008b }
            double r9 = (double) r3     // Catch:{ NumberFormatException -> 0x008b }
            r6.videoCaptureRate = r9     // Catch:{ NumberFormatException -> 0x008b }
            goto L_0x008c
        L_0x008b:
        L_0x008c:
            boolean r3 = r4.equals(r5)
            if (r3 != 0) goto L_0x009a
            int r3 = java.lang.Integer.parseInt(r4)     // Catch:{ NumberFormatException -> 0x0099 }
            r6.videoBitRate = r3     // Catch:{ NumberFormatException -> 0x0099 }
            goto L_0x009a
        L_0x0099:
        L_0x009a:
            boolean r3 = r14.video_high_speed
            if (r3 == 0) goto L_0x00a7
            int r3 = r6.videoBitRate
            r4 = 56000000(0x3567e00, float:6.303359E-37)
            if (r3 >= r4) goto L_0x00a7
            r6.videoBitRate = r4
        L_0x00a7:
            boolean r3 = r14.has_capture_rate_factor
            r4 = 0
            if (r3 == 0) goto L_0x010a
            float r0 = r14.capture_rate_factor
            double r9 = (double) r0
            r3 = 925353388(0x3727c5ac, float:1.0E-5)
            r11 = 4607182418800017408(0x3ff0000000000000, double:1.0)
            int r5 = (r9 > r11 ? 1 : (r9 == r11 ? 0 : -1))
            if (r5 >= 0) goto L_0x00e4
            int r0 = r6.videoFrameRate
            float r0 = (float) r0
            float r5 = r14.capture_rate_factor
            float r0 = r0 * r5
            r5 = 1056964608(0x3f000000, float:0.5)
            float r0 = r0 + r5
            int r0 = (int) r0
            r6.videoFrameRate = r0
            int r0 = r6.videoBitRate
            float r0 = (float) r0
            float r9 = r14.capture_rate_factor
            float r0 = r0 * r9
            float r0 = r0 + r5
            int r0 = (int) r0
            r6.videoBitRate = r0
            float r9 = r9 - r5
            float r0 = java.lang.Math.abs(r9)
            int r0 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r0 >= 0) goto L_0x0109
            double r9 = r6.videoCaptureRate
            r11 = 4562254508917369340(0x3f50624dd2f1a9fc, double:0.001)
            double r9 = r9 + r11
            r6.videoCaptureRate = r9
            goto L_0x0109
        L_0x00e4:
            double r9 = (double) r0
            int r0 = (r9 > r11 ? 1 : (r9 == r11 ? 0 : -1))
            if (r0 <= 0) goto L_0x0109
            double r9 = r6.videoCaptureRate
            float r0 = r14.capture_rate_factor
            double r11 = (double) r0
            java.lang.Double.isNaN(r11)
            double r9 = r9 / r11
            r6.videoCaptureRate = r9
            r5 = 1073741824(0x40000000, float:2.0)
            float r0 = r0 - r5
            float r0 = java.lang.Math.abs(r0)
            int r0 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r0 >= 0) goto L_0x0109
            double r9 = r6.videoCaptureRate
            r11 = 4562254509136412672(0x3f50624de0000000, double:0.0010000000474974513)
            double r9 = r9 - r11
            r6.videoCaptureRate = r9
        L_0x0109:
            r0 = 0
        L_0x010a:
            boolean r3 = r14.using_android_l
            r5 = 21
            r9 = 2
            if (r3 == 0) goto L_0x0118
            int r3 = android.os.Build.VERSION.SDK_INT
            if (r3 < r5) goto L_0x0118
            r6.videoSource = r9
            goto L_0x011a
        L_0x0118:
            r6.videoSource = r8
        L_0x011a:
            int r3 = android.os.Build.VERSION.SDK_INT
            r10 = 23
            if (r3 < r10) goto L_0x0131
            if (r0 == 0) goto L_0x0131
            android.content.Context r3 = r14.getContext()
            java.lang.String r10 = "android.permission.RECORD_AUDIO"
            int r3 = android.support.p000v4.content.ContextCompat.checkSelfPermission(r3, r10)
            if (r3 == 0) goto L_0x0131
            r6.no_audio_permission = r8
            r0 = 0
        L_0x0131:
            r6.record_audio = r0
            r3 = 24
            r10 = 5
            r11 = 4
            r12 = 3
            if (r0 == 0) goto L_0x01c5
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            java.lang.String r0 = r0.getRecordAudioSourcePref()
            int r13 = r0.hashCode()
            switch(r13) {
                case -1778647043: goto L_0x017a;
                case -1591996925: goto L_0x0170;
                case 349734033: goto L_0x0166;
                case 573829957: goto L_0x015c;
                case 799584998: goto L_0x0152;
                case 1217798166: goto L_0x0148;
                default: goto L_0x0147;
            }
        L_0x0147:
            goto L_0x0184
        L_0x0148:
            java.lang.String r13 = "audio_src_camcorder"
            boolean r0 = r0.equals(r13)
            if (r0 == 0) goto L_0x0184
            r0 = 5
            goto L_0x0185
        L_0x0152:
            java.lang.String r13 = "audio_src_voice_recognition"
            boolean r0 = r0.equals(r13)
            if (r0 == 0) goto L_0x0184
            r0 = 3
            goto L_0x0185
        L_0x015c:
            java.lang.String r13 = "audio_src_voice_communication"
            boolean r0 = r0.equals(r13)
            if (r0 == 0) goto L_0x0184
            r0 = 2
            goto L_0x0185
        L_0x0166:
            java.lang.String r13 = "audio_src_unprocessed"
            boolean r0 = r0.equals(r13)
            if (r0 == 0) goto L_0x0184
            r0 = 4
            goto L_0x0185
        L_0x0170:
            java.lang.String r13 = "audio_src_mic"
            boolean r0 = r0.equals(r13)
            if (r0 == 0) goto L_0x0184
            r0 = 0
            goto L_0x0185
        L_0x017a:
            java.lang.String r13 = "audio_src_default"
            boolean r0 = r0.equals(r13)
            if (r0 == 0) goto L_0x0184
            r0 = 1
            goto L_0x0185
        L_0x0184:
            r0 = -1
        L_0x0185:
            if (r0 == 0) goto L_0x01ae
            if (r0 == r8) goto L_0x01ab
            if (r0 == r9) goto L_0x01a7
            if (r0 == r12) goto L_0x01a3
            if (r0 == r11) goto L_0x0192
            r6.audioSource = r10
            goto L_0x01b0
        L_0x0192:
            int r0 = android.os.Build.VERSION.SDK_INT
            if (r0 < r3) goto L_0x019b
            r0 = 9
            r6.audioSource = r0
            goto L_0x01b0
        L_0x019b:
            java.lang.String r0 = "audio_src_voice_unprocessed requires Android 7"
            android.util.Log.e(r1, r0)
            r6.audioSource = r10
            goto L_0x01b0
        L_0x01a3:
            r0 = 6
            r6.audioSource = r0
            goto L_0x01b0
        L_0x01a7:
            r0 = 7
            r6.audioSource = r0
            goto L_0x01b0
        L_0x01ab:
            r6.audioSource = r4
            goto L_0x01b0
        L_0x01ae:
            r6.audioSource = r8
        L_0x01b0:
            java.lang.String r0 = "audio_mono"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x01bb
            r6.audioChannels = r8
            goto L_0x01c5
        L_0x01bb:
            java.lang.String r0 = "audio_stereo"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x01c5
            r6.audioChannels = r9
        L_0x01c5:
            net.sourceforge.opencamera.preview.ApplicationInterface r0 = r14.applicationInterface
            java.lang.String r0 = r0.getRecordVideoOutputFormatPref()
            int r2 = r0.hashCode()
            switch(r2) {
                case 474898602: goto L_0x01fa;
                case 474949644: goto L_0x01f0;
                case 527338191: goto L_0x01e7;
                case 1642530246: goto L_0x01dd;
                case 1644553675: goto L_0x01d3;
                default: goto L_0x01d2;
            }
        L_0x01d2:
            goto L_0x0204
        L_0x01d3:
            java.lang.String r2 = "preference_video_output_format_webm"
            boolean r2 = r0.equals(r2)
            if (r2 == 0) goto L_0x0204
            r4 = 4
            goto L_0x0205
        L_0x01dd:
            java.lang.String r2 = "preference_video_output_format_3gpp"
            boolean r2 = r0.equals(r2)
            if (r2 == 0) goto L_0x0204
            r4 = 3
            goto L_0x0205
        L_0x01e7:
            java.lang.String r2 = "preference_video_output_format_default"
            boolean r2 = r0.equals(r2)
            if (r2 == 0) goto L_0x0204
            goto L_0x0205
        L_0x01f0:
            java.lang.String r2 = "preference_video_output_format_mpeg4_hevc"
            boolean r2 = r0.equals(r2)
            if (r2 == 0) goto L_0x0204
            r4 = 2
            goto L_0x0205
        L_0x01fa:
            java.lang.String r2 = "preference_video_output_format_mpeg4_h264"
            boolean r2 = r0.equals(r2)
            if (r2 == 0) goto L_0x0204
            r4 = 1
            goto L_0x0205
        L_0x0204:
            r4 = -1
        L_0x0205:
            if (r4 == 0) goto L_0x024e
            if (r4 == r8) goto L_0x0248
            if (r4 == r9) goto L_0x023d
            if (r4 == r12) goto L_0x0236
            if (r4 == r11) goto L_0x0224
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "unknown pref_video_output_format: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            android.util.Log.e(r1, r0)
            goto L_0x024e
        L_0x0224:
            int r0 = android.os.Build.VERSION.SDK_INT
            if (r0 < r5) goto L_0x024e
            r0 = 9
            r6.fileFormat = r0
            r6.videoCodec = r11
            r0 = 6
            r6.audioCodec = r0
            java.lang.String r0 = "webm"
            r6.fileExtension = r0
            goto L_0x024e
        L_0x0236:
            r6.fileFormat = r8
            java.lang.String r0 = "3gp"
            r6.fileExtension = r0
            goto L_0x024e
        L_0x023d:
            int r0 = android.os.Build.VERSION.SDK_INT
            if (r0 < r3) goto L_0x024e
            r6.fileFormat = r9
            r6.videoCodec = r10
            r6.audioCodec = r12
            goto L_0x024e
        L_0x0248:
            r6.fileFormat = r9
            r6.videoCodec = r9
            r6.audioCodec = r12
        L_0x024e:
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.preview.Preview.getVideoProfile():net.sourceforge.opencamera.preview.VideoProfile");
    }

    private static String formatFloatToString(float f) {
        int i = (int) f;
        if (f == ((float) i)) {
            return Integer.toString(i);
        }
        return String.format(Locale.getDefault(), "%.2f", new Object[]{Float.valueOf(f)});
    }

    private static int greatestCommonFactor(int i, int i2) {
        while (true) {
            int i3 = i2;
            int i4 = i;
            i = i3;
            if (i <= 0) {
                return i4;
            }
            i2 = i4 % i;
        }
    }

    private static String getAspectRatio(int i, int i2) {
        int greatestCommonFactor = greatestCommonFactor(i, i2);
        if (greatestCommonFactor > 0) {
            i /= greatestCommonFactor;
            i2 /= greatestCommonFactor;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(i);
        sb.append(":");
        sb.append(i2);
        return sb.toString();
    }

    private static String getMPString(int i, int i2) {
        float f = ((float) (i * i2)) / 1000000.0f;
        StringBuilder sb = new StringBuilder();
        sb.append(formatFloatToString(f));
        sb.append("MP");
        return sb.toString();
    }

    private static String getBurstString(Resources resources, boolean z) {
        if (z) {
            return BuildConfig.FLAVOR;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(", ");
        sb.append(resources.getString(C0316R.string.no_burst));
        return sb.toString();
    }

    public static String getAspectRatioMPString(Resources resources, int i, int i2, boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(getAspectRatio(i, i2));
        sb.append(", ");
        sb.append(getMPString(i, i2));
        sb.append(getBurstString(resources, z));
        sb.append(")");
        return sb.toString();
    }

    public String getCamcorderProfileDescriptionShort(String str) {
        if (this.camera_controller == null) {
            return BuildConfig.FLAVOR;
        }
        CamcorderProfile camcorderProfile = getCamcorderProfile(str);
        StringBuilder sb = new StringBuilder();
        sb.append(camcorderProfile.videoFrameWidth);
        sb.append("x");
        sb.append(camcorderProfile.videoFrameHeight);
        return sb.toString();
    }

    public String getCamcorderProfileDescription(String str) {
        CameraController cameraController = this.camera_controller;
        String str2 = BuildConfig.FLAVOR;
        if (cameraController == null) {
            return str2;
        }
        CamcorderProfile camcorderProfile = getCamcorderProfile(str);
        String str3 = camcorderProfile.quality == 1 ? "Highest: " : str2;
        if (camcorderProfile.videoFrameWidth == 3840 && camcorderProfile.videoFrameHeight == 2160) {
            str2 = "4K Ultra HD ";
        } else if (camcorderProfile.videoFrameWidth == 1920 && camcorderProfile.videoFrameHeight == 1080) {
            str2 = "Full HD ";
        } else if (camcorderProfile.videoFrameWidth == 1280 && camcorderProfile.videoFrameHeight == 720) {
            str2 = "HD ";
        } else if (camcorderProfile.videoFrameWidth == 720 && camcorderProfile.videoFrameHeight == 480) {
            str2 = "SD ";
        } else if (camcorderProfile.videoFrameWidth == 640 && camcorderProfile.videoFrameHeight == 480) {
            str2 = "VGA ";
        } else if (camcorderProfile.videoFrameWidth == 352 && camcorderProfile.videoFrameHeight == 288) {
            str2 = "CIF ";
        } else if (camcorderProfile.videoFrameWidth == 320 && camcorderProfile.videoFrameHeight == 240) {
            str2 = "QVGA ";
        } else if (camcorderProfile.videoFrameWidth == 176 && camcorderProfile.videoFrameHeight == 144) {
            str2 = "QCIF ";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str3);
        sb.append(str2);
        sb.append(camcorderProfile.videoFrameWidth);
        sb.append("x");
        sb.append(camcorderProfile.videoFrameHeight);
        sb.append(" ");
        sb.append(getAspectRatioMPString(getResources(), camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight, true));
        return sb.toString();
    }

    public double getTargetRatio() {
        return this.preview_targetRatio;
    }

    private double calculateTargetRatioForPreview(Point point) {
        double d;
        double d2;
        if (!this.applicationInterface.getPreviewSizePref().equals("preference_preview_size_wysiwyg") && !this.is_video) {
            d2 = (double) point.x;
            d = (double) point.y;
            Double.isNaN(d2);
            Double.isNaN(d);
        } else if (this.is_video) {
            VideoProfile videoProfile = getVideoProfile();
            d2 = (double) videoProfile.videoFrameWidth;
            d = (double) videoProfile.videoFrameHeight;
            Double.isNaN(d2);
            Double.isNaN(d);
        } else {
            Size pictureSize = this.camera_controller.getPictureSize();
            d2 = (double) pictureSize.width;
            d = (double) pictureSize.height;
            Double.isNaN(d2);
            Double.isNaN(d);
        }
        double d3 = d2 / d;
        this.preview_targetRatio = d3;
        return d3;
    }

    private static Size getClosestSize(List<Size> list, double d, Size size) {
        Size size2 = null;
        double d2 = Double.MAX_VALUE;
        for (Size size3 : list) {
            double d3 = (double) size3.width;
            double d4 = (double) size3.height;
            Double.isNaN(d3);
            Double.isNaN(d4);
            double d5 = d3 / d4;
            if (size == null || (size3.width <= size.width && size3.height <= size.height)) {
                double d6 = d5 - d;
                if (Math.abs(d6) < d2) {
                    d2 = Math.abs(d6);
                    size2 = size3;
                }
            }
        }
        return size2;
    }

    public Size getOptimalPreviewSize(List<Size> list) {
        if (list == null) {
            return null;
        }
        if (!this.is_video || !this.video_high_speed) {
            Point point = new Point();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(point);
            if (point.x < point.y) {
                point.set(point.y, point.x);
            }
            double calculateTargetRatioForPreview = calculateTargetRatioForPreview(point);
            int min = Math.min(point.y, point.x);
            if (min <= 0) {
                min = point.y;
            }
            double d = Double.MAX_VALUE;
            Size size = null;
            for (Size size2 : list) {
                double d2 = (double) size2.width;
                double d3 = (double) size2.height;
                Double.isNaN(d2);
                Double.isNaN(d3);
                if (Math.abs((d2 / d3) - calculateTargetRatioForPreview) <= 0.05d && ((double) Math.abs(size2.height - min)) < d) {
                    d = (double) Math.abs(size2.height - min);
                    size = size2;
                }
            }
            if (size == null) {
                size = getClosestSize(list, calculateTargetRatioForPreview, null);
            }
            return size;
        }
        VideoProfile videoProfile = getVideoProfile();
        return new Size(videoProfile.videoFrameWidth, videoProfile.videoFrameHeight);
    }

    public Size getOptimalVideoPictureSize(List<Size> list, double d) {
        return getOptimalVideoPictureSize(list, d, this.video_quality_handler.getMaxSupportedVideoSize());
    }

    public static Size getOptimalVideoPictureSize(List<Size> list, double d, Size size) {
        Size size2 = null;
        if (list == null) {
            return null;
        }
        for (Size size3 : list) {
            double d2 = (double) size3.width;
            double d3 = (double) size3.height;
            Double.isNaN(d2);
            Double.isNaN(d3);
            if (Math.abs((d2 / d3) - d) <= 0.05d && size3.width <= size.width && size3.height <= size.height) {
                if (size2 == null || size3.width > size2.width) {
                    size2 = size3;
                }
            }
        }
        if (size2 == null) {
            size2 = getClosestSize(list, d, size);
        }
        return size2;
    }

    private void setAspectRatio(double d) {
        if (d > 0.0d) {
            this.has_aspect_ratio = true;
            if (this.aspect_ratio != d) {
                this.aspect_ratio = d;
                this.cameraSurface.getView().requestLayout();
                CanvasView canvasView2 = this.canvasView;
                if (canvasView2 != null) {
                    canvasView2.requestLayout();
                    return;
                }
                return;
            }
            return;
        }
        throw new IllegalArgumentException();
    }

    private boolean hasAspectRatio() {
        return this.has_aspect_ratio;
    }

    private double getAspectRatio() {
        return this.aspect_ratio;
    }

    public int getDisplayRotation() {
        int rotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation();
        if (!this.applicationInterface.getPreviewRotationPref().equals("180")) {
            return rotation;
        }
        if (rotation == 0) {
            return 2;
        }
        if (rotation == 1) {
            return 3;
        }
        if (rotation == 2) {
            return 0;
        }
        if (rotation != 3) {
            return rotation;
        }
        return 1;
    }

    /* access modifiers changed from: private */
    public int getDisplayRotationDegrees() {
        int displayRotation = getDisplayRotation();
        if (displayRotation == 0) {
            return 0;
        }
        if (displayRotation == 1) {
            return 90;
        }
        if (displayRotation != 2) {
            return displayRotation != 3 ? 0 : 270;
        }
        return 180;
    }

    public void setCameraDisplayOrientation() {
        if (this.camera_controller != null) {
            if (this.using_android_l) {
                configureTransform();
            } else {
                this.camera_controller.setDisplayOrientation(getDisplayRotationDegrees());
            }
        }
    }

    /* access modifiers changed from: private */
    public void onOrientationChanged(int i) {
        int i2;
        if (i != -1) {
            CameraController cameraController = this.camera_controller;
            if (cameraController != null) {
                int i3 = ((i + 45) / 90) * 90;
                this.current_orientation = i3 % 360;
                int cameraOrientation = cameraController.getCameraOrientation();
                if (this.camera_controller.isFrontFacing()) {
                    i2 = ((cameraOrientation - i3) + 360) % 360;
                } else {
                    i2 = (cameraOrientation + i3) % 360;
                }
                if (i2 != this.current_rotation) {
                    this.current_rotation = i2;
                }
            }
        }
    }

    private int getDeviceDefaultOrientation() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService("window");
        Configuration configuration = getResources().getConfiguration();
        int rotation = windowManager.getDefaultDisplay().getRotation();
        if (((rotation == 0 || rotation == 2) && configuration.orientation == 2) || ((rotation == 1 || rotation == 3) && configuration.orientation == 1)) {
            return 2;
        }
        return 1;
    }

    private int getImageVideoRotation() {
        String lockOrientationPref = this.applicationInterface.getLockOrientationPref();
        if (lockOrientationPref.equals("landscape")) {
            int cameraOrientation = this.camera_controller.getCameraOrientation();
            if (getDeviceDefaultOrientation() == 1) {
                if (this.camera_controller.isFrontFacing()) {
                    cameraOrientation = (cameraOrientation + 90) % 360;
                } else {
                    cameraOrientation = (cameraOrientation + 270) % 360;
                }
            }
            return cameraOrientation;
        } else if (!lockOrientationPref.equals("portrait")) {
            return this.current_rotation;
        } else {
            int cameraOrientation2 = this.camera_controller.getCameraOrientation();
            if (getDeviceDefaultOrientation() != 1) {
                if (this.camera_controller.isFrontFacing()) {
                    cameraOrientation2 = (cameraOrientation2 + 270) % 360;
                } else {
                    cameraOrientation2 = (cameraOrientation2 + 90) % 360;
                }
            }
            return cameraOrientation2;
        }
    }

    public void draw(Canvas canvas) {
        if (!this.app_is_paused) {
            if (!(this.focus_success == 3 || this.focus_complete_time == -1 || System.currentTimeMillis() <= this.focus_complete_time + min_safe_restart_video_time)) {
                this.focus_success = 3;
            }
            this.applicationInterface.onDrawPreview(canvas);
        }
    }

    public int getScaledZoomFactor(float f) {
        CameraController cameraController = this.camera_controller;
        if (cameraController == null || !this.has_zoom) {
            return 0;
        }
        int zoom = cameraController.getZoom();
        float intValue = (((float) ((Integer) this.zoom_ratios.get(zoom)).intValue()) / 100.0f) * f;
        if (intValue <= 1.0f) {
            return 0;
        }
        if (intValue >= ((float) ((Integer) this.zoom_ratios.get(this.max_zoom_factor)).intValue()) / 100.0f) {
            return this.max_zoom_factor;
        }
        if (f > 1.0f) {
            for (int i = zoom; i < this.zoom_ratios.size(); i++) {
                if (((float) ((Integer) this.zoom_ratios.get(i)).intValue()) / 100.0f >= intValue) {
                    return i;
                }
            }
        } else {
            for (int i2 = zoom; i2 >= 0; i2--) {
                if (((float) ((Integer) this.zoom_ratios.get(i2)).intValue()) / 100.0f <= intValue) {
                    return i2;
                }
            }
        }
        return zoom;
    }

    public void scaleZoom(float f) {
        if (this.camera_controller != null && this.has_zoom) {
            this.applicationInterface.multitouchZoom(getScaledZoomFactor(f));
        }
    }

    public void zoomTo(int i) {
        if (i < 0) {
            i = 0;
        } else {
            int i2 = this.max_zoom_factor;
            if (i > i2) {
                i = i2;
            }
        }
        CameraController cameraController = this.camera_controller;
        if (cameraController != null && this.has_zoom) {
            cameraController.setZoom(i);
            this.applicationInterface.setZoomPref(i);
            clearFocusAreas();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:30:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setFocusDistance(float r6, boolean r7) {
        /*
            r5 = this;
            net.sourceforge.opencamera.cameracontroller.CameraController r0 = r5.camera_controller
            if (r0 == 0) goto L_0x00a8
            r0 = 0
            int r1 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1))
            if (r1 >= 0) goto L_0x000b
            r6 = 0
            goto L_0x0012
        L_0x000b:
            float r1 = r5.minimum_focus_distance
            int r2 = (r6 > r1 ? 1 : (r6 == r1 ? 0 : -1))
            if (r2 <= 0) goto L_0x0012
            r6 = r1
        L_0x0012:
            r1 = 0
            r2 = 1
            if (r7 == 0) goto L_0x0022
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r5.camera_controller
            r1.setFocusBracketingTargetDistance(r6)
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r5.camera_controller
            r1.setFocusDistance(r6)
        L_0x0020:
            r1 = 1
            goto L_0x0030
        L_0x0022:
            net.sourceforge.opencamera.cameracontroller.CameraController r3 = r5.camera_controller
            boolean r3 = r3.setFocusDistance(r6)
            if (r3 == 0) goto L_0x0030
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r5.camera_controller
            r1.setFocusBracketingSourceDistance(r6)
            goto L_0x0020
        L_0x0030:
            if (r1 == 0) goto L_0x00a8
            net.sourceforge.opencamera.preview.ApplicationInterface r1 = r5.applicationInterface
            r1.setFocusDistancePref(r6, r7)
            int r0 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1))
            if (r0 <= 0) goto L_0x0060
            r0 = 1065353216(0x3f800000, float:1.0)
            float r0 = r0 / r6
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.text.DecimalFormat r1 = r5.decimal_format_2dp
            double r3 = (double) r0
            java.lang.String r0 = r1.format(r3)
            r6.append(r0)
            android.content.res.Resources r0 = r5.getResources()
            r1 = 2131493020(0x7f0c009c, float:1.8609508E38)
            java.lang.String r0 = r0.getString(r1)
            r6.append(r0)
            java.lang.String r6 = r6.toString()
            goto L_0x006b
        L_0x0060:
            android.content.res.Resources r6 = r5.getResources()
            r0 = 2131493009(0x7f0c0091, float:1.8609486E38)
            java.lang.String r6 = r6.getString(r0)
        L_0x006b:
            r0 = 2131492997(0x7f0c0085, float:1.8609462E38)
            boolean r1 = r5.supports_focus_bracketing
            if (r1 == 0) goto L_0x0089
            net.sourceforge.opencamera.preview.ApplicationInterface r1 = r5.applicationInterface
            boolean r1 = r1.isFocusBracketingPref()
            if (r1 == 0) goto L_0x0089
            if (r7 == 0) goto L_0x0083
            r7 = 2131492996(0x7f0c0084, float:1.860946E38)
            r0 = 2131492996(0x7f0c0084, float:1.860946E38)
            goto L_0x0089
        L_0x0083:
            r7 = 2131492995(0x7f0c0083, float:1.8609458E38)
            r0 = 2131492995(0x7f0c0083, float:1.8609458E38)
        L_0x0089:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            android.content.res.Resources r1 = r5.getResources()
            java.lang.String r0 = r1.getString(r0)
            r7.append(r0)
            java.lang.String r0 = " "
            r7.append(r0)
            r7.append(r6)
            java.lang.String r6 = r7.toString()
            r5.showToast(r6, r2)
        L_0x00a8:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.preview.Preview.setFocusDistance(float, boolean):void");
    }

    public void stoppedSettingFocusDistance(boolean z) {
        if (z) {
            CameraController cameraController = this.camera_controller;
            if (cameraController != null) {
                cameraController.setFocusDistance(cameraController.getFocusBracketingSourceDistance());
            }
        }
    }

    public void setExposure(int i) {
        if (this.camera_controller == null) {
            return;
        }
        if (this.min_exposure != 0 || this.max_exposure != 0) {
            cancelAutoFocus();
            int i2 = this.min_exposure;
            if (i >= i2) {
                i2 = this.max_exposure;
                if (i <= i2) {
                    i2 = i;
                }
            }
            if (this.camera_controller.setExposureCompensation(i2)) {
                this.applicationInterface.setExposureCompensationPref(i2);
                showToast(getExposureCompensationString(i2), 0, true);
            }
        }
    }

    public void setWhiteBalanceTemperature(int i) {
        CameraController cameraController = this.camera_controller;
        if (cameraController != null && cameraController.setWhiteBalanceTemperature(i)) {
            this.applicationInterface.setWhiteBalanceTemperaturePref(i);
            StringBuilder sb = new StringBuilder();
            sb.append(getResources().getString(C0316R.string.white_balance));
            sb.append(" ");
            sb.append(i);
            showToast(sb.toString(), 0, true);
        }
    }

    public int parseManualISOValue(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException unused) {
            return -1;
        }
    }

    public void setISO(int i) {
        if (this.camera_controller != null && this.supports_iso_range) {
            int i2 = this.min_iso;
            if (i >= i2) {
                i2 = this.max_iso;
                if (i <= i2) {
                    i2 = i;
                }
            }
            if (this.camera_controller.setISO(i2)) {
                ApplicationInterface applicationInterface2 = this.applicationInterface;
                StringBuilder sb = new StringBuilder();
                sb.append(BuildConfig.FLAVOR);
                sb.append(i2);
                applicationInterface2.setISOPref(sb.toString());
                showToast(getISOString(i2), 0, true);
            }
        }
    }

    public void setExposureTime(long j) {
        if (this.camera_controller != null && this.supports_exposure_time) {
            if (j < getMinimumExposureTime()) {
                j = getMinimumExposureTime();
            } else if (j > getMaximumExposureTime()) {
                j = getMaximumExposureTime();
            }
            if (this.camera_controller.setExposureTime(j)) {
                this.applicationInterface.setExposureTimePref(j);
                showToast(getExposureTimeString(j), 0, true);
            }
        }
    }

    public String getExposureCompensationString(int i) {
        float f = ((float) i) * this.exposure_step;
        StringBuilder sb = new StringBuilder();
        sb.append(getResources().getString(C0316R.string.exposure_compensation));
        sb.append(" ");
        sb.append(i > 0 ? "+" : BuildConfig.FLAVOR);
        sb.append(this.decimal_format_2dp.format((double) f));
        sb.append(" EV");
        return sb.toString();
    }

    public String getISOString(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append(getResources().getString(C0316R.string.iso));
        sb.append(" ");
        sb.append(i);
        return sb.toString();
    }

    public String getExposureTimeString(long j) {
        double d = (double) j;
        Double.isNaN(d);
        double d2 = d / 1.0E9d;
        if (j > 100000000) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.decimal_format_1dp.format(d2));
            sb.append(getResources().getString(C0316R.string.seconds_abbreviation));
            return sb.toString();
        }
        double d3 = 1.0d / d2;
        StringBuilder sb2 = new StringBuilder();
        sb2.append(" 1/");
        sb2.append((int) (d3 + 0.5d));
        sb2.append(getResources().getString(C0316R.string.seconds_abbreviation));
        return sb2.toString();
    }

    public String getFrameDurationString(long j) {
        double d = (double) j;
        Double.isNaN(d);
        double d2 = 1.0d / (d / 1.0E9d);
        StringBuilder sb = new StringBuilder();
        sb.append(getResources().getString(C0316R.string.fps));
        sb.append(" ");
        sb.append(this.decimal_format_1dp.format(d2));
        return sb.toString();
    }

    public boolean canSwitchCamera() {
        if (this.phase == 2 || isVideoRecording() || this.camera_controller_manager.getNumberOfCameras() == 0) {
            return false;
        }
        return true;
    }

    public void setCamera(final int i) {
        if (i < 0 || i >= this.camera_controller_manager.getNumberOfCameras()) {
            i = 0;
        }
        if (this.camera_open_state != CameraOpenState.CAMERAOPENSTATE_OPENING && canSwitchCamera()) {
            closeCamera(true, new CloseCameraCallback() {
                public void onClosed() {
                    Preview.this.applicationInterface.setCameraIdPref(i);
                    Preview.this.openCamera();
                }
            });
        }
    }

    public static int[] matchPreviewFpsToVideo(List<int[]> list, int i) {
        int i2 = -1;
        int i3 = -1;
        int i4 = -1;
        for (int[] iArr : list) {
            int i5 = iArr[0];
            int i6 = iArr[1];
            if (i5 <= i && i6 >= i) {
                int i7 = i6 - i5;
                if (i3 == -1 || i7 < i3) {
                    i4 = i6;
                    i3 = i7;
                    i2 = i5;
                }
            }
        }
        if (i2 == -1) {
            int i8 = -1;
            int i9 = -1;
            for (int[] iArr2 : list) {
                int i10 = iArr2[0];
                int i11 = iArr2[1];
                int i12 = i11 - i10;
                int i13 = i11 < i ? i - i11 : i10 - i;
                if (i8 == -1 || i13 < i8 || (i13 == i8 && i12 < i9)) {
                    i4 = i11;
                    i2 = i10;
                    i9 = i12;
                    i8 = i13;
                }
            }
        }
        return new int[]{i2, i4};
    }

    public static int[] chooseBestPreviewFps(List<int[]> list) {
        int i = -1;
        int i2 = -1;
        for (int[] iArr : list) {
            int i3 = iArr[0];
            int i4 = iArr[1];
            if (i4 >= 30000 && (i == -1 || i3 < i || (i3 == i && i4 > i2))) {
                i2 = i4;
                i = i3;
            }
        }
        if (i == -1) {
            int i5 = -1;
            for (int[] iArr2 : list) {
                int i6 = iArr2[0];
                int i7 = iArr2[1];
                int i8 = i7 - i6;
                if (i5 == -1 || i8 > i5 || (i8 == i5 && i7 > i2)) {
                    i2 = i7;
                    i = i6;
                    i5 = i8;
                }
            }
        }
        return new int[]{i, i2};
    }

    private void setPreviewFps() {
        int[] iArr;
        VideoProfile videoProfile = getVideoProfile();
        List supportedPreviewFpsRange = this.camera_controller.getSupportedPreviewFpsRange();
        if (supportedPreviewFpsRange != null && supportedPreviewFpsRange.size() != 0) {
            int[] iArr2 = null;
            if (this.is_video) {
                boolean z = this.using_android_l || Build.MODEL.equals("Nexus 5") || Build.MODEL.equals("Nexus 6");
                String videoFPSPref = this.applicationInterface.getVideoFPSPref();
                String str = "default";
                if (!videoFPSPref.equals(str) || !this.using_android_l) {
                    if (!videoFPSPref.equals(str) || !z) {
                        iArr = matchPreviewFpsToVideo(supportedPreviewFpsRange, (int) (videoProfile.videoCaptureRate * 1000.0d));
                    } else {
                        iArr = chooseBestPreviewFps(supportedPreviewFpsRange);
                    }
                    iArr2 = iArr;
                }
            } else if (!this.using_android_l) {
                iArr2 = chooseBestPreviewFps(supportedPreviewFpsRange);
            }
            if (iArr2 != null) {
                this.camera_controller.setPreviewFpsRange(iArr2[0], iArr2[1]);
            } else if (this.using_android_l) {
                this.camera_controller.clearPreviewFpsRange();
            }
        }
    }

    public void switchVideo(boolean z, boolean z2) {
        if (this.camera_controller != null) {
            if (this.is_video || this.supports_video) {
                boolean z3 = this.is_video;
                if (z3) {
                    if (this.video_recorder != null) {
                        stopVideo(false);
                    }
                    this.is_video = false;
                } else if (isOnTimer()) {
                    cancelTimer();
                    this.is_video = true;
                } else if (this.phase != 2) {
                    this.is_video = true;
                }
                if (this.is_video != z3) {
                    setFocusPref(false);
                    if (z2) {
                        this.applicationInterface.setVideoPref(this.is_video);
                    }
                    if (!z) {
                        updateFlashForVideo();
                    }
                    if (!z) {
                        int i = this.current_focus_index;
                        if (i != -1) {
                            String str = (String) this.supported_focus_values.get(i);
                        }
                        reopenCamera();
                    }
                    if (this.is_video && VERSION.SDK_INT >= 23 && this.applicationInterface.getRecordAudioPref() && ContextCompat.checkSelfPermission(getContext(), "android.permission.RECORD_AUDIO") != 0) {
                        this.applicationInterface.requestRecordAudioPermission();
                    }
                }
            }
        }
    }

    private boolean focusIsVideo() {
        CameraController cameraController = this.camera_controller;
        if (cameraController != null) {
            return cameraController.focusIsVideo();
        }
        return false;
    }

    private void setFocusPref(boolean z) {
        String focusPref = this.applicationInterface.getFocusPref(this.is_video);
        if (focusPref.length() <= 0) {
            updateFocus(this.is_video ? "focus_mode_continuous_video" : "focus_mode_continuous_picture", true, true, z);
        } else if (!updateFocus(focusPref, true, false, z)) {
            updateFocus(0, true, true, z);
        }
    }

    private String updateFocusForVideo() {
        if (this.supported_focus_values == null || this.camera_controller == null || !this.is_video || focusIsVideo() == this.is_video) {
            return null;
        }
        String currentFocusValue = getCurrentFocusValue();
        updateFocus("focus_mode_continuous_video", true, false, false);
        return currentFocusValue;
    }

    private void updateFlashForVideo() {
        if (this.is_video) {
            String currentFlashValue = getCurrentFlashValue();
            if (currentFlashValue != null && !isFlashSupportedForVideo(currentFlashValue)) {
                this.current_flash_index = -1;
                updateFlash("flash_off", false);
            }
        }
    }

    public static boolean isFlashSupportedForVideo(String str) {
        return str != null && (str.equals("flash_off") || str.equals("flash_torch") || str.equals("flash_frontscreen_torch"));
    }

    public String getErrorFeatures(VideoProfile videoProfile) {
        String str;
        boolean z = false;
        boolean z2 = true;
        boolean z3 = videoProfile.videoFrameWidth == 3840 && videoProfile.videoFrameHeight == 2160 && this.applicationInterface.getForce4KPref();
        String str2 = "default";
        boolean z4 = !this.applicationInterface.getVideoBitratePref().equals(str2);
        String videoFPSPref = this.applicationInterface.getVideoFPSPref();
        if (this.applicationInterface.getVideoCaptureRateFactor() >= 0.99999f) {
            if (!videoFPSPref.equals(str2)) {
                z = true;
            }
            z2 = false;
        }
        String str3 = BuildConfig.FLAVOR;
        if (!z3 && !z4 && !z && !z2) {
            return str3;
        }
        if (z3) {
            str3 = "4K UHD";
        }
        if (z4) {
            if (str3.length() == 0) {
                str3 = "Bitrate";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(str3);
                sb.append("/Bitrate");
                str3 = sb.toString();
            }
        }
        if (z) {
            if (str3.length() == 0) {
                str = "Frame rate";
            } else {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(str3);
                sb2.append("/Frame rate");
                str = sb2.toString();
            }
            str3 = str;
        }
        if (!z2) {
            return str3;
        }
        if (str3.length() == 0) {
            return "Slow motion";
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append(str3);
        sb3.append("/Slow motion");
        return sb3.toString();
    }

    public void updateFlash(String str) {
        if (this.phase != 2 || this.is_video) {
            updateFlash(str, true);
        }
    }

    private boolean updateFlash(String str, boolean z) {
        List<String> list = this.supported_flash_values;
        if (list != null) {
            int indexOf = list.indexOf(str);
            if (indexOf != -1) {
                updateFlash(indexOf, z);
                return true;
            }
        }
        return false;
    }

    public void cycleFlash(boolean z, boolean z2) {
        List<String> list = this.supported_flash_values;
        if (list != null) {
            int size = (this.current_flash_index + 1) % list.size();
            if (((String) this.supported_flash_values.get(size)).equals("flash_torch")) {
                size = (size + 1) % this.supported_flash_values.size();
            }
            updateFlash(size, z2);
        }
    }

    private void updateFlash(int i, boolean z) {
        if (this.supported_flash_values != null) {
            int i2 = this.current_flash_index;
            if (i != i2) {
                int i3 = 0;
                boolean z2 = i2 == -1;
                this.current_flash_index = i;
                String[] stringArray = getResources().getStringArray(C0316R.array.flash_entries);
                String str = (String) this.supported_flash_values.get(this.current_flash_index);
                String[] stringArray2 = getResources().getStringArray(C0316R.array.flash_values);
                while (true) {
                    if (i3 >= stringArray2.length) {
                        break;
                    } else if (!str.equals(stringArray2[i3])) {
                        i3++;
                    } else if (!z2) {
                        showToast(this.flash_toast, stringArray[i3]);
                    }
                }
                setFlash(str);
                if (z) {
                    this.applicationInterface.setFlashPref(str);
                }
            }
        }
    }

    private void setFlash(String str) {
        this.set_flash_value_after_autofocus = BuildConfig.FLAVOR;
        if (this.camera_controller != null) {
            cancelAutoFocus();
            this.camera_controller.setFlashValue(str);
        }
    }

    public String getCurrentFlashValue() {
        int i = this.current_flash_index;
        if (i == -1) {
            return null;
        }
        return (String) this.supported_flash_values.get(i);
    }

    public void updateFocus(String str, boolean z, boolean z2) {
        if (this.phase != 2) {
            updateFocus(str, z, true, z2);
        }
    }

    private boolean supportedFocusValue(String str) {
        List<String> list = this.supported_focus_values;
        if (list == null || list.indexOf(str) == -1) {
            return false;
        }
        return true;
    }

    private boolean updateFocus(String str, boolean z, boolean z2, boolean z3) {
        List<String> list = this.supported_focus_values;
        if (list != null) {
            int indexOf = list.indexOf(str);
            if (indexOf != -1) {
                updateFocus(indexOf, z, z2, z3);
                return true;
            }
        }
        return false;
    }

    private String findEntryForValue(String str, int i, int i2) {
        String[] stringArray = getResources().getStringArray(i);
        String[] stringArray2 = getResources().getStringArray(i2);
        for (int i3 = 0; i3 < stringArray2.length; i3++) {
            if (str.equals(stringArray2[i3])) {
                return stringArray[i3];
            }
        }
        return null;
    }

    public String findFocusEntryForValue(String str) {
        return findEntryForValue(str, C0316R.array.focus_mode_entries, C0316R.array.focus_mode_values);
    }

    private void updateFocus(int i, boolean z, boolean z2, boolean z3) {
        List<String> list = this.supported_focus_values;
        if (list != null && i != this.current_focus_index) {
            this.current_focus_index = i;
            String str = (String) list.get(this.current_focus_index);
            if (!z) {
                String findFocusEntryForValue = findFocusEntryForValue(str);
                if (findFocusEntryForValue != null) {
                    showToast(this.focus_toast, findFocusEntryForValue);
                }
            }
            setFocusValue(str, z3);
            if (z2) {
                this.applicationInterface.setFocusPref(str, this.is_video);
            }
        }
    }

    public String getCurrentFocusValue() {
        if (this.camera_controller == null) {
            return null;
        }
        List<String> list = this.supported_focus_values;
        if (list != null) {
            int i = this.current_focus_index;
            if (i != -1) {
                return (String) list.get(i);
            }
        }
        return null;
    }

    private void setFocusValue(String str, boolean z) {
        if (this.camera_controller != null) {
            cancelAutoFocus();
            removePendingContinuousFocusReset();
            this.autofocus_in_continuous_mode = false;
            this.camera_controller.setFocusValue(str);
            setupContinuousFocusMove();
            clearFocusAreas();
            if (z && !str.equals("focus_mode_locked")) {
                tryAutoFocus(false, false);
            }
        }
    }

    private void setupContinuousFocusMove() {
        if (this.continuous_focus_move_is_started) {
            this.continuous_focus_move_is_started = false;
            this.applicationInterface.onContinuousFocusMove(false);
        }
        int i = this.current_focus_index;
        String str = i != -1 ? (String) this.supported_focus_values.get(i) : null;
        if (this.camera_controller == null || str == null || !str.equals("focus_mode_continuous_picture") || this.is_video) {
            CameraController cameraController = this.camera_controller;
            if (cameraController != null) {
                cameraController.setContinuousFocusMoveCallback(null);
                return;
            }
            return;
        }
        this.camera_controller.setContinuousFocusMoveCallback(new ContinuousFocusMoveCallback() {
            public void onContinuousFocusMove(boolean z) {
                if (z != Preview.this.continuous_focus_move_is_started) {
                    Preview.this.continuous_focus_move_is_started = z;
                    Preview.this.count_cameraContinuousFocusMoving++;
                    Preview.this.applicationInterface.onContinuousFocusMove(z);
                }
            }
        });
    }

    public void toggleWhiteBalanceLock() {
        if (!(this.phase == 2 || this.camera_controller == null || !this.is_white_balance_lock_supported)) {
            this.is_white_balance_locked = !this.is_white_balance_locked;
            cancelAutoFocus();
            this.camera_controller.setAutoWhiteBalanceLock(this.is_white_balance_locked);
        }
    }

    public void toggleExposureLock() {
        if (!(this.phase == 2 || this.camera_controller == null || !this.is_exposure_lock_supported)) {
            this.is_exposure_locked = !this.is_exposure_locked;
            cancelAutoFocus();
            this.camera_controller.setAutoExposureLock(this.is_exposure_locked);
        }
    }

    public void takePicturePressed(boolean z, boolean z2) {
        int i;
        if (this.camera_controller == null) {
            this.phase = 0;
        } else if (!this.has_surface) {
            this.phase = 0;
        } else if (this.is_video && z2) {
            Log.e(TAG, "continuous_fast_burst not supported for video mode");
            this.phase = 0;
        } else if (isOnTimer()) {
            cancelTimer();
            showToast(this.take_photo_toast, (int) C0316R.string.cancelled_timer);
        } else if (this.is_video && isVideoRecording() && !z) {
            if (this.video_start_time_set && System.currentTimeMillis() - this.video_start_time >= 500) {
                stopVideo(false);
            }
        } else if ((!this.is_video || z) && this.phase == 2) {
            if (this.remaining_repeat_photos != 0) {
                cancelRepeat();
                showToast(this.take_photo_toast, (int) C0316R.string.cancelled_repeat_mode);
            } else if (!this.is_video && this.camera_controller.getBurstType() == BurstType.BURSTTYPE_FOCUS && this.camera_controller.isCapturingBurst()) {
                this.camera_controller.stopFocusBracketingBurst();
                showToast(this.take_photo_toast, (int) C0316R.string.cancelled_focus_bracketing);
            }
        } else if ((this.is_video && !z) || this.applicationInterface.canTakeNewPhoto()) {
            startCameraPreview();
            if (z || z2) {
                takePicture(false, z, z2);
                return;
            }
            long timerPref = this.applicationInterface.getTimerPref();
            String repeatPref = this.applicationInterface.getRepeatPref();
            if (repeatPref.equals("unlimited")) {
                this.remaining_repeat_photos = -1;
            } else {
                try {
                    i = Integer.parseInt(repeatPref);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    i = 1;
                }
                this.remaining_repeat_photos = i - 1;
            }
            if (timerPref == 0) {
                takePicture(false, z, z2);
            } else {
                takePictureOnTimer(timerPref, false);
            }
        }
    }

    private void takePictureOnTimer(final long j, boolean z) {
        this.phase = 1;
        this.take_photo_time = System.currentTimeMillis() + j;
        Timer timer = this.takePictureTimer;
        AnonymousClass1TakePictureTimerTask r0 = new TimerTask() {
            public void run() {
                if (Preview.this.beepTimerTask != null) {
                    Preview.this.beepTimerTask.cancel();
                    Preview.this.beepTimerTask = null;
                }
                ((Activity) Preview.this.getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        if (Preview.this.camera_controller != null && Preview.this.takePictureTimerTask != null) {
                            Preview.this.takePicture(false, false, false);
                        }
                    }
                });
            }
        };
        this.takePictureTimerTask = r0;
        timer.schedule(r0, j);
        Timer timer2 = this.beepTimer;
        AnonymousClass1BeepTimerTask r2 = new TimerTask() {
            long remaining_time = j;

            public void run() {
                if (this.remaining_time > 0) {
                    Preview.this.applicationInterface.timerBeep(this.remaining_time);
                }
                this.remaining_time -= Preview.min_safe_restart_video_time;
            }
        };
        this.beepTimerTask = r2;
        timer2.schedule(r2, 0, min_safe_restart_video_time);
    }

    /* access modifiers changed from: private */
    public void flashVideo() {
        String flashValue = this.camera_controller.getFlashValue();
        if (flashValue.length() != 0) {
            String currentFlashValue = getCurrentFlashValue();
            if (currentFlashValue != null) {
                String str = "flash_torch";
                if (!currentFlashValue.equals(str)) {
                    if (flashValue.equals(str)) {
                        cancelAutoFocus();
                        this.camera_controller.setFlashValue(currentFlashValue);
                        return;
                    }
                    cancelAutoFocus();
                    this.camera_controller.setFlashValue(str);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    cancelAutoFocus();
                    this.camera_controller.setFlashValue(currentFlashValue);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onVideoInfo(int i, int i2) {
        boolean z = false;
        if (VERSION.SDK_INT < 26 || i != 802 || !this.video_restart_on_max_filesize) {
            if (i == 801 && this.video_restart_on_max_filesize) {
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        if (Preview.this.camera_controller != null) {
                            Preview.this.restartVideo(true);
                        }
                    }
                });
            } else if (i == 800) {
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        if (Preview.this.camera_controller != null) {
                            Preview.this.restartVideo(false);
                        }
                    }
                });
            } else if (i == 801) {
                stopVideo(false);
            }
        } else if (this.video_recorder != null && this.applicationInterface.getVideoMaxDurationPref() <= 0) {
            try {
                this.applicationInterface.getVideoMaxFileSizePref();
                z = true;
            } catch (NoFreeStorageException unused) {
            }
            VideoProfile videoProfile = getVideoProfile();
            if (!videoProfile.fileExtension.equals("3gp") && z) {
                VideoFileInfo createVideoFile = createVideoFile(videoProfile.fileExtension);
                if (createVideoFile != null) {
                    try {
                        if (createVideoFile.video_method == 0) {
                            this.video_recorder.setNextOutputFile(new File(createVideoFile.video_filename));
                        } else {
                            this.video_recorder.setNextOutputFile(createVideoFile.video_pfd_saf.getFileDescriptor());
                        }
                        this.videoFileInfo = createVideoFile;
                    } catch (IOException e) {
                        Log.e(TAG, "failed to setNextOutputFile");
                        e.printStackTrace();
                    }
                }
            }
        }
        this.applicationInterface.onVideoInfo(i, i2);
    }

    /* access modifiers changed from: private */
    public void onVideoError(int i, int i2) {
        stopVideo(false);
        this.applicationInterface.onVideoError(i, i2);
    }

    /* access modifiers changed from: private */
    public void takePicture(boolean z, boolean z2, boolean z3) {
        if (!this.is_video || z2) {
            this.phase = 2;
        } else if (this.phase == 1) {
            this.phase = 0;
        }
        synchronized (this) {
            this.take_photo_after_autofocus = false;
        }
        if (this.camera_controller == null) {
            this.phase = 0;
            this.applicationInterface.cameraInOperation(false, false);
            if (this.is_video) {
                this.applicationInterface.cameraInOperation(false, true);
            }
        } else if (!this.has_surface) {
            this.phase = 0;
            this.applicationInterface.cameraInOperation(false, false);
            if (this.is_video) {
                this.applicationInterface.cameraInOperation(false, true);
            }
        } else if (this.applicationInterface.getGeotaggingPref() && this.applicationInterface.getRequireLocationPref() && this.applicationInterface.getLocation() == null) {
            showToast((ToastBoxer) null, (int) C0316R.string.location_not_available);
            if (!this.is_video || z2) {
                this.phase = 0;
            }
            this.applicationInterface.cameraInOperation(false, false);
            if (this.is_video) {
                this.applicationInterface.cameraInOperation(false, true);
            }
        } else if (!this.is_video || z2) {
            takePhoto(false, z3);
        } else {
            startVideoRecording(z);
        }
    }

    private VideoFileInfo createVideoFile(String str) {
        Uri uri;
        ParcelFileDescriptor parcelFileDescriptor;
        String str2;
        try {
            int createOutputVideoMethod = this.applicationInterface.createOutputVideoMethod();
            if (createOutputVideoMethod == 0) {
                str2 = this.applicationInterface.createOutputVideoFile(str).getAbsolutePath();
                uri = null;
                parcelFileDescriptor = null;
            } else {
                if (createOutputVideoMethod == 1) {
                    uri = this.applicationInterface.createOutputVideoSAF(str);
                } else {
                    uri = this.applicationInterface.createOutputVideoUri();
                }
                parcelFileDescriptor = getContext().getContentResolver().openFileDescriptor(uri, "rw");
                str2 = null;
            }
            return new VideoFileInfo(createOutputVideoMethod, uri, str2, parcelFileDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x0138  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0162  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startVideoRecording(boolean r13) {
        /*
            r12 = this;
            r0 = 3
            r12.focus_success = r0
            net.sourceforge.opencamera.preview.VideoProfile r0 = r12.getVideoProfile()
            java.lang.String r1 = r0.fileExtension
            net.sourceforge.opencamera.preview.Preview$VideoFileInfo r1 = r12.createVideoFile(r1)
            r2 = 0
            r3 = 1
            if (r1 != 0) goto L_0x0024
            net.sourceforge.opencamera.preview.Preview$VideoFileInfo r13 = new net.sourceforge.opencamera.preview.Preview$VideoFileInfo
            r13.<init>()
            r12.videoFileInfo = r13
            net.sourceforge.opencamera.preview.ApplicationInterface r13 = r12.applicationInterface
            r13.onFailedCreateVideoFileError()
            net.sourceforge.opencamera.preview.ApplicationInterface r13 = r12.applicationInterface
            r13.cameraInOperation(r2, r3)
            goto L_0x0194
        L_0x0024:
            r12.videoFileInfo = r1
            net.sourceforge.opencamera.preview.ApplicationInterface r1 = r12.applicationInterface
            boolean r1 = r1.getShutterSoundPref()
            net.sourceforge.opencamera.cameracontroller.CameraController r4 = r12.camera_controller
            r4.enableShutterSound(r1)
            android.media.MediaRecorder r1 = new android.media.MediaRecorder
            r1.<init>()
            net.sourceforge.opencamera.cameracontroller.CameraController r4 = r12.camera_controller
            r4.unlock()
            net.sourceforge.opencamera.preview.Preview$12 r4 = new net.sourceforge.opencamera.preview.Preview$12
            r4.<init>()
            r1.setOnInfoListener(r4)
            net.sourceforge.opencamera.preview.Preview$13 r4 = new net.sourceforge.opencamera.preview.Preview$13
            r4.<init>()
            r1.setOnErrorListener(r4)
            net.sourceforge.opencamera.cameracontroller.CameraController r4 = r12.camera_controller
            r4.initVideoRecorderPrePrepare(r1)
            boolean r4 = r0.no_audio_permission
            r5 = 0
            if (r4 == 0) goto L_0x005b
            r4 = 2131493046(0x7f0c00b6, float:1.8609561E38)
            r12.showToast(r5, r4)
        L_0x005b:
            net.sourceforge.opencamera.preview.ApplicationInterface r4 = r12.applicationInterface
            boolean r4 = r4.getGeotaggingPref()
            if (r4 == 0) goto L_0x007e
            net.sourceforge.opencamera.preview.ApplicationInterface r4 = r12.applicationInterface
            android.location.Location r4 = r4.getLocation()
            if (r4 == 0) goto L_0x007e
            net.sourceforge.opencamera.preview.ApplicationInterface r4 = r12.applicationInterface
            android.location.Location r4 = r4.getLocation()
            double r6 = r4.getLatitude()
            float r6 = (float) r6
            double r7 = r4.getLongitude()
            float r4 = (float) r7
            r1.setLocation(r6, r4)
        L_0x007e:
            r0.copyToMediaRecorder(r1)
            net.sourceforge.opencamera.preview.ApplicationInterface r4 = r12.applicationInterface     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            net.sourceforge.opencamera.preview.ApplicationInterface$VideoMaxFileSize r4 = r4.getVideoMaxFileSizePref()     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            long r6 = r4.max_filesize     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            r8 = 0
            int r10 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1))
            if (r10 <= 0) goto L_0x0097
            r1.setMaxFileSize(r6)     // Catch:{ RuntimeException -> 0x0093 }
            goto L_0x0097
        L_0x0093:
            r6 = move-exception
            r6.printStackTrace()     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
        L_0x0097:
            boolean r4 = r4.auto_restart     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            r12.video_restart_on_max_filesize = r4     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            net.sourceforge.opencamera.preview.ApplicationInterface r4 = r12.applicationInterface     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            long r6 = r4.getVideoMaxDurationPref()     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            r10 = 1000(0x3e8, double:4.94E-321)
            if (r13 == 0) goto L_0x00b2
            int r4 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1))
            if (r4 <= 0) goto L_0x00b4
            long r8 = r12.video_accumulated_time     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            long r6 = r6 - r8
            int r4 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1))
            if (r4 >= 0) goto L_0x00b4
            r6 = r10
            goto L_0x00b4
        L_0x00b2:
            r12.video_accumulated_time = r8     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
        L_0x00b4:
            int r4 = (int) r6     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            r1.setMaxDuration(r4)     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            net.sourceforge.opencamera.preview.Preview$VideoFileInfo r4 = r12.videoFileInfo     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            int r4 = r4.video_method     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            if (r4 != 0) goto L_0x00ca
            net.sourceforge.opencamera.preview.Preview$VideoFileInfo r4 = r12.videoFileInfo     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            java.lang.String r4 = r4.video_filename     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            r1.setOutputFile(r4)     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            goto L_0x00d7
        L_0x00ca:
            net.sourceforge.opencamera.preview.Preview$VideoFileInfo r4 = r12.videoFileInfo     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            android.os.ParcelFileDescriptor r4 = r4.video_pfd_saf     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            java.io.FileDescriptor r4 = r4.getFileDescriptor()     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            r1.setOutputFile(r4)     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
        L_0x00d7:
            net.sourceforge.opencamera.preview.ApplicationInterface r4 = r12.applicationInterface     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            r4.cameraInOperation(r3, r3)     // Catch:{ IOException -> 0x016b, CameraControllerException -> 0x015a, NoFreeStorageException -> 0x012f }
            net.sourceforge.opencamera.preview.ApplicationInterface r4 = r12.applicationInterface     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            r4.startingVideo()     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            net.sourceforge.opencamera.preview.camerasurface.CameraSurface r4 = r12.cameraSurface     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            r4.setVideoRecorder(r1)     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            int r4 = r12.getImageVideoRotation()     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            r1.setOrientationHint(r4)     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            r1.prepare()     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            boolean r4 = r12.supportsPhotoVideoRecording()     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            if (r4 == 0) goto L_0x0100
            net.sourceforge.opencamera.preview.ApplicationInterface r4 = r12.applicationInterface     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            boolean r4 = r4.usePhotoVideoRecording()     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            if (r4 == 0) goto L_0x0100
            r4 = 1
            goto L_0x0101
        L_0x0100:
            r4 = 0
        L_0x0101:
            net.sourceforge.opencamera.cameracontroller.CameraController r6 = r12.camera_controller     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            r6.initVideoRecorderPostPrepare(r1, r4)     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            r1.start()     // Catch:{ RuntimeException -> 0x0110 }
            r12.video_recorder = r1     // Catch:{ RuntimeException -> 0x0110 }
            r12.videoRecordingStarted(r13)     // Catch:{ RuntimeException -> 0x0110 }
            goto L_0x0194
        L_0x0110:
            r13 = move-exception
            java.lang.String r4 = "Preview"
            java.lang.String r6 = "runtime exception starting video recorder"
            android.util.Log.e(r4, r6)     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            r13.printStackTrace()     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            r12.video_recorder = r1     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            net.sourceforge.opencamera.preview.ApplicationInterface r13 = r12.applicationInterface     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            r13.stoppingVideo()     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            r12.failedToStartVideoRecorder(r0)     // Catch:{ IOException -> 0x012c, CameraControllerException -> 0x0129, NoFreeStorageException -> 0x0126 }
            goto L_0x0194
        L_0x0126:
            r13 = move-exception
            r0 = 1
            goto L_0x0131
        L_0x0129:
            r13 = move-exception
            r2 = 1
            goto L_0x015b
        L_0x012c:
            r13 = move-exception
            r0 = 1
            goto L_0x016d
        L_0x012f:
            r13 = move-exception
            r0 = 0
        L_0x0131:
            r13.printStackTrace()
            r12.video_recorder = r1
            if (r0 == 0) goto L_0x013d
            net.sourceforge.opencamera.preview.ApplicationInterface r13 = r12.applicationInterface
            r13.stoppingVideo()
        L_0x013d:
            android.media.MediaRecorder r13 = r12.video_recorder
            r13.reset()
            android.media.MediaRecorder r13 = r12.video_recorder
            r13.release()
            r12.video_recorder = r5
            r12.video_recorder_is_paused = r2
            net.sourceforge.opencamera.preview.ApplicationInterface r13 = r12.applicationInterface
            r13.cameraInOperation(r2, r3)
            r12.reconnectCamera(r3)
            r13 = 2131493616(0x7f0c02f0, float:1.8610717E38)
            r12.showToast(r5, r13)
            goto L_0x0194
        L_0x015a:
            r13 = move-exception
        L_0x015b:
            r13.printStackTrace()
            r12.video_recorder = r1
            if (r2 == 0) goto L_0x0167
            net.sourceforge.opencamera.preview.ApplicationInterface r13 = r12.applicationInterface
            r13.stoppingVideo()
        L_0x0167:
            r12.failedToStartVideoRecorder(r0)
            goto L_0x0194
        L_0x016b:
            r13 = move-exception
            r0 = 0
        L_0x016d:
            r13.printStackTrace()
            r12.video_recorder = r1
            if (r0 == 0) goto L_0x0179
            net.sourceforge.opencamera.preview.ApplicationInterface r13 = r12.applicationInterface
            r13.stoppingVideo()
        L_0x0179:
            net.sourceforge.opencamera.preview.ApplicationInterface r13 = r12.applicationInterface
            r13.onFailedCreateVideoFileError()
            android.media.MediaRecorder r13 = r12.video_recorder
            r13.reset()
            android.media.MediaRecorder r13 = r12.video_recorder
            r13.release()
            r12.video_recorder = r5
            r12.video_recorder_is_paused = r2
            net.sourceforge.opencamera.preview.ApplicationInterface r13 = r12.applicationInterface
            r13.cameraInOperation(r2, r3)
            r12.reconnectCamera(r3)
        L_0x0194:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.preview.Preview.startVideoRecording(boolean):void");
    }

    private void videoRecordingStarted(boolean z) {
        this.video_recorder_is_paused = false;
        if (this.using_face_detection && !this.using_android_l) {
            this.camera_controller.startFaceDetection();
            this.faces_detected = null;
        }
        if (!this.test_video_failure) {
            this.video_start_time = System.currentTimeMillis();
            this.video_start_time_set = true;
            this.applicationInterface.startedVideo();
            if (this.remaining_restart_video == 0 && !z) {
                this.remaining_restart_video = this.applicationInterface.getVideoRestartTimesPref();
            }
            if (this.applicationInterface.getVideoFlashPref() && supportsFlash()) {
                Timer timer = this.flashVideoTimer;
                AnonymousClass1FlashVideoTimerTask r1 = new TimerTask() {
                    public void run() {
                        ((Activity) Preview.this.getContext()).runOnUiThread(new Runnable() {
                            public void run() {
                                if (Preview.this.camera_controller != null && Preview.this.flashVideoTimerTask != null) {
                                    Preview.this.flashVideo();
                                }
                            }
                        });
                    }
                };
                this.flashVideoTimerTask = r1;
                timer.schedule(r1, 0, min_safe_restart_video_time);
            }
            if (this.applicationInterface.getVideoLowPowerCheckPref()) {
                Timer timer2 = this.batteryCheckVideoTimer;
                AnonymousClass1BatteryCheckVideoTimerTask r12 = new TimerTask() {
                    public void run() {
                        Intent registerReceiver = Preview.this.getContext().registerReceiver(null, Preview.this.battery_ifilter);
                        double intExtra = (double) registerReceiver.getIntExtra("level", -1);
                        double intExtra2 = (double) registerReceiver.getIntExtra("scale", -1);
                        Double.isNaN(intExtra);
                        Double.isNaN(intExtra2);
                        if (intExtra / intExtra2 <= 0.03d) {
                            ((Activity) Preview.this.getContext()).runOnUiThread(new Runnable() {
                                public void run() {
                                    if (Preview.this.camera_controller != null && Preview.this.batteryCheckVideoTimerTask != null) {
                                        Preview.this.stopVideo(false);
                                        Preview.this.showToast((ToastBoxer) null, Preview.this.getContext().getResources().getString(C0316R.string.video_power_critical));
                                    }
                                }
                            });
                        }
                    }
                };
                this.batteryCheckVideoTimerTask = r12;
                timer2.schedule(r12, 60000, 60000);
                return;
            }
            return;
        }
        throw new RuntimeException();
    }

    private void failedToStartVideoRecorder(VideoProfile videoProfile) {
        this.applicationInterface.onVideoRecordStartError(videoProfile);
        this.video_recorder.reset();
        this.video_recorder.release();
        this.video_recorder = null;
        this.video_recorder_is_paused = false;
        this.applicationInterface.cameraInOperation(false, true);
        reconnectCamera(true);
    }

    public void pauseVideo() {
        int i = VERSION.SDK_INT;
        String str = TAG;
        if (i < 24) {
            Log.e(str, "pauseVideo called but requires Android N");
        } else if (!isVideoRecording()) {
            Log.e(str, "pauseVideo called but not video recording");
        } else if (this.video_recorder_is_paused) {
            this.video_recorder.resume();
            this.video_recorder_is_paused = false;
            this.video_start_time = System.currentTimeMillis();
            showToast(this.pause_video_toast, (int) C0316R.string.video_resume);
        } else {
            this.video_recorder.pause();
            this.video_recorder_is_paused = true;
            this.video_accumulated_time += System.currentTimeMillis() - this.video_start_time;
            showToast(this.pause_video_toast, (int) C0316R.string.video_pause);
        }
    }

    private void takePhoto(boolean z, final boolean z2) {
        if (this.camera_controller == null) {
            Log.e(TAG, "camera not opened in takePhoto!");
            return;
        }
        boolean z3 = false;
        this.applicationInterface.cameraInOperation(true, false);
        String currentFocusValue = getCurrentFocusValue();
        if (this.autofocus_in_continuous_mode) {
            synchronized (this) {
                if (this.focus_success == 0) {
                    this.take_photo_after_autofocus = true;
                    z3 = true;
                }
            }
            if (z3) {
                this.camera_controller.setCaptureFollowAutofocusHint(true);
            } else {
                takePhotoWhenFocused(z2);
            }
        } else if (this.camera_controller.focusIsContinuous()) {
            this.camera_controller.autoFocus(new AutoFocusCallback() {
                public void onAutoFocus(boolean z) {
                    Preview.this.takePhotoWhenFocused(z2);
                }
            }, true);
        } else if (z || recentlyFocused()) {
            takePhotoWhenFocused(z2);
        } else if (currentFocusValue == null || (!currentFocusValue.equals("focus_mode_auto") && !currentFocusValue.equals("focus_mode_macro"))) {
            takePhotoWhenFocused(z2);
        } else {
            synchronized (this) {
                if (this.focus_success == 0) {
                    this.take_photo_after_autofocus = true;
                    z3 = true;
                } else {
                    this.focus_success = 3;
                }
            }
            if (z3) {
                this.camera_controller.setCaptureFollowAutofocusHint(true);
            } else {
                this.camera_controller.autoFocus(new AutoFocusCallback() {
                    public void onAutoFocus(boolean z) {
                        Preview.this.ensureFlashCorrect();
                        Preview.this.prepareAutoFocusPhoto();
                        Preview.this.takePhotoWhenFocused(z2);
                    }
                }, true);
                this.count_cameraAutoFocus++;
            }
        }
    }

    /* access modifiers changed from: private */
    public void prepareAutoFocusPhoto() {
        if (this.using_android_l) {
            String flashValue = this.camera_controller.getFlashValue();
            if (flashValue.length() <= 0) {
                return;
            }
            if (flashValue.equals("flash_auto") || flashValue.equals("flash_red_eye")) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void takePhotoWhenFocused(boolean z) {
        boolean z2 = false;
        if (this.camera_controller == null) {
            this.phase = 0;
            this.applicationInterface.cameraInOperation(false, false);
        } else if (!this.has_surface) {
            this.phase = 0;
            this.applicationInterface.cameraInOperation(false, false);
        } else {
            int i = this.current_focus_index;
            final String str = i != -1 ? (String) this.supported_focus_values.get(i) : null;
            if (str != null && str.equals("focus_mode_locked") && this.focus_success == 0) {
                cancelAutoFocus();
            }
            removePendingContinuousFocusReset();
            updateParametersFromLocation();
            this.focus_success = 3;
            this.successfully_focused = false;
            C034516 r2 = new PictureCallback() {
                private Date current_date = null;
                private boolean has_date = false;
                private boolean success = false;

                public void onStarted() {
                    Preview.this.applicationInterface.onCaptureStarted();
                    if (Preview.this.applicationInterface.getBurstForNoiseReduction() && Preview.this.applicationInterface.getNRModePref() == NRModePref.NRMODE_LOW_LIGHT && Preview.this.camera_controller.getBurstTotal() >= 15) {
                        Preview.this.showToast((ToastBoxer) null, (int) C0316R.string.preference_nr_mode_low_light_message);
                    }
                }

                public void onCompleted() {
                    Preview.this.applicationInterface.onPictureCompleted();
                    if (!Preview.this.using_android_l) {
                        Preview.this.is_preview_started = false;
                    }
                    Preview.this.phase = 0;
                    if (Preview.this.remaining_repeat_photos == -1 || Preview.this.remaining_repeat_photos > 0) {
                        if (!Preview.this.is_preview_started) {
                            Preview.this.startCameraPreview();
                        }
                        Preview.this.applicationInterface.cameraInOperation(false, false);
                    } else {
                        Preview.this.phase = 0;
                        if (!Preview.this.applicationInterface.getPausePreviewPref() || !this.success) {
                            if (!Preview.this.is_preview_started) {
                                Preview.this.startCameraPreview();
                            }
                            Preview.this.applicationInterface.cameraInOperation(false, false);
                        } else {
                            if (Preview.this.is_preview_started) {
                                if (Preview.this.camera_controller != null) {
                                    Preview.this.camera_controller.stopPreview();
                                }
                                Preview.this.is_preview_started = false;
                            }
                            Preview.this.setPreviewPaused(true);
                        }
                    }
                    Preview.this.continuousFocusReset();
                    if (Preview.this.camera_controller != null) {
                        String str = str;
                        if (str != null && (str.equals("focus_mode_continuous_picture") || str.equals("focus_mode_continuous_video"))) {
                            Preview.this.camera_controller.cancelAutoFocus();
                        }
                    }
                    if (Preview.this.camera_controller != null && Preview.this.camera_controller.getBurstType() == BurstType.BURSTTYPE_CONTINUOUS) {
                        Preview.this.setupBurstMode();
                    }
                    if (Preview.this.remaining_repeat_photos == -1 || Preview.this.remaining_repeat_photos > 0) {
                        Preview.this.takeRemainingRepeatPhotos();
                    }
                }

                private void initDate() {
                    if (!this.has_date) {
                        this.has_date = true;
                        this.current_date = new Date();
                    }
                }

                public void onPictureTaken(byte[] bArr) {
                    initDate();
                    if (!Preview.this.applicationInterface.onPictureTaken(bArr, this.current_date)) {
                        this.success = false;
                    } else {
                        this.success = true;
                    }
                }

                public void onRawPictureTaken(RawImage rawImage) {
                    initDate();
                    Preview.this.applicationInterface.onRawPictureTaken(rawImage, this.current_date);
                }

                public void onBurstPictureTaken(List<byte[]> list) {
                    initDate();
                    this.success = true;
                    if (!Preview.this.applicationInterface.onBurstPictureTaken(list, this.current_date)) {
                        this.success = false;
                    }
                }

                public void onRawBurstPictureTaken(List<RawImage> list) {
                    initDate();
                    Preview.this.applicationInterface.onRawBurstPictureTaken(list, this.current_date);
                }

                public boolean imageQueueWouldBlock(int i, int i2) {
                    return Preview.this.applicationInterface.imageQueueWouldBlock(i, i2);
                }

                public void onFrontScreenTurnOn() {
                    Preview.this.applicationInterface.turnFrontScreenFlashOn();
                }
            };
            C034617 r0 = new ErrorCallback() {
                public void onError() {
                    Preview.this.count_cameraTakePicture--;
                    Preview.this.applicationInterface.onPhotoError();
                    Preview.this.phase = 0;
                    Preview.this.startCameraPreview();
                    Preview.this.applicationInterface.cameraInOperation(false, false);
                }
            };
            this.camera_controller.setRotation(getImageVideoRotation());
            boolean shutterSoundPref = this.applicationInterface.getShutterSoundPref();
            if (!this.is_video || !isVideoRecording()) {
                z2 = shutterSoundPref;
            }
            this.camera_controller.enableShutterSound(z2);
            if (this.using_android_l) {
                this.camera_controller.setUseExpoFastBurst(this.applicationInterface.useCamera2FastBurst());
            }
            if (z) {
                this.camera_controller.setBurstType(BurstType.BURSTTYPE_CONTINUOUS);
            }
            this.camera_controller.takePicture(r2, r0);
            this.count_cameraTakePicture++;
        }
    }

    /* access modifiers changed from: private */
    public void takeRemainingRepeatPhotos() {
        int i = this.remaining_repeat_photos;
        if (i == -1 || i > 0) {
            if (this.camera_controller == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("remaining_repeat_photos still set, but camera is closed!: ");
                sb.append(this.remaining_repeat_photos);
                Log.e(TAG, sb.toString());
                cancelRepeat();
            } else if (!this.applicationInterface.canTakeNewPhoto()) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Preview.this.takeRemainingRepeatPhotos();
                    }
                }, 500);
            } else {
                int i2 = this.remaining_repeat_photos;
                if (i2 > 0) {
                    this.remaining_repeat_photos = i2 - 1;
                }
                long repeatIntervalPref = this.applicationInterface.getRepeatIntervalPref();
                if (repeatIntervalPref == 0) {
                    this.phase = 2;
                    takePhoto(true, false);
                } else {
                    takePictureOnTimer(repeatIntervalPref, true);
                }
            }
        }
    }

    public void requestAutoFocus() {
        cancelAutoFocus();
        tryAutoFocus(false, true);
    }

    /* access modifiers changed from: private */
    public void tryAutoFocus(boolean z, final boolean z2) {
        if (this.camera_controller == null || !this.has_surface || !this.is_preview_started) {
            return;
        }
        if ((z2 && this.is_video) || (!isVideoRecording() && !isTakingPhotoOrOnTimer())) {
            if (z2) {
                removePendingContinuousFocusReset();
            }
            if (z2 && !this.is_video && this.camera_controller.focusIsContinuous()) {
                String str = "focus_mode_auto";
                if (supportedFocusValue(str)) {
                    this.camera_controller.setFocusValue(str);
                    this.autofocus_in_continuous_mode = true;
                }
            }
            if (this.camera_controller.supportsAutoFocus()) {
                if (!this.using_android_l) {
                    this.set_flash_value_after_autofocus = BuildConfig.FLAVOR;
                    String flashValue = this.camera_controller.getFlashValue();
                    if (z && flashValue.length() > 0) {
                        String str2 = "flash_off";
                        if (!flashValue.equals(str2) && !flashValue.equals("flash_torch")) {
                            this.set_flash_value_after_autofocus = flashValue;
                            this.camera_controller.setFlashValue(str2);
                        }
                    }
                }
                C034819 r4 = new AutoFocusCallback() {
                    public void onAutoFocus(boolean z) {
                        Preview.this.autoFocusCompleted(z2, z, false);
                    }
                };
                this.focus_success = 0;
                this.focus_complete_time = -1;
                this.successfully_focused = false;
                this.camera_controller.autoFocus(r4, false);
                this.count_cameraAutoFocus++;
                this.focus_started_time = System.currentTimeMillis();
            } else if (this.has_focus_area) {
                this.focus_success = 1;
                this.focus_complete_time = System.currentTimeMillis();
            }
        }
    }

    private void removePendingContinuousFocusReset() {
        Runnable runnable = this.reset_continuous_focus_runnable;
        if (runnable != null) {
            this.reset_continuous_focus_handler.removeCallbacks(runnable);
            this.reset_continuous_focus_runnable = null;
        }
    }

    /* access modifiers changed from: private */
    public void continuousFocusReset() {
        if (this.camera_controller != null && this.autofocus_in_continuous_mode) {
            this.autofocus_in_continuous_mode = false;
            String currentFocusValue = getCurrentFocusValue();
            if (currentFocusValue != null && !this.camera_controller.getFocusValue().equals(currentFocusValue) && this.camera_controller.getFocusValue().equals("focus_mode_auto")) {
                this.camera_controller.cancelAutoFocus();
                this.camera_controller.setFocusValue(currentFocusValue);
            }
        }
    }

    private void cancelAutoFocus() {
        CameraController cameraController = this.camera_controller;
        if (cameraController != null) {
            cameraController.cancelAutoFocus();
            autoFocusCompleted(false, false, true);
        }
    }

    /* access modifiers changed from: private */
    public void ensureFlashCorrect() {
        if (this.set_flash_value_after_autofocus.length() > 0) {
            CameraController cameraController = this.camera_controller;
            if (cameraController != null) {
                cameraController.setFlashValue(this.set_flash_value_after_autofocus);
                this.set_flash_value_after_autofocus = BuildConfig.FLAVOR;
            }
        }
    }

    /* access modifiers changed from: private */
    public void autoFocusCompleted(boolean z, boolean z2, boolean z3) {
        boolean z4;
        if (z3) {
            this.focus_success = 3;
        } else {
            this.focus_success = z2 ? 1 : 2;
            this.focus_complete_time = System.currentTimeMillis();
        }
        if (z && !z3 && (z2 || this.applicationInterface.isTestAlwaysFocus())) {
            this.successfully_focused = true;
            this.successfully_focused_time = this.focus_complete_time;
        }
        if (z && this.camera_controller != null && this.autofocus_in_continuous_mode) {
            String currentFocusValue = getCurrentFocusValue();
            if (currentFocusValue != null && !this.camera_controller.getFocusValue().equals(currentFocusValue) && this.camera_controller.getFocusValue().equals("focus_mode_auto")) {
                this.reset_continuous_focus_runnable = new Runnable() {
                    public void run() {
                        Preview.this.reset_continuous_focus_runnable = null;
                        Preview.this.continuousFocusReset();
                    }
                };
                this.reset_continuous_focus_handler.postDelayed(this.reset_continuous_focus_runnable, 3000);
            }
        }
        ensureFlashCorrect();
        if (this.using_face_detection && !z3) {
            CameraController cameraController = this.camera_controller;
            if (cameraController != null) {
                cameraController.cancelAutoFocus();
            }
        }
        synchronized (this) {
            z4 = this.take_photo_after_autofocus;
            this.take_photo_after_autofocus = false;
        }
        if (z4) {
            prepareAutoFocusPhoto();
            takePhotoWhenFocused(false);
        }
    }

    public void startCameraPreview() {
        if (this.camera_controller != null && !isTakingPhotoOrOnTimer() && !this.is_preview_started) {
            this.camera_controller.setRecordingHint(this.is_video);
            setPreviewFps();
            try {
                this.camera_controller.startPreview();
                this.count_cameraStartPreview++;
                this.is_preview_started = true;
                if (this.using_face_detection) {
                    this.camera_controller.startFaceDetection();
                    this.faces_detected = null;
                }
            } catch (CameraControllerException e) {
                e.printStackTrace();
                this.applicationInterface.onFailedStartPreview();
                return;
            }
        }
        setPreviewPaused(false);
        setupContinuousFocusMove();
    }

    /* access modifiers changed from: private */
    public void setPreviewPaused(boolean z) {
        this.applicationInterface.hasPausedPreview(z);
        if (z) {
            this.phase = 3;
            return;
        }
        this.phase = 0;
        this.applicationInterface.cameraInOperation(false, false);
    }

    public void onAccelerometerSensorChanged(SensorEvent sensorEvent) {
        this.has_gravity = true;
        for (int i = 0; i < 3; i++) {
            float[] fArr = this.gravity;
            fArr[i] = (fArr[i] * sensor_alpha) + (sensorEvent.values[i] * 0.19999999f);
        }
        calculateGeoDirection();
        float[] fArr2 = this.gravity;
        double d = (double) fArr2[0];
        double d2 = (double) fArr2[1];
        double d3 = (double) fArr2[2];
        Double.isNaN(d);
        Double.isNaN(d);
        double d4 = d * d;
        Double.isNaN(d2);
        Double.isNaN(d2);
        double d5 = d4 + (d2 * d2);
        Double.isNaN(d3);
        Double.isNaN(d3);
        double sqrt = Math.sqrt(d5 + (d3 * d3));
        this.has_pitch_angle = false;
        if (sqrt > 1.0E-8d) {
            this.has_pitch_angle = true;
            Double.isNaN(d3);
            this.pitch_angle = (Math.asin((-d3) / sqrt) * 180.0d) / 3.141592653589793d;
            this.has_level_angle = true;
            Double.isNaN(d);
            this.natural_level_angle = (Math.atan2(-d, d2) * 180.0d) / 3.141592653589793d;
            double d6 = this.natural_level_angle;
            if (d6 < -0.0d) {
                this.natural_level_angle = d6 + 360.0d;
            }
            updateLevelAngles();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("accel sensor has zero mag: ");
        sb.append(sqrt);
        Log.e(TAG, sb.toString());
        this.has_level_angle = false;
    }

    public void updateLevelAngles() {
        if (this.has_level_angle) {
            this.level_angle = this.natural_level_angle;
            this.level_angle -= this.applicationInterface.getCalibratedLevelAngle();
            double d = this.level_angle;
            this.orig_level_angle = d;
            double d2 = (double) ((float) this.current_orientation);
            Double.isNaN(d2);
            this.level_angle = d - d2;
            double d3 = this.level_angle;
            if (d3 < -180.0d) {
                this.level_angle = d3 + 360.0d;
            } else if (d3 > 180.0d) {
                this.level_angle = d3 - 360.0d;
            }
        }
    }

    public boolean hasLevelAngle() {
        return this.has_level_angle;
    }

    public boolean hasLevelAngleStable() {
        if (this.is_test || !this.has_pitch_angle || Math.abs(this.pitch_angle) <= 70.0d) {
            return this.has_level_angle;
        }
        return false;
    }

    public double getLevelAngleUncalibrated() {
        double d = this.natural_level_angle;
        double d2 = (double) this.current_orientation;
        Double.isNaN(d2);
        return d - d2;
    }

    public double getLevelAngle() {
        return this.level_angle;
    }

    public double getOrigLevelAngle() {
        return this.orig_level_angle;
    }

    public boolean hasPitchAngle() {
        return this.has_pitch_angle;
    }

    public double getPitchAngle() {
        return this.pitch_angle;
    }

    public void onMagneticSensorChanged(SensorEvent sensorEvent) {
        this.has_geomagnetic = true;
        for (int i = 0; i < 3; i++) {
            float[] fArr = this.geomagnetic;
            fArr[i] = (fArr[i] * sensor_alpha) + (sensorEvent.values[i] * 0.19999999f);
        }
        calculateGeoDirection();
    }

    private void calculateGeoDirection() {
        if (this.has_gravity && this.has_geomagnetic && SensorManager.getRotationMatrix(this.deviceRotation, this.deviceInclination, this.gravity, this.geomagnetic)) {
            SensorManager.remapCoordinateSystem(this.deviceRotation, 1, 3, this.cameraRotation);
            boolean z = this.has_geo_direction;
            this.has_geo_direction = true;
            SensorManager.getOrientation(this.cameraRotation, this.new_geo_direction);
            for (int i = 0; i < 3; i++) {
                float degrees = (float) Math.toDegrees((double) this.geo_direction[i]);
                float degrees2 = (float) Math.toDegrees((double) this.new_geo_direction[i]);
                if (z) {
                    degrees2 = lowPassFilter(degrees, degrees2, 0.1f, 10.0f);
                }
                this.geo_direction[i] = (float) Math.toRadians((double) degrees2);
            }
        }
    }

    private float lowPassFilter(float f, float f2, float f3, float f4) {
        float f5 = f2 - f;
        float abs = Math.abs(f5);
        if (abs < 180.0f) {
            return abs > f4 ? f2 : f + (f3 * f5);
        }
        if (360.0f - abs > f4) {
            return f2;
        }
        return ((f > f2 ? f + (f3 * (((f2 + 360.0f) - f) % 360.0f)) : f - (f3 * (((360.0f - f2) + f) % 360.0f))) + 360.0f) % 360.0f;
    }

    public boolean hasGeoDirection() {
        return this.has_geo_direction;
    }

    public double getGeoDirection() {
        return (double) this.geo_direction[0];
    }

    public boolean supportsFaceDetection() {
        return this.supports_face_detection;
    }

    public boolean supportsVideoStabilization() {
        return this.supports_video_stabilization;
    }

    public boolean supportsPhotoVideoRecording() {
        return this.supports_photo_video_recording && !this.video_high_speed;
    }

    public boolean isVideoHighSpeed() {
        return this.is_video && this.video_high_speed;
    }

    public boolean canDisableShutterSound() {
        return this.can_disable_shutter_sound;
    }

    public int getTonemapMaxCurvePoints() {
        return this.tonemap_max_curve_points;
    }

    public boolean supportsTonemapCurve() {
        return this.supports_tonemap_curve;
    }

    public List<String> getSupportedColorEffects() {
        return this.color_effects;
    }

    public List<String> getSupportedSceneModes() {
        return this.scene_modes;
    }

    public List<String> getSupportedWhiteBalances() {
        return this.white_balances;
    }

    public List<String> getSupportedAntiBanding() {
        return this.antibanding;
    }

    public List<String> getSupportedEdgeModes() {
        return this.edge_modes;
    }

    public List<String> getSupportedNoiseReductionModes() {
        return this.noise_reduction_modes;
    }

    public String getISOKey() {
        CameraController cameraController = this.camera_controller;
        return cameraController == null ? BuildConfig.FLAVOR : cameraController.getISOKey();
    }

    public boolean supportsWhiteBalanceTemperature() {
        return this.supports_white_balance_temperature;
    }

    public int getMinimumWhiteBalanceTemperature() {
        return this.min_temperature;
    }

    public int getMaximumWhiteBalanceTemperature() {
        return this.max_temperature;
    }

    public boolean supportsISORange() {
        return this.supports_iso_range;
    }

    public List<String> getSupportedISOs() {
        return this.isos;
    }

    public int getMinimumISO() {
        return this.min_iso;
    }

    public int getMaximumISO() {
        return this.max_iso;
    }

    public float getMinimumFocusDistance() {
        return this.minimum_focus_distance;
    }

    public boolean supportsExposureTime() {
        return this.supports_exposure_time;
    }

    public long getMinimumExposureTime() {
        return this.min_exposure_time;
    }

    public long getMaximumExposureTime() {
        long j = this.max_exposure_time;
        if (!this.applicationInterface.isExpoBracketingPref() && !this.applicationInterface.isFocusBracketingPref() && !this.applicationInterface.isCameraBurstPref()) {
            return j;
        }
        if (this.applicationInterface.getBurstForNoiseReduction()) {
            return Math.min(this.max_exposure_time, 2000000000);
        }
        return Math.min(this.max_exposure_time, 500000000);
    }

    public boolean supportsExposures() {
        return this.exposures != null;
    }

    public int getMinimumExposure() {
        return this.min_exposure;
    }

    public int getMaximumExposure() {
        return this.max_exposure;
    }

    public int getCurrentExposure() {
        CameraController cameraController = this.camera_controller;
        if (cameraController == null) {
            return 0;
        }
        return cameraController.getExposureCompensation();
    }

    public boolean supportsExpoBracketing() {
        return this.supports_expo_bracketing;
    }

    public int maxExpoBracketingNImages() {
        return this.max_expo_bracketing_n_images;
    }

    public boolean supportsFocusBracketing() {
        return this.supports_focus_bracketing;
    }

    public boolean supportsBurst() {
        return this.supports_burst;
    }

    public boolean supportsRaw() {
        return this.supports_raw;
    }

    public float getViewAngleX(boolean z) {
        Size currentPreviewSize = z ? getCurrentPreviewSize() : getCurrentPictureSize();
        if (currentPreviewSize == null) {
            Log.e(TAG, "can't find view angle x size");
            return this.view_angle_x;
        }
        float f = this.view_angle_x / this.view_angle_y;
        float f2 = ((float) currentPreviewSize.width) / ((float) currentPreviewSize.height);
        if (Math.abs(f2 - f) < 1.0E-5f) {
            return this.view_angle_x;
        }
        if (f2 > f) {
            return this.view_angle_x;
        }
        double d = (double) (f2 / f);
        double tan = Math.tan(Math.toRadians((double) this.view_angle_x) / 2.0d);
        Double.isNaN(d);
        return (float) Math.toDegrees(Math.atan(d * tan) * 2.0d);
    }

    public float getViewAngleY(boolean z) {
        Size currentPreviewSize = z ? getCurrentPreviewSize() : getCurrentPictureSize();
        if (currentPreviewSize == null) {
            Log.e(TAG, "can't find view angle y size");
            return this.view_angle_y;
        }
        float f = this.view_angle_x / this.view_angle_y;
        float f2 = ((float) currentPreviewSize.width) / ((float) currentPreviewSize.height);
        if (Math.abs(f2 - f) < 1.0E-5f) {
            return this.view_angle_y;
        }
        if (f2 <= f) {
            return this.view_angle_y;
        }
        double d = (double) (f / f2);
        double tan = Math.tan(Math.toRadians((double) this.view_angle_y) / 2.0d);
        Double.isNaN(d);
        return (float) Math.toDegrees(Math.atan(d * tan) * 2.0d);
    }

    public List<Size> getSupportedPreviewSizes() {
        return this.supported_preview_sizes;
    }

    public Size getCurrentPreviewSize() {
        return new Size(this.preview_w, this.preview_h);
    }

    public double getCurrentPreviewAspectRatio() {
        double d = (double) this.preview_w;
        double d2 = (double) this.preview_h;
        Double.isNaN(d);
        Double.isNaN(d2);
        return d / d2;
    }

    public List<Size> getSupportedPictureSizes(boolean z) {
        if (z) {
            CameraController cameraController = this.camera_controller;
            if (cameraController != null && cameraController.isBurstOrExpo()) {
                ArrayList arrayList = new ArrayList();
                for (Size size : this.sizes) {
                    if (size.supports_burst) {
                        arrayList.add(size);
                    }
                }
                return arrayList;
            }
        }
        return this.sizes;
    }

    public Size getCurrentPictureSize() {
        int i = this.current_size_index;
        if (i != -1) {
            List<Size> list = this.sizes;
            if (list != null) {
                return (Size) list.get(i);
            }
        }
        return null;
    }

    public VideoQualityHandler getVideoQualityHander() {
        return this.video_quality_handler;
    }

    public List<String> getSupportedVideoQuality(String str) {
        if (!str.equals("default") && this.supports_video_high_speed) {
            try {
                int parseInt = Integer.parseInt(str);
                ArrayList arrayList = new ArrayList();
                for (String str2 : this.video_quality_handler.getSupportedVideoQuality()) {
                    CamcorderProfile camcorderProfile = getCamcorderProfile(str2);
                    if (this.video_quality_handler.findVideoSizeForFrameRate(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight, (double) parseInt) != null) {
                        arrayList.add(str2);
                    }
                }
                return arrayList;
            } catch (NumberFormatException unused) {
            }
        }
        return this.video_quality_handler.getSupportedVideoQuality();
    }

    public boolean fpsIsHighSpeed(String str) {
        if (!str.equals("default") && this.supports_video_high_speed) {
            try {
                int parseInt = Integer.parseInt(str);
                if (this.video_quality_handler.videoSupportsFrameRate(parseInt)) {
                    return false;
                }
                if (this.video_quality_handler.videoSupportsFrameRateHighSpeed(parseInt)) {
                    return true;
                }
                Log.e(TAG, "fps is neither normal nor high speed");
            } catch (NumberFormatException unused) {
            }
        }
        return false;
    }

    public boolean supportsVideoHighSpeed() {
        return this.supports_video_high_speed;
    }

    public List<String> getSupportedFlashValues() {
        return this.supported_flash_values;
    }

    public List<String> getSupportedFocusValues() {
        return this.supported_focus_values;
    }

    public int getCameraId() {
        CameraController cameraController = this.camera_controller;
        if (cameraController == null) {
            return 0;
        }
        return cameraController.getCameraId();
    }

    public String getCameraAPI() {
        CameraController cameraController = this.camera_controller;
        if (cameraController == null) {
            return "None";
        }
        return cameraController.getAPI();
    }

    public void onResume() {
        recreatePreviewBitmap();
        this.app_is_paused = false;
        this.cameraSurface.onResume();
        CanvasView canvasView2 = this.canvasView;
        if (canvasView2 != null) {
            canvasView2.onResume();
        }
        if (this.camera_open_state == CameraOpenState.CAMERAOPENSTATE_CLOSING) {
            CloseCameraTask closeCameraTask = this.close_camera_task;
            if (closeCameraTask != null) {
                closeCameraTask.reopen = true;
            } else {
                Log.e(TAG, "onResume: state is CAMERAOPENSTATE_CLOSING, but close_camera_task is null");
            }
        } else {
            openCamera();
        }
    }

    public void onPause() {
        this.app_is_paused = true;
        if (this.camera_open_state == CameraOpenState.CAMERAOPENSTATE_OPENING) {
            AsyncTask<Void, Void, CameraController> asyncTask = this.open_camera_task;
            if (asyncTask != null) {
                asyncTask.cancel(true);
            } else {
                Log.e(TAG, "onPause: state is CAMERAOPENSTATE_OPENING, but open_camera_task is null");
            }
        }
        closeCamera(true, null);
        this.cameraSurface.onPause();
        CanvasView canvasView2 = this.canvasView;
        if (canvasView2 != null) {
            canvasView2.onPause();
        }
        freePreviewBitmap();
    }

    public void onDestroy() {
        boolean refreshPreviewBitmapTaskIsRunning = refreshPreviewBitmapTaskIsRunning();
        String str = TAG;
        if (refreshPreviewBitmapTaskIsRunning) {
            try {
                this.refreshPreviewBitmapTask.get();
            } catch (InterruptedException | ExecutionException e) {
                Log.e(str, "exception while waiting for background_task to finish");
                e.printStackTrace();
            }
        }
        freePreviewBitmap();
        RenderScript renderScript = this.f19rs;
        if (renderScript != null) {
            try {
                renderScript.destroy();
            } catch (RSInvalidStateException e2) {
                e2.printStackTrace();
            }
            this.f19rs = null;
        }
        if (this.camera_open_state != CameraOpenState.CAMERAOPENSTATE_CLOSING) {
            return;
        }
        if (this.close_camera_task != null) {
            System.currentTimeMillis();
            try {
                this.close_camera_task.get(3000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e3) {
                Log.e(str, "exception while waiting for close_camera_task to finish");
                e3.printStackTrace();
            }
        } else {
            Log.e(str, "onResume: state is CAMERAOPENSTATE_CLOSING, but close_camera_task is null");
        }
    }

    public void showToast(ToastBoxer toastBoxer, int i) {
        showToast(toastBoxer, getResources().getString(i), false);
    }

    public void showToast(ToastBoxer toastBoxer, String str) {
        showToast(toastBoxer, str, false);
    }

    public void showToast(String str, boolean z) {
        showToast((ToastBoxer) null, str, z);
    }

    public void showToast(ToastBoxer toastBoxer, String str, boolean z) {
        showToast(toastBoxer, str, 32, z);
    }

    private void showToast(String str, int i, boolean z) {
        showToast(null, str, i, z);
    }

    private void showToast(ToastBoxer toastBoxer, String str, int i, boolean z) {
        if (this.applicationInterface.getShowToastsPref()) {
            Activity activity = (Activity) getContext();
            final int i2 = i;
            final boolean z2 = z;
            final String str2 = str;
            final Activity activity2 = activity;
            final ToastBoxer toastBoxer2 = toastBoxer;
            C035721 r1 = new Runnable() {
                public void run() {
                    Toast toast;
                    int i = (int) ((((float) i2) * Preview.this.getResources().getDisplayMetrics().density) + 0.5f);
                    if (z2) {
                        if (Preview.this.active_fake_toast != null) {
                            Preview.this.active_fake_toast.setText(str2);
                            Preview.this.active_fake_toast.setOffsetY(i);
                            Preview.this.active_fake_toast.invalidate();
                            Preview.this.fake_toast_handler.removeCallbacksAndMessages(null);
                        } else {
                            Preview preview = Preview.this;
                            preview.active_fake_toast = new RotatedTextView(str2, i, activity2);
                            ((FrameLayout) ((Activity) Preview.this.getContext()).findViewById(16908290)).addView(Preview.this.active_fake_toast);
                        }
                        Preview.this.fake_toast_handler.postDelayed(new Runnable() {
                            public void run() {
                                ViewParent parent = Preview.this.active_fake_toast.getParent();
                                if (parent != null) {
                                    ((ViewGroup) parent).removeView(Preview.this.active_fake_toast);
                                }
                                Preview.this.active_fake_toast = null;
                            }
                        }, 2000);
                        return;
                    }
                    long currentTimeMillis = System.currentTimeMillis();
                    ToastBoxer toastBoxer = toastBoxer2;
                    if (toastBoxer == null || toastBoxer.toast == null || toastBoxer2.toast != Preview.this.last_toast || currentTimeMillis >= Preview.this.last_toast_time_ms + 2000) {
                        ToastBoxer toastBoxer2 = toastBoxer2;
                        if (!(toastBoxer2 == null || toastBoxer2.toast == null)) {
                            toastBoxer2.toast.cancel();
                        }
                        toast = new Toast(activity2);
                        ToastBoxer toastBoxer3 = toastBoxer2;
                        if (toastBoxer3 != null) {
                            toastBoxer3.toast = toast;
                        }
                        toast.setView(new RotatedTextView(str2, i, activity2));
                        Preview.this.last_toast_time_ms = currentTimeMillis;
                    } else {
                        toast = toastBoxer2.toast;
                        RotatedTextView rotatedTextView = (RotatedTextView) toast.getView();
                        rotatedTextView.setText(str2);
                        rotatedTextView.setOffsetY(i);
                        rotatedTextView.invalidate();
                        toast.setView(rotatedTextView);
                    }
                    toast.setDuration(0);
                    if (!((Activity) Preview.this.getContext()).isFinishing()) {
                        toast.show();
                    }
                    Preview.this.last_toast = toast;
                }
            };
            activity.runOnUiThread(r1);
        }
    }

    public void setUIRotation(int i) {
        this.ui_rotation = i;
    }

    public int getUIRotation() {
        return this.ui_rotation;
    }

    private void updateParametersFromLocation() {
        if (this.camera_controller == null) {
            return;
        }
        if (!this.applicationInterface.getGeotaggingPref() || this.applicationInterface.getLocation() == null) {
            this.camera_controller.removeLocationInfo();
            return;
        }
        this.camera_controller.setLocationInfo(this.applicationInterface.getLocation());
    }

    public void enablePreviewBitmap() {
        if (this.cameraSurface instanceof TextureView) {
            this.want_preview_bitmap = true;
            recreatePreviewBitmap();
        }
    }

    public void disablePreviewBitmap() {
        freePreviewBitmap();
        this.want_preview_bitmap = false;
        this.histogramScript = null;
    }

    public boolean isPreviewBitmapEnabled() {
        return this.want_preview_bitmap;
    }

    public boolean refreshPreviewBitmapTaskIsRunning() {
        return this.refreshPreviewBitmapTask != null;
    }

    private void recycleBitmapForPreviewTask(final Bitmap bitmap) {
        if (!refreshPreviewBitmapTaskIsRunning()) {
            bitmap.recycle();
            return;
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (!Preview.this.refreshPreviewBitmapTaskIsRunning()) {
                    bitmap.recycle();
                } else {
                    handler.postDelayed(this, 500);
                }
            }
        }, 500);
    }

    private void freePreviewBitmap() {
        cancelRefreshPreviewBitmap();
        this.histogram = null;
        Bitmap bitmap = this.preview_bitmap;
        if (bitmap != null) {
            recycleBitmapForPreviewTask(bitmap);
            this.preview_bitmap = null;
        }
        freeZebraStripesBitmap();
        freeFocusPeakingBitmap();
    }

    private void recreatePreviewBitmap() {
        freePreviewBitmap();
        if (this.want_preview_bitmap) {
            int i = this.textureview_w / 4;
            int i2 = this.textureview_h / 4;
            int displayRotationDegrees = getDisplayRotationDegrees();
            if (displayRotationDegrees == 90 || displayRotationDegrees == 270) {
                int i3 = i2;
                i2 = i;
                i = i3;
            }
            try {
                this.preview_bitmap = Bitmap.createBitmap(i, i2, Config.ARGB_8888);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "failed to create preview_bitmap");
                e.printStackTrace();
            }
            createZebraStripesBitmap();
            createFocusPeakingBitmap();
        }
    }

    private void freeZebraStripesBitmap() {
        Bitmap bitmap = this.zebra_stripes_bitmap_buffer;
        if (bitmap != null) {
            recycleBitmapForPreviewTask(bitmap);
            this.zebra_stripes_bitmap_buffer = null;
        }
        Bitmap bitmap2 = this.zebra_stripes_bitmap;
        if (bitmap2 != null) {
            bitmap2.recycle();
            this.zebra_stripes_bitmap = null;
        }
    }

    private void createZebraStripesBitmap() {
        if (this.want_zebra_stripes) {
            Bitmap bitmap = this.preview_bitmap;
            if (bitmap != null) {
                try {
                    this.zebra_stripes_bitmap_buffer = Bitmap.createBitmap(bitmap.getWidth(), this.preview_bitmap.getHeight(), Config.ARGB_8888);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "failed to create zebra_stripes_bitmap_buffer");
                    e.printStackTrace();
                }
            }
        }
    }

    private void freeFocusPeakingBitmap() {
        Bitmap bitmap = this.focus_peaking_bitmap_buffer;
        if (bitmap != null) {
            recycleBitmapForPreviewTask(bitmap);
            this.focus_peaking_bitmap_buffer = null;
        }
        Bitmap bitmap2 = this.focus_peaking_bitmap;
        if (bitmap2 != null) {
            bitmap2.recycle();
            this.focus_peaking_bitmap = null;
        }
    }

    private void createFocusPeakingBitmap() {
        if (this.want_focus_peaking && (this.preview_bitmap != null)) {
            try {
                this.focus_peaking_bitmap_buffer = Bitmap.createBitmap(this.preview_bitmap.getWidth(), this.preview_bitmap.getHeight(), Config.ARGB_8888);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "failed to create focus_peaking_bitmap_buffer");
                e.printStackTrace();
            }
        }
    }

    public void enableHistogram(HistogramType histogramType) {
        this.want_histogram = true;
        this.histogram_type = histogramType;
    }

    public void disableHistogram() {
        this.want_histogram = false;
    }

    public int[] getHistogram() {
        return this.histogram;
    }

    public void enableZebraStripes(int i) {
        this.want_zebra_stripes = true;
        this.zebra_stripes_threshold = i;
        if (this.zebra_stripes_bitmap_buffer == null) {
            createZebraStripesBitmap();
        }
    }

    public void disableZebraStripes() {
        if (this.want_zebra_stripes) {
            this.want_zebra_stripes = false;
            freeZebraStripesBitmap();
        }
    }

    public Bitmap getZebraStripesBitmap() {
        return this.zebra_stripes_bitmap;
    }

    public void enableFocusPeaking() {
        this.want_focus_peaking = true;
        if (this.focus_peaking_bitmap_buffer == null) {
            createFocusPeakingBitmap();
        }
    }

    public void disableFocusPeaking() {
        if (this.want_focus_peaking) {
            this.want_focus_peaking = false;
            freeFocusPeakingBitmap();
        }
    }

    public Bitmap getFocusPeakingBitmap() {
        return this.focus_peaking_bitmap;
    }

    private void refreshPreviewBitmap() {
        long j = (this.want_zebra_stripes || this.want_focus_peaking) ? 40 : 200;
        long currentTimeMillis = System.currentTimeMillis();
        if (this.want_preview_bitmap && this.preview_bitmap != null && VERSION.SDK_INT >= 21 && !this.app_is_paused && !this.applicationInterface.isPreviewInBackground() && !refreshPreviewBitmapTaskIsRunning() && currentTimeMillis > this.last_preview_bitmap_time_ms + j) {
            boolean z = this.want_histogram && currentTimeMillis > this.last_histogram_time_ms + 200;
            this.last_preview_bitmap_time_ms = currentTimeMillis;
            if (z) {
                this.last_histogram_time_ms = currentTimeMillis;
            }
            this.refreshPreviewBitmapTask = new RefreshPreviewBitmapTask(this, z);
            this.refreshPreviewBitmapTask.execute(new Void[0]);
        }
    }

    private void cancelRefreshPreviewBitmap() {
        if (refreshPreviewBitmapTaskIsRunning()) {
            this.refreshPreviewBitmapTask.cancel(true);
        }
    }

    public boolean isVideo() {
        return this.is_video;
    }

    public boolean isVideoRecording() {
        return this.video_recorder != null && this.video_start_time_set;
    }

    public boolean isVideoRecordingPaused() {
        return isVideoRecording() && this.video_recorder_is_paused;
    }

    public long getVideoTime() {
        if (isVideoRecordingPaused()) {
            return this.video_accumulated_time;
        }
        return (System.currentTimeMillis() - this.video_start_time) + this.video_accumulated_time;
    }

    public long getVideoAccumulatedTime() {
        return this.video_accumulated_time;
    }

    public int getMaxAmplitude() {
        if (this.video_recorder != null) {
            return this.video_recorder.getMaxAmplitude();
        }
        return 0;
    }

    public long getFrameRate() {
        if (VERSION.SDK_INT >= 24) {
            return 16;
        }
        return isTakingPhoto() ? 500 : 100;
    }

    public boolean isTakingPhoto() {
        return this.phase == 2;
    }

    public boolean usingCamera2API() {
        return this.using_android_l;
    }

    public CameraController getCameraController() {
        return this.camera_controller;
    }

    public CameraControllerManager getCameraControllerManager() {
        return this.camera_controller_manager;
    }

    public boolean supportsFocus() {
        return this.supported_focus_values != null;
    }

    public boolean supportsFlash() {
        return this.supported_flash_values != null;
    }

    public boolean supportsExposureLock() {
        return this.is_exposure_lock_supported;
    }

    public boolean isExposureLocked() {
        return this.is_exposure_locked;
    }

    public boolean supportsWhiteBalanceLock() {
        return this.is_white_balance_lock_supported;
    }

    public boolean isWhiteBalanceLocked() {
        return this.is_white_balance_locked;
    }

    public boolean supportsZoom() {
        return this.has_zoom;
    }

    public int getMaxZoom() {
        return this.max_zoom_factor;
    }

    public boolean hasFocusArea() {
        return this.has_focus_area;
    }

    public Pair<Integer, Integer> getFocusPos() {
        return new Pair<>(Integer.valueOf(this.focus_screen_x), Integer.valueOf(this.focus_screen_y));
    }

    public int getMaxNumFocusAreas() {
        return this.max_num_focus_areas;
    }

    public boolean isTakingPhotoOrOnTimer() {
        return this.phase == 2 || this.phase == 1;
    }

    public boolean isOnTimer() {
        return this.phase == 1;
    }

    public long getTimerEndTime() {
        return this.take_photo_time;
    }

    public boolean isPreviewPaused() {
        return this.phase == 3;
    }

    public boolean isPreviewStarted() {
        return this.is_preview_started;
    }

    public boolean isFocusWaiting() {
        return this.focus_success == 0;
    }

    public boolean isFocusRecentSuccess() {
        return this.focus_success == 1;
    }

    public long timeSinceStartedAutoFocus() {
        if (this.focus_started_time != -1) {
            return System.currentTimeMillis() - this.focus_started_time;
        }
        return 0;
    }

    public boolean isFocusRecentFailure() {
        return this.focus_success == 2;
    }

    private boolean recentlyFocused() {
        return this.successfully_focused && System.currentTimeMillis() < this.successfully_focused_time + 5000;
    }

    public Face[] getFacesDetected() {
        return this.faces_detected;
    }

    public float getZoomRatio() {
        if (this.zoom_ratios == null) {
            return 1.0f;
        }
        return ((float) ((Integer) this.zoom_ratios.get(this.camera_controller.getZoom())).intValue()) / 100.0f;
    }
}
