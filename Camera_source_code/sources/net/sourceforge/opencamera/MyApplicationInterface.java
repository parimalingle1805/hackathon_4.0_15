package net.sourceforge.opencamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.Settings.System;
import android.support.p000v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import net.sourceforge.opencamera.GyroSensor.TargetCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.cameracontroller.CameraController.Size;
import net.sourceforge.opencamera.cameracontroller.RawImage;
import net.sourceforge.opencamera.p004ui.DrawPreview;
import net.sourceforge.opencamera.preview.ApplicationInterface.NRModePref;
import net.sourceforge.opencamera.preview.ApplicationInterface.NoFreeStorageException;
import net.sourceforge.opencamera.preview.ApplicationInterface.RawPref;
import net.sourceforge.opencamera.preview.ApplicationInterface.VideoMaxFileSize;
import net.sourceforge.opencamera.preview.BasicApplicationInterface;
import net.sourceforge.opencamera.preview.Preview;
import net.sourceforge.opencamera.preview.VideoProfile;

public class MyApplicationInterface extends BasicApplicationInterface {
    private static final String TAG = "MyApplicationInterface";
    private static final int cameraId_default = 0;
    public static final int max_panorama_pics_c = 10;
    private static final String nr_mode_default = "preference_nr_mode_normal";
    private static final float panorama_pics_per_screen = 3.33333f;
    private int cameraId = 0;
    private final DrawPreview drawPreview;
    /* access modifiers changed from: private */
    public final GyroSensor gyroSensor;
    private boolean has_set_cameraId;
    private final ImageSaver imageSaver;
    private final List<LastImage> last_images = new ArrayList();
    private boolean last_images_saf;
    /* access modifiers changed from: private */
    public File last_video_file = null;
    /* access modifiers changed from: private */
    public Uri last_video_file_saf = null;
    private final LocationSupplier locationSupplier;
    /* access modifiers changed from: private */
    public final MainActivity main_activity;
    private int n_capture_images = 0;
    private int n_capture_images_raw = 0;
    /* access modifiers changed from: private */
    public int n_panorama_pics = 0;
    private String nr_mode;
    /* access modifiers changed from: private */
    public boolean panorama_dir_left_to_right = true;
    private boolean panorama_pic_accepted;
    private final ToastBoxer photo_delete_toast = new ToastBoxer();
    private final SharedPreferences sharedPreferences;
    /* access modifiers changed from: private */
    public final StorageUtils storageUtils;
    private final Timer subtitleVideoTimer = new Timer();
    private TimerTask subtitleVideoTimerTask;
    public long test_available_memory;
    public boolean test_set_available_memory;
    private final Rect text_bounds = new Rect();
    private boolean used_front_screen_flash;
    private int zoom_factor;

    public enum Alignment {
        ALIGNMENT_TOP,
        ALIGNMENT_CENTRE,
        ALIGNMENT_BOTTOM
    }

    private static class LastImage {
        final String name;
        final boolean share;
        Uri uri;

        LastImage(Uri uri2, boolean z) {
            this.name = null;
            this.uri = uri2;
            this.share = z;
        }

        LastImage(String str, boolean z) {
            this.name = str;
            if (VERSION.SDK_INT >= 24) {
                this.uri = null;
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("file://");
                sb.append(this.name);
                this.uri = Uri.parse(sb.toString());
            }
            this.share = z;
        }
    }

    public enum PhotoMode {
        Standard,
        DRO,
        HDR,
        ExpoBracketing,
        FocusBracketing,
        FastBurst,
        NoiseReduction,
        Panorama
    }

    public enum Shadow {
        SHADOW_NONE,
        SHADOW_OUTLINE,
        SHADOW_BACKGROUND
    }

    static float getPanoramaPicsPerScreen() {
        return panorama_pics_per_screen;
    }

    public boolean needsStoragePermission() {
        return true;
    }

    MyApplicationInterface(MainActivity mainActivity, Bundle bundle) {
        String str = nr_mode_default;
        this.nr_mode = str;
        this.test_set_available_memory = false;
        this.test_available_memory = 0;
        this.main_activity = mainActivity;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        this.locationSupplier = new LocationSupplier(mainActivity);
        this.gyroSensor = new GyroSensor(mainActivity);
        this.storageUtils = new StorageUtils(mainActivity, this);
        this.drawPreview = new DrawPreview(mainActivity, this);
        this.imageSaver = new ImageSaver(mainActivity);
        this.imageSaver.start();
        reset();
        if (bundle != null) {
            this.has_set_cameraId = true;
            this.cameraId = bundle.getInt("cameraId", 0);
            this.nr_mode = bundle.getString("nr_mode", str);
        }
    }

