package net.sourceforge.opencamera.cameracontroller;

import android.graphics.Rect;
import android.location.Location;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;
import android.view.TextureView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.sourceforge.opencamera.BuildConfig;

public abstract class CameraController {
    public static final String ANTIBANDING_DEFAULT = "auto";
    public static final String COLOR_EFFECT_DEFAULT = "none";
    public static final String EDGE_MODE_DEFAULT = "default";
    public static final long EXPOSURE_TIME_DEFAULT = 33333333;
    public static final String ISO_DEFAULT = "auto";
    public static final String NOISE_REDUCTION_MODE_DEFAULT = "default";
    public static final int N_IMAGES_NR_DARK = 8;
    public static final int N_IMAGES_NR_DARK_LOW_LIGHT = 15;
    public static final String SCENE_MODE_DEFAULT = "auto";
    private static final String TAG = "CameraController";
    public static final String WHITE_BALANCE_DEFAULT = "auto";
    private final int cameraId;
    volatile int count_camera_parameters_exception;
    public volatile int count_precapture_timeout;
    public volatile int test_af_state_null_focus;
    public volatile int test_capture_results;
    public volatile int test_fake_flash_focus;
    public volatile int test_fake_flash_photo;
    public volatile int test_fake_flash_precapture;
    public volatile boolean test_release_during_photo;
    public volatile boolean test_used_tonemap_curve;
    public volatile boolean test_wait_capture_result;

    public static class Area {
        final Rect rect;
        final int weight;

        public Area(Rect rect2, int i) {
            this.rect = rect2;
            this.weight = i;
        }
    }

    public interface AutoFocusCallback {
        void onAutoFocus(boolean z);
    }

    public enum BurstType {
        BURSTTYPE_NONE,
        BURSTTYPE_EXPO,
        BURSTTYPE_FOCUS,
        BURSTTYPE_NORMAL,
        BURSTTYPE_CONTINUOUS
    }

    public static class CameraFeatures {
        public boolean can_disable_shutter_sound;
        public float exposure_step;
        public boolean is_exposure_lock_supported;
        public boolean is_photo_video_recording_supported;
        public boolean is_video_stabilization_supported;
        public boolean is_white_balance_lock_supported;
        public boolean is_zoom_supported;
        public int max_expo_bracketing_n_images;
        public int max_exposure;
        public long max_exposure_time;
        public int max_iso;
        public int max_num_focus_areas;
        public int max_temperature;
        public int max_zoom;
        public int min_exposure;
        public long min_exposure_time;
        public int min_iso;
        public int min_temperature;
        public float minimum_focus_distance;
        public List<Size> picture_sizes;
        public List<Size> preview_sizes;
        public List<String> supported_flash_values;
        public List<String> supported_focus_values;
        public boolean supports_burst;
        public boolean supports_expo_bracketing;
        public boolean supports_exposure_time;
        public boolean supports_face_detection;
        public boolean supports_focus_bracketing;
        public boolean supports_iso_range;
        public boolean supports_raw;
        public boolean supports_tonemap_curve;
        public boolean supports_white_balance_temperature;
        public int tonemap_max_curve_points;
        public List<Size> video_sizes;
        public List<Size> video_sizes_high_speed;
        public float view_angle_x;
        public float view_angle_y;
        public List<Integer> zoom_ratios;

        public static boolean supportsFrameRate(List<Size> list, int i) {
            if (list == null) {
                return false;
            }
            for (Size supportsFrameRate : list) {
                if (supportsFrameRate.supportsFrameRate((double) i)) {
                    return true;
                }
            }
            return false;
        }

        public static Size findSize(List<Size> list, Size size, double d, boolean z) {
            Size size2 = null;
            Size size3 = null;
            for (Size size4 : list) {
                if (size.equals(size4)) {
                    if (d <= 0.0d || size4.supportsFrameRate(d)) {
                        return size4;
                    }
                    size3 = size4;
                }
            }
            if (z) {
                size2 = size3;
            }
            return size2;
        }
    }

    public interface ContinuousFocusMoveCallback {
        void onContinuousFocusMove(boolean z);
    }

    public interface ErrorCallback {
        void onError();
    }

    public static class Face {
        public final Rect rect;
        public final int score;

        Face(int i, Rect rect2) {
            this.score = i;
            this.rect = rect2;
        }
    }

    public interface FaceDetectionListener {
        void onFaceDetection(Face[] faceArr);
    }

    public interface PictureCallback {
        boolean imageQueueWouldBlock(int i, int i2);

        void onBurstPictureTaken(List<byte[]> list);

        void onCompleted();

        void onFrontScreenTurnOn();

        void onPictureTaken(byte[] bArr);

        void onRawBurstPictureTaken(List<RawImage> list);

        void onRawPictureTaken(RawImage rawImage);

