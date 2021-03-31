package net.sourceforge.opencamera;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.support.p000v4.app.ActivityCompat;

public class PermissionHandler {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 3;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 1;
    private static final String TAG = "PermissionHandler";
    /* access modifiers changed from: private */
    public final MainActivity main_activity;

    PermissionHandler(MainActivity mainActivity) {
        this.main_activity = mainActivity;
    }

    private void showRequestPermissionRationale(final int i) {
        final String[] strArr;
        if (VERSION.SDK_INT >= 23) {
            int i2 = 0;
            boolean z = true;
            if (i == 0) {
                strArr = new String[]{"android.permission.CAMERA"};
                i2 = C0316R.string.permission_rationale_camera;
            } else if (i == 1) {
                strArr = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
                i2 = C0316R.string.permission_rationale_storage;
            } else if (i == 2) {
                strArr = new String[]{"android.permission.RECORD_AUDIO"};
                i2 = C0316R.string.permission_rationale_record_audio;
            } else if (i != 3) {
                strArr = null;
                z = false;
            } else {
                strArr = new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};
                i2 = C0316R.string.permission_rationale_location;
            }
            if (z) {
                new Builder(this.main_activity).setTitle(C0316R.string.permission_rationale_title).setMessage(i2).setIcon(17301543).setPositiveButton(17039370, null).setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialogInterface) {
                        ActivityCompat.requestPermissions(PermissionHandler.this.main_activity, strArr, i);
                    }
                }).show();
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void requestCameraPermission() {
        if (VERSION.SDK_INT >= 23) {
            String str = "android.permission.CAMERA";
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.main_activity, str)) {
                showRequestPermissionRationale(0);
            } else {
                ActivityCompat.requestPermissions(this.main_activity, new String[]{str}, 0);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void requestStoragePermission() {
        if (VERSION.SDK_INT >= 23) {
            String str = "android.permission.WRITE_EXTERNAL_STORAGE";
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.main_activity, str)) {
                showRequestPermissionRationale(1);
            } else {
                ActivityCompat.requestPermissions(this.main_activity, new String[]{str}, 1);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void requestRecordAudioPermission() {
        if (VERSION.SDK_INT >= 23) {
            String str = "android.permission.RECORD_AUDIO";
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.main_activity, str)) {
                showRequestPermissionRationale(2);
            } else {
                ActivityCompat.requestPermissions(this.main_activity, new String[]{str}, 2);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void requestLocationPermission() {
        if (VERSION.SDK_INT >= 23) {
            String str = "android.permission.ACCESS_FINE_LOCATION";
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this.main_activity, str)) {
                String str2 = "android.permission.ACCESS_COARSE_LOCATION";
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this.main_activity, str2)) {
                    ActivityCompat.requestPermissions(this.main_activity, new String[]{str, str2}, 3);
                }
            }
            showRequestPermissionRationale(3);
        }
    }

    public void onRequestPermissionsResult(int i, int[] iArr) {
        if (VERSION.SDK_INT >= 23) {
            if (i == 0) {
                if (iArr.length > 0 && iArr[0] == 0) {
                    this.main_activity.getPreview().retryOpenCamera();
                }
            } else if (i == 1) {
                if (iArr.length > 0 && iArr[0] == 0) {
                    this.main_activity.getPreview().retryOpenCamera();
                }
            } else if (i == 2) {
                if (iArr.length > 0) {
                    int i2 = iArr[0];
                }
            } else if (i == 3) {
                if (iArr.length <= 0 || iArr[0] != 0) {
                    this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.permission_location_not_available);
                    Editor edit = PreferenceManager.getDefaultSharedPreferences(this.main_activity).edit();
                    edit.putBoolean(PreferenceKeys.LocationPreferenceKey, false);
                    edit.apply();
                } else {
                    this.main_activity.initLocation();
                }
            }
        }
    }
}
