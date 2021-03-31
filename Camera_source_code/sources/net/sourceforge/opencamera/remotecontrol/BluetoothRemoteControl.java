package net.sourceforge.opencamera.remotecontrol;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import net.sourceforge.opencamera.BuildConfig;
import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyApplicationInterface;
import net.sourceforge.opencamera.PreferenceKeys;
import net.sourceforge.opencamera.p004ui.MainUI;
import net.sourceforge.opencamera.remotecontrol.BluetoothLeService.LocalBinder;

public class BluetoothRemoteControl {
    private static final String TAG = "BluetoothRemoteControl";
    /* access modifiers changed from: private */
    public BluetoothLeService bluetoothLeService;
    /* access modifiers changed from: private */
    public boolean is_connected;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (VERSION.SDK_INT >= 18) {
                BluetoothRemoteControl.this.bluetoothLeService = ((LocalBinder) iBinder).getService();
                if (!BluetoothRemoteControl.this.bluetoothLeService.initialize()) {
                    Log.e(BluetoothRemoteControl.TAG, "Unable to initialize Bluetooth");
                    BluetoothRemoteControl.this.stopRemoteControl();
                }
                BluetoothRemoteControl.this.bluetoothLeService.connect(BluetoothRemoteControl.this.remoteDeviceAddress);
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (VERSION.SDK_INT >= 18) {
                        BluetoothRemoteControl.this.bluetoothLeService.connect(BluetoothRemoteControl.this.remoteDeviceAddress);
                    }
                }
            }, 5000);
        }
    };
    /* access modifiers changed from: private */
    public final MainActivity main_activity;
    private final BroadcastReceiver remoteControlCommandReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (VERSION.SDK_INT >= 18) {
                String action = intent.getAction();
                MyApplicationInterface applicationInterface = BluetoothRemoteControl.this.main_activity.getApplicationInterface();
                MainUI mainUI = BluetoothRemoteControl.this.main_activity.getMainUI();
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    BluetoothRemoteControl.this.bluetoothLeService.setRemoteDeviceType(BluetoothRemoteControl.this.remoteDeviceType);
                    BluetoothRemoteControl.this.main_activity.setBrightnessForCamera(false);
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    BluetoothRemoteControl.this.is_connected = false;
                    applicationInterface.getDrawPreview().onExtraOSDValuesChanged("-- °C", "-- m");
                    mainUI.updateRemoteConnectionIcon();
                    BluetoothRemoteControl.this.main_activity.setBrightnessToMinimumIfWanted();
                    if (mainUI.isExposureUIOpen()) {
                        mainUI.toggleExposureUI();
                    }
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    BluetoothRemoteControl.this.is_connected = true;
                    mainUI.updateRemoteConnectionIcon();
                } else if (BluetoothLeService.ACTION_SENSOR_VALUE.equals(action)) {
                    double doubleExtra = intent.getDoubleExtra(BluetoothLeService.SENSOR_TEMPERATURE, -1.0d);
                    double doubleExtra2 = intent.getDoubleExtra(BluetoothLeService.SENSOR_DEPTH, -1.0d);
                    double waterDensity = (double) BluetoothRemoteControl.this.main_activity.getWaterDensity();
                    Double.isNaN(waterDensity);
                    double round = (double) Math.round((doubleExtra2 / waterDensity) * 10.0d);
                    Double.isNaN(round);
                    double d = round / 10.0d;
                    StringBuilder sb = new StringBuilder();
                    String str = BuildConfig.FLAVOR;
                    sb.append(str);
                    sb.append(doubleExtra);
                    sb.append(" °C");
                    String sb2 = sb.toString();
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append(str);
                    sb3.append(d);
                    sb3.append(" m");
                    applicationInterface.getDrawPreview().onExtraOSDValuesChanged(sb2, sb3.toString());
                } else if (BluetoothLeService.ACTION_REMOTE_COMMAND.equals(action)) {
                    int intExtra = intent.getIntExtra(BluetoothLeService.EXTRA_DATA, -1);
                    if (intExtra != 16) {
                        if (intExtra == 32) {
                            BluetoothRemoteControl.this.main_activity.takePicture(false);
                        } else if (intExtra != 48) {
                            String str2 = "focus_mode_manual2";
                            if (intExtra != 64) {
                                if (intExtra != 80) {
                                    if (intExtra == 97) {
                                        mainUI.togglePopupSettings();
                                    }
                                } else if (!mainUI.processRemoteDownButton()) {
                                    if (BluetoothRemoteControl.this.main_activity.getPreview().getCurrentFocusValue() == null || !BluetoothRemoteControl.this.main_activity.getPreview().getCurrentFocusValue().equals(str2)) {
                                        BluetoothRemoteControl.this.main_activity.zoomOut();
                                    } else {
                                        BluetoothRemoteControl.this.main_activity.changeFocusDistance(25, false);
                                    }
                                }
                            } else if (!mainUI.processRemoteUpButton()) {
                                if (BluetoothRemoteControl.this.main_activity.getPreview().getCurrentFocusValue() == null || !BluetoothRemoteControl.this.main_activity.getPreview().getCurrentFocusValue().equals(str2)) {
                                    BluetoothRemoteControl.this.main_activity.zoomIn();
                                } else {
                                    BluetoothRemoteControl.this.main_activity.changeFocusDistance(-25, false);
                                }
                            }
                        } else if (mainUI.popupIsOpen()) {
                            mainUI.commandMenuPopup();
                        } else if (!mainUI.isExposureUIOpen()) {
                            mainUI.toggleExposureUI();
                        } else {
                            mainUI.commandMenuExposure();
                        }
                    } else if (mainUI.popupIsOpen()) {
                        mainUI.togglePopupSettings();
                    } else if (mainUI.isExposureUIOpen()) {
                        mainUI.toggleExposureUI();
                    } else {
                        BluetoothRemoteControl.this.main_activity.clickedSwitchVideo(null);
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public String remoteDeviceAddress;
    /* access modifiers changed from: private */
    public String remoteDeviceType;

    public BluetoothRemoteControl(MainActivity mainActivity) {
        this.main_activity = mainActivity;
    }

    public boolean remoteConnected() {
        return this.is_connected;
    }

    private static IntentFilter makeRemoteCommandIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_REMOTE_COMMAND);
        intentFilter.addAction(BluetoothLeService.ACTION_SENSOR_VALUE);
        return intentFilter;
    }

    public void startRemoteControl() {
        if (VERSION.SDK_INT >= 18) {
            Intent intent = new Intent(this.main_activity, BluetoothLeService.class);
            if (remoteEnabled()) {
                this.main_activity.bindService(intent, this.mServiceConnection, 1);
                this.main_activity.registerReceiver(this.remoteControlCommandReceiver, makeRemoteCommandIntentFilter());
            } else {
                try {
                    this.main_activity.unregisterReceiver(this.remoteControlCommandReceiver);
                    this.main_activity.unbindService(this.mServiceConnection);
                    this.is_connected = false;
                    this.main_activity.getMainUI().updateRemoteConnectionIcon();
                } catch (IllegalArgumentException unused) {
                }
            }
        }
    }

    public void stopRemoteControl() {
        if (remoteEnabled()) {
            try {
                this.main_activity.unregisterReceiver(this.remoteControlCommandReceiver);
                this.main_activity.unbindService(this.mServiceConnection);
                this.is_connected = false;
                this.main_activity.getMainUI().updateRemoteConnectionIcon();
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Remote Service was not running, that's strange");
                e.printStackTrace();
            }
        }
    }

    public boolean remoteEnabled() {
        boolean z = false;
        if (VERSION.SDK_INT < 18) {
            return false;
        }
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.main_activity);
        boolean z2 = defaultSharedPreferences.getBoolean(PreferenceKeys.EnableRemote, false);
        String str = "undefined";
        this.remoteDeviceType = defaultSharedPreferences.getString(PreferenceKeys.RemoteType, str);
        this.remoteDeviceAddress = defaultSharedPreferences.getString(PreferenceKeys.RemoteName, str);
        if (z2 && !this.remoteDeviceAddress.equals(str)) {
            z = true;
        }
        return z;
    }
}
