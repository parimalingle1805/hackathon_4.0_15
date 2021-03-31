package net.sourceforge.opencamera.cameracontroller;

import android.hardware.Camera;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Build.VERSION;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.TextureView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sourceforge.opencamera.BuildConfig;
import net.sourceforge.opencamera.cameracontroller.CameraController.Area;
import net.sourceforge.opencamera.cameracontroller.CameraController.AutoFocusCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController.BurstType;
import net.sourceforge.opencamera.cameracontroller.CameraController.CameraFeatures;
import net.sourceforge.opencamera.cameracontroller.CameraController.ContinuousFocusMoveCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController.ErrorCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController.FaceDetectionListener;
import net.sourceforge.opencamera.cameracontroller.CameraController.PictureCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController.SizeSorter;
import net.sourceforge.opencamera.cameracontroller.CameraController.SupportedValues;

public class CameraController1 extends CameraController {
    private static final String TAG = "CameraController1";
    private static final int max_expo_bracketing_n_images = 3;
    /* access modifiers changed from: private */
    public List<Integer> burst_exposures;
    /* access modifiers changed from: private */
    public Camera camera;
    private final ErrorCallback camera_error_cb;
    private final CameraInfo camera_info = new CameraInfo();
    private int current_exposure_compensation;
    private int current_zoom_value;
    private int display_orientation;
    private int expo_bracketing_n_images = 3;
    private double expo_bracketing_stops = 2.0d;
    private boolean frontscreen_flash;
    private String iso_key;
    /* access modifiers changed from: private */
    public int n_burst;
    /* access modifiers changed from: private */
    public final List<byte[]> pending_burst_images = new ArrayList();
    private int picture_height;
    private int picture_width;
    private boolean sounds_enabled = true;
    /* access modifiers changed from: private */
    public boolean want_expo_bracketing;

    private class CameraErrorCallback implements Camera.ErrorCallback {
        private CameraErrorCallback() {
        }

        public void onError(int i, Camera camera) {
            StringBuilder sb = new StringBuilder();
            sb.append("camera onError: ");
            sb.append(i);
            String sb2 = sb.toString();
            String str = CameraController1.TAG;
            Log.e(str, sb2);
            if (i == 100) {
                Log.e(str, "    CAMERA_ERROR_SERVER_DIED");
                CameraController1.this.onError();
            } else if (i == 1) {
                Log.e(str, "    CAMERA_ERROR_UNKNOWN ");
            }
        }
    }

    private static class TakePictureShutterCallback implements ShutterCallback {
        public void onShutter() {
        }

        private TakePictureShutterCallback() {
        }
    }

    public void clearPreviewFpsRange() {
    }

    public String getAPI() {
        return "Camera";
    }

    public String getEdgeMode() {
        return null;
    }

    public long getExposureTime() {
        return 0;
    }

    public float getFocusBracketingSourceDistance() {
        return 0.0f;
    }

    public float getFocusBracketingTargetDistance() {
        return 0.0f;
    }

    public float getFocusDistance() {
        return 0.0f;
    }

    public int getISO() {
        return 0;
    }

    public String getNoiseReductionMode() {
        return null;
    }

    public int getWhiteBalanceTemperature() {
        return 0;
    }

    public void initVideoRecorderPostPrepare(MediaRecorder mediaRecorder, boolean z) throws CameraControllerException {
    }

    public boolean isBurstOrExpo() {
        return false;
    }

    public boolean isContinuousBurstInProgress() {
        return false;
    }

    public boolean isLogProfile() {
        return false;
    }

    public boolean isManualISO() {
        return false;
    }

    public boolean sceneModeAffectsFunctionality() {
        return true;
    }

    public void setBurstForNoiseReduction(boolean z, boolean z2) {
    }

    public void setBurstNImages(int i) {
    }

    public void setCaptureFollowAutofocusHint(boolean z) {
    }

    public SupportedValues setEdgeMode(String str) {
        return null;
    }

    public boolean setExposureTime(long j) {
        return false;
    }

    public void setFocusBracketingAddInfinity(boolean z) {
    }

    public void setFocusBracketingNImages(int i) {
    }

    public void setFocusBracketingSourceDistance(float f) {
    }

    public void setFocusBracketingTargetDistance(float f) {
    }

    public boolean setFocusDistance(float f) {
        return false;
    }

    public boolean setISO(int i) {
        return false;
    }

    public void setLogProfile(boolean z, float f) {
    }

    public void setManualISO(boolean z, int i) {
    }

    public SupportedValues setNoiseReductionMode(String str) {
        return null;
    }

    public void setOptimiseAEForDRO(boolean z) {
    }

    public void setRaw(boolean z, int i) {
    }

    public void setUseExpoFastBurst(boolean z) {
    }

    public void setVideoHighSpeed(boolean z) {
    }

    public boolean setWhiteBalanceTemperature(int i) {
        return false;
    }

    public void stopContinuousBurst() {
    }

    public void stopFocusBracketingBurst() {
    }

    public CameraController1(int i, ErrorCallback errorCallback) throws CameraControllerException {
        super(i);
        this.camera_error_cb = errorCallback;
        try {
            this.camera = Camera.open(i);
            if (this.camera != null) {
                try {
                    Camera.getCameraInfo(i, this.camera_info);
                    this.camera.setErrorCallback(new CameraErrorCallback());
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    release();
                    throw new CameraControllerException();
                }
            } else {
                throw new CameraControllerException();
            }
        } catch (RuntimeException e2) {
            e2.printStackTrace();
            throw new CameraControllerException();
        }
    }

    public void onError() {
        Log.e(TAG, "onError");
        Camera camera2 = this.camera;
        if (camera2 != null) {
            camera2.release();
            this.camera = null;
        }
        ErrorCallback errorCallback = this.camera_error_cb;
        if (errorCallback != null) {
            errorCallback.onError();
        }
    }

