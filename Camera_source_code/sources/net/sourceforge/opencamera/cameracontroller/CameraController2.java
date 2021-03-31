package net.sourceforge.opencamera.cameracontroller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.params.TonemapCurve;
import android.location.Location;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.p000v4.app.NotificationManagerCompat;
import android.util.Log;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import net.sourceforge.opencamera.BuildConfig;
import net.sourceforge.opencamera.cameracontroller.CameraController.Area;
import net.sourceforge.opencamera.cameracontroller.CameraController.AutoFocusCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController.BurstType;
import net.sourceforge.opencamera.cameracontroller.CameraController.CameraFeatures;
import net.sourceforge.opencamera.cameracontroller.CameraController.ContinuousFocusMoveCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController.ErrorCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController.FaceDetectionListener;
import net.sourceforge.opencamera.cameracontroller.CameraController.PictureCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController.RangeSorter;
import net.sourceforge.opencamera.cameracontroller.CameraController.SizeSorter;
import net.sourceforge.opencamera.cameracontroller.CameraController.SupportedValues;

public class CameraController2 extends CameraController {
    private static final int STATE_NORMAL = 0;
    private static final int STATE_WAITING_AUTOFOCUS = 1;
    private static final int STATE_WAITING_FAKE_PRECAPTURE_DONE = 5;
    private static final int STATE_WAITING_FAKE_PRECAPTURE_START = 4;
    private static final int STATE_WAITING_PRECAPTURE_DONE = 3;
    private static final int STATE_WAITING_PRECAPTURE_START = 2;
    private static final String TAG = "CameraController2";
    private static final boolean do_af_trigger_for_continuous = true;
    private static final int max_expo_bracketing_n_images = 5;
    private static final long max_preview_exposure_time_c = 83333333;
    private static final int max_white_balance_temperature_c = 15000;
    private static final int min_white_balance_temperature_c = 1000;
    private static final long precapture_done_timeout_c = 3000;
    private static final long precapture_start_timeout_c = 2000;
    private static final int tonemap_max_curve_points_c = 64;
    private List<int[]> ae_fps_ranges;
    /* access modifiers changed from: private */
    public AutoFocusCallback autofocus_cb;
    /* access modifiers changed from: private */
    public final Object background_camera_lock = new Object();
    private boolean burst_for_noise_reduction;
    private int burst_requested_n_images;
    /* access modifiers changed from: private */
    public boolean burst_single_request;
    /* access modifiers changed from: private */
    public BurstType burst_type = BurstType.BURSTTYPE_NONE;
    /* access modifiers changed from: private */
    public CameraDevice camera;
    /* access modifiers changed from: private */
    public String cameraIdS;
    private final ErrorCallback camera_error_cb;
    /* access modifiers changed from: private */
    public final CameraSettings camera_settings = new CameraSettings();
    /* access modifiers changed from: private */
    public CameraCaptureSession captureSession;
    /* access modifiers changed from: private */
    public boolean capture_follows_autofocus_hint;
    /* access modifiers changed from: private */
    public Integer capture_result_ae;
    /* access modifiers changed from: private */
    public long capture_result_exposure_time;
    /* access modifiers changed from: private */
    public long capture_result_frame_duration;
    /* access modifiers changed from: private */
    public boolean capture_result_has_exposure_time;
    /* access modifiers changed from: private */
    public boolean capture_result_has_frame_duration;
    /* access modifiers changed from: private */
    public boolean capture_result_has_iso;
    /* access modifiers changed from: private */
    public boolean capture_result_has_white_balance_rggb;
    /* access modifiers changed from: private */
    public boolean capture_result_is_ae_scanning;
    /* access modifiers changed from: private */
    public int capture_result_iso;
    /* access modifiers changed from: private */
    public RggbChannelVector capture_result_white_balance_rggb;
    /* access modifiers changed from: private */
    public CameraCharacteristics characteristics;
    /* access modifiers changed from: private */
    public boolean characteristics_is_front_facing;
    /* access modifiers changed from: private */
    public int characteristics_sensor_orientation;
    /* access modifiers changed from: private */
    public final Context context;
    private boolean continuous_burst_in_progress;
    /* access modifiers changed from: private */
    public boolean continuous_burst_requested_last_capture;
    /* access modifiers changed from: private */
    public ContinuousFocusMoveCallback continuous_focus_move_callback;
    private int current_zoom_value;
    /* access modifiers changed from: private */
    public boolean done_all_captures;
    private int expo_bracketing_n_images = 3;
    private double expo_bracketing_stops = 2.0d;
    /* access modifiers changed from: private */
    public FaceDetectionListener face_detection_listener;
    /* access modifiers changed from: private */
    public boolean fake_precapture_torch_focus_performed;
    /* access modifiers changed from: private */
    public boolean fake_precapture_torch_performed;
    /* access modifiers changed from: private */
    public CaptureRequest fake_precapture_turn_on_torch_id = null;
    private boolean fake_precapture_use_flash;
    private long fake_precapture_use_flash_time_ms = -1;
    private boolean focus_bracketing_add_infinity = false;
    /* access modifiers changed from: private */
    public boolean focus_bracketing_in_progress;
    private int focus_bracketing_n_images = 3;
    private float focus_bracketing_source_distance = 0.0f;
    private float focus_bracketing_target_distance = 0.0f;
    /* access modifiers changed from: private */
    public Handler handler;
    /* access modifiers changed from: private */
    public boolean has_received_frame;
    private List<int[]> hs_fps_ranges;
    private ImageReader imageReader;
    /* access modifiers changed from: private */
    public ImageReader imageReaderRaw;
    /* access modifiers changed from: private */
    public boolean is_flash_required;
    /* access modifiers changed from: private */
    public final boolean is_samsung_s7;
    private boolean is_video_high_speed;
    /* access modifiers changed from: private */
    public boolean jpeg_todo;
    /* access modifiers changed from: private */
    public int last_faces_detected = -1;
    private int max_raw_images;
    private final MediaActionSound media_action_sound = new MediaActionSound();
    /* access modifiers changed from: private */
    public boolean modified_from_camera_settings;
    /* access modifiers changed from: private */
    public int n_burst;
    /* access modifiers changed from: private */
    public int n_burst_raw;
    /* access modifiers changed from: private */
    public int n_burst_taken;
    /* access modifiers changed from: private */
    public int n_burst_total;
    private boolean noise_reduction_low_light;
    /* access modifiers changed from: private */
    public OnRawImageAvailableListener onRawImageAvailableListener;
    /* access modifiers changed from: private */
    public final Object open_camera_lock = new Object();
    private boolean optimise_ae_for_dro = false;
    /* access modifiers changed from: private */
    public final List<byte[]> pending_burst_images = new ArrayList();
    /* access modifiers changed from: private */
    public final List<RawImage> pending_burst_images_raw = new ArrayList();
    /* access modifiers changed from: private */
    public RawImage pending_raw_image;
    /* access modifiers changed from: private */
    public PictureCallback picture_cb;
    private int picture_height;
    private int picture_width;
    /* access modifiers changed from: private */
    public long precapture_state_change_time_ms = -1;
    /* access modifiers changed from: private */
    public Builder previewBuilder;
    /* access modifiers changed from: private */
    public final CaptureCallback previewCaptureCallback = new CaptureCallback() {
        private int last_af_state = -1;
        private long last_process_frame_number = 0;

        private RequestTagType getRequestTagType(CaptureRequest captureRequest) {
            Object tag = captureRequest.getTag();
            if (tag == null) {
                return null;
            }
            return ((RequestTagObject) tag).getType();
        }

        public void onCaptureBufferLost(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, Surface surface, long j) {
            super.onCaptureBufferLost(cameraCaptureSession, captureRequest, surface, j);
        }

        public void onCaptureFailed(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureFailure captureFailure) {
            super.onCaptureFailed(cameraCaptureSession, captureRequest, captureFailure);
        }

        public void onCaptureSequenceAborted(CameraCaptureSession cameraCaptureSession, int i) {
            super.onCaptureSequenceAborted(cameraCaptureSession, i);
        }

        public void onCaptureSequenceCompleted(CameraCaptureSession cameraCaptureSession, int i, long j) {
            super.onCaptureSequenceCompleted(cameraCaptureSession, i, j);
        }

        public void onCaptureStarted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, long j, long j2) {
            super.onCaptureStarted(cameraCaptureSession, captureRequest, j, j2);
        }

        public void onCaptureProgressed(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureResult captureResult) {
            super.onCaptureProgressed(cameraCaptureSession, captureRequest, captureResult);
        }

