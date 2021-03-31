package net.sourceforge.opencamera.p004ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.support.p000v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.ZoomControls;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import net.sourceforge.opencamera.BuildConfig;
import net.sourceforge.opencamera.C0316R;
import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyApplicationInterface.PhotoMode;
import net.sourceforge.opencamera.PreferenceKeys;
import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.preview.ApplicationInterface.RawPref;
import net.sourceforge.opencamera.preview.Preview;

/* renamed from: net.sourceforge.opencamera.ui.MainUI */
public class MainUI {
    private static final String TAG = "MainUI";
    private static final boolean cache_popup = true;
    private static final String manual_iso_value = "m";
    private int current_orientation;
    private boolean force_destroy_popup = false;
    private final int highlightColor = Color.rgb(183, 28, 28);
    private final int highlightColorExposureUIElement = Color.rgb(244, 67, 54);
    private boolean immersive_mode;
    private int iso_button_manual_index = -1;
    private List<View> iso_buttons;
    private boolean keydown_volume_down;
    private boolean keydown_volume_up;
    private int mExposureLine = 0;
    private View mHighlightedIcon;
    private LinearLayout mHighlightedLine;
    private int mPopupIcon = 0;
    private int mPopupLine = 0;
    private boolean mSelectingExposureUIElement = false;
    private boolean mSelectingIcons = false;
    private boolean mSelectingLines = false;
    /* access modifiers changed from: private */
    public final MainActivity main_activity;
    private PopupView popup_view;
    private volatile boolean popup_view_is_open;
    private boolean remote_control_mode;
    /* access modifiers changed from: private */
    public boolean show_gui_photo = cache_popup;
    /* access modifiers changed from: private */
    public boolean show_gui_video = cache_popup;
    public int test_saved_popup_height;
    public int test_saved_popup_width;
    private final Map<String, View> test_ui_buttons = new Hashtable();
    private View top_icon = null;
    private UIPlacement ui_placement = UIPlacement.UIPLACEMENT_RIGHT;
    private boolean view_rotate_animation;