    public void release() {
        Camera camera2 = this.camera;
        if (camera2 != null) {
            camera2.release();
            this.camera = null;
        }
    }

    /* access modifiers changed from: private */
    public Parameters getParameters() {
        return this.camera.getParameters();
    }

    /* access modifiers changed from: private */
    public void setCameraParameters(Parameters parameters) {
        try {
            this.camera.setParameters(parameters);
        } catch (RuntimeException e) {
            e.printStackTrace();
            this.count_camera_parameters_exception++;
        }
    }

    private List<String> convertFlashModesToValues(List<String> list) {
        ArrayList arrayList = new ArrayList();
        String str = "flash_off";
        if (list != null) {
            if (list.contains("off")) {
                arrayList.add(str);
            }
            if (list.contains("auto")) {
                arrayList.add("flash_auto");
            }
            if (list.contains("on")) {
                arrayList.add("flash_on");
            }
            if (list.contains("torch")) {
                arrayList.add("flash_torch");
            }
            if (list.contains("red-eye")) {
                arrayList.add("flash_red_eye");
            }
        }
        if (arrayList.size() <= 1) {
            if (isFrontFacing()) {
                arrayList.clear();
                arrayList.add(str);
                arrayList.add("flash_frontscreen_on");
                arrayList.add("flash_frontscreen_torch");
            } else {
                arrayList.clear();
            }
        }
        return arrayList;
    }

    private List<String> convertFocusModesToValues(List<String> list) {
        ArrayList arrayList = new ArrayList();
        if (list != null) {
            String str = "auto";
            if (list.contains(str)) {
                arrayList.add("focus_mode_auto");
            }
            if (list.contains("infinity")) {
                arrayList.add("focus_mode_infinity");
            }
            if (list.contains("macro")) {
                arrayList.add("focus_mode_macro");
            }
            if (list.contains(str)) {
                arrayList.add("focus_mode_locked");
            }
            if (list.contains("fixed")) {
                arrayList.add("focus_mode_fixed");
            }
            if (list.contains("edof")) {
                arrayList.add("focus_mode_edof");
            }
            if (list.contains("continuous-picture")) {
                arrayList.add("focus_mode_continuous_picture");
            }
            if (list.contains("continuous-video")) {
                arrayList.add("focus_mode_continuous_video");
            }
        }
        return arrayList;
    }

    public CameraFeatures getCameraFeatures() throws CameraControllerException {
        String str = TAG;
        try {
            Parameters parameters = getParameters();
            CameraFeatures cameraFeatures = new CameraFeatures();
            cameraFeatures.is_zoom_supported = parameters.isZoomSupported();
            if (cameraFeatures.is_zoom_supported) {
                cameraFeatures.max_zoom = parameters.getMaxZoom();
                try {
                    cameraFeatures.zoom_ratios = parameters.getZoomRatios();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    cameraFeatures.is_zoom_supported = false;
                    cameraFeatures.max_zoom = 0;
                    cameraFeatures.zoom_ratios = null;
                }
            }
            boolean z = true;
            cameraFeatures.supports_face_detection = parameters.getMaxNumDetectedFaces() > 0;
            List<Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
            if (supportedPictureSizes != null) {
                cameraFeatures.picture_sizes = new ArrayList();
                for (Size size : supportedPictureSizes) {
                    cameraFeatures.picture_sizes.add(new CameraController.Size(size.width, size.height));
                }
                Collections.sort(cameraFeatures.picture_sizes, new SizeSorter());
                cameraFeatures.supported_flash_values = convertFlashModesToValues(parameters.getSupportedFlashModes());
                cameraFeatures.supported_focus_values = convertFocusModesToValues(parameters.getSupportedFocusModes());
                cameraFeatures.max_num_focus_areas = parameters.getMaxNumFocusAreas();
                cameraFeatures.is_exposure_lock_supported = parameters.isAutoExposureLockSupported();
                cameraFeatures.is_white_balance_lock_supported = parameters.isAutoWhiteBalanceLockSupported();
                cameraFeatures.is_video_stabilization_supported = parameters.isVideoStabilizationSupported();
                cameraFeatures.is_photo_video_recording_supported = parameters.isVideoSnapshotSupported();
                cameraFeatures.min_exposure = parameters.getMinExposureCompensation();
                cameraFeatures.max_exposure = parameters.getMaxExposureCompensation();
                cameraFeatures.exposure_step = getExposureCompensationStep();
                if (cameraFeatures.min_exposure == 0 || cameraFeatures.max_exposure == 0) {
                    z = false;
                }
                cameraFeatures.supports_expo_bracketing = z;
                cameraFeatures.max_expo_bracketing_n_images = 3;
                List<Size> supportedVideoSizes = parameters.getSupportedVideoSizes();
                if (supportedVideoSizes == null) {
                    supportedVideoSizes = parameters.getSupportedPreviewSizes();
                }
                cameraFeatures.video_sizes = new ArrayList();
                for (Size size2 : supportedVideoSizes) {
                    cameraFeatures.video_sizes.add(new CameraController.Size(size2.width, size2.height));
                }
                Collections.sort(cameraFeatures.video_sizes, new SizeSorter());
                List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
                cameraFeatures.preview_sizes = new ArrayList();
                for (Size size3 : supportedPreviewSizes) {
                    cameraFeatures.preview_sizes.add(new CameraController.Size(size3.width, size3.height));
                }
                if (VERSION.SDK_INT >= 17) {
                    cameraFeatures.can_disable_shutter_sound = this.camera_info.canDisableShutterSound;
                } else {
                    cameraFeatures.can_disable_shutter_sound = false;
                }
                try {
                    cameraFeatures.view_angle_x = parameters.getHorizontalViewAngle();
                    cameraFeatures.view_angle_y = parameters.getVerticalViewAngle();
                } catch (Exception e2) {
                    e2.printStackTrace();
                    Log.e(str, "exception reading horizontal or vertical view angles");
                    cameraFeatures.view_angle_x = 55.0f;
                    cameraFeatures.view_angle_y = 43.0f;
                }
                if (cameraFeatures.view_angle_x > 150.0f || cameraFeatures.view_angle_y > 150.0f) {
                    Log.e(str, "camera API reporting stupid view angles, set to sensible defaults");
                    cameraFeatures.view_angle_x = 55.0f;
                    cameraFeatures.view_angle_y = 43.0f;
                }
                return cameraFeatures;
            }
            Log.e(str, "getSupportedPictureSizes() returned null!");
            throw new CameraControllerException();
        } catch (RuntimeException e3) {
            Log.e(str, "failed to get camera parameters");
            e3.printStackTrace();
            throw new CameraControllerException();
        }
    }

