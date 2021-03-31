package net.sourceforge.opencamera.p004ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;
import java.util.Map;
import net.sourceforge.opencamera.C0316R;
import net.sourceforge.opencamera.MainActivity;

/* renamed from: net.sourceforge.opencamera.ui.PopupView */
public class PopupView extends LinearLayout {
    public static final float ALPHA_BUTTON = 0.6f;
    public static final float ALPHA_BUTTON_SELECTED = 1.0f;
    private static final String TAG = "PopupView";
    private static final float arrow_button_h_dp = 48.0f;
    private static final float arrow_button_w_dp = 60.0f;
    private static final float arrow_text_size_dip = 16.0f;
    private static final float button_text_size_dip = 12.0f;
    private static final float standard_text_size_dip = 16.0f;
    private static final float title_text_size_dip = 17.0f;
    private final int arrow_button_h;
    private final int arrow_button_w;
    /* access modifiers changed from: private */
    public int burst_n_images_index = -1;
    /* access modifiers changed from: private */
    public int grid_index = -1;
    /* access modifiers changed from: private */
    public int nr_mode_index = -1;
    /* access modifiers changed from: private */
    public int picture_size_index = -1;
    /* access modifiers changed from: private */
    public int repeat_mode_index = -1;
    /* access modifiers changed from: private */
    public int timer_index = -1;
    private int total_width_dp;
    /* access modifiers changed from: private */
    public int video_capture_rate_index = -1;
    /* access modifiers changed from: private */
    public int video_size_index = -1;

    /* renamed from: net.sourceforge.opencamera.ui.PopupView$ArrayOptionsPopupListener */
    private abstract class ArrayOptionsPopupListener {
        /* access modifiers changed from: protected */
        public abstract int onClickNext();

        /* access modifiers changed from: protected */
        public abstract int onClickPrev();

        private ArrayOptionsPopupListener() {
        }
    }

    /* renamed from: net.sourceforge.opencamera.ui.PopupView$ButtonOptionsPopupListener */
    static abstract class ButtonOptionsPopupListener {
        public abstract void onClick(String str);

        ButtonOptionsPopupListener() {
        }
    }

    /* renamed from: net.sourceforge.opencamera.ui.PopupView$RadioOptionsListener */
    private abstract class RadioOptionsListener {
        /* access modifiers changed from: protected */
        public abstract void onClick(String str);

