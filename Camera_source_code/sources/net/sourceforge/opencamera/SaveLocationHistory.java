package net.sourceforge.opencamera;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import java.util.ArrayList;

public class SaveLocationHistory {
    private static final String TAG = "SaveLocationHistory";
    private final MainActivity main_activity;
    private final String pref_base;
    private final ArrayList<String> save_location_history = new ArrayList<>();

    SaveLocationHistory(MainActivity mainActivity, String str, String str2) {
        this.main_activity = mainActivity;
        this.pref_base = str;
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        this.save_location_history.clear();
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append("_size");
        int i = defaultSharedPreferences.getInt(sb.toString(), 0);
        for (int i2 = 0; i2 < i; i2++) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str);
            sb2.append("_");
            sb2.append(i2);
            String string = defaultSharedPreferences.getString(sb2.toString(), null);
            if (string != null) {
                this.save_location_history.add(string);
            }
        }
        updateFolderHistory(str2, false);
    }

    /* access modifiers changed from: 0000 */
    public void updateFolderHistory(String str, boolean z) {
        updateFolderHistory(str);
        if (z) {
            this.main_activity.updateGalleryIcon();
        }
    }

    private void updateFolderHistory(String str) {
        do {
        } while (this.save_location_history.remove(str));
        this.save_location_history.add(str);
        while (this.save_location_history.size() > 6) {
            this.save_location_history.remove(0);
        }
        writeSaveLocations();
    }

    /* access modifiers changed from: 0000 */
    public void clearFolderHistory(String str) {
        this.save_location_history.clear();
        updateFolderHistory(str, true);
    }

    private void writeSaveLocations() {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(this.main_activity).edit();
        StringBuilder sb = new StringBuilder();
        sb.append(this.pref_base);
        sb.append("_size");
        edit.putInt(sb.toString(), this.save_location_history.size());
        for (int i = 0; i < this.save_location_history.size(); i++) {
            String str = (String) this.save_location_history.get(i);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(this.pref_base);
            sb2.append("_");
            sb2.append(i);
            edit.putString(sb2.toString(), str);
        }
        edit.apply();
    }

    public int size() {
        return this.save_location_history.size();
    }

    public String get(int i) {
        return (String) this.save_location_history.get(i);
    }

    public boolean contains(String str) {
        return this.save_location_history.contains(str);
    }
}