    public SupportedValues setSceneMode(String str) {
        try {
            Parameters parameters = getParameters();
            SupportedValues checkModeIsSupported = checkModeIsSupported(parameters.getSupportedSceneModes(), str, "auto");
            if (checkModeIsSupported != null) {
                String sceneMode = parameters.getSceneMode();
                if (sceneMode != null && !sceneMode.equals(checkModeIsSupported.selected_value)) {
                    parameters.setSceneMode(checkModeIsSupported.selected_value);
                    setCameraParameters(parameters);
                }
            }
            return checkModeIsSupported;
        } catch (RuntimeException e) {
            Log.e(TAG, "exception from getParameters");
            e.printStackTrace();
            return null;
        }
    }

    public String getSceneMode() {
        return getParameters().getSceneMode();
    }

    public SupportedValues setColorEffect(String str) {
        Parameters parameters = getParameters();
        SupportedValues checkModeIsSupported = checkModeIsSupported(parameters.getSupportedColorEffects(), str, CameraController.COLOR_EFFECT_DEFAULT);
        if (checkModeIsSupported != null) {
            String colorEffect = parameters.getColorEffect();
            if (colorEffect == null || !colorEffect.equals(checkModeIsSupported.selected_value)) {
                parameters.setColorEffect(checkModeIsSupported.selected_value);
                setCameraParameters(parameters);
            }
        }
        return checkModeIsSupported;
    }

    public String getColorEffect() {
        return getParameters().getColorEffect();
    }

    public SupportedValues setWhiteBalance(String str) {
        Parameters parameters = getParameters();
        List supportedWhiteBalance = parameters.getSupportedWhiteBalance();
        if (supportedWhiteBalance != null) {
            while (true) {
                String str2 = "manual";
                if (!supportedWhiteBalance.contains(str2)) {
                    break;
                }
                supportedWhiteBalance.remove(str2);
            }
        }
        SupportedValues checkModeIsSupported = checkModeIsSupported(supportedWhiteBalance, str, "auto");
        if (checkModeIsSupported != null) {
            String whiteBalance = parameters.getWhiteBalance();
            if (whiteBalance != null && !whiteBalance.equals(checkModeIsSupported.selected_value)) {
                parameters.setWhiteBalance(checkModeIsSupported.selected_value);
                setCameraParameters(parameters);
            }
        }
        return checkModeIsSupported;
    }

    public String getWhiteBalance() {
        return getParameters().getWhiteBalance();
    }

    public SupportedValues setAntiBanding(String str) {
        Parameters parameters = getParameters();
        SupportedValues checkModeIsSupported = checkModeIsSupported(parameters.getSupportedAntibanding(), str, "auto");
        if (checkModeIsSupported != null && checkModeIsSupported.selected_value.equals(str)) {
            String antibanding = parameters.getAntibanding();
            if (antibanding == null || !antibanding.equals(checkModeIsSupported.selected_value)) {
                parameters.setAntibanding(checkModeIsSupported.selected_value);
                setCameraParameters(parameters);
            }
        }
        return checkModeIsSupported;
    }

