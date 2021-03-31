package net.sourceforge.opencamera;

import android.content.Context;
import android.location.Location;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TextFormatter {
    private static final String TAG = "TextFormatter";
    private final Context context;
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.0");

    TextFormatter(Context context2) {
        this.context = context2;
    }

    public static String getDateString(String str, Date date) {
        if (str.equals("preference_stamp_dateformat_none")) {
            return BuildConfig.FLAVOR;
        }
        char c = 65535;
        int hashCode = str.hashCode();
        if (hashCode != -1966818982) {
            if (hashCode != -34803366) {
                if (hashCode == 2084430170 && str.equals("preference_stamp_dateformat_yyyymmdd")) {
                    c = 0;
                }
            } else if (str.equals("preference_stamp_dateformat_mmddyyyy")) {
                c = 2;
            }
        } else if (str.equals("preference_stamp_dateformat_ddmmyyyy")) {
            c = 1;
        }
        if (c == 0) {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        } else if (c == 1) {
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
        } else if (c != 2) {
            return DateFormat.getDateInstance().format(date);
        } else {
            return new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(date);
        }
    }

    public static String getTimeString(String str, Date date) {
        if (str.equals("preference_stamp_timeformat_none")) {
            return BuildConfig.FLAVOR;
        }
        char c = 65535;
        int hashCode = str.hashCode();
        if (hashCode != 2061556288) {
            if (hashCode == 2092032481 && str.equals("preference_stamp_timeformat_24hour")) {
                c = 1;
            }
        } else if (str.equals("preference_stamp_timeformat_12hour")) {
            c = 0;
        }
        if (c == 0) {
            return new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(date);
        } else if (c != 1) {
            return DateFormat.getTimeInstance().format(date);
        } else {
            return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date);
        }
    }

    private String getDistanceString(double d, String str) {
        String string = this.context.getResources().getString(C0316R.string.metres_abbreviation);
        if (str.equals("preference_units_distance_ft")) {
            d *= 3.28084d;
            string = this.context.getResources().getString(C0316R.string.feet_abbreviation);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.decimalFormat.format(d));
        sb.append(string);
        return sb.toString();
    }

    public String getGPSString(String str, String str2, boolean z, Location location, boolean z2, double d) {
        String str3;
        boolean equals = str.equals("preference_stamp_gpsformat_none");
        String str4 = BuildConfig.FLAVOR;
        if (equals) {
            return str4;
        }
        String str5 = ", ";
        if (z) {
            if (str.equals("preference_stamp_gpsformat_dms")) {
                StringBuilder sb = new StringBuilder();
                sb.append(str4);
                sb.append(LocationSupplier.locationToDMS(location.getLatitude()));
                sb.append(str5);
                sb.append(LocationSupplier.locationToDMS(location.getLongitude()));
                str3 = sb.toString();
            } else {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(str4);
                sb2.append(Location.convert(location.getLatitude(), 0));
                sb2.append(str5);
                sb2.append(Location.convert(location.getLongitude(), 0));
                str3 = sb2.toString();
            }
            if (location.hasAltitude()) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append(str3);
                sb3.append(str5);
                sb3.append(getDistanceString(location.getAltitude(), str2));
                str3 = sb3.toString();
            }
        } else {
            str3 = str4;
        }
        if (!z2) {
            return str3;
        }
        float degrees = (float) Math.toDegrees(d);
        if (degrees < 0.0f) {
            degrees += 360.0f;
        }
        if (str3.length() > 0) {
            StringBuilder sb4 = new StringBuilder();
            sb4.append(str3);
            sb4.append(str5);
            str3 = sb4.toString();
        }
        StringBuilder sb5 = new StringBuilder();
        sb5.append(str3);
        sb5.append(str4);
        sb5.append(Math.round(degrees));
        sb5.append(176);
        return sb5.toString();
    }

    public static String formatTimeMS(long j) {
        int i = ((int) j) % 1000;
        int i2 = ((int) (j / 1000)) % 60;
        int i3 = (int) ((j / 60000) % 60);
        int i4 = (int) (j / 3600000);
        return String.format(Locale.getDefault(), "%02d:%02d:%02d,%03d", new Object[]{Integer.valueOf(i4), Integer.valueOf(i3), Integer.valueOf(i2), Integer.valueOf(i)});
    }
}
