package net.sourceforge.opencamera;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.TwoStatePreference;
import android.support.p000v4.view.ViewCompat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import net.sourceforge.opencamera.p004ui.FolderChooserDialog;

public class MyPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    private static final String TAG = "MyPreferenceFragment";
    /* access modifiers changed from: private */
    public int cameraId;
    /* access modifiers changed from: private */
    public final HashSet<AlertDialog> dialogs = new HashSet<>();

    public static class LoadSettingsFileChooserDialog extends FolderChooserDialog {
        public void onDismiss(DialogInterface dialogInterface) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                String chosenFile = getChosenFile();
                if (chosenFile != null) {
                    mainActivity.getSettingsManager().loadSettings(chosenFile);
                }
            }
            super.onDismiss(dialogInterface);
        }
    }

    public static class SaveFolderChooserDialog extends FolderChooserDialog {
        public void onDismiss(DialogInterface dialogInterface) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.updateSaveFolder(getChosenFolder());
            }
            super.onDismiss(dialogInterface);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:114:0x0543  */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x0556  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x0563  */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x0576  */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x058e A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x05ab  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x05bd  */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x05c3  */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x05d4  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x05e5  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x05f6  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0607  */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x0618  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x062b  */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x063b  */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0656  */
    /* JADX WARNING: Removed duplicated region for block: B:155:0x066d  */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x06a1  */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x06cf  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x06ee  */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x0715  */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x0747  */
    /* JADX WARNING: Removed duplicated region for block: B:172:0x0765  */
    /* JADX WARNING: Removed duplicated region for block: B:175:0x0795  */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x07f5  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x081b  */
    /* JADX WARNING: Removed duplicated region for block: B:182:0x082b  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00ea  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0112  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0177 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0236  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x02d3 A[LOOP:2: B:67:0x02d1->B:68:0x02d3, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x031e  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x032e  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0372  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0398  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x03ca  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0443  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x0462 A[ADDED_TO_REGION] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onCreate(android.os.Bundle r72) {
        /*
            r71 = this;
            r15 = r71
            super.onCreate(r72)
            r0 = 2131689472(0x7f0f0000, float:1.900796E38)
            r15.addPreferencesFromResource(r0)
            android.os.Bundle r14 = r71.getArguments()
            java.lang.String r0 = "cameraId"
            int r0 = r14.getInt(r0)
            r15.cameraId = r0
            java.lang.String r0 = "nCameras"
            int r6 = r14.getInt(r0)
            java.lang.String r0 = "camera_api"
            java.lang.String r7 = r14.getString(r0)
            java.lang.String r0 = "using_android_l"
            boolean r53 = r14.getBoolean(r0)
            android.app.Activity r0 = r71.getActivity()
            android.content.SharedPreferences r13 = android.preference.PreferenceManager.getDefaultSharedPreferences(r0)
            java.lang.String r0 = "supports_auto_stabilise"
            boolean r27 = r14.getBoolean(r0)
            java.lang.String r0 = "supports_flash"
            boolean r8 = r14.getBoolean(r0)
            java.lang.String r0 = "antibanding"
            java.lang.String[] r1 = r14.getStringArray(r0)
            if (r1 == 0) goto L_0x0060
            int r0 = r1.length
            if (r0 <= 0) goto L_0x0060
            java.lang.String r0 = "antibanding_entries"
            java.lang.String[] r2 = r14.getStringArray(r0)
            if (r2 == 0) goto L_0x0060
            int r0 = r2.length
            int r3 = r1.length
            if (r0 != r3) goto L_0x0060
            java.lang.String r3 = "preference_antibanding"
            java.lang.String r4 = "auto"
            java.lang.String r5 = "preference_screen_processing_settings"
            r0 = r71
            r0.readFromBundle(r1, r2, r3, r4, r5)
            r0 = 1
            goto L_0x0061
        L_0x0060:
            r0 = 0
        L_0x0061:
            java.lang.String r11 = "preference_screen_processing_settings"
            if (r0 != 0) goto L_0x0074
            java.lang.String r0 = "preference_antibanding"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r1 = r15.findPreference(r11)
            android.preference.PreferenceGroup r1 = (android.preference.PreferenceGroup) r1
            r1.removePreference(r0)
        L_0x0074:
            java.lang.String r0 = "edge_modes"
            java.lang.String[] r1 = r14.getStringArray(r0)
            if (r1 == 0) goto L_0x0098
            int r0 = r1.length
            if (r0 <= 0) goto L_0x0098
            java.lang.String r0 = "edge_modes_entries"
            java.lang.String[] r2 = r14.getStringArray(r0)
            if (r2 == 0) goto L_0x0098
            int r0 = r2.length
            int r3 = r1.length
            if (r0 != r3) goto L_0x0098
            java.lang.String r3 = "preference_edge_mode"
            java.lang.String r4 = "default"
            java.lang.String r5 = "preference_screen_processing_settings"
            r0 = r71
            r0.readFromBundle(r1, r2, r3, r4, r5)
            r0 = 1
            goto L_0x0099
        L_0x0098:
            r0 = 0
        L_0x0099:
            if (r0 != 0) goto L_0x00aa
            java.lang.String r0 = "preference_edge_mode"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r1 = r15.findPreference(r11)
            android.preference.PreferenceGroup r1 = (android.preference.PreferenceGroup) r1
            r1.removePreference(r0)
        L_0x00aa:
            java.lang.String r0 = "noise_reduction_modes"
            java.lang.String[] r1 = r14.getStringArray(r0)
            if (r1 == 0) goto L_0x00ce
            int r0 = r1.length
            if (r0 <= 0) goto L_0x00ce
            java.lang.String r0 = "noise_reduction_modes_entries"
            java.lang.String[] r2 = r14.getStringArray(r0)
            if (r2 == 0) goto L_0x00ce
            int r0 = r2.length
            int r3 = r1.length
            if (r0 != r3) goto L_0x00ce
            java.lang.String r3 = "preference_noise_reduction_mode"
            java.lang.String r4 = "default"
            java.lang.String r5 = "preference_screen_processing_settings"
            r0 = r71
            r0.readFromBundle(r1, r2, r3, r4, r5)
            r0 = 1
            goto L_0x00cf
        L_0x00ce:
            r0 = 0
        L_0x00cf:
            if (r0 != 0) goto L_0x00e0
            java.lang.String r0 = "preference_noise_reduction_mode"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r1 = r15.findPreference(r11)
            android.preference.PreferenceGroup r1 = (android.preference.PreferenceGroup) r1
            r1.removePreference(r0)
        L_0x00e0:
            java.lang.String r0 = "supports_face_detection"
            boolean r28 = r14.getBoolean(r0)
            java.lang.String r0 = "preference_screen_gui"
            if (r28 != 0) goto L_0x010a
            java.lang.String r1 = "preference_face_detection"
            android.preference.Preference r1 = r15.findPreference(r1)
            java.lang.String r2 = "preference_category_camera_controls"
            android.preference.Preference r2 = r15.findPreference(r2)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r1)
            java.lang.String r1 = "preference_show_face_detection"
            android.preference.Preference r1 = r15.findPreference(r1)
            android.preference.Preference r2 = r15.findPreference(r0)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r1)
        L_0x010a:
            int r1 = android.os.Build.VERSION.SDK_INT
            r2 = 18
            java.lang.String r11 = "preference_screen_camera_controls_more"
            if (r1 >= r2) goto L_0x0121
            java.lang.String r1 = "preference_screen_remote_control"
            android.preference.Preference r1 = r15.findPreference(r1)
            android.preference.Preference r2 = r15.findPreference(r11)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r1)
        L_0x0121:
            java.lang.String r1 = "preview_width"
            int r12 = r14.getInt(r1)
            java.lang.String r1 = "preview_height"
            int r16 = r14.getInt(r1)
            java.lang.String r1 = "preview_widths"
            int[] r17 = r14.getIntArray(r1)
            java.lang.String r1 = "preview_heights"
            int[] r18 = r14.getIntArray(r1)
            java.lang.String r1 = "video_widths"
            int[] r19 = r14.getIntArray(r1)
            java.lang.String r1 = "video_heights"
            int[] r20 = r14.getIntArray(r1)
            java.lang.String r1 = "video_fps"
            int[] r1 = r14.getIntArray(r1)
            java.lang.String r2 = "video_fps_high_speed"
            boolean[] r2 = r14.getBooleanArray(r2)
            java.lang.String r3 = "resolution_width"
            int r21 = r14.getInt(r3)
            java.lang.String r3 = "resolution_height"
            int r22 = r14.getInt(r3)
            java.lang.String r3 = "resolution_widths"
            int[] r5 = r14.getIntArray(r3)
            java.lang.String r3 = "resolution_heights"
            int[] r23 = r14.getIntArray(r3)
            java.lang.String r3 = "resolution_supports_burst"
            boolean[] r24 = r14.getBooleanArray(r3)
            java.lang.String r3 = "preference_resolution"
            java.lang.String r4 = ""
            java.lang.String r9 = "preference_screen_photo_settings"
            if (r5 == 0) goto L_0x020d
            if (r23 == 0) goto L_0x020d
            if (r24 == 0) goto L_0x020d
            int r10 = r5.length
            java.lang.CharSequence[] r10 = new java.lang.CharSequence[r10]
            r26 = r12
            int r12 = r5.length
            java.lang.CharSequence[] r12 = new java.lang.CharSequence[r12]
            r30 = r6
            r29 = r7
            r7 = 0
        L_0x0188:
            int r6 = r5.length
            if (r7 >= r6) goto L_0x01e8
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r31 = r8
            r8 = r5[r7]
            r6.append(r8)
            java.lang.String r8 = " x "
            r6.append(r8)
            r8 = r23[r7]
            r6.append(r8)
            java.lang.String r8 = " "
            r6.append(r8)
            android.content.res.Resources r8 = r71.getResources()
            r32 = r0
            r0 = r5[r7]
            r33 = r11
            r11 = r23[r7]
            r34 = r14
            boolean r14 = r24[r7]
            java.lang.String r0 = net.sourceforge.opencamera.preview.Preview.getAspectRatioMPString(r8, r0, r11, r14)
            r6.append(r0)
            java.lang.String r0 = r6.toString()
            r10[r7] = r0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r6 = r5[r7]
            r0.append(r6)
            java.lang.String r6 = " "
            r0.append(r6)
            r6 = r23[r7]
            r0.append(r6)
            java.lang.String r0 = r0.toString()
            r12[r7] = r0
            int r7 = r7 + 1
            r8 = r31
            r0 = r32
            r11 = r33
            r14 = r34
            goto L_0x0188
        L_0x01e8:
            r32 = r0
            r31 = r8
            r33 = r11
            r34 = r14
            android.preference.Preference r0 = r15.findPreference(r3)
            android.preference.ListPreference r0 = (android.preference.ListPreference) r0
            r0.setEntries(r10)
            r0.setEntryValues(r12)
            int r3 = r15.cameraId
            java.lang.String r3 = net.sourceforge.opencamera.PreferenceKeys.getResolutionPreferenceKey(r3)
            java.lang.String r6 = r13.getString(r3, r4)
            r0.setValue(r6)
            r0.setKey(r3)
            goto L_0x0228
        L_0x020d:
            r32 = r0
            r30 = r6
            r29 = r7
            r31 = r8
            r33 = r11
            r26 = r12
            r34 = r14
            android.preference.Preference r0 = r15.findPreference(r3)
            android.preference.Preference r3 = r15.findPreference(r9)
            android.preference.PreferenceGroup r3 = (android.preference.PreferenceGroup) r3
            r3.removePreference(r0)
        L_0x0228:
            int r0 = r15.cameraId
            java.lang.String r0 = net.sourceforge.opencamera.PreferenceKeys.getVideoFPSPreferenceKey(r0)
            java.lang.String r3 = "default"
            java.lang.String r6 = r13.getString(r0, r3)
            if (r1 == 0) goto L_0x02ca
            int r7 = r1.length
            r8 = 1
            int r7 = r7 + r8
            java.lang.CharSequence[] r7 = new java.lang.CharSequence[r7]
            int r10 = r1.length
            int r10 = r10 + r8
            java.lang.CharSequence[] r8 = new java.lang.CharSequence[r10]
            android.content.res.Resources r10 = r71.getResources()
            r11 = 2131493492(0x7f0c0274, float:1.8610466E38)
            java.lang.String r10 = r10.getString(r11)
            r11 = 0
            r7[r11] = r10
            r8[r11] = r3
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r10 = " ["
            r3.append(r10)
            android.content.res.Resources r10 = r71.getResources()
            r11 = 2131493007(0x7f0c008f, float:1.8609482E38)
            java.lang.String r10 = r10.getString(r11)
            r3.append(r10)
            java.lang.String r10 = "]"
            r3.append(r10)
            java.lang.String r3 = r3.toString()
            r10 = 0
            r11 = 1
        L_0x0272:
            int r12 = r1.length
            if (r10 >= r12) goto L_0x02b6
            r12 = r1[r10]
            if (r2 == 0) goto L_0x028f
            boolean r14 = r2[r10]
            if (r14 == 0) goto L_0x028f
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            r14.append(r12)
            r14.append(r3)
            java.lang.String r14 = r14.toString()
            r7[r11] = r14
            goto L_0x02a0
        L_0x028f:
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            r14.append(r4)
            r14.append(r12)
            java.lang.String r14 = r14.toString()
            r7[r11] = r14
        L_0x02a0:
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            r14.append(r4)
            r14.append(r12)
            java.lang.String r12 = r14.toString()
            r8[r11] = r12
            int r11 = r11 + 1
            int r10 = r10 + 1
            goto L_0x0272
        L_0x02b6:
            java.lang.String r1 = "preference_video_fps"
            android.preference.Preference r1 = r15.findPreference(r1)
            android.preference.ListPreference r1 = (android.preference.ListPreference) r1
            r1.setEntries(r7)
            r1.setEntryValues(r8)
            r1.setValue(r6)
            r1.setKey(r0)
        L_0x02ca:
            r0 = 100
            java.lang.CharSequence[] r1 = new java.lang.CharSequence[r0]
            java.lang.CharSequence[] r2 = new java.lang.CharSequence[r0]
            r3 = 0
        L_0x02d1:
            if (r3 >= r0) goto L_0x02fe
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r6.append(r4)
            int r7 = r3 + 1
            r6.append(r7)
            java.lang.String r8 = "%"
            r6.append(r8)
            java.lang.String r6 = r6.toString()
            r1[r3] = r6
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r6.append(r4)
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            r2[r3] = r6
            r3 = r7
            goto L_0x02d1
        L_0x02fe:
            java.lang.String r0 = "preference_quality"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.ListPreference r0 = (android.preference.ListPreference) r0
            r0.setEntries(r1)
            r0.setEntryValues(r2)
            java.lang.String r0 = "supports_raw"
            r14 = r34
            boolean r34 = r14.getBoolean(r0)
            java.lang.String r0 = "supports_burst_raw"
            boolean r0 = r14.getBoolean(r0)
            r1 = 24
            if (r34 != 0) goto L_0x032e
            java.lang.String r2 = "preference_raw"
            android.preference.Preference r2 = r15.findPreference(r2)
            android.preference.Preference r3 = r15.findPreference(r9)
            android.preference.PreferenceGroup r3 = (android.preference.PreferenceGroup) r3
            r3.removePreference(r2)
            goto L_0x034e
        L_0x032e:
            java.lang.String r2 = "preference_raw"
            android.preference.Preference r2 = r15.findPreference(r2)
            android.preference.ListPreference r2 = (android.preference.ListPreference) r2
            int r3 = android.os.Build.VERSION.SDK_INT
            if (r3 >= r1) goto L_0x0346
            r3 = 2130772027(0x7f01003b, float:1.714716E38)
            r2.setEntries(r3)
            r3 = 2130772029(0x7f01003d, float:1.7147165E38)
            r2.setEntryValues(r3)
        L_0x0346:
            net.sourceforge.opencamera.MyPreferenceFragment$1 r3 = new net.sourceforge.opencamera.MyPreferenceFragment$1
            r3.<init>(r13)
            r2.setOnPreferenceChangeListener(r3)
        L_0x034e:
            if (r34 == 0) goto L_0x0352
            if (r0 != 0) goto L_0x036a
        L_0x0352:
            android.preference.Preference r0 = r15.findPreference(r9)
            android.preference.PreferenceGroup r0 = (android.preference.PreferenceGroup) r0
            java.lang.String r2 = "preference_raw_expo_bracketing"
            android.preference.Preference r2 = r15.findPreference(r2)
            r0.removePreference(r2)
            java.lang.String r2 = "preference_raw_focus_bracketing"
            android.preference.Preference r2 = r15.findPreference(r2)
            r0.removePreference(r2)
        L_0x036a:
            java.lang.String r0 = "supports_hdr"
            boolean r35 = r14.getBoolean(r0)
            if (r35 != 0) goto L_0x0390
            java.lang.String r0 = "preference_hdr_save_expo"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r2 = r15.findPreference(r9)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r0)
            java.lang.String r0 = "preference_hdr_contrast_enhancement"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r2 = r15.findPreference(r9)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r0)
        L_0x0390:
            java.lang.String r0 = "supports_panorama"
            boolean r36 = r14.getBoolean(r0)
            if (r36 != 0) goto L_0x03b6
            java.lang.String r0 = "preference_panorama_crop"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r2 = r15.findPreference(r9)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r0)
            java.lang.String r0 = "preference_panorama_save"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r2 = r15.findPreference(r9)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r0)
        L_0x03b6:
            java.lang.String r0 = "supports_expo_bracketing"
            boolean r37 = r14.getBoolean(r0)
            java.lang.String r0 = "max_expo_bracketing_n_images"
            int r0 = r14.getInt(r0)
            java.lang.String r2 = "supports_nr"
            boolean r2 = r14.getBoolean(r2)
            if (r2 != 0) goto L_0x03d9
            java.lang.String r2 = "preference_nr_save"
            android.preference.Preference r2 = r15.findPreference(r2)
            android.preference.Preference r3 = r15.findPreference(r9)
            android.preference.PreferenceGroup r3 = (android.preference.PreferenceGroup) r3
            r3.removePreference(r2)
        L_0x03d9:
            java.lang.String r2 = "supports_exposure_compensation"
            boolean r38 = r14.getBoolean(r2)
            java.lang.String r2 = "exposure_compensation_min"
            int r39 = r14.getInt(r2)
            java.lang.String r2 = "exposure_compensation_max"
            int r40 = r14.getInt(r2)
            java.lang.String r2 = "supports_iso_range"
            boolean r41 = r14.getBoolean(r2)
            java.lang.String r2 = "iso_range_min"
            int r42 = r14.getInt(r2)
            java.lang.String r2 = "iso_range_max"
            int r43 = r14.getInt(r2)
            java.lang.String r2 = "supports_exposure_time"
            boolean r44 = r14.getBoolean(r2)
            java.lang.String r2 = "exposure_time_min"
            long r45 = r14.getLong(r2)
            java.lang.String r2 = "exposure_time_max"
            long r47 = r14.getLong(r2)
            java.lang.String r2 = "supports_exposure_lock"
            boolean r2 = r14.getBoolean(r2)
            java.lang.String r3 = "supports_white_balance_lock"
            boolean r3 = r14.getBoolean(r3)
            java.lang.String r6 = "supports_white_balance_temperature"
            boolean r49 = r14.getBoolean(r6)
            java.lang.String r6 = "white_balance_temperature_min"
            int r50 = r14.getInt(r6)
            java.lang.String r6 = "white_balance_temperature_max"
            int r51 = r14.getInt(r6)
            if (r37 == 0) goto L_0x0432
            r6 = 3
            if (r0 > r6) goto L_0x0441
        L_0x0432:
            java.lang.String r0 = "preference_expo_bracketing_n_images"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r6 = r15.findPreference(r9)
            android.preference.PreferenceGroup r6 = (android.preference.PreferenceGroup) r6
            r6.removePreference(r0)
        L_0x0441:
            if (r37 != 0) goto L_0x0452
            java.lang.String r0 = "preference_expo_bracketing_stops"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r6 = r15.findPreference(r9)
            android.preference.PreferenceGroup r6 = (android.preference.PreferenceGroup) r6
            r6.removePreference(r0)
        L_0x0452:
            java.lang.String r0 = "video_quality"
            java.lang.String[] r12 = r14.getStringArray(r0)
            java.lang.String r0 = "video_quality_string"
            java.lang.String[] r0 = r14.getStringArray(r0)
            java.lang.String r6 = "preference_screen_video_settings"
            if (r12 == 0) goto L_0x04e1
            if (r0 == 0) goto L_0x04e1
            int r7 = r12.length
            java.lang.CharSequence[] r7 = new java.lang.CharSequence[r7]
            int r8 = r12.length
            java.lang.CharSequence[] r8 = new java.lang.CharSequence[r8]
            r10 = 0
        L_0x046b:
            int r11 = r12.length
            if (r10 >= r11) goto L_0x0479
            r11 = r0[r10]
            r7[r10] = r11
            r11 = r12[r10]
            r8[r10] = r11
            int r10 = r10 + 1
            goto L_0x046b
        L_0x0479:
            java.lang.String r0 = "preference_video_quality"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.ListPreference r0 = (android.preference.ListPreference) r0
            r0.setEntries(r7)
            r0.setEntryValues(r8)
            java.lang.String r7 = "video_quality_preference_key"
            java.lang.String r7 = r14.getString(r7)
            java.lang.String r4 = r13.getString(r7, r4)
            r0.setKey(r7)
            r0.setValue(r4)
            java.lang.String r4 = "video_is_high_speed"
            boolean r4 = r14.getBoolean(r4)
            if (r4 == 0) goto L_0x04cf
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            android.content.res.Resources r7 = r71.getResources()
            r8 = 2131493619(0x7f0c02f3, float:1.8610723E38)
            java.lang.String r7 = r7.getString(r8)
            r4.append(r7)
            java.lang.String r7 = " ["
            r4.append(r7)
            android.content.res.Resources r7 = r71.getResources()
            r8 = 2131493007(0x7f0c008f, float:1.8609482E38)
            java.lang.String r7 = r7.getString(r8)
            r4.append(r7)
            java.lang.String r7 = "]"
            r4.append(r7)
            java.lang.String r4 = r4.toString()
            goto L_0x04da
        L_0x04cf:
            android.content.res.Resources r4 = r71.getResources()
            r7 = 2131493619(0x7f0c02f3, float:1.8610723E38)
            java.lang.String r4 = r4.getString(r7)
        L_0x04da:
            r0.setTitle(r4)
            r0.setDialogTitle(r4)
            goto L_0x04f0
        L_0x04e1:
            java.lang.String r0 = "preference_video_quality"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r4 = r15.findPreference(r6)
            android.preference.PreferenceGroup r4 = (android.preference.PreferenceGroup) r4
            r4.removePreference(r0)
        L_0x04f0:
            java.lang.String r0 = "current_video_quality"
            java.lang.String r52 = r14.getString(r0)
            java.lang.String r0 = "video_frame_width"
            int r54 = r14.getInt(r0)
            java.lang.String r0 = "video_frame_height"
            int r55 = r14.getInt(r0)
            java.lang.String r0 = "video_bit_rate"
            int r56 = r14.getInt(r0)
            java.lang.String r0 = "video_frame_rate"
            int r57 = r14.getInt(r0)
            java.lang.String r0 = "video_capture_rate"
            double r58 = r14.getDouble(r0)
            java.lang.String r0 = "video_high_speed"
            boolean r60 = r14.getBoolean(r0)
            java.lang.String r0 = "video_capture_rate_factor"
            float r61 = r14.getFloat(r0)
            java.lang.String r0 = "supports_force_video_4k"
            boolean r0 = r14.getBoolean(r0)
            if (r0 == 0) goto L_0x052a
            if (r12 != 0) goto L_0x053b
        L_0x052a:
            java.lang.String r0 = "preference_force_video_4k"
            android.preference.Preference r0 = r15.findPreference(r0)
            java.lang.String r4 = "preference_category_video_debugging"
            android.preference.Preference r4 = r15.findPreference(r4)
            android.preference.PreferenceGroup r4 = (android.preference.PreferenceGroup) r4
            r4.removePreference(r0)
        L_0x053b:
            java.lang.String r0 = "supports_video_stabilization"
            boolean r62 = r14.getBoolean(r0)
            if (r62 != 0) goto L_0x0552
            java.lang.String r0 = "preference_video_stabilization"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r4 = r15.findPreference(r6)
            android.preference.PreferenceGroup r4 = (android.preference.PreferenceGroup) r4
            r4.removePreference(r0)
        L_0x0552:
            int r0 = android.os.Build.VERSION.SDK_INT
            if (r0 >= r1) goto L_0x055d
            java.lang.String r0 = "preference_video_output_format"
            java.lang.String r4 = "preference_video_output_format_mpeg4_hevc"
            r15.filterArrayEntry(r0, r4)
        L_0x055d:
            int r0 = android.os.Build.VERSION.SDK_INT
            r7 = 21
            if (r0 >= r7) goto L_0x056a
            java.lang.String r0 = "preference_video_output_format"
            java.lang.String r4 = "preference_video_output_format_webm"
            r15.filterArrayEntry(r0, r4)
        L_0x056a:
            java.lang.String r0 = "preference_record_audio_src"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.ListPreference r0 = (android.preference.ListPreference) r0
            int r4 = android.os.Build.VERSION.SDK_INT
            if (r4 >= r1) goto L_0x0582
            r4 = 2130772033(0x7f010041, float:1.7147173E38)
            r0.setEntries(r4)
            r4 = 2130772035(0x7f010043, float:1.7147177E38)
            r0.setEntryValues(r4)
        L_0x0582:
            java.lang.String r0 = "can_disable_shutter_sound"
            boolean r63 = r14.getBoolean(r0)
            int r0 = android.os.Build.VERSION.SDK_INT
            r4 = 17
            if (r0 < r4) goto L_0x0594
            if (r63 != 0) goto L_0x0591
            goto L_0x0594
        L_0x0591:
            r8 = r33
            goto L_0x05a5
        L_0x0594:
            java.lang.String r0 = "preference_shutter_sound"
            android.preference.Preference r0 = r15.findPreference(r0)
            r8 = r33
            android.preference.Preference r4 = r15.findPreference(r8)
            android.preference.PreferenceGroup r4 = (android.preference.PreferenceGroup) r4
            r4.removePreference(r0)
        L_0x05a5:
            int r0 = android.os.Build.VERSION.SDK_INT
            r4 = 19
            if (r0 >= r4) goto L_0x05bd
            java.lang.String r0 = "preference_immersive_mode"
            android.preference.Preference r0 = r15.findPreference(r0)
            r4 = r32
            android.preference.Preference r10 = r15.findPreference(r4)
            android.preference.PreferenceGroup r10 = (android.preference.PreferenceGroup) r10
            r10.removePreference(r0)
            goto L_0x05bf
        L_0x05bd:
            r4 = r32
        L_0x05bf:
            java.lang.String r0 = "preference_preview"
            if (r53 != 0) goto L_0x05d2
            java.lang.String r10 = "preference_focus_assist"
            android.preference.Preference r10 = r15.findPreference(r10)
            android.preference.Preference r11 = r15.findPreference(r0)
            android.preference.PreferenceGroup r11 = (android.preference.PreferenceGroup) r11
            r11.removePreference(r10)
        L_0x05d2:
            if (r31 != 0) goto L_0x05e3
            java.lang.String r10 = "preference_show_cycle_flash"
            android.preference.Preference r10 = r15.findPreference(r10)
            android.preference.Preference r11 = r15.findPreference(r4)
            android.preference.PreferenceGroup r11 = (android.preference.PreferenceGroup) r11
            r11.removePreference(r10)
        L_0x05e3:
            if (r2 != 0) goto L_0x05f4
            java.lang.String r2 = "preference_show_exposure_lock"
            android.preference.Preference r2 = r15.findPreference(r2)
            android.preference.Preference r10 = r15.findPreference(r4)
            android.preference.PreferenceGroup r10 = (android.preference.PreferenceGroup) r10
            r10.removePreference(r2)
        L_0x05f4:
            if (r34 != 0) goto L_0x0605
            java.lang.String r2 = "preference_show_cycle_raw"
            android.preference.Preference r2 = r15.findPreference(r2)
            android.preference.Preference r10 = r15.findPreference(r4)
            android.preference.PreferenceGroup r10 = (android.preference.PreferenceGroup) r10
            r10.removePreference(r2)
        L_0x0605:
            if (r3 != 0) goto L_0x0616
            java.lang.String r2 = "preference_show_white_balance_lock"
            android.preference.Preference r2 = r15.findPreference(r2)
            android.preference.Preference r3 = r15.findPreference(r4)
            android.preference.PreferenceGroup r3 = (android.preference.PreferenceGroup) r3
            r3.removePreference(r2)
        L_0x0616:
            if (r27 != 0) goto L_0x0627
            java.lang.String r2 = "preference_show_auto_level"
            android.preference.Preference r2 = r15.findPreference(r2)
            android.preference.Preference r3 = r15.findPreference(r4)
            android.preference.PreferenceGroup r3 = (android.preference.PreferenceGroup) r3
            r3.removePreference(r2)
        L_0x0627:
            int r2 = android.os.Build.VERSION.SDK_INT
            if (r2 >= r1) goto L_0x063b
            java.lang.String r1 = "preference_category_exif_tags"
            android.preference.Preference r1 = r15.findPreference(r1)
            android.preference.Preference r2 = r15.findPreference(r9)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r1)
            goto L_0x0645
        L_0x063b:
            java.lang.String r1 = "preference_exif_artist"
            r15.setSummary(r1)
            java.lang.String r1 = "preference_exif_copyright"
            r15.setSummary(r1)
        L_0x0645:
            java.lang.String r1 = "preference_save_photo_prefix"
            r15.setSummary(r1)
            java.lang.String r1 = "preference_save_video_prefix"
            r15.setSummary(r1)
            java.lang.String r1 = "preference_textstamp"
            r15.setSummary(r1)
            if (r53 != 0) goto L_0x0665
            java.lang.String r1 = "preference_show_iso"
            android.preference.Preference r1 = r15.findPreference(r1)
            android.preference.Preference r2 = r15.findPreference(r0)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r1)
        L_0x0665:
            java.lang.String r1 = "supports_preview_bitmaps"
            boolean r1 = r14.getBoolean(r1)
            if (r1 != 0) goto L_0x0697
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.PreferenceGroup r0 = (android.preference.PreferenceGroup) r0
            java.lang.String r1 = "preference_histogram"
            android.preference.Preference r1 = r15.findPreference(r1)
            r0.removePreference(r1)
            java.lang.String r1 = "preference_zebra_stripes"
            android.preference.Preference r1 = r15.findPreference(r1)
            r0.removePreference(r1)
            java.lang.String r1 = "preference_focus_peaking"
            android.preference.Preference r1 = r15.findPreference(r1)
            r0.removePreference(r1)
            java.lang.String r1 = "preference_focus_peaking_color"
            android.preference.Preference r1 = r15.findPreference(r1)
            r0.removePreference(r1)
        L_0x0697:
            java.lang.String r0 = "supports_photo_video_recording"
            boolean r0 = r14.getBoolean(r0)
            java.lang.String r1 = "preference_category_photo_debugging"
            if (r53 != 0) goto L_0x06cf
            java.lang.String r0 = "preference_camera2_fake_flash"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r2 = r15.findPreference(r1)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r0)
            java.lang.String r0 = "preference_camera2_fast_burst"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r2 = r15.findPreference(r1)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r0)
            java.lang.String r0 = "preference_camera2_photo_video_recording"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r2 = r15.findPreference(r1)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r0)
            goto L_0x06e0
        L_0x06cf:
            if (r0 != 0) goto L_0x06e0
            java.lang.String r0 = "preference_camera2_photo_video_recording"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r2 = r15.findPreference(r1)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r0)
        L_0x06e0:
            java.lang.String r0 = "tonemap_max_curve_points"
            int r64 = r14.getInt(r0)
            java.lang.String r0 = "supports_tonemap_curve"
            boolean r0 = r14.getBoolean(r0)
            if (r0 != 0) goto L_0x06fd
            java.lang.String r0 = "preference_video_log"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r2 = r15.findPreference(r6)
            android.preference.PreferenceGroup r2 = (android.preference.PreferenceGroup) r2
            r2.removePreference(r0)
        L_0x06fd:
            java.lang.String r0 = "camera_view_angle_x"
            float r65 = r14.getFloat(r0)
            java.lang.String r0 = "camera_view_angle_y"
            float r66 = r14.getFloat(r0)
            android.preference.Preference r0 = r15.findPreference(r1)
            android.preference.PreferenceGroup r0 = (android.preference.PreferenceGroup) r0
            int r1 = r0.getPreferenceCount()
            if (r1 != 0) goto L_0x071e
            android.preference.Preference r1 = r15.findPreference(r9)
            android.preference.PreferenceGroup r1 = (android.preference.PreferenceGroup) r1
            r1.removePreference(r0)
        L_0x071e:
            java.util.ArrayList r6 = new java.util.ArrayList
            r6.<init>()
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            java.lang.String r1 = "preference_camera_api_old"
            r6.add(r1)
            android.app.Activity r1 = r71.getActivity()
            android.content.res.Resources r1 = r1.getResources()
            r2 = 2131493131(0x7f0c010b, float:1.8609733E38)
            java.lang.String r1 = r1.getString(r2)
            r0.add(r1)
            java.lang.String r1 = "supports_camera2"
            boolean r1 = r14.getBoolean(r1)
            if (r1 == 0) goto L_0x075e
            java.lang.String r1 = "preference_camera_api_camera2"
            r6.add(r1)
            android.app.Activity r1 = r71.getActivity()
            android.content.res.Resources r1 = r1.getResources()
            r2 = 2131493130(0x7f0c010a, float:1.8609731E38)
            java.lang.String r1 = r1.getString(r2)
            r0.add(r1)
        L_0x075e:
            int r1 = r6.size()
            r2 = 1
            if (r1 != r2) goto L_0x076b
            r6.clear()
            r0.clear()
        L_0x076b:
            r1 = 0
            java.lang.String[] r2 = new java.lang.String[r1]
            java.lang.Object[] r2 = r6.toArray(r2)
            java.lang.String[] r2 = (java.lang.String[]) r2
            java.lang.String[] r1 = new java.lang.String[r1]
            java.lang.Object[] r0 = r0.toArray(r1)
            r3 = r0
            java.lang.String[] r3 = (java.lang.String[]) r3
            java.lang.String r4 = "preference_camera_api"
            java.lang.String r9 = "preference_camera_api_old"
            java.lang.String r10 = "preference_category_online"
            r0 = r71
            r1 = r2
            r2 = r3
            r3 = r4
            r4 = r9
            r11 = r5
            r5 = r10
            r0.readFromBundle(r1, r2, r3, r4, r5)
            int r0 = r6.size()
            r1 = 2
            if (r0 < r1) goto L_0x07a3
            java.lang.String r0 = "preference_camera_api"
            android.preference.Preference r0 = r15.findPreference(r0)
            net.sourceforge.opencamera.MyPreferenceFragment$2 r1 = new net.sourceforge.opencamera.MyPreferenceFragment$2
            r1.<init>(r0)
            r0.setOnPreferenceChangeListener(r1)
        L_0x07a3:
            java.lang.String r0 = "preference_online_help"
            android.preference.Preference r0 = r15.findPreference(r0)
            net.sourceforge.opencamera.MyPreferenceFragment$3 r1 = new net.sourceforge.opencamera.MyPreferenceFragment$3
            r1.<init>(r0)
            r0.setOnPreferenceClickListener(r1)
            java.lang.String r0 = "preference_privacy_policy"
            android.preference.Preference r0 = r15.findPreference(r0)
            net.sourceforge.opencamera.MyPreferenceFragment$4 r1 = new net.sourceforge.opencamera.MyPreferenceFragment$4
            r1.<init>(r0)
            r0.setOnPreferenceClickListener(r1)
            java.lang.String r0 = "preference_licence_open_camera"
            android.preference.Preference r0 = r15.findPreference(r0)
            net.sourceforge.opencamera.MyPreferenceFragment$5 r1 = new net.sourceforge.opencamera.MyPreferenceFragment$5
            r1.<init>(r0)
            r0.setOnPreferenceClickListener(r1)
            java.lang.String r0 = "preference_licence_google_icons"
            android.preference.Preference r0 = r15.findPreference(r0)
            net.sourceforge.opencamera.MyPreferenceFragment$6 r1 = new net.sourceforge.opencamera.MyPreferenceFragment$6
            r1.<init>(r0)
            r0.setOnPreferenceClickListener(r1)
            java.lang.String r0 = "preference_licence_online"
            android.preference.Preference r0 = r15.findPreference(r0)
            net.sourceforge.opencamera.MyPreferenceFragment$7 r1 = new net.sourceforge.opencamera.MyPreferenceFragment$7
            r1.<init>(r0)
            r0.setOnPreferenceClickListener(r1)
            java.lang.String r0 = "preference_ghost_image"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.ListPreference r0 = (android.preference.ListPreference) r0
            int r1 = android.os.Build.VERSION.SDK_INT
            if (r1 >= r7) goto L_0x0801
            r1 = 2130772001(0x7f010021, float:1.7147108E38)
            r0.setEntries(r1)
            r1 = 2130772003(0x7f010023, float:1.7147112E38)
            r0.setEntryValues(r1)
        L_0x0801:
            net.sourceforge.opencamera.MyPreferenceFragment$8 r1 = new net.sourceforge.opencamera.MyPreferenceFragment$8
            r1.<init>()
            r0.setOnPreferenceChangeListener(r1)
            java.lang.String r0 = "preference_save_location"
            android.preference.Preference r0 = r15.findPreference(r0)
            net.sourceforge.opencamera.MyPreferenceFragment$9 r1 = new net.sourceforge.opencamera.MyPreferenceFragment$9
            r1.<init>()
            r0.setOnPreferenceClickListener(r1)
            int r0 = android.os.Build.VERSION.SDK_INT
            if (r0 >= r7) goto L_0x082b
            java.lang.String r0 = "preference_using_saf"
            android.preference.Preference r0 = r15.findPreference(r0)
            android.preference.Preference r1 = r15.findPreference(r8)
            android.preference.PreferenceGroup r1 = (android.preference.PreferenceGroup) r1
            r1.removePreference(r0)
            goto L_0x0839
        L_0x082b:
            java.lang.String r0 = "preference_using_saf"
            android.preference.Preference r0 = r15.findPreference(r0)
            net.sourceforge.opencamera.MyPreferenceFragment$10 r1 = new net.sourceforge.opencamera.MyPreferenceFragment$10
            r1.<init>(r0, r13)
            r0.setOnPreferenceClickListener(r1)
        L_0x0839:
            java.lang.String r0 = "preference_calibrate_level"
            android.preference.Preference r0 = r15.findPreference(r0)
            net.sourceforge.opencamera.MyPreferenceFragment$11 r1 = new net.sourceforge.opencamera.MyPreferenceFragment$11
            r1.<init>(r0, r13)
            r0.setOnPreferenceClickListener(r1)
            java.lang.String r0 = "preference_about"
            android.preference.Preference r10 = r15.findPreference(r0)
            r2 = r10
            net.sourceforge.opencamera.MyPreferenceFragment$12 r9 = new net.sourceforge.opencamera.MyPreferenceFragment$12
            r0 = r9
            r1 = r71
            r3 = r30
            r4 = r29
            r5 = r13
            r6 = r17
            r7 = r18
            r8 = r26
            r67 = r9
            r9 = r16
            r68 = r10
            r10 = r11
            r11 = r23
            r16 = r12
            r12 = r24
            r69 = r13
            r13 = r21
            r70 = r14
            r14 = r22
            r15 = r16
            r16 = r19
            r17 = r20
            r18 = r52
            r19 = r54
            r20 = r55
            r21 = r56
            r22 = r57
            r23 = r58
            r25 = r60
            r26 = r61
            r29 = r34
            r30 = r35
            r31 = r36
            r32 = r37
            r33 = r38
            r34 = r39
            r35 = r40
            r36 = r41
            r37 = r42
            r38 = r43
            r39 = r44
            r40 = r45
            r42 = r47
            r44 = r49
            r45 = r50
            r46 = r51
            r47 = r62
            r48 = r64
            r49 = r63
            r50 = r65
            r51 = r66
            r52 = r70
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r25, r26, r27, r28, r29, r30, r31, r32, r33, r34, r35, r36, r37, r38, r39, r40, r42, r44, r45, r46, r47, r48, r49, r50, r51, r52, r53)
            r1 = r67
            r0 = r68
            r0.setOnPreferenceClickListener(r1)
            java.lang.String r0 = "preference_restore_settings"
            r1 = r71
            android.preference.Preference r0 = r1.findPreference(r0)
            net.sourceforge.opencamera.MyPreferenceFragment$13 r2 = new net.sourceforge.opencamera.MyPreferenceFragment$13
            r2.<init>(r0)
            r0.setOnPreferenceClickListener(r2)
            java.lang.String r0 = "preference_save_settings"
            android.preference.Preference r0 = r1.findPreference(r0)
            net.sourceforge.opencamera.MyPreferenceFragment$14 r2 = new net.sourceforge.opencamera.MyPreferenceFragment$14
            r2.<init>(r0)
            r0.setOnPreferenceClickListener(r2)
            java.lang.String r0 = "preference_reset"
            android.preference.Preference r0 = r1.findPreference(r0)
            net.sourceforge.opencamera.MyPreferenceFragment$15 r2 = new net.sourceforge.opencamera.MyPreferenceFragment$15
            r3 = r69
            r2.<init>(r0, r3)
            r0.setOnPreferenceClickListener(r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.MyPreferenceFragment.onCreate(android.os.Bundle):void");
    }

    public void clickedPrivacyPolicy() {
        Builder builder = new Builder(getActivity());
        builder.setTitle(C0316R.string.preference_privacy_policy);
        builder.setMessage(C0316R.string.preference_privacy_policy_text);
        builder.setPositiveButton(17039370, null);
        builder.setNegativeButton(C0316R.string.preference_privacy_policy_online, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ((MainActivity) MyPreferenceFragment.this.getActivity()).launchOnlinePrivacyPolicy();
            }
        });
        final AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                MyPreferenceFragment.this.dialogs.remove(create);
            }
        });
        create.show();
        this.dialogs.add(create);
    }

    /* access modifiers changed from: private */
    public void displayTextDialog(int i, String str) {
        try {
            Scanner useDelimiter = new Scanner(getActivity().getAssets().open(str)).useDelimiter("\\A");
            Builder builder = new Builder(getActivity());
            builder.setTitle(getActivity().getResources().getString(i));
            builder.setMessage(useDelimiter.next());
            builder.setPositiveButton(17039370, null);
            final AlertDialog create = builder.create();
            create.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    MyPreferenceFragment.this.dialogs.remove(create);
                }
            });
            create.show();
            this.dialogs.add(create);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterArrayEntry(String str, String str2) {
        ListPreference listPreference = (ListPreference) findPreference(str);
        CharSequence[] entries = listPreference.getEntries();
        CharSequence[] entryValues = listPreference.getEntryValues();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (int i = 0; i < entries.length; i++) {
            CharSequence charSequence = entryValues[i];
            if (!charSequence.equals(str2)) {
                arrayList.add(entries[i]);
                arrayList2.add(charSequence);
            }
        }
        CharSequence[] charSequenceArr = new CharSequence[arrayList.size()];
        arrayList.toArray(charSequenceArr);
        CharSequence[] charSequenceArr2 = new CharSequence[arrayList2.size()];
        arrayList2.toArray(charSequenceArr2);
        listPreference.setEntries(charSequenceArr);
        listPreference.setEntryValues(charSequenceArr2);
    }

    private void readFromBundle(String[] strArr, String[] strArr2, String str, String str2, String str3) {
        if (strArr == null || strArr.length <= 0) {
            ((PreferenceGroup) findPreference(str3)).removePreference(findPreference(str));
            return;
        }
        ListPreference listPreference = (ListPreference) findPreference(str);
        listPreference.setEntries(strArr2);
        listPreference.setEntryValues(strArr);
        listPreference.setValue(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(str, str2));
    }

    public void onResume() {
        super.onResume();
        TypedArray obtainStyledAttributes = getActivity().getTheme().obtainStyledAttributes(new int[]{16842801});
        getView().setBackgroundColor(obtainStyledAttributes.getColor(0, ViewCompat.MEASURED_STATE_MASK));
        obtainStyledAttributes.recycle();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    public void onPause() {
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        Iterator it = this.dialogs.iterator();
        while (it.hasNext()) {
            ((AlertDialog) it.next()).dismiss();
        }
        Fragment findFragmentByTag = getFragmentManager().findFragmentByTag("FOLDER_FRAGMENT");
        if (findFragmentByTag != null) {
            ((DialogFragment) findFragmentByTag).dismissAllowingStateLoss();
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        Preference findPreference = findPreference(str);
        if (findPreference instanceof TwoStatePreference) {
            ((TwoStatePreference) findPreference).setChecked(sharedPreferences.getBoolean(str, true));
        } else if (findPreference instanceof ListPreference) {
            ((ListPreference) findPreference).setValue(sharedPreferences.getString(str, BuildConfig.FLAVOR));
        }
        setSummary(str);
    }

    private void setSummary(String str) {
        Preference findPreference = findPreference(str);
        if (findPreference instanceof EditTextPreference) {
            String key = findPreference.getKey();
            String str2 = PreferenceKeys.ExifArtistPreferenceKey;
            boolean equals = key.equals(str2);
            String str3 = PreferenceKeys.TextStampPreferenceKey;
            String str4 = PreferenceKeys.ExifCopyrightPreferenceKey;
            String str5 = "preference_save_video_prefix";
            String str6 = "preference_save_photo_prefix";
            if (equals || findPreference.getKey().equals(str4) || findPreference.getKey().equals(str6) || findPreference.getKey().equals(str5) || findPreference.getKey().equals(str3)) {
                String str7 = findPreference.getKey().equals(str6) ? "IMG_" : findPreference.getKey().equals(str5) ? "VID_" : BuildConfig.FLAVOR;
                EditTextPreference editTextPreference = (EditTextPreference) findPreference;
                if (editTextPreference.getText().equals(str7)) {
                    String key2 = findPreference.getKey();
                    char c = 65535;
                    switch (key2.hashCode()) {
                        case -1785261252:
                            if (key2.equals(str4)) {
                                c = 1;
                                break;
                            }
                            break;
                        case -1635275788:
                            if (key2.equals(str5)) {
                                c = 3;
                                break;
                            }
                            break;
                        case -290882574:
                            if (key2.equals(str2)) {
                                c = 0;
                                break;
                            }
                            break;
                        case 178484829:
                            if (key2.equals(str6)) {
                                c = 2;
                                break;
                            }
                            break;
                        case 1533629522:
                            if (key2.equals(str3)) {
                                c = 4;
                                break;
                            }
                            break;
                    }
                    if (c == 0) {
                        findPreference.setSummary(C0316R.string.preference_exif_artist_summary);
                    } else if (c == 1) {
                        findPreference.setSummary(C0316R.string.preference_exif_copyright_summary);
                    } else if (c == 2) {
                        findPreference.setSummary(C0316R.string.preference_save_photo_prefix_summary);
                    } else if (c == 3) {
                        findPreference.setSummary(C0316R.string.preference_save_video_prefix_summary);
                    } else if (c == 4) {
                        findPreference.setSummary(C0316R.string.preference_textstamp_summary);
                    }
                } else {
                    findPreference.setSummary(editTextPreference.getText());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void loadSettings() {
        Builder builder = new Builder(getActivity());
        builder.setIcon(17301543);
        builder.setTitle(C0316R.string.preference_restore_settings);
        builder.setMessage(C0316R.string.preference_restore_settings_question);
        builder.setPositiveButton(17039379, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity mainActivity = (MainActivity) MyPreferenceFragment.this.getActivity();
                LoadSettingsFileChooserDialog loadSettingsFileChooserDialog = new LoadSettingsFileChooserDialog();
                loadSettingsFileChooserDialog.setShowDCIMShortcut(false);
                loadSettingsFileChooserDialog.setShowNewFolderButton(false);
                loadSettingsFileChooserDialog.setModeFolder(false);
                loadSettingsFileChooserDialog.setExtension(".xml");
                loadSettingsFileChooserDialog.setStartFolder(mainActivity.getStorageUtils().getSettingsFolder());
                loadSettingsFileChooserDialog.show(MyPreferenceFragment.this.getFragmentManager(), "FOLDER_FRAGMENT");
            }
        });
        builder.setNegativeButton(17039369, null);
        final AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                MyPreferenceFragment.this.dialogs.remove(create);
            }
        });
        create.show();
        this.dialogs.add(create);
    }
}
