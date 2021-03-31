package net.sourceforge.opencamera.preview;

import android.content.Context;
import android.graphics.Canvas;
import android.location.Location;
import android.net.Uri;
import android.util.Pair;
import android.view.MotionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import net.sourceforge.opencamera.cameracontroller.RawImage;

public interface ApplicationInterface {
    public static final int VIDEOMETHOD_FILE = 0;
    public static final int VIDEOMETHOD_SAF = 1;
    public static final int VIDEOMETHOD_URI = 2;

    public enum NRModePref {
        NRMODE_NORMAL,
        NRMODE_LOW_LIGHT
    }

    public static class NoFreeStorageException extends Exception {
        private static final long serialVersionUID = -2021932609486148748L;
    }

    public enum RawPref {
        RAWPREF_JPEG_ONLY,
        RAWPREF_JPEG_DNG
    }

    public static class VideoMaxFileSize {
        public boolean auto_restart;
        public long max_filesize;
    }

    boolean allowZoom();

    void cameraClosed();

    void cameraInOperation(boolean z, boolean z2);

    void cameraSetup();

    boolean canTakeNewPhoto();

    void clearColorEffectPref();

    void clearExposureCompensationPref();

    void clearExposureTimePref();

    void clearISOPref();

    void clearSceneModePref();

    void clearWhiteBalancePref();

    File createOutputVideoFile(String str) throws IOException;

    int createOutputVideoMethod();

    Uri createOutputVideoSAF(String str) throws IOException;

    Uri createOutputVideoUri();

    String getAntiBandingPref();

    boolean getBurstForNoiseReduction();

    int getBurstNImages();

    double getCalibratedLevelAngle();

    int getCameraIdPref();

    String getCameraNoiseReductionModePref();

    Pair<Integer, Integer> getCameraResolutionPref();

    String getColorEffectPref();

    Context getContext();

    boolean getDoubleTapCapturePref();

    String getEdgeModePref();

    int getExpoBracketingNImagesPref();

    double getExpoBracketingStopsPref();

    int getExposureCompensationPref();

    long getExposureTimePref();

    boolean getFaceDetectionPref();

    String getFlashPref();

    boolean getFocusBracketingAddInfinityPref();

    int getFocusBracketingNImagesPref();

    float getFocusDistancePref(boolean z);

    String getFocusPref(boolean z);

    boolean getForce4KPref();

    boolean getGeotaggingPref();

    String getISOPref();

    int getImageQualityPref();

    Location getLocation();

    String getLockOrientationPref();

    int getMaxRawImages();

    NRModePref getNRModePref();

    boolean getOptimiseAEForDROPref();

    boolean getPausePreviewPref();

    String getPreviewRotationPref();

    String getPreviewSizePref();

    RawPref getRawPref();

    String getRecordAudioChannelsPref();

    boolean getRecordAudioPref();

    String getRecordAudioSourcePref();

    String getRecordVideoOutputFormatPref();

    long getRepeatIntervalPref();

    String getRepeatPref();

    boolean getRequireLocationPref();

    String getSceneModePref();

    boolean getShowToastsPref();

    boolean getShutterSoundPref();

    boolean getStartupFocusPref();

    long getTimerPref();

    boolean getTouchCapturePref();

    String getVideoBitratePref();

    float getVideoCaptureRateFactor();

    String getVideoFPSPref();

    boolean getVideoFlashPref();

    float getVideoLogProfileStrength();

    boolean getVideoLowPowerCheckPref();

    long getVideoMaxDurationPref();

    VideoMaxFileSize getVideoMaxFileSizePref() throws NoFreeStorageException;

    String getVideoQualityPref();

    int getVideoRestartTimesPref();

    boolean getVideoStabilizationPref();

    String getWhiteBalancePref();

    int getWhiteBalanceTemperaturePref();

    int getZoomPref();

    void hasPausedPreview(boolean z);

    boolean imageQueueWouldBlock(int i, int i2);

    boolean isCameraBurstPref();

    boolean isExpoBracketingPref();

    boolean isFocusBracketingPref();

    boolean isPreviewInBackground();

    boolean isTestAlwaysFocus();

    boolean isVideoPref();

    void multitouchZoom(int i);

    boolean needsStoragePermission();

    boolean onBurstPictureTaken(List<byte[]> list, Date date);

    void onCameraError();

    void onCaptureStarted();

    void onContinuousFocusMove(boolean z);

    void onDrawPreview(Canvas canvas);

    void onFailedCreateVideoFileError();

    void onFailedReconnectError();

    void onFailedStartPreview();

    void onPhotoError();

    void onPictureCompleted();

    boolean onPictureTaken(byte[] bArr, Date date);

    boolean onRawBurstPictureTaken(List<RawImage> list, Date date);

    boolean onRawPictureTaken(RawImage rawImage, Date date);

    void onVideoError(int i, int i2);

    void onVideoInfo(int i, int i2);

    void onVideoRecordStartError(VideoProfile videoProfile);

    void onVideoRecordStopError(VideoProfile videoProfile);

    void requestCameraPermission();

    void requestRecordAudioPermission();

    void requestStoragePermission();

    void setCameraIdPref(int i);

    void setCameraResolutionPref(int i, int i2);

    void setColorEffectPref(String str);

    void setExposureCompensationPref(int i);

    void setExposureTimePref(long j);

    void setFlashPref(String str);

    void setFocusDistancePref(float f, boolean z);

    void setFocusPref(String str, boolean z);

    void setISOPref(String str);

    void setSceneModePref(String str);

    void setVideoPref(boolean z);

    void setVideoQualityPref(String str);

    void setWhiteBalancePref(String str);

    void setWhiteBalanceTemperaturePref(int i);

    void setZoomPref(int i);

    void startedVideo();

    void startingVideo();

    void stoppedVideo(int i, Uri uri, String str);

    void stoppingVideo();

    void timerBeep(long j);

    void touchEvent(MotionEvent motionEvent);

    void turnFrontScreenFlashOn();

    boolean useCamera2();

    boolean useCamera2FakeFlash();

    boolean useCamera2FastBurst();

    boolean usePhotoVideoRecording();

    boolean useVideoLogProfile();
}