    /* renamed from: net.sourceforge.opencamera.ui.MainUI$8 */
    static /* synthetic */ class C03938 {
        static final /* synthetic */ int[] $SwitchMap$net$sourceforge$opencamera$ui$MainUI$UIPlacement = new int[UIPlacement.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|6) */
        /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0014 */
        static {
            /*
                net.sourceforge.opencamera.ui.MainUI$UIPlacement[] r0 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$net$sourceforge$opencamera$ui$MainUI$UIPlacement = r0
                int[] r0 = $SwitchMap$net$sourceforge$opencamera$ui$MainUI$UIPlacement     // Catch:{ NoSuchFieldError -> 0x0014 }
                net.sourceforge.opencamera.ui.MainUI$UIPlacement r1 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.UIPLACEMENT_TOP     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                int[] r0 = $SwitchMap$net$sourceforge$opencamera$ui$MainUI$UIPlacement     // Catch:{ NoSuchFieldError -> 0x001f }
                net.sourceforge.opencamera.ui.MainUI$UIPlacement r1 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.UIPLACEMENT_LEFT     // Catch:{ NoSuchFieldError -> 0x001f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.MainUI.C03938.<clinit>():void");
        }
    }

    /* renamed from: net.sourceforge.opencamera.ui.MainUI$UIPlacement */
    enum UIPlacement {
        UIPLACEMENT_RIGHT,
        UIPLACEMENT_LEFT,
        UIPLACEMENT_TOP
    }

    public MainUI(MainActivity mainActivity) {
        this.main_activity = mainActivity;
        setSeekbarColors();
    }

    private void setSeekbarColors() {
        if (VERSION.SDK_INT >= 21) {
            ColorStateList valueOf = ColorStateList.valueOf(Color.argb(255, 240, 240, 240));
            ColorStateList valueOf2 = ColorStateList.valueOf(Color.argb(255, 255, 255, 255));
            SeekBar seekBar = (SeekBar) this.main_activity.findViewById(C0316R.C0318id.zoom_seekbar);
            seekBar.setProgressTintList(valueOf);
            seekBar.setThumbTintList(valueOf2);
            SeekBar seekBar2 = (SeekBar) this.main_activity.findViewById(C0316R.C0318id.focus_seekbar);
            seekBar2.setProgressTintList(valueOf);
            seekBar2.setThumbTintList(valueOf2);
            SeekBar seekBar3 = (SeekBar) this.main_activity.findViewById(C0316R.C0318id.focus_bracketing_target_seekbar);
            seekBar3.setProgressTintList(valueOf);
            seekBar3.setThumbTintList(valueOf2);
            SeekBar seekBar4 = (SeekBar) this.main_activity.findViewById(C0316R.C0318id.exposure_seekbar);
            seekBar4.setProgressTintList(valueOf);
            seekBar4.setThumbTintList(valueOf2);
            SeekBar seekBar5 = (SeekBar) this.main_activity.findViewById(C0316R.C0318id.iso_seekbar);
            seekBar5.setProgressTintList(valueOf);
            seekBar5.setThumbTintList(valueOf2);
            SeekBar seekBar6 = (SeekBar) this.main_activity.findViewById(C0316R.C0318id.exposure_time_seekbar);
            seekBar6.setProgressTintList(valueOf);
            seekBar6.setThumbTintList(valueOf2);
            SeekBar seekBar7 = (SeekBar) this.main_activity.findViewById(C0316R.C0318id.white_balance_seekbar);
            seekBar7.setProgressTintList(valueOf);
            seekBar7.setThumbTintList(valueOf2);
        }
    }

    private void setViewRotation(View view, float f) {
        if (!this.view_rotate_animation) {
            view.setRotation(f);
        }
        float rotation = f - view.getRotation();
        if (rotation > 181.0f) {
            rotation -= 360.0f;
        } else if (rotation < -181.0f) {
            rotation += 360.0f;
        }
        view.animate().rotationBy(rotation).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }

    public void layoutUI() {
        layoutUI(false);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x003b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public net.sourceforge.opencamera.p004ui.MainUI.UIPlacement computeUIPlacement() {
        /*
            r5 = this;
            net.sourceforge.opencamera.MainActivity r0 = r5.main_activity
            android.content.SharedPreferences r0 = android.preference.PreferenceManager.getDefaultSharedPreferences(r0)
            java.lang.String r1 = "ui_top"
            java.lang.String r2 = "preference_ui_placement"
            java.lang.String r0 = r0.getString(r2, r1)
            int r2 = r0.hashCode()
            r3 = -845441750(0xffffffffcd9b952a, float:-3.26280512E8)
            r4 = 1
            if (r2 == r3) goto L_0x0028
            r1 = -439138606(0xffffffffe5d346d2, float:-1.2471572E23)
            if (r2 == r1) goto L_0x001e
            goto L_0x0030
        L_0x001e:
            java.lang.String r1 = "ui_left"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0030
            r0 = 0
            goto L_0x0031
        L_0x0028:
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0030
            r0 = 1
            goto L_0x0031
        L_0x0030:
            r0 = -1
        L_0x0031:
            if (r0 == 0) goto L_0x003b
            if (r0 == r4) goto L_0x0038
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r0 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.UIPLACEMENT_RIGHT
            return r0
        L_0x0038:
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r0 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.UIPLACEMENT_TOP
            return r0
        L_0x003b:
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r0 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.UIPLACEMENT_LEFT
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.MainUI.computeUIPlacement():net.sourceforge.opencamera.ui.MainUI$UIPlacement");
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:109:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x004d  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0062  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0292  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x046c  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0475  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x053d  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x0545  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x05a4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void layoutUI(boolean r25) {
        /*
            r24 = this;
            r0 = r24
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            android.content.SharedPreferences r1 = android.preference.PreferenceManager.getDefaultSharedPreferences(r1)
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r2 = r24.computeUIPlacement()
            r0.ui_placement = r2
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            android.view.WindowManager r2 = r2.getWindowManager()
            android.view.Display r2 = r2.getDefaultDisplay()
            int r2 = r2.getRotation()
            r3 = 3
            r4 = 2
            r5 = 1
            if (r2 == 0) goto L_0x0027
            if (r2 == r5) goto L_0x002f
            if (r2 == r4) goto L_0x002c
            if (r2 == r3) goto L_0x0029
        L_0x0027:
            r2 = 0
            goto L_0x0031
        L_0x0029:
            r2 = 270(0x10e, float:3.78E-43)
            goto L_0x0031
        L_0x002c:
            r2 = 180(0xb4, float:2.52E-43)
            goto L_0x0031
        L_0x002f:
            r2 = 90
        L_0x0031:
            int r7 = r0.current_orientation
            int r7 = r7 + r2
            int r7 = r7 % 360
            int r2 = 360 - r7
            int r2 = r2 % 360
            net.sourceforge.opencamera.MainActivity r7 = r0.main_activity
            net.sourceforge.opencamera.preview.Preview r7 = r7.getPreview()
            r7.setUIRotation(r2)
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r9 = r0.ui_placement
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r10 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.UIPLACEMENT_LEFT
            r13 = 12
            r14 = 10
            if (r9 != r10) goto L_0x0062
            r3 = 12
            r4 = 10
            r5 = 10
            r7 = 12
            r8 = 1
            r9 = 2
            r10 = 3
            r13 = 9
            r14 = 11
            r15 = 0
            r16 = 3
            r17 = 2
            goto L_0x0089
        L_0x0062:
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r9 = r0.ui_placement
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r10 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.UIPLACEMENT_TOP
            if (r9 != r10) goto L_0x0075
            r3 = 9
            r4 = 11
            r5 = 12
            r7 = 10
            r8 = 2
            r9 = 0
            r10 = 1
            r15 = 3
            goto L_0x0085
        L_0x0075:
            r3 = 10
            r4 = 12
            r5 = 12
            r7 = 10
            r8 = 1
            r9 = 2
            r10 = 3
            r13 = 9
            r14 = 11
            r15 = 0
        L_0x0085:
            r16 = 2
            r17 = 3
        L_0x0089:
            android.graphics.Point r11 = new android.graphics.Point
            r11.<init>()
            net.sourceforge.opencamera.MainActivity r12 = r0.main_activity
            android.view.WindowManager r12 = r12.getWindowManager()
            android.view.Display r12 = r12.getDefaultDisplay()
            r12.getSize(r11)
            int r12 = r11.x
            int r11 = r11.y
            int r11 = java.lang.Math.min(r12, r11)
            if (r25 != 0) goto L_0x046c
            r12 = 0
            r0.top_icon = r12
            net.sourceforge.opencamera.MainActivity r12 = r0.main_activity
            r6 = 2131099693(0x7f06002d, float:1.7811746E38)
            android.view.View r6 = r12.findViewById(r6)
            android.view.ViewGroup$LayoutParams r12 = r6.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r12 = (android.widget.RelativeLayout.LayoutParams) r12
            r18 = r1
            r1 = 0
            r12.addRule(r13, r1)
            r1 = -1
            r12.addRule(r14, r1)
            r12.addRule(r3, r1)
            r1 = 0
            r12.addRule(r4, r1)
            r12.addRule(r9, r1)
            r12.addRule(r10, r1)
            r12.addRule(r15, r1)
            r12.addRule(r8, r1)
            r6.setLayoutParams(r12)
            float r1 = (float) r2
            r0.setViewRotation(r6, r1)
            java.util.ArrayList r12 = new java.util.ArrayList
            r12.<init>()
            r19 = r6
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r6 = r0.ui_placement
            r20 = r2
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r2 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.UIPLACEMENT_TOP
            if (r6 != r2) goto L_0x0128
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099692(0x7f06002c, float:1.7811744E38)
            android.view.View r2 = r2.findViewById(r6)
            android.view.ViewGroup$LayoutParams r6 = r2.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r6 = (android.widget.RelativeLayout.LayoutParams) r6
            r22 = r8
            r21 = r11
            r8 = 0
            r11 = 9
            r6.addRule(r11, r8)
            r8 = -1
            r11 = 11
            r6.addRule(r11, r8)
            r6.addRule(r7, r8)
            r8 = 0
            r6.addRule(r5, r8)
            r11 = r16
            r6.addRule(r11, r8)
            r11 = r17
            r6.addRule(r11, r8)
            r6.addRule(r8, r8)
            r11 = 1
            r6.addRule(r11, r8)
            r2.setLayoutParams(r6)
            r0.setViewRotation(r2, r1)
            goto L_0x0138
        L_0x0128:
            r22 = r8
            r21 = r11
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099692(0x7f06002c, float:1.7811744E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
        L_0x0138:
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099723(0x7f06004b, float:1.7811807E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099717(0x7f060045, float:1.7811795E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099677(0x7f06001d, float:1.7811714E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099679(0x7f06001f, float:1.7811718E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099744(0x7f060060, float:1.781185E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099672(0x7f060018, float:1.7811704E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099729(0x7f060051, float:1.781182E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099739(0x7f06005b, float:1.781184E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099726(0x7f06004e, float:1.7811813E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099659(0x7f06000b, float:1.7811677E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099671(0x7f060017, float:1.7811702E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099685(0x7f060025, float:1.781173E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099658(0x7f06000a, float:1.7811675E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r6 = 2131099703(0x7f060037, float:1.7811767E38)
            android.view.View r2 = r2.findViewById(r6)
            r12.add(r2)
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>(r12)
            net.sourceforge.opencamera.MainActivity r6 = r0.main_activity
            r8 = 2131099743(0x7f06005f, float:1.7811848E38)
            android.view.View r6 = r6.findViewById(r8)
            r2.add(r6)
            net.sourceforge.opencamera.MainActivity r6 = r0.main_activity
            r8 = 2131099724(0x7f06004c, float:1.781181E38)
            android.view.View r6 = r6.findViewById(r8)
            r2.add(r6)
            java.util.Iterator r2 = r2.iterator()
        L_0x0201:
            boolean r6 = r2.hasNext()
            if (r6 == 0) goto L_0x0240
            java.lang.Object r6 = r2.next()
            android.view.View r6 = (android.view.View) r6
            android.view.ViewGroup$LayoutParams r8 = r6.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r8 = (android.widget.RelativeLayout.LayoutParams) r8
            r11 = 0
            r8.addRule(r13, r11)
            r8.addRule(r14, r11)
            r23 = r2
            r2 = -1
            r8.addRule(r3, r2)
            r8.addRule(r4, r11)
            r8.addRule(r9, r11)
            r8.addRule(r10, r11)
            int r2 = r19.getId()
            r8.addRule(r15, r2)
            r2 = r22
            r8.addRule(r2, r11)
            r6.setLayoutParams(r8)
            r0.setViewRotation(r6, r1)
            r19 = r6
            r2 = r23
            goto L_0x0201
        L_0x0240:
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            android.content.res.Resources r2 = r2.getResources()
            r3 = 2130968600(0x7f040018, float:1.7545858E38)
            int r2 = r2.getDimensionPixelSize(r3)
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r3 = r0.ui_placement
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r4 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.UIPLACEMENT_TOP
            if (r3 != r4) goto L_0x02c2
            java.util.Iterator r3 = r12.iterator()
            r4 = 0
            r6 = 0
            r8 = 0
        L_0x025a:
            boolean r9 = r3.hasNext()
            if (r9 == 0) goto L_0x0273
            java.lang.Object r9 = r3.next()
            android.view.View r9 = (android.view.View) r9
            int r10 = r9.getVisibility()
            if (r10 != 0) goto L_0x025a
            if (r6 != 0) goto L_0x026f
            r6 = r9
        L_0x026f:
            int r4 = r4 + 1
            r8 = r9
            goto L_0x025a
        L_0x0273:
            if (r4 <= 0) goto L_0x02bf
            int r3 = r4 * r2
            r9 = r21
            if (r3 <= r9) goto L_0x027e
            int r2 = r9 / r4
            goto L_0x0287
        L_0x027e:
            r10 = 1
            if (r4 <= r10) goto L_0x0287
            int r11 = r9 - r3
            int r4 = r4 - r10
            int r3 = r11 / r4
            goto L_0x0288
        L_0x0287:
            r3 = 0
        L_0x0288:
            java.util.Iterator r4 = r12.iterator()
        L_0x028c:
            boolean r10 = r4.hasNext()
            if (r10 == 0) goto L_0x02bc
            java.lang.Object r10 = r4.next()
            android.view.View r10 = (android.view.View) r10
            int r11 = r10.getVisibility()
            if (r11 != 0) goto L_0x028c
            android.view.ViewGroup$LayoutParams r11 = r10.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r11 = (android.widget.RelativeLayout.LayoutParams) r11
            if (r10 != r6) goto L_0x02a8
            r12 = 0
            goto L_0x02aa
        L_0x02a8:
            int r12 = r3 / 2
        L_0x02aa:
            if (r10 != r8) goto L_0x02ae
            r13 = 0
            goto L_0x02b0
        L_0x02ae:
            int r13 = r3 / 2
        L_0x02b0:
            r14 = 0
            r11.setMargins(r14, r12, r14, r13)
            r11.width = r2
            r11.height = r2
            r10.setLayoutParams(r11)
            goto L_0x028c
        L_0x02bc:
            r0.top_icon = r6
            goto L_0x02e6
        L_0x02bf:
            r9 = r21
            goto L_0x02e6
        L_0x02c2:
            r9 = r21
            java.util.Iterator r3 = r12.iterator()
        L_0x02c8:
            boolean r4 = r3.hasNext()
            if (r4 == 0) goto L_0x02e6
            java.lang.Object r4 = r3.next()
            android.view.View r4 = (android.view.View) r4
            android.view.ViewGroup$LayoutParams r6 = r4.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r6 = (android.widget.RelativeLayout.LayoutParams) r6
            r8 = 0
            r6.setMargins(r8, r8, r8, r8)
            r6.width = r2
            r6.height = r2
            r4.setLayoutParams(r6)
            goto L_0x02c8
        L_0x02e6:
            r8 = 0
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r3 = 2131099735(0x7f060057, float:1.7811832E38)
            android.view.View r2 = r2.findViewById(r3)
            android.view.ViewGroup$LayoutParams r3 = r2.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r3 = (android.widget.RelativeLayout.LayoutParams) r3
            r4 = 9
            r3.addRule(r4, r8)
            r6 = 11
            r10 = -1
            r3.addRule(r6, r10)
            r2.setLayoutParams(r3)
            r0.setViewRotation(r2, r1)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r3 = 2131099730(0x7f060052, float:1.7811821E38)
            android.view.View r2 = r2.findViewById(r3)
            android.view.ViewGroup$LayoutParams r3 = r2.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r3 = (android.widget.RelativeLayout.LayoutParams) r3
            r3.addRule(r4, r8)
            r3.addRule(r6, r10)
            r2.setLayoutParams(r3)
            r0.setViewRotation(r2, r1)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r3 = 2131099716(0x7f060044, float:1.7811793E38)
            android.view.View r2 = r2.findViewById(r3)
            android.view.ViewGroup$LayoutParams r3 = r2.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r3 = (android.widget.RelativeLayout.LayoutParams) r3
            r3.addRule(r4, r8)
            r3.addRule(r6, r10)
            r2.setLayoutParams(r3)
            r0.setViewRotation(r2, r1)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r3 = 2131099663(0x7f06000f, float:1.7811686E38)
            android.view.View r2 = r2.findViewById(r3)
            android.view.ViewGroup$LayoutParams r3 = r2.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r3 = (android.widget.RelativeLayout.LayoutParams) r3
            r3.addRule(r4, r8)
            r3.addRule(r6, r10)
            r2.setLayoutParams(r3)
            r0.setViewRotation(r2, r1)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r3 = 2131099731(0x7f060053, float:1.7811823E38)
            android.view.View r2 = r2.findViewById(r3)
            android.view.ViewGroup$LayoutParams r3 = r2.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r3 = (android.widget.RelativeLayout.LayoutParams) r3
            r3.addRule(r4, r8)
            r3.addRule(r6, r10)
            r2.setLayoutParams(r3)
            r0.setViewRotation(r2, r1)
            net.sourceforge.opencamera.MainActivity r2 = r0.main_activity
            r3 = 2131099736(0x7f060058, float:1.7811834E38)
            android.view.View r2 = r2.findViewById(r3)
            android.view.ViewGroup$LayoutParams r3 = r2.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r3 = (android.widget.RelativeLayout.LayoutParams) r3
            r3.addRule(r4, r8)
            r3.addRule(r6, r10)
            r2.setLayoutParams(r3)
            r0.setViewRotation(r2, r1)
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            r2 = 2131099749(0x7f060065, float:1.781186E38)
            android.view.View r1 = r1.findViewById(r2)
            android.view.ViewGroup$LayoutParams r3 = r1.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r3 = (android.widget.RelativeLayout.LayoutParams) r3
            r3.addRule(r4, r8)
            r3.addRule(r6, r10)
            r3.addRule(r7, r8)
            r3.addRule(r5, r10)
            r1.setLayoutParams(r3)
            r3 = 1127481344(0x43340000, float:180.0)
            r1.setRotation(r3)
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            r3 = 2131099750(0x7f060066, float:1.7811862E38)
            android.view.View r1 = r1.findViewById(r3)
            android.view.ViewGroup$LayoutParams r4 = r1.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r4 = (android.widget.RelativeLayout.LayoutParams) r4
            java.lang.String r6 = "preference_show_zoom_controls"
            r10 = r18
            boolean r6 = r10.getBoolean(r6, r8)
            if (r6 == 0) goto L_0x03ed
            r6 = 5
            r4.addRule(r6, r8)
            r6 = 7
            r4.addRule(r6, r2)
            r6 = r16
            r4.addRule(r6, r2)
            r2 = r17
            r4.addRule(r2, r8)
            r10 = 9
            r4.addRule(r10, r8)
            r11 = 11
            r4.addRule(r11, r8)
            r4.addRule(r7, r8)
            r4.addRule(r5, r8)
            goto L_0x0410
        L_0x03ed:
            r6 = r16
            r2 = r17
            r10 = 9
            r11 = 11
            r4.addRule(r10, r8)
            r10 = -1
            r4.addRule(r11, r10)
            r4.addRule(r7, r8)
            r4.addRule(r5, r10)
            r10 = 5
            r4.addRule(r10, r8)
            r10 = 7
            r4.addRule(r10, r8)
            r4.addRule(r6, r8)
            r4.addRule(r2, r8)
        L_0x0410:
            r1.setLayoutParams(r4)
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            r4 = 2131099690(0x7f06002a, float:1.781174E38)
            android.view.View r1 = r1.findViewById(r4)
            android.view.ViewGroup$LayoutParams r4 = r1.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r4 = (android.widget.RelativeLayout.LayoutParams) r4
            r10 = 2131099719(0x7f060047, float:1.78118E38)
            r11 = 5
            r4.addRule(r11, r10)
            r10 = 7
            r4.addRule(r10, r8)
            r4.addRule(r8, r3)
            r10 = 1
            r4.addRule(r10, r8)
            r4.addRule(r7, r8)
            r10 = -1
            r4.addRule(r5, r10)
            r1.setLayoutParams(r4)
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            r4 = 2131099689(0x7f060029, float:1.7811738E38)
            android.view.View r1 = r1.findViewById(r4)
            android.view.ViewGroup$LayoutParams r4 = r1.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r4 = (android.widget.RelativeLayout.LayoutParams) r4
            r10 = 2131099719(0x7f060047, float:1.78118E38)
            r11 = 5
            r4.addRule(r11, r10)
            r10 = 7
            r4.addRule(r10, r8)
            r4.addRule(r8, r3)
            r3 = 1
            r4.addRule(r3, r8)
            r3 = 2131099690(0x7f06002a, float:1.781174E38)
            r4.addRule(r6, r3)
            r4.addRule(r2, r8)
            r1.setLayoutParams(r4)
            goto L_0x0473
        L_0x046c:
            r20 = r2
            r9 = r11
            r6 = r16
            r2 = r17
        L_0x0473:
            if (r25 != 0) goto L_0x053d
            if (r20 == 0) goto L_0x0489
            r1 = 180(0xb4, float:2.52E-43)
            r3 = r20
            if (r3 != r1) goto L_0x047e
            goto L_0x048b
        L_0x047e:
            r1 = 250(0xfa, float:3.5E-43)
            r4 = 1
            int r8 = r0.getMaxHeightDp(r4)
            if (r1 <= r8) goto L_0x048d
            r1 = r8
            goto L_0x048d
        L_0x0489:
            r3 = r20
        L_0x048b:
            r1 = 350(0x15e, float:4.9E-43)
        L_0x048d:
            r4 = 50
            net.sourceforge.opencamera.MainActivity r8 = r0.main_activity
            android.content.res.Resources r8 = r8.getResources()
            android.util.DisplayMetrics r8 = r8.getDisplayMetrics()
            float r8 = r8.density
            float r1 = (float) r1
            float r1 = r1 * r8
            r10 = 1056964608(0x3f000000, float:0.5)
            float r1 = r1 + r10
            int r1 = (int) r1
            float r4 = (float) r4
            float r4 = r4 * r8
            float r4 = r4 + r10
            int r4 = (int) r4
            net.sourceforge.opencamera.MainActivity r8 = r0.main_activity
            r11 = 2131099725(0x7f06004d, float:1.7811811E38)
            android.view.View r8 = r8.findViewById(r11)
            float r11 = (float) r3
            r0.setViewRotation(r8, r11)
            r11 = 0
            r8.setTranslationX(r11)
            r8.setTranslationY(r11)
            r11 = 90
            if (r3 == r11) goto L_0x04d2
            r11 = 270(0x10e, float:3.78E-43)
            if (r3 != r11) goto L_0x04c4
            goto L_0x04d2
        L_0x04c4:
            if (r3 != 0) goto L_0x04cb
            float r11 = (float) r4
            r8.setTranslationY(r11)
            goto L_0x04d8
        L_0x04cb:
            int r11 = r4 * -1
            float r11 = (float) r11
            r8.setTranslationY(r11)
            goto L_0x04d8
        L_0x04d2:
            int r11 = r4 * 2
            float r11 = (float) r11
            r8.setTranslationX(r11)
        L_0x04d8:
            net.sourceforge.opencamera.MainActivity r8 = r0.main_activity
            r11 = 2131099680(0x7f060020, float:1.781172E38)
            android.view.View r8 = r8.findViewById(r11)
            android.view.ViewGroup$LayoutParams r11 = r8.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r11 = (android.widget.RelativeLayout.LayoutParams) r11
            r11.width = r1
            r11.height = r4
            r8.setLayoutParams(r11)
            net.sourceforge.opencamera.MainActivity r8 = r0.main_activity
            r11 = 2131099682(0x7f060022, float:1.7811724E38)
            android.view.View r8 = r8.findViewById(r11)
            r8.setAlpha(r10)
            net.sourceforge.opencamera.MainActivity r8 = r0.main_activity
            r10 = 2131099700(0x7f060034, float:1.781176E38)
            android.view.View r8 = r8.findViewById(r10)
            android.view.ViewGroup$LayoutParams r10 = r8.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r10 = (android.widget.RelativeLayout.LayoutParams) r10
            r10.width = r1
            r10.height = r4
            r8.setLayoutParams(r10)
            net.sourceforge.opencamera.MainActivity r8 = r0.main_activity
            r10 = 2131099683(0x7f060023, float:1.7811726E38)
            android.view.View r8 = r8.findViewById(r10)
            android.view.ViewGroup$LayoutParams r10 = r8.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r10 = (android.widget.RelativeLayout.LayoutParams) r10
            r10.width = r1
            r10.height = r4
            r8.setLayoutParams(r10)
            net.sourceforge.opencamera.MainActivity r8 = r0.main_activity
            r10 = 2131099745(0x7f060061, float:1.7811852E38)
            android.view.View r8 = r8.findViewById(r10)
            android.view.ViewGroup$LayoutParams r10 = r8.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r10 = (android.widget.RelativeLayout.LayoutParams) r10
            r10.width = r1
            r10.height = r4
            r8.setLayoutParams(r10)
            goto L_0x053f
        L_0x053d:
            r3 = r20
        L_0x053f:
            boolean r1 = r24.popupIsOpen()
            if (r1 == 0) goto L_0x05a2
            net.sourceforge.opencamera.MainActivity r1 = r0.main_activity
            r4 = 2131099718(0x7f060046, float:1.7811797E38)
            android.view.View r1 = r1.findViewById(r4)
            android.view.ViewGroup$LayoutParams r4 = r1.getLayoutParams()
            android.widget.RelativeLayout$LayoutParams r4 = (android.widget.RelativeLayout.LayoutParams) r4
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r8 = r0.ui_placement
            net.sourceforge.opencamera.ui.MainUI$UIPlacement r10 = net.sourceforge.opencamera.p004ui.MainUI.UIPlacement.UIPLACEMENT_TOP
            if (r8 != r10) goto L_0x0577
            r8 = 7
            r10 = 0
            r4.addRule(r8, r10)
            r4.addRule(r6, r10)
            r4.addRule(r2, r10)
            r4.addRule(r10, r10)
            r11 = 2131099717(0x7f060045, float:1.7811795E38)
            r12 = 1
            r4.addRule(r12, r11)
            r13 = -1
            r4.addRule(r7, r13)
            r4.addRule(r5, r13)
            goto L_0x0593
        L_0x0577:
            r8 = 7
            r10 = 0
            r11 = 2131099717(0x7f060045, float:1.7811795E38)
            r12 = 1
            r13 = -1
            r4.addRule(r8, r11)
            r4.addRule(r6, r10)
            r4.addRule(r2, r11)
            r4.addRule(r10, r10)
            r4.addRule(r12, r10)
            r4.addRule(r7, r10)
            r4.addRule(r5, r13)
        L_0x0593:
            r1.setLayoutParams(r4)
            android.view.ViewTreeObserver r2 = r1.getViewTreeObserver()
            net.sourceforge.opencamera.ui.MainUI$1 r4 = new net.sourceforge.opencamera.ui.MainUI$1
            r4.<init>(r3, r9, r1)
            r2.addOnGlobalLayoutListener(r4)
        L_0x05a2:
            if (r25 != 0) goto L_0x05a7
            r24.setTakePhotoIcon()
        L_0x05a7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.MainUI.layoutUI(boolean):void");
    }

    /* access modifiers changed from: private */
    public void setPopupViewRotation(int i, int i2) {
        View findViewById = this.main_activity.findViewById(C0316R.C0318id.popup_container);
        setViewRotation(findViewById, (float) i);
        float f = 0.0f;
        findViewById.setTranslationX(0.0f);
        findViewById.setTranslationY(0.0f);
        int width = findViewById.getWidth();
        int height = findViewById.getHeight();
        this.test_saved_popup_width = width;
        this.test_saved_popup_height = height;
        PopupView popupView = this.popup_view;
        if (popupView != null) {
            double d = (double) width;
            double totalWidth = (double) popupView.getTotalWidth();
            Double.isNaN(totalWidth);
            if (d > totalWidth * 1.2d) {
                Log.e(TAG, "### popup view is too big?!");
                this.force_destroy_popup = cache_popup;
                if (i != 0 || i == 180) {
                    findViewById.setPivotX(((float) width) / 2.0f);
                    findViewById.setPivotY(((float) height) / 2.0f);
                } else if (this.ui_placement == UIPlacement.UIPLACEMENT_TOP) {
                    findViewById.setPivotX(0.0f);
                    findViewById.setPivotY(0.0f);
                    if (i == 90) {
                        findViewById.setTranslationX((float) height);
                        return;
                    } else if (i == 270) {
                        findViewById.setTranslationY((float) i2);
                        return;
                    } else {
                        return;
                    }
                } else {
                    float f2 = (float) width;
                    findViewById.setPivotX(f2);
                    if (this.ui_placement != UIPlacement.UIPLACEMENT_RIGHT) {
                        f = (float) height;
                    }
                    findViewById.setPivotY(f);
                    if (this.ui_placement == UIPlacement.UIPLACEMENT_RIGHT) {
                        if (i == 90) {
                            findViewById.setTranslationY(f2);
                            return;
                        } else if (i == 270) {
                            findViewById.setTranslationX((float) (-height));
                            return;
                        } else {
                            return;
                        }
                    } else if (i == 90) {
                        findViewById.setTranslationX((float) (-height));
                        return;
                    } else if (i == 270) {
                        findViewById.setTranslationY((float) (-width));
                        return;
                    } else {
                        return;
                    }
                }
            }
        }
        this.force_destroy_popup = false;
        if (i != 0) {
        }
        findViewById.setPivotX(((float) width) / 2.0f);
        findViewById.setPivotY(((float) height) / 2.0f);
    }

    public void setTakePhotoIcon() {
        int i;
        int i2;
        if (this.main_activity.getPreview() != null) {
            ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.take_photo);
            boolean isVideo = this.main_activity.getPreview().isVideo();
            int i3 = C0316R.string.switch_to_video;
            if (isVideo) {
                i2 = this.main_activity.getPreview().isVideoRecording() ? C0316R.C0317drawable.take_video_recording : C0316R.C0317drawable.take_video_selector;
                i = this.main_activity.getPreview().isVideoRecording() ? C0316R.string.stop_video : C0316R.string.start_video;
                i3 = C0316R.string.switch_to_photo;
            } else if (this.main_activity.getApplicationInterface().getPhotoMode() != PhotoMode.Panorama || !this.main_activity.getApplicationInterface().getGyroSensor().isRecording()) {
                i2 = C0316R.C0317drawable.take_photo_selector;
                i = C0316R.string.take_photo;
            } else {
                i2 = C0316R.C0317drawable.baseline_check_white_48;
                i = C0316R.string.finish_panorama;
            }
            imageButton.setImageResource(i2);
            imageButton.setContentDescription(this.main_activity.getResources().getString(i));
            imageButton.setTag(Integer.valueOf(i2));
            ImageButton imageButton2 = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.switch_video);
            imageButton2.setContentDescription(this.main_activity.getResources().getString(i3));
            int i4 = this.main_activity.getPreview().isVideo() ? C0316R.C0317drawable.take_photo : C0316R.C0317drawable.take_video;
            imageButton2.setImageResource(i4);
            imageButton2.setTag(Integer.valueOf(i4));
        }
    }

    public void setSwitchCameraContentDescription() {
        if (this.main_activity.getPreview() != null && this.main_activity.getPreview().canSwitchCamera()) {
            ((ImageButton) this.main_activity.findViewById(C0316R.C0318id.switch_camera)).setContentDescription(this.main_activity.getResources().getString(this.main_activity.getPreview().getCameraControllerManager().isFrontFacing(this.main_activity.getNextCameraId()) ? C0316R.string.switch_to_front_camera : C0316R.string.switch_to_back_camera));
        }
    }

    public void setPauseVideoContentDescription() {
        int i;
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.pause_video);
        if (this.main_activity.getPreview().isVideoRecordingPaused()) {
            i = C0316R.string.resume_video;
            imageButton.setImageResource(C0316R.C0317drawable.ic_play_circle_outline_white_48dp);
        } else {
            i = C0316R.string.pause_video;
            imageButton.setImageResource(C0316R.C0317drawable.ic_pause_circle_outline_white_48dp);
        }
        imageButton.setContentDescription(this.main_activity.getResources().getString(i));
    }

    public UIPlacement getUIPlacement() {
        return this.ui_placement;
    }

    public void updateRemoteConnectionIcon() {
        View findViewById = this.main_activity.findViewById(C0316R.C0318id.kraken_icon);
        if (this.main_activity.getBluetoothRemoteControl().remoteConnected()) {
            findViewById.setVisibility(0);
        } else {
            findViewById.setVisibility(8);
        }
    }

    public void onOrientationChanged(int i) {
        if (i != -1) {
            int abs = Math.abs(i - this.current_orientation);
            if (abs > 180) {
                abs = 360 - abs;
            }
            if (abs > 60) {
                int i2 = (((i + 45) / 90) * 90) % 360;
                if (i2 != this.current_orientation) {
                    this.current_orientation = i2;
                    this.view_rotate_animation = cache_popup;
                    layoutUI();
                    this.view_rotate_animation = false;
                }
            }
        }
    }

    public boolean showExposureLockIcon() {
        if (!this.main_activity.getPreview().supportsExposureLock()) {
            return false;
        }
        return PreferenceManager.getDefaultSharedPreferences(this.main_activity).getBoolean(PreferenceKeys.ShowExposureLockPreferenceKey, cache_popup);
    }

    public boolean showWhiteBalanceLockIcon() {
        if (!this.main_activity.getPreview().supportsWhiteBalanceLock()) {
            return false;
        }
        return PreferenceManager.getDefaultSharedPreferences(this.main_activity).getBoolean(PreferenceKeys.ShowWhiteBalanceLockPreferenceKey, false);
    }

    public boolean showCycleRawIcon() {
        if (this.main_activity.getPreview().supportsRaw() && this.main_activity.getApplicationInterface().isRawAllowed(this.main_activity.getApplicationInterface().getPhotoMode())) {
            return PreferenceManager.getDefaultSharedPreferences(this.main_activity).getBoolean(PreferenceKeys.ShowCycleRawPreferenceKey, false);
        }
        return false;
    }

    public boolean showStoreLocationIcon() {
        return PreferenceManager.getDefaultSharedPreferences(this.main_activity).getBoolean(PreferenceKeys.ShowStoreLocationPreferenceKey, false);
    }

    public boolean showTextStampIcon() {
        return PreferenceManager.getDefaultSharedPreferences(this.main_activity).getBoolean(PreferenceKeys.ShowTextStampPreferenceKey, false);
    }

    public boolean showStampIcon() {
        return PreferenceManager.getDefaultSharedPreferences(this.main_activity).getBoolean(PreferenceKeys.ShowStampPreferenceKey, false);
    }

    public boolean showAutoLevelIcon() {
        if (!this.main_activity.supportsAutoStabilise()) {
            return false;
        }
        return PreferenceManager.getDefaultSharedPreferences(this.main_activity).getBoolean(PreferenceKeys.ShowAutoLevelPreferenceKey, false);
    }

    public boolean showCycleFlashIcon() {
        if (!this.main_activity.getPreview().supportsFlash()) {
            return false;
        }
        return PreferenceManager.getDefaultSharedPreferences(this.main_activity).getBoolean(PreferenceKeys.ShowCycleFlashPreferenceKey, false);
    }

    public boolean showFaceDetectionIcon() {
        if (!this.main_activity.getPreview().supportsFaceDetection()) {
            return false;
        }
        return PreferenceManager.getDefaultSharedPreferences(this.main_activity).getBoolean(PreferenceKeys.ShowFaceDetectionPreferenceKey, false);
    }

    public void setImmersiveMode(final boolean z) {
        this.immersive_mode = z;
        this.main_activity.runOnUiThread(new Runnable() {
            public void run() {
                SharedPreferences sharedPreferences;
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainUI.this.main_activity);
                int i = z ? 8 : 0;
                View findViewById = MainUI.this.main_activity.findViewById(C0316R.C0318id.switch_camera);
                View findViewById2 = MainUI.this.main_activity.findViewById(C0316R.C0318id.switch_video);
                View findViewById3 = MainUI.this.main_activity.findViewById(C0316R.C0318id.exposure);
                View findViewById4 = MainUI.this.main_activity.findViewById(C0316R.C0318id.exposure_lock);
                View findViewById5 = MainUI.this.main_activity.findViewById(C0316R.C0318id.white_balance_lock);
                View findViewById6 = MainUI.this.main_activity.findViewById(C0316R.C0318id.cycle_raw);
                View findViewById7 = MainUI.this.main_activity.findViewById(C0316R.C0318id.store_location);
                View findViewById8 = MainUI.this.main_activity.findViewById(C0316R.C0318id.text_stamp);
                View findViewById9 = MainUI.this.main_activity.findViewById(C0316R.C0318id.stamp);
                View findViewById10 = MainUI.this.main_activity.findViewById(C0316R.C0318id.auto_level);
                View findViewById11 = MainUI.this.main_activity.findViewById(C0316R.C0318id.cycle_flash);
                View findViewById12 = MainUI.this.main_activity.findViewById(C0316R.C0318id.face_detection);
                SharedPreferences sharedPreferences2 = defaultSharedPreferences;
                View findViewById13 = MainUI.this.main_activity.findViewById(C0316R.C0318id.audio_control);
                View findViewById14 = MainUI.this.main_activity.findViewById(C0316R.C0318id.popup);
                View findViewById15 = MainUI.this.main_activity.findViewById(C0316R.C0318id.gallery);
                View findViewById16 = MainUI.this.main_activity.findViewById(C0316R.C0318id.settings);
                View findViewById17 = MainUI.this.main_activity.findViewById(C0316R.C0318id.zoom);
                View findViewById18 = MainUI.this.main_activity.findViewById(C0316R.C0318id.zoom_seekbar);
                if (MainUI.this.main_activity.getPreview().getCameraControllerManager().getNumberOfCameras() > 1) {
                    findViewById.setVisibility(i);
                }
                findViewById2.setVisibility(i);
                if (MainUI.this.main_activity.supportsExposureButton()) {
                    findViewById3.setVisibility(i);
                }
                if (MainUI.this.showExposureLockIcon()) {
                    findViewById4.setVisibility(i);
                }
                if (MainUI.this.showWhiteBalanceLockIcon()) {
                    findViewById5.setVisibility(i);
                }
                if (MainUI.this.showCycleRawIcon()) {
                    findViewById6.setVisibility(i);
                }
                if (MainUI.this.showStoreLocationIcon()) {
                    findViewById7.setVisibility(i);
                }
                if (MainUI.this.showTextStampIcon()) {
                    findViewById8.setVisibility(i);
                }
                if (MainUI.this.showStampIcon()) {
                    findViewById9.setVisibility(i);
                }
                if (MainUI.this.showAutoLevelIcon()) {
                    findViewById10.setVisibility(i);
                }
                if (MainUI.this.showCycleFlashIcon()) {
                    findViewById11.setVisibility(i);
                }
                if (MainUI.this.showFaceDetectionIcon()) {
                    findViewById12.setVisibility(i);
                }
                if (MainUI.this.main_activity.hasAudioControl()) {
                    findViewById13.setVisibility(i);
                }
                findViewById14.setVisibility(i);
                findViewById15.setVisibility(i);
                findViewById16.setVisibility(i);
                if (MainUI.this.main_activity.getPreview().supportsZoom()) {
                    sharedPreferences = sharedPreferences2;
                    if (sharedPreferences.getBoolean(PreferenceKeys.ShowZoomControlsPreferenceKey, false)) {
                        findViewById17.setVisibility(i);
                    }
                } else {
                    sharedPreferences = sharedPreferences2;
                }
                if (MainUI.this.main_activity.getPreview().supportsZoom() && sharedPreferences.getBoolean(PreferenceKeys.ShowZoomSliderControlsPreferenceKey, MainUI.cache_popup)) {
                    findViewById18.setVisibility(i);
                }
                if (sharedPreferences.getString(PreferenceKeys.ImmersiveModePreferenceKey, "immersive_mode_low_profile").equals("immersive_mode_everything")) {
                    if (sharedPreferences.getBoolean(PreferenceKeys.ShowTakePhotoPreferenceKey, MainUI.cache_popup)) {
                        MainUI.this.main_activity.findViewById(C0316R.C0318id.take_photo).setVisibility(i);
                    }
                    if (VERSION.SDK_INT >= 24 && MainUI.this.main_activity.getPreview().isVideoRecording()) {
                        MainUI.this.main_activity.findViewById(C0316R.C0318id.pause_video).setVisibility(i);
                    }
                    if (MainUI.this.main_activity.getPreview().supportsPhotoVideoRecording() && MainUI.this.main_activity.getApplicationInterface().usePhotoVideoRecording() && MainUI.this.main_activity.getPreview().isVideoRecording()) {
                        MainUI.this.main_activity.findViewById(C0316R.C0318id.take_photo_when_video_recording).setVisibility(i);
                    }
                    if (MainUI.this.main_activity.getApplicationInterface().getGyroSensor().isRecording()) {
                        MainUI.this.main_activity.findViewById(C0316R.C0318id.cancel_panorama).setVisibility(i);
                    }
                }
                if (!z) {
                    MainUI.this.showGUI();
                }
            }
        });
    }

