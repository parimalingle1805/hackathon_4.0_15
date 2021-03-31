package net.sourceforge.opencamera.preview;

import android.graphics.Canvas;
import android.location.Location;
import android.net.Uri;
import android.util.Pair;
import android.view.MotionEvent;
import java.util.Date;
import java.util.List;
import net.sourceforge.opencamera.BuildConfig;
import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.cameracontroller.RawImage;
import net.sourceforge.opencamera.preview.ApplicationInterface.NRModePref;
import net.sourceforge.opencamera.preview.ApplicationInterface.NoFreeStorageException;
import net.sourceforge.opencamera.preview.ApplicationInterface.RawPref;
import net.sourceforge.opencamera.preview.ApplicationInterface.VideoMaxFileSize;

public abstract class BasicApplicationInterface implements ApplicationInterface {
    public boolean allowZoom() {
        return true;
    }

    public void cameraClosed() {
    }

    public void cameraInOperation(boolean z, boolean z2) {
    }

    public void cameraSetup() {
    }

    public boolean canTakeNewPhoto() {
        return true;
    }

    public void clearColorEffectPref() {
    }

    public void clearExposureCompensationPref() {
    }

    public void clearExposureTimePref() {
    }

    public void clearISOPref() {
    }

    public void clearSceneModePref() {
    }

    public void clearWhiteBalancePref() {
    }

    public String getAntiBandingPref() {
        return "auto";
    }

    public boolean getBurstForNoiseReduction() {
        return false;
    }

    public int getBurstNImages() {
        return 5;
    }

    public double getCalibratedLevelAngle() {
        return 0.0d;
    }

    public int getCameraIdPref() {
        return 0;
    }

    public String getCameraNoiseReductionModePref() {
        return "default";
    }

    public Pair<Integer, Integer> getCameraResolutionPref() {
        return null;
    }

    public String getColorEffectPref() {
        return CameraController.COLOR_EFFECT_DEFAULT;
    }

    public boolean getDoubleTapCapturePref() {
        return false;
    }

    public String getEdgeModePref() {
        return "default";
    }

    public int getExpoBracketingNImagesPref() {
        return 3;
    }

    public double getExpoBracketingStopsPref() {
        return 2.0d;
    }

    public int getExposureCompensationPref() {
        return 0;
    }

    public long getExposureTimePref() {
        return CameraController.EXPOSURE_TIME_DEFAULT;
    }

    public boolean getFaceDetectionPref() {
        return false;
    }

    public String getFlashPref() {
        return "flash_off";
    }

    public boolean getFocusBracketingAddInfinityPref() {
        return false;
    }

    public int getFocusBracketingNImagesPref() {
        return 3;
    }

    public float getFocusDistancePref(boolean z) {
        return 0.0f;
    }

    public String getFocusPref(boolean z) {
        return "focus_mode_continuous_picture";
    }

    public boolean getForce4KPref() {
        return false;
    }

    public boolean getGeotaggingPref() {
        return false;
    }

    public String getISOPref() {
        return "auto";
    }

    public int getImageQualityPref() {
        return 90;
    }

    public Location getLocation() {
        return null;
    }

    public String getLockOrientationPref() {
        return CameraController.COLOR_EFFECT_DEFAULT;
    }

    public int getMaxRawImages() {
        return 2;
    }

    public boolean getOptimiseAEForDROPref() {
        return false;
    }

    public boolean getPausePreviewPref() {
        return false;
    }

    public String getPreviewRotationPref() {
        return "0";
    }

    public String getPreviewSizePref() {
        return "preference_preview_size_wysiwyg";
    }

    public String getRecordAudioChannelsPref() {
        return "audio_default";
    }

    public boolean getRecordAudioPref() {
        return true;
    }

    public String getRecordAudioSourcePref() {
        return "audio_src_camcorder";
    }

    public String getRecordVideoOutputFormatPref() {
        return "preference_video_output_format_default";
    }

    public long getRepeatIntervalPref() {
        return 0;
    }

    public String getRepeatPref() {
        return "1";
    }

    public boolean getRequireLocationPref() {
        return false;
    }

    public String getSceneModePref() {
        return "auto";
    }

    public boolean getShowToastsPref() {
        return true;
    }

    public boolean getShutterSoundPref() {
        return true;
    }

    public boolean getStartupFocusPref() {
        return true;
    }

    public long getTimerPref() {
        return 0;
    }

    public boolean getTouchCapturePref() {
        return false;
    }