    /* access modifiers changed from: 0000 */
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putInt("cameraId", this.cameraId);
        bundle.putString("nr_mode", this.nr_mode);
    }

    /* access modifiers changed from: 0000 */
    public void onDestroy() {
        DrawPreview drawPreview2 = this.drawPreview;
        if (drawPreview2 != null) {
            drawPreview2.onDestroy();
        }
        ImageSaver imageSaver2 = this.imageSaver;
        if (imageSaver2 != null) {
            imageSaver2.onDestroy();
        }
    }

    /* access modifiers changed from: 0000 */
    public LocationSupplier getLocationSupplier() {
        return this.locationSupplier;
    }

    public GyroSensor getGyroSensor() {
        return this.gyroSensor;
    }

    /* access modifiers changed from: 0000 */
    public StorageUtils getStorageUtils() {
        return this.storageUtils;
    }

    public ImageSaver getImageSaver() {
        return this.imageSaver;
    }

    public DrawPreview getDrawPreview() {
        return this.drawPreview;
    }

    public Context getContext() {
        return this.main_activity;
    }

    public boolean useCamera2() {
        if (this.main_activity.supportsCamera2()) {
            if ("preference_camera_api_camera2".equals(this.sharedPreferences.getString(PreferenceKeys.CameraAPIPreferenceKey, PreferenceKeys.CameraAPIPreferenceDefault))) {
                return true;
            }
        }
        return false;
    }

    public Location getLocation() {
        return this.locationSupplier.getLocation();
    }

    public int createOutputVideoMethod() {
        if (!"android.media.action.VIDEO_CAPTURE".equals(this.main_activity.getIntent().getAction())) {
            return this.storageUtils.isUsingSAF() ? 1 : 0;
        }
        Bundle extras = this.main_activity.getIntent().getExtras();
        return (extras == null || ((Uri) extras.getParcelable("output")) == null) ? 0 : 2;
    }

    public File createOutputVideoFile(String str) throws IOException {
        this.last_video_file = this.storageUtils.createOutputMediaFile(2, BuildConfig.FLAVOR, str, new Date());
        return this.last_video_file;
    }

    public Uri createOutputVideoSAF(String str) throws IOException {
        this.last_video_file_saf = this.storageUtils.createOutputMediaFileSAF(2, BuildConfig.FLAVOR, str, new Date());
        return this.last_video_file_saf;
    }

    public Uri createOutputVideoUri() {
        if ("android.media.action.VIDEO_CAPTURE".equals(this.main_activity.getIntent().getAction())) {
            Bundle extras = this.main_activity.getIntent().getExtras();
            if (extras != null) {
                Uri uri = (Uri) extras.getParcelable("output");
                if (uri != null) {
                    return uri;
                }
            }
        }
        throw new RuntimeException();
    }

    public int getCameraIdPref() {
        return this.cameraId;
    }

    public String getFlashPref() {
        return this.sharedPreferences.getString(PreferenceKeys.getFlashPreferenceKey(this.cameraId), BuildConfig.FLAVOR);
    }

    public String getFocusPref(boolean z) {
        if (getPhotoMode() != PhotoMode.FocusBracketing || this.main_activity.getPreview().isVideo()) {
            return this.sharedPreferences.getString(PreferenceKeys.getFocusPreferenceKey(this.cameraId, z), BuildConfig.FLAVOR);
        }
        return "focus_mode_manual2";
    }

    /* access modifiers changed from: 0000 */
    public int getFocusAssistPref() {
        int i;
        try {
            i = Integer.parseInt(this.sharedPreferences.getString(PreferenceKeys.FocusAssistPreferenceKey, "0"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            i = 0;
        }
        if (i <= 0 || !this.main_activity.getPreview().isVideoRecording()) {
            return i;
        }
        return 0;
    }

    public boolean isVideoPref() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.IsVideoPreferenceKey, false);
    }

    public String getSceneModePref() {
        return this.sharedPreferences.getString(PreferenceKeys.SceneModePreferenceKey, "auto");
    }

    public String getColorEffectPref() {
        return this.sharedPreferences.getString(PreferenceKeys.ColorEffectPreferenceKey, CameraController.COLOR_EFFECT_DEFAULT);
    }

    public String getWhiteBalancePref() {
        return this.sharedPreferences.getString(PreferenceKeys.WhiteBalancePreferenceKey, "auto");
    }

    public int getWhiteBalanceTemperaturePref() {
        return this.sharedPreferences.getInt(PreferenceKeys.WhiteBalanceTemperaturePreferenceKey, 5000);
    }

    public String getAntiBandingPref() {
        return this.sharedPreferences.getString(PreferenceKeys.AntiBandingPreferenceKey, "auto");
    }

    public String getEdgeModePref() {
        return this.sharedPreferences.getString(PreferenceKeys.EdgeModePreferenceKey, "default");
    }

    public String getCameraNoiseReductionModePref() {
        return this.sharedPreferences.getString(PreferenceKeys.CameraNoiseReductionModePreferenceKey, "default");
    }

    public String getISOPref() {
        return this.sharedPreferences.getString(PreferenceKeys.ISOPreferenceKey, "auto");
    }

    public int getExposureCompensationPref() {
        try {
            return Integer.parseInt(this.sharedPreferences.getString(PreferenceKeys.ExposurePreferenceKey, "0"));
        } catch (NumberFormatException unused) {
            return 0;
        }
    }

    public static Size choosePanoramaResolution(List<Size> list) {
        boolean z = false;
        Size size = null;
        for (Size size2 : list) {
            if (size2.width <= 2080) {
                double d = (double) size2.width;
                double d2 = (double) size2.height;
                Double.isNaN(d);
                Double.isNaN(d2);
                if (Math.abs((d / d2) - 1.3333333333333333d) < 1.0E-5d && (!z || size2.width > size.width)) {
                    size = size2;
                    z = true;
                }
            }
        }
        if (z) {
            return size;
        }
        for (Size size3 : list) {
            if (size3.width <= 2080 && (!z || size3.width > size.width)) {
                size = size3;
                z = true;
            }
        }
        if (z) {
            return size;
        }
        for (Size size4 : list) {
            if (!z || size4.width < size.width) {
                size = size4;
                z = true;
            }
        }
        return size;
    }

    public Pair<Integer, Integer> getCameraResolutionPref() {
        if (getPhotoMode() == PhotoMode.Panorama) {
            Size choosePanoramaResolution = choosePanoramaResolution(this.main_activity.getPreview().getSupportedPictureSizes(false));
            return new Pair<>(Integer.valueOf(choosePanoramaResolution.width), Integer.valueOf(choosePanoramaResolution.height));
        }
        String string = this.sharedPreferences.getString(PreferenceKeys.getResolutionPreferenceKey(this.cameraId), BuildConfig.FLAVOR);
        if (string.length() > 0) {
            int indexOf = string.indexOf(32);
            if (indexOf != -1) {
                String substring = string.substring(0, indexOf);
                String substring2 = string.substring(indexOf + 1);
                try {
                    return new Pair<>(Integer.valueOf(Integer.parseInt(substring)), Integer.valueOf(Integer.parseInt(substring2)));
                } catch (NumberFormatException unused) {
                }
            }
        }
        return null;
    }

    private int getSaveImageQualityPref() {
        int i;
        try {
            i = Integer.parseInt(this.sharedPreferences.getString(PreferenceKeys.QualityPreferenceKey, "90"));
        } catch (NumberFormatException unused) {
            i = 90;
        }
        return isRawOnly() ? Math.min(i, 70) : i;
    }

    public int getImageQualityPref() {
        PhotoMode photoMode = getPhotoMode();
        if ((!this.main_activity.getPreview().isVideo() && (photoMode == PhotoMode.DRO || photoMode == PhotoMode.HDR || photoMode == PhotoMode.NoiseReduction)) || getImageFormatPref() != ImageFormat.STD) {
            return 100;
        }
        return getSaveImageQualityPref();
    }

    public boolean getFaceDetectionPref() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.FaceDetectionPreferenceKey, false);
    }

    public boolean fpsIsHighSpeed() {
        return this.main_activity.getPreview().fpsIsHighSpeed(getVideoFPSPref());
    }

    public String getVideoQualityPref() {
        if ("android.media.action.VIDEO_CAPTURE".equals(this.main_activity.getIntent().getAction())) {
            String str = "android.intent.extra.videoQuality";
            if (this.main_activity.getIntent().hasExtra(str)) {
                int intExtra = this.main_activity.getIntent().getIntExtra(str, 0);
                if (intExtra == 0 || intExtra == 1) {
                    List supportedVideoQuality = this.main_activity.getPreview().getVideoQualityHander().getSupportedVideoQuality();
                    if (intExtra == 0) {
                        return (String) supportedVideoQuality.get(supportedVideoQuality.size() - 1);
                    }
                    return (String) supportedVideoQuality.get(0);
                }
            }
        }
        return this.sharedPreferences.getString(PreferenceKeys.getVideoQualityPreferenceKey(this.cameraId, fpsIsHighSpeed()), BuildConfig.FLAVOR);
    }

    public boolean getVideoStabilizationPref() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.getVideoStabilizationPreferenceKey(), false);
    }

    public boolean getForce4KPref() {
        return this.cameraId == 0 && this.sharedPreferences.getBoolean(PreferenceKeys.getForceVideo4KPreferenceKey(), false) && this.main_activity.supportsForceVideo4K();
    }

    public String getRecordVideoOutputFormatPref() {
        return this.sharedPreferences.getString(PreferenceKeys.VideoFormatPreferenceKey, "preference_video_output_format_default");
    }

    public String getVideoBitratePref() {
        return this.sharedPreferences.getString(PreferenceKeys.getVideoBitratePreferenceKey(), "default");
    }

    public String getVideoFPSPref() {
        String str = "default";
        if ("android.media.action.VIDEO_CAPTURE".equals(this.main_activity.getIntent().getAction())) {
            String str2 = "android.intent.extra.videoQuality";
            if (this.main_activity.getIntent().hasExtra(str2)) {
                int intExtra = this.main_activity.getIntent().getIntExtra(str2, 0);
                if (intExtra == 0 || intExtra == 1) {
                    return str;
                }
            }
        }
        float videoCaptureRateFactor = getVideoCaptureRateFactor();
        if (videoCaptureRateFactor >= 0.99999f) {
            return this.sharedPreferences.getString(PreferenceKeys.getVideoFPSPreferenceKey(this.cameraId), str);
        }
        double d = (double) videoCaptureRateFactor;
        Double.isNaN(d);
        int i = (int) ((30.0d / d) + 0.5d);
        boolean videoSupportsFrameRateHighSpeed = this.main_activity.getPreview().getVideoQualityHander().videoSupportsFrameRateHighSpeed(i);
        String str3 = BuildConfig.FLAVOR;
        if (videoSupportsFrameRateHighSpeed || this.main_activity.getPreview().getVideoQualityHander().videoSupportsFrameRate(i)) {
            StringBuilder sb = new StringBuilder();
            sb.append(str3);
            sb.append(i);
            return sb.toString();
        }
        while (i < 240) {
            i *= 2;
            if (!this.main_activity.getPreview().getVideoQualityHander().videoSupportsFrameRateHighSpeed(i)) {
                if (this.main_activity.getPreview().getVideoQualityHander().videoSupportsFrameRate(i)) {
                }
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str3);
            sb2.append(i);
            return sb2.toString();
        }
        Log.e(TAG, "can't find valid fps for slow motion");
        return str;
    }

    public float getVideoCaptureRateFactor() {
        float f = this.sharedPreferences.getFloat(PreferenceKeys.getVideoCaptureRatePreferenceKey(this.main_activity.getPreview().getCameraId()), 1.0f);
        if (((double) Math.abs(f - 1.0f)) <= 1.0E-5d) {
            return f;
        }
        boolean z = false;
        Iterator it = getSupportedVideoCaptureRates().iterator();
        while (true) {
            if (it.hasNext()) {
                if (((double) Math.abs(f - ((Float) it.next()).floatValue())) < 1.0E-5d) {
                    z = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (z) {
            return f;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("stored capture_rate_factor: ");
        sb.append(f);
        sb.append(" not supported");
        Log.e(TAG, sb.toString());
        return 1.0f;
    }

    public List<Float> getSupportedVideoCaptureRates() {
        ArrayList arrayList = new ArrayList();
        if (this.main_activity.getPreview().supportsVideoHighSpeed()) {
            if (this.main_activity.getPreview().getVideoQualityHander().videoSupportsFrameRateHighSpeed(240) || this.main_activity.getPreview().getVideoQualityHander().videoSupportsFrameRate(240)) {
                arrayList.add(Float.valueOf(0.125f));
                arrayList.add(Float.valueOf(0.25f));
                arrayList.add(Float.valueOf(0.5f));
            } else if (this.main_activity.getPreview().getVideoQualityHander().videoSupportsFrameRateHighSpeed(120) || this.main_activity.getPreview().getVideoQualityHander().videoSupportsFrameRate(120)) {
                arrayList.add(Float.valueOf(0.25f));
                arrayList.add(Float.valueOf(0.5f));
            } else if (this.main_activity.getPreview().getVideoQualityHander().videoSupportsFrameRateHighSpeed(60) || this.main_activity.getPreview().getVideoQualityHander().videoSupportsFrameRate(60)) {
                arrayList.add(Float.valueOf(0.5f));
            }
        }
        arrayList.add(Float.valueOf(1.0f));
        if (VERSION.SDK_INT >= 21) {
            arrayList.add(Float.valueOf(2.0f));
            arrayList.add(Float.valueOf(3.0f));
            arrayList.add(Float.valueOf(4.0f));
            arrayList.add(Float.valueOf(5.0f));
            arrayList.add(Float.valueOf(10.0f));
            arrayList.add(Float.valueOf(20.0f));
            arrayList.add(Float.valueOf(30.0f));
            arrayList.add(Float.valueOf(60.0f));
            arrayList.add(Float.valueOf(120.0f));
            arrayList.add(Float.valueOf(240.0f));
        }
        return arrayList;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean useVideoLogProfile() {
        /*
            r9 = this;
            android.content.SharedPreferences r0 = r9.sharedPreferences
            java.lang.String r1 = "off"
            java.lang.String r2 = "preference_video_log"
            java.lang.String r0 = r0.getString(r2, r1)
            int r2 = r0.hashCode()
            r3 = 5
            r4 = 4
            r5 = 3
            r6 = 2
            r7 = 0
            r8 = 1
            switch(r2) {
                case -1078030475: goto L_0x0048;
                case -891980137: goto L_0x003e;
                case 107348: goto L_0x0034;
                case 109935: goto L_0x002c;
                case 3143098: goto L_0x0022;
                case 1419914662: goto L_0x0018;
                default: goto L_0x0017;
            }
        L_0x0017:
            goto L_0x0052
        L_0x0018:
            java.lang.String r1 = "extra_strong"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0052
            r0 = 5
            goto L_0x0053
        L_0x0022:
            java.lang.String r1 = "fine"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0052
            r0 = 1
            goto L_0x0053
        L_0x002c:
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0052
            r0 = 0
            goto L_0x0053
        L_0x0034:
            java.lang.String r1 = "low"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0052
            r0 = 2
            goto L_0x0053
        L_0x003e:
            java.lang.String r1 = "strong"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0052
            r0 = 4
            goto L_0x0053
        L_0x0048:
            java.lang.String r1 = "medium"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0052
            r0 = 3
            goto L_0x0053
        L_0x0052:
            r0 = -1
        L_0x0053:
            if (r0 == 0) goto L_0x0061
            if (r0 == r8) goto L_0x0060
            if (r0 == r6) goto L_0x0060
            if (r0 == r5) goto L_0x0060
            if (r0 == r4) goto L_0x0060
            if (r0 == r3) goto L_0x0060
            return r7
        L_0x0060:
            return r8
        L_0x0061:
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MyApplicationInterface.useVideoLogProfile():boolean");
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public float getVideoLogProfileStrength() {
        /*
            r8 = this;
            android.content.SharedPreferences r0 = r8.sharedPreferences
            java.lang.String r1 = "off"
            java.lang.String r2 = "preference_video_log"
            java.lang.String r0 = r0.getString(r2, r1)
            int r2 = r0.hashCode()
            r3 = 5
            r4 = 4
            r5 = 3
            r6 = 2
            r7 = 1
            switch(r2) {
                case -1078030475: goto L_0x0047;
                case -891980137: goto L_0x003d;
                case 107348: goto L_0x0033;
                case 109935: goto L_0x002b;
                case 3143098: goto L_0x0021;
                case 1419914662: goto L_0x0017;
                default: goto L_0x0016;
            }
        L_0x0016:
            goto L_0x0051
        L_0x0017:
            java.lang.String r1 = "extra_strong"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0051
            r0 = 5
            goto L_0x0052
        L_0x0021:
            java.lang.String r1 = "fine"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0051
            r0 = 1
            goto L_0x0052
        L_0x002b:
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0051
            r0 = 0
            goto L_0x0052
        L_0x0033:
            java.lang.String r1 = "low"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0051
            r0 = 2
            goto L_0x0052
        L_0x003d:
            java.lang.String r1 = "strong"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0051
            r0 = 4
            goto L_0x0052
        L_0x0047:
            java.lang.String r1 = "medium"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0051
            r0 = 3
            goto L_0x0052
        L_0x0051:
            r0 = -1
        L_0x0052:
            r1 = 0
            if (r0 == 0) goto L_0x006f
            if (r0 == r7) goto L_0x006c
            if (r0 == r6) goto L_0x0069
            if (r0 == r5) goto L_0x0066
            if (r0 == r4) goto L_0x0063
            if (r0 == r3) goto L_0x0060
            return r1
        L_0x0060:
            r0 = 1140457472(0x43fa0000, float:500.0)
            return r0
        L_0x0063:
            r0 = 1120403456(0x42c80000, float:100.0)
            return r0
        L_0x0066:
            r0 = 1092616192(0x41200000, float:10.0)
            return r0
        L_0x0069:
            r0 = 1084227584(0x40a00000, float:5.0)
            return r0
        L_0x006c:
            r0 = 1065353216(0x3f800000, float:1.0)
            return r0
        L_0x006f:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MyApplicationInterface.getVideoLogProfileStrength():float");
    }

    public long getVideoMaxDurationPref() {
        long j;
        if ("android.media.action.VIDEO_CAPTURE".equals(this.main_activity.getIntent().getAction())) {
            String str = "android.intent.extra.durationLimit";
            if (this.main_activity.getIntent().hasExtra(str)) {
                return (long) (this.main_activity.getIntent().getIntExtra(str, 0) * 1000);
            }
        }
        try {
            j = ((long) Integer.parseInt(this.sharedPreferences.getString(PreferenceKeys.getVideoMaxDurationPreferenceKey(), "0"))) * 1000;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            j = 0;
        }
        return j;
    }

    public int getVideoRestartTimesPref() {
        try {
            return Integer.parseInt(this.sharedPreferences.getString(PreferenceKeys.getVideoRestartPreferenceKey(), "0"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /* access modifiers changed from: 0000 */
    public long getVideoMaxFileSizeUserPref() {
        long j = 0;
        if ("android.media.action.VIDEO_CAPTURE".equals(this.main_activity.getIntent().getAction())) {
            String str = "android.intent.extra.sizeLimit";
            if (this.main_activity.getIntent().hasExtra(str)) {
                return this.main_activity.getIntent().getLongExtra(str, 0);
            }
        }
        try {
            j = Long.parseLong(this.sharedPreferences.getString(PreferenceKeys.getVideoMaxFileSizePreferenceKey(), "0"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return j;
    }

    private boolean getVideoRestartMaxFileSizeUserPref() {
        if (!"android.media.action.VIDEO_CAPTURE".equals(this.main_activity.getIntent().getAction()) || !this.main_activity.getIntent().hasExtra("android.intent.extra.sizeLimit")) {
            return this.sharedPreferences.getBoolean(PreferenceKeys.getVideoRestartMaxFileSizePreferenceKey(), true);
        }
        return false;
    }

    public VideoMaxFileSize getVideoMaxFileSizePref() throws NoFreeStorageException {
        VideoMaxFileSize videoMaxFileSize = new VideoMaxFileSize();
        videoMaxFileSize.max_filesize = getVideoMaxFileSizeUserPref();
        videoMaxFileSize.auto_restart = getVideoRestartMaxFileSizeUserPref();
        boolean z = true;
        if (!this.storageUtils.isUsingSAF()) {
            String saveLocation = this.storageUtils.getSaveLocation();
            if (saveLocation.startsWith("/") && !saveLocation.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                z = false;
            }
        }
        if (z) {
            long freeMemory = this.storageUtils.freeMemory();
            if (freeMemory >= 0) {
                long j = ((freeMemory * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) - 50000000;
                if (this.test_set_available_memory) {
                    j = this.test_available_memory;
                }
                if (j <= 20000000) {
                    throw new NoFreeStorageException();
                } else if (videoMaxFileSize.max_filesize == 0 || videoMaxFileSize.max_filesize > j) {
                    videoMaxFileSize.max_filesize = j;
                }
            }
        }
        return videoMaxFileSize;
    }

    public boolean getVideoFlashPref() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.getVideoFlashPreferenceKey(), false);
    }

    public boolean getVideoLowPowerCheckPref() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.getVideoLowPowerCheckPreferenceKey(), true);
    }

    public String getPreviewSizePref() {
        return this.sharedPreferences.getString(PreferenceKeys.PreviewSizePreferenceKey, "preference_preview_size_wysiwyg");
    }

    public String getPreviewRotationPref() {
        return this.sharedPreferences.getString(PreferenceKeys.getRotatePreviewPreferenceKey(), "0");
    }

    public String getLockOrientationPref() {
        if (getPhotoMode() == PhotoMode.Panorama) {
            return "portrait";
        }
        return this.sharedPreferences.getString(PreferenceKeys.getLockOrientationPreferenceKey(), CameraController.COLOR_EFFECT_DEFAULT);
    }

    public boolean getTouchCapturePref() {
        return this.sharedPreferences.getString(PreferenceKeys.TouchCapturePreferenceKey, CameraController.COLOR_EFFECT_DEFAULT).equals("single");
    }

    public boolean getDoubleTapCapturePref() {
        return this.sharedPreferences.getString(PreferenceKeys.TouchCapturePreferenceKey, CameraController.COLOR_EFFECT_DEFAULT).equals("double");
    }

    public boolean getPausePreviewPref() {
        if (!this.main_activity.getPreview().isVideoRecording() && !this.main_activity.lastContinuousFastBurst() && getPhotoMode() != PhotoMode.Panorama) {
            return this.sharedPreferences.getBoolean(PreferenceKeys.PausePreviewPreferenceKey, false);
        }
        return false;
    }

    public boolean getShowToastsPref() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.ShowToastsPreferenceKey, true);
    }

    public boolean getThumbnailAnimationPref() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.ThumbnailAnimationPreferenceKey, true);
    }

    public boolean getShutterSoundPref() {
        if (getPhotoMode() == PhotoMode.Panorama) {
            return false;
        }
        return this.sharedPreferences.getBoolean(PreferenceKeys.getShutterSoundPreferenceKey(), true);
    }

    public boolean getStartupFocusPref() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.getStartupFocusPreferenceKey(), true);
    }

    public long getTimerPref() {
        long j = 0;
        if (getPhotoMode() == PhotoMode.Panorama) {
            return 0;
        }
        try {
            j = 1000 * ((long) Integer.parseInt(this.sharedPreferences.getString(PreferenceKeys.getTimerPreferenceKey(), "0")));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return j;
    }

    public String getRepeatPref() {
        String str = "1";
        if (getPhotoMode() == PhotoMode.Panorama) {
            return str;
        }
        return this.sharedPreferences.getString(PreferenceKeys.getRepeatModePreferenceKey(), str);
    }

    public long getRepeatIntervalPref() {
        try {
            return (long) (Float.parseFloat(this.sharedPreferences.getString(PreferenceKeys.getRepeatIntervalPreferenceKey(), "0")) * 1000.0f);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean getGeotaggingPref() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.LocationPreferenceKey, false);
    }

    public boolean getRequireLocationPref() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.RequireLocationPreferenceKey, false);
    }

    /* access modifiers changed from: 0000 */
    public boolean getGeodirectionPref() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.GPSDirectionPreferenceKey, false);
    }

    public boolean getRecordAudioPref() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.getRecordAudioPreferenceKey(), true);
    }

    public String getRecordAudioChannelsPref() {
        return this.sharedPreferences.getString(PreferenceKeys.getRecordAudioChannelsPreferenceKey(), "audio_default");
    }

    public String getRecordAudioSourcePref() {
        return this.sharedPreferences.getString(PreferenceKeys.getRecordAudioSourcePreferenceKey(), "audio_src_camcorder");
    }

    public boolean getAutoStabilisePref() {
        if (!this.sharedPreferences.getBoolean(PreferenceKeys.AutoStabilisePreferenceKey, false) || !this.main_activity.supportsAutoStabilise()) {
            return false;
        }
        return true;
    }

    public String getStampPref() {
        return this.sharedPreferences.getString(PreferenceKeys.StampPreferenceKey, "preference_stamp_no");
    }

    private String getStampDateFormatPref() {
        return this.sharedPreferences.getString(PreferenceKeys.StampDateFormatPreferenceKey, "preference_stamp_dateformat_default");
    }

    private String getStampTimeFormatPref() {
        return this.sharedPreferences.getString(PreferenceKeys.StampTimeFormatPreferenceKey, "preference_stamp_timeformat_default");
    }

    private String getStampGPSFormatPref() {
        return this.sharedPreferences.getString(PreferenceKeys.StampGPSFormatPreferenceKey, "preference_stamp_gpsformat_default");
    }

    private String getStampGeoAddressPref() {
        return this.sharedPreferences.getString(PreferenceKeys.StampGeoAddressPreferenceKey, "preference_stamp_geo_address_no");
    }

    private String getUnitsDistancePref() {
        return this.sharedPreferences.getString(PreferenceKeys.UnitsDistancePreferenceKey, "preference_units_distance_m");
    }

    public String getTextStampPref() {
        return this.sharedPreferences.getString(PreferenceKeys.TextStampPreferenceKey, BuildConfig.FLAVOR);
    }

    private int getTextStampFontSizePref() {
        try {
            return Integer.parseInt(this.sharedPreferences.getString(PreferenceKeys.StampFontSizePreferenceKey, "12"));
        } catch (NumberFormatException unused) {
            return 12;
        }
    }

    private String getVideoSubtitlePref() {
        return this.sharedPreferences.getString(PreferenceKeys.VideoSubtitlePref, "preference_video_subtitle_no");
    }

    public int getZoomPref() {
        return this.zoom_factor;
    }

    public double getCalibratedLevelAngle() {
        return (double) this.sharedPreferences.getFloat(PreferenceKeys.CalibratedLevelAnglePreferenceKey, 0.0f);
    }

    public boolean canTakeNewPhoto() {
        int i;
        int i2;
        if (this.main_activity.getPreview().isVideo()) {
            i2 = 0;
            i = 1;
        } else {
            i2 = (!this.main_activity.getPreview().supportsExpoBracketing() || !isExpoBracketingPref()) ? ((!this.main_activity.getPreview().supportsFocusBracketing() || !isFocusBracketingPref()) && this.main_activity.getPreview().supportsBurst() && isCameraBurstPref()) ? getBurstForNoiseReduction() ? getNRModePref() == NRModePref.NRMODE_LOW_LIGHT ? 15 : 8 : getBurstNImages() : 1 : getExpoBracketingNImagesPref();
            if (!this.main_activity.getPreview().supportsRaw() || getRawPref() != RawPref.RAWPREF_JPEG_DNG) {
                i = i2;
                i2 = 0;
            } else {
                i = i2;
            }
        }
        int computePhotoCost = this.imageSaver.computePhotoCost(i2, i);
        if (this.imageSaver.queueWouldBlock(computePhotoCost)) {
            return false;
        }
        int nImagesToSave = this.imageSaver.getNImagesToSave();
        PhotoMode photoMode = getPhotoMode();
        if ((photoMode == PhotoMode.FastBurst || photoMode == PhotoMode.Panorama) && nImagesToSave > 0) {
            return false;
        }
        if (photoMode == PhotoMode.NoiseReduction && nImagesToSave >= computePhotoCost * 2) {
            return false;
        }
        if (i > 1 && nImagesToSave >= computePhotoCost * 3) {
            return false;
        }
        if (i2 <= 0 || nImagesToSave < computePhotoCost * 3) {
            return nImagesToSave < computePhotoCost * 5 || (this.main_activity.supportsNoiseReduction() && nImagesToSave <= 8);
        }
        return false;
    }

    public boolean imageQueueWouldBlock(int i, int i2) {
        return this.imageSaver.queueWouldBlock(i, i2);
    }

    public long getExposureTimePref() {
        return this.sharedPreferences.getLong(PreferenceKeys.ExposureTimePreferenceKey, CameraController.EXPOSURE_TIME_DEFAULT);
    }

    public float getFocusDistancePref(boolean z) {
        return this.sharedPreferences.getFloat(z ? PreferenceKeys.FocusBracketingTargetDistancePreferenceKey : PreferenceKeys.FocusDistancePreferenceKey, 0.0f);
    }

    public boolean isExpoBracketingPref() {
        PhotoMode photoMode = getPhotoMode();
        return photoMode == PhotoMode.HDR || photoMode == PhotoMode.ExpoBracketing;
    }

    public boolean isFocusBracketingPref() {
        return getPhotoMode() == PhotoMode.FocusBracketing;
    }

    public boolean isCameraBurstPref() {
        PhotoMode photoMode = getPhotoMode();
        return photoMode == PhotoMode.FastBurst || photoMode == PhotoMode.NoiseReduction;
    }

    public int getBurstNImages() {
        int i;
        if (getPhotoMode() != PhotoMode.FastBurst) {
            return 1;
        }
        try {
            i = Integer.parseInt(this.sharedPreferences.getString(PreferenceKeys.FastBurstNImagesPreferenceKey, "5"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            i = 5;
        }
        return i;
    }

    public boolean getBurstForNoiseReduction() {
        return getPhotoMode() == PhotoMode.NoiseReduction;
    }

    public void setNRMode(String str) {
        this.nr_mode = str;
    }

    public String getNRMode() {
        return this.nr_mode;
    }

    public NRModePref getNRModePref() {
        String str = this.nr_mode;
        if (((str.hashCode() == 753800774 && str.equals("preference_nr_mode_low_light")) ? (char) 0 : 65535) != 0) {
            return NRModePref.NRMODE_NORMAL;
        }
        return NRModePref.NRMODE_LOW_LIGHT;
    }

    public int getExpoBracketingNImagesPref() {
        if (getPhotoMode() == PhotoMode.HDR) {
            return 3;
        }
        try {
            return Integer.parseInt(this.sharedPreferences.getString(PreferenceKeys.ExpoBracketingNImagesPreferenceKey, "3"));
        } catch (NumberFormatException unused) {
            return 3;
        }
    }

    public double getExpoBracketingStopsPref() {
        if (getPhotoMode() == PhotoMode.HDR) {
            return 2.0d;
        }
        try {
            return Double.parseDouble(this.sharedPreferences.getString(PreferenceKeys.ExpoBracketingStopsPreferenceKey, "2"));
        } catch (NumberFormatException unused) {
            return 2.0d;
        }
    }

    public int getFocusBracketingNImagesPref() {
        try {
            return Integer.parseInt(this.sharedPreferences.getString(PreferenceKeys.FocusBracketingNImagesPreferenceKey, "3"));
        } catch (NumberFormatException unused) {
            return 3;
        }
    }

    public boolean getFocusBracketingAddInfinityPref() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.FocusBracketingAddInfinityPreferenceKey, false);
    }

    public PhotoMode getPhotoMode() {
        String string = this.sharedPreferences.getString(PreferenceKeys.PhotoModePreferenceKey, "preference_photo_mode_std");
        if (string.equals("preference_photo_mode_dro") && this.main_activity.supportsDRO()) {
            return PhotoMode.DRO;
        }
        if (string.equals("preference_photo_mode_hdr") && this.main_activity.supportsHDR()) {
            return PhotoMode.HDR;
        }
        if (string.equals("preference_photo_mode_expo_bracketing") && this.main_activity.supportsExpoBracketing()) {
            return PhotoMode.ExpoBracketing;
        }
        if (string.equals("preference_photo_mode_focus_bracketing") && this.main_activity.supportsFocusBracketing()) {
            return PhotoMode.FocusBracketing;
        }
        if (string.equals("preference_photo_mode_fast_burst") && this.main_activity.supportsFastBurst()) {
            return PhotoMode.FastBurst;
        }
        if (string.equals("preference_photo_mode_noise_reduction") && this.main_activity.supportsNoiseReduction()) {
            return PhotoMode.NoiseReduction;
        }
        if (!string.equals("preference_photo_mode_panorama") || this.main_activity.getPreview().isVideo() || !this.main_activity.supportsPanorama()) {
            return PhotoMode.Standard;
        }
        return PhotoMode.Panorama;
    }

    public boolean getOptimiseAEForDROPref() {
        return getPhotoMode() == PhotoMode.DRO;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0031  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0039  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private net.sourceforge.opencamera.ImageSaver.Request.ImageFormat getImageFormatPref() {
        /*
            r4 = this;
            android.content.SharedPreferences r0 = r4.sharedPreferences
            java.lang.String r1 = "preference_image_format"
            java.lang.String r2 = "preference_image_format_jpeg"
            java.lang.String r0 = r0.getString(r1, r2)
            int r1 = r0.hashCode()
            r2 = -659525079(0xffffffffd8b07229, float:-1.55203489E15)
            r3 = 1
            if (r1 == r2) goto L_0x0024
            r2 = 1029758876(0x3d60df9c, float:0.05490075)
            if (r1 == r2) goto L_0x001a
            goto L_0x002e
        L_0x001a:
            java.lang.String r1 = "preference_image_format_webp"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x002e
            r0 = 0
            goto L_0x002f
        L_0x0024:
            java.lang.String r1 = "preference_image_format_png"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x002e
            r0 = 1
            goto L_0x002f
        L_0x002e:
            r0 = -1
        L_0x002f:
            if (r0 == 0) goto L_0x0039
            if (r0 == r3) goto L_0x0036
            net.sourceforge.opencamera.ImageSaver$Request$ImageFormat r0 = net.sourceforge.opencamera.ImageSaver.Request.ImageFormat.STD
            return r0
        L_0x0036:
            net.sourceforge.opencamera.ImageSaver$Request$ImageFormat r0 = net.sourceforge.opencamera.ImageSaver.Request.ImageFormat.PNG
            return r0
        L_0x0039:
            net.sourceforge.opencamera.ImageSaver$Request$ImageFormat r0 = net.sourceforge.opencamera.ImageSaver.Request.ImageFormat.WEBP
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MyApplicationInterface.getImageFormatPref():net.sourceforge.opencamera.ImageSaver$Request$ImageFormat");
    }

    public boolean isRawAllowed(PhotoMode photoMode) {
        boolean z = false;
        if (isImageCaptureIntent() || this.main_activity.getPreview().isVideo()) {
            return false;
        }
        if (photoMode == PhotoMode.Standard || photoMode == PhotoMode.DRO) {
            return true;
        }
        PhotoMode photoMode2 = PhotoMode.ExpoBracketing;
        String str = PreferenceKeys.AllowRawForExpoBracketingPreferenceKey;
        if (photoMode == photoMode2) {
            if (this.sharedPreferences.getBoolean(str, true) && this.main_activity.supportsBurstRaw()) {
                z = true;
            }
            return z;
        } else if (photoMode == PhotoMode.HDR) {
            if (this.sharedPreferences.getBoolean(PreferenceKeys.HDRSaveExpoPreferenceKey, false) && this.sharedPreferences.getBoolean(str, true) && this.main_activity.supportsBurstRaw()) {
                z = true;
            }
            return z;
        } else {
            if (photoMode == PhotoMode.FocusBracketing && this.sharedPreferences.getBoolean(PreferenceKeys.AllowRawForFocusBracketingPreferenceKey, true) && this.main_activity.supportsBurstRaw()) {
                z = true;
            }
            return z;
        }
    }

    public RawPref getRawPref() {
        if (isRawAllowed(getPhotoMode())) {
            String string = this.sharedPreferences.getString(PreferenceKeys.RawPreferenceKey, "preference_raw_no");
            char c = 65535;
            int hashCode = string.hashCode();
            if (hashCode != -1076775865) {
                if (hashCode == -866009364 && string.equals("preference_raw_yes")) {
                    c = 0;
                }
            } else if (string.equals("preference_raw_only")) {
                c = 1;
            }
            if (c == 0 || c == 1) {
                return RawPref.RAWPREF_JPEG_DNG;
            }
        }
        return RawPref.RAWPREF_JPEG_ONLY;
    }

    public boolean isRawOnly() {
        return isRawOnly(getPhotoMode());
    }

    /* access modifiers changed from: 0000 */
    public boolean isRawOnly(PhotoMode photoMode) {
        if (isRawAllowed(photoMode)) {
            String string = this.sharedPreferences.getString(PreferenceKeys.RawPreferenceKey, "preference_raw_no");
            char c = 65535;
            if (string.hashCode() == -1076775865 && string.equals("preference_raw_only")) {
                c = 0;
            }
            if (c == 0) {
                return true;
            }
        }
        return false;
    }

    public int getMaxRawImages() {
        return this.imageSaver.getMaxDNG();
    }

    public boolean useCamera2FakeFlash() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.Camera2FakeFlashPreferenceKey, false);
    }

    public boolean useCamera2FastBurst() {
        return this.sharedPreferences.getBoolean(PreferenceKeys.Camera2FastBurstPreferenceKey, true);
    }

    public boolean usePhotoVideoRecording() {
        if (!useCamera2()) {
            return true;
        }
        return this.sharedPreferences.getBoolean(PreferenceKeys.Camera2PhotoVideoRecordingPreferenceKey, true);
    }

    public boolean isPreviewInBackground() {
        return this.main_activity.isCameraInBackground();
    }

    public boolean allowZoom() {
        return getPhotoMode() != PhotoMode.Panorama;
    }

    public boolean isTestAlwaysFocus() {
        return this.main_activity.is_test;
    }

    public void cameraSetup() {
        this.main_activity.cameraSetup();
        this.drawPreview.clearContinuousFocusMove();
        this.drawPreview.updateSettings();
    }

    public void onContinuousFocusMove(boolean z) {
        this.drawPreview.onContinuousFocusMove(z);
    }

    /* access modifiers changed from: 0000 */
    public void startPanorama() {
        this.gyroSensor.startRecording();
        this.n_panorama_pics = 0;
        this.panorama_pic_accepted = false;
        this.panorama_dir_left_to_right = true;
        this.main_activity.getMainUI().setTakePhotoIcon();
        this.main_activity.findViewById(C0316R.C0318id.cancel_panorama).setVisibility(0);
        this.main_activity.getMainUI().clearSeekBar();
    }

    /* access modifiers changed from: 0000 */
    public void finishPanorama() {
        this.imageSaver.getImageBatchRequest().panorama_dir_left_to_right = this.panorama_dir_left_to_right;
        stopPanorama(false);
        this.imageSaver.finishImageBatch(saveInBackground(isImageCaptureIntent()));
    }

    /* access modifiers changed from: 0000 */
    public void stopPanorama(boolean z) {
        if (this.gyroSensor.isRecording()) {
            this.gyroSensor.stopRecording();
            clearPanoramaPoint();
            if (z) {
                this.imageSaver.flushImageBatch();
            }
            this.main_activity.getMainUI().setTakePhotoIcon();
            this.main_activity.findViewById(C0316R.C0318id.cancel_panorama).setVisibility(8);
            this.main_activity.getMainUI().showGUI();
        }
    }

    private void setNextPanoramaPoint(boolean z) {
        float viewAngleY = this.main_activity.getPreview().getViewAngleY(false);
        if (!z) {
            this.n_panorama_pics++;
        }
        if (this.n_panorama_pics == 10) {
            finishPanorama();
            return;
        }
        float radians = (float) Math.toRadians((double) viewAngleY);
        int i = this.n_panorama_pics;
        float f = radians * ((float) i);
        if (i > 1 && !this.panorama_dir_left_to_right) {
            f = -f;
        }
        double d = (double) (f / panorama_pics_per_screen);
        setNextPanoramaPoint((float) Math.sin(d), 0.0f, (float) (-Math.cos(d)));
        if (this.n_panorama_pics == 1) {
            double d2 = (double) ((-f) / panorama_pics_per_screen);
            float sin = (float) Math.sin(d2);
            float f2 = (float) (-Math.cos(d2));
            this.gyroSensor.addTarget(sin, 0.0f, f2);
            this.drawPreview.addGyroDirectionMarker(sin, 0.0f, f2);
        }
    }

    private void setNextPanoramaPoint(float f, float f2, float f3) {
        this.gyroSensor.setTarget(f, f2, f3, 0.017453292f, 0.03490481f, 0.7853982f, new TargetCallback() {
            public void onAchieved(int i) {
                MyApplicationInterface.this.gyroSensor.disableTargetCallback();
                boolean z = true;
                if (MyApplicationInterface.this.n_panorama_pics == 1) {
                    MyApplicationInterface myApplicationInterface = MyApplicationInterface.this;
                    if (i != 0) {
                        z = false;
                    }
                    myApplicationInterface.panorama_dir_left_to_right = z;
                }
                MyApplicationInterface.this.main_activity.takePicturePressed(false, false);
            }

            public void onTooFar() {
                if (!MyApplicationInterface.this.main_activity.is_test) {
                    MyApplicationInterface.this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.panorama_cancelled);
                    MyApplicationInterface.this.stopPanorama(true);
                }
            }
        });
        this.drawPreview.setGyroDirectionMarker(f, f2, f3);
    }

    private void clearPanoramaPoint() {
        this.gyroSensor.clearTarget();
        this.drawPreview.clearGyroDirectionMarker();
    }

    public void touchEvent(MotionEvent motionEvent) {
        this.main_activity.getMainUI().clearSeekBar();
        this.main_activity.getMainUI().closePopup();
        if (this.main_activity.usingKitKatImmersiveMode()) {
            this.main_activity.setImmersiveMode(false);
        }
    }

    public void startingVideo() {
        if (this.sharedPreferences.getBoolean(PreferenceKeys.getLockVideoPreferenceKey(), false)) {
            this.main_activity.lockScreen();
        }
        this.main_activity.stopAudioListeners();
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.take_photo);
        imageButton.setImageResource(C0316R.C0317drawable.take_video_recording);
        imageButton.setContentDescription(getContext().getResources().getString(C0316R.string.stop_video));
        imageButton.setTag(Integer.valueOf(C0316R.C0317drawable.take_video_recording));
        this.main_activity.getMainUI().destroyPopup();
    }

    public void startedVideo() {
        if (VERSION.SDK_INT >= 24) {
            if (!this.main_activity.getMainUI().inImmersiveMode() || !this.main_activity.usingKitKatImmersiveModeEverything()) {
                this.main_activity.findViewById(C0316R.C0318id.pause_video).setVisibility(0);
            }
            this.main_activity.getMainUI().setPauseVideoContentDescription();
        }
        if (this.main_activity.getPreview().supportsPhotoVideoRecording() && usePhotoVideoRecording() && (!this.main_activity.getMainUI().inImmersiveMode() || !this.main_activity.usingKitKatImmersiveModeEverything())) {
            this.main_activity.findViewById(C0316R.C0318id.take_photo_when_video_recording).setVisibility(0);
        }
        if (this.main_activity.getMainUI().isExposureUIOpen()) {
            this.main_activity.getMainUI().setupExposureUI();
        }
        final int createOutputVideoMethod = createOutputVideoMethod();
        if (getVideoSubtitlePref().equals("preference_video_subtitle_yes") && createOutputVideoMethod != 2) {
            final String stampDateFormatPref = getStampDateFormatPref();
            final String stampTimeFormatPref = getStampTimeFormatPref();
            final String stampGPSFormatPref = getStampGPSFormatPref();
            final String unitsDistancePref = getUnitsDistancePref();
            final String stampGeoAddressPref = getStampGeoAddressPref();
            final boolean geotaggingPref = getGeotaggingPref();
            final boolean geodirectionPref = getGeodirectionPref();
            Timer timer = this.subtitleVideoTimer;
            AnonymousClass1SubtitleVideoTimerTask r0 = new TimerTask() {
                private int count = 1;
                private long min_video_time_from = 0;
                OutputStreamWriter writer;

                private String getSubtitleFilename(String str) {
                    int indexOf = str.indexOf(46);
                    if (indexOf != -1) {
                        str = str.substring(0, indexOf);
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(str);
                    sb.append(".srt");
                    return sb.toString();
                }

                /* JADX WARNING: Removed duplicated region for block: B:58:0x0162  */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    /*
                        r19 = this;
                        r1 = r19
                        net.sourceforge.opencamera.MyApplicationInterface r0 = net.sourceforge.opencamera.MyApplicationInterface.this
                        net.sourceforge.opencamera.MainActivity r0 = r0.main_activity
                        net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()
                        long r2 = r0.getVideoTime()
                        net.sourceforge.opencamera.MyApplicationInterface r0 = net.sourceforge.opencamera.MyApplicationInterface.this
                        net.sourceforge.opencamera.MainActivity r0 = r0.main_activity
                        net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()
                        boolean r0 = r0.isVideoRecording()
                        if (r0 != 0) goto L_0x0021
                        return
                    L_0x0021:
                        net.sourceforge.opencamera.MyApplicationInterface r0 = net.sourceforge.opencamera.MyApplicationInterface.this
                        net.sourceforge.opencamera.MainActivity r0 = r0.main_activity
                        net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()
                        boolean r0 = r0.isVideoRecordingPaused()
                        if (r0 == 0) goto L_0x0032
                        return
                    L_0x0032:
                        java.util.Date r0 = new java.util.Date
                        r0.<init>()
                        java.util.Calendar r4 = java.util.Calendar.getInstance()
                        r5 = 14
                        int r4 = r4.get(r5)
                        java.lang.String r5 = r2
                        java.lang.String r5 = net.sourceforge.opencamera.TextFormatter.getDateString(r5, r0)
                        java.lang.String r6 = r3
                        java.lang.String r0 = net.sourceforge.opencamera.TextFormatter.getTimeString(r6, r0)
                        boolean r6 = r4
                        if (r6 == 0) goto L_0x0058
                        net.sourceforge.opencamera.MyApplicationInterface r6 = net.sourceforge.opencamera.MyApplicationInterface.this
                        android.location.Location r6 = r6.getLocation()
                        goto L_0x0059
                    L_0x0058:
                        r6 = 0
                    L_0x0059:
                        boolean r8 = r5
                        if (r8 == 0) goto L_0x007c
                        net.sourceforge.opencamera.MyApplicationInterface r8 = net.sourceforge.opencamera.MyApplicationInterface.this
                        net.sourceforge.opencamera.MainActivity r8 = r8.main_activity
                        net.sourceforge.opencamera.preview.Preview r8 = r8.getPreview()
                        boolean r8 = r8.hasGeoDirection()
                        if (r8 == 0) goto L_0x007c
                        net.sourceforge.opencamera.MyApplicationInterface r8 = net.sourceforge.opencamera.MyApplicationInterface.this
                        net.sourceforge.opencamera.MainActivity r8 = r8.main_activity
                        net.sourceforge.opencamera.preview.Preview r8 = r8.getPreview()
                        double r8 = r8.getGeoDirection()
                        goto L_0x007e
                    L_0x007c:
                        r8 = 0
                    L_0x007e:
                        r16 = r8
                        net.sourceforge.opencamera.MyApplicationInterface r8 = net.sourceforge.opencamera.MyApplicationInterface.this
                        net.sourceforge.opencamera.MainActivity r8 = r8.main_activity
                        net.sourceforge.opencamera.TextFormatter r8 = r8.getTextFormatter()
                        java.lang.String r9 = r6
                        java.lang.String r10 = r7
                        boolean r11 = r4
                        r18 = 1
                        r14 = 0
                        if (r11 == 0) goto L_0x0099
                        if (r6 == 0) goto L_0x0099
                        r11 = 1
                        goto L_0x009a
                    L_0x0099:
                        r11 = 0
                    L_0x009a:
                        boolean r12 = r5
                        if (r12 == 0) goto L_0x00b0
                        net.sourceforge.opencamera.MyApplicationInterface r12 = net.sourceforge.opencamera.MyApplicationInterface.this
                        net.sourceforge.opencamera.MainActivity r12 = r12.main_activity
                        net.sourceforge.opencamera.preview.Preview r12 = r12.getPreview()
                        boolean r12 = r12.hasGeoDirection()
                        if (r12 == 0) goto L_0x00b0
                        r13 = 1
                        goto L_0x00b1
                    L_0x00b0:
                        r13 = 0
                    L_0x00b1:
                        r12 = r6
                        r7 = 0
                        r14 = r16
                        java.lang.String r8 = r8.getGPSString(r9, r10, r11, r12, r13, r14)
                        java.lang.String r9 = ""
                        int r10 = r5.length()
                        if (r10 <= 0) goto L_0x00d0
                        java.lang.StringBuilder r10 = new java.lang.StringBuilder
                        r10.<init>()
                        r10.append(r9)
                        r10.append(r5)
                        java.lang.String r9 = r10.toString()
                    L_0x00d0:
                        int r5 = r0.length()
                        if (r5 <= 0) goto L_0x00fc
                        int r5 = r9.length()
                        if (r5 <= 0) goto L_0x00ed
                        java.lang.StringBuilder r5 = new java.lang.StringBuilder
                        r5.<init>()
                        r5.append(r9)
                        java.lang.String r9 = " "
                        r5.append(r9)
                        java.lang.String r9 = r5.toString()
                    L_0x00ed:
                        java.lang.StringBuilder r5 = new java.lang.StringBuilder
                        r5.<init>()
                        r5.append(r9)
                        r5.append(r0)
                        java.lang.String r9 = r5.toString()
                    L_0x00fc:
                        java.lang.StringBuilder r5 = new java.lang.StringBuilder
                        r5.<init>()
                        int r0 = r9.length()
                        if (r0 <= 0) goto L_0x010f
                        r5.append(r9)
                        java.lang.String r0 = "\n"
                        r5.append(r0)
                    L_0x010f:
                        int r0 = r8.length()
                        if (r0 <= 0) goto L_0x01cb
                        boolean r0 = r4
                        if (r0 == 0) goto L_0x015f
                        java.lang.String r0 = r8
                        java.lang.String r9 = "preference_stamp_geo_address_no"
                        boolean r0 = r0.equals(r9)
                        if (r0 != 0) goto L_0x015f
                        boolean r0 = android.location.Geocoder.isPresent()
                        if (r0 == 0) goto L_0x015f
                        android.location.Geocoder r9 = new android.location.Geocoder
                        net.sourceforge.opencamera.MyApplicationInterface r0 = net.sourceforge.opencamera.MyApplicationInterface.this
                        net.sourceforge.opencamera.MainActivity r0 = r0.main_activity
                        java.util.Locale r10 = java.util.Locale.getDefault()
                        r9.<init>(r0, r10)
                        double r10 = r6.getLatitude()     // Catch:{ Exception -> 0x0154 }
                        double r12 = r6.getLongitude()     // Catch:{ Exception -> 0x0154 }
                        r14 = 1
                        java.util.List r0 = r9.getFromLocation(r10, r12, r14)     // Catch:{ Exception -> 0x0154 }
                        if (r0 == 0) goto L_0x015f
                        int r6 = r0.size()     // Catch:{ Exception -> 0x0154 }
                        if (r6 <= 0) goto L_0x015f
                        java.lang.Object r0 = r0.get(r7)     // Catch:{ Exception -> 0x0154 }
                        android.location.Address r0 = (android.location.Address) r0     // Catch:{ Exception -> 0x0154 }
                        goto L_0x0160
                    L_0x0154:
                        r0 = move-exception
                        java.lang.String r6 = "MyApplicationInterface"
                        java.lang.String r9 = "failed to read from geocoder"
                        android.util.Log.e(r6, r9)
                        r0.printStackTrace()
                    L_0x015f:
                        r0 = 0
                    L_0x0160:
                        if (r0 == 0) goto L_0x0178
                        r6 = 0
                    L_0x0163:
                        int r9 = r0.getMaxAddressLineIndex()
                        if (r6 > r9) goto L_0x0178
                        java.lang.String r9 = r0.getAddressLine(r6)
                        r5.append(r9)
                        java.lang.String r9 = "\n"
                        r5.append(r9)
                        int r6 = r6 + 1
                        goto L_0x0163
                    L_0x0178:
                        if (r0 == 0) goto L_0x01c3
                        java.lang.String r0 = r8
                        java.lang.String r6 = "preference_stamp_geo_address_both"
                        boolean r0 = r0.equals(r6)
                        if (r0 == 0) goto L_0x0185
                        goto L_0x01c3
                    L_0x0185:
                        boolean r0 = r5
                        if (r0 == 0) goto L_0x01cb
                        net.sourceforge.opencamera.MyApplicationInterface r0 = net.sourceforge.opencamera.MyApplicationInterface.this
                        net.sourceforge.opencamera.MainActivity r0 = r0.main_activity
                        net.sourceforge.opencamera.TextFormatter r10 = r0.getTextFormatter()
                        java.lang.String r11 = r6
                        java.lang.String r12 = r7
                        r13 = 0
                        r14 = 0
                        boolean r0 = r5
                        if (r0 == 0) goto L_0x01af
                        net.sourceforge.opencamera.MyApplicationInterface r0 = net.sourceforge.opencamera.MyApplicationInterface.this
                        net.sourceforge.opencamera.MainActivity r0 = r0.main_activity
                        net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()
                        boolean r0 = r0.hasGeoDirection()
                        if (r0 == 0) goto L_0x01af
                        r15 = 1
                        goto L_0x01b0
                    L_0x01af:
                        r15 = 0
                    L_0x01b0:
                        java.lang.String r0 = r10.getGPSString(r11, r12, r13, r14, r15, r16)
                        int r6 = r0.length()
                        if (r6 <= 0) goto L_0x01cb
                        r5.append(r0)
                        java.lang.String r0 = "\n"
                        r5.append(r0)
                        goto L_0x01cb
                    L_0x01c3:
                        r5.append(r8)
                        java.lang.String r0 = "\n"
                        r5.append(r0)
                    L_0x01cb:
                        int r0 = r5.length()
                        if (r0 != 0) goto L_0x01d2
                        return
                    L_0x01d2:
                        long r6 = (long) r4
                        long r2 = r2 - r6
                        r6 = 999(0x3e7, double:4.936E-321)
                        long r6 = r6 + r2
                        long r8 = r1.min_video_time_from
                        int r0 = (r2 > r8 ? 1 : (r2 == r8 ? 0 : -1))
                        if (r0 >= 0) goto L_0x01de
                        r2 = r8
                    L_0x01de:
                        r8 = 1
                        long r8 = r8 + r6
                        r1.min_video_time_from = r8
                        java.lang.String r0 = net.sourceforge.opencamera.TextFormatter.formatTimeMS(r2)
                        java.lang.String r2 = net.sourceforge.opencamera.TextFormatter.formatTimeMS(r6)
                        monitor-enter(r19)     // Catch:{ IOException -> 0x028f }
                        java.io.OutputStreamWriter r3 = r1.writer     // Catch:{ all -> 0x028c }
                        if (r3 != 0) goto L_0x0245
                        int r3 = r9     // Catch:{ all -> 0x028c }
                        if (r3 != 0) goto L_0x020a
                        net.sourceforge.opencamera.MyApplicationInterface r3 = net.sourceforge.opencamera.MyApplicationInterface.this     // Catch:{ all -> 0x028c }
                        java.io.File r3 = r3.last_video_file     // Catch:{ all -> 0x028c }
                        java.lang.String r3 = r3.getAbsolutePath()     // Catch:{ all -> 0x028c }
                        java.lang.String r3 = r1.getSubtitleFilename(r3)     // Catch:{ all -> 0x028c }
                        java.io.FileWriter r4 = new java.io.FileWriter     // Catch:{ all -> 0x028c }
                        r4.<init>(r3)     // Catch:{ all -> 0x028c }
                        r1.writer = r4     // Catch:{ all -> 0x028c }
                        goto L_0x0245
                    L_0x020a:
                        net.sourceforge.opencamera.MyApplicationInterface r3 = net.sourceforge.opencamera.MyApplicationInterface.this     // Catch:{ all -> 0x028c }
                        net.sourceforge.opencamera.StorageUtils r3 = r3.storageUtils     // Catch:{ all -> 0x028c }
                        net.sourceforge.opencamera.MyApplicationInterface r4 = net.sourceforge.opencamera.MyApplicationInterface.this     // Catch:{ all -> 0x028c }
                        android.net.Uri r4 = r4.last_video_file_saf     // Catch:{ all -> 0x028c }
                        java.lang.String r3 = r3.getFileName(r4)     // Catch:{ all -> 0x028c }
                        java.lang.String r3 = r1.getSubtitleFilename(r3)     // Catch:{ all -> 0x028c }
                        net.sourceforge.opencamera.MyApplicationInterface r4 = net.sourceforge.opencamera.MyApplicationInterface.this     // Catch:{ all -> 0x028c }
                        net.sourceforge.opencamera.StorageUtils r4 = r4.storageUtils     // Catch:{ all -> 0x028c }
                        java.lang.String r6 = ""
                        android.net.Uri r3 = r4.createOutputFileSAF(r3, r6)     // Catch:{ all -> 0x028c }
                        net.sourceforge.opencamera.MyApplicationInterface r4 = net.sourceforge.opencamera.MyApplicationInterface.this     // Catch:{ all -> 0x028c }
                        android.content.Context r4 = r4.getContext()     // Catch:{ all -> 0x028c }
                        android.content.ContentResolver r4 = r4.getContentResolver()     // Catch:{ all -> 0x028c }
                        java.lang.String r6 = "w"
                        android.os.ParcelFileDescriptor r3 = r4.openFileDescriptor(r3, r6)     // Catch:{ all -> 0x028c }
                        java.io.FileWriter r4 = new java.io.FileWriter     // Catch:{ all -> 0x028c }
                        java.io.FileDescriptor r3 = r3.getFileDescriptor()     // Catch:{ all -> 0x028c }
                        r4.<init>(r3)     // Catch:{ all -> 0x028c }
                        r1.writer = r4     // Catch:{ all -> 0x028c }
                    L_0x0245:
                        java.io.OutputStreamWriter r3 = r1.writer     // Catch:{ all -> 0x028c }
                        if (r3 == 0) goto L_0x0284
                        java.io.OutputStreamWriter r3 = r1.writer     // Catch:{ all -> 0x028c }
                        int r4 = r1.count     // Catch:{ all -> 0x028c }
                        java.lang.String r4 = java.lang.Integer.toString(r4)     // Catch:{ all -> 0x028c }
                        r3.append(r4)     // Catch:{ all -> 0x028c }
                        java.io.OutputStreamWriter r3 = r1.writer     // Catch:{ all -> 0x028c }
                        r4 = 10
                        r3.append(r4)     // Catch:{ all -> 0x028c }
                        java.io.OutputStreamWriter r3 = r1.writer     // Catch:{ all -> 0x028c }
                        r3.append(r0)     // Catch:{ all -> 0x028c }
                        java.io.OutputStreamWriter r0 = r1.writer     // Catch:{ all -> 0x028c }
                        java.lang.String r3 = " --> "
                        r0.append(r3)     // Catch:{ all -> 0x028c }
                        java.io.OutputStreamWriter r0 = r1.writer     // Catch:{ all -> 0x028c }
                        r0.append(r2)     // Catch:{ all -> 0x028c }
                        java.io.OutputStreamWriter r0 = r1.writer     // Catch:{ all -> 0x028c }
                        r0.append(r4)     // Catch:{ all -> 0x028c }
                        java.io.OutputStreamWriter r0 = r1.writer     // Catch:{ all -> 0x028c }
                        java.lang.String r2 = r5.toString()     // Catch:{ all -> 0x028c }
                        r0.append(r2)     // Catch:{ all -> 0x028c }
                        java.io.OutputStreamWriter r0 = r1.writer     // Catch:{ all -> 0x028c }
                        r0.append(r4)     // Catch:{ all -> 0x028c }
                        java.io.OutputStreamWriter r0 = r1.writer     // Catch:{ all -> 0x028c }
                        r0.flush()     // Catch:{ all -> 0x028c }
                    L_0x0284:
                        monitor-exit(r19)     // Catch:{ all -> 0x028c }
                        int r0 = r1.count     // Catch:{ IOException -> 0x028f }
                        int r0 = r0 + 1
                        r1.count = r0     // Catch:{ IOException -> 0x028f }
                        goto L_0x0293
                    L_0x028c:
                        r0 = move-exception
                        monitor-exit(r19)     // Catch:{ all -> 0x028c }
                        throw r0     // Catch:{ IOException -> 0x028f }
                    L_0x028f:
                        r0 = move-exception
                        r0.printStackTrace()
                    L_0x0293:
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MyApplicationInterface.AnonymousClass1SubtitleVideoTimerTask.run():void");
                }

                public boolean cancel() {
                    synchronized (this) {
                        if (this.writer != null) {
                            try {
                                this.writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            this.writer = null;
                        }
                    }
                    return super.cancel();
                }
            };
            this.subtitleVideoTimerTask = r0;
            timer.schedule(r0, 0, 1000);
        }
    }

    public void stoppingVideo() {
        this.main_activity.unlockScreen();
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.take_photo);
        imageButton.setImageResource(C0316R.C0317drawable.take_video_selector);
        imageButton.setContentDescription(getContext().getResources().getString(C0316R.string.start_video));
        imageButton.setTag(Integer.valueOf(C0316R.C0317drawable.take_video_selector));
    }

    /* JADX WARNING: type inference failed for: r1v2 */
    /* JADX WARNING: type inference failed for: r1v5, types: [android.content.Intent] */
    /* JADX WARNING: type inference failed for: r1v9 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0083  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00a4  */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stoppedVideo(int r7, android.net.Uri r8, java.lang.String r9) {
        /*
            r6 = this;
            net.sourceforge.opencamera.MainActivity r0 = r6.main_activity
            r1 = 2131099716(0x7f060044, float:1.7811793E38)
            android.view.View r0 = r0.findViewById(r1)
            r1 = 8
            r0.setVisibility(r1)
            net.sourceforge.opencamera.MainActivity r0 = r6.main_activity
            r2 = 2131099736(0x7f060058, float:1.7811834E38)
            android.view.View r0 = r0.findViewById(r2)
            r0.setVisibility(r1)
            net.sourceforge.opencamera.MainActivity r0 = r6.main_activity
            net.sourceforge.opencamera.ui.MainUI r0 = r0.getMainUI()
            r0.setPauseVideoContentDescription()
            net.sourceforge.opencamera.MainActivity r0 = r6.main_activity
            net.sourceforge.opencamera.ui.MainUI r0 = r0.getMainUI()
            r0.destroyPopup()
            net.sourceforge.opencamera.MainActivity r0 = r6.main_activity
            net.sourceforge.opencamera.ui.MainUI r0 = r0.getMainUI()
            boolean r0 = r0.isExposureUIOpen()
            if (r0 == 0) goto L_0x0041
            net.sourceforge.opencamera.MainActivity r0 = r6.main_activity
            net.sourceforge.opencamera.ui.MainUI r0 = r0.getMainUI()
            r0.setupExposureUI()
        L_0x0041:
            java.util.TimerTask r0 = r6.subtitleVideoTimerTask
            r1 = 0
            if (r0 == 0) goto L_0x004b
            r0.cancel()
            r6.subtitleVideoTimerTask = r1
        L_0x004b:
            r0 = 0
            r2 = 1
            if (r7 != 0) goto L_0x005d
            if (r9 == 0) goto L_0x0070
            java.io.File r3 = new java.io.File
            r3.<init>(r9)
            net.sourceforge.opencamera.StorageUtils r4 = r6.storageUtils
            r4.broadcastFile(r3, r0, r2, r2)
        L_0x005b:
            r3 = 1
            goto L_0x0071
        L_0x005d:
            if (r8 == 0) goto L_0x0070
            net.sourceforge.opencamera.StorageUtils r3 = r6.storageUtils
            java.io.File r3 = r3.broadcastUri(r8, r0, r2, r2)
            if (r3 == 0) goto L_0x005b
            net.sourceforge.opencamera.MainActivity r4 = r6.main_activity
            java.lang.String r3 = r3.getAbsolutePath()
            r4.test_last_saved_image = r3
            goto L_0x005b
        L_0x0070:
            r3 = 0
        L_0x0071:
            net.sourceforge.opencamera.MainActivity r4 = r6.main_activity
            android.content.Intent r4 = r4.getIntent()
            java.lang.String r4 = r4.getAction()
            java.lang.String r5 = "android.media.action.VIDEO_CAPTURE"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L_0x00a4
            if (r3 == 0) goto L_0x0089
            if (r7 != 0) goto L_0x0089
            goto L_0x0137
        L_0x0089:
            if (r3 == 0) goto L_0x0095
            if (r7 != r2) goto L_0x0095
            android.content.Intent r1 = new android.content.Intent
            r1.<init>()
            r1.setData(r8)
        L_0x0095:
            net.sourceforge.opencamera.MainActivity r7 = r6.main_activity
            if (r3 == 0) goto L_0x009a
            r0 = -1
        L_0x009a:
            r7.setResult(r0, r1)
            net.sourceforge.opencamera.MainActivity r7 = r6.main_activity
            r7.finish()
            goto L_0x0137
        L_0x00a4:
            if (r3 == 0) goto L_0x0137
            java.lang.System.currentTimeMillis()
            android.media.MediaMetadataRetriever r0 = new android.media.MediaMetadataRetriever
            r0.<init>()
            if (r7 != 0) goto L_0x00bd
            java.io.File r7 = new java.io.File     // Catch:{ FileNotFoundException -> 0x00e2, RuntimeException -> 0x00e0 }
            r7.<init>(r9)     // Catch:{ FileNotFoundException -> 0x00e2, RuntimeException -> 0x00e0 }
            java.lang.String r7 = r7.getPath()     // Catch:{ FileNotFoundException -> 0x00e2, RuntimeException -> 0x00e0 }
            r0.setDataSource(r7)     // Catch:{ FileNotFoundException -> 0x00e2, RuntimeException -> 0x00e0 }
            goto L_0x00d2
        L_0x00bd:
            android.content.Context r7 = r6.getContext()     // Catch:{ FileNotFoundException -> 0x00e2, RuntimeException -> 0x00e0 }
            android.content.ContentResolver r7 = r7.getContentResolver()     // Catch:{ FileNotFoundException -> 0x00e2, RuntimeException -> 0x00e0 }
            java.lang.String r9 = "r"
            android.os.ParcelFileDescriptor r7 = r7.openFileDescriptor(r8, r9)     // Catch:{ FileNotFoundException -> 0x00e2, RuntimeException -> 0x00e0 }
            java.io.FileDescriptor r7 = r7.getFileDescriptor()     // Catch:{ FileNotFoundException -> 0x00e2, RuntimeException -> 0x00e0 }
            r0.setDataSource(r7)     // Catch:{ FileNotFoundException -> 0x00e2, RuntimeException -> 0x00e0 }
        L_0x00d2:
            r7 = -1
            android.graphics.Bitmap r1 = r0.getFrameAtTime(r7)     // Catch:{ FileNotFoundException -> 0x00e2, RuntimeException -> 0x00e0 }
        L_0x00d8:
            r0.release()     // Catch:{ RuntimeException -> 0x00dc }
            goto L_0x00ee
        L_0x00dc:
            goto L_0x00ee
        L_0x00de:
            r7 = move-exception
            goto L_0x0133
        L_0x00e0:
            r7 = move-exception
            goto L_0x00e3
        L_0x00e2:
            r7 = move-exception
        L_0x00e3:
            java.lang.String r8 = "MyApplicationInterface"
            java.lang.String r9 = "failed to find thumbnail"
            android.util.Log.d(r8, r9)     // Catch:{ all -> 0x00de }
            r7.printStackTrace()     // Catch:{ all -> 0x00de }
            goto L_0x00d8
        L_0x00ee:
            if (r1 == 0) goto L_0x0137
            net.sourceforge.opencamera.MainActivity r7 = r6.main_activity
            r8 = 2131099692(0x7f06002c, float:1.7811744E38)
            android.view.View r7 = r7.findViewById(r8)
            android.widget.ImageButton r7 = (android.widget.ImageButton) r7
            int r8 = r1.getWidth()
            int r9 = r1.getHeight()
            int r0 = r7.getWidth()
            if (r8 <= r0) goto L_0x0127
            int r7 = r7.getWidth()
            float r7 = (float) r7
            float r8 = (float) r8
            float r7 = r7 / r8
            float r8 = r8 * r7
            int r8 = java.lang.Math.round(r8)
            float r9 = (float) r9
            float r7 = r7 * r9
            int r7 = java.lang.Math.round(r7)
            android.graphics.Bitmap r7 = android.graphics.Bitmap.createScaledBitmap(r1, r8, r7, r2)
            if (r7 == r1) goto L_0x0127
            r1.recycle()
            goto L_0x0128
        L_0x0127:
            r7 = r1
        L_0x0128:
            net.sourceforge.opencamera.MainActivity r8 = r6.main_activity
            net.sourceforge.opencamera.MyApplicationInterface$2 r9 = new net.sourceforge.opencamera.MyApplicationInterface$2
            r9.<init>(r7)
            r8.runOnUiThread(r9)
            goto L_0x0137
        L_0x0133:
            r0.release()     // Catch:{ RuntimeException -> 0x0136 }
        L_0x0136:
            throw r7
        L_0x0137:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MyApplicationInterface.stoppedVideo(int, android.net.Uri, java.lang.String):void");
    }

    public void onVideoInfo(int i, int i2) {
        if (VERSION.SDK_INT >= 26 && i == 803) {
            this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.video_max_filesize);
        } else if (i == 801) {
            this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.video_max_filesize);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("info_");
        sb.append(i);
        sb.append("_");
        sb.append(i2);
        String sb2 = sb.toString();
        Editor edit = this.sharedPreferences.edit();
        edit.putString("last_video_error", sb2);
        edit.apply();
    }

    public void onFailedStartPreview() {
        this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.failed_to_start_camera_preview);
    }

    public void onCameraError() {
        this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.camera_error);
    }

    public void onPhotoError() {
        this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.failed_to_take_picture);
    }

    public void onVideoError(int i, int i2) {
        this.main_activity.getPreview().showToast((ToastBoxer) null, i == 100 ? C0316R.string.video_error_server_died : C0316R.string.video_error_unknown);
        StringBuilder sb = new StringBuilder();
        sb.append("error_");
        sb.append(i);
        sb.append("_");
        sb.append(i2);
        String sb2 = sb.toString();
        Editor edit = this.sharedPreferences.edit();
        edit.putString("last_video_error", sb2);
        edit.apply();
    }

    public void onVideoRecordStartError(VideoProfile videoProfile) {
        String str;
        String errorFeatures = this.main_activity.getPreview().getErrorFeatures(videoProfile);
        if (errorFeatures.length() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(getContext().getResources().getString(C0316R.string.sorry));
            sb.append(", ");
            sb.append(errorFeatures);
            sb.append(" ");
            sb.append(getContext().getResources().getString(C0316R.string.not_supported));
            str = sb.toString();
        } else {
            str = getContext().getResources().getString(C0316R.string.failed_to_record_video);
        }
        this.main_activity.getPreview().showToast((ToastBoxer) null, str);
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.take_photo);
        imageButton.setImageResource(C0316R.C0317drawable.take_video_selector);
        imageButton.setContentDescription(getContext().getResources().getString(C0316R.string.start_video));
        imageButton.setTag(Integer.valueOf(C0316R.C0317drawable.take_video_selector));
    }

    public void onVideoRecordStopError(VideoProfile videoProfile) {
        String errorFeatures = this.main_activity.getPreview().getErrorFeatures(videoProfile);
        String string = getContext().getResources().getString(C0316R.string.video_may_be_corrupted);
        if (errorFeatures.length() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(string);
            sb.append(", ");
            sb.append(errorFeatures);
            sb.append(" ");
            sb.append(getContext().getResources().getString(C0316R.string.not_supported));
            string = sb.toString();
        }
        this.main_activity.getPreview().showToast((ToastBoxer) null, string);
    }

    public void onFailedReconnectError() {
        this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.failed_to_reconnect_camera);
    }

    public void onFailedCreateVideoFileError() {
        this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.failed_to_save_video);
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.take_photo);
        imageButton.setImageResource(C0316R.C0317drawable.take_video_selector);
        imageButton.setContentDescription(getContext().getResources().getString(C0316R.string.start_video));
        imageButton.setTag(Integer.valueOf(C0316R.C0317drawable.take_video_selector));
    }

    public void hasPausedPreview(boolean z) {
        View findViewById = this.main_activity.findViewById(C0316R.C0318id.share);
        View findViewById2 = this.main_activity.findViewById(C0316R.C0318id.trash);
        if (z) {
            findViewById.setVisibility(0);
            findViewById2.setVisibility(0);
            return;
        }
        findViewById.setVisibility(8);
        findViewById2.setVisibility(8);
        clearLastImages();
    }

    public void cameraInOperation(boolean z, boolean z2) {
        if (!z && this.used_front_screen_flash) {
            this.main_activity.setBrightnessForCamera(false);
            this.used_front_screen_flash = false;
        }
        this.drawPreview.cameraInOperation(z);
        this.main_activity.getMainUI().showGUI(!z, z2);
    }

    public void turnFrontScreenFlashOn() {
        this.used_front_screen_flash = true;
        this.main_activity.setBrightnessForCamera(true);
        this.drawPreview.turnFrontScreenFlashOn();
    }

    public void onCaptureStarted() {
        this.n_capture_images = 0;
        this.n_capture_images_raw = 0;
        this.drawPreview.onCaptureStarted();
    }

    public void onPictureCompleted() {
        PhotoMode photoMode = getPhotoMode();
        if (this.main_activity.getPreview().isVideo()) {
            photoMode = PhotoMode.Standard;
        }
        if (photoMode == PhotoMode.NoiseReduction) {
            this.imageSaver.finishImageBatch(saveInBackground(isImageCaptureIntent()));
        } else if (photoMode != PhotoMode.Panorama || !this.gyroSensor.isRecording()) {
            if (photoMode == PhotoMode.FocusBracketing && getShutterSoundPref()) {
                MediaPlayer create = MediaPlayer.create(getContext(), System.DEFAULT_NOTIFICATION_URI);
                if (create != null) {
                    create.start();
                }
            }
        } else if (this.panorama_pic_accepted) {
            setNextPanoramaPoint(false);
        } else {
            setNextPanoramaPoint(true);
        }
        this.drawPreview.cameraInOperation(false);
    }

    public void cameraClosed() {
        stopPanorama(true);
        this.main_activity.getMainUI().clearSeekBar();
        this.main_activity.getMainUI().destroyPopup();
        this.drawPreview.clearContinuousFocusMove();
    }

    /* access modifiers changed from: 0000 */
    public void updateThumbnail(Bitmap bitmap, boolean z) {
        this.main_activity.updateGalleryIcon(bitmap);
        this.drawPreview.updateThumbnail(bitmap, z, true);
        if (!z && getPausePreviewPref()) {
            this.drawPreview.showLastImage();
        }
    }

    public void timerBeep(long j) {
        boolean z = true;
        if (this.sharedPreferences.getBoolean(PreferenceKeys.getTimerBeepPreferenceKey(), true)) {
            if (j > 1000) {
                z = false;
            }
            this.main_activity.getSoundPoolManager().playSound(z ? C0316R.raw.mybeep_hi : C0316R.raw.mybeep);
        }
        if (this.sharedPreferences.getBoolean(PreferenceKeys.getTimerSpeakPreferenceKey(), false)) {
            int i = (int) (j / 1000);
            if (i <= 60) {
                MainActivity mainActivity = this.main_activity;
                StringBuilder sb = new StringBuilder();
                sb.append(BuildConfig.FLAVOR);
                sb.append(i);
                mainActivity.speak(sb.toString());
            }
        }
    }

    public void multitouchZoom(int i) {
        this.main_activity.getMainUI().setSeekbarZoom(i);
    }

    /* access modifiers changed from: 0000 */
    public void switchToCamera(boolean z) {
        int numberOfCameras = this.main_activity.getPreview().getCameraControllerManager().getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            if (this.main_activity.getPreview().getCameraControllerManager().isFrontFacing(i) == z) {
                setCameraIdPref(i);
                return;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean hasSetCameraId() {
        return this.has_set_cameraId;
    }

    public void setCameraIdPref(int i) {
        this.has_set_cameraId = true;
        this.cameraId = i;
    }

    public void setFlashPref(String str) {
        Editor edit = this.sharedPreferences.edit();
        edit.putString(PreferenceKeys.getFlashPreferenceKey(this.cameraId), str);
        edit.apply();
    }

    public void setFocusPref(String str, boolean z) {
        Editor edit = this.sharedPreferences.edit();
        edit.putString(PreferenceKeys.getFocusPreferenceKey(this.cameraId, z), str);
        edit.apply();
        this.main_activity.setManualFocusSeekBarVisibility(false);
    }

    public void setVideoPref(boolean z) {
        Editor edit = this.sharedPreferences.edit();
        edit.putBoolean(PreferenceKeys.IsVideoPreferenceKey, z);
        edit.apply();
    }

    public void setSceneModePref(String str) {
        Editor edit = this.sharedPreferences.edit();
        edit.putString(PreferenceKeys.SceneModePreferenceKey, str);
        edit.apply();
    }

    public void clearSceneModePref() {
        Editor edit = this.sharedPreferences.edit();
        edit.remove(PreferenceKeys.SceneModePreferenceKey);
        edit.apply();
    }

    public void setColorEffectPref(String str) {
        Editor edit = this.sharedPreferences.edit();
        edit.putString(PreferenceKeys.ColorEffectPreferenceKey, str);
        edit.apply();
    }

    public void clearColorEffectPref() {
        Editor edit = this.sharedPreferences.edit();
        edit.remove(PreferenceKeys.ColorEffectPreferenceKey);
        edit.apply();
    }

    public void setWhiteBalancePref(String str) {
        Editor edit = this.sharedPreferences.edit();
        edit.putString(PreferenceKeys.WhiteBalancePreferenceKey, str);
        edit.apply();
    }

    public void clearWhiteBalancePref() {
        Editor edit = this.sharedPreferences.edit();
        edit.remove(PreferenceKeys.WhiteBalancePreferenceKey);
        edit.apply();
    }

    public void setWhiteBalanceTemperaturePref(int i) {
        Editor edit = this.sharedPreferences.edit();
        edit.putInt(PreferenceKeys.WhiteBalanceTemperaturePreferenceKey, i);
        edit.apply();
    }

    public void setISOPref(String str) {
        Editor edit = this.sharedPreferences.edit();
        edit.putString(PreferenceKeys.ISOPreferenceKey, str);
        edit.apply();
    }

    public void clearISOPref() {
        Editor edit = this.sharedPreferences.edit();
        edit.remove(PreferenceKeys.ISOPreferenceKey);
        edit.apply();
    }

    public void setExposureCompensationPref(int i) {
        Editor edit = this.sharedPreferences.edit();
        StringBuilder sb = new StringBuilder();
        sb.append(BuildConfig.FLAVOR);
        sb.append(i);
        edit.putString(PreferenceKeys.ExposurePreferenceKey, sb.toString());
        edit.apply();
    }

    public void clearExposureCompensationPref() {
        Editor edit = this.sharedPreferences.edit();
        edit.remove(PreferenceKeys.ExposurePreferenceKey);
        edit.apply();
    }

    public void setCameraResolutionPref(int i, int i2) {
        if (getPhotoMode() != PhotoMode.Panorama) {
            StringBuilder sb = new StringBuilder();
            sb.append(i);
            sb.append(" ");
            sb.append(i2);
            String sb2 = sb.toString();
            Editor edit = this.sharedPreferences.edit();
            edit.putString(PreferenceKeys.getResolutionPreferenceKey(this.cameraId), sb2);
            edit.apply();
        }
    }

    public void setVideoQualityPref(String str) {
        Editor edit = this.sharedPreferences.edit();
        edit.putString(PreferenceKeys.getVideoQualityPreferenceKey(this.cameraId, fpsIsHighSpeed()), str);
        edit.apply();
    }

    public void setZoomPref(int i) {
        this.zoom_factor = i;
    }

    public void requestCameraPermission() {
        this.main_activity.getPermissionHandler().requestCameraPermission();
    }

    public void requestStoragePermission() {
        this.main_activity.getPermissionHandler().requestStoragePermission();
    }

    public void requestRecordAudioPermission() {
        this.main_activity.getPermissionHandler().requestRecordAudioPermission();
    }

    public void setExposureTimePref(long j) {
        Editor edit = this.sharedPreferences.edit();
        edit.putLong(PreferenceKeys.ExposureTimePreferenceKey, j);
        edit.apply();
    }

    public void clearExposureTimePref() {
        Editor edit = this.sharedPreferences.edit();
        edit.remove(PreferenceKeys.ExposureTimePreferenceKey);
        edit.apply();
    }

    public void setFocusDistancePref(float f, boolean z) {
        Editor edit = this.sharedPreferences.edit();
        edit.putFloat(z ? PreferenceKeys.FocusBracketingTargetDistancePreferenceKey : PreferenceKeys.FocusDistancePreferenceKey, f);
        edit.apply();
    }

    private int getStampFontColor() {
        return Color.parseColor(this.sharedPreferences.getString(PreferenceKeys.StampFontColorPreferenceKey, "#ffffff"));
    }

    /* access modifiers changed from: 0000 */
    public void reset() {
        this.zoom_factor = 0;
    }

    public void onDrawPreview(Canvas canvas) {
        if (!this.main_activity.isCameraInBackground()) {
            this.drawPreview.onDrawPreview(canvas);
        }
    }

    public int drawTextWithBackground(Canvas canvas, Paint paint, String str, int i, int i2, int i3, int i4) {
        return drawTextWithBackground(canvas, paint, str, i, i2, i3, i4, Alignment.ALIGNMENT_BOTTOM);
    }

    public int drawTextWithBackground(Canvas canvas, Paint paint, String str, int i, int i2, int i3, int i4, Alignment alignment) {
        return drawTextWithBackground(canvas, paint, str, i, i2, i3, i4, alignment, null, Shadow.SHADOW_OUTLINE);
    }

    public int drawTextWithBackground(Canvas canvas, Paint paint, String str, int i, int i2, int i3, int i4, Alignment alignment, String str2, Shadow shadow) {
        return drawTextWithBackground(canvas, paint, str, i, i2, i3, i4, alignment, null, shadow, null);
    }

    public int drawTextWithBackground(Canvas canvas, Paint paint, String str, int i, int i2, int i3, int i4, Alignment alignment, String str2, Shadow shadow, Rect rect) {
        int i5;
        int i6;
        Canvas canvas2 = canvas;
        Paint paint2 = paint;
        String str3 = str;
        int i7 = i2;
        int i8 = i3;
        Alignment alignment2 = alignment;
        String str4 = str2;
        Shadow shadow2 = shadow;
        Rect rect2 = rect;
        float f = getContext().getResources().getDisplayMetrics().density;
        paint2.setStyle(Style.FILL);
        paint2.setColor(i7);
        paint2.setAlpha(64);
        if (rect2 != null) {
            this.text_bounds.set(rect2);
        } else {
            if (str4 != null) {
                paint2.getTextBounds(str4, 0, str2.length(), this.text_bounds);
                i6 = this.text_bounds.bottom - this.text_bounds.top;
            } else {
                i6 = 0;
            }
            paint2.getTextBounds(str3, 0, str.length(), this.text_bounds);
            if (str4 != null) {
                Rect rect3 = this.text_bounds;
                rect3.bottom = rect3.top + i6;
            }
        }
        int i9 = (int) ((f * 2.0f) + 0.5f);
        if (paint.getTextAlign() == Align.RIGHT || paint.getTextAlign() == Align.CENTER) {
            float measureText = paint.measureText(str);
            if (paint.getTextAlign() == Align.CENTER) {
                measureText /= 2.0f;
            }
            Rect rect4 = this.text_bounds;
            rect4.left = (int) (((float) rect4.left) - measureText);
            Rect rect5 = this.text_bounds;
            rect5.right = (int) (((float) rect5.right) - measureText);
        }
        this.text_bounds.left += i8 - i9;
        this.text_bounds.right += i8 + i9;
        int i10 = ((-this.text_bounds.top) + i9) - 1;
        if (alignment2 == Alignment.ALIGNMENT_TOP) {
            int i11 = (this.text_bounds.bottom - this.text_bounds.top) + (i9 * 2);
            Rect rect6 = this.text_bounds;
            rect6.top = i4 - 1;
            rect6.bottom = rect6.top + i11;
            i5 = i4 + i10;
        } else if (alignment2 == Alignment.ALIGNMENT_CENTRE) {
            int i12 = (this.text_bounds.bottom - this.text_bounds.top) + (i9 * 2);
            Rect rect7 = this.text_bounds;
            double d = (double) ((i4 - 1) + ((rect7.top + i4) - i9));
            Double.isNaN(d);
            rect7.top = (int) (d * 0.5d);
            Rect rect8 = this.text_bounds;
            rect8.bottom = rect8.top + i12;
            double d2 = (double) i10;
            Double.isNaN(d2);
            i5 = i4 + ((int) (d2 * 0.5d));
        } else {
            this.text_bounds.top += i4 - i9;
            this.text_bounds.bottom += i4 + i9;
            i5 = i4;
        }
        if (shadow2 == Shadow.SHADOW_BACKGROUND) {
            paint2.setColor(i7);
            paint2.setAlpha(64);
            canvas2.drawRect(this.text_bounds, paint2);
            paint2.setAlpha(255);
        }
        paint2.setColor(i);
        float f2 = (float) i8;
        float f3 = (float) i5;
        canvas2.drawText(str3, f2, f3, paint2);
        if (shadow2 == Shadow.SHADOW_OUTLINE) {
            paint2.setColor(i7);
            paint2.setStyle(Style.STROKE);
            float strokeWidth = paint.getStrokeWidth();
            paint2.setStrokeWidth(1.0f);
            canvas2.drawText(str3, f2, f3, paint2);
            paint2.setStyle(Style.FILL);
            paint2.setStrokeWidth(strokeWidth);
        }
        return this.text_bounds.bottom - this.text_bounds.top;
    }

    private boolean saveInBackground(boolean z) {
        return !z && !getPausePreviewPref();
    }

    /* access modifiers changed from: 0000 */
    public boolean isImageCaptureIntent() {
        String action = this.main_activity.getIntent().getAction();
        return "android.media.action.IMAGE_CAPTURE".equals(action) || "android.media.action.IMAGE_CAPTURE_SECURE".equals(action);
    }

    private boolean forceSuffix(PhotoMode photoMode) {
        return photoMode == PhotoMode.FocusBracketing || photoMode == PhotoMode.FastBurst || (this.main_activity.getPreview().getCameraController() != null && this.main_activity.getPreview().getCameraController().isCapturingBurst());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:103:0x0265, code lost:
        if (r0.n_panorama_pics == 0) goto L_0x0267;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0269, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x026d, code lost:
        if (r0.n_capture_images == 1) goto L_0x0267;
     */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x0261  */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x026b  */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x0272  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0052  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x0372  */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x037a  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0384  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0103  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x010a  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0125  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0160  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x01ad  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x01f0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean saveImage(boolean r48, java.util.List<byte[]> r49, java.util.Date r50) {
        /*
            r47 = this;
            r0 = r47
            java.lang.System.gc()
            boolean r8 = r47.isImageCaptureIntent()
            r40 = 0
            if (r8 == 0) goto L_0x0023
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            android.content.Intent r1 = r1.getIntent()
            android.os.Bundle r1 = r1.getExtras()
            if (r1 == 0) goto L_0x0023
            java.lang.String r2 = "output"
            android.os.Parcelable r1 = r1.getParcelable(r2)
            android.net.Uri r1 = (android.net.Uri) r1
            r9 = r1
            goto L_0x0025
        L_0x0023:
            r9 = r40
        L_0x0025:
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r1 = r1.getPreview()
            boolean r10 = r1.usingCamera2API()
            net.sourceforge.opencamera.ImageSaver$Request$ImageFormat r11 = r47.getImageFormatPref()
            int r12 = r47.getSaveImageQualityPref()
            boolean r1 = r47.getAutoStabilisePref()
            r15 = 0
            r14 = 1
            if (r1 == 0) goto L_0x004d
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r1 = r1.getPreview()
            boolean r1 = r1.hasLevelAngleStable()
            if (r1 == 0) goto L_0x004d
            r13 = 1
            goto L_0x004e
        L_0x004d:
            r13 = 0
        L_0x004e:
            r1 = 0
            if (r13 == 0) goto L_0x005d
            net.sourceforge.opencamera.MainActivity r3 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r3 = r3.getPreview()
            double r3 = r3.getLevelAngle()
            goto L_0x005e
        L_0x005d:
            r3 = r1
        L_0x005e:
            if (r13 == 0) goto L_0x006b
            net.sourceforge.opencamera.MainActivity r5 = r0.main_activity
            boolean r5 = r5.test_have_angle
            if (r5 == 0) goto L_0x006b
            net.sourceforge.opencamera.MainActivity r3 = r0.main_activity
            float r3 = r3.test_angle
            double r3 = (double) r3
        L_0x006b:
            if (r13 == 0) goto L_0x0078
            net.sourceforge.opencamera.MainActivity r5 = r0.main_activity
            boolean r5 = r5.test_low_memory
            if (r5 == 0) goto L_0x0078
            r3 = 4631530004285489152(0x4046800000000000, double:45.0)
        L_0x0078:
            r16 = r3
            net.sourceforge.opencamera.MainActivity r3 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r3 = r3.getPreview()
            net.sourceforge.opencamera.cameracontroller.CameraController r3 = r3.getCameraController()
            if (r3 == 0) goto L_0x0099
            net.sourceforge.opencamera.MainActivity r3 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r3 = r3.getPreview()
            net.sourceforge.opencamera.cameracontroller.CameraController r3 = r3.getCameraController()
            boolean r3 = r3.isFrontFacing()
            if (r3 == 0) goto L_0x0099
            r18 = 1
            goto L_0x009b
        L_0x0099:
            r18 = 0
        L_0x009b:
            if (r18 == 0) goto L_0x00b2
            android.content.SharedPreferences r3 = r0.sharedPreferences
            java.lang.String r4 = "preference_front_camera_mirror"
            java.lang.String r5 = "preference_front_camera_mirror_no"
            java.lang.String r3 = r3.getString(r4, r5)
            java.lang.String r4 = "preference_front_camera_mirror_photo"
            boolean r3 = r3.equals(r4)
            if (r3 == 0) goto L_0x00b2
            r19 = 1
            goto L_0x00b4
        L_0x00b2:
            r19 = 0
        L_0x00b4:
            java.lang.String r24 = r47.getStampPref()
            java.lang.String r25 = r47.getTextStampPref()
            int r26 = r47.getTextStampFontSizePref()
            int r27 = r47.getStampFontColor()
            android.content.SharedPreferences r3 = r0.sharedPreferences
            java.lang.String r4 = "preference_stamp_style"
            java.lang.String r5 = "preference_stamp_style_shadowed"
            java.lang.String r28 = r3.getString(r4, r5)
            java.lang.String r29 = r47.getStampDateFormatPref()
            java.lang.String r30 = r47.getStampTimeFormatPref()
            java.lang.String r31 = r47.getStampGPSFormatPref()
            java.lang.String r32 = r47.getStampGeoAddressPref()
            java.lang.String r33 = r47.getUnitsDistancePref()
            android.content.SharedPreferences r3 = r0.sharedPreferences
            java.lang.String r4 = "preference_panorama_crop_on"
            java.lang.String r5 = "preference_panorama_crop"
            java.lang.String r3 = r3.getString(r5, r4)
            boolean r34 = r3.equals(r4)
            boolean r3 = r47.getGeotaggingPref()
            if (r3 == 0) goto L_0x00ff
            android.location.Location r3 = r47.getLocation()
            if (r3 == 0) goto L_0x00ff
            r35 = 1
            goto L_0x0101
        L_0x00ff:
            r35 = 0
        L_0x0101:
            if (r35 == 0) goto L_0x010a
            android.location.Location r3 = r47.getLocation()
            r36 = r3
            goto L_0x010c
        L_0x010a:
            r36 = r40
        L_0x010c:
            net.sourceforge.opencamera.MainActivity r3 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r3 = r3.getPreview()
            boolean r3 = r3.hasGeoDirection()
            if (r3 == 0) goto L_0x0121
            boolean r3 = r47.getGeodirectionPref()
            if (r3 == 0) goto L_0x0121
            r37 = 1
            goto L_0x0123
        L_0x0121:
            r37 = 0
        L_0x0123:
            if (r37 == 0) goto L_0x012f
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r1 = r1.getPreview()
            double r1 = r1.getGeoDirection()
        L_0x012f:
            r38 = r1
            android.content.SharedPreferences r1 = r0.sharedPreferences
            java.lang.String r2 = ""
            java.lang.String r3 = "preference_exif_artist"
            java.lang.String r41 = r1.getString(r3, r2)
            android.content.SharedPreferences r1 = r0.sharedPreferences
            java.lang.String r3 = "preference_exif_copyright"
            java.lang.String r42 = r1.getString(r3, r2)
            android.content.SharedPreferences r1 = r0.sharedPreferences
            java.lang.String r2 = "preference_hdr_contrast_enhancement"
            java.lang.String r3 = "preference_hdr_contrast_enhancement_smart"
            java.lang.String r20 = r1.getString(r2, r3)
            r1 = 800(0x320, float:1.121E-42)
            r2 = 33333333(0x1fca055, double:1.64688547E-316)
            r4 = 1065353216(0x3f800000, float:1.0)
            net.sourceforge.opencamera.MainActivity r5 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r5 = r5.getPreview()
            net.sourceforge.opencamera.cameracontroller.CameraController r5 = r5.getCameraController()
            if (r5 == 0) goto L_0x01ad
            net.sourceforge.opencamera.MainActivity r4 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r4 = r4.getPreview()
            net.sourceforge.opencamera.cameracontroller.CameraController r4 = r4.getCameraController()
            boolean r4 = r4.captureResultHasIso()
            if (r4 == 0) goto L_0x017e
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r1 = r1.getPreview()
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r1.getCameraController()
            int r1 = r1.captureResultIso()
        L_0x017e:
            net.sourceforge.opencamera.MainActivity r4 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r4 = r4.getPreview()
            net.sourceforge.opencamera.cameracontroller.CameraController r4 = r4.getCameraController()
            boolean r4 = r4.captureResultHasExposureTime()
            if (r4 == 0) goto L_0x019c
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r2 = r2.getPreview()
            net.sourceforge.opencamera.cameracontroller.CameraController r2 = r2.getCameraController()
            long r2 = r2.captureResultExposureTime()
        L_0x019c:
            net.sourceforge.opencamera.MainActivity r4 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r4 = r4.getPreview()
            float r4 = r4.getZoomRatio()
            r21 = r1
            r22 = r2
            r43 = r4
            goto L_0x01b3
        L_0x01ad:
            r22 = r2
            r21 = 800(0x320, float:1.121E-42)
            r43 = 1065353216(0x3f800000, float:1.0)
        L_0x01b3:
            boolean r1 = r47.getThumbnailAnimationPref()
            boolean r2 = r0.saveInBackground(r8)
            android.content.SharedPreferences r3 = r0.sharedPreferences
            java.lang.String r4 = "preference_ghost_image"
            java.lang.String r5 = "preference_ghost_image_off"
            java.lang.String r3 = r3.getString(r4, r5)
            boolean r4 = r47.getPausePreviewPref()
            if (r4 != 0) goto L_0x01de
            java.lang.String r4 = "preference_ghost_image_last"
            boolean r3 = r3.equals(r4)
            if (r3 != 0) goto L_0x01de
            r3 = 4
            if (r1 != 0) goto L_0x01db
            r1 = 16
            r44 = 16
            goto L_0x01e0
        L_0x01db:
            r44 = 4
            goto L_0x01e0
        L_0x01de:
            r44 = 1
        L_0x01e0:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = r47.getPhotoMode()
            net.sourceforge.opencamera.MainActivity r3 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r3 = r3.getPreview()
            boolean r3 = r3.isVideo()
            if (r3 == 0) goto L_0x01f2
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Standard
        L_0x01f2:
            r7 = r1
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            boolean r1 = r1.is_test
            if (r1 != 0) goto L_0x0219
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r7 != r1) goto L_0x0219
            net.sourceforge.opencamera.GyroSensor r1 = r0.gyroSensor
            boolean r1 = r1.isRecording()
            if (r1 == 0) goto L_0x0219
            net.sourceforge.opencamera.GyroSensor r1 = r0.gyroSensor
            boolean r1 = r1.hasTarget()
            if (r1 == 0) goto L_0x0219
            net.sourceforge.opencamera.GyroSensor r1 = r0.gyroSensor
            boolean r1 = r1.isTargetAchieved()
            if (r1 != 0) goto L_0x0219
            r0.panorama_pic_accepted = r15
            goto L_0x0394
        L_0x0219:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.NoiseReduction
            if (r7 == r1) goto L_0x025d
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r7 != r1) goto L_0x0222
            goto L_0x025d
        L_0x0222:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.DRO
            if (r7 == r1) goto L_0x022d
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.HDR
            if (r7 != r1) goto L_0x022b
            goto L_0x022d
        L_0x022b:
            r3 = 0
            goto L_0x022e
        L_0x022d:
            r3 = 1
        L_0x022e:
            boolean r4 = r0.forceSuffix(r7)
            net.sourceforge.opencamera.ImageSaver r1 = r0.imageSaver
            if (r4 == 0) goto L_0x023a
            int r5 = r0.n_capture_images
            int r5 = r5 - r14
            goto L_0x023b
        L_0x023a:
            r5 = 0
        L_0x023b:
            r34 = 0
            r6 = r48
            r7 = r49
            r14 = r16
            r16 = r18
            r17 = r19
            r18 = r50
            r19 = r20
            r20 = r21
            r21 = r22
            r23 = r43
            r40 = r41
            r41 = r42
            r42 = r44
            boolean r14 = r1.saveImageJpeg(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r16, r17, r18, r19, r20, r21, r23, r24, r25, r26, r27, r28, r29, r30, r31, r32, r33, r34, r35, r36, r37, r38, r40, r41, r42)
            goto L_0x0394
        L_0x025d:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r7 != r1) goto L_0x026b
            r0.panorama_pic_accepted = r14
            int r1 = r0.n_panorama_pics
            if (r1 != 0) goto L_0x0269
        L_0x0267:
            r1 = 1
            goto L_0x0270
        L_0x0269:
            r1 = 0
            goto L_0x0270
        L_0x026b:
            int r1 = r0.n_capture_images
            if (r1 != r14) goto L_0x0269
            goto L_0x0267
        L_0x0270:
            if (r1 == 0) goto L_0x0372
            net.sourceforge.opencamera.ImageSaver$Request$SaveBase r1 = net.sourceforge.opencamera.ImageSaver.Request.SaveBase.SAVEBASE_NONE
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r2 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.NoiseReduction
            r3 = -1
            if (r7 != r2) goto L_0x02b0
            android.content.SharedPreferences r2 = r0.sharedPreferences
            java.lang.String r4 = "preference_nr_save"
            java.lang.String r5 = "preference_nr_save_no"
            java.lang.String r2 = r2.getString(r4, r5)
            int r4 = r2.hashCode()
            r5 = 2040491670(0x799f6e96, float:1.0347727E35)
            if (r4 == r5) goto L_0x029c
            r5 = 2127916851(0x7ed56f33, float:1.4185147E38)
            if (r4 == r5) goto L_0x0292
            goto L_0x02a5
        L_0x0292:
            java.lang.String r4 = "preference_nr_save_single"
            boolean r2 = r2.equals(r4)
            if (r2 == 0) goto L_0x02a5
            r3 = 0
            goto L_0x02a5
        L_0x029c:
            java.lang.String r4 = "preference_nr_save_all"
            boolean r2 = r2.equals(r4)
            if (r2 == 0) goto L_0x02a5
            r3 = 1
        L_0x02a5:
            if (r3 == 0) goto L_0x02ad
            if (r3 == r14) goto L_0x02aa
            goto L_0x02ea
        L_0x02aa:
            net.sourceforge.opencamera.ImageSaver$Request$SaveBase r1 = net.sourceforge.opencamera.ImageSaver.Request.SaveBase.SAVEBASE_ALL
            goto L_0x02ea
        L_0x02ad:
            net.sourceforge.opencamera.ImageSaver$Request$SaveBase r1 = net.sourceforge.opencamera.ImageSaver.Request.SaveBase.SAVEBASE_FIRST
            goto L_0x02ea
        L_0x02b0:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r2 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r7 != r2) goto L_0x02ea
            android.content.SharedPreferences r2 = r0.sharedPreferences
            java.lang.String r4 = "preference_panorama_save"
            java.lang.String r5 = "preference_panorama_save_no"
            java.lang.String r2 = r2.getString(r4, r5)
            int r4 = r2.hashCode()
            r5 = -764434427(0xffffffffd26fa805, float:-2.57329021E11)
            if (r4 == r5) goto L_0x02d7
            r5 = -265202712(0xfffffffff03153e8, float:-2.1952113E29)
            if (r4 == r5) goto L_0x02cd
            goto L_0x02e0
        L_0x02cd:
            java.lang.String r4 = "preference_panorama_save_all_plus_debug"
            boolean r2 = r2.equals(r4)
            if (r2 == 0) goto L_0x02e0
            r3 = 1
            goto L_0x02e0
        L_0x02d7:
            java.lang.String r4 = "preference_panorama_save_all"
            boolean r2 = r2.equals(r4)
            if (r2 == 0) goto L_0x02e0
            r3 = 0
        L_0x02e0:
            if (r3 == 0) goto L_0x02e8
            if (r3 == r14) goto L_0x02e5
            goto L_0x02ea
        L_0x02e5:
            net.sourceforge.opencamera.ImageSaver$Request$SaveBase r1 = net.sourceforge.opencamera.ImageSaver.Request.SaveBase.SAVEBASE_ALL_PLUS_DEBUG
            goto L_0x02ea
        L_0x02e8:
            net.sourceforge.opencamera.ImageSaver$Request$SaveBase r1 = net.sourceforge.opencamera.ImageSaver.Request.SaveBase.SAVEBASE_ALL
        L_0x02ea:
            r4 = r1
            net.sourceforge.opencamera.ImageSaver r1 = r0.imageSaver
            r2 = 1
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r3 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.NoiseReduction
            if (r7 != r3) goto L_0x02f5
            net.sourceforge.opencamera.ImageSaver$Request$ProcessType r3 = net.sourceforge.opencamera.ImageSaver.Request.ProcessType.AVERAGE
            goto L_0x02f7
        L_0x02f5:
            net.sourceforge.opencamera.ImageSaver$Request$ProcessType r3 = net.sourceforge.opencamera.ImageSaver.Request.ProcessType.PANORAMA
        L_0x02f7:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r7 != r5) goto L_0x02fe
            r20 = 1
            goto L_0x0300
        L_0x02fe:
            r20 = 0
        L_0x0300:
            r5 = r8
            r6 = r9
            r9 = r7
            r7 = r10
            r8 = r11
            r11 = r9
            r9 = r12
            r10 = r13
            r13 = r11
            r11 = r16
            r45 = r13
            r13 = r20
            r46 = 1
            r14 = r18
            r15 = r19
            r16 = r50
            r17 = r21
            r18 = r22
            r20 = r43
            r21 = r24
            r22 = r25
            r23 = r26
            r24 = r27
            r25 = r28
            r26 = r29
            r27 = r30
            r28 = r31
            r29 = r32
            r30 = r33
            r31 = r34
            r32 = r35
            r33 = r36
            r34 = r37
            r35 = r38
            r37 = r41
            r38 = r42
            r39 = r44
            r1.startImageBatch(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r13, r14, r15, r16, r17, r18, r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30, r31, r32, r33, r34, r35, r37, r38, r39)
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            r2 = r45
            if (r2 != r1) goto L_0x0370
            net.sourceforge.opencamera.ImageSaver r1 = r0.imageSaver
            net.sourceforge.opencamera.ImageSaver$Request r1 = r1.getImageBatchRequest()
            net.sourceforge.opencamera.MainActivity r3 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r3 = r3.getPreview()
            r4 = 0
            float r3 = r3.getViewAngleX(r4)
            r1.camera_view_angle_x = r3
            net.sourceforge.opencamera.ImageSaver r1 = r0.imageSaver
            net.sourceforge.opencamera.ImageSaver$Request r1 = r1.getImageBatchRequest()
            net.sourceforge.opencamera.MainActivity r3 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r3 = r3.getPreview()
            float r3 = r3.getViewAngleY(r4)
            r1.camera_view_angle_y = r3
            goto L_0x0376
        L_0x0370:
            r4 = 0
            goto L_0x0376
        L_0x0372:
            r2 = r7
            r4 = 0
            r46 = 1
        L_0x0376:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r2 != r1) goto L_0x0384
            r1 = 9
            float[] r1 = new float[r1]
            net.sourceforge.opencamera.GyroSensor r2 = r0.gyroSensor
            r2.getRotationMatrix(r1)
            goto L_0x0386
        L_0x0384:
            r1 = r40
        L_0x0386:
            net.sourceforge.opencamera.ImageSaver r2 = r0.imageSaver
            r3 = r49
            java.lang.Object r3 = r3.get(r4)
            byte[] r3 = (byte[]) r3
            r2.addImageBatch(r3, r1)
            r14 = 1
        L_0x0394:
            return r14
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MyApplicationInterface.saveImage(boolean, java.util.List, java.util.Date):boolean");
    }

    public boolean onPictureTaken(byte[] bArr, Date date) {
        this.n_capture_images++;
        ArrayList arrayList = new ArrayList();
        arrayList.add(bArr);
        return saveImage(false, arrayList, date);
    }

    public boolean onBurstPictureTaken(List<byte[]> list, Date date) {
        PhotoMode photoMode = getPhotoMode();
        if (this.main_activity.getPreview().isVideo()) {
            photoMode = PhotoMode.Standard;
        }
        if (photoMode == PhotoMode.HDR) {
            return saveImage(this.sharedPreferences.getBoolean(PreferenceKeys.HDRSaveExpoPreferenceKey, false), list, date);
        }
        return saveImage(true, list, date);
    }

    public boolean onRawPictureTaken(RawImage rawImage, Date date) {
        System.gc();
        this.n_capture_images_raw++;
        boolean saveInBackground = saveInBackground(false);
        PhotoMode photoMode = getPhotoMode();
        if (this.main_activity.getPreview().isVideo()) {
            photoMode = PhotoMode.Standard;
        }
        boolean forceSuffix = forceSuffix(photoMode);
        return this.imageSaver.saveImageRaw(saveInBackground, forceSuffix, forceSuffix ? this.n_capture_images_raw - 1 : 0, rawImage, date);
    }

    public boolean onRawBurstPictureTaken(List<RawImage> list, Date date) {
        System.gc();
        boolean saveInBackground = saveInBackground(false);
        boolean z = true;
        for (int i = 0; i < list.size() && z; i++) {
            z = this.imageSaver.saveImageRaw(saveInBackground, true, i, (RawImage) list.get(i), date);
        }
        return z;
    }

    /* access modifiers changed from: 0000 */
    public void addLastImage(File file, boolean z) {
        this.last_images_saf = false;
        this.last_images.add(new LastImage(file.getAbsolutePath(), z));
    }

    /* access modifiers changed from: 0000 */
    public void addLastImageSAF(Uri uri, boolean z) {
        this.last_images_saf = true;
        this.last_images.add(new LastImage(uri, z));
    }

    /* access modifiers changed from: 0000 */
    public void clearLastImages() {
        this.last_images_saf = false;
        this.last_images.clear();
        this.drawPreview.clearLastImage();
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:24:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void shareLastImage() {
        /*
            r6 = this;
            net.sourceforge.opencamera.MainActivity r0 = r6.main_activity
            net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()
            boolean r1 = r0.isPreviewPaused()
            if (r1 == 0) goto L_0x005d
            r1 = 0
            r2 = 0
            r3 = r1
            r1 = 0
        L_0x0010:
            java.util.List<net.sourceforge.opencamera.MyApplicationInterface$LastImage> r4 = r6.last_images
            int r4 = r4.size()
            if (r1 >= r4) goto L_0x002a
            if (r3 != 0) goto L_0x002a
            java.util.List<net.sourceforge.opencamera.MyApplicationInterface$LastImage> r4 = r6.last_images
            java.lang.Object r4 = r4.get(r1)
            net.sourceforge.opencamera.MyApplicationInterface$LastImage r4 = (net.sourceforge.opencamera.MyApplicationInterface.LastImage) r4
            boolean r5 = r4.share
            if (r5 == 0) goto L_0x0027
            r3 = r4
        L_0x0027:
            int r1 = r1 + 1
            goto L_0x0010
        L_0x002a:
            if (r3 == 0) goto L_0x0054
            android.net.Uri r1 = r3.uri
            if (r1 != 0) goto L_0x0038
            java.lang.String r1 = "MyApplicationInterface"
            java.lang.String r3 = "can't share last image as don't yet have uri"
            android.util.Log.e(r1, r3)
            goto L_0x0055
        L_0x0038:
            android.content.Intent r2 = new android.content.Intent
            java.lang.String r3 = "android.intent.action.SEND"
            r2.<init>(r3)
            java.lang.String r3 = "image/jpeg"
            r2.setType(r3)
            java.lang.String r3 = "android.intent.extra.STREAM"
            r2.putExtra(r3, r1)
            net.sourceforge.opencamera.MainActivity r1 = r6.main_activity
            java.lang.String r3 = "Photo"
            android.content.Intent r2 = android.content.Intent.createChooser(r2, r3)
            r1.startActivity(r2)
        L_0x0054:
            r2 = 1
        L_0x0055:
            if (r2 == 0) goto L_0x005d
            r6.clearLastImages()
            r0.startCameraPreview()
        L_0x005d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MyApplicationInterface.shareLastImage():void");
    }

    private void trashImage(boolean z, Uri uri, String str) {
        Preview preview = this.main_activity.getPreview();
        if (z && uri != null) {
            File fileFromDocumentUriSAF = this.storageUtils.getFileFromDocumentUriSAF(uri, false);
            try {
                if (DocumentsContract.deleteDocument(this.main_activity.getContentResolver(), uri)) {
                    preview.showToast((ToastBoxer) null, (int) C0316R.string.photo_deleted);
                    if (fileFromDocumentUriSAF != null) {
                        this.storageUtils.broadcastFile(fileFromDocumentUriSAF, false, false, true);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (str != null) {
            File file = new File(str);
            if (file.delete()) {
                preview.showToast(this.photo_delete_toast, (int) C0316R.string.photo_deleted);
                this.storageUtils.broadcastFile(file, false, false, true);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void trashLastImage() {
        Preview preview = this.main_activity.getPreview();
        if (preview.isPreviewPaused()) {
            for (int i = 0; i < this.last_images.size(); i++) {
                LastImage lastImage = (LastImage) this.last_images.get(i);
                trashImage(this.last_images_saf, lastImage.uri, lastImage.name);
            }
            clearLastImages();
            this.drawPreview.clearGhostImage();
            preview.startCameraPreview();
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                MyApplicationInterface.this.main_activity.updateGalleryIcon();
            }
        }, 500);
    }

    /* access modifiers changed from: 0000 */
    public void scannedFile(File file, Uri uri) {
        for (int i = 0; i < this.last_images.size(); i++) {
            LastImage lastImage = (LastImage) this.last_images.get(i);
            if (lastImage.uri == null && lastImage.name != null && lastImage.name.equals(file.getAbsolutePath())) {
                lastImage.uri = uri;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean hasThumbnailAnimation() {
        return this.drawPreview.hasThumbnailAnimation();
    }

    public HDRProcessor getHDRProcessor() {
        return this.imageSaver.getHDRProcessor();
    }

    public PanoramaProcessor getPanoramaProcessor() {
        return this.imageSaver.getPanoramaProcessor();
    }
}