    public boolean inImmersiveMode() {
        return this.immersive_mode;
    }

    public void showGUI(boolean z, boolean z2) {
        if (z2) {
            this.show_gui_video = z;
        } else {
            this.show_gui_photo = z;
        }
        showGUI();
    }

    public void showGUI() {
        if (!inImmersiveMode()) {
            if ((this.show_gui_photo || this.show_gui_video) && this.main_activity.usingKitKatImmersiveMode()) {
                this.main_activity.initImmersiveMode();
            }
            this.main_activity.runOnUiThread(new Runnable() {
                public void run() {
                    int i;
                    int i2;
                    boolean isRecording = MainUI.this.main_activity.getApplicationInterface().getGyroSensor().isRecording();
                    if (!isRecording && MainUI.this.show_gui_photo && MainUI.this.show_gui_video) {
                        i = 0;
                    } else {
                        i = 8;
                    }
                    if (!isRecording && MainUI.this.show_gui_photo) {
                        i2 = 0;
                    } else {
                        i2 = 8;
                    }
                    View findViewById = MainUI.this.main_activity.findViewById(C0316R.C0318id.switch_camera);
                    View findViewById2 = MainUI.this.main_activity.findViewById(C0316R.C0318id.switch_video);
                    View findViewById3 = MainUI.this.main_activity.findViewById(C0316R.C0318id.exposure);
                    View findViewById4 = MainUI.this.main_activity.findViewById(C0316R.C0318id.exposure_lock);
                    View findViewById5 = MainUI.this.main_activity.findViewById(C0316R.C0318id.white_balance_lock);
                    View findViewById6 = MainUI.this.main_activity.findViewById(C0316R.C0318id.cycle_raw);
                    View findViewById7 = MainUI.this.main_activity.findViewById(C0316R.C0318id.store_location);
                    View findViewById8 = MainUI.this.main_activity.findViewById(C0316R.C0318id.text_stamp);
                    View findViewById9 = MainUI.this.main_activity.findViewById(C0316R.C0318id.stamp);
                    View findViewById10 = MainUI.this.main_activity.findViewById(C0316R.C0318id.auto_level);
                    View findViewById11 = MainUI.this.main_activity.findViewById(C0316R.C0318id.cycle_flash);
                    View findViewById12 = MainUI.this.main_activity.findViewById(C0316R.C0318id.face_detection);
                    View findViewById13 = MainUI.this.main_activity.findViewById(C0316R.C0318id.audio_control);
                    View findViewById14 = MainUI.this.main_activity.findViewById(C0316R.C0318id.popup);
                    if (MainUI.this.main_activity.getPreview().getCameraControllerManager().getNumberOfCameras() > 1) {
                        findViewById.setVisibility(i);
                    }
                    findViewById2.setVisibility(i);
                    if (MainUI.this.main_activity.supportsExposureButton()) {
                        findViewById3.setVisibility(i2);
                    }
                    if (MainUI.this.showExposureLockIcon()) {
                        findViewById4.setVisibility(i2);
                    }
                    if (MainUI.this.showWhiteBalanceLockIcon()) {
                        findViewById5.setVisibility(i2);
                    }
                    if (MainUI.this.showCycleRawIcon()) {
                        findViewById6.setVisibility(i);
                    }
                    if (MainUI.this.showStoreLocationIcon()) {
                        findViewById7.setVisibility(i);
                    }
                    if (MainUI.this.showTextStampIcon()) {
                        findViewById8.setVisibility(i);
                    }
                    if (MainUI.this.showStampIcon()) {
                        findViewById9.setVisibility(i);
                    }
                    if (MainUI.this.showAutoLevelIcon()) {
                        findViewById10.setVisibility(i);
                    }
                    if (MainUI.this.showCycleFlashIcon()) {
                        findViewById11.setVisibility(i);
                    }
                    if (MainUI.this.showFaceDetectionIcon()) {
                        findViewById12.setVisibility(i);
                    }
                    if (MainUI.this.main_activity.hasAudioControl()) {
                        findViewById13.setVisibility(i);
                    }
                    if (!MainUI.this.show_gui_photo || !MainUI.this.show_gui_video) {
                        MainUI.this.closePopup();
                    }
                    View findViewById15 = MainUI.this.main_activity.findViewById(C0316R.C0318id.kraken_icon);
                    if (MainUI.this.main_activity.getBluetoothRemoteControl().remoteConnected()) {
                        findViewById15.setVisibility(0);
                    } else {
                        findViewById15.setVisibility(8);
                    }
                    if (MainUI.this.main_activity.getPreview().supportsFlash()) {
                        i = i2;
                    }
                    findViewById14.setVisibility(i);
                    if (MainUI.this.show_gui_photo && MainUI.this.show_gui_video) {
                        MainUI.this.layoutUI();
                    }
                }
            });
        }
    }