    public String getVideoBitratePref() {
        return "default";
    }

    public float getVideoCaptureRateFactor() {
        return 1.0f;
    }

    public String getVideoFPSPref() {
        return "default";
    }

    public boolean getVideoFlashPref() {
        return false;
    }

    public float getVideoLogProfileStrength() {
        return 0.0f;
    }

    public boolean getVideoLowPowerCheckPref() {
        return true;
    }

    public long getVideoMaxDurationPref() {
        return 0;
    }

    public String getVideoQualityPref() {
        return BuildConfig.FLAVOR;
    }

    public int getVideoRestartTimesPref() {
        return 0;
    }

    public boolean getVideoStabilizationPref() {
        return false;
    }

    public String getWhiteBalancePref() {
        return "auto";
    }

    public int getWhiteBalanceTemperaturePref() {
        return 0;
    }

    public int getZoomPref() {
        return 0;
    }

    public void hasPausedPreview(boolean z) {
    }

    public boolean imageQueueWouldBlock(int i, int i2) {
        return false;
    }

    public boolean isCameraBurstPref() {
        return false;
    }

    public boolean isExpoBracketingPref() {
        return false;
    }

    public boolean isFocusBracketingPref() {
        return false;
    }

    public boolean isPreviewInBackground() {
        return false;
    }

    public boolean isTestAlwaysFocus() {
        return false;
    }

    public boolean isVideoPref() {
        return false;
    }

    public void multitouchZoom(int i) {
    }

    public boolean onBurstPictureTaken(List<byte[]> list, Date date) {
        return false;
    }

    public void onCameraError() {
    }

    public void onCaptureStarted() {
    }

    public void onContinuousFocusMove(boolean z) {
    }

    public void onDrawPreview(Canvas canvas) {
    }

    public void onFailedCreateVideoFileError() {
    }

    public void onFailedReconnectError() {
    }

    public void onFailedStartPreview() {
    }

    public void onPhotoError() {
    }

    public void onPictureCompleted() {
    }

    public boolean onRawBurstPictureTaken(List<RawImage> list, Date date) {
        return false;
    }

    public boolean onRawPictureTaken(RawImage rawImage, Date date) {
        return false;
    }

    public void onVideoError(int i, int i2) {
    }

    public void onVideoInfo(int i, int i2) {
    }

    public void onVideoRecordStartError(VideoProfile videoProfile) {
    }

    public void onVideoRecordStopError(VideoProfile videoProfile) {
    }

    public void setCameraIdPref(int i) {
    }

    public void setCameraResolutionPref(int i, int i2) {
    }

    public void setColorEffectPref(String str) {
    }

    public void setExposureCompensationPref(int i) {
    }

    public void setExposureTimePref(long j) {
    }

    public void setFlashPref(String str) {
    }

    public void setFocusDistancePref(float f, boolean z) {
    }

    public void setFocusPref(String str, boolean z) {
    }

    public void setISOPref(String str) {
    }

    public void setSceneModePref(String str) {
    }

    public void setVideoPref(boolean z) {
    }

    public void setVideoQualityPref(String str) {
    }

    public void setWhiteBalancePref(String str) {
    }

    public void setWhiteBalanceTemperaturePref(int i) {
    }

    public void setZoomPref(int i) {
    }

    public void startedVideo() {
    }

    public void startingVideo() {
    }

    public void stoppedVideo(int i, Uri uri, String str) {
    }

    public void stoppingVideo() {
    }

    public void timerBeep(long j) {
    }

    public void touchEvent(MotionEvent motionEvent) {
    }

    public void turnFrontScreenFlashOn() {
    }

    public boolean useCamera2FakeFlash() {
        return false;
    }

    public boolean useCamera2FastBurst() {
        return true;
    }

    public boolean usePhotoVideoRecording() {
        return true;
    }

    public boolean useVideoLogProfile() {
        return false;
    }

    public VideoMaxFileSize getVideoMaxFileSizePref() throws NoFreeStorageException {
        VideoMaxFileSize videoMaxFileSize = new VideoMaxFileSize();
        videoMaxFileSize.max_filesize = 0;
        videoMaxFileSize.auto_restart = true;
        return videoMaxFileSize;
    }

    public NRModePref getNRModePref() {
        return NRModePref.NRMODE_NORMAL;
    }

    public RawPref getRawPref() {
        return RawPref.RAWPREF_JPEG_ONLY;
    }
}
