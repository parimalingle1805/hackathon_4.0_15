package net.sourceforge.opencamera.remotecontrol;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothLeService extends Service {
    public static final String ACTION_DATA_AVAILABLE = "net.sourceforge.opencamera.Remotecontrol.ACTION_DATA_AVAILABLE";
    public static final String ACTION_GATT_CONNECTED = "net.sourceforge.opencamera.Remotecontrol.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "net.sourceforge.opencamera.Remotecontrol.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "net.sourceforge.opencamera.Remotecontrol.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_REMOTE_COMMAND = "net.sourceforge.opencamera.Remotecontrol.COMMAND";
    public static final String ACTION_SENSOR_VALUE = "net.sourceforge.opencamera.Remotecontrol.SENSOR";
    public static final int COMMAND_AFMF = 97;
    public static final int COMMAND_DOWN = 80;
    public static final int COMMAND_MENU = 48;
    public static final int COMMAND_MODE = 16;
    public static final int COMMAND_SHUTTER = 32;
    public static final int COMMAND_UP = 64;
    public static final String EXTRA_DATA = "net.sourceforge.opencamera.Remotecontrol.EXTRA_DATA";
    public static final String SENSOR_DEPTH = "net.sourceforge.opencamera.Remotecontrol.DEPTH";
    public static final String SENSOR_TEMPERATURE = "net.sourceforge.opencamera.Remotecontrol.TEMPERATURE";
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_DISCONNECTED = 0;
    private static final String TAG = "BluetoothLeService";
    private BluetoothAdapter bluetoothAdapter;
    /* access modifiers changed from: private */
    public BluetoothGatt bluetoothGatt;
    private BluetoothManager bluetoothManager;
    /* access modifiers changed from: private */
    public final List<BluetoothGattCharacteristic> charsToSubscribe = new ArrayList();
    /* access modifiers changed from: private */
    public double currentDepth = -1.0d;
    /* access modifiers changed from: private */
    public double currentTemp = -1.0d;
    /* access modifiers changed from: private */
    public String device_address;
    private final IBinder mBinder = new LocalBinder();
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            if (i2 == 2) {
                BluetoothLeService.this.broadcastUpdate(BluetoothLeService.ACTION_GATT_CONNECTED);
                BluetoothLeService.this.bluetoothGatt.discoverServices();
                BluetoothLeService.this.currentDepth = -1.0d;
                BluetoothLeService.this.currentTemp = -1.0d;
            } else if (i2 == 0) {
                BluetoothLeService.this.broadcastUpdate(BluetoothLeService.ACTION_GATT_DISCONNECTED);
                attemptReconnect();
            }
        }

        /* access modifiers changed from: 0000 */
        public void attemptReconnect() {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    BluetoothLeService.this.connect(BluetoothLeService.this.device_address);
                }
            }, 5000);
        }

        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            if (i == 0) {
                BluetoothLeService.this.broadcastUpdate(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
                BluetoothLeService.this.subscribeToServices();
            }
        }

        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (i == 0) {
                BluetoothLeService.this.broadcastUpdate(BluetoothLeService.ACTION_DATA_AVAILABLE, bluetoothGattCharacteristic);
            }
        }

        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            BluetoothLeService.this.broadcastUpdate(BluetoothLeService.ACTION_DATA_AVAILABLE, bluetoothGattCharacteristic);
        }

        public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            if (!BluetoothLeService.this.charsToSubscribe.isEmpty()) {
                BluetoothLeService bluetoothLeService = BluetoothLeService.this;
                bluetoothLeService.setCharacteristicNotification((BluetoothGattCharacteristic) bluetoothLeService.charsToSubscribe.remove(0), true);
            }
        }
    };
    private String remote_device_type;
    private final HashMap<String, BluetoothGattCharacteristic> subscribed_characteristics = new HashMap<>();

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    public void setRemoteDeviceType(String str) {
        this.remote_device_type = str;
    }

    /* access modifiers changed from: private */
    public void subscribeToServices() {
        List list;
        List<BluetoothGattService> supportedGattServices = getSupportedGattServices();
        if (supportedGattServices != null) {
            String str = this.remote_device_type;
            char c = 65535;
            if (str.hashCode() == -2121335734 && str.equals("preference_remote_type_kraken")) {
                c = 0;
            }
            if (c != 0) {
                list = Collections.singletonList(UUID.fromString("0000"));
            } else {
                list = KrakenGattAttributes.getDesiredCharacteristics();
            }
            for (BluetoothGattService characteristics : supportedGattServices) {
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristics.getCharacteristics()) {
                    if (list.contains(bluetoothGattCharacteristic.getUuid())) {
                        this.charsToSubscribe.add(bluetoothGattCharacteristic);
                    }
                }
            }
            setCharacteristicNotification((BluetoothGattCharacteristic) this.charsToSubscribe.remove(0), true);
        }
    }

    /* access modifiers changed from: private */
    public void broadcastUpdate(String str) {
        sendBroadcast(new Intent(str));
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0042, code lost:
        if (r7 == 80) goto L_0x0046;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void broadcastUpdate(java.lang.String r7, android.bluetooth.BluetoothGattCharacteristic r8) {
        /*
            r6 = this;
            java.util.UUID r7 = r8.getUuid()
            java.util.UUID r0 = net.sourceforge.opencamera.remotecontrol.KrakenGattAttributes.KRAKEN_BUTTONS_CHARACTERISTIC
            boolean r0 = r0.equals(r7)
            r1 = 0
            if (r0 == 0) goto L_0x0058
            r7 = 17
            java.lang.Integer r7 = r8.getIntValue(r7, r1)
            int r7 = r7.intValue()
            r8 = 80
            r0 = 64
            r1 = 97
            r2 = 48
            r3 = 16
            r4 = 32
            r5 = -1
            if (r7 != r4) goto L_0x0029
            r8 = 32
            goto L_0x0046
        L_0x0029:
            if (r7 != r3) goto L_0x002e
            r8 = 16
            goto L_0x0046
        L_0x002e:
            if (r7 != r2) goto L_0x0033
            r8 = 48
            goto L_0x0046
        L_0x0033:
            if (r7 != r1) goto L_0x0038
            r8 = 97
            goto L_0x0046
        L_0x0038:
            r1 = 96
            if (r7 != r1) goto L_0x003d
            goto L_0x0045
        L_0x003d:
            if (r7 != r0) goto L_0x0042
            r8 = 64
            goto L_0x0046
        L_0x0042:
            if (r7 != r8) goto L_0x0045
            goto L_0x0046
        L_0x0045:
            r8 = -1
        L_0x0046:
            if (r8 <= r5) goto L_0x00a4
            android.content.Intent r7 = new android.content.Intent
            java.lang.String r0 = "net.sourceforge.opencamera.Remotecontrol.COMMAND"
            r7.<init>(r0)
            java.lang.String r0 = "net.sourceforge.opencamera.Remotecontrol.EXTRA_DATA"
            r7.putExtra(r0, r8)
            r6.sendBroadcast(r7)
            goto L_0x00a4
        L_0x0058:
            java.util.UUID r0 = net.sourceforge.opencamera.remotecontrol.KrakenGattAttributes.KRAKEN_SENSORS_CHARACTERISTIC
            boolean r7 = r0.equals(r7)
            if (r7 == 0) goto L_0x00a4
            r7 = 2
            r0 = 18
            java.lang.Integer r7 = r8.getIntValue(r0, r7)
            int r7 = r7.intValue()
            double r2 = (double) r7
            r4 = 4621819117588971520(0x4024000000000000, double:10.0)
            java.lang.Double.isNaN(r2)
            double r2 = r2 / r4
            java.lang.Integer r7 = r8.getIntValue(r0, r1)
            int r7 = r7.intValue()
            double r7 = (double) r7
            java.lang.Double.isNaN(r7)
            double r7 = r7 / r4
            double r0 = r6.currentTemp
            int r4 = (r2 > r0 ? 1 : (r2 == r0 ? 0 : -1))
            if (r4 != 0) goto L_0x008c
            double r0 = r6.currentDepth
            int r4 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r4 != 0) goto L_0x008c
            return
        L_0x008c:
            r6.currentDepth = r7
            r6.currentTemp = r2
            android.content.Intent r0 = new android.content.Intent
            java.lang.String r1 = "net.sourceforge.opencamera.Remotecontrol.SENSOR"
            r0.<init>(r1)
            java.lang.String r1 = "net.sourceforge.opencamera.Remotecontrol.TEMPERATURE"
            r0.putExtra(r1, r2)
            java.lang.String r1 = "net.sourceforge.opencamera.Remotecontrol.DEPTH"
            r0.putExtra(r1, r7)
            r6.sendBroadcast(r0)
        L_0x00a4:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.remotecontrol.BluetoothLeService.broadcastUpdate(java.lang.String, android.bluetooth.BluetoothGattCharacteristic):void");
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public boolean initialize() {
        BluetoothManager bluetoothManager2 = this.bluetoothManager;
        String str = TAG;
        if (bluetoothManager2 == null) {
            this.bluetoothManager = (BluetoothManager) getSystemService("bluetooth");
            if (this.bluetoothManager == null) {
                Log.e(str, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        this.bluetoothAdapter = this.bluetoothManager.getAdapter();
        if (this.bluetoothAdapter != null) {
            return true;
        }
        Log.e(str, "Unable to obtain a BluetoothAdapter.");
        return false;
    }

    public boolean connect(final String str) {
        if (this.bluetoothAdapter == null || str == null) {
            return false;
        }
        String str2 = this.device_address;
        if (str2 != null && str.equals(str2)) {
            BluetoothGatt bluetoothGatt2 = this.bluetoothGatt;
            if (bluetoothGatt2 != null) {
                bluetoothGatt2.disconnect();
                this.bluetoothGatt.close();
                this.bluetoothGatt = null;
            }
        }
        BluetoothDevice remoteDevice = this.bluetoothAdapter.getRemoteDevice(str);
        if (remoteDevice == null) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    BluetoothLeService.this.connect(str);
                }
            }, 5000);
            return false;
        }
        this.bluetoothGatt = remoteDevice.connectGatt(this, false, this.mGattCallback);
        this.device_address = str;
        return true;
    }

    private void close() {
        BluetoothGatt bluetoothGatt2 = this.bluetoothGatt;
        if (bluetoothGatt2 != null) {
            bluetoothGatt2.close();
            this.bluetoothGatt = null;
        }
    }

    /* access modifiers changed from: private */
    public void setCharacteristicNotification(BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean z) {
        if (this.bluetoothAdapter != null && this.bluetoothGatt != null) {
            String uuid = bluetoothGattCharacteristic.getUuid().toString();
            this.bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, z);
            if (z) {
                this.subscribed_characteristics.put(uuid, bluetoothGattCharacteristic);
            } else {
                this.subscribed_characteristics.remove(uuid);
            }
            BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(KrakenGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            this.bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    private List<BluetoothGattService> getSupportedGattServices() {
        BluetoothGatt bluetoothGatt2 = this.bluetoothGatt;
        if (bluetoothGatt2 == null) {
            return null;
        }
        return bluetoothGatt2.getServices();
    }
}