    public void updateExposureLockIcon() {
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.exposure_lock);
        boolean isExposureLocked = this.main_activity.getPreview().isExposureLocked();
        imageButton.setImageResource(isExposureLocked ? C0316R.C0317drawable.exposure_locked : C0316R.C0317drawable.exposure_unlocked);
        imageButton.setContentDescription(this.main_activity.getResources().getString(isExposureLocked ? C0316R.string.exposure_unlock : C0316R.string.exposure_lock));
    }

    public void updateWhiteBalanceLockIcon() {
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.white_balance_lock);
        boolean isWhiteBalanceLocked = this.main_activity.getPreview().isWhiteBalanceLocked();
        imageButton.setImageResource(isWhiteBalanceLocked ? C0316R.C0317drawable.white_balance_locked : C0316R.C0317drawable.white_balance_unlocked);
        imageButton.setContentDescription(this.main_activity.getResources().getString(isWhiteBalanceLocked ? C0316R.string.white_balance_unlock : C0316R.string.white_balance_lock));
    }

    public void updateCycleRawIcon() {
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.cycle_raw);
        if (this.main_activity.getApplicationInterface().getRawPref() != RawPref.RAWPREF_JPEG_DNG) {
            imageButton.setImageResource(C0316R.C0317drawable.raw_off_icon);
        } else if (this.main_activity.getApplicationInterface().isRawOnly()) {
            imageButton.setImageResource(C0316R.C0317drawable.raw_only_icon);
        } else {
            imageButton.setImageResource(C0316R.C0317drawable.raw_icon);
        }
    }

    public void updateStoreLocationIcon() {
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.store_location);
        boolean geotaggingPref = this.main_activity.getApplicationInterface().getGeotaggingPref();
        imageButton.setImageResource(geotaggingPref ? C0316R.C0317drawable.ic_gps_fixed_red_48dp : C0316R.C0317drawable.ic_gps_fixed_white_48dp);
        imageButton.setContentDescription(this.main_activity.getResources().getString(geotaggingPref ? C0316R.string.preference_location_disable : C0316R.string.preference_location_enable));
    }

    public void updateTextStampIcon() {
        ((ImageButton) this.main_activity.findViewById(C0316R.C0318id.text_stamp)).setImageResource(this.main_activity.getApplicationInterface().getTextStampPref().isEmpty() ^ cache_popup ? C0316R.C0317drawable.baseline_text_fields_red_48 : C0316R.C0317drawable.baseline_text_fields_white_48);
    }

    public void updateStampIcon() {
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.stamp);
        boolean equals = this.main_activity.getApplicationInterface().getStampPref().equals("preference_stamp_yes");
        imageButton.setImageResource(equals ? C0316R.C0317drawable.ic_text_format_red_48dp : C0316R.C0317drawable.ic_text_format_white_48dp);
        imageButton.setContentDescription(this.main_activity.getResources().getString(equals ? C0316R.string.stamp_disable : C0316R.string.stamp_enable));
    }

    public void updateAutoLevelIcon() {
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.auto_level);
        boolean autoStabilisePref = this.main_activity.getApplicationInterface().getAutoStabilisePref();
        imageButton.setImageResource(autoStabilisePref ? C0316R.C0317drawable.auto_stabilise_icon_red : C0316R.C0317drawable.auto_stabilise_icon);
        imageButton.setContentDescription(this.main_activity.getResources().getString(autoStabilisePref ? C0316R.string.auto_level_disable : C0316R.string.auto_level_enable));
    }

    public void updateCycleFlashIcon() {
        String flashPref = this.main_activity.getApplicationInterface().getFlashPref();
        if (flashPref != null) {
            ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.cycle_flash);
            char c = 65535;
            switch (flashPref.hashCode()) {
                case -1524012984:
                    if (flashPref.equals("flash_frontscreen_auto")) {
                        c = 2;
                        break;
                    }
                    break;
                case -1195303778:
                    if (flashPref.equals("flash_auto")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1146923872:
                    if (flashPref.equals("flash_off")) {
                        c = 0;
                        break;
                    }
                    break;
                case -10523976:
                    if (flashPref.equals("flash_frontscreen_on")) {
                        c = 4;
                        break;
                    }
                    break;
                case 17603715:
                    if (flashPref.equals("flash_frontscreen_torch")) {
                        c = 6;
                        break;
                    }
                    break;
                case 1617654509:
                    if (flashPref.equals("flash_torch")) {
                        c = 5;
                        break;
                    }
                    break;
                case 1625570446:
                    if (flashPref.equals("flash_on")) {
                        c = 3;
                        break;
                    }
                    break;
                case 2008442932:
                    if (flashPref.equals("flash_red_eye")) {
                        c = 7;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    imageButton.setImageResource(C0316R.C0317drawable.flash_off);
                    return;
                case 1:
                case 2:
                    imageButton.setImageResource(C0316R.C0317drawable.flash_auto);
                    return;
                case 3:
                case 4:
                    imageButton.setImageResource(C0316R.C0317drawable.flash_on);
                    return;
                case 5:
                case 6:
                    imageButton.setImageResource(C0316R.C0317drawable.baseline_highlight_white_48);
                    return;
                case 7:
                    imageButton.setImageResource(C0316R.C0317drawable.baseline_remove_red_eye_white_48);
                    return;
                default:
                    return;
            }
        }
    }

    public void updateFaceDetectionIcon() {
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.face_detection);
        boolean faceDetectionPref = this.main_activity.getApplicationInterface().getFaceDetectionPref();
        imageButton.setImageResource(faceDetectionPref ? C0316R.C0317drawable.ic_face_red_48dp : C0316R.C0317drawable.ic_face_white_48dp);
        imageButton.setContentDescription(this.main_activity.getResources().getString(faceDetectionPref ? C0316R.string.face_detection_disable : C0316R.string.face_detection_enable));
    }

    public void updateOnScreenIcons() {
        updateExposureLockIcon();
        updateWhiteBalanceLockIcon();
        updateCycleRawIcon();
        updateStoreLocationIcon();
        updateTextStampIcon();
        updateStampIcon();
        updateAutoLevelIcon();
        updateCycleFlashIcon();
        updateFaceDetectionIcon();
    }

    public void audioControlStarted() {
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.audio_control);
        imageButton.setImageResource(C0316R.C0317drawable.ic_mic_red_48dp);
        imageButton.setContentDescription(this.main_activity.getResources().getString(C0316R.string.audio_control_stop));
    }

    public void audioControlStopped() {
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.audio_control);
        imageButton.setImageResource(C0316R.C0317drawable.ic_mic_white_48dp);
        imageButton.setContentDescription(this.main_activity.getResources().getString(C0316R.string.audio_control_start));
    }

    public boolean isExposureUIOpen() {
        int visibility = this.main_activity.findViewById(C0316R.C0318id.exposure_container).getVisibility();
        int visibility2 = this.main_activity.findViewById(C0316R.C0318id.manual_exposure_container).getVisibility();
        if (visibility == 0 || visibility2 == 0) {
            return cache_popup;
        }
        return false;
    }

    public void toggleExposureUI() {
        closePopup();
        this.mSelectingExposureUIElement = false;
        if (isExposureUIOpen()) {
            clearSeekBar();
        } else if (this.main_activity.getPreview().getCameraController() != null) {
            setupExposureUI();
            if (this.main_activity.getBluetoothRemoteControl().remoteEnabled()) {
                initRemoteControlForExposureUI();
            }
        }
    }

    private void initRemoteControlForExposureUI() {
        if (isExposureUIOpen()) {
            this.remote_control_mode = cache_popup;
            this.mExposureLine = 0;
            highlightExposureUILine(cache_popup);
        }
    }

    private void clearRemoteControlForExposureUI() {
        if (isExposureUIOpen() && this.remote_control_mode) {
            this.remote_control_mode = false;
            resetExposureUIHighlights();
        }
    }

    private void resetExposureUIHighlights() {
        ViewGroup viewGroup = (ViewGroup) this.main_activity.findViewById(C0316R.C0318id.iso_buttons);
        View findViewById = this.main_activity.findViewById(C0316R.C0318id.exposure_container);
        View findViewById2 = this.main_activity.findViewById(C0316R.C0318id.exposure_time_seekbar);
        View findViewById3 = this.main_activity.findViewById(C0316R.C0318id.iso_seekbar);
        View findViewById4 = this.main_activity.findViewById(C0316R.C0318id.white_balance_seekbar);
        viewGroup.setBackgroundColor(0);
        findViewById.setBackgroundColor(0);
        findViewById2.setBackgroundColor(0);
        findViewById3.setBackgroundColor(0);
        findViewById4.setBackgroundColor(0);
    }

    private void highlightExposureUILine(boolean z) {
        if (isExposureUIOpen()) {
            ViewGroup viewGroup = (ViewGroup) this.main_activity.findViewById(C0316R.C0318id.iso_buttons);
            View findViewById = this.main_activity.findViewById(C0316R.C0318id.exposure_container);
            View findViewById2 = this.main_activity.findViewById(C0316R.C0318id.exposure_time_seekbar);
            View findViewById3 = this.main_activity.findViewById(C0316R.C0318id.iso_seekbar);
            View findViewById4 = this.main_activity.findViewById(C0316R.C0318id.white_balance_seekbar);
            this.mExposureLine = (this.mExposureLine + 5) % 5;
            if (z) {
                if (this.mExposureLine == 0 && !viewGroup.isShown()) {
                    this.mExposureLine++;
                }
                if (this.mExposureLine == 1 && !findViewById3.isShown()) {
                    this.mExposureLine++;
                }
                if (this.mExposureLine == 2 && !findViewById2.isShown()) {
                    this.mExposureLine++;
                }
                if (this.mExposureLine == 3 && !findViewById.isShown()) {
                    this.mExposureLine++;
                }
                if (this.mExposureLine == 4 && !findViewById4.isShown()) {
                    this.mExposureLine++;
                }
            } else {
                if (this.mExposureLine == 4 && !findViewById4.isShown()) {
                    this.mExposureLine--;
                }
                if (this.mExposureLine == 3 && !findViewById.isShown()) {
                    this.mExposureLine--;
                }
                if (this.mExposureLine == 2 && !findViewById2.isShown()) {
                    this.mExposureLine--;
                }
                if (this.mExposureLine == 1 && !findViewById3.isShown()) {
                    this.mExposureLine--;
                }
                if (this.mExposureLine == 0 && !viewGroup.isShown()) {
                    this.mExposureLine--;
                }
            }
            this.mExposureLine = (this.mExposureLine + 5) % 5;
            resetExposureUIHighlights();
            int i = this.mExposureLine;
            if (i == 0) {
                viewGroup.setBackgroundColor(this.highlightColor);
                return;
            }
            if (i == 1) {
                findViewById3.setBackgroundColor(this.highlightColor);
            } else if (i == 2) {
                findViewById2.setBackgroundColor(this.highlightColor);
            } else if (i == 3) {
                findViewById.setBackgroundColor(this.highlightColor);
            } else if (i == 4) {
                findViewById4.setBackgroundColor(this.highlightColor);
            }
        }
    }

    private void nextExposureUILine() {
        this.mExposureLine++;
        highlightExposureUILine(cache_popup);
    }

    private void previousExposureUILine() {
        this.mExposureLine--;
        highlightExposureUILine(false);
    }

    private void nextExposureUIItem() {
        int i = this.mExposureLine;
        if (i == 0) {
            nextIsoItem(false);
        } else if (i == 1) {
            changeSeekbar(C0316R.C0318id.iso_seekbar, 10);
        } else if (i == 2) {
            changeSeekbar(C0316R.C0318id.exposure_time_seekbar, 5);
        } else if (i == 3) {
            changeSeekbar(C0316R.C0318id.exposure_seekbar, 1);
        } else if (i == 4) {
            changeSeekbar(C0316R.C0318id.white_balance_seekbar, 3);
        }
    }

    private void previousExposureUIItem() {
        int i = this.mExposureLine;
        if (i == 0) {
            nextIsoItem(cache_popup);
        } else if (i == 1) {
            changeSeekbar(C0316R.C0318id.iso_seekbar, -10);
        } else if (i == 2) {
            changeSeekbar(C0316R.C0318id.exposure_time_seekbar, -5);
        } else if (i == 3) {
            changeSeekbar(C0316R.C0318id.exposure_seekbar, -1);
        } else if (i == 4) {
            changeSeekbar(C0316R.C0318id.white_balance_seekbar, -3);
        }
    }

    private void nextIsoItem(boolean z) {
        String string = PreferenceManager.getDefaultSharedPreferences(this.main_activity).getString(PreferenceKeys.ISOPreferenceKey, "auto");
        int size = this.iso_buttons.size();
        boolean z2 = cache_popup;
        int i = z ? -1 : 1;
        int i2 = 0;
        while (true) {
            if (i2 >= size) {
                z2 = false;
                break;
            }
            Button button = (Button) this.iso_buttons.get(i2);
            StringBuilder sb = new StringBuilder();
            String str = BuildConfig.FLAVOR;
            sb.append(str);
            sb.append(button.getText());
            if (sb.toString().contains(string)) {
                int i3 = i2 + size;
                Button button2 = (Button) this.iso_buttons.get((i3 + i) % size);
                StringBuilder sb2 = new StringBuilder();
                sb2.append(str);
                sb2.append(button2.getText());
                if (sb2.toString().contains(manual_iso_value)) {
                    button2 = (Button) this.iso_buttons.get((i3 + (i * 2)) % size);
                }
                button2.callOnClick();
            } else {
                i2++;
            }
        }
        if (!z2) {
            ((View) this.iso_buttons.get(0)).callOnClick();
        }
    }

    private void selectExposureUILine() {
        if (isExposureUIOpen()) {
            int i = this.mExposureLine;
            if (i == 0) {
                ((ViewGroup) this.main_activity.findViewById(C0316R.C0318id.iso_buttons)).setBackgroundColor(this.highlightColorExposureUIElement);
                String string = PreferenceManager.getDefaultSharedPreferences(this.main_activity).getString(PreferenceKeys.ISOPreferenceKey, "auto");
                Button button = null;
                boolean z = false;
                for (View view : this.iso_buttons) {
                    Button button2 = (Button) view;
                    StringBuilder sb = new StringBuilder();
                    sb.append(BuildConfig.FLAVOR);
                    sb.append(button2.getText());
                    String sb2 = sb.toString();
                    if (sb2.contains(string)) {
                        PopupView.setButtonSelected(button2, cache_popup);
                        z = cache_popup;
                    } else {
                        if (sb2.contains(manual_iso_value)) {
                            button = button2;
                        }
                        PopupView.setButtonSelected(button2, false);
                        button2.setBackgroundColor(0);
                    }
                }
                if (!z && button != null) {
                    PopupView.setButtonSelected(button, cache_popup);
                    button.setBackgroundColor(this.highlightColorExposureUIElement);
                }
                this.mSelectingExposureUIElement = cache_popup;
            } else if (i == 1) {
                this.main_activity.findViewById(C0316R.C0318id.iso_seekbar).setBackgroundColor(this.highlightColorExposureUIElement);
                this.mSelectingExposureUIElement = cache_popup;
            } else if (i == 2) {
                this.main_activity.findViewById(C0316R.C0318id.exposure_time_seekbar).setBackgroundColor(this.highlightColorExposureUIElement);
                this.mSelectingExposureUIElement = cache_popup;
            } else if (i == 3) {
                this.main_activity.findViewById(C0316R.C0318id.exposure_container).setBackgroundColor(this.highlightColorExposureUIElement);
                this.mSelectingExposureUIElement = cache_popup;
            } else if (i == 4) {
                this.main_activity.findViewById(C0316R.C0318id.white_balance_seekbar).setBackgroundColor(this.highlightColorExposureUIElement);
                this.mSelectingExposureUIElement = cache_popup;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public int getMaxHeightDp(boolean z) {
        Display defaultDisplay = this.main_activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getMetrics(displayMetrics);
        return ((int) (((float) Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels)) / this.main_activity.getResources().getDisplayMetrics().density)) - (z ? 120 : 50);
    }

    public boolean isSelectingExposureUIElement() {
        return this.mSelectingExposureUIElement;
    }

    public boolean processRemoteUpButton() {
        if (popupIsOpen()) {
            if (selectingIcons()) {
                previousPopupIcon();
                return cache_popup;
            } else if (!selectingLines()) {
                return cache_popup;
            } else {
                previousPopupLine();
                return cache_popup;
            }
        } else if (!isExposureUIOpen()) {
            return false;
        } else {
            if (isSelectingExposureUIElement()) {
                nextExposureUIItem();
                return cache_popup;
            }
            previousExposureUILine();
            return cache_popup;
        }
    }

    public boolean processRemoteDownButton() {
        if (popupIsOpen()) {
            if (selectingIcons()) {
                nextPopupIcon();
                return cache_popup;
            } else if (!selectingLines()) {
                return cache_popup;
            } else {
                nextPopupLine();
                return cache_popup;
            }
        } else if (!isExposureUIOpen()) {
            return false;
        } else {
            if (isSelectingExposureUIElement()) {
                previousExposureUIItem();
                return cache_popup;
            }
            nextExposureUILine();
            return cache_popup;
        }
    }

    public void setupExposureUI() {
        List list;
        this.test_ui_buttons.clear();
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.main_activity);
        final Preview preview = this.main_activity.getPreview();
        this.main_activity.findViewById(C0316R.C0318id.sliders_container).setVisibility(0);
        ViewGroup viewGroup = (ViewGroup) this.main_activity.findViewById(C0316R.C0318id.iso_buttons);
        viewGroup.removeAllViews();
        boolean isVideoRecording = preview.isVideoRecording();
        String str = manual_iso_value;
        String str2 = "auto";
        if (isVideoRecording) {
            list = null;
        } else if (preview.supportsISORange()) {
            int minimumISO = preview.getMinimumISO();
            int maximumISO = preview.getMaximumISO();
            List arrayList = new ArrayList();
            arrayList.add(str2);
            arrayList.add(str);
            this.iso_button_manual_index = 1;
            int[] iArr = {50, 100, 200, 400, 800, 1600, 3200, 6400};
            StringBuilder sb = new StringBuilder();
            String str3 = BuildConfig.FLAVOR;
            sb.append(str3);
            sb.append(minimumISO);
            arrayList.add(sb.toString());
            for (int i : iArr) {
                if (i > minimumISO && i < maximumISO) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(str3);
                    sb2.append(i);
                    arrayList.add(sb2.toString());
                }
            }
            StringBuilder sb3 = new StringBuilder();
            sb3.append(str3);
            sb3.append(maximumISO);
            arrayList.add(sb3.toString());
            list = arrayList;
        } else {
            list = preview.getSupportedISOs();
            this.iso_button_manual_index = -1;
        }
        String string = defaultSharedPreferences.getString(PreferenceKeys.ISOPreferenceKey, str2);
        String str4 = (string.equals(str2) || list == null || !list.contains(str) || list.contains(string)) ? string : str;
        int maxHeightDp = getMaxHeightDp(cache_popup);
        int i2 = 280 > maxHeightDp ? maxHeightDp : 280;
        MainActivity mainActivity = this.main_activity;
        Map<String, View> map = this.test_ui_buttons;
        C03894 r17 = new ButtonOptionsPopupListener() {
            public void onClick(String str) {
                Editor edit = defaultSharedPreferences.edit();
                SharedPreferences sharedPreferences = defaultSharedPreferences;
                String str2 = "auto";
                String str3 = PreferenceKeys.ISOPreferenceKey;
                String string = sharedPreferences.getString(str3, str2);
                edit.putString(str3, str);
                if (preview.supportsISORange()) {
                    boolean equals = str.equals(str2);
                    String str4 = "ISO: ";
                    String str5 = PreferenceKeys.ExposureTimePreferenceKey;
                    if (equals) {
                        edit.putLong(str5, CameraController.EXPOSURE_TIME_DEFAULT);
                        edit.apply();
                        MainActivity access$100 = MainUI.this.main_activity;
                        StringBuilder sb = new StringBuilder();
                        sb.append(str4);
                        sb.append(str);
                        access$100.updateForSettings(sb.toString());
                    } else {
                        boolean equals2 = string.equals(str2);
                        String str6 = MainUI.manual_iso_value;
                        String str7 = BuildConfig.FLAVOR;
                        if (equals2) {
                            String str8 = "800";
                            if (str.equals(str6)) {
                                if (preview.getCameraController() == null || !preview.getCameraController().captureResultHasIso()) {
                                    edit.putString(str3, str8);
                                    str = str8;
                                } else {
                                    int captureResultIso = preview.getCameraController().captureResultIso();
                                    StringBuilder sb2 = new StringBuilder();
                                    sb2.append(str7);
                                    sb2.append(captureResultIso);
                                    edit.putString(str3, sb2.toString());
                                    StringBuilder sb3 = new StringBuilder();
                                    sb3.append(str7);
                                    sb3.append(captureResultIso);
                                    str = sb3.toString();
                                }
                            }
                            if (preview.getCameraController() != null && preview.getCameraController().captureResultHasExposureTime()) {
                                edit.putLong(str5, preview.getCameraController().captureResultExposureTime());
                            }
                            edit.apply();
                            MainActivity access$1002 = MainUI.this.main_activity;
                            StringBuilder sb4 = new StringBuilder();
                            sb4.append(str4);
                            sb4.append(str);
                            access$1002.updateForSettings(sb4.toString());
                        } else {
                            if (str.equals(str6)) {
                                StringBuilder sb5 = new StringBuilder();
                                sb5.append(str7);
                                sb5.append(string);
                                edit.putString(str3, sb5.toString());
                            }
                            edit.apply();
                            int parseManualISOValue = preview.parseManualISOValue(str);
                            if (parseManualISOValue >= 0) {
                                MainUI.this.main_activity.getManualSeekbars().setISOProgressBarToClosest((SeekBar) MainUI.this.main_activity.findViewById(C0316R.C0318id.iso_seekbar), (long) parseManualISOValue);
                            }
                        }
                    }
                } else {
                    edit.apply();
                    if (preview.getCameraController() != null) {
                        preview.getCameraController().setISO(str);
                    }
                }
                MainUI.this.setupExposureUI();
            }
        };
        String str5 = str2;
        this.iso_buttons = PopupView.createButtonOptions(viewGroup, mainActivity, i2, map, list, -1, -1, "ISO", false, str4, 0, "TEST_ISO", r17);
        if (list != null) {
            this.main_activity.findViewById(C0316R.C0318id.iso_container).setVisibility(0);
        }
        View findViewById = this.main_activity.findViewById(C0316R.C0318id.exposure_container);
        View findViewById2 = this.main_activity.findViewById(C0316R.C0318id.manual_exposure_container);
        String iSOPref = this.main_activity.getApplicationInterface().getISOPref();
        if (!this.main_activity.getPreview().usingCamera2API() || iSOPref.equals(str5)) {
            findViewById2.setVisibility(8);
            if (this.main_activity.getPreview().supportsExposures()) {
                findViewById.setVisibility(0);
                ((ZoomControls) this.main_activity.findViewById(C0316R.C0318id.exposure_seekbar_zoom)).setVisibility(0);
            } else {
                findViewById.setVisibility(8);
            }
        } else {
            findViewById.setVisibility(8);
            if (this.main_activity.getPreview().supportsISORange()) {
                findViewById2.setVisibility(0);
                SeekBar seekBar = (SeekBar) this.main_activity.findViewById(C0316R.C0318id.exposure_time_seekbar);
                if (this.main_activity.getPreview().supportsExposureTime()) {
                    seekBar.setVisibility(0);
                } else {
                    seekBar.setVisibility(8);
                }
            } else {
                findViewById2.setVisibility(8);
            }
        }
        View findViewById3 = this.main_activity.findViewById(C0316R.C0318id.manual_white_balance_container);
        if (this.main_activity.getPreview().supportsWhiteBalanceTemperature()) {
            String whiteBalancePref = this.main_activity.getApplicationInterface().getWhiteBalancePref();
            if (!this.main_activity.getPreview().usingCamera2API() || !whiteBalancePref.equals("manual")) {
                findViewById3.setVisibility(8);
            } else {
                findViewById3.setVisibility(0);
            }
        } else {
            findViewById3.setVisibility(8);
        }
    }

    public void updateSelectedISOButton() {
        if (this.main_activity.getPreview().supportsISORange() && isExposureUIOpen()) {
            String str = "auto";
            String string = PreferenceManager.getDefaultSharedPreferences(this.main_activity).getString(PreferenceKeys.ISOPreferenceKey, str);
            boolean z = false;
            for (View view : this.iso_buttons) {
                Button button = (Button) view;
                StringBuilder sb = new StringBuilder();
                sb.append(BuildConfig.FLAVOR);
                sb.append(button.getText());
                if (sb.toString().contains(string)) {
                    PopupView.setButtonSelected(button, cache_popup);
                    z = cache_popup;
                } else {
                    PopupView.setButtonSelected(button, false);
                }
            }
            if (!z && !string.equals(str)) {
                int i = this.iso_button_manual_index;
                if (i >= 0 && i < this.iso_buttons.size()) {
                    PopupView.setButtonSelected((Button) this.iso_buttons.get(this.iso_button_manual_index), cache_popup);
                }
            }
        }
    }

    public void setSeekbarZoom(int i) {
        ((SeekBar) this.main_activity.findViewById(C0316R.C0318id.zoom_seekbar)).setProgress(this.main_activity.getPreview().getMaxZoom() - i);
    }

    public void changeSeekbar(int i, int i2) {
        SeekBar seekBar = (SeekBar) this.main_activity.findViewById(i);
        int progress = seekBar.getProgress();
        int i3 = i2 + progress;
        if (i3 < 0) {
            i3 = 0;
        } else if (i3 > seekBar.getMax()) {
            i3 = seekBar.getMax();
        }
        if (i3 != progress) {
            seekBar.setProgress(i3);
        }
    }

    public void clearSeekBar() {
        clearRemoteControlForExposureUI();
        this.main_activity.findViewById(C0316R.C0318id.sliders_container).setVisibility(8);
        this.main_activity.findViewById(C0316R.C0318id.iso_container).setVisibility(8);
        this.main_activity.findViewById(C0316R.C0318id.exposure_container).setVisibility(8);
        this.main_activity.findViewById(C0316R.C0318id.manual_exposure_container).setVisibility(8);
        this.main_activity.findViewById(C0316R.C0318id.manual_white_balance_container).setVisibility(8);
    }

    public void setPopupIcon() {
        ImageButton imageButton = (ImageButton) this.main_activity.findViewById(C0316R.C0318id.popup);
        String currentFlashValue = this.main_activity.getPreview().getCurrentFlashValue();
        if (this.main_activity.getMainUI().showCycleFlashIcon()) {
            imageButton.setImageResource(C0316R.C0317drawable.popup);
        } else if (currentFlashValue != null && currentFlashValue.equals("flash_off")) {
            imageButton.setImageResource(C0316R.C0317drawable.popup_flash_off);
        } else if (currentFlashValue != null && (currentFlashValue.equals("flash_torch") || currentFlashValue.equals("flash_frontscreen_torch"))) {
            imageButton.setImageResource(C0316R.C0317drawable.popup_flash_torch);
        } else if (currentFlashValue != null && (currentFlashValue.equals("flash_auto") || currentFlashValue.equals("flash_frontscreen_auto"))) {
            imageButton.setImageResource(C0316R.C0317drawable.popup_flash_auto);
        } else if (currentFlashValue != null && (currentFlashValue.equals("flash_on") || currentFlashValue.equals("flash_frontscreen_on"))) {
            imageButton.setImageResource(C0316R.C0317drawable.popup_flash_on);
        } else if (currentFlashValue == null || !currentFlashValue.equals("flash_red_eye")) {
            imageButton.setImageResource(C0316R.C0317drawable.popup);
        } else {
            imageButton.setImageResource(C0316R.C0317drawable.popup_flash_red_eye);
        }
    }

    public void closePopup() {
        if (popupIsOpen()) {
            clearRemoteControlForPopup();
            clearSelectionState();
            this.popup_view_is_open = false;
            if (!this.force_destroy_popup) {
                this.popup_view.setVisibility(8);
            } else {
                destroyPopup();
            }
            this.main_activity.initImmersiveMode();
        }
    }

    public boolean popupIsOpen() {
        return this.popup_view_is_open;
    }

    public boolean selectingIcons() {
        return this.mSelectingIcons;
    }

    public boolean selectingLines() {
        return this.mSelectingLines;
    }

    public void destroyPopup() {
        this.force_destroy_popup = false;
        if (popupIsOpen()) {
            closePopup();
        }
        ((ViewGroup) this.main_activity.findViewById(C0316R.C0318id.popup_container)).removeAllViews();
        this.popup_view = null;
    }

    private void highlightPopupLine(boolean z, boolean z2) {
        if (!popupIsOpen()) {
            clearSelectionState();
            return;
        }
        ViewGroup viewGroup = (ViewGroup) this.main_activity.findViewById(C0316R.C0318id.popup_container);
        Rect rect = new Rect();
        viewGroup.getDrawingRect(rect);
        LinearLayout linearLayout = (LinearLayout) viewGroup.getChildAt(0);
        if (linearLayout != null) {
            int childCount = linearLayout.getChildCount();
            boolean z3 = false;
            while (!z3) {
                this.mPopupLine = (this.mPopupLine + childCount) % childCount;
                View childAt = linearLayout.getChildAt(this.mPopupLine);
                int i = 1;
                if (!childAt.isShown() || !(childAt instanceof LinearLayout)) {
                    int i2 = this.mPopupLine;
                    if (z2) {
                        i = -1;
                    }
                    this.mPopupLine = i2 + i;
                } else {
                    if (z) {
                        childAt.setBackgroundColor(this.highlightColor);
                        if (childAt.getBottom() > rect.bottom || childAt.getTop() < rect.top) {
                            viewGroup.scrollTo(0, childAt.getTop());
                        }
                        this.mHighlightedLine = (LinearLayout) childAt;
                    } else {
                        childAt.setBackgroundColor(0);
                        childAt.setAlpha(1.0f);
                    }
                    z3 = cache_popup;
                }
            }
        }
    }

    private void highlightPopupIcon(boolean z, boolean z2) {
        if (!popupIsOpen()) {
            clearSelectionState();
            return;
        }
        highlightPopupLine(false, false);
        int childCount = this.mHighlightedLine.getChildCount();
        boolean z3 = false;
        while (!z3) {
            this.mPopupIcon = (this.mPopupIcon + childCount) % childCount;
            View childAt = this.mHighlightedLine.getChildAt(this.mPopupIcon);
            int i = 1;
            if ((childAt instanceof ImageButton) || (childAt instanceof Button)) {
                if (z) {
                    childAt.setBackgroundColor(this.highlightColor);
                    this.mHighlightedIcon = childAt;
                    this.mSelectingIcons = cache_popup;
                } else {
                    childAt.setBackgroundColor(0);
                }
                z3 = cache_popup;
            } else {
                int i2 = this.mPopupIcon;
                if (z2) {
                    i = -1;
                }
                this.mPopupIcon = i2 + i;
            }
        }
    }

    private void nextPopupLine() {
        highlightPopupLine(false, false);
        this.mPopupLine++;
        highlightPopupLine(cache_popup, false);
    }

    private void previousPopupLine() {
        highlightPopupLine(false, cache_popup);
        this.mPopupLine--;
        highlightPopupLine(cache_popup, cache_popup);
    }

    private void nextPopupIcon() {
        highlightPopupIcon(false, false);
        this.mPopupIcon++;
        highlightPopupIcon(cache_popup, false);
    }

    private void previousPopupIcon() {
        highlightPopupIcon(false, cache_popup);
        this.mPopupIcon--;
        highlightPopupIcon(cache_popup, cache_popup);
    }

    private void clickSelectedIcon() {
        View view = this.mHighlightedIcon;
        if (view != null) {
            view.callOnClick();
        }
    }

    private void clearSelectionState() {
        this.mPopupLine = 0;
        this.mPopupIcon = 0;
        this.mSelectingIcons = false;
        this.mSelectingLines = false;
        this.mHighlightedIcon = null;
        this.mHighlightedLine = null;
    }

    public void togglePopupSettings() {
        final ViewGroup viewGroup = (ViewGroup) this.main_activity.findViewById(C0316R.C0318id.popup_container);
        if (popupIsOpen()) {
            closePopup();
        } else if (this.main_activity.getPreview().getCameraController() != null) {
            clearSeekBar();
            this.main_activity.getPreview().cancelTimer();
            this.main_activity.stopAudioListeners();
            final long currentTimeMillis = System.currentTimeMillis();
            viewGroup.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
            viewGroup.setAlpha(0.9f);
            PopupView popupView = this.popup_view;
            if (popupView == null) {
                this.test_ui_buttons.clear();
                this.popup_view = new PopupView(this.main_activity);
                viewGroup.addView(this.popup_view);
            } else {
                popupView.setVisibility(0);
            }
            this.popup_view_is_open = cache_popup;
            if (this.main_activity.getBluetoothRemoteControl().remoteEnabled()) {
                initRemoteControlForPopup();
            }
            viewGroup.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    float f;
                    float f2;
                    float f3;
                    MainUI.this.layoutUI(MainUI.cache_popup);
                    if (VERSION.SDK_INT > 15) {
                        viewGroup.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        viewGroup.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    int i = C03938.$SwitchMap$net$sourceforge$opencamera$ui$MainUI$UIPlacement[MainUI.this.computeUIPlacement().ordinal()];
                    if (i != 1) {
                        if (i != 2) {
                            f3 = 1.0f;
                        } else {
                            f2 = 1.0f;
                            f = 1.0f;
                            ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 1, f2, 1, f);
                            scaleAnimation.setDuration(100);
                            viewGroup.setAnimation(scaleAnimation);
                        }
                    } else if (MainUI.this.main_activity.getPreview().getUIRotation() == 270) {
                        f2 = 0.0f;
                        f = 1.0f;
                        ScaleAnimation scaleAnimation2 = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 1, f2, 1, f);
                        scaleAnimation2.setDuration(100);
                        viewGroup.setAnimation(scaleAnimation2);
                    } else {
                        f3 = 0.0f;
                    }
                    f = 0.0f;
                    ScaleAnimation scaleAnimation22 = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 1, f2, 1, f);
                    scaleAnimation22.setDuration(100);
                    viewGroup.setAnimation(scaleAnimation22);
                }
            });
        }
    }

    private void initRemoteControlForPopup() {
        if (popupIsOpen()) {
            clearSelectionState();
            this.remote_control_mode = cache_popup;
            this.mSelectingLines = cache_popup;
            highlightPopupLine(cache_popup, false);
        }
    }

    private void clearRemoteControlForPopup() {
        if (popupIsOpen() && this.remote_control_mode) {
            this.remote_control_mode = false;
            ViewGroup viewGroup = (ViewGroup) this.main_activity.findViewById(C0316R.C0318id.popup_container);
            viewGroup.getDrawingRect(new Rect());
            LinearLayout linearLayout = (LinearLayout) viewGroup.getChildAt(0);
            if (linearLayout != null) {
                View childAt = linearLayout.getChildAt(this.mPopupLine);
                if (childAt.isShown() && (childAt instanceof LinearLayout)) {
                    childAt.setBackgroundColor(0);
                    childAt.setAlpha(1.0f);
                }
                LinearLayout linearLayout2 = this.mHighlightedLine;
                if (linearLayout2 != null) {
                    View childAt2 = linearLayout2.getChildAt(this.mPopupIcon);
                    if ((childAt2 instanceof ImageButton) || (childAt2 instanceof Button)) {
                        childAt2.setBackgroundColor(0);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:79:0x00f5, code lost:
        if (r0.isWiredHeadsetOn() == false) goto L_0x02cd;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onKeyDown(int r13, android.view.KeyEvent r14) {
        /*
            r12 = this;
            r0 = 19
            r1 = 0
            r2 = 1
            if (r13 == r0) goto L_0x02ae
            r0 = 20
            if (r13 == r0) goto L_0x028f
            r0 = 86
            r3 = 85
            r4 = 88
            r5 = 25
            r6 = 24
            if (r13 == r6) goto L_0x00c0
            if (r13 == r5) goto L_0x00c0
            r7 = 27
            if (r13 == r7) goto L_0x0092
            r7 = 62
            if (r13 == r7) goto L_0x006a
            r7 = 69
            if (r13 == r7) goto L_0x0064
            r7 = 76
            if (r13 == r7) goto L_0x005f
            if (r13 == r4) goto L_0x00c0
            r7 = 119(0x77, float:1.67E-43)
            if (r13 == r7) goto L_0x005a
            r7 = 146(0x92, float:2.05E-43)
            if (r13 == r7) goto L_0x028f
            r7 = 149(0x95, float:2.09E-43)
            if (r13 == r7) goto L_0x006a
            r7 = 152(0x98, float:2.13E-43)
            if (r13 == r7) goto L_0x02ae
            if (r13 == r3) goto L_0x00c0
            if (r13 == r0) goto L_0x00c0
            r0 = 168(0xa8, float:2.35E-43)
            if (r13 == r0) goto L_0x0054
            r0 = 169(0xa9, float:2.37E-43)
            if (r13 == r0) goto L_0x0064
            switch(r13) {
                case 80: goto L_0x009e;
                case 81: goto L_0x0054;
                case 82: goto L_0x004e;
                default: goto L_0x0049;
            }
        L_0x0049:
            switch(r13) {
                case 154: goto L_0x005f;
                case 155: goto L_0x005a;
                case 156: goto L_0x0064;
                case 157: goto L_0x0054;
                default: goto L_0x004c;
            }
        L_0x004c:
            goto L_0x02cd
        L_0x004e:
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.openSettings()
            return r2
        L_0x0054:
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.zoomIn()
            return r2
        L_0x005a:
            r12.togglePopupSettings()
            goto L_0x02cd
        L_0x005f:
            r12.toggleExposureUI()
            goto L_0x02cd
        L_0x0064:
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.zoomOut()
            return r2
        L_0x006a:
            boolean r13 = r12.isExposureUIOpen()
            if (r13 == 0) goto L_0x0078
            boolean r13 = r12.remote_control_mode
            if (r13 == 0) goto L_0x0078
            r12.commandMenuExposure()
            return r2
        L_0x0078:
            boolean r13 = r12.popupIsOpen()
            if (r13 == 0) goto L_0x0086
            boolean r13 = r12.remote_control_mode
            if (r13 == 0) goto L_0x0086
            r12.commandMenuPopup()
            return r2
        L_0x0086:
            int r13 = r14.getRepeatCount()
            if (r13 != 0) goto L_0x02cd
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.takePicture(r1)
            return r2
        L_0x0092:
            int r13 = r14.getRepeatCount()
            if (r13 != 0) goto L_0x009e
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.takePicture(r1)
            return r2
        L_0x009e:
            long r0 = r14.getDownTime()
            long r13 = r14.getEventTime()
            int r3 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
            if (r3 != 0) goto L_0x00bf
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            net.sourceforge.opencamera.preview.Preview r13 = r13.getPreview()
            boolean r13 = r13.isFocusWaiting()
            if (r13 != 0) goto L_0x00bf
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            net.sourceforge.opencamera.preview.Preview r13 = r13.getPreview()
            r13.requestAutoFocus()
        L_0x00bf:
            return r2
        L_0x00c0:
            if (r13 != r6) goto L_0x00c5
            r12.keydown_volume_up = r2
            goto L_0x00c9
        L_0x00c5:
            if (r13 != r5) goto L_0x00c9
            r12.keydown_volume_down = r2
        L_0x00c9:
            net.sourceforge.opencamera.MainActivity r5 = r12.main_activity
            android.content.SharedPreferences r5 = android.preference.PreferenceManager.getDefaultSharedPreferences(r5)
            java.lang.String r7 = "volume_take_photo"
            java.lang.String r8 = "preference_volume_keys"
            java.lang.String r8 = r5.getString(r8, r7)
            if (r13 == r4) goto L_0x00dd
            if (r13 == r3) goto L_0x00dd
            if (r13 != r0) goto L_0x00f9
        L_0x00dd:
            boolean r0 = r8.equals(r7)
            if (r0 != 0) goto L_0x00f9
            net.sourceforge.opencamera.MainActivity r0 = r12.main_activity
            java.lang.String r3 = "audio"
            java.lang.Object r0 = r0.getSystemService(r3)
            android.media.AudioManager r0 = (android.media.AudioManager) r0
            if (r0 != 0) goto L_0x00f1
            goto L_0x02cd
        L_0x00f1:
            boolean r0 = r0.isWiredHeadsetOn()
            if (r0 != 0) goto L_0x00f9
            goto L_0x02cd
        L_0x00f9:
            int r0 = r8.hashCode()
            r3 = 5
            r4 = 4
            r9 = 3
            r10 = 2
            r11 = -1
            switch(r0) {
                case -1359912077: goto L_0x0136;
                case -925372737: goto L_0x012e;
                case -874555944: goto L_0x0124;
                case -692640628: goto L_0x011a;
                case 529947390: goto L_0x0110;
                case 915660971: goto L_0x0106;
                default: goto L_0x0105;
            }
        L_0x0105:
            goto L_0x0140
        L_0x0106:
            java.lang.String r0 = "volume_auto_stabilise"
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0140
            r0 = 4
            goto L_0x0141
        L_0x0110:
            java.lang.String r0 = "volume_really_nothing"
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0140
            r0 = 5
            goto L_0x0141
        L_0x011a:
            java.lang.String r0 = "volume_exposure"
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0140
            r0 = 3
            goto L_0x0141
        L_0x0124:
            java.lang.String r0 = "volume_zoom"
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0140
            r0 = 2
            goto L_0x0141
        L_0x012e:
            boolean r0 = r8.equals(r7)
            if (r0 == 0) goto L_0x0140
            r0 = 0
            goto L_0x0141
        L_0x0136:
            java.lang.String r0 = "volume_focus"
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0140
            r0 = 1
            goto L_0x0141
        L_0x0140:
            r0 = -1
        L_0x0141:
            if (r0 == 0) goto L_0x0289
            if (r0 == r2) goto L_0x022d
            if (r0 == r10) goto L_0x021f
            if (r0 == r9) goto L_0x01d0
            if (r0 == r4) goto L_0x0150
            if (r0 == r3) goto L_0x014f
            goto L_0x02cd
        L_0x014f:
            return r2
        L_0x0150:
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            boolean r13 = r13.supportsAutoStabilise()
            if (r13 == 0) goto L_0x01bd
            java.lang.String r13 = "preference_auto_stabilise"
            boolean r14 = r5.getBoolean(r13, r1)
            r14 = r14 ^ r2
            android.content.SharedPreferences$Editor r0 = r5.edit()
            r0.putBoolean(r13, r14)
            r0.apply()
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            r13.<init>()
            net.sourceforge.opencamera.MainActivity r0 = r12.main_activity
            android.content.res.Resources r0 = r0.getResources()
            r1 = 2131493081(0x7f0c00d9, float:1.8609632E38)
            java.lang.String r0 = r0.getString(r1)
            r13.append(r0)
            java.lang.String r0 = ": "
            r13.append(r0)
            net.sourceforge.opencamera.MainActivity r0 = r12.main_activity
            android.content.res.Resources r0 = r0.getResources()
            if (r14 == 0) goto L_0x018f
            r14 = 2131493033(0x7f0c00a9, float:1.8609535E38)
            goto L_0x0192
        L_0x018f:
            r14 = 2131493032(0x7f0c00a8, float:1.8609533E38)
        L_0x0192:
            java.lang.String r14 = r0.getString(r14)
            r13.append(r14)
            java.lang.String r13 = r13.toString()
            net.sourceforge.opencamera.MainActivity r14 = r12.main_activity
            net.sourceforge.opencamera.preview.Preview r14 = r14.getPreview()
            net.sourceforge.opencamera.MainActivity r0 = r12.main_activity
            net.sourceforge.opencamera.ToastBoxer r0 = r0.getChangedAutoStabiliseToastBoxer()
            r14.showToast(r0, r13)
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            net.sourceforge.opencamera.MyApplicationInterface r13 = r13.getApplicationInterface()
            net.sourceforge.opencamera.ui.DrawPreview r13 = r13.getDrawPreview()
            r13.updateSettings()
            r12.destroyPopup()
            goto L_0x01cf
        L_0x01bd:
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            net.sourceforge.opencamera.preview.Preview r13 = r13.getPreview()
            net.sourceforge.opencamera.MainActivity r14 = r12.main_activity
            net.sourceforge.opencamera.ToastBoxer r14 = r14.getChangedAutoStabiliseToastBoxer()
            r0 = 2131492888(0x7f0c0018, float:1.860924E38)
            r13.showToast(r14, r0)
        L_0x01cf:
            return r2
        L_0x01d0:
            net.sourceforge.opencamera.MainActivity r14 = r12.main_activity
            net.sourceforge.opencamera.preview.Preview r14 = r14.getPreview()
            net.sourceforge.opencamera.cameracontroller.CameraController r14 = r14.getCameraController()
            if (r14 == 0) goto L_0x021e
            java.lang.String r14 = "auto"
            java.lang.String r0 = "preference_iso"
            java.lang.String r0 = r5.getString(r0, r14)
            boolean r14 = r0.equals(r14)
            r14 = r14 ^ r2
            if (r13 != r6) goto L_0x0205
            if (r14 == 0) goto L_0x01ff
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            net.sourceforge.opencamera.preview.Preview r13 = r13.getPreview()
            boolean r13 = r13.supportsISORange()
            if (r13 == 0) goto L_0x021e
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.changeISO(r2)
            goto L_0x021e
        L_0x01ff:
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.changeExposure(r2)
            goto L_0x021e
        L_0x0205:
            if (r14 == 0) goto L_0x0219
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            net.sourceforge.opencamera.preview.Preview r13 = r13.getPreview()
            boolean r13 = r13.supportsISORange()
            if (r13 == 0) goto L_0x021e
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.changeISO(r11)
            goto L_0x021e
        L_0x0219:
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.changeExposure(r11)
        L_0x021e:
            return r2
        L_0x021f:
            if (r13 != r6) goto L_0x0227
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.zoomIn()
            goto L_0x022c
        L_0x0227:
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.zoomOut()
        L_0x022c:
            return r2
        L_0x022d:
            boolean r0 = r12.keydown_volume_up
            if (r0 == 0) goto L_0x023b
            boolean r0 = r12.keydown_volume_down
            if (r0 == 0) goto L_0x023b
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.takePicture(r1)
            goto L_0x0288
        L_0x023b:
            net.sourceforge.opencamera.MainActivity r0 = r12.main_activity
            net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()
            java.lang.String r0 = r0.getCurrentFocusValue()
            if (r0 == 0) goto L_0x0267
            net.sourceforge.opencamera.MainActivity r0 = r12.main_activity
            net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()
            java.lang.String r0 = r0.getCurrentFocusValue()
            java.lang.String r3 = "focus_mode_manual2"
            boolean r0 = r0.equals(r3)
            if (r0 == 0) goto L_0x0267
            if (r13 != r6) goto L_0x0261
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.changeFocusDistance(r11, r1)
            goto L_0x0288
        L_0x0261:
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.changeFocusDistance(r2, r1)
            goto L_0x0288
        L_0x0267:
            long r0 = r14.getDownTime()
            long r13 = r14.getEventTime()
            int r3 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
            if (r3 != 0) goto L_0x0288
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            net.sourceforge.opencamera.preview.Preview r13 = r13.getPreview()
            boolean r13 = r13.isFocusWaiting()
            if (r13 != 0) goto L_0x0288
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            net.sourceforge.opencamera.preview.Preview r13 = r13.getPreview()
            r13.requestAutoFocus()
        L_0x0288:
            return r2
        L_0x0289:
            net.sourceforge.opencamera.MainActivity r13 = r12.main_activity
            r13.takePicture(r1)
            return r2
        L_0x028f:
            boolean r13 = r12.remote_control_mode
            if (r13 != 0) goto L_0x02a7
            boolean r13 = r12.popupIsOpen()
            if (r13 == 0) goto L_0x029d
            r12.initRemoteControlForPopup()
            return r2
        L_0x029d:
            boolean r13 = r12.isExposureUIOpen()
            if (r13 == 0) goto L_0x02cd
            r12.initRemoteControlForExposureUI()
            return r2
        L_0x02a7:
            boolean r13 = r12.processRemoteDownButton()
            if (r13 == 0) goto L_0x02cd
            return r2
        L_0x02ae:
            boolean r13 = r12.remote_control_mode
            if (r13 != 0) goto L_0x02c6
            boolean r13 = r12.popupIsOpen()
            if (r13 == 0) goto L_0x02bc
            r12.initRemoteControlForPopup()
            return r2
        L_0x02bc:
            boolean r13 = r12.isExposureUIOpen()
            if (r13 == 0) goto L_0x02cd
            r12.initRemoteControlForExposureUI()
            return r2
        L_0x02c6:
            boolean r13 = r12.processRemoteUpButton()
            if (r13 == 0) goto L_0x02cd
            return r2
        L_0x02cd:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.MainUI.onKeyDown(int, android.view.KeyEvent):boolean");
    }

    public void onKeyUp(int i, KeyEvent keyEvent) {
        if (i == 24) {
            this.keydown_volume_up = false;
        } else if (i == 25) {
            this.keydown_volume_down = false;
        }
    }

    public void commandMenuExposure() {
        if (!isExposureUIOpen()) {
            return;
        }
        if (isSelectingExposureUIElement()) {
            toggleExposureUI();
        } else {
            selectExposureUILine();
        }
    }

    public void commandMenuPopup() {
        if (!popupIsOpen()) {
            return;
        }
        if (selectingIcons()) {
            clickSelectedIcon();
        } else {
            highlightPopupIcon(cache_popup, false);
        }
    }

    public AlertDialog showInfoDialog(int i, int i2, final String str) {
        Builder builder = new Builder(this.main_activity);
        builder.setTitle(i);
        if (i2 != 0) {
            builder.setMessage(i2);
        }
        builder.setPositiveButton(17039370, null);
        builder.setNegativeButton(C0316R.string.dont_show_again, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Editor edit = PreferenceManager.getDefaultSharedPreferences(MainUI.this.main_activity).edit();
                edit.putBoolean(str, MainUI.cache_popup);
                edit.apply();
            }
        });
        this.main_activity.showPreview(false);
        this.main_activity.setWindowFlagsForSettings(false);
        AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                MainUI.this.main_activity.setWindowFlagsForCamera();
                MainUI.this.main_activity.showPreview(MainUI.cache_popup);
            }
        });
        this.main_activity.showAlert(create);
        return create;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getEntryForWhiteBalance(java.lang.String r3) {
        /*
            r2 = this;
            int r0 = r3.hashCode()
            r1 = -1
            switch(r0) {
                case -1081415738: goto L_0x0059;
                case -939299377: goto L_0x004f;
                case -719316704: goto L_0x0045;
                case 3005871: goto L_0x003b;
                case 109399597: goto L_0x0031;
                case 474934723: goto L_0x0027;
                case 1650323088: goto L_0x001d;
                case 1902580840: goto L_0x0013;
                case 1942983418: goto L_0x0009;
                default: goto L_0x0008;
            }
        L_0x0008:
            goto L_0x0064
        L_0x0009:
            java.lang.String r0 = "daylight"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 2
            goto L_0x0065
        L_0x0013:
            java.lang.String r0 = "fluorescent"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 3
            goto L_0x0065
        L_0x001d:
            java.lang.String r0 = "twilight"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 6
            goto L_0x0065
        L_0x0027:
            java.lang.String r0 = "cloudy-daylight"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 1
            goto L_0x0065
        L_0x0031:
            java.lang.String r0 = "shade"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 5
            goto L_0x0065
        L_0x003b:
            java.lang.String r0 = "auto"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 0
            goto L_0x0065
        L_0x0045:
            java.lang.String r0 = "warm-fluorescent"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 7
            goto L_0x0065
        L_0x004f:
            java.lang.String r0 = "incandescent"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 4
            goto L_0x0065
        L_0x0059:
            java.lang.String r0 = "manual"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 8
            goto L_0x0065
        L_0x0064:
            r0 = -1
        L_0x0065:
            switch(r0) {
                case 0: goto L_0x008a;
                case 1: goto L_0x0086;
                case 2: goto L_0x0082;
                case 3: goto L_0x007e;
                case 4: goto L_0x007a;
                case 5: goto L_0x0076;
                case 6: goto L_0x0072;
                case 7: goto L_0x006e;
                case 8: goto L_0x006a;
                default: goto L_0x0068;
            }
        L_0x0068:
            r0 = -1
            goto L_0x008d
        L_0x006a:
            r0 = 2131493631(0x7f0c02ff, float:1.8610748E38)
            goto L_0x008d
        L_0x006e:
            r0 = 2131493636(0x7f0c0304, float:1.8610758E38)
            goto L_0x008d
        L_0x0072:
            r0 = 2131493633(0x7f0c0301, float:1.8610752E38)
            goto L_0x008d
        L_0x0076:
            r0 = 2131493632(0x7f0c0300, float:1.861075E38)
            goto L_0x008d
        L_0x007a:
            r0 = 2131493628(0x7f0c02fc, float:1.8610742E38)
            goto L_0x008d
        L_0x007e:
            r0 = 2131493627(0x7f0c02fb, float:1.861074E38)
            goto L_0x008d
        L_0x0082:
            r0 = 2131493626(0x7f0c02fa, float:1.8610737E38)
            goto L_0x008d
        L_0x0086:
            r0 = 2131493625(0x7f0c02f9, float:1.8610735E38)
            goto L_0x008d
        L_0x008a:
            r0 = 2131493624(0x7f0c02f8, float:1.8610733E38)
        L_0x008d:
            if (r0 == r1) goto L_0x0099
            net.sourceforge.opencamera.MainActivity r3 = r2.main_activity
            android.content.res.Resources r3 = r3.getResources()
            java.lang.String r3 = r3.getString(r0)
        L_0x0099:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.MainUI.getEntryForWhiteBalance(java.lang.String):java.lang.String");
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getEntryForSceneMode(java.lang.String r3) {
        /*
            r2 = this;
            int r0 = r3.hashCode()
            r1 = -1
            switch(r0) {
                case -1422950858: goto L_0x00ae;
                case -1350043241: goto L_0x00a3;
                case -895760513: goto L_0x0098;
                case -891172202: goto L_0x008d;
                case -333584256: goto L_0x0083;
                case -300277408: goto L_0x0078;
                case -264202484: goto L_0x006e;
                case 3005871: goto L_0x0064;
                case 3535235: goto L_0x0059;
                case 93610339: goto L_0x004f;
                case 104817688: goto L_0x0044;
                case 106437350: goto L_0x0038;
                case 729267099: goto L_0x002c;
                case 1430647483: goto L_0x0021;
                case 1664284080: goto L_0x0015;
                case 1900012073: goto L_0x000a;
                default: goto L_0x0008;
            }
        L_0x0008:
            goto L_0x00b8
        L_0x000a:
            java.lang.String r0 = "candlelight"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 3
            goto L_0x00b9
        L_0x0015:
            java.lang.String r0 = "night-portrait"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 8
            goto L_0x00b9
        L_0x0021:
            java.lang.String r0 = "landscape"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 6
            goto L_0x00b9
        L_0x002c:
            java.lang.String r0 = "portrait"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 10
            goto L_0x00b9
        L_0x0038:
            java.lang.String r0 = "party"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 9
            goto L_0x00b9
        L_0x0044:
            java.lang.String r0 = "night"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 7
            goto L_0x00b9
        L_0x004f:
            java.lang.String r0 = "beach"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 2
            goto L_0x00b9
        L_0x0059:
            java.lang.String r0 = "snow"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 11
            goto L_0x00b9
        L_0x0064:
            java.lang.String r0 = "auto"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 4
            goto L_0x00b9
        L_0x006e:
            java.lang.String r0 = "fireworks"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 5
            goto L_0x00b9
        L_0x0078:
            java.lang.String r0 = "steadyphoto"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 13
            goto L_0x00b9
        L_0x0083:
            java.lang.String r0 = "barcode"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 1
            goto L_0x00b9
        L_0x008d:
            java.lang.String r0 = "sunset"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 14
            goto L_0x00b9
        L_0x0098:
            java.lang.String r0 = "sports"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 12
            goto L_0x00b9
        L_0x00a3:
            java.lang.String r0 = "theatre"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 15
            goto L_0x00b9
        L_0x00ae:
            java.lang.String r0 = "action"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x00b8
            r0 = 0
            goto L_0x00b9
        L_0x00b8:
            r0 = -1
        L_0x00b9:
            switch(r0) {
                case 0: goto L_0x00fa;
                case 1: goto L_0x00f6;
                case 2: goto L_0x00f2;
                case 3: goto L_0x00ee;
                case 4: goto L_0x00ea;
                case 5: goto L_0x00e6;
                case 6: goto L_0x00e2;
                case 7: goto L_0x00de;
                case 8: goto L_0x00da;
                case 9: goto L_0x00d6;
                case 10: goto L_0x00d2;
                case 11: goto L_0x00ce;
                case 12: goto L_0x00ca;
                case 13: goto L_0x00c6;
                case 14: goto L_0x00c2;
                case 15: goto L_0x00be;
                default: goto L_0x00bc;
            }
        L_0x00bc:
            r0 = -1
            goto L_0x00fd
        L_0x00be:
            r0 = 2131493577(0x7f0c02c9, float:1.8610638E38)
            goto L_0x00fd
        L_0x00c2:
            r0 = 2131493576(0x7f0c02c8, float:1.8610636E38)
            goto L_0x00fd
        L_0x00c6:
            r0 = 2131493575(0x7f0c02c7, float:1.8610634E38)
            goto L_0x00fd
        L_0x00ca:
            r0 = 2131493574(0x7f0c02c6, float:1.8610632E38)
            goto L_0x00fd
        L_0x00ce:
            r0 = 2131493573(0x7f0c02c5, float:1.861063E38)
            goto L_0x00fd
        L_0x00d2:
            r0 = 2131493572(0x7f0c02c4, float:1.8610628E38)
            goto L_0x00fd
        L_0x00d6:
            r0 = 2131493571(0x7f0c02c3, float:1.8610626E38)
            goto L_0x00fd
        L_0x00da:
            r0 = 2131493570(0x7f0c02c2, float:1.8610624E38)
            goto L_0x00fd
        L_0x00de:
            r0 = 2131493569(0x7f0c02c1, float:1.8610622E38)
            goto L_0x00fd
        L_0x00e2:
            r0 = 2131493568(0x7f0c02c0, float:1.861062E38)
            goto L_0x00fd
        L_0x00e6:
            r0 = 2131493567(0x7f0c02bf, float:1.8610618E38)
            goto L_0x00fd
        L_0x00ea:
            r0 = 2131493563(0x7f0c02bb, float:1.861061E38)
            goto L_0x00fd
        L_0x00ee:
            r0 = 2131493566(0x7f0c02be, float:1.8610616E38)
            goto L_0x00fd
        L_0x00f2:
            r0 = 2131493565(0x7f0c02bd, float:1.8610614E38)
            goto L_0x00fd
        L_0x00f6:
            r0 = 2131493564(0x7f0c02bc, float:1.8610612E38)
            goto L_0x00fd
        L_0x00fa:
            r0 = 2131493562(0x7f0c02ba, float:1.8610608E38)
        L_0x00fd:
            if (r0 == r1) goto L_0x0109
            net.sourceforge.opencamera.MainActivity r3 = r2.main_activity
            android.content.res.Resources r3 = r3.getResources()
            java.lang.String r3 = r3.getString(r0)
        L_0x0109:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.MainUI.getEntryForSceneMode(java.lang.String):java.lang.String");
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getEntryForColorEffect(java.lang.String r3) {
        /*
            r2 = this;
            int r0 = r3.hashCode()
            r1 = -1
            switch(r0) {
                case -1635350969: goto L_0x005a;
                case 3002044: goto L_0x0050;
                case 3357411: goto L_0x0046;
                case 3387192: goto L_0x003c;
                case 109324790: goto L_0x0032;
                case 261182557: goto L_0x0027;
                case 921111605: goto L_0x001d;
                case 1473417203: goto L_0x0013;
                case 2008448231: goto L_0x0009;
                default: goto L_0x0008;
            }
        L_0x0008:
            goto L_0x0064
        L_0x0009:
            java.lang.String r0 = "posterize"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 5
            goto L_0x0065
        L_0x0013:
            java.lang.String r0 = "solarize"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 7
            goto L_0x0065
        L_0x001d:
            java.lang.String r0 = "negative"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 3
            goto L_0x0065
        L_0x0027:
            java.lang.String r0 = "whiteboard"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 8
            goto L_0x0065
        L_0x0032:
            java.lang.String r0 = "sepia"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 6
            goto L_0x0065
        L_0x003c:
            java.lang.String r0 = "none"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 4
            goto L_0x0065
        L_0x0046:
            java.lang.String r0 = "mono"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 2
            goto L_0x0065
        L_0x0050:
            java.lang.String r0 = "aqua"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 0
            goto L_0x0065
        L_0x005a:
            java.lang.String r0 = "blackboard"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x0064
            r0 = 1
            goto L_0x0065
        L_0x0064:
            r0 = -1
        L_0x0065:
            switch(r0) {
                case 0: goto L_0x008a;
                case 1: goto L_0x0086;
                case 2: goto L_0x0082;
                case 3: goto L_0x007e;
                case 4: goto L_0x007a;
                case 5: goto L_0x0076;
                case 6: goto L_0x0072;
                case 7: goto L_0x006e;
                case 8: goto L_0x006a;
                default: goto L_0x0068;
            }
        L_0x0068:
            r0 = -1
            goto L_0x008d
        L_0x006a:
            r0 = 2131492921(0x7f0c0039, float:1.8609308E38)
            goto L_0x008d
        L_0x006e:
            r0 = 2131492920(0x7f0c0038, float:1.8609306E38)
            goto L_0x008d
        L_0x0072:
            r0 = 2131492919(0x7f0c0037, float:1.8609303E38)
            goto L_0x008d
        L_0x0076:
            r0 = 2131492918(0x7f0c0036, float:1.8609301E38)
            goto L_0x008d
        L_0x007a:
            r0 = 2131492917(0x7f0c0035, float:1.86093E38)
            goto L_0x008d
        L_0x007e:
            r0 = 2131492916(0x7f0c0034, float:1.8609297E38)
            goto L_0x008d
        L_0x0082:
            r0 = 2131492915(0x7f0c0033, float:1.8609295E38)
            goto L_0x008d
        L_0x0086:
            r0 = 2131492914(0x7f0c0032, float:1.8609293E38)
            goto L_0x008d
        L_0x008a:
            r0 = 2131492913(0x7f0c0031, float:1.8609291E38)
        L_0x008d:
            if (r0 == r1) goto L_0x0099
            net.sourceforge.opencamera.MainActivity r3 = r2.main_activity
            android.content.res.Resources r3 = r3.getResources()
            java.lang.String r3 = r3.getString(r0)
        L_0x0099:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.MainUI.getEntryForColorEffect(java.lang.String):java.lang.String");
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getEntryForAntiBanding(java.lang.String r6) {
        /*
            r5 = this;
            int r0 = r6.hashCode()
            r1 = 3
            r2 = 2
            r3 = 1
            r4 = -1
            switch(r0) {
                case 109935: goto L_0x002a;
                case 1628397: goto L_0x0020;
                case 1658188: goto L_0x0016;
                case 3005871: goto L_0x000c;
                default: goto L_0x000b;
            }
        L_0x000b:
            goto L_0x0034
        L_0x000c:
            java.lang.String r0 = "auto"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x0034
            r0 = 0
            goto L_0x0035
        L_0x0016:
            java.lang.String r0 = "60hz"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x0034
            r0 = 2
            goto L_0x0035
        L_0x0020:
            java.lang.String r0 = "50hz"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x0034
            r0 = 1
            goto L_0x0035
        L_0x002a:
            java.lang.String r0 = "off"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x0034
            r0 = 3
            goto L_0x0035
        L_0x0034:
            r0 = -1
        L_0x0035:
            if (r0 == 0) goto L_0x004b
            if (r0 == r3) goto L_0x0047
            if (r0 == r2) goto L_0x0043
            if (r0 == r1) goto L_0x003f
            r0 = -1
            goto L_0x004e
        L_0x003f:
            r0 = 2131492878(0x7f0c000e, float:1.860922E38)
            goto L_0x004e
        L_0x0043:
            r0 = 2131492876(0x7f0c000c, float:1.8609216E38)
            goto L_0x004e
        L_0x0047:
            r0 = 2131492875(0x7f0c000b, float:1.8609214E38)
            goto L_0x004e
        L_0x004b:
            r0 = 2131492877(0x7f0c000d, float:1.8609218E38)
        L_0x004e:
            if (r0 == r4) goto L_0x005a
            net.sourceforge.opencamera.MainActivity r6 = r5.main_activity
            android.content.res.Resources r6 = r6.getResources()
            java.lang.String r6 = r6.getString(r0)
        L_0x005a:
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.MainUI.getEntryForAntiBanding(java.lang.String):java.lang.String");
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getEntryForNoiseReductionMode(java.lang.String r7) {
        /*
            r6 = this;
            int r0 = r7.hashCode()
            r1 = 4
            r2 = 3
            r3 = 2
            r4 = 1
            r5 = -1
            switch(r0) {
                case 109935: goto L_0x0035;
                case 3135580: goto L_0x002b;
                case 1064537505: goto L_0x0021;
                case 1544803905: goto L_0x0017;
                case 1790083938: goto L_0x000d;
                default: goto L_0x000c;
            }
        L_0x000c:
            goto L_0x003f
        L_0x000d:
            java.lang.String r0 = "high_quality"
            boolean r0 = r7.equals(r0)
            if (r0 == 0) goto L_0x003f
            r0 = 4
            goto L_0x0040
        L_0x0017:
            java.lang.String r0 = "default"
            boolean r0 = r7.equals(r0)
            if (r0 == 0) goto L_0x003f
            r0 = 0
            goto L_0x0040
        L_0x0021:
            java.lang.String r0 = "minimal"
            boolean r0 = r7.equals(r0)
            if (r0 == 0) goto L_0x003f
            r0 = 2
            goto L_0x0040
        L_0x002b:
            java.lang.String r0 = "fast"
            boolean r0 = r7.equals(r0)
            if (r0 == 0) goto L_0x003f
            r0 = 3
            goto L_0x0040
        L_0x0035:
            java.lang.String r0 = "off"
            boolean r0 = r7.equals(r0)
            if (r0 == 0) goto L_0x003f
            r0 = 1
            goto L_0x0040
        L_0x003f:
            r0 = -1
        L_0x0040:
            if (r0 == 0) goto L_0x005c
            if (r0 == r4) goto L_0x0058
            if (r0 == r3) goto L_0x0054
            if (r0 == r2) goto L_0x0050
            if (r0 == r1) goto L_0x004c
            r0 = -1
            goto L_0x005f
        L_0x004c:
            r0 = 2131493028(0x7f0c00a4, float:1.8609525E38)
            goto L_0x005f
        L_0x0050:
            r0 = 2131493027(0x7f0c00a3, float:1.8609523E38)
            goto L_0x005f
        L_0x0054:
            r0 = 2131493029(0x7f0c00a5, float:1.8609527E38)
            goto L_0x005f
        L_0x0058:
            r0 = 2131493030(0x7f0c00a6, float:1.8609529E38)
            goto L_0x005f
        L_0x005c:
            r0 = 2131493026(0x7f0c00a2, float:1.860952E38)
        L_0x005f:
            if (r0 == r5) goto L_0x006b
            net.sourceforge.opencamera.MainActivity r7 = r6.main_activity
            android.content.res.Resources r7 = r7.getResources()
            java.lang.String r7 = r7.getString(r0)
        L_0x006b:
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.MainUI.getEntryForNoiseReductionMode(java.lang.String):java.lang.String");
    }

    /* access modifiers changed from: 0000 */
    public View getTopIcon() {
        return this.top_icon;
    }

    public View getUIButton(String str) {
        return (View) this.test_ui_buttons.get(str);
    }

    /* access modifiers changed from: 0000 */
    public Map<String, View> getTestUIButtonsMap() {
        return this.test_ui_buttons;
    }

    public PopupView getPopupView() {
        return this.popup_view;
    }

    public boolean testGetRemoteControlMode() {
        return this.remote_control_mode;
    }

    public int testGetPopupLine() {
        return this.mPopupLine;
    }

    public int testGetPopupIcon() {
        return this.mPopupIcon;
    }

    public int testGetExposureLine() {
        return this.mExposureLine;
    }
}
