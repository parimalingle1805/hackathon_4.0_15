package net.sourceforge.opencamera.p004ui;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.p000v4.view.ViewCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import net.sourceforge.opencamera.BuildConfig;
import net.sourceforge.opencamera.C0316R;
import net.sourceforge.opencamera.GyroSensor;
import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyApplicationInterface;
import net.sourceforge.opencamera.MyApplicationInterface.Alignment;
import net.sourceforge.opencamera.MyApplicationInterface.PhotoMode;
import net.sourceforge.opencamera.PreferenceKeys;
import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.cameracontroller.CameraController.Face;
import net.sourceforge.opencamera.preview.ApplicationInterface.RawPref;
import net.sourceforge.opencamera.preview.Preview;
import net.sourceforge.opencamera.preview.Preview.HistogramType;

/* renamed from: net.sourceforge.opencamera.ui.DrawPreview */
public class DrawPreview {
    private static final String TAG = "DrawPreview";
    private static final double close_level_angle = 1.0d;
    private static final DecimalFormat decimalFormat = new DecimalFormat("#0.0");
    private String OSDLine1;
    private String OSDLine2;
    private long ae_started_scanning_ms = -1;
    private boolean allow_ghost_last_image;
    private int angle_highlight_color_pref;
    private String angle_string;
    private final MyApplicationInterface applicationInterface;
    private Bitmap audio_disabled_bitmap;
    private Bitmap auto_stabilise_bitmap;
    private boolean auto_stabilise_pref;
    private float battery_frac;
    private final IntentFilter battery_ifilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
    private Bitmap burst_bitmap;
    private double cached_angle;
    private Calendar calendar;
    private float capture_rate_factor;
    private boolean capture_started;
    private boolean continuous_focus_moving;
    private long continuous_focus_moving_ms;
    private String current_time_string;
    private final DateFormat dateFormatTimeInstance = DateFormat.getTimeInstance();
    private final RectF draw_rect = new RectF();
    private Bitmap dro_bitmap;
    private boolean enable_gyro_target_spot;
    private Bitmap expo_bitmap;
    private Bitmap face_detection_bitmap;
    private Bitmap flash_bitmap;
    private Bitmap focus_bracket_bitmap;
    private int focus_peaking_color_pref;
    private float free_memory_gb = -1.0f;
    private String free_memory_gb_string;
    private boolean front_screen_flash;
    private String ghost_image_pref;
    private Bitmap ghost_selected_image_bitmap;
    private String ghost_selected_image_pref = BuildConfig.FLAVOR;
    private final int[] gui_location = new int[2];
    private final float[] gyro_direction_up = new float[3];
    private final List<float[]> gyro_directions = new ArrayList();
    private boolean has_battery_frac;
    private boolean has_settings;
    private boolean has_stamp_pref;
    private boolean has_video_max_amp;
    private Bitmap hdr_bitmap;
    private Bitmap high_speed_fps_bitmap;
    private HistogramType histogram_type;
    private final Rect icon_dest = new Rect();
    private boolean image_queue_full;
    private boolean immersive_mode_everything_pref;
    private boolean is_audio_enabled_pref;
    private boolean is_face_detection_pref;
    private boolean is_high_speed;
    private boolean is_raw_only_pref;
    private boolean is_raw_pref;
    private boolean is_scanning;
    private String iso_exposure_string;
    private long last_angle_string_time;
    private long last_battery_time;
    private long last_current_time_time;
    private long last_free_memory_time;
    private final RectF last_image_dst_rect = new RectF();
    private final Matrix last_image_matrix = new Matrix();
    private final RectF last_image_src_rect = new RectF();
    private long last_iso_exposure_time;
    private long last_need_flash_indicator_time;
    private Bitmap last_thumbnail;
    private boolean last_thumbnail_is_video;
    private long last_video_max_amp_time;
    private long last_view_angles_time;
    private Bitmap location_bitmap;
    private Bitmap location_off_bitmap;
    private final MainActivity main_activity;
    private boolean need_flash_indicator = false;
    private long needs_flash_time = -1;
    private Bitmap nr_bitmap;

    /* renamed from: p */
    private final Paint f22p = new Paint();
    private Bitmap panorama_bitmap;
    private final Path path = new Path();
    private PhotoMode photoMode;
    private Bitmap photostamp_bitmap;
    private String preference_grid_pref;
    private boolean preview_size_wysiwyg_pref;
    private Bitmap raw_jpeg_bitmap;
    private Bitmap raw_only_bitmap;
    private Bitmap rotate_left_bitmap;
    private Bitmap rotate_right_bitmap;
    private final float scale;
    private final SharedPreferences sharedPreferences;
    private boolean show_angle_line_pref;
    private boolean show_angle_pref;
    private boolean show_battery_pref;
    private boolean show_free_memory_pref;
    private boolean show_geo_direction_lines_pref;
    private boolean show_geo_direction_pref;
    private boolean show_iso_pref;
    private boolean show_last_image;
    private boolean show_pitch_lines_pref;
    private boolean show_time_pref;
    private boolean show_video_max_amp_pref;
    private boolean show_zoom_pref;
    private Bitmap slow_motion_bitmap;
    private boolean store_location_pref;
    private final float stroke_width;
    private boolean take_photo_border_pref;
    private boolean taking_picture;
    private final int[] temp_histogram_channel = new int[256];
    private Rect text_bounds_angle_double;
    private Rect text_bounds_angle_single;
    private Rect text_bounds_free_memory;
    private Rect text_bounds_time;
    private volatile boolean thumbnail_anim;
    private final RectF thumbnail_anim_dst_rect = new RectF();
    private final Matrix thumbnail_anim_matrix = new Matrix();
    private final RectF thumbnail_anim_src_rect = new RectF();
    private long thumbnail_anim_start_ms = -1;
    private Bitmap time_lapse_bitmap;
    private final float[] transformed_gyro_direction = new float[3];
    private final float[] transformed_gyro_direction_up = new float[3];
    private int video_max_amp;
    private int video_max_amp_peak;
    private int video_max_amp_prev2;
    private float view_angle_x_preview;
    private float view_angle_y_preview;
    private boolean want_focus_peaking;
    private boolean want_histogram;
    private boolean want_zebra_stripes;
    private final String ybounds_text;
    private int zebra_stripes_threshold;

