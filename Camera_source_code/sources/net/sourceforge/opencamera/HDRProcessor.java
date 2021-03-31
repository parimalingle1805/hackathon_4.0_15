package net.sourceforge.opencamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RSInvalidStateException;
import android.renderscript.RenderScript;
import android.renderscript.Script.LaunchOptions;
import android.renderscript.Type;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HDRProcessor {
    private static final String TAG = "HDRProcessor";
    private ScriptC_align_mtb alignMTBScript;
    private int cached_avg_sample_size = 1;
    private final Context context;
    private ScriptC_create_mtb createMTBScript;
    private final boolean is_test;
    public int[] offsets_x = null;
    public int[] offsets_y = null;
    private ScriptC_process_avg processAvgScript;

    /* renamed from: rs */
    private RenderScript f11rs;
    public int sharp_index = 0;

    /* renamed from: net.sourceforge.opencamera.HDRProcessor$2 */
    static /* synthetic */ class C02362 {
        static final /* synthetic */ int[] $SwitchMap$net$sourceforge$opencamera$HDRProcessor$HDRAlgorithm = new int[HDRAlgorithm.values().length];

        /* renamed from: $SwitchMap$net$sourceforge$opencamera$HDRProcessor$TonemappingAlgorithm */
        static final /* synthetic */ int[] f12x219ad625 = new int[TonemappingAlgorithm.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(17:0|(2:1|2)|3|5|6|7|8|9|10|11|12|13|15|16|17|18|20) */
        /* JADX WARNING: Can't wrap try/catch for region: R(18:0|1|2|3|5|6|7|8|9|10|11|12|13|15|16|17|18|20) */
        /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x0035 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0053 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x001f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x002a */
        static {
            /*
                net.sourceforge.opencamera.HDRProcessor$TonemappingAlgorithm[] r0 = net.sourceforge.opencamera.HDRProcessor.TonemappingAlgorithm.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f12x219ad625 = r0
                r0 = 1
                int[] r1 = f12x219ad625     // Catch:{ NoSuchFieldError -> 0x0014 }
                net.sourceforge.opencamera.HDRProcessor$TonemappingAlgorithm r2 = net.sourceforge.opencamera.HDRProcessor.TonemappingAlgorithm.TONEMAPALGORITHM_CLAMP     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r1[r2] = r0     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                r1 = 2
                int[] r2 = f12x219ad625     // Catch:{ NoSuchFieldError -> 0x001f }
                net.sourceforge.opencamera.HDRProcessor$TonemappingAlgorithm r3 = net.sourceforge.opencamera.HDRProcessor.TonemappingAlgorithm.TONEMAPALGORITHM_EXPONENTIAL     // Catch:{ NoSuchFieldError -> 0x001f }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2[r3] = r1     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                int[] r2 = f12x219ad625     // Catch:{ NoSuchFieldError -> 0x002a }
                net.sourceforge.opencamera.HDRProcessor$TonemappingAlgorithm r3 = net.sourceforge.opencamera.HDRProcessor.TonemappingAlgorithm.TONEMAPALGORITHM_REINHARD     // Catch:{ NoSuchFieldError -> 0x002a }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x002a }
                r4 = 3
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x002a }
            L_0x002a:
                int[] r2 = f12x219ad625     // Catch:{ NoSuchFieldError -> 0x0035 }
                net.sourceforge.opencamera.HDRProcessor$TonemappingAlgorithm r3 = net.sourceforge.opencamera.HDRProcessor.TonemappingAlgorithm.TONEMAPALGORITHM_FILMIC     // Catch:{ NoSuchFieldError -> 0x0035 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0035 }
                r4 = 4
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x0035 }
            L_0x0035:
                int[] r2 = f12x219ad625     // Catch:{ NoSuchFieldError -> 0x0040 }
                net.sourceforge.opencamera.HDRProcessor$TonemappingAlgorithm r3 = net.sourceforge.opencamera.HDRProcessor.TonemappingAlgorithm.TONEMAPALGORITHM_ACES     // Catch:{ NoSuchFieldError -> 0x0040 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0040 }
                r4 = 5
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x0040 }
            L_0x0040:
                net.sourceforge.opencamera.HDRProcessor$HDRAlgorithm[] r2 = net.sourceforge.opencamera.HDRProcessor.HDRAlgorithm.values()
                int r2 = r2.length
                int[] r2 = new int[r2]
                $SwitchMap$net$sourceforge$opencamera$HDRProcessor$HDRAlgorithm = r2
                int[] r2 = $SwitchMap$net$sourceforge$opencamera$HDRProcessor$HDRAlgorithm     // Catch:{ NoSuchFieldError -> 0x0053 }
                net.sourceforge.opencamera.HDRProcessor$HDRAlgorithm r3 = net.sourceforge.opencamera.HDRProcessor.HDRAlgorithm.HDRALGORITHM_SINGLE_IMAGE     // Catch:{ NoSuchFieldError -> 0x0053 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0053 }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x0053 }
            L_0x0053:
                int[] r0 = $SwitchMap$net$sourceforge$opencamera$HDRProcessor$HDRAlgorithm     // Catch:{ NoSuchFieldError -> 0x005d }
                net.sourceforge.opencamera.HDRProcessor$HDRAlgorithm r2 = net.sourceforge.opencamera.HDRProcessor.HDRAlgorithm.HDRALGORITHM_STANDARD     // Catch:{ NoSuchFieldError -> 0x005d }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x005d }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x005d }
            L_0x005d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.HDRProcessor.C02362.<clinit>():void");
        }
    }

    public static class AvgData {
        Allocation allocation_avg_align;
        public Allocation allocation_out;
        Bitmap bitmap_avg_align;

        AvgData(Allocation allocation, Bitmap bitmap, Allocation allocation2) {
            this.allocation_out = allocation;
            this.bitmap_avg_align = bitmap;
            this.allocation_avg_align = allocation2;
        }

        public void destroy() {
            Allocation allocation = this.allocation_out;
            if (allocation != null) {
                allocation.destroy();
                this.allocation_out = null;
            }
            Bitmap bitmap = this.bitmap_avg_align;
            if (bitmap != null) {
                bitmap.recycle();
                this.bitmap_avg_align = null;
            }
            Allocation allocation2 = this.allocation_avg_align;
            if (allocation2 != null) {
                allocation2.destroy();
                this.allocation_avg_align = null;
            }
        }
    }

    public static class BrightenFactors {
        public final float gain;
        public final float gamma;
        public final float low_x;
        public final float mid_x;

        BrightenFactors(float f, float f2, float f3, float f4) {
            this.gain = f;
            this.low_x = f2;
            this.mid_x = f3;
            this.gamma = f4;
        }
    }

    static class BrightnessDetails {
        final int median_brightness;

        BrightnessDetails(int i) {
            this.median_brightness = i;
        }
    }

    public enum DROTonemappingAlgorithm {
        DROALGORITHM_NONE,
        DROALGORITHM_GAINGAMMA
    }

    private enum HDRAlgorithm {
        HDRALGORITHM_STANDARD,
        HDRALGORITHM_SINGLE_IMAGE
    }

    static class HistogramInfo {
        final int max_brightness;
        final int mean_brightness;
        final int median_brightness;
        final int total;

        HistogramInfo(int i, int i2, int i3, int i4) {
            this.total = i;
            this.mean_brightness = i2;
            this.median_brightness = i3;
            this.max_brightness = i4;
        }
    }

    private static class LuminanceInfo {
        final int median_value;
        final boolean noisy;

        LuminanceInfo(int i, boolean z) {
            this.median_value = i;
            this.noisy = z;
        }
    }

    private static class ResponseFunction {
        float parameter_A;
        float parameter_B;

        private ResponseFunction(float f, float f2) {
            this.parameter_A = f;
            this.parameter_B = f2;
        }

        static ResponseFunction createIdentity() {
            return new ResponseFunction(1.0f, 0.0f);
        }

        /* JADX WARNING: Removed duplicated region for block: B:19:0x00a6  */
        /* JADX WARNING: Removed duplicated region for block: B:39:? A[RETURN, SYNTHETIC] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        ResponseFunction(android.content.Context r27, int r28, java.util.List<java.lang.Double> r29, java.util.List<java.lang.Double> r30, java.util.List<java.lang.Double> r31) {
            /*
                r26 = this;
                r0 = r26
                r1 = r29
                r2 = r30
                r3 = r31
                r26.<init>()
                int r4 = r29.size()
                int r5 = r30.size()
                if (r4 != r5) goto L_0x0100
                int r4 = r29.size()
                int r5 = r31.size()
                if (r4 != r5) goto L_0x00fa
                int r4 = r29.size()
                r5 = 3
                if (r4 <= r5) goto L_0x00f4
                r5 = 0
                r8 = r5
                r10 = r8
                r12 = r10
                r14 = r12
                r16 = r14
                r7 = 0
            L_0x002f:
                int r4 = r29.size()
                if (r7 >= r4) goto L_0x0068
                java.lang.Object r4 = r1.get(r7)
                java.lang.Double r4 = (java.lang.Double) r4
                double r18 = r4.doubleValue()
                java.lang.Object r4 = r2.get(r7)
                java.lang.Double r4 = (java.lang.Double) r4
                double r20 = r4.doubleValue()
                java.lang.Object r4 = r3.get(r7)
                java.lang.Double r4 = (java.lang.Double) r4
                double r22 = r4.doubleValue()
                double r24 = r22 * r18
                double r10 = r10 + r24
                double r18 = r18 * r24
                double r16 = r16 + r18
                double r24 = r24 * r20
                double r14 = r14 + r24
                double r20 = r20 * r22
                double r8 = r8 + r20
                double r12 = r12 + r22
                int r7 = r7 + 1
                goto L_0x002f
            L_0x0068:
                double r18 = r8 * r10
                double r14 = r14 * r12
                double r18 = r18 - r14
                double r14 = r10 * r10
                double r16 = r16 * r12
                double r14 = r14 - r16
                double r16 = java.lang.Math.abs(r14)
                r20 = 4532020583610935537(0x3ee4f8b588e368f1, double:1.0E-5)
                int r4 = (r16 > r20 ? 1 : (r16 == r20 ? 0 : -1))
                if (r4 >= 0) goto L_0x0082
                goto L_0x00a1
            L_0x0082:
                double r14 = r18 / r14
                float r4 = (float) r14
                r0.parameter_A = r4
                float r4 = r0.parameter_A
                double r14 = (double) r4
                java.lang.Double.isNaN(r14)
                double r14 = r14 * r10
                double r8 = r8 - r14
                double r8 = r8 / r12
                float r7 = (float) r8
                r0.parameter_B = r7
                double r7 = (double) r4
                int r4 = (r7 > r20 ? 1 : (r7 == r20 ? 0 : -1))
                if (r4 >= 0) goto L_0x009a
                goto L_0x00a1
            L_0x009a:
                float r4 = r0.parameter_B
                double r7 = (double) r4
                int r4 = (r7 > r20 ? 1 : (r7 == r20 ? 0 : -1))
                if (r4 >= 0) goto L_0x00a3
            L_0x00a1:
                r4 = 0
                goto L_0x00a4
            L_0x00a3:
                r4 = 1
            L_0x00a4:
                if (r4 != 0) goto L_0x00f3
                r7 = r5
                r4 = 0
            L_0x00a8:
                int r9 = r29.size()
                if (r4 >= r9) goto L_0x00d7
                java.lang.Object r9 = r1.get(r4)
                java.lang.Double r9 = (java.lang.Double) r9
                double r9 = r9.doubleValue()
                java.lang.Object r11 = r2.get(r4)
                java.lang.Double r11 = (java.lang.Double) r11
                double r11 = r11.doubleValue()
                java.lang.Object r13 = r3.get(r4)
                java.lang.Double r13 = (java.lang.Double) r13
                double r13 = r13.doubleValue()
                double r13 = r13 * r9
                double r11 = r11 * r13
                double r7 = r7 + r11
                double r13 = r13 * r9
                double r5 = r5 + r13
                int r4 = r4 + 1
                goto L_0x00a8
            L_0x00d7:
                int r1 = (r5 > r20 ? 1 : (r5 == r20 ? 0 : -1))
                if (r1 >= 0) goto L_0x00e0
                r1 = 1065353216(0x3f800000, float:1.0)
                r0.parameter_A = r1
                goto L_0x00f0
            L_0x00e0:
                double r7 = r7 / r5
                float r1 = (float) r7
                r0.parameter_A = r1
                float r1 = r0.parameter_A
                double r1 = (double) r1
                int r3 = (r1 > r20 ? 1 : (r1 == r20 ? 0 : -1))
                if (r3 >= 0) goto L_0x00f0
                r1 = 925353388(0x3727c5ac, float:1.0E-5)
                r0.parameter_A = r1
            L_0x00f0:
                r1 = 0
                r0.parameter_B = r1
            L_0x00f3:
                return
            L_0x00f4:
                java.lang.RuntimeException r1 = new java.lang.RuntimeException
                r1.<init>()
                throw r1
            L_0x00fa:
                java.lang.RuntimeException r1 = new java.lang.RuntimeException
                r1.<init>()
                throw r1
            L_0x0100:
                java.lang.RuntimeException r1 = new java.lang.RuntimeException
                r1.<init>()
                goto L_0x0107
            L_0x0106:
                throw r1
            L_0x0107:
                goto L_0x0106
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.HDRProcessor.ResponseFunction.<init>(android.content.Context, int, java.util.List, java.util.List, java.util.List):void");
        }
    }

    public interface SortCallback {
        void sortOrder(List<Integer> list);
    }

    public enum TonemappingAlgorithm {
        TONEMAPALGORITHM_CLAMP,
        TONEMAPALGORITHM_EXPONENTIAL,
        TONEMAPALGORITHM_REINHARD,
        TONEMAPALGORITHM_FILMIC,
        TONEMAPALGORITHM_ACES
    }

    public HDRProcessor(Context context2, boolean z) {
        this.context = context2;
        this.is_test = z;
    }

    private void freeScripts() {
        this.processAvgScript = null;
        this.createMTBScript = null;
        this.alignMTBScript = null;
    }

    public void onDestroy() {
        freeScripts();
        RenderScript renderScript = this.f11rs;
        if (renderScript != null) {
            try {
                renderScript.destroy();
            } catch (RSInvalidStateException e) {
                e.printStackTrace();
            }
            this.f11rs = null;
        }
    }

    public void processHDR(List<Bitmap> list, boolean z, Bitmap bitmap, boolean z2, SortCallback sortCallback, float f, int i, boolean z3, TonemappingAlgorithm tonemappingAlgorithm, DROTonemappingAlgorithm dROTonemappingAlgorithm) throws HDRProcessorException {
        List<Bitmap> list2;
        SortCallback sortCallback2 = sortCallback;
        if (z2 || z) {
            list2 = list;
        } else {
            List<Bitmap> list3 = list;
            list2 = new ArrayList<>(list);
        }
        int size = list2.size();
        if (size < 1 || size > 7) {
            throw new HDRProcessorException(0);
        }
        int i2 = 1;
        while (i2 < size) {
            if (((Bitmap) list2.get(i2)).getWidth() == ((Bitmap) list2.get(0)).getWidth() && ((Bitmap) list2.get(i2)).getHeight() == ((Bitmap) list2.get(0)).getHeight()) {
                i2++;
            } else {
                throw new HDRProcessorException(1);
            }
        }
        int i3 = C02362.$SwitchMap$net$sourceforge$opencamera$HDRProcessor$HDRAlgorithm[(size == 1 ? HDRAlgorithm.HDRALGORITHM_SINGLE_IMAGE : HDRAlgorithm.HDRALGORITHM_STANDARD).ordinal()];
        if (i3 == 1) {
            if (!z2 && sortCallback2 != null) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(Integer.valueOf(0));
                sortCallback.sortOrder(arrayList);
            }
            processSingleImage(list2, z, bitmap, f, i, z3, dROTonemappingAlgorithm);
        } else if (i3 == 2) {
            processHDRCore(list2, z, bitmap, z2, sortCallback, f, i, z3, tonemappingAlgorithm);
        } else {
            throw new RuntimeException();
        }
    }

    private ResponseFunction createFunctionFromBitmaps(int i, Bitmap bitmap, Bitmap bitmap2, int i2, int i3) {
        ArrayList arrayList;
        ArrayList arrayList2;
        ArrayList arrayList3 = new ArrayList();
        ArrayList arrayList4 = new ArrayList();
        ArrayList arrayList5 = new ArrayList();
        int sqrt = (int) Math.sqrt(100.0d);
        int i4 = 100 / sqrt;
        double d = 0.0d;
        double d2 = 0.0d;
        int i5 = 0;
        while (i5 < i4) {
            double d3 = (double) i5;
            double d4 = 1.0d;
            Double.isNaN(d3);
            double d5 = d3 + 1.0d;
            ArrayList arrayList6 = arrayList3;
            double d6 = (double) i4;
            Double.isNaN(d6);
            double d7 = d5 / (d6 + 1.0d);
            double height = (double) bitmap.getHeight();
            Double.isNaN(height);
            int i6 = (int) (d7 * height);
            int i7 = 0;
            while (i7 < sqrt) {
                double d8 = (double) i7;
                Double.isNaN(d8);
                double d9 = d8 + d4;
                ArrayList arrayList7 = arrayList5;
                int i8 = i5;
                double d10 = (double) sqrt;
                Double.isNaN(d10);
                double d11 = d9 / (d10 + d4);
                double width = (double) bitmap.getWidth();
                Double.isNaN(width);
                int i9 = (int) (d11 * width);
                int i10 = i9 + i2;
                if (i10 >= 0 && i10 < bitmap.getWidth()) {
                    int i11 = i6 + i3;
                    if (i11 >= 0 && i11 < bitmap.getHeight()) {
                        int pixel = bitmap.getPixel(i10, i11);
                        int pixel2 = bitmap2.getPixel(i9, i6);
                        double averageRGB = averageRGB(pixel);
                        double averageRGB2 = averageRGB(pixel2);
                        d2 += averageRGB;
                        d += averageRGB2;
                        arrayList2 = arrayList6;
                        arrayList2.add(Double.valueOf(averageRGB));
                        arrayList4.add(Double.valueOf(averageRGB2));
                        i7++;
                        arrayList6 = arrayList2;
                        arrayList5 = arrayList7;
                        i5 = i8;
                        d4 = 1.0d;
                    }
                }
                Bitmap bitmap3 = bitmap;
                Bitmap bitmap4 = bitmap2;
                arrayList2 = arrayList6;
                i7++;
                arrayList6 = arrayList2;
                arrayList5 = arrayList7;
                i5 = i8;
                d4 = 1.0d;
            }
            Bitmap bitmap5 = bitmap;
            Bitmap bitmap6 = bitmap2;
            ArrayList arrayList8 = arrayList5;
            i5++;
            arrayList3 = arrayList6;
        }
        ArrayList arrayList9 = arrayList3;
        ArrayList arrayList10 = arrayList5;
        if (arrayList9.size() == 0) {
            Log.e(TAG, "no samples for response function!");
            d2 += 255.0d;
            d += 255.0d;
            arrayList9.add(Double.valueOf(255.0d));
            arrayList4.add(Double.valueOf(255.0d));
        }
        double size = (double) arrayList9.size();
        Double.isNaN(size);
        double d12 = d2 / size;
        double size2 = (double) arrayList9.size();
        Double.isNaN(size2);
        boolean z = d12 < d / size2;
        double doubleValue = ((Double) arrayList9.get(0)).doubleValue();
        double doubleValue2 = ((Double) arrayList9.get(0)).doubleValue();
        for (int i12 = 1; i12 < arrayList9.size(); i12++) {
            double doubleValue3 = ((Double) arrayList9.get(i12)).doubleValue();
            if (doubleValue3 < doubleValue) {
                doubleValue = doubleValue3;
            }
            if (doubleValue3 > doubleValue2) {
                doubleValue2 = doubleValue3;
            }
        }
        double d13 = (doubleValue + doubleValue2) * 0.5d;
        int i13 = 0;
        double doubleValue4 = ((Double) arrayList4.get(0)).doubleValue();
        double doubleValue5 = ((Double) arrayList4.get(0)).doubleValue();
        for (int i14 = 1; i14 < arrayList4.size(); i14++) {
            double doubleValue6 = ((Double) arrayList4.get(i14)).doubleValue();
            if (doubleValue6 < doubleValue4) {
                doubleValue4 = doubleValue6;
            }
            if (doubleValue6 > doubleValue5) {
                doubleValue5 = doubleValue6;
            }
        }
        double d14 = (doubleValue4 + doubleValue5) * 0.5d;
        while (i13 < arrayList9.size()) {
            double doubleValue7 = ((Double) arrayList9.get(i13)).doubleValue();
            double doubleValue8 = ((Double) arrayList4.get(i13)).doubleValue();
            if (z) {
                double d15 = doubleValue7 <= d13 ? doubleValue7 - doubleValue : doubleValue2 - doubleValue7;
                double d16 = doubleValue8 <= d14 ? doubleValue8 - doubleValue4 : doubleValue5 - doubleValue8;
                if (d16 < d15) {
                    d15 = d16;
                }
                arrayList = arrayList10;
                arrayList.add(Double.valueOf(d15));
            } else {
                arrayList = arrayList10;
                arrayList.add(Double.valueOf(doubleValue7 <= d13 ? doubleValue7 - doubleValue : doubleValue2 - doubleValue7));
            }
            i13++;
            arrayList10 = arrayList;
        }
        ResponseFunction responseFunction = new ResponseFunction(this.context, i, arrayList9, arrayList4, arrayList10);
        return responseFunction;
    }

    private double averageRGB(int i) {
        double d = (double) (((16711680 & i) >> 16) + ((65280 & i) >> 8) + (i & 255));
        Double.isNaN(d);
        return d / 3.0d;
    }

    private void processHDRCore(List<Bitmap> list, boolean z, Bitmap bitmap, boolean z2, SortCallback sortCallback, float f, int i, boolean z3, TonemappingAlgorithm tonemappingAlgorithm) {
        boolean z4;
        Bitmap bitmap2;
        Allocation allocation;
        Allocation allocation2;
        Allocation allocation3;
        ResponseFunction responseFunction;
        List<Bitmap> list2 = list;
        Bitmap bitmap3 = bitmap;
        long currentTimeMillis = System.currentTimeMillis();
        int size = list.size();
        int width = ((Bitmap) list2.get(0)).getWidth();
        int height = ((Bitmap) list2.get(0)).getHeight();
        ResponseFunction[] responseFunctionArr = new ResponseFunction[size];
        this.offsets_x = new int[size];
        this.offsets_y = new int[size];
        initRenderscript();
        Allocation[] allocationArr = new Allocation[size];
        for (int i2 = 0; i2 < size; i2++) {
            allocationArr[i2] = Allocation.createFromBitmap(this.f11rs, (Bitmap) list2.get(i2));
        }
        int i3 = size % 2;
        int i4 = i3 == 0 ? size / 2 : (size - 1) / 2;
        Allocation[] allocationArr2 = allocationArr;
        ResponseFunction[] responseFunctionArr2 = responseFunctionArr;
        int i5 = size;
        int i6 = autoAlignment(this.offsets_x, this.offsets_y, allocationArr, width, height, list, i4, z2, sortCallback, true, false, 1, true, 1, width, height, currentTimeMillis).median_brightness;
        int i7 = i5;
        boolean z5 = i7 != 3;
        int i8 = 0;
        while (i8 < i7) {
            int i9 = i4;
            if (i8 != i9) {
                List<Bitmap> list3 = list;
                responseFunction = createFunctionFromBitmaps(i8, (Bitmap) list3.get(i8), (Bitmap) list3.get(i9), this.offsets_x[i8], this.offsets_y[i8]);
            } else {
                List<Bitmap> list4 = list;
                responseFunction = z5 ? ResponseFunction.createIdentity() : null;
            }
            responseFunctionArr2[i8] = responseFunction;
            i8++;
            i4 = i9;
        }
        List<Bitmap> list5 = list;
        int i10 = i4;
        if (i3 == 0) {
            int i11 = i10 - 1;
            float sqrt = (float) Math.sqrt((double) responseFunctionArr2[i11].parameter_A);
            float f2 = responseFunctionArr2[i11].parameter_B / (sqrt + 1.0f);
            if (sqrt < 1.0E-5f) {
                sqrt = 1.0E-5f;
            }
            for (int i12 = 0; i12 < i7; i12++) {
                float f3 = responseFunctionArr2[i12].parameter_A;
                float f4 = responseFunctionArr2[i12].parameter_B;
                responseFunctionArr2[i12].parameter_A = f3 / sqrt;
                responseFunctionArr2[i12].parameter_B = f4 - ((f3 * f2) / sqrt);
            }
        }
        ScriptC_process_hdr scriptC_process_hdr = new ScriptC_process_hdr(this.f11rs);
        scriptC_process_hdr.set_bitmap0(allocationArr2[0]);
        if (i7 > 2) {
            scriptC_process_hdr.set_bitmap2(allocationArr2[2]);
        }
        scriptC_process_hdr.set_offset_x0(this.offsets_x[0]);
        scriptC_process_hdr.set_offset_y0(this.offsets_y[0]);
        if (i7 > 2) {
            scriptC_process_hdr.set_offset_x2(this.offsets_x[2]);
            scriptC_process_hdr.set_offset_y2(this.offsets_y[2]);
        }
        scriptC_process_hdr.set_parameter_A0(responseFunctionArr2[0].parameter_A);
        scriptC_process_hdr.set_parameter_B0(responseFunctionArr2[0].parameter_B);
        if (i7 > 2) {
            scriptC_process_hdr.set_parameter_A2(responseFunctionArr2[2].parameter_A);
            scriptC_process_hdr.set_parameter_B2(responseFunctionArr2[2].parameter_B);
        }
        if (z5) {
            scriptC_process_hdr.set_bitmap1(allocationArr2[1]);
            scriptC_process_hdr.set_offset_x1(this.offsets_x[1]);
            scriptC_process_hdr.set_offset_y1(this.offsets_y[1]);
            scriptC_process_hdr.set_parameter_A1(responseFunctionArr2[1].parameter_A);
            scriptC_process_hdr.set_parameter_B1(responseFunctionArr2[1].parameter_B);
        }
        if (i7 > 3) {
            scriptC_process_hdr.set_bitmap3(allocationArr2[3]);
            scriptC_process_hdr.set_offset_x3(this.offsets_x[3]);
            scriptC_process_hdr.set_offset_y3(this.offsets_y[3]);
            scriptC_process_hdr.set_parameter_A3(responseFunctionArr2[3].parameter_A);
            scriptC_process_hdr.set_parameter_B3(responseFunctionArr2[3].parameter_B);
            if (i7 > 4) {
                scriptC_process_hdr.set_bitmap4(allocationArr2[4]);
                scriptC_process_hdr.set_offset_x4(this.offsets_x[4]);
                scriptC_process_hdr.set_offset_y4(this.offsets_y[4]);
                scriptC_process_hdr.set_parameter_A4(responseFunctionArr2[4].parameter_A);
                scriptC_process_hdr.set_parameter_B4(responseFunctionArr2[4].parameter_B);
                if (i7 > 5) {
                    scriptC_process_hdr.set_bitmap5(allocationArr2[5]);
                    scriptC_process_hdr.set_offset_x5(this.offsets_x[5]);
                    scriptC_process_hdr.set_offset_y5(this.offsets_y[5]);
                    scriptC_process_hdr.set_parameter_A5(responseFunctionArr2[5].parameter_A);
                    scriptC_process_hdr.set_parameter_B5(responseFunctionArr2[5].parameter_B);
                    if (i7 > 6) {
                        scriptC_process_hdr.set_bitmap6(allocationArr2[6]);
                        scriptC_process_hdr.set_offset_x6(this.offsets_x[6]);
                        scriptC_process_hdr.set_offset_y6(this.offsets_y[6]);
                        scriptC_process_hdr.set_parameter_A6(responseFunctionArr2[6].parameter_A);
                        scriptC_process_hdr.set_parameter_B6(responseFunctionArr2[6].parameter_B);
                    }
                }
            }
        }
        int i13 = C02362.f12x219ad625[tonemappingAlgorithm.ordinal()];
        if (i13 == 1) {
            scriptC_process_hdr.set_tonemap_algorithm(scriptC_process_hdr.get_tonemap_algorithm_clamp_c());
        } else if (i13 == 2) {
            scriptC_process_hdr.set_tonemap_algorithm(scriptC_process_hdr.get_tonemap_algorithm_exponential_c());
        } else if (i13 == 3) {
            scriptC_process_hdr.set_tonemap_algorithm(scriptC_process_hdr.get_tonemap_algorithm_reinhard_c());
        } else if (i13 == 4) {
            scriptC_process_hdr.set_tonemap_algorithm(scriptC_process_hdr.get_tonemap_algorithm_filmic_c());
        } else if (i13 == 5) {
            scriptC_process_hdr.set_tonemap_algorithm(scriptC_process_hdr.get_tonemap_algorithm_aces_c());
        }
        float f5 = 255.0f;
        float f6 = (responseFunctionArr2[0].parameter_A * 255.0f) + responseFunctionArr2[0].parameter_B;
        if (f6 < 255.0f) {
            f6 = 255.0f;
        }
        float f7 = 255.0f / f6;
        float brightnessTarget = (float) getBrightnessTarget(i6, 2.0f, 119);
        float f8 = brightnessTarget / ((float) i6);
        if (f7 < (f8 + (brightnessTarget / 255.0f)) - 1.0f) {
            float f9 = f8 - f7;
            if (f9 != 0.0f) {
                f5 = (255.0f - brightnessTarget) / f9;
            }
        }
        scriptC_process_hdr.set_tonemap_scale(f5);
        int i14 = C02362.f12x219ad625[tonemappingAlgorithm.ordinal()];
        if (i14 == 2) {
            double d = (double) ((-scriptC_process_hdr.get_exposure()) * f6);
            Double.isNaN(d);
            scriptC_process_hdr.set_linear_scale((float) (1.0d / (1.0d - Math.exp(d / 255.0d))));
        } else if (i14 == 3) {
            scriptC_process_hdr.set_linear_scale((f5 + f6) / f6);
        } else if (i14 == 4) {
            scriptC_process_hdr.set_W(scriptC_process_hdr.get_filmic_exposure_bias() * f6);
        }
        if (z) {
            bitmap2 = bitmap;
            allocation = allocationArr2[i10];
            z4 = false;
        } else {
            bitmap2 = bitmap;
            allocation = Allocation.createFromBitmap(this.f11rs, bitmap2);
            z4 = true;
        }
        if (z5) {
            scriptC_process_hdr.set_n_bitmaps_g(i7);
            scriptC_process_hdr.forEach_hdr_n(allocationArr2[i10], allocation);
        } else {
            scriptC_process_hdr.forEach_hdr(allocationArr2[i10], allocation);
        }
        if (z) {
            for (int i15 = 0; i15 < list.size(); i15++) {
                if (i15 != i10) {
                    ((Bitmap) list5.get(i15)).recycle();
                }
            }
        }
        if (f != 0.0f) {
            allocation2 = allocation;
            adjustHistogram(allocation, allocation, width, height, f, i, z3, currentTimeMillis);
        } else {
            allocation2 = allocation;
        }
        if (z) {
            allocationArr2[i10].copyTo((Bitmap) list5.get(i10));
            list5.set(0, list5.get(i10));
            for (int i16 = 1; i16 < list.size(); i16++) {
                list5.set(i16, null);
            }
            allocation3 = allocation2;
        } else {
            allocation3 = allocation2;
            allocation3.copyTo(bitmap2);
        }
        if (z4) {
            allocation3.destroy();
        }
        for (int i17 = 0; i17 < i7; i17++) {
            allocationArr2[i17].destroy();
            allocationArr2[i17] = null;
        }
        freeScripts();
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x00c5  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00d0  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00d5  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processSingleImage(java.util.List<android.graphics.Bitmap> r23, boolean r24, android.graphics.Bitmap r25, float r26, int r27, boolean r28, net.sourceforge.opencamera.HDRProcessor.DROTonemappingAlgorithm r29) throws net.sourceforge.opencamera.HDRProcessorException {
        /*
            r22 = this;
            r10 = r22
            r11 = r23
            r12 = r25
            long r8 = java.lang.System.currentTimeMillis()
            r13 = 0
            java.lang.Object r0 = r11.get(r13)
            android.graphics.Bitmap r0 = (android.graphics.Bitmap) r0
            int r3 = r0.getWidth()
            java.lang.Object r0 = r11.get(r13)
            android.graphics.Bitmap r0 = (android.graphics.Bitmap) r0
            int r4 = r0.getHeight()
            r22.initRenderscript()
            android.renderscript.RenderScript r0 = r10.f11rs
            java.lang.Object r1 = r11.get(r13)
            android.graphics.Bitmap r1 = (android.graphics.Bitmap) r1
            android.renderscript.Allocation r0 = android.renderscript.Allocation.createFromBitmap(r0, r1)
            if (r24 == 0) goto L_0x0033
            r14 = r0
            r1 = 0
            goto L_0x003b
        L_0x0033:
            r1 = 1
            android.renderscript.RenderScript r2 = r10.f11rs
            android.renderscript.Allocation r2 = android.renderscript.Allocation.createFromBitmap(r2, r12)
            r14 = r2
        L_0x003b:
            net.sourceforge.opencamera.HDRProcessor$DROTonemappingAlgorithm r2 = net.sourceforge.opencamera.HDRProcessor.DROTonemappingAlgorithm.DROALGORITHM_GAINGAMMA
            r5 = r29
            if (r5 != r2) goto L_0x00b4
            int[] r2 = r10.computeHistogram(r0, r13, r13)
            net.sourceforge.opencamera.HDRProcessor$HistogramInfo r2 = r10.getHistogramInfo(r2)
            int r5 = r2.median_brightness
            int r2 = r2.max_brightness
            r15 = 0
            r16 = 0
            r17 = 0
            r19 = r5
            r20 = r2
            net.sourceforge.opencamera.HDRProcessor$BrightenFactors r5 = computeBrightenFactors(r15, r16, r17, r19, r20)
            float r6 = r5.gain
            float r7 = r5.gamma
            float r15 = r5.low_x
            float r5 = r5.mid_x
            r21 = r14
            double r13 = (double) r6
            r16 = 4607182418800017408(0x3ff0000000000000, double:1.0)
            java.lang.Double.isNaN(r13)
            double r13 = r13 - r16
            double r13 = java.lang.Math.abs(r13)
            r18 = 4532020583610935537(0x3ee4f8b588e368f1, double:1.0E-5)
            int r20 = (r13 > r18 ? 1 : (r13 == r18 ? 0 : -1))
            if (r20 > 0) goto L_0x008f
            r13 = 255(0xff, float:3.57E-43)
            if (r2 != r13) goto L_0x008f
            double r13 = (double) r7
            java.lang.Double.isNaN(r13)
            double r13 = r13 - r16
            double r13 = java.lang.Math.abs(r13)
            int r16 = (r13 > r18 ? 1 : (r13 == r18 ? 0 : -1))
            if (r16 <= 0) goto L_0x008c
            goto L_0x008f
        L_0x008c:
            r14 = r21
            goto L_0x00b4
        L_0x008f:
            net.sourceforge.opencamera.ScriptC_avg_brighten r13 = new net.sourceforge.opencamera.ScriptC_avg_brighten
            android.renderscript.RenderScript r14 = r10.f11rs
            r13.<init>(r14)
            float r2 = (float) r2
            r14 = r15
            r15 = r13
            r16 = r6
            r17 = r7
            r18 = r14
            r19 = r5
            r20 = r2
            r15.invoke_setBrightenParameters(r16, r17, r18, r19, r20)
            r14 = r21
            r13.forEach_dro_brighten(r0, r14)
            if (r1 == 0) goto L_0x00b1
            r0.destroy()
            r1 = 0
        L_0x00b1:
            r15 = r1
            r13 = r14
            goto L_0x00b6
        L_0x00b4:
            r13 = r0
            r15 = r1
        L_0x00b6:
            r0 = r22
            r1 = r13
            r2 = r14
            r5 = r26
            r6 = r27
            r7 = r28
            r0.adjustHistogram(r1, r2, r3, r4, r5, r6, r7, r8)
            if (r24 == 0) goto L_0x00d0
            r0 = 0
            java.lang.Object r0 = r11.get(r0)
            android.graphics.Bitmap r0 = (android.graphics.Bitmap) r0
            r13.copyTo(r0)
            goto L_0x00d3
        L_0x00d0:
            r14.copyTo(r12)
        L_0x00d3:
            if (r15 == 0) goto L_0x00d8
            r13.destroy()
        L_0x00d8:
            r14.destroy()
            r22.freeScripts()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.HDRProcessor.processSingleImage(java.util.List, boolean, android.graphics.Bitmap, float, int, boolean, net.sourceforge.opencamera.HDRProcessor$DROTonemappingAlgorithm):void");
    }

    /* access modifiers changed from: 0000 */
    public void brightenImage(Bitmap bitmap, int i, int i2, int i3) {
        BrightenFactors computeBrightenFactors = computeBrightenFactors(false, 0, 0, i, i2, i3, false);
        float f = computeBrightenFactors.gain;
        float f2 = computeBrightenFactors.gamma;
        float f3 = computeBrightenFactors.low_x;
        float f4 = computeBrightenFactors.mid_x;
        double d = (double) f;
        Double.isNaN(d);
        if (Math.abs(d - 1.0d) <= 1.0E-5d && i2 == 255) {
            double d2 = (double) f2;
            Double.isNaN(d2);
            if (Math.abs(d2 - 1.0d) <= 1.0E-5d) {
                return;
            }
        }
        initRenderscript();
        Allocation createFromBitmap = Allocation.createFromBitmap(this.f11rs, bitmap);
        ScriptC_avg_brighten scriptC_avg_brighten = new ScriptC_avg_brighten(this.f11rs);
        scriptC_avg_brighten.invoke_setBrightenParameters(f, f2, f3, f4, (float) i2);
        scriptC_avg_brighten.forEach_dro_brighten(createFromBitmap, createFromBitmap);
        createFromBitmap.copyTo(bitmap);
        createFromBitmap.destroy();
        freeScripts();
    }

    private void initRenderscript() {
        if (this.f11rs == null) {
            this.f11rs = RenderScript.create(this.context);
        }
    }

    public int getAvgSampleSize(int i) {
        this.cached_avg_sample_size = i >= 1100 ? 2 : 1;
        return this.cached_avg_sample_size;
    }

    public int getAvgSampleSize() {
        return this.cached_avg_sample_size;
    }

    public AvgData processAvg(Bitmap bitmap, Bitmap bitmap2, float f, int i, float f2) throws HDRProcessorException {
        if (bitmap.getWidth() == bitmap2.getWidth() && bitmap.getHeight() == bitmap2.getHeight()) {
            long currentTimeMillis = System.currentTimeMillis();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            initRenderscript();
            return processAvgCore(null, null, bitmap, bitmap2, width, height, f, i, f2, null, null, currentTimeMillis);
        }
        throw new HDRProcessorException(1);
    }

    public void updateAvg(AvgData avgData, int i, int i2, Bitmap bitmap, float f, int i3, float f2) throws HDRProcessorException {
        AvgData avgData2 = avgData;
        if (i == bitmap.getWidth() && i2 == bitmap.getHeight()) {
            Bitmap bitmap2 = bitmap;
            int i4 = i;
            int i5 = i2;
            float f3 = f;
            int i6 = i3;
            float f4 = f2;
            processAvgCore(avgData2.allocation_out, avgData2.allocation_out, null, bitmap2, i4, i5, f3, i6, f4, avgData2.allocation_avg_align, avgData2.bitmap_avg_align, System.currentTimeMillis());
            return;
        }
        throw new HDRProcessorException(1);
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x01bd  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x01c3  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x01cd  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x01d2  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x01d7  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private net.sourceforge.opencamera.HDRProcessor.AvgData processAvgCore(android.renderscript.Allocation r35, android.renderscript.Allocation r36, android.graphics.Bitmap r37, android.graphics.Bitmap r38, int r39, int r40, float r41, int r42, float r43, android.renderscript.Allocation r44, android.graphics.Bitmap r45, long r46) throws net.sourceforge.opencamera.HDRProcessorException {
        /*
            r34 = this;
            r15 = r34
            r14 = r37
            r13 = r38
            r12 = r39
            r11 = r40
            r10 = r41
            r9 = r42
            r7 = 2
            int[] r0 = new int[r7]
            r15.offsets_x = r0
            int[] r0 = new int[r7]
            r15.offsets_y = r0
            r19 = 0
            r8 = 1
            if (r14 != 0) goto L_0x001f
            r20 = 1
            goto L_0x0021
        L_0x001f:
            r20 = 0
        L_0x0021:
            java.util.ArrayList r6 = new java.util.ArrayList
            r6.<init>()
            android.renderscript.Allocation[] r5 = new android.renderscript.Allocation[r7]
            r0 = 1081711002(0x4079999a, float:3.9)
            int r0 = (r43 > r0 ? 1 : (r43 == r0 ? 0 : -1))
            if (r0 <= 0) goto L_0x0031
            r4 = 1
            goto L_0x003c
        L_0x0031:
            r0 = 4
            int r1 = r15.getAvgSampleSize(r9)
            int r0 = r0 / r1
            int r0 = java.lang.Math.max(r0, r8)
            r4 = r0
        L_0x003c:
            android.graphics.Matrix r3 = new android.graphics.Matrix
            r3.<init>()
            float r0 = (float) r4
            r21 = 1065353216(0x3f800000, float:1.0)
            float r0 = r21 / r0
            r3.postScale(r0, r0)
            int r16 = r12 / r4
            int r17 = r11 / r4
            int r18 = r12 / 2
            int r22 = r11 / 2
            int r0 = r12 - r18
            int r23 = r0 / 2
            int r0 = r11 - r22
            int r24 = r0 / 2
            r25 = 0
            if (r44 != 0) goto L_0x0081
            r26 = 0
            r0 = r37
            r1 = r23
            r2 = r24
            r27 = r3
            r3 = r18
            r28 = r4
            r4 = r22
            r29 = r5
            r5 = r27
            r7 = r6
            r6 = r26
            android.graphics.Bitmap r0 = android.graphics.Bitmap.createBitmap(r0, r1, r2, r3, r4, r5, r6)
            android.renderscript.RenderScript r1 = r15.f11rs
            android.renderscript.Allocation r1 = android.renderscript.Allocation.createFromBitmap(r1, r0)
            r6 = r0
            r5 = r1
            goto L_0x008c
        L_0x0081:
            r27 = r3
            r28 = r4
            r29 = r5
            r7 = r6
            r5 = r44
            r6 = r45
        L_0x008c:
            r26 = 0
            r0 = r38
            r1 = r23
            r2 = r24
            r3 = r18
            r4 = r22
            r13 = r5
            r5 = r27
            r12 = r6
            r6 = r26
            android.graphics.Bitmap r6 = android.graphics.Bitmap.createBitmap(r0, r1, r2, r3, r4, r5, r6)
            android.renderscript.RenderScript r0 = r15.f11rs
            android.renderscript.Allocation r22 = android.renderscript.Allocation.createFromBitmap(r0, r6)
            int r4 = r6.getWidth()
            int r5 = r6.getHeight()
            r7.add(r12)
            r7.add(r6)
            r29[r19] = r13
            r29[r8] = r22
            r18 = 0
            r3 = 1100(0x44c, float:1.541E-42)
            if (r9 < r3) goto L_0x00c2
            r0 = 1
            goto L_0x00c3
        L_0x00c2:
            r0 = 0
        L_0x00c3:
            int[] r1 = r15.offsets_x
            int[] r2 = r15.offsets_y
            r23 = 0
            r24 = 1
            r26 = 0
            r27 = 0
            r31 = 1
            if (r0 == 0) goto L_0x00d6
            r30 = 2
            goto L_0x00d8
        L_0x00d6:
            r30 = 1
        L_0x00d8:
            r0 = r34
            r3 = r29
            r29 = r6
            r6 = r7
            r7 = r23
            r23 = 1
            r8 = r24
            r9 = r26
            r10 = r27
            r11 = r18
            r32 = r12
            r12 = r31
            r33 = r13
            r13 = r25
            r14 = r30
            r15 = r16
            r16 = r17
            r17 = r46
            r0.autoAlignment(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17)
            r1 = 0
        L_0x00ff:
            int[] r2 = r0.offsets_x
            int r3 = r2.length
            if (r1 >= r3) goto L_0x010d
            r3 = r2[r1]
            int r3 = r3 * r28
            r2[r1] = r3
            int r1 = r1 + 1
            goto L_0x00ff
        L_0x010d:
            r1 = 0
        L_0x010e:
            int[] r2 = r0.offsets_y
            int r3 = r2.length
            if (r1 >= r3) goto L_0x011c
            r3 = r2[r1]
            int r3 = r3 * r28
            r2[r1] = r3
            int r1 = r1 + 1
            goto L_0x010e
        L_0x011c:
            if (r29 == 0) goto L_0x0121
            r29.recycle()
        L_0x0121:
            if (r22 == 0) goto L_0x0126
            r22.destroy()
        L_0x0126:
            if (r35 != 0) goto L_0x013b
            android.renderscript.RenderScript r1 = r0.f11rs
            android.renderscript.Element r2 = android.renderscript.Element.F32_3(r1)
            r3 = r39
            r4 = r40
            android.renderscript.Type r2 = android.renderscript.Type.createXY(r1, r2, r3, r4)
            android.renderscript.Allocation r1 = android.renderscript.Allocation.createTyped(r1, r2)
            goto L_0x013d
        L_0x013b:
            r1 = r35
        L_0x013d:
            if (r36 != 0) goto L_0x014a
            android.renderscript.RenderScript r2 = r0.f11rs
            r3 = r37
            android.renderscript.Allocation r2 = android.renderscript.Allocation.createFromBitmap(r2, r3)
            r19 = 1
            goto L_0x014e
        L_0x014a:
            r3 = r37
            r2 = r36
        L_0x014e:
            net.sourceforge.opencamera.ScriptC_process_avg r4 = r0.processAvgScript
            if (r4 != 0) goto L_0x015b
            net.sourceforge.opencamera.ScriptC_process_avg r4 = new net.sourceforge.opencamera.ScriptC_process_avg
            android.renderscript.RenderScript r5 = r0.f11rs
            r4.<init>(r5)
            r0.processAvgScript = r4
        L_0x015b:
            android.renderscript.RenderScript r4 = r0.f11rs
            r5 = r38
            android.renderscript.Allocation r4 = android.renderscript.Allocation.createFromBitmap(r4, r5)
            net.sourceforge.opencamera.ScriptC_process_avg r6 = r0.processAvgScript
            r6.set_bitmap_new(r4)
            net.sourceforge.opencamera.ScriptC_process_avg r6 = r0.processAvgScript
            int[] r7 = r0.offsets_x
            r7 = r7[r23]
            r6.set_offset_x_new(r7)
            net.sourceforge.opencamera.ScriptC_process_avg r6 = r0.processAvgScript
            int[] r7 = r0.offsets_y
            r7 = r7[r23]
            r6.set_offset_y_new(r7)
            net.sourceforge.opencamera.ScriptC_process_avg r6 = r0.processAvgScript
            r7 = r41
            r6.set_avg_factor(r7)
            r6 = 400(0x190, float:5.6E-43)
            r8 = r42
            int r6 = java.lang.Math.min(r8, r6)
            float r6 = (float) r6
            r9 = 700(0x2bc, float:9.81E-43)
            if (r8 < r9) goto L_0x0197
            r6 = 1145569280(0x44480000, float:800.0)
            r9 = 1100(0x44c, float:1.541E-42)
            if (r8 < r9) goto L_0x0197
            r8 = 1090519040(0x41000000, float:8.0)
            goto L_0x0199
        L_0x0197:
            r8 = 1065353216(0x3f800000, float:1.0)
        L_0x0199:
            r9 = 1120403456(0x42c80000, float:100.0)
            float r6 = java.lang.Math.max(r6, r9)
            r9 = 1092616192(0x41200000, float:10.0)
            float r6 = r6 * r9
            r9 = 4602678819172646912(0x3fe0000000000000, double:0.5)
            double r11 = (double) r7
            double r9 = java.lang.Math.pow(r9, r11)
            float r7 = (float) r9
            float r21 = r21 - r7
            float r6 = r6 / r21
            float r8 = r8 * r6
            net.sourceforge.opencamera.ScriptC_process_avg r7 = r0.processAvgScript
            r7.set_wiener_C(r6)
            net.sourceforge.opencamera.ScriptC_process_avg r6 = r0.processAvgScript
            r6.set_wiener_C_cutoff(r8)
            if (r20 == 0) goto L_0x01c3
            net.sourceforge.opencamera.ScriptC_process_avg r6 = r0.processAvgScript
            r6.forEach_avg_f(r2, r1)
            goto L_0x01c8
        L_0x01c3:
            net.sourceforge.opencamera.ScriptC_process_avg r6 = r0.processAvgScript
            r6.forEach_avg(r2, r1)
        L_0x01c8:
            r4.destroy()
            if (r19 == 0) goto L_0x01d0
            r2.destroy()
        L_0x01d0:
            if (r3 == 0) goto L_0x01d5
            r37.recycle()
        L_0x01d5:
            if (r5 == 0) goto L_0x01da
            r38.recycle()
        L_0x01da:
            net.sourceforge.opencamera.HDRProcessor$AvgData r2 = new net.sourceforge.opencamera.HDRProcessor$AvgData
            r3 = r32
            r4 = r33
            r2.<init>(r1, r3, r4)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.HDRProcessor.processAvgCore(android.renderscript.Allocation, android.renderscript.Allocation, android.graphics.Bitmap, android.graphics.Bitmap, int, int, float, int, float, android.renderscript.Allocation, android.graphics.Bitmap, long):net.sourceforge.opencamera.HDRProcessor$AvgData");
    }

    public void processAvgMulti(List<Bitmap> list, float f, int i, boolean z) throws HDRProcessorException {
        List<Bitmap> list2 = list;
        int size = list.size();
        if (size == 8) {
            int i2 = 1;
            while (i2 < size) {
                if (((Bitmap) list2.get(i2)).getWidth() == ((Bitmap) list2.get(0)).getWidth() && ((Bitmap) list2.get(i2)).getHeight() == ((Bitmap) list2.get(0)).getHeight()) {
                    i2++;
                } else {
                    throw new HDRProcessorException(1);
                }
            }
            long currentTimeMillis = System.currentTimeMillis();
            int width = ((Bitmap) list2.get(0)).getWidth();
            int height = ((Bitmap) list2.get(0)).getHeight();
            initRenderscript();
            Allocation createFromBitmap = Allocation.createFromBitmap(this.f11rs, (Bitmap) list2.get(0));
            Allocation createFromBitmap2 = Allocation.createFromBitmap(this.f11rs, (Bitmap) list2.get(1));
            Allocation createFromBitmap3 = Allocation.createFromBitmap(this.f11rs, (Bitmap) list2.get(2));
            Allocation createFromBitmap4 = Allocation.createFromBitmap(this.f11rs, (Bitmap) list2.get(3));
            Allocation createFromBitmap5 = Allocation.createFromBitmap(this.f11rs, (Bitmap) list2.get(4));
            Allocation createFromBitmap6 = Allocation.createFromBitmap(this.f11rs, (Bitmap) list2.get(5));
            Allocation createFromBitmap7 = Allocation.createFromBitmap(this.f11rs, (Bitmap) list2.get(6));
            Allocation createFromBitmap8 = Allocation.createFromBitmap(this.f11rs, (Bitmap) list2.get(7));
            ScriptC_process_avg scriptC_process_avg = new ScriptC_process_avg(this.f11rs);
            scriptC_process_avg.set_bitmap1(createFromBitmap2);
            scriptC_process_avg.set_bitmap2(createFromBitmap3);
            scriptC_process_avg.set_bitmap3(createFromBitmap4);
            scriptC_process_avg.set_bitmap4(createFromBitmap5);
            scriptC_process_avg.set_bitmap5(createFromBitmap6);
            scriptC_process_avg.set_bitmap6(createFromBitmap7);
            scriptC_process_avg.set_bitmap7(createFromBitmap8);
            scriptC_process_avg.forEach_avg_multi(createFromBitmap, createFromBitmap);
            for (int i3 = 1; i3 < list.size(); i3++) {
                ((Bitmap) list2.get(i3)).recycle();
            }
            if (f != 0.0f) {
                adjustHistogram(createFromBitmap, createFromBitmap, width, height, f, i, z, currentTimeMillis);
            }
            createFromBitmap.copyTo((Bitmap) list2.get(0));
            return;
        }
        throw new HDRProcessorException(0);
    }

    /* access modifiers changed from: 0000 */
    public void autoAlignment(int[] iArr, int[] iArr2, int i, int i2, List<Bitmap> list, int i3, boolean z, int i4) {
        initRenderscript();
        Allocation[] allocationArr = new Allocation[list.size()];
        for (int i5 = 0; i5 < list.size(); i5++) {
            allocationArr[i5] = Allocation.createFromBitmap(this.f11rs, (Bitmap) list.get(i5));
        }
        autoAlignment(iArr, iArr2, allocationArr, i, i2, list, i3, true, null, z, false, 1, false, i4, i, i2, 0);
        for (int i6 = 0; i6 < allocationArr.length; i6++) {
            if (allocationArr[i6] != null) {
                allocationArr[i6].destroy();
                allocationArr[i6] = null;
            }
        }
        freeScripts();
    }

    private BrightnessDetails autoAlignment(int[] iArr, int[] iArr2, Allocation[] allocationArr, int i, int i2, List<Bitmap> list, int i3, boolean z, SortCallback sortCallback, boolean z2, boolean z3, int i4, boolean z4, int i5, int i6, int i7, long j) {
        int i8;
        int i9;
        int i10;
        int i11;
        int i12;
        LuminanceInfo[] luminanceInfoArr;
        int i13;
        int i14;
        int i15;
        int i16;
        int[] iArr3 = iArr;
        Allocation[] allocationArr2 = allocationArr;
        List<Bitmap> list2 = list;
        int i17 = i3;
        SortCallback sortCallback2 = sortCallback;
        for (int i18 = 0; i18 < iArr3.length; i18++) {
            iArr3[i18] = 0;
            iArr2[i18] = 0;
        }
        Allocation[] allocationArr3 = new Allocation[allocationArr2.length];
        if (z4) {
            int i19 = i / 2;
            int i20 = i2 / 2;
            i11 = i19;
            i9 = i20;
            i8 = i19 / 2;
            i10 = i20 / 2;
        } else {
            i11 = i;
            i9 = i2;
            i10 = 0;
            i8 = 0;
        }
        if (this.createMTBScript == null) {
            this.createMTBScript = new ScriptC_create_mtb(this.f11rs);
        }
        if (z2) {
            LuminanceInfo[] luminanceInfoArr2 = new LuminanceInfo[allocationArr2.length];
            int i21 = 0;
            while (i21 < allocationArr2.length) {
                int i22 = i21;
                LuminanceInfo[] luminanceInfoArr3 = luminanceInfoArr2;
                int i23 = i11;
                int i24 = i10;
                luminanceInfoArr3[i22] = computeMedianLuminance((Bitmap) list2.get(i21), i8, i10, i11, i9);
                i21 = i22 + 1;
                i10 = i24;
                luminanceInfoArr2 = luminanceInfoArr3;
            }
            luminanceInfoArr = luminanceInfoArr2;
            i12 = i11;
            i13 = i10;
        } else {
            i12 = i11;
            i13 = i10;
            luminanceInfoArr = null;
        }
        if (z || !z2) {
            i14 = i13;
        } else {
            ArrayList arrayList = new ArrayList(list.size());
            int i25 = 0;
            while (i25 < list.size()) {
                AnonymousClass1BitmapInfo r7 = r0;
                int i26 = i25;
                int i27 = i13;
                ArrayList arrayList2 = arrayList;
                AnonymousClass1BitmapInfo r0 = new Object(luminanceInfoArr[i25], (Bitmap) list2.get(i25), allocationArr2[i25], i26) {
                    final Allocation allocation;
                    final Bitmap bitmap;
                    final int index;
                    final LuminanceInfo luminanceInfo;

                    {
                        this.luminanceInfo = r2;
                        this.bitmap = r3;
                        this.allocation = r4;
                        this.index = r5;
                    }
                };
                arrayList2.add(r7);
                i25 = i26 + 1;
                int[] iArr4 = iArr;
                arrayList = arrayList2;
                i13 = i27;
            }
            i14 = i13;
            ArrayList arrayList3 = arrayList;
            Collections.sort(arrayList3, new Comparator<AnonymousClass1BitmapInfo>() {
                public int compare(AnonymousClass1BitmapInfo r1, AnonymousClass1BitmapInfo r2) {
                    return r1.luminanceInfo.median_value - r2.luminanceInfo.median_value;
                }
            });
            list.clear();
            for (int i28 = 0; i28 < arrayList3.size(); i28++) {
                list2.add(((AnonymousClass1BitmapInfo) arrayList3.get(i28)).bitmap);
                luminanceInfoArr[i28] = ((AnonymousClass1BitmapInfo) arrayList3.get(i28)).luminanceInfo;
                allocationArr2[i28] = ((AnonymousClass1BitmapInfo) arrayList3.get(i28)).allocation;
            }
            if (sortCallback2 != null) {
                ArrayList arrayList4 = new ArrayList();
                for (int i29 = 0; i29 < arrayList3.size(); i29++) {
                    arrayList4.add(Integer.valueOf(((AnonymousClass1BitmapInfo) arrayList3.get(i29)).index));
                }
                sortCallback2.sortOrder(arrayList4);
            }
        }
        int i30 = z2 ? luminanceInfoArr[i17].median_value : -1;
        int i31 = 0;
        while (i31 < allocationArr2.length) {
            int i32 = z2 ? luminanceInfoArr[i31].median_value : -1;
            if (!z2 || !luminanceInfoArr[i31].noisy) {
                RenderScript renderScript = this.f11rs;
                i16 = i12;
                allocationArr3[i31] = Allocation.createTyped(renderScript, Type.createXY(renderScript, Element.U8(renderScript), i16, i9));
                if (z2) {
                    this.createMTBScript.set_median_value(i32);
                }
                this.createMTBScript.set_start_x(i8);
                i15 = i14;
                this.createMTBScript.set_start_y(i15);
                this.createMTBScript.set_out_bitmap(allocationArr3[i31]);
                LaunchOptions launchOptions = new LaunchOptions();
                launchOptions.setX(i8, i8 + i16);
                launchOptions.setY(i15, i15 + i9);
                if (z2) {
                    this.createMTBScript.forEach_create_mtb(allocationArr2[i31], launchOptions);
                } else if (!z3 || i31 != 0) {
                    this.createMTBScript.forEach_create_greyscale(allocationArr2[i31], launchOptions);
                } else {
                    this.createMTBScript.forEach_create_greyscale_f(allocationArr2[i31], launchOptions);
                }
            } else {
                allocationArr3[i31] = null;
                i16 = i12;
                i15 = i14;
            }
            i31++;
            i12 = i16;
            i14 = i15;
        }
        int i33 = i12;
        int i34 = 1;
        while (i34 < (Math.max(i6, i7) * i5) / 150) {
            i34 *= 2;
        }
        if (allocationArr3[i17] == null) {
            for (int i35 = 0; i35 < allocationArr3.length; i35++) {
                if (allocationArr3[i35] != null) {
                    allocationArr3[i35].destroy();
                    allocationArr3[i35] = null;
                }
            }
            return new BrightnessDetails(i30);
        }
        if (this.alignMTBScript == null) {
            this.alignMTBScript = new ScriptC_align_mtb(this.f11rs);
        }
        this.alignMTBScript.set_bitmap0(allocationArr3[i17]);
        int i36 = 0;
        while (i36 < allocationArr2.length) {
            if (i36 == i17 || allocationArr3[i36] == null) {
                int i37 = i4;
            } else {
                this.alignMTBScript.set_bitmap1(allocationArr3[i36]);
                int i38 = i4;
                int i39 = i34;
                while (i39 > i38) {
                    i39 /= 2;
                    int i40 = i39 * 1;
                    if (i40 > i33 || i40 > i9) {
                        i40 = i39;
                    }
                    this.alignMTBScript.set_off_x(iArr[i36]);
                    this.alignMTBScript.set_off_y(iArr2[i36]);
                    this.alignMTBScript.set_step_size(i40);
                    RenderScript renderScript2 = this.f11rs;
                    Allocation createSized = Allocation.createSized(renderScript2, Element.I32(renderScript2), 9);
                    this.alignMTBScript.bind_errors(createSized);
                    this.alignMTBScript.invoke_init_errors();
                    LaunchOptions launchOptions2 = new LaunchOptions();
                    int i41 = i33 / i40;
                    int i42 = i9 / i40;
                    launchOptions2.setX(0, i41);
                    launchOptions2.setY(0, i42);
                    System.currentTimeMillis();
                    if (z2) {
                        this.alignMTBScript.forEach_align_mtb(allocationArr3[i17], launchOptions2);
                    } else {
                        this.alignMTBScript.forEach_align(allocationArr3[i17], launchOptions2);
                    }
                    int[] iArr5 = new int[9];
                    createSized.copyTo(iArr5);
                    createSized.destroy();
                    int i43 = 0;
                    int i44 = -1;
                    int i45 = -1;
                    for (int i46 = 9; i43 < i46; i46 = 9) {
                        int i47 = iArr5[i43];
                        int i48 = i34;
                        if (i45 == -1 || i47 < i44) {
                            i44 = i47;
                            i45 = i43;
                        }
                        i43++;
                        i34 = i48;
                    }
                    int i49 = i34;
                    if (i44 >= 2000000000) {
                        Log.e(TAG, "    auto-alignment failed due to overflow");
                        i45 = 4;
                        if (this.is_test) {
                            throw new RuntimeException();
                        }
                    }
                    if (i45 != -1) {
                        int i50 = (i45 / 3) - 1;
                        iArr[i36] = iArr[i36] + (((i45 % 3) - 1) * i39);
                        iArr2[i36] = iArr2[i36] + (i50 * i39);
                    }
                    i34 = i49;
                }
            }
            i36++;
            i34 = i34;
        }
        for (int i51 = 0; i51 < allocationArr3.length; i51++) {
            if (allocationArr3[i51] != null) {
                allocationArr3[i51].destroy();
                allocationArr3[i51] = null;
            }
        }
        return new BrightnessDetails(i30);
    }

    private LuminanceInfo computeMedianLuminance(Bitmap bitmap, int i, int i2, int i3, int i4) {
        int sqrt = (int) Math.sqrt(100.0d);
        int i5 = 100 / sqrt;
        int[] iArr = new int[256];
        for (int i6 = 0; i6 < 256; i6++) {
            iArr[i6] = 0;
        }
        int i7 = 0;
        int i8 = 0;
        while (i7 < i5) {
            double d = (double) i7;
            double d2 = 1.0d;
            Double.isNaN(d);
            double d3 = d + 1.0d;
            double d4 = (double) i5;
            Double.isNaN(d4);
            double d5 = d3 / (d4 + 1.0d);
            double d6 = (double) i4;
            Double.isNaN(d6);
            int i9 = i2 + ((int) (d5 * d6));
            int i10 = i8;
            int i11 = 0;
            while (i11 < sqrt) {
                double d7 = (double) i11;
                Double.isNaN(d7);
                double d8 = d7 + d2;
                int i12 = i7;
                double d9 = (double) sqrt;
                Double.isNaN(d9);
                double d10 = d8 / (d9 + d2);
                double d11 = (double) i3;
                Double.isNaN(d11);
                int pixel = bitmap.getPixel(i + ((int) (d10 * d11)), i9);
                int max = Math.max(Math.max((16711680 & pixel) >> 16, (65280 & pixel) >> 8), pixel & 255);
                iArr[max] = iArr[max] + 1;
                i10++;
                i11++;
                i7 = i12;
                d2 = 1.0d;
            }
            Bitmap bitmap2 = bitmap;
            int i13 = i3;
            i7++;
            i8 = i10;
        }
        int i14 = i8 / 2;
        int i15 = 0;
        for (int i16 = 0; i16 < 256; i16++) {
            i15 += iArr[i16];
            if (i15 >= i14) {
                int i17 = 0;
                for (int i18 = 0; i18 <= i16 - 4; i18++) {
                    i17 += iArr[i18];
                }
                int i19 = 0;
                while (i19 <= i16 + 4 && i19 < 256) {
                    int i20 = iArr[i19];
                    i19++;
                }
                double d12 = (double) i17;
                double d13 = (double) i8;
                Double.isNaN(d12);
                Double.isNaN(d13);
                return new LuminanceInfo(i16, d12 / d13 < 0.2d);
            }
        }
        Log.e(TAG, "computeMedianLuminance failed");
        return new LuminanceInfo(127, true);
    }

    /* access modifiers changed from: 0000 */
    public void adjustHistogram(Allocation allocation, Allocation allocation2, int i, int i2, float f, int i3, boolean z, long j) {
        ScriptC_histogram_compute scriptC_histogram_compute;
        int i4;
        Allocation allocation3 = allocation;
        int i5 = i;
        int i6 = i2;
        int i7 = i3;
        RenderScript renderScript = this.f11rs;
        Allocation createSized = Allocation.createSized(renderScript, Element.I32(renderScript), 256);
        ScriptC_histogram_compute scriptC_histogram_compute2 = new ScriptC_histogram_compute(this.f11rs);
        scriptC_histogram_compute2.bind_histogram(createSized);
        int i8 = i7 * i7 * 256;
        int[] iArr = new int[i8];
        int[] iArr2 = new int[256];
        int i9 = 0;
        while (i9 < i7) {
            double d = (double) i9;
            int i10 = i9;
            double d2 = (double) i7;
            Double.isNaN(d);
            Double.isNaN(d2);
            double d3 = d / d2;
            Double.isNaN(d);
            double d4 = d + 1.0d;
            Double.isNaN(d2);
            double d5 = d4 / d2;
            int i11 = i8;
            double d6 = (double) i5;
            Double.isNaN(d6);
            int[] iArr3 = iArr;
            int[] iArr4 = iArr2;
            int i12 = (int) (d3 * d6);
            Double.isNaN(d6);
            int i13 = (int) (d5 * d6);
            if (i13 != i12) {
                int i14 = 0;
                while (i14 < i7) {
                    double d7 = (double) i14;
                    Double.isNaN(d7);
                    Double.isNaN(d2);
                    double d8 = d7 / d2;
                    Double.isNaN(d7);
                    double d9 = d7 + 1.0d;
                    Double.isNaN(d2);
                    double d10 = d9 / d2;
                    double d11 = d2;
                    double d12 = (double) i6;
                    Double.isNaN(d12);
                    int i15 = (int) (d8 * d12);
                    Double.isNaN(d12);
                    int i16 = (int) (d10 * d12);
                    if (i16 == i15) {
                        scriptC_histogram_compute = scriptC_histogram_compute2;
                    } else {
                        LaunchOptions launchOptions = new LaunchOptions();
                        launchOptions.setX(i12, i13);
                        launchOptions.setY(i15, i16);
                        scriptC_histogram_compute2.invoke_init_histogram();
                        scriptC_histogram_compute2.forEach_histogram_compute_by_value(allocation3, launchOptions);
                        int i17 = 256;
                        int[] iArr5 = new int[256];
                        createSized.copyTo(iArr5);
                        int i18 = (i13 - i12) * (i16 - i15);
                        int i19 = (i18 * 5) / 256;
                        int i20 = i19;
                        int i21 = 0;
                        while (true) {
                            if (i20 - i21 <= 1) {
                                break;
                            }
                            int i22 = (i20 + i21) / 2;
                            ScriptC_histogram_compute scriptC_histogram_compute3 = scriptC_histogram_compute2;
                            int i23 = 0;
                            int i24 = 0;
                            while (i24 < i17) {
                                if (iArr5[i24] > i22) {
                                    i23 += iArr5[i24] - i19;
                                }
                                i24++;
                                i17 = 256;
                            }
                            if (i23 > (i19 - i22) * 256) {
                                i20 = i22;
                            } else {
                                i21 = i22;
                            }
                            scriptC_histogram_compute2 = scriptC_histogram_compute3;
                            i17 = 256;
                        }
                        scriptC_histogram_compute = scriptC_histogram_compute2;
                        int i25 = (i20 + i21) / 2;
                        int i26 = 0;
                        int i27 = 0;
                        for (int i28 = 256; i26 < i28; i28 = 256) {
                            if (iArr5[i26] > i25) {
                                i27 += iArr5[i26] - i25;
                                iArr5[i26] = i25;
                            }
                            i26++;
                        }
                        int i29 = i27 / 256;
                        for (int i30 = 0; i30 < 256; i30++) {
                            iArr5[i30] = iArr5[i30] + i29;
                        }
                        if (z) {
                            iArr4[0] = iArr5[0];
                            int i31 = 1;
                            for (int i32 = 256; i31 < i32; i32 = 256) {
                                iArr4[i31] = iArr4[i31 - 1] + iArr5[i31];
                                i31++;
                            }
                            int i33 = i18 / 256;
                            int i34 = 0;
                            while (i34 < 128) {
                                int i35 = i34 + 1;
                                if (iArr4[i34] < i33 * i35) {
                                    int i36 = (int) ((1.0f - (((float) i34) / 128.0f)) * ((float) i33));
                                    if (iArr5[i34] < i36) {
                                        for (int i37 = i35; i37 < 256 && iArr5[i34] < i36; i37++) {
                                            if (iArr5[i37] > i33) {
                                                int min = Math.min(iArr5[i37] - i33, i36 - iArr5[i34]);
                                                iArr5[i34] = iArr5[i34] + min;
                                                iArr5[i37] = iArr5[i37] - min;
                                            }
                                        }
                                    }
                                }
                                i34 = i35;
                            }
                        }
                        int i38 = ((i10 * i7) + i14) * 256;
                        iArr3[i38] = iArr5[0];
                        for (i4 = 1; i4 < 256; i4++) {
                            int i39 = i38 + i4;
                            iArr3[i39] = iArr3[i39 - 1] + iArr5[i4];
                        }
                    }
                    i14++;
                    int i40 = i;
                    i6 = i2;
                    scriptC_histogram_compute2 = scriptC_histogram_compute;
                    d2 = d11;
                }
            }
            i9 = i10 + 1;
            i5 = i;
            i6 = i2;
            scriptC_histogram_compute2 = scriptC_histogram_compute2;
            i8 = i11;
            iArr = iArr3;
            iArr2 = iArr4;
        }
        int i41 = i8;
        int[] iArr6 = iArr;
        RenderScript renderScript2 = this.f11rs;
        Allocation createSized2 = Allocation.createSized(renderScript2, Element.I32(renderScript2), i8);
        createSized2.copyFrom(iArr6);
        ScriptC_histogram_adjust scriptC_histogram_adjust = new ScriptC_histogram_adjust(this.f11rs);
        scriptC_histogram_adjust.set_c_histogram(createSized2);
        scriptC_histogram_adjust.set_hdr_alpha(f);
        scriptC_histogram_adjust.set_n_tiles(i7);
        scriptC_histogram_adjust.set_width(i);
        scriptC_histogram_adjust.set_height(i2);
        scriptC_histogram_adjust.forEach_histogram_adjust(allocation3, allocation2);
        createSized.destroy();
        createSized2.destroy();
    }

    private Allocation computeHistogramAllocation(Allocation allocation, boolean z, boolean z2, long j) {
        RenderScript renderScript = this.f11rs;
        Allocation createSized = Allocation.createSized(renderScript, Element.I32(renderScript), 256);
        ScriptC_histogram_compute scriptC_histogram_compute = new ScriptC_histogram_compute(this.f11rs);
        scriptC_histogram_compute.bind_histogram(createSized);
        scriptC_histogram_compute.invoke_init_histogram();
        if (z) {
            if (z2) {
                scriptC_histogram_compute.forEach_histogram_compute_by_intensity_f(allocation);
            } else {
                scriptC_histogram_compute.forEach_histogram_compute_by_intensity(allocation);
            }
        } else if (z2) {
            scriptC_histogram_compute.forEach_histogram_compute_by_value_f(allocation);
        } else {
            scriptC_histogram_compute.forEach_histogram_compute_by_value(allocation);
        }
        return createSized;
    }

    public int[] computeHistogram(Bitmap bitmap, boolean z) {
        System.currentTimeMillis();
        initRenderscript();
        Allocation createFromBitmap = Allocation.createFromBitmap(this.f11rs, bitmap);
        int[] computeHistogram = computeHistogram(createFromBitmap, z, false);
        createFromBitmap.destroy();
        freeScripts();
        return computeHistogram;
    }

    private int[] computeHistogram(Allocation allocation, boolean z, boolean z2) {
        int[] iArr = new int[256];
        Allocation computeHistogramAllocation = computeHistogramAllocation(allocation, z, z2, System.currentTimeMillis());
        computeHistogramAllocation.copyTo(iArr);
        computeHistogramAllocation.destroy();
        return iArr;
    }

    /* access modifiers changed from: 0000 */
    public HistogramInfo getHistogramInfo(int[] iArr) {
        int i = 0;
        for (int i2 : iArr) {
            i += i2;
        }
        int i3 = i / 2;
        double d = 0.0d;
        int i4 = 0;
        int i5 = -1;
        int i6 = 0;
        for (int i7 = 0; i7 < iArr.length; i7++) {
            i4 += iArr[i7];
            double d2 = (double) (iArr[i7] * i7);
            Double.isNaN(d2);
            d += d2;
            if (i4 >= i3 && i5 == -1) {
                i5 = i7;
            }
            if (iArr[i7] > 0) {
                i6 = i7;
            }
        }
        double d3 = (double) i4;
        Double.isNaN(d3);
        return new HistogramInfo(i, (int) ((d / d3) + 0.1d), i5, i6);
    }

    private static int getBrightnessTarget(int i, float f, int i2) {
        if (i <= 0) {
            i = 1;
        }
        return Math.max(i, Math.min(i2, (int) (f * ((float) i))));
    }

    public static BrightenFactors computeBrightenFactors(boolean z, int i, long j, int i2, int i3) {
        return computeBrightenFactors(z, i, j, i2, i3, getBrightnessTarget(i2, 1.5f, (!z || i >= 1100 || j >= 16949152) ? 119 : 199), true);
    }

    private static BrightenFactors computeBrightenFactors(boolean z, int i, long j, int i2, int i3, int i4, boolean z2) {
        if (i2 <= 0) {
            i2 = 1;
        }
        float f = ((float) i4) / ((float) i2);
        float f2 = 1.0f;
        if (f < 1.0f && z2) {
            f = 1.0f;
        }
        float f3 = (float) i3;
        float f4 = f * f3;
        float f5 = 255.5f;
        if (f4 > 255.0f) {
            float f6 = (!z || i >= 1100 || j >= 16949152) ? 204.0f : 153.0f;
            f5 = f6 / f;
            f2 = (float) (Math.log((double) (f6 / 255.0f)) / Math.log((double) (f5 / f3)));
        } else if (z2 && f4 < 255.0f && i3 > 0) {
            float min = Math.min(255.0f / f3, 4.0f);
            if (min > f) {
                f = min;
            }
        }
        float f7 = 0.0f;
        if (z && i >= 400) {
            f7 = Math.min(8.0f, (127.5f / f) * 0.125f);
        }
        return new BrightenFactors(f, f7, f5, f2);
    }

    public Bitmap avgBrighten(Allocation allocation, int i, int i2, int i3, long j) {
        Allocation allocation2 = allocation;
        int i4 = i3;
        initRenderscript();
        long currentTimeMillis = System.currentTimeMillis();
        int i5 = 0;
        int[] computeHistogram = computeHistogram(allocation2, false, true);
        HistogramInfo histogramInfo = getHistogramInfo(computeHistogram);
        int i6 = histogramInfo.median_brightness;
        int i7 = histogramInfo.max_brightness;
        BrightenFactors computeBrightenFactors = computeBrightenFactors(true, i3, j, i6, i7);
        float f = computeBrightenFactors.gain;
        float f2 = computeBrightenFactors.low_x;
        float f3 = computeBrightenFactors.mid_x;
        float f4 = computeBrightenFactors.gamma;
        ScriptC_avg_brighten scriptC_avg_brighten = new ScriptC_avg_brighten(this.f11rs);
        scriptC_avg_brighten.set_bitmap(allocation2);
        int i8 = (int) (((float) histogramInfo.total) * 0.001f);
        long j2 = currentTimeMillis;
        int i9 = -1;
        int i10 = 0;
        while (i5 < computeHistogram.length) {
            int i11 = i10 + computeHistogram[i5];
            if (i11 >= i8 && i9 == -1) {
                i9 = i5;
            }
            i5++;
            i10 = i11;
        }
        scriptC_avg_brighten.invoke_setBlackLevel(Math.min(Math.max(0.0f, (float) i9), i4 <= 700 ? 18.0f : 4.0f));
        scriptC_avg_brighten.set_median_filter_strength(this.cached_avg_sample_size >= 2 ? 0.5f : 1.0f);
        scriptC_avg_brighten.invoke_setBrightenParameters(f, f4, f2, f3, (float) i7);
        Bitmap createBitmap = Bitmap.createBitmap(i, i2, Config.ARGB_8888);
        Allocation createFromBitmap = Allocation.createFromBitmap(this.f11rs, createBitmap);
        scriptC_avg_brighten.forEach_avg_brighten_f(allocation2, createFromBitmap);
        if (i4 < 1100 && j < 16949152) {
            float min = Math.min(Math.max(((float) (histogramInfo.median_brightness - 60)) / -25.0f, 0.0f), 1.0f);
            adjustHistogram(createFromBitmap, createFromBitmap, i, i2, ((1.0f - min) * 0.25f) + (min * 0.5f), 1, true, j2);
        }
        createFromBitmap.copyTo(createBitmap);
        createFromBitmap.destroy();
        freeScripts();
        return createBitmap;
    }

    private float computeSharpness(Allocation allocation, int i, long j) {
        RenderScript renderScript = this.f11rs;
        Allocation createSized = Allocation.createSized(renderScript, Element.I32(renderScript), i);
        ScriptC_calculate_sharpness scriptC_calculate_sharpness = new ScriptC_calculate_sharpness(this.f11rs);
        scriptC_calculate_sharpness.bind_sums(createSized);
        scriptC_calculate_sharpness.set_bitmap(allocation);
        scriptC_calculate_sharpness.set_width(i);
        scriptC_calculate_sharpness.invoke_init_sums();
        scriptC_calculate_sharpness.forEach_calculate_sharpness(allocation);
        int[] iArr = new int[i];
        createSized.copyTo(iArr);
        createSized.destroy();
        float f = 0.0f;
        for (int i2 = 0; i2 < i; i2++) {
            f += (float) iArr[i2];
        }
        return f;
    }
}