    public String getAntiBanding() {
        return getParameters().getAntibanding();
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0060  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x008b  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00c6 A[RETURN] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public net.sourceforge.opencamera.cameracontroller.CameraController.SupportedValues setISO(java.lang.String r10) {
        /*
            r9 = this;
            android.hardware.Camera$Parameters r0 = r9.getParameters()
            java.lang.String r1 = "iso-values"
            java.lang.String r1 = r0.get(r1)
            if (r1 != 0) goto L_0x0022
            java.lang.String r1 = "iso-mode-values"
            java.lang.String r1 = r0.get(r1)
            if (r1 != 0) goto L_0x0022
            java.lang.String r1 = "iso-speed-values"
            java.lang.String r1 = r0.get(r1)
            if (r1 != 0) goto L_0x0022
            java.lang.String r1 = "nv-picture-iso-values"
            java.lang.String r1 = r0.get(r1)
        L_0x0022:
            r2 = 0
            if (r1 == 0) goto L_0x0053
            int r3 = r1.length()
            if (r3 <= 0) goto L_0x0053
            java.lang.String r3 = ","
            java.lang.String[] r1 = r1.split(r3)
            int r3 = r1.length
            if (r3 <= 0) goto L_0x0053
            java.util.HashSet r3 = new java.util.HashSet
            r3.<init>()
            java.util.ArrayList r4 = new java.util.ArrayList
            r4.<init>()
            int r5 = r1.length
            r6 = 0
        L_0x0040:
            if (r6 >= r5) goto L_0x0054
            r7 = r1[r6]
            boolean r8 = r3.contains(r7)
            if (r8 != 0) goto L_0x0050
            r4.add(r7)
            r3.add(r7)
        L_0x0050:
            int r6 = r6 + 1
            goto L_0x0040
        L_0x0053:
            r4 = r2
        L_0x0054:
            java.lang.String r1 = "iso"
            r9.iso_key = r1
            java.lang.String r3 = r9.iso_key
            java.lang.String r3 = r0.get(r3)
            if (r3 != 0) goto L_0x0087
            java.lang.String r3 = "iso-speed"
            r9.iso_key = r3
            java.lang.String r3 = r9.iso_key
            java.lang.String r3 = r0.get(r3)
            if (r3 != 0) goto L_0x0087
            java.lang.String r3 = "nv-picture-iso"
            r9.iso_key = r3
            java.lang.String r3 = r9.iso_key
            java.lang.String r3 = r0.get(r3)
            if (r3 != 0) goto L_0x0087
            java.lang.String r3 = android.os.Build.MODEL
            java.lang.String r5 = "Z00"
            boolean r3 = r3.contains(r5)
            if (r3 == 0) goto L_0x0085
            r9.iso_key = r1
            goto L_0x0087
        L_0x0085:
            r9.iso_key = r2
        L_0x0087:
            java.lang.String r1 = r9.iso_key
            if (r1 == 0) goto L_0x00c6
            java.lang.String r1 = "auto"
            if (r4 != 0) goto L_0x00b5
            java.util.ArrayList r4 = new java.util.ArrayList
            r4.<init>()
            r4.add(r1)
            java.lang.String r2 = "50"
            r4.add(r2)
            java.lang.String r2 = "100"
            r4.add(r2)
            java.lang.String r2 = "200"
            r4.add(r2)
            java.lang.String r2 = "400"
            r4.add(r2)
            java.lang.String r2 = "800"
            r4.add(r2)
            java.lang.String r2 = "1600"
            r4.add(r2)
        L_0x00b5:
            net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues r10 = r9.checkModeIsSupported(r4, r10, r1)
            if (r10 == 0) goto L_0x00c5
            java.lang.String r1 = r9.iso_key
            java.lang.String r2 = r10.selected_value
            r0.set(r1, r2)
            r9.setCameraParameters(r0)
        L_0x00c5:
            return r10
        L_0x00c6:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController1.setISO(java.lang.String):net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues");
    }

    public String getISOKey() {
        return this.iso_key;
    }

    public CameraController.Size getPictureSize() {
        return new CameraController.Size(this.picture_width, this.picture_height);
    }

    public void setPictureSize(int i, int i2) {
        Parameters parameters = getParameters();
        this.picture_width = i;
        this.picture_height = i2;
        parameters.setPictureSize(i, i2);
        setCameraParameters(parameters);
    }

    public CameraController.Size getPreviewSize() {
        Size previewSize = getParameters().getPreviewSize();
        return new CameraController.Size(previewSize.width, previewSize.height);
    }

    public void setPreviewSize(int i, int i2) {
        Parameters parameters = getParameters();
        parameters.setPreviewSize(i, i2);
        setCameraParameters(parameters);
    }

    public void setBurstType(BurstType burstType) {
        if (this.camera != null) {
            if (burstType == BurstType.BURSTTYPE_NONE || burstType == BurstType.BURSTTYPE_EXPO) {
                this.want_expo_bracketing = burstType == BurstType.BURSTTYPE_EXPO;
            } else {
                Log.e(TAG, "burst type not supported");
            }
        }
    }

    public BurstType getBurstType() {
        return this.want_expo_bracketing ? BurstType.BURSTTYPE_EXPO : BurstType.BURSTTYPE_NONE;
    }

    public void setExpoBracketingNImages(int i) {
        if (i <= 1 || i % 2 == 0) {
            throw new RuntimeException();
        }
        if (i > 3) {
            i = 3;
        }
        this.expo_bracketing_n_images = i;
    }

    public void setExpoBracketingStops(double d) {
        if (d > 0.0d) {
            this.expo_bracketing_stops = d;
            return;
        }
        throw new RuntimeException();
    }

    public boolean isCapturingBurst() {
        return getBurstTotal() > 1 && getNBurstTaken() < getBurstTotal();
    }

    public int getNBurstTaken() {
        return this.pending_burst_images.size();
    }

    public int getBurstTotal() {
        return this.n_burst;
    }

    public void setVideoStabilization(boolean z) {
        Parameters parameters = getParameters();
        parameters.setVideoStabilization(z);
        setCameraParameters(parameters);
    }

    public boolean getVideoStabilization() {
        return getParameters().getVideoStabilization();
    }

    public int getJpegQuality() {
        return getParameters().getJpegQuality();
    }

    public void setJpegQuality(int i) {
        Parameters parameters = getParameters();
        parameters.setJpegQuality(i);
        setCameraParameters(parameters);
    }

    public int getZoom() {
        return this.current_zoom_value;
    }

    public void setZoom(int i) {
        try {
            Parameters parameters = getParameters();
            this.current_zoom_value = i;
            parameters.setZoom(i);
            setCameraParameters(parameters);
        } catch (RuntimeException e) {
            Log.e(TAG, "failed to set parameters for zoom");
            e.printStackTrace();
        }
    }

    public int getExposureCompensation() {
        return this.current_exposure_compensation;
    }

    private float getExposureCompensationStep() {
        try {
            return getParameters().getExposureCompensationStep();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.33333334f;
        }
    }

    public boolean setExposureCompensation(int i) {
        if (i == this.current_exposure_compensation) {
            return false;
        }
        Parameters parameters = getParameters();
        this.current_exposure_compensation = i;
        parameters.setExposureCompensation(i);
        setCameraParameters(parameters);
        return true;
    }

    public void setPreviewFpsRange(int i, int i2) {
        try {
            Parameters parameters = getParameters();
            parameters.setPreviewFpsRange(i, i2);
            setCameraParameters(parameters);
        } catch (RuntimeException e) {
            Log.e(TAG, "setPreviewFpsRange failed to get parameters");
            e.printStackTrace();
        }
    }

    public List<int[]> getSupportedPreviewFpsRange() {
        try {
            return getParameters().getSupportedPreviewFpsRange();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setFocusValue(java.lang.String r3) {
        /*
            r2 = this;
            android.hardware.Camera$Parameters r0 = r2.getParameters()
            int r1 = r3.hashCode()
            switch(r1) {
                case -2084726721: goto L_0x0052;
                case -1897460700: goto L_0x0048;
                case -1897358037: goto L_0x003e;
                case -711944829: goto L_0x0034;
                case 402565696: goto L_0x002a;
                case 590698013: goto L_0x0020;
                case 1312524191: goto L_0x0016;
                case 1318730743: goto L_0x000c;
                default: goto L_0x000b;
            }
        L_0x000b:
            goto L_0x005c
        L_0x000c:
            java.lang.String r1 = "focus_mode_macro"
            boolean r3 = r3.equals(r1)
            if (r3 == 0) goto L_0x005c
            r3 = 3
            goto L_0x005d
        L_0x0016:
            java.lang.String r1 = "focus_mode_fixed"
            boolean r3 = r3.equals(r1)
            if (r3 == 0) goto L_0x005c
            r3 = 4
            goto L_0x005d
        L_0x0020:
            java.lang.String r1 = "focus_mode_infinity"
            boolean r3 = r3.equals(r1)
            if (r3 == 0) goto L_0x005c
            r3 = 2
            goto L_0x005d
        L_0x002a:
            java.lang.String r1 = "focus_mode_continuous_video"
            boolean r3 = r3.equals(r1)
            if (r3 == 0) goto L_0x005c
            r3 = 7
            goto L_0x005d
        L_0x0034:
            java.lang.String r1 = "focus_mode_continuous_picture"
            boolean r3 = r3.equals(r1)
            if (r3 == 0) goto L_0x005c
            r3 = 6
            goto L_0x005d
        L_0x003e:
            java.lang.String r1 = "focus_mode_edof"
            boolean r3 = r3.equals(r1)
            if (r3 == 0) goto L_0x005c
            r3 = 5
            goto L_0x005d
        L_0x0048:
            java.lang.String r1 = "focus_mode_auto"
            boolean r3 = r3.equals(r1)
            if (r3 == 0) goto L_0x005c
            r3 = 0
            goto L_0x005d
        L_0x0052:
            java.lang.String r1 = "focus_mode_locked"
            boolean r3 = r3.equals(r1)
            if (r3 == 0) goto L_0x005c
            r3 = 1
            goto L_0x005d
        L_0x005c:
            r3 = -1
        L_0x005d:
            switch(r3) {
                case 0: goto L_0x0085;
                case 1: goto L_0x0085;
                case 2: goto L_0x007f;
                case 3: goto L_0x0079;
                case 4: goto L_0x0073;
                case 5: goto L_0x006d;
                case 6: goto L_0x0067;
                case 7: goto L_0x0061;
                default: goto L_0x0060;
            }
        L_0x0060:
            goto L_0x008a
        L_0x0061:
            java.lang.String r3 = "continuous-video"
            r0.setFocusMode(r3)
            goto L_0x008a
        L_0x0067:
            java.lang.String r3 = "continuous-picture"
            r0.setFocusMode(r3)
            goto L_0x008a
        L_0x006d:
            java.lang.String r3 = "edof"
            r0.setFocusMode(r3)
            goto L_0x008a
        L_0x0073:
            java.lang.String r3 = "fixed"
            r0.setFocusMode(r3)
            goto L_0x008a
        L_0x0079:
            java.lang.String r3 = "macro"
            r0.setFocusMode(r3)
            goto L_0x008a
        L_0x007f:
            java.lang.String r3 = "infinity"
            r0.setFocusMode(r3)
            goto L_0x008a
        L_0x0085:
            java.lang.String r3 = "auto"
            r0.setFocusMode(r3)
        L_0x008a:
            r2.setCameraParameters(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController1.setFocusValue(java.lang.String):void");
    }

    private String convertFocusModeToValue(String str) {
        if (str != null) {
            if (str.equals("auto")) {
                return "focus_mode_auto";
            }
            if (str.equals("infinity")) {
                return "focus_mode_infinity";
            }
            if (str.equals("macro")) {
                return "focus_mode_macro";
            }
            if (str.equals("fixed")) {
                return "focus_mode_fixed";
            }
            if (str.equals("edof")) {
                return "focus_mode_edof";
            }
            if (str.equals("continuous-picture")) {
                return "focus_mode_continuous_picture";
            }
            if (str.equals("continuous-video")) {
                return "focus_mode_continuous_video";
            }
        }
        return BuildConfig.FLAVOR;
    }

    public String getFocusValue() {
        return convertFocusModeToValue(getParameters().getFocusMode());
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String convertFlashValueToMode(java.lang.String r2) {
        /*
            r1 = this;
            int r0 = r2.hashCode()
            switch(r0) {
                case -1195303778: goto L_0x0044;
                case -1146923872: goto L_0x003a;
                case -10523976: goto L_0x0030;
                case 17603715: goto L_0x0026;
                case 1617654509: goto L_0x001c;
                case 1625570446: goto L_0x0012;
                case 2008442932: goto L_0x0008;
                default: goto L_0x0007;
            }
        L_0x0007:
            goto L_0x004e
        L_0x0008:
            java.lang.String r0 = "flash_red_eye"
            boolean r2 = r2.equals(r0)
            if (r2 == 0) goto L_0x004e
            r2 = 4
            goto L_0x004f
        L_0x0012:
            java.lang.String r0 = "flash_on"
            boolean r2 = r2.equals(r0)
            if (r2 == 0) goto L_0x004e
            r2 = 2
            goto L_0x004f
        L_0x001c:
            java.lang.String r0 = "flash_torch"
            boolean r2 = r2.equals(r0)
            if (r2 == 0) goto L_0x004e
            r2 = 3
            goto L_0x004f
        L_0x0026:
            java.lang.String r0 = "flash_frontscreen_torch"
            boolean r2 = r2.equals(r0)
            if (r2 == 0) goto L_0x004e
            r2 = 6
            goto L_0x004f
        L_0x0030:
            java.lang.String r0 = "flash_frontscreen_on"
            boolean r2 = r2.equals(r0)
            if (r2 == 0) goto L_0x004e
            r2 = 5
            goto L_0x004f
        L_0x003a:
            java.lang.String r0 = "flash_off"
            boolean r2 = r2.equals(r0)
            if (r2 == 0) goto L_0x004e
            r2 = 0
            goto L_0x004f
        L_0x0044:
            java.lang.String r0 = "flash_auto"
            boolean r2 = r2.equals(r0)
            if (r2 == 0) goto L_0x004e
            r2 = 1
            goto L_0x004f
        L_0x004e:
            r2 = -1
        L_0x004f:
            java.lang.String r0 = "off"
            switch(r2) {
                case 0: goto L_0x0062;
                case 1: goto L_0x0060;
                case 2: goto L_0x005d;
                case 3: goto L_0x005a;
                case 4: goto L_0x0057;
                case 5: goto L_0x0062;
                case 6: goto L_0x0062;
                default: goto L_0x0054;
            }
        L_0x0054:
            java.lang.String r0 = ""
            goto L_0x0062
        L_0x0057:
            java.lang.String r0 = "red-eye"
            goto L_0x0062
        L_0x005a:
            java.lang.String r0 = "torch"
            goto L_0x0062
        L_0x005d:
            java.lang.String r0 = "on"
            goto L_0x0062
        L_0x0060:
            java.lang.String r0 = "auto"
        L_0x0062:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController1.convertFlashValueToMode(java.lang.String):java.lang.String");
    }

    public void setFlashValue(String str) {
        Parameters parameters = getParameters();
        this.frontscreen_flash = false;
        if (str.equals("flash_frontscreen_on")) {
            this.frontscreen_flash = true;
        } else if (parameters.getFlashMode() != null) {
            final String convertFlashValueToMode = convertFlashValueToMode(str);
            if (convertFlashValueToMode.length() > 0 && !convertFlashValueToMode.equals(parameters.getFlashMode())) {
                if (parameters.getFlashMode().equals("torch")) {
                    String str2 = "off";
                    if (!convertFlashValueToMode.equals(str2)) {
                        parameters.setFlashMode(str2);
                        setCameraParameters(parameters);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                if (CameraController1.this.camera != null) {
                                    Parameters access$200 = CameraController1.this.getParameters();
                                    access$200.setFlashMode(convertFlashValueToMode);
                                    CameraController1.this.setCameraParameters(access$200);
                                }
                            }
                        }, 100);
                    }
                }
                parameters.setFlashMode(convertFlashValueToMode);
                setCameraParameters(parameters);
            }
        }
    }

    private String convertFlashModeToValue(String str) {
        if (str != null) {
            if (str.equals("off")) {
                return "flash_off";
            }
            if (str.equals("auto")) {
                return "flash_auto";
            }
            if (str.equals("on")) {
                return "flash_on";
            }
            if (str.equals("torch")) {
                return "flash_torch";
            }
            if (str.equals("red-eye")) {
                return "flash_red_eye";
            }
        }
        return BuildConfig.FLAVOR;
    }

    public String getFlashValue() {
        return convertFlashModeToValue(getParameters().getFlashMode());
    }

    public void setRecordingHint(boolean z) {
        try {
            Parameters parameters = getParameters();
            String focusMode = parameters.getFocusMode();
            if (focusMode != null && !focusMode.equals("continuous-video")) {
                parameters.setRecordingHint(z);
                setCameraParameters(parameters);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "setRecordingHint failed to get parameters");
            e.printStackTrace();
        }
    }

    public void setAutoExposureLock(boolean z) {
        Parameters parameters = getParameters();
        parameters.setAutoExposureLock(z);
        setCameraParameters(parameters);
    }

    public boolean getAutoExposureLock() {
        Parameters parameters = getParameters();
        if (!parameters.isAutoExposureLockSupported()) {
            return false;
        }
        return parameters.getAutoExposureLock();
    }

    public void setAutoWhiteBalanceLock(boolean z) {
        Parameters parameters = getParameters();
        parameters.setAutoWhiteBalanceLock(z);
        setCameraParameters(parameters);
    }

    public boolean getAutoWhiteBalanceLock() {
        Parameters parameters = getParameters();
        if (!parameters.isAutoWhiteBalanceLockSupported()) {
            return false;
        }
        return parameters.getAutoWhiteBalanceLock();
    }

    public void setRotation(int i) {
        Parameters parameters = getParameters();
        parameters.setRotation(i);
        setCameraParameters(parameters);
    }

    public void setLocationInfo(Location location) {
        Parameters parameters = getParameters();
        parameters.removeGpsData();
        parameters.setGpsTimestamp(System.currentTimeMillis() / 1000);
        parameters.setGpsLatitude(location.getLatitude());
        parameters.setGpsLongitude(location.getLongitude());
        parameters.setGpsProcessingMethod(location.getProvider());
        if (location.hasAltitude()) {
            parameters.setGpsAltitude(location.getAltitude());
        } else {
            parameters.setGpsAltitude(0.0d);
        }
        if (location.getTime() != 0) {
            parameters.setGpsTimestamp(location.getTime() / 1000);
        }
        setCameraParameters(parameters);
    }

    public void removeLocationInfo() {
        Parameters parameters = getParameters();
        parameters.removeGpsData();
        setCameraParameters(parameters);
    }

    public void enableShutterSound(boolean z) {
        if (VERSION.SDK_INT >= 17) {
            this.camera.enableShutterSound(z);
        }
        this.sounds_enabled = z;
    }

    public boolean setFocusAndMeteringArea(List<Area> list) {
        ArrayList arrayList = new ArrayList();
        for (Area area : list) {
            arrayList.add(new Camera.Area(area.rect, area.weight));
        }
        try {
            Parameters parameters = getParameters();
            String focusMode = parameters.getFocusMode();
            if (parameters.getMaxNumFocusAreas() == 0 || focusMode == null || (!focusMode.equals("auto") && !focusMode.equals("macro") && !focusMode.equals("continuous-picture") && !focusMode.equals("continuous-video"))) {
                if (parameters.getMaxNumMeteringAreas() != 0) {
                    parameters.setMeteringAreas(arrayList);
                    setCameraParameters(parameters);
                }
                return false;
            }
            parameters.setFocusAreas(arrayList);
            if (parameters.getMaxNumMeteringAreas() != 0) {
                parameters.setMeteringAreas(arrayList);
            }
            setCameraParameters(parameters);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void clearFocusAndMetering() {
        try {
            Parameters parameters = getParameters();
            boolean z = false;
            if (parameters.getMaxNumFocusAreas() > 0) {
                parameters.setFocusAreas(null);
                z = true;
            }
            if (parameters.getMaxNumMeteringAreas() > 0) {
                parameters.setMeteringAreas(null);
                z = true;
            }
            if (z) {
                setCameraParameters(parameters);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public List<Area> getFocusAreas() {
        List<Camera.Area> focusAreas = getParameters().getFocusAreas();
        if (focusAreas == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (Camera.Area area : focusAreas) {
            arrayList.add(new Area(area.rect, area.weight));
        }
        return arrayList;
    }

    public List<Area> getMeteringAreas() {
        List<Camera.Area> meteringAreas = getParameters().getMeteringAreas();
        if (meteringAreas == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (Camera.Area area : meteringAreas) {
            arrayList.add(new Area(area.rect, area.weight));
        }
        return arrayList;
    }

    public boolean supportsAutoFocus() {
        try {
            String focusMode = getParameters().getFocusMode();
            if (focusMode != null && (focusMode.equals("auto") || focusMode.equals("macro"))) {
                return true;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean focusIsContinuous() {
        try {
            String focusMode = getParameters().getFocusMode();
            if (focusMode != null && (focusMode.equals("continuous-picture") || focusMode.equals("continuous-video"))) {
                return true;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean focusIsVideo() {
        String focusMode = getParameters().getFocusMode();
        return focusMode != null && focusMode.equals("continuous-video");
    }

    public void reconnect() throws CameraControllerException {
        try {
            this.camera.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
            throw new CameraControllerException();
        }
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) throws CameraControllerException {
        try {
            this.camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CameraControllerException();
        }
    }

    public void setPreviewTexture(TextureView textureView) throws CameraControllerException {
        try {
            this.camera.setPreviewTexture(textureView.getSurfaceTexture());
        } catch (IOException e) {
            e.printStackTrace();
            throw new CameraControllerException();
        }
    }

    public void startPreview() throws CameraControllerException {
        try {
            this.camera.startPreview();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CameraControllerException();
        }
    }

    public void stopPreview() {
        Camera camera2 = this.camera;
        if (camera2 != null) {
            camera2.stopPreview();
        }
    }

    public boolean startFaceDetection() {
        try {
            this.camera.startFaceDetection();
            return true;
        } catch (RuntimeException unused) {
            return false;
        }
    }

    public void setFaceDetectionListener(final FaceDetectionListener faceDetectionListener) {
        this.camera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
            public void onFaceDetection(Face[] faceArr, Camera camera) {
                CameraController.Face[] faceArr2 = new CameraController.Face[faceArr.length];
                for (int i = 0; i < faceArr.length; i++) {
                    faceArr2[i] = new CameraController.Face(faceArr[i].score, faceArr[i].rect);
                }
                faceDetectionListener.onFaceDetection(faceArr2);
            }
        });
    }

    public void autoFocus(final AutoFocusCallback autoFocusCallback, boolean z) {
        try {
            this.camera.autoFocus(new Camera.AutoFocusCallback() {
                boolean done_autofocus = false;

                public void onAutoFocus(boolean z, Camera camera) {
                    if (!this.done_autofocus) {
                        this.done_autofocus = true;
                        autoFocusCallback.onAutoFocus(z);
                    }
                }
            });
        } catch (RuntimeException e) {
            e.printStackTrace();
            autoFocusCallback.onAutoFocus(false);
        }
    }

    public void cancelAutoFocus() {
        try {
            this.camera.cancelAutoFocus();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void setContinuousFocusMoveCallback(final ContinuousFocusMoveCallback continuousFocusMoveCallback) {
        if (VERSION.SDK_INT < 16) {
            return;
        }
        if (continuousFocusMoveCallback != null) {
            try {
                this.camera.setAutoFocusMoveCallback(new AutoFocusMoveCallback() {
                    public void onAutoFocusMoving(boolean z, Camera camera) {
                        continuousFocusMoveCallback.onContinuousFocusMove(z);
                    }
                });
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } else {
            this.camera.setAutoFocusMoveCallback(null);
        }
    }

    private void clearPending() {
        this.pending_burst_images.clear();
        this.burst_exposures = null;
        this.n_burst = 0;
    }

    /* access modifiers changed from: private */
    public void takePictureNow(final PictureCallback pictureCallback, final ErrorCallback errorCallback) {
        Camera.PictureCallback pictureCallback2;
        ShutterCallback takePictureShutterCallback = this.sounds_enabled ? new TakePictureShutterCallback() : null;
        if (pictureCallback == null) {
            pictureCallback2 = null;
        } else {
            pictureCallback2 = new Camera.PictureCallback() {
                public void onPictureTaken(byte[] bArr, Camera camera) {
                    if (!CameraController1.this.want_expo_bracketing || CameraController1.this.n_burst <= 1) {
                        pictureCallback.onPictureTaken(bArr);
                        pictureCallback.onCompleted();
                        return;
                    }
                    CameraController1.this.pending_burst_images.add(bArr);
                    if (CameraController1.this.pending_burst_images.size() >= CameraController1.this.n_burst) {
                        if (CameraController1.this.pending_burst_images.size() > CameraController1.this.n_burst) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("pending_burst_images size ");
                            sb.append(CameraController1.this.pending_burst_images.size());
                            sb.append(" is greater than n_burst ");
                            sb.append(CameraController1.this.n_burst);
                            Log.e(CameraController1.TAG, sb.toString());
                        }
                        CameraController1 cameraController1 = CameraController1.this;
                        cameraController1.setExposureCompensation(((Integer) cameraController1.burst_exposures.get(0)).intValue());
                        int size = CameraController1.this.pending_burst_images.size() / 2;
                        ArrayList arrayList = new ArrayList();
                        int i = 0;
                        while (i < size) {
                            i++;
                            arrayList.add(CameraController1.this.pending_burst_images.get(i));
                        }
                        arrayList.add(CameraController1.this.pending_burst_images.get(0));
                        for (int i2 = 0; i2 < size; i2++) {
                            arrayList.add(CameraController1.this.pending_burst_images.get(size + 1));
                        }
                        pictureCallback.onBurstPictureTaken(arrayList);
                        CameraController1.this.pending_burst_images.clear();
                        pictureCallback.onCompleted();
                        return;
                    }
                    CameraController1 cameraController12 = CameraController1.this;
                    cameraController12.setExposureCompensation(((Integer) cameraController12.burst_exposures.get(CameraController1.this.pending_burst_images.size())).intValue());
                    try {
                        CameraController1.this.startPreview();
                    } catch (CameraControllerException e) {
                        e.printStackTrace();
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            if (CameraController1.this.camera != null) {
                                CameraController1.this.takePictureNow(pictureCallback, errorCallback);
                            }
                        }
                    }, 1000);
                }
            };
        }
        if (pictureCallback != null) {
            pictureCallback.onStarted();
        }
        try {
            this.camera.takePicture(takePictureShutterCallback, null, pictureCallback2);
        } catch (RuntimeException e) {
            e.printStackTrace();
            errorCallback.onError();
        }
    }

    public void takePicture(final PictureCallback pictureCallback, final ErrorCallback errorCallback) {
        clearPending();
        if (this.want_expo_bracketing) {
            Parameters parameters = getParameters();
            int i = this.expo_bracketing_n_images / 2;
            int minExposureCompensation = parameters.getMinExposureCompensation();
            int maxExposureCompensation = parameters.getMaxExposureCompensation();
            float exposureCompensationStep = getExposureCompensationStep();
            if (exposureCompensationStep == 0.0f) {
                exposureCompensationStep = 0.33333334f;
            }
            int exposureCompensation = getExposureCompensation();
            double d = this.expo_bracketing_stops;
            double d2 = (double) i;
            Double.isNaN(d2);
            double d3 = (d / d2) + 1.0E-5d;
            double d4 = (double) exposureCompensationStep;
            Double.isNaN(d4);
            int max = Math.max((int) (d3 / d4), 1);
            ArrayList arrayList = new ArrayList();
            arrayList.add(Integer.valueOf(exposureCompensation));
            int i2 = 0;
            for (int i3 = 0; i3 < i; i3++) {
                arrayList.add(Integer.valueOf(Math.max(exposureCompensation - ((i - i3) * max), minExposureCompensation)));
            }
            while (i2 < i) {
                i2++;
                arrayList.add(Integer.valueOf(Math.min((i2 * max) + exposureCompensation, maxExposureCompensation)));
            }
            this.burst_exposures = arrayList;
            this.n_burst = arrayList.size();
        }
        if (this.frontscreen_flash) {
            pictureCallback.onFrontScreenTurnOn();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (CameraController1.this.camera != null) {
                        CameraController1.this.takePictureNow(pictureCallback, errorCallback);
                    }
                }
            }, 1000);
            return;
        }
        takePictureNow(pictureCallback, errorCallback);
    }

    public void setDisplayOrientation(int i) {
        int i2;
        if (this.camera_info.facing == 1) {
            i2 = (360 - ((this.camera_info.orientation + i) % 360)) % 360;
        } else {
            i2 = ((this.camera_info.orientation - i) + 360) % 360;
        }
        try {
            this.camera.setDisplayOrientation(i2);
        } catch (RuntimeException e) {
            Log.e(TAG, "failed to set display orientation");
            e.printStackTrace();
        }
        this.display_orientation = i2;
    }

    public int getDisplayOrientation() {
        return this.display_orientation;
    }

    public int getCameraOrientation() {
        return this.camera_info.orientation;
    }

    public boolean isFrontFacing() {
        return this.camera_info.facing == 1;
    }

    public void unlock() {
        stopPreview();
        this.camera.unlock();
    }

    public void initVideoRecorderPrePrepare(MediaRecorder mediaRecorder) {
        mediaRecorder.setCamera(this.camera);
    }

    public String getParametersString() {
        try {
            return getParameters().flatten();
        } catch (Exception e) {
            e.printStackTrace();
            return BuildConfig.FLAVOR;
        }
    }
}