        void onStarted();
    }

    public static class RangeSorter implements Comparator<int[]>, Serializable {
        private static final long serialVersionUID = 5802214721073728212L;

        public int compare(int[] iArr, int[] iArr2) {
            int i;
            int i2;
            if (iArr[0] == iArr2[0]) {
                i = iArr[1];
                i2 = iArr2[1];
            } else {
                i = iArr[0];
                i2 = iArr2[0];
            }
            return i - i2;
        }
    }

    public static class Size {
        final List<int[]> fps_ranges;
        public final int height;
        public final boolean high_speed;
        public boolean supports_burst;
        public final int width;

        Size(int i, int i2, List<int[]> list, boolean z) {
            this.width = i;
            this.height = i2;
            this.supports_burst = true;
            this.fps_ranges = list;
            this.high_speed = z;
            Collections.sort(this.fps_ranges, new RangeSorter());
        }

        public Size(int i, int i2) {
            this(i, i2, new ArrayList(), false);
        }

        /* access modifiers changed from: 0000 */
        public boolean supportsFrameRate(double d) {
            boolean z;
            Iterator it = this.fps_ranges.iterator();
            while (true) {
                z = false;
                if (!it.hasNext()) {
                    break;
                }
                int[] iArr = (int[]) it.next();
                if (((double) iArr[0]) <= d) {
                    z = true;
                    if (d <= ((double) iArr[1])) {
                        break;
                    }
                }
            }
            return z;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof Size)) {
                return false;
            }
            Size size = (Size) obj;
            if (this.width == size.width && this.height == size.height) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (this.width * 41) + this.height;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int[] iArr : this.fps_ranges) {
                sb.append(" [");
                sb.append(iArr[0]);
                sb.append("-");
                sb.append(iArr[1]);
                sb.append("]");
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append(this.width);
            sb2.append("x");
            sb2.append(this.height);
            sb2.append(" ");
            sb2.append(sb);
            sb2.append(this.high_speed ? "-hs" : BuildConfig.FLAVOR);
            return sb2.toString();
        }
    }

    public static class SizeSorter implements Comparator<Size>, Serializable {
        private static final long serialVersionUID = 5802214721073718212L;

        public int compare(Size size, Size size2) {
            return (size2.width * size2.height) - (size.width * size.height);
        }
    }

    public static class SupportedValues {
        public final String selected_value;
        public final List<String> values;

        SupportedValues(List<String> list, String str) {
            this.values = list;
            this.selected_value = str;
        }
    }

    public abstract void autoFocus(AutoFocusCallback autoFocusCallback, boolean z);

    public abstract void cancelAutoFocus();

    public long captureResultExposureTime() {
        return 0;
    }

    public long captureResultFrameDuration() {
        return 0;
    }

    public boolean captureResultHasExposureTime() {
        return false;
    }

    public boolean captureResultHasFrameDuration() {
        return false;
    }

    public boolean captureResultHasIso() {
        return false;
    }

    public boolean captureResultHasWhiteBalanceTemperature() {
        return false;
    }

    public boolean captureResultIsAEScanning() {
        return false;
    }

    public int captureResultIso() {
        return 0;
    }

    public int captureResultWhiteBalanceTemperature() {
        return 0;
    }

    public abstract void clearFocusAndMetering();

    public abstract void clearPreviewFpsRange();

    public abstract void enableShutterSound(boolean z);

    public abstract boolean focusIsContinuous();

    public abstract boolean focusIsVideo();

    public abstract String getAPI();

    public abstract String getAntiBanding();

    public abstract boolean getAutoExposureLock();

    public abstract boolean getAutoWhiteBalanceLock();

    public abstract int getBurstTotal();

    public abstract BurstType getBurstType();

    public abstract CameraFeatures getCameraFeatures() throws CameraControllerException;

    public abstract int getCameraOrientation();

    public abstract String getColorEffect();

    public abstract int getDisplayOrientation();

    public abstract String getEdgeMode();

    public abstract int getExposureCompensation();

    public abstract long getExposureTime();

    public abstract String getFlashValue();

    public abstract List<Area> getFocusAreas();

    public abstract float getFocusBracketingSourceDistance();

    public abstract float getFocusBracketingTargetDistance();

    public abstract float getFocusDistance();

    public abstract String getFocusValue();

    public abstract int getISO();

    public abstract String getISOKey();

    public abstract int getJpegQuality();

    public abstract List<Area> getMeteringAreas();

    public abstract int getNBurstTaken();

    public abstract String getNoiseReductionMode();

    public abstract String getParametersString();

    public abstract Size getPictureSize();

    public abstract Size getPreviewSize();

    public abstract String getSceneMode();

    public abstract List<int[]> getSupportedPreviewFpsRange();

    public boolean getUseCamera2FakeFlash() {
        return false;
    }

    public abstract boolean getVideoStabilization();

    public abstract String getWhiteBalance();

    public abstract int getWhiteBalanceTemperature();

    public abstract int getZoom();

    public abstract void initVideoRecorderPostPrepare(MediaRecorder mediaRecorder, boolean z) throws CameraControllerException;

    public abstract void initVideoRecorderPrePrepare(MediaRecorder mediaRecorder);

    public abstract boolean isBurstOrExpo();

    public abstract boolean isCapturingBurst();

    public abstract boolean isContinuousBurstInProgress();

    public abstract boolean isFrontFacing();

    public abstract boolean isLogProfile();

    public abstract boolean isManualISO();

    public boolean needsFlash() {
        return false;
    }

    public boolean needsFrontScreenFlash() {
        return false;
    }

    public abstract void onError();

    public abstract void reconnect() throws CameraControllerException;

    public abstract void release();

    public abstract void removeLocationInfo();

    public abstract boolean sceneModeAffectsFunctionality();

    public abstract SupportedValues setAntiBanding(String str);

    public abstract void setAutoExposureLock(boolean z);

    public abstract void setAutoWhiteBalanceLock(boolean z);

    public abstract void setBurstForNoiseReduction(boolean z, boolean z2);

    public abstract void setBurstNImages(int i);

    public abstract void setBurstType(BurstType burstType);

    public abstract void setCaptureFollowAutofocusHint(boolean z);

    public abstract SupportedValues setColorEffect(String str);

    public abstract void setContinuousFocusMoveCallback(ContinuousFocusMoveCallback continuousFocusMoveCallback);

    public abstract void setDisplayOrientation(int i);

    public abstract SupportedValues setEdgeMode(String str);

    public abstract void setExpoBracketingNImages(int i);

    public abstract void setExpoBracketingStops(double d);

    public abstract boolean setExposureCompensation(int i);

    public abstract boolean setExposureTime(long j);

    public abstract void setFaceDetectionListener(FaceDetectionListener faceDetectionListener);

    public abstract void setFlashValue(String str);

    public abstract boolean setFocusAndMeteringArea(List<Area> list);

    public abstract void setFocusBracketingAddInfinity(boolean z);

    public abstract void setFocusBracketingNImages(int i);

    public abstract void setFocusBracketingSourceDistance(float f);

    public abstract void setFocusBracketingTargetDistance(float f);

    public abstract boolean setFocusDistance(float f);

    public abstract void setFocusValue(String str);

    public abstract SupportedValues setISO(String str);

    public abstract boolean setISO(int i);

    public abstract void setJpegQuality(int i);

    public abstract void setLocationInfo(Location location);

    public abstract void setLogProfile(boolean z, float f);

    public abstract void setManualISO(boolean z, int i);

    public abstract SupportedValues setNoiseReductionMode(String str);

    public abstract void setOptimiseAEForDRO(boolean z);

    public abstract void setPictureSize(int i, int i2);

    public abstract void setPreviewDisplay(SurfaceHolder surfaceHolder) throws CameraControllerException;

    public abstract void setPreviewFpsRange(int i, int i2);

    public abstract void setPreviewSize(int i, int i2);

    public abstract void setPreviewTexture(TextureView textureView) throws CameraControllerException;

    public abstract void setRaw(boolean z, int i);

    public abstract void setRecordingHint(boolean z);

    public abstract void setRotation(int i);

    public abstract SupportedValues setSceneMode(String str);

    public void setUseCamera2FakeFlash(boolean z) {
    }

    public abstract void setUseExpoFastBurst(boolean z);

    public abstract void setVideoHighSpeed(boolean z);

    public abstract void setVideoStabilization(boolean z);

    public abstract SupportedValues setWhiteBalance(String str);

    public abstract boolean setWhiteBalanceTemperature(int i);

    public abstract void setZoom(int i);

    public boolean shouldCoverPreview() {
        return false;
    }

    public abstract boolean startFaceDetection();

    public abstract void startPreview() throws CameraControllerException;

    public abstract void stopContinuousBurst();

    public abstract void stopFocusBracketingBurst();

    public abstract void stopPreview();

    public abstract boolean supportsAutoFocus();

    public abstract void takePicture(PictureCallback pictureCallback, ErrorCallback errorCallback);

    public abstract void unlock();

    CameraController(int i) {
        this.cameraId = i;
    }

    public int getCameraId() {
        return this.cameraId;
    }

    /* access modifiers changed from: 0000 */
    public SupportedValues checkModeIsSupported(List<String> list, String str, String str2) {
        if (list == null || list.size() <= 1) {
            return null;
        }
        if (!list.contains(str)) {
            if (list.contains(str2)) {
                str = str2;
            } else {
                str = (String) list.get(0);
            }
        }
        return new SupportedValues(list, str);
    }
}
