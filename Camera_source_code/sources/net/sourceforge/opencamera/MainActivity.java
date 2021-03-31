package net.sourceforge.opencamera;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.renderscript.RenderScript;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.p000v4.content.ContextCompat;
import android.support.p000v4.view.ViewCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ZoomControls;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.sourceforge.opencamera.MyApplicationInterface.PhotoMode;
import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.cameracontroller.CameraController.Size;
import net.sourceforge.opencamera.cameracontroller.CameraControllerManager2;
import net.sourceforge.opencamera.p004ui.FolderChooserDialog;
import net.sourceforge.opencamera.p004ui.MainUI;
import net.sourceforge.opencamera.p004ui.ManualSeekbars;
import net.sourceforge.opencamera.preview.Preview;
import net.sourceforge.opencamera.preview.VideoProfile;
import net.sourceforge.opencamera.remotecontrol.BluetoothRemoteControl;

public class MainActivity extends Activity {
    private static final String ACTION_SHORTCUT_CAMERA = "net.sourceforge.opencamera.SHORTCUT_CAMERA";
    private static final String ACTION_SHORTCUT_GALLERY = "net.sourceforge.opencamera.SHORTCUT_GALLERY";
    private static final String ACTION_SHORTCUT_SELFIE = "net.sourceforge.opencamera.SHORTCUT_SELFIE";
    private static final String ACTION_SHORTCUT_SETTINGS = "net.sourceforge.opencamera.SHORTCUT_SETTINGS";
    private static final String ACTION_SHORTCUT_VIDEO = "net.sourceforge.opencamera.SHORTCUT_VIDEO";
    private static final int CHOOSE_GHOST_IMAGE_SAF_CODE = 43;
    private static final int CHOOSE_LOAD_SETTINGS_SAF_CODE = 44;
    private static final int CHOOSE_SAVE_FOLDER_SAF_CODE = 42;
    private static final String TAG = "MainActivity";
    private static final float WATER_DENSITY_FRESHWATER = 1.0f;
    private static final float WATER_DENSITY_SALTWATER = 1.03f;
    private static int activity_count;
    public static boolean test_force_supports_camera2;
    private final String CHANNEL_ID = "open_camera_channel";
    private final SensorEventListener accelerometerListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            MainActivity.this.preview.onAccelerometerSensorChanged(sensorEvent);
        }
    };
    /* access modifiers changed from: private */
    public MyApplicationInterface applicationInterface;
    private final ToastBoxer audio_control_toast = new ToastBoxer();
    private AudioListener audio_listener;
    private boolean block_startup_toast = false;
    /* access modifiers changed from: private */
    public BluetoothRemoteControl bluetoothRemoteControl;
    private final BroadcastReceiver cameraReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            MainActivity.this.takePicture(false);
        }
    };
    /* access modifiers changed from: private */
    public boolean camera_in_background;
    private final ToastBoxer changed_auto_stabilise_toast = new ToastBoxer();
    private final ToastBoxer exposure_lock_toast = new ToastBoxer();
    public volatile Bitmap gallery_bitmap;
    /* access modifiers changed from: private */
    public ValueAnimator gallery_save_anim;
    /* access modifiers changed from: private */
    public GestureDetector gestureDetector;
    private boolean has_notification;
    private final int image_saving_notification_id = 1;
    private Handler immersive_timer_handler = null;
    private Runnable immersive_timer_runnable = null;
    public boolean is_test;
    private int large_heap_memory;
    private boolean last_continuous_fast_burst;
    private Sensor mSensorAccelerometer;
    private SensorManager mSensorManager;
    /* access modifiers changed from: private */
    public float mWaterDensity = 1.0f;
    private MagneticSensor magneticSensor;
    /* access modifiers changed from: private */
    public MainUI mainUI;
    /* access modifiers changed from: private */
    public ManualSeekbars manualSeekbars;
    private OrientationEventListener orientationEventListener;
    private PermissionHandler permissionHandler;
    private final PreferencesListener preferencesListener = new PreferencesListener();
    private final Map<Integer, Bitmap> preloaded_bitmap_resources = new Hashtable();
    /* access modifiers changed from: private */
    public Preview preview;
    private boolean saf_dialog_from_preferences;
    private SaveLocationHistory save_location_history;
    private SaveLocationHistory save_location_history_saf;
    private boolean screen_is_locked;
    /* access modifiers changed from: private */
    public final ToastBoxer screen_locked_toast = new ToastBoxer();
    private SettingsManager settingsManager;
    private SoundPoolManager soundPoolManager;
    private SpeechControl speechControl;
    private final ToastBoxer stamp_toast = new ToastBoxer();
    private boolean supports_auto_stabilise;
    private boolean supports_camera2;
    private boolean supports_force_video_4k;
    private final ToastBoxer switch_video_toast = new ToastBoxer();
    public volatile float test_angle;
    public volatile boolean test_have_angle;
    public volatile String test_last_saved_image;
    public volatile boolean test_low_memory;
    public volatile String test_save_settings_file;
    private TextFormatter textFormatter;
    /* access modifiers changed from: private */
    public TextToSpeech textToSpeech;
    /* access modifiers changed from: private */
    public boolean textToSpeechSuccess;
    private final ToastBoxer white_balance_lock_toast = new ToastBoxer();

    public static class MyFolderChooserDialog extends FolderChooserDialog {
        public void onDismiss(DialogInterface dialogInterface) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.setWindowFlagsForCamera();
                mainActivity.showPreview(true);
                mainActivity.updateSaveFolder(getChosenFolder());
            }
            super.onDismiss(dialogInterface);
        }
    }

    private class MyGestureDetector extends SimpleOnGestureListener {
        private MyGestureDetector() {
        }

        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            try {
                int i = (int) ((MainActivity.this.getResources().getDisplayMetrics().density * 160.0f) + 0.5f);
                int scaledMinimumFlingVelocity = ViewConfiguration.get(MainActivity.this).getScaledMinimumFlingVelocity();
                float x = motionEvent.getX() - motionEvent2.getX();
                float y = motionEvent.getY() - motionEvent2.getY();
                float f3 = (f * f) + (f2 * f2);
                if ((x * x) + (y * y) > ((float) (i * i)) && f3 > ((float) (scaledMinimumFlingVelocity * scaledMinimumFlingVelocity))) {
                    MainActivity.this.preview.showToast(MainActivity.this.screen_locked_toast, (int) C0316R.string.unlocked);
                    MainActivity.this.unlockScreen();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        public boolean onDown(MotionEvent motionEvent) {
            MainActivity.this.preview.showToast(MainActivity.this.screen_locked_toast, (int) C0316R.string.screen_is_locked);
            return true;
        }
    }

    class PreferencesListener implements OnSharedPreferenceChangeListener {
        private static final String TAG = "PreferencesListener";
        private boolean any_change;
        private boolean any_significant_change;

        PreferencesListener() {
        }

        /* access modifiers changed from: 0000 */
        public void startListening() {
            this.any_significant_change = false;
            this.any_change = false;
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).registerOnSharedPreferenceChangeListener(this);
        }

        /* access modifiers changed from: 0000 */
        public void stopListening() {
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).unregisterOnSharedPreferenceChangeListener(this);
        }

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onSharedPreferenceChanged(android.content.SharedPreferences r4, java.lang.String r5) {
            /*
                r3 = this;
                r0 = 1
                r3.any_change = r0
                int r1 = r5.hashCode()
                java.lang.String r2 = "preference_water_type"
                switch(r1) {
                    case -2059186362: goto L_0x0255;
                    case -1918724313: goto L_0x024a;
                    case -1861900216: goto L_0x023f;
                    case -1661105071: goto L_0x0234;
                    case -1635275788: goto L_0x0229;
                    case -1373729873: goto L_0x021e;
                    case -1360636171: goto L_0x0213;
                    case -1248115276: goto L_0x0208;
                    case -1232076213: goto L_0x01fd;
                    case -1132243472: goto L_0x01f1;
                    case -1022902671: goto L_0x01e5;
                    case -808707380: goto L_0x01da;
                    case -805932824: goto L_0x01cf;
                    case -747455470: goto L_0x01c3;
                    case -678680493: goto L_0x01b7;
                    case -558278614: goto L_0x01ab;
                    case -508187834: goto L_0x01a1;
                    case -375123486: goto L_0x0196;
                    case -315108532: goto L_0x018a;
                    case -151465775: goto L_0x017e;
                    case -123860331: goto L_0x0172;
                    case -115633313: goto L_0x0166;
                    case -115026207: goto L_0x015b;
                    case -22723297: goto L_0x014f;
                    case -17029569: goto L_0x0143;
                    case 62465456: goto L_0x0137;
                    case 178484829: goto L_0x012b;
                    case 228228749: goto L_0x011f;
                    case 286861363: goto L_0x0113;
                    case 328194955: goto L_0x0107;
                    case 398708251: goto L_0x00fc;
                    case 421536510: goto L_0x00f0;
                    case 460470897: goto L_0x00e4;
                    case 614628560: goto L_0x00d8;
                    case 649406571: goto L_0x00cc;
                    case 649591153: goto L_0x00c0;
                    case 715902196: goto L_0x00b5;
                    case 899755525: goto L_0x00a9;
                    case 923613130: goto L_0x009d;
                    case 1161002468: goto L_0x0091;
                    case 1314657610: goto L_0x0085;
                    case 1420641088: goto L_0x0079;
                    case 1531025950: goto L_0x006d;
                    case 1533629522: goto L_0x0061;
                    case 1548737586: goto L_0x0055;
                    case 1610089537: goto L_0x0049;
                    case 1725400365: goto L_0x003d;
                    case 1769764707: goto L_0x0031;
                    case 1994265049: goto L_0x0026;
                    case 2039564089: goto L_0x001a;
                    case 2115846626: goto L_0x000e;
                    default: goto L_0x000c;
                }
            L_0x000c:
                goto L_0x025f
            L_0x000e:
                java.lang.String r1 = "preference_show_pitch_lines"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 19
                goto L_0x0260
            L_0x001a:
                java.lang.String r1 = "preference_using_saf"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 10
                goto L_0x0260
            L_0x0026:
                java.lang.String r1 = "preference_shutter_sound"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 5
                goto L_0x0260
            L_0x0031:
                java.lang.String r1 = "preference_record_audio_channels"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 44
                goto L_0x0260
            L_0x003d:
                java.lang.String r1 = "preference_take_photo_border"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 29
                goto L_0x0260
            L_0x0049:
                java.lang.String r1 = "preference_stamp_gpsformat"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 36
                goto L_0x0260
            L_0x0055:
                java.lang.String r1 = "preference_startup_focus"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 15
                goto L_0x0260
            L_0x0061:
                java.lang.String r1 = "preference_textstamp"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 37
                goto L_0x0260
            L_0x006d:
                java.lang.String r1 = "preference_enable_remote"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 48
                goto L_0x0260
            L_0x0079:
                java.lang.String r1 = "preference_video_subtitle"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 46
                goto L_0x0260
            L_0x0085:
                java.lang.String r1 = "preference_show_toasts"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 27
                goto L_0x0260
            L_0x0091:
                java.lang.String r1 = "preference_stamp_timeformat"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 35
                goto L_0x0260
            L_0x009d:
                java.lang.String r1 = "preference_save_zulu_time"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 13
                goto L_0x0260
            L_0x00a9:
                java.lang.String r1 = "preference_stamp_dateformat"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 34
                goto L_0x0260
            L_0x00b5:
                java.lang.String r1 = "preference_timer_speak"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 7
                goto L_0x0260
            L_0x00c0:
                java.lang.String r1 = "preference_show_zoom"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 16
                goto L_0x0260
            L_0x00cc:
                java.lang.String r1 = "preference_show_time"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 22
                goto L_0x0260
            L_0x00d8:
                java.lang.String r1 = "preference_free_memory"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 23
                goto L_0x0260
            L_0x00e4:
                java.lang.String r1 = "preference_record_audio_src"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 43
                goto L_0x0260
            L_0x00f0:
                java.lang.String r1 = "preference_show_angle_line"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 18
                goto L_0x0260
            L_0x00fc:
                java.lang.String r1 = "preference_pause_preview"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 4
                goto L_0x0260
            L_0x0107:
                java.lang.String r1 = "preference_audio_noise_control_sensitivity"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 9
                goto L_0x0260
            L_0x0113:
                java.lang.String r1 = "preference_require_location"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 47
                goto L_0x0260
            L_0x011f:
                java.lang.String r1 = "preference_thumbnail_animation"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 28
                goto L_0x0260
            L_0x012b:
                java.lang.String r1 = "preference_save_photo_prefix"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 11
                goto L_0x0260
            L_0x0137:
                java.lang.String r1 = "preference_stamp_fontsize"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 38
                goto L_0x0260
            L_0x0143:
                java.lang.String r1 = "preference_remote_device_name"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 49
                goto L_0x0260
            L_0x014f:
                java.lang.String r1 = "preference_front_camera_mirror"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 32
                goto L_0x0260
            L_0x015b:
                java.lang.String r1 = "preference_timer"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 0
                goto L_0x0260
            L_0x0166:
                java.lang.String r1 = "preference_stamp"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 33
                goto L_0x0260
            L_0x0172:
                java.lang.String r1 = "preference_volume_keys"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 8
                goto L_0x0260
            L_0x017e:
                java.lang.String r1 = "preference_stamp_style"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 40
                goto L_0x0260
            L_0x018a:
                java.lang.String r1 = "preference_record_audio"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 42
                goto L_0x0260
            L_0x0196:
                java.lang.String r1 = "preference_touch_capture"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 3
                goto L_0x0260
            L_0x01a1:
                boolean r5 = r5.equals(r2)
                if (r5 == 0) goto L_0x025f
                r5 = 50
                goto L_0x0260
            L_0x01ab:
                java.lang.String r1 = "preference_grid"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 25
                goto L_0x0260
            L_0x01b7:
                java.lang.String r1 = "preference_stamp_font_color"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 39
                goto L_0x0260
            L_0x01c3:
                java.lang.String r1 = "preference_keep_display_on"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 30
                goto L_0x0260
            L_0x01cf:
                java.lang.String r1 = "preference_burst_interval"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 2
                goto L_0x0260
            L_0x01da:
                java.lang.String r1 = "preference_timer_beep"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 6
                goto L_0x0260
            L_0x01e5:
                java.lang.String r1 = "preference_crop_guide"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 26
                goto L_0x0260
            L_0x01f1:
                java.lang.String r1 = "preference_max_brightness"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 31
                goto L_0x0260
            L_0x01fd:
                java.lang.String r1 = "preference_lock_video"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 45
                goto L_0x0260
            L_0x0208:
                java.lang.String r1 = "preference_background_photo_saving"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 41
                goto L_0x0260
            L_0x0213:
                java.lang.String r1 = "preference_show_angle"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 17
                goto L_0x0260
            L_0x021e:
                java.lang.String r1 = "preference_show_battery"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 21
                goto L_0x0260
            L_0x0229:
                java.lang.String r1 = "preference_save_video_prefix"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 12
                goto L_0x0260
            L_0x0234:
                java.lang.String r1 = "preference_show_when_locked"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 14
                goto L_0x0260
            L_0x023f:
                java.lang.String r1 = "preference_angle_highlight_color"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 20
                goto L_0x0260
            L_0x024a:
                java.lang.String r1 = "preference_show_iso"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 24
                goto L_0x0260
            L_0x0255:
                java.lang.String r1 = "preference_burst_mode"
                boolean r5 = r5.equals(r1)
                if (r5 == 0) goto L_0x025f
                r5 = 1
                goto L_0x0260
            L_0x025f:
                r5 = -1
            L_0x0260:
                switch(r5) {
                    case 0: goto L_0x02a0;
                    case 1: goto L_0x02a0;
                    case 2: goto L_0x02a0;
                    case 3: goto L_0x02a0;
                    case 4: goto L_0x02a0;
                    case 5: goto L_0x02a0;
                    case 6: goto L_0x02a0;
                    case 7: goto L_0x02a0;
                    case 8: goto L_0x02a0;
                    case 9: goto L_0x02a0;
                    case 10: goto L_0x02a0;
                    case 11: goto L_0x02a0;
                    case 12: goto L_0x02a0;
                    case 13: goto L_0x02a0;
                    case 14: goto L_0x02a0;
                    case 15: goto L_0x02a0;
                    case 16: goto L_0x02a0;
                    case 17: goto L_0x02a0;
                    case 18: goto L_0x02a0;
                    case 19: goto L_0x02a0;
                    case 20: goto L_0x02a0;
                    case 21: goto L_0x02a0;
                    case 22: goto L_0x02a0;
                    case 23: goto L_0x02a0;
                    case 24: goto L_0x02a0;
                    case 25: goto L_0x02a0;
                    case 26: goto L_0x02a0;
                    case 27: goto L_0x02a0;
                    case 28: goto L_0x02a0;
                    case 29: goto L_0x02a0;
                    case 30: goto L_0x02a0;
                    case 31: goto L_0x02a0;
                    case 32: goto L_0x02a0;
                    case 33: goto L_0x02a0;
                    case 34: goto L_0x02a0;
                    case 35: goto L_0x02a0;
                    case 36: goto L_0x02a0;
                    case 37: goto L_0x02a0;
                    case 38: goto L_0x02a0;
                    case 39: goto L_0x02a0;
                    case 40: goto L_0x02a0;
                    case 41: goto L_0x02a0;
                    case 42: goto L_0x02a0;
                    case 43: goto L_0x02a0;
                    case 44: goto L_0x02a0;
                    case 45: goto L_0x02a0;
                    case 46: goto L_0x02a0;
                    case 47: goto L_0x02a0;
                    case 48: goto L_0x0297;
                    case 49: goto L_0x0278;
                    case 50: goto L_0x0266;
                    default: goto L_0x0263;
                }
            L_0x0263:
                r3.any_significant_change = r0
                goto L_0x02a0
            L_0x0266:
                boolean r4 = r4.getBoolean(r2, r0)
                net.sourceforge.opencamera.MainActivity r5 = net.sourceforge.opencamera.MainActivity.this
                if (r4 == 0) goto L_0x0272
                r4 = 1065604874(0x3f83d70a, float:1.03)
                goto L_0x0274
            L_0x0272:
                r4 = 1065353216(0x3f800000, float:1.0)
            L_0x0274:
                r5.mWaterDensity = r4
                goto L_0x02a0
            L_0x0278:
                net.sourceforge.opencamera.MainActivity r4 = net.sourceforge.opencamera.MainActivity.this
                net.sourceforge.opencamera.remotecontrol.BluetoothRemoteControl r4 = r4.bluetoothRemoteControl
                boolean r4 = r4.remoteEnabled()
                if (r4 == 0) goto L_0x028d
                net.sourceforge.opencamera.MainActivity r4 = net.sourceforge.opencamera.MainActivity.this
                net.sourceforge.opencamera.remotecontrol.BluetoothRemoteControl r4 = r4.bluetoothRemoteControl
                r4.stopRemoteControl()
            L_0x028d:
                net.sourceforge.opencamera.MainActivity r4 = net.sourceforge.opencamera.MainActivity.this
                net.sourceforge.opencamera.remotecontrol.BluetoothRemoteControl r4 = r4.bluetoothRemoteControl
                r4.startRemoteControl()
                goto L_0x02a0
            L_0x0297:
                net.sourceforge.opencamera.MainActivity r4 = net.sourceforge.opencamera.MainActivity.this
                net.sourceforge.opencamera.remotecontrol.BluetoothRemoteControl r4 = r4.bluetoothRemoteControl
                r4.startRemoteControl()
            L_0x02a0:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MainActivity.PreferencesListener.onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String):void");
        }

        /* access modifiers changed from: 0000 */
        public boolean anyChange() {
            return this.any_change;
        }

        /* access modifiers changed from: 0000 */
        public boolean anySignificantChange() {
            return this.any_significant_change;
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        int i;
        activity_count++;
        super.onCreate(bundle);
        if (VERSION.SDK_INT >= 18) {
            LayoutParams attributes = getWindow().getAttributes();
            attributes.rotationAnimation = 1;
            getWindow().setAttributes(attributes);
        }
        setContentView(C0316R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, C0316R.xml.preferences, false);
        if (!(getIntent() == null || getIntent().getExtras() == null)) {
            this.is_test = getIntent().getExtras().getBoolean("test_project");
        }
        if (getIntent() != null) {
            getIntent().getExtras();
        }
        if (getIntent() != null) {
            getIntent().getAction();
        }
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ActivityManager activityManager = (ActivityManager) getSystemService("activity");
        this.large_heap_memory = activityManager.getLargeMemoryClass();
        if (this.large_heap_memory >= 128) {
            this.supports_auto_stabilise = true;
        }
        if (activityManager.getMemoryClass() >= 128 || activityManager.getLargeMemoryClass() >= 512) {
            this.supports_force_video_4k = true;
        }
        this.bluetoothRemoteControl = new BluetoothRemoteControl(this);
        this.permissionHandler = new PermissionHandler(this);
        this.settingsManager = new SettingsManager(this);
        this.mainUI = new MainUI(this);
        this.manualSeekbars = new ManualSeekbars();
        this.applicationInterface = new MyApplicationInterface(this, bundle);
        this.textFormatter = new TextFormatter(this);
        this.soundPoolManager = new SoundPoolManager(this);
        this.magneticSensor = new MagneticSensor(this);
        this.speechControl = new SpeechControl(this);
        initCamera2Support();
        if (VERSION.SDK_INT >= 16) {
            findViewById(C0316R.C0318id.hide_container).setImportantForAccessibility(2);
        }
        setWindowFlagsForCamera();
        this.save_location_history = new SaveLocationHistory(this, "save_location_history", getStorageUtils().getSaveLocation());
        if (this.applicationInterface.getStorageUtils().isUsingSAF()) {
            this.save_location_history_saf = new SaveLocationHistory(this, "save_location_history_saf", getStorageUtils().getSaveLocationSAF());
        }
        this.mSensorManager = (SensorManager) getSystemService("sensor");
        if (this.mSensorManager.getDefaultSensor(1) != null) {
            this.mSensorAccelerometer = this.mSensorManager.getDefaultSensor(1);
        }
        this.magneticSensor.initSensor(this.mSensorManager);
        this.mainUI.clearSeekBar();
        this.preview = new Preview(this.applicationInterface, (ViewGroup) findViewById(C0316R.C0318id.preview));
        findViewById(C0316R.C0318id.switch_camera).setVisibility(this.preview.getCameraControllerManager().getNumberOfCameras() > 1 ? 0 : 8);
        findViewById(C0316R.C0318id.audio_control).setVisibility(8);
        findViewById(C0316R.C0318id.pause_video).setVisibility(8);
        findViewById(C0316R.C0318id.take_photo_when_video_recording).setVisibility(8);
        findViewById(C0316R.C0318id.cancel_panorama).setVisibility(8);
        View findViewById = findViewById(C0316R.C0318id.take_photo);
        findViewById.setVisibility(4);
        findViewById(C0316R.C0318id.zoom).setVisibility(8);
        findViewById(C0316R.C0318id.zoom_seekbar).setVisibility(4);
        this.mainUI.updateOnScreenIcons();
        this.orientationEventListener = new OrientationEventListener(this) {
            public void onOrientationChanged(int i) {
                MainActivity.this.mainUI.onOrientationChanged(i);
            }
        };
        findViewById.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View view) {
                return MainActivity.this.longClickedTakePhoto();
            }
        });
        findViewById.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == 1) {
                    MainActivity.this.takePhotoButtonLongClickCancelled();
                }
                return false;
            }
        });
        findViewById(C0316R.C0318id.gallery).setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View view) {
                MainActivity.this.longClickedGallery();
                return true;
            }
        });
        this.gestureDetector = new GestureDetector(this, new MyGestureDetector());
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int i) {
                if (MainActivity.this.usingKitKatImmersiveMode()) {
                    if ((i & 4) == 0) {
                        MainActivity.this.mainUI.setImmersiveMode(false);
                        MainActivity.this.setImmersiveTimer();
                    } else {
                        MainActivity.this.mainUI.setImmersiveMode(true);
                    }
                }
            }
        });
        boolean contains = defaultSharedPreferences.contains(PreferenceKeys.FirstTimePreferenceKey);
        if (!contains) {
            setDeviceDefaults();
        }
        if (!contains) {
            if (!this.is_test) {
                Builder builder = new Builder(this);
                builder.setTitle(C0316R.string.app_name);
                builder.setMessage(C0316R.string.intro_text);
                builder.setPositiveButton(17039370, null);
                builder.setNegativeButton(C0316R.string.preference_online_help, new OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.launchOnlineHelp();
                    }
                });
                builder.show();
            }
            setFirstTimeFlag();
        }
        try {
            i = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            i = -1;
        }
        if (i != -1) {
            String str = PreferenceKeys.LatestVersionPreferenceKey;
            int i2 = defaultSharedPreferences.getInt(str, 0);
            int min = Math.min(74, i);
            boolean z = defaultSharedPreferences.getBoolean(PreferenceKeys.ShowWhatsNewPreferenceKey, true);
            if (contains && z && min > i2) {
                Builder builder2 = new Builder(this);
                builder2.setTitle(C0316R.string.whats_new);
                builder2.setMessage(C0316R.string.whats_new_text);
                builder2.setPositiveButton(17039370, null);
                builder2.show();
            }
            Editor edit = defaultSharedPreferences.edit();
            edit.putInt(str, i);
            edit.apply();
        }
        setModeFromIntents(bundle);
        preloadIcons(C0316R.array.flash_icons);
        preloadIcons(C0316R.array.focus_mode_icons);
        this.textToSpeechSuccess = false;
        new Thread(new Runnable() {
            public void run() {
                MainActivity mainActivity = MainActivity.this;
                mainActivity.textToSpeech = new TextToSpeech(mainActivity, new OnInitListener() {
                    public void onInit(int i) {
                        if (i == 0) {
                            MainActivity.this.textToSpeechSuccess = true;
                        }
                    }
                });
            }
        }).start();
        if (VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel("open_camera_channel", "Open Camera Image Saving", 2);
            notificationChannel.setDescription("Notification channel for processing and saving images in the background");
            ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(notificationChannel);
        }
    }

    /* access modifiers changed from: 0000 */
    public void setDeviceDefaults() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean contains = Build.MANUFACTURER.toLowerCase(Locale.US).contains("samsung");
        boolean contains2 = Build.MANUFACTURER.toLowerCase(Locale.US).contains("oneplus");
        if (contains || contains2) {
            Editor edit = defaultSharedPreferences.edit();
            edit.putBoolean(PreferenceKeys.Camera2FakeFlashPreferenceKey, true);
            edit.apply();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x00c0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setModeFromIntents(android.os.Bundle r6) {
        /*
            r5 = this;
            if (r6 == 0) goto L_0x0003
            return
        L_0x0003:
            android.content.Intent r6 = r5.getIntent()
            java.lang.String r6 = r6.getAction()
            java.lang.String r0 = "android.media.action.VIDEO_CAMERA"
            boolean r0 = r0.equals(r6)
            r1 = 0
            r2 = 1
            if (r0 != 0) goto L_0x00b0
            java.lang.String r0 = "android.media.action.VIDEO_CAPTURE"
            boolean r0 = r0.equals(r6)
            if (r0 == 0) goto L_0x001f
            goto L_0x00b0
        L_0x001f:
            java.lang.String r0 = "android.media.action.IMAGE_CAPTURE"
            boolean r0 = r0.equals(r6)
            if (r0 != 0) goto L_0x00aa
            java.lang.String r0 = "android.media.action.IMAGE_CAPTURE_SECURE"
            boolean r0 = r0.equals(r6)
            if (r0 != 0) goto L_0x00aa
            java.lang.String r0 = "android.media.action.STILL_IMAGE_CAMERA"
            boolean r0 = r0.equals(r6)
            if (r0 != 0) goto L_0x00aa
            java.lang.String r0 = "android.media.action.STILL_IMAGE_CAMERA_SECURE"
            boolean r0 = r0.equals(r6)
            if (r0 == 0) goto L_0x0041
            goto L_0x00aa
        L_0x0041:
            int r0 = android.os.Build.VERSION.SDK_INT
            r3 = 24
            if (r0 < r3) goto L_0x004f
            java.lang.String r0 = "net.sourceforge.opencamera.TILE_CAMERA"
            boolean r0 = r0.equals(r6)
            if (r0 != 0) goto L_0x0057
        L_0x004f:
            java.lang.String r0 = "net.sourceforge.opencamera.SHORTCUT_CAMERA"
            boolean r0 = r0.equals(r6)
            if (r0 == 0) goto L_0x005d
        L_0x0057:
            net.sourceforge.opencamera.MyApplicationInterface r6 = r5.applicationInterface
            r6.setVideoPref(r1)
            goto L_0x00b5
        L_0x005d:
            int r0 = android.os.Build.VERSION.SDK_INT
            if (r0 < r3) goto L_0x0069
            java.lang.String r0 = "net.sourceforge.opencamera.TILE_VIDEO"
            boolean r0 = r0.equals(r6)
            if (r0 != 0) goto L_0x0071
        L_0x0069:
            java.lang.String r0 = "net.sourceforge.opencamera.SHORTCUT_VIDEO"
            boolean r0 = r0.equals(r6)
            if (r0 == 0) goto L_0x0077
        L_0x0071:
            net.sourceforge.opencamera.MyApplicationInterface r6 = r5.applicationInterface
            r6.setVideoPref(r2)
            goto L_0x00b5
        L_0x0077:
            int r0 = android.os.Build.VERSION.SDK_INT
            if (r0 < r3) goto L_0x0083
            java.lang.String r0 = "net.sourceforge.opencamera.TILE_FRONT_CAMERA"
            boolean r0 = r0.equals(r6)
            if (r0 != 0) goto L_0x008b
        L_0x0083:
            java.lang.String r0 = "net.sourceforge.opencamera.SHORTCUT_SELFIE"
            boolean r0 = r0.equals(r6)
            if (r0 == 0) goto L_0x0092
        L_0x008b:
            net.sourceforge.opencamera.MyApplicationInterface r6 = r5.applicationInterface
            r6.switchToCamera(r2)
            r6 = 1
            goto L_0x00b6
        L_0x0092:
            java.lang.String r0 = "net.sourceforge.opencamera.SHORTCUT_GALLERY"
            boolean r0 = r0.equals(r6)
            if (r0 == 0) goto L_0x009e
            r5.openGallery()
            goto L_0x00b5
        L_0x009e:
            java.lang.String r0 = "net.sourceforge.opencamera.SHORTCUT_SETTINGS"
            boolean r6 = r0.equals(r6)
            if (r6 == 0) goto L_0x00b5
            r5.openSettings()
            goto L_0x00b5
        L_0x00aa:
            net.sourceforge.opencamera.MyApplicationInterface r6 = r5.applicationInterface
            r6.setVideoPref(r1)
            goto L_0x00b5
        L_0x00b0:
            net.sourceforge.opencamera.MyApplicationInterface r6 = r5.applicationInterface
            r6.setVideoPref(r2)
        L_0x00b5:
            r6 = 0
        L_0x00b6:
            android.content.Intent r0 = r5.getIntent()
            android.os.Bundle r0 = r0.getExtras()
            if (r0 == 0) goto L_0x0108
            r3 = -1
            if (r6 != 0) goto L_0x00d8
            java.lang.String r4 = "android.intent.extras.CAMERA_FACING"
            int r4 = r0.getInt(r4, r3)
            if (r4 == 0) goto L_0x00cd
            if (r4 != r2) goto L_0x00d8
        L_0x00cd:
            net.sourceforge.opencamera.MyApplicationInterface r6 = r5.applicationInterface
            if (r4 != r2) goto L_0x00d3
            r4 = 1
            goto L_0x00d4
        L_0x00d3:
            r4 = 0
        L_0x00d4:
            r6.switchToCamera(r4)
            r6 = 1
        L_0x00d8:
            if (r6 != 0) goto L_0x00e8
            java.lang.String r4 = "android.intent.extras.LENS_FACING_FRONT"
            int r4 = r0.getInt(r4, r3)
            if (r4 != r2) goto L_0x00e8
            net.sourceforge.opencamera.MyApplicationInterface r6 = r5.applicationInterface
            r6.switchToCamera(r2)
            r6 = 1
        L_0x00e8:
            if (r6 != 0) goto L_0x00f8
            java.lang.String r4 = "android.intent.extras.LENS_FACING_BACK"
            int r3 = r0.getInt(r4, r3)
            if (r3 != r2) goto L_0x00f8
            net.sourceforge.opencamera.MyApplicationInterface r6 = r5.applicationInterface
            r6.switchToCamera(r1)
            r6 = 1
        L_0x00f8:
            if (r6 != 0) goto L_0x0108
            java.lang.String r3 = "android.intent.extra.USE_FRONT_CAMERA"
            boolean r0 = r0.getBoolean(r3, r1)
            if (r0 == 0) goto L_0x0108
            net.sourceforge.opencamera.MyApplicationInterface r6 = r5.applicationInterface
            r6.switchToCamera(r2)
            r6 = 1
        L_0x0108:
            if (r6 != 0) goto L_0x0117
            net.sourceforge.opencamera.MyApplicationInterface r6 = r5.applicationInterface
            boolean r6 = r6.hasSetCameraId()
            if (r6 != 0) goto L_0x0117
            net.sourceforge.opencamera.MyApplicationInterface r6 = r5.applicationInterface
            r6.switchToCamera(r1)
        L_0x0117:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MainActivity.setModeFromIntents(android.os.Bundle):void");
    }

    private void initCamera2Support() {
        this.supports_camera2 = false;
        if (VERSION.SDK_INT >= 21) {
            CameraControllerManager2 cameraControllerManager2 = new CameraControllerManager2(this);
            this.supports_camera2 = false;
            int numberOfCameras = cameraControllerManager2.getNumberOfCameras();
            if (numberOfCameras == 0) {
                this.supports_camera2 = false;
            }
            for (int i = 0; i < numberOfCameras && !this.supports_camera2; i++) {
                if (cameraControllerManager2.allowCamera2Support(i)) {
                    this.supports_camera2 = true;
                }
            }
        }
        if (test_force_supports_camera2 && VERSION.SDK_INT >= 21) {
            this.supports_camera2 = true;
        }
        if (this.supports_camera2) {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String str = PreferenceKeys.CameraAPIPreferenceKey;
            if (!defaultSharedPreferences.contains(str)) {
                String str2 = "preference_use_camera2";
                if (defaultSharedPreferences.contains(str2) && defaultSharedPreferences.getBoolean(str2, false)) {
                    Editor edit = defaultSharedPreferences.edit();
                    edit.putString(str, "preference_camera_api_camera2");
                    edit.remove(str2);
                    edit.apply();
                }
            }
        }
    }

    private void preloadIcons(int i) {
        for (String identifier : getResources().getStringArray(i)) {
            int identifier2 = getResources().getIdentifier(identifier, null, getApplicationContext().getPackageName());
            this.preloaded_bitmap_resources.put(Integer.valueOf(identifier2), BitmapFactory.decodeResource(getResources(), identifier2));
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        activity_count--;
        cancelImageSavingNotification();
        waitUntilImageQueueEmpty();
        this.preview.onDestroy();
        MyApplicationInterface myApplicationInterface = this.applicationInterface;
        if (myApplicationInterface != null) {
            myApplicationInterface.onDestroy();
        }
        if (VERSION.SDK_INT >= 23 && activity_count == 0) {
            RenderScript.releaseAllContexts();
        }
        for (Entry value : this.preloaded_bitmap_resources.entrySet()) {
            ((Bitmap) value.getValue()).recycle();
        }
        this.preloaded_bitmap_resources.clear();
        TextToSpeech textToSpeech2 = this.textToSpeech;
        if (textToSpeech2 != null) {
            textToSpeech2.stop();
            this.textToSpeech.shutdown();
            this.textToSpeech = null;
        }
        super.onDestroy();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0316R.menu.main, menu);
        return true;
    }

    private void setFirstTimeFlag() {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
        edit.putBoolean(PreferenceKeys.FirstTimePreferenceKey, true);
        edit.apply();
    }

    private static String getOnlineHelpUrl(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://opencamera.sourceforge.io/");
        sb.append(str);
        return sb.toString();
    }

    /* access modifiers changed from: 0000 */
    public void launchOnlineHelp() {
        startActivity(new Intent("android.intent.action.VIEW", Uri.parse(getOnlineHelpUrl(BuildConfig.FLAVOR))));
    }

    /* access modifiers changed from: 0000 */
    public void launchOnlinePrivacyPolicy() {
        startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://opencamera.sourceforge.io/privacy_oc.html")));
    }

    /* access modifiers changed from: 0000 */
    public void launchOnlineLicences() {
        startActivity(new Intent("android.intent.action.VIEW", Uri.parse(getOnlineHelpUrl("#licence"))));
    }

    /* access modifiers changed from: 0000 */
    public void audioTrigger() {
        if (!popupIsOpen() && !this.camera_in_background && !this.preview.isTakingPhotoOrOnTimer() && !this.preview.isVideoRecording()) {
            runOnUiThread(new Runnable() {
                public void run() {
                    MainActivity.this.takePicture(false);
                }
            });
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (this.mainUI.onKeyDown(i, keyEvent)) {
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        this.mainUI.onKeyUp(i, keyEvent);
        return super.onKeyUp(i, keyEvent);
    }

    public void zoomIn() {
        this.mainUI.changeSeekbar(C0316R.C0318id.zoom_seekbar, -1);
    }

    public void zoomOut() {
        this.mainUI.changeSeekbar(C0316R.C0318id.zoom_seekbar, 1);
    }

    public void changeExposure(int i) {
        this.mainUI.changeSeekbar(C0316R.C0318id.exposure_seekbar, i);
    }

    public void changeISO(int i) {
        this.mainUI.changeSeekbar(C0316R.C0318id.iso_seekbar, i);
    }

    public void changeFocusDistance(int i, boolean z) {
        this.mainUI.changeSeekbar(z ? C0316R.C0318id.focus_bracketing_target_seekbar : C0316R.C0318id.focus_seekbar, i);
    }

    public float getWaterDensity() {
        return this.mWaterDensity;
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        cancelImageSavingNotification();
        getWindow().getDecorView().getRootView().setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        this.mSensorManager.registerListener(this.accelerometerListener, this.mSensorAccelerometer, 3);
        this.magneticSensor.registerMagneticListener(this.mSensorManager);
        this.orientationEventListener.enable();
        registerReceiver(this.cameraReceiver, new IntentFilter("com.miband2.action.CAMERA"));
        this.bluetoothRemoteControl.startRemoteControl();
        this.speechControl.initSpeechRecognizer();
        initLocation();
        initGyroSensors();
        this.soundPoolManager.initSound();
        this.soundPoolManager.loadSound(C0316R.raw.mybeep);
        this.soundPoolManager.loadSound(C0316R.raw.mybeep_hi);
        this.mainUI.layoutUI();
        updateGalleryIcon();
        this.applicationInterface.reset();
        this.preview.onResume();
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (!this.camera_in_background && z) {
            initImmersiveMode();
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        this.mainUI.destroyPopup();
        this.mSensorManager.unregisterListener(this.accelerometerListener);
        this.magneticSensor.unregisterMagneticListener(this.mSensorManager);
        this.orientationEventListener.disable();
        try {
            unregisterReceiver(this.cameraReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        this.bluetoothRemoteControl.stopRemoteControl();
        freeAudioListener(false);
        this.speechControl.stopSpeechRecognizer();
        this.applicationInterface.getLocationSupplier().freeLocationListeners();
        this.applicationInterface.stopPanorama(true);
        this.applicationInterface.getGyroSensor().disableSensors();
        this.soundPoolManager.releaseSound();
        this.applicationInterface.clearLastImages();
        this.applicationInterface.getDrawPreview().clearGhostImage();
        this.preview.onPause();
        if (this.applicationInterface.getImageSaver().getNImagesToSave() > 0) {
            createImageSavingNotification();
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        this.preview.setCameraDisplayOrientation();
        super.onConfigurationChanged(configuration);
    }

    public void waitUntilImageQueueEmpty() {
        this.applicationInterface.getImageSaver().waitUntilDone();
    }

    /* access modifiers changed from: private */
    public boolean longClickedTakePhoto() {
        if (supportsFastBurst()) {
            Size currentPictureSize = this.preview.getCurrentPictureSize();
            if (currentPictureSize != null && currentPictureSize.supports_burst) {
                PhotoMode photoMode = this.applicationInterface.getPhotoMode();
                if ((photoMode != PhotoMode.Standard || !this.applicationInterface.isRawOnly(photoMode)) && (photoMode == PhotoMode.Standard || photoMode == PhotoMode.FastBurst)) {
                    takePicturePressed(false, true);
                    return true;
                }
            }
        }
        return false;
    }

    public void clickedTakePhoto(View view) {
        takePicture(false);
    }

    public void clickedTakePhotoVideoSnapshot(View view) {
        takePicture(true);
    }

    public void clickedPauseVideo(View view) {
        if (this.preview.isVideoRecording()) {
            this.preview.pauseVideo();
            this.mainUI.setPauseVideoContentDescription();
        }
    }

    public void clickedCancelPanorama(View view) {
        this.applicationInterface.stopPanorama(true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0041  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:25:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void clickedCycleRaw(android.view.View r10) {
        /*
            r9 = this;
            android.content.SharedPreferences r10 = android.preference.PreferenceManager.getDefaultSharedPreferences(r9)
            java.lang.String r0 = "preference_raw"
            java.lang.String r1 = "preference_raw_no"
            java.lang.String r2 = r10.getString(r0, r1)
            int r3 = r2.hashCode()
            r4 = -1076775865(0xffffffffbfd1b447, float:-1.6383141)
            java.lang.String r5 = "preference_raw_yes"
            java.lang.String r6 = "preference_raw_only"
            r7 = 2
            r8 = 1
            if (r3 == r4) goto L_0x0036
            r4 = -866009364(0xffffffffcc61beec, float:-5.9177904E7)
            if (r3 == r4) goto L_0x002e
            r4 = 664800540(0x27a00d1c, float:4.4423134E-15)
            if (r3 == r4) goto L_0x0026
            goto L_0x003e
        L_0x0026:
            boolean r2 = r2.equals(r1)
            if (r2 == 0) goto L_0x003e
            r2 = 0
            goto L_0x003f
        L_0x002e:
            boolean r2 = r2.equals(r5)
            if (r2 == 0) goto L_0x003e
            r2 = 1
            goto L_0x003f
        L_0x0036:
            boolean r2 = r2.equals(r6)
            if (r2 == 0) goto L_0x003e
            r2 = 2
            goto L_0x003f
        L_0x003e:
            r2 = -1
        L_0x003f:
            if (r2 == 0) goto L_0x0050
            if (r2 == r8) goto L_0x004e
            if (r2 == r7) goto L_0x0051
            java.lang.String r1 = "MainActivity"
            java.lang.String r2 = "unrecognised raw preference"
            android.util.Log.e(r1, r2)
            r1 = 0
            goto L_0x0051
        L_0x004e:
            r1 = r6
            goto L_0x0051
        L_0x0050:
            r1 = r5
        L_0x0051:
            if (r1 == 0) goto L_0x0070
            android.content.SharedPreferences$Editor r10 = r10.edit()
            r10.putString(r0, r1)
            r10.apply()
            net.sourceforge.opencamera.ui.MainUI r10 = r9.mainUI
            r10.updateCycleRawIcon()
            net.sourceforge.opencamera.MyApplicationInterface r10 = r9.applicationInterface
            net.sourceforge.opencamera.ui.DrawPreview r10 = r10.getDrawPreview()
            r10.updateSettings()
            net.sourceforge.opencamera.preview.Preview r10 = r9.preview
            r10.reopenCamera()
        L_0x0070:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MainActivity.clickedCycleRaw(android.view.View):void");
    }

    public void clickedStoreLocation(View view) {
        boolean z = !this.applicationInterface.getGeotaggingPref();
        Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
        edit.putBoolean(PreferenceKeys.LocationPreferenceKey, z);
        edit.apply();
        this.mainUI.updateStoreLocationIcon();
        this.applicationInterface.getDrawPreview().updateSettings();
        initLocation();
        closePopup();
    }

    public void clickedTextStamp(View view) {
        closePopup();
        Builder builder = new Builder(this);
        builder.setTitle(C0316R.string.preference_textstamp);
        final EditText editText = new EditText(this);
        editText.setText(this.applicationInterface.getTextStampPref());
        builder.setView(editText);
        builder.setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                String obj = editText.getText().toString();
                Editor edit = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                edit.putString(PreferenceKeys.TextStampPreferenceKey, obj);
                edit.apply();
                MainActivity.this.mainUI.updateTextStampIcon();
            }
        });
        builder.setNegativeButton(17039360, null);
        AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                MainActivity.this.setWindowFlagsForCamera();
                MainActivity.this.showPreview(true);
            }
        });
        showPreview(false);
        setWindowFlagsForSettings();
        showAlert(create);
    }

    public void clickedStamp(View view) {
        closePopup();
        String str = "preference_stamp_yes";
        boolean z = !this.applicationInterface.getStampPref().equals(str);
        Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
        if (!z) {
            str = "preference_stamp_no";
        }
        edit.putString(PreferenceKeys.StampPreferenceKey, str);
        edit.apply();
        this.mainUI.updateStampIcon();
        this.applicationInterface.getDrawPreview().updateSettings();
        this.preview.showToast(this.stamp_toast, z ? C0316R.string.stamp_enabled : C0316R.string.stamp_disabled);
    }

    public void clickedAutoLevel(View view) {
        clickedAutoLevel();
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x0032  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void clickedAutoLevel() {
        /*
            r6 = this;
            net.sourceforge.opencamera.MyApplicationInterface r0 = r6.applicationInterface
            boolean r0 = r0.getAutoStabilisePref()
            r1 = 1
            r0 = r0 ^ r1
            android.content.SharedPreferences r2 = android.preference.PreferenceManager.getDefaultSharedPreferences(r6)
            android.content.SharedPreferences$Editor r3 = r2.edit()
            java.lang.String r4 = "preference_auto_stabilise"
            r3.putBoolean(r4, r0)
            r3.apply()
            r3 = 0
            r4 = 2131493081(0x7f0c00d9, float:1.8609632E38)
            if (r0 == 0) goto L_0x002f
            java.lang.String r5 = "done_auto_stabilise_info"
            boolean r2 = r2.contains(r5)
            if (r2 != 0) goto L_0x002f
            net.sourceforge.opencamera.ui.MainUI r2 = r6.mainUI
            r3 = 2131492887(0x7f0c0017, float:1.8609239E38)
            r2.showInfoDialog(r4, r3, r5)
            goto L_0x0030
        L_0x002f:
            r1 = 0
        L_0x0030:
            if (r1 != 0) goto L_0x0068
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            android.content.res.Resources r2 = r6.getResources()
            java.lang.String r2 = r2.getString(r4)
            r1.append(r2)
            java.lang.String r2 = ": "
            r1.append(r2)
            android.content.res.Resources r2 = r6.getResources()
            if (r0 == 0) goto L_0x0051
            r0 = 2131493033(0x7f0c00a9, float:1.8609535E38)
            goto L_0x0054
        L_0x0051:
            r0 = 2131493032(0x7f0c00a8, float:1.8609533E38)
        L_0x0054:
            java.lang.String r0 = r2.getString(r0)
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            net.sourceforge.opencamera.preview.Preview r1 = r6.preview
            net.sourceforge.opencamera.ToastBoxer r2 = r6.getChangedAutoStabiliseToastBoxer()
            r1.showToast(r2, r0)
        L_0x0068:
            net.sourceforge.opencamera.ui.MainUI r0 = r6.mainUI
            r0.updateAutoLevelIcon()
            net.sourceforge.opencamera.MyApplicationInterface r0 = r6.applicationInterface
            net.sourceforge.opencamera.ui.DrawPreview r0 = r0.getDrawPreview()
            r0.updateSettings()
            r6.closePopup()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MainActivity.clickedAutoLevel():void");
    }

    public void clickedCycleFlash(View view) {
        this.preview.cycleFlash(true, true);
        this.mainUI.updateCycleFlashIcon();
    }

    public void clickedFaceDetection(View view) {
        closePopup();
        boolean z = !this.applicationInterface.getFaceDetectionPref();
        Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
        edit.putBoolean(PreferenceKeys.FaceDetectionPreferenceKey, z);
        edit.apply();
        this.mainUI.updateFaceDetectionIcon();
        this.preview.showToast(this.stamp_toast, z ? C0316R.string.face_detection_enabled : C0316R.string.face_detection_disabled);
        this.block_startup_toast = true;
        this.preview.reopenCamera();
    }

    public void clickedAudioControl(View view) {
        if (hasAudioControl()) {
            closePopup();
            String string = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.AudioControlPreferenceKey, CameraController.COLOR_EFFECT_DEFAULT);
            if (!string.equals("voice") || !this.speechControl.hasSpeechRecognition()) {
                if (string.equals("noise")) {
                    if (this.audio_listener != null) {
                        freeAudioListener(false);
                    } else {
                        startAudioListener();
                    }
                }
            } else if (this.speechControl.isStarted()) {
                this.speechControl.stopListening();
            } else {
                boolean z = true;
                if (VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != 0) {
                    this.applicationInterface.requestRecordAudioPermission();
                    z = false;
                }
                if (z) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(getResources().getString(C0316R.string.speech_recognizer_started));
                    sb.append("\n");
                    sb.append(getResources().getString(C0316R.string.speech_recognizer_extra_info));
                    this.preview.showToast(this.audio_control_toast, sb.toString());
                    this.speechControl.startSpeechRecognizerIntent();
                    this.speechControl.speechRecognizerStarted();
                }
            }
        }
    }

    public int getNextCameraId() {
        int cameraId = this.preview.getCameraId();
        if (!this.preview.canSwitchCamera()) {
            return cameraId;
        }
        return (cameraId + 1) % this.preview.getCameraControllerManager().getNumberOfCameras();
    }

    public void clickedSwitchCamera(View view) {
        if (!this.preview.isOpeningCamera()) {
            closePopup();
            if (this.preview.canSwitchCamera()) {
                int nextCameraId = getNextCameraId();
                if (this.preview.getCameraControllerManager().getNumberOfCameras() > 2) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(getResources().getString(this.preview.getCameraControllerManager().isFrontFacing(nextCameraId) ? C0316R.string.front_camera : C0316R.string.back_camera));
                    sb.append(" : ");
                    sb.append(getResources().getString(C0316R.string.camera_id));
                    sb.append(" ");
                    sb.append(nextCameraId);
                    this.preview.showToast((ToastBoxer) null, sb.toString());
                }
                View findViewById = findViewById(C0316R.C0318id.switch_camera);
                findViewById.setEnabled(false);
                this.applicationInterface.reset();
                this.preview.setCamera(nextCameraId);
                findViewById.setEnabled(true);
            }
        }
    }

    public void clickedSwitchVideo(View view) {
        closePopup();
        this.mainUI.destroyPopup();
        this.applicationInterface.stopPanorama(true);
        View findViewById = findViewById(C0316R.C0318id.switch_video);
        findViewById.setEnabled(false);
        this.applicationInterface.reset();
        this.preview.switchVideo(false, true);
        findViewById.setEnabled(true);
        this.mainUI.setTakePhotoIcon();
        this.mainUI.setPopupIcon();
        checkDisableGUIIcons();
        if (!this.block_startup_toast) {
            showPhotoVideoToast(true);
        }
    }

    public void clickedWhiteBalanceLock(View view) {
        this.preview.toggleWhiteBalanceLock();
        this.mainUI.updateWhiteBalanceLockIcon();
        Preview preview2 = this.preview;
        preview2.showToast(this.white_balance_lock_toast, preview2.isWhiteBalanceLocked() ? C0316R.string.white_balance_locked : C0316R.string.white_balance_unlocked);
    }

    public void clickedExposureLock(View view) {
        this.preview.toggleExposureLock();
        this.mainUI.updateExposureLockIcon();
        Preview preview2 = this.preview;
        preview2.showToast(this.exposure_lock_toast, preview2.isExposureLocked() ? C0316R.string.exposure_locked : C0316R.string.exposure_unlocked);
    }

    public void clickedExposure(View view) {
        this.mainUI.toggleExposureUI();
    }

    public void clickedSettings(View view) {
        openSettings();
    }

    public boolean popupIsOpen() {
        return this.mainUI.popupIsOpen();
    }

    public View getUIButton(String str) {
        return this.mainUI.getUIButton(str);
    }

    public void closePopup() {
        this.mainUI.closePopup();
    }

    public Bitmap getPreloadedBitmap(int i) {
        return (Bitmap) this.preloaded_bitmap_resources.get(Integer.valueOf(i));
    }

    public void clickedPopupSettings(View view) {
        this.mainUI.togglePopupSettings();
    }

    public void openSettings() {
        closePopup();
        this.preview.cancelTimer();
        this.preview.cancelRepeat();
        this.preview.stopVideo(false);
        this.applicationInterface.stopPanorama(true);
        stopAudioListeners();
        Bundle bundle = new Bundle();
        bundle.putInt("cameraId", this.preview.getCameraId());
        bundle.putInt("nCameras", this.preview.getCameraControllerManager().getNumberOfCameras());
        bundle.putString("camera_api", this.preview.getCameraAPI());
        bundle.putBoolean("using_android_l", this.preview.usingCamera2API());
        bundle.putBoolean("supports_auto_stabilise", this.supports_auto_stabilise);
        bundle.putBoolean("supports_flash", this.preview.supportsFlash());
        bundle.putBoolean("supports_force_video_4k", this.supports_force_video_4k);
        bundle.putBoolean("supports_camera2", this.supports_camera2);
        bundle.putBoolean("supports_face_detection", this.preview.supportsFaceDetection());
        bundle.putBoolean("supports_raw", this.preview.supportsRaw());
        bundle.putBoolean("supports_burst_raw", supportsBurstRaw());
        bundle.putBoolean("supports_hdr", supportsHDR());
        bundle.putBoolean("supports_nr", supportsNoiseReduction());
        bundle.putBoolean("supports_panorama", supportsPanorama());
        bundle.putBoolean("supports_expo_bracketing", supportsExpoBracketing());
        bundle.putBoolean("supports_preview_bitmaps", supportsPreviewBitmaps());
        bundle.putInt("max_expo_bracketing_n_images", maxExpoBracketingNImages());
        bundle.putBoolean("supports_exposure_compensation", this.preview.supportsExposures());
        bundle.putInt("exposure_compensation_min", this.preview.getMinimumExposure());
        bundle.putInt("exposure_compensation_max", this.preview.getMaximumExposure());
        bundle.putBoolean("supports_iso_range", this.preview.supportsISORange());
        bundle.putInt("iso_range_min", this.preview.getMinimumISO());
        bundle.putInt("iso_range_max", this.preview.getMaximumISO());
        bundle.putBoolean("supports_exposure_time", this.preview.supportsExposureTime());
        bundle.putBoolean("supports_exposure_lock", this.preview.supportsExposureLock());
        bundle.putBoolean("supports_white_balance_lock", this.preview.supportsWhiteBalanceLock());
        bundle.putLong("exposure_time_min", this.preview.getMinimumExposureTime());
        bundle.putLong("exposure_time_max", this.preview.getMaximumExposureTime());
        bundle.putBoolean("supports_white_balance_temperature", this.preview.supportsWhiteBalanceTemperature());
        bundle.putInt("white_balance_temperature_min", this.preview.getMinimumWhiteBalanceTemperature());
        bundle.putInt("white_balance_temperature_max", this.preview.getMaximumWhiteBalanceTemperature());
        bundle.putBoolean("supports_video_stabilization", this.preview.supportsVideoStabilization());
        bundle.putBoolean("can_disable_shutter_sound", this.preview.canDisableShutterSound());
        bundle.putInt("tonemap_max_curve_points", this.preview.getTonemapMaxCurvePoints());
        bundle.putBoolean("supports_tonemap_curve", this.preview.supportsTonemapCurve());
        bundle.putBoolean("supports_photo_video_recording", this.preview.supportsPhotoVideoRecording());
        bundle.putFloat("camera_view_angle_x", this.preview.getViewAngleX(false));
        bundle.putFloat("camera_view_angle_y", this.preview.getViewAngleY(false));
        putBundleExtra(bundle, "color_effects", this.preview.getSupportedColorEffects());
        putBundleExtra(bundle, "scene_modes", this.preview.getSupportedSceneModes());
        putBundleExtra(bundle, "white_balances", this.preview.getSupportedWhiteBalances());
        putBundleExtra(bundle, "isos", this.preview.getSupportedISOs());
        bundle.putInt("magnetic_accuracy", this.magneticSensor.getMagneticAccuracy());
        bundle.putString("iso_key", this.preview.getISOKey());
        if (this.preview.getCameraController() != null) {
            bundle.putString("parameters_string", this.preview.getCameraController().getParametersString());
        }
        List<String> supportedAntiBanding = this.preview.getSupportedAntiBanding();
        putBundleExtra(bundle, "antibanding", supportedAntiBanding);
        if (supportedAntiBanding != null) {
            String[] strArr = new String[supportedAntiBanding.size()];
            int i = 0;
            for (String entryForAntiBanding : supportedAntiBanding) {
                strArr[i] = getMainUI().getEntryForAntiBanding(entryForAntiBanding);
                i++;
            }
            bundle.putStringArray("antibanding_entries", strArr);
        }
        List<String> supportedEdgeModes = this.preview.getSupportedEdgeModes();
        putBundleExtra(bundle, "edge_modes", supportedEdgeModes);
        if (supportedEdgeModes != null) {
            String[] strArr2 = new String[supportedEdgeModes.size()];
            int i2 = 0;
            for (String entryForNoiseReductionMode : supportedEdgeModes) {
                strArr2[i2] = getMainUI().getEntryForNoiseReductionMode(entryForNoiseReductionMode);
                i2++;
            }
            bundle.putStringArray("edge_modes_entries", strArr2);
        }
        List<String> supportedNoiseReductionModes = this.preview.getSupportedNoiseReductionModes();
        putBundleExtra(bundle, "noise_reduction_modes", supportedNoiseReductionModes);
        if (supportedNoiseReductionModes != null) {
            String[] strArr3 = new String[supportedNoiseReductionModes.size()];
            int i3 = 0;
            for (String entryForNoiseReductionMode2 : supportedNoiseReductionModes) {
                strArr3[i3] = getMainUI().getEntryForNoiseReductionMode(entryForNoiseReductionMode2);
                i3++;
            }
            bundle.putStringArray("noise_reduction_modes_entries", strArr3);
        }
        List<Size> supportedPreviewSizes = this.preview.getSupportedPreviewSizes();
        if (supportedPreviewSizes != null) {
            int[] iArr = new int[supportedPreviewSizes.size()];
            int[] iArr2 = new int[supportedPreviewSizes.size()];
            int i4 = 0;
            for (Size size : supportedPreviewSizes) {
                iArr[i4] = size.width;
                iArr2[i4] = size.height;
                i4++;
            }
            bundle.putIntArray("preview_widths", iArr);
            bundle.putIntArray("preview_heights", iArr2);
        }
        bundle.putInt("preview_width", this.preview.getCurrentPreviewSize().width);
        bundle.putInt("preview_height", this.preview.getCurrentPreviewSize().height);
        List<Size> supportedPictureSizes = this.preview.getSupportedPictureSizes(false);
        if (supportedPictureSizes != null) {
            int[] iArr3 = new int[supportedPictureSizes.size()];
            int[] iArr4 = new int[supportedPictureSizes.size()];
            boolean[] zArr = new boolean[supportedPictureSizes.size()];
            int i5 = 0;
            for (Size size2 : supportedPictureSizes) {
                iArr3[i5] = size2.width;
                iArr4[i5] = size2.height;
                zArr[i5] = size2.supports_burst;
                i5++;
            }
            bundle.putIntArray("resolution_widths", iArr3);
            bundle.putIntArray("resolution_heights", iArr4);
            bundle.putBooleanArray("resolution_supports_burst", zArr);
        }
        if (this.preview.getCurrentPictureSize() != null) {
            bundle.putInt("resolution_width", this.preview.getCurrentPictureSize().width);
            bundle.putInt("resolution_height", this.preview.getCurrentPictureSize().height);
        }
        String videoFPSPref = this.applicationInterface.getVideoFPSPref();
        List<String> supportedVideoQuality = this.preview.getSupportedVideoQuality(videoFPSPref);
        if (supportedVideoQuality == null || supportedVideoQuality.size() == 0) {
            Log.e(TAG, "can't find any supported video sizes for current fps!");
            supportedVideoQuality = this.preview.getVideoQualityHander().getSupportedVideoQuality();
        }
        if (!(supportedVideoQuality == null || this.preview.getCameraController() == null)) {
            String[] strArr4 = new String[supportedVideoQuality.size()];
            String[] strArr5 = new String[supportedVideoQuality.size()];
            int i6 = 0;
            for (String str : supportedVideoQuality) {
                strArr4[i6] = str;
                strArr5[i6] = this.preview.getCamcorderProfileDescription(str);
                i6++;
            }
            bundle.putStringArray("video_quality", strArr4);
            bundle.putStringArray("video_quality_string", strArr5);
            boolean fpsIsHighSpeed = this.preview.fpsIsHighSpeed(videoFPSPref);
            bundle.putBoolean("video_is_high_speed", fpsIsHighSpeed);
            bundle.putString("video_quality_preference_key", PreferenceKeys.getVideoQualityPreferenceKey(this.preview.getCameraId(), fpsIsHighSpeed));
        }
        if (this.preview.getVideoQualityHander().getCurrentVideoQuality() != null) {
            bundle.putString("current_video_quality", this.preview.getVideoQualityHander().getCurrentVideoQuality());
        }
        VideoProfile videoProfile = this.preview.getVideoProfile();
        bundle.putInt("video_frame_width", videoProfile.videoFrameWidth);
        bundle.putInt("video_frame_height", videoProfile.videoFrameHeight);
        bundle.putInt("video_bit_rate", videoProfile.videoBitRate);
        bundle.putInt("video_frame_rate", videoProfile.videoFrameRate);
        bundle.putDouble("video_capture_rate", videoProfile.videoCaptureRate);
        bundle.putBoolean("video_high_speed", this.preview.isVideoHighSpeed());
        bundle.putFloat("video_capture_rate_factor", this.applicationInterface.getVideoCaptureRateFactor());
        List<Size> supportedVideoSizes = this.preview.getVideoQualityHander().getSupportedVideoSizes();
        if (supportedVideoSizes != null) {
            int[] iArr5 = new int[supportedVideoSizes.size()];
            int[] iArr6 = new int[supportedVideoSizes.size()];
            int i7 = 0;
            for (Size size3 : supportedVideoSizes) {
                iArr5[i7] = size3.width;
                iArr6[i7] = size3.height;
                i7++;
            }
            bundle.putIntArray("video_widths", iArr5);
            bundle.putIntArray("video_heights", iArr6);
        }
        String str2 = "video_fps_high_speed";
        String str3 = "video_fps";
        if (this.preview.usingCamera2API()) {
            int[] iArr7 = {15, 24, 25, 30, 60, 96, 100, 120, 240};
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            for (int i8 : iArr7) {
                Preview preview2 = this.preview;
                StringBuilder sb = new StringBuilder();
                sb.append(BuildConfig.FLAVOR);
                sb.append(i8);
                if (preview2.fpsIsHighSpeed(sb.toString())) {
                    arrayList.add(Integer.valueOf(i8));
                    arrayList2.add(Boolean.valueOf(true));
                } else if (this.preview.getVideoQualityHander().videoSupportsFrameRate(i8)) {
                    arrayList.add(Integer.valueOf(i8));
                    arrayList2.add(Boolean.valueOf(false));
                }
            }
            int[] iArr8 = new int[arrayList.size()];
            for (int i9 = 0; i9 < arrayList.size(); i9++) {
                iArr8[i9] = ((Integer) arrayList.get(i9)).intValue();
            }
            bundle.putIntArray(str3, iArr8);
            boolean[] zArr2 = new boolean[arrayList2.size()];
            for (int i10 = 0; i10 < arrayList2.size(); i10++) {
                zArr2[i10] = ((Boolean) arrayList2.get(i10)).booleanValue();
            }
            bundle.putBooleanArray(str2, zArr2);
        } else {
            int[] iArr9 = {15, 24, 25, 30, 60, 96, 100, 120};
            bundle.putIntArray(str3, iArr9);
            boolean[] zArr3 = new boolean[iArr9.length];
            for (int i11 = 0; i11 < iArr9.length; i11++) {
                zArr3[i11] = false;
            }
            bundle.putBooleanArray(str2, zArr3);
        }
        putBundleExtra(bundle, "flash_values", this.preview.getSupportedFlashValues());
        putBundleExtra(bundle, "focus_values", this.preview.getSupportedFocusValues());
        this.preferencesListener.startListening();
        showPreview(false);
        setWindowFlagsForSettings();
        MyPreferenceFragment myPreferenceFragment = new MyPreferenceFragment();
        myPreferenceFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().add(16908290, myPreferenceFragment, "PREFERENCE_FRAGMENT").addToBackStack(null).commitAllowingStateLoss();
    }

    public void updateForSettings() {
        updateForSettings(null, false);
    }

    public void updateForSettings(String str) {
        updateForSettings(str, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0075  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x008e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateForSettings(java.lang.String r6, boolean r7) {
        /*
            r5 = this;
            net.sourceforge.opencamera.SaveLocationHistory r0 = r5.save_location_history
            net.sourceforge.opencamera.StorageUtils r1 = r5.getStorageUtils()
            java.lang.String r1 = r1.getSaveLocation()
            r2 = 1
            r0.updateFolderHistory(r1, r2)
            r5.imageQueueChanged()
            if (r7 != 0) goto L_0x0018
            net.sourceforge.opencamera.ui.MainUI r7 = r5.mainUI
            r7.destroyPopup()
        L_0x0018:
            net.sourceforge.opencamera.preview.Preview r7 = r5.preview
            net.sourceforge.opencamera.cameracontroller.CameraController r7 = r7.getCameraController()
            r0 = 0
            if (r7 == 0) goto L_0x005a
            android.content.SharedPreferences r7 = android.preference.PreferenceManager.getDefaultSharedPreferences(r5)
            net.sourceforge.opencamera.preview.Preview r1 = r5.preview
            net.sourceforge.opencamera.cameracontroller.CameraController r1 = r1.getCameraController()
            java.lang.String r1 = r1.getSceneMode()
            java.lang.String r3 = "preference_scene_mode"
            java.lang.String r4 = "auto"
            java.lang.String r7 = r7.getString(r3, r4)
            boolean r7 = r7.equals(r1)
            if (r7 != 0) goto L_0x003f
        L_0x003d:
            r7 = 1
            goto L_0x005b
        L_0x003f:
            net.sourceforge.opencamera.MyApplicationInterface r7 = r5.applicationInterface
            boolean r7 = r7.useCamera2()
            if (r7 == 0) goto L_0x005a
            net.sourceforge.opencamera.preview.Preview r7 = r5.preview
            net.sourceforge.opencamera.cameracontroller.CameraController r7 = r7.getCameraController()
            boolean r7 = r7.getUseCamera2FakeFlash()
            net.sourceforge.opencamera.MyApplicationInterface r1 = r5.applicationInterface
            boolean r1 = r1.useCamera2FakeFlash()
            if (r1 == r7) goto L_0x005a
            goto L_0x003d
        L_0x005a:
            r7 = 0
        L_0x005b:
            net.sourceforge.opencamera.ui.MainUI r1 = r5.mainUI
            r1.layoutUI()
            r5.checkDisableGUIIcons()
            android.content.SharedPreferences r1 = android.preference.PreferenceManager.getDefaultSharedPreferences(r5)
            java.lang.String r3 = "none"
            java.lang.String r4 = "preference_audio_control"
            java.lang.String r1 = r1.getString(r4, r3)
            boolean r1 = r1.equals(r3)
            if (r1 == 0) goto L_0x0081
            r1 = 2131099658(0x7f06000a, float:1.7811675E38)
            android.view.View r1 = r5.findViewById(r1)
            r3 = 8
            r1.setVisibility(r3)
        L_0x0081:
            net.sourceforge.opencamera.SpeechControl r1 = r5.speechControl
            r1.initSpeechRecognizer()
            r5.initLocation()
            r5.initGyroSensors()
            if (r6 == 0) goto L_0x0090
            r5.block_startup_toast = r2
        L_0x0090:
            if (r7 != 0) goto L_0x00ab
            net.sourceforge.opencamera.preview.Preview r7 = r5.preview
            net.sourceforge.opencamera.cameracontroller.CameraController r7 = r7.getCameraController()
            if (r7 != 0) goto L_0x009b
            goto L_0x00ab
        L_0x009b:
            net.sourceforge.opencamera.preview.Preview r7 = r5.preview
            r7.setCameraDisplayOrientation()
            net.sourceforge.opencamera.preview.Preview r7 = r5.preview
            r7.pausePreview(r2)
            net.sourceforge.opencamera.preview.Preview r7 = r5.preview
            r7.setupCamera(r0)
            goto L_0x00b0
        L_0x00ab:
            net.sourceforge.opencamera.preview.Preview r7 = r5.preview
            r7.reopenCamera()
        L_0x00b0:
            if (r6 == 0) goto L_0x00be
            int r7 = r6.length()
            if (r7 <= 0) goto L_0x00be
            net.sourceforge.opencamera.preview.Preview r7 = r5.preview
            r0 = 0
            r7.showToast(r0, r6)
        L_0x00be:
            net.sourceforge.opencamera.MagneticSensor r6 = r5.magneticSensor
            android.hardware.SensorManager r7 = r5.mSensorManager
            r6.registerMagneticListener(r7)
            net.sourceforge.opencamera.MagneticSensor r6 = r5.magneticSensor
            r6.checkMagneticAccuracy()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MainActivity.updateForSettings(java.lang.String, boolean):void");
    }

    private void checkDisableGUIIcons() {
        if (!this.mainUI.showExposureLockIcon()) {
            findViewById(C0316R.C0318id.exposure_lock).setVisibility(8);
        }
        if (!this.mainUI.showWhiteBalanceLockIcon()) {
            findViewById(C0316R.C0318id.white_balance_lock).setVisibility(8);
        }
        if (!this.mainUI.showCycleRawIcon()) {
            findViewById(C0316R.C0318id.cycle_raw).setVisibility(8);
        }
        if (!this.mainUI.showStoreLocationIcon()) {
            findViewById(C0316R.C0318id.store_location).setVisibility(8);
        }
        if (!this.mainUI.showTextStampIcon()) {
            findViewById(C0316R.C0318id.text_stamp).setVisibility(8);
        }
        if (!this.mainUI.showStampIcon()) {
            findViewById(C0316R.C0318id.stamp).setVisibility(8);
        }
        if (!this.mainUI.showAutoLevelIcon()) {
            findViewById(C0316R.C0318id.auto_level).setVisibility(8);
        }
        if (!this.mainUI.showCycleFlashIcon()) {
            findViewById(C0316R.C0318id.cycle_flash).setVisibility(8);
        }
        if (!this.mainUI.showFaceDetectionIcon()) {
            findViewById(C0316R.C0318id.face_detection).setVisibility(8);
        }
    }

    public MyPreferenceFragment getPreferenceFragment() {
        return (MyPreferenceFragment) getFragmentManager().findFragmentByTag("PREFERENCE_FRAGMENT");
    }

    private boolean settingsIsOpen() {
        return getPreferenceFragment() != null;
    }

    private void settingsClosing() {
        setWindowFlagsForCamera();
        showPreview(true);
        this.preferencesListener.stopListening();
        this.applicationInterface.getDrawPreview().updateSettings();
        if (this.preferencesListener.anyChange()) {
            this.mainUI.updateOnScreenIcons();
        }
        if (this.preferencesListener.anySignificantChange()) {
            updateForSettings();
        } else if (this.preferencesListener.anyChange()) {
            this.mainUI.destroyPopup();
        }
    }

    public void onBackPressed() {
        if (this.screen_is_locked) {
            this.preview.showToast(this.screen_locked_toast, (int) C0316R.string.screen_is_locked);
            return;
        }
        if (settingsIsOpen()) {
            settingsClosing();
        } else {
            Preview preview2 = this.preview;
            if (preview2 != null && preview2.isPreviewPaused()) {
                this.preview.startCameraPreview();
                return;
            } else if (popupIsOpen()) {
                closePopup();
                return;
            }
        }
        super.onBackPressed();
    }

    public boolean usingKitKatImmersiveMode() {
        if (VERSION.SDK_INT >= 19) {
            String string = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.ImmersiveModePreferenceKey, "immersive_mode_low_profile");
            if (string.equals("immersive_mode_gui") || string.equals("immersive_mode_everything")) {
                return true;
            }
        }
        return false;
    }

    public boolean usingKitKatImmersiveModeEverything() {
        return VERSION.SDK_INT >= 19 && PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.ImmersiveModePreferenceKey, "immersive_mode_low_profile").equals("immersive_mode_everything");
    }

    /* access modifiers changed from: private */
    public void setImmersiveTimer() {
        Handler handler = this.immersive_timer_handler;
        if (handler != null) {
            Runnable runnable = this.immersive_timer_runnable;
            if (runnable != null) {
                handler.removeCallbacks(runnable);
            }
        }
        this.immersive_timer_handler = new Handler();
        Handler handler2 = this.immersive_timer_handler;
        C024713 r1 = new Runnable() {
            public void run() {
                if (!MainActivity.this.camera_in_background && !MainActivity.this.popupIsOpen() && MainActivity.this.usingKitKatImmersiveMode()) {
                    MainActivity.this.setImmersiveMode(true);
                }
            }
        };
        this.immersive_timer_runnable = r1;
        handler2.postDelayed(r1, 5000);
    }

    public void initImmersiveMode() {
        if (!usingKitKatImmersiveMode()) {
            setImmersiveMode(true);
        } else {
            setImmersiveTimer();
        }
    }

    /* access modifiers changed from: 0000 */
    public void setImmersiveMode(boolean z) {
        if (!z) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else if (VERSION.SDK_INT < 19 || !usingKitKatImmersiveMode()) {
            String str = "immersive_mode_low_profile";
            if (PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.ImmersiveModePreferenceKey, str).equals(str)) {
                getWindow().getDecorView().setSystemUiVisibility(1);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(0);
            }
        } else if (this.applicationInterface.getPhotoMode() == PhotoMode.Panorama) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(2310);
        }
    }

    public void setBrightnessForCamera(boolean z) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final LayoutParams attributes = getWindow().getAttributes();
        if (z || defaultSharedPreferences.getBoolean(PreferenceKeys.getMaxBrightnessPreferenceKey(), true)) {
            attributes.screenBrightness = 1.0f;
        } else {
            attributes.screenBrightness = -1.0f;
        }
        runOnUiThread(new Runnable() {
            public void run() {
                MainActivity.this.getWindow().setAttributes(attributes);
            }
        });
    }

    public void setBrightnessToMinimumIfWanted() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final LayoutParams attributes = getWindow().getAttributes();
        if (defaultSharedPreferences.getBoolean(PreferenceKeys.DimWhenDisconnectedPreferenceKey, false)) {
            attributes.screenBrightness = 0.0f;
        } else {
            attributes.screenBrightness = -1.0f;
        }
        runOnUiThread(new Runnable() {
            public void run() {
                MainActivity.this.getWindow().setAttributes(attributes);
            }
        });
    }

    public void setWindowFlagsForCamera() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setRequestedOrientation(0);
        Preview preview2 = this.preview;
        if (preview2 != null) {
            preview2.setCameraDisplayOrientation();
        }
        if (this.preview != null) {
            MainUI mainUI2 = this.mainUI;
            if (mainUI2 != null) {
                mainUI2.layoutUI();
            }
        }
        if (defaultSharedPreferences.getBoolean(PreferenceKeys.getKeepDisplayOnPreferenceKey(), true)) {
            getWindow().addFlags(128);
        } else {
            getWindow().clearFlags(128);
        }
        if (defaultSharedPreferences.getBoolean(PreferenceKeys.getShowWhenLockedPreferenceKey(), true)) {
            showWhenLocked(true);
        } else {
            showWhenLocked(false);
        }
        setBrightnessForCamera(false);
        initImmersiveMode();
        this.camera_in_background = false;
        this.magneticSensor.clearDialog();
    }

    private void setWindowFlagsForSettings() {
        setWindowFlagsForSettings(true);
    }

    public void setWindowFlagsForSettings(boolean z) {
        setRequestedOrientation(-1);
        getWindow().clearFlags(128);
        if (z) {
            showWhenLocked(false);
        }
        LayoutParams attributes = getWindow().getAttributes();
        attributes.screenBrightness = -1.0f;
        getWindow().setAttributes(attributes);
        setImmersiveMode(false);
        this.camera_in_background = true;
    }

    private void showWhenLocked(boolean z) {
        if (z) {
            getWindow().addFlags(524288);
        } else {
            getWindow().clearFlags(524288);
        }
    }

    public void showAlert(final AlertDialog alertDialog) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                alertDialog.show();
            }
        }, 20);
    }

    public void showPreview(boolean z) {
        ((ViewGroup) findViewById(C0316R.C0318id.hide_container)).setVisibility(z ? 8 : 0);
    }

    /* access modifiers changed from: private */
    public void updateGalleryIconToBlank() {
        ImageButton imageButton = (ImageButton) findViewById(C0316R.C0318id.gallery);
        int paddingBottom = imageButton.getPaddingBottom();
        int paddingTop = imageButton.getPaddingTop();
        int paddingRight = imageButton.getPaddingRight();
        int paddingLeft = imageButton.getPaddingLeft();
        imageButton.setImageBitmap(null);
        imageButton.setImageResource(C0316R.C0317drawable.baseline_photo_library_white_48);
        imageButton.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        this.gallery_bitmap = null;
    }

    /* access modifiers changed from: 0000 */
    public void updateGalleryIcon(Bitmap bitmap) {
        ((ImageButton) findViewById(C0316R.C0318id.gallery)).setImageBitmap(bitmap);
        this.gallery_bitmap = bitmap;
    }

    public void updateGalleryIcon() {
        final boolean equals = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.GhostImagePreferenceKey, "preference_ghost_image_off").equals("preference_ghost_image_last");
        new AsyncTask<Void, Void, Bitmap>() {
            private static final String TAG = "MainActivity/AsyncTask";
            private boolean is_video;

            /* access modifiers changed from: protected */
            /* JADX WARNING: Removed duplicated region for block: B:39:0x00c0 A[SYNTHETIC, Splitter:B:39:0x00c0] */
            /* JADX WARNING: Removed duplicated region for block: B:53:0x0117 A[Catch:{ all -> 0x011c }] */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public android.graphics.Bitmap doInBackground(java.lang.Void... r13) {
                /*
                    r12 = this;
                    java.lang.String r13 = "MainActivity/AsyncTask"
                    net.sourceforge.opencamera.MainActivity r0 = net.sourceforge.opencamera.MainActivity.this
                    net.sourceforge.opencamera.MyApplicationInterface r0 = r0.applicationInterface
                    net.sourceforge.opencamera.StorageUtils r0 = r0.getStorageUtils()
                    net.sourceforge.opencamera.StorageUtils$Media r0 = r0.getLatestMedia()
                    net.sourceforge.opencamera.MainActivity r1 = net.sourceforge.opencamera.MainActivity.this
                    java.lang.String r2 = "keyguard"
                    java.lang.Object r1 = r1.getSystemService(r2)
                    android.app.KeyguardManager r1 = (android.app.KeyguardManager) r1
                    r2 = 0
                    r3 = 1
                    if (r1 == 0) goto L_0x0026
                    boolean r1 = r1.inKeyguardRestrictedInputMode()
                    if (r1 == 0) goto L_0x0026
                    r1 = 1
                    goto L_0x0027
                L_0x0026:
                    r1 = 0
                L_0x0027:
                    r4 = 0
                    if (r0 == 0) goto L_0x011d
                    net.sourceforge.opencamera.MainActivity r5 = net.sourceforge.opencamera.MainActivity.this
                    android.content.ContentResolver r5 = r5.getContentResolver()
                    if (r5 == 0) goto L_0x011d
                    if (r1 != 0) goto L_0x011d
                    boolean r1 = r0
                    if (r1 == 0) goto L_0x00bd
                    boolean r1 = r0.video
                    if (r1 != 0) goto L_0x00bd
                    android.graphics.BitmapFactory$Options r1 = new android.graphics.BitmapFactory$Options     // Catch:{ IOException -> 0x00b1 }
                    r1.<init>()     // Catch:{ IOException -> 0x00b1 }
                    net.sourceforge.opencamera.MainActivity r5 = net.sourceforge.opencamera.MainActivity.this     // Catch:{ IOException -> 0x00b1 }
                    android.content.ContentResolver r5 = r5.getContentResolver()     // Catch:{ IOException -> 0x00b1 }
                    android.net.Uri r6 = r0.uri     // Catch:{ IOException -> 0x00b1 }
                    java.io.InputStream r5 = r5.openInputStream(r6)     // Catch:{ IOException -> 0x00b1 }
                    r1.inJustDecodeBounds = r3     // Catch:{ IOException -> 0x00b1 }
                    android.graphics.BitmapFactory.decodeStream(r5, r4, r1)     // Catch:{ IOException -> 0x00b1 }
                    int r6 = r1.outWidth     // Catch:{ IOException -> 0x00b1 }
                    int r7 = r1.outHeight     // Catch:{ IOException -> 0x00b1 }
                    android.graphics.Point r8 = new android.graphics.Point     // Catch:{ IOException -> 0x00b1 }
                    r8.<init>()     // Catch:{ IOException -> 0x00b1 }
                    net.sourceforge.opencamera.MainActivity r9 = net.sourceforge.opencamera.MainActivity.this     // Catch:{ IOException -> 0x00b1 }
                    android.view.WindowManager r9 = r9.getWindowManager()     // Catch:{ IOException -> 0x00b1 }
                    android.view.Display r9 = r9.getDefaultDisplay()     // Catch:{ IOException -> 0x00b1 }
                    r9.getSize(r8)     // Catch:{ IOException -> 0x00b1 }
                    int r9 = r8.x     // Catch:{ IOException -> 0x00b1 }
                    int r10 = r8.y     // Catch:{ IOException -> 0x00b1 }
                    if (r9 >= r10) goto L_0x0075
                    int r9 = r8.y     // Catch:{ IOException -> 0x00b1 }
                    int r10 = r8.x     // Catch:{ IOException -> 0x00b1 }
                    r8.set(r9, r10)     // Catch:{ IOException -> 0x00b1 }
                L_0x0075:
                    if (r6 >= r7) goto L_0x0078
                    goto L_0x0079
                L_0x0078:
                    r6 = r7
                L_0x0079:
                    r1.inSampleSize = r3     // Catch:{ IOException -> 0x00b1 }
                L_0x007b:
                    int r7 = r1.inSampleSize     // Catch:{ IOException -> 0x00b1 }
                    int r7 = r7 * 2
                    int r7 = r6 / r7
                    int r9 = r8.y     // Catch:{ IOException -> 0x00b1 }
                    if (r7 < r9) goto L_0x008c
                    int r7 = r1.inSampleSize     // Catch:{ IOException -> 0x00b1 }
                    int r7 = r7 * 2
                    r1.inSampleSize = r7     // Catch:{ IOException -> 0x00b1 }
                    goto L_0x007b
                L_0x008c:
                    r1.inJustDecodeBounds = r2     // Catch:{ IOException -> 0x00b1 }
                    r5.close()     // Catch:{ IOException -> 0x00b1 }
                    net.sourceforge.opencamera.MainActivity r2 = net.sourceforge.opencamera.MainActivity.this     // Catch:{ IOException -> 0x00b1 }
                    android.content.ContentResolver r2 = r2.getContentResolver()     // Catch:{ IOException -> 0x00b1 }
                    android.net.Uri r5 = r0.uri     // Catch:{ IOException -> 0x00b1 }
                    java.io.InputStream r2 = r2.openInputStream(r5)     // Catch:{ IOException -> 0x00b1 }
                    android.graphics.Bitmap r1 = android.graphics.BitmapFactory.decodeStream(r2, r4, r1)     // Catch:{ IOException -> 0x00b1 }
                    if (r1 != 0) goto L_0x00a8
                    java.lang.String r5 = "decodeStream returned null bitmap for ghost image last"
                    android.util.Log.e(r13, r5)     // Catch:{ IOException -> 0x00ac }
                L_0x00a8:
                    r2.close()     // Catch:{ IOException -> 0x00ac }
                    goto L_0x00be
                L_0x00ac:
                    r2 = move-exception
                    r11 = r2
                    r2 = r1
                    r1 = r11
                    goto L_0x00b3
                L_0x00b1:
                    r1 = move-exception
                    r2 = r4
                L_0x00b3:
                    java.lang.String r5 = "failed to load bitmap for ghost image last"
                    android.util.Log.e(r13, r5)
                    r1.printStackTrace()
                    r1 = r2
                    goto L_0x00be
                L_0x00bd:
                    r1 = r4
                L_0x00be:
                    if (r1 != 0) goto L_0x00e4
                    boolean r13 = r0.video     // Catch:{ all -> 0x00e0 }
                    if (r13 == 0) goto L_0x00d3
                    net.sourceforge.opencamera.MainActivity r13 = net.sourceforge.opencamera.MainActivity.this     // Catch:{ all -> 0x00e0 }
                    android.content.ContentResolver r13 = r13.getContentResolver()     // Catch:{ all -> 0x00e0 }
                    long r5 = r0.f18id     // Catch:{ all -> 0x00e0 }
                    android.graphics.Bitmap r1 = android.provider.MediaStore.Video.Thumbnails.getThumbnail(r13, r5, r3, r4)     // Catch:{ all -> 0x00e0 }
                    r12.is_video = r3     // Catch:{ all -> 0x00e0 }
                    goto L_0x00e4
                L_0x00d3:
                    net.sourceforge.opencamera.MainActivity r13 = net.sourceforge.opencamera.MainActivity.this     // Catch:{ all -> 0x00e0 }
                    android.content.ContentResolver r13 = r13.getContentResolver()     // Catch:{ all -> 0x00e0 }
                    long r5 = r0.f18id     // Catch:{ all -> 0x00e0 }
                    android.graphics.Bitmap r1 = android.provider.MediaStore.Images.Thumbnails.getThumbnail(r13, r5, r3, r4)     // Catch:{ all -> 0x00e0 }
                    goto L_0x00e4
                L_0x00e0:
                    r13 = move-exception
                    r13.printStackTrace()
                L_0x00e4:
                    if (r1 == 0) goto L_0x011c
                    int r13 = r0.orientation
                    if (r13 == 0) goto L_0x011c
                    android.graphics.Matrix r7 = new android.graphics.Matrix
                    r7.<init>()
                    int r13 = r0.orientation
                    float r13 = (float) r13
                    int r0 = r1.getWidth()
                    float r0 = (float) r0
                    r2 = 1056964608(0x3f000000, float:0.5)
                    float r0 = r0 * r2
                    int r3 = r1.getHeight()
                    float r3 = (float) r3
                    float r3 = r3 * r2
                    r7.setRotate(r13, r0, r3)
                    r3 = 0
                    r4 = 0
                    int r5 = r1.getWidth()     // Catch:{ all -> 0x011c }
                    int r6 = r1.getHeight()     // Catch:{ all -> 0x011c }
                    r8 = 1
                    r2 = r1
                    android.graphics.Bitmap r13 = android.graphics.Bitmap.createBitmap(r2, r3, r4, r5, r6, r7, r8)     // Catch:{ all -> 0x011c }
                    if (r13 == r1) goto L_0x011c
                    r1.recycle()     // Catch:{ all -> 0x011c }
                    r4 = r13
                    goto L_0x011d
                L_0x011c:
                    r4 = r1
                L_0x011d:
                    return r4
                */
                throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MainActivity.C025117.doInBackground(java.lang.Void[]):android.graphics.Bitmap");
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Bitmap bitmap) {
                MainActivity.this.applicationInterface.getStorageUtils().clearLastMediaScanned();
                if (bitmap != null) {
                    MainActivity.this.updateGalleryIcon(bitmap);
                    MainActivity.this.applicationInterface.getDrawPreview().updateThumbnail(bitmap, this.is_video, false);
                    return;
                }
                MainActivity.this.updateGalleryIconToBlank();
            }
        }.execute(new Void[0]);
    }

    /* access modifiers changed from: 0000 */
    public void savingImage(final boolean z) {
        runOnUiThread(new Runnable() {
            public void run() {
                final ImageButton imageButton = (ImageButton) MainActivity.this.findViewById(C0316R.C0318id.gallery);
                if (z) {
                    if (MainActivity.this.gallery_save_anim == null) {
                        MainActivity.this.gallery_save_anim = ValueAnimator.ofInt(new int[]{Color.argb(200, 255, 255, 255), Color.argb(63, 255, 255, 255)});
                        MainActivity.this.gallery_save_anim.setEvaluator(new ArgbEvaluator());
                        MainActivity.this.gallery_save_anim.setRepeatCount(-1);
                        MainActivity.this.gallery_save_anim.setRepeatMode(2);
                        MainActivity.this.gallery_save_anim.setDuration(500);
                    }
                    MainActivity.this.gallery_save_anim.addUpdateListener(new AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            imageButton.setColorFilter(((Integer) valueAnimator.getAnimatedValue()).intValue(), Mode.MULTIPLY);
                        }
                    });
                    MainActivity.this.gallery_save_anim.start();
                } else if (MainActivity.this.gallery_save_anim != null) {
                    MainActivity.this.gallery_save_anim.cancel();
                }
                imageButton.setColorFilter(null);
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void imageQueueChanged() {
        this.applicationInterface.getDrawPreview().setImageQueueFull(!this.applicationInterface.canTakeNewPhoto());
        if (this.applicationInterface.getImageSaver().getNImagesToSave() == 0) {
            cancelImageSavingNotification();
        } else if (this.has_notification) {
            createImageSavingNotification();
        }
    }

    private void createImageSavingNotification() {
        if (VERSION.SDK_INT >= 26) {
            int nRealImagesToSave = this.applicationInterface.getImageSaver().getNRealImagesToSave();
            Notification.Builder contentTitle = new Notification.Builder(this, "open_camera_channel").setSmallIcon(C0316R.C0317drawable.ic_stat_notify_take_photo).setContentTitle(getString(C0316R.string.app_name));
            StringBuilder sb = new StringBuilder();
            sb.append(getString(C0316R.string.image_saving_notification));
            String str = " ";
            sb.append(str);
            sb.append(nRealImagesToSave);
            sb.append(str);
            sb.append(getString(C0316R.string.remaining));
            ((NotificationManager) getSystemService(NotificationManager.class)).notify(1, contentTitle.setContentText(sb.toString()).build());
            this.has_notification = true;
        }
    }

    private void cancelImageSavingNotification() {
        if (VERSION.SDK_INT >= 26) {
            ((NotificationManager) getSystemService(NotificationManager.class)).cancel(1);
            this.has_notification = false;
        }
    }

    public void clickedGallery(View view) {
        openGallery();
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0036 A[SYNTHETIC, Splitter:B:12:0x0036] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0052  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0066  */
    /* JADX WARNING: Removed duplicated region for block: B:38:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:39:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void openGallery() {
        /*
            r7 = this;
            net.sourceforge.opencamera.MyApplicationInterface r0 = r7.applicationInterface
            net.sourceforge.opencamera.StorageUtils r0 = r0.getStorageUtils()
            android.net.Uri r0 = r0.getLastMediaScanned()
            r1 = 1
            r2 = 0
            if (r0 != 0) goto L_0x0032
            net.sourceforge.opencamera.MyApplicationInterface r3 = r7.applicationInterface
            net.sourceforge.opencamera.StorageUtils r3 = r3.getStorageUtils()
            net.sourceforge.opencamera.StorageUtils$Media r3 = r3.getLatestMedia()
            if (r3 == 0) goto L_0x0032
            android.net.Uri r0 = r3.uri
            java.lang.String r4 = r3.path
            if (r4 == 0) goto L_0x0032
            java.lang.String r3 = r3.path
            java.util.Locale r4 = java.util.Locale.US
            java.lang.String r3 = r3.toLowerCase(r4)
            java.lang.String r4 = ".dng"
            boolean r3 = r3.endsWith(r4)
            if (r3 == 0) goto L_0x0032
            r3 = 1
            goto L_0x0033
        L_0x0032:
            r3 = 0
        L_0x0033:
            r4 = 0
            if (r0 == 0) goto L_0x0049
            android.content.ContentResolver r5 = r7.getContentResolver()     // Catch:{ IOException -> 0x0047 }
            java.lang.String r6 = "r"
            android.os.ParcelFileDescriptor r5 = r5.openFileDescriptor(r0, r6)     // Catch:{ IOException -> 0x0047 }
            if (r5 != 0) goto L_0x0043
            goto L_0x0047
        L_0x0043:
            r5.close()     // Catch:{ IOException -> 0x0047 }
            goto L_0x0049
        L_0x0047:
            r0 = r4
            r3 = 0
        L_0x0049:
            if (r0 != 0) goto L_0x004e
            android.net.Uri r0 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            r3 = 0
        L_0x004e:
            boolean r5 = r7.is_test
            if (r5 != 0) goto L_0x008f
            if (r3 != 0) goto L_0x0063
            android.content.Intent r3 = new android.content.Intent     // Catch:{ ActivityNotFoundException -> 0x005f }
            java.lang.String r5 = "com.android.camera.action.REVIEW"
            r3.<init>(r5, r0)     // Catch:{ ActivityNotFoundException -> 0x005f }
            r7.startActivity(r3)     // Catch:{ ActivityNotFoundException -> 0x005f }
            goto L_0x0064
        L_0x005f:
            r1 = move-exception
            r1.printStackTrace()
        L_0x0063:
            r1 = 0
        L_0x0064:
            if (r1 != 0) goto L_0x008f
            android.content.Intent r1 = new android.content.Intent
            java.lang.String r2 = "android.intent.action.VIEW"
            r1.<init>(r2, r0)
            android.content.pm.PackageManager r0 = r7.getPackageManager()
            android.content.ComponentName r0 = r1.resolveActivity(r0)
            if (r0 == 0) goto L_0x0087
            r7.startActivity(r1)     // Catch:{ SecurityException -> 0x007b }
            goto L_0x008f
        L_0x007b:
            r0 = move-exception
            java.lang.String r1 = "MainActivity"
            java.lang.String r2 = "SecurityException from ACTION_VIEW startActivity"
            android.util.Log.e(r1, r2)
            r0.printStackTrace()
            goto L_0x008f
        L_0x0087:
            net.sourceforge.opencamera.preview.Preview r0 = r7.preview
            r1 = 2131493024(0x7f0c00a0, float:1.8609516E38)
            r0.showToast(r4, r1)
        L_0x008f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MainActivity.openGallery():void");
    }

    /* access modifiers changed from: 0000 */
    public void openFolderChooserDialogSAF(boolean z) {
        this.saf_dialog_from_preferences = z;
        startActivityForResult(new Intent("android.intent.action.OPEN_DOCUMENT_TREE"), 42);
    }

    /* access modifiers changed from: 0000 */
    public void openGhostImageChooserDialogSAF(boolean z) {
        this.saf_dialog_from_preferences = z;
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("image/*");
        try {
            startActivityForResult(intent, 43);
        } catch (ActivityNotFoundException e) {
            this.preview.showToast((ToastBoxer) null, (int) C0316R.string.open_files_saf_exception_ghost);
            Log.e(TAG, "ActivityNotFoundException from startActivityForResult");
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: 0000 */
    public void openLoadSettingsChooserDialogSAF(boolean z) {
        this.saf_dialog_from_preferences = z;
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("text/xml");
        try {
            startActivityForResult(intent, 44);
        } catch (ActivityNotFoundException e) {
            this.preview.showToast((ToastBoxer) null, (int) C0316R.string.open_files_saf_exception_generic);
            Log.e(TAG, "ActivityNotFoundException from startActivityForResult");
            e.printStackTrace();
        }
    }

    public void updateFolderHistorySAF(String str) {
        if (this.save_location_history_saf == null) {
            this.save_location_history_saf = new SaveLocationHistory(this, "save_location_history_saf", str);
        }
        this.save_location_history_saf.updateFolderHistory(str, true);
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        String str = "SecurityException failed to take permission";
        String str2 = TAG;
        String str3 = BuildConfig.FLAVOR;
        switch (i) {
            case 42:
                if (i2 != -1 || intent == null) {
                    SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    if (defaultSharedPreferences.getString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), str3).length() == 0) {
                        Editor edit = defaultSharedPreferences.edit();
                        edit.putBoolean(PreferenceKeys.getUsingSAFPreferenceKey(), false);
                        edit.apply();
                        this.preview.showToast((ToastBoxer) null, (int) C0316R.string.saf_cancelled);
                    }
                } else {
                    Uri data = intent.getData();
                    try {
                        getContentResolver().takePersistableUriPermission(data, intent.getFlags() & 3);
                        Editor edit2 = PreferenceManager.getDefaultSharedPreferences(this).edit();
                        edit2.putString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), data.toString());
                        edit2.apply();
                        updateFolderHistorySAF(data.toString());
                        File imageFolder = this.applicationInterface.getStorageUtils().getImageFolder();
                        if (imageFolder != null) {
                            Preview preview2 = this.preview;
                            StringBuilder sb = new StringBuilder();
                            sb.append(getResources().getString(C0316R.string.changed_save_location));
                            sb.append("\n");
                            sb.append(imageFolder.getAbsolutePath());
                            preview2.showToast((ToastBoxer) null, sb.toString());
                        }
                    } catch (SecurityException e) {
                        Log.e(str2, str);
                        e.printStackTrace();
                        this.preview.showToast((ToastBoxer) null, (int) C0316R.string.saf_permission_failed);
                        SharedPreferences defaultSharedPreferences2 = PreferenceManager.getDefaultSharedPreferences(this);
                        if (defaultSharedPreferences2.getString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), str3).length() == 0) {
                            Editor edit3 = defaultSharedPreferences2.edit();
                            edit3.putBoolean(PreferenceKeys.getUsingSAFPreferenceKey(), false);
                            edit3.apply();
                        }
                    }
                }
                if (!this.saf_dialog_from_preferences) {
                    setWindowFlagsForCamera();
                    showPreview(true);
                    return;
                }
                return;
            case 43:
                String str4 = "preference_ghost_image_off";
                String str5 = PreferenceKeys.GhostImagePreferenceKey;
                String str6 = PreferenceKeys.GhostSelectedImageSAFPreferenceKey;
                if (i2 != -1 || intent == null) {
                    SharedPreferences defaultSharedPreferences3 = PreferenceManager.getDefaultSharedPreferences(this);
                    if (defaultSharedPreferences3.getString(str6, str3).length() == 0) {
                        Editor edit4 = defaultSharedPreferences3.edit();
                        edit4.putString(str5, str4);
                        edit4.apply();
                    }
                } else {
                    Uri data2 = intent.getData();
                    try {
                        getContentResolver().takePersistableUriPermission(data2, intent.getFlags() & 3);
                        Editor edit5 = PreferenceManager.getDefaultSharedPreferences(this).edit();
                        edit5.putString(str6, data2.toString());
                        edit5.apply();
                    } catch (SecurityException e2) {
                        Log.e(str2, str);
                        e2.printStackTrace();
                        this.preview.showToast((ToastBoxer) null, (int) C0316R.string.saf_permission_failed_open_image);
                        SharedPreferences defaultSharedPreferences4 = PreferenceManager.getDefaultSharedPreferences(this);
                        if (defaultSharedPreferences4.getString(str6, str3).length() == 0) {
                            Editor edit6 = defaultSharedPreferences4.edit();
                            edit6.putString(str5, str4);
                            edit6.apply();
                        }
                    }
                }
                if (!this.saf_dialog_from_preferences) {
                    setWindowFlagsForCamera();
                    showPreview(true);
                    return;
                }
                return;
            case 44:
                if (i2 == -1 && intent != null) {
                    Uri data3 = intent.getData();
                    try {
                        getContentResolver().takePersistableUriPermission(data3, intent.getFlags() & 3);
                        this.settingsManager.loadSettings(data3);
                    } catch (SecurityException e3) {
                        Log.e(str2, str);
                        e3.printStackTrace();
                        this.preview.showToast((ToastBoxer) null, (int) C0316R.string.restore_settings_failed);
                    }
                }
                if (!this.saf_dialog_from_preferences) {
                    setWindowFlagsForCamera();
                    showPreview(true);
                    return;
                }
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: 0000 */
    public void updateSaveFolder(String str) {
        if (str != null) {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (!this.applicationInterface.getStorageUtils().getSaveLocation().equals(str)) {
                Editor edit = defaultSharedPreferences.edit();
                edit.putString(PreferenceKeys.getSaveLocationPreferenceKey(), str);
                edit.apply();
                this.save_location_history.updateFolderHistory(getStorageUtils().getSaveLocation(), true);
                Preview preview2 = this.preview;
                StringBuilder sb = new StringBuilder();
                sb.append(getResources().getString(C0316R.string.changed_save_location));
                sb.append("\n");
                sb.append(this.applicationInterface.getStorageUtils().getSaveLocation());
                preview2.showToast((ToastBoxer) null, sb.toString());
            }
        }
    }

    /* access modifiers changed from: private */
    public void openFolderChooserDialog() {
        showPreview(false);
        setWindowFlagsForSettings();
        File imageFolder = getStorageUtils().getImageFolder();
        MyFolderChooserDialog myFolderChooserDialog = new MyFolderChooserDialog();
        myFolderChooserDialog.setStartFolder(imageFolder);
        getFragmentManager().beginTransaction().add(myFolderChooserDialog, "FOLDER_FRAGMENT").commitAllowingStateLoss();
    }

    /* access modifiers changed from: private */
    public void longClickedGallery() {
        int i = 0;
        if (this.applicationInterface.getStorageUtils().isUsingSAF()) {
            SaveLocationHistory saveLocationHistory = this.save_location_history_saf;
            if (saveLocationHistory == null || saveLocationHistory.size() <= 1) {
                openFolderChooserDialogSAF(false);
                return;
            }
        } else if (this.save_location_history.size() <= 1) {
            openFolderChooserDialog();
            return;
        }
        final SaveLocationHistory saveLocationHistory2 = this.applicationInterface.getStorageUtils().isUsingSAF() ? this.save_location_history_saf : this.save_location_history;
        showPreview(false);
        Builder builder = new Builder(this);
        builder.setTitle(C0316R.string.choose_save_location);
        CharSequence[] charSequenceArr = new CharSequence[(saveLocationHistory2.size() + 2)];
        final int i2 = 0;
        while (i < saveLocationHistory2.size()) {
            String str = saveLocationHistory2.get((saveLocationHistory2.size() - 1) - i);
            if (this.applicationInterface.getStorageUtils().isUsingSAF()) {
                File fileFromDocumentUriSAF = this.applicationInterface.getStorageUtils().getFileFromDocumentUriSAF(Uri.parse(str), true);
                if (fileFromDocumentUriSAF != null) {
                    str = fileFromDocumentUriSAF.getAbsolutePath();
                }
            }
            int i3 = i2 + 1;
            charSequenceArr[i2] = str;
            i++;
            i2 = i3;
        }
        final int i4 = i2 + 1;
        charSequenceArr[i2] = getResources().getString(C0316R.string.clear_folder_history);
        charSequenceArr[i4] = getResources().getString(C0316R.string.choose_another_folder);
        builder.setItems(charSequenceArr, new OnClickListener() {
            /* JADX WARNING: Removed duplicated region for block: B:21:0x00ef  */
            /* JADX WARNING: Removed duplicated region for block: B:22:0x00f7  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onClick(android.content.DialogInterface r7, int r8) {
                /*
                    r6 = this;
                    int r7 = r5
                    if (r8 != r7) goto L_0x0046
                    android.app.AlertDialog$Builder r7 = new android.app.AlertDialog$Builder
                    net.sourceforge.opencamera.MainActivity r8 = net.sourceforge.opencamera.MainActivity.this
                    r7.<init>(r8)
                    r8 = 17301543(0x1080027, float:2.4979364E-38)
                    android.app.AlertDialog$Builder r7 = r7.setIcon(r8)
                    r8 = 2131492910(0x7f0c002e, float:1.8609285E38)
                    android.app.AlertDialog$Builder r7 = r7.setTitle(r8)
                    r8 = 2131492911(0x7f0c002f, float:1.8609287E38)
                    android.app.AlertDialog$Builder r7 = r7.setMessage(r8)
                    r8 = 17039379(0x1040013, float:2.4244624E-38)
                    net.sourceforge.opencamera.MainActivity$19$3 r0 = new net.sourceforge.opencamera.MainActivity$19$3
                    r0.<init>()
                    android.app.AlertDialog$Builder r7 = r7.setPositiveButton(r8, r0)
                    r8 = 17039369(0x1040009, float:2.4244596E-38)
                    net.sourceforge.opencamera.MainActivity$19$2 r0 = new net.sourceforge.opencamera.MainActivity$19$2
                    r0.<init>()
                    android.app.AlertDialog$Builder r7 = r7.setNegativeButton(r8, r0)
                    net.sourceforge.opencamera.MainActivity$19$1 r8 = new net.sourceforge.opencamera.MainActivity$19$1
                    r8.<init>()
                    android.app.AlertDialog$Builder r7 = r7.setOnCancelListener(r8)
                    r7.show()
                    goto L_0x0110
                L_0x0046:
                    int r7 = r1
                    if (r8 != r7) goto L_0x0069
                    net.sourceforge.opencamera.MainActivity r7 = net.sourceforge.opencamera.MainActivity.this
                    net.sourceforge.opencamera.MyApplicationInterface r7 = r7.applicationInterface
                    net.sourceforge.opencamera.StorageUtils r7 = r7.getStorageUtils()
                    boolean r7 = r7.isUsingSAF()
                    if (r7 == 0) goto L_0x0062
                    net.sourceforge.opencamera.MainActivity r7 = net.sourceforge.opencamera.MainActivity.this
                    r8 = 0
                    r7.openFolderChooserDialogSAF(r8)
                    goto L_0x0110
                L_0x0062:
                    net.sourceforge.opencamera.MainActivity r7 = net.sourceforge.opencamera.MainActivity.this
                    r7.openFolderChooserDialog()
                    goto L_0x0110
                L_0x0069:
                    r7 = 1
                    if (r8 < 0) goto L_0x0106
                    net.sourceforge.opencamera.SaveLocationHistory r0 = r0
                    int r0 = r0.size()
                    if (r8 >= r0) goto L_0x0106
                    net.sourceforge.opencamera.SaveLocationHistory r0 = r0
                    int r1 = r0.size()
                    int r1 = r1 - r7
                    int r1 = r1 - r8
                    java.lang.String r8 = r0.get(r1)
                    net.sourceforge.opencamera.MainActivity r0 = net.sourceforge.opencamera.MainActivity.this
                    net.sourceforge.opencamera.MyApplicationInterface r0 = r0.applicationInterface
                    net.sourceforge.opencamera.StorageUtils r0 = r0.getStorageUtils()
                    boolean r0 = r0.isUsingSAF()
                    if (r0 == 0) goto L_0x00a9
                    net.sourceforge.opencamera.MainActivity r0 = net.sourceforge.opencamera.MainActivity.this
                    net.sourceforge.opencamera.MyApplicationInterface r0 = r0.applicationInterface
                    net.sourceforge.opencamera.StorageUtils r0 = r0.getStorageUtils()
                    android.net.Uri r1 = android.net.Uri.parse(r8)
                    java.io.File r0 = r0.getFileFromDocumentUriSAF(r1, r7)
                    if (r0 == 0) goto L_0x00a9
                    java.lang.String r0 = r0.getAbsolutePath()
                    goto L_0x00aa
                L_0x00a9:
                    r0 = r8
                L_0x00aa:
                    net.sourceforge.opencamera.MainActivity r1 = net.sourceforge.opencamera.MainActivity.this
                    net.sourceforge.opencamera.preview.Preview r1 = r1.preview
                    r2 = 0
                    java.lang.StringBuilder r3 = new java.lang.StringBuilder
                    r3.<init>()
                    net.sourceforge.opencamera.MainActivity r4 = net.sourceforge.opencamera.MainActivity.this
                    android.content.res.Resources r4 = r4.getResources()
                    r5 = 2131492907(0x7f0c002b, float:1.860928E38)
                    java.lang.String r4 = r4.getString(r5)
                    r3.append(r4)
                    java.lang.String r4 = "\n"
                    r3.append(r4)
                    r3.append(r0)
                    java.lang.String r0 = r3.toString()
                    r1.showToast(r2, r0)
                    net.sourceforge.opencamera.MainActivity r0 = net.sourceforge.opencamera.MainActivity.this
                    android.content.SharedPreferences r0 = android.preference.PreferenceManager.getDefaultSharedPreferences(r0)
                    android.content.SharedPreferences$Editor r0 = r0.edit()
                    net.sourceforge.opencamera.MainActivity r1 = net.sourceforge.opencamera.MainActivity.this
                    net.sourceforge.opencamera.MyApplicationInterface r1 = r1.applicationInterface
                    net.sourceforge.opencamera.StorageUtils r1 = r1.getStorageUtils()
                    boolean r1 = r1.isUsingSAF()
                    if (r1 == 0) goto L_0x00f7
                    java.lang.String r1 = net.sourceforge.opencamera.PreferenceKeys.getSaveLocationSAFPreferenceKey()
                    r0.putString(r1, r8)
                    goto L_0x00fe
                L_0x00f7:
                    java.lang.String r1 = net.sourceforge.opencamera.PreferenceKeys.getSaveLocationPreferenceKey()
                    r0.putString(r1, r8)
                L_0x00fe:
                    r0.apply()
                    net.sourceforge.opencamera.SaveLocationHistory r0 = r0
                    r0.updateFolderHistory(r8, r7)
                L_0x0106:
                    net.sourceforge.opencamera.MainActivity r8 = net.sourceforge.opencamera.MainActivity.this
                    r8.setWindowFlagsForCamera()
                    net.sourceforge.opencamera.MainActivity r8 = net.sourceforge.opencamera.MainActivity.this
                    r8.showPreview(r7)
                L_0x0110:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MainActivity.C025419.onClick(android.content.DialogInterface, int):void");
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialogInterface) {
                MainActivity.this.setWindowFlagsForCamera();
                MainActivity.this.showPreview(true);
            }
        });
        setWindowFlagsForSettings();
        showAlert(builder.create());
    }

    public void clearFolderHistory() {
        this.save_location_history.clearFolderHistory(getStorageUtils().getSaveLocation());
    }

    public void clearFolderHistorySAF() {
        this.save_location_history_saf.clearFolderHistory(getStorageUtils().getSaveLocationSAF());
    }

    private static void putBundleExtra(Bundle bundle, String str, List<String> list) {
        if (list != null) {
            String[] strArr = new String[list.size()];
            int i = 0;
            for (String str2 : list) {
                strArr[i] = str2;
                i++;
            }
            bundle.putStringArray(str, strArr);
        }
    }

    public void clickedShare(View view) {
        this.applicationInterface.shareLastImage();
    }

    public void clickedTrash(View view) {
        this.applicationInterface.trashLastImage();
    }

    public void takePicture(boolean z) {
        if (this.applicationInterface.getPhotoMode() == PhotoMode.Panorama && !this.preview.isTakingPhoto()) {
            if (this.applicationInterface.getGyroSensor().isRecording()) {
                this.applicationInterface.finishPanorama();
                return;
            } else if (this.applicationInterface.canTakeNewPhoto()) {
                this.applicationInterface.startPanorama();
            }
        }
        takePicturePressed(z, false);
    }

    /* access modifiers changed from: 0000 */
    public boolean lastContinuousFastBurst() {
        return this.last_continuous_fast_burst;
    }

    /* access modifiers changed from: 0000 */
    public void takePicturePressed(boolean z, boolean z2) {
        closePopup();
        this.last_continuous_fast_burst = z2;
        this.preview.takePicturePressed(z, z2);
    }

    /* access modifiers changed from: 0000 */
    public void lockScreen() {
        findViewById(C0316R.C0318id.locker).setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return MainActivity.this.gestureDetector.onTouchEvent(motionEvent);
            }
        });
        this.screen_is_locked = true;
    }

    /* access modifiers changed from: 0000 */
    public void unlockScreen() {
        findViewById(C0316R.C0318id.locker).setOnTouchListener(null);
        this.screen_is_locked = false;
    }

    public boolean isScreenLocked() {
        return this.screen_is_locked;
    }

    /* access modifiers changed from: protected */
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        Preview preview2 = this.preview;
        if (preview2 != null) {
            preview2.onSaveInstanceState(bundle);
        }
        MyApplicationInterface myApplicationInterface = this.applicationInterface;
        if (myApplicationInterface != null) {
            myApplicationInterface.onSaveInstanceState(bundle);
        }
    }

    public boolean supportsExposureButton() {
        boolean z = false;
        if (this.preview.getCameraController() == null || this.preview.isVideoHighSpeed()) {
            return false;
        }
        String str = "auto";
        boolean z2 = !PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.ISOPreferenceKey, str).equals(str);
        if (this.preview.supportsExposures() || (z2 && this.preview.supportsISORange())) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: 0000 */
    public void cameraSetup() {
        if (supportsForceVideo4K() && this.preview.usingCamera2API()) {
            disableForceVideo4K();
        }
        if (supportsForceVideo4K() && this.preview.getVideoQualityHander().getSupportedVideoSizes() != null) {
            for (Size size : this.preview.getVideoQualityHander().getSupportedVideoSizes()) {
                if (size.width >= 3840 && size.height >= 2160) {
                    disableForceVideo4K();
                }
            }
        }
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ZoomControls zoomControls = (ZoomControls) findViewById(C0316R.C0318id.zoom);
        SeekBar seekBar = (SeekBar) findViewById(C0316R.C0318id.zoom_seekbar);
        int i = 8;
        if (this.preview.supportsZoom()) {
            if (defaultSharedPreferences.getBoolean(PreferenceKeys.ShowZoomControlsPreferenceKey, false)) {
                zoomControls.setIsZoomInEnabled(true);
                zoomControls.setIsZoomOutEnabled(true);
                zoomControls.setZoomSpeed(20);
                zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        MainActivity.this.zoomIn();
                    }
                });
                zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        MainActivity.this.zoomOut();
                    }
                });
                if (!this.mainUI.inImmersiveMode()) {
                    zoomControls.setVisibility(0);
                }
            } else {
                zoomControls.setVisibility(8);
            }
            seekBar.setOnSeekBarChangeListener(null);
            seekBar.setMax(this.preview.getMaxZoom());
            seekBar.setProgress(this.preview.getMaxZoom() - this.preview.getCameraController().getZoom());
            seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                    MainActivity.this.preview.zoomTo(MainActivity.this.preview.getMaxZoom() - i);
                }
            });
            if (!defaultSharedPreferences.getBoolean(PreferenceKeys.ShowZoomSliderControlsPreferenceKey, true)) {
                seekBar.setVisibility(4);
            } else if (!this.mainUI.inImmersiveMode()) {
                seekBar.setVisibility(0);
            }
        } else {
            zoomControls.setVisibility(8);
            seekBar.setVisibility(4);
        }
        View findViewById = findViewById(C0316R.C0318id.take_photo);
        if (!defaultSharedPreferences.getBoolean(PreferenceKeys.ShowTakePhotoPreferenceKey, true)) {
            findViewById.setVisibility(4);
        } else if (!this.mainUI.inImmersiveMode()) {
            findViewById.setVisibility(0);
        }
        setManualFocusSeekbar(false);
        setManualFocusSeekbar(true);
        if (this.preview.supportsISORange()) {
            SeekBar seekBar2 = (SeekBar) findViewById(C0316R.C0318id.iso_seekbar);
            seekBar2.setOnSeekBarChangeListener(null);
            this.manualSeekbars.setProgressSeekbarISO(seekBar2, (long) this.preview.getMinimumISO(), (long) this.preview.getMaximumISO(), (long) this.preview.getCameraController().getISO());
            seekBar2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                    MainActivity.this.preview.setISO(MainActivity.this.manualSeekbars.getISO(i));
                    MainActivity.this.mainUI.updateSelectedISOButton();
                }
            });
            if (this.preview.supportsExposureTime()) {
                SeekBar seekBar3 = (SeekBar) findViewById(C0316R.C0318id.exposure_time_seekbar);
                seekBar3.setOnSeekBarChangeListener(null);
                this.manualSeekbars.setProgressSeekbarShutterSpeed(seekBar3, this.preview.getMinimumExposureTime(), this.preview.getMaximumExposureTime(), this.preview.getCameraController().getExposureTime());
                seekBar3.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                        MainActivity.this.preview.setExposureTime(MainActivity.this.manualSeekbars.getExposureTime(i));
                    }
                });
            }
        }
        setManualWBSeekbar();
        if (this.preview.supportsExposures()) {
            final int minimumExposure = this.preview.getMinimumExposure();
            SeekBar seekBar4 = (SeekBar) findViewById(C0316R.C0318id.exposure_seekbar);
            seekBar4.setOnSeekBarChangeListener(null);
            seekBar4.setMax(this.preview.getMaximumExposure() - minimumExposure);
            seekBar4.setProgress(this.preview.getCurrentExposure() - minimumExposure);
            seekBar4.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                    MainActivity.this.preview.setExposure(minimumExposure + i);
                }
            });
            ZoomControls zoomControls2 = (ZoomControls) findViewById(C0316R.C0318id.exposure_seekbar_zoom);
            zoomControls2.setOnZoomInClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    MainActivity.this.changeExposure(1);
                }
            });
            zoomControls2.setOnZoomOutClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    MainActivity.this.changeExposure(-1);
                }
            });
        }
        View findViewById2 = findViewById(C0316R.C0318id.exposure);
        if (supportsExposureButton() && !this.mainUI.inImmersiveMode()) {
            i = 0;
        }
        findViewById2.setVisibility(i);
        this.mainUI.updateOnScreenIcons();
        this.mainUI.setPopupIcon();
        this.mainUI.setTakePhotoIcon();
        this.mainUI.setSwitchCameraContentDescription();
        if (!this.block_startup_toast) {
            showPhotoVideoToast(false);
        }
        this.block_startup_toast = false;
    }

    private void setManualFocusSeekbar(final boolean z) {
        final SeekBar seekBar = (SeekBar) findViewById(z ? C0316R.C0318id.focus_bracketing_target_seekbar : C0316R.C0318id.focus_seekbar);
        seekBar.setOnSeekBarChangeListener(null);
        double minimumFocusDistance = (double) this.preview.getMinimumFocusDistance();
        CameraController cameraController = this.preview.getCameraController();
        ManualSeekbars.setProgressSeekbarScaled(seekBar, 0.0d, minimumFocusDistance, (double) (z ? cameraController.getFocusBracketingTargetDistance() : cameraController.getFocusDistance()));
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            private boolean has_saved_zoom;
            private int saved_zoom_factor;

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                double d = (double) i;
                double max = (double) seekBar.getMax();
                Double.isNaN(d);
                Double.isNaN(max);
                double seekbarScaling = ManualSeekbars.seekbarScaling(d / max);
                double minimumFocusDistance = (double) MainActivity.this.preview.getMinimumFocusDistance();
                Double.isNaN(minimumFocusDistance);
                MainActivity.this.preview.setFocusDistance((float) (seekbarScaling * minimumFocusDistance), z);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                this.has_saved_zoom = false;
                if (MainActivity.this.preview.supportsZoom()) {
                    int focusAssistPref = MainActivity.this.applicationInterface.getFocusAssistPref();
                    if (focusAssistPref > 0 && MainActivity.this.preview.getCameraController() != null) {
                        this.has_saved_zoom = true;
                        this.saved_zoom_factor = MainActivity.this.preview.getCameraController().getZoom();
                        MainActivity.this.preview.getCameraController().setZoom(MainActivity.this.preview.getScaledZoomFactor((float) focusAssistPref));
                    }
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (this.has_saved_zoom && MainActivity.this.preview.getCameraController() != null) {
                    MainActivity.this.preview.getCameraController().setZoom(this.saved_zoom_factor);
                }
                MainActivity.this.preview.stoppedSettingFocusDistance(z);
            }
        });
        setManualFocusSeekBarVisibility(z);
    }

    /* access modifiers changed from: 0000 */
    public void setManualFocusSeekBarVisibility(boolean z) {
        SeekBar seekBar = (SeekBar) findViewById(z ? C0316R.C0318id.focus_bracketing_target_seekbar : C0316R.C0318id.focus_seekbar);
        int i = 0;
        boolean z2 = this.preview.getCurrentFocusValue() != null && getPreview().getCurrentFocusValue().equals("focus_mode_manual2");
        if (z) {
            z2 = z2 && this.applicationInterface.getPhotoMode() == PhotoMode.FocusBracketing && !this.preview.isVideo();
        }
        if (!z2) {
            i = 8;
        }
        seekBar.setVisibility(i);
    }

    public void setManualWBSeekbar() {
        if (this.preview.getSupportedWhiteBalances() != null && this.preview.supportsWhiteBalanceTemperature()) {
            SeekBar seekBar = (SeekBar) findViewById(C0316R.C0318id.white_balance_seekbar);
            seekBar.setOnSeekBarChangeListener(null);
            int minimumWhiteBalanceTemperature = this.preview.getMinimumWhiteBalanceTemperature();
            int maximumWhiteBalanceTemperature = this.preview.getMaximumWhiteBalanceTemperature();
            this.manualSeekbars.setProgressSeekbarWhiteBalance(seekBar, (long) minimumWhiteBalanceTemperature, (long) maximumWhiteBalanceTemperature, (long) this.preview.getCameraController().getWhiteBalanceTemperature());
            seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                    MainActivity.this.preview.setWhiteBalanceTemperature(MainActivity.this.manualSeekbars.getWhiteBalanceTemperature(i));
                }
            });
        }
    }

    public boolean supportsAutoStabilise() {
        if (!this.applicationInterface.isRawOnly() && this.applicationInterface.getPhotoMode() != PhotoMode.Panorama) {
            return this.supports_auto_stabilise;
        }
        return false;
    }

    public boolean supportsDRO() {
        boolean z = false;
        if (this.applicationInterface.isRawOnly(PhotoMode.DRO)) {
            return false;
        }
        if (VERSION.SDK_INT >= 21) {
            z = true;
        }
        return z;
    }

    public boolean supportsHDR() {
        return VERSION.SDK_INT >= 21 && this.large_heap_memory >= 128 && this.preview.supportsExpoBracketing();
    }

    public boolean supportsExpoBracketing() {
        if (this.applicationInterface.isImageCaptureIntent()) {
            return false;
        }
        return this.preview.supportsExpoBracketing();
    }

    public boolean supportsFocusBracketing() {
        if (this.applicationInterface.isImageCaptureIntent()) {
            return false;
        }
        return this.preview.supportsFocusBracketing();
    }

    public boolean supportsPanorama() {
        boolean z = false;
        if (this.applicationInterface.isImageCaptureIntent()) {
            return false;
        }
        if (VERSION.SDK_INT >= 21 && this.large_heap_memory >= 256 && this.applicationInterface.getGyroSensor().hasSensors()) {
            z = true;
        }
        return z;
    }

    public boolean supportsFastBurst() {
        boolean z = false;
        if (this.applicationInterface.isImageCaptureIntent()) {
            return false;
        }
        if (this.preview.usingCamera2API() && this.large_heap_memory >= 512 && this.preview.supportsBurst()) {
            z = true;
        }
        return z;
    }

    public boolean supportsNoiseReduction() {
        return VERSION.SDK_INT >= 24 && this.preview.usingCamera2API() && this.large_heap_memory >= 512 && this.preview.supportsBurst() && this.preview.supportsExposureTime();
    }

    public boolean supportsBurstRaw() {
        return this.large_heap_memory >= 512;
    }

    public boolean supportsPreviewBitmaps() {
        return VERSION.SDK_INT >= 21 && (this.preview.getView() instanceof TextureView) && this.large_heap_memory >= 128;
    }

    private int maxExpoBracketingNImages() {
        return this.preview.maxExpoBracketingNImages();
    }

    public boolean supportsForceVideo4K() {
        return this.supports_force_video_4k;
    }

    public boolean supportsCamera2() {
        return this.supports_camera2;
    }

    private void disableForceVideo4K() {
        this.supports_force_video_4k = false;
    }

    public Preview getPreview() {
        return this.preview;
    }

    public boolean isCameraInBackground() {
        return this.camera_in_background;
    }

    public BluetoothRemoteControl getBluetoothRemoteControl() {
        return this.bluetoothRemoteControl;
    }

    public PermissionHandler getPermissionHandler() {
        return this.permissionHandler;
    }

    public SettingsManager getSettingsManager() {
        return this.settingsManager;
    }

    public MainUI getMainUI() {
        return this.mainUI;
    }

    public ManualSeekbars getManualSeekbars() {
        return this.manualSeekbars;
    }

    public MyApplicationInterface getApplicationInterface() {
        return this.applicationInterface;
    }

    public TextFormatter getTextFormatter() {
        return this.textFormatter;
    }

    /* access modifiers changed from: 0000 */
    public SoundPoolManager getSoundPoolManager() {
        return this.soundPoolManager;
    }

    public LocationSupplier getLocationSupplier() {
        return this.applicationInterface.getLocationSupplier();
    }

    public StorageUtils getStorageUtils() {
        return this.applicationInterface.getStorageUtils();
    }

    public File getImageFolder() {
        return this.applicationInterface.getStorageUtils().getImageFolder();
    }

    public ToastBoxer getChangedAutoStabiliseToastBoxer() {
        return this.changed_auto_stabilise_toast;
    }

    /* JADX WARNING: Removed duplicated region for block: B:101:0x0495  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x0632  */
    /* JADX WARNING: Removed duplicated region for block: B:155:0x067b  */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x06b0  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x06fb A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x046d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void showPhotoVideoToast(boolean r27) {
        /*
            r26 = this;
            r1 = r26
            java.lang.String r2 = "none"
            net.sourceforge.opencamera.preview.Preview r0 = r1.preview
            net.sourceforge.opencamera.cameracontroller.CameraController r0 = r0.getCameraController()
            if (r0 == 0) goto L_0x0704
            boolean r3 = r1.camera_in_background
            if (r3 == 0) goto L_0x0012
            goto L_0x0704
        L_0x0012:
            android.content.SharedPreferences r3 = android.preference.PreferenceManager.getDefaultSharedPreferences(r26)
            net.sourceforge.opencamera.preview.Preview r4 = r1.preview
            boolean r4 = r4.isVideoHighSpeed()
            net.sourceforge.opencamera.MyApplicationInterface r5 = r1.applicationInterface
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r5 = r5.getPhotoMode()
            net.sourceforge.opencamera.preview.Preview r6 = r1.preview
            boolean r6 = r6.isVideo()
            java.lang.String r7 = ")"
            java.lang.String r8 = " ("
            java.lang.String r9 = "x"
            java.lang.String r10 = " "
            java.lang.String r12 = "0"
            java.lang.String r13 = ""
            java.lang.String r14 = ": "
            java.lang.String r15 = "\n"
            r16 = 0
            if (r6 == 0) goto L_0x030b
            net.sourceforge.opencamera.preview.Preview r6 = r1.preview
            net.sourceforge.opencamera.preview.VideoProfile r6 = r6.getVideoProfile()
            java.lang.String r11 = r6.fileExtension
            r17 = r2
            java.lang.String r2 = r6.fileExtension
            r18 = r0
            java.lang.String r0 = "mp4"
            boolean r0 = r2.equals(r0)
            int r2 = r6.videoBitRate
            r19 = r0
            r0 = 10000000(0x989680, float:1.4012985E-38)
            if (r2 < r0) goto L_0x0072
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            int r2 = r6.videoBitRate
            r20 = 1000000(0xf4240, float:1.401298E-39)
            int r2 = r2 / r20
            r0.append(r2)
            java.lang.String r2 = "Mbps"
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            goto L_0x00a1
        L_0x0072:
            int r0 = r6.videoBitRate
            r2 = 10000(0x2710, float:1.4013E-41)
            if (r0 < r2) goto L_0x008e
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            int r2 = r6.videoBitRate
            int r2 = r2 / 1000
            r0.append(r2)
            java.lang.String r2 = "Kbps"
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            goto L_0x00a1
        L_0x008e:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            int r2 = r6.videoBitRate
            r0.append(r2)
            java.lang.String r2 = "bps"
            r0.append(r2)
            java.lang.String r0 = r0.toString()
        L_0x00a1:
            net.sourceforge.opencamera.MyApplicationInterface r2 = r1.applicationInterface
            java.lang.String r2 = r2.getVideoBitratePref()
            r20 = r10
            java.lang.String r10 = "default"
            boolean r2 = r2.equals(r10)
            r21 = r3
            if (r2 != 0) goto L_0x00b5
            r19 = 0
        L_0x00b5:
            double r2 = r6.videoCaptureRate
            r22 = 4621537642612260864(0x4023000000000000, double:9.5)
            int r24 = (r2 > r22 ? 1 : (r2 == r22 ? 0 : -1))
            r22 = r5
            if (r24 >= 0) goto L_0x00ce
            java.text.DecimalFormat r5 = new java.text.DecimalFormat
            r23 = r12
            java.lang.String r12 = "#0.###"
            r5.<init>(r12)
            java.lang.String r2 = r5.format(r2)
            r3 = r13
            goto L_0x00e7
        L_0x00ce:
            r23 = r12
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r13)
            r3 = r13
            double r12 = r6.videoCaptureRate
            r24 = 4602678819172646912(0x3fe0000000000000, double:0.5)
            double r12 = r12 + r24
            int r5 = (int) r12
            r2.append(r5)
            java.lang.String r2 = r2.toString()
        L_0x00e7:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            android.content.res.Resources r12 = r26.getResources()
            r13 = 2131493609(0x7f0c02e9, float:1.8610703E38)
            java.lang.String r12 = r12.getString(r13)
            r5.append(r12)
            r5.append(r14)
            int r12 = r6.videoFrameWidth
            r5.append(r12)
            r5.append(r9)
            int r6 = r6.videoFrameHeight
            r5.append(r6)
            r5.append(r15)
            r5.append(r2)
            android.content.res.Resources r2 = r26.getResources()
            r6 = 2131493000(0x7f0c0088, float:1.8609468E38)
            java.lang.String r2 = r2.getString(r6)
            r5.append(r2)
            if (r4 == 0) goto L_0x0142
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = " ["
            r2.append(r3)
            android.content.res.Resources r3 = r26.getResources()
            r6 = 2131493007(0x7f0c008f, float:1.8609482E38)
            java.lang.String r3 = r3.getString(r6)
            r2.append(r3)
            java.lang.String r3 = "]"
            r2.append(r3)
            java.lang.String r13 = r2.toString()
            r3 = r13
        L_0x0142:
            r5.append(r3)
            java.lang.String r2 = ", "
            r5.append(r2)
            r5.append(r0)
            r5.append(r8)
            r5.append(r11)
            r5.append(r7)
            java.lang.String r0 = r5.toString()
            net.sourceforge.opencamera.MyApplicationInterface r2 = r1.applicationInterface
            java.lang.String r2 = r2.getVideoFPSPref()
            boolean r2 = r2.equals(r10)
            if (r2 == 0) goto L_0x0168
            if (r4 == 0) goto L_0x016a
        L_0x0168:
            r19 = 0
        L_0x016a:
            net.sourceforge.opencamera.MyApplicationInterface r2 = r1.applicationInterface
            float r2 = r2.getVideoCaptureRateFactor()
            r3 = 1065353216(0x3f800000, float:1.0)
            float r3 = r2 - r3
            float r3 = java.lang.Math.abs(r3)
            double r5 = (double) r3
            r7 = 4532020583610935537(0x3ee4f8b588e368f1, double:1.0E-5)
            int r3 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r3 <= 0) goto L_0x01aa
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r0)
            r3.append(r15)
            android.content.res.Resources r0 = r26.getResources()
            r5 = 2131493487(0x7f0c026f, float:1.8610456E38)
            java.lang.String r0 = r0.getString(r5)
            r3.append(r0)
            r3.append(r14)
            r3.append(r2)
            r3.append(r9)
            java.lang.String r0 = r3.toString()
            r19 = 0
        L_0x01aa:
            net.sourceforge.opencamera.MyApplicationInterface r2 = r1.applicationInterface
            boolean r2 = r2.useVideoLogProfile()
            if (r2 == 0) goto L_0x01d9
            net.sourceforge.opencamera.preview.Preview r2 = r1.preview
            boolean r2 = r2.supportsTonemapCurve()
            if (r2 == 0) goto L_0x01d9
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r0)
            r2.append(r15)
            android.content.res.Resources r0 = r26.getResources()
            r3 = 2131493612(0x7f0c02ec, float:1.861071E38)
            java.lang.String r0 = r0.getString(r3)
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            r19 = 0
        L_0x01d9:
            net.sourceforge.opencamera.MyApplicationInterface r2 = r1.applicationInterface
            boolean r2 = r2.getRecordAudioPref()
            if (r2 != 0) goto L_0x0200
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r0)
            r2.append(r15)
            android.content.res.Resources r0 = r26.getResources()
            r3 = 2131492882(0x7f0c0012, float:1.8609228E38)
            java.lang.String r0 = r0.getString(r3)
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            r19 = 0
        L_0x0200:
            java.lang.String r2 = net.sourceforge.opencamera.PreferenceKeys.getVideoMaxDurationPreferenceKey()
            r5 = r21
            r6 = r23
            java.lang.String r2 = r5.getString(r2, r6)
            int r3 = r2.length()
            if (r3 <= 0) goto L_0x0260
            boolean r3 = r2.equals(r6)
            if (r3 != 0) goto L_0x0260
            android.content.res.Resources r3 = r26.getResources()
            r7 = 2130772068(0x7f010064, float:1.7147244E38)
            java.lang.String[] r3 = r3.getStringArray(r7)
            android.content.res.Resources r7 = r26.getResources()
            r8 = 2130772069(0x7f010065, float:1.7147246E38)
            java.lang.String[] r7 = r7.getStringArray(r8)
            java.util.List r7 = java.util.Arrays.asList(r7)
            int r2 = r7.indexOf(r2)
            r7 = -1
            if (r2 == r7) goto L_0x0260
            r2 = r3[r2]
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r0)
            r3.append(r15)
            android.content.res.Resources r0 = r26.getResources()
            r7 = 2131493017(0x7f0c0099, float:1.8609502E38)
            java.lang.String r0 = r0.getString(r7)
            r3.append(r0)
            r3.append(r14)
            r3.append(r2)
            java.lang.String r0 = r3.toString()
            r19 = 0
        L_0x0260:
            net.sourceforge.opencamera.MyApplicationInterface r2 = r1.applicationInterface
            long r2 = r2.getVideoMaxFileSizeUserPref()
            r7 = 0
            int r9 = (r2 > r7 ? 1 : (r2 == r7 ? 0 : -1))
            if (r9 == 0) goto L_0x02d5
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r0)
            r7.append(r15)
            android.content.res.Resources r0 = r26.getResources()
            r8 = 2131493018(0x7f0c009a, float:1.8609504E38)
            java.lang.String r0 = r0.getString(r8)
            r7.append(r0)
            r7.append(r14)
            java.lang.String r0 = r7.toString()
            r7 = 1073741824(0x40000000, double:5.304989477E-315)
            int r9 = (r2 > r7 ? 1 : (r2 == r7 ? 0 : -1))
            if (r9 < 0) goto L_0x02b2
            long r2 = r2 / r7
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r0)
            r7.append(r2)
            android.content.res.Resources r0 = r26.getResources()
            r2 = 2131493004(0x7f0c008c, float:1.8609476E38)
            java.lang.String r0 = r0.getString(r2)
            r7.append(r0)
            java.lang.String r0 = r7.toString()
            goto L_0x02d3
        L_0x02b2:
            r7 = 1048576(0x100000, double:5.180654E-318)
            long r2 = r2 / r7
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r0)
            r7.append(r2)
            android.content.res.Resources r0 = r26.getResources()
            r2 = 2131493019(0x7f0c009b, float:1.8609506E38)
            java.lang.String r0 = r0.getString(r2)
            r7.append(r0)
            java.lang.String r0 = r7.toString()
        L_0x02d3:
            r19 = 0
        L_0x02d5:
            net.sourceforge.opencamera.MyApplicationInterface r2 = r1.applicationInterface
            boolean r2 = r2.getVideoFlashPref()
            if (r2 == 0) goto L_0x0304
            net.sourceforge.opencamera.preview.Preview r2 = r1.preview
            boolean r2 = r2.supportsFlash()
            if (r2 == 0) goto L_0x0304
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r0)
            r2.append(r15)
            android.content.res.Resources r0 = r26.getResources()
            r3 = 2131493489(0x7f0c0271, float:1.861046E38)
            java.lang.String r0 = r0.getString(r3)
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            r19 = 0
        L_0x0304:
            r3 = r0
            r0 = r20
            r2 = r22
            goto L_0x0498
        L_0x030b:
            r18 = r0
            r17 = r2
            r22 = r5
            r20 = r10
            r6 = r12
            r5 = r3
            r3 = r13
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r0 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            r2 = r22
            if (r2 != r0) goto L_0x0320
            r13 = r3
            r0 = r20
            goto L_0x034f
        L_0x0320:
            android.content.res.Resources r0 = r26.getResources()
            r10 = 2131493047(0x7f0c00b7, float:1.8609563E38)
            java.lang.String r0 = r0.getString(r10)
            net.sourceforge.opencamera.preview.Preview r10 = r1.preview
            net.sourceforge.opencamera.cameracontroller.CameraController$Size r10 = r10.getCurrentPictureSize()
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            r11.append(r0)
            r0 = r20
            r11.append(r0)
            int r12 = r10.width
            r11.append(r12)
            r11.append(r9)
            int r9 = r10.height
            r11.append(r9)
            java.lang.String r13 = r11.toString()
        L_0x034f:
            r9 = 0
            int[] r10 = net.sourceforge.opencamera.MainActivity.C027232.f15x2f3f7d5c
            int r11 = r2.ordinal()
            r10 = r10[r11]
            switch(r10) {
                case 1: goto L_0x03dc;
                case 2: goto L_0x03d0;
                case 3: goto L_0x03c4;
                case 4: goto L_0x039d;
                case 5: goto L_0x0376;
                case 6: goto L_0x036a;
                case 7: goto L_0x035d;
                default: goto L_0x035b;
            }
        L_0x035b:
            goto L_0x03e7
        L_0x035d:
            android.content.res.Resources r7 = r26.getResources()
            r8 = 2131493061(0x7f0c00c5, float:1.8609592E38)
            java.lang.String r9 = r7.getString(r8)
            goto L_0x03e7
        L_0x036a:
            android.content.res.Resources r7 = r26.getResources()
            r8 = 2131493059(0x7f0c00c3, float:1.8609587E38)
            java.lang.String r9 = r7.getString(r8)
            goto L_0x03e7
        L_0x0376:
            android.content.res.Resources r9 = r26.getResources()
            r10 = 2131493054(0x7f0c00be, float:1.8609577E38)
            java.lang.String r9 = r9.getString(r10)
            net.sourceforge.opencamera.MyApplicationInterface r10 = r1.applicationInterface
            int r10 = r10.getBurstNImages()
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            r11.append(r9)
            r11.append(r8)
            r11.append(r10)
            r11.append(r7)
            java.lang.String r9 = r11.toString()
            goto L_0x03e7
        L_0x039d:
            android.content.res.Resources r9 = r26.getResources()
            r10 = 2131493056(0x7f0c00c0, float:1.8609581E38)
            java.lang.String r9 = r9.getString(r10)
            net.sourceforge.opencamera.MyApplicationInterface r10 = r1.applicationInterface
            int r10 = r10.getFocusBracketingNImagesPref()
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            r11.append(r9)
            r11.append(r8)
            r11.append(r10)
            r11.append(r7)
            java.lang.String r9 = r11.toString()
            goto L_0x03e7
        L_0x03c4:
            android.content.res.Resources r7 = r26.getResources()
            r8 = 2131493052(0x7f0c00bc, float:1.8609573E38)
            java.lang.String r9 = r7.getString(r8)
            goto L_0x03e7
        L_0x03d0:
            android.content.res.Resources r7 = r26.getResources()
            r8 = 2131493057(0x7f0c00c1, float:1.8609583E38)
            java.lang.String r9 = r7.getString(r8)
            goto L_0x03e7
        L_0x03dc:
            android.content.res.Resources r7 = r26.getResources()
            r8 = 2131493050(0x7f0c00ba, float:1.860957E38)
            java.lang.String r9 = r7.getString(r8)
        L_0x03e7:
            r7 = 1
            if (r9 == 0) goto L_0x0418
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r13)
            int r10 = r13.length()
            if (r10 != 0) goto L_0x03fa
            r10 = r3
            goto L_0x03fb
        L_0x03fa:
            r10 = r15
        L_0x03fb:
            r8.append(r10)
            android.content.res.Resources r10 = r26.getResources()
            r11 = 2131493049(0x7f0c00b9, float:1.8609567E38)
            java.lang.String r10 = r10.getString(r11)
            r8.append(r10)
            r8.append(r14)
            r8.append(r9)
            java.lang.String r13 = r8.toString()
            r8 = 0
            goto L_0x0419
        L_0x0418:
            r8 = 1
        L_0x0419:
            net.sourceforge.opencamera.preview.Preview r9 = r1.preview
            boolean r9 = r9.supportsFocus()
            if (r9 == 0) goto L_0x0464
            net.sourceforge.opencamera.preview.Preview r9 = r1.preview
            java.util.List r9 = r9.getSupportedFocusValues()
            int r9 = r9.size()
            if (r9 <= r7) goto L_0x0464
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.FocusBracketing
            if (r2 == r7) goto L_0x0464
            net.sourceforge.opencamera.preview.Preview r7 = r1.preview
            java.lang.String r7 = r7.getCurrentFocusValue()
            if (r7 == 0) goto L_0x0464
            java.lang.String r9 = "focus_mode_auto"
            boolean r9 = r7.equals(r9)
            if (r9 != 0) goto L_0x0464
            java.lang.String r9 = "focus_mode_continuous_picture"
            boolean r9 = r7.equals(r9)
            if (r9 != 0) goto L_0x0464
            net.sourceforge.opencamera.preview.Preview r9 = r1.preview
            java.lang.String r7 = r9.findFocusEntryForValue(r7)
            if (r7 == 0) goto L_0x0464
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            r9.append(r13)
            r9.append(r15)
            r9.append(r7)
            java.lang.String r7 = r9.toString()
            goto L_0x0465
        L_0x0464:
            r7 = r13
        L_0x0465:
            net.sourceforge.opencamera.MyApplicationInterface r9 = r1.applicationInterface
            boolean r9 = r9.getAutoStabilisePref()
            if (r9 == 0) goto L_0x0495
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r7)
            int r7 = r7.length()
            if (r7 != 0) goto L_0x047c
            goto L_0x047d
        L_0x047c:
            r3 = r15
        L_0x047d:
            r8.append(r3)
            android.content.res.Resources r3 = r26.getResources()
            r7 = 2131493081(0x7f0c00d9, float:1.8609632E38)
            java.lang.String r3 = r3.getString(r7)
            r8.append(r3)
            java.lang.String r3 = r8.toString()
            r19 = 0
            goto L_0x0498
        L_0x0495:
            r3 = r7
            r19 = r8
        L_0x0498:
            net.sourceforge.opencamera.MyApplicationInterface r7 = r1.applicationInterface
            boolean r7 = r7.getFaceDetectionPref()
            if (r7 == 0) goto L_0x04bf
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r3)
            r7.append(r15)
            android.content.res.Resources r3 = r26.getResources()
            r8 = 2131493173(0x7f0c0135, float:1.8609819E38)
            java.lang.String r3 = r3.getString(r8)
            r7.append(r3)
            java.lang.String r3 = r7.toString()
            r19 = 0
        L_0x04bf:
            java.lang.String r7 = "auto"
            if (r4 != 0) goto L_0x052b
            net.sourceforge.opencamera.MyApplicationInterface r4 = r1.applicationInterface
            java.lang.String r4 = r4.getISOPref()
            boolean r8 = r4.equals(r7)
            if (r8 != 0) goto L_0x050b
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r3)
            java.lang.String r3 = "\nISO: "
            r8.append(r3)
            r8.append(r4)
            java.lang.String r3 = r8.toString()
            net.sourceforge.opencamera.preview.Preview r4 = r1.preview
            boolean r4 = r4.supportsExposureTime()
            if (r4 == 0) goto L_0x0509
            net.sourceforge.opencamera.MyApplicationInterface r4 = r1.applicationInterface
            long r8 = r4.getExposureTimePref()
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r3)
            r4.append(r0)
            net.sourceforge.opencamera.preview.Preview r3 = r1.preview
            java.lang.String r3 = r3.getExposureTimeString(r8)
            r4.append(r3)
            java.lang.String r3 = r4.toString()
        L_0x0509:
            r19 = 0
        L_0x050b:
            int r4 = r18.getExposureCompensation()
            if (r4 == 0) goto L_0x052b
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r3)
            r8.append(r15)
            net.sourceforge.opencamera.preview.Preview r3 = r1.preview
            java.lang.String r3 = r3.getExposureCompensationString(r4)
            r8.append(r3)
            java.lang.String r3 = r8.toString()
            r19 = 0
        L_0x052b:
            java.lang.String r4 = r18.getSceneMode()     // Catch:{ RuntimeException -> 0x05fb }
            java.lang.String r8 = r18.getWhiteBalance()     // Catch:{ RuntimeException -> 0x05fb }
            java.lang.String r9 = r18.getColorEffect()     // Catch:{ RuntimeException -> 0x05fb }
            if (r4 == 0) goto L_0x056a
            boolean r10 = r4.equals(r7)     // Catch:{ RuntimeException -> 0x05fb }
            if (r10 != 0) goto L_0x056a
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x05fb }
            r10.<init>()     // Catch:{ RuntimeException -> 0x05fb }
            r10.append(r3)     // Catch:{ RuntimeException -> 0x05fb }
            r10.append(r15)     // Catch:{ RuntimeException -> 0x05fb }
            android.content.res.Resources r11 = r26.getResources()     // Catch:{ RuntimeException -> 0x05fb }
            r12 = 2131493561(0x7f0c02b9, float:1.8610606E38)
            java.lang.String r11 = r11.getString(r12)     // Catch:{ RuntimeException -> 0x05fb }
            r10.append(r11)     // Catch:{ RuntimeException -> 0x05fb }
            r10.append(r14)     // Catch:{ RuntimeException -> 0x05fb }
            net.sourceforge.opencamera.ui.MainUI r11 = r1.mainUI     // Catch:{ RuntimeException -> 0x05fb }
            java.lang.String r4 = r11.getEntryForSceneMode(r4)     // Catch:{ RuntimeException -> 0x05fb }
            r10.append(r4)     // Catch:{ RuntimeException -> 0x05fb }
            java.lang.String r3 = r10.toString()     // Catch:{ RuntimeException -> 0x05fb }
            r19 = 0
        L_0x056a:
            if (r8 == 0) goto L_0x05c3
            boolean r4 = r8.equals(r7)     // Catch:{ RuntimeException -> 0x05fb }
            if (r4 != 0) goto L_0x05c3
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x05fb }
            r4.<init>()     // Catch:{ RuntimeException -> 0x05fb }
            r4.append(r3)     // Catch:{ RuntimeException -> 0x05fb }
            r4.append(r15)     // Catch:{ RuntimeException -> 0x05fb }
            android.content.res.Resources r7 = r26.getResources()     // Catch:{ RuntimeException -> 0x05fb }
            r10 = 2131493623(0x7f0c02f7, float:1.8610731E38)
            java.lang.String r7 = r7.getString(r10)     // Catch:{ RuntimeException -> 0x05fb }
            r4.append(r7)     // Catch:{ RuntimeException -> 0x05fb }
            r4.append(r14)     // Catch:{ RuntimeException -> 0x05fb }
            net.sourceforge.opencamera.ui.MainUI r7 = r1.mainUI     // Catch:{ RuntimeException -> 0x05fb }
            java.lang.String r7 = r7.getEntryForWhiteBalance(r8)     // Catch:{ RuntimeException -> 0x05fb }
            r4.append(r7)     // Catch:{ RuntimeException -> 0x05fb }
            java.lang.String r3 = r4.toString()     // Catch:{ RuntimeException -> 0x05fb }
            java.lang.String r4 = "manual"
            boolean r4 = r8.equals(r4)     // Catch:{ RuntimeException -> 0x05fb }
            if (r4 == 0) goto L_0x05c1
            net.sourceforge.opencamera.preview.Preview r4 = r1.preview     // Catch:{ RuntimeException -> 0x05fb }
            boolean r4 = r4.supportsWhiteBalanceTemperature()     // Catch:{ RuntimeException -> 0x05fb }
            if (r4 == 0) goto L_0x05c1
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x05fb }
            r4.<init>()     // Catch:{ RuntimeException -> 0x05fb }
            r4.append(r3)     // Catch:{ RuntimeException -> 0x05fb }
            r4.append(r0)     // Catch:{ RuntimeException -> 0x05fb }
            int r0 = r18.getWhiteBalanceTemperature()     // Catch:{ RuntimeException -> 0x05fb }
            r4.append(r0)     // Catch:{ RuntimeException -> 0x05fb }
            java.lang.String r3 = r4.toString()     // Catch:{ RuntimeException -> 0x05fb }
        L_0x05c1:
            r19 = 0
        L_0x05c3:
            r4 = r17
            if (r9 == 0) goto L_0x0601
            boolean r0 = r9.equals(r4)     // Catch:{ RuntimeException -> 0x05f9 }
            if (r0 != 0) goto L_0x0601
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x05f9 }
            r0.<init>()     // Catch:{ RuntimeException -> 0x05f9 }
            r0.append(r3)     // Catch:{ RuntimeException -> 0x05f9 }
            r0.append(r15)     // Catch:{ RuntimeException -> 0x05f9 }
            android.content.res.Resources r7 = r26.getResources()     // Catch:{ RuntimeException -> 0x05f9 }
            r8 = 2131492912(0x7f0c0030, float:1.860929E38)
            java.lang.String r7 = r7.getString(r8)     // Catch:{ RuntimeException -> 0x05f9 }
            r0.append(r7)     // Catch:{ RuntimeException -> 0x05f9 }
            r0.append(r14)     // Catch:{ RuntimeException -> 0x05f9 }
            net.sourceforge.opencamera.ui.MainUI r7 = r1.mainUI     // Catch:{ RuntimeException -> 0x05f9 }
            java.lang.String r7 = r7.getEntryForColorEffect(r9)     // Catch:{ RuntimeException -> 0x05f9 }
            r0.append(r7)     // Catch:{ RuntimeException -> 0x05f9 }
            java.lang.String r3 = r0.toString()     // Catch:{ RuntimeException -> 0x05f9 }
            r19 = 0
            goto L_0x0601
        L_0x05f9:
            r0 = move-exception
            goto L_0x05fe
        L_0x05fb:
            r0 = move-exception
            r4 = r17
        L_0x05fe:
            r0.printStackTrace()
        L_0x0601:
            net.sourceforge.opencamera.MyApplicationInterface r0 = r1.applicationInterface
            java.lang.String r0 = r0.getLockOrientationPref()
            boolean r4 = r0.equals(r4)
            if (r4 != 0) goto L_0x0648
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r4 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r2 == r4) goto L_0x0648
            android.content.res.Resources r4 = r26.getResources()
            r7 = 2130772014(0x7f01002e, float:1.7147134E38)
            java.lang.String[] r4 = r4.getStringArray(r7)
            android.content.res.Resources r7 = r26.getResources()
            r8 = 2130772015(0x7f01002f, float:1.7147136E38)
            java.lang.String[] r7 = r7.getStringArray(r8)
            java.util.List r7 = java.util.Arrays.asList(r7)
            int r0 = r7.indexOf(r0)
            r7 = -1
            if (r0 == r7) goto L_0x0648
            r0 = r4[r0]
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r3)
            r4.append(r15)
            r4.append(r0)
            java.lang.String r3 = r4.toString()
            r19 = 0
        L_0x0648:
            java.lang.String r0 = net.sourceforge.opencamera.PreferenceKeys.getTimerPreferenceKey()
            java.lang.String r0 = r5.getString(r0, r6)
            boolean r4 = r0.equals(r6)
            if (r4 != 0) goto L_0x06a2
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r4 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r2 == r4) goto L_0x06a2
            android.content.res.Resources r2 = r26.getResources()
            r4 = 2130772056(0x7f010058, float:1.714722E38)
            java.lang.String[] r2 = r2.getStringArray(r4)
            android.content.res.Resources r4 = r26.getResources()
            r5 = 2130772057(0x7f010059, float:1.7147222E38)
            java.lang.String[] r4 = r4.getStringArray(r5)
            java.util.List r4 = java.util.Arrays.asList(r4)
            int r0 = r4.indexOf(r0)
            r4 = -1
            if (r0 == r4) goto L_0x06a2
            r0 = r2[r0]
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r3)
            r2.append(r15)
            android.content.res.Resources r3 = r26.getResources()
            r4 = 2131493440(0x7f0c0240, float:1.861036E38)
            java.lang.String r3 = r3.getString(r4)
            r2.append(r3)
            r2.append(r14)
            r2.append(r0)
            java.lang.String r3 = r2.toString()
            r19 = 0
        L_0x06a2:
            net.sourceforge.opencamera.MyApplicationInterface r0 = r1.applicationInterface
            java.lang.String r0 = r0.getRepeatPref()
            java.lang.String r2 = "1"
            boolean r2 = r0.equals(r2)
            if (r2 != 0) goto L_0x06f7
            android.content.res.Resources r2 = r26.getResources()
            r4 = 2130771982(0x7f01000e, float:1.714707E38)
            java.lang.String[] r2 = r2.getStringArray(r4)
            android.content.res.Resources r4 = r26.getResources()
            r5 = 2130771983(0x7f01000f, float:1.7147072E38)
            java.lang.String[] r4 = r4.getStringArray(r5)
            java.util.List r4 = java.util.Arrays.asList(r4)
            int r0 = r4.indexOf(r0)
            r4 = -1
            if (r0 == r4) goto L_0x06f7
            r0 = r2[r0]
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r3)
            r2.append(r15)
            android.content.res.Resources r3 = r26.getResources()
            r4 = 2131493104(0x7f0c00f0, float:1.8609679E38)
            java.lang.String r3 = r3.getString(r4)
            r2.append(r3)
            r2.append(r14)
            r2.append(r0)
            java.lang.String r3 = r2.toString()
            goto L_0x06f9
        L_0x06f7:
            r16 = r19
        L_0x06f9:
            if (r16 == 0) goto L_0x06fd
            if (r27 == 0) goto L_0x0704
        L_0x06fd:
            net.sourceforge.opencamera.preview.Preview r0 = r1.preview
            net.sourceforge.opencamera.ToastBoxer r2 = r1.switch_video_toast
            r0.showToast(r2, r3)
        L_0x0704:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MainActivity.showPhotoVideoToast(boolean):void");
    }

    private void freeAudioListener(boolean z) {
        AudioListener audioListener = this.audio_listener;
        if (audioListener != null) {
            audioListener.release(z);
            this.audio_listener = null;
        }
        this.mainUI.audioControlStopped();
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startAudioListener() {
        /*
            r9 = this;
            int r0 = android.os.Build.VERSION.SDK_INT
            r1 = 23
            if (r0 < r1) goto L_0x0014
            java.lang.String r0 = "android.permission.RECORD_AUDIO"
            int r0 = android.support.p000v4.content.ContextCompat.checkSelfPermission(r9, r0)
            if (r0 == 0) goto L_0x0014
            net.sourceforge.opencamera.MyApplicationInterface r0 = r9.applicationInterface
            r0.requestRecordAudioPermission()
            return
        L_0x0014:
            net.sourceforge.opencamera.MyAudioTriggerListenerCallback r0 = new net.sourceforge.opencamera.MyAudioTriggerListenerCallback
            r0.<init>(r9)
            net.sourceforge.opencamera.AudioListener r1 = new net.sourceforge.opencamera.AudioListener
            r1.<init>(r0)
            r9.audio_listener = r1
            net.sourceforge.opencamera.AudioListener r1 = r9.audio_listener
            boolean r1 = r1.status()
            r2 = 1
            if (r1 == 0) goto L_0x00ba
            net.sourceforge.opencamera.preview.Preview r1 = r9.preview
            net.sourceforge.opencamera.ToastBoxer r3 = r9.audio_control_toast
            r4 = 2131492884(0x7f0c0014, float:1.8609233E38)
            r1.showToast(r3, r4)
            net.sourceforge.opencamera.AudioListener r1 = r9.audio_listener
            r1.start()
            android.content.SharedPreferences r1 = android.preference.PreferenceManager.getDefaultSharedPreferences(r9)
            java.lang.String r3 = "preference_audio_noise_control_sensitivity"
            java.lang.String r4 = "0"
            java.lang.String r1 = r1.getString(r3, r4)
            r3 = -1
            int r4 = r1.hashCode()
            r5 = 5
            r6 = 4
            r7 = 3
            r8 = 2
            switch(r4) {
                case 49: goto L_0x0086;
                case 50: goto L_0x007c;
                case 51: goto L_0x0072;
                default: goto L_0x0050;
            }
        L_0x0050:
            switch(r4) {
                case 1444: goto L_0x0068;
                case 1445: goto L_0x005e;
                case 1446: goto L_0x0054;
                default: goto L_0x0053;
            }
        L_0x0053:
            goto L_0x0090
        L_0x0054:
            java.lang.String r4 = "-3"
            boolean r1 = r1.equals(r4)
            if (r1 == 0) goto L_0x0090
            r1 = 5
            goto L_0x0091
        L_0x005e:
            java.lang.String r4 = "-2"
            boolean r1 = r1.equals(r4)
            if (r1 == 0) goto L_0x0090
            r1 = 4
            goto L_0x0091
        L_0x0068:
            java.lang.String r4 = "-1"
            boolean r1 = r1.equals(r4)
            if (r1 == 0) goto L_0x0090
            r1 = 3
            goto L_0x0091
        L_0x0072:
            java.lang.String r4 = "3"
            boolean r1 = r1.equals(r4)
            if (r1 == 0) goto L_0x0090
            r1 = 0
            goto L_0x0091
        L_0x007c:
            java.lang.String r4 = "2"
            boolean r1 = r1.equals(r4)
            if (r1 == 0) goto L_0x0090
            r1 = 1
            goto L_0x0091
        L_0x0086:
            java.lang.String r4 = "1"
            boolean r1 = r1.equals(r4)
            if (r1 == 0) goto L_0x0090
            r1 = 2
            goto L_0x0091
        L_0x0090:
            r1 = -1
        L_0x0091:
            if (r1 == 0) goto L_0x00af
            if (r1 == r2) goto L_0x00ac
            if (r1 == r8) goto L_0x00a9
            if (r1 == r7) goto L_0x00a6
            if (r1 == r6) goto L_0x00a3
            if (r1 == r5) goto L_0x00a0
            r1 = 100
            goto L_0x00b1
        L_0x00a0:
            r1 = 400(0x190, float:5.6E-43)
            goto L_0x00b1
        L_0x00a3:
            r1 = 200(0xc8, float:2.8E-43)
            goto L_0x00b1
        L_0x00a6:
            r1 = 150(0x96, float:2.1E-43)
            goto L_0x00b1
        L_0x00a9:
            r1 = 125(0x7d, float:1.75E-43)
            goto L_0x00b1
        L_0x00ac:
            r1 = 75
            goto L_0x00b1
        L_0x00af:
            r1 = 50
        L_0x00b1:
            r0.setAudioNoiseSensitivity(r1)
            net.sourceforge.opencamera.ui.MainUI r0 = r9.mainUI
            r0.audioControlStarted()
            goto L_0x00ca
        L_0x00ba:
            net.sourceforge.opencamera.AudioListener r0 = r9.audio_listener
            r0.release(r2)
            r0 = 0
            r9.audio_listener = r0
            net.sourceforge.opencamera.preview.Preview r1 = r9.preview
            r2 = 2131492883(0x7f0c0013, float:1.860923E38)
            r1.showToast(r0, r2)
        L_0x00ca:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MainActivity.startAudioListener():void");
    }

    public boolean hasAudioControl() {
        String string = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.AudioControlPreferenceKey, CameraController.COLOR_EFFECT_DEFAULT);
        if (string.equals("voice")) {
            return this.speechControl.hasSpeechRecognition();
        }
        return string.equals("noise");
    }

    public void stopAudioListeners() {
        freeAudioListener(true);
        if (this.speechControl.hasSpeechRecognition()) {
            this.speechControl.stopListening();
        }
    }

    /* access modifiers changed from: 0000 */
    public void initLocation() {
        if (!this.applicationInterface.getLocationSupplier().setupLocationListener()) {
            this.permissionHandler.requestLocationPermission();
        }
    }

    private void initGyroSensors() {
        if (this.applicationInterface.getPhotoMode() == PhotoMode.Panorama) {
            this.applicationInterface.getGyroSensor().enableSensors();
        } else {
            this.applicationInterface.getGyroSensor().disableSensors();
        }
    }

    /* access modifiers changed from: 0000 */
    public void speak(String str) {
        TextToSpeech textToSpeech2 = this.textToSpeech;
        if (textToSpeech2 != null && this.textToSpeechSuccess) {
            textToSpeech2.speak(str, 0, null);
        }
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        this.permissionHandler.onRequestPermissionsResult(i, iArr);
    }

    public void restartOpenCamera() {
        waitUntilImageQueueEmpty();
        Intent launchIntentForPackage = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        launchIntentForPackage.addFlags(67108864);
        startActivity(launchIntentForPackage);
    }

    public void takePhotoButtonLongClickCancelled() {
        if (this.preview.getCameraController() != null && this.preview.getCameraController().isContinuousBurstInProgress()) {
            this.preview.getCameraController().stopContinuousBurst();
        }
    }

    /* access modifiers changed from: 0000 */
    public ToastBoxer getAudioControlToast() {
        return this.audio_control_toast;
    }

    public SaveLocationHistory getSaveLocationHistory() {
        return this.save_location_history;
    }

    public SaveLocationHistory getSaveLocationHistorySAF() {
        return this.save_location_history_saf;
    }

    public void usedFolderPicker() {
        if (this.applicationInterface.getStorageUtils().isUsingSAF()) {
            this.save_location_history_saf.updateFolderHistory(getStorageUtils().getSaveLocationSAF(), true);
        } else {
            this.save_location_history.updateFolderHistory(getStorageUtils().getSaveLocation(), true);
        }
    }

    public boolean hasThumbnailAnimation() {
        return this.applicationInterface.hasThumbnailAnimation();
    }

    public boolean testHasNotification() {
        return this.has_notification;
    }
}