        public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
            process(captureRequest, totalCaptureResult);
            processCompleted(captureRequest, totalCaptureResult);
            super.onCaptureCompleted(cameraCaptureSession, captureRequest, totalCaptureResult);
        }

        private void updateCachedAECaptureStatus(CaptureResult captureResult) {
            Integer num = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
            Integer num2 = (Integer) captureResult.get(CaptureResult.FLASH_MODE);
            if (!CameraController2.this.use_fake_precapture_mode || ((!CameraController2.this.fake_precapture_torch_focus_performed && !CameraController2.this.fake_precapture_torch_performed) || num2 == null || num2.intValue() != 2)) {
                if (num == null) {
                    CameraController2.this.capture_result_ae = null;
                    CameraController2.this.is_flash_required = false;
                } else if (!num.equals(CameraController2.this.capture_result_ae)) {
                    CameraController2.this.capture_result_ae = num;
                    if (CameraController2.this.capture_result_ae.intValue() == 4 && !CameraController2.this.is_flash_required) {
                        CameraController2.this.is_flash_required = CameraController2.do_af_trigger_for_continuous;
                    } else if (CameraController2.this.capture_result_ae.intValue() == 2 && CameraController2.this.is_flash_required) {
                        CameraController2.this.is_flash_required = false;
                    }
                }
            }
            if (num == null || num.intValue() != 1) {
                CameraController2.this.capture_result_is_ae_scanning = false;
            } else {
                CameraController2.this.capture_result_is_ae_scanning = CameraController2.do_af_trigger_for_continuous;
            }
        }

        private void handleStateChange(CaptureRequest captureRequest, CaptureResult captureResult) {
            CaptureResult captureResult2 = captureResult;
            Integer num = (Integer) captureResult2.get(CaptureResult.CONTROL_AF_STATE);
            Integer num2 = (Integer) captureResult2.get(CaptureResult.CONTROL_AE_STATE);
            boolean z = CameraController2.do_af_trigger_for_continuous;
            if (num == null || num.intValue() != 1) {
                CameraController2.this.ready_for_capture = CameraController2.do_af_trigger_for_continuous;
                if (CameraController2.this.autofocus_cb != null && CameraController2.this.use_fake_precapture_mode && CameraController2.this.focusIsContinuous()) {
                    Integer num3 = (Integer) CameraController2.this.previewBuilder.get(CaptureRequest.CONTROL_AF_MODE);
                    if (num3 != null && num3.intValue() == 4) {
                        boolean z2 = (num == null || !(num.intValue() == 4 || num.intValue() == 2)) ? false : CameraController2.do_af_trigger_for_continuous;
                        if (num == null) {
                            CameraController2.this.test_af_state_null_focus++;
                        }
                        CameraController2.this.autofocus_cb.onAutoFocus(z2);
                        CameraController2.this.autofocus_cb = null;
                        CameraController2.this.capture_follows_autofocus_hint = false;
                    }
                }
            } else {
                CameraController2.this.ready_for_capture = false;
            }
            if (CameraController2.this.fake_precapture_turn_on_torch_id != null && CameraController2.this.fake_precapture_turn_on_torch_id == captureRequest) {
                CameraController2.this.fake_precapture_turn_on_torch_id = null;
            }
            if (CameraController2.this.state != 0) {
                if (CameraController2.this.state != 1) {
                    int access$11600 = CameraController2.this.state;
                    String str = CameraController2.TAG;
                    if (access$11600 == 2) {
                        if (num2 == null || num2.intValue() == 5) {
                            CameraController2.this.state = 3;
                            CameraController2.this.precapture_state_change_time_ms = System.currentTimeMillis();
                        } else if (CameraController2.this.precapture_state_change_time_ms != -1 && System.currentTimeMillis() - CameraController2.this.precapture_state_change_time_ms > CameraController2.precapture_start_timeout_c) {
                            Log.e(str, "precapture start timeout");
                            CameraController2.this.count_precapture_timeout++;
                            CameraController2.this.state = 3;
                            CameraController2.this.precapture_state_change_time_ms = System.currentTimeMillis();
                        }
                    } else if (CameraController2.this.state == 3) {
                        if (num2 == null || num2.intValue() != 5) {
                            CameraController2.this.state = 0;
                            CameraController2.this.precapture_state_change_time_ms = -1;
                            CameraController2.this.takePictureAfterPrecapture();
                        } else if (CameraController2.this.precapture_state_change_time_ms != -1 && System.currentTimeMillis() - CameraController2.this.precapture_state_change_time_ms > CameraController2.precapture_done_timeout_c) {
                            Log.e(str, "precapture done timeout");
                            CameraController2.this.count_precapture_timeout++;
                            CameraController2.this.state = 0;
                            CameraController2.this.precapture_state_change_time_ms = -1;
                            CameraController2.this.takePictureAfterPrecapture();
                        }
                    } else if (CameraController2.this.state == 4) {
                        CameraController2.this.fake_precapture_turn_on_torch_id;
                        if (CameraController2.this.fake_precapture_turn_on_torch_id == null && (num2 == null || num2.intValue() == 1)) {
                            CameraController2.this.state = 5;
                            CameraController2.this.precapture_state_change_time_ms = System.currentTimeMillis();
                        } else if (CameraController2.this.precapture_state_change_time_ms != -1 && System.currentTimeMillis() - CameraController2.this.precapture_state_change_time_ms > CameraController2.precapture_start_timeout_c) {
                            Log.e(str, "fake precapture start timeout");
                            CameraController2.this.count_precapture_timeout++;
                            CameraController2.this.state = 5;
                            CameraController2.this.precapture_state_change_time_ms = System.currentTimeMillis();
                            CameraController2.this.fake_precapture_turn_on_torch_id = null;
                        }
                    } else if (CameraController2.this.state != 5) {
                    } else {
                        if (CameraController2.this.ready_for_capture && (num2 == null || num2.intValue() != 1)) {
                            CameraController2.this.state = 0;
                            CameraController2.this.precapture_state_change_time_ms = -1;
                            CameraController2.this.takePictureAfterPrecapture();
                        } else if (CameraController2.this.precapture_state_change_time_ms != -1 && System.currentTimeMillis() - CameraController2.this.precapture_state_change_time_ms > CameraController2.precapture_done_timeout_c) {
                            Log.e(str, "fake precapture done timeout");
                            CameraController2.this.count_precapture_timeout++;
                            CameraController2.this.state = 0;
                            CameraController2.this.precapture_state_change_time_ms = -1;
                            CameraController2.this.takePictureAfterPrecapture();
                        }
                    }
                } else if (num == null) {
                    CameraController2.this.test_af_state_null_focus++;
                    CameraController2.this.state = 0;
                    CameraController2.this.precapture_state_change_time_ms = -1;
                    if (CameraController2.this.autofocus_cb != null) {
                        CameraController2.this.autofocus_cb.onAutoFocus(false);
                        CameraController2.this.autofocus_cb = null;
                    }
                    CameraController2.this.capture_follows_autofocus_hint = false;
                } else if (num.intValue() == this.last_af_state) {
                } else {
                    if (num.intValue() == 4 || num.intValue() == 5) {
                        if (!(num.intValue() == 4 || num.intValue() == 2)) {
                            z = false;
                        }
                        CameraController2.this.state = 0;
                        CameraController2.this.precapture_state_change_time_ms = -1;
                        if (CameraController2.this.use_fake_precapture_mode && CameraController2.this.fake_precapture_torch_focus_performed) {
                            CameraController2.this.fake_precapture_torch_focus_performed = false;
                            if (!CameraController2.this.capture_follows_autofocus_hint) {
                                String access$6400 = CameraController2.this.camera_settings.flash_value;
                                CameraController2.this.camera_settings.flash_value = "flash_off";
                                CameraController2.this.camera_settings.setAEMode(CameraController2.this.previewBuilder, false);
                                try {
                                    CameraController2.this.capture();
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                                CameraController2.this.camera_settings.flash_value = access$6400;
                                CameraController2.this.camera_settings.setAEMode(CameraController2.this.previewBuilder, false);
                                try {
                                    CameraController2.this.setRepeatingRequest();
                                } catch (CameraAccessException e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                        if (CameraController2.this.autofocus_cb != null) {
                            CameraController2.this.autofocus_cb.onAutoFocus(z);
                            CameraController2.this.autofocus_cb = null;
                        }
                        CameraController2.this.capture_follows_autofocus_hint = false;
                    }
                }
            }
        }

        private void handleContinuousFocusMove(CaptureResult captureResult) {
            Integer num = (Integer) captureResult.get(CaptureResult.CONTROL_AF_STATE);
            if (num == null || num.intValue() != 1 || num.intValue() == this.last_af_state) {
                if (num != null && this.last_af_state == 1 && num.intValue() != this.last_af_state && CameraController2.this.continuous_focus_move_callback != null) {
                    CameraController2.this.continuous_focus_move_callback.onContinuousFocusMove(false);
                }
            } else if (CameraController2.this.continuous_focus_move_callback != null) {
                CameraController2.this.continuous_focus_move_callback.onContinuousFocusMove(CameraController2.do_af_trigger_for_continuous);
            }
        }

        private void process(CaptureRequest captureRequest, CaptureResult captureResult) {
            if (captureResult.getFrameNumber() >= this.last_process_frame_number) {
                this.last_process_frame_number = captureResult.getFrameNumber();
                updateCachedAECaptureStatus(captureResult);
                handleStateChange(captureRequest, captureResult);
                handleContinuousFocusMove(captureResult);
                Integer num = (Integer) captureResult.get(CaptureResult.CONTROL_AF_STATE);
                if (!(num == null || num.intValue() == this.last_af_state)) {
                    this.last_af_state = num.intValue();
                }
            }
        }

        private void updateCachedCaptureResult(CaptureResult captureResult) {
            if (!CameraController2.this.modified_from_camera_settings) {
                if (captureResult.get(CaptureResult.SENSOR_SENSITIVITY) != null) {
                    CameraController2.this.capture_result_has_iso = CameraController2.do_af_trigger_for_continuous;
                    CameraController2.this.capture_result_iso = ((Integer) captureResult.get(CaptureResult.SENSOR_SENSITIVITY)).intValue();
                } else {
                    CameraController2.this.capture_result_has_iso = false;
                }
            }
            if (!CameraController2.this.modified_from_camera_settings) {
                if (captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME) != null) {
                    CameraController2.this.capture_result_has_exposure_time = CameraController2.do_af_trigger_for_continuous;
                    CameraController2.this.capture_result_exposure_time = ((Long) captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME)).longValue();
                    if (CameraController2.this.camera_settings.has_iso && CameraController2.this.camera_settings.exposure_time > CameraController2.max_preview_exposure_time_c) {
                        CameraController2 cameraController2 = CameraController2.this;
                        cameraController2.capture_result_exposure_time = cameraController2.camera_settings.exposure_time;
                    }
                    if (CameraController2.this.capture_result_exposure_time <= 0) {
                        CameraController2.this.capture_result_has_exposure_time = false;
                    }
                } else {
                    CameraController2.this.capture_result_has_exposure_time = false;
                }
            }
            if (!CameraController2.this.modified_from_camera_settings) {
                if (captureResult.get(CaptureResult.SENSOR_FRAME_DURATION) != null) {
                    CameraController2.this.capture_result_has_frame_duration = CameraController2.do_af_trigger_for_continuous;
                    CameraController2.this.capture_result_frame_duration = ((Long) captureResult.get(CaptureResult.SENSOR_FRAME_DURATION)).longValue();
                } else {
                    CameraController2.this.capture_result_has_frame_duration = false;
                }
            }
            RggbChannelVector rggbChannelVector = (RggbChannelVector) captureResult.get(CaptureResult.COLOR_CORRECTION_GAINS);
            if (!CameraController2.this.modified_from_camera_settings && rggbChannelVector != null) {
                CameraController2.this.capture_result_has_white_balance_rggb = CameraController2.do_af_trigger_for_continuous;
                CameraController2.this.capture_result_white_balance_rggb = rggbChannelVector;
            }
        }

        private void handleFaceDetection(CaptureResult captureResult) {
            if (CameraController2.this.face_detection_listener != null && CameraController2.this.previewBuilder != null) {
                Integer num = (Integer) CameraController2.this.previewBuilder.get(CaptureRequest.STATISTICS_FACE_DETECT_MODE);
                if (num != null && num.intValue() != 0) {
                    Rect access$13100 = CameraController2.this.getViewableRect();
                    Face[] faceArr = (Face[]) captureResult.get(CaptureResult.STATISTICS_FACES);
                    if (faceArr == null) {
                        return;
                    }
                    if (faceArr.length != 0 || CameraController2.this.last_faces_detected != 0) {
                        CameraController2.this.last_faces_detected = faceArr.length;
                        CameraController.Face[] faceArr2 = new CameraController.Face[faceArr.length];
                        for (int i = 0; i < faceArr.length; i++) {
                            faceArr2[i] = CameraController2.this.convertFromCameraFace(access$13100, faceArr[i]);
                        }
                        CameraController2.this.face_detection_listener.onFaceDetection(faceArr2);
                    }
                }
            }
        }

        private void handleRawCaptureResult(CaptureResult captureResult) {
            if (CameraController2.this.test_wait_capture_result) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (CameraController2.this.onRawImageAvailableListener != null) {
                CameraController2.this.onRawImageAvailableListener.setCaptureResult(captureResult);
            }
        }

        private void handleCaptureBurstInProgress(CaptureResult captureResult) {
            handleRawCaptureResult(captureResult);
        }

        private void handleCaptureCompleted(CaptureResult captureResult) {
            CameraController2.this.test_capture_results++;
            CameraController2.this.modified_from_camera_settings = false;
            handleRawCaptureResult(captureResult);
            if (CameraController2.this.previewBuilder != null) {
                CameraController2.this.previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(2));
                String access$6400 = CameraController2.this.camera_settings.flash_value;
                if (CameraController2.this.use_fake_precapture_mode && CameraController2.this.fake_precapture_torch_performed) {
                    CameraController2.this.camera_settings.flash_value = "flash_off";
                }
                CameraController2.this.camera_settings.setAEMode(CameraController2.this.previewBuilder, false);
                try {
                    CameraController2.this.capture();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                if (CameraController2.this.use_fake_precapture_mode && CameraController2.this.fake_precapture_torch_performed) {
                    CameraController2.this.camera_settings.flash_value = access$6400;
                    CameraController2.this.camera_settings.setAEMode(CameraController2.this.previewBuilder, false);
                }
                CameraController2.this.previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
                try {
                    CameraController2.this.setRepeatingRequest();
                } catch (CameraAccessException e2) {
                    e2.printStackTrace();
                    CameraController2.this.preview_error_cb.onError();
                }
            }
            CameraController2.this.fake_precapture_torch_performed = false;
            if (CameraController2.this.burst_type == BurstType.BURSTTYPE_FOCUS && CameraController2.this.previewBuilder != null) {
                CameraController2.this.camera_settings.setFocusDistance(CameraController2.this.previewBuilder);
                try {
                    CameraController2.this.setRepeatingRequest();
                } catch (CameraAccessException e3) {
                    e3.printStackTrace();
                }
            }
            ((Activity) CameraController2.this.context).runOnUiThread(new Runnable() {
                public void run() {
                    synchronized (CameraController2.this.background_camera_lock) {
                        CameraController2.this.done_all_captures = CameraController2.do_af_trigger_for_continuous;
                    }
                    CameraController2.this.checkImagesCompleted();
                }
            });
        }

        private void processCompleted(CaptureRequest captureRequest, CaptureResult captureResult) {
            if (!CameraController2.this.has_received_frame) {
                CameraController2.this.has_received_frame = CameraController2.do_af_trigger_for_continuous;
            }
            updateCachedCaptureResult(captureResult);
            handleFaceDetection(captureResult);
            if (CameraController2.this.push_repeating_request_when_torch_off && CameraController2.this.push_repeating_request_when_torch_off_id == captureRequest && CameraController2.this.previewBuilder != null) {
                Integer num = (Integer) captureResult.get(CaptureResult.FLASH_STATE);
                if (num != null && num.intValue() == 2) {
                    CameraController2.this.push_repeating_request_when_torch_off = false;
                    CameraController2.this.push_repeating_request_when_torch_off_id = null;
                    try {
                        CameraController2.this.setRepeatingRequest();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            RequestTagType requestTagType = getRequestTagType(captureRequest);
            if (requestTagType == RequestTagType.CAPTURE) {
                handleCaptureCompleted(captureResult);
            } else if (requestTagType == RequestTagType.CAPTURE_BURST_IN_PROGRESS) {
                handleCaptureBurstInProgress(captureResult);
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean previewIsVideoMode;
    /* access modifiers changed from: private */
    public final ErrorCallback preview_error_cb;
    private int preview_height;
    private int preview_width;
    /* access modifiers changed from: private */
    public boolean push_repeating_request_when_torch_off = false;
    /* access modifiers changed from: private */
    public CaptureRequest push_repeating_request_when_torch_off_id = null;
    private Size raw_size;
    /* access modifiers changed from: private */
    public boolean raw_todo;
    /* access modifiers changed from: private */
    public boolean ready_for_capture;
    /* access modifiers changed from: private */
    public List<CaptureRequest> slow_burst_capture_requests;
    private long slow_burst_start_ms = 0;
    private boolean sounds_enabled = do_af_trigger_for_continuous;
    /* access modifiers changed from: private */
    public int state = 0;
    private boolean supports_face_detect_mode_full;
    private boolean supports_face_detect_mode_simple;
    private boolean supports_photo_video_recording;
    private Surface surface_texture;
    /* access modifiers changed from: private */
    public ErrorCallback take_picture_error_cb;
    private SurfaceTexture texture;
    private HandlerThread thread;
    private boolean use_expo_fast_burst = do_af_trigger_for_continuous;
    private boolean use_fake_precapture;
    /* access modifiers changed from: private */
    public boolean use_fake_precapture_mode;
    /* access modifiers changed from: private */
    public Surface video_recorder_surface;
    /* access modifiers changed from: private */
    public boolean want_raw;
    private boolean want_video_high_speed;
    private List<Integer> zoom_ratios;

    private class CameraSettings {
        /* access modifiers changed from: private */
        public int ae_exposure_compensation;
        /* access modifiers changed from: private */
        public boolean ae_lock;
        /* access modifiers changed from: private */
        public MeteringRectangle[] ae_regions;
        /* access modifiers changed from: private */
        public Range<Integer> ae_target_fps_range;
        /* access modifiers changed from: private */
        public int af_mode;
        /* access modifiers changed from: private */
        public MeteringRectangle[] af_regions;
        /* access modifiers changed from: private */
        public int antibanding;
        /* access modifiers changed from: private */
        public int color_effect;
        private Integer default_edge_mode;
        private Integer default_noise_reduction_mode;
        private Integer default_tonemap_mode;
        /* access modifiers changed from: private */
        public int edge_mode;
        /* access modifiers changed from: private */
        public long exposure_time;
        /* access modifiers changed from: private */
        public int face_detect_mode;
        /* access modifiers changed from: private */
        public String flash_value;
        /* access modifiers changed from: private */
        public float focus_distance;
        /* access modifiers changed from: private */
        public float focus_distance_manual;
        /* access modifiers changed from: private */
        public boolean has_ae_exposure_compensation;
        /* access modifiers changed from: private */
        public boolean has_af_mode;
        /* access modifiers changed from: private */
        public boolean has_antibanding;
        /* access modifiers changed from: private */
        public boolean has_edge_mode;
        /* access modifiers changed from: private */
        public boolean has_face_detect_mode;
        /* access modifiers changed from: private */
        public boolean has_iso;
        /* access modifiers changed from: private */
        public boolean has_noise_reduction_mode;
        /* access modifiers changed from: private */
        public int iso;
        /* access modifiers changed from: private */
        public byte jpeg_quality;
        /* access modifiers changed from: private */
        public Location location;
        /* access modifiers changed from: private */
        public float log_profile_strength;
        /* access modifiers changed from: private */
        public int noise_reduction_mode;
        /* access modifiers changed from: private */
        public int rotation;
        /* access modifiers changed from: private */
        public Rect scalar_crop_region;
        /* access modifiers changed from: private */
        public int scene_mode;
        /* access modifiers changed from: private */
        public long sensor_frame_duration;
        /* access modifiers changed from: private */
        public boolean use_log_profile;
        /* access modifiers changed from: private */
        public boolean video_stabilization;
        /* access modifiers changed from: private */
        public boolean wb_lock;
        /* access modifiers changed from: private */
        public int white_balance;
        /* access modifiers changed from: private */
        public int white_balance_temperature;

        private CameraSettings() {
            this.jpeg_quality = 90;
            this.scene_mode = 0;
            this.color_effect = 0;
            this.white_balance = 1;
            this.antibanding = 3;
            this.edge_mode = 1;
            this.noise_reduction_mode = 1;
            this.white_balance_temperature = 5000;
            this.flash_value = "flash_off";
            this.exposure_time = CameraController.EXPOSURE_TIME_DEFAULT;
            this.af_mode = 1;
            this.face_detect_mode = 0;
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x002e, code lost:
            if (r5.this$0.isFrontFacing() != false) goto L_0x0023;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
            return 8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x001f, code lost:
            if (r5.this$0.isFrontFacing() != false) goto L_0x0021;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int getExifOrientation() {
            /*
                r5 = this;
                int r0 = r5.rotation
                int r0 = r0 + 360
                int r0 = r0 % 360
                r1 = 1
                if (r0 == 0) goto L_0x0031
                r2 = 90
                r3 = 8
                r4 = 6
                if (r0 == r2) goto L_0x0028
                r2 = 180(0xb4, float:2.52E-43)
                if (r0 == r2) goto L_0x0026
                r2 = 270(0x10e, float:3.78E-43)
                if (r0 == r2) goto L_0x0019
                goto L_0x0031
            L_0x0019:
                net.sourceforge.opencamera.cameracontroller.CameraController2 r0 = net.sourceforge.opencamera.cameracontroller.CameraController2.this
                boolean r0 = r0.isFrontFacing()
                if (r0 == 0) goto L_0x0023
            L_0x0021:
                r1 = 6
                goto L_0x0031
            L_0x0023:
                r1 = 8
                goto L_0x0031
            L_0x0026:
                r1 = 3
                goto L_0x0031
            L_0x0028:
                net.sourceforge.opencamera.cameracontroller.CameraController2 r0 = net.sourceforge.opencamera.cameracontroller.CameraController2.this
                boolean r0 = r0.isFrontFacing()
                if (r0 == 0) goto L_0x0021
                goto L_0x0023
            L_0x0031:
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.CameraSettings.getExifOrientation():int");
        }

        /* access modifiers changed from: private */
        public void setupBuilder(Builder builder, boolean z) {
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
            setSceneMode(builder);
            setColorEffect(builder);
            setWhiteBalance(builder);
            setAntiBanding(builder);
            setAEMode(builder, z);
            setCropRegion(builder);
            setExposureCompensation(builder);
            setFocusMode(builder);
            setFocusDistance(builder);
            setAutoExposureLock(builder);
            setAutoWhiteBalanceLock(builder);
            setAFRegions(builder);
            setAERegions(builder);
            setFaceDetectMode(builder);
            setRawMode(builder);
            setVideoStabilization(builder);
            setLogProfile(builder);
            if (z) {
                if (this.location != null) {
                    builder.set(CaptureRequest.JPEG_GPS_LOCATION, this.location);
                }
                builder.set(CaptureRequest.JPEG_ORIENTATION, Integer.valueOf(this.rotation));
                builder.set(CaptureRequest.JPEG_QUALITY, Byte.valueOf(this.jpeg_quality));
            }
            setEdgeMode(builder);
            setNoiseReductionMode(builder);
        }

        /* access modifiers changed from: private */
        public boolean setSceneMode(Builder builder) {
            Integer num = (Integer) builder.get(CaptureRequest.CONTROL_SCENE_MODE);
            boolean z = this.has_face_detect_mode;
            Integer valueOf = Integer.valueOf(2);
            Integer valueOf2 = Integer.valueOf(1);
            if (z) {
                if (num == null || num.intValue() != 1) {
                    builder.set(CaptureRequest.CONTROL_MODE, valueOf);
                    builder.set(CaptureRequest.CONTROL_SCENE_MODE, valueOf2);
                    return CameraController2.do_af_trigger_for_continuous;
                }
            } else if (num == null || num.intValue() != this.scene_mode) {
                if (this.scene_mode == 0) {
                    builder.set(CaptureRequest.CONTROL_MODE, valueOf2);
                } else {
                    builder.set(CaptureRequest.CONTROL_MODE, valueOf);
                }
                builder.set(CaptureRequest.CONTROL_SCENE_MODE, Integer.valueOf(this.scene_mode));
                return CameraController2.do_af_trigger_for_continuous;
            }
            return false;
        }

        /* access modifiers changed from: private */
        public boolean setColorEffect(Builder builder) {
            if (builder.get(CaptureRequest.CONTROL_EFFECT_MODE) != null && ((Integer) builder.get(CaptureRequest.CONTROL_EFFECT_MODE)).intValue() == this.color_effect) {
                return false;
            }
            builder.set(CaptureRequest.CONTROL_EFFECT_MODE, Integer.valueOf(this.color_effect));
            return CameraController2.do_af_trigger_for_continuous;
        }

        /* access modifiers changed from: private */
        public boolean setWhiteBalance(Builder builder) {
            boolean z;
            if (builder.get(CaptureRequest.CONTROL_AWB_MODE) == null || ((Integer) builder.get(CaptureRequest.CONTROL_AWB_MODE)).intValue() != this.white_balance) {
                builder.set(CaptureRequest.CONTROL_AWB_MODE, Integer.valueOf(this.white_balance));
                z = CameraController2.do_af_trigger_for_continuous;
            } else {
                z = false;
            }
            if (this.white_balance != 0) {
                return z;
            }
            RggbChannelVector access$000 = CameraController2.this.convertTemperatureToRggb(this.white_balance_temperature);
            builder.set(CaptureRequest.COLOR_CORRECTION_MODE, Integer.valueOf(0));
            builder.set(CaptureRequest.COLOR_CORRECTION_GAINS, access$000);
            return CameraController2.do_af_trigger_for_continuous;
        }

        /* access modifiers changed from: private */
        public boolean setAntiBanding(Builder builder) {
            if (!this.has_antibanding || (builder.get(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE) != null && ((Integer) builder.get(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE)).intValue() == this.antibanding)) {
                return false;
            }
            builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, Integer.valueOf(this.antibanding));
            return CameraController2.do_af_trigger_for_continuous;
        }

        /* access modifiers changed from: private */
        public boolean setEdgeMode(Builder builder) {
            if (this.has_edge_mode) {
                if (this.default_edge_mode == null) {
                    this.default_edge_mode = (Integer) builder.get(CaptureRequest.EDGE_MODE);
                }
                if (builder.get(CaptureRequest.EDGE_MODE) == null || ((Integer) builder.get(CaptureRequest.EDGE_MODE)).intValue() != this.edge_mode) {
                    builder.set(CaptureRequest.EDGE_MODE, Integer.valueOf(this.edge_mode));
                    return CameraController2.do_af_trigger_for_continuous;
                }
            } else if (CameraController2.this.is_samsung_s7) {
                builder.set(CaptureRequest.EDGE_MODE, Integer.valueOf(0));
            } else if (!(this.default_edge_mode == null || builder.get(CaptureRequest.EDGE_MODE) == null || ((Integer) builder.get(CaptureRequest.EDGE_MODE)).equals(this.default_edge_mode))) {
                builder.set(CaptureRequest.EDGE_MODE, this.default_edge_mode);
                return CameraController2.do_af_trigger_for_continuous;
            }
            return false;
        }

        /* access modifiers changed from: private */
        public boolean setNoiseReductionMode(Builder builder) {
            if (this.has_noise_reduction_mode) {
                if (this.default_noise_reduction_mode == null) {
                    this.default_noise_reduction_mode = (Integer) builder.get(CaptureRequest.NOISE_REDUCTION_MODE);
                }
                if (builder.get(CaptureRequest.NOISE_REDUCTION_MODE) == null || ((Integer) builder.get(CaptureRequest.NOISE_REDUCTION_MODE)).intValue() != this.noise_reduction_mode) {
                    builder.set(CaptureRequest.NOISE_REDUCTION_MODE, Integer.valueOf(this.noise_reduction_mode));
                    return CameraController2.do_af_trigger_for_continuous;
                }
            } else if (CameraController2.this.is_samsung_s7) {
                builder.set(CaptureRequest.NOISE_REDUCTION_MODE, Integer.valueOf(0));
            } else if (!(this.default_noise_reduction_mode == null || builder.get(CaptureRequest.NOISE_REDUCTION_MODE) == null || ((Integer) builder.get(CaptureRequest.NOISE_REDUCTION_MODE)).equals(this.default_noise_reduction_mode))) {
                builder.set(CaptureRequest.NOISE_REDUCTION_MODE, this.default_noise_reduction_mode);
                return CameraController2.do_af_trigger_for_continuous;
            }
            return false;
        }

        /* access modifiers changed from: private */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean setAEMode(android.hardware.camera2.CaptureRequest.Builder r10, boolean r11) {
            /*
                r9 = this;
                boolean r0 = r9.has_iso
                r1 = 1
                java.lang.Integer r2 = java.lang.Integer.valueOf(r1)
                r3 = 0
                java.lang.Integer r4 = java.lang.Integer.valueOf(r3)
                if (r0 == 0) goto L_0x004c
                android.hardware.camera2.CaptureRequest$Key r0 = android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE
                r10.set(r0, r4)
                android.hardware.camera2.CaptureRequest$Key r0 = android.hardware.camera2.CaptureRequest.SENSOR_SENSITIVITY
                int r2 = r9.iso
                java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
                r10.set(r0, r2)
                long r2 = r9.exposure_time
                if (r11 != 0) goto L_0x0029
                r5 = 83333333(0x4f790d5, double:4.1172137E-316)
                long r2 = java.lang.Math.min(r2, r5)
            L_0x0029:
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.SENSOR_EXPOSURE_TIME
                java.lang.Long r0 = java.lang.Long.valueOf(r2)
                r10.set(r11, r0)
                long r2 = r9.sensor_frame_duration
                r5 = 0
                int r11 = (r2 > r5 ? 1 : (r2 == r5 ? 0 : -1))
                if (r11 <= 0) goto L_0x0045
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.SENSOR_FRAME_DURATION
                long r2 = r9.sensor_frame_duration
                java.lang.Long r0 = java.lang.Long.valueOf(r2)
                r10.set(r11, r0)
            L_0x0045:
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.FLASH_MODE
                r10.set(r11, r4)
                goto L_0x0133
            L_0x004c:
                android.util.Range<java.lang.Integer> r11 = r9.ae_target_fps_range
                if (r11 == 0) goto L_0x006f
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                r11.<init>()
                java.lang.String r0 = "set ae_target_fps_range: "
                r11.append(r0)
                android.util.Range<java.lang.Integer> r0 = r9.ae_target_fps_range
                r11.append(r0)
                java.lang.String r11 = r11.toString()
                java.lang.String r0 = "CameraController2"
                android.util.Log.d(r0, r11)
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE
                android.util.Range<java.lang.Integer> r0 = r9.ae_target_fps_range
                r10.set(r11, r0)
            L_0x006f:
                java.lang.String r11 = r9.flash_value
                r0 = -1
                int r5 = r11.hashCode()
                r6 = 4
                r7 = 3
                r8 = 2
                switch(r5) {
                    case -1524012984: goto L_0x00c3;
                    case -1195303778: goto L_0x00b9;
                    case -1146923872: goto L_0x00af;
                    case -10523976: goto L_0x00a5;
                    case 17603715: goto L_0x009b;
                    case 1617654509: goto L_0x0091;
                    case 1625570446: goto L_0x0087;
                    case 2008442932: goto L_0x007d;
                    default: goto L_0x007c;
                }
            L_0x007c:
                goto L_0x00cd
            L_0x007d:
                java.lang.String r3 = "flash_red_eye"
                boolean r11 = r11.equals(r3)
                if (r11 == 0) goto L_0x00cd
                r11 = 4
                goto L_0x00ce
            L_0x0087:
                java.lang.String r3 = "flash_on"
                boolean r11 = r11.equals(r3)
                if (r11 == 0) goto L_0x00cd
                r11 = 2
                goto L_0x00ce
            L_0x0091:
                java.lang.String r3 = "flash_torch"
                boolean r11 = r11.equals(r3)
                if (r11 == 0) goto L_0x00cd
                r11 = 3
                goto L_0x00ce
            L_0x009b:
                java.lang.String r3 = "flash_frontscreen_torch"
                boolean r11 = r11.equals(r3)
                if (r11 == 0) goto L_0x00cd
                r11 = 7
                goto L_0x00ce
            L_0x00a5:
                java.lang.String r3 = "flash_frontscreen_on"
                boolean r11 = r11.equals(r3)
                if (r11 == 0) goto L_0x00cd
                r11 = 6
                goto L_0x00ce
            L_0x00af:
                java.lang.String r5 = "flash_off"
                boolean r11 = r11.equals(r5)
                if (r11 == 0) goto L_0x00cd
                r11 = 0
                goto L_0x00ce
            L_0x00b9:
                java.lang.String r3 = "flash_auto"
                boolean r11 = r11.equals(r3)
                if (r11 == 0) goto L_0x00cd
                r11 = 1
                goto L_0x00ce
            L_0x00c3:
                java.lang.String r3 = "flash_frontscreen_auto"
                boolean r11 = r11.equals(r3)
                if (r11 == 0) goto L_0x00cd
                r11 = 5
                goto L_0x00ce
            L_0x00cd:
                r11 = -1
            L_0x00ce:
                switch(r11) {
                    case 0: goto L_0x0129;
                    case 1: goto L_0x011a;
                    case 2: goto L_0x010b;
                    case 3: goto L_0x00fc;
                    case 4: goto L_0x00dd;
                    case 5: goto L_0x00d2;
                    case 6: goto L_0x00d2;
                    case 7: goto L_0x00d2;
                    default: goto L_0x00d1;
                }
            L_0x00d1:
                goto L_0x0133
            L_0x00d2:
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE
                r10.set(r11, r2)
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.FLASH_MODE
                r10.set(r11, r4)
                goto L_0x0133
            L_0x00dd:
                net.sourceforge.opencamera.cameracontroller.CameraController2 r11 = net.sourceforge.opencamera.cameracontroller.CameraController2.this
                net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r11 = r11.burst_type
                net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r0 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_NONE
                if (r11 == r0) goto L_0x00ed
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE
                r10.set(r11, r2)
                goto L_0x00f6
            L_0x00ed:
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE
                java.lang.Integer r0 = java.lang.Integer.valueOf(r6)
                r10.set(r11, r0)
            L_0x00f6:
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.FLASH_MODE
                r10.set(r11, r4)
                goto L_0x0133
            L_0x00fc:
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE
                r10.set(r11, r2)
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.FLASH_MODE
                java.lang.Integer r0 = java.lang.Integer.valueOf(r8)
                r10.set(r11, r0)
                goto L_0x0133
            L_0x010b:
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE
                java.lang.Integer r0 = java.lang.Integer.valueOf(r7)
                r10.set(r11, r0)
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.FLASH_MODE
                r10.set(r11, r4)
                goto L_0x0133
            L_0x011a:
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE
                java.lang.Integer r0 = java.lang.Integer.valueOf(r8)
                r10.set(r11, r0)
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.FLASH_MODE
                r10.set(r11, r4)
                goto L_0x0133
            L_0x0129:
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE
                r10.set(r11, r2)
                android.hardware.camera2.CaptureRequest$Key r11 = android.hardware.camera2.CaptureRequest.FLASH_MODE
                r10.set(r11, r4)
            L_0x0133:
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.CameraSettings.setAEMode(android.hardware.camera2.CaptureRequest$Builder, boolean):boolean");
        }

        /* access modifiers changed from: private */
        public void setCropRegion(Builder builder) {
            if (this.scalar_crop_region != null) {
                builder.set(CaptureRequest.SCALER_CROP_REGION, this.scalar_crop_region);
            }
        }

        /* access modifiers changed from: private */
        public boolean setExposureCompensation(Builder builder) {
            if (!this.has_ae_exposure_compensation || this.has_iso) {
                return false;
            }
            if (builder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION) != null && this.ae_exposure_compensation == ((Integer) builder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION)).intValue()) {
                return false;
            }
            builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, Integer.valueOf(this.ae_exposure_compensation));
            return CameraController2.do_af_trigger_for_continuous;
        }

        /* access modifiers changed from: private */
        public void setFocusMode(Builder builder) {
            if (this.has_af_mode) {
                builder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(this.af_mode));
            }
        }

        /* access modifiers changed from: private */
        public void setFocusDistance(Builder builder) {
            builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, Float.valueOf(this.focus_distance));
        }

        /* access modifiers changed from: private */
        public void setAutoExposureLock(Builder builder) {
            builder.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.valueOf(this.ae_lock));
        }

        /* access modifiers changed from: private */
        public void setAutoWhiteBalanceLock(Builder builder) {
            builder.set(CaptureRequest.CONTROL_AWB_LOCK, Boolean.valueOf(this.wb_lock));
        }

        /* access modifiers changed from: private */
        public void setAFRegions(Builder builder) {
            if (this.af_regions != null && ((Integer) CameraController2.this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)).intValue() > 0) {
                builder.set(CaptureRequest.CONTROL_AF_REGIONS, this.af_regions);
            }
        }

        /* access modifiers changed from: private */
        public void setAERegions(Builder builder) {
            if (this.ae_regions != null && ((Integer) CameraController2.this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)).intValue() > 0) {
                builder.set(CaptureRequest.CONTROL_AE_REGIONS, this.ae_regions);
            }
        }

        /* access modifiers changed from: private */
        public void setFaceDetectMode(Builder builder) {
            if (this.has_face_detect_mode) {
                builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, Integer.valueOf(this.face_detect_mode));
            } else {
                builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, Integer.valueOf(0));
            }
        }

        private void setRawMode(Builder builder) {
            if (CameraController2.this.want_raw && !CameraController2.this.previewIsVideoMode) {
                builder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE, Integer.valueOf(1));
            }
        }

        /* access modifiers changed from: private */
        public void setVideoStabilization(Builder builder) {
            builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, Integer.valueOf(this.video_stabilization ? 1 : 0));
        }

        private float getLogProfile(float f) {
            float f2 = this.log_profile_strength;
            return (float) Math.pow((double) ((float) (Math.log1p((double) (f * f2)) / Math.log1p((double) f2))), 0.45454543828964233d);
        }

        /* access modifiers changed from: private */
        public void setLogProfile(Builder builder) {
            if (this.use_log_profile && this.log_profile_strength > 0.0f) {
                if (this.default_tonemap_mode == null) {
                    this.default_tonemap_mode = (Integer) builder.get(CaptureRequest.TONEMAP_MODE);
                }
                float[] fArr = new float[128];
                int i = 0;
                int i2 = 1;
                for (int i3 = 0; i3 < 232; i3 += i2) {
                    float f = ((float) i3) / 255.0f;
                    float logProfile = getLogProfile(f);
                    int i4 = i + 1;
                    fArr[i] = f;
                    i = i4 + 1;
                    fArr[i4] = logProfile;
                    if ((i / 2) % 16 == 0) {
                        i2 *= 2;
                    }
                }
                int i5 = i + 1;
                fArr[i] = 1.0f;
                fArr[i5] = getLogProfile(1.0f);
                builder.set(CaptureRequest.TONEMAP_MODE, Integer.valueOf(0));
                builder.set(CaptureRequest.TONEMAP_CURVE, new TonemapCurve(fArr, fArr, fArr));
                CameraController2.this.test_used_tonemap_curve = CameraController2.do_af_trigger_for_continuous;
            } else if (this.default_tonemap_mode != null) {
                builder.set(CaptureRequest.TONEMAP_MODE, this.default_tonemap_mode);
            }
        }
    }

    private class OnImageAvailableListener implements android.media.ImageReader.OnImageAvailableListener {
        private OnImageAvailableListener() {
        }

        public void onImageAvailable(ImageReader imageReader) {
            boolean z;
            if (CameraController2.this.picture_cb == null || !CameraController2.this.jpeg_todo) {
                Log.e(CameraController2.TAG, "no picture callback available");
                imageReader.acquireNextImage().close();
                return;
            }
            ArrayList arrayList = null;
            Image acquireNextImage = imageReader.acquireNextImage();
            boolean z2 = false;
            ByteBuffer buffer = acquireNextImage.getPlanes()[0].getBuffer();
            byte[] bArr = new byte[buffer.remaining()];
            buffer.get(bArr);
            acquireNextImage.close();
            synchronized (CameraController2.this.background_camera_lock) {
                CameraController2.this.n_burst_taken = CameraController2.this.n_burst_taken + 1;
                if (CameraController2.this.burst_single_request) {
                    CameraController2.this.pending_burst_images.add(bArr);
                    if (CameraController2.this.pending_burst_images.size() >= CameraController2.this.n_burst) {
                        if (CameraController2.this.pending_burst_images.size() > CameraController2.this.n_burst) {
                            String str = CameraController2.TAG;
                            StringBuilder sb = new StringBuilder();
                            sb.append("pending_burst_images size ");
                            sb.append(CameraController2.this.pending_burst_images.size());
                            sb.append(" is greater than n_burst ");
                            sb.append(CameraController2.this.n_burst);
                            Log.e(str, sb.toString());
                        }
                        arrayList = new ArrayList(CameraController2.this.pending_burst_images);
                    } else {
                        z = CameraController2.do_af_trigger_for_continuous;
                    }
                }
                z = false;
            }
            if (arrayList != null) {
                CameraController2.this.picture_cb.onBurstPictureTaken(arrayList);
            } else if (!CameraController2.this.burst_single_request) {
                CameraController2.this.picture_cb.onPictureTaken(bArr);
            }
            synchronized (CameraController2.this.background_camera_lock) {
                if (arrayList != null) {
                    CameraController2.this.pending_burst_images.clear();
                } else if (!CameraController2.this.burst_single_request) {
                    CameraController2.this.n_burst = CameraController2.this.n_burst - 1;
                    if (CameraController2.this.burst_type != BurstType.BURSTTYPE_CONTINUOUS || CameraController2.this.continuous_burst_requested_last_capture) {
                        if (CameraController2.this.n_burst == 0) {
                        }
                    }
                    z = CameraController2.do_af_trigger_for_continuous;
                }
                z2 = CameraController2.do_af_trigger_for_continuous;
            }
            if (z) {
                takePhotoPartial();
            } else if (z2) {
                takePhotoCompleted();
            }
        }

        private void takePhotoPartial() {
            ErrorCallback errorCallback;
            synchronized (CameraController2.this.background_camera_lock) {
                errorCallback = null;
                if (CameraController2.this.slow_burst_capture_requests != null) {
                    if (CameraController2.this.burst_type != BurstType.BURSTTYPE_FOCUS) {
                        try {
                            if (!(CameraController2.this.camera == null || CameraController2.this.captureSession == null)) {
                                CameraController2.this.captureSession.capture((CaptureRequest) CameraController2.this.slow_burst_capture_requests.get(CameraController2.this.n_burst_taken), CameraController2.this.previewCaptureCallback, CameraController2.this.handler);
                            }
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                            CameraController2.this.jpeg_todo = false;
                            CameraController2.this.raw_todo = false;
                            CameraController2.this.picture_cb = null;
                            errorCallback = CameraController2.this.take_picture_error_cb;
                        }
                    } else if (CameraController2.this.previewBuilder != null) {
                        if (!CameraController2.this.focus_bracketing_in_progress) {
                            CameraController2.this.slow_burst_capture_requests.subList(CameraController2.this.n_burst_taken + 1, CameraController2.this.slow_burst_capture_requests.size()).clear();
                            if (CameraController2.this.burst_single_request) {
                                CameraController2.this.n_burst = CameraController2.this.slow_burst_capture_requests.size();
                                if (CameraController2.this.n_burst_raw > 0) {
                                    CameraController2.this.n_burst_raw = CameraController2.this.slow_burst_capture_requests.size();
                                }
                            } else {
                                CameraController2.this.n_burst = 1;
                                if (CameraController2.this.n_burst_raw > 0) {
                                    CameraController2.this.n_burst_raw = 1;
                                }
                            }
                            ((RequestTagObject) ((CaptureRequest) CameraController2.this.slow_burst_capture_requests.get(CameraController2.this.slow_burst_capture_requests.size() - 1)).getTag()).setType(RequestTagType.CAPTURE);
                        }
                        try {
                            float floatValue = ((Float) ((CaptureRequest) CameraController2.this.slow_burst_capture_requests.get(CameraController2.this.n_burst_taken)).get(CaptureRequest.LENS_FOCUS_DISTANCE)).floatValue();
                            CameraController2.this.previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(0));
                            CameraController2.this.previewBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, Float.valueOf(floatValue));
                            CameraController2.this.setRepeatingRequest(CameraController2.this.previewBuilder.build());
                        } catch (CameraAccessException e2) {
                            e2.printStackTrace();
                            CameraController2.this.jpeg_todo = false;
                            CameraController2.this.raw_todo = false;
                            CameraController2.this.picture_cb = null;
                            errorCallback = CameraController2.this.take_picture_error_cb;
                        }
                        CameraController2.this.handler.postDelayed(new Runnable() {
                            public void run() {
                                if (CameraController2.this.camera != null && CameraController2.this.captureSession != null) {
                                    if (CameraController2.this.picture_cb.imageQueueWouldBlock(CameraController2.this.imageReaderRaw != null ? 1 : 0, 1)) {
                                        CameraController2.this.handler.postDelayed(this, 100);
                                        return;
                                    }
                                    CameraController2.this.playSound(0);
                                    try {
                                        CameraController2.this.captureSession.capture((CaptureRequest) CameraController2.this.slow_burst_capture_requests.get(CameraController2.this.n_burst_taken), CameraController2.this.previewCaptureCallback, CameraController2.this.handler);
                                    } catch (CameraAccessException e) {
                                        e.printStackTrace();
                                        CameraController2.this.jpeg_todo = false;
                                        CameraController2.this.raw_todo = false;
                                        CameraController2.this.picture_cb = null;
                                        if (CameraController2.this.take_picture_error_cb != null) {
                                            CameraController2.this.take_picture_error_cb.onError();
                                            CameraController2.this.take_picture_error_cb = null;
                                        }
                                    }
                                }
                            }
                        }, 500);
                    }
                }
            }
            if (errorCallback != null) {
                errorCallback.onError();
            }
        }

        private void takePhotoCompleted() {
            synchronized (CameraController2.this.background_camera_lock) {
                CameraController2.this.jpeg_todo = false;
            }
            CameraController2.this.checkImagesCompleted();
        }
    }

    private class OnRawImageAvailableListener implements android.media.ImageReader.OnImageAvailableListener {
        private final Queue<CaptureResult> capture_results;
        private final Queue<Image> images;

        private OnRawImageAvailableListener() {
            this.capture_results = new LinkedList();
            this.images = new LinkedList();
        }

        /* access modifiers changed from: 0000 */
        public void setCaptureResult(CaptureResult captureResult) {
            synchronized (CameraController2.this.background_camera_lock) {
                this.capture_results.add(captureResult);
                if (this.images.size() > 0) {
                    ((Activity) CameraController2.this.context).runOnUiThread(new Runnable() {
                        public void run() {
                            OnRawImageAvailableListener.this.processImage();
                        }
                    });
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void clear() {
            synchronized (CameraController2.this.background_camera_lock) {
                this.capture_results.clear();
                this.images.clear();
            }
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x00f6, code lost:
            if (net.sourceforge.opencamera.cameracontroller.CameraController2.access$3500(r7.this$0) == null) goto L_0x00ff;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x00f8, code lost:
            net.sourceforge.opencamera.cameracontroller.CameraController2.access$2800(r7.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x00ff, code lost:
            if (r1 == null) goto L_0x010b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0101, code lost:
            net.sourceforge.opencamera.cameracontroller.CameraController2.access$600(r7.this$0).onRawBurstPictureTaken(r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x0111, code lost:
            if (net.sourceforge.opencamera.cameracontroller.CameraController2.access$1000(r7.this$0) != false) goto L_0x0121;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x0113, code lost:
            net.sourceforge.opencamera.cameracontroller.CameraController2.access$600(r7.this$0).onRawPictureTaken(new net.sourceforge.opencamera.cameracontroller.RawImage(r3, r2));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x0121, code lost:
            r2 = net.sourceforge.opencamera.cameracontroller.CameraController2.access$800(r7.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x0127, code lost:
            monitor-enter(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x0129, code lost:
            if (r1 == null) goto L_0x0135;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
            net.sourceforge.opencamera.cameracontroller.CameraController2.access$3600(r7.this$0).clear();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x013b, code lost:
            if (net.sourceforge.opencamera.cameracontroller.CameraController2.access$1000(r7.this$0) != false) goto L_0x015e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:0x013d, code lost:
            net.sourceforge.opencamera.cameracontroller.CameraController2.access$2310(r7.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x014a, code lost:
            if (net.sourceforge.opencamera.cameracontroller.CameraController2.access$200(r7.this$0) != net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_CONTINUOUS) goto L_0x0155;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:48:0x0152, code lost:
            if (net.sourceforge.opencamera.cameracontroller.CameraController2.access$1300(r7.this$0) != false) goto L_0x0155;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:51:0x015b, code lost:
            if (net.sourceforge.opencamera.cameracontroller.CameraController2.access$2300(r7.this$0) != 0) goto L_0x015e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:53:0x015e, code lost:
            r4 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:54:0x015f, code lost:
            monitor-exit(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:55:0x0160, code lost:
            if (r4 == false) goto L_0x0178;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:56:0x0162, code lost:
            r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.access$800(r7.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:0x0168, code lost:
            monitor-enter(r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
            net.sourceforge.opencamera.cameracontroller.CameraController2.access$1902(r7.this$0, false);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:60:0x016e, code lost:
            monitor-exit(r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:61:0x016f, code lost:
            net.sourceforge.opencamera.cameracontroller.CameraController2.access$2800(r7.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:66:0x0178, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void processImage() {
            /*
                r7 = this;
                net.sourceforge.opencamera.cameracontroller.CameraController2 r0 = net.sourceforge.opencamera.cameracontroller.CameraController2.this
                java.lang.Object r0 = r0.background_camera_lock
                monitor-enter(r0)
                java.util.Queue<android.hardware.camera2.CaptureResult> r1 = r7.capture_results     // Catch:{ all -> 0x017c }
                int r1 = r1.size()     // Catch:{ all -> 0x017c }
                if (r1 != 0) goto L_0x0011
                monitor-exit(r0)     // Catch:{ all -> 0x017c }
                return
            L_0x0011:
                java.util.Queue<android.media.Image> r1 = r7.images     // Catch:{ all -> 0x017c }
                int r1 = r1.size()     // Catch:{ all -> 0x017c }
                if (r1 != 0) goto L_0x001b
                monitor-exit(r0)     // Catch:{ all -> 0x017c }
                return
            L_0x001b:
                java.util.Queue<android.hardware.camera2.CaptureResult> r1 = r7.capture_results     // Catch:{ all -> 0x017c }
                java.lang.Object r1 = r1.remove()     // Catch:{ all -> 0x017c }
                android.hardware.camera2.CaptureResult r1 = (android.hardware.camera2.CaptureResult) r1     // Catch:{ all -> 0x017c }
                java.util.Queue<android.media.Image> r2 = r7.images     // Catch:{ all -> 0x017c }
                java.lang.Object r2 = r2.remove()     // Catch:{ all -> 0x017c }
                android.media.Image r2 = (android.media.Image) r2     // Catch:{ all -> 0x017c }
                android.hardware.camera2.DngCreator r3 = new android.hardware.camera2.DngCreator     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController2 r4 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                android.hardware.camera2.CameraCharacteristics r4 = r4.characteristics     // Catch:{ all -> 0x017c }
                r3.<init>(r4, r1)     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r1 = r1.camera_settings     // Catch:{ all -> 0x017c }
                int r1 = r1.getExifOrientation()     // Catch:{ all -> 0x017c }
                r3.setOrientation(r1)     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r1 = r1.camera_settings     // Catch:{ all -> 0x017c }
                android.location.Location r1 = r1.location     // Catch:{ all -> 0x017c }
                if (r1 == 0) goto L_0x005c
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r1 = r1.camera_settings     // Catch:{ all -> 0x017c }
                android.location.Location r1 = r1.location     // Catch:{ all -> 0x017c }
                r3.setLocation(r1)     // Catch:{ all -> 0x017c }
            L_0x005c:
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                int r1 = r1.n_burst_total     // Catch:{ all -> 0x017c }
                r4 = 1
                if (r1 != r4) goto L_0x007a
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r1 = r1.burst_type     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r5 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_CONTINUOUS     // Catch:{ all -> 0x017c }
                if (r1 == r5) goto L_0x007a
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.RawImage r5 = new net.sourceforge.opencamera.cameracontroller.RawImage     // Catch:{ all -> 0x017c }
                r5.<init>(r3, r2)     // Catch:{ all -> 0x017c }
                r1.pending_raw_image = r5     // Catch:{ all -> 0x017c }
                goto L_0x00ee
            L_0x007a:
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                boolean r1 = r1.burst_single_request     // Catch:{ all -> 0x017c }
                if (r1 == 0) goto L_0x00ee
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                java.util.List r1 = r1.pending_burst_images_raw     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.RawImage r5 = new net.sourceforge.opencamera.cameracontroller.RawImage     // Catch:{ all -> 0x017c }
                r5.<init>(r3, r2)     // Catch:{ all -> 0x017c }
                r1.add(r5)     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                java.util.List r1 = r1.pending_burst_images_raw     // Catch:{ all -> 0x017c }
                int r1 = r1.size()     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController2 r5 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                int r5 = r5.n_burst_raw     // Catch:{ all -> 0x017c }
                if (r1 < r5) goto L_0x00ee
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                java.util.List r1 = r1.pending_burst_images_raw     // Catch:{ all -> 0x017c }
                int r1 = r1.size()     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController2 r5 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                int r5 = r5.n_burst_raw     // Catch:{ all -> 0x017c }
                if (r1 <= r5) goto L_0x00e2
                java.lang.String r1 = "CameraController2"
                java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x017c }
                r5.<init>()     // Catch:{ all -> 0x017c }
                java.lang.String r6 = "pending_burst_images_raw size "
                r5.append(r6)     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController2 r6 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                java.util.List r6 = r6.pending_burst_images_raw     // Catch:{ all -> 0x017c }
                int r6 = r6.size()     // Catch:{ all -> 0x017c }
                r5.append(r6)     // Catch:{ all -> 0x017c }
                java.lang.String r6 = " is greater than n_burst_raw "
                r5.append(r6)     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController2 r6 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                int r6 = r6.n_burst_raw     // Catch:{ all -> 0x017c }
                r5.append(r6)     // Catch:{ all -> 0x017c }
                java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x017c }
                android.util.Log.e(r1, r5)     // Catch:{ all -> 0x017c }
            L_0x00e2:
                java.util.ArrayList r1 = new java.util.ArrayList     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController2 r5 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x017c }
                java.util.List r5 = r5.pending_burst_images_raw     // Catch:{ all -> 0x017c }
                r1.<init>(r5)     // Catch:{ all -> 0x017c }
                goto L_0x00ef
            L_0x00ee:
                r1 = 0
            L_0x00ef:
                monitor-exit(r0)     // Catch:{ all -> 0x017c }
                net.sourceforge.opencamera.cameracontroller.CameraController2 r0 = net.sourceforge.opencamera.cameracontroller.CameraController2.this
                net.sourceforge.opencamera.cameracontroller.RawImage r0 = r0.pending_raw_image
                if (r0 == 0) goto L_0x00ff
                net.sourceforge.opencamera.cameracontroller.CameraController2 r0 = net.sourceforge.opencamera.cameracontroller.CameraController2.this
                r0.checkImagesCompleted()
                goto L_0x0178
            L_0x00ff:
                if (r1 == 0) goto L_0x010b
                net.sourceforge.opencamera.cameracontroller.CameraController2 r0 = net.sourceforge.opencamera.cameracontroller.CameraController2.this
                net.sourceforge.opencamera.cameracontroller.CameraController$PictureCallback r0 = r0.picture_cb
                r0.onRawBurstPictureTaken(r1)
                goto L_0x0121
            L_0x010b:
                net.sourceforge.opencamera.cameracontroller.CameraController2 r0 = net.sourceforge.opencamera.cameracontroller.CameraController2.this
                boolean r0 = r0.burst_single_request
                if (r0 != 0) goto L_0x0121
                net.sourceforge.opencamera.cameracontroller.CameraController2 r0 = net.sourceforge.opencamera.cameracontroller.CameraController2.this
                net.sourceforge.opencamera.cameracontroller.CameraController$PictureCallback r0 = r0.picture_cb
                net.sourceforge.opencamera.cameracontroller.RawImage r5 = new net.sourceforge.opencamera.cameracontroller.RawImage
                r5.<init>(r3, r2)
                r0.onRawPictureTaken(r5)
            L_0x0121:
                net.sourceforge.opencamera.cameracontroller.CameraController2 r0 = net.sourceforge.opencamera.cameracontroller.CameraController2.this
                java.lang.Object r2 = r0.background_camera_lock
                monitor-enter(r2)
                r0 = 0
                if (r1 == 0) goto L_0x0135
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x0179 }
                java.util.List r1 = r1.pending_burst_images_raw     // Catch:{ all -> 0x0179 }
                r1.clear()     // Catch:{ all -> 0x0179 }
                goto L_0x015f
            L_0x0135:
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x0179 }
                boolean r1 = r1.burst_single_request     // Catch:{ all -> 0x0179 }
                if (r1 != 0) goto L_0x015e
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x0179 }
                r1.n_burst_raw = r1.n_burst_raw - 1     // Catch:{ all -> 0x0179 }
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x0179 }
                net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r1 = r1.burst_type     // Catch:{ all -> 0x0179 }
                net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r3 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_CONTINUOUS     // Catch:{ all -> 0x0179 }
                if (r1 != r3) goto L_0x0155
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x0179 }
                boolean r1 = r1.continuous_burst_requested_last_capture     // Catch:{ all -> 0x0179 }
                if (r1 != 0) goto L_0x0155
                goto L_0x015e
            L_0x0155:
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x0179 }
                int r1 = r1.n_burst_raw     // Catch:{ all -> 0x0179 }
                if (r1 != 0) goto L_0x015e
                goto L_0x015f
            L_0x015e:
                r4 = 0
            L_0x015f:
                monitor-exit(r2)     // Catch:{ all -> 0x0179 }
                if (r4 == 0) goto L_0x0178
                net.sourceforge.opencamera.cameracontroller.CameraController2 r1 = net.sourceforge.opencamera.cameracontroller.CameraController2.this
                java.lang.Object r1 = r1.background_camera_lock
                monitor-enter(r1)
                net.sourceforge.opencamera.cameracontroller.CameraController2 r2 = net.sourceforge.opencamera.cameracontroller.CameraController2.this     // Catch:{ all -> 0x0175 }
                r2.raw_todo = r0     // Catch:{ all -> 0x0175 }
                monitor-exit(r1)     // Catch:{ all -> 0x0175 }
                net.sourceforge.opencamera.cameracontroller.CameraController2 r0 = net.sourceforge.opencamera.cameracontroller.CameraController2.this
                r0.checkImagesCompleted()
                goto L_0x0178
            L_0x0175:
                r0 = move-exception
                monitor-exit(r1)     // Catch:{ all -> 0x0175 }
                throw r0
            L_0x0178:
                return
            L_0x0179:
                r0 = move-exception
                monitor-exit(r2)     // Catch:{ all -> 0x0179 }
                throw r0
            L_0x017c:
                r1 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x017c }
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.OnRawImageAvailableListener.processImage():void");
        }

        public void onImageAvailable(ImageReader imageReader) {
            if (CameraController2.this.picture_cb == null || !CameraController2.this.raw_todo) {
                Log.e(CameraController2.TAG, "no picture callback available");
                imageReader.acquireNextImage().close();
                return;
            }
            synchronized (CameraController2.this.background_camera_lock) {
                this.images.add(imageReader.acquireNextImage());
            }
            processImage();
        }
    }

    private static class RequestTagObject {
        private RequestTagType type;

        private RequestTagObject(RequestTagType requestTagType) {
            this.type = requestTagType;
        }

        /* access modifiers changed from: private */
        public RequestTagType getType() {
            return this.type;
        }

        /* access modifiers changed from: private */
        public void setType(RequestTagType requestTagType) {
            this.type = requestTagType;
        }
    }

    private enum RequestTagType {
        CAPTURE,
        CAPTURE_BURST_IN_PROGRESS,
        NONE
    }

    private String convertAntiBanding(int i) {
        if (i == 0) {
            return "off";
        }
        if (i == 1) {
            return "50hz";
        }
        if (i == 2) {
            return "60hz";
        }
        if (i != 3) {
            return null;
        }
        return "auto";
    }

    private String convertColorEffect(int i) {
        switch (i) {
            case 0:
                return CameraController.COLOR_EFFECT_DEFAULT;
            case 1:
                return "mono";
            case 2:
                return "negative";
            case 3:
                return "solarize";
            case 4:
                return "sepia";
            case 5:
                return "posterize";
            case 6:
                return "whiteboard";
            case 7:
                return "blackboard";
            case 8:
                return "aqua";
            default:
                return null;
        }
    }

    private String convertEdgeMode(int i) {
        if (i == 0) {
            return "off";
        }
        if (i == 1) {
            return "fast";
        }
        if (i != 2) {
            return null;
        }
        return "high_quality";
    }

    private String convertFocusModeToValue(int i) {
        return i != 0 ? i != 1 ? i != 2 ? i != 3 ? i != 4 ? i != 5 ? BuildConfig.FLAVOR : "focus_mode_edof" : "focus_mode_continuous_picture" : "focus_mode_continuous_video" : "focus_mode_macro" : "focus_mode_auto" : "focus_mode_manual2";
    }

    private String convertNoiseReductionMode(int i) {
        if (i == 0) {
            return "off";
        }
        if (i == 1) {
            return "fast";
        }
        if (i == 2) {
            return "high_quality";
        }
        if (i != 3) {
            return null;
        }
        return "minimal";
    }

    private String convertSceneMode(int i) {
        if (i == 0) {
            return "auto";
        }
        switch (i) {
            case 2:
                return "action";
            case 3:
                return "portrait";
            case 4:
                return "landscape";
            case 5:
                return "night";
            case 6:
                return "night-portrait";
            case 7:
                return "theatre";
            case 8:
                return "beach";
            case 9:
                return "snow";
            case 10:
                return "sunset";
            case 11:
                return "steadyphoto";
            case 12:
                return "fireworks";
            case 13:
                return "sports";
            case 14:
                return "party";
            case 15:
                return "candlelight";
            case 16:
                return "barcode";
            default:
                return null;
        }
    }

    private String convertWhiteBalance(int i) {
        switch (i) {
            case 0:
                return "manual";
            case 1:
                return "auto";
            case 2:
                return "incandescent";
            case 3:
                return "fluorescent";
            case 4:
                return "warm-fluorescent";
            case 5:
                return "daylight";
            case 6:
                return "cloudy-daylight";
            case 7:
                return "twilight";
            case 8:
                return "shade";
            default:
                return null;
        }
    }

    public String getAPI() {
        return "Camera2 (Android L)";
    }

    public String getISOKey() {
        return BuildConfig.FLAVOR;
    }

    public String getParametersString() {
        return null;
    }

    public boolean sceneModeAffectsFunctionality() {
        return false;
    }

    public void setRecordingHint(boolean z) {
    }

    public void unlock() {
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x004e, code lost:
        if (r0 > 255.0f) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x006c, code lost:
        if (r0 > 255.0f) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x009c, code lost:
        if (r2 > 255.0f) goto L_0x0074;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x002d, code lost:
        if (r4 > 255.0f) goto L_0x000f;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.hardware.camera2.params.RggbChannelVector convertTemperatureToRggb(int r12) {
        /*
            r11 = this;
            float r12 = (float) r12
            r0 = 1120403456(0x42c80000, float:100.0)
            float r12 = r12 / r0
            r0 = 1114636288(0x42700000, float:60.0)
            r1 = 1115947008(0x42840000, float:66.0)
            r2 = 0
            r3 = 1132396544(0x437f0000, float:255.0)
            int r4 = (r12 > r1 ? 1 : (r12 == r1 ? 0 : -1))
            if (r4 > 0) goto L_0x0012
        L_0x000f:
            r4 = 1132396544(0x437f0000, float:255.0)
            goto L_0x0030
        L_0x0012:
            float r4 = r12 - r0
            r5 = 4644507737543448116(0x40749b2dfcd49634, double:329.698727446)
            double r7 = (double) r4
            r9 = -4629404809333063611(0xbfc10cda8237c045, double:-0.1332047592)
            double r7 = java.lang.Math.pow(r7, r9)
            double r7 = r7 * r5
            float r4 = (float) r7
            int r5 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1))
            if (r5 >= 0) goto L_0x002b
            r4 = 0
        L_0x002b:
            int r5 = (r4 > r3 ? 1 : (r4 == r3 ? 0 : -1))
            if (r5 <= 0) goto L_0x0030
            goto L_0x000f
        L_0x0030:
            int r5 = (r12 > r1 ? 1 : (r12 == r1 ? 0 : -1))
            if (r5 > 0) goto L_0x0051
            r5 = 4636700052397198078(0x4058de21a12b8afe, double:99.4708025861)
            double r7 = (double) r12
            double r7 = java.lang.Math.log(r7)
            double r7 = r7 * r5
            r5 = 4639872907401388378(0x406423d3809e615a, double:161.1195681661)
            double r7 = r7 - r5
            float r0 = (float) r7
            int r5 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r5 >= 0) goto L_0x004c
            r0 = 0
        L_0x004c:
            int r5 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r5 <= 0) goto L_0x0070
            goto L_0x006e
        L_0x0051:
            float r0 = r12 - r0
            r5 = 4643776315001473500(0x407201f4680909dc, double:288.1221695283)
            double r7 = (double) r0
            r9 = -4633266197844121933(0xbfb354f0efad26b3, double:-0.0755148492)
            double r7 = java.lang.Math.pow(r7, r9)
            double r7 = r7 * r5
            float r0 = (float) r7
            int r5 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r5 >= 0) goto L_0x006a
            r0 = 0
        L_0x006a:
            int r5 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r5 <= 0) goto L_0x0070
        L_0x006e:
            r0 = 1132396544(0x437f0000, float:255.0)
        L_0x0070:
            int r1 = (r12 > r1 ? 1 : (r12 == r1 ? 0 : -1))
            if (r1 < 0) goto L_0x0077
        L_0x0074:
            r2 = 1132396544(0x437f0000, float:255.0)
            goto L_0x009f
        L_0x0077:
            r1 = 1100480512(0x41980000, float:19.0)
            int r1 = (r12 > r1 ? 1 : (r12 == r1 ? 0 : -1))
            if (r1 > 0) goto L_0x007e
            goto L_0x009f
        L_0x007e:
            r1 = 1092616192(0x41200000, float:10.0)
            float r12 = r12 - r1
            r5 = 4639077675960494756(0x406150914111eaa4, double:138.5177312231)
            double r7 = (double) r12
            double r7 = java.lang.Math.log(r7)
            double r7 = r7 * r5
            r5 = 4644074020937209672(0x407310b778951748, double:305.0447927307)
            double r7 = r7 - r5
            float r12 = (float) r7
            int r1 = (r12 > r2 ? 1 : (r12 == r2 ? 0 : -1))
            if (r1 >= 0) goto L_0x0099
            goto L_0x009a
        L_0x0099:
            r2 = r12
        L_0x009a:
            int r12 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r12 <= 0) goto L_0x009f
            goto L_0x0074
        L_0x009f:
            android.hardware.camera2.params.RggbChannelVector r12 = new android.hardware.camera2.params.RggbChannelVector
            float r4 = r4 / r3
            r1 = 1073741824(0x40000000, float:2.0)
            float r4 = r4 * r1
            float r0 = r0 / r3
            float r2 = r2 / r3
            float r2 = r2 * r1
            r12.<init>(r4, r0, r0, r2)
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.convertTemperatureToRggb(int):android.hardware.camera2.params.RggbChannelVector");
    }

    private int convertRggbToTemperature(RggbChannelVector rggbChannelVector) {
        int i;
        float red = rggbChannelVector.getRed();
        float greenEven = rggbChannelVector.getGreenEven();
        float greenOdd = rggbChannelVector.getGreenOdd();
        float blue = rggbChannelVector.getBlue();
        float f = (greenEven + greenOdd) * 0.5f;
        float max = Math.max(red, blue);
        if (f > max) {
            f = max;
        }
        float f2 = 255.0f / max;
        int i2 = (int) (red * f2);
        int i3 = (int) (f * f2);
        int i4 = (int) (blue * f2);
        if (i2 == i4) {
            i = 6600;
        } else if (i2 > i4) {
            double d = (double) i3;
            Double.isNaN(d);
            int exp = (int) (Math.exp((d + 161.1195681661d) / 99.4708025861d) * 100.0d);
            if (i4 != 0) {
                double d2 = (double) i4;
                Double.isNaN(d2);
                exp = (exp + ((int) ((Math.exp((d2 + 305.0447927307d) / 138.5177312231d) + 10.0d) * 100.0d))) / 2;
            }
            i = exp;
        } else if (i2 <= 1 || i3 <= 1) {
            i = max_white_balance_temperature_c;
        } else {
            double d3 = (double) i2;
            Double.isNaN(d3);
            int pow = (int) ((Math.pow(d3 / 329.698727446d, -7.507239275877164d) + 60.0d) * 100.0d);
            double d4 = (double) i3;
            Double.isNaN(d4);
            i = (pow + ((int) ((Math.pow(d4 / 288.1221695283d, -13.24242861627803d) + 60.0d) * 100.0d))) / 2;
        }
        return Math.min(Math.max(i, 1000), max_white_balance_temperature_c);
    }

    public void onError() {
        Log.e(TAG, "onError");
        CameraDevice cameraDevice = this.camera;
        if (cameraDevice != null) {
            onError(cameraDevice);
        }
    }

    /* access modifiers changed from: private */
    public void onError(CameraDevice cameraDevice) {
        String str = TAG;
        Log.e(str, "onError");
        boolean z = this.camera != null ? do_af_trigger_for_continuous : false;
        this.camera = null;
        cameraDevice.close();
        if (z) {
            Log.e(str, "error occurred after camera was opened");
            this.camera_error_cb.onError();
        }
    }

    public CameraController2(Context context2, int i, ErrorCallback errorCallback, ErrorCallback errorCallback2) throws CameraControllerException {
        super(i);
        this.context = context2;
        this.preview_error_cb = errorCallback;
        this.camera_error_cb = errorCallback2;
        this.is_samsung_s7 = Build.MODEL.toLowerCase(Locale.US).contains("sm-g93");
        this.thread = new HandlerThread("CameraBackground");
        this.thread.start();
        this.handler = new Handler(this.thread.getLooper());
        final CameraManager cameraManager = (CameraManager) context2.getSystemService("camera");
        final AnonymousClass1MyStateCallback r8 = new StateCallback() {
            boolean callback_done;
            boolean first_callback = CameraController2.do_af_trigger_for_continuous;

            public void onOpened(CameraDevice cameraDevice) {
                if (this.first_callback) {
                    boolean z = false;
                    this.first_callback = false;
                    try {
                        CameraController2.this.characteristics = cameraManager.getCameraCharacteristics(CameraController2.this.cameraIdS);
                        CameraController2.this.characteristics_sensor_orientation = ((Integer) CameraController2.this.characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
                        CameraController2 cameraController2 = CameraController2.this;
                        if (((Integer) CameraController2.this.characteristics.get(CameraCharacteristics.LENS_FACING)).intValue() == 0) {
                            z = CameraController2.do_af_trigger_for_continuous;
                        }
                        cameraController2.characteristics_is_front_facing = z;
                        CameraController2.this.camera = cameraDevice;
                        CameraController2.this.createPreviewRequest();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    synchronized (CameraController2.this.open_camera_lock) {
                        this.callback_done = CameraController2.do_af_trigger_for_continuous;
                        CameraController2.this.open_camera_lock.notifyAll();
                    }
                }
            }

            public void onClosed(CameraDevice cameraDevice) {
                if (this.first_callback) {
                    this.first_callback = false;
                }
            }

            public void onDisconnected(CameraDevice cameraDevice) {
                if (this.first_callback) {
                    this.first_callback = false;
                    CameraController2.this.camera = null;
                    cameraDevice.close();
                    synchronized (CameraController2.this.open_camera_lock) {
                        this.callback_done = CameraController2.do_af_trigger_for_continuous;
                        CameraController2.this.open_camera_lock.notifyAll();
                    }
                }
            }

            public void onError(CameraDevice cameraDevice, int i) {
                StringBuilder sb = new StringBuilder();
                sb.append("camera error: ");
                sb.append(i);
                Log.e(CameraController2.TAG, sb.toString());
                if (this.first_callback) {
                    this.first_callback = false;
                }
                CameraController2.this.onError(cameraDevice);
                synchronized (CameraController2.this.open_camera_lock) {
                    this.callback_done = CameraController2.do_af_trigger_for_continuous;
                    CameraController2.this.open_camera_lock.notifyAll();
                }
            }
        };
        try {
            this.cameraIdS = cameraManager.getCameraIdList()[i];
            cameraManager.openCamera(this.cameraIdS, r8, this.handler);
            this.handler.postDelayed(new Runnable() {
                public void run() {
                    synchronized (CameraController2.this.open_camera_lock) {
                        if (!r8.callback_done) {
                            Log.e(CameraController2.TAG, "timeout waiting for camera callback");
                            r8.first_callback = CameraController2.do_af_trigger_for_continuous;
                            r8.callback_done = CameraController2.do_af_trigger_for_continuous;
                            CameraController2.this.open_camera_lock.notifyAll();
                        }
                    }
                }
            }, 10000);
            synchronized (this.open_camera_lock) {
                while (!r8.callback_done) {
                    try {
                        this.open_camera_lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (this.camera != null) {
                this.media_action_sound.load(2);
                this.media_action_sound.load(3);
                this.media_action_sound.load(0);
                return;
            }
            Log.e(TAG, "camera failed to open");
            throw new CameraControllerException();
        } catch (CameraAccessException e2) {
            e2.printStackTrace();
            throw new CameraControllerException();
        } catch (UnsupportedOperationException e3) {
            e3.printStackTrace();
            throw new CameraControllerException();
        } catch (SecurityException e4) {
            e4.printStackTrace();
            throw new CameraControllerException();
        } catch (IllegalArgumentException e5) {
            e5.printStackTrace();
            throw new CameraControllerException();
        } catch (ArrayIndexOutOfBoundsException e6) {
            e6.printStackTrace();
            throw new CameraControllerException();
        }
    }

    public void release() {
        synchronized (this.background_camera_lock) {
            if (this.captureSession != null) {
                this.captureSession.close();
                this.captureSession = null;
            }
        }
        this.previewBuilder = null;
        this.previewIsVideoMode = false;
        CameraDevice cameraDevice = this.camera;
        if (cameraDevice != null) {
            cameraDevice.close();
            this.camera = null;
        }
        closePictureImageReader();
        HandlerThread handlerThread = this.thread;
        if (handlerThread != null) {
            handlerThread.quitSafely();
            try {
                this.thread.join();
                this.thread = null;
                this.handler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void closePictureImageReader() {
        ImageReader imageReader2 = this.imageReader;
        if (imageReader2 != null) {
            imageReader2.close();
            this.imageReader = null;
        }
        ImageReader imageReader3 = this.imageReaderRaw;
        if (imageReader3 != null) {
            imageReader3.close();
            this.imageReaderRaw = null;
            this.onRawImageAvailableListener = null;
        }
    }

    private List<String> convertFocusModesToValues(int[] iArr, float f) {
        if (iArr.length == 0) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (int valueOf : iArr) {
            arrayList.add(Integer.valueOf(valueOf));
        }
        ArrayList arrayList2 = new ArrayList();
        if (arrayList.contains(Integer.valueOf(1))) {
            arrayList2.add("focus_mode_auto");
        }
        if (arrayList.contains(Integer.valueOf(2))) {
            arrayList2.add("focus_mode_macro");
        }
        if (arrayList.contains(Integer.valueOf(1))) {
            arrayList2.add("focus_mode_locked");
        }
        if (arrayList.contains(Integer.valueOf(0))) {
            arrayList2.add("focus_mode_infinity");
            if (f > 0.0f) {
                arrayList2.add("focus_mode_manual2");
            }
        }
        if (arrayList.contains(Integer.valueOf(5))) {
            arrayList2.add("focus_mode_edof");
        }
        if (arrayList.contains(Integer.valueOf(4))) {
            arrayList2.add("focus_mode_continuous_picture");
        }
        if (arrayList.contains(Integer.valueOf(3))) {
            arrayList2.add("focus_mode_continuous_video");
        }
        return arrayList2;
    }

    public CameraFeatures getCameraFeatures() throws CameraControllerException {
        int[] iArr;
        Range[] rangeArr;
        boolean z;
        Range[] highSpeedVideoFpsRanges;
        Size[] highSpeedVideoSizes;
        Range[] highSpeedVideoFpsRangesFor;
        boolean z2;
        CameraFeatures cameraFeatures = new CameraFeatures();
        float floatValue = ((Float) this.characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)).floatValue();
        cameraFeatures.is_zoom_supported = floatValue > 0.0f ? do_af_trigger_for_continuous : false;
        if (cameraFeatures.is_zoom_supported) {
            double d = (double) floatValue;
            Double.isNaN(d);
            int log = (int) ((Math.log(d + 1.0E-11d) * 20.0d) / Math.log(2.0d));
            double d2 = (double) log;
            Double.isNaN(d2);
            double pow = Math.pow(d, 1.0d / d2);
            cameraFeatures.zoom_ratios = new ArrayList();
            cameraFeatures.zoom_ratios.add(Integer.valueOf(100));
            double d3 = 1.0d;
            for (int i = 0; i < log - 1; i++) {
                d3 *= pow;
                cameraFeatures.zoom_ratios.add(Integer.valueOf((int) (d3 * 100.0d)));
            }
            cameraFeatures.zoom_ratios.add(Integer.valueOf((int) (floatValue * 100.0f)));
            cameraFeatures.max_zoom = cameraFeatures.zoom_ratios.size() - 1;
            this.zoom_ratios = cameraFeatures.zoom_ratios;
        } else {
            this.zoom_ratios = null;
        }
        int[] iArr2 = (int[]) this.characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
        cameraFeatures.supports_face_detection = false;
        this.supports_face_detect_mode_simple = false;
        this.supports_face_detect_mode_full = false;
        for (int i2 : iArr2) {
            if (i2 == 1) {
                cameraFeatures.supports_face_detection = do_af_trigger_for_continuous;
                this.supports_face_detect_mode_simple = do_af_trigger_for_continuous;
            } else if (i2 == 2) {
                cameraFeatures.supports_face_detection = do_af_trigger_for_continuous;
                this.supports_face_detect_mode_full = do_af_trigger_for_continuous;
            }
        }
        if (cameraFeatures.supports_face_detection && ((Integer) this.characteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT)).intValue() <= 0) {
            cameraFeatures.supports_face_detection = false;
            this.supports_face_detect_mode_simple = false;
            this.supports_face_detect_mode_full = false;
        }
        if (cameraFeatures.supports_face_detection) {
            int[] iArr3 = (int[]) this.characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES);
            int length = iArr3.length;
            int i3 = 0;
            while (true) {
                if (i3 >= length) {
                    z2 = false;
                    break;
                } else if (iArr3[i3] == 1) {
                    z2 = do_af_trigger_for_continuous;
                    break;
                } else {
                    i3++;
                }
            }
            if (!z2) {
                cameraFeatures.supports_face_detection = false;
                this.supports_face_detect_mode_simple = false;
                this.supports_face_detect_mode_full = false;
            }
        }
        boolean z3 = false;
        boolean z4 = false;
        boolean z5 = false;
        for (int i4 : (int[]) this.characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)) {
            if (i4 == 2) {
                z5 = do_af_trigger_for_continuous;
            } else if (i4 == 3) {
                z3 = do_af_trigger_for_continuous;
            } else if (i4 == 9 && VERSION.SDK_INT >= 23) {
                z4 = do_af_trigger_for_continuous;
            }
        }
        cameraFeatures.supports_burst = CameraControllerManager2.isHardwareLevelSupported(this.characteristics, 0);
        try {
            StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) this.characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] outputSizes = streamConfigurationMap.getOutputSizes(256);
            cameraFeatures.picture_sizes = new ArrayList();
            if (VERSION.SDK_INT >= 23) {
                Size[] highResolutionOutputSizes = streamConfigurationMap.getHighResolutionOutputSizes(256);
                if (highResolutionOutputSizes != null) {
                    for (Size size : highResolutionOutputSizes) {
                        boolean z6 = false;
                        for (Size equals : outputSizes) {
                            if (equals.equals(size)) {
                                z6 = do_af_trigger_for_continuous;
                            }
                        }
                        if (!z6) {
                            CameraController.Size size2 = new CameraController.Size(size.getWidth(), size.getHeight());
                            size2.supports_burst = false;
                            cameraFeatures.picture_sizes.add(size2);
                        }
                    }
                }
            }
            for (Size size3 : outputSizes) {
                cameraFeatures.picture_sizes.add(new CameraController.Size(size3.getWidth(), size3.getHeight()));
            }
            Collections.sort(cameraFeatures.picture_sizes, new SizeSorter());
            this.raw_size = null;
            if (z3) {
                Size[] outputSizes2 = streamConfigurationMap.getOutputSizes(32);
                if (outputSizes2 == null) {
                    this.want_raw = false;
                } else {
                    for (Size size4 : outputSizes2) {
                        if (this.raw_size == null || size4.getWidth() * size4.getHeight() > this.raw_size.getWidth() * this.raw_size.getHeight()) {
                            this.raw_size = size4;
                        }
                    }
                    if (this.raw_size == null) {
                        this.want_raw = false;
                    } else {
                        cameraFeatures.supports_raw = do_af_trigger_for_continuous;
                    }
                }
            } else {
                this.want_raw = false;
            }
            this.ae_fps_ranges = new ArrayList();
            for (Range range : (Range[]) this.characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)) {
                this.ae_fps_ranges.add(new int[]{((Integer) range.getLower()).intValue(), ((Integer) range.getUpper()).intValue()});
            }
            Collections.sort(this.ae_fps_ranges, new RangeSorter());
            Size[] outputSizes3 = streamConfigurationMap.getOutputSizes(MediaRecorder.class);
            cameraFeatures.video_sizes = new ArrayList();
            int i5 = 9999;
            for (int[] iArr4 : this.ae_fps_ranges) {
                i5 = Math.min(i5, iArr4[0]);
            }
            for (Size size5 : outputSizes3) {
                if (size5.getWidth() <= 4096 && size5.getHeight() <= 2160) {
                    double outputMinFrameDuration = (double) streamConfigurationMap.getOutputMinFrameDuration(MediaRecorder.class, size5);
                    Double.isNaN(outputMinFrameDuration);
                    int i6 = (int) ((1.0d / outputMinFrameDuration) * 1.0E9d);
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(new int[]{i5, i6});
                    cameraFeatures.video_sizes.add(new CameraController.Size(size5.getWidth(), size5.getHeight(), arrayList, false));
                }
            }
            Collections.sort(cameraFeatures.video_sizes, new SizeSorter());
            if (z4) {
                this.hs_fps_ranges = new ArrayList();
                cameraFeatures.video_sizes_high_speed = new ArrayList();
                for (Range range2 : streamConfigurationMap.getHighSpeedVideoFpsRanges()) {
                    this.hs_fps_ranges.add(new int[]{((Integer) range2.getLower()).intValue(), ((Integer) range2.getUpper()).intValue()});
                }
                Collections.sort(this.hs_fps_ranges, new RangeSorter());
                for (Size size6 : streamConfigurationMap.getHighSpeedVideoSizes()) {
                    ArrayList arrayList2 = new ArrayList();
                    for (Range range3 : streamConfigurationMap.getHighSpeedVideoFpsRangesFor(size6)) {
                        arrayList2.add(new int[]{((Integer) range3.getLower()).intValue(), ((Integer) range3.getUpper()).intValue()});
                    }
                    if (size6.getWidth() <= 4096) {
                        if (size6.getHeight() <= 2160) {
                            cameraFeatures.video_sizes_high_speed.add(new CameraController.Size(size6.getWidth(), size6.getHeight(), arrayList2, do_af_trigger_for_continuous));
                        }
                    }
                }
                Collections.sort(cameraFeatures.video_sizes_high_speed, new SizeSorter());
            }
            Size[] outputSizes4 = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
            cameraFeatures.preview_sizes = new ArrayList();
            Point point = new Point();
            ((Activity) this.context).getWindowManager().getDefaultDisplay().getRealSize(point);
            if (point.x < point.y) {
                point.set(point.y, point.x);
            }
            for (Size size7 : outputSizes4) {
                if (size7.getWidth() <= point.x && size7.getHeight() <= point.y) {
                    cameraFeatures.preview_sizes.add(new CameraController.Size(size7.getWidth(), size7.getHeight()));
                }
            }
            String str = "flash_off";
            if (((Boolean) this.characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)).booleanValue()) {
                cameraFeatures.supported_flash_values = new ArrayList();
                cameraFeatures.supported_flash_values.add(str);
                cameraFeatures.supported_flash_values.add("flash_auto");
                cameraFeatures.supported_flash_values.add("flash_on");
                cameraFeatures.supported_flash_values.add("flash_torch");
                if (!this.use_fake_precapture) {
                    cameraFeatures.supported_flash_values.add("flash_red_eye");
                }
            } else if (isFrontFacing()) {
                cameraFeatures.supported_flash_values = new ArrayList();
                cameraFeatures.supported_flash_values.add(str);
                cameraFeatures.supported_flash_values.add("flash_frontscreen_auto");
                cameraFeatures.supported_flash_values.add("flash_frontscreen_on");
                cameraFeatures.supported_flash_values.add("flash_frontscreen_torch");
            }
            Float f = (Float) this.characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
            if (f != null) {
                cameraFeatures.minimum_focus_distance = f.floatValue();
            } else {
                cameraFeatures.minimum_focus_distance = 0.0f;
            }
            cameraFeatures.supported_focus_values = convertFocusModesToValues((int[]) this.characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES), cameraFeatures.minimum_focus_distance);
            if (cameraFeatures.supported_focus_values == null || !cameraFeatures.supported_focus_values.contains("focus_mode_manual2")) {
                z = do_af_trigger_for_continuous;
            } else {
                z = do_af_trigger_for_continuous;
                cameraFeatures.supports_focus_bracketing = do_af_trigger_for_continuous;
            }
            cameraFeatures.max_num_focus_areas = ((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)).intValue();
            cameraFeatures.is_exposure_lock_supported = z;
            cameraFeatures.is_white_balance_lock_supported = z;
            cameraFeatures.is_video_stabilization_supported = false;
            int[] iArr5 = (int[]) this.characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
            if (iArr5 != null) {
                int length2 = iArr5.length;
                int i7 = 0;
                while (i7 < length2) {
                    if (iArr5[i7] == z) {
                        cameraFeatures.is_video_stabilization_supported = z;
                    }
                    i7++;
                    z = do_af_trigger_for_continuous;
                }
            }
            cameraFeatures.is_photo_video_recording_supported = CameraControllerManager2.isHardwareLevelSupported(this.characteristics, 0);
            this.supports_photo_video_recording = cameraFeatures.is_photo_video_recording_supported;
            int[] iArr6 = (int[]) this.characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
            if (iArr6 != null) {
                for (int i8 : iArr6) {
                    if (i8 == 0 && z5 && allowManualWB()) {
                        cameraFeatures.supports_white_balance_temperature = do_af_trigger_for_continuous;
                        cameraFeatures.min_temperature = 1000;
                        cameraFeatures.max_temperature = max_white_balance_temperature_c;
                    }
                }
            }
            if (CameraControllerManager2.isHardwareLevelSupported(this.characteristics, 0)) {
                Range range4 = (Range) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                if (range4 != null) {
                    cameraFeatures.supports_iso_range = do_af_trigger_for_continuous;
                    cameraFeatures.min_iso = ((Integer) range4.getLower()).intValue();
                    cameraFeatures.max_iso = ((Integer) range4.getUpper()).intValue();
                    Range range5 = (Range) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                    if (range5 != null) {
                        cameraFeatures.supports_exposure_time = do_af_trigger_for_continuous;
                        cameraFeatures.supports_expo_bracketing = do_af_trigger_for_continuous;
                        cameraFeatures.max_expo_bracketing_n_images = 5;
                        cameraFeatures.min_exposure_time = ((Long) range5.getLower()).longValue();
                        cameraFeatures.max_exposure_time = ((Long) range5.getUpper()).longValue();
                    }
                }
            }
            Range range6 = (Range) this.characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
            cameraFeatures.min_exposure = ((Integer) range6.getLower()).intValue();
            cameraFeatures.max_exposure = ((Integer) range6.getUpper()).intValue();
            cameraFeatures.exposure_step = ((Rational) this.characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)).floatValue();
            boolean z7 = do_af_trigger_for_continuous;
            cameraFeatures.can_disable_shutter_sound = do_af_trigger_for_continuous;
            if (z5) {
                Integer num = (Integer) this.characteristics.get(CameraCharacteristics.TONEMAP_MAX_CURVE_POINTS);
                if (num != null) {
                    cameraFeatures.tonemap_max_curve_points = num.intValue();
                    if (num.intValue() < 64) {
                        z7 = false;
                    }
                    cameraFeatures.supports_tonemap_curve = z7;
                }
            }
            Rect rect = (Rect) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            SizeF sizeF = (SizeF) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            Size size8 = (Size) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
            float[] fArr = (float[]) this.characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            float width = ((float) rect.width()) / ((float) size8.getWidth());
            float height = ((float) rect.height()) / ((float) size8.getHeight());
            double width2 = (double) (sizeF.getWidth() * width);
            double d4 = (double) fArr[0];
            Double.isNaN(d4);
            cameraFeatures.view_angle_x = (float) Math.toDegrees(Math.atan2(width2, d4 * 2.0d) * 2.0d);
            double height2 = (double) (sizeF.getHeight() * height);
            double d5 = (double) fArr[0];
            Double.isNaN(d5);
            cameraFeatures.view_angle_y = (float) Math.toDegrees(Math.atan2(height2, d5 * 2.0d) * 2.0d);
            return cameraFeatures;
        } catch (IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
            throw new CameraControllerException();
        }
    }

    public boolean shouldCoverPreview() {
        return this.has_received_frame ^ do_af_trigger_for_continuous;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00d8, code lost:
        if (r5.equals("barcode") != false) goto L_0x0107;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public net.sourceforge.opencamera.cameracontroller.CameraController.SupportedValues setSceneMode(java.lang.String r21) {
        /*
            r20 = this;
            r1 = r20
            android.hardware.camera2.CameraCharacteristics r0 = r1.characteristics
            android.hardware.camera2.CameraCharacteristics$Key r2 = android.hardware.camera2.CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES
            java.lang.Object r0 = r0.get(r2)
            int[] r0 = (int[]) r0
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            r3 = 1
            r4 = 0
            if (r0 == 0) goto L_0x002b
            int r5 = r0.length
            r6 = 0
            r7 = 0
        L_0x0018:
            if (r6 >= r5) goto L_0x002c
            r8 = r0[r6]
            if (r8 != 0) goto L_0x001f
            r7 = 1
        L_0x001f:
            java.lang.String r8 = r1.convertSceneMode(r8)
            if (r8 == 0) goto L_0x0028
            r2.add(r8)
        L_0x0028:
            int r6 = r6 + 1
            goto L_0x0018
        L_0x002b:
            r7 = 0
        L_0x002c:
            java.lang.String r0 = "auto"
            if (r7 != 0) goto L_0x0033
            r2.add(r4, r0)
        L_0x0033:
            r5 = r21
            net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues r2 = r1.checkModeIsSupported(r2, r5, r0)
            if (r2 == 0) goto L_0x0149
            java.lang.String r5 = r2.selected_value
            r6 = -1
            int r7 = r5.hashCode()
            r8 = 11
            r9 = 13
            r10 = 9
            r11 = 3
            r12 = 14
            r13 = 6
            r14 = 5
            r15 = 4
            r16 = 12
            r17 = 15
            r18 = 8
            r19 = 2
            switch(r7) {
                case -1422950858: goto L_0x00fc;
                case -1350043241: goto L_0x00f1;
                case -895760513: goto L_0x00e6;
                case -891172202: goto L_0x00db;
                case -333584256: goto L_0x00d2;
                case -300277408: goto L_0x00c7;
                case -264202484: goto L_0x00bd;
                case 3005871: goto L_0x00b5;
                case 3535235: goto L_0x00aa;
                case 93610339: goto L_0x00a0;
                case 104817688: goto L_0x0095;
                case 106437350: goto L_0x0089;
                case 729267099: goto L_0x007d;
                case 1430647483: goto L_0x0072;
                case 1664284080: goto L_0x0066;
                case 1900012073: goto L_0x005b;
                default: goto L_0x0059;
            }
        L_0x0059:
            goto L_0x0106
        L_0x005b:
            java.lang.String r0 = "candlelight"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 3
            goto L_0x0107
        L_0x0066:
            java.lang.String r0 = "night-portrait"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 8
            goto L_0x0107
        L_0x0072:
            java.lang.String r0 = "landscape"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 6
            goto L_0x0107
        L_0x007d:
            java.lang.String r0 = "portrait"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 10
            goto L_0x0107
        L_0x0089:
            java.lang.String r0 = "party"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 9
            goto L_0x0107
        L_0x0095:
            java.lang.String r0 = "night"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 7
            goto L_0x0107
        L_0x00a0:
            java.lang.String r0 = "beach"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 2
            goto L_0x0107
        L_0x00aa:
            java.lang.String r0 = "snow"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 11
            goto L_0x0107
        L_0x00b5:
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 4
            goto L_0x0107
        L_0x00bd:
            java.lang.String r0 = "fireworks"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 5
            goto L_0x0107
        L_0x00c7:
            java.lang.String r0 = "steadyphoto"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 13
            goto L_0x0107
        L_0x00d2:
            java.lang.String r0 = "barcode"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            goto L_0x0107
        L_0x00db:
            java.lang.String r0 = "sunset"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 14
            goto L_0x0107
        L_0x00e6:
            java.lang.String r0 = "sports"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 12
            goto L_0x0107
        L_0x00f1:
            java.lang.String r0 = "theatre"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 15
            goto L_0x0107
        L_0x00fc:
            java.lang.String r0 = "action"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0106
            r3 = 0
            goto L_0x0107
        L_0x0106:
            r3 = -1
        L_0x0107:
            switch(r3) {
                case 0: goto L_0x0130;
                case 1: goto L_0x012d;
                case 2: goto L_0x012a;
                case 3: goto L_0x0127;
                case 4: goto L_0x0131;
                case 5: goto L_0x0124;
                case 6: goto L_0x0122;
                case 7: goto L_0x0120;
                case 8: goto L_0x011e;
                case 9: goto L_0x011b;
                case 10: goto L_0x0119;
                case 11: goto L_0x0116;
                case 12: goto L_0x0113;
                case 13: goto L_0x0110;
                case 14: goto L_0x010d;
                case 15: goto L_0x010b;
                default: goto L_0x010a;
            }
        L_0x010a:
            goto L_0x0131
        L_0x010b:
            r4 = 7
            goto L_0x0131
        L_0x010d:
            r4 = 10
            goto L_0x0131
        L_0x0110:
            r4 = 11
            goto L_0x0131
        L_0x0113:
            r4 = 13
            goto L_0x0131
        L_0x0116:
            r4 = 9
            goto L_0x0131
        L_0x0119:
            r4 = 3
            goto L_0x0131
        L_0x011b:
            r4 = 14
            goto L_0x0131
        L_0x011e:
            r4 = 6
            goto L_0x0131
        L_0x0120:
            r4 = 5
            goto L_0x0131
        L_0x0122:
            r4 = 4
            goto L_0x0131
        L_0x0124:
            r4 = 12
            goto L_0x0131
        L_0x0127:
            r4 = 15
            goto L_0x0131
        L_0x012a:
            r4 = 8
            goto L_0x0131
        L_0x012d:
            r4 = 16
            goto L_0x0131
        L_0x0130:
            r4 = 2
        L_0x0131:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r0 = r1.camera_settings
            r0.scene_mode = r4
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r0 = r1.camera_settings
            android.hardware.camera2.CaptureRequest$Builder r3 = r1.previewBuilder
            boolean r0 = r0.setSceneMode(r3)
            if (r0 == 0) goto L_0x0149
            r20.setRepeatingRequest()     // Catch:{ CameraAccessException -> 0x0144 }
            goto L_0x0149
        L_0x0144:
            r0 = move-exception
            r3 = r0
            r3.printStackTrace()
        L_0x0149:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.setSceneMode(java.lang.String):net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues");
    }

    public String getSceneMode() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_SCENE_MODE) == null) {
            return null;
        }
        return convertSceneMode(((Integer) this.previewBuilder.get(CaptureRequest.CONTROL_SCENE_MODE)).intValue());
    }

    public SupportedValues setColorEffect(String str) {
        int[] iArr = (int[]) this.characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
        if (iArr == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        int i = 0;
        for (int convertColorEffect : iArr) {
            String convertColorEffect2 = convertColorEffect(convertColorEffect);
            if (convertColorEffect2 != null) {
                arrayList.add(convertColorEffect2);
            }
        }
        String str2 = CameraController.COLOR_EFFECT_DEFAULT;
        SupportedValues checkModeIsSupported = checkModeIsSupported(arrayList, str, str2);
        if (checkModeIsSupported != null) {
            String str3 = checkModeIsSupported.selected_value;
            char c = 65535;
            switch (str3.hashCode()) {
                case -1635350969:
                    if (str3.equals("blackboard")) {
                        c = 1;
                        break;
                    }
                    break;
                case 3002044:
                    if (str3.equals("aqua")) {
                        c = 0;
                        break;
                    }
                    break;
                case 3357411:
                    if (str3.equals("mono")) {
                        c = 2;
                        break;
                    }
                    break;
                case 3387192:
                    if (str3.equals(str2)) {
                        c = 4;
                        break;
                    }
                    break;
                case 109324790:
                    if (str3.equals("sepia")) {
                        c = 6;
                        break;
                    }
                    break;
                case 261182557:
                    if (str3.equals("whiteboard")) {
                        c = 8;
                        break;
                    }
                    break;
                case 921111605:
                    if (str3.equals("negative")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1473417203:
                    if (str3.equals("solarize")) {
                        c = 7;
                        break;
                    }
                    break;
                case 2008448231:
                    if (str3.equals("posterize")) {
                        c = 5;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    i = 8;
                    break;
                case 1:
                    i = 7;
                    break;
                case 2:
                    i = 1;
                    break;
                case 3:
                    i = 2;
                    break;
                case 5:
                    i = 5;
                    break;
                case 6:
                    i = 4;
                    break;
                case 7:
                    i = 3;
                    break;
                case 8:
                    i = 6;
                    break;
            }
            this.camera_settings.color_effect = i;
            if (this.camera_settings.setColorEffect(this.previewBuilder)) {
                try {
                    setRepeatingRequest();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return checkModeIsSupported;
    }

    public String getColorEffect() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_EFFECT_MODE) == null) {
            return null;
        }
        return convertColorEffect(((Integer) this.previewBuilder.get(CaptureRequest.CONTROL_EFFECT_MODE)).intValue());
    }

    private boolean allowManualWB() {
        return Build.MODEL.toLowerCase(Locale.US).contains("nexus 6") ^ do_af_trigger_for_continuous;
    }

    public SupportedValues setWhiteBalance(String str) {
        int[] iArr = (int[]) this.characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
        if (iArr == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (int i : iArr) {
            String convertWhiteBalance = convertWhiteBalance(i);
            if (convertWhiteBalance != null && (i != 0 || allowManualWB())) {
                arrayList.add(convertWhiteBalance);
            }
        }
        String str2 = "auto";
        boolean remove = arrayList.remove(str2);
        String str3 = "manual";
        if (arrayList.remove(str3)) {
            arrayList.add(0, str3);
        }
        if (remove) {
            arrayList.add(0, str2);
        }
        SupportedValues checkModeIsSupported = checkModeIsSupported(arrayList, str, str2);
        if (checkModeIsSupported != null) {
            String str4 = checkModeIsSupported.selected_value;
            char c = 65535;
            int i2 = 1;
            switch (str4.hashCode()) {
                case -1081415738:
                    if (str4.equals(str3)) {
                        c = 8;
                        break;
                    }
                    break;
                case -939299377:
                    if (str4.equals("incandescent")) {
                        c = 4;
                        break;
                    }
                    break;
                case -719316704:
                    if (str4.equals("warm-fluorescent")) {
                        c = 7;
                        break;
                    }
                    break;
                case 3005871:
                    if (str4.equals(str2)) {
                        c = 0;
                        break;
                    }
                    break;
                case 109399597:
                    if (str4.equals("shade")) {
                        c = 5;
                        break;
                    }
                    break;
                case 474934723:
                    if (str4.equals("cloudy-daylight")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1650323088:
                    if (str4.equals("twilight")) {
                        c = 6;
                        break;
                    }
                    break;
                case 1902580840:
                    if (str4.equals("fluorescent")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1942983418:
                    if (str4.equals("daylight")) {
                        c = 2;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 1:
                    i2 = 6;
                    break;
                case 2:
                    i2 = 5;
                    break;
                case 3:
                    i2 = 3;
                    break;
                case 4:
                    i2 = 2;
                    break;
                case 5:
                    i2 = 8;
                    break;
                case 6:
                    i2 = 7;
                    break;
                case 7:
                    i2 = 4;
                    break;
                case 8:
                    i2 = 0;
                    break;
            }
            this.camera_settings.white_balance = i2;
            if (this.camera_settings.setWhiteBalance(this.previewBuilder)) {
                try {
                    setRepeatingRequest();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return checkModeIsSupported;
    }

    public String getWhiteBalance() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_AWB_MODE) == null) {
            return null;
        }
        return convertWhiteBalance(((Integer) this.previewBuilder.get(CaptureRequest.CONTROL_AWB_MODE)).intValue());
    }

    public boolean setWhiteBalanceTemperature(int i) {
        if (this.camera_settings.white_balance == i) {
            return false;
        }
        try {
            this.camera_settings.white_balance_temperature = Math.min(Math.max(i, 1000), max_white_balance_temperature_c);
            if (this.camera_settings.setWhiteBalance(this.previewBuilder)) {
                setRepeatingRequest();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return do_af_trigger_for_continuous;
    }

    public int getWhiteBalanceTemperature() {
        return this.camera_settings.white_balance_temperature;
    }

    public SupportedValues setAntiBanding(String str) {
        int[] iArr = (int[]) this.characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
        if (iArr == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (int convertAntiBanding : iArr) {
            String convertAntiBanding2 = convertAntiBanding(convertAntiBanding);
            if (convertAntiBanding2 != null) {
                arrayList.add(convertAntiBanding2);
            }
        }
        String str2 = "auto";
        SupportedValues checkModeIsSupported = checkModeIsSupported(arrayList, str, str2);
        if (checkModeIsSupported != null && checkModeIsSupported.selected_value.equals(str)) {
            String str3 = checkModeIsSupported.selected_value;
            char c = 65535;
            int i = 3;
            switch (str3.hashCode()) {
                case 109935:
                    if (str3.equals("off")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1628397:
                    if (str3.equals("50hz")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1658188:
                    if (str3.equals("60hz")) {
                        c = 2;
                        break;
                    }
                    break;
                case 3005871:
                    if (str3.equals(str2)) {
                        c = 0;
                        break;
                    }
                    break;
            }
            if (c != 0) {
                if (c == 1) {
                    i = 1;
                } else if (c == 2) {
                    i = 2;
                } else if (c == 3) {
                    i = 0;
                }
            }
            this.camera_settings.has_antibanding = do_af_trigger_for_continuous;
            this.camera_settings.antibanding = i;
            if (this.camera_settings.setAntiBanding(this.previewBuilder)) {
                try {
                    setRepeatingRequest();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return checkModeIsSupported;
    }

    public String getAntiBanding() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE) == null) {
            return null;
        }
        return convertAntiBanding(((Integer) this.previewBuilder.get(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE)).intValue());
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a6 A[SYNTHETIC, Splitter:B:43:0x00a6] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public net.sourceforge.opencamera.cameracontroller.CameraController.SupportedValues setEdgeMode(java.lang.String r8) {
        /*
            r7 = this;
            android.hardware.camera2.CameraCharacteristics r0 = r7.characteristics
            android.hardware.camera2.CameraCharacteristics$Key r1 = android.hardware.camera2.CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES
            java.lang.Object r0 = r0.get(r1)
            int[] r0 = (int[]) r0
            if (r0 != 0) goto L_0x000e
            r8 = 0
            return r8
        L_0x000e:
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            java.lang.String r2 = "default"
            r1.add(r2)
            int r3 = r0.length
            r4 = 0
            r5 = 0
        L_0x001b:
            if (r5 >= r3) goto L_0x002b
            r6 = r0[r5]
            java.lang.String r6 = r7.convertEdgeMode(r6)
            if (r6 == 0) goto L_0x0028
            r1.add(r6)
        L_0x0028:
            int r5 = r5 + 1
            goto L_0x001b
        L_0x002b:
            net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues r0 = r7.checkModeIsSupported(r1, r8, r2)
            if (r0 == 0) goto L_0x00ae
            java.lang.String r1 = r0.selected_value
            boolean r1 = r1.equals(r8)
            if (r1 == 0) goto L_0x00ae
            boolean r8 = r8.equals(r2)
            r1 = 2
            r2 = 1
            if (r8 != 0) goto L_0x0080
            java.lang.String r8 = r0.selected_value
            r3 = -1
            int r5 = r8.hashCode()
            r6 = 109935(0x1ad6f, float:1.54052E-40)
            if (r5 == r6) goto L_0x006c
            r6 = 3135580(0x2fd85c, float:4.393883E-39)
            if (r5 == r6) goto L_0x0062
            r6 = 1790083938(0x6ab28362, float:1.0790462E26)
            if (r5 == r6) goto L_0x0058
            goto L_0x0075
        L_0x0058:
            java.lang.String r5 = "high_quality"
            boolean r8 = r8.equals(r5)
            if (r8 == 0) goto L_0x0075
            r3 = 1
            goto L_0x0075
        L_0x0062:
            java.lang.String r5 = "fast"
            boolean r8 = r8.equals(r5)
            if (r8 == 0) goto L_0x0075
            r3 = 0
            goto L_0x0075
        L_0x006c:
            java.lang.String r5 = "off"
            boolean r8 = r8.equals(r5)
            if (r8 == 0) goto L_0x0075
            r3 = 2
        L_0x0075:
            if (r3 == 0) goto L_0x007e
            if (r3 == r2) goto L_0x0082
            if (r3 == r1) goto L_0x007c
            goto L_0x0080
        L_0x007c:
            r1 = 0
            goto L_0x0082
        L_0x007e:
            r1 = 1
            goto L_0x0082
        L_0x0080:
            r1 = 1
            r2 = 0
        L_0x0082:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            boolean r8 = r8.has_edge_mode
            if (r8 != r2) goto L_0x0092
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            int r8 = r8.edge_mode
            if (r8 == r1) goto L_0x00ae
        L_0x0092:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            r8.has_edge_mode = r2
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            r8.edge_mode = r1
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            android.hardware.camera2.CaptureRequest$Builder r1 = r7.previewBuilder
            boolean r8 = r8.setEdgeMode(r1)
            if (r8 == 0) goto L_0x00ae
            r7.setRepeatingRequest()     // Catch:{ CameraAccessException -> 0x00aa }
            goto L_0x00ae
        L_0x00aa:
            r8 = move-exception
            r8.printStackTrace()
        L_0x00ae:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.setEdgeMode(java.lang.String):net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues");
    }

    public String getEdgeMode() {
        if (this.previewBuilder.get(CaptureRequest.EDGE_MODE) == null) {
            return null;
        }
        return convertEdgeMode(((Integer) this.previewBuilder.get(CaptureRequest.EDGE_MODE)).intValue());
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x00b8 A[SYNTHETIC, Splitter:B:47:0x00b8] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public net.sourceforge.opencamera.cameracontroller.CameraController.SupportedValues setNoiseReductionMode(java.lang.String r8) {
        /*
            r7 = this;
            android.hardware.camera2.CameraCharacteristics r0 = r7.characteristics
            android.hardware.camera2.CameraCharacteristics$Key r1 = android.hardware.camera2.CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES
            java.lang.Object r0 = r0.get(r1)
            int[] r0 = (int[]) r0
            if (r0 != 0) goto L_0x000e
            r8 = 0
            return r8
        L_0x000e:
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            java.lang.String r2 = "default"
            r1.add(r2)
            int r3 = r0.length
            r4 = 0
            r5 = 0
        L_0x001b:
            if (r5 >= r3) goto L_0x002b
            r6 = r0[r5]
            java.lang.String r6 = r7.convertNoiseReductionMode(r6)
            if (r6 == 0) goto L_0x0028
            r1.add(r6)
        L_0x0028:
            int r5 = r5 + 1
            goto L_0x001b
        L_0x002b:
            net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues r0 = r7.checkModeIsSupported(r1, r8, r2)
            if (r0 == 0) goto L_0x00c0
            java.lang.String r1 = r0.selected_value
            boolean r1 = r1.equals(r8)
            if (r1 == 0) goto L_0x00c0
            boolean r8 = r8.equals(r2)
            r1 = 3
            r2 = 2
            r3 = 1
            if (r8 != 0) goto L_0x0092
            java.lang.String r8 = r0.selected_value
            r5 = -1
            int r6 = r8.hashCode()
            switch(r6) {
                case 109935: goto L_0x006b;
                case 3135580: goto L_0x0061;
                case 1064537505: goto L_0x0057;
                case 1790083938: goto L_0x004d;
                default: goto L_0x004c;
            }
        L_0x004c:
            goto L_0x0074
        L_0x004d:
            java.lang.String r6 = "high_quality"
            boolean r8 = r8.equals(r6)
            if (r8 == 0) goto L_0x0074
            r5 = 1
            goto L_0x0074
        L_0x0057:
            java.lang.String r6 = "minimal"
            boolean r8 = r8.equals(r6)
            if (r8 == 0) goto L_0x0074
            r5 = 2
            goto L_0x0074
        L_0x0061:
            java.lang.String r6 = "fast"
            boolean r8 = r8.equals(r6)
            if (r8 == 0) goto L_0x0074
            r5 = 0
            goto L_0x0074
        L_0x006b:
            java.lang.String r6 = "off"
            boolean r8 = r8.equals(r6)
            if (r8 == 0) goto L_0x0074
            r5 = 3
        L_0x0074:
            if (r5 == 0) goto L_0x0090
            if (r5 == r3) goto L_0x008e
            if (r5 == r2) goto L_0x007f
            if (r5 == r1) goto L_0x007d
            goto L_0x0092
        L_0x007d:
            r1 = 0
            goto L_0x0094
        L_0x007f:
            int r8 = android.os.Build.VERSION.SDK_INT
            r2 = 23
            if (r8 < r2) goto L_0x0086
            goto L_0x0094
        L_0x0086:
            java.lang.String r8 = "CameraController2"
            java.lang.String r1 = "noise reduction minimal, but pre-Android M!"
            android.util.Log.e(r8, r1)
            goto L_0x0090
        L_0x008e:
            r1 = 2
            goto L_0x0094
        L_0x0090:
            r1 = 1
            goto L_0x0094
        L_0x0092:
            r1 = 1
            r3 = 0
        L_0x0094:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            boolean r8 = r8.has_noise_reduction_mode
            if (r8 != r3) goto L_0x00a4
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            int r8 = r8.noise_reduction_mode
            if (r8 == r1) goto L_0x00c0
        L_0x00a4:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            r8.has_noise_reduction_mode = r3
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            r8.noise_reduction_mode = r1
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            android.hardware.camera2.CaptureRequest$Builder r1 = r7.previewBuilder
            boolean r8 = r8.setNoiseReductionMode(r1)
            if (r8 == 0) goto L_0x00c0
            r7.setRepeatingRequest()     // Catch:{ CameraAccessException -> 0x00bc }
            goto L_0x00c0
        L_0x00bc:
            r8 = move-exception
            r8.printStackTrace()
        L_0x00c0:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.setNoiseReductionMode(java.lang.String):net.sourceforge.opencamera.cameracontroller.CameraController$SupportedValues");
    }

    public String getNoiseReductionMode() {
        if (this.previewBuilder.get(CaptureRequest.NOISE_REDUCTION_MODE) == null) {
            return null;
        }
        return convertNoiseReductionMode(((Integer) this.previewBuilder.get(CaptureRequest.NOISE_REDUCTION_MODE)).intValue());
    }

    public SupportedValues setISO(String str) {
        setManualISO(false, 0);
        return null;
    }

    public void setManualISO(boolean z, int i) {
        if (z) {
            try {
                Range range = (Range) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                if (range != null) {
                    this.camera_settings.has_iso = do_af_trigger_for_continuous;
                    this.camera_settings.iso = Math.min(Math.max(i, ((Integer) range.getLower()).intValue()), ((Integer) range.getUpper()).intValue());
                } else {
                    return;
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            this.camera_settings.has_iso = false;
            this.camera_settings.iso = 0;
        }
        if (this.camera_settings.setAEMode(this.previewBuilder, false)) {
            setRepeatingRequest();
        }
    }

    public boolean isManualISO() {
        return this.camera_settings.has_iso;
    }

    public boolean setISO(int i) {
        if (this.camera_settings.iso == i) {
            return false;
        }
        try {
            this.camera_settings.iso = i;
            if (this.camera_settings.setAEMode(this.previewBuilder, false)) {
                setRepeatingRequest();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return do_af_trigger_for_continuous;
    }

    public int getISO() {
        return this.camera_settings.iso;
    }

    public long getExposureTime() {
        return this.camera_settings.exposure_time;
    }

    public boolean setExposureTime(long j) {
        if (this.camera_settings.exposure_time == j) {
            return false;
        }
        try {
            this.camera_settings.exposure_time = j;
            if (this.camera_settings.setAEMode(this.previewBuilder, false)) {
                setRepeatingRequest();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return do_af_trigger_for_continuous;
    }

    public CameraController.Size getPictureSize() {
        return new CameraController.Size(this.picture_width, this.picture_height);
    }

    public void setPictureSize(int i, int i2) {
        if (this.camera != null) {
            if (this.captureSession == null) {
                this.picture_width = i;
                this.picture_height = i2;
                return;
            }
            throw new RuntimeException();
        }
    }

    public void setRaw(boolean z, int i) {
        if (this.camera != null) {
            if (this.want_raw != z || this.max_raw_images != i) {
                if (z && this.raw_size == null) {
                    return;
                }
                if (this.captureSession == null) {
                    this.want_raw = z;
                    this.max_raw_images = i;
                    return;
                }
                throw new RuntimeException();
            }
        }
    }

    public void setVideoHighSpeed(boolean z) {
        if (this.camera != null && this.want_video_high_speed != z) {
            if (this.captureSession == null) {
                this.want_video_high_speed = z;
                this.is_video_high_speed = false;
                return;
            }
            throw new RuntimeException();
        }
    }

    public void setBurstType(BurstType burstType) {
        if (this.camera != null && this.burst_type != burstType) {
            this.burst_type = burstType;
            updateUseFakePrecaptureMode(this.camera_settings.flash_value);
            this.camera_settings.setAEMode(this.previewBuilder, false);
        }
    }

    public BurstType getBurstType() {
        return this.burst_type;
    }

    public void setExpoBracketingNImages(int i) {
        if (i <= 1 || i % 2 == 0) {
            throw new RuntimeException();
        }
        if (i > 5) {
            i = 5;
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

    public void setUseExpoFastBurst(boolean z) {
        this.use_expo_fast_burst = z;
    }

    public boolean isBurstOrExpo() {
        if (this.burst_type != BurstType.BURSTTYPE_NONE) {
            return do_af_trigger_for_continuous;
        }
        return false;
    }

    public boolean isCapturingBurst() {
        boolean z = false;
        if (!isBurstOrExpo()) {
            return false;
        }
        if (this.burst_type == BurstType.BURSTTYPE_CONTINUOUS) {
            if (this.continuous_burst_in_progress || this.n_burst > 0 || this.n_burst_raw > 0) {
                z = do_af_trigger_for_continuous;
            }
            return z;
        }
        if (getBurstTotal() > 1 && getNBurstTaken() < getBurstTotal()) {
            z = do_af_trigger_for_continuous;
        }
        return z;
    }

    public int getNBurstTaken() {
        return this.n_burst_taken;
    }

    public int getBurstTotal() {
        if (this.burst_type == BurstType.BURSTTYPE_CONTINUOUS) {
            return 0;
        }
        return this.n_burst_total;
    }

    public void setOptimiseAEForDRO(boolean z) {
        if (Build.MANUFACTURER.toLowerCase(Locale.US).contains("oneplus")) {
            this.optimise_ae_for_dro = false;
        } else {
            this.optimise_ae_for_dro = z;
        }
    }

    public void setBurstNImages(int i) {
        this.burst_requested_n_images = i;
    }

    public void setBurstForNoiseReduction(boolean z, boolean z2) {
        this.burst_for_noise_reduction = z;
        this.noise_reduction_low_light = z2;
    }

    public boolean isContinuousBurstInProgress() {
        return this.continuous_burst_in_progress;
    }

    public void stopContinuousBurst() {
        this.continuous_burst_in_progress = false;
    }

    public void stopFocusBracketingBurst() {
        if (this.burst_type == BurstType.BURSTTYPE_FOCUS) {
            this.focus_bracketing_in_progress = false;
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("stopFocusBracketingBurst burst_type is: ");
        sb.append(this.burst_type);
        Log.e(TAG, sb.toString());
    }

    public void setUseCamera2FakeFlash(boolean z) {
        if (this.camera != null && this.use_fake_precapture != z) {
            this.use_fake_precapture = z;
            this.use_fake_precapture_mode = z;
        }
    }

    public boolean getUseCamera2FakeFlash() {
        return this.use_fake_precapture;
    }

    private void createPictureImageReader() {
        if (this.captureSession == null) {
            closePictureImageReader();
            int i = this.picture_width;
            if (i != 0) {
                int i2 = this.picture_height;
                if (i2 != 0) {
                    this.imageReader = ImageReader.newInstance(i, i2, 256, 2);
                    this.imageReader.setOnImageAvailableListener(new OnImageAvailableListener(), null);
                    if (this.want_raw) {
                        Size size = this.raw_size;
                        if (size != null && !this.previewIsVideoMode) {
                            this.imageReaderRaw = ImageReader.newInstance(size.getWidth(), this.raw_size.getHeight(), 32, this.max_raw_images);
                            ImageReader imageReader2 = this.imageReaderRaw;
                            OnRawImageAvailableListener onRawImageAvailableListener2 = new OnRawImageAvailableListener();
                            this.onRawImageAvailableListener = onRawImageAvailableListener2;
                            imageReader2.setOnImageAvailableListener(onRawImageAvailableListener2, null);
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
            throw new RuntimeException();
        }
        throw new RuntimeException();
    }

    private void clearPending() {
        this.pending_burst_images.clear();
        this.pending_burst_images_raw.clear();
        this.pending_raw_image = null;
        OnRawImageAvailableListener onRawImageAvailableListener2 = this.onRawImageAvailableListener;
        if (onRawImageAvailableListener2 != null) {
            onRawImageAvailableListener2.clear();
        }
        this.slow_burst_capture_requests = null;
        this.n_burst = 0;
        this.n_burst_taken = 0;
        this.n_burst_total = 0;
        this.n_burst_raw = 0;
        this.burst_single_request = false;
        this.slow_burst_start_ms = 0;
    }

    private void takePendingRaw() {
        if (this.pending_raw_image != null) {
            synchronized (this.background_camera_lock) {
                this.raw_todo = false;
            }
            this.picture_cb.onRawPictureTaken(this.pending_raw_image);
            this.pending_raw_image = null;
            OnRawImageAvailableListener onRawImageAvailableListener2 = this.onRawImageAvailableListener;
            if (onRawImageAvailableListener2 != null) {
                onRawImageAvailableListener2.clear();
            }
        }
    }

    /* access modifiers changed from: private */
    public void checkImagesCompleted() {
        boolean z;
        boolean z2;
        synchronized (this.background_camera_lock) {
            boolean z3 = this.done_all_captures;
            z = do_af_trigger_for_continuous;
            if (z3) {
                if (this.picture_cb != null) {
                    if (!this.jpeg_todo && !this.raw_todo) {
                        z2 = do_af_trigger_for_continuous;
                        z = false;
                    } else if (!this.jpeg_todo && this.pending_raw_image != null) {
                        z2 = do_af_trigger_for_continuous;
                    }
                }
            }
            z2 = false;
            z = false;
        }
        if (z) {
            takePendingRaw();
        }
        if (z2) {
            PictureCallback pictureCallback = this.picture_cb;
            this.picture_cb = null;
            pictureCallback.onCompleted();
            synchronized (this.background_camera_lock) {
                if (this.burst_type == BurstType.BURSTTYPE_FOCUS) {
                    this.focus_bracketing_in_progress = false;
                }
            }
        }
    }

    public CameraController.Size getPreviewSize() {
        return new CameraController.Size(this.preview_width, this.preview_height);
    }

    public void setPreviewSize(int i, int i2) {
        this.preview_width = i;
        this.preview_height = i2;
    }

    public void setVideoStabilization(boolean z) {
        this.camera_settings.video_stabilization = z;
        this.camera_settings.setVideoStabilization(this.previewBuilder);
        try {
            setRepeatingRequest();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public boolean getVideoStabilization() {
        return this.camera_settings.video_stabilization;
    }

    public void setLogProfile(boolean z, float f) {
        if (this.camera_settings.use_log_profile != z || this.camera_settings.log_profile_strength != f) {
            this.camera_settings.use_log_profile = z;
            if (z) {
                this.camera_settings.log_profile_strength = f;
            } else {
                this.camera_settings.log_profile_strength = 0.0f;
            }
            this.camera_settings.setLogProfile(this.previewBuilder);
            try {
                setRepeatingRequest();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isLogProfile() {
        return this.camera_settings.use_log_profile;
    }

    public Builder testGetPreviewBuilder() {
        return this.previewBuilder;
    }

    public TonemapCurve testGetTonemapCurve() {
        return (TonemapCurve) this.previewBuilder.get(CaptureRequest.TONEMAP_CURVE);
    }

    public int getJpegQuality() {
        return this.camera_settings.jpeg_quality;
    }

    public void setJpegQuality(int i) {
        if (i < 0 || i > 100) {
            throw new RuntimeException();
        }
        this.camera_settings.jpeg_quality = (byte) i;
    }

    public int getZoom() {
        return this.current_zoom_value;
    }

    public void setZoom(int i) {
        List<Integer> list = this.zoom_ratios;
        if (list != null) {
            if (i < 0 || i > list.size()) {
                throw new RuntimeException();
            }
            Rect rect = (Rect) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            int width = rect.width() / 2;
            int height = rect.height() / 2;
            double width2 = (double) rect.width();
            double intValue = (double) (((float) ((Integer) this.zoom_ratios.get(i)).intValue()) / 100.0f);
            Double.isNaN(intValue);
            double d = intValue * 2.0d;
            Double.isNaN(width2);
            int i2 = (int) (width2 / d);
            double height2 = (double) rect.height();
            Double.isNaN(height2);
            int i3 = (int) (height2 / d);
            int i4 = width - i2;
            int i5 = width + i2;
            this.camera_settings.scalar_crop_region = new Rect(i4, height - i3, i5, height + i3);
            this.camera_settings.setCropRegion(this.previewBuilder);
            this.current_zoom_value = i;
            try {
                setRepeatingRequest();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public int getExposureCompensation() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION) == null) {
            return 0;
        }
        return ((Integer) this.previewBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION)).intValue();
    }

    public boolean setExposureCompensation(int i) {
        this.camera_settings.has_ae_exposure_compensation = do_af_trigger_for_continuous;
        this.camera_settings.ae_exposure_compensation = i;
        if (!this.camera_settings.setExposureCompensation(this.previewBuilder)) {
            return false;
        }
        try {
            setRepeatingRequest();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return do_af_trigger_for_continuous;
    }

    public void setPreviewFpsRange(int i, int i2) {
        this.camera_settings.ae_target_fps_range = new Range(Integer.valueOf(i / 1000), Integer.valueOf(i2 / 1000));
        CameraSettings cameraSettings = this.camera_settings;
        double d = (double) i;
        Double.isNaN(d);
        cameraSettings.sensor_frame_duration = (long) ((1.0d / (d / 1000.0d)) * 1.0E9d);
        try {
            if (this.camera_settings.setAEMode(this.previewBuilder, false)) {
                setRepeatingRequest();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void clearPreviewFpsRange() {
        if (this.camera_settings.ae_target_fps_range != null || this.camera_settings.sensor_frame_duration != 0) {
            this.camera_settings.ae_target_fps_range = null;
            this.camera_settings.sensor_frame_duration = 0;
            createPreviewRequest();
            try {
                if (this.camera_settings.setAEMode(this.previewBuilder, false)) {
                    setRepeatingRequest();
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public List<int[]> getSupportedPreviewFpsRange() {
        ArrayList arrayList = new ArrayList();
        for (int[] iArr : this.want_video_high_speed ? this.hs_fps_ranges : this.ae_fps_ranges) {
            arrayList.add(new int[]{iArr[0] * 1000, iArr[1] * 1000});
        }
        return arrayList;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setFocusValue(java.lang.String r8) {
        /*
            r7 = this;
            int r0 = r8.hashCode()
            r1 = 3
            r2 = 4
            r3 = 5
            r4 = 2
            r5 = 0
            r6 = 1
            switch(r0) {
                case -2084726721: goto L_0x0054;
                case -1897460700: goto L_0x004a;
                case -1897358037: goto L_0x0040;
                case -711944829: goto L_0x0036;
                case 295129751: goto L_0x002c;
                case 402565696: goto L_0x0022;
                case 590698013: goto L_0x0018;
                case 1318730743: goto L_0x000e;
                default: goto L_0x000d;
            }
        L_0x000d:
            goto L_0x005e
        L_0x000e:
            java.lang.String r0 = "focus_mode_macro"
            boolean r8 = r8.equals(r0)
            if (r8 == 0) goto L_0x005e
            r8 = 4
            goto L_0x005f
        L_0x0018:
            java.lang.String r0 = "focus_mode_infinity"
            boolean r8 = r8.equals(r0)
            if (r8 == 0) goto L_0x005e
            r8 = 2
            goto L_0x005f
        L_0x0022:
            java.lang.String r0 = "focus_mode_continuous_video"
            boolean r8 = r8.equals(r0)
            if (r8 == 0) goto L_0x005e
            r8 = 7
            goto L_0x005f
        L_0x002c:
            java.lang.String r0 = "focus_mode_manual2"
            boolean r8 = r8.equals(r0)
            if (r8 == 0) goto L_0x005e
            r8 = 3
            goto L_0x005f
        L_0x0036:
            java.lang.String r0 = "focus_mode_continuous_picture"
            boolean r8 = r8.equals(r0)
            if (r8 == 0) goto L_0x005e
            r8 = 6
            goto L_0x005f
        L_0x0040:
            java.lang.String r0 = "focus_mode_edof"
            boolean r8 = r8.equals(r0)
            if (r8 == 0) goto L_0x005e
            r8 = 5
            goto L_0x005f
        L_0x004a:
            java.lang.String r0 = "focus_mode_auto"
            boolean r8 = r8.equals(r0)
            if (r8 == 0) goto L_0x005e
            r8 = 0
            goto L_0x005f
        L_0x0054:
            java.lang.String r0 = "focus_mode_locked"
            boolean r8 = r8.equals(r0)
            if (r8 == 0) goto L_0x005e
            r8 = 1
            goto L_0x005f
        L_0x005e:
            r8 = -1
        L_0x005f:
            switch(r8) {
                case 0: goto L_0x007c;
                case 1: goto L_0x007c;
                case 2: goto L_0x0075;
                case 3: goto L_0x006b;
                case 4: goto L_0x0069;
                case 5: goto L_0x0067;
                case 6: goto L_0x0065;
                case 7: goto L_0x0063;
                default: goto L_0x0062;
            }
        L_0x0062:
            return
        L_0x0063:
            r5 = 3
            goto L_0x007d
        L_0x0065:
            r5 = 4
            goto L_0x007d
        L_0x0067:
            r5 = 5
            goto L_0x007d
        L_0x0069:
            r5 = 2
            goto L_0x007d
        L_0x006b:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            float r0 = r8.focus_distance_manual
            r8.focus_distance = r0
            goto L_0x007d
        L_0x0075:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            r0 = 0
            r8.focus_distance = r0
            goto L_0x007d
        L_0x007c:
            r5 = 1
        L_0x007d:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            r8.has_af_mode = r6
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            r8.af_mode = r5
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            android.hardware.camera2.CaptureRequest$Builder r0 = r7.previewBuilder
            r8.setFocusMode(r0)
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r7.camera_settings
            android.hardware.camera2.CaptureRequest$Builder r0 = r7.previewBuilder
            r8.setFocusDistance(r0)
            r7.setRepeatingRequest()     // Catch:{ CameraAccessException -> 0x0099 }
            goto L_0x009d
        L_0x0099:
            r8 = move-exception
            r8.printStackTrace()
        L_0x009d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.setFocusValue(java.lang.String):void");
    }

    public String getFocusValue() {
        Integer num = (Integer) this.previewBuilder.get(CaptureRequest.CONTROL_AF_MODE);
        if (num == null) {
            num = Integer.valueOf(1);
        }
        return convertFocusModeToValue(num.intValue());
    }

    public float getFocusDistance() {
        return this.camera_settings.focus_distance;
    }

    public boolean setFocusDistance(float f) {
        if (this.camera_settings.focus_distance == f) {
            return false;
        }
        this.camera_settings.focus_distance = f;
        this.camera_settings.focus_distance_manual = f;
        this.camera_settings.setFocusDistance(this.previewBuilder);
        try {
            setRepeatingRequest();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return do_af_trigger_for_continuous;
    }

    public void setFocusBracketingNImages(int i) {
        this.focus_bracketing_n_images = i;
    }

    public void setFocusBracketingAddInfinity(boolean z) {
        this.focus_bracketing_add_infinity = z;
    }

    public void setFocusBracketingSourceDistance(float f) {
        this.focus_bracketing_source_distance = f;
    }

    public float getFocusBracketingSourceDistance() {
        return this.focus_bracketing_source_distance;
    }

    public void setFocusBracketingTargetDistance(float f) {
        this.focus_bracketing_target_distance = f;
    }

    public float getFocusBracketingTargetDistance() {
        return this.focus_bracketing_target_distance;
    }

    private void updateUseFakePrecaptureMode(String str) {
        if ((str.equals("flash_frontscreen_auto") || str.equals("flash_frontscreen_on")) ? do_af_trigger_for_continuous : false) {
            this.use_fake_precapture_mode = do_af_trigger_for_continuous;
        } else if (this.burst_type != BurstType.BURSTTYPE_NONE) {
            this.use_fake_precapture_mode = do_af_trigger_for_continuous;
        } else {
            this.use_fake_precapture_mode = this.use_fake_precapture;
        }
    }

    public void setFlashValue(String str) {
        String str2 = "flash_off";
        if (!this.camera_settings.flash_value.equals(str)) {
            try {
                updateUseFakePrecaptureMode(str);
                if (!this.camera_settings.flash_value.equals("flash_torch") || str.equals(str2)) {
                    this.camera_settings.flash_value = str;
                    if (this.camera_settings.setAEMode(this.previewBuilder, false)) {
                        setRepeatingRequest();
                    }
                }
                this.camera_settings.flash_value = str2;
                this.camera_settings.setAEMode(this.previewBuilder, false);
                CaptureRequest build = this.previewBuilder.build();
                this.camera_settings.flash_value = str;
                this.camera_settings.setAEMode(this.previewBuilder, false);
                this.push_repeating_request_when_torch_off = do_af_trigger_for_continuous;
                this.push_repeating_request_when_torch_off_id = build;
                setRepeatingRequest(build);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public String getFlashValue() {
        if (!((Boolean) this.characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)).booleanValue()) {
            return BuildConfig.FLAVOR;
        }
        return this.camera_settings.flash_value;
    }

    public void setAutoExposureLock(boolean z) {
        this.camera_settings.ae_lock = z;
        this.camera_settings.setAutoExposureLock(this.previewBuilder);
        try {
            setRepeatingRequest();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public boolean getAutoExposureLock() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_AE_LOCK) == null) {
            return false;
        }
        return ((Boolean) this.previewBuilder.get(CaptureRequest.CONTROL_AE_LOCK)).booleanValue();
    }

    public void setAutoWhiteBalanceLock(boolean z) {
        this.camera_settings.wb_lock = z;
        this.camera_settings.setAutoWhiteBalanceLock(this.previewBuilder);
        try {
            setRepeatingRequest();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public boolean getAutoWhiteBalanceLock() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_AWB_LOCK) == null) {
            return false;
        }
        return ((Boolean) this.previewBuilder.get(CaptureRequest.CONTROL_AWB_LOCK)).booleanValue();
    }

    public void setRotation(int i) {
        this.camera_settings.rotation = i;
    }

    public void setLocationInfo(Location location) {
        this.camera_settings.location = location;
    }

    public void removeLocationInfo() {
        this.camera_settings.location = null;
    }

    public void enableShutterSound(boolean z) {
        this.sounds_enabled = z;
    }

    /* access modifiers changed from: private */
    public void playSound(int i) {
        if (this.sounds_enabled && ((AudioManager) this.context.getSystemService("audio")).getRingerMode() == 2) {
            this.media_action_sound.play(i);
        }
    }

    /* access modifiers changed from: private */
    public Rect getViewableRect() {
        Builder builder = this.previewBuilder;
        if (builder != null) {
            Rect rect = (Rect) builder.get(CaptureRequest.SCALER_CROP_REGION);
            if (rect != null) {
                return rect;
            }
        }
        Rect rect2 = (Rect) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        rect2.right -= rect2.left;
        rect2.left = 0;
        rect2.bottom -= rect2.top;
        rect2.top = 0;
        return rect2;
    }

    private Rect convertRectToCamera2(Rect rect, Rect rect2) {
        double d = (double) (rect2.left + 1000);
        Double.isNaN(d);
        double d2 = d / 2000.0d;
        double d3 = (double) (rect2.top + 1000);
        Double.isNaN(d3);
        double d4 = d3 / 2000.0d;
        double d5 = (double) (rect2.right + 1000);
        Double.isNaN(d5);
        double d6 = d5 / 2000.0d;
        double d7 = (double) (rect2.bottom + 1000);
        Double.isNaN(d7);
        double d8 = d7 / 2000.0d;
        double d9 = (double) rect.left;
        double width = (double) (rect.width() - 1);
        Double.isNaN(width);
        double d10 = d2 * width;
        Double.isNaN(d9);
        int i = (int) (d9 + d10);
        double d11 = (double) rect.left;
        double width2 = (double) (rect.width() - 1);
        Double.isNaN(width2);
        double d12 = d6 * width2;
        Double.isNaN(d11);
        int i2 = (int) (d11 + d12);
        double d13 = (double) rect.top;
        double height = (double) (rect.height() - 1);
        Double.isNaN(height);
        double d14 = d4 * height;
        Double.isNaN(d13);
        int i3 = (int) (d13 + d14);
        double d15 = (double) rect.top;
        double height2 = (double) (rect.height() - 1);
        Double.isNaN(height2);
        double d16 = d8 * height2;
        Double.isNaN(d15);
        int i4 = (int) (d15 + d16);
        int max = Math.max(i, rect.left);
        int max2 = Math.max(i2, rect.left);
        int max3 = Math.max(i3, rect.top);
        int max4 = Math.max(i4, rect.top);
        return new Rect(Math.min(max, rect.right), Math.min(max3, rect.bottom), Math.min(max2, rect.right), Math.min(max4, rect.bottom));
    }

    private MeteringRectangle convertAreaToMeteringRectangle(Rect rect, Area area) {
        return new MeteringRectangle(convertRectToCamera2(rect, area.rect), area.weight);
    }

    private Rect convertRectFromCamera2(Rect rect, Rect rect2) {
        double d = (double) (rect2.left - rect.left);
        double width = (double) (rect.width() - 1);
        Double.isNaN(d);
        Double.isNaN(width);
        double d2 = d / width;
        double d3 = (double) (rect2.top - rect.top);
        double height = (double) (rect.height() - 1);
        Double.isNaN(d3);
        Double.isNaN(height);
        double d4 = d3 / height;
        double d5 = (double) (rect2.right - rect.left);
        double width2 = (double) (rect.width() - 1);
        Double.isNaN(d5);
        Double.isNaN(width2);
        double d6 = d5 / width2;
        double d7 = (double) (rect2.bottom - rect.top);
        double height2 = (double) (rect.height() - 1);
        Double.isNaN(d7);
        Double.isNaN(height2);
        int i = ((int) (d6 * 2000.0d)) - 1000;
        int i2 = ((int) (d4 * 2000.0d)) - 1000;
        int i3 = ((int) ((d7 / height2) * 2000.0d)) - 1000;
        int max = Math.max(((int) (d2 * 2000.0d)) - 1000, NotificationManagerCompat.IMPORTANCE_UNSPECIFIED);
        int max2 = Math.max(i, NotificationManagerCompat.IMPORTANCE_UNSPECIFIED);
        int max3 = Math.max(i2, NotificationManagerCompat.IMPORTANCE_UNSPECIFIED);
        int max4 = Math.max(i3, NotificationManagerCompat.IMPORTANCE_UNSPECIFIED);
        return new Rect(Math.min(max, 1000), Math.min(max3, 1000), Math.min(max2, 1000), Math.min(max4, 1000));
    }

    private Area convertMeteringRectangleToArea(Rect rect, MeteringRectangle meteringRectangle) {
        return new Area(convertRectFromCamera2(rect, meteringRectangle.getRect()), meteringRectangle.getMeteringWeight());
    }

    /* access modifiers changed from: private */
    public CameraController.Face convertFromCameraFace(Rect rect, Face face) {
        return new CameraController.Face(face.getScore(), convertRectFromCamera2(rect, face.getBounds()));
    }

    public boolean setFocusAndMeteringArea(List<Area> list) {
        boolean z;
        Rect viewableRect = getViewableRect();
        int intValue = ((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)).intValue();
        boolean z2 = do_af_trigger_for_continuous;
        int i = 0;
        if (intValue > 0) {
            this.camera_settings.af_regions = new MeteringRectangle[list.size()];
            int i2 = 0;
            for (Area convertAreaToMeteringRectangle : list) {
                int i3 = i2 + 1;
                this.camera_settings.af_regions[i2] = convertAreaToMeteringRectangle(viewableRect, convertAreaToMeteringRectangle);
                i2 = i3;
            }
            this.camera_settings.setAFRegions(this.previewBuilder);
            z = do_af_trigger_for_continuous;
        } else {
            this.camera_settings.af_regions = null;
            z = false;
        }
        if (((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)).intValue() > 0) {
            this.camera_settings.ae_regions = new MeteringRectangle[list.size()];
            for (Area convertAreaToMeteringRectangle2 : list) {
                int i4 = i + 1;
                this.camera_settings.ae_regions[i] = convertAreaToMeteringRectangle(viewableRect, convertAreaToMeteringRectangle2);
                i = i4;
            }
            this.camera_settings.setAERegions(this.previewBuilder);
        } else {
            this.camera_settings.ae_regions = null;
            z2 = false;
        }
        if (z || z2) {
            try {
                setRepeatingRequest();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        return z;
    }

    public void clearFocusAndMetering() {
        boolean z;
        Rect viewableRect = getViewableRect();
        boolean z2 = false;
        if (viewableRect.width() <= 0 || viewableRect.height() <= 0) {
            this.camera_settings.af_regions = null;
            this.camera_settings.ae_regions = null;
            z = false;
        } else {
            if (((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)).intValue() > 0) {
                this.camera_settings.af_regions = new MeteringRectangle[1];
                MeteringRectangle[] access$9100 = this.camera_settings.af_regions;
                MeteringRectangle meteringRectangle = new MeteringRectangle(0, 0, viewableRect.width() - 1, viewableRect.height() - 1, 0);
                access$9100[0] = meteringRectangle;
                this.camera_settings.setAFRegions(this.previewBuilder);
                z = do_af_trigger_for_continuous;
            } else {
                this.camera_settings.af_regions = null;
                z = false;
            }
            if (((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)).intValue() > 0) {
                this.camera_settings.ae_regions = new MeteringRectangle[1];
                MeteringRectangle[] access$9300 = this.camera_settings.ae_regions;
                MeteringRectangle meteringRectangle2 = new MeteringRectangle(0, 0, viewableRect.width() - 1, viewableRect.height() - 1, 0);
                access$9300[0] = meteringRectangle2;
                this.camera_settings.setAERegions(this.previewBuilder);
                z2 = do_af_trigger_for_continuous;
            } else {
                this.camera_settings.ae_regions = null;
            }
        }
        if (z || z2) {
            try {
                setRepeatingRequest();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Area> getFocusAreas() {
        if (((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)).intValue() == 0) {
            return null;
        }
        MeteringRectangle[] meteringRectangleArr = (MeteringRectangle[]) this.previewBuilder.get(CaptureRequest.CONTROL_AF_REGIONS);
        if (meteringRectangleArr == null) {
            return null;
        }
        Rect viewableRect = getViewableRect();
        MeteringRectangle[] access$9100 = this.camera_settings.af_regions;
        MeteringRectangle meteringRectangle = new MeteringRectangle(0, 0, viewableRect.width() - 1, viewableRect.height() - 1, 0);
        access$9100[0] = meteringRectangle;
        if (meteringRectangleArr.length == 1 && meteringRectangleArr[0].getRect().left == 0 && meteringRectangleArr[0].getRect().top == 0 && meteringRectangleArr[0].getRect().right == viewableRect.width() - 1 && meteringRectangleArr[0].getRect().bottom == viewableRect.height() - 1) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (MeteringRectangle convertMeteringRectangleToArea : meteringRectangleArr) {
            arrayList.add(convertMeteringRectangleToArea(viewableRect, convertMeteringRectangleToArea));
        }
        return arrayList;
    }

    public List<Area> getMeteringAreas() {
        if (((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)).intValue() == 0) {
            return null;
        }
        MeteringRectangle[] meteringRectangleArr = (MeteringRectangle[]) this.previewBuilder.get(CaptureRequest.CONTROL_AE_REGIONS);
        if (meteringRectangleArr == null) {
            return null;
        }
        Rect viewableRect = getViewableRect();
        if (meteringRectangleArr.length == 1 && meteringRectangleArr[0].getRect().left == 0 && meteringRectangleArr[0].getRect().top == 0 && meteringRectangleArr[0].getRect().right == viewableRect.width() - 1 && meteringRectangleArr[0].getRect().bottom == viewableRect.height() - 1) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (MeteringRectangle convertMeteringRectangleToArea : meteringRectangleArr) {
            arrayList.add(convertMeteringRectangleToArea(viewableRect, convertMeteringRectangleToArea));
        }
        return arrayList;
    }

    public boolean supportsAutoFocus() {
        Builder builder = this.previewBuilder;
        if (builder == null) {
            return false;
        }
        Integer num = (Integer) builder.get(CaptureRequest.CONTROL_AF_MODE);
        if (num == null) {
            return false;
        }
        if (num.intValue() == 1 || num.intValue() == 2) {
            return do_af_trigger_for_continuous;
        }
        return false;
    }

    public boolean focusIsContinuous() {
        Builder builder = this.previewBuilder;
        if (builder == null) {
            return false;
        }
        Integer num = (Integer) builder.get(CaptureRequest.CONTROL_AF_MODE);
        if (num == null) {
            return false;
        }
        if (num.intValue() == 4 || num.intValue() == 3) {
            return do_af_trigger_for_continuous;
        }
        return false;
    }

    public boolean focusIsVideo() {
        Builder builder = this.previewBuilder;
        if (builder == null) {
            return false;
        }
        Integer num = (Integer) builder.get(CaptureRequest.CONTROL_AF_MODE);
        if (num != null && num.intValue() == 3) {
            return do_af_trigger_for_continuous;
        }
        return false;
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) throws CameraControllerException {
        throw new RuntimeException();
    }

    public void setPreviewTexture(TextureView textureView) throws CameraControllerException {
        if (this.texture == null) {
            this.texture = textureView.getSurfaceTexture();
            return;
        }
        throw new RuntimeException();
    }

    /* access modifiers changed from: private */
    public void setRepeatingRequest() throws CameraAccessException {
        setRepeatingRequest(this.previewBuilder.build());
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0037, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setRepeatingRequest(android.hardware.camera2.CaptureRequest r5) throws android.hardware.camera2.CameraAccessException {
        /*
            r4 = this;
            java.lang.Object r0 = r4.background_camera_lock
            monitor-enter(r0)
            android.hardware.camera2.CameraDevice r1 = r4.camera     // Catch:{ all -> 0x0038 }
            if (r1 == 0) goto L_0x0036
            android.hardware.camera2.CameraCaptureSession r1 = r4.captureSession     // Catch:{ all -> 0x0038 }
            if (r1 != 0) goto L_0x000c
            goto L_0x0036
        L_0x000c:
            boolean r1 = r4.is_video_high_speed     // Catch:{ IllegalStateException -> 0x0030 }
            if (r1 == 0) goto L_0x0026
            int r1 = android.os.Build.VERSION.SDK_INT     // Catch:{ IllegalStateException -> 0x0030 }
            r2 = 23
            if (r1 < r2) goto L_0x0026
            android.hardware.camera2.CameraCaptureSession r1 = r4.captureSession     // Catch:{ IllegalStateException -> 0x0030 }
            android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession r1 = (android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession) r1     // Catch:{ IllegalStateException -> 0x0030 }
            java.util.List r5 = r1.createHighSpeedRequestList(r5)     // Catch:{ IllegalStateException -> 0x0030 }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r2 = r4.previewCaptureCallback     // Catch:{ IllegalStateException -> 0x0030 }
            android.os.Handler r3 = r4.handler     // Catch:{ IllegalStateException -> 0x0030 }
            r1.setRepeatingBurst(r5, r2, r3)     // Catch:{ IllegalStateException -> 0x0030 }
            goto L_0x0034
        L_0x0026:
            android.hardware.camera2.CameraCaptureSession r1 = r4.captureSession     // Catch:{ IllegalStateException -> 0x0030 }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r2 = r4.previewCaptureCallback     // Catch:{ IllegalStateException -> 0x0030 }
            android.os.Handler r3 = r4.handler     // Catch:{ IllegalStateException -> 0x0030 }
            r1.setRepeatingRequest(r5, r2, r3)     // Catch:{ IllegalStateException -> 0x0030 }
            goto L_0x0034
        L_0x0030:
            r5 = move-exception
            r5.printStackTrace()     // Catch:{ all -> 0x0038 }
        L_0x0034:
            monitor-exit(r0)     // Catch:{ all -> 0x0038 }
            return
        L_0x0036:
            monitor-exit(r0)     // Catch:{ all -> 0x0038 }
            return
        L_0x0038:
            r5 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0038 }
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.setRepeatingRequest(android.hardware.camera2.CaptureRequest):void");
    }

    /* access modifiers changed from: private */
    public void capture() throws CameraAccessException {
        capture(this.previewBuilder.build());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0018, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void capture(android.hardware.camera2.CaptureRequest r5) throws android.hardware.camera2.CameraAccessException {
        /*
            r4 = this;
            java.lang.Object r0 = r4.background_camera_lock
            monitor-enter(r0)
            android.hardware.camera2.CameraDevice r1 = r4.camera     // Catch:{ all -> 0x0019 }
            if (r1 == 0) goto L_0x0017
            android.hardware.camera2.CameraCaptureSession r1 = r4.captureSession     // Catch:{ all -> 0x0019 }
            if (r1 != 0) goto L_0x000c
            goto L_0x0017
        L_0x000c:
            android.hardware.camera2.CameraCaptureSession r1 = r4.captureSession     // Catch:{ all -> 0x0019 }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r2 = r4.previewCaptureCallback     // Catch:{ all -> 0x0019 }
            android.os.Handler r3 = r4.handler     // Catch:{ all -> 0x0019 }
            r1.capture(r5, r2, r3)     // Catch:{ all -> 0x0019 }
            monitor-exit(r0)     // Catch:{ all -> 0x0019 }
            return
        L_0x0017:
            monitor-exit(r0)     // Catch:{ all -> 0x0019 }
            return
        L_0x0019:
            r5 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0019 }
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.capture(android.hardware.camera2.CaptureRequest):void");
    }

    /* access modifiers changed from: private */
    public void createPreviewRequest() {
        CameraDevice cameraDevice = this.camera;
        if (cameraDevice != null) {
            try {
                this.previewBuilder = cameraDevice.createCaptureRequest(1);
                this.previewIsVideoMode = false;
                this.previewBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, Integer.valueOf(1));
                this.camera_settings.setupBuilder(this.previewBuilder, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    public Surface getPreviewSurface() {
        return this.surface_texture;
    }

    /* JADX WARNING: Removed duplicated region for block: B:80:0x0104 A[LOOP:0: B:80:0x0104->B:133:0x0104, LOOP_START, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0119 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void createCaptureSession(final android.media.MediaRecorder r9, boolean r10) throws net.sourceforge.opencamera.cameracontroller.CameraControllerException {
        /*
            r8 = this;
            android.hardware.camera2.CaptureRequest$Builder r0 = r8.previewBuilder
            if (r0 == 0) goto L_0x0152
            android.hardware.camera2.CameraDevice r0 = r8.camera
            if (r0 != 0) goto L_0x0009
            return
        L_0x0009:
            java.lang.Object r0 = r8.background_camera_lock
            monitor-enter(r0)
            android.hardware.camera2.CameraCaptureSession r1 = r8.captureSession     // Catch:{ all -> 0x014f }
            r2 = 0
            if (r1 == 0) goto L_0x0018
            android.hardware.camera2.CameraCaptureSession r1 = r8.captureSession     // Catch:{ all -> 0x014f }
            r1.close()     // Catch:{ all -> 0x014f }
            r8.captureSession = r2     // Catch:{ all -> 0x014f }
        L_0x0018:
            monitor-exit(r0)     // Catch:{ all -> 0x014f }
            if (r9 == 0) goto L_0x002d
            boolean r0 = r8.supports_photo_video_recording     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            if (r0 == 0) goto L_0x0029
            boolean r0 = r8.want_video_high_speed     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            if (r0 != 0) goto L_0x0029
            if (r10 == 0) goto L_0x0029
            r8.createPictureImageReader()     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            goto L_0x0030
        L_0x0029:
            r8.closePictureImageReader()     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            goto L_0x0030
        L_0x002d:
            r8.createPictureImageReader()     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
        L_0x0030:
            android.graphics.SurfaceTexture r0 = r8.texture     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            if (r0 == 0) goto L_0x0067
            int r0 = r8.preview_width     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            if (r0 == 0) goto L_0x0061
            int r0 = r8.preview_height     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            if (r0 == 0) goto L_0x0061
            android.graphics.SurfaceTexture r0 = r8.texture     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            int r1 = r8.preview_width     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            int r3 = r8.preview_height     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            r0.setDefaultBufferSize(r1, r3)     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            java.lang.Object r0 = r8.background_camera_lock     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            monitor-enter(r0)     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            android.view.Surface r1 = r8.surface_texture     // Catch:{ all -> 0x005e }
            if (r1 == 0) goto L_0x0053
            android.hardware.camera2.CaptureRequest$Builder r1 = r8.previewBuilder     // Catch:{ all -> 0x005e }
            android.view.Surface r3 = r8.surface_texture     // Catch:{ all -> 0x005e }
            r1.removeTarget(r3)     // Catch:{ all -> 0x005e }
        L_0x0053:
            android.view.Surface r1 = new android.view.Surface     // Catch:{ all -> 0x005e }
            android.graphics.SurfaceTexture r3 = r8.texture     // Catch:{ all -> 0x005e }
            r1.<init>(r3)     // Catch:{ all -> 0x005e }
            r8.surface_texture = r1     // Catch:{ all -> 0x005e }
            monitor-exit(r0)     // Catch:{ all -> 0x005e }
            goto L_0x0067
        L_0x005e:
            r9 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x005e }
            throw r9     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
        L_0x0061:
            java.lang.RuntimeException r9 = new java.lang.RuntimeException     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            r9.<init>()     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            throw r9     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
        L_0x0067:
            java.lang.Object r0 = r8.background_camera_lock     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            monitor-enter(r0)     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            if (r9 == 0) goto L_0x0073
            android.view.Surface r1 = r9.getSurface()     // Catch:{ all -> 0x0138 }
            r8.video_recorder_surface = r1     // Catch:{ all -> 0x0138 }
            goto L_0x0075
        L_0x0073:
            r8.video_recorder_surface = r2     // Catch:{ all -> 0x0138 }
        L_0x0075:
            monitor-exit(r0)     // Catch:{ all -> 0x0138 }
            net.sourceforge.opencamera.cameracontroller.CameraController2$2MyStateCallback r0 = new net.sourceforge.opencamera.cameracontroller.CameraController2$2MyStateCallback     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            r0.<init>(r9)     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            java.lang.Object r1 = r8.background_camera_lock     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            monitor-enter(r1)     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            android.view.Surface r2 = r8.getPreviewSurface()     // Catch:{ all -> 0x0135 }
            r3 = 3
            r4 = 2
            r5 = 1
            r6 = 0
            if (r9 == 0) goto L_0x00b4
            boolean r7 = r8.supports_photo_video_recording     // Catch:{ all -> 0x0135 }
            if (r7 == 0) goto L_0x00a7
            boolean r7 = r8.want_video_high_speed     // Catch:{ all -> 0x0135 }
            if (r7 != 0) goto L_0x00a7
            if (r10 == 0) goto L_0x00a7
            android.view.Surface[] r10 = new android.view.Surface[r3]     // Catch:{ all -> 0x0135 }
            r10[r6] = r2     // Catch:{ all -> 0x0135 }
            android.view.Surface r2 = r8.video_recorder_surface     // Catch:{ all -> 0x0135 }
            r10[r5] = r2     // Catch:{ all -> 0x0135 }
            android.media.ImageReader r2 = r8.imageReader     // Catch:{ all -> 0x0135 }
            android.view.Surface r2 = r2.getSurface()     // Catch:{ all -> 0x0135 }
            r10[r4] = r2     // Catch:{ all -> 0x0135 }
            java.util.List r10 = java.util.Arrays.asList(r10)     // Catch:{ all -> 0x0135 }
            goto L_0x00e1
        L_0x00a7:
            android.view.Surface[] r10 = new android.view.Surface[r4]     // Catch:{ all -> 0x0135 }
            r10[r6] = r2     // Catch:{ all -> 0x0135 }
            android.view.Surface r2 = r8.video_recorder_surface     // Catch:{ all -> 0x0135 }
            r10[r5] = r2     // Catch:{ all -> 0x0135 }
            java.util.List r10 = java.util.Arrays.asList(r10)     // Catch:{ all -> 0x0135 }
            goto L_0x00e1
        L_0x00b4:
            android.media.ImageReader r10 = r8.imageReaderRaw     // Catch:{ all -> 0x0135 }
            if (r10 == 0) goto L_0x00d1
            android.view.Surface[] r10 = new android.view.Surface[r3]     // Catch:{ all -> 0x0135 }
            r10[r6] = r2     // Catch:{ all -> 0x0135 }
            android.media.ImageReader r2 = r8.imageReader     // Catch:{ all -> 0x0135 }
            android.view.Surface r2 = r2.getSurface()     // Catch:{ all -> 0x0135 }
            r10[r5] = r2     // Catch:{ all -> 0x0135 }
            android.media.ImageReader r2 = r8.imageReaderRaw     // Catch:{ all -> 0x0135 }
            android.view.Surface r2 = r2.getSurface()     // Catch:{ all -> 0x0135 }
            r10[r4] = r2     // Catch:{ all -> 0x0135 }
            java.util.List r10 = java.util.Arrays.asList(r10)     // Catch:{ all -> 0x0135 }
            goto L_0x00e1
        L_0x00d1:
            android.view.Surface[] r10 = new android.view.Surface[r4]     // Catch:{ all -> 0x0135 }
            r10[r6] = r2     // Catch:{ all -> 0x0135 }
            android.media.ImageReader r2 = r8.imageReader     // Catch:{ all -> 0x0135 }
            android.view.Surface r2 = r2.getSurface()     // Catch:{ all -> 0x0135 }
            r10[r5] = r2     // Catch:{ all -> 0x0135 }
            java.util.List r10 = java.util.Arrays.asList(r10)     // Catch:{ all -> 0x0135 }
        L_0x00e1:
            monitor-exit(r1)     // Catch:{ all -> 0x0135 }
            if (r9 == 0) goto L_0x00f8
            boolean r9 = r8.want_video_high_speed     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            if (r9 == 0) goto L_0x00f8
            int r9 = android.os.Build.VERSION.SDK_INT     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            r1 = 23
            if (r9 < r1) goto L_0x00f8
            android.hardware.camera2.CameraDevice r9 = r8.camera     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            android.os.Handler r1 = r8.handler     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            r9.createConstrainedHighSpeedCaptureSession(r10, r0, r1)     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            r8.is_video_high_speed = r5     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            goto L_0x0101
        L_0x00f8:
            android.hardware.camera2.CameraDevice r9 = r8.camera     // Catch:{ NullPointerException -> 0x012b }
            android.os.Handler r1 = r8.handler     // Catch:{ NullPointerException -> 0x012b }
            r9.createCaptureSession(r10, r0, r1)     // Catch:{ NullPointerException -> 0x012b }
            r8.is_video_high_speed = r6     // Catch:{ NullPointerException -> 0x012b }
        L_0x0101:
            java.lang.Object r9 = r8.background_camera_lock     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            monitor-enter(r9)     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
        L_0x0104:
            boolean r10 = r0.callback_done     // Catch:{ all -> 0x0128 }
            if (r10 != 0) goto L_0x0115
            java.lang.Object r10 = r8.background_camera_lock     // Catch:{ InterruptedException -> 0x0110 }
            r10.wait()     // Catch:{ InterruptedException -> 0x0110 }
            goto L_0x0104
        L_0x0110:
            r10 = move-exception
            r10.printStackTrace()     // Catch:{ all -> 0x0128 }
            goto L_0x0104
        L_0x0115:
            monitor-exit(r9)     // Catch:{ all -> 0x0128 }
            java.lang.Object r9 = r8.background_camera_lock     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            monitor-enter(r9)     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            android.hardware.camera2.CameraCaptureSession r10 = r8.captureSession     // Catch:{ all -> 0x0125 }
            if (r10 == 0) goto L_0x011f
            monitor-exit(r9)     // Catch:{ all -> 0x0125 }
            return
        L_0x011f:
            net.sourceforge.opencamera.cameracontroller.CameraControllerException r10 = new net.sourceforge.opencamera.cameracontroller.CameraControllerException     // Catch:{ all -> 0x0125 }
            r10.<init>()     // Catch:{ all -> 0x0125 }
            throw r10     // Catch:{ all -> 0x0125 }
        L_0x0125:
            r10 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x0125 }
            throw r10     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
        L_0x0128:
            r10 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x0128 }
            throw r10     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
        L_0x012b:
            r9 = move-exception
            r9.printStackTrace()     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            net.sourceforge.opencamera.cameracontroller.CameraControllerException r9 = new net.sourceforge.opencamera.cameracontroller.CameraControllerException     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            r9.<init>()     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
            throw r9     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
        L_0x0135:
            r9 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0135 }
            throw r9     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
        L_0x0138:
            r9 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0138 }
            throw r9     // Catch:{ CameraAccessException -> 0x0145, IllegalArgumentException -> 0x013b }
        L_0x013b:
            r9 = move-exception
            r9.printStackTrace()
            net.sourceforge.opencamera.cameracontroller.CameraControllerException r9 = new net.sourceforge.opencamera.cameracontroller.CameraControllerException
            r9.<init>()
            throw r9
        L_0x0145:
            r9 = move-exception
            r9.printStackTrace()
            net.sourceforge.opencamera.cameracontroller.CameraControllerException r9 = new net.sourceforge.opencamera.cameracontroller.CameraControllerException
            r9.<init>()
            throw r9
        L_0x014f:
            r9 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x014f }
            throw r9
        L_0x0152:
            java.lang.RuntimeException r9 = new java.lang.RuntimeException
            r9.<init>()
            goto L_0x0159
        L_0x0158:
            throw r9
        L_0x0159:
            goto L_0x0158
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.createCaptureSession(android.media.MediaRecorder, boolean):void");
    }

    public void startPreview() throws CameraControllerException {
        synchronized (this.background_camera_lock) {
            if (this.captureSession != null) {
                try {
                    setRepeatingRequest();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                    throw new CameraControllerException();
                }
            } else {
                createCaptureSession(null, false);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003c, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stopPreview() {
        /*
            r3 = this;
            java.lang.Object r0 = r3.background_camera_lock
            monitor-enter(r0)
            android.hardware.camera2.CameraDevice r1 = r3.camera     // Catch:{ all -> 0x003d }
            if (r1 == 0) goto L_0x003b
            android.hardware.camera2.CameraCaptureSession r1 = r3.captureSession     // Catch:{ all -> 0x003d }
            if (r1 != 0) goto L_0x000c
            goto L_0x003b
        L_0x000c:
            android.hardware.camera2.CameraCaptureSession r1 = r3.captureSession     // Catch:{ IllegalStateException -> 0x0014 }
            r1.stopRepeating()     // Catch:{ IllegalStateException -> 0x0014 }
            goto L_0x0018
        L_0x0012:
            r1 = move-exception
            goto L_0x0021
        L_0x0014:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ CameraAccessException -> 0x0012 }
        L_0x0018:
            android.hardware.camera2.CameraCaptureSession r1 = r3.captureSession     // Catch:{ CameraAccessException -> 0x0012 }
            r1.close()     // Catch:{ CameraAccessException -> 0x0012 }
            r1 = 0
            r3.captureSession = r1     // Catch:{ CameraAccessException -> 0x0012 }
            goto L_0x0024
        L_0x0021:
            r1.printStackTrace()     // Catch:{ all -> 0x003d }
        L_0x0024:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r1 = r3.camera_settings     // Catch:{ all -> 0x003d }
            boolean r1 = r1.has_face_detect_mode     // Catch:{ all -> 0x003d }
            if (r1 == 0) goto L_0x0039
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r1 = r3.camera_settings     // Catch:{ all -> 0x003d }
            r2 = 0
            r1.has_face_detect_mode = r2     // Catch:{ all -> 0x003d }
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r1 = r3.camera_settings     // Catch:{ all -> 0x003d }
            android.hardware.camera2.CaptureRequest$Builder r2 = r3.previewBuilder     // Catch:{ all -> 0x003d }
            r1.setFaceDetectMode(r2)     // Catch:{ all -> 0x003d }
        L_0x0039:
            monitor-exit(r0)     // Catch:{ all -> 0x003d }
            return
        L_0x003b:
            monitor-exit(r0)     // Catch:{ all -> 0x003d }
            return
        L_0x003d:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x003d }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.stopPreview():void");
    }

    public boolean startFaceDetection() {
        if (this.previewBuilder.get(CaptureRequest.STATISTICS_FACE_DETECT_MODE) != null && ((Integer) this.previewBuilder.get(CaptureRequest.STATISTICS_FACE_DETECT_MODE)).intValue() != 0) {
            return false;
        }
        if (this.supports_face_detect_mode_full) {
            this.camera_settings.has_face_detect_mode = do_af_trigger_for_continuous;
            this.camera_settings.face_detect_mode = 2;
        } else if (this.supports_face_detect_mode_simple) {
            this.camera_settings.has_face_detect_mode = do_af_trigger_for_continuous;
            this.camera_settings.face_detect_mode = 1;
        } else {
            Log.e(TAG, "startFaceDetection() called but face detection not available");
            return false;
        }
        this.camera_settings.setFaceDetectMode(this.previewBuilder);
        this.camera_settings.setSceneMode(this.previewBuilder);
        try {
            setRepeatingRequest();
            return do_af_trigger_for_continuous;
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setFaceDetectionListener(FaceDetectionListener faceDetectionListener) {
        this.face_detection_listener = faceDetectionListener;
        this.last_faces_detected = -1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0085, code lost:
        if (fireAutoFlash() != false) goto L_0x0087;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00f1, code lost:
        if (r9 == null) goto L_0x00f6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00f3, code lost:
        r9.onAutoFocus(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00f6, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x008c A[Catch:{ CameraAccessException -> 0x00d8 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void autoFocus(net.sourceforge.opencamera.cameracontroller.CameraController.AutoFocusCallback r9, boolean r10) {
        /*
            r8 = this;
            java.lang.Object r0 = r8.background_camera_lock
            monitor-enter(r0)
            r1 = 0
            r8.fake_precapture_torch_focus_performed = r1     // Catch:{ all -> 0x00fc }
            android.hardware.camera2.CameraDevice r2 = r8.camera     // Catch:{ all -> 0x00fc }
            if (r2 == 0) goto L_0x00f7
            android.hardware.camera2.CameraCaptureSession r2 = r8.captureSession     // Catch:{ all -> 0x00fc }
            if (r2 != 0) goto L_0x0010
            goto L_0x00f7
        L_0x0010:
            android.hardware.camera2.CaptureRequest$Builder r2 = r8.previewBuilder     // Catch:{ all -> 0x00fc }
            android.hardware.camera2.CaptureRequest$Key r3 = android.hardware.camera2.CaptureRequest.CONTROL_AF_MODE     // Catch:{ all -> 0x00fc }
            java.lang.Object r2 = r2.get(r3)     // Catch:{ all -> 0x00fc }
            java.lang.Integer r2 = (java.lang.Integer) r2     // Catch:{ all -> 0x00fc }
            r3 = 1
            if (r2 != 0) goto L_0x0022
            r9.onAutoFocus(r3)     // Catch:{ all -> 0x00fc }
            monitor-exit(r0)     // Catch:{ all -> 0x00fc }
            return
        L_0x0022:
            boolean r4 = r8.use_fake_precapture_mode     // Catch:{ all -> 0x00fc }
            if (r4 == 0) goto L_0x0033
            int r2 = r2.intValue()     // Catch:{ all -> 0x00fc }
            r4 = 4
            if (r2 != r4) goto L_0x0033
            r8.capture_follows_autofocus_hint = r10     // Catch:{ all -> 0x00fc }
            r8.autofocus_cb = r9     // Catch:{ all -> 0x00fc }
            monitor-exit(r0)     // Catch:{ all -> 0x00fc }
            return
        L_0x0033:
            boolean r2 = r8.is_video_high_speed     // Catch:{ all -> 0x00fc }
            if (r2 == 0) goto L_0x003c
            r9.onAutoFocus(r3)     // Catch:{ all -> 0x00fc }
            monitor-exit(r0)     // Catch:{ all -> 0x00fc }
            return
        L_0x003c:
            android.hardware.camera2.CaptureRequest$Builder r2 = r8.previewBuilder     // Catch:{ all -> 0x00fc }
            r8.state = r3     // Catch:{ all -> 0x00fc }
            r4 = -1
            r8.precapture_state_change_time_ms = r4     // Catch:{ all -> 0x00fc }
            r8.capture_follows_autofocus_hint = r10     // Catch:{ all -> 0x00fc }
            r8.autofocus_cb = r9     // Catch:{ all -> 0x00fc }
            r9 = 0
            boolean r10 = r8.use_fake_precapture_mode     // Catch:{ CameraAccessException -> 0x00d8 }
            if (r10 == 0) goto L_0x00b7
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r10 = r8.camera_settings     // Catch:{ CameraAccessException -> 0x00d8 }
            boolean r10 = r10.has_iso     // Catch:{ CameraAccessException -> 0x00d8 }
            if (r10 != 0) goto L_0x00b7
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r10 = r8.camera_settings     // Catch:{ CameraAccessException -> 0x00d8 }
            java.lang.String r10 = r10.flash_value     // Catch:{ CameraAccessException -> 0x00d8 }
            java.lang.String r6 = "flash_auto"
            boolean r10 = r10.equals(r6)     // Catch:{ CameraAccessException -> 0x00d8 }
            if (r10 != 0) goto L_0x0081
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r10 = r8.camera_settings     // Catch:{ CameraAccessException -> 0x00d8 }
            java.lang.String r10 = r10.flash_value     // Catch:{ CameraAccessException -> 0x00d8 }
            java.lang.String r6 = "flash_frontscreen_auto"
            boolean r10 = r10.equals(r6)     // Catch:{ CameraAccessException -> 0x00d8 }
            if (r10 == 0) goto L_0x0072
            goto L_0x0081
        L_0x0072:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r10 = r8.camera_settings     // Catch:{ CameraAccessException -> 0x00d8 }
            java.lang.String r10 = r10.flash_value     // Catch:{ CameraAccessException -> 0x00d8 }
            java.lang.String r6 = "flash_on"
            boolean r10 = r10.equals(r6)     // Catch:{ CameraAccessException -> 0x00d8 }
            if (r10 == 0) goto L_0x0089
            goto L_0x0087
        L_0x0081:
            boolean r10 = r8.fireAutoFlash()     // Catch:{ CameraAccessException -> 0x00d8 }
            if (r10 == 0) goto L_0x0089
        L_0x0087:
            r10 = 1
            goto L_0x008a
        L_0x0089:
            r10 = 0
        L_0x008a:
            if (r10 == 0) goto L_0x00b7
            android.hardware.camera2.CaptureRequest$Key r10 = android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE     // Catch:{ CameraAccessException -> 0x00d8 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r3)     // Catch:{ CameraAccessException -> 0x00d8 }
            r2.set(r10, r6)     // Catch:{ CameraAccessException -> 0x00d8 }
            android.hardware.camera2.CaptureRequest$Key r10 = android.hardware.camera2.CaptureRequest.FLASH_MODE     // Catch:{ CameraAccessException -> 0x00d8 }
            r6 = 2
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ CameraAccessException -> 0x00d8 }
            r2.set(r10, r6)     // Catch:{ CameraAccessException -> 0x00d8 }
            int r10 = r8.test_fake_flash_focus     // Catch:{ CameraAccessException -> 0x00d8 }
            int r10 = r10 + r3
            r8.test_fake_flash_focus = r10     // Catch:{ CameraAccessException -> 0x00d8 }
            r8.fake_precapture_torch_focus_performed = r3     // Catch:{ CameraAccessException -> 0x00d8 }
            android.hardware.camera2.CaptureRequest r10 = r2.build()     // Catch:{ CameraAccessException -> 0x00d8 }
            r8.setRepeatingRequest(r10)     // Catch:{ CameraAccessException -> 0x00d8 }
            r6 = 200(0xc8, double:9.9E-322)
            java.lang.Thread.sleep(r6)     // Catch:{ InterruptedException -> 0x00b3 }
            goto L_0x00b7
        L_0x00b3:
            r10 = move-exception
            r10.printStackTrace()     // Catch:{ CameraAccessException -> 0x00d8 }
        L_0x00b7:
            android.hardware.camera2.CaptureRequest$Key r10 = android.hardware.camera2.CaptureRequest.CONTROL_AF_TRIGGER     // Catch:{ CameraAccessException -> 0x00d8 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r1)     // Catch:{ CameraAccessException -> 0x00d8 }
            r2.set(r10, r6)     // Catch:{ CameraAccessException -> 0x00d8 }
            android.hardware.camera2.CaptureRequest r10 = r2.build()     // Catch:{ CameraAccessException -> 0x00d8 }
            r8.setRepeatingRequest(r10)     // Catch:{ CameraAccessException -> 0x00d8 }
            android.hardware.camera2.CaptureRequest$Key r10 = android.hardware.camera2.CaptureRequest.CONTROL_AF_TRIGGER     // Catch:{ CameraAccessException -> 0x00d8 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ CameraAccessException -> 0x00d8 }
            r2.set(r10, r3)     // Catch:{ CameraAccessException -> 0x00d8 }
            android.hardware.camera2.CaptureRequest r10 = r2.build()     // Catch:{ CameraAccessException -> 0x00d8 }
            r8.capture(r10)     // Catch:{ CameraAccessException -> 0x00d8 }
            goto L_0x00e7
        L_0x00d8:
            r10 = move-exception
            r10.printStackTrace()     // Catch:{ all -> 0x00fc }
            r8.state = r1     // Catch:{ all -> 0x00fc }
            r8.precapture_state_change_time_ms = r4     // Catch:{ all -> 0x00fc }
            net.sourceforge.opencamera.cameracontroller.CameraController$AutoFocusCallback r10 = r8.autofocus_cb     // Catch:{ all -> 0x00fc }
            r8.autofocus_cb = r9     // Catch:{ all -> 0x00fc }
            r8.capture_follows_autofocus_hint = r1     // Catch:{ all -> 0x00fc }
            r9 = r10
        L_0x00e7:
            android.hardware.camera2.CaptureRequest$Key r10 = android.hardware.camera2.CaptureRequest.CONTROL_AF_TRIGGER     // Catch:{ all -> 0x00fc }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x00fc }
            r2.set(r10, r3)     // Catch:{ all -> 0x00fc }
            monitor-exit(r0)     // Catch:{ all -> 0x00fc }
            if (r9 == 0) goto L_0x00f6
            r9.onAutoFocus(r1)
        L_0x00f6:
            return
        L_0x00f7:
            r9.onAutoFocus(r1)     // Catch:{ all -> 0x00fc }
            monitor-exit(r0)     // Catch:{ all -> 0x00fc }
            return
        L_0x00fc:
            r9 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x00fc }
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.autoFocus(net.sourceforge.opencamera.cameracontroller.CameraController$AutoFocusCallback, boolean):void");
    }

    public void setCaptureFollowAutofocusHint(boolean z) {
        synchronized (this.background_camera_lock) {
            this.capture_follows_autofocus_hint = z;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0048, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void cancelAutoFocus() {
        /*
            r5 = this;
            java.lang.Object r0 = r5.background_camera_lock
            monitor-enter(r0)
            android.hardware.camera2.CameraDevice r1 = r5.camera     // Catch:{ all -> 0x0049 }
            if (r1 == 0) goto L_0x0047
            android.hardware.camera2.CameraCaptureSession r1 = r5.captureSession     // Catch:{ all -> 0x0049 }
            if (r1 != 0) goto L_0x000c
            goto L_0x0047
        L_0x000c:
            boolean r1 = r5.is_video_high_speed     // Catch:{ all -> 0x0049 }
            if (r1 == 0) goto L_0x0012
            monitor-exit(r0)     // Catch:{ all -> 0x0049 }
            return
        L_0x0012:
            android.hardware.camera2.CaptureRequest$Builder r1 = r5.previewBuilder     // Catch:{ all -> 0x0049 }
            android.hardware.camera2.CaptureRequest$Key r2 = android.hardware.camera2.CaptureRequest.CONTROL_AF_TRIGGER     // Catch:{ all -> 0x0049 }
            r3 = 2
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x0049 }
            r1.set(r2, r3)     // Catch:{ all -> 0x0049 }
            r5.capture()     // Catch:{ CameraAccessException -> 0x0022 }
            goto L_0x0026
        L_0x0022:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x0049 }
        L_0x0026:
            android.hardware.camera2.CaptureRequest$Builder r1 = r5.previewBuilder     // Catch:{ all -> 0x0049 }
            android.hardware.camera2.CaptureRequest$Key r2 = android.hardware.camera2.CaptureRequest.CONTROL_AF_TRIGGER     // Catch:{ all -> 0x0049 }
            r3 = 0
            java.lang.Integer r4 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x0049 }
            r1.set(r2, r4)     // Catch:{ all -> 0x0049 }
            r1 = 0
            r5.autofocus_cb = r1     // Catch:{ all -> 0x0049 }
            r5.capture_follows_autofocus_hint = r3     // Catch:{ all -> 0x0049 }
            r5.state = r3     // Catch:{ all -> 0x0049 }
            r1 = -1
            r5.precapture_state_change_time_ms = r1     // Catch:{ all -> 0x0049 }
            r5.setRepeatingRequest()     // Catch:{ CameraAccessException -> 0x0041 }
            goto L_0x0045
        L_0x0041:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x0049 }
        L_0x0045:
            monitor-exit(r0)     // Catch:{ all -> 0x0049 }
            return
        L_0x0047:
            monitor-exit(r0)     // Catch:{ all -> 0x0049 }
            return
        L_0x0049:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0049 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.cancelAutoFocus():void");
    }

    public void setContinuousFocusMoveCallback(ContinuousFocusMoveCallback continuousFocusMoveCallback) {
        this.continuous_focus_move_callback = continuousFocusMoveCallback;
    }

    public static double getScaleForExposureTime(long j, long j2, long j3, double d) {
        double d2 = (double) (j - j2);
        double d3 = (double) (j3 - j2);
        Double.isNaN(d2);
        Double.isNaN(d3);
        double d4 = d2 / d3;
        if (d4 < 0.0d) {
            d4 = 0.0d;
        } else if (d4 > 1.0d) {
            d4 = 1.0d;
        }
        return (1.0d - d4) + (d4 * d);
    }

    private void setManualExposureTime(Builder builder, long j) {
        Range range = (Range) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
        Range range2 = (Range) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
        if (range != null && range2 != null) {
            long longValue = ((Long) range.getLower()).longValue();
            long longValue2 = ((Long) range.getUpper()).longValue();
            if (j < longValue) {
                j = longValue;
            }
            if (j > longValue2) {
                j = longValue2;
            }
            builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(0));
            int i = 800;
            if (this.capture_result_has_iso) {
                i = this.capture_result_iso;
            }
            builder.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(Math.min(Math.max(i, ((Integer) range2.getLower()).intValue()), ((Integer) range2.getUpper()).intValue())));
            if (this.capture_result_has_frame_duration) {
                builder.set(CaptureRequest.SENSOR_FRAME_DURATION, Long.valueOf(this.capture_result_frame_duration));
            } else {
                builder.set(CaptureRequest.SENSOR_FRAME_DURATION, Long.valueOf(CameraController.EXPOSURE_TIME_DEFAULT));
            }
            builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf(j));
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x01b1, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x01b7  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0155  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x015e  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:73:0x0133=Splitter:B:73:0x0133, B:120:0x01bb=Splitter:B:120:0x01bb} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void takePictureAfterPrecapture() {
        /*
            r18 = this;
            r1 = r18
            boolean r0 = r1.previewIsVideoMode
            r2 = 0
            if (r0 != 0) goto L_0x0028
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r0 = r1.burst_type
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r3 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_EXPO
            if (r0 == r3) goto L_0x0024
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r0 = r1.burst_type
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r3 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_FOCUS
            if (r0 != r3) goto L_0x0014
            goto L_0x0024
        L_0x0014:
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r0 = r1.burst_type
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r3 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_NORMAL
            if (r0 == r3) goto L_0x0020
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r0 = r1.burst_type
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r3 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_CONTINUOUS
            if (r0 != r3) goto L_0x0028
        L_0x0020:
            r1.takePictureBurst(r2)
            return
        L_0x0024:
            r18.takePictureBurstBracketing()
            return
        L_0x0028:
            java.lang.Object r3 = r1.background_camera_lock
            monitor-enter(r3)
            android.hardware.camera2.CameraDevice r0 = r1.camera     // Catch:{ all -> 0x01bd }
            if (r0 == 0) goto L_0x01bb
            android.hardware.camera2.CameraCaptureSession r0 = r1.captureSession     // Catch:{ all -> 0x01bd }
            if (r0 != 0) goto L_0x0035
            goto L_0x01bb
        L_0x0035:
            r0 = 1
            r4 = 0
            android.hardware.camera2.CameraDevice r5 = r1.camera     // Catch:{ CameraAccessException -> 0x0140, IllegalStateException -> 0x0131 }
            boolean r6 = r1.previewIsVideoMode     // Catch:{ CameraAccessException -> 0x0140, IllegalStateException -> 0x0131 }
            r7 = 2
            if (r6 == 0) goto L_0x0040
            r6 = 4
            goto L_0x0041
        L_0x0040:
            r6 = 2
        L_0x0041:
            android.hardware.camera2.CaptureRequest$Builder r5 = r5.createCaptureRequest(r6)     // Catch:{ CameraAccessException -> 0x0140, IllegalStateException -> 0x0131 }
            android.hardware.camera2.CaptureRequest$Key r6 = android.hardware.camera2.CaptureRequest.CONTROL_CAPTURE_INTENT     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r7)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r5.set(r6, r8)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject r6 = new net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagType r8 = net.sourceforge.opencamera.cameracontroller.CameraController2.RequestTagType.CAPTURE     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r6.<init>(r8)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r5.setTag(r6)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r6 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r6.setupBuilder(r5, r0)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            boolean r6 = r1.use_fake_precapture_mode     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            if (r6 == 0) goto L_0x0084
            boolean r6 = r1.fake_precapture_torch_performed     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            if (r6 == 0) goto L_0x0084
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r6 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            boolean r6 = r6.has_iso     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            if (r6 != 0) goto L_0x0076
            android.hardware.camera2.CaptureRequest$Key r6 = android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r0)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r5.set(r6, r8)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
        L_0x0076:
            android.hardware.camera2.CaptureRequest$Key r6 = android.hardware.camera2.CaptureRequest.FLASH_MODE     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r5.set(r6, r7)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            int r6 = r1.test_fake_flash_photo     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            int r6 = r6 + r0
            r1.test_fake_flash_photo = r6     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
        L_0x0084:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r6 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            boolean r6 = r6.has_iso     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            if (r6 != 0) goto L_0x00e6
            boolean r6 = r1.optimise_ae_for_dro     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            if (r6 == 0) goto L_0x00e6
            boolean r6 = r1.capture_result_has_exposure_time     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            if (r6 == 0) goto L_0x00e6
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r6 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            java.lang.String r6 = r6.flash_value     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            java.lang.String r7 = "flash_off"
            boolean r6 = r6.equals(r7)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            if (r6 != 0) goto L_0x00be
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r6 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            java.lang.String r6 = r6.flash_value     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            java.lang.String r7 = "flash_auto"
            boolean r6 = r6.equals(r7)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            if (r6 != 0) goto L_0x00be
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r6 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            java.lang.String r6 = r6.flash_value     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            java.lang.String r7 = "flash_frontscreen_auto"
            boolean r6 = r6.equals(r7)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            if (r6 == 0) goto L_0x00e6
        L_0x00be:
            r6 = 4611686018427387904(0x4000000000000000, double:2.0)
            r8 = -4620693217682128896(0xbfe0000000000000, double:-0.5)
            double r16 = java.lang.Math.pow(r6, r8)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            long r6 = r1.capture_result_exposure_time     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r8 = 16666666(0xfe502a, double:8.234427E-317)
            int r10 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1))
            if (r10 > 0) goto L_0x00e6
            r12 = 16666666(0xfe502a, double:8.234427E-317)
            r14 = 8333333(0x7f2815, double:4.1172136E-317)
            r10 = r6
            double r8 = getScaleForExposureTime(r10, r12, r14, r16)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            double r6 = (double) r6
            java.lang.Double.isNaN(r6)
            double r6 = r6 * r8
            long r6 = (long) r6
            r1.modified_from_camera_settings = r0     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r1.setManualExposureTime(r5, r6)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
        L_0x00e6:
            int r6 = android.os.Build.VERSION.SDK_INT     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r7 = 26
            if (r6 < r7) goto L_0x00f5
            android.hardware.camera2.CaptureRequest$Key r6 = android.hardware.camera2.CaptureRequest.CONTROL_ENABLE_ZSL     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            java.lang.Boolean r7 = java.lang.Boolean.valueOf(r0)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r5.set(r6, r7)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
        L_0x00f5:
            r18.clearPending()     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            android.media.ImageReader r6 = r1.imageReader     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            android.view.Surface r6 = r6.getSurface()     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r5.addTarget(r6)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            android.media.ImageReader r6 = r1.imageReaderRaw     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            if (r6 == 0) goto L_0x010e
            android.media.ImageReader r6 = r1.imageReaderRaw     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            android.view.Surface r6 = r6.getSurface()     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r5.addTarget(r6)     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
        L_0x010e:
            r1.n_burst = r0     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r1.n_burst_taken = r2     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            int r6 = r1.n_burst     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r1.n_burst_total = r6     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            boolean r6 = r1.raw_todo     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            if (r6 == 0) goto L_0x011d
            int r6 = r1.n_burst     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            goto L_0x011e
        L_0x011d:
            r6 = 0
        L_0x011e:
            r1.n_burst_raw = r6     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r1.burst_single_request = r2     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            boolean r6 = r1.previewIsVideoMode     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            if (r6 != 0) goto L_0x012b
            android.hardware.camera2.CameraCaptureSession r6 = r1.captureSession     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
            r6.stopRepeating()     // Catch:{ CameraAccessException -> 0x012f, IllegalStateException -> 0x012d }
        L_0x012b:
            r6 = r5
            goto L_0x013e
        L_0x012d:
            r0 = move-exception
            goto L_0x0133
        L_0x012f:
            r0 = move-exception
            goto L_0x0142
        L_0x0131:
            r0 = move-exception
            r5 = r4
        L_0x0133:
            r0.printStackTrace()     // Catch:{ all -> 0x01bd }
            r1.jpeg_todo = r2     // Catch:{ all -> 0x01bd }
            r1.raw_todo = r2     // Catch:{ all -> 0x01bd }
            r1.picture_cb = r4     // Catch:{ all -> 0x01bd }
            r6 = r5
            r0 = 0
        L_0x013e:
            r5 = r4
            goto L_0x0152
        L_0x0140:
            r0 = move-exception
            r5 = r4
        L_0x0142:
            r0.printStackTrace()     // Catch:{ all -> 0x01bd }
            r1.jpeg_todo = r2     // Catch:{ all -> 0x01bd }
            r1.raw_todo = r2     // Catch:{ all -> 0x01bd }
            r1.picture_cb = r4     // Catch:{ all -> 0x01bd }
            net.sourceforge.opencamera.cameracontroller.CameraController$ErrorCallback r0 = r1.take_picture_error_cb     // Catch:{ all -> 0x01bd }
            r1.take_picture_error_cb = r4     // Catch:{ all -> 0x01bd }
            r6 = r5
            r5 = r0
            r0 = 0
        L_0x0152:
            monitor-exit(r3)     // Catch:{ all -> 0x01bd }
            if (r0 == 0) goto L_0x015c
            net.sourceforge.opencamera.cameracontroller.CameraController$PictureCallback r3 = r1.picture_cb
            if (r3 == 0) goto L_0x015c
            r3.onStarted()
        L_0x015c:
            if (r0 == 0) goto L_0x01b5
            java.lang.Object r3 = r1.background_camera_lock
            monitor-enter(r3)
            android.hardware.camera2.CameraDevice r0 = r1.camera     // Catch:{ all -> 0x01b2 }
            if (r0 == 0) goto L_0x01b0
            android.hardware.camera2.CameraCaptureSession r0 = r1.captureSession     // Catch:{ all -> 0x01b2 }
            if (r0 != 0) goto L_0x016a
            goto L_0x01b0
        L_0x016a:
            boolean r0 = r1.test_release_during_photo     // Catch:{ all -> 0x01b2 }
            if (r0 == 0) goto L_0x0185
            android.content.Context r0 = r1.context     // Catch:{ all -> 0x01b2 }
            android.app.Activity r0 = (android.app.Activity) r0     // Catch:{ all -> 0x01b2 }
            net.sourceforge.opencamera.cameracontroller.CameraController2$2 r7 = new net.sourceforge.opencamera.cameracontroller.CameraController2$2     // Catch:{ all -> 0x01b2 }
            r7.<init>()     // Catch:{ all -> 0x01b2 }
            r0.runOnUiThread(r7)     // Catch:{ all -> 0x01b2 }
            r7 = 1000(0x3e8, double:4.94E-321)
            java.lang.Thread.sleep(r7)     // Catch:{ InterruptedException -> 0x0180 }
            goto L_0x0185
        L_0x0180:
            r0 = move-exception
            r7 = r0
            r7.printStackTrace()     // Catch:{ all -> 0x01b2 }
        L_0x0185:
            android.hardware.camera2.CameraCaptureSession r0 = r1.captureSession     // Catch:{ CameraAccessException -> 0x01a1, IllegalStateException -> 0x0196 }
            android.hardware.camera2.CaptureRequest r6 = r6.build()     // Catch:{ CameraAccessException -> 0x01a1, IllegalStateException -> 0x0196 }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r7 = r1.previewCaptureCallback     // Catch:{ CameraAccessException -> 0x01a1, IllegalStateException -> 0x0196 }
            android.os.Handler r8 = r1.handler     // Catch:{ CameraAccessException -> 0x01a1, IllegalStateException -> 0x0196 }
            r0.capture(r6, r7, r8)     // Catch:{ CameraAccessException -> 0x01a1, IllegalStateException -> 0x0196 }
            r1.playSound(r2)     // Catch:{ CameraAccessException -> 0x01a1, IllegalStateException -> 0x0196 }
            goto L_0x01ae
        L_0x0196:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x01b2 }
            r1.jpeg_todo = r2     // Catch:{ all -> 0x01b2 }
            r1.raw_todo = r2     // Catch:{ all -> 0x01b2 }
            r1.picture_cb = r4     // Catch:{ all -> 0x01b2 }
            goto L_0x01ae
        L_0x01a1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x01b2 }
            r1.jpeg_todo = r2     // Catch:{ all -> 0x01b2 }
            r1.raw_todo = r2     // Catch:{ all -> 0x01b2 }
            r1.picture_cb = r4     // Catch:{ all -> 0x01b2 }
            net.sourceforge.opencamera.cameracontroller.CameraController$ErrorCallback r0 = r1.take_picture_error_cb     // Catch:{ all -> 0x01b2 }
            r5 = r0
        L_0x01ae:
            monitor-exit(r3)     // Catch:{ all -> 0x01b2 }
            goto L_0x01b5
        L_0x01b0:
            monitor-exit(r3)     // Catch:{ all -> 0x01b2 }
            return
        L_0x01b2:
            r0 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x01b2 }
            throw r0
        L_0x01b5:
            if (r5 == 0) goto L_0x01ba
            r5.onError()
        L_0x01ba:
            return
        L_0x01bb:
            monitor-exit(r3)     // Catch:{ all -> 0x01bd }
            return
        L_0x01bd:
            r0 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x01bd }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.takePictureAfterPrecapture():void");
    }

    public static List<Float> setupFocusBracketingDistances(float f, float f2, int i) {
        float f3;
        ArrayList arrayList = new ArrayList();
        float max = 1.0f / Math.max(f, 0.1f);
        float max2 = 1.0f / Math.max(f2, 0.1f);
        int i2 = 0;
        while (i2 < i) {
            if (i2 == 0) {
                f3 = f;
            } else {
                int i3 = i - 1;
                if (i2 == i3) {
                    f3 = f2;
                } else {
                    float log = (float) (1.0d - (Math.log((double) (i - (max > max2 ? i3 - i2 : i2))) / Math.log((double) i)));
                    if (max > max2) {
                        log = 1.0f - log;
                    }
                    f3 = 1.0f / (((1.0f - log) * max) + (log * max2));
                }
            }
            arrayList.add(Float.valueOf(f3));
            i2++;
        }
        return arrayList;
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [net.sourceforge.opencamera.cameracontroller.CameraController2] */
    /* JADX WARNING: type inference failed for: r1v1, types: [net.sourceforge.opencamera.cameracontroller.CameraController2] */
    /* JADX WARNING: type inference failed for: r1v2, types: [net.sourceforge.opencamera.cameracontroller.CameraController2] */
    /* JADX WARNING: type inference failed for: r1v3 */
    /* JADX WARNING: type inference failed for: r1v4 */
    /* JADX WARNING: type inference failed for: r1v5, types: [net.sourceforge.opencamera.cameracontroller.CameraController2] */
    /* JADX WARNING: type inference failed for: r1v6 */
    /* JADX WARNING: type inference failed for: r1v7, types: [net.sourceforge.opencamera.cameracontroller.CameraController2] */
    /* JADX WARNING: type inference failed for: r1v13, types: [net.sourceforge.opencamera.cameracontroller.CameraController2] */
    /* JADX WARNING: type inference failed for: r1v21, types: [int] */
    /* JADX WARNING: type inference failed for: r1v26 */
    /* JADX WARNING: type inference failed for: r1v27 */
    /* JADX WARNING: type inference failed for: r1v28 */
    /* JADX WARNING: type inference failed for: r1v29 */
    /* JADX WARNING: type inference failed for: r1v30 */
    /* JADX WARNING: type inference failed for: r1v31 */
    /* JADX WARNING: type inference failed for: r1v32 */
    /* JADX WARNING: type inference failed for: r1v33 */
    /* JADX WARNING: type inference failed for: r1v34 */
    /* JADX WARNING: type inference failed for: r1v35 */
    /* JADX WARNING: type inference failed for: r1v36 */
    /* JADX WARNING: type inference failed for: r1v37 */
    /* JADX WARNING: type inference failed for: r1v38 */
    /* JADX WARNING: type inference failed for: r1v39 */
    /* JADX WARNING: type inference failed for: r1v40 */
    /* JADX WARNING: type inference failed for: r1v41 */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x0374, code lost:
        return;
     */
    /* JADX WARNING: Multi-variable type inference failed. Error: jadx.core.utils.exceptions.JadxRuntimeException: No candidate types for var: r1v3
      assigns: []
      uses: []
      mth insns count: 401
    	at jadx.core.dex.visitors.typeinference.TypeSearch.fillTypeCandidates(TypeSearch.java:237)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    	at jadx.core.dex.visitors.typeinference.TypeSearch.run(TypeSearch.java:53)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runMultiVariableSearch(TypeInferenceVisitor.java:99)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:92)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
    	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
    	at jadx.core.ProcessClass.process(ProcessClass.java:30)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:311)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:217)
     */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x030a  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0313  */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x037b  */
    /* JADX WARNING: Unknown variable types count: 10 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void takePictureBurstBracketing() {
        /*
            r20 = this;
            r1 = r20
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r2 = r1.burst_type
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r3 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_EXPO
            if (r2 == r3) goto L_0x0026
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r2 = r1.burst_type
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r3 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_FOCUS
            if (r2 == r3) goto L_0x0026
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "takePictureBurstBracketing called but unexpected burst_type: "
            r2.append(r3)
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r3 = r1.burst_type
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            java.lang.String r3 = "CameraController2"
            android.util.Log.e(r3, r2)
        L_0x0026:
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            java.lang.Object r3 = r1.background_camera_lock
            monitor-enter(r3)
            android.hardware.camera2.CameraDevice r4 = r1.camera     // Catch:{ all -> 0x0381 }
            if (r4 == 0) goto L_0x037f
            android.hardware.camera2.CameraCaptureSession r4 = r1.captureSession     // Catch:{ all -> 0x0381 }
            if (r4 != 0) goto L_0x0038
            goto L_0x037f
        L_0x0038:
            r5 = 1
            r6 = 0
            android.hardware.camera2.CameraDevice r7 = r1.camera     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            boolean r8 = r1.previewIsVideoMode     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r9 = 2
            if (r8 == 0) goto L_0x0043
            r8 = 4
            goto L_0x0044
        L_0x0043:
            r8 = 2
        L_0x0044:
            android.hardware.camera2.CaptureRequest$Builder r7 = r7.createCaptureRequest(r8)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            android.hardware.camera2.CaptureRequest$Key r8 = android.hardware.camera2.CaptureRequest.CONTROL_CAPTURE_INTENT     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r9)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.set(r8, r10)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r8.setupBuilder(r7, r5)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r20.clearPending()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            android.media.ImageReader r8 = r1.imageReader     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            android.view.Surface r8 = r8.getSurface()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.addTarget(r8)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            boolean r8 = r1.raw_todo     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r8 == 0) goto L_0x006f
            android.media.ImageReader r8 = r1.imageReaderRaw     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            android.view.Surface r8 = r8.getSurface()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.addTarget(r8)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
        L_0x006f:
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r8 = r1.burst_type     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r10 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_EXPO     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r8 != r10) goto L_0x020b
            android.hardware.camera2.CaptureRequest$Key r8 = android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r6)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.set(r8, r10)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            boolean r8 = r1.use_fake_precapture_mode     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r8 == 0) goto L_0x0094
            boolean r8 = r1.fake_precapture_torch_performed     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r8 == 0) goto L_0x0094
            android.hardware.camera2.CaptureRequest$Key r8 = android.hardware.camera2.CaptureRequest.FLASH_MODE     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r9)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.set(r8, r10)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r8 = r1.test_fake_flash_photo     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r8 = r8 + r5
            r1.test_fake_flash_photo = r8     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
        L_0x0094:
            android.hardware.camera2.CameraCharacteristics r8 = r1.characteristics     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            android.hardware.camera2.CameraCharacteristics$Key r10 = android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Object r8 = r8.get(r10)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            android.util.Range r8 = (android.util.Range) r8     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r8 != 0) goto L_0x00a8
            java.lang.String r8 = "CameraController2"
            java.lang.String r10 = "takePictureBurstBracketing called but null iso_range"
            android.util.Log.e(r8, r10)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            goto L_0x00e4
        L_0x00a8:
            r10 = 800(0x320, float:1.121E-42)
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r11 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            boolean r11 = r11.has_iso     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r11 == 0) goto L_0x00b9
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r10 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r10 = r10.iso     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            goto L_0x00bf
        L_0x00b9:
            boolean r11 = r1.capture_result_has_iso     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r11 == 0) goto L_0x00bf
            int r10 = r1.capture_result_iso     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
        L_0x00bf:
            java.lang.Comparable r11 = r8.getLower()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Integer r11 = (java.lang.Integer) r11     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r11 = r11.intValue()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r10 = java.lang.Math.max(r10, r11)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Comparable r8 = r8.getUpper()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Integer r8 = (java.lang.Integer) r8     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r8 = r8.intValue()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r8 = java.lang.Math.min(r10, r8)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            android.hardware.camera2.CaptureRequest$Key r10 = android.hardware.camera2.CaptureRequest.SENSOR_SENSITIVITY     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.set(r10, r8)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
        L_0x00e4:
            boolean r8 = r1.capture_result_has_frame_duration     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r10 = 33333333(0x1fca055, double:1.64688547E-316)
            if (r8 == 0) goto L_0x00f7
            android.hardware.camera2.CaptureRequest$Key r8 = android.hardware.camera2.CaptureRequest.SENSOR_FRAME_DURATION     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            long r12 = r1.capture_result_frame_duration     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Long r12 = java.lang.Long.valueOf(r12)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.set(r8, r12)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            goto L_0x0100
        L_0x00f7:
            android.hardware.camera2.CaptureRequest$Key r8 = android.hardware.camera2.CaptureRequest.SENSOR_FRAME_DURATION     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Long r12 = java.lang.Long.valueOf(r10)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.set(r8, r12)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
        L_0x0100:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            boolean r8 = r8.has_iso     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r8 == 0) goto L_0x010f
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            long r10 = r8.exposure_time     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            goto L_0x0115
        L_0x010f:
            boolean r8 = r1.capture_result_has_exposure_time     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r8 == 0) goto L_0x0115
            long r10 = r1.capture_result_exposure_time     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
        L_0x0115:
            int r8 = r1.expo_bracketing_n_images     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r8 = r8 / r9
            r12 = 4611686018427387904(0x4000000000000000, double:2.0)
            double r14 = r1.expo_bracketing_stops     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            double r5 = (double) r8
            java.lang.Double.isNaN(r5)
            double r14 = r14 / r5
            double r5 = java.lang.Math.pow(r12, r14)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            android.hardware.camera2.CameraCharacteristics r9 = r1.characteristics     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            android.hardware.camera2.CameraCharacteristics$Key r12 = android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Object r9 = r9.get(r12)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            android.util.Range r9 = (android.util.Range) r9     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r9 == 0) goto L_0x0146
            java.lang.Comparable r12 = r9.getLower()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Long r12 = (java.lang.Long) r12     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            long r12 = r12.longValue()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Comparable r14 = r9.getUpper()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Long r14 = (java.lang.Long) r14     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            long r14 = r14.longValue()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            goto L_0x0148
        L_0x0146:
            r12 = r10
            r14 = r12
        L_0x0148:
            r4 = 0
        L_0x0149:
            if (r4 >= r8) goto L_0x018e
            if (r9 == 0) goto L_0x0185
            r1 = r4
            r16 = r5
        L_0x0150:
            r18 = r14
            int r14 = r8 + -1
            if (r1 >= r14) goto L_0x015d
            double r16 = r16 * r5
            int r1 = r1 + 1
            r14 = r18
            goto L_0x0150
        L_0x015d:
            double r14 = (double) r10
            java.lang.Double.isNaN(r14)
            double r14 = r14 / r16
            long r14 = (long) r14
            int r1 = (r14 > r12 ? 1 : (r14 == r12 ? 0 : -1))
            if (r1 >= 0) goto L_0x0169
            r14 = r12
        L_0x0169:
            android.hardware.camera2.CaptureRequest$Key r1 = android.hardware.camera2.CaptureRequest.SENSOR_EXPOSURE_TIME     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            java.lang.Long r14 = java.lang.Long.valueOf(r14)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r7.set(r1, r14)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject r1 = new net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagType r14 = net.sourceforge.opencamera.cameracontroller.CameraController2.RequestTagType.CAPTURE_BURST_IN_PROGRESS     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r15 = 0
            r1.<init>(r14)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r7.setTag(r1)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            android.hardware.camera2.CaptureRequest r1 = r7.build()     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r2.add(r1)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            goto L_0x0187
        L_0x0185:
            r18 = r14
        L_0x0187:
            int r4 = r4 + 1
            r1 = r20
            r14 = r18
            goto L_0x0149
        L_0x018e:
            r18 = r14
            android.hardware.camera2.CaptureRequest$Key r1 = android.hardware.camera2.CaptureRequest.SENSOR_EXPOSURE_TIME     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            java.lang.Long r4 = java.lang.Long.valueOf(r10)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r7.set(r1, r4)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject r1 = new net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagType r4 = net.sourceforge.opencamera.cameracontroller.CameraController2.RequestTagType.CAPTURE_BURST_IN_PROGRESS     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r12 = 0
            r1.<init>(r4)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r7.setTag(r1)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            android.hardware.camera2.CaptureRequest r1 = r7.build()     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r2.add(r1)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r1 = 0
        L_0x01ac:
            if (r1 >= r8) goto L_0x01f4
            if (r9 == 0) goto L_0x01f1
            r12 = r5
            r4 = 0
        L_0x01b2:
            if (r4 >= r1) goto L_0x01b9
            double r12 = r12 * r5
            int r4 = r4 + 1
            goto L_0x01b2
        L_0x01b9:
            double r14 = (double) r10
            java.lang.Double.isNaN(r14)
            double r14 = r14 * r12
            long r14 = (long) r14
            int r4 = (r14 > r18 ? 1 : (r14 == r18 ? 0 : -1))
            if (r4 <= 0) goto L_0x01c6
            r14 = r18
        L_0x01c6:
            android.hardware.camera2.CaptureRequest$Key r4 = android.hardware.camera2.CaptureRequest.SENSOR_EXPOSURE_TIME     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            java.lang.Long r12 = java.lang.Long.valueOf(r14)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r7.set(r4, r12)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            int r4 = r8 + -1
            if (r1 != r4) goto L_0x01df
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject r4 = new net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagType r12 = net.sourceforge.opencamera.cameracontroller.CameraController2.RequestTagType.CAPTURE     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r13 = 0
            r4.<init>(r12)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r7.setTag(r4)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            goto L_0x01ea
        L_0x01df:
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject r4 = new net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagType r12 = net.sourceforge.opencamera.cameracontroller.CameraController2.RequestTagType.CAPTURE_BURST_IN_PROGRESS     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r13 = 0
            r4.<init>(r12)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r7.setTag(r4)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
        L_0x01ea:
            android.hardware.camera2.CaptureRequest r4 = r7.build()     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
            r2.add(r4)     // Catch:{ CameraAccessException -> 0x0206, IllegalStateException -> 0x0201, all -> 0x01fc }
        L_0x01f1:
            int r1 = r1 + 1
            goto L_0x01ac
        L_0x01f4:
            r4 = 1
            r1 = r20
            r1.burst_single_request = r4     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r4 = 0
            goto L_0x02c5
        L_0x01fc:
            r0 = move-exception
            r1 = r20
            goto L_0x0382
        L_0x0201:
            r0 = move-exception
            r1 = r20
            goto L_0x02e7
        L_0x0206:
            r0 = move-exception
            r1 = r20
            goto L_0x02f7
        L_0x020b:
            boolean r4 = r1.use_fake_precapture_mode     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r4 == 0) goto L_0x0234
            boolean r4 = r1.fake_precapture_torch_performed     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r4 == 0) goto L_0x0234
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r4 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            boolean r4 = r4.has_iso     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r4 != 0) goto L_0x0225
            android.hardware.camera2.CaptureRequest$Key r4 = android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r5 = 1
            java.lang.Integer r6 = java.lang.Integer.valueOf(r5)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.set(r4, r6)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
        L_0x0225:
            android.hardware.camera2.CaptureRequest$Key r4 = android.hardware.camera2.CaptureRequest.FLASH_MODE     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r9)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.set(r4, r5)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r4 = r1.test_fake_flash_photo     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r5 = 1
            int r4 = r4 + r5
            r1.test_fake_flash_photo = r4     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
        L_0x0234:
            android.hardware.camera2.CaptureRequest$Key r4 = android.hardware.camera2.CaptureRequest.CONTROL_AF_MODE     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r5)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.set(r4, r6)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r4 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            float r4 = r4.focus_distance     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            float r5 = r1.focus_bracketing_source_distance     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            float r4 = r4 - r5
            float r4 = java.lang.Math.abs(r4)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            double r4 = (double) r4     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r8 = 4532020583610935537(0x3ee4f8b588e368f1, double:1.0E-5)
            int r6 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1))
            if (r6 >= 0) goto L_0x0256
            goto L_0x0270
        L_0x0256:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r4 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            float r4 = r4.focus_distance     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            float r5 = r1.focus_bracketing_target_distance     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            float r4 = r4 - r5
            float r4 = java.lang.Math.abs(r4)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            double r4 = (double) r4     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r6 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1))
            if (r6 >= 0) goto L_0x0269
            goto L_0x0270
        L_0x0269:
            java.lang.String r4 = "CameraController2"
            java.lang.String r5 = "current focus matches neither source nor target"
            android.util.Log.d(r4, r5)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
        L_0x0270:
            float r4 = r1.focus_bracketing_source_distance     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            float r5 = r1.focus_bracketing_target_distance     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r6 = r1.focus_bracketing_n_images     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.util.List r4 = setupFocusBracketingDistances(r4, r5, r6)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            boolean r5 = r1.focus_bracketing_add_infinity     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r5 == 0) goto L_0x0286
            r5 = 0
            java.lang.Float r5 = java.lang.Float.valueOf(r5)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r4.add(r5)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
        L_0x0286:
            r5 = 0
        L_0x0287:
            int r6 = r4.size()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r5 >= r6) goto L_0x02c2
            android.hardware.camera2.CaptureRequest$Key r6 = android.hardware.camera2.CaptureRequest.LENS_FOCUS_DISTANCE     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            java.lang.Object r8 = r4.get(r5)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.set(r6, r8)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r6 = r4.size()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r8 = 1
            int r6 = r6 - r8
            if (r5 != r6) goto L_0x02aa
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject r6 = new net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagType r8 = net.sourceforge.opencamera.cameracontroller.CameraController2.RequestTagType.CAPTURE     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r9 = 0
            r6.<init>(r8)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.setTag(r6)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            goto L_0x02b5
        L_0x02aa:
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject r6 = new net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagType r8 = net.sourceforge.opencamera.cameracontroller.CameraController2.RequestTagType.CAPTURE_BURST_IN_PROGRESS     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r9 = 0
            r6.<init>(r8)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r7.setTag(r6)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
        L_0x02b5:
            android.hardware.camera2.CaptureRequest r6 = r7.build()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r2.add(r6)     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r6 = 1
            r1.focus_bracketing_in_progress = r6     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r5 = r5 + 1
            goto L_0x0287
        L_0x02c2:
            r4 = 0
            r1.burst_single_request = r4     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
        L_0x02c5:
            int r5 = r2.size()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r1.n_burst = r5     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            int r5 = r1.n_burst     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r1.n_burst_total = r5     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r1.n_burst_taken = r4     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            boolean r4 = r1.raw_todo     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r4 == 0) goto L_0x02d8
            int r6 = r1.n_burst     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            goto L_0x02d9
        L_0x02d8:
            r6 = 0
        L_0x02d9:
            r1.n_burst_raw = r6     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            boolean r4 = r1.previewIsVideoMode     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            if (r4 != 0) goto L_0x02e4
            android.hardware.camera2.CameraCaptureSession r4 = r1.captureSession     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
            r4.stopRepeating()     // Catch:{ CameraAccessException -> 0x02f6, IllegalStateException -> 0x02e6 }
        L_0x02e4:
            r4 = 1
            goto L_0x02f4
        L_0x02e6:
            r0 = move-exception
        L_0x02e7:
            r4 = r0
            r4.printStackTrace()     // Catch:{ all -> 0x0381 }
            r4 = 0
            r1.jpeg_todo = r4     // Catch:{ all -> 0x0381 }
            r1.raw_todo = r4     // Catch:{ all -> 0x0381 }
            r4 = 0
            r1.picture_cb = r4     // Catch:{ all -> 0x0381 }
            r4 = 0
        L_0x02f4:
            r5 = 0
            goto L_0x0307
        L_0x02f6:
            r0 = move-exception
        L_0x02f7:
            r4 = r0
            r4.printStackTrace()     // Catch:{ all -> 0x0381 }
            r4 = 0
            r1.jpeg_todo = r4     // Catch:{ all -> 0x0381 }
            r1.raw_todo = r4     // Catch:{ all -> 0x0381 }
            r4 = 0
            r1.picture_cb = r4     // Catch:{ all -> 0x0381 }
            net.sourceforge.opencamera.cameracontroller.CameraController$ErrorCallback r4 = r1.take_picture_error_cb     // Catch:{ all -> 0x0381 }
            r5 = r4
            r4 = 0
        L_0x0307:
            monitor-exit(r3)     // Catch:{ all -> 0x0381 }
            if (r4 == 0) goto L_0x0311
            net.sourceforge.opencamera.cameracontroller.CameraController$PictureCallback r3 = r1.picture_cb
            if (r3 == 0) goto L_0x0311
            r3.onStarted()
        L_0x0311:
            if (r4 == 0) goto L_0x0379
            java.lang.Object r3 = r1.background_camera_lock
            monitor-enter(r3)
            android.hardware.camera2.CameraDevice r4 = r1.camera     // Catch:{ all -> 0x0375 }
            if (r4 == 0) goto L_0x0373
            android.hardware.camera2.CameraCaptureSession r4 = r1.captureSession     // Catch:{ all -> 0x0375 }
            if (r4 != 0) goto L_0x031f
            goto L_0x0373
        L_0x031f:
            r4 = 1
            r1.modified_from_camera_settings = r4     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            boolean r4 = r1.use_expo_fast_burst     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            if (r4 == 0) goto L_0x0337
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r4 = r1.burst_type     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r6 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_EXPO     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            if (r4 != r6) goto L_0x0337
            android.hardware.camera2.CameraCaptureSession r4 = r1.captureSession     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r6 = r1.previewCaptureCallback     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            android.os.Handler r7 = r1.handler     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            r4.captureBurst(r2, r6, r7)     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            r6 = 0
            goto L_0x034f
        L_0x0337:
            r1.slow_burst_capture_requests = r2     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            long r6 = java.lang.System.currentTimeMillis()     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            r1.slow_burst_start_ms = r6     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            android.hardware.camera2.CameraCaptureSession r4 = r1.captureSession     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            r6 = 0
            java.lang.Object r2 = r2.get(r6)     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            android.hardware.camera2.CaptureRequest r2 = (android.hardware.camera2.CaptureRequest) r2     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r7 = r1.previewCaptureCallback     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            android.os.Handler r8 = r1.handler     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            r4.capture(r2, r7, r8)     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
        L_0x034f:
            r1.playSound(r6)     // Catch:{ CameraAccessException -> 0x0361, IllegalStateException -> 0x0353 }
            goto L_0x0371
        L_0x0353:
            r0 = move-exception
            r2 = r0
            r2.printStackTrace()     // Catch:{ all -> 0x0375 }
            r2 = 0
            r1.jpeg_todo = r2     // Catch:{ all -> 0x0375 }
            r1.raw_todo = r2     // Catch:{ all -> 0x0375 }
            r2 = 0
            r1.picture_cb = r2     // Catch:{ all -> 0x0375 }
            goto L_0x0371
        L_0x0361:
            r0 = move-exception
            r2 = r0
            r2.printStackTrace()     // Catch:{ all -> 0x0375 }
            r2 = 0
            r1.jpeg_todo = r2     // Catch:{ all -> 0x0375 }
            r1.raw_todo = r2     // Catch:{ all -> 0x0375 }
            r2 = 0
            r1.picture_cb = r2     // Catch:{ all -> 0x0375 }
            net.sourceforge.opencamera.cameracontroller.CameraController$ErrorCallback r2 = r1.take_picture_error_cb     // Catch:{ all -> 0x0375 }
            r5 = r2
        L_0x0371:
            monitor-exit(r3)     // Catch:{ all -> 0x0375 }
            goto L_0x0379
        L_0x0373:
            monitor-exit(r3)     // Catch:{ all -> 0x0375 }
            return
        L_0x0375:
            r0 = move-exception
            r2 = r0
            monitor-exit(r3)     // Catch:{ all -> 0x0375 }
            throw r2
        L_0x0379:
            if (r5 == 0) goto L_0x037e
            r5.onError()
        L_0x037e:
            return
        L_0x037f:
            monitor-exit(r3)     // Catch:{ all -> 0x0381 }
            return
        L_0x0381:
            r0 = move-exception
        L_0x0382:
            r2 = r0
            monitor-exit(r3)     // Catch:{ all -> 0x0381 }
            goto L_0x0386
        L_0x0385:
            throw r2
        L_0x0386:
            goto L_0x0385
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.takePictureBurstBracketing():void");
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x023c, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x01cf  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0242  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0170 A[Catch:{ CameraAccessException -> 0x01ac }] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0181 A[Catch:{ CameraAccessException -> 0x01ac }] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0184 A[Catch:{ CameraAccessException -> 0x01ac }] */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x01c4  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:95:0x01b2=Splitter:B:95:0x01b2, B:145:0x0246=Splitter:B:145:0x0246} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void takePictureBurst(boolean r19) {
        /*
            r18 = this;
            r1 = r18
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r0 = r1.burst_type
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r2 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_NORMAL
            if (r0 == r2) goto L_0x0026
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r0 = r1.burst_type
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r2 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_CONTINUOUS
            if (r0 == r2) goto L_0x0026
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "takePictureBurstBracketing called but unexpected burst_type: "
            r0.append(r2)
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r2 = r1.burst_type
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            java.lang.String r2 = "CameraController2"
            android.util.Log.e(r2, r0)
        L_0x0026:
            java.lang.Object r2 = r1.background_camera_lock
            monitor-enter(r2)
            android.hardware.camera2.CameraDevice r0 = r1.camera     // Catch:{ all -> 0x0248 }
            if (r0 == 0) goto L_0x0246
            android.hardware.camera2.CameraCaptureSession r0 = r1.captureSession     // Catch:{ all -> 0x0248 }
            if (r0 != 0) goto L_0x0033
            goto L_0x0246
        L_0x0033:
            r3 = 0
            r4 = 1
            r5 = 0
            android.hardware.camera2.CameraDevice r0 = r1.camera     // Catch:{ CameraAccessException -> 0x01af }
            boolean r6 = r1.previewIsVideoMode     // Catch:{ CameraAccessException -> 0x01af }
            r7 = 4
            r8 = 2
            if (r6 == 0) goto L_0x0040
            r6 = 4
            goto L_0x0041
        L_0x0040:
            r6 = 2
        L_0x0041:
            android.hardware.camera2.CaptureRequest$Builder r0 = r0.createCaptureRequest(r6)     // Catch:{ CameraAccessException -> 0x01af }
            android.hardware.camera2.CaptureRequest$Key r6 = android.hardware.camera2.CaptureRequest.CONTROL_CAPTURE_INTENT     // Catch:{ CameraAccessException -> 0x01af }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r8)     // Catch:{ CameraAccessException -> 0x01af }
            r0.set(r6, r9)     // Catch:{ CameraAccessException -> 0x01af }
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r6 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x01af }
            r6.setupBuilder(r0, r4)     // Catch:{ CameraAccessException -> 0x01af }
            boolean r6 = r1.use_fake_precapture_mode     // Catch:{ CameraAccessException -> 0x01af }
            if (r6 == 0) goto L_0x007a
            boolean r6 = r1.fake_precapture_torch_performed     // Catch:{ CameraAccessException -> 0x01af }
            if (r6 == 0) goto L_0x007a
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r6 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x01af }
            boolean r6 = r6.has_iso     // Catch:{ CameraAccessException -> 0x01af }
            if (r6 != 0) goto L_0x006c
            android.hardware.camera2.CaptureRequest$Key r6 = android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE     // Catch:{ CameraAccessException -> 0x01af }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r4)     // Catch:{ CameraAccessException -> 0x01af }
            r0.set(r6, r9)     // Catch:{ CameraAccessException -> 0x01af }
        L_0x006c:
            android.hardware.camera2.CaptureRequest$Key r6 = android.hardware.camera2.CaptureRequest.FLASH_MODE     // Catch:{ CameraAccessException -> 0x01af }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r8)     // Catch:{ CameraAccessException -> 0x01af }
            r0.set(r6, r9)     // Catch:{ CameraAccessException -> 0x01af }
            int r6 = r1.test_fake_flash_photo     // Catch:{ CameraAccessException -> 0x01af }
            int r6 = r6 + r4
            r1.test_fake_flash_photo = r6     // Catch:{ CameraAccessException -> 0x01af }
        L_0x007a:
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r6 = r1.burst_type     // Catch:{ CameraAccessException -> 0x01af }
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r9 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_NORMAL     // Catch:{ CameraAccessException -> 0x01af }
            if (r6 != r9) goto L_0x009f
            boolean r6 = r1.burst_for_noise_reduction     // Catch:{ CameraAccessException -> 0x01af }
            if (r6 == 0) goto L_0x009f
            android.hardware.camera2.CaptureRequest$Key r6 = android.hardware.camera2.CaptureRequest.NOISE_REDUCTION_MODE     // Catch:{ CameraAccessException -> 0x01af }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r5)     // Catch:{ CameraAccessException -> 0x01af }
            r0.set(r6, r9)     // Catch:{ CameraAccessException -> 0x01af }
            android.hardware.camera2.CaptureRequest$Key r6 = android.hardware.camera2.CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE     // Catch:{ CameraAccessException -> 0x01af }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r5)     // Catch:{ CameraAccessException -> 0x01af }
            r0.set(r6, r9)     // Catch:{ CameraAccessException -> 0x01af }
            android.hardware.camera2.CaptureRequest$Key r6 = android.hardware.camera2.CaptureRequest.EDGE_MODE     // Catch:{ CameraAccessException -> 0x01af }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r5)     // Catch:{ CameraAccessException -> 0x01af }
            r0.set(r6, r9)     // Catch:{ CameraAccessException -> 0x01af }
        L_0x009f:
            if (r19 != 0) goto L_0x00a4
            r18.clearPending()     // Catch:{ CameraAccessException -> 0x01af }
        L_0x00a4:
            android.media.ImageReader r6 = r1.imageReader     // Catch:{ CameraAccessException -> 0x01af }
            android.view.Surface r6 = r6.getSurface()     // Catch:{ CameraAccessException -> 0x01af }
            r0.addTarget(r6)     // Catch:{ CameraAccessException -> 0x01af }
            boolean r6 = r1.use_fake_precapture_mode     // Catch:{ CameraAccessException -> 0x01af }
            if (r6 == 0) goto L_0x00c3
            boolean r6 = r1.fake_precapture_torch_performed     // Catch:{ CameraAccessException -> 0x01af }
            if (r6 == 0) goto L_0x00c3
            android.hardware.camera2.CaptureRequest$Key r6 = android.hardware.camera2.CaptureRequest.FLASH_MODE     // Catch:{ CameraAccessException -> 0x01af }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ CameraAccessException -> 0x01af }
            r0.set(r6, r8)     // Catch:{ CameraAccessException -> 0x01af }
            int r6 = r1.test_fake_flash_photo     // Catch:{ CameraAccessException -> 0x01af }
            int r6 = r6 + r4
            r1.test_fake_flash_photo = r6     // Catch:{ CameraAccessException -> 0x01af }
        L_0x00c3:
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r6 = r1.burst_type     // Catch:{ CameraAccessException -> 0x01af }
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r8 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_CONTINUOUS     // Catch:{ CameraAccessException -> 0x01af }
            if (r6 != r8) goto L_0x00dd
            r1.raw_todo = r5     // Catch:{ CameraAccessException -> 0x01af }
            if (r19 == 0) goto L_0x00d5
            int r6 = r1.n_burst     // Catch:{ CameraAccessException -> 0x01af }
            int r6 = r6 + r4
            r1.n_burst = r6     // Catch:{ CameraAccessException -> 0x01af }
            r6 = 0
            goto L_0x016c
        L_0x00d5:
            r1.continuous_burst_in_progress = r4     // Catch:{ CameraAccessException -> 0x01af }
            r1.n_burst = r4     // Catch:{ CameraAccessException -> 0x01af }
            r1.n_burst_taken = r5     // Catch:{ CameraAccessException -> 0x01af }
            goto L_0x016b
        L_0x00dd:
            boolean r6 = r1.burst_for_noise_reduction     // Catch:{ CameraAccessException -> 0x01af }
            if (r6 == 0) goto L_0x0165
            r1.n_burst = r7     // Catch:{ CameraAccessException -> 0x01af }
            r1.n_burst_taken = r5     // Catch:{ CameraAccessException -> 0x01af }
            boolean r6 = r1.capture_result_has_iso     // Catch:{ CameraAccessException -> 0x01af }
            if (r6 == 0) goto L_0x016b
            int r6 = r1.capture_result_iso     // Catch:{ CameraAccessException -> 0x01af }
            r7 = 1100(0x44c, float:1.541E-42)
            if (r6 < r7) goto L_0x012d
            boolean r6 = r1.noise_reduction_low_light     // Catch:{ CameraAccessException -> 0x01af }
            if (r6 == 0) goto L_0x00f6
            r6 = 15
            goto L_0x00f8
        L_0x00f6:
            r6 = 8
        L_0x00f8:
            r1.n_burst = r6     // Catch:{ CameraAccessException -> 0x01af }
            java.lang.String r6 = android.os.Build.MANUFACTURER     // Catch:{ CameraAccessException -> 0x01af }
            java.util.Locale r7 = java.util.Locale.US     // Catch:{ CameraAccessException -> 0x01af }
            java.lang.String r6 = r6.toLowerCase(r7)     // Catch:{ CameraAccessException -> 0x01af }
            java.lang.String r7 = "oneplus"
            boolean r6 = r6.contains(r7)     // Catch:{ CameraAccessException -> 0x01af }
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r7 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x01af }
            boolean r7 = r7.has_iso     // Catch:{ CameraAccessException -> 0x01af }
            if (r7 != 0) goto L_0x016b
            if (r6 != 0) goto L_0x016b
            boolean r6 = r1.noise_reduction_low_light     // Catch:{ CameraAccessException -> 0x01af }
            if (r6 == 0) goto L_0x011a
            r6 = 333333333(0x13de4355, double:1.646885484E-315)
            goto L_0x011d
        L_0x011a:
            r6 = 100000000(0x5f5e100, double:4.94065646E-316)
        L_0x011d:
            boolean r8 = r1.capture_result_has_exposure_time     // Catch:{ CameraAccessException -> 0x01af }
            if (r8 == 0) goto L_0x0127
            long r8 = r1.capture_result_exposure_time     // Catch:{ CameraAccessException -> 0x01af }
            int r10 = (r8 > r6 ? 1 : (r8 == r6 ? 0 : -1))
            if (r10 >= 0) goto L_0x016b
        L_0x0127:
            r1.modified_from_camera_settings = r4     // Catch:{ CameraAccessException -> 0x01af }
            r1.setManualExposureTime(r0, r6)     // Catch:{ CameraAccessException -> 0x01af }
            goto L_0x016b
        L_0x012d:
            boolean r6 = r1.capture_result_has_exposure_time     // Catch:{ CameraAccessException -> 0x01af }
            if (r6 == 0) goto L_0x016b
            r6 = 4611686018427387904(0x4000000000000000, double:2.0)
            r8 = -4620693217682128896(0xbfe0000000000000, double:-0.5)
            double r16 = java.lang.Math.pow(r6, r8)     // Catch:{ CameraAccessException -> 0x01af }
            long r6 = r1.capture_result_exposure_time     // Catch:{ CameraAccessException -> 0x01af }
            r8 = 16666666(0xfe502a, double:8.234427E-317)
            int r10 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1))
            if (r10 > 0) goto L_0x016b
            r8 = 3
            r1.n_burst = r8     // Catch:{ CameraAccessException -> 0x01af }
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r8 = r1.camera_settings     // Catch:{ CameraAccessException -> 0x01af }
            boolean r8 = r8.has_iso     // Catch:{ CameraAccessException -> 0x01af }
            if (r8 != 0) goto L_0x016b
            r12 = 16666666(0xfe502a, double:8.234427E-317)
            r14 = 8333333(0x7f2815, double:4.1172136E-317)
            r10 = r6
            double r8 = getScaleForExposureTime(r10, r12, r14, r16)     // Catch:{ CameraAccessException -> 0x01af }
            double r6 = (double) r6
            java.lang.Double.isNaN(r6)
            double r6 = r6 * r8
            long r6 = (long) r6
            r1.modified_from_camera_settings = r4     // Catch:{ CameraAccessException -> 0x01af }
            r1.setManualExposureTime(r0, r6)     // Catch:{ CameraAccessException -> 0x01af }
            goto L_0x016b
        L_0x0165:
            int r6 = r1.burst_requested_n_images     // Catch:{ CameraAccessException -> 0x01af }
            r1.n_burst = r6     // Catch:{ CameraAccessException -> 0x01af }
            r1.n_burst_taken = r5     // Catch:{ CameraAccessException -> 0x01af }
        L_0x016b:
            r6 = 1
        L_0x016c:
            boolean r7 = r1.raw_todo     // Catch:{ CameraAccessException -> 0x01ac }
            if (r7 == 0) goto L_0x0179
            android.media.ImageReader r7 = r1.imageReaderRaw     // Catch:{ CameraAccessException -> 0x01ac }
            android.view.Surface r7 = r7.getSurface()     // Catch:{ CameraAccessException -> 0x01ac }
            r0.addTarget(r7)     // Catch:{ CameraAccessException -> 0x01ac }
        L_0x0179:
            int r7 = r1.n_burst     // Catch:{ CameraAccessException -> 0x01ac }
            r1.n_burst_total = r7     // Catch:{ CameraAccessException -> 0x01ac }
            boolean r7 = r1.raw_todo     // Catch:{ CameraAccessException -> 0x01ac }
            if (r7 == 0) goto L_0x0184
            int r7 = r1.n_burst     // Catch:{ CameraAccessException -> 0x01ac }
            goto L_0x0185
        L_0x0184:
            r7 = 0
        L_0x0185:
            r1.n_burst_raw = r7     // Catch:{ CameraAccessException -> 0x01ac }
            r1.burst_single_request = r5     // Catch:{ CameraAccessException -> 0x01ac }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject r7 = new net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject     // Catch:{ CameraAccessException -> 0x01ac }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagType r8 = net.sourceforge.opencamera.cameracontroller.CameraController2.RequestTagType.CAPTURE_BURST_IN_PROGRESS     // Catch:{ CameraAccessException -> 0x01ac }
            r7.<init>(r8)     // Catch:{ CameraAccessException -> 0x01ac }
            r0.setTag(r7)     // Catch:{ CameraAccessException -> 0x01ac }
            android.hardware.camera2.CaptureRequest r7 = r0.build()     // Catch:{ CameraAccessException -> 0x01ac }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject r8 = new net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagObject     // Catch:{ CameraAccessException -> 0x01aa }
            net.sourceforge.opencamera.cameracontroller.CameraController2$RequestTagType r9 = net.sourceforge.opencamera.cameracontroller.CameraController2.RequestTagType.CAPTURE     // Catch:{ CameraAccessException -> 0x01aa }
            r8.<init>(r9)     // Catch:{ CameraAccessException -> 0x01aa }
            r0.setTag(r8)     // Catch:{ CameraAccessException -> 0x01aa }
            android.hardware.camera2.CaptureRequest r0 = r0.build()     // Catch:{ CameraAccessException -> 0x01aa }
            r9 = r0
            r8 = r7
            r0 = 1
            r7 = r3
            goto L_0x01c1
        L_0x01aa:
            r0 = move-exception
            goto L_0x01b2
        L_0x01ac:
            r0 = move-exception
            r7 = r3
            goto L_0x01b2
        L_0x01af:
            r0 = move-exception
            r7 = r3
            r6 = 1
        L_0x01b2:
            r0.printStackTrace()     // Catch:{ all -> 0x0248 }
            r1.jpeg_todo = r5     // Catch:{ all -> 0x0248 }
            r1.raw_todo = r5     // Catch:{ all -> 0x0248 }
            r1.picture_cb = r3     // Catch:{ all -> 0x0248 }
            net.sourceforge.opencamera.cameracontroller.CameraController$ErrorCallback r0 = r1.take_picture_error_cb     // Catch:{ all -> 0x0248 }
            r9 = r3
            r8 = r7
            r7 = r0
            r0 = 0
        L_0x01c1:
            monitor-exit(r2)     // Catch:{ all -> 0x0248 }
            if (r0 == 0) goto L_0x01cd
            net.sourceforge.opencamera.cameracontroller.CameraController$PictureCallback r2 = r1.picture_cb
            if (r2 == 0) goto L_0x01cd
            if (r6 == 0) goto L_0x01cd
            r2.onStarted()
        L_0x01cd:
            if (r0 == 0) goto L_0x0240
            java.lang.Object r2 = r1.background_camera_lock
            monitor-enter(r2)
            android.hardware.camera2.CameraDevice r0 = r1.camera     // Catch:{ all -> 0x023d }
            if (r0 == 0) goto L_0x023b
            android.hardware.camera2.CameraCaptureSession r0 = r1.captureSession     // Catch:{ all -> 0x023d }
            if (r0 != 0) goto L_0x01db
            goto L_0x023b
        L_0x01db:
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r0 = r1.burst_type     // Catch:{ CameraAccessException -> 0x022c }
            net.sourceforge.opencamera.cameracontroller.CameraController$BurstType r6 = net.sourceforge.opencamera.cameracontroller.CameraController.BurstType.BURSTTYPE_CONTINUOUS     // Catch:{ CameraAccessException -> 0x022c }
            if (r0 != r6) goto L_0x0209
            boolean r0 = r1.continuous_burst_in_progress     // Catch:{ CameraAccessException -> 0x022c }
            if (r0 != 0) goto L_0x01e6
            goto L_0x01e7
        L_0x01e6:
            r4 = 0
        L_0x01e7:
            r1.continuous_burst_requested_last_capture = r4     // Catch:{ CameraAccessException -> 0x022c }
            android.hardware.camera2.CameraCaptureSession r0 = r1.captureSession     // Catch:{ CameraAccessException -> 0x022c }
            boolean r4 = r1.continuous_burst_in_progress     // Catch:{ CameraAccessException -> 0x022c }
            if (r4 == 0) goto L_0x01f0
            goto L_0x01f1
        L_0x01f0:
            r8 = r9
        L_0x01f1:
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r4 = r1.previewCaptureCallback     // Catch:{ CameraAccessException -> 0x022c }
            android.os.Handler r6 = r1.handler     // Catch:{ CameraAccessException -> 0x022c }
            r0.capture(r8, r4, r6)     // Catch:{ CameraAccessException -> 0x022c }
            boolean r0 = r1.continuous_burst_in_progress     // Catch:{ CameraAccessException -> 0x022c }
            if (r0 == 0) goto L_0x0226
            android.os.Handler r0 = r1.handler     // Catch:{ CameraAccessException -> 0x022c }
            net.sourceforge.opencamera.cameracontroller.CameraController2$3 r4 = new net.sourceforge.opencamera.cameracontroller.CameraController2$3     // Catch:{ CameraAccessException -> 0x022c }
            r4.<init>()     // Catch:{ CameraAccessException -> 0x022c }
            r8 = 100
            r0.postDelayed(r4, r8)     // Catch:{ CameraAccessException -> 0x022c }
            goto L_0x0226
        L_0x0209:
            java.util.ArrayList r0 = new java.util.ArrayList     // Catch:{ CameraAccessException -> 0x022c }
            r0.<init>()     // Catch:{ CameraAccessException -> 0x022c }
            r6 = 0
        L_0x020f:
            int r10 = r1.n_burst     // Catch:{ CameraAccessException -> 0x022c }
            int r10 = r10 - r4
            if (r6 >= r10) goto L_0x021a
            r0.add(r8)     // Catch:{ CameraAccessException -> 0x022c }
            int r6 = r6 + 1
            goto L_0x020f
        L_0x021a:
            r0.add(r9)     // Catch:{ CameraAccessException -> 0x022c }
            android.hardware.camera2.CameraCaptureSession r4 = r1.captureSession     // Catch:{ CameraAccessException -> 0x022c }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r6 = r1.previewCaptureCallback     // Catch:{ CameraAccessException -> 0x022c }
            android.os.Handler r8 = r1.handler     // Catch:{ CameraAccessException -> 0x022c }
            r4.captureBurst(r0, r6, r8)     // Catch:{ CameraAccessException -> 0x022c }
        L_0x0226:
            if (r19 != 0) goto L_0x0239
            r1.playSound(r5)     // Catch:{ CameraAccessException -> 0x022c }
            goto L_0x0239
        L_0x022c:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x023d }
            r1.jpeg_todo = r5     // Catch:{ all -> 0x023d }
            r1.raw_todo = r5     // Catch:{ all -> 0x023d }
            r1.picture_cb = r3     // Catch:{ all -> 0x023d }
            net.sourceforge.opencamera.cameracontroller.CameraController$ErrorCallback r0 = r1.take_picture_error_cb     // Catch:{ all -> 0x023d }
            r7 = r0
        L_0x0239:
            monitor-exit(r2)     // Catch:{ all -> 0x023d }
            goto L_0x0240
        L_0x023b:
            monitor-exit(r2)     // Catch:{ all -> 0x023d }
            return
        L_0x023d:
            r0 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x023d }
            throw r0
        L_0x0240:
            if (r7 == 0) goto L_0x0245
            r7.onError()
        L_0x0245:
            return
        L_0x0246:
            monitor-exit(r2)     // Catch:{ all -> 0x0248 }
            return
        L_0x0248:
            r0 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0248 }
            goto L_0x024c
        L_0x024b:
            throw r0
        L_0x024c:
            goto L_0x024b
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.takePictureBurst(boolean):void");
    }

    private void runPrecapture() {
        ErrorCallback errorCallback;
        synchronized (this.background_camera_lock) {
            errorCallback = null;
            try {
                Builder createCaptureRequest = this.camera.createCaptureRequest(this.previewIsVideoMode ? 4 : 2);
                createCaptureRequest.set(CaptureRequest.CONTROL_CAPTURE_INTENT, Integer.valueOf(2));
                this.camera_settings.setupBuilder(createCaptureRequest, false);
                createCaptureRequest.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Integer.valueOf(0));
                createCaptureRequest.addTarget(getPreviewSurface());
                this.state = 2;
                this.precapture_state_change_time_ms = System.currentTimeMillis();
                this.captureSession.capture(createCaptureRequest.build(), this.previewCaptureCallback, this.handler);
                this.captureSession.setRepeatingRequest(createCaptureRequest.build(), this.previewCaptureCallback, this.handler);
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Integer.valueOf(1));
                this.captureSession.capture(createCaptureRequest.build(), this.previewCaptureCallback, this.handler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                this.jpeg_todo = false;
                this.raw_todo = false;
                this.picture_cb = null;
                errorCallback = this.take_picture_error_cb;
            }
        }
        if (errorCallback != null) {
            errorCallback.onError();
        }
    }

    private void runFakePrecapture() {
        boolean z;
        ErrorCallback errorCallback;
        synchronized (this.background_camera_lock) {
            String access$6400 = this.camera_settings.flash_value;
            char c = 65535;
            int hashCode = access$6400.hashCode();
            z = do_af_trigger_for_continuous;
            switch (hashCode) {
                case -1524012984:
                    if (access$6400.equals("flash_frontscreen_auto")) {
                        c = 2;
                        break;
                    }
                    break;
                case -1195303778:
                    if (access$6400.equals("flash_auto")) {
                        c = 0;
                        break;
                    }
                    break;
                case -10523976:
                    if (access$6400.equals("flash_frontscreen_on")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1625570446:
                    if (access$6400.equals("flash_on")) {
                        c = 1;
                        break;
                    }
                    break;
            }
            if (c == 0 || c == 1) {
                this.previewBuilder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
                this.previewBuilder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(2));
                this.test_fake_flash_precapture++;
                this.fake_precapture_torch_performed = do_af_trigger_for_continuous;
            } else if (!(c == 2 || c == 3)) {
            }
            z = false;
        }
        if (z) {
            PictureCallback pictureCallback = this.picture_cb;
            if (pictureCallback != null) {
                pictureCallback.onFrontScreenTurnOn();
            }
        }
        synchronized (this.background_camera_lock) {
            this.state = 4;
            this.precapture_state_change_time_ms = System.currentTimeMillis();
            errorCallback = null;
            this.fake_precapture_turn_on_torch_id = null;
            try {
                CaptureRequest build = this.previewBuilder.build();
                if (this.fake_precapture_torch_performed) {
                    this.fake_precapture_turn_on_torch_id = build;
                }
                setRepeatingRequest(build);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                this.jpeg_todo = false;
                this.raw_todo = false;
                this.picture_cb = null;
                errorCallback = this.take_picture_error_cb;
            }
        }
        if (errorCallback != null) {
            errorCallback.onError();
        }
    }

    private boolean fireAutoFlashFrontScreen() {
        if (!this.capture_result_has_iso || this.capture_result_iso < 750) {
            return false;
        }
        return do_af_trigger_for_continuous;
    }

    private boolean fireAutoFlash() {
        long currentTimeMillis = System.currentTimeMillis();
        long j = this.fake_precapture_use_flash_time_ms;
        if (j == -1 || currentTimeMillis - j >= precapture_done_timeout_c) {
            String access$6400 = this.camera_settings.flash_value;
            char c = 65535;
            int hashCode = access$6400.hashCode();
            if (hashCode != -1524012984) {
                if (hashCode == -1195303778 && access$6400.equals("flash_auto")) {
                    c = 0;
                }
            } else if (access$6400.equals("flash_frontscreen_auto")) {
                c = 1;
            }
            if (c == 0) {
                this.fake_precapture_use_flash = this.is_flash_required;
            } else if (c != 1) {
                this.fake_precapture_use_flash = false;
            } else {
                this.fake_precapture_use_flash = fireAutoFlashFrontScreen();
            }
            if (this.fake_precapture_use_flash) {
                this.fake_precapture_use_flash_time_ms = currentTimeMillis;
            } else {
                this.fake_precapture_use_flash_time_ms = -1;
            }
            return this.fake_precapture_use_flash;
        }
        this.fake_precapture_use_flash_time_ms = currentTimeMillis;
        return this.fake_precapture_use_flash;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00c6, code lost:
        if (r6 == false) goto L_0x00cc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00c8, code lost:
        takePictureAfterPrecapture();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00cc, code lost:
        if (r2 == false) goto L_0x00d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ce, code lost:
        runFakePrecapture();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00d2, code lost:
        if (r7 == false) goto L_0x00d7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00d4, code lost:
        runPrecapture();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00d7, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void takePicture(net.sourceforge.opencamera.cameracontroller.CameraController.PictureCallback r6, net.sourceforge.opencamera.cameracontroller.CameraController.ErrorCallback r7) {
        /*
            r5 = this;
            java.lang.Object r0 = r5.background_camera_lock
            monitor-enter(r0)
            android.hardware.camera2.CameraDevice r1 = r5.camera     // Catch:{ all -> 0x00dd }
            if (r1 == 0) goto L_0x00d8
            android.hardware.camera2.CameraCaptureSession r1 = r5.captureSession     // Catch:{ all -> 0x00dd }
            if (r1 != 0) goto L_0x000d
            goto L_0x00d8
        L_0x000d:
            r5.picture_cb = r6     // Catch:{ all -> 0x00dd }
            r6 = 1
            r5.jpeg_todo = r6     // Catch:{ all -> 0x00dd }
            android.media.ImageReader r1 = r5.imageReaderRaw     // Catch:{ all -> 0x00dd }
            r2 = 0
            if (r1 == 0) goto L_0x0019
            r1 = 1
            goto L_0x001a
        L_0x0019:
            r1 = 0
        L_0x001a:
            r5.raw_todo = r1     // Catch:{ all -> 0x00dd }
            r5.done_all_captures = r2     // Catch:{ all -> 0x00dd }
            r5.take_picture_error_cb = r7     // Catch:{ all -> 0x00dd }
            r5.fake_precapture_torch_performed = r2     // Catch:{ all -> 0x00dd }
            boolean r7 = r5.ready_for_capture     // Catch:{ all -> 0x00dd }
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r7 = r5.camera_settings     // Catch:{ all -> 0x00dd }
            boolean r7 = r7.has_iso     // Catch:{ all -> 0x00dd }
            if (r7 != 0) goto L_0x00c4
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r7 = r5.camera_settings     // Catch:{ all -> 0x00dd }
            java.lang.String r7 = r7.flash_value     // Catch:{ all -> 0x00dd }
            java.lang.String r1 = "flash_off"
            boolean r7 = r7.equals(r1)     // Catch:{ all -> 0x00dd }
            if (r7 != 0) goto L_0x00c4
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r7 = r5.camera_settings     // Catch:{ all -> 0x00dd }
            java.lang.String r7 = r7.flash_value     // Catch:{ all -> 0x00dd }
            java.lang.String r1 = "flash_torch"
            boolean r7 = r7.equals(r1)     // Catch:{ all -> 0x00dd }
            if (r7 == 0) goto L_0x004a
            goto L_0x00c4
        L_0x004a:
            boolean r7 = r5.use_fake_precapture_mode     // Catch:{ all -> 0x00dd }
            r1 = 2
            if (r7 == 0) goto L_0x00a1
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r7 = r5.camera_settings     // Catch:{ all -> 0x00dd }
            java.lang.String r7 = r7.flash_value     // Catch:{ all -> 0x00dd }
            java.lang.String r3 = "flash_auto"
            boolean r7 = r7.equals(r3)     // Catch:{ all -> 0x00dd }
            if (r7 != 0) goto L_0x006e
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r7 = r5.camera_settings     // Catch:{ all -> 0x00dd }
            java.lang.String r7 = r7.flash_value     // Catch:{ all -> 0x00dd }
            java.lang.String r3 = "flash_frontscreen_auto"
            boolean r7 = r7.equals(r3)     // Catch:{ all -> 0x00dd }
            if (r7 == 0) goto L_0x006c
            goto L_0x006e
        L_0x006c:
            r7 = 0
            goto L_0x006f
        L_0x006e:
            r7 = 1
        L_0x006f:
            android.hardware.camera2.CaptureRequest$Builder r3 = r5.previewBuilder     // Catch:{ all -> 0x00dd }
            android.hardware.camera2.CaptureRequest$Key r4 = android.hardware.camera2.CaptureRequest.FLASH_MODE     // Catch:{ all -> 0x00dd }
            java.lang.Object r3 = r3.get(r4)     // Catch:{ all -> 0x00dd }
            java.lang.Integer r3 = (java.lang.Integer) r3     // Catch:{ all -> 0x00dd }
            if (r7 == 0) goto L_0x0083
            boolean r7 = r5.fireAutoFlash()     // Catch:{ all -> 0x00dd }
            if (r7 != 0) goto L_0x0083
        L_0x0081:
            r7 = 0
            goto L_0x009f
        L_0x0083:
            if (r3 == 0) goto L_0x009d
            int r7 = r3.intValue()     // Catch:{ all -> 0x00dd }
            if (r7 != r1) goto L_0x009d
            r5.fake_precapture_torch_performed = r6     // Catch:{ all -> 0x00dd }
            int r7 = r5.test_fake_flash_precapture     // Catch:{ all -> 0x00dd }
            int r7 = r7 + r6
            r5.test_fake_flash_precapture = r7     // Catch:{ all -> 0x00dd }
            r6 = 5
            r5.state = r6     // Catch:{ all -> 0x00dd }
            long r6 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x00dd }
            r5.precapture_state_change_time_ms = r6     // Catch:{ all -> 0x00dd }
            r6 = 0
            goto L_0x0081
        L_0x009d:
            r6 = 0
            r7 = 1
        L_0x009f:
            r2 = r7
            goto L_0x00c4
        L_0x00a1:
            java.lang.Integer r7 = r5.capture_result_ae     // Catch:{ all -> 0x00dd }
            if (r7 == 0) goto L_0x00af
            java.lang.Integer r7 = r5.capture_result_ae     // Catch:{ all -> 0x00dd }
            int r7 = r7.intValue()     // Catch:{ all -> 0x00dd }
            if (r7 == r1) goto L_0x00af
            r7 = 1
            goto L_0x00b0
        L_0x00af:
            r7 = 0
        L_0x00b0:
            net.sourceforge.opencamera.cameracontroller.CameraController2$CameraSettings r1 = r5.camera_settings     // Catch:{ all -> 0x00dd }
            java.lang.String r1 = r1.flash_value     // Catch:{ all -> 0x00dd }
            java.lang.String r3 = "flash_auto"
            boolean r1 = r1.equals(r3)     // Catch:{ all -> 0x00dd }
            if (r1 == 0) goto L_0x00c1
            if (r7 != 0) goto L_0x00c1
            goto L_0x00c4
        L_0x00c1:
            r6 = 0
            r7 = 1
            goto L_0x00c5
        L_0x00c4:
            r7 = 0
        L_0x00c5:
            monitor-exit(r0)     // Catch:{ all -> 0x00dd }
            if (r6 == 0) goto L_0x00cc
            r5.takePictureAfterPrecapture()
            goto L_0x00d7
        L_0x00cc:
            if (r2 == 0) goto L_0x00d2
            r5.runFakePrecapture()
            goto L_0x00d7
        L_0x00d2:
            if (r7 == 0) goto L_0x00d7
            r5.runPrecapture()
        L_0x00d7:
            return
        L_0x00d8:
            r7.onError()     // Catch:{ all -> 0x00dd }
            monitor-exit(r0)     // Catch:{ all -> 0x00dd }
            return
        L_0x00dd:
            r6 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x00dd }
            goto L_0x00e1
        L_0x00e0:
            throw r6
        L_0x00e1:
            goto L_0x00e0
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.cameracontroller.CameraController2.takePicture(net.sourceforge.opencamera.cameracontroller.CameraController$PictureCallback, net.sourceforge.opencamera.cameracontroller.CameraController$ErrorCallback):void");
    }

    public void setDisplayOrientation(int i) {
        throw new RuntimeException();
    }

    public int getDisplayOrientation() {
        throw new RuntimeException();
    }

    public int getCameraOrientation() {
        return this.characteristics_sensor_orientation;
    }

    public boolean isFrontFacing() {
        return this.characteristics_is_front_facing;
    }

    public void initVideoRecorderPrePrepare(MediaRecorder mediaRecorder) {
        playSound(2);
    }

    public void initVideoRecorderPostPrepare(MediaRecorder mediaRecorder, boolean z) throws CameraControllerException {
        CameraDevice cameraDevice = this.camera;
        if (cameraDevice != null) {
            try {
                this.previewBuilder = cameraDevice.createCaptureRequest(3);
                this.previewIsVideoMode = do_af_trigger_for_continuous;
                this.previewBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, Integer.valueOf(3));
                this.camera_settings.setupBuilder(this.previewBuilder, false);
                createCaptureSession(mediaRecorder, z);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                throw new CameraControllerException();
            }
        } else {
            Log.e(TAG, "no camera");
            throw new CameraControllerException();
        }
    }

    public void reconnect() throws CameraControllerException {
        playSound(3);
        createPreviewRequest();
        createCaptureSession(null, false);
    }

    public boolean captureResultIsAEScanning() {
        return this.capture_result_is_ae_scanning;
    }

    public boolean needsFlash() {
        return this.is_flash_required;
    }

    public boolean needsFrontScreenFlash() {
        if (this.camera_settings.flash_value.equals("flash_frontscreen_on") || (this.camera_settings.flash_value.equals("flash_frontscreen_auto") && fireAutoFlashFrontScreen())) {
            return do_af_trigger_for_continuous;
        }
        return false;
    }

    public boolean captureResultHasWhiteBalanceTemperature() {
        return this.capture_result_has_white_balance_rggb;
    }

    public int captureResultWhiteBalanceTemperature() {
        return convertRggbToTemperature(this.capture_result_white_balance_rggb);
    }

    public boolean captureResultHasIso() {
        return this.capture_result_has_iso;
    }

    public int captureResultIso() {
        return this.capture_result_iso;
    }

    public boolean captureResultHasExposureTime() {
        return this.capture_result_has_exposure_time;
    }

    public long captureResultExposureTime() {
        return this.capture_result_exposure_time;
    }

    public boolean captureResultHasFrameDuration() {
        return this.capture_result_has_frame_duration;
    }

    public long captureResultFrameDuration() {
        return this.capture_result_frame_duration;
    }
}
