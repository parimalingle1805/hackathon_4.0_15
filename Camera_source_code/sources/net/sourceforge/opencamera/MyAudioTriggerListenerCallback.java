package net.sourceforge.opencamera;

import android.preference.PreferenceManager;
import net.sourceforge.opencamera.AudioListener.AudioListenerCallback;
import net.sourceforge.opencamera.cameracontroller.CameraController;

public class MyAudioTriggerListenerCallback implements AudioListenerCallback {
    private static final String TAG = "MyAudioTriggerLstnrCb";
    private int audio_noise_sensitivity = -1;
    private int last_level = -1;
    private final MainActivity main_activity;
    private long time_last_audio_trigger_photo = -1;
    private long time_quiet_loud = -1;

    MyAudioTriggerListenerCallback(MainActivity mainActivity) {
        this.main_activity = mainActivity;
    }

    /* access modifiers changed from: 0000 */
    public void setAudioNoiseSensitivity(int i) {
        this.audio_noise_sensitivity = i;
    }

    public void onAudio(int i) {
        int i2 = this.last_level;
        if (i2 == -1) {
            this.last_level = i;
            return;
        }
        int i3 = i - i2;
        int i4 = this.audio_noise_sensitivity;
        boolean z = false;
        if (i3 > i4) {
            this.time_quiet_loud = System.currentTimeMillis();
        } else if (i3 < (-i4) && this.time_quiet_loud != -1) {
            if (System.currentTimeMillis() - this.time_quiet_loud < 1500) {
                z = true;
            }
            this.time_quiet_loud = -1;
        }
        this.last_level = i;
        if (z) {
            long currentTimeMillis = System.currentTimeMillis();
            boolean equals = PreferenceManager.getDefaultSharedPreferences(this.main_activity).getString(PreferenceKeys.AudioControlPreferenceKey, CameraController.COLOR_EFFECT_DEFAULT).equals("noise");
            long j = this.time_last_audio_trigger_photo;
            if ((j == -1 || currentTimeMillis - j >= 5000) && equals) {
                this.time_last_audio_trigger_photo = currentTimeMillis;
                this.main_activity.audioTrigger();
            }
        }
    }
}
