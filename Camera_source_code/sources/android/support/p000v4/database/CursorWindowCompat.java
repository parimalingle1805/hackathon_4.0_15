package android.support.p000v4.database;

import android.database.CursorWindow;
import android.os.Build.VERSION;

/* renamed from: android.support.v4.database.CursorWindowCompat */
public final class CursorWindowCompat {
    private CursorWindowCompat() {
    }

    public static CursorWindow create(String str, long j) {
        if (VERSION.SDK_INT >= 28) {
            return new CursorWindow(str, j);
        }
        if (VERSION.SDK_INT >= 15) {
            return new CursorWindow(str);
        }
        return new CursorWindow(false);
    }
}
