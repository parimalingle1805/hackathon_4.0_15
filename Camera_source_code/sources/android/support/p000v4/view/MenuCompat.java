package android.support.p000v4.view;

import android.os.Build.VERSION;
import android.support.p000v4.internal.view.SupportMenu;
import android.view.Menu;
import android.view.MenuItem;

/* renamed from: android.support.v4.view.MenuCompat */
public final class MenuCompat {
    @Deprecated
    public static void setShowAsAction(MenuItem menuItem, int i) {
        menuItem.setShowAsAction(i);
    }

    public static void setGroupDividerEnabled(Menu menu, boolean z) {
        if (menu instanceof SupportMenu) {
            ((SupportMenu) menu).setGroupDividerEnabled(z);
        } else if (VERSION.SDK_INT >= 28) {
            menu.setGroupDividerEnabled(z);
        }
    }

    private MenuCompat() {
    }
}
