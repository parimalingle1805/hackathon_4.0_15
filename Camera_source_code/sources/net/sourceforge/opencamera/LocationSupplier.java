package net.sourceforge.opencamera;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.p000v4.content.ContextCompat;

public class LocationSupplier {
    private static final String TAG = "LocationSupplier";
    private final Context context;
    private MyLocationListener[] locationListeners;
    private final LocationManager locationManager;
    private volatile boolean test_force_no_location;

    private static class MyLocationListener implements LocationListener {
        private Location location;
        volatile boolean test_has_received_location;

        public void onProviderEnabled(String str) {
        }

        private MyLocationListener() {
        }

        /* access modifiers changed from: 0000 */
        public Location getLocation() {
            return this.location;
        }

        public void onLocationChanged(Location location2) {
            this.test_has_received_location = true;
            if (location2 == null) {
                return;
            }
            if (location2.getLatitude() != 0.0d || location2.getLongitude() != 0.0d) {
                this.location = location2;
            }
        }

        public void onStatusChanged(String str, int i, Bundle bundle) {
            if (i == 0 || i == 1) {
                this.location = null;
                this.test_has_received_location = false;
            }
        }

        public void onProviderDisabled(String str) {
            this.location = null;
            this.test_has_received_location = false;
        }
    }

    LocationSupplier(Context context2) {
        this.context = context2;
        this.locationManager = (LocationManager) context2.getSystemService("location");
    }

    public Location getLocation() {
        if (this.locationListeners == null || this.test_force_no_location) {
            return null;
        }
        for (MyLocationListener location : this.locationListeners) {
            Location location2 = location.getLocation();
            if (location2 != null) {
                return location2;
            }
        }
        return null;
    }

    /* access modifiers changed from: 0000 */
    public boolean setupLocationListener() {
        boolean z = PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(PreferenceKeys.LocationPreferenceKey, false);
        if (z && this.locationListeners == null) {
            if (VERSION.SDK_INT >= 23) {
                boolean z2 = ContextCompat.checkSelfPermission(this.context, "android.permission.ACCESS_COARSE_LOCATION") == 0;
                boolean z3 = ContextCompat.checkSelfPermission(this.context, "android.permission.ACCESS_FINE_LOCATION") == 0;
                if (!z2 || !z3) {
                    return false;
                }
            }
            this.locationListeners = new MyLocationListener[2];
            this.locationListeners[0] = new MyLocationListener();
            this.locationListeners[1] = new MyLocationListener();
            if (this.locationManager.getAllProviders().contains("network")) {
                this.locationManager.requestLocationUpdates("network", 1000, 0.0f, this.locationListeners[1]);
            }
            if (this.locationManager.getAllProviders().contains("gps")) {
                this.locationManager.requestLocationUpdates("gps", 1000, 0.0f, this.locationListeners[0]);
            }
        } else if (!z) {
            freeLocationListeners();
        }
        return true;
    }

    /* access modifiers changed from: 0000 */
    public void freeLocationListeners() {
        if (this.locationListeners != null) {
            int i = 0;
            if (VERSION.SDK_INT >= 23) {
                boolean z = true;
                boolean z2 = ContextCompat.checkSelfPermission(this.context, "android.permission.ACCESS_COARSE_LOCATION") == 0;
                if (ContextCompat.checkSelfPermission(this.context, "android.permission.ACCESS_FINE_LOCATION") != 0) {
                    z = false;
                }
                if (!z2 && !z) {
                    return;
                }
            }
            while (true) {
                MyLocationListener[] myLocationListenerArr = this.locationListeners;
                if (i >= myLocationListenerArr.length) {
                    break;
                }
                this.locationManager.removeUpdates(myLocationListenerArr[i]);
                this.locationListeners[i] = null;
                i++;
            }
            this.locationListeners = null;
        }
    }

    public boolean testHasReceivedLocation() {
        MyLocationListener[] myLocationListenerArr = this.locationListeners;
        if (myLocationListenerArr == null) {
            return false;
        }
        for (MyLocationListener myLocationListener : myLocationListenerArr) {
            if (myLocationListener.test_has_received_location) {
                return true;
            }
        }
        return false;
    }

    public void setForceNoLocation(boolean z) {
        this.test_force_no_location = z;
    }

    public boolean hasLocationListeners() {
        MyLocationListener[] myLocationListenerArr = this.locationListeners;
        if (myLocationListenerArr == null || myLocationListenerArr.length != 2) {
            return false;
        }
        for (MyLocationListener myLocationListener : myLocationListenerArr) {
            if (myLocationListener == null) {
                return false;
            }
        }
        return true;
    }

    public static String locationToDMS(double d) {
        String str = BuildConfig.FLAVOR;
        String str2 = d < 0.0d ? "-" : str;
        double abs = Math.abs(d);
        int i = (int) abs;
        boolean z = true;
        boolean z2 = i == 0;
        String valueOf = String.valueOf(i);
        double d2 = (double) i;
        Double.isNaN(d2);
        double d3 = (abs - d2) * 60.0d;
        int i2 = (int) d3;
        boolean z3 = z2 && i2 == 0;
        double d4 = (double) i2;
        Double.isNaN(d4);
        double d5 = d3 - d4;
        String valueOf2 = String.valueOf(i2);
        int i3 = (int) (d5 * 60.0d);
        if (!z3 || i3 != 0) {
            z = false;
        }
        String valueOf3 = String.valueOf(i3);
        if (!z) {
            str = str2;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append(valueOf);
        sb.append("°");
        sb.append(valueOf2);
        sb.append("'");
        sb.append(valueOf3);
        sb.append("\"");
        return sb.toString();
    }
}
