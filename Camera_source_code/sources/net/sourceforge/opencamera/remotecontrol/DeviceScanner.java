package net.sourceforge.opencamera.remotecontrol;

import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.p000v4.app.ActivityCompat;
import android.support.p000v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import net.sourceforge.opencamera.C0316R;
import net.sourceforge.opencamera.PreferenceKeys;
import net.sourceforge.opencamera.cameracontroller.CameraController;

public class DeviceScanner extends ListActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSIONS = 2;
    private static final String TAG = "OC-BLEScanner";
    /* access modifiers changed from: private */
    public BluetoothAdapter bluetoothAdapter;
    private Handler bluetoothHandler;
    /* access modifiers changed from: private */
    public boolean is_scanning;
    /* access modifiers changed from: private */
    public LeDeviceListAdapter leDeviceListAdapter;
    /* access modifiers changed from: private */
    public final LeScanCallback mLeScanCallback = new LeScanCallback() {
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
            DeviceScanner.this.runOnUiThread(new Runnable() {
                public void run() {
                    DeviceScanner.this.leDeviceListAdapter.addDevice(bluetoothDevice);
                    DeviceScanner.this.leDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
    private SharedPreferences mSharedPreferences;

    private class LeDeviceListAdapter extends BaseAdapter {
        private final LayoutInflater mInflator;
        private final ArrayList<BluetoothDevice> mLeDevices = new ArrayList<>();

        public long getItemId(int i) {
            return (long) i;
        }

        LeDeviceListAdapter() {
            this.mInflator = DeviceScanner.this.getLayoutInflater();
        }

        /* access modifiers changed from: 0000 */
        public void addDevice(BluetoothDevice bluetoothDevice) {
            if (!this.mLeDevices.contains(bluetoothDevice)) {
                this.mLeDevices.add(bluetoothDevice);
            }
        }

        /* access modifiers changed from: 0000 */
        public BluetoothDevice getDevice(int i) {
            return (BluetoothDevice) this.mLeDevices.get(i);
        }

        /* access modifiers changed from: 0000 */
        public void clear() {
            this.mLeDevices.clear();
        }

        public int getCount() {
            return this.mLeDevices.size();
        }

        public Object getItem(int i) {
            return this.mLeDevices.get(i);
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = this.mInflator.inflate(C0316R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(C0316R.C0318id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(C0316R.C0318id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice bluetoothDevice = (BluetoothDevice) this.mLeDevices.get(i);
            String name = bluetoothDevice.getName();
            if (name == null || name.length() <= 0) {
                viewHolder.deviceName.setText(C0316R.string.unknown_device);
            } else {
                viewHolder.deviceName.setText(name);
            }
            viewHolder.deviceAddress.setText(bluetoothDevice.getAddress());
            return view;
        }
    }

    static class ViewHolder {
        TextView deviceAddress;
        TextView deviceName;

        ViewHolder() {
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0316R.layout.activity_device_select);
        this.bluetoothHandler = new Handler();
        if (!getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            Toast.makeText(this, C0316R.string.ble_not_supported, 0).show();
            finish();
        }
        this.bluetoothAdapter = ((BluetoothManager) getSystemService("bluetooth")).getAdapter();
        if (this.bluetoothAdapter == null) {
            Toast.makeText(this, C0316R.string.bluetooth_not_supported, 0).show();
            finish();
            return;
        }
        ((Button) findViewById(C0316R.C0318id.StartScanButton)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                DeviceScanner.this.startScanning();
            }
        });
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String string = this.mSharedPreferences.getString(PreferenceKeys.RemoteName, CameraController.COLOR_EFFECT_DEFAULT);
        TextView textView = (TextView) findViewById(C0316R.C0318id.currentRemote);
        StringBuilder sb = new StringBuilder();
        sb.append(getResources().getString(C0316R.string.bluetooth_current_remote));
        sb.append(" ");
        sb.append(string);
        textView.setText(sb.toString());
    }

    /* access modifiers changed from: private */
    public void startScanning() {
        if (!this.bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
        }
        this.leDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(this.leDeviceListAdapter);
        if ((VERSION.SDK_INT >= 23 ? ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") : 0) == 0) {
            scanLeDevice(true);
        } else {
            askForLocationPermission();
        }
    }

    private void askForLocationPermission() {
        String str = "android.permission.ACCESS_FINE_LOCATION";
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, str)) {
            String str2 = "android.permission.ACCESS_COARSE_LOCATION";
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, str2)) {
                ActivityCompat.requestPermissions(this, new String[]{str, str2}, 2);
                return;
            }
        }
        showRequestLocationPermissionRationale();
    }

    private void showRequestLocationPermissionRationale() {
        if (VERSION.SDK_INT >= 23) {
            final String[] strArr = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};
            new Builder(this).setTitle(C0316R.string.permission_rationale_title).setMessage(C0316R.string.permission_rationale_location).setIcon(17301543).setPositiveButton(17039370, null).setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    ActivityCompat.requestPermissions(DeviceScanner.this, strArr, 2);
                }
            }).show();
        }
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 2 && iArr.length > 0 && iArr[0] == 0) {
            scanLeDevice(true);
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1 && i2 == 0) {
            finish();
        } else {
            super.onActivityResult(i, i2, intent);
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        if (this.is_scanning) {
            scanLeDevice(false);
            this.leDeviceListAdapter.clear();
        }
    }

    /* access modifiers changed from: protected */
    public void onListItemClick(ListView listView, View view, int i, long j) {
        BluetoothDevice device = this.leDeviceListAdapter.getDevice(i);
        if (device != null) {
            Editor edit = this.mSharedPreferences.edit();
            edit.putString(PreferenceKeys.RemoteName, device.getAddress());
            edit.apply();
            scanLeDevice(false);
            finish();
        }
    }

    private void scanLeDevice(boolean z) {
        if (z) {
            this.bluetoothHandler.postDelayed(new Runnable() {
                public void run() {
                    DeviceScanner.this.is_scanning = false;
                    DeviceScanner.this.bluetoothAdapter.stopLeScan(DeviceScanner.this.mLeScanCallback);
                    DeviceScanner.this.invalidateOptionsMenu();
                }
            }, 10000);
            this.is_scanning = true;
            this.bluetoothAdapter.startLeScan(this.mLeScanCallback);
        } else {
            this.is_scanning = false;
            this.bluetoothAdapter.stopLeScan(this.mLeScanCallback);
        }
        invalidateOptionsMenu();
    }
}