        private RadioOptionsListener() {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:114:0x03c6  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x045c  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0558  */
    /* JADX WARNING: Removed duplicated region for block: B:164:0x05d5  */
    /* JADX WARNING: Removed duplicated region for block: B:236:0x022a A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00f1  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0108  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x015b  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0174  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x018d  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x01a6  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x01bf  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x01d8  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x01f1  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0224  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x022f  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0231  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x026a  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x02df  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x033a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public PopupView(android.content.Context r26) {
        /*
            r25 = this;
            r11 = r25
            r25.<init>(r26)
            r10 = -1
            r11.picture_size_index = r10
            r11.nr_mode_index = r10
            r11.burst_n_images_index = r10
            r11.video_size_index = r10
            r11.video_capture_rate_index = r10
            r11.timer_index = r10
            r11.repeat_mode_index = r10
            r11.grid_index = r10
            java.lang.System.nanoTime()
            r12 = 1
            r11.setOrientation(r12)
            android.content.res.Resources r0 = r25.getResources()
            android.util.DisplayMetrics r0 = r0.getDisplayMetrics()
            float r0 = r0.density
            r1 = 1114636288(0x42700000, float:60.0)
            float r1 = r1 * r0
            r13 = 1056964608(0x3f000000, float:0.5)
            float r1 = r1 + r13
            int r1 = (int) r1
            r11.arrow_button_w = r1
            r1 = 1111490560(0x42400000, float:48.0)
            float r1 = r1 * r0
            float r1 = r1 + r13
            int r1 = (int) r1
            r11.arrow_button_h = r1
            android.content.Context r1 = r25.getContext()
            r14 = r1
            net.sourceforge.opencamera.MainActivity r14 = (net.sourceforge.opencamera.MainActivity) r14
            r1 = 280(0x118, float:3.92E-43)
            r11.total_width_dp = r1
            net.sourceforge.opencamera.ui.MainUI r1 = r14.getMainUI()
            r15 = 0
            int r1 = r1.getMaxHeightDp(r15)
            int r2 = r11.total_width_dp
            if (r2 <= r1) goto L_0x0056
            r11.total_width_dp = r1
            r16 = 1
            goto L_0x0058
        L_0x0056:
            r16 = 0
        L_0x0058:
            net.sourceforge.opencamera.preview.Preview r9 = r14.getPreview()
            net.sourceforge.opencamera.ui.MainUI r1 = r14.getMainUI()
            boolean r1 = r1.showCycleFlashIcon()
            if (r1 != 0) goto L_0x00c5
            java.util.List r1 = r9.getSupportedFlashValues()
            boolean r2 = r9.isVideo()
            if (r2 == 0) goto L_0x0091
            if (r1 == 0) goto L_0x0091
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            java.util.Iterator r1 = r1.iterator()
        L_0x007b:
            boolean r3 = r1.hasNext()
            if (r3 == 0) goto L_0x0092
            java.lang.Object r3 = r1.next()
            java.lang.String r3 = (java.lang.String) r3
            boolean r4 = net.sourceforge.opencamera.preview.Preview.isFlashSupportedForVideo(r3)
            if (r4 == 0) goto L_0x007b
            r2.add(r3)
            goto L_0x007b
        L_0x0091:
            r2 = r1
        L_0x0092:
            if (r2 == 0) goto L_0x00c5
            int r1 = r2.size()
            if (r1 <= r12) goto L_0x00c5
            r3 = 2130771969(0x7f010001, float:1.7147043E38)
            r4 = 2130771970(0x7f010002, float:1.7147045E38)
            android.content.res.Resources r1 = r25.getResources()
            r5 = 2131492989(0x7f0c007d, float:1.8609445E38)
            java.lang.String r5 = r1.getString(r5)
            java.lang.String r6 = r9.getCurrentFlashValue()
            r7 = 0
            net.sourceforge.opencamera.ui.PopupView$1 r8 = new net.sourceforge.opencamera.ui.PopupView$1
            r8.<init>(r9, r14)
            java.lang.String r17 = "TEST_FLASH"
            r1 = r25
            r18 = r8
            r8 = r17
            r26 = r9
            r9 = r18
            r1.addButtonOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9)
            goto L_0x00c7
        L_0x00c5:
            r26 = r9
        L_0x00c7:
            boolean r1 = r26.isVideo()
            if (r1 == 0) goto L_0x00d5
            boolean r1 = r26.isVideoRecording()
            if (r1 == 0) goto L_0x00d5
            goto L_0x0886
        L_0x00d5:
            java.util.List r1 = r26.getSupportedFocusValues()
            net.sourceforge.opencamera.MyApplicationInterface r2 = r14.getApplicationInterface()
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r9 = r2.getPhotoMode()
            boolean r2 = r26.isVideo()
            r17 = 0
            if (r2 != 0) goto L_0x00ef
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r2 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.FocusBracketing
            if (r9 != r2) goto L_0x00ef
            r1 = r17
        L_0x00ef:
            if (r1 == 0) goto L_0x0108
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>(r1)
            boolean r1 = r26.isVideo()
            if (r1 == 0) goto L_0x0102
            java.lang.String r1 = "focus_mode_continuous_picture"
            r2.remove(r1)
            goto L_0x0109
        L_0x0102:
            java.lang.String r1 = "focus_mode_continuous_video"
            r2.remove(r1)
            goto L_0x0109
        L_0x0108:
            r2 = r1
        L_0x0109:
            r3 = 2130771972(0x7f010004, float:1.714705E38)
            r4 = 2130771973(0x7f010005, float:1.7147051E38)
            android.content.res.Resources r1 = r25.getResources()
            r5 = 2131492998(0x7f0c0086, float:1.8609464E38)
            java.lang.String r5 = r1.getString(r5)
            java.lang.String r6 = r26.getCurrentFocusValue()
            r7 = 0
            net.sourceforge.opencamera.ui.PopupView$2 r8 = new net.sourceforge.opencamera.ui.PopupView$2
            r1 = r26
            r8.<init>(r1, r14)
            java.lang.String r18 = "TEST_FOCUS"
            r1 = r25
            r19 = r8
            r8 = r18
            r13 = r9
            r9 = r19
            r1.addButtonOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9)
            android.content.SharedPreferences r9 = android.preference.PreferenceManager.getDefaultSharedPreferences(r14)
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            android.content.res.Resources r3 = r25.getResources()
            r4 = 2131493062(0x7f0c00c6, float:1.8609594E38)
            java.lang.String r3 = r3.getString(r4)
            r2.add(r3)
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r3 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Standard
            r1.add(r3)
            boolean r3 = r14.supportsNoiseReduction()
            if (r3 == 0) goto L_0x016e
            android.content.res.Resources r3 = r25.getResources()
            r4 = 2131493058(0x7f0c00c2, float:1.8609585E38)
            java.lang.String r3 = r3.getString(r4)
            r2.add(r3)
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r3 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.NoiseReduction
            r1.add(r3)
        L_0x016e:
            boolean r3 = r14.supportsDRO()
            if (r3 == 0) goto L_0x0187
            android.content.res.Resources r3 = r25.getResources()
            r4 = 2131493050(0x7f0c00ba, float:1.860957E38)
            java.lang.String r3 = r3.getString(r4)
            r2.add(r3)
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r3 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.DRO
            r1.add(r3)
        L_0x0187:
            boolean r3 = r14.supportsHDR()
            if (r3 == 0) goto L_0x01a0
            android.content.res.Resources r3 = r25.getResources()
            r4 = 2131493057(0x7f0c00c1, float:1.8609583E38)
            java.lang.String r3 = r3.getString(r4)
            r2.add(r3)
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r3 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.HDR
            r1.add(r3)
        L_0x01a0:
            boolean r3 = r14.supportsPanorama()
            if (r3 == 0) goto L_0x01b9
            android.content.res.Resources r3 = r25.getResources()
            r4 = 2131493060(0x7f0c00c4, float:1.860959E38)
            java.lang.String r3 = r3.getString(r4)
            r2.add(r3)
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r3 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            r1.add(r3)
        L_0x01b9:
            boolean r3 = r14.supportsFastBurst()
            if (r3 == 0) goto L_0x01d2
            android.content.res.Resources r3 = r25.getResources()
            r4 = 2131493053(0x7f0c00bd, float:1.8609575E38)
            java.lang.String r3 = r3.getString(r4)
            r2.add(r3)
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r3 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.FastBurst
            r1.add(r3)
        L_0x01d2:
            boolean r3 = r14.supportsExpoBracketing()
            if (r3 == 0) goto L_0x01eb
            android.content.res.Resources r3 = r25.getResources()
            r4 = 2131493051(0x7f0c00bb, float:1.8609571E38)
            java.lang.String r3 = r3.getString(r4)
            r2.add(r3)
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r3 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.ExpoBracketing
            r1.add(r3)
        L_0x01eb:
            boolean r3 = r14.supportsFocusBracketing()
            if (r3 == 0) goto L_0x0204
            android.content.res.Resources r3 = r25.getResources()
            r4 = 2131493055(0x7f0c00bf, float:1.860958E38)
            java.lang.String r3 = r3.getString(r4)
            r2.add(r3)
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r3 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.FocusBracketing
            r1.add(r3)
        L_0x0204:
            boolean r3 = r26.isVideo()
            java.lang.String r8 = ""
            if (r3 == 0) goto L_0x020d
            goto L_0x025e
        L_0x020d:
            int r3 = r2.size()
            if (r3 <= r12) goto L_0x025e
            r4 = r17
            r3 = 0
        L_0x0216:
            int r5 = r2.size()
            if (r3 >= r5) goto L_0x022d
            if (r4 != 0) goto L_0x022d
            java.lang.Object r5 = r1.get(r3)
            if (r5 != r13) goto L_0x022a
            java.lang.Object r4 = r2.get(r3)
            java.lang.String r4 = (java.lang.String) r4
        L_0x022a:
            int r3 = r3 + 1
            goto L_0x0216
        L_0x022d:
            if (r4 != 0) goto L_0x0231
            r6 = r8
            goto L_0x0232
        L_0x0231:
            r6 = r4
        L_0x0232:
            android.content.res.Resources r3 = r25.getResources()
            r4 = 2131493049(0x7f0c00b9, float:1.8609567E38)
            java.lang.String r3 = r3.getString(r4)
            r11.addTitleToPopup(r3)
            r3 = -1
            r4 = -1
            r7 = 4
            net.sourceforge.opencamera.ui.PopupView$4 r5 = new net.sourceforge.opencamera.ui.PopupView$4
            r5.<init>(r2, r1)
            java.lang.String r19 = ""
            java.lang.String r20 = "TEST_PHOTO_MODE"
            r1 = r25
            r21 = r5
            r5 = r19
            r22 = r8
            r8 = r20
            r23 = r9
            r9 = r21
            r1.addButtonOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9)
            goto L_0x0262
        L_0x025e:
            r22 = r8
            r23 = r9
        L_0x0262:
            boolean r1 = r26.isVideo()
            java.lang.String r9 = "PopupView"
            if (r1 != 0) goto L_0x02d6
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.NoiseReduction
            if (r13 != r1) goto L_0x02d6
            android.content.res.Resources r1 = r25.getResources()
            r2 = 2130772017(0x7f010031, float:1.714714E38)
            java.lang.String[] r1 = r1.getStringArray(r2)
            android.content.res.Resources r2 = r25.getResources()
            r3 = 2130772016(0x7f010030, float:1.7147139E38)
            java.lang.String[] r2 = r2.getStringArray(r3)
            int r3 = r1.length
            int r4 = r2.length
            if (r3 != r4) goto L_0x02ca
            net.sourceforge.opencamera.MyApplicationInterface r3 = r14.getApplicationInterface()
            java.lang.String r3 = r3.getNRMode()
            java.util.List r4 = java.util.Arrays.asList(r1)
            int r3 = r4.indexOf(r3)
            r11.nr_mode_index = r3
            int r3 = r11.nr_mode_index
            if (r3 != r10) goto L_0x02a0
            r11.nr_mode_index = r15
        L_0x02a0:
            java.util.List r2 = java.util.Arrays.asList(r2)
            android.content.res.Resources r3 = r25.getResources()
            r4 = 2131493272(0x7f0c0198, float:1.861002E38)
            java.lang.String r3 = r3.getString(r4)
            r4 = 1
            r5 = 1
            int r6 = r11.nr_mode_index
            r7 = 0
            net.sourceforge.opencamera.ui.PopupView$5 r8 = new net.sourceforge.opencamera.ui.PopupView$5
            r15 = r26
            r8.<init>(r1, r14, r15)
            java.lang.String r20 = "NR_MODE"
            r1 = r25
            r21 = r8
            r8 = r20
            r10 = r9
            r9 = r21
            r1.addArrayOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9)
            goto L_0x02d9
        L_0x02ca:
            r10 = r9
            java.lang.String r0 = "preference_nr_mode_values and preference_nr_mode_entries are different lengths"
            android.util.Log.e(r10, r0)
            java.lang.RuntimeException r0 = new java.lang.RuntimeException
            r0.<init>()
            throw r0
        L_0x02d6:
            r15 = r26
            r10 = r9
        L_0x02d9:
            boolean r1 = r14.supportsAutoStabilise()
            if (r1 == 0) goto L_0x0332
            net.sourceforge.opencamera.ui.MainUI r1 = r14.getMainUI()
            boolean r1 = r1.showAutoLevelIcon()
            if (r1 != 0) goto L_0x0332
            android.widget.CheckBox r1 = new android.widget.CheckBox
            r1.<init>(r14)
            android.content.res.Resources r2 = r25.getResources()
            r3 = 2131493081(0x7f0c00d9, float:1.8609632E38)
            java.lang.String r2 = r2.getString(r3)
            r1.setText(r2)
            r2 = 1098907648(0x41800000, float:16.0)
            r1.setTextSize(r12, r2)
            r2 = -1
            r1.setTextColor(r2)
            android.widget.LinearLayout$LayoutParams r3 = new android.widget.LinearLayout$LayoutParams
            r3.<init>(r2, r2)
            r2 = 1092616192(0x41200000, float:10.0)
            float r2 = r2 * r0
            r4 = 1056964608(0x3f000000, float:0.5)
            float r2 = r2 + r4
            int r2 = (int) r2
            r4 = 0
            r3.setMargins(r2, r4, r4, r4)
            r1.setLayoutParams(r3)
            java.lang.String r2 = "preference_auto_stabilise"
            r9 = r23
            boolean r2 = r9.getBoolean(r2, r4)
            if (r2 == 0) goto L_0x0326
            r1.setChecked(r2)
        L_0x0326:
            net.sourceforge.opencamera.ui.PopupView$6 r2 = new net.sourceforge.opencamera.ui.PopupView$6
            r2.<init>(r14)
            r1.setOnCheckedChangeListener(r2)
            r11.addView(r1)
            goto L_0x0334
        L_0x0332:
            r9 = r23
        L_0x0334:
            boolean r1 = r15.isVideo()
            if (r1 != 0) goto L_0x03be
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r13 == r1) goto L_0x03be
            java.util.ArrayList r1 = new java.util.ArrayList
            java.util.List r2 = r15.getSupportedPictureSizes(r12)
            r1.<init>(r2)
            java.util.Collections.reverse(r1)
            r2 = -1
            r11.picture_size_index = r2
            net.sourceforge.opencamera.cameracontroller.CameraController$Size r2 = r15.getCurrentPictureSize()
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            r4 = 0
        L_0x0357:
            int r5 = r1.size()
            if (r4 >= r5) goto L_0x0389
            java.lang.Object r5 = r1.get(r4)
            net.sourceforge.opencamera.cameracontroller.CameraController$Size r5 = (net.sourceforge.opencamera.cameracontroller.CameraController.Size) r5
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            int r7 = r5.width
            r6.append(r7)
            java.lang.String r7 = " x "
            r6.append(r7)
            int r7 = r5.height
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            r3.add(r6)
            boolean r5 = r5.equals(r2)
            if (r5 == 0) goto L_0x0386
            r11.picture_size_index = r4
        L_0x0386:
            int r4 = r4 + 1
            goto L_0x0357
        L_0x0389:
            int r2 = r11.picture_size_index
            r4 = -1
            if (r2 != r4) goto L_0x0393
            java.lang.String r2 = "couldn't find index of current picture size"
            android.util.Log.e(r10, r2)
        L_0x0393:
            android.content.res.Resources r2 = r25.getResources()
            r4 = 2131493329(0x7f0c01d1, float:1.8610135E38)
            java.lang.String r4 = r2.getString(r4)
            r5 = 0
            r6 = 0
            int r7 = r11.picture_size_index
            r8 = 0
            net.sourceforge.opencamera.ui.PopupView$7 r2 = new net.sourceforge.opencamera.ui.PopupView$7
            r2.<init>(r14, r1, r15)
            java.lang.String r20 = "PHOTO_RESOLUTIONS"
            r1 = r25
            r21 = r2
            r2 = r3
            r3 = r4
            r4 = r5
            r5 = r6
            r6 = r7
            r7 = r8
            r8 = r20
            r24 = r9
            r9 = r21
            r1.addArrayOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9)
            goto L_0x03c0
        L_0x03be:
            r24 = r9
        L_0x03c0:
            boolean r1 = r15.isVideo()
            if (r1 == 0) goto L_0x0456
            net.sourceforge.opencamera.MyApplicationInterface r1 = r14.getApplicationInterface()
            java.lang.String r1 = r1.getVideoFPSPref()
            java.util.List r1 = r15.getSupportedVideoQuality(r1)
            int r2 = r1.size()
            if (r2 != 0) goto L_0x03e5
            java.lang.String r1 = "can't find any supported video sizes for current fps!"
            android.util.Log.e(r10, r1)
            net.sourceforge.opencamera.preview.VideoQualityHandler r1 = r15.getVideoQualityHander()
            java.util.List r1 = r1.getSupportedVideoQuality()
        L_0x03e5:
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>(r1)
            java.util.Collections.reverse(r2)
            int r1 = r2.size()
            int r1 = r1 - r12
            r11.video_size_index = r1
            r1 = 0
        L_0x03f5:
            int r3 = r2.size()
            if (r1 >= r3) goto L_0x0415
            java.lang.Object r3 = r2.get(r1)
            java.lang.String r3 = (java.lang.String) r3
            net.sourceforge.opencamera.preview.VideoQualityHandler r4 = r15.getVideoQualityHander()
            java.lang.String r4 = r4.getCurrentVideoQuality()
            boolean r3 = r3.equals(r4)
            if (r3 == 0) goto L_0x0412
            r11.video_size_index = r1
            goto L_0x0415
        L_0x0412:
            int r1 = r1 + 1
            goto L_0x03f5
        L_0x0415:
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            java.util.Iterator r1 = r2.iterator()
        L_0x041e:
            boolean r4 = r1.hasNext()
            if (r4 == 0) goto L_0x0432
            java.lang.Object r4 = r1.next()
            java.lang.String r4 = (java.lang.String) r4
            java.lang.String r4 = r15.getCamcorderProfileDescriptionShort(r4)
            r3.add(r4)
            goto L_0x041e
        L_0x0432:
            android.content.res.Resources r1 = r25.getResources()
            r4 = 2131493619(0x7f0c02f3, float:1.8610723E38)
            java.lang.String r4 = r1.getString(r4)
            r5 = 0
            r6 = 0
            int r7 = r11.video_size_index
            r8 = 0
            net.sourceforge.opencamera.ui.PopupView$8 r9 = new net.sourceforge.opencamera.ui.PopupView$8
            r9.<init>(r14, r2, r15)
            java.lang.String r20 = "VIDEO_RESOLUTIONS"
            r1 = r25
            r2 = r3
            r3 = r4
            r4 = r5
            r5 = r6
            r6 = r7
            r7 = r8
            r8 = r20
            r1.addArrayOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9)
        L_0x0456:
            boolean r1 = r15.isVideo()
            if (r1 != 0) goto L_0x0532
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.FastBurst
            if (r13 != r1) goto L_0x0532
            android.content.res.Resources r0 = r25.getResources()
            r1 = 2130771991(0x7f010017, float:1.7147088E38)
            java.lang.String[] r1 = r0.getStringArray(r1)
            android.content.res.Resources r0 = r25.getResources()
            r2 = 2130771990(0x7f010016, float:1.7147086E38)
            java.lang.String[] r2 = r0.getStringArray(r2)
            int r0 = r1.length
            int r3 = r2.length
            if (r0 != r3) goto L_0x0527
            net.sourceforge.opencamera.MyApplicationInterface r0 = r14.getApplicationInterface()
            net.sourceforge.opencamera.ImageSaver r0 = r0.getImageSaver()
            int r0 = r0.getQueueSize()
            int r0 = r0 + r12
            r3 = 2
            int r3 = java.lang.Math.max(r3, r0)
            java.util.ArrayList r4 = new java.util.ArrayList
            r4.<init>()
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            r6 = 0
        L_0x0497:
            int r0 = r1.length
            if (r6 >= r0) goto L_0x04d3
            r0 = r1[r6]     // Catch:{ NumberFormatException -> 0x04ae }
            int r0 = java.lang.Integer.parseInt(r0)     // Catch:{ NumberFormatException -> 0x04ae }
            if (r0 <= r3) goto L_0x04a3
            goto L_0x04d0
        L_0x04a3:
            r0 = r1[r6]
            r4.add(r0)
            r0 = r2[r6]
            r5.add(r0)
            goto L_0x04d0
        L_0x04ae:
            r0 = move-exception
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "failed to parse "
            r7.append(r8)
            r7.append(r6)
            java.lang.String r8 = "th preference_fast_burst_n_images_values value: "
            r7.append(r8)
            r8 = r1[r6]
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            android.util.Log.e(r10, r7)
            r0.printStackTrace()
        L_0x04d0:
            int r6 = r6 + 1
            goto L_0x0497
        L_0x04d3:
            r6 = 0
            java.lang.String[] r0 = new java.lang.String[r6]
            java.lang.Object[] r0 = r4.toArray(r0)
            java.lang.String[] r0 = (java.lang.String[]) r0
            java.lang.String[] r1 = new java.lang.String[r6]
            java.lang.Object[] r1 = r5.toArray(r1)
            java.lang.String[] r1 = (java.lang.String[]) r1
            java.lang.String r2 = "preference_fast_burst_n_images"
            java.lang.String r3 = "5"
            r9 = r24
            java.lang.String r2 = r9.getString(r2, r3)
            java.util.List r3 = java.util.Arrays.asList(r0)
            int r2 = r3.indexOf(r2)
            r11.burst_n_images_index = r2
            int r2 = r11.burst_n_images_index
            r3 = -1
            if (r2 != r3) goto L_0x04ff
            r11.burst_n_images_index = r6
        L_0x04ff:
            java.util.List r2 = java.util.Arrays.asList(r1)
            android.content.res.Resources r1 = r25.getResources()
            r3 = 2131493175(0x7f0c0137, float:1.8609823E38)
            java.lang.String r3 = r1.getString(r3)
            r4 = 1
            r5 = 0
            int r6 = r11.burst_n_images_index
            r7 = 0
            net.sourceforge.opencamera.ui.PopupView$9 r8 = new net.sourceforge.opencamera.ui.PopupView$9
            r8.<init>(r0, r14, r15)
            java.lang.String r0 = "FAST_BURST_N_IMAGES"
            r1 = r25
            r18 = r8
            r8 = r0
            r12 = r9
            r9 = r18
            r1.addArrayOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9)
            goto L_0x05e0
        L_0x0527:
            java.lang.String r0 = "preference_fast_burst_n_images_values and preference_fast_burst_n_images_entries are different lengths"
            android.util.Log.e(r10, r0)
            java.lang.RuntimeException r0 = new java.lang.RuntimeException
            r0.<init>()
            throw r0
        L_0x0532:
            r12 = r24
            boolean r1 = r15.isVideo()
            if (r1 != 0) goto L_0x05e0
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r1 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.FocusBracketing
            if (r13 != r1) goto L_0x05e0
            android.content.res.Resources r1 = r25.getResources()
            r2 = 2130771995(0x7f01001b, float:1.7147096E38)
            java.lang.String[] r1 = r1.getStringArray(r2)
            android.content.res.Resources r2 = r25.getResources()
            r3 = 2130771994(0x7f01001a, float:1.7147094E38)
            java.lang.String[] r2 = r2.getStringArray(r3)
            int r3 = r1.length
            int r4 = r2.length
            if (r3 != r4) goto L_0x05d5
            java.lang.String r3 = "preference_focus_bracketing_n_images"
            java.lang.String r4 = "3"
            java.lang.String r3 = r12.getString(r3, r4)
            java.util.List r4 = java.util.Arrays.asList(r1)
            int r3 = r4.indexOf(r3)
            r11.burst_n_images_index = r3
            int r3 = r11.burst_n_images_index
            r4 = -1
            if (r3 != r4) goto L_0x0572
            r3 = 0
            r11.burst_n_images_index = r3
        L_0x0572:
            java.util.List r2 = java.util.Arrays.asList(r2)
            android.content.res.Resources r3 = r25.getResources()
            r4 = 2131493191(0x7f0c0147, float:1.8609855E38)
            java.lang.String r3 = r3.getString(r4)
            r4 = 1
            r5 = 0
            int r6 = r11.burst_n_images_index
            r7 = 0
            net.sourceforge.opencamera.ui.PopupView$10 r9 = new net.sourceforge.opencamera.ui.PopupView$10
            r9.<init>(r1, r14, r15)
            java.lang.String r8 = "FOCUS_BRACKETING_N_IMAGES"
            r1 = r25
            r1.addArrayOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9)
            android.widget.Switch r1 = new android.widget.Switch
            r1.<init>(r14)
            android.content.res.Resources r2 = r25.getResources()
            r3 = 2131492994(0x7f0c0082, float:1.8609456E38)
            java.lang.String r2 = r2.getString(r3)
            r1.setText(r2)
            r2 = 5
            r1.setGravity(r2)
            android.widget.LinearLayout$LayoutParams r2 = new android.widget.LinearLayout$LayoutParams
            r3 = -1
            r2.<init>(r3, r3)
            r3 = 1101004800(0x41a00000, float:20.0)
            float r0 = r0 * r3
            r3 = 1056964608(0x3f000000, float:0.5)
            float r0 = r0 + r3
            int r0 = (int) r0
            r3 = 0
            r2.setMargins(r3, r3, r0, r3)
            r1.setLayoutParams(r2)
            java.lang.String r0 = "preference_focus_bracketing_add_infinity"
            boolean r0 = r12.getBoolean(r0, r3)
            if (r0 == 0) goto L_0x05c9
            r1.setChecked(r0)
        L_0x05c9:
            net.sourceforge.opencamera.ui.PopupView$11 r0 = new net.sourceforge.opencamera.ui.PopupView$11
            r0.<init>(r14, r15)
            r1.setOnCheckedChangeListener(r0)
            r11.addView(r1)
            goto L_0x05e0
        L_0x05d5:
            java.lang.String r0 = "preference_focus_bracketing_n_images_values and preference_focus_bracketing_n_images_entries are different lengths"
            android.util.Log.e(r10, r0)
            java.lang.RuntimeException r0 = new java.lang.RuntimeException
            r0.<init>()
            throw r0
        L_0x05e0:
            boolean r0 = r15.isVideo()
            if (r0 == 0) goto L_0x06a9
            net.sourceforge.opencamera.MyApplicationInterface r0 = r14.getApplicationInterface()
            java.util.List r4 = r0.getSupportedVideoCaptureRates()
            int r0 = r4.size()
            r1 = 1
            if (r0 <= r1) goto L_0x06a9
            int r0 = r15.getCameraId()
            java.lang.String r0 = net.sourceforge.opencamera.PreferenceKeys.getVideoCaptureRatePreferenceKey(r0)
            r1 = 1065353216(0x3f800000, float:1.0)
            float r0 = r12.getFloat(r0, r1)
            java.util.ArrayList r7 = new java.util.ArrayList
            r7.<init>()
            r2 = 0
            r3 = -1
        L_0x060a:
            int r5 = r4.size()
            if (r2 >= r5) goto L_0x0667
            java.lang.Object r5 = r4.get(r2)
            java.lang.Float r5 = (java.lang.Float) r5
            float r5 = r5.floatValue()
            float r6 = r1 - r5
            float r6 = java.lang.Math.abs(r6)
            double r8 = (double) r6
            r23 = 4532020583610935537(0x3ee4f8b588e368f1, double:1.0E-5)
            int r6 = (r8 > r23 ? 1 : (r8 == r23 ? 0 : -1))
            if (r6 >= 0) goto L_0x063c
            android.content.res.Resources r3 = r25.getResources()
            r6 = 2131493488(0x7f0c0270, float:1.8610458E38)
            java.lang.String r3 = r3.getString(r6)
            r7.add(r3)
            r3 = r2
            r8 = r22
            goto L_0x0655
        L_0x063c:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r8 = r22
            r6.append(r8)
            r6.append(r5)
            java.lang.String r9 = "x"
            r6.append(r9)
            java.lang.String r6 = r6.toString()
            r7.add(r6)
        L_0x0655:
            float r5 = r0 - r5
            float r5 = java.lang.Math.abs(r5)
            double r5 = (double) r5
            int r9 = (r5 > r23 ? 1 : (r5 == r23 ? 0 : -1))
            if (r9 >= 0) goto L_0x0662
            r11.video_capture_rate_index = r2
        L_0x0662:
            int r2 = r2 + 1
            r22 = r8
            goto L_0x060a
        L_0x0667:
            int r0 = r11.video_capture_rate_index
            r1 = -1
            if (r0 != r1) goto L_0x067a
            r11.video_capture_rate_index = r3
            int r0 = r11.video_capture_rate_index
            if (r0 != r1) goto L_0x067a
            java.lang.String r0 = "can't find capture_rate_std_index"
            android.util.Log.e(r10, r0)
            r1 = 0
            r11.video_capture_rate_index = r1
        L_0x067a:
            android.content.res.Resources r0 = r25.getResources()
            r1 = 2131493487(0x7f0c026f, float:1.8610456E38)
            java.lang.String r0 = r0.getString(r1)
            r8 = 1
            r9 = 0
            int r10 = r11.video_capture_rate_index
            r18 = 0
            net.sourceforge.opencamera.ui.PopupView$12 r21 = new net.sourceforge.opencamera.ui.PopupView$12
            r1 = r21
            r2 = r25
            r3 = r14
            r5 = r15
            r6 = r7
            r1.<init>(r3, r4, r5, r6)
            java.lang.String r22 = "VIDEOCAPTURERATE"
            r1 = r25
            r2 = r7
            r3 = r0
            r4 = r8
            r5 = r9
            r6 = r10
            r7 = r18
            r8 = r22
            r9 = r21
            r1.addArrayOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9)
        L_0x06a9:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r0 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r13 == r0) goto L_0x0700
            android.content.res.Resources r0 = r25.getResources()
            r1 = 2130772057(0x7f010059, float:1.7147222E38)
            java.lang.String[] r0 = r0.getStringArray(r1)
            android.content.res.Resources r1 = r25.getResources()
            r2 = 2130772056(0x7f010058, float:1.714722E38)
            java.lang.String[] r1 = r1.getStringArray(r2)
            java.lang.String r2 = net.sourceforge.opencamera.PreferenceKeys.getTimerPreferenceKey()
            java.lang.String r3 = "0"
            java.lang.String r2 = r12.getString(r2, r3)
            java.util.List r3 = java.util.Arrays.asList(r0)
            int r2 = r3.indexOf(r2)
            r11.timer_index = r2
            int r2 = r11.timer_index
            r3 = -1
            if (r2 != r3) goto L_0x06df
            r2 = 0
            r11.timer_index = r2
        L_0x06df:
            java.util.List r2 = java.util.Arrays.asList(r1)
            android.content.res.Resources r1 = r25.getResources()
            r3 = 2131493440(0x7f0c0240, float:1.861036E38)
            java.lang.String r3 = r1.getString(r3)
            r4 = r16 ^ 1
            r5 = 0
            int r6 = r11.timer_index
            r7 = 0
            net.sourceforge.opencamera.ui.PopupView$13 r9 = new net.sourceforge.opencamera.ui.PopupView$13
            r9.<init>(r0, r14)
            java.lang.String r8 = "TIMER"
            r1 = r25
            r1.addArrayOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9)
        L_0x0700:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r0 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r13 == r0) goto L_0x0758
            android.content.res.Resources r0 = r25.getResources()
            r1 = 2130771983(0x7f01000f, float:1.7147072E38)
            java.lang.String[] r0 = r0.getStringArray(r1)
            android.content.res.Resources r1 = r25.getResources()
            r2 = 2130771982(0x7f01000e, float:1.714707E38)
            java.lang.String[] r1 = r1.getStringArray(r2)
            java.lang.String r2 = net.sourceforge.opencamera.PreferenceKeys.getRepeatModePreferenceKey()
            java.lang.String r3 = "1"
            java.lang.String r2 = r12.getString(r2, r3)
            java.util.List r3 = java.util.Arrays.asList(r0)
            int r2 = r3.indexOf(r2)
            r11.repeat_mode_index = r2
            int r2 = r11.repeat_mode_index
            r3 = -1
            if (r2 != r3) goto L_0x0736
            r2 = 0
            r11.repeat_mode_index = r2
        L_0x0736:
            java.util.List r2 = java.util.Arrays.asList(r1)
            android.content.res.Resources r1 = r25.getResources()
            r3 = 2131493104(0x7f0c00f0, float:1.8609679E38)
            java.lang.String r3 = r1.getString(r3)
            r1 = 1
            r4 = r16 ^ 1
            r5 = 1
            int r6 = r11.repeat_mode_index
            r7 = 0
            net.sourceforge.opencamera.ui.PopupView$14 r9 = new net.sourceforge.opencamera.ui.PopupView$14
            r9.<init>(r0, r14)
            java.lang.String r8 = "REPEAT_MODE"
            r1 = r25
            r1.addArrayOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9)
        L_0x0758:
            android.content.res.Resources r0 = r25.getResources()
            r1 = 2130772005(0x7f010025, float:1.7147116E38)
            java.lang.String[] r0 = r0.getStringArray(r1)
            android.content.res.Resources r1 = r25.getResources()
            r2 = 2130772004(0x7f010024, float:1.7147114E38)
            java.lang.String[] r1 = r1.getStringArray(r2)
            java.lang.String r2 = "preference_grid"
            java.lang.String r3 = "preference_grid_none"
            java.lang.String r2 = r12.getString(r2, r3)
            java.util.List r3 = java.util.Arrays.asList(r0)
            int r2 = r3.indexOf(r2)
            r11.grid_index = r2
            int r2 = r11.grid_index
            r3 = -1
            if (r2 != r3) goto L_0x0788
            r2 = 0
            r11.grid_index = r2
        L_0x0788:
            java.util.List r2 = java.util.Arrays.asList(r1)
            android.content.res.Resources r1 = r25.getResources()
            r3 = 2131493005(0x7f0c008d, float:1.8609478E38)
            java.lang.String r3 = r1.getString(r3)
            r4 = 1
            r5 = 1
            int r6 = r11.grid_index
            r7 = 1
            net.sourceforge.opencamera.ui.PopupView$15 r9 = new net.sourceforge.opencamera.ui.PopupView$15
            r9.<init>(r0, r14)
            java.lang.String r8 = "GRID"
            r1 = r25
            r1.addArrayOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9)
            net.sourceforge.opencamera.cameracontroller.CameraController r0 = r15.getCameraController()
            if (r0 == 0) goto L_0x0886
            java.util.List r4 = r15.getSupportedWhiteBalances()
            if (r4 == 0) goto L_0x07d7
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            java.util.Iterator r1 = r4.iterator()
        L_0x07bd:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x07d5
            java.lang.Object r2 = r1.next()
            java.lang.String r2 = (java.lang.String) r2
            net.sourceforge.opencamera.ui.MainUI r3 = r14.getMainUI()
            java.lang.String r2 = r3.getEntryForWhiteBalance(r2)
            r0.add(r2)
            goto L_0x07bd
        L_0x07d5:
            r3 = r0
            goto L_0x07d9
        L_0x07d7:
            r3 = r17
        L_0x07d9:
            android.content.res.Resources r0 = r25.getResources()
            r1 = 2131493623(0x7f0c02f7, float:1.8610731E38)
            java.lang.String r5 = r0.getString(r1)
            r8 = 0
            net.sourceforge.opencamera.ui.PopupView$16 r10 = new net.sourceforge.opencamera.ui.PopupView$16
            r10.<init>()
            java.lang.String r6 = "preference_white_balance"
            java.lang.String r7 = "auto"
            java.lang.String r9 = "TEST_WHITE_BALANCE"
            r1 = r25
            r2 = r12
            r1.addRadioOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9, r10)
            java.util.List r4 = r15.getSupportedSceneModes()
            if (r4 == 0) goto L_0x081f
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            java.util.Iterator r1 = r4.iterator()
        L_0x0805:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x081d
            java.lang.Object r2 = r1.next()
            java.lang.String r2 = (java.lang.String) r2
            net.sourceforge.opencamera.ui.MainUI r3 = r14.getMainUI()
            java.lang.String r2 = r3.getEntryForSceneMode(r2)
            r0.add(r2)
            goto L_0x0805
        L_0x081d:
            r3 = r0
            goto L_0x0821
        L_0x081f:
            r3 = r17
        L_0x0821:
            android.content.res.Resources r0 = r25.getResources()
            r1 = 2131493561(0x7f0c02b9, float:1.8610606E38)
            java.lang.String r5 = r0.getString(r1)
            r8 = 0
            net.sourceforge.opencamera.ui.PopupView$17 r10 = new net.sourceforge.opencamera.ui.PopupView$17
            r10.<init>(r15, r14)
            java.lang.String r6 = "preference_scene_mode"
            java.lang.String r7 = "auto"
            java.lang.String r9 = "TEST_SCENE_MODE"
            r1 = r25
            r2 = r12
            r1.addRadioOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9, r10)
            java.util.List r4 = r15.getSupportedColorEffects()
            if (r4 == 0) goto L_0x0867
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            java.util.Iterator r1 = r4.iterator()
        L_0x084d:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x0865
            java.lang.Object r2 = r1.next()
            java.lang.String r2 = (java.lang.String) r2
            net.sourceforge.opencamera.ui.MainUI r3 = r14.getMainUI()
            java.lang.String r2 = r3.getEntryForColorEffect(r2)
            r0.add(r2)
            goto L_0x084d
        L_0x0865:
            r3 = r0
            goto L_0x0869
        L_0x0867:
            r3 = r17
        L_0x0869:
            android.content.res.Resources r0 = r25.getResources()
            r1 = 2131492912(0x7f0c0030, float:1.860929E38)
            java.lang.String r5 = r0.getString(r1)
            r8 = 0
            net.sourceforge.opencamera.ui.PopupView$18 r10 = new net.sourceforge.opencamera.ui.PopupView$18
            r10.<init>(r15)
            java.lang.String r6 = "preference_color_effect"
            java.lang.String r7 = "none"
            java.lang.String r9 = "TEST_COLOR_EFFECT"
            r1 = r25
            r2 = r12
            r1.addRadioOptionsToPopup(r2, r3, r4, r5, r6, r7, r8, r9, r10)
        L_0x0886:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.PopupView.<init>(android.content.Context):void");
    }

    /* access modifiers changed from: 0000 */
    public int getTotalWidth() {
        return (int) ((((float) this.total_width_dp) * getResources().getDisplayMetrics().density) + 0.5f);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00f9  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void changePhotoMode(java.util.List<java.lang.String> r7, java.util.List<net.sourceforge.opencamera.MyApplicationInterface.PhotoMode> r8, java.lang.String r9) {
        /*
            r6 = this;
            android.content.Context r0 = r6.getContext()
            net.sourceforge.opencamera.MainActivity r0 = (net.sourceforge.opencamera.MainActivity) r0
            r1 = 0
            r2 = -1
            r3 = 0
            r4 = -1
        L_0x000a:
            int r5 = r7.size()
            if (r3 >= r5) goto L_0x0020
            if (r4 != r2) goto L_0x0020
            java.lang.Object r5 = r7.get(r3)
            boolean r5 = r9.equals(r5)
            if (r5 == 0) goto L_0x001d
            r4 = r3
        L_0x001d:
            int r3 = r3 + 1
            goto L_0x000a
        L_0x0020:
            if (r4 != r2) goto L_0x0024
            goto L_0x010f
        L_0x0024:
            java.lang.Object r7 = r8.get(r4)
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r7 = (net.sourceforge.opencamera.MyApplicationInterface.PhotoMode) r7
            int[] r8 = net.sourceforge.opencamera.p004ui.PopupView.C041325.f23x2f3f7d5c
            int r2 = r7.ordinal()
            r8 = r8[r2]
            r2 = 2131493061(0x7f0c00c5, float:1.8609592E38)
            switch(r8) {
                case 1: goto L_0x0072;
                case 2: goto L_0x0066;
                case 3: goto L_0x005a;
                case 4: goto L_0x004e;
                case 5: goto L_0x0042;
                case 6: goto L_0x0039;
                default: goto L_0x0038;
            }
        L_0x0038:
            goto L_0x007d
        L_0x0039:
            android.content.res.Resources r8 = r6.getResources()
            java.lang.String r9 = r8.getString(r2)
            goto L_0x007d
        L_0x0042:
            android.content.res.Resources r8 = r6.getResources()
            r9 = 2131493059(0x7f0c00c3, float:1.8609587E38)
            java.lang.String r9 = r8.getString(r9)
            goto L_0x007d
        L_0x004e:
            android.content.res.Resources r8 = r6.getResources()
            r9 = 2131493054(0x7f0c00be, float:1.8609577E38)
            java.lang.String r9 = r8.getString(r9)
            goto L_0x007d
        L_0x005a:
            android.content.res.Resources r8 = r6.getResources()
            r9 = 2131493056(0x7f0c00c0, float:1.8609581E38)
            java.lang.String r9 = r8.getString(r9)
            goto L_0x007d
        L_0x0066:
            android.content.res.Resources r8 = r6.getResources()
            r9 = 2131493052(0x7f0c00bc, float:1.8609573E38)
            java.lang.String r9 = r8.getString(r9)
            goto L_0x007d
        L_0x0072:
            android.content.res.Resources r8 = r6.getResources()
            r9 = 2131493063(0x7f0c00c7, float:1.8609596E38)
            java.lang.String r9 = r8.getString(r9)
        L_0x007d:
            android.content.SharedPreferences r8 = android.preference.PreferenceManager.getDefaultSharedPreferences(r0)
            android.content.SharedPreferences$Editor r3 = r8.edit()
            int[] r4 = net.sourceforge.opencamera.p004ui.PopupView.C041325.f23x2f3f7d5c
            int r5 = r7.ordinal()
            r4 = r4[r5]
            java.lang.String r5 = "preference_photo_mode"
            switch(r4) {
                case 1: goto L_0x00bd;
                case 2: goto L_0x00b7;
                case 3: goto L_0x00b1;
                case 4: goto L_0x00ab;
                case 5: goto L_0x00a5;
                case 6: goto L_0x009f;
                case 7: goto L_0x0099;
                case 8: goto L_0x0093;
                default: goto L_0x0092;
            }
        L_0x0092:
            goto L_0x00c2
        L_0x0093:
            java.lang.String r4 = "preference_photo_mode_hdr"
            r3.putString(r5, r4)
            goto L_0x00c2
        L_0x0099:
            java.lang.String r4 = "preference_photo_mode_dro"
            r3.putString(r5, r4)
            goto L_0x00c2
        L_0x009f:
            java.lang.String r4 = "preference_photo_mode_panorama"
            r3.putString(r5, r4)
            goto L_0x00c2
        L_0x00a5:
            java.lang.String r4 = "preference_photo_mode_noise_reduction"
            r3.putString(r5, r4)
            goto L_0x00c2
        L_0x00ab:
            java.lang.String r4 = "preference_photo_mode_fast_burst"
            r3.putString(r5, r4)
            goto L_0x00c2
        L_0x00b1:
            java.lang.String r4 = "preference_photo_mode_focus_bracketing"
            r3.putString(r5, r4)
            goto L_0x00c2
        L_0x00b7:
            java.lang.String r4 = "preference_photo_mode_expo_bracketing"
            r3.putString(r5, r4)
            goto L_0x00c2
        L_0x00bd:
            java.lang.String r4 = "preference_photo_mode_std"
            r3.putString(r5, r4)
        L_0x00c2:
            r3.apply()
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r3 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.HDR
            r4 = 1
            if (r7 != r3) goto L_0x00e0
            java.lang.String r7 = "done_hdr_info"
            boolean r8 = r8.contains(r7)
            if (r8 != 0) goto L_0x00f7
            net.sourceforge.opencamera.ui.MainUI r8 = r0.getMainUI()
            r1 = 2131493057(0x7f0c00c1, float:1.8609583E38)
            r2 = 2131493006(0x7f0c008e, float:1.860948E38)
            r8.showInfoDialog(r1, r2, r7)
            goto L_0x00f6
        L_0x00e0:
            net.sourceforge.opencamera.MyApplicationInterface$PhotoMode r3 = net.sourceforge.opencamera.MyApplicationInterface.PhotoMode.Panorama
            if (r7 != r3) goto L_0x00f7
            java.lang.String r7 = "done_panorama_info"
            boolean r8 = r8.contains(r7)
            if (r8 != 0) goto L_0x00f7
            net.sourceforge.opencamera.ui.MainUI r8 = r0.getMainUI()
            r1 = 2131493037(0x7f0c00ad, float:1.8609543E38)
            r8.showInfoDialog(r2, r1, r7)
        L_0x00f6:
            r1 = 1
        L_0x00f7:
            if (r1 == 0) goto L_0x00fa
            r9 = 0
        L_0x00fa:
            net.sourceforge.opencamera.MyApplicationInterface r7 = r0.getApplicationInterface()
            net.sourceforge.opencamera.ui.DrawPreview r7 = r7.getDrawPreview()
            r7.updateSettings()
            r0.updateForSettings(r9)
            net.sourceforge.opencamera.ui.MainUI r7 = r0.getMainUI()
            r7.destroyPopup()
        L_0x010f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.PopupView.changePhotoMode(java.util.List, java.util.List, java.lang.String):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0054  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:20:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void switchToWhiteBalance(java.lang.String r7) {
        /*
            r6 = this;
            android.content.Context r0 = r6.getContext()
            net.sourceforge.opencamera.MainActivity r0 = (net.sourceforge.opencamera.MainActivity) r0
            net.sourceforge.opencamera.preview.Preview r1 = r0.getPreview()
            java.lang.String r2 = "manual"
            boolean r3 = r7.equals(r2)
            r4 = -1
            if (r3 == 0) goto L_0x004d
            net.sourceforge.opencamera.cameracontroller.CameraController r3 = r1.getCameraController()
            if (r3 == 0) goto L_0x004d
            net.sourceforge.opencamera.cameracontroller.CameraController r3 = r1.getCameraController()
            java.lang.String r3 = r3.getWhiteBalance()
            if (r3 == 0) goto L_0x0029
            boolean r2 = r3.equals(r2)
            if (r2 != 0) goto L_0x004d
        L_0x0029:
            r2 = 1
            net.sourceforge.opencamera.cameracontroller.CameraController r3 = r1.getCameraController()
            boolean r3 = r3.captureResultHasWhiteBalanceTemperature()
            if (r3 == 0) goto L_0x004e
            net.sourceforge.opencamera.cameracontroller.CameraController r3 = r1.getCameraController()
            int r4 = r3.captureResultWhiteBalanceTemperature()
            android.content.SharedPreferences r3 = android.preference.PreferenceManager.getDefaultSharedPreferences(r0)
            android.content.SharedPreferences$Editor r3 = r3.edit()
            java.lang.String r5 = "preference_white_balance_temperature"
            r3.putInt(r5, r4)
            r3.apply()
            goto L_0x004e
        L_0x004d:
            r2 = 0
        L_0x004e:
            net.sourceforge.opencamera.cameracontroller.CameraController r3 = r1.getCameraController()
            if (r3 == 0) goto L_0x0067
            net.sourceforge.opencamera.cameracontroller.CameraController r3 = r1.getCameraController()
            r3.setWhiteBalance(r7)
            if (r4 <= 0) goto L_0x0067
            net.sourceforge.opencamera.cameracontroller.CameraController r7 = r1.getCameraController()
            r7.setWhiteBalanceTemperature(r4)
            r0.setManualWBSeekbar()
        L_0x0067:
            if (r2 == 0) goto L_0x006c
            r0.closePopup()
        L_0x006c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.PopupView.switchToWhiteBalance(java.lang.String):void");
    }

    private void addButtonOptionsToPopup(List<String> list, int i, int i2, String str, String str2, int i3, String str3, ButtonOptionsPopupListener buttonOptionsPopupListener) {
        createButtonOptions(this, getContext(), this.total_width_dp, ((MainActivity) getContext()).getMainUI().getTestUIButtonsMap(), list, i, i2, str, true, str2, i3, str3, buttonOptionsPopupListener);
    }

    /* JADX WARNING: type inference failed for: r27v1 */
    /* JADX WARNING: type inference failed for: r27v2 */
    /* JADX WARNING: type inference failed for: r15v5, types: [android.view.View] */
    /* JADX WARNING: type inference failed for: r5v7, types: [android.view.View, java.lang.Object] */
    /* JADX WARNING: type inference failed for: r15v8 */
    /* JADX WARNING: type inference failed for: r27v3 */
    /* JADX WARNING: type inference failed for: r15v10 */
    /* JADX WARNING: type inference failed for: r15v11 */
    /* JADX WARNING: type inference failed for: r27v4 */
    /* JADX WARNING: type inference failed for: r5v15 */
    /* JADX WARNING: type inference failed for: r5v16 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x00bd A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00c0  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00e0  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00e8  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x017e  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01aa  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x01ec  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01f3  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0202  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x021c  */
    /* JADX WARNING: Unknown variable types count: 5 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static java.util.List<android.view.View> createButtonOptions(android.view.ViewGroup r21, android.content.Context r22, int r23, java.util.Map<java.lang.String, android.view.View> r24, java.util.List<java.lang.String> r25, int r26, int r27, java.lang.String r28, boolean r29, java.lang.String r30, int r31, java.lang.String r32, net.sourceforge.opencamera.p004ui.PopupView.ButtonOptionsPopupListener r33) {
        /*
            r0 = r21
            r1 = r22
            r2 = r23
            r3 = r24
            r4 = r25
            r5 = r26
            r6 = r27
            r7 = r28
            r8 = r31
            java.util.ArrayList r9 = new java.util.ArrayList
            r9.<init>()
            if (r4 == 0) goto L_0x026e
            java.lang.System.nanoTime()
            android.widget.LinearLayout r10 = new android.widget.LinearLayout
            r10.<init>(r1)
            r11 = 0
            r10.setOrientation(r11)
            r13 = -1
            if (r5 == r13) goto L_0x0031
            android.content.res.Resources r14 = r22.getResources()
            java.lang.String[] r5 = r14.getStringArray(r5)
            goto L_0x0032
        L_0x0031:
            r5 = 0
        L_0x0032:
            if (r6 == r13) goto L_0x003d
            android.content.res.Resources r14 = r22.getResources()
            java.lang.String[] r6 = r14.getStringArray(r6)
            goto L_0x003e
        L_0x003d:
            r6 = 0
        L_0x003e:
            android.content.res.Resources r14 = r22.getResources()
            android.util.DisplayMetrics r14 = r14.getDisplayMetrics()
            float r14 = r14.density
            int r15 = r25.size()
            if (r8 <= 0) goto L_0x0052
            int r15 = java.lang.Math.min(r15, r8)
        L_0x0052:
            int r15 = r2 / r15
            r12 = 48
            if (r15 >= r12) goto L_0x005c
            if (r8 != 0) goto L_0x005c
            r15 = 1
            goto L_0x005e
        L_0x005c:
            r12 = r15
            r15 = 0
        L_0x005e:
            float r12 = (float) r12
            float r12 = r12 * r14
            r16 = 1056964608(0x3f000000, float:0.5)
            float r12 = r12 + r16
            int r12 = (int) r12
            net.sourceforge.opencamera.ui.PopupView$19 r13 = new net.sourceforge.opencamera.ui.PopupView$19
            r11 = r33
            r13.<init>(r11)
            r11 = r10
            r17 = r12
            r27 = 0
            r10 = 0
        L_0x0073:
            int r12 = r25.size()
            if (r10 >= r12) goto L_0x0234
            java.lang.Object r12 = r4.get(r10)
            java.lang.String r12 = (java.lang.String) r12
            if (r8 <= 0) goto L_0x00a3
            if (r10 <= 0) goto L_0x00a3
            int r18 = r10 % r8
            if (r18 != 0) goto L_0x00a3
            r0.addView(r11)
            android.widget.LinearLayout r11 = new android.widget.LinearLayout
            r11.<init>(r1)
            r4 = 0
            r11.setOrientation(r4)
            int r4 = r25.size()
            int r4 = r4 - r10
            if (r4 > r8) goto L_0x00a3
            int r4 = r2 / r4
            float r4 = (float) r4
            float r4 = r4 * r14
            float r4 = r4 + r16
            int r4 = (int) r4
            goto L_0x00a5
        L_0x00a3:
            r4 = r17
        L_0x00a5:
            if (r5 == 0) goto L_0x00d6
            if (r6 == 0) goto L_0x00d6
            r0 = -1
            r8 = 0
        L_0x00ab:
            int r2 = r6.length
            if (r8 >= r2) goto L_0x00bd
            r2 = -1
            if (r0 != r2) goto L_0x00be
            r2 = r6[r8]
            boolean r2 = r2.equals(r12)
            if (r2 == 0) goto L_0x00ba
            r0 = r8
        L_0x00ba:
            int r8 = r8 + 1
            goto L_0x00ab
        L_0x00bd:
            r2 = -1
        L_0x00be:
            if (r0 == r2) goto L_0x00d6
            android.content.res.Resources r2 = r22.getResources()
            r0 = r5[r0]
            android.content.Context r8 = r22.getApplicationContext()
            java.lang.String r8 = r8.getPackageName()
            r18 = r5
            r5 = 0
            int r0 = r2.getIdentifier(r0, r5, r8)
            goto L_0x00da
        L_0x00d6:
            r18 = r5
            r5 = 0
            r0 = -1
        L_0x00da:
            int r2 = r28.length()
            if (r2 != 0) goto L_0x00e8
            r19 = r6
            r2 = r12
            r20 = r15
        L_0x00e5:
            r5 = -1
            goto L_0x017c
        L_0x00e8:
            java.lang.String r2 = "ISO"
            boolean r8 = r7.equalsIgnoreCase(r2)
            java.lang.String r5 = "\n"
            java.lang.String r17 = ""
            if (r8 == 0) goto L_0x0129
            int r8 = r12.length()
            r19 = r6
            r6 = 4
            r20 = r15
            if (r8 < r6) goto L_0x012d
            r8 = 0
            java.lang.String r15 = r12.substring(r8, r6)
            java.lang.String r8 = "ISO_"
            boolean r8 = r15.equalsIgnoreCase(r8)
            if (r8 == 0) goto L_0x012d
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            if (r29 == 0) goto L_0x0115
            r8 = r7
            goto L_0x0117
        L_0x0115:
            r8 = r17
        L_0x0117:
            r2.append(r8)
            r2.append(r5)
            java.lang.String r5 = r12.substring(r6)
            r2.append(r5)
            java.lang.String r2 = r2.toString()
            goto L_0x00e5
        L_0x0129:
            r19 = r6
            r20 = r15
        L_0x012d:
            boolean r6 = r7.equalsIgnoreCase(r2)
            if (r6 == 0) goto L_0x0162
            int r6 = r12.length()
            r8 = 3
            if (r6 < r8) goto L_0x0162
            r6 = 0
            java.lang.String r15 = r12.substring(r6, r8)
            boolean r2 = r15.equalsIgnoreCase(r2)
            if (r2 == 0) goto L_0x0162
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            if (r29 == 0) goto L_0x014e
            r6 = r7
            goto L_0x0150
        L_0x014e:
            r6 = r17
        L_0x0150:
            r2.append(r6)
            r2.append(r5)
            java.lang.String r5 = r12.substring(r8)
            r2.append(r5)
            java.lang.String r2 = r2.toString()
            goto L_0x00e5
        L_0x0162:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            if (r29 == 0) goto L_0x016b
            r6 = r7
            goto L_0x016d
        L_0x016b:
            r6 = r17
        L_0x016d:
            r2.append(r6)
            r2.append(r5)
            r2.append(r12)
            java.lang.String r2 = r2.toString()
            goto L_0x00e5
        L_0x017c:
            if (r0 == r5) goto L_0x01aa
            android.widget.ImageButton r5 = new android.widget.ImageButton
            r5.<init>(r1)
            r9.add(r5)
            r11.addView(r5)
            r6 = r1
            net.sourceforge.opencamera.MainActivity r6 = (net.sourceforge.opencamera.MainActivity) r6
            android.graphics.Bitmap r0 = r6.getPreloadedBitmap(r0)
            if (r0 == 0) goto L_0x0195
            r5.setImageBitmap(r0)
        L_0x0195:
            android.widget.ImageView$ScaleType r0 = android.widget.ImageView.ScaleType.FIT_CENTER
            r5.setScaleType(r0)
            r0 = 0
            r5.setBackgroundColor(r0)
            r6 = 1088421888(0x40e00000, float:7.0)
            float r6 = r6 * r14
            float r6 = r6 + r16
            int r6 = (int) r6
            r5.setPadding(r6, r6, r6, r6)
            r0 = -1
            goto L_0x01cf
        L_0x01aa:
            r0 = 0
            android.widget.Button r5 = new android.widget.Button
            r5.<init>(r1)
            r5.setBackgroundColor(r0)
            r9.add(r5)
            r11.addView(r5)
            r5.setText(r2)
            r0 = 1094713344(0x41400000, float:12.0)
            r6 = 1
            r5.setTextSize(r6, r0)
            r0 = -1
            r5.setTextColor(r0)
            r6 = 0
            float r6 = r6 * r14
            float r6 = r6 + r16
            int r6 = (int) r6
            r5.setPadding(r6, r6, r6, r6)
        L_0x01cf:
            android.view.ViewGroup$LayoutParams r6 = r5.getLayoutParams()
            r6.width = r4
            r8 = 1113325568(0x425c0000, float:55.0)
            float r8 = r8 * r14
            float r8 = r8 + r16
            int r8 = (int) r8
            r6.height = r8
            r5.setLayoutParams(r6)
            r5.setContentDescription(r2)
            r2 = r30
            boolean r6 = r12.equals(r2)
            if (r6 == 0) goto L_0x01f3
            r6 = 1
            setButtonSelected(r5, r6)
            r15 = r5
            r8 = 0
            goto L_0x01fa
        L_0x01f3:
            r6 = 1
            r8 = 0
            setButtonSelected(r5, r8)
            r15 = r27
        L_0x01fa:
            r5.setTag(r12)
            r5.setOnClickListener(r13)
            if (r3 == 0) goto L_0x021c
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r6 = r32
            r0.append(r6)
            java.lang.String r8 = "_"
            r0.append(r8)
            r0.append(r12)
            java.lang.String r0 = r0.toString()
            r3.put(r0, r5)
            goto L_0x021e
        L_0x021c:
            r6 = r32
        L_0x021e:
            int r10 = r10 + 1
            r0 = r21
            r2 = r23
            r8 = r31
            r17 = r4
            r27 = r15
            r5 = r18
            r6 = r19
            r15 = r20
            r4 = r25
            goto L_0x0073
        L_0x0234:
            r20 = r15
            if (r20 == 0) goto L_0x0269
            r0 = r23
            float r0 = (float) r0
            float r0 = r0 * r14
            float r0 = r0 + r16
            int r0 = (int) r0
            android.widget.HorizontalScrollView r2 = new android.widget.HorizontalScrollView
            r2.<init>(r1)
            r2.addView(r11)
            android.widget.LinearLayout$LayoutParams r1 = new android.widget.LinearLayout$LayoutParams
            r3 = -2
            r1.<init>(r0, r3)
            r2.setLayoutParams(r1)
            r1 = r21
            r1.addView(r2)
            if (r27 == 0) goto L_0x026e
            android.view.ViewTreeObserver r1 = r21.getViewTreeObserver()
            net.sourceforge.opencamera.ui.PopupView$20 r3 = new net.sourceforge.opencamera.ui.PopupView$20
            r15 = r27
            r4 = r17
            r3.<init>(r15, r0, r4, r2)
            r1.addOnGlobalLayoutListener(r3)
            goto L_0x026e
        L_0x0269:
            r1 = r21
            r1.addView(r11)
        L_0x026e:
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.PopupView.createButtonOptions(android.view.ViewGroup, android.content.Context, int, java.util.Map, java.util.List, int, int, java.lang.String, boolean, java.lang.String, int, java.lang.String, net.sourceforge.opencamera.ui.PopupView$ButtonOptionsPopupListener):java.util.List");
    }

    static void setButtonSelected(View view, boolean z) {
        view.setAlpha(z ? 1.0f : 0.6f);
    }

    private void addTitleToPopup(String str) {
        TextView textView = new TextView(getContext());
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append(":");
        textView.setText(sb.toString());
        textView.setTextColor(-1);
        textView.setGravity(17);
        textView.setTextSize(1, title_text_size_dip);
        textView.setTypeface(null, 1);
        addView(textView);
    }

    private void addRadioOptionsToPopup(SharedPreferences sharedPreferences, List<String> list, List<String> list2, String str, String str2, String str3, String str4, String str5, RadioOptionsListener radioOptionsListener) {
        if (list != null) {
            final MainActivity mainActivity = (MainActivity) getContext();
            System.nanoTime();
            Button button = new Button(getContext());
            button.setBackgroundColor(0);
            StringBuilder sb = new StringBuilder();
            final String str6 = str;
            sb.append(str6);
            sb.append("...");
            button.setText(sb.toString());
            button.setAllCaps(false);
            button.setTextSize(1, title_text_size_dip);
            addView(button);
            RadioGroup radioGroup = new RadioGroup(getContext());
            radioGroup.setOrientation(1);
            radioGroup.setVisibility(8);
            final String str7 = str5;
            mainActivity.getMainUI().getTestUIButtonsMap().put(str7, radioGroup);
            final RadioGroup radioGroup2 = radioGroup;
            final SharedPreferences sharedPreferences2 = sharedPreferences;
            final List<String> list3 = list;
            final List<String> list4 = list2;
            final String str8 = str2;
            final String str9 = str3;
            final String str10 = str4;
            C040821 r13 = r0;
            final RadioOptionsListener radioOptionsListener2 = radioOptionsListener;
            C040821 r0 = new OnClickListener() {
                private boolean created = false;
                private boolean opened = false;

                public void onClick(View view) {
                    if (this.opened) {
                        radioGroup2.setVisibility(8);
                        ScrollView scrollView = (ScrollView) mainActivity.findViewById(C0316R.C0318id.popup_container);
                        scrollView.invalidate();
                        scrollView.requestLayout();
                    } else {
                        if (!this.created) {
                            PopupView.this.addRadioOptionsToGroup(radioGroup2, sharedPreferences2, list3, list4, str6, str8, str9, str10, str7, radioOptionsListener2);
                            this.created = true;
                        }
                        radioGroup2.setVisibility(0);
                        final ScrollView scrollView2 = (ScrollView) mainActivity.findViewById(C0316R.C0318id.popup_container);
                        scrollView2.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                            public void onGlobalLayout() {
                                if (VERSION.SDK_INT > 15) {
                                    scrollView2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                } else {
                                    scrollView2.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                }
                                if (radioGroup2.getChildCount() > 0) {
                                    int checkedRadioButtonId = radioGroup2.getCheckedRadioButtonId();
                                    if (checkedRadioButtonId >= 0 && checkedRadioButtonId < radioGroup2.getChildCount()) {
                                        scrollView2.smoothScrollBy(0, radioGroup2.getChildAt(checkedRadioButtonId).getBottom());
                                    }
                                }
                            }
                        });
                    }
                    this.opened = !this.opened;
                }
            };
            button.setOnClickListener(r13);
            addView(radioGroup);
            return;
        }
    }

    /* access modifiers changed from: private */
    public void addRadioOptionsToGroup(RadioGroup radioGroup, SharedPreferences sharedPreferences, List<String> list, List<String> list2, String str, String str2, String str3, String str4, String str5, RadioOptionsListener radioOptionsListener) {
        RadioGroup radioGroup2 = radioGroup;
        String str6 = str2;
        String string = str6 != null ? sharedPreferences.getString(str6, str3) : str4;
        System.nanoTime();
        MainActivity mainActivity = (MainActivity) getContext();
        int i = 0;
        int i2 = 0;
        while (i2 < list.size()) {
            final String str7 = (String) list.get(i2);
            String str8 = (String) list2.get(i2);
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setId(i);
            radioButton.setText(str7);
            radioButton.setTextSize(1, 16.0f);
            radioButton.setTextColor(-1);
            radioGroup2.addView(radioButton);
            if (str8.equals(string)) {
                radioGroup2.check(i);
            }
            int i3 = i + 1;
            radioButton.setContentDescription(str7);
            final String str9 = str8;
            final String str10 = str2;
            final MainActivity mainActivity2 = mainActivity;
            C041022 r0 = r1;
            final RadioOptionsListener radioOptionsListener2 = radioOptionsListener;
            RadioButton radioButton2 = radioButton;
            final String str11 = str;
            C041022 r1 = new OnClickListener() {
                public void onClick(View view) {
                    if (str10 != null) {
                        Editor edit = PreferenceManager.getDefaultSharedPreferences(mainActivity2).edit();
                        edit.putString(str10, str9);
                        edit.apply();
                    }
                    RadioOptionsListener radioOptionsListener = radioOptionsListener2;
                    if (radioOptionsListener != null) {
                        radioOptionsListener.onClick(str9);
                        return;
                    }
                    MainActivity mainActivity = mainActivity2;
                    StringBuilder sb = new StringBuilder();
                    sb.append(str11);
                    sb.append(": ");
                    sb.append(str7);
                    mainActivity.updateForSettings(sb.toString());
                    mainActivity2.closePopup();
                }
            };
            radioButton2.setOnClickListener(r0);
            Map testUIButtonsMap = mainActivity.getMainUI().getTestUIButtonsMap();
            StringBuilder sb = new StringBuilder();
            sb.append(str5);
            sb.append("_");
            sb.append(str8);
            testUIButtonsMap.put(sb.toString(), radioButton2);
            i2++;
            radioGroup2 = radioGroup;
            String str12 = str2;
            i = i3;
        }
    }

    /* access modifiers changed from: private */
    public void setArrayOptionsText(List<String> list, String str, TextView textView, boolean z, boolean z2, int i) {
        if (!z || (i != 0 && z2)) {
            textView.setText((CharSequence) list.get(i));
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append(": ");
        sb.append((String) list.get(i));
        textView.setText(sb.toString());
    }

    private void addArrayOptionsToPopup(List<String> list, String str, boolean z, boolean z2, int i, boolean z3, String str2, ArrayOptionsPopupListener arrayOptionsPopupListener) {
        String str3 = str;
        int i2 = i;
        String str4 = str2;
        if (list != null && i2 != -1) {
            if (!z) {
                addTitleToPopup(str3);
            }
            MainActivity mainActivity = (MainActivity) getContext();
            LinearLayout linearLayout = new LinearLayout(getContext());
            int i3 = 0;
            linearLayout.setOrientation(0);
            TextView textView = new TextView(getContext());
            setArrayOptionsText(list, str, textView, z, z2, i);
            textView.setTextSize(1, 16.0f);
            textView.setTextColor(-1);
            textView.setGravity(17);
            textView.setLayoutParams(new LayoutParams(-2, -2, 1.0f));
            int i4 = (int) ((getResources().getDisplayMetrics().density * 0.0f) + 0.5f);
            Button button = new Button(getContext());
            button.setBackgroundColor(0);
            linearLayout.addView(button);
            button.setText("<");
            button.setTextSize(1, 16.0f);
            button.setTypeface(null, 1);
            button.setPadding(i4, i4, i4, i4);
            ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
            layoutParams.width = this.arrow_button_w;
            layoutParams.height = this.arrow_button_h;
            button.setLayoutParams(layoutParams);
            button.setVisibility((z3 || i2 > 0) ? 0 : 4);
            StringBuilder sb = new StringBuilder();
            sb.append(getResources().getString(C0316R.string.previous));
            String str5 = " ";
            sb.append(str5);
            sb.append(str3);
            button.setContentDescription(sb.toString());
            Map testUIButtonsMap = mainActivity.getMainUI().getTestUIButtonsMap();
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str4);
            sb2.append("_PREV");
            testUIButtonsMap.put(sb2.toString(), button);
            linearLayout.addView(textView);
            mainActivity.getMainUI().getTestUIButtonsMap().put(str4, textView);
            Button button2 = new Button(getContext());
            button2.setBackgroundColor(0);
            linearLayout.addView(button2);
            button2.setText(">");
            button2.setTextSize(1, 16.0f);
            button2.setTypeface(null, 1);
            button2.setPadding(i4, i4, i4, i4);
            ViewGroup.LayoutParams layoutParams2 = button2.getLayoutParams();
            layoutParams2.width = this.arrow_button_w;
            layoutParams2.height = this.arrow_button_h;
            button2.setLayoutParams(layoutParams2);
            if (!z3 && i2 >= list.size() - 1) {
                i3 = 4;
            }
            button2.setVisibility(i3);
            StringBuilder sb3 = new StringBuilder();
            sb3.append(getResources().getString(C0316R.string.next));
            sb3.append(str5);
            sb3.append(str3);
            button2.setContentDescription(sb3.toString());
            Map testUIButtonsMap2 = mainActivity.getMainUI().getTestUIButtonsMap();
            StringBuilder sb4 = new StringBuilder();
            sb4.append(str4);
            sb4.append("_NEXT");
            testUIButtonsMap2.put(sb4.toString(), button2);
            final ArrayOptionsPopupListener arrayOptionsPopupListener2 = arrayOptionsPopupListener;
            final List<String> list2 = list;
            final String str6 = str;
            final TextView textView2 = textView;
            final Button button3 = button2;
            final boolean z4 = z;
            final boolean z5 = z2;
            final Button button4 = button;
            Button button5 = button;
            final boolean z6 = z3;
            Button button6 = button3;
            C041123 r0 = new OnClickListener() {
                public void onClick(View view) {
                    int onClickPrev = arrayOptionsPopupListener2.onClickPrev();
                    if (onClickPrev != -1) {
                        PopupView.this.setArrayOptionsText(list2, str6, textView2, z4, z5, onClickPrev);
                        int i = 4;
                        button4.setVisibility((z6 || onClickPrev > 0) ? 0 : 4);
                        Button button = button3;
                        if (z6 || onClickPrev < list2.size() - 1) {
                            i = 0;
                        }
                        button.setVisibility(i);
                    }
                }
            };
            button5.setOnClickListener(r0);
            final Button button7 = button5;
            C041224 r02 = new OnClickListener() {
                public void onClick(View view) {
                    int onClickNext = arrayOptionsPopupListener2.onClickNext();
                    if (onClickNext != -1) {
                        PopupView.this.setArrayOptionsText(list2, str6, textView2, z4, z5, onClickNext);
                        int i = 4;
                        button7.setVisibility((z6 || onClickNext > 0) ? 0 : 4);
                        Button button = button3;
                        if (z6 || onClickNext < list2.size() - 1) {
                            i = 0;
                        }
                        button.setVisibility(i);
                    }
                }
            };
            button6.setOnClickListener(r02);
            addView(linearLayout);
        }
    }
}
