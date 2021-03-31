package net.sourceforge.opencamera;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;

class MagneticSensor {
    private static final String TAG = "MagneticSensor";
    private Sensor mSensorMagnetic;
    private final SensorEventListener magneticListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int i) {
            MagneticSensor.this.magnetic_accuracy = i;
            MagneticSensor.this.setMagneticAccuracyDialogText();
            MagneticSensor.this.checkMagneticAccuracy();
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            MagneticSensor.this.main_activity.getPreview().onMagneticSensorChanged(sensorEvent);
        }
    };
    private boolean magneticListenerIsRegistered;
    /* access modifiers changed from: private */
    public int magnetic_accuracy = -1;
    private AlertDialog magnetic_accuracy_dialog;
    /* access modifiers changed from: private */
    public final MainActivity main_activity;
    private boolean shown_magnetic_accuracy_dialog = false;

    MagneticSensor(MainActivity mainActivity) {
        this.main_activity = mainActivity;
    }

    /* access modifiers changed from: 0000 */
    public void initSensor(SensorManager sensorManager) {
        if (sensorManager.getDefaultSensor(2) != null) {
            this.mSensorMagnetic = sensorManager.getDefaultSensor(2);
        }
    }

    /* access modifiers changed from: 0000 */
    public void registerMagneticListener(SensorManager sensorManager) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.main_activity);
        if (!this.magneticListenerIsRegistered) {
            if (needsMagneticSensor(defaultSharedPreferences)) {
                sensorManager.registerListener(this.magneticListener, this.mSensorMagnetic, 3);
                this.magneticListenerIsRegistered = true;
            }
        } else if (!needsMagneticSensor(defaultSharedPreferences)) {
            sensorManager.unregisterListener(this.magneticListener);
            this.magneticListenerIsRegistered = false;
        }
    }

    /* access modifiers changed from: 0000 */
    public void unregisterMagneticListener(SensorManager sensorManager) {
        if (this.magneticListenerIsRegistered) {
            sensorManager.unregisterListener(this.magneticListener);
            this.magneticListenerIsRegistered = false;
        }
    }

    /* access modifiers changed from: private */
    public void setMagneticAccuracyDialogText() {
        String str;
        if (this.magnetic_accuracy_dialog != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.main_activity.getResources().getString(C0316R.string.magnetic_accuracy_info));
            sb.append(" ");
            String sb2 = sb.toString();
            int i = this.magnetic_accuracy;
            if (i == 0) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append(sb2);
                sb3.append(this.main_activity.getResources().getString(C0316R.string.accuracy_unreliable));
                str = sb3.toString();
            } else if (i == 1) {
                StringBuilder sb4 = new StringBuilder();
                sb4.append(sb2);
                sb4.append(this.main_activity.getResources().getString(C0316R.string.accuracy_low));
                str = sb4.toString();
            } else if (i == 2) {
                StringBuilder sb5 = new StringBuilder();
                sb5.append(sb2);
                sb5.append(this.main_activity.getResources().getString(C0316R.string.accuracy_medium));
                str = sb5.toString();
            } else if (i != 3) {
                StringBuilder sb6 = new StringBuilder();
                sb6.append(sb2);
                sb6.append(this.main_activity.getResources().getString(C0316R.string.accuracy_unknown));
                str = sb6.toString();
            } else {
                StringBuilder sb7 = new StringBuilder();
                sb7.append(sb2);
                sb7.append(this.main_activity.getResources().getString(C0316R.string.accuracy_high));
                str = sb7.toString();
            }
            this.magnetic_accuracy_dialog.setMessage(str);
        }
    }

    /* access modifiers changed from: 0000 */
    public void checkMagneticAccuracy() {
        int i = this.magnetic_accuracy;
        if ((i == 0 || i == 1) && !this.shown_magnetic_accuracy_dialog && !this.main_activity.getPreview().isTakingPhotoOrOnTimer() && !this.main_activity.getPreview().isVideoRecording() && !this.main_activity.isCameraInBackground()) {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.main_activity);
            if (needsMagneticSensor(defaultSharedPreferences)) {
                String str = PreferenceKeys.MagneticAccuracyPreferenceKey;
                if (defaultSharedPreferences.contains(str)) {
                    this.shown_magnetic_accuracy_dialog = true;
                    return;
                }
                this.shown_magnetic_accuracy_dialog = true;
                this.magnetic_accuracy_dialog = this.main_activity.getMainUI().showInfoDialog(C0316R.string.magnetic_accuracy_title, 0, str);
                setMagneticAccuracyDialogText();
            }
        }
    }

    private boolean needsMagneticSensor(SharedPreferences sharedPreferences) {
        if (this.main_activity.getApplicationInterface().getGeodirectionPref() || sharedPreferences.getBoolean(PreferenceKeys.ShowGeoDirectionLinesPreferenceKey, false) || sharedPreferences.getBoolean(PreferenceKeys.ShowGeoDirectionPreferenceKey, false)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: 0000 */
    public int getMagneticAccuracy() {
        return this.magnetic_accuracy;
    }

    /* access modifiers changed from: 0000 */
    public void clearDialog() {
        this.magnetic_accuracy_dialog = null;
    }
}