    public DrawPreview(MainActivity mainActivity, MyApplicationInterface myApplicationInterface) {
        this.main_activity = mainActivity;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        this.applicationInterface = myApplicationInterface;
        this.f22p.setAntiAlias(true);
        this.f22p.setTypeface(Typeface.create(Typeface.DEFAULT, 1));
        this.f22p.setStrokeCap(Cap.ROUND);
        this.scale = getContext().getResources().getDisplayMetrics().density;
        this.stroke_width = (this.scale * 1.0f) + 0.5f;
        this.f22p.setStrokeWidth(this.stroke_width);
        this.location_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.ic_gps_fixed_white_48dp);
        this.location_off_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.ic_gps_off_white_48dp);
        this.raw_jpeg_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.raw_icon);
        this.raw_only_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.raw_only_icon);
        this.auto_stabilise_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.auto_stabilise_icon);
        this.dro_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.dro_icon);
        this.hdr_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.ic_hdr_on_white_48dp);
        this.panorama_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.baseline_panorama_horizontal_white_48);
        this.expo_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.expo_icon);
        this.focus_bracket_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.focus_bracket_icon);
        this.burst_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.ic_burst_mode_white_48dp);
        this.nr_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.nr_icon);
        this.photostamp_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.ic_text_format_white_48dp);
        this.flash_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.flash_on);
        this.face_detection_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.ic_face_white_48dp);
        this.audio_disabled_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.ic_mic_off_white_48dp);
        this.high_speed_fps_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.ic_fast_forward_white_48dp);
        this.slow_motion_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.ic_slow_motion_video_white_48dp);
        this.time_lapse_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.ic_timelapse_white_48dp);
        this.rotate_left_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.baseline_rotate_left_white_48);
        this.rotate_right_bitmap = BitmapFactory.decodeResource(getContext().getResources(), C0316R.C0317drawable.baseline_rotate_right_white_48);
        StringBuilder sb = new StringBuilder();
        sb.append(getContext().getResources().getString(C0316R.string.zoom));
        sb.append(getContext().getResources().getString(C0316R.string.angle));
        sb.append(getContext().getResources().getString(C0316R.string.direction));
        this.ybounds_text = sb.toString();
    }

    public void onDestroy() {
        Bitmap bitmap = this.location_bitmap;
        if (bitmap != null) {
            bitmap.recycle();
            this.location_bitmap = null;
        }
        Bitmap bitmap2 = this.location_off_bitmap;
        if (bitmap2 != null) {
            bitmap2.recycle();
            this.location_off_bitmap = null;
        }
        Bitmap bitmap3 = this.raw_jpeg_bitmap;
        if (bitmap3 != null) {
            bitmap3.recycle();
            this.raw_jpeg_bitmap = null;
        }
        Bitmap bitmap4 = this.raw_only_bitmap;
        if (bitmap4 != null) {
            bitmap4.recycle();
            this.raw_only_bitmap = null;
        }
        Bitmap bitmap5 = this.auto_stabilise_bitmap;
        if (bitmap5 != null) {
            bitmap5.recycle();
            this.auto_stabilise_bitmap = null;
        }
        Bitmap bitmap6 = this.dro_bitmap;
        if (bitmap6 != null) {
            bitmap6.recycle();
            this.dro_bitmap = null;
        }
        Bitmap bitmap7 = this.hdr_bitmap;
        if (bitmap7 != null) {
            bitmap7.recycle();
            this.hdr_bitmap = null;
        }
        Bitmap bitmap8 = this.panorama_bitmap;
        if (bitmap8 != null) {
            bitmap8.recycle();
            this.panorama_bitmap = null;
        }
        Bitmap bitmap9 = this.expo_bitmap;
        if (bitmap9 != null) {
            bitmap9.recycle();
            this.expo_bitmap = null;
        }
        Bitmap bitmap10 = this.focus_bracket_bitmap;
        if (bitmap10 != null) {
            bitmap10.recycle();
            this.focus_bracket_bitmap = null;
        }
        Bitmap bitmap11 = this.burst_bitmap;
        if (bitmap11 != null) {
            bitmap11.recycle();
            this.burst_bitmap = null;
        }
        Bitmap bitmap12 = this.nr_bitmap;
        if (bitmap12 != null) {
            bitmap12.recycle();
            this.nr_bitmap = null;
        }
        Bitmap bitmap13 = this.photostamp_bitmap;
        if (bitmap13 != null) {
            bitmap13.recycle();
            this.photostamp_bitmap = null;
        }
        Bitmap bitmap14 = this.flash_bitmap;
        if (bitmap14 != null) {
            bitmap14.recycle();
            this.flash_bitmap = null;
        }
        Bitmap bitmap15 = this.face_detection_bitmap;
        if (bitmap15 != null) {
            bitmap15.recycle();
            this.face_detection_bitmap = null;
        }
        Bitmap bitmap16 = this.audio_disabled_bitmap;
        if (bitmap16 != null) {
            bitmap16.recycle();
            this.audio_disabled_bitmap = null;
        }
        Bitmap bitmap17 = this.high_speed_fps_bitmap;
        if (bitmap17 != null) {
            bitmap17.recycle();
            this.high_speed_fps_bitmap = null;
        }
        Bitmap bitmap18 = this.slow_motion_bitmap;
        if (bitmap18 != null) {
            bitmap18.recycle();
            this.slow_motion_bitmap = null;
        }
        Bitmap bitmap19 = this.time_lapse_bitmap;
        if (bitmap19 != null) {
            bitmap19.recycle();
            this.time_lapse_bitmap = null;
        }
        Bitmap bitmap20 = this.rotate_left_bitmap;
        if (bitmap20 != null) {
            bitmap20.recycle();
            this.rotate_left_bitmap = null;
        }
        Bitmap bitmap21 = this.rotate_right_bitmap;
        if (bitmap21 != null) {
            bitmap21.recycle();
            this.rotate_right_bitmap = null;
        }
        Bitmap bitmap22 = this.ghost_selected_image_bitmap;
        if (bitmap22 != null) {
            bitmap22.recycle();
            this.ghost_selected_image_bitmap = null;
        }
        this.ghost_selected_image_pref = BuildConfig.FLAVOR;
    }

    private Context getContext() {
        return this.main_activity;
    }

    public void updateThumbnail(Bitmap bitmap, boolean z, boolean z2) {
        if (z2 && this.applicationInterface.getThumbnailAnimationPref()) {
            this.thumbnail_anim = true;
            this.thumbnail_anim_start_ms = System.currentTimeMillis();
        }
        Bitmap bitmap2 = this.last_thumbnail;
        this.last_thumbnail = bitmap;
        this.last_thumbnail_is_video = z;
        this.allow_ghost_last_image = true;
        if (bitmap2 != null) {
            bitmap2.recycle();
        }
    }

    public boolean hasThumbnailAnimation() {
        return this.thumbnail_anim;
    }

    public void showLastImage() {
        this.show_last_image = true;
    }

    public void clearLastImage() {
        this.show_last_image = false;
    }

    public void clearGhostImage() {
        this.allow_ghost_last_image = false;
    }

    public void cameraInOperation(boolean z) {
        if (!z || this.main_activity.getPreview().isVideo()) {
            this.taking_picture = false;
            this.front_screen_flash = false;
            this.capture_started = false;
            return;
        }
        this.taking_picture = true;
    }

    public void setImageQueueFull(boolean z) {
        this.image_queue_full = z;
    }

    public void turnFrontScreenFlashOn() {
        this.front_screen_flash = true;
    }

    public void onCaptureStarted() {
        this.capture_started = true;
    }

    public void onContinuousFocusMove(boolean z) {
        if (z && !this.continuous_focus_moving) {
            this.continuous_focus_moving = true;
            this.continuous_focus_moving_ms = System.currentTimeMillis();
        }
    }

    public void clearContinuousFocusMove() {
        if (this.continuous_focus_moving) {
            this.continuous_focus_moving = false;
            this.continuous_focus_moving_ms = 0;
        }
    }

    public void setGyroDirectionMarker(float f, float f2, float f3) {
        this.enable_gyro_target_spot = true;
        this.gyro_directions.clear();
        addGyroDirectionMarker(f, f2, f3);
        float[] fArr = this.gyro_direction_up;
        fArr[0] = 0.0f;
        fArr[1] = 1.0f;
        fArr[2] = 0.0f;
    }

    public void addGyroDirectionMarker(float f, float f2, float f3) {
        this.gyro_directions.add(new float[]{f, f2, f3});
    }

    public void clearGyroDirectionMarker() {
        this.enable_gyro_target_spot = false;
    }

    public void updateSettings() {
        this.photoMode = this.applicationInterface.getPhotoMode();
        this.show_time_pref = this.sharedPreferences.getBoolean(PreferenceKeys.ShowTimePreferenceKey, true);
        this.show_free_memory_pref = this.sharedPreferences.getBoolean(PreferenceKeys.ShowFreeMemoryPreferenceKey, true);
        this.show_iso_pref = this.sharedPreferences.getBoolean(PreferenceKeys.ShowISOPreferenceKey, true);
        boolean z = false;
        this.show_video_max_amp_pref = this.sharedPreferences.getBoolean(PreferenceKeys.ShowVideoMaxAmpPreferenceKey, false);
        this.show_zoom_pref = this.sharedPreferences.getBoolean(PreferenceKeys.ShowZoomPreferenceKey, true);
        this.show_battery_pref = this.sharedPreferences.getBoolean(PreferenceKeys.ShowBatteryPreferenceKey, true);
        this.show_angle_pref = this.sharedPreferences.getBoolean(PreferenceKeys.ShowAnglePreferenceKey, false);
        this.angle_highlight_color_pref = Color.parseColor(this.sharedPreferences.getString(PreferenceKeys.ShowAngleHighlightColorPreferenceKey, "#14e715"));
        this.show_geo_direction_pref = this.sharedPreferences.getBoolean(PreferenceKeys.ShowGeoDirectionPreferenceKey, false);
        this.take_photo_border_pref = this.sharedPreferences.getBoolean(PreferenceKeys.TakePhotoBorderPreferenceKey, true);
        String str = "preference_preview_size_wysiwyg";
        this.preview_size_wysiwyg_pref = this.sharedPreferences.getString(PreferenceKeys.PreviewSizePreferenceKey, str).equals(str);
        this.store_location_pref = this.sharedPreferences.getBoolean(PreferenceKeys.LocationPreferenceKey, false);
        this.show_angle_line_pref = this.sharedPreferences.getBoolean(PreferenceKeys.ShowAngleLinePreferenceKey, false);
        this.show_pitch_lines_pref = this.sharedPreferences.getBoolean(PreferenceKeys.ShowPitchLinesPreferenceKey, false);
        this.show_geo_direction_lines_pref = this.sharedPreferences.getBoolean(PreferenceKeys.ShowGeoDirectionLinesPreferenceKey, false);
        this.immersive_mode_everything_pref = this.sharedPreferences.getString(PreferenceKeys.ImmersiveModePreferenceKey, "immersive_mode_low_profile").equals("immersive_mode_everything");
        this.has_stamp_pref = this.applicationInterface.getStampPref().equals("preference_stamp_yes");
        this.is_raw_pref = this.applicationInterface.getRawPref() != RawPref.RAWPREF_JPEG_ONLY;
        this.is_raw_only_pref = this.applicationInterface.isRawOnly();
        this.is_face_detection_pref = this.applicationInterface.getFaceDetectionPref();
        this.is_audio_enabled_pref = this.applicationInterface.getRecordAudioPref();
        this.is_high_speed = this.applicationInterface.fpsIsHighSpeed();
        this.capture_rate_factor = this.applicationInterface.getVideoCaptureRateFactor();
        this.auto_stabilise_pref = this.applicationInterface.getAutoStabilisePref();
        this.preference_grid_pref = this.sharedPreferences.getString(PreferenceKeys.ShowGridPreferenceKey, "preference_grid_none");
        this.ghost_image_pref = this.sharedPreferences.getString(PreferenceKeys.GhostImagePreferenceKey, "preference_ghost_image_off");
        boolean equals = this.ghost_image_pref.equals("preference_ghost_image_selected");
        String str2 = BuildConfig.FLAVOR;
        if (equals) {
            String string = this.sharedPreferences.getString(PreferenceKeys.GhostSelectedImageSAFPreferenceKey, str2);
            KeyguardManager keyguardManager = (KeyguardManager) this.main_activity.getSystemService("keyguard");
            if (keyguardManager != null && keyguardManager.inKeyguardRestrictedInputMode()) {
                Bitmap bitmap = this.ghost_selected_image_bitmap;
                if (bitmap != null) {
                    bitmap.recycle();
                    this.ghost_selected_image_bitmap = null;
                    this.ghost_selected_image_pref = str2;
                }
            } else if (!string.equals(this.ghost_selected_image_pref)) {
                this.ghost_selected_image_pref = string;
                Bitmap bitmap2 = this.ghost_selected_image_bitmap;
                if (bitmap2 != null) {
                    bitmap2.recycle();
                    this.ghost_selected_image_bitmap = null;
                }
                Uri parse = Uri.parse(this.ghost_selected_image_pref);
                try {
                    this.ghost_selected_image_bitmap = loadBitmap(parse, this.main_activity.getStorageUtils().getFileFromDocumentUriSAF(parse, false));
                } catch (IOException e) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("failed to load ghost_selected_image uri: ");
                    sb.append(parse);
                    Log.e(TAG, sb.toString());
                    e.printStackTrace();
                    this.ghost_selected_image_bitmap = null;
                }
            }
        } else {
            Bitmap bitmap3 = this.ghost_selected_image_bitmap;
            if (bitmap3 != null) {
                bitmap3.recycle();
                this.ghost_selected_image_bitmap = null;
            }
            this.ghost_selected_image_pref = str2;
        }
        String str3 = "preference_histogram_off";
        String string2 = this.sharedPreferences.getString(PreferenceKeys.HistogramPreferenceKey, str3);
        this.want_histogram = !string2.equals(str3) && this.main_activity.supportsPreviewBitmaps();
        this.histogram_type = HistogramType.HISTOGRAM_TYPE_VALUE;
        if (this.want_histogram) {
            char c = 65535;
            switch (string2.hashCode()) {
                case -683780238:
                    if (string2.equals("preference_histogram_value")) {
                        c = 2;
                        break;
                    }
                    break;
                case 43977486:
                    if (string2.equals("preference_histogram_rgb")) {
                        c = 0;
                        break;
                    }
                    break;
                case 605025716:
                    if (string2.equals("preference_histogram_intensity")) {
                        c = 3;
                        break;
                    }
                    break;
                case 792142318:
                    if (string2.equals("preference_histogram_lightness")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1283793529:
                    if (string2.equals("preference_histogram_luminance")) {
                        c = 1;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                this.histogram_type = HistogramType.HISTOGRAM_TYPE_RGB;
            } else if (c == 1) {
                this.histogram_type = HistogramType.HISTOGRAM_TYPE_LUMINANCE;
            } else if (c == 2) {
                this.histogram_type = HistogramType.HISTOGRAM_TYPE_VALUE;
            } else if (c == 3) {
                this.histogram_type = HistogramType.HISTOGRAM_TYPE_INTENSITY;
            } else if (c == 4) {
                this.histogram_type = HistogramType.HISTOGRAM_TYPE_LIGHTNESS;
            }
        }
        try {
            this.zebra_stripes_threshold = Integer.parseInt(this.sharedPreferences.getString(PreferenceKeys.ZebraStripesPreferenceKey, "0"));
        } catch (NumberFormatException e2) {
            e2.printStackTrace();
            this.zebra_stripes_threshold = 0;
        }
        this.want_zebra_stripes = (this.zebra_stripes_threshold != 0) & this.main_activity.supportsPreviewBitmaps();
        String str4 = "preference_focus_peaking_off";
        if (!this.sharedPreferences.getString(PreferenceKeys.FocusPeakingPreferenceKey, str4).equals(str4) && this.main_activity.supportsPreviewBitmaps()) {
            z = true;
        }
        this.want_focus_peaking = z;
        this.focus_peaking_color_pref = Color.parseColor(this.sharedPreferences.getString(PreferenceKeys.FocusPeakingColorPreferenceKey, "#ffffff"));
        this.last_view_angles_time = 0;
        this.has_settings = true;
    }

    private void updateCachedViewAngles(long j) {
        long j2 = this.last_view_angles_time;
        if (j2 == 0 || j > j2 + 10000) {
            Preview preview = this.main_activity.getPreview();
            this.view_angle_x_preview = preview.getViewAngleX(true);
            this.view_angle_y_preview = preview.getViewAngleY(true);
            this.last_view_angles_time = j;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002d, code lost:
        if (r10 != null) goto L_0x002f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r10.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0033, code lost:
        r10 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0034, code lost:
        r11.addSuppressed(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0037, code lost:
        throw r0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0068  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.graphics.Bitmap loadBitmap(android.net.Uri r10, java.io.File r11) throws java.io.IOException {
        /*
            r9 = this;
            java.lang.String r0 = "DrawPreview"
            net.sourceforge.opencamera.MainActivity r1 = r9.main_activity     // Catch:{ Exception -> 0x00a4 }
            android.content.ContentResolver r1 = r1.getContentResolver()     // Catch:{ Exception -> 0x00a4 }
            android.graphics.Bitmap r1 = android.provider.MediaStore.Images.Media.getBitmap(r1, r10)     // Catch:{ Exception -> 0x00a4 }
            if (r1 == 0) goto L_0x0099
            r0 = 0
            int r2 = android.os.Build.VERSION.SDK_INT
            r3 = 24
            if (r2 < r3) goto L_0x0038
            net.sourceforge.opencamera.MainActivity r11 = r9.main_activity
            android.content.ContentResolver r11 = r11.getContentResolver()
            java.io.InputStream r10 = r11.openInputStream(r10)
            android.media.ExifInterface r0 = new android.media.ExifInterface     // Catch:{ all -> 0x002a }
            r0.<init>(r10)     // Catch:{ all -> 0x002a }
            if (r10 == 0) goto L_0x0043
            r10.close()
            goto L_0x0043
        L_0x002a:
            r11 = move-exception
            throw r11     // Catch:{ all -> 0x002c }
        L_0x002c:
            r0 = move-exception
            if (r10 == 0) goto L_0x0037
            r10.close()     // Catch:{ all -> 0x0033 }
            goto L_0x0037
        L_0x0033:
            r10 = move-exception
            r11.addSuppressed(r10)
        L_0x0037:
            throw r0
        L_0x0038:
            if (r11 == 0) goto L_0x0043
            android.media.ExifInterface r0 = new android.media.ExifInterface
            java.lang.String r10 = r11.getAbsolutePath()
            r0.<init>(r10)
        L_0x0043:
            if (r0 == 0) goto L_0x0097
            r10 = 0
            java.lang.String r11 = "Orientation"
            int r11 = r0.getAttributeInt(r11, r10)
            r0 = 1
            if (r11 == 0) goto L_0x0065
            if (r11 != r0) goto L_0x0052
            goto L_0x0065
        L_0x0052:
            r2 = 3
            if (r11 != r2) goto L_0x0058
            r10 = 180(0xb4, float:2.52E-43)
            goto L_0x0066
        L_0x0058:
            r2 = 6
            if (r11 != r2) goto L_0x005e
            r10 = 90
            goto L_0x0066
        L_0x005e:
            r2 = 8
            if (r11 != r2) goto L_0x0065
            r10 = 270(0x10e, float:3.78E-43)
            goto L_0x0066
        L_0x0065:
            r0 = 0
        L_0x0066:
            if (r0 == 0) goto L_0x0097
            android.graphics.Matrix r7 = new android.graphics.Matrix
            r7.<init>()
            float r10 = (float) r10
            int r11 = r1.getWidth()
            float r11 = (float) r11
            r0 = 1056964608(0x3f000000, float:0.5)
            float r11 = r11 * r0
            int r2 = r1.getHeight()
            float r2 = (float) r2
            float r2 = r2 * r0
            r7.setRotate(r10, r11, r2)
            r3 = 0
            r4 = 0
            int r5 = r1.getWidth()
            int r6 = r1.getHeight()
            r8 = 1
            r2 = r1
            android.graphics.Bitmap r10 = android.graphics.Bitmap.createBitmap(r2, r3, r4, r5, r6, r7, r8)
            if (r10 == r1) goto L_0x0097
            r1.recycle()
            goto L_0x0098
        L_0x0097:
            r10 = r1
        L_0x0098:
            return r10
        L_0x0099:
            java.lang.String r10 = "MediaStore.Images.Media.getBitmap returned null"
            android.util.Log.e(r0, r10)
            java.io.IOException r10 = new java.io.IOException
            r10.<init>()
            throw r10
        L_0x00a4:
            r10 = move-exception
            java.lang.String r11 = "MediaStore.Images.Media.getBitmap exception"
            android.util.Log.e(r0, r11)
            r10.printStackTrace()
            java.io.IOException r10 = new java.io.IOException
            r10.<init>()
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.DrawPreview.loadBitmap(android.net.Uri, java.io.File):android.graphics.Bitmap");
    }

    private String getTimeStringFromSeconds(long j) {
        int i = (int) (j % 60);
        long j2 = j / 60;
        int i2 = (int) (j2 % 60);
        long j3 = j2 / 60;
        StringBuilder sb = new StringBuilder();
        sb.append(j3);
        String str = ":";
        sb.append(str);
        Object[] objArr = {Integer.valueOf(i2)};
        String str2 = "%02d";
        sb.append(String.format(Locale.getDefault(), str2, objArr));
        sb.append(str);
        sb.append(String.format(Locale.getDefault(), str2, new Object[]{Integer.valueOf(i)}));
        return sb.toString();
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void drawGrids(android.graphics.Canvas r20) {
        /*
            r19 = this;
            r0 = r19
            r7 = r20
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r1 = r1.getPreview()
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r1.getCameraController()
            if (r1 != 0) goto L_0x0011
            return
        L_0x0011:
            android.graphics.Paint r1 = r0.f22p
            float r2 = r0.stroke_width
            r1.setStrokeWidth(r2)
            java.lang.String r1 = r0.preference_grid_pref
            int r2 = r1.hashCode()
            java.lang.String r3 = "preference_grid_golden_spiral_upside_down_left"
            java.lang.String r4 = "preference_grid_golden_spiral_upside_down_right"
            java.lang.String r5 = "preference_grid_golden_spiral_left"
            r6 = 3
            r8 = 1
            r9 = 0
            r10 = 2
            r11 = -1
            switch(r2) {
                case -2062044919: goto L_0x008f;
                case -1861951923: goto L_0x0085;
                case -1499749228: goto L_0x007d;
                case -1261883239: goto L_0x0073;
                case -1261882279: goto L_0x0069;
                case 305030335: goto L_0x0061;
                case 563846404: goto L_0x0059;
                case 758075183: goto L_0x004f;
                case 1473299275: goto L_0x0045;
                case 1925582811: goto L_0x003a;
                case 1925582812: goto L_0x002e;
                default: goto L_0x002c;
            }
        L_0x002c:
            goto L_0x009a
        L_0x002e:
            java.lang.String r2 = "preference_grid_golden_triangle_2"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x009a
            r1 = 9
            goto L_0x009b
        L_0x003a:
            java.lang.String r2 = "preference_grid_golden_triangle_1"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x009a
            r1 = 8
            goto L_0x009b
        L_0x0045:
            java.lang.String r2 = "preference_grid_phi_3x3"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x009a
            r1 = 1
            goto L_0x009b
        L_0x004f:
            java.lang.String r2 = "preference_grid_golden_spiral_right"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x009a
            r1 = 4
            goto L_0x009b
        L_0x0059:
            boolean r1 = r1.equals(r3)
            if (r1 == 0) goto L_0x009a
            r1 = 7
            goto L_0x009b
        L_0x0061:
            boolean r1 = r1.equals(r4)
            if (r1 == 0) goto L_0x009a
            r1 = 6
            goto L_0x009b
        L_0x0069:
            java.lang.String r2 = "preference_grid_4x2"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x009a
            r1 = 2
            goto L_0x009b
        L_0x0073:
            java.lang.String r2 = "preference_grid_3x3"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x009a
            r1 = 0
            goto L_0x009b
        L_0x007d:
            boolean r1 = r1.equals(r5)
            if (r1 == 0) goto L_0x009a
            r1 = 5
            goto L_0x009b
        L_0x0085:
            java.lang.String r2 = "preference_grid_crosshair"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x009a
            r1 = 3
            goto L_0x009b
        L_0x008f:
            java.lang.String r2 = "preference_grid_diagonals"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x009a
            r1 = 10
            goto L_0x009b
        L_0x009a:
            r1 = -1
        L_0x009b:
            r12 = 1056964608(0x3f000000, float:0.5)
            r13 = 1077936128(0x40400000, float:3.0)
            r14 = 1073741824(0x40000000, float:2.0)
            r15 = 1065353216(0x3f800000, float:1.0)
            switch(r1) {
                case 0: goto L_0x0557;
                case 1: goto L_0x04d0;
                case 2: goto L_0x03f8;
                case 3: goto L_0x03b7;
                case 4: goto L_0x01e1;
                case 5: goto L_0x01e1;
                case 6: goto L_0x01e1;
                case 7: goto L_0x01e1;
                case 8: goto L_0x0118;
                case 9: goto L_0x0118;
                case 10: goto L_0x00a8;
                default: goto L_0x00a6;
            }
        L_0x00a6:
            goto L_0x05d6
        L_0x00a8:
            android.graphics.Paint r1 = r0.f22p
            r1.setColor(r11)
            r2 = 0
            r3 = 0
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r4 = r1 - r15
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r2 = r1 - r15
            r4 = 0
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            int r1 = r20.getWidth()
            int r2 = r20.getHeight()
            int r8 = r1 - r2
            if (r8 <= 0) goto L_0x05d6
            float r9 = (float) r8
            r3 = 0
            int r1 = r20.getHeight()
            int r1 = r1 + r8
            float r1 = (float) r1
            float r4 = r1 - r15
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r2 = r9
            r1.drawLine(r2, r3, r4, r5, r6)
            int r1 = r20.getHeight()
            int r8 = r8 + r1
            float r1 = (float) r8
            float r2 = r1 - r15
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r4 = r9
            r1.drawLine(r2, r3, r4, r5, r6)
            goto L_0x05d6
        L_0x0118:
            android.graphics.Paint r1 = r0.f22p
            r1.setColor(r11)
            int r1 = r20.getWidth()
            double r1 = (double) r1
            int r3 = r20.getHeight()
            double r3 = (double) r3
            double r1 = java.lang.Math.atan2(r1, r3)
            int r3 = r20.getHeight()
            double r3 = (double) r3
            double r5 = java.lang.Math.cos(r1)
            java.lang.Double.isNaN(r3)
            double r3 = r3 * r5
            double r5 = java.lang.Math.sin(r1)
            double r5 = r5 * r3
            float r8 = (float) r5
            double r1 = java.lang.Math.cos(r1)
            double r3 = r3 * r1
            float r9 = (float) r3
            java.lang.String r1 = r0.preference_grid_pref
            java.lang.String r2 = "preference_grid_golden_triangle_1"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0199
            r2 = 0
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r3 = r1 - r15
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 - r15
            r5 = 0
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            r3 = 0
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r9
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r4 = r8
            r1.drawLine(r2, r3, r4, r5, r6)
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r1 = r1 - r15
            float r2 = r1 - r8
            float r3 = r9 - r15
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 - r15
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            goto L_0x05d6
        L_0x0199:
            r2 = 0
            r3 = 0
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 - r15
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r2 = r1 - r15
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r1 = r1 - r15
            float r4 = r1 - r8
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r9
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            float r3 = r9 - r15
            r4 = 0
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r2 = r8
            r1.drawLine(r2, r3, r4, r5, r6)
            goto L_0x05d6
        L_0x01e1:
            r20.save()
            java.lang.String r1 = r0.preference_grid_pref
            int r2 = r1.hashCode()
            switch(r2) {
                case -1499749228: goto L_0x0208;
                case 305030335: goto L_0x0200;
                case 563846404: goto L_0x01f8;
                case 758075183: goto L_0x01ee;
                default: goto L_0x01ed;
            }
        L_0x01ed:
            goto L_0x0210
        L_0x01ee:
            java.lang.String r2 = "preference_grid_golden_spiral_right"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0210
            r1 = 1
            goto L_0x0211
        L_0x01f8:
            boolean r1 = r1.equals(r3)
            if (r1 == 0) goto L_0x0210
            r1 = 2
            goto L_0x0211
        L_0x0200:
            boolean r1 = r1.equals(r4)
            if (r1 == 0) goto L_0x0210
            r1 = 3
            goto L_0x0211
        L_0x0208:
            boolean r1 = r1.equals(r5)
            if (r1 == 0) goto L_0x0210
            r1 = 0
            goto L_0x0211
        L_0x0210:
            r1 = -1
        L_0x0211:
            if (r1 == 0) goto L_0x0242
            if (r1 == r8) goto L_0x0255
            if (r1 == r10) goto L_0x022e
            if (r1 == r6) goto L_0x021a
            goto L_0x0255
        L_0x021a:
            r1 = -1082130432(0xffffffffbf800000, float:-1.0)
            int r2 = r20.getWidth()
            float r2 = (float) r2
            float r2 = r2 * r12
            int r3 = r20.getHeight()
            float r3 = (float) r3
            float r3 = r3 * r12
            r7.scale(r15, r1, r2, r3)
            goto L_0x0255
        L_0x022e:
            r1 = 1127481344(0x43340000, float:180.0)
            int r2 = r20.getWidth()
            float r2 = (float) r2
            float r2 = r2 * r12
            int r3 = r20.getHeight()
            float r3 = (float) r3
            float r3 = r3 * r12
            r7.rotate(r1, r2, r3)
            goto L_0x0255
        L_0x0242:
            r1 = -1082130432(0xffffffffbf800000, float:-1.0)
            int r2 = r20.getWidth()
            float r2 = (float) r2
            float r2 = r2 * r12
            int r3 = r20.getHeight()
            float r3 = (float) r3
            float r3 = r3 * r12
            r7.scale(r1, r15, r2, r3)
        L_0x0255:
            android.graphics.Paint r1 = r0.f22p
            r1.setColor(r11)
            android.graphics.Paint r1 = r0.f22p
            android.graphics.Paint$Style r2 = android.graphics.Paint.Style.STROKE
            r1.setStyle(r2)
            android.graphics.Paint r1 = r0.f22p
            float r2 = r0.stroke_width
            r1.setStrokeWidth(r2)
            r1 = 34
            r2 = 21
            int r3 = r20.getWidth()
            int r4 = r20.getHeight()
            double r5 = (double) r3
            double r11 = (double) r2
            java.lang.Double.isNaN(r5)
            java.lang.Double.isNaN(r11)
            double r5 = r5 * r11
            double r11 = (double) r1
            java.lang.Double.isNaN(r11)
            double r5 = r5 / r11
            int r5 = (int) r5
            r8 = r3
            r1 = 0
            r2 = 0
            r3 = 34
            r6 = 21
        L_0x028b:
            if (r9 >= r10) goto L_0x03ab
            r20.save()
            android.graphics.RectF r11 = r0.draw_rect
            float r12 = (float) r1
            float r13 = (float) r2
            int r14 = r1 + r5
            float r15 = (float) r14
            int r10 = r2 + r4
            float r10 = (float) r10
            r11.set(r12, r13, r15, r10)
            android.graphics.RectF r10 = r0.draw_rect
            r7.clipRect(r10)
            android.graphics.RectF r10 = r0.draw_rect
            android.graphics.Paint r11 = r0.f22p
            r7.drawRect(r10, r11)
            android.graphics.RectF r10 = r0.draw_rect
            int r11 = r5 * 2
            int r1 = r1 + r11
            float r1 = (float) r1
            int r11 = r4 * 2
            int r11 = r11 + r2
            float r11 = (float) r11
            r10.set(r12, r13, r1, r11)
            android.graphics.RectF r1 = r0.draw_rect
            android.graphics.Paint r10 = r0.f22p
            r7.drawOval(r1, r10)
            r20.restore()
            int r3 = r3 - r6
            int r8 = r8 - r5
            double r10 = (double) r4
            r1 = r4
            double r4 = (double) r3
            java.lang.Double.isNaN(r10)
            java.lang.Double.isNaN(r4)
            double r10 = r10 * r4
            r16 = r4
            double r4 = (double) r6
            java.lang.Double.isNaN(r4)
            double r10 = r10 / r4
            int r4 = (int) r10
            r20.save()
            android.graphics.RectF r5 = r0.draw_rect
            int r10 = r14 + r8
            float r10 = (float) r10
            int r11 = r2 + r4
            float r12 = (float) r11
            r5.set(r15, r13, r10, r12)
            android.graphics.RectF r5 = r0.draw_rect
            r7.clipRect(r5)
            android.graphics.RectF r5 = r0.draw_rect
            android.graphics.Paint r15 = r0.f22p
            r7.drawRect(r5, r15)
            android.graphics.RectF r5 = r0.draw_rect
            int r15 = r14 - r8
            float r15 = (float) r15
            int r18 = r4 * 2
            int r2 = r2 + r18
            float r2 = (float) r2
            r5.set(r15, r13, r10, r2)
            android.graphics.RectF r2 = r0.draw_rect
            android.graphics.Paint r5 = r0.f22p
            r7.drawOval(r2, r5)
            r20.restore()
            int r6 = r6 - r3
            int r4 = r1 - r4
            double r1 = (double) r8
            r5 = r9
            double r9 = (double) r6
            java.lang.Double.isNaN(r1)
            java.lang.Double.isNaN(r9)
            double r1 = r1 * r9
            java.lang.Double.isNaN(r16)
            double r1 = r1 / r16
            int r1 = (int) r1
            int r8 = r8 - r1
            int r14 = r14 + r8
            r20.save()
            android.graphics.RectF r2 = r0.draw_rect
            float r13 = (float) r14
            int r15 = r14 + r1
            float r15 = (float) r15
            r16 = r5
            int r5 = r11 + r4
            float r5 = (float) r5
            r2.set(r13, r12, r15, r5)
            android.graphics.RectF r2 = r0.draw_rect
            r7.clipRect(r2)
            android.graphics.RectF r2 = r0.draw_rect
            android.graphics.Paint r12 = r0.f22p
            r7.drawRect(r2, r12)
            android.graphics.RectF r2 = r0.draw_rect
            int r1 = r14 - r1
            float r1 = (float) r1
            int r12 = r11 - r4
            float r12 = (float) r12
            r2.set(r1, r12, r15, r5)
            android.graphics.RectF r1 = r0.draw_rect
            android.graphics.Paint r2 = r0.f22p
            r7.drawOval(r1, r2)
            r20.restore()
            int r3 = r3 - r6
            int r1 = r14 - r8
            double r12 = (double) r4
            double r14 = (double) r3
            java.lang.Double.isNaN(r12)
            java.lang.Double.isNaN(r14)
            double r12 = r12 * r14
            java.lang.Double.isNaN(r9)
            double r12 = r12 / r9
            int r2 = (int) r12
            int r4 = r4 - r2
            int r11 = r11 + r4
            r20.save()
            android.graphics.RectF r5 = r0.draw_rect
            float r9 = (float) r1
            float r10 = (float) r11
            int r12 = r1 + r8
            float r12 = (float) r12
            int r13 = r11 + r2
            float r13 = (float) r13
            r5.set(r9, r10, r12, r13)
            android.graphics.RectF r5 = r0.draw_rect
            r7.clipRect(r5)
            android.graphics.RectF r5 = r0.draw_rect
            android.graphics.Paint r10 = r0.f22p
            r7.drawRect(r5, r10)
            android.graphics.RectF r5 = r0.draw_rect
            int r2 = r11 - r2
            float r2 = (float) r2
            int r10 = r8 * 2
            int r10 = r10 + r1
            float r10 = (float) r10
            r5.set(r9, r2, r10, r13)
            android.graphics.RectF r2 = r0.draw_rect
            android.graphics.Paint r5 = r0.f22p
            r7.drawOval(r2, r5)
            r20.restore()
            int r6 = r6 - r3
            int r2 = r11 - r4
            double r9 = (double) r8
            double r11 = (double) r6
            java.lang.Double.isNaN(r9)
            java.lang.Double.isNaN(r11)
            double r9 = r9 * r11
            java.lang.Double.isNaN(r14)
            double r9 = r9 / r14
            int r5 = (int) r9
            int r9 = r16 + 1
            r10 = 2
            goto L_0x028b
        L_0x03ab:
            r20.restore()
            android.graphics.Paint r1 = r0.f22p
            android.graphics.Paint$Style r2 = android.graphics.Paint.Style.FILL
            r1.setStyle(r2)
            goto L_0x05d6
        L_0x03b7:
            android.graphics.Paint r1 = r0.f22p
            r1.setColor(r11)
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r2 = r1 / r14
            r3 = 0
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 / r14
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            r2 = 0
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r3 = r1 / r14
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 - r15
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 / r14
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            goto L_0x05d6
        L_0x03f8:
            android.graphics.Paint r1 = r0.f22p
            r2 = -7829368(0xffffffffff888888, float:NaN)
            r1.setColor(r2)
            int r1 = r20.getWidth()
            float r1 = (float) r1
            r8 = 1082130432(0x40800000, float:4.0)
            float r2 = r1 / r8
            r3 = 0
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 / r8
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r2 = r1 / r14
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 / r14
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r1 = r1 * r13
            float r2 = r1 / r8
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r1 = r1 * r13
            float r4 = r1 / r8
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            r2 = 0
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r3 = r1 / r14
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 - r15
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 / r14
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            android.graphics.Paint r1 = r0.f22p
            r1.setColor(r11)
            r1 = 1101004800(0x41a00000, float:20.0)
            float r2 = r0.scale
            float r2 = r2 * r1
            float r2 = r2 + r12
            int r1 = (int) r2
            int r2 = r20.getWidth()
            float r2 = (float) r2
            float r2 = r2 / r14
            int r3 = r20.getHeight()
            float r3 = (float) r3
            float r3 = r3 / r14
            float r8 = (float) r1
            float r3 = r3 - r8
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 / r14
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r1 = r1 / r14
            float r5 = r1 + r8
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r1 = r1 / r14
            float r2 = r1 - r8
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r3 = r1 / r14
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r1 = r1 / r14
            float r4 = r1 + r8
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 / r14
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            goto L_0x05d6
        L_0x04d0:
            android.graphics.Paint r1 = r0.f22p
            r1.setColor(r11)
            int r1 = r20.getWidth()
            float r1 = (float) r1
            r8 = 1076333904(0x40278d50, float:2.618)
            float r2 = r1 / r8
            r3 = 0
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 / r8
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            int r1 = r20.getWidth()
            float r1 = (float) r1
            r9 = 1070537376(0x3fcf1aa0, float:1.618)
            float r1 = r1 * r9
            float r2 = r1 / r8
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r1 = r1 * r9
            float r4 = r1 / r8
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            r2 = 0
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r3 = r1 / r8
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 - r15
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 / r8
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r1 = r1 * r9
            float r3 = r1 / r8
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 - r15
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r1 = r1 * r9
            float r5 = r1 / r8
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            goto L_0x05d6
        L_0x0557:
            android.graphics.Paint r1 = r0.f22p
            r1.setColor(r11)
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r2 = r1 / r13
            r3 = 0
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 / r13
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r1 = r1 * r14
            float r2 = r1 / r13
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r1 = r1 * r14
            float r4 = r1 / r13
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 - r15
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            r2 = 0
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r3 = r1 / r13
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 - r15
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r5 = r1 / r13
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r1 = r1 * r14
            float r3 = r1 / r13
            int r1 = r20.getWidth()
            float r1 = (float) r1
            float r4 = r1 - r15
            int r1 = r20.getHeight()
            float r1 = (float) r1
            float r1 = r1 * r14
            float r5 = r1 / r13
            android.graphics.Paint r6 = r0.f22p
            r1 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
        L_0x05d6:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.DrawPreview.drawGrids(android.graphics.Canvas):void");
    }

    private void drawCropGuides(Canvas canvas) {
        int i;
        int i2;
        int i3;
        int i4;
        Preview preview = this.main_activity.getPreview();
        CameraController cameraController = preview.getCameraController();
        if (preview.isVideo() || this.preview_size_wysiwyg_pref) {
            String str = "crop_guide_none";
            String string = this.sharedPreferences.getString(PreferenceKeys.ShowCropGuidePreferenceKey, str);
            if (cameraController != null && preview.getTargetRatio() > 0.0d && !string.equals(str)) {
                this.f22p.setStyle(Style.STROKE);
                this.f22p.setStrokeWidth(this.stroke_width);
                this.f22p.setColor(Color.rgb(255, 235, 59));
                double d = -1.0d;
                char c = 65535;
                switch (string.hashCode()) {
                    case -1272821505:
                        if (string.equals("crop_guide_1")) {
                            c = 0;
                            break;
                        }
                        break;
                    case -1272821504:
                        if (string.equals("crop_guide_2")) {
                            c = 7;
                            break;
                        }
                        break;
                    case 884214533:
                        if (string.equals("crop_guide_1.4")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 884214534:
                        if (string.equals("crop_guide_1.5")) {
                            c = 4;
                            break;
                        }
                        break;
                    case 884215494:
                        if (string.equals("crop_guide_2.4")) {
                            c = 10;
                            break;
                        }
                        break;
                    case 1640846738:
                        if (string.equals("crop_guide_1.25")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 1640846767:
                        if (string.equals("crop_guide_1.33")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 1640846896:
                        if (string.equals("crop_guide_1.78")) {
                            c = 5;
                            break;
                        }
                        break;
                    case 1640846924:
                        if (string.equals("crop_guide_1.85")) {
                            c = 6;
                            break;
                        }
                        break;
                    case 1640876558:
                        if (string.equals("crop_guide_2.33")) {
                            c = 8;
                            break;
                        }
                        break;
                    case 1640876560:
                        if (string.equals("crop_guide_2.35")) {
                            c = 9;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        d = close_level_angle;
                        break;
                    case 1:
                        d = 1.25d;
                        break;
                    case 2:
                        d = 1.33333333d;
                        break;
                    case 3:
                        d = 1.4d;
                        break;
                    case 4:
                        d = 1.5d;
                        break;
                    case 5:
                        d = 1.77777778d;
                        break;
                    case 6:
                        d = 1.85d;
                        break;
                    case 7:
                        d = 2.0d;
                        break;
                    case 8:
                        d = 2.33333333d;
                        break;
                    case 9:
                        d = 2.3500612d;
                        break;
                    case 10:
                        d = 2.4d;
                        break;
                }
                if (d > 0.0d && Math.abs(preview.getCurrentPreviewAspectRatio() - d) > 1.0E-5d) {
                    int width = canvas.getWidth() - 1;
                    int height = canvas.getHeight() - 1;
                    if (d > preview.getTargetRatio()) {
                        double width2 = (double) canvas.getWidth();
                        double d2 = d * 2.0d;
                        Double.isNaN(width2);
                        int i5 = (int) (width2 / d2);
                        i4 = (canvas.getHeight() / 2) - i5;
                        i = i5 + (canvas.getHeight() / 2);
                        i2 = width;
                        i3 = 1;
                    } else {
                        double height2 = (double) canvas.getHeight();
                        Double.isNaN(height2);
                        int i6 = (int) ((height2 * d) / 2.0d);
                        i3 = (canvas.getWidth() / 2) - i6;
                        i = height;
                        i2 = i6 + (canvas.getWidth() / 2);
                        i4 = 1;
                    }
                    canvas.drawRect((float) i3, (float) i4, (float) i2, (float) i, this.f22p);
                }
                this.f22p.setStyle(Style.FILL);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:243:0x071b, code lost:
        if (r13 == 180) goto L_0x0766;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:249:0x0764, code lost:
        if (r13 == 180) goto L_0x0766;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:251:0x076a, code lost:
        r6 = r6 + (r2 + r4);
     */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x0341  */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x0399  */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x03aa  */
    /* JADX WARNING: Removed duplicated region for block: B:254:0x0776  */
    /* JADX WARNING: Removed duplicated region for block: B:270:0x07b1  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x07b6  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x0813  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x081b  */
    /* JADX WARNING: Removed duplicated region for block: B:284:0x081e  */
    /* JADX WARNING: Removed duplicated region for block: B:285:0x0822  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0261  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x029a  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x02a2  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x02f0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onDrawInfoLines(android.graphics.Canvas r31, int r32, int r33, int r34, long r35) {
        /*
            r30 = this;
            r0 = r30
            r13 = r31
            r14 = r35
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r12 = r1.getPreview()
            net.sourceforge.opencamera.cameracontroller.CameraController r16 = r12.getCameraController()
            int r11 = r12.getUIRotation()
            android.graphics.Paint r1 = r0.f22p
            float r2 = r0.scale
            r17 = 1098907648(0x41800000, float:16.0)
            float r2 = r2 * r17
            r18 = 1056964608(0x3f000000, float:0.5)
            float r2 = r2 + r18
            r1.setTextSize(r2)
            android.graphics.Paint r1 = r0.f22p
            android.graphics.Paint$Align r2 = android.graphics.Paint.Align.LEFT
            r1.setTextAlign(r2)
            float r1 = r0.scale
            r2 = 0
            float r2 = r2 * r1
            float r2 = r2 + r18
            int r10 = (int) r2
            r2 = 1073741824(0x40000000, float:2.0)
            float r1 = r1 * r2
            float r1 = r1 + r18
            int r9 = (int) r1
            r8 = 90
            if (r11 == r8) goto L_0x0047
            r1 = 270(0x10e, float:3.78E-43)
            if (r11 != r1) goto L_0x0042
            goto L_0x0047
        L_0x0042:
            r2 = r32
            r1 = r33
            goto L_0x0056
        L_0x0047:
            int r1 = r31.getWidth()
            int r2 = r31.getHeight()
            int r1 = r1 - r2
            int r1 = r1 / 2
            int r2 = r32 + r1
            int r1 = r33 - r1
        L_0x0056:
            if (r11 != r8) goto L_0x006a
            int r3 = r31.getHeight()
            int r3 = r3 - r1
            r1 = 1101004800(0x41a00000, float:20.0)
            float r4 = r0.scale
            float r4 = r4 * r1
            float r4 = r4 + r18
            int r1 = (int) r4
            int r3 = r3 - r1
            r19 = r3
            goto L_0x006c
        L_0x006a:
            r19 = r1
        L_0x006c:
            r7 = 180(0xb4, float:2.52E-43)
            if (r11 != r7) goto L_0x007f
            int r1 = r31.getWidth()
            int r1 = r1 - r2
            android.graphics.Paint r2 = r0.f22p
            android.graphics.Paint$Align r3 = android.graphics.Paint.Align.RIGHT
            r2.setTextAlign(r3)
            r20 = r1
            goto L_0x0081
        L_0x007f:
            r20 = r2
        L_0x0081:
            boolean r1 = r0.show_time_pref
            r6 = 0
            if (r1 == 0) goto L_0x0108
            java.lang.String r1 = r0.current_time_string
            if (r1 == 0) goto L_0x0098
            r1 = 1000(0x3e8, double:4.94E-321)
            long r1 = r14 / r1
            long r3 = r0.last_current_time_time
            r21 = 1000(0x3e8, double:4.94E-321)
            long r3 = r3 / r21
            int r5 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r5 <= 0) goto L_0x00b6
        L_0x0098:
            java.util.Calendar r1 = r0.calendar
            if (r1 != 0) goto L_0x00a3
            java.util.Calendar r1 = java.util.Calendar.getInstance()
            r0.calendar = r1
            goto L_0x00a6
        L_0x00a3:
            r1.setTimeInMillis(r14)
        L_0x00a6:
            java.text.DateFormat r1 = r0.dateFormatTimeInstance
            java.util.Calendar r2 = r0.calendar
            java.util.Date r2 = r2.getTime()
            java.lang.String r1 = r1.format(r2)
            r0.current_time_string = r1
            r0.last_current_time_time = r14
        L_0x00b6:
            android.graphics.Rect r1 = r0.text_bounds_time
            if (r1 != 0) goto L_0x00cc
            android.graphics.Rect r1 = new android.graphics.Rect
            r1.<init>()
            r0.text_bounds_time = r1
            android.graphics.Paint r1 = r0.f22p
            r2 = 8
            android.graphics.Rect r3 = r0.text_bounds_time
            java.lang.String r4 = "00:00:00"
            r1.getTextBounds(r4, r6, r2, r3)
        L_0x00cc:
            net.sourceforge.opencamera.MyApplicationInterface r1 = r0.applicationInterface
            android.graphics.Paint r3 = r0.f22p
            java.lang.String r4 = r0.current_time_string
            r5 = -1
            r21 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            net.sourceforge.opencamera.MyApplicationInterface$Alignment r22 = net.sourceforge.opencamera.MyApplicationInterface.Alignment.ALIGNMENT_TOP
            r23 = 0
            net.sourceforge.opencamera.MyApplicationInterface$Shadow r24 = net.sourceforge.opencamera.MyApplicationInterface.Shadow.SHADOW_OUTLINE
            android.graphics.Rect r2 = r0.text_bounds_time
            r25 = r2
            r2 = r31
            r6 = r21
            r7 = r20
            r8 = r19
            r21 = r9
            r9 = r22
            r22 = r10
            r10 = r23
            r28 = r11
            r11 = r24
            r13 = r12
            r12 = r25
            int r1 = r1.drawTextWithBackground(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
            int r1 = r1 + r22
            r12 = r28
            r11 = 90
            if (r12 != r11) goto L_0x0105
            int r19 = r19 - r1
            goto L_0x0110
        L_0x0105:
            int r19 = r19 + r1
            goto L_0x0110
        L_0x0108:
            r21 = r9
            r22 = r10
            r13 = r12
            r12 = r11
            r11 = 90
        L_0x0110:
            if (r16 == 0) goto L_0x01cd
            boolean r1 = r0.show_free_memory_pref
            if (r1 == 0) goto L_0x01cd
            long r1 = r0.last_free_memory_time
            r3 = 0
            int r5 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r5 == 0) goto L_0x0125
            r3 = 10000(0x2710, double:4.9407E-320)
            long r1 = r1 + r3
            int r3 = (r14 > r1 ? 1 : (r14 == r1 ? 0 : -1))
            if (r3 <= 0) goto L_0x0175
        L_0x0125:
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            net.sourceforge.opencamera.StorageUtils r1 = r1.getStorageUtils()
            long r1 = r1.freeMemory()
            r3 = 0
            int r5 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r5 < 0) goto L_0x0173
            float r1 = (float) r1
            r2 = 1149239296(0x44800000, float:1024.0)
            float r1 = r1 / r2
            float r2 = r0.free_memory_gb
            float r2 = r1 - r2
            float r2 = java.lang.Math.abs(r2)
            r3 = 981668463(0x3a83126f, float:0.001)
            int r2 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r2 <= 0) goto L_0x0173
            r0.free_memory_gb = r1
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.text.DecimalFormat r2 = decimalFormat
            float r3 = r0.free_memory_gb
            double r3 = (double) r3
            java.lang.String r2 = r2.format(r3)
            r1.append(r2)
            android.content.Context r2 = r30.getContext()
            android.content.res.Resources r2 = r2.getResources()
            r3 = 2131493004(0x7f0c008c, float:1.8609476E38)
            java.lang.String r2 = r2.getString(r3)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.free_memory_gb_string = r1
        L_0x0173:
            r0.last_free_memory_time = r14
        L_0x0175:
            float r1 = r0.free_memory_gb
            r2 = 0
            int r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
            if (r1 < 0) goto L_0x01cd
            java.lang.String r1 = r0.free_memory_gb_string
            if (r1 == 0) goto L_0x01cd
            android.graphics.Rect r1 = r0.text_bounds_free_memory
            if (r1 != 0) goto L_0x019a
            android.graphics.Rect r1 = new android.graphics.Rect
            r1.<init>()
            r0.text_bounds_free_memory = r1
            android.graphics.Paint r1 = r0.f22p
            java.lang.String r2 = r0.free_memory_gb_string
            int r3 = r2.length()
            android.graphics.Rect r4 = r0.text_bounds_free_memory
            r10 = 0
            r1.getTextBounds(r2, r10, r3, r4)
            goto L_0x019b
        L_0x019a:
            r10 = 0
        L_0x019b:
            net.sourceforge.opencamera.MyApplicationInterface r1 = r0.applicationInterface
            android.graphics.Paint r3 = r0.f22p
            java.lang.String r4 = r0.free_memory_gb_string
            r5 = -1
            r6 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            net.sourceforge.opencamera.MyApplicationInterface$Alignment r9 = net.sourceforge.opencamera.MyApplicationInterface.Alignment.ALIGNMENT_TOP
            r23 = 0
            net.sourceforge.opencamera.MyApplicationInterface$Shadow r24 = net.sourceforge.opencamera.MyApplicationInterface.Shadow.SHADOW_OUTLINE
            android.graphics.Rect r8 = r0.text_bounds_free_memory
            r2 = r31
            r7 = r20
            r25 = r8
            r8 = r19
            r10 = r23
            r11 = r24
            r23 = r13
            r13 = r12
            r12 = r25
            int r1 = r1.drawTextWithBackground(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
            int r1 = r1 + r22
            r12 = 90
            if (r13 != r12) goto L_0x01ca
            int r19 = r19 - r1
            goto L_0x01d2
        L_0x01ca:
            int r19 = r19 + r1
            goto L_0x01d2
        L_0x01cd:
            r23 = r13
            r13 = r12
            r12 = 90
        L_0x01d2:
            r1 = 1104674816(0x41d80000, float:27.0)
            float r2 = r0.scale
            float r1 = r1 * r2
            float r1 = r1 + r18
            int r1 = (int) r1
            android.graphics.Paint r3 = r0.f22p
            r4 = 1103101952(0x41c00000, float:24.0)
            float r2 = r2 * r4
            float r2 = r2 + r18
            r3.setTextSize(r2)
            java.lang.String r2 = r0.OSDLine1
            if (r2 == 0) goto L_0x0208
            int r2 = r2.length()
            if (r2 <= 0) goto L_0x0208
            net.sourceforge.opencamera.MyApplicationInterface r2 = r0.applicationInterface
            android.graphics.Paint r3 = r0.f22p
            java.lang.String r4 = r0.OSDLine1
            r5 = -1
            r6 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r8 = r34 - r1
            net.sourceforge.opencamera.MyApplicationInterface$Alignment r9 = net.sourceforge.opencamera.MyApplicationInterface.Alignment.ALIGNMENT_BOTTOM
            r10 = 0
            net.sourceforge.opencamera.MyApplicationInterface$Shadow r11 = net.sourceforge.opencamera.MyApplicationInterface.Shadow.SHADOW_OUTLINE
            r1 = r2
            r2 = r31
            r7 = r20
            r1.drawTextWithBackground(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
        L_0x0208:
            java.lang.String r1 = r0.OSDLine2
            if (r1 == 0) goto L_0x0229
            int r1 = r1.length()
            if (r1 <= 0) goto L_0x0229
            net.sourceforge.opencamera.MyApplicationInterface r1 = r0.applicationInterface
            android.graphics.Paint r3 = r0.f22p
            java.lang.String r4 = r0.OSDLine2
            r5 = -1
            r6 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            net.sourceforge.opencamera.MyApplicationInterface$Alignment r9 = net.sourceforge.opencamera.MyApplicationInterface.Alignment.ALIGNMENT_BOTTOM
            r10 = 0
            net.sourceforge.opencamera.MyApplicationInterface$Shadow r11 = net.sourceforge.opencamera.MyApplicationInterface.Shadow.SHADOW_OUTLINE
            r2 = r31
            r7 = r20
            r8 = r34
            r1.drawTextWithBackground(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
        L_0x0229:
            android.graphics.Paint r1 = r0.f22p
            float r2 = r0.scale
            float r2 = r2 * r17
            float r2 = r2 + r18
            r1.setTextSize(r2)
            r11 = 235(0xeb, float:3.3E-43)
            r24 = 500(0x1f4, double:2.47E-321)
            r9 = -1
            r8 = 1
            r7 = 255(0xff, float:3.57E-43)
            if (r16 == 0) goto L_0x039b
            boolean r1 = r0.show_iso_pref
            if (r1 == 0) goto L_0x039b
            java.lang.String r1 = r0.iso_exposure_string
            if (r1 == 0) goto L_0x0255
            long r1 = r0.last_iso_exposure_time
            long r1 = r1 + r24
            int r3 = (r14 > r1 ? 1 : (r14 == r1 ? 0 : -1))
            if (r3 <= 0) goto L_0x0250
            goto L_0x0255
        L_0x0250:
            r6 = r23
            r5 = 0
            goto L_0x0339
        L_0x0255:
            java.lang.String r1 = ""
            r0.iso_exposure_string = r1
            boolean r1 = r16.captureResultHasIso()
            java.lang.String r2 = " "
            if (r1 == 0) goto L_0x029a
            int r1 = r16.captureResultIso()
            java.lang.String r3 = r0.iso_exposure_string
            int r3 = r3.length()
            if (r3 <= 0) goto L_0x0280
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = r0.iso_exposure_string
            r3.append(r4)
            r3.append(r2)
            java.lang.String r3 = r3.toString()
            r0.iso_exposure_string = r3
        L_0x0280:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = r0.iso_exposure_string
            r3.append(r4)
            r6 = r23
            java.lang.String r1 = r6.getISOString(r1)
            r3.append(r1)
            java.lang.String r1 = r3.toString()
            r0.iso_exposure_string = r1
            goto L_0x029c
        L_0x029a:
            r6 = r23
        L_0x029c:
            boolean r1 = r16.captureResultHasExposureTime()
            if (r1 == 0) goto L_0x02d8
            long r3 = r16.captureResultExposureTime()
            java.lang.String r1 = r0.iso_exposure_string
            int r1 = r1.length()
            if (r1 <= 0) goto L_0x02c1
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r5 = r0.iso_exposure_string
            r1.append(r5)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.iso_exposure_string = r1
        L_0x02c1:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r5 = r0.iso_exposure_string
            r1.append(r5)
            java.lang.String r3 = r6.getExposureTimeString(r3)
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            r0.iso_exposure_string = r1
        L_0x02d8:
            boolean r1 = r6.isVideoRecording()
            if (r1 == 0) goto L_0x031a
            boolean r1 = r16.captureResultHasFrameDuration()
            if (r1 == 0) goto L_0x031a
            long r3 = r16.captureResultFrameDuration()
            java.lang.String r1 = r0.iso_exposure_string
            int r1 = r1.length()
            if (r1 <= 0) goto L_0x0303
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r5 = r0.iso_exposure_string
            r1.append(r5)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.iso_exposure_string = r1
        L_0x0303:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = r0.iso_exposure_string
            r1.append(r2)
            java.lang.String r2 = r6.getFrameDurationString(r3)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.iso_exposure_string = r1
        L_0x031a:
            r5 = 0
            r0.is_scanning = r5
            boolean r1 = r16.captureResultIsAEScanning()
            if (r1 == 0) goto L_0x0337
            android.content.SharedPreferences r1 = r0.sharedPreferences
            java.lang.String r2 = "preference_iso"
            java.lang.String r3 = "auto"
            java.lang.String r1 = r1.getString(r2, r3)
            java.lang.String r2 = "auto"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0337
            r0.is_scanning = r8
        L_0x0337:
            r0.last_iso_exposure_time = r14
        L_0x0339:
            java.lang.String r1 = r0.iso_exposure_string
            int r1 = r1.length()
            if (r1 <= 0) goto L_0x0399
            r1 = 59
            int r1 = android.graphics.Color.rgb(r7, r11, r1)
            boolean r2 = r0.is_scanning
            if (r2 == 0) goto L_0x0365
            long r2 = r0.ae_started_scanning_ms
            int r4 = (r2 > r9 ? 1 : (r2 == r9 ? 0 : -1))
            if (r4 != 0) goto L_0x0354
            r0.ae_started_scanning_ms = r14
            goto L_0x0367
        L_0x0354:
            long r2 = r14 - r2
            int r4 = (r2 > r24 ? 1 : (r2 == r24 ? 0 : -1))
            if (r4 <= 0) goto L_0x0367
            r1 = 244(0xf4, float:3.42E-43)
            r2 = 67
            r3 = 54
            int r1 = android.graphics.Color.rgb(r1, r2, r3)
            goto L_0x0367
        L_0x0365:
            r0.ae_started_scanning_ms = r9
        L_0x0367:
            r23 = r1
            net.sourceforge.opencamera.MyApplicationInterface r1 = r0.applicationInterface
            android.graphics.Paint r3 = r0.f22p
            java.lang.String r4 = r0.iso_exposure_string
            r26 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            net.sourceforge.opencamera.MyApplicationInterface$Alignment r27 = net.sourceforge.opencamera.MyApplicationInterface.Alignment.ALIGNMENT_TOP
            java.lang.String r2 = r0.ybounds_text
            net.sourceforge.opencamera.MyApplicationInterface$Shadow r28 = net.sourceforge.opencamera.MyApplicationInterface.Shadow.SHADOW_OUTLINE
            r29 = r2
            r2 = r31
            r5 = r23
            r23 = r6
            r6 = r26
            r7 = r20
            r8 = r19
            r9 = r27
            r10 = r29
            r11 = r28
            int r1 = r1.drawTextWithBackground(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
            int r1 = r1 + r22
            if (r13 != r12) goto L_0x0396
            int r19 = r19 - r1
            goto L_0x039b
        L_0x0396:
            int r19 = r19 + r1
            goto L_0x039b
        L_0x0399:
            r23 = r6
        L_0x039b:
            r1 = r19
            float r2 = r0.scale
            r3 = 1065353216(0x3f800000, float:1.0)
            float r4 = r2 * r3
            float r4 = r4 + r18
            int r4 = (int) r4
            r5 = 64
            if (r16 == 0) goto L_0x0822
            int r6 = r20 - r4
            float r2 = r2 * r17
            float r2 = r2 + r18
            int r2 = (int) r2
            r7 = 180(0xb4, float:2.52E-43)
            if (r13 != r7) goto L_0x03b8
            int r6 = r20 - r2
            int r6 = r6 + r4
        L_0x03b8:
            boolean r8 = r0.store_location_pref
            r9 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r10 = 0
            if (r8 == 0) goto L_0x0457
            android.graphics.Rect r8 = r0.icon_dest
            int r11 = r6 + r2
            int r12 = r1 + r2
            r8.set(r6, r1, r11, r12)
            android.graphics.Paint r8 = r0.f22p
            android.graphics.Paint$Style r12 = android.graphics.Paint.Style.FILL
            r8.setStyle(r12)
            android.graphics.Paint r8 = r0.f22p
            r8.setColor(r9)
            android.graphics.Paint r8 = r0.f22p
            r8.setAlpha(r5)
            android.graphics.Rect r8 = r0.icon_dest
            android.graphics.Paint r12 = r0.f22p
            r3 = r31
            r17 = r23
            r3.drawRect(r8, r12)
            android.graphics.Paint r8 = r0.f22p
            r12 = 255(0xff, float:3.57E-43)
            r8.setAlpha(r12)
            net.sourceforge.opencamera.MyApplicationInterface r8 = r0.applicationInterface
            android.location.Location r8 = r8.getLocation()
            if (r8 == 0) goto L_0x043f
            android.graphics.Bitmap r8 = r0.location_bitmap
            android.graphics.Rect r5 = r0.icon_dest
            android.graphics.Paint r9 = r0.f22p
            r3.drawBitmap(r8, r10, r5, r9)
            int r5 = r2 / 10
            double r8 = (double) r5
            r22 = 4609434218613702656(0x3ff8000000000000, double:1.5)
            java.lang.Double.isNaN(r8)
            double r8 = r8 * r22
            int r8 = (int) r8
            int r11 = r11 - r8
            int r8 = r8 + r1
            android.graphics.Paint r9 = r0.f22p
            net.sourceforge.opencamera.MyApplicationInterface r7 = r0.applicationInterface
            android.location.Location r7 = r7.getLocation()
            float r7 = r7.getAccuracy()
            r19 = 1103631483(0x41c8147b, float:25.01)
            int r7 = (r7 > r19 ? 1 : (r7 == r19 ? 0 : -1))
            if (r7 >= 0) goto L_0x0429
            r7 = 37
            r10 = 155(0x9b, float:2.17E-43)
            r12 = 36
            int r7 = android.graphics.Color.rgb(r7, r10, r12)
            r10 = 235(0xeb, float:3.3E-43)
            goto L_0x0433
        L_0x0429:
            r7 = 59
            r10 = 235(0xeb, float:3.3E-43)
            r12 = 255(0xff, float:3.57E-43)
            int r7 = android.graphics.Color.rgb(r12, r10, r7)
        L_0x0433:
            r9.setColor(r7)
            float r7 = (float) r11
            float r8 = (float) r8
            float r5 = (float) r5
            android.graphics.Paint r9 = r0.f22p
            r3.drawCircle(r7, r8, r5, r9)
            goto L_0x044b
        L_0x043f:
            r10 = 235(0xeb, float:3.3E-43)
            android.graphics.Bitmap r5 = r0.location_off_bitmap
            android.graphics.Rect r7 = r0.icon_dest
            android.graphics.Paint r8 = r0.f22p
            r9 = 0
            r3.drawBitmap(r5, r9, r7, r8)
        L_0x044b:
            r5 = 180(0xb4, float:2.52E-43)
            if (r13 != r5) goto L_0x0453
            int r5 = r2 + r4
            int r6 = r6 - r5
            goto L_0x045d
        L_0x0453:
            int r5 = r2 + r4
            int r6 = r6 + r5
            goto L_0x045d
        L_0x0457:
            r3 = r31
            r17 = r23
            r10 = 235(0xeb, float:3.3E-43)
        L_0x045d:
            boolean r5 = r0.is_raw_pref
            if (r5 == 0) goto L_0x04af
            boolean r5 = r17.supportsRaw()
            if (r5 == 0) goto L_0x04af
            android.graphics.Rect r5 = r0.icon_dest
            int r7 = r6 + r2
            int r8 = r1 + r2
            r5.set(r6, r1, r7, r8)
            android.graphics.Paint r5 = r0.f22p
            android.graphics.Paint$Style r7 = android.graphics.Paint.Style.FILL
            r5.setStyle(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r5.setColor(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 64
            r5.setAlpha(r7)
            android.graphics.Rect r5 = r0.icon_dest
            android.graphics.Paint r7 = r0.f22p
            r3.drawRect(r5, r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 255(0xff, float:3.57E-43)
            r5.setAlpha(r7)
            boolean r5 = r0.is_raw_only_pref
            if (r5 == 0) goto L_0x049a
            android.graphics.Bitmap r5 = r0.raw_only_bitmap
            goto L_0x049c
        L_0x049a:
            android.graphics.Bitmap r5 = r0.raw_jpeg_bitmap
        L_0x049c:
            android.graphics.Rect r7 = r0.icon_dest
            android.graphics.Paint r8 = r0.f22p
            r9 = 0
            r3.drawBitmap(r5, r9, r7, r8)
            r5 = 180(0xb4, float:2.52E-43)
            if (r13 != r5) goto L_0x04ac
            int r5 = r2 + r4
            int r6 = r6 - r5
            goto L_0x04af
        L_0x04ac:
            int r5 = r2 + r4
            int r6 = r6 + r5
        L_0x04af:
            boolean r5 = r0.is_face_detection_pref
            if (r5 == 0) goto L_0x04fa
            boolean r5 = r17.supportsFaceDetection()
            if (r5 == 0) goto L_0x04fa
            android.graphics.Rect r5 = r0.icon_dest
            int r7 = r6 + r2
            int r8 = r1 + r2
            r5.set(r6, r1, r7, r8)
            android.graphics.Paint r5 = r0.f22p
            android.graphics.Paint$Style r7 = android.graphics.Paint.Style.FILL
            r5.setStyle(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r5.setColor(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 64
            r5.setAlpha(r7)
            android.graphics.Rect r5 = r0.icon_dest
            android.graphics.Paint r7 = r0.f22p
            r3.drawRect(r5, r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 255(0xff, float:3.57E-43)
            r5.setAlpha(r7)
            android.graphics.Bitmap r5 = r0.face_detection_bitmap
            android.graphics.Rect r7 = r0.icon_dest
            android.graphics.Paint r8 = r0.f22p
            r9 = 0
            r3.drawBitmap(r5, r9, r7, r8)
            r5 = 180(0xb4, float:2.52E-43)
            if (r13 != r5) goto L_0x04f7
            int r5 = r2 + r4
            int r6 = r6 - r5
            goto L_0x04fa
        L_0x04f7:
            int r5 = r2 + r4
            int r6 = r6 + r5
        L_0x04fa:
            boolean r5 = r0.auto_stabilise_pref
            if (r5 == 0) goto L_0x0545
            boolean r5 = r17.hasLevelAngleStable()
            if (r5 == 0) goto L_0x0545
            android.graphics.Rect r5 = r0.icon_dest
            int r7 = r6 + r2
            int r8 = r1 + r2
            r5.set(r6, r1, r7, r8)
            android.graphics.Paint r5 = r0.f22p
            android.graphics.Paint$Style r7 = android.graphics.Paint.Style.FILL
            r5.setStyle(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r5.setColor(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 64
            r5.setAlpha(r7)
            android.graphics.Rect r5 = r0.icon_dest
            android.graphics.Paint r7 = r0.f22p
            r3.drawRect(r5, r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 255(0xff, float:3.57E-43)
            r5.setAlpha(r7)
            android.graphics.Bitmap r5 = r0.auto_stabilise_bitmap
            android.graphics.Rect r7 = r0.icon_dest
            android.graphics.Paint r8 = r0.f22p
            r9 = 0
            r3.drawBitmap(r5, r9, r7, r8)
            r5 = 180(0xb4, float:2.52E-43)
            if (r13 != r5) goto L_0x0542
            int r5 = r2 + r4
            int r6 = r6 - r5
            goto L_0x0545
        L_0x0542:
            int r5 = r2 + r4
            int r6 = r6 + r5
        L_0x0545:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.DRO
            if (r5 == r7) goto L_0x056f
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.HDR
            if (r5 == r7) goto L_0x056f
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r5 == r7) goto L_0x056f
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.ExpoBracketing
            if (r5 == r7) goto L_0x056f
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.FocusBracketing
            if (r5 == r7) goto L_0x056f
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.FastBurst
            if (r5 == r7) goto L_0x056f
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.NoiseReduction
            if (r5 != r7) goto L_0x0621
        L_0x056f:
            net.sourceforge.opencamera.MyApplicationInterface r5 = r0.applicationInterface
            boolean r5 = r5.isVideoPref()
            if (r5 != 0) goto L_0x0621
            android.graphics.Rect r5 = r0.icon_dest
            int r7 = r6 + r2
            int r8 = r1 + r2
            r5.set(r6, r1, r7, r8)
            android.graphics.Paint r5 = r0.f22p
            android.graphics.Paint$Style r7 = android.graphics.Paint.Style.FILL
            r5.setStyle(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r5.setColor(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 64
            r5.setAlpha(r7)
            android.graphics.Rect r5 = r0.icon_dest
            android.graphics.Paint r7 = r0.f22p
            r3.drawRect(r5, r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 255(0xff, float:3.57E-43)
            r5.setAlpha(r7)
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.DRO
            if (r5 != r7) goto L_0x05ac
            android.graphics.Bitmap r5 = r0.dro_bitmap
            goto L_0x05e3
        L_0x05ac:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.HDR
            if (r5 != r7) goto L_0x05b5
            android.graphics.Bitmap r5 = r0.hdr_bitmap
            goto L_0x05e3
        L_0x05b5:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r5 != r7) goto L_0x05be
            android.graphics.Bitmap r5 = r0.panorama_bitmap
            goto L_0x05e3
        L_0x05be:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.ExpoBracketing
            if (r5 != r7) goto L_0x05c7
            android.graphics.Bitmap r5 = r0.expo_bitmap
            goto L_0x05e3
        L_0x05c7:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.FocusBracketing
            if (r5 != r7) goto L_0x05d0
            android.graphics.Bitmap r5 = r0.focus_bracket_bitmap
            goto L_0x05e3
        L_0x05d0:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.FastBurst
            if (r5 != r7) goto L_0x05d9
            android.graphics.Bitmap r5 = r0.burst_bitmap
            goto L_0x05e3
        L_0x05d9:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.NoiseReduction
            if (r5 != r7) goto L_0x05e2
            android.graphics.Bitmap r5 = r0.nr_bitmap
            goto L_0x05e3
        L_0x05e2:
            r5 = 0
        L_0x05e3:
            if (r5 == 0) goto L_0x0621
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = r0.photoMode
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r8 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.NoiseReduction
            if (r7 != r8) goto L_0x0609
            net.sourceforge.opencamera.MyApplicationInterface r7 = r0.applicationInterface
            net.sourceforge.opencamera.preview.ApplicationInterface$NRModePref r7 = r7.getNRModePref()
            net.sourceforge.opencamera.preview.ApplicationInterface$NRModePref r8 = net.sourceforge.opencamera.preview.ApplicationInterface.NRModePref.NRMODE_LOW_LIGHT
            if (r7 != r8) goto L_0x0609
            android.graphics.Paint r7 = r0.f22p
            android.graphics.PorterDuffColorFilter r8 = new android.graphics.PorterDuffColorFilter
            r9 = 59
            r11 = 255(0xff, float:3.57E-43)
            int r9 = android.graphics.Color.rgb(r11, r10, r9)
            android.graphics.PorterDuff$Mode r10 = android.graphics.PorterDuff.Mode.SRC_IN
            r8.<init>(r9, r10)
            r7.setColorFilter(r8)
        L_0x0609:
            android.graphics.Rect r7 = r0.icon_dest
            android.graphics.Paint r8 = r0.f22p
            r9 = 0
            r3.drawBitmap(r5, r9, r7, r8)
            android.graphics.Paint r5 = r0.f22p
            r5.setColorFilter(r9)
            r5 = 180(0xb4, float:2.52E-43)
            if (r13 != r5) goto L_0x061e
            int r5 = r2 + r4
            int r6 = r6 - r5
            goto L_0x0621
        L_0x061e:
            int r5 = r2 + r4
            int r6 = r6 + r5
        L_0x0621:
            boolean r5 = r0.has_stamp_pref
            if (r5 == 0) goto L_0x0670
            boolean r5 = r0.is_raw_only_pref
            if (r5 == 0) goto L_0x062f
            boolean r5 = r17.supportsRaw()
            if (r5 != 0) goto L_0x0670
        L_0x062f:
            android.graphics.Rect r5 = r0.icon_dest
            int r7 = r6 + r2
            int r8 = r1 + r2
            r5.set(r6, r1, r7, r8)
            android.graphics.Paint r5 = r0.f22p
            android.graphics.Paint$Style r7 = android.graphics.Paint.Style.FILL
            r5.setStyle(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r5.setColor(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 64
            r5.setAlpha(r7)
            android.graphics.Rect r5 = r0.icon_dest
            android.graphics.Paint r7 = r0.f22p
            r3.drawRect(r5, r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 255(0xff, float:3.57E-43)
            r5.setAlpha(r7)
            android.graphics.Bitmap r5 = r0.photostamp_bitmap
            android.graphics.Rect r7 = r0.icon_dest
            android.graphics.Paint r8 = r0.f22p
            r9 = 0
            r3.drawBitmap(r5, r9, r7, r8)
            r5 = 180(0xb4, float:2.52E-43)
            if (r13 != r5) goto L_0x066d
            int r5 = r2 + r4
            int r6 = r6 - r5
            goto L_0x0670
        L_0x066d:
            int r5 = r2 + r4
            int r6 = r6 + r5
        L_0x0670:
            boolean r5 = r0.is_audio_enabled_pref
            if (r5 != 0) goto L_0x06bd
            net.sourceforge.opencamera.MyApplicationInterface r5 = r0.applicationInterface
            boolean r5 = r5.isVideoPref()
            if (r5 == 0) goto L_0x06bd
            android.graphics.Rect r5 = r0.icon_dest
            int r7 = r6 + r2
            int r8 = r1 + r2
            r5.set(r6, r1, r7, r8)
            android.graphics.Paint r5 = r0.f22p
            android.graphics.Paint$Style r7 = android.graphics.Paint.Style.FILL
            r5.setStyle(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r5.setColor(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 64
            r5.setAlpha(r7)
            android.graphics.Rect r5 = r0.icon_dest
            android.graphics.Paint r7 = r0.f22p
            r3.drawRect(r5, r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 255(0xff, float:3.57E-43)
            r5.setAlpha(r7)
            android.graphics.Bitmap r5 = r0.audio_disabled_bitmap
            android.graphics.Rect r7 = r0.icon_dest
            android.graphics.Paint r8 = r0.f22p
            r9 = 0
            r3.drawBitmap(r5, r9, r7, r8)
            r5 = 180(0xb4, float:2.52E-43)
            if (r13 != r5) goto L_0x06ba
            int r5 = r2 + r4
            int r6 = r6 - r5
            goto L_0x06bd
        L_0x06ba:
            int r5 = r2 + r4
            int r6 = r6 + r5
        L_0x06bd:
            float r5 = r0.capture_rate_factor
            r7 = 1065353216(0x3f800000, float:1.0)
            float r5 = r5 - r7
            float r5 = java.lang.Math.abs(r5)
            double r7 = (double) r5
            r9 = 4532020583610935537(0x3ee4f8b588e368f1, double:1.0E-5)
            int r5 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1))
            if (r5 <= 0) goto L_0x071e
            net.sourceforge.opencamera.MyApplicationInterface r5 = r0.applicationInterface
            boolean r5 = r5.isVideoPref()
            if (r5 == 0) goto L_0x071e
            android.graphics.Rect r5 = r0.icon_dest
            int r7 = r6 + r2
            int r8 = r1 + r2
            r5.set(r6, r1, r7, r8)
            android.graphics.Paint r5 = r0.f22p
            android.graphics.Paint$Style r7 = android.graphics.Paint.Style.FILL
            r5.setStyle(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r5.setColor(r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 64
            r5.setAlpha(r7)
            android.graphics.Rect r5 = r0.icon_dest
            android.graphics.Paint r7 = r0.f22p
            r3.drawRect(r5, r7)
            android.graphics.Paint r5 = r0.f22p
            r7 = 255(0xff, float:3.57E-43)
            r5.setAlpha(r7)
            float r5 = r0.capture_rate_factor
            r7 = 1065353216(0x3f800000, float:1.0)
            int r5 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r5 >= 0) goto L_0x070f
            android.graphics.Bitmap r5 = r0.slow_motion_bitmap
            goto L_0x0711
        L_0x070f:
            android.graphics.Bitmap r5 = r0.time_lapse_bitmap
        L_0x0711:
            android.graphics.Rect r8 = r0.icon_dest
            android.graphics.Paint r9 = r0.f22p
            r10 = 0
            r3.drawBitmap(r5, r10, r8, r9)
            r5 = 180(0xb4, float:2.52E-43)
            if (r13 != r5) goto L_0x076a
            goto L_0x0766
        L_0x071e:
            r7 = 1065353216(0x3f800000, float:1.0)
            boolean r5 = r0.is_high_speed
            if (r5 == 0) goto L_0x076d
            net.sourceforge.opencamera.MyApplicationInterface r5 = r0.applicationInterface
            boolean r5 = r5.isVideoPref()
            if (r5 == 0) goto L_0x076d
            android.graphics.Rect r5 = r0.icon_dest
            int r8 = r6 + r2
            int r9 = r1 + r2
            r5.set(r6, r1, r8, r9)
            android.graphics.Paint r5 = r0.f22p
            android.graphics.Paint$Style r8 = android.graphics.Paint.Style.FILL
            r5.setStyle(r8)
            android.graphics.Paint r5 = r0.f22p
            r8 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r5.setColor(r8)
            android.graphics.Paint r5 = r0.f22p
            r8 = 64
            r5.setAlpha(r8)
            android.graphics.Rect r5 = r0.icon_dest
            android.graphics.Paint r8 = r0.f22p
            r3.drawRect(r5, r8)
            android.graphics.Paint r5 = r0.f22p
            r8 = 255(0xff, float:3.57E-43)
            r5.setAlpha(r8)
            android.graphics.Bitmap r5 = r0.high_speed_fps_bitmap
            android.graphics.Rect r8 = r0.icon_dest
            android.graphics.Paint r9 = r0.f22p
            r10 = 0
            r3.drawBitmap(r5, r10, r8, r9)
            r5 = 180(0xb4, float:2.52E-43)
            if (r13 != r5) goto L_0x076a
        L_0x0766:
            int r5 = r2 + r4
            int r6 = r6 - r5
            goto L_0x076d
        L_0x076a:
            int r5 = r2 + r4
            int r6 = r6 + r5
        L_0x076d:
            long r8 = r0.last_need_flash_indicator_time
            r10 = 100
            long r8 = r8 + r10
            int r5 = (r14 > r8 ? 1 : (r14 == r8 ? 0 : -1))
            if (r5 <= 0) goto L_0x07b1
            r5 = 0
            r0.need_flash_indicator = r5
            java.lang.String r8 = r17.getCurrentFlashValue()
            if (r8 == 0) goto L_0x07ae
            java.lang.String r9 = "flash_on"
            boolean r9 = r8.equals(r9)
            if (r9 != 0) goto L_0x07a3
            java.lang.String r9 = "flash_auto"
            boolean r9 = r8.equals(r9)
            if (r9 != 0) goto L_0x0797
            java.lang.String r9 = "flash_red_eye"
            boolean r8 = r8.equals(r9)
            if (r8 == 0) goto L_0x079d
        L_0x0797:
            boolean r8 = r16.needsFlash()
            if (r8 != 0) goto L_0x07a3
        L_0x079d:
            boolean r8 = r16.needsFrontScreenFlash()
            if (r8 == 0) goto L_0x07ae
        L_0x07a3:
            net.sourceforge.opencamera.MyApplicationInterface r8 = r0.applicationInterface
            boolean r8 = r8.isVideoPref()
            if (r8 != 0) goto L_0x07ae
            r8 = 1
            r0.need_flash_indicator = r8
        L_0x07ae:
            r0.last_need_flash_indicator_time = r14
            goto L_0x07b2
        L_0x07b1:
            r5 = 0
        L_0x07b2:
            boolean r8 = r0.need_flash_indicator
            if (r8 == 0) goto L_0x0813
            long r8 = r0.needs_flash_time
            r10 = -1
            int r12 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1))
            if (r12 == 0) goto L_0x0810
            long r10 = r14 - r8
            float r10 = (float) r10
            r11 = 1140457472(0x43fa0000, float:500.0)
            float r10 = r10 / r11
            long r8 = r14 - r8
            int r11 = (r8 > r24 ? 1 : (r8 == r24 ? 0 : -1))
            if (r11 < 0) goto L_0x07cc
            r10 = 1065353216(0x3f800000, float:1.0)
        L_0x07cc:
            android.graphics.Rect r7 = r0.icon_dest
            int r8 = r6 + r2
            int r9 = r1 + r2
            r7.set(r6, r1, r8, r9)
            android.graphics.Paint r6 = r0.f22p
            android.graphics.Paint$Style r7 = android.graphics.Paint.Style.FILL
            r6.setStyle(r7)
            android.graphics.Paint r6 = r0.f22p
            r7 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r6.setColor(r7)
            android.graphics.Paint r6 = r0.f22p
            r7 = 1115684864(0x42800000, float:64.0)
            float r7 = r7 * r10
            int r7 = (int) r7
            r6.setAlpha(r7)
            android.graphics.Rect r6 = r0.icon_dest
            android.graphics.Paint r7 = r0.f22p
            r3.drawRect(r6, r7)
            android.graphics.Paint r6 = r0.f22p
            r7 = 1132396544(0x437f0000, float:255.0)
            float r10 = r10 * r7
            int r7 = (int) r10
            r6.setAlpha(r7)
            android.graphics.Bitmap r6 = r0.flash_bitmap
            android.graphics.Rect r7 = r0.icon_dest
            android.graphics.Paint r8 = r0.f22p
            r9 = 0
            r3.drawBitmap(r6, r9, r7, r8)
            android.graphics.Paint r6 = r0.f22p
            r7 = 255(0xff, float:3.57E-43)
            r6.setAlpha(r7)
            goto L_0x0817
        L_0x0810:
            r0.needs_flash_time = r14
            goto L_0x0817
        L_0x0813:
            r10 = -1
            r0.needs_flash_time = r10
        L_0x0817:
            r6 = 90
            if (r13 != r6) goto L_0x081e
            int r1 = r1 - r21
            goto L_0x0827
        L_0x081e:
            int r2 = r2 + r21
            int r1 = r1 + r2
            goto L_0x0827
        L_0x0822:
            r3 = r31
            r17 = r23
            r5 = 0
        L_0x0827:
            if (r16 == 0) goto L_0x0911
            boolean r2 = r17.isPreviewBitmapEnabled()
            if (r2 == 0) goto L_0x0911
            int[] r2 = r17.getHistogram()
            if (r2 == 0) goto L_0x0911
            r6 = 1120403456(0x42c80000, float:100.0)
            float r7 = r0.scale
            float r6 = r6 * r7
            float r6 = r6 + r18
            int r6 = (int) r6
            r8 = 1114636288(0x42700000, float:60.0)
            float r7 = r7 * r8
            float r7 = r7 + r18
            int r7 = (int) r7
            int r8 = r20 - r4
            r9 = 180(0xb4, float:2.52E-43)
            if (r13 != r9) goto L_0x084f
            int r20 = r20 - r6
            int r8 = r20 + r4
        L_0x084f:
            android.graphics.Rect r9 = r0.icon_dest
            int r8 = r8 - r4
            int r6 = r6 + r8
            int r4 = r1 + r7
            r9.set(r8, r1, r6, r4)
            r1 = 90
            if (r13 != r1) goto L_0x086a
            android.graphics.Rect r1 = r0.icon_dest
            int r4 = r1.top
            int r4 = r4 - r7
            r1.top = r4
            android.graphics.Rect r1 = r0.icon_dest
            int r4 = r1.bottom
            int r4 = r4 - r7
            r1.bottom = r4
        L_0x086a:
            android.graphics.Paint r1 = r0.f22p
            android.graphics.Paint$Style r4 = android.graphics.Paint.Style.FILL
            r1.setStyle(r4)
            android.graphics.Paint r1 = r0.f22p
            r4 = 64
            int r4 = android.graphics.Color.argb(r4, r5, r5, r5)
            r1.setColor(r4)
            android.graphics.Rect r1 = r0.icon_dest
            android.graphics.Paint r4 = r0.f22p
            r3.drawRect(r1, r4)
            int r1 = r2.length
            r4 = 0
            r6 = 0
        L_0x0886:
            if (r4 >= r1) goto L_0x0891
            r7 = r2[r4]
            int r6 = java.lang.Math.max(r6, r7)
            int r4 = r4 + 1
            goto L_0x0886
        L_0x0891:
            int r1 = r2.length
            r4 = 768(0x300, float:1.076E-42)
            if (r1 != r4) goto L_0x0901
            r1 = 0
            r4 = 0
        L_0x0898:
            r7 = 256(0x100, float:3.59E-43)
            if (r1 >= r7) goto L_0x08a8
            int[] r7 = r0.temp_histogram_channel
            int r8 = r4 + 1
            r4 = r2[r4]
            r7[r1] = r4
            int r1 = r1 + 1
            r4 = r8
            goto L_0x0898
        L_0x08a8:
            android.graphics.Paint r1 = r0.f22p
            r7 = 151(0x97, float:2.12E-43)
            r8 = 255(0xff, float:3.57E-43)
            int r7 = android.graphics.Color.argb(r7, r8, r5, r5)
            r1.setColor(r7)
            int[] r1 = r0.temp_histogram_channel
            r0.drawHistogramChannel(r3, r1, r6)
            r1 = 0
        L_0x08bb:
            r7 = 256(0x100, float:3.59E-43)
            if (r1 >= r7) goto L_0x08cb
            int[] r7 = r0.temp_histogram_channel
            int r8 = r4 + 1
            r4 = r2[r4]
            r7[r1] = r4
            int r1 = r1 + 1
            r4 = r8
            goto L_0x08bb
        L_0x08cb:
            android.graphics.Paint r1 = r0.f22p
            r7 = 110(0x6e, float:1.54E-43)
            r8 = 255(0xff, float:3.57E-43)
            int r7 = android.graphics.Color.argb(r7, r5, r8, r5)
            r1.setColor(r7)
            int[] r1 = r0.temp_histogram_channel
            r0.drawHistogramChannel(r3, r1, r6)
            r1 = 0
        L_0x08de:
            r7 = 256(0x100, float:3.59E-43)
            if (r1 >= r7) goto L_0x08ee
            int[] r7 = r0.temp_histogram_channel
            int r8 = r4 + 1
            r4 = r2[r4]
            r7[r1] = r4
            int r1 = r1 + 1
            r4 = r8
            goto L_0x08de
        L_0x08ee:
            android.graphics.Paint r1 = r0.f22p
            r2 = 94
            r4 = 255(0xff, float:3.57E-43)
            int r2 = android.graphics.Color.argb(r2, r5, r5, r4)
            r1.setColor(r2)
            int[] r1 = r0.temp_histogram_channel
            r0.drawHistogramChannel(r3, r1, r6)
            goto L_0x0911
        L_0x0901:
            r4 = 255(0xff, float:3.57E-43)
            android.graphics.Paint r1 = r0.f22p
            r5 = 192(0xc0, float:2.69E-43)
            int r4 = android.graphics.Color.argb(r5, r4, r4, r4)
            r1.setColor(r4)
            r0.drawHistogramChannel(r3, r2, r6)
        L_0x0911:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.DrawPreview.onDrawInfoLines(android.graphics.Canvas, int, int, int, long):void");
    }

    private void drawHistogramChannel(Canvas canvas, int[] iArr, int i) {
        this.path.reset();
        this.path.moveTo((float) this.icon_dest.left, (float) this.icon_dest.bottom);
        for (int i2 = 0; i2 < iArr.length; i2++) {
            double d = (double) i2;
            double length = (double) iArr.length;
            Double.isNaN(d);
            Double.isNaN(length);
            double d2 = d / length;
            double width = (double) this.icon_dest.width();
            Double.isNaN(width);
            this.path.lineTo((float) (this.icon_dest.left + ((int) (d2 * width))), (float) (this.icon_dest.bottom - ((iArr[i2] * this.icon_dest.height()) / i)));
        }
        this.path.lineTo((float) this.icon_dest.right, (float) this.icon_dest.bottom);
        this.path.close();
        canvas.drawPath(this.path, this.f22p);
    }

    public static String formatLevelAngle(double d) {
        String format = decimalFormat.format(d);
        return Math.abs(d) < 0.1d ? format.replaceAll("^-(?=0(.0*)?$)", BuildConfig.FLAVOR) : format;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:62:0x017a, code lost:
        if (r4 > (r7.last_angle_string_time + 500)) goto L_0x0181;
     */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x0683  */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x0822  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x082d  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x083b  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0109  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0112  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x011d  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x021a  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0225  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x029c  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0312  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void drawUI(android.graphics.Canvas r40, long r41) {
        /*
            r39 = this;
            r7 = r39
            r6 = r40
            r4 = r41
            net.sourceforge.opencamera.MainActivity r0 = r7.main_activity
            net.sourceforge.opencamera.preview.Preview r20 = r0.getPreview()
            net.sourceforge.opencamera.cameracontroller.CameraController r0 = r20.getCameraController()
            int r3 = r20.getUIRotation()
            net.sourceforge.opencamera.MainActivity r1 = r7.main_activity
            net.sourceforge.opencamera.ui.MainUI r1 = r1.getMainUI()
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r1 = r1.getUIPlacement()
            boolean r2 = r20.hasLevelAngle()
            double r8 = r20.getLevelAngle()
            boolean r10 = r20.hasGeoDirection()
            double r21 = r20.getGeoDirection()
            r40.save()
            float r11 = (float) r3
            int r12 = r40.getWidth()
            float r12 = (float) r12
            r13 = 1073741824(0x40000000, float:2.0)
            float r12 = r12 / r13
            int r14 = r40.getHeight()
            float r14 = (float) r14
            float r14 = r14 / r13
            r6.rotate(r11, r12, r14)
            r11 = 180(0xb4, float:2.52E-43)
            r23 = 1096810496(0x41600000, float:14.0)
            r25 = 1056964608(0x3f000000, float:0.5)
            if (r0 == 0) goto L_0x06e1
            boolean r26 = r20.isPreviewPaused()
            if (r26 != 0) goto L_0x06e1
            r16 = 1101004800(0x41a00000, float:20.0)
            float r12 = r7.scale
            float r16 = r16 * r12
            float r14 = r16 + r25
            int r14 = (int) r14
            r16 = 1098907648(0x41800000, float:16.0)
            float r12 = r12 * r16
            float r12 = r12 + r25
            int r12 = (int) r12
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r15 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.UIPLACEMENT_TOP
            if (r1 != r15) goto L_0x007c
            if (r3 == 0) goto L_0x0069
            if (r3 != r11) goto L_0x007c
        L_0x0069:
            int r1 = r40.getHeight()
            r15 = 4602678819172646912(0x3fe0000000000000, double:0.5)
            r28 = r12
            double r11 = (double) r14
            java.lang.Double.isNaN(r11)
            double r11 = r11 * r15
            int r11 = (int) r11
            int r1 = r1 - r11
        L_0x0079:
            r6 = r1
            goto L_0x0107
        L_0x007c:
            r28 = r12
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r11 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.UIPLACEMENT_RIGHT
            if (r1 != r11) goto L_0x0084
            r11 = 0
            goto L_0x0086
        L_0x0084:
            r11 = 180(0xb4, float:2.52E-43)
        L_0x0086:
            if (r3 != r11) goto L_0x0097
            int r1 = r40.getHeight()
            r11 = 4602678819172646912(0x3fe0000000000000, double:0.5)
            double r4 = (double) r14
            java.lang.Double.isNaN(r4)
            double r4 = r4 * r11
            int r4 = (int) r4
        L_0x0095:
            int r1 = r1 - r4
            goto L_0x0079
        L_0x0097:
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r4 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.UIPLACEMENT_RIGHT
            if (r1 != r4) goto L_0x009e
            r1 = 180(0xb4, float:2.52E-43)
            goto L_0x009f
        L_0x009e:
            r1 = 0
        L_0x009f:
            if (r3 != r1) goto L_0x00af
            int r1 = r40.getHeight()
            r4 = 4612811918334230528(0x4004000000000000, double:2.5)
            double r11 = (double) r14
            java.lang.Double.isNaN(r11)
            double r11 = r11 * r4
            int r4 = (int) r11
            goto L_0x0095
        L_0x00af:
            r1 = 90
            if (r3 == r1) goto L_0x00ba
            r1 = 270(0x10e, float:3.78E-43)
            if (r3 != r1) goto L_0x00b8
            goto L_0x00ba
        L_0x00b8:
            r6 = 0
            goto L_0x0107
        L_0x00ba:
            android.view.View r1 = r20.getView()
            android.view.View r1 = r1.getRootView()
            int r1 = r1.getRight()
            int r1 = r1 / 2
            r4 = 1120403456(0x42c80000, float:100.0)
            float r5 = r7.scale
            float r5 = r5 * r4
            float r5 = r5 + r25
            int r4 = (int) r5
            int r1 = r1 - r4
            int r4 = r40.getWidth()
            r5 = 90
            if (r3 != r5) goto L_0x00e4
            r11 = 4612811918334230528(0x4004000000000000, double:2.5)
            double r5 = (double) r14
            java.lang.Double.isNaN(r5)
            double r5 = r5 * r11
            int r5 = (int) r5
            int r4 = r4 - r5
        L_0x00e4:
            int r5 = r40.getWidth()
            int r5 = r5 / 2
            int r5 = r5 + r1
            if (r5 <= r4) goto L_0x00f5
            int r1 = r40.getWidth()
            int r1 = r1 / 2
            int r1 = r4 - r1
        L_0x00f5:
            int r4 = r40.getHeight()
            int r4 = r4 / 2
            int r4 = r4 + r1
            r5 = 4602678819172646912(0x3fe0000000000000, double:0.5)
            double r11 = (double) r14
            java.lang.Double.isNaN(r11)
            double r11 = r11 * r5
            int r1 = (int) r11
            int r4 = r4 - r1
            r6 = r4
        L_0x0107:
            if (r2 == 0) goto L_0x010f
            boolean r1 = r7.show_angle_pref
            if (r1 == 0) goto L_0x010f
            r1 = 1
            goto L_0x0110
        L_0x010f:
            r1 = 0
        L_0x0110:
            if (r10 == 0) goto L_0x0118
            boolean r2 = r7.show_geo_direction_pref
            if (r2 == 0) goto L_0x0118
            r2 = 1
            goto L_0x0119
        L_0x0118:
            r2 = 0
        L_0x0119:
            r4 = 500(0x1f4, double:2.47E-321)
            if (r1 == 0) goto L_0x021a
            android.graphics.Paint r10 = r7.f22p
            float r11 = r7.scale
            float r11 = r11 * r23
            float r11 = r11 + r25
            r10.setTextSize(r11)
            if (r2 == 0) goto L_0x013c
            r10 = 1108082688(0x420c0000, float:35.0)
            float r11 = r7.scale
            float r11 = r11 * r10
            float r11 = r11 + r25
            int r10 = (int) r11
            int r10 = -r10
            android.graphics.Paint r11 = r7.f22p
            android.graphics.Paint$Align r12 = android.graphics.Paint.Align.LEFT
            r11.setTextAlign(r12)
            goto L_0x0157
        L_0x013c:
            r10 = 0
            int r12 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1))
            if (r12 >= 0) goto L_0x0145
            r10 = 16
            goto L_0x0147
        L_0x0145:
            r10 = 14
        L_0x0147:
            float r10 = (float) r10
            float r11 = r7.scale
            float r10 = r10 * r11
            float r10 = r10 + r25
            int r10 = (int) r10
            int r10 = -r10
            android.graphics.Paint r11 = r7.f22p
            android.graphics.Paint$Align r12 = android.graphics.Paint.Align.LEFT
            r11.setTextAlign(r12)
        L_0x0157:
            double r11 = java.lang.Math.abs(r8)
            r15 = 4607182418800017408(0x3ff0000000000000, double:1.0)
            int r29 = (r11 > r15 ? 1 : (r11 == r15 ? 0 : -1))
            if (r29 > 0) goto L_0x016b
            int r11 = r7.angle_highlight_color_pref
            android.graphics.Paint r12 = r7.f22p
            r15 = 1
            r12.setUnderlineText(r15)
            r12 = r11
            goto L_0x016d
        L_0x016b:
            r15 = 1
            r12 = -1
        L_0x016d:
            java.lang.String r11 = r7.angle_string
            if (r11 == 0) goto L_0x017d
            r16 = r14
            long r13 = r7.last_angle_string_time
            long r13 = r13 + r4
            r4 = r41
            int r11 = (r4 > r13 ? 1 : (r4 == r13 ? 0 : -1))
            if (r11 <= 0) goto L_0x019c
            goto L_0x0181
        L_0x017d:
            r4 = r41
            r16 = r14
        L_0x0181:
            r7.last_angle_string_time = r4
            java.lang.String r11 = formatLevelAngle(r8)
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            r13.<init>()
            r13.append(r11)
            r11 = 176(0xb0, float:2.47E-43)
            r13.append(r11)
            java.lang.String r11 = r13.toString()
            r7.angle_string = r11
            r7.cached_angle = r8
        L_0x019c:
            android.graphics.Rect r8 = r7.text_bounds_angle_single
            if (r8 != 0) goto L_0x01b2
            android.graphics.Rect r8 = new android.graphics.Rect
            r8.<init>()
            r7.text_bounds_angle_single = r8
            android.graphics.Paint r8 = r7.f22p
            r9 = 5
            android.graphics.Rect r11 = r7.text_bounds_angle_single
            java.lang.String r13 = "-9.0°"
            r14 = 0
            r8.getTextBounds(r13, r14, r9, r11)
        L_0x01b2:
            android.graphics.Rect r8 = r7.text_bounds_angle_double
            if (r8 != 0) goto L_0x01c9
            android.graphics.Rect r8 = new android.graphics.Rect
            r8.<init>()
            r7.text_bounds_angle_double = r8
            android.graphics.Paint r8 = r7.f22p
            r9 = 6
            android.graphics.Rect r11 = r7.text_bounds_angle_double
            java.lang.String r13 = "-45.0°"
            r14 = 0
            r8.getTextBounds(r13, r14, r9, r11)
            goto L_0x01ca
        L_0x01c9:
            r14 = 0
        L_0x01ca:
            net.sourceforge.opencamera.MyApplicationInterface r8 = r7.applicationInterface
            android.graphics.Paint r11 = r7.f22p
            java.lang.String r13 = r7.angle_string
            r24 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r9 = r40.getWidth()
            int r9 = r9 / 2
            int r30 = r9 + r10
            net.sourceforge.opencamera.MyApplicationInterface$Alignment r31 = net.sourceforge.opencamera.MyApplicationInterface.Alignment.ALIGNMENT_BOTTOM
            r32 = 0
            net.sourceforge.opencamera.MyApplicationInterface$Shadow r33 = net.sourceforge.opencamera.MyApplicationInterface.Shadow.SHADOW_OUTLINE
            double r9 = r7.cached_angle
            double r9 = java.lang.Math.abs(r9)
            r34 = 4621819117588971520(0x4024000000000000, double:10.0)
            int r36 = (r9 > r34 ? 1 : (r9 == r34 ? 0 : -1))
            if (r36 >= 0) goto L_0x01ef
            android.graphics.Rect r9 = r7.text_bounds_angle_single
            goto L_0x01f1
        L_0x01ef:
            android.graphics.Rect r9 = r7.text_bounds_angle_double
        L_0x01f1:
            r34 = r9
            r9 = r40
            r10 = r11
            r11 = r13
            r26 = r28
            r13 = 244(0xf4, float:3.42E-43)
            r27 = r3
            r3 = 0
            r14 = 90
            r15 = 244(0xf4, float:3.42E-43)
            r13 = r24
            r24 = r16
            r14 = r30
            r15 = r6
            r16 = r31
            r17 = r32
            r18 = r33
            r19 = r34
            r8.drawTextWithBackground(r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19)
            android.graphics.Paint r8 = r7.f22p
            r8.setUnderlineText(r3)
            goto L_0x0223
        L_0x021a:
            r4 = r41
            r27 = r3
            r24 = r14
            r26 = r28
            r3 = 0
        L_0x0223:
            if (r2 == 0) goto L_0x0296
            r12 = -1
            android.graphics.Paint r2 = r7.f22p
            float r8 = r7.scale
            float r8 = r8 * r23
            float r8 = r8 + r25
            r2.setTextSize(r8)
            if (r1 == 0) goto L_0x0244
            r1 = 1092616192(0x41200000, float:10.0)
            float r2 = r7.scale
            float r2 = r2 * r1
            float r2 = r2 + r25
            int r1 = (int) r2
            android.graphics.Paint r2 = r7.f22p
            android.graphics.Paint$Align r8 = android.graphics.Paint.Align.LEFT
            r2.setTextAlign(r8)
            goto L_0x0253
        L_0x0244:
            float r1 = r7.scale
            float r1 = r1 * r23
            float r1 = r1 + r25
            int r1 = (int) r1
            int r1 = -r1
            android.graphics.Paint r2 = r7.f22p
            android.graphics.Paint$Align r8 = android.graphics.Paint.Align.LEFT
            r2.setTextAlign(r8)
        L_0x0253:
            double r8 = java.lang.Math.toDegrees(r21)
            float r2 = (float) r8
            r8 = 0
            int r8 = (r2 > r8 ? 1 : (r2 == r8 ? 0 : -1))
            if (r8 >= 0) goto L_0x0260
            r8 = 1135869952(0x43b40000, float:360.0)
            float r2 = r2 + r8
        L_0x0260:
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = ""
            r8.append(r9)
            int r2 = java.lang.Math.round(r2)
            r8.append(r2)
            r2 = 176(0xb0, float:2.47E-43)
            r8.append(r2)
            java.lang.String r11 = r8.toString()
            net.sourceforge.opencamera.MyApplicationInterface r8 = r7.applicationInterface
            android.graphics.Paint r10 = r7.f22p
            r13 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r2 = r40.getWidth()
            int r2 = r2 / 2
            int r14 = r2 + r1
            net.sourceforge.opencamera.MyApplicationInterface$Alignment r16 = net.sourceforge.opencamera.MyApplicationInterface.Alignment.ALIGNMENT_BOTTOM
            java.lang.String r1 = r7.ybounds_text
            net.sourceforge.opencamera.MyApplicationInterface$Shadow r18 = net.sourceforge.opencamera.MyApplicationInterface.Shadow.SHADOW_OUTLINE
            r9 = r40
            r15 = r6
            r17 = r1
            r8.drawTextWithBackground(r9, r10, r11, r12, r13, r14, r15, r16, r17, r18)
        L_0x0296:
            boolean r1 = r20.isOnTimer()
            if (r1 == 0) goto L_0x0312
            long r0 = r20.getTimerEndTime()
            long r0 = r0 - r4
            r8 = 999(0x3e7, double:4.936E-321)
            long r0 = r0 + r8
            r8 = 1000(0x3e8, double:4.94E-321)
            long r0 = r0 / r8
            r8 = 0
            int r2 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1))
            if (r2 <= 0) goto L_0x0300
            android.graphics.Paint r2 = r7.f22p
            r8 = 1109917696(0x42280000, float:42.0)
            float r9 = r7.scale
            float r9 = r9 * r8
            float r9 = r9 + r25
            r2.setTextSize(r9)
            android.graphics.Paint r2 = r7.f22p
            android.graphics.Paint$Align r8 = android.graphics.Paint.Align.CENTER
            r2.setTextAlign(r8)
            r8 = 60
            int r2 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1))
            if (r2 >= 0) goto L_0x02d9
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r8 = ""
            r2.append(r8)
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            goto L_0x02dd
        L_0x02d9:
            java.lang.String r0 = r7.getTimeStringFromSeconds(r0)
        L_0x02dd:
            r11 = r0
            net.sourceforge.opencamera.MyApplicationInterface r8 = r7.applicationInterface
            android.graphics.Paint r10 = r7.f22p
            r0 = 244(0xf4, float:3.42E-43)
            r1 = 67
            r2 = 54
            int r12 = android.graphics.Color.rgb(r0, r1, r2)
            r13 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r9 = r40.getWidth()
            int r14 = r9 / 2
            int r9 = r40.getHeight()
            int r15 = r9 / 2
            r9 = r40
            r8.drawTextWithBackground(r9, r10, r11, r12, r13, r14, r15)
            goto L_0x0306
        L_0x0300:
            r0 = 244(0xf4, float:3.42E-43)
            r1 = 67
            r2 = 54
        L_0x0306:
            r2 = r4
            r4 = r27
            r1 = -1
            r5 = 67
            r19 = 0
        L_0x030e:
            r21 = 1065353216(0x3f800000, float:1.0)
            goto L_0x066e
        L_0x0312:
            r1 = 67
            r2 = 54
            r15 = 244(0xf4, float:3.42E-43)
            boolean r8 = r20.isVideoRecording()
            if (r8 == 0) goto L_0x04d4
            long r8 = r20.getVideoTime()
            r10 = 1000(0x3e8, double:4.94E-321)
            long r8 = r8 / r10
            java.lang.String r0 = r7.getTimeStringFromSeconds(r8)
            android.graphics.Paint r8 = r7.f22p
            float r9 = r7.scale
            float r9 = r9 * r23
            float r9 = r9 + r25
            r8.setTextSize(r9)
            android.graphics.Paint r8 = r7.f22p
            android.graphics.Paint$Align r9 = android.graphics.Paint.Align.CENTER
            r8.setTextAlign(r9)
            int r16 = r26 * 2
            int r17 = android.graphics.Color.rgb(r15, r1, r2)
            net.sourceforge.opencamera.MainActivity r8 = r7.main_activity
            boolean r8 = r8.isScreenLocked()
            if (r8 == 0) goto L_0x0393
            net.sourceforge.opencamera.MyApplicationInterface r8 = r7.applicationInterface
            android.graphics.Paint r10 = r7.f22p
            android.content.Context r9 = r39.getContext()
            android.content.res.Resources r9 = r9.getResources()
            r11 = 2131493580(0x7f0c02cc, float:1.8610644E38)
            java.lang.String r11 = r9.getString(r11)
            r13 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r9 = r40.getWidth()
            int r14 = r9 / 2
            int r18 = r6 - r16
            r9 = r40
            r12 = r17
            r15 = r18
            r8.drawTextWithBackground(r9, r10, r11, r12, r13, r14, r15)
            int r16 = r16 + r26
            net.sourceforge.opencamera.MyApplicationInterface r8 = r7.applicationInterface
            android.graphics.Paint r10 = r7.f22p
            android.content.Context r9 = r39.getContext()
            android.content.res.Resources r9 = r9.getResources()
            r11 = 2131493579(0x7f0c02cb, float:1.8610642E38)
            java.lang.String r11 = r9.getString(r11)
            int r9 = r40.getWidth()
            int r14 = r9 / 2
            int r15 = r6 - r16
            r9 = r40
            r8.drawTextWithBackground(r9, r10, r11, r12, r13, r14, r15)
            int r16 = r16 + r26
        L_0x0393:
            boolean r8 = r20.isVideoRecordingPaused()
            if (r8 == 0) goto L_0x03a2
            r8 = 500(0x1f4, double:2.47E-321)
            long r8 = r4 / r8
            int r9 = (int) r8
            int r9 = r9 % 2
            if (r9 != 0) goto L_0x03ba
        L_0x03a2:
            net.sourceforge.opencamera.MyApplicationInterface r8 = r7.applicationInterface
            android.graphics.Paint r10 = r7.f22p
            r13 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r9 = r40.getWidth()
            int r14 = r9 / 2
            int r15 = r6 - r16
            r9 = r40
            r11 = r0
            r12 = r17
            r8.drawTextWithBackground(r9, r10, r11, r12, r13, r14, r15)
            int r16 = r16 + r26
        L_0x03ba:
            boolean r0 = r7.show_video_max_amp_pref
            if (r0 == 0) goto L_0x04c4
            boolean r0 = r20.isVideoRecordingPaused()
            if (r0 != 0) goto L_0x04c4
            boolean r0 = r7.has_video_max_amp
            if (r0 == 0) goto L_0x03d4
            long r8 = r7.last_video_max_amp_time
            r10 = 50
            long r8 = r8 + r10
            int r0 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1))
            if (r0 <= 0) goto L_0x03d2
            goto L_0x03d4
        L_0x03d2:
            r8 = 1
            goto L_0x03ef
        L_0x03d4:
            r8 = 1
            r7.has_video_max_amp = r8
            int r0 = r7.video_max_amp_prev2
            int r9 = r7.video_max_amp
            r7.video_max_amp_prev2 = r9
            int r9 = r20.getMaxAmplitude()
            r7.video_max_amp = r9
            r7.last_video_max_amp_time = r4
            int r9 = r7.video_max_amp_prev2
            if (r9 <= r0) goto L_0x03ef
            int r0 = r7.video_max_amp
            if (r9 <= r0) goto L_0x03ef
            r7.video_max_amp_peak = r9
        L_0x03ef:
            int r0 = r7.video_max_amp
            float r0 = (float) r0
            r9 = 1191181824(0x46fffe00, float:32767.0)
            float r0 = r0 / r9
            r9 = 0
            float r0 = java.lang.Math.max(r0, r9)
            r9 = 1065353216(0x3f800000, float:1.0)
            float r10 = java.lang.Math.min(r0, r9)
            int r16 = r16 + r26
            r0 = 1126170624(0x43200000, float:160.0)
            float r11 = r7.scale
            float r0 = r0 * r11
            float r0 = r0 + r25
            int r12 = (int) r0
            r0 = 1092616192(0x41200000, float:10.0)
            float r11 = r11 * r0
            float r11 = r11 + r25
            int r0 = (int) r11
            int r11 = r40.getWidth()
            int r11 = r11 - r12
            int r11 = r11 / 2
            android.graphics.Paint r13 = r7.f22p
            r14 = -1
            r13.setColor(r14)
            android.graphics.Paint r13 = r7.f22p
            android.graphics.Paint$Style r15 = android.graphics.Paint.Style.STROKE
            r13.setStyle(r15)
            android.graphics.Paint r13 = r7.f22p
            float r15 = r7.stroke_width
            r13.setStrokeWidth(r15)
            float r13 = (float) r11
            int r15 = r6 - r16
            float r8 = (float) r15
            int r11 = r11 + r12
            float r11 = (float) r11
            int r15 = r15 + r0
            float r15 = (float) r15
            android.graphics.Paint r0 = r7.f22p
            r16 = r0
            r0 = r40
            r1 = r13
            r2 = r8
            r14 = r27
            r19 = 0
            r3 = r11
            r4 = r15
            r5 = r16
            r0.drawRect(r1, r2, r3, r4, r5)
            android.graphics.Paint r0 = r7.f22p
            android.graphics.Paint$Style r1 = android.graphics.Paint.Style.FILL
            r0.setStyle(r1)
            float r12 = (float) r12
            float r0 = r10 * r12
            float r16 = r13 + r0
            android.graphics.Paint r5 = r7.f22p
            r0 = r40
            r1 = r13
            r3 = r16
            r0.drawRect(r1, r2, r3, r4, r5)
            int r0 = (r10 > r9 ? 1 : (r10 == r9 ? 0 : -1))
            if (r0 >= 0) goto L_0x0484
            android.graphics.Paint r0 = r7.f22p
            r1 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r0.setColor(r1)
            android.graphics.Paint r0 = r7.f22p
            r1 = 64
            r0.setAlpha(r1)
            float r1 = r16 + r9
            android.graphics.Paint r5 = r7.f22p
            r0 = r40
            r2 = r8
            r3 = r11
            r4 = r15
            r0.drawRect(r1, r2, r3, r4, r5)
            android.graphics.Paint r0 = r7.f22p
            r1 = 255(0xff, float:3.57E-43)
            r0.setAlpha(r1)
        L_0x0484:
            int r0 = r7.video_max_amp_peak
            int r1 = r7.video_max_amp
            if (r0 <= r1) goto L_0x04c2
            float r0 = (float) r0
            r1 = 1191181824(0x46fffe00, float:32767.0)
            float r0 = r0 / r1
            r1 = 0
            float r0 = java.lang.Math.max(r0, r1)
            float r0 = java.lang.Math.min(r0, r9)
            android.graphics.Paint r1 = r7.f22p
            r2 = -256(0xffffffffffffff00, float:NaN)
            r1.setColor(r2)
            android.graphics.Paint r1 = r7.f22p
            android.graphics.Paint$Style r2 = android.graphics.Paint.Style.STROKE
            r1.setStyle(r2)
            android.graphics.Paint r1 = r7.f22p
            float r2 = r7.stroke_width
            r1.setStrokeWidth(r2)
            float r0 = r0 * r12
            float r3 = r13 + r0
            android.graphics.Paint r5 = r7.f22p
            r0 = r40
            r1 = r3
            r2 = r8
            r4 = r15
            r0.drawLine(r1, r2, r3, r4, r5)
            android.graphics.Paint r0 = r7.f22p
            r1 = -1
            r0.setColor(r1)
            goto L_0x04cb
        L_0x04c2:
            r1 = -1
            goto L_0x04cb
        L_0x04c4:
            r14 = r27
            r1 = -1
            r9 = 1065353216(0x3f800000, float:1.0)
            r19 = 0
        L_0x04cb:
            r2 = r41
            r4 = r14
            r0 = 244(0xf4, float:3.42E-43)
            r5 = 67
            goto L_0x030e
        L_0x04d4:
            r14 = r27
            r1 = -1
            r9 = 1065353216(0x3f800000, float:1.0)
            r19 = 0
            boolean r2 = r7.taking_picture
            if (r2 == 0) goto L_0x05e0
            boolean r2 = r7.capture_started
            if (r2 == 0) goto L_0x05e0
            boolean r2 = r0.isCapturingBurst()
            if (r2 == 0) goto L_0x056c
            int r2 = r0.getNBurstTaken()
            r3 = 1
            int r2 = r2 + r3
            int r0 = r0.getBurstTotal()
            android.graphics.Paint r4 = r7.f22p
            float r5 = r7.scale
            float r5 = r5 * r23
            float r5 = r5 + r25
            r4.setTextSize(r5)
            android.graphics.Paint r4 = r7.f22p
            android.graphics.Paint$Align r5 = android.graphics.Paint.Align.CENTER
            r4.setTextAlign(r5)
            int r12 = r26 * 2
            if (r14 != 0) goto L_0x0515
            net.sourceforge.opencamera.MyApplicationInterface r4 = r7.applicationInterface
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r4 = r4.getPhotoMode()
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.FocusBracketing
            if (r4 != r5) goto L_0x0515
            int r12 = r24 * 5
        L_0x0515:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            android.content.Context r5 = r39.getContext()
            android.content.res.Resources r5 = r5.getResources()
            r8 = 2131492905(0x7f0c0029, float:1.8609275E38)
            java.lang.String r5 = r5.getString(r8)
            r4.append(r5)
            java.lang.String r5 = " "
            r4.append(r5)
            r4.append(r2)
            java.lang.String r2 = r4.toString()
            if (r0 <= 0) goto L_0x0550
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r2)
            java.lang.String r2 = " / "
            r4.append(r2)
            r4.append(r0)
            java.lang.String r0 = r4.toString()
            r11 = r0
            goto L_0x0551
        L_0x0550:
            r11 = r2
        L_0x0551:
            net.sourceforge.opencamera.MyApplicationInterface r8 = r7.applicationInterface
            android.graphics.Paint r10 = r7.f22p
            r0 = -1
            r13 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r2 = r40.getWidth()
            int r2 = r2 / 2
            int r15 = r6 - r12
            r21 = 1065353216(0x3f800000, float:1.0)
            r9 = r40
            r12 = r0
            r4 = r14
            r14 = r2
            r8.drawTextWithBackground(r9, r10, r11, r12, r13, r14, r15)
            goto L_0x05d8
        L_0x056c:
            r4 = r14
            r3 = 1
            r21 = 1065353216(0x3f800000, float:1.0)
            boolean r2 = r0.isManualISO()
            if (r2 == 0) goto L_0x05d8
            long r8 = r0.getExposureTime()
            r10 = 500000000(0x1dcd6500, double:2.47032823E-315)
            int r0 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1))
            if (r0 < 0) goto L_0x05d8
            r14 = r41
            r8 = 500(0x1f4, double:2.47E-321)
            long r8 = r14 / r8
            int r0 = (int) r8
            int r0 = r0 % 2
            if (r0 != 0) goto L_0x05d6
            android.graphics.Paint r0 = r7.f22p
            float r2 = r7.scale
            float r2 = r2 * r23
            float r2 = r2 + r25
            r0.setTextSize(r2)
            android.graphics.Paint r0 = r7.f22p
            android.graphics.Paint$Align r2 = android.graphics.Paint.Align.CENTER
            r0.setTextAlign(r2)
            int r12 = r26 * 2
            r0 = 244(0xf4, float:3.42E-43)
            r2 = 54
            r5 = 67
            int r13 = android.graphics.Color.rgb(r0, r5, r2)
            net.sourceforge.opencamera.MyApplicationInterface r8 = r7.applicationInterface
            android.graphics.Paint r10 = r7.f22p
            android.content.Context r9 = r39.getContext()
            android.content.res.Resources r9 = r9.getResources()
            r11 = 2131492905(0x7f0c0029, float:1.8609275E38)
            java.lang.String r11 = r9.getString(r11)
            r16 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r9 = r40.getWidth()
            int r17 = r9 / 2
            int r18 = r6 - r12
            r9 = r40
            r12 = r13
            r13 = r16
            r2 = r14
            r14 = r17
            r15 = r18
            r8.drawTextWithBackground(r9, r10, r11, r12, r13, r14, r15)
            goto L_0x066e
        L_0x05d6:
            r2 = r14
            goto L_0x05da
        L_0x05d8:
            r2 = r41
        L_0x05da:
            r0 = 244(0xf4, float:3.42E-43)
            r5 = 67
            goto L_0x066e
        L_0x05e0:
            r2 = r41
            r4 = r14
            r0 = 244(0xf4, float:3.42E-43)
            r5 = 67
            r21 = 1065353216(0x3f800000, float:1.0)
            boolean r8 = r7.image_queue_full
            if (r8 == 0) goto L_0x066e
            r8 = 500(0x1f4, double:2.47E-321)
            long r8 = r2 / r8
            int r9 = (int) r8
            int r9 = r9 % 2
            if (r9 != 0) goto L_0x066e
            android.graphics.Paint r8 = r7.f22p
            float r9 = r7.scale
            float r9 = r9 * r23
            float r9 = r9 + r25
            r8.setTextSize(r9)
            android.graphics.Paint r8 = r7.f22p
            android.graphics.Paint$Align r9 = android.graphics.Paint.Align.CENTER
            r8.setTextAlign(r9)
            int r12 = r26 * 2
            net.sourceforge.opencamera.MyApplicationInterface r8 = r7.applicationInterface
            net.sourceforge.opencamera.ImageSaver r8 = r8.getImageSaver()
            int r8 = r8.getNRealImagesToSave()
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            android.content.Context r10 = r39.getContext()
            android.content.res.Resources r10 = r10.getResources()
            r11 = 2131493545(0x7f0c02a9, float:1.8610573E38)
            java.lang.String r10 = r10.getString(r11)
            r9.append(r10)
            java.lang.String r10 = " ("
            r9.append(r10)
            r9.append(r8)
            java.lang.String r8 = " "
            r9.append(r8)
            android.content.Context r8 = r39.getContext()
            android.content.res.Resources r8 = r8.getResources()
            r10 = 2131493548(0x7f0c02ac, float:1.861058E38)
            java.lang.String r8 = r8.getString(r10)
            r9.append(r8)
            java.lang.String r8 = ")"
            r9.append(r8)
            java.lang.String r11 = r9.toString()
            net.sourceforge.opencamera.MyApplicationInterface r8 = r7.applicationInterface
            android.graphics.Paint r10 = r7.f22p
            r13 = -3355444(0xffffffffffcccccc, float:NaN)
            r14 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r9 = r40.getWidth()
            int r15 = r9 / 2
            int r16 = r6 - r12
            r9 = r40
            r12 = r13
            r13 = r14
            r14 = r15
            r15 = r16
            r8.drawTextWithBackground(r9, r10, r11, r12, r13, r14, r15)
        L_0x066e:
            boolean r8 = r20.supportsZoom()
            if (r8 == 0) goto L_0x06da
            boolean r8 = r7.show_zoom_pref
            if (r8 == 0) goto L_0x06da
            float r8 = r20.getZoomRatio()
            r9 = 1065353300(0x3f800054, float:1.00001)
            int r9 = (r8 > r9 ? 1 : (r8 == r9 ? 0 : -1))
            if (r9 <= 0) goto L_0x06da
            android.graphics.Paint r9 = r7.f22p
            float r10 = r7.scale
            float r10 = r10 * r23
            float r10 = r10 + r25
            r9.setTextSize(r10)
            android.graphics.Paint r9 = r7.f22p
            android.graphics.Paint$Align r10 = android.graphics.Paint.Align.CENTER
            r9.setTextAlign(r10)
            net.sourceforge.opencamera.MyApplicationInterface r9 = r7.applicationInterface
            android.graphics.Paint r10 = r7.f22p
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            android.content.Context r12 = r39.getContext()
            android.content.res.Resources r12 = r12.getResources()
            r13 = 2131493637(0x7f0c0305, float:1.861076E38)
            java.lang.String r12 = r12.getString(r13)
            r11.append(r12)
            java.lang.String r12 = ": "
            r11.append(r12)
            r11.append(r8)
            java.lang.String r8 = "x"
            r11.append(r8)
            java.lang.String r11 = r11.toString()
            r12 = -1
            r13 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r8 = r40.getWidth()
            int r14 = r8 / 2
            int r15 = r6 - r26
            net.sourceforge.opencamera.MyApplicationInterface$Alignment r16 = net.sourceforge.opencamera.MyApplicationInterface.Alignment.ALIGNMENT_BOTTOM
            java.lang.String r8 = r7.ybounds_text
            net.sourceforge.opencamera.MyApplicationInterface$Shadow r18 = net.sourceforge.opencamera.MyApplicationInterface.Shadow.SHADOW_OUTLINE
            r17 = r8
            r8 = r9
            r9 = r40
            r8.drawTextWithBackground(r9, r10, r11, r12, r13, r14, r15, r16, r17, r18)
        L_0x06da:
            r13 = r40
            r8 = r6
            r6 = 244(0xf4, float:3.42E-43)
            goto L_0x07b0
        L_0x06e1:
            r1 = -1
            r6 = 244(0xf4, float:3.42E-43)
            r19 = 0
            r21 = 1065353216(0x3f800000, float:1.0)
            r37 = r4
            r4 = r3
            r2 = r37
            r5 = 67
            if (r0 != 0) goto L_0x07ad
            android.graphics.Paint r0 = r7.f22p
            r0.setColor(r1)
            android.graphics.Paint r0 = r7.f22p
            float r8 = r7.scale
            float r8 = r8 * r23
            float r8 = r8 + r25
            r0.setTextSize(r8)
            android.graphics.Paint r0 = r7.f22p
            android.graphics.Paint$Align r8 = android.graphics.Paint.Align.CENTER
            r0.setTextAlign(r8)
            r0 = 1101004800(0x41a00000, float:20.0)
            float r8 = r7.scale
            float r8 = r8 * r0
            float r8 = r8 + r25
            int r0 = (int) r8
            boolean r8 = r20.hasPermissions()
            if (r8 == 0) goto L_0x0788
            boolean r8 = r20.openCameraFailed()
            if (r8 == 0) goto L_0x07ad
            android.content.Context r8 = r39.getContext()
            android.content.res.Resources r8 = r8.getResources()
            r9 = 2131492970(0x7f0c006a, float:1.8609407E38)
            java.lang.String r8 = r8.getString(r9)
            int r9 = r40.getWidth()
            float r9 = (float) r9
            r10 = 1073741824(0x40000000, float:2.0)
            float r9 = r9 / r10
            int r11 = r40.getHeight()
            float r11 = (float) r11
            float r11 = r11 / r10
            android.graphics.Paint r12 = r7.f22p
            r13 = r40
            r13.drawText(r8, r9, r11, r12)
            android.content.Context r8 = r39.getContext()
            android.content.res.Resources r8 = r8.getResources()
            r9 = 2131492971(0x7f0c006b, float:1.8609409E38)
            java.lang.String r8 = r8.getString(r9)
            int r9 = r40.getWidth()
            float r9 = (float) r9
            float r9 = r9 / r10
            int r11 = r40.getHeight()
            float r11 = (float) r11
            float r11 = r11 / r10
            float r12 = (float) r0
            float r11 = r11 + r12
            android.graphics.Paint r12 = r7.f22p
            r13.drawText(r8, r9, r11, r12)
            android.content.Context r8 = r39.getContext()
            android.content.res.Resources r8 = r8.getResources()
            r9 = 2131492972(0x7f0c006c, float:1.860941E38)
            java.lang.String r8 = r8.getString(r9)
            int r9 = r40.getWidth()
            float r9 = (float) r9
            float r9 = r9 / r10
            int r11 = r40.getHeight()
            float r11 = (float) r11
            float r11 = r11 / r10
            int r0 = r0 * 2
            float r0 = (float) r0
            float r11 = r11 + r0
            android.graphics.Paint r0 = r7.f22p
            r13.drawText(r8, r9, r11, r0)
            goto L_0x07af
        L_0x0788:
            r13 = r40
            android.content.Context r0 = r39.getContext()
            android.content.res.Resources r0 = r0.getResources()
            r8 = 2131493025(0x7f0c00a1, float:1.8609518E38)
            java.lang.String r0 = r0.getString(r8)
            int r8 = r40.getWidth()
            float r8 = (float) r8
            r9 = 1073741824(0x40000000, float:2.0)
            float r8 = r8 / r9
            int r10 = r40.getHeight()
            float r10 = (float) r10
            float r10 = r10 / r9
            android.graphics.Paint r9 = r7.f22p
            r13.drawText(r0, r8, r10, r9)
            goto L_0x07af
        L_0x07ad:
            r13 = r40
        L_0x07af:
            r8 = 0
        L_0x07b0:
            float r0 = r7.scale
            r9 = 1084227584(0x40a00000, float:5.0)
            float r10 = r0 * r9
            float r10 = r10 + r25
            int r10 = (int) r10
            float r0 = r0 * r9
            float r0 = r0 + r25
            int r0 = (int) r0
            net.sourceforge.opencamera.MainActivity r11 = r7.main_activity
            net.sourceforge.opencamera.ui.MainUI r11 = r11.getMainUI()
            android.view.View r11 = r11.getTopIcon()
            if (r11 == 0) goto L_0x07f5
            int[] r12 = r7.gui_location
            r11.getLocationOnScreen(r12)
            int[] r12 = r7.gui_location
            r12 = r12[r19]
            int r11 = r11.getWidth()
            int r12 = r12 + r11
            android.view.View r11 = r20.getView()
            int[] r14 = r7.gui_location
            r11.getLocationOnScreen(r14)
            int[] r11 = r7.gui_location
            r11 = r11[r19]
            int r12 = r12 - r11
            if (r12 <= 0) goto L_0x07f5
            r11 = 90
            if (r4 == r11) goto L_0x07f3
            r14 = 270(0x10e, float:3.78E-43)
            if (r4 != r14) goto L_0x07f1
            goto L_0x07f3
        L_0x07f1:
            int r10 = r10 + r12
            goto L_0x07f7
        L_0x07f3:
            int r0 = r0 + r12
            goto L_0x07f7
        L_0x07f5:
            r11 = 90
        L_0x07f7:
            r12 = r0
            float r0 = r7.scale
            float r14 = r0 * r9
            float r14 = r14 + r25
            int r14 = (int) r14
            int r14 = r14 + r12
            float r0 = r0 * r9
            float r0 = r0 + r25
            int r0 = (int) r0
            int r9 = r0 * 4
            if (r4 == r11) goto L_0x0811
            r15 = 270(0x10e, float:3.78E-43)
            if (r4 != r15) goto L_0x080e
            goto L_0x0811
        L_0x080e:
            r16 = r10
            goto L_0x0820
        L_0x0811:
            int r15 = r40.getWidth()
            int r16 = r40.getHeight()
            int r15 = r15 - r16
            int r15 = r15 / 2
            int r16 = r10 + r15
            int r14 = r14 - r15
        L_0x0820:
            if (r4 != r11) goto L_0x0829
            int r11 = r40.getHeight()
            int r11 = r11 - r14
            int r14 = r11 - r9
        L_0x0829:
            r11 = 180(0xb4, float:2.52E-43)
            if (r4 != r11) goto L_0x0835
            int r4 = r40.getWidth()
            int r4 = r4 - r16
            int r16 = r4 - r0
        L_0x0835:
            r4 = r16
            boolean r11 = r7.show_battery_pref
            if (r11 == 0) goto L_0x0907
            boolean r11 = r7.has_battery_frac
            if (r11 == 0) goto L_0x084c
            long r5 = r7.last_battery_time
            r15 = 60000(0xea60, double:2.9644E-319)
            long r5 = r5 + r15
            int r11 = (r2 > r5 ? 1 : (r2 == r5 ? 0 : -1))
            if (r11 <= 0) goto L_0x084a
            goto L_0x084c
        L_0x084a:
            r5 = 1
            goto L_0x086b
        L_0x084c:
            net.sourceforge.opencamera.MainActivity r5 = r7.main_activity
            r6 = 0
            android.content.IntentFilter r11 = r7.battery_ifilter
            android.content.Intent r5 = r5.registerReceiver(r6, r11)
            java.lang.String r6 = "level"
            int r6 = r5.getIntExtra(r6, r1)
            java.lang.String r11 = "scale"
            int r1 = r5.getIntExtra(r11, r1)
            r5 = 1
            r7.has_battery_frac = r5
            float r6 = (float) r6
            float r1 = (float) r1
            float r6 = r6 / r1
            r7.battery_frac = r6
            r7.last_battery_time = r2
        L_0x086b:
            float r1 = r7.battery_frac
            r6 = 1028443341(0x3d4ccccd, float:0.05)
            int r1 = (r1 > r6 ? 1 : (r1 == r6 ? 0 : -1))
            if (r1 > 0) goto L_0x0882
            r15 = 1000(0x3e8, double:4.94E-321)
            long r15 = r2 / r15
            r17 = 2
            long r15 = r15 % r17
            r17 = 0
            int r1 = (r15 > r17 ? 1 : (r15 == r17 ? 0 : -1))
            if (r1 != 0) goto L_0x0884
        L_0x0882:
            r19 = 1
        L_0x0884:
            if (r19 == 0) goto L_0x08fd
            android.graphics.Paint r1 = r7.f22p
            float r5 = r7.battery_frac
            r6 = 1041865114(0x3e19999a, float:0.15)
            int r5 = (r5 > r6 ? 1 : (r5 == r6 ? 0 : -1))
            if (r5 <= 0) goto L_0x089c
            r5 = 37
            r6 = 155(0x9b, float:2.17E-43)
            r11 = 36
            int r5 = android.graphics.Color.rgb(r5, r6, r11)
            goto L_0x08a6
        L_0x089c:
            r5 = 54
            r6 = 67
            r11 = 244(0xf4, float:3.42E-43)
            int r5 = android.graphics.Color.rgb(r11, r6, r5)
        L_0x08a6:
            r1.setColor(r5)
            android.graphics.Paint r1 = r7.f22p
            android.graphics.Paint$Style r5 = android.graphics.Paint.Style.FILL
            r1.setStyle(r5)
            float r6 = (float) r4
            float r11 = (float) r14
            float r1 = r7.battery_frac
            float r1 = r21 - r1
            int r5 = r9 + -2
            float r15 = (float) r5
            float r1 = r1 * r15
            float r5 = r11 + r1
            int r4 = r4 + r0
            float r4 = (float) r4
            int r14 = r14 + r9
            float r9 = (float) r14
            android.graphics.Paint r14 = r7.f22p
            r0 = r40
            r1 = r6
            r2 = r5
            r3 = r4
            r16 = r4
            r4 = r9
            r5 = r14
            r0.drawRect(r1, r2, r3, r4, r5)
            float r0 = r7.battery_frac
            int r0 = (r0 > r21 ? 1 : (r0 == r21 ? 0 : -1))
            if (r0 >= 0) goto L_0x08fd
            android.graphics.Paint r0 = r7.f22p
            r1 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r0.setColor(r1)
            android.graphics.Paint r0 = r7.f22p
            r1 = 64
            r0.setAlpha(r1)
            float r0 = r7.battery_frac
            float r0 = r21 - r0
            float r0 = r0 * r15
            float r4 = r11 + r0
            android.graphics.Paint r5 = r7.f22p
            r0 = r40
            r1 = r6
            r2 = r11
            r3 = r16
            r0.drawRect(r1, r2, r3, r4, r5)
            android.graphics.Paint r0 = r7.f22p
            r1 = 255(0xff, float:3.57E-43)
            r0.setAlpha(r1)
        L_0x08fd:
            r0 = 1092616192(0x41200000, float:10.0)
            float r1 = r7.scale
            float r1 = r1 * r0
            float r1 = r1 + r25
            int r0 = (int) r1
            int r10 = r10 + r0
        L_0x0907:
            r2 = r10
            r0 = r39
            r1 = r40
            r3 = r12
            r4 = r8
            r5 = r41
            r0.onDrawInfoLines(r1, r2, r3, r4, r5)
            r40.restore()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.DrawPreview.drawUI(android.graphics.Canvas, long):void");
    }

    private void drawAngleLines(Canvas canvas, long j) {
        boolean z;
        Preview preview;
        boolean z2;
        int i;
        int i2;
        double d;
        int i3;
        float f;
        int i4;
        float f2;
        int i5;
        float f3;
        float f4;
        int i6;
        int i7;
        float f5;
        int i8;
        int i9;
        int i10;
        int i11;
        float f6;
        float f7;
        Canvas canvas2 = canvas;
        Preview preview2 = this.main_activity.getPreview();
        CameraController cameraController = preview2.getCameraController();
        boolean hasLevelAngle = preview2.hasLevelAngle();
        if (this.photoMode == PhotoMode.Panorama) {
            z = !this.main_activity.getApplicationInterface().getGyroSensor().isRecording();
        } else {
            z = this.show_angle_line_pref;
        }
        if (cameraController != null && !preview2.isPreviewPaused() && hasLevelAngle) {
            if (z || this.show_pitch_lines_pref || this.show_geo_direction_lines_pref) {
                int uIRotation = preview2.getUIRotation();
                double levelAngle = preview2.getLevelAngle();
                boolean hasPitchAngle = preview2.hasPitchAngle();
                double pitchAngle = preview2.getPitchAngle();
                boolean hasGeoDirection = preview2.hasGeoDirection();
                double geoDirection = preview2.getGeoDirection();
                int i12 = (int) ((((float) ((uIRotation == 90 || uIRotation == 270) ? 60 : 80)) * this.scale) + 0.5f);
                double d2 = -preview2.getOrigLevelAngle();
                int rotation = this.main_activity.getWindowManager().getDefaultDisplay().getRotation();
                if (rotation == 1 || rotation == 3) {
                    d2 -= 90.0d;
                }
                int width = canvas.getWidth() / 2;
                int height = canvas.getHeight() / 2;
                boolean z3 = hasLevelAngle && Math.abs(levelAngle) <= close_level_angle;
                if (z3) {
                    double d3 = (double) i12;
                    Double.isNaN(d3);
                    i12 = (int) (d3 * 1.2d);
                }
                canvas.save();
                float f8 = (float) d2;
                float f9 = (float) width;
                float f10 = (float) height;
                canvas2.rotate(f8, f9, f10);
                float f11 = (this.scale * 0.5f) + 0.5f;
                this.f22p.setStyle(Style.FILL);
                if (!z || !preview2.hasLevelAngleStable()) {
                    i3 = height;
                    i2 = width;
                    preview = preview2;
                    i = uIRotation;
                    z2 = hasPitchAngle;
                    d = pitchAngle;
                } else {
                    this.f22p.setColor(ViewCompat.MEASURED_STATE_MASK);
                    this.f22p.setAlpha(64);
                    float f12 = (float) (width - i12);
                    float f13 = f12 - f11;
                    i3 = height;
                    float f14 = f11 * 2.0f;
                    d = pitchAngle;
                    float f15 = (float) (width + i12);
                    i2 = width;
                    float f16 = f15 + f11;
                    i = uIRotation;
                    this.draw_rect.set(f13, f10 - f14, f16, f10 + f14);
                    canvas2.drawRoundRect(this.draw_rect, f14, f14, this.f22p);
                    float f17 = ((float) i12) / 2.0f;
                    float f18 = f10 - f17;
                    z2 = hasPitchAngle;
                    preview = preview2;
                    float f19 = f17 + f10;
                    float f20 = f14;
                    this.draw_rect.set(f9 - f14, f18 - f11, f9 + f14, f19 + f11);
                    canvas2.drawRoundRect(this.draw_rect, f11, f11, this.f22p);
                    if (z3) {
                        this.f22p.setColor(this.angle_highlight_color_pref);
                    } else {
                        this.f22p.setColor(-1);
                    }
                    this.f22p.setAlpha(160);
                    this.draw_rect.set(f12, f10 - f11, f15, f10 + f11);
                    canvas2.drawRoundRect(this.draw_rect, f11, f11, this.f22p);
                    this.draw_rect.set(f9 - f11, f18, f9 + f11, f19);
                    canvas2.drawRoundRect(this.draw_rect, f11, f11, this.f22p);
                    if (z3) {
                        this.f22p.setColor(ViewCompat.MEASURED_STATE_MASK);
                        this.f22p.setAlpha(64);
                        this.draw_rect.set(f13, f10 - (7.0f * f11), f16, f10 - (3.0f * f11));
                        float f21 = f20;
                        canvas2.drawRoundRect(this.draw_rect, f21, f21, this.f22p);
                        this.f22p.setColor(this.angle_highlight_color_pref);
                        this.f22p.setAlpha(160);
                        this.draw_rect.set(f12, f10 - (6.0f * f11), f15, f10 - (f11 * 4.0f));
                        canvas2.drawRoundRect(this.draw_rect, f11, f11, this.f22p);
                    }
                }
                updateCachedViewAngles(j);
                float f22 = this.view_angle_x_preview;
                float f23 = this.view_angle_y_preview;
                double width2 = (double) canvas.getWidth();
                double d4 = (double) f22;
                Double.isNaN(d4);
                double tan = Math.tan(Math.toRadians(d4 / 2.0d)) * 2.0d;
                Double.isNaN(width2);
                float f24 = (float) (width2 / tan);
                double height2 = (double) canvas.getHeight();
                double d5 = (double) f23;
                Double.isNaN(d5);
                double tan2 = Math.tan(Math.toRadians(d5 / 2.0d)) * 2.0d;
                Double.isNaN(height2);
                float f25 = (float) (height2 / tan2);
                float sqrt = ((float) Math.sqrt((double) ((f24 * f24) + (f25 * f25)))) * preview.getZoomRatio();
                if (!z2 || !this.show_pitch_lines_pref) {
                    f = f11;
                    f2 = f9;
                    i4 = i3;
                    i5 = i;
                } else {
                    i5 = i;
                    int i13 = (int) ((((float) ((i5 == 90 || i5 == 270) ? 100 : 80)) * this.scale) + 0.5f);
                    int i14 = preview.getZoomRatio() >= 2.0f ? 5 : 10;
                    int i15 = 90;
                    int i16 = -90;
                    while (i16 <= i15) {
                        double d6 = (double) i16;
                        Double.isNaN(d6);
                        double d7 = d - d6;
                        if (Math.abs(d7) < 90.0d) {
                            float tan3 = ((float) Math.tan(Math.toRadians(d7))) * sqrt;
                            this.f22p.setColor(ViewCompat.MEASURED_STATE_MASK);
                            this.f22p.setAlpha(64);
                            float f26 = (float) (i2 - i13);
                            float f27 = tan3 + f10;
                            float f28 = f11 * 2.0f;
                            float f29 = f27 - f28;
                            float f30 = (float) (i2 + i13);
                            float f31 = f10;
                            float f32 = f9;
                            this.draw_rect.set(f26 - f11, f29, f30 + f11, f27 + f28);
                            canvas2.drawRoundRect(this.draw_rect, f28, f28, this.f22p);
                            this.f22p.setColor(-1);
                            this.f22p.setTextAlign(Align.LEFT);
                            if (i16 == 0 && Math.abs(d) < close_level_angle) {
                                this.f22p.setAlpha(255);
                            } else if (i16 == 90 && Math.abs(d - 90.0d) < 3.0d) {
                                this.f22p.setAlpha(255);
                            } else if (i16 != -90 || Math.abs(d + 90.0d) >= 3.0d) {
                                this.f22p.setAlpha(160);
                            } else {
                                this.f22p.setAlpha(255);
                            }
                            this.draw_rect.set(f26, f27 - f11, f30, f27 + f11);
                            canvas2.drawRoundRect(this.draw_rect, f11, f11, this.f22p);
                            MyApplicationInterface myApplicationInterface = this.applicationInterface;
                            Paint paint = this.f22p;
                            StringBuilder sb = new StringBuilder();
                            sb.append(BuildConfig.FLAVOR);
                            sb.append(i16);
                            sb.append("°");
                            i10 = i3;
                            i9 = i2;
                            i11 = i16;
                            i8 = i13;
                            f5 = f11;
                            int i17 = (int) (f30 + (f11 * 4.0f));
                            f7 = f31;
                            int i18 = (int) f29;
                            f6 = f32;
                            myApplicationInterface.drawTextWithBackground(canvas, paint, sb.toString(), this.f22p.getColor(), ViewCompat.MEASURED_STATE_MASK, i17, i18, Alignment.ALIGNMENT_CENTRE);
                        } else {
                            i11 = i16;
                            f5 = f11;
                            f7 = f10;
                            f6 = f9;
                            i10 = i3;
                            i9 = i2;
                            i8 = i13;
                        }
                        i16 = i11 + i14;
                        f10 = f7;
                        f9 = f6;
                        i2 = i9;
                        i13 = i8;
                        f11 = f5;
                        i15 = 90;
                        i3 = i10;
                    }
                    f = f11;
                    f2 = f9;
                    i4 = i3;
                }
                if (hasGeoDirection && z2 && this.show_geo_direction_lines_pref) {
                    int i19 = (int) ((((float) ((i5 == 90 || i5 == 270) ? 80 : 100)) * this.scale) + 0.5f);
                    float degrees = (float) Math.toDegrees(geoDirection);
                    int i20 = preview.getZoomRatio() >= 2.0f ? 5 : 10;
                    int i21 = 0;
                    while (i21 < 360) {
                        double d8 = (double) (((float) i21) - degrees);
                        while (d8 >= 360.0d) {
                            d8 -= 360.0d;
                        }
                        while (d8 < -360.0d) {
                            d8 += 360.0d;
                        }
                        if (d8 > 180.0d) {
                            d8 = -(360.0d - d8);
                        }
                        if (Math.abs(d8) < 90.0d) {
                            float tan4 = ((float) Math.tan(Math.toRadians(d8))) * sqrt;
                            this.f22p.setColor(ViewCompat.MEASURED_STATE_MASK);
                            this.f22p.setAlpha(64);
                            float f33 = tan4 + f2;
                            float f34 = f;
                            float f35 = f34 * 2.0f;
                            float f36 = (float) (i4 - i19);
                            f3 = sqrt;
                            float f37 = (float) (i4 + i19);
                            i7 = i19;
                            this.draw_rect.set(f33 - f35, f36 - f34, f33 + f35, f37 + f34);
                            canvas2.drawRoundRect(this.draw_rect, f35, f35, this.f22p);
                            this.f22p.setColor(-1);
                            this.f22p.setTextAlign(Align.CENTER);
                            this.f22p.setAlpha(160);
                            this.draw_rect.set(f33 - f34, f36, f33 + f34, f37);
                            canvas2.drawRoundRect(this.draw_rect, f34, f34, this.f22p);
                            MyApplicationInterface myApplicationInterface2 = this.applicationInterface;
                            Paint paint2 = this.f22p;
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append(BuildConfig.FLAVOR);
                            sb2.append(i21);
                            sb2.append("°");
                            f4 = f34;
                            i6 = i21;
                            myApplicationInterface2.drawTextWithBackground(canvas, paint2, sb2.toString(), this.f22p.getColor(), ViewCompat.MEASURED_STATE_MASK, (int) f33, (int) (f36 - (f34 * 4.0f)), Alignment.ALIGNMENT_BOTTOM);
                        } else {
                            i6 = i21;
                            f3 = sqrt;
                            i7 = i19;
                            f4 = f;
                        }
                        i21 = i6 + i20;
                        sqrt = f3;
                        i19 = i7;
                        f = f4;
                    }
                }
                this.f22p.setAlpha(255);
                this.f22p.setStyle(Style.FILL);
                canvas.restore();
            }
        }
    }

    private void doThumbnailAnimation(Canvas canvas, long j) {
        Preview preview = this.main_activity.getPreview();
        if (preview.getCameraController() != null && this.thumbnail_anim && this.last_thumbnail != null) {
            int uIRotation = preview.getUIRotation();
            long j2 = j - this.thumbnail_anim_start_ms;
            if (j2 > 500) {
                this.thumbnail_anim = false;
                return;
            }
            RectF rectF = this.thumbnail_anim_src_rect;
            rectF.left = 0.0f;
            rectF.top = 0.0f;
            rectF.right = (float) this.last_thumbnail.getWidth();
            this.thumbnail_anim_src_rect.bottom = (float) this.last_thumbnail.getHeight();
            View findViewById = this.main_activity.findViewById(C0316R.C0318id.gallery);
            float f = ((float) j2) / 500.0f;
            float f2 = 1.0f - f;
            int width = (int) ((((float) (canvas.getWidth() / 2)) * f2) + (((float) (findViewById.getLeft() + (findViewById.getWidth() / 2))) * f));
            int height = (int) ((f2 * ((float) (canvas.getHeight() / 2))) + (((float) (findViewById.getTop() + (findViewById.getHeight() / 2))) * f));
            float width2 = (float) canvas.getWidth();
            float height2 = (float) canvas.getHeight();
            int width3 = (int) (width2 / ((((width2 / ((float) findViewById.getWidth())) - 1.0f) * f) + 1.0f));
            int height3 = (int) (height2 / ((f * ((height2 / ((float) findViewById.getHeight())) - 1.0f)) + 1.0f));
            RectF rectF2 = this.thumbnail_anim_dst_rect;
            float f3 = (float) width;
            float f4 = ((float) width3) / 2.0f;
            rectF2.left = f3 - f4;
            float f5 = (float) height;
            float f6 = ((float) height3) / 2.0f;
            rectF2.top = f5 - f6;
            rectF2.right = f3 + f4;
            rectF2.bottom = f5 + f6;
            this.thumbnail_anim_matrix.setRectToRect(this.thumbnail_anim_src_rect, rectF2, ScaleToFit.FILL);
            if (uIRotation == 90 || uIRotation == 270) {
                float width4 = ((float) this.last_thumbnail.getWidth()) / ((float) this.last_thumbnail.getHeight());
                this.thumbnail_anim_matrix.preScale(width4, 1.0f / width4, ((float) this.last_thumbnail.getWidth()) / 2.0f, ((float) this.last_thumbnail.getHeight()) / 2.0f);
            }
            this.thumbnail_anim_matrix.preRotate((float) uIRotation, ((float) this.last_thumbnail.getWidth()) / 2.0f, ((float) this.last_thumbnail.getHeight()) / 2.0f);
            canvas.drawBitmap(this.last_thumbnail, this.thumbnail_anim_matrix, this.f22p);
        }
    }

    private void doFocusAnimation(Canvas canvas, long j) {
        int i;
        int i2;
        float f;
        float f2;
        float f3;
        float f4;
        Preview preview = this.main_activity.getPreview();
        if (preview.getCameraController() == null || !this.continuous_focus_moving || this.taking_picture) {
            Canvas canvas2 = canvas;
        } else {
            long j2 = j - this.continuous_focus_moving_ms;
            if (j2 <= 1000) {
                float f5 = ((float) j2) / 1000.0f;
                float width = ((float) canvas.getWidth()) / 2.0f;
                float height = ((float) canvas.getHeight()) / 2.0f;
                float f6 = this.scale;
                float f7 = (f6 * 40.0f) + 0.5f;
                float f8 = (f6 * 60.0f) + 0.5f;
                if (f5 < 0.5f) {
                    float f9 = f5 * 2.0f;
                    f3 = (1.0f - f9) * f7;
                    f4 = f9 * f8;
                } else {
                    float f10 = (f5 - 0.5f) * 2.0f;
                    f3 = (1.0f - f10) * f8;
                    f4 = f10 * f7;
                }
                float f11 = f3 + f4;
                this.f22p.setColor(-1);
                this.f22p.setStyle(Style.STROKE);
                this.f22p.setStrokeWidth(this.stroke_width);
                canvas.drawCircle(width, height, f11, this.f22p);
                this.f22p.setStyle(Style.FILL);
            } else {
                Canvas canvas3 = canvas;
                clearContinuousFocusMove();
            }
        }
        if (preview.isFocusWaiting() || preview.isFocusRecentSuccess() || preview.isFocusRecentFailure()) {
            long timeSinceStartedAutoFocus = preview.timeSinceStartedAutoFocus();
            float f12 = this.scale;
            float f13 = (40.0f * f12) + 0.5f;
            float f14 = (f12 * 45.0f) + 0.5f;
            if (timeSinceStartedAutoFocus > 0) {
                float f15 = ((float) timeSinceStartedAutoFocus) / 500.0f;
                if (f15 > 1.0f) {
                    f15 = 1.0f;
                }
                if (f15 < 0.5f) {
                    float f16 = f15 * 2.0f;
                    f2 = (1.0f - f16) * f13;
                    f = f16 * f14;
                } else {
                    float f17 = (f15 - 0.5f) * 2.0f;
                    f2 = (1.0f - f17) * f14;
                    f = f17 * f13;
                }
                f13 = f2 + f;
            }
            int i3 = (int) f13;
            if (preview.isFocusRecentSuccess()) {
                this.f22p.setColor(Color.rgb(20, 231, 21));
            } else if (preview.isFocusRecentFailure()) {
                this.f22p.setColor(Color.rgb(244, 67, 54));
            } else {
                this.f22p.setColor(-1);
            }
            this.f22p.setStyle(Style.STROKE);
            this.f22p.setStrokeWidth(this.stroke_width);
            if (preview.hasFocusArea()) {
                Pair focusPos = preview.getFocusPos();
                i = ((Integer) focusPos.first).intValue();
                i2 = ((Integer) focusPos.second).intValue();
            } else {
                i = canvas.getWidth() / 2;
                i2 = canvas.getHeight() / 2;
            }
            float f18 = (float) (i - i3);
            float f19 = (float) (i2 - i3);
            float f20 = (float) i;
            float f21 = ((float) i3) * 0.5f;
            float f22 = f20 - f21;
            Canvas canvas4 = canvas;
            float f23 = f19;
            float f24 = f19;
            canvas4.drawLine(f18, f23, f22, f24, this.f22p);
            float f25 = f20 + f21;
            float f26 = (float) (i + i3);
            canvas4.drawLine(f25, f23, f26, f24, this.f22p);
            float f27 = (float) (i3 + i2);
            float f28 = f27;
            float f29 = f27;
            canvas4.drawLine(f18, f28, f22, f29, this.f22p);
            canvas4.drawLine(f25, f28, f26, f29, this.f22p);
            float f30 = (float) i2;
            float f31 = f30 - f21;
            float f32 = f18;
            float f33 = f18;
            canvas4.drawLine(f32, f19, f33, f31, this.f22p);
            float f34 = f30 + f21;
            canvas4.drawLine(f32, f34, f33, f27, this.f22p);
            float f35 = f26;
            float f36 = f26;
            canvas4.drawLine(f35, f19, f36, f31, this.f22p);
            canvas4.drawLine(f35, f34, f36, f27, this.f22p);
            this.f22p.setStyle(Style.FILL);
        }
    }

    public void onDrawPreview(Canvas canvas) {
        int i;
        int i2;
        float f;
        Object obj;
        Canvas canvas2 = canvas;
        if (!this.has_settings) {
            updateSettings();
        }
        Preview preview = this.main_activity.getPreview();
        CameraController cameraController = preview.getCameraController();
        int uIRotation = preview.getUIRotation();
        long currentTimeMillis = System.currentTimeMillis();
        boolean z = this.want_histogram || this.want_zebra_stripes || this.want_focus_peaking;
        if (z != preview.isPreviewBitmapEnabled()) {
            if (z) {
                preview.enablePreviewBitmap();
            } else {
                preview.disablePreviewBitmap();
            }
        }
        if (z) {
            if (this.want_histogram) {
                preview.enableHistogram(this.histogram_type);
            } else {
                preview.disableHistogram();
            }
            if (this.want_zebra_stripes) {
                preview.enableZebraStripes(this.zebra_stripes_threshold);
            } else {
                preview.disableZebraStripes();
            }
            if (this.want_focus_peaking) {
                preview.enableFocusPeaking();
            } else {
                preview.disableFocusPeaking();
            }
        }
        if (preview.usingCamera2API() && (cameraController == null || cameraController.shouldCoverPreview())) {
            this.f22p.setColor(ViewCompat.MEASURED_STATE_MASK);
            canvas.drawRect(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight(), this.f22p);
        }
        if (cameraController == null || !this.front_screen_flash) {
            i = -1;
            if ("flash_frontscreen_torch".equals(preview.getCurrentFlashValue())) {
                this.f22p.setColor(-1);
                this.f22p.setAlpha(200);
                canvas.drawRect(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight(), this.f22p);
                this.f22p.setAlpha(255);
            }
        } else {
            this.f22p.setColor(-1);
            i = -1;
            canvas.drawRect(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight(), this.f22p);
        }
        if (!this.main_activity.getMainUI().inImmersiveMode() || !this.immersive_mode_everything_pref) {
            if (cameraController != null && this.taking_picture && !this.front_screen_flash && this.take_photo_border_pref) {
                this.f22p.setColor(i);
                this.f22p.setStyle(Style.STROKE);
                this.f22p.setStrokeWidth(this.stroke_width);
                this.f22p.setStrokeWidth((this.scale * 5.0f) + 0.5f);
                canvas.drawRect(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight(), this.f22p);
                this.f22p.setStyle(Style.FILL);
                this.f22p.setStrokeWidth(this.stroke_width);
            }
            drawGrids(canvas);
            drawCropGuides(canvas);
            if (this.last_thumbnail == null || this.last_thumbnail_is_video || cameraController == null || (!this.show_last_image && (!this.allow_ghost_last_image || this.front_screen_flash || !this.ghost_image_pref.equals("preference_ghost_image_last")))) {
                i2 = 127;
                if (cameraController != null && !this.front_screen_flash) {
                    Bitmap bitmap = this.ghost_selected_image_bitmap;
                    if (bitmap != null) {
                        setLastImageMatrix(canvas2, bitmap, uIRotation, true);
                        this.f22p.setAlpha(127);
                        canvas2.drawBitmap(this.ghost_selected_image_bitmap, this.last_image_matrix, this.f22p);
                        this.f22p.setAlpha(255);
                    }
                }
            } else {
                if (this.show_last_image) {
                    this.f22p.setColor(Color.rgb(0, 0, 0));
                    i2 = 127;
                    canvas.drawRect(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight(), this.f22p);
                } else {
                    i2 = 127;
                }
                setLastImageMatrix(canvas2, this.last_thumbnail, uIRotation, !this.show_last_image);
                if (!this.show_last_image) {
                    this.f22p.setAlpha(i2);
                }
                canvas2.drawBitmap(this.last_thumbnail, this.last_image_matrix, this.f22p);
                if (!this.show_last_image) {
                    this.f22p.setAlpha(255);
                }
            }
            if (preview.isPreviewBitmapEnabled()) {
                Bitmap zebraStripesBitmap = preview.getZebraStripesBitmap();
                if (zebraStripesBitmap != null) {
                    setLastImageMatrix(canvas2, zebraStripesBitmap, 0, false);
                    this.f22p.setAlpha(255);
                    canvas2.drawBitmap(zebraStripesBitmap, this.last_image_matrix, this.f22p);
                }
                Bitmap focusPeakingBitmap = preview.getFocusPeakingBitmap();
                if (focusPeakingBitmap != null) {
                    setLastImageMatrix(canvas2, focusPeakingBitmap, 0, false);
                    this.f22p.setAlpha(i2);
                    int i3 = this.focus_peaking_color_pref;
                    if (i3 != -1) {
                        this.f22p.setColorFilter(new PorterDuffColorFilter(i3, Mode.SRC_IN));
                    }
                    canvas2.drawBitmap(focusPeakingBitmap, this.last_image_matrix, this.f22p);
                    if (this.focus_peaking_color_pref != -1) {
                        this.f22p.setColorFilter(null);
                    }
                    this.f22p.setAlpha(255);
                }
            }
            doThumbnailAnimation(canvas2, currentTimeMillis);
            drawUI(canvas2, currentTimeMillis);
            drawAngleLines(canvas2, currentTimeMillis);
            doFocusAnimation(canvas2, currentTimeMillis);
            Face[] facesDetected = preview.getFacesDetected();
            if (facesDetected != null) {
                this.f22p.setColor(Color.rgb(255, 235, 59));
                this.f22p.setStyle(Style.STROKE);
                this.f22p.setStrokeWidth(this.stroke_width);
                for (Face face : facesDetected) {
                    if (face.score >= 50) {
                        canvas2.drawRect(face.rect, this.f22p);
                    }
                }
                this.f22p.setStyle(Style.FILL);
            }
            if (this.enable_gyro_target_spot && cameraController != null) {
                GyroSensor gyroSensor = this.main_activity.getApplicationInterface().getGyroSensor();
                if (gyroSensor.isRecording()) {
                    for (float[] relativeInverseVector : this.gyro_directions) {
                        gyroSensor.getRelativeInverseVector(this.transformed_gyro_direction, relativeInverseVector);
                        gyroSensor.getRelativeInverseVector(this.transformed_gyro_direction_up, this.gyro_direction_up);
                        float f2 = -((float) Math.asin((double) this.transformed_gyro_direction[1]));
                        float f3 = -((float) Math.asin((double) this.transformed_gyro_direction[0]));
                        if (((double) Math.abs(f2)) >= 1.5707963267948966d || ((double) Math.abs(f3)) >= 1.5707963267948966d) {
                            f = f2;
                        } else {
                            updateCachedViewAngles(currentTimeMillis);
                            float f4 = this.view_angle_x_preview;
                            float f5 = this.view_angle_y_preview;
                            double width = (double) canvas.getWidth();
                            double d = (double) f4;
                            Double.isNaN(d);
                            double tan = Math.tan(Math.toRadians(d / 2.0d)) * 2.0d;
                            Double.isNaN(width);
                            float f6 = (float) (width / tan);
                            double height = (double) canvas.getHeight();
                            double d2 = (double) f5;
                            Double.isNaN(d2);
                            double tan2 = Math.tan(Math.toRadians(d2 / 2.0d)) * 2.0d;
                            Double.isNaN(height);
                            float zoomRatio = f6 * preview.getZoomRatio() * ((float) Math.tan((double) f2));
                            float zoomRatio2 = ((float) (height / tan2)) * preview.getZoomRatio() * ((float) Math.tan((double) f3));
                            this.f22p.setColor(-1);
                            f = f2;
                            drawGyroSpot(canvas, 0.0f, 0.0f, -1.0f, 0.0f, 48, true);
                            this.f22p.setColor(-16776961);
                            float[] fArr = this.transformed_gyro_direction_up;
                            drawGyroSpot(canvas, zoomRatio, zoomRatio2, -fArr[1], -fArr[0], 45, false);
                        }
                        if (gyroSensor.isUpright() == 0 || Math.abs(f) > 0.34906584f) {
                            obj = null;
                        } else {
                            canvas.save();
                            canvas2.rotate((float) uIRotation, ((float) canvas.getWidth()) / 2.0f, ((float) canvas.getHeight()) / 2.0f);
                            float f7 = this.scale;
                            int width2 = canvas.getWidth() / 2;
                            int height2 = (canvas.getHeight() / 2) - ((int) ((f7 * 80.0f) + 0.5f));
                            int i4 = ((int) ((64.0f * f7) + 0.5f)) / 2;
                            this.icon_dest.set(width2 - i4, height2 - i4, width2 + i4, height2 + i4);
                            obj = null;
                            canvas2.drawBitmap(gyroSensor.isUpright() > 0 ? this.rotate_left_bitmap : this.rotate_right_bitmap, null, this.icon_dest, this.f22p);
                            canvas.restore();
                        }
                        Object obj2 = obj;
                    }
                }
            }
        }
    }

    private void setLastImageMatrix(Canvas canvas, Bitmap bitmap, int i, boolean z) {
        CameraController cameraController = this.main_activity.getPreview().getCameraController();
        RectF rectF = this.last_image_src_rect;
        rectF.left = 0.0f;
        rectF.top = 0.0f;
        rectF.right = (float) bitmap.getWidth();
        this.last_image_src_rect.bottom = (float) bitmap.getHeight();
        if (i == 90 || i == 270) {
            this.last_image_src_rect.right = (float) bitmap.getHeight();
            this.last_image_src_rect.bottom = (float) bitmap.getWidth();
        }
        RectF rectF2 = this.last_image_dst_rect;
        rectF2.left = 0.0f;
        rectF2.top = 0.0f;
        rectF2.right = (float) canvas.getWidth();
        this.last_image_dst_rect.bottom = (float) canvas.getHeight();
        this.last_image_matrix.setRectToRect(this.last_image_src_rect, this.last_image_dst_rect, ScaleToFit.CENTER);
        if (i == 90 || i == 270) {
            float height = (float) (bitmap.getHeight() - bitmap.getWidth());
            this.last_image_matrix.preTranslate(height / 2.0f, (-height) / 2.0f);
        }
        this.last_image_matrix.preRotate((float) i, ((float) bitmap.getWidth()) / 2.0f, ((float) bitmap.getHeight()) / 2.0f);
        if (z) {
            if ((cameraController != null && cameraController.isFrontFacing()) && !this.sharedPreferences.getString(PreferenceKeys.FrontCameraMirrorKey, "preference_front_camera_mirror_no").equals("preference_front_camera_mirror_photo")) {
                this.last_image_matrix.preScale(-1.0f, 1.0f, ((float) bitmap.getWidth()) / 2.0f, 0.0f);
            }
        }
    }

    private void drawGyroSpot(Canvas canvas, float f, float f2, float f3, float f4, int i, boolean z) {
        if (z) {
            this.f22p.setStyle(Style.STROKE);
            this.f22p.setStrokeWidth(this.stroke_width);
            this.f22p.setAlpha(255);
        } else {
            this.f22p.setAlpha(127);
        }
        canvas.drawCircle((((float) canvas.getWidth()) / 2.0f) + f, (((float) canvas.getHeight()) / 2.0f) + f2, (((float) i) * this.scale) + 0.5f, this.f22p);
        this.f22p.setAlpha(255);
        this.f22p.setStyle(Style.FILL);
    }

    public void onExtraOSDValuesChanged(String str, String str2) {
        this.OSDLine1 = str;
        this.OSDLine2 = str2;
    }

    public boolean getStoredHasStampPref() {
        return this.has_stamp_pref;
    }

    public boolean getStoredAutoStabilisePref() {
        return this.auto_stabilise_pref;
    }
}
