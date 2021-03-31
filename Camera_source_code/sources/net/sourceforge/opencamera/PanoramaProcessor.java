package net.sourceforge.opencamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Element.DataType;
import android.renderscript.RSInvalidStateException;
import android.renderscript.RenderScript;
import android.renderscript.Script.LaunchOptions;
import android.renderscript.Type;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PanoramaProcessor {
    private static final String TAG = "PanoramaProcessor";
    private static final int blend_n_levels = 4;
    private final Context context;
    private ScriptC_feature_detector featureDetectorScript = null;
    private final HDRProcessor hdrProcessor;
    private ScriptC_pyramid_blending pyramidBlendingScript = null;

    /* renamed from: rs */
    private RenderScript f16rs;

    static class AutoAlignmentByFeatureResult {
        final int offset_x;
        final int offset_y;
        final float rotation;
        final float y_scale;

        AutoAlignmentByFeatureResult(int i, int i2, float f, float f2) {
            this.offset_x = i;
            this.offset_y = i2;
            this.rotation = f;
            this.y_scale = f2;
        }
    }

    private static class ComputeDistancesBetweenMatchesThread extends Thread {
        private final List<Bitmap> bitmaps;
        private final int feature_descriptor_radius;
        private final List<FeatureMatch> matches;
        private final int nd_indx;
        private final int[] pixels0;
        private final int[] pixels1;
        private final int st_indx;

        ComputeDistancesBetweenMatchesThread(List<FeatureMatch> list, int i, int i2, int i3, List<Bitmap> list2, int[] iArr, int[] iArr2) {
            this.matches = list;
            this.st_indx = i;
            this.nd_indx = i2;
            this.feature_descriptor_radius = i3;
            this.bitmaps = list2;
            this.pixels0 = iArr;
            this.pixels1 = iArr2;
        }

        public void run() {
            PanoramaProcessor.computeDistancesBetweenMatches(this.matches, this.st_indx, this.nd_indx, this.feature_descriptor_radius, this.bitmaps, this.pixels0, this.pixels1);
        }
    }

    private static class FeatureMatch implements Comparable<FeatureMatch> {
        /* access modifiers changed from: private */
        public float distance;
        /* access modifiers changed from: private */
        public final int index0;
        /* access modifiers changed from: private */
        public final int index1;

        private FeatureMatch(int i, int i2) {
            this.index0 = i;
            this.index1 = i2;
        }

        public int compareTo(FeatureMatch featureMatch) {
            return Float.compare(this.distance, featureMatch.distance);
        }

        public boolean equals(Object obj) {
            return (obj instanceof FeatureMatch) && compareTo((FeatureMatch) obj) == 0;
        }
    }

    private static int nextPowerOf2(int i) {
        int i2 = 1;
        while (i > i2) {
            i2 *= 2;
        }
        return i2;
    }

    public PanoramaProcessor(Context context2, HDRProcessor hDRProcessor) {
        this.context = context2;
        this.hdrProcessor = hDRProcessor;
    }

    private void freeScripts() {
        this.pyramidBlendingScript = null;
        this.featureDetectorScript = null;
    }

    public void onDestroy() {
        freeScripts();
        RenderScript renderScript = this.f16rs;
        if (renderScript != null) {
            try {
                renderScript.destroy();
            } catch (RSInvalidStateException e) {
                e.printStackTrace();
            }
            this.f16rs = null;
        }
    }

    private void initRenderscript() {
        if (this.f16rs == null) {
            this.f16rs = RenderScript.create(this.context);
        }
    }

    private Allocation reduceBitmap(ScriptC_pyramid_blending scriptC_pyramid_blending, Allocation allocation) {
        int x = allocation.getType().getX();
        int y = allocation.getType().getY();
        RenderScript renderScript = this.f16rs;
        Allocation createTyped = Allocation.createTyped(renderScript, Type.createXY(renderScript, Element.RGBA_8888(renderScript), x / 2, y / 2));
        scriptC_pyramid_blending.set_bitmap(allocation);
        scriptC_pyramid_blending.forEach_reduce(createTyped, createTyped);
        return createTyped;
    }

    private Allocation expandBitmap(ScriptC_pyramid_blending scriptC_pyramid_blending, Allocation allocation) {
        int x = allocation.getType().getX();
        int y = allocation.getType().getY();
        RenderScript renderScript = this.f16rs;
        int i = x * 2;
        int i2 = y * 2;
        Allocation createTyped = Allocation.createTyped(renderScript, Type.createXY(renderScript, Element.RGBA_8888(renderScript), i, i2));
        scriptC_pyramid_blending.set_bitmap(allocation);
        scriptC_pyramid_blending.forEach_expand(createTyped, createTyped);
        RenderScript renderScript2 = this.f16rs;
        Allocation createTyped2 = Allocation.createTyped(renderScript2, Type.createXY(renderScript2, Element.RGBA_8888(renderScript2), i, i2));
        scriptC_pyramid_blending.set_bitmap(createTyped);
        scriptC_pyramid_blending.forEach_blur1dX(createTyped, createTyped2);
        scriptC_pyramid_blending.set_bitmap(createTyped2);
        scriptC_pyramid_blending.forEach_blur1dY(createTyped2, createTyped);
        createTyped2.destroy();
        return createTyped;
    }

    private Allocation subtractBitmap(ScriptC_pyramid_blending scriptC_pyramid_blending, Allocation allocation, Allocation allocation2) {
        int x = allocation.getType().getX();
        int y = allocation.getType().getY();
        if (allocation2.getType().getX() == x && allocation2.getType().getY() == y) {
            RenderScript renderScript = this.f16rs;
            Allocation createTyped = Allocation.createTyped(renderScript, Type.createXY(renderScript, Element.F32_3(renderScript), x, y));
            scriptC_pyramid_blending.set_bitmap(allocation2);
            scriptC_pyramid_blending.forEach_subtract(allocation, createTyped);
            return createTyped;
        }
        Log.e(TAG, "allocations of different dimensions");
        throw new RuntimeException();
    }

    private void addBitmap(ScriptC_pyramid_blending scriptC_pyramid_blending, Allocation allocation, Allocation allocation2) {
        int x = allocation.getType().getX();
        int y = allocation.getType().getY();
        if (allocation2.getType().getX() == x && allocation2.getType().getY() == y) {
            scriptC_pyramid_blending.set_bitmap(allocation2);
            scriptC_pyramid_blending.forEach_add(allocation, allocation);
            return;
        }
        Log.e(TAG, "allocations of different dimensions");
        throw new RuntimeException();
    }

    private List<Allocation> createGaussianPyramid(ScriptC_pyramid_blending scriptC_pyramid_blending, Bitmap bitmap, int i) {
        ArrayList arrayList = new ArrayList();
        Allocation createFromBitmap = Allocation.createFromBitmap(this.f16rs, bitmap);
        arrayList.add(createFromBitmap);
        for (int i2 = 0; i2 < i; i2++) {
            createFromBitmap = reduceBitmap(scriptC_pyramid_blending, createFromBitmap);
            arrayList.add(createFromBitmap);
        }
        return arrayList;
    }

    private List<Allocation> createLaplacianPyramid(ScriptC_pyramid_blending scriptC_pyramid_blending, Bitmap bitmap, int i, String str) {
        List createGaussianPyramid = createGaussianPyramid(scriptC_pyramid_blending, bitmap, i);
        ArrayList arrayList = new ArrayList();
        int i2 = 0;
        while (i2 < createGaussianPyramid.size() - 1) {
            Allocation allocation = (Allocation) createGaussianPyramid.get(i2);
            int i3 = i2 + 1;
            Allocation expandBitmap = expandBitmap(scriptC_pyramid_blending, (Allocation) createGaussianPyramid.get(i3));
            arrayList.add(subtractBitmap(scriptC_pyramid_blending, allocation, expandBitmap));
            allocation.destroy();
            createGaussianPyramid.set(i2, null);
            expandBitmap.destroy();
            i2 = i3;
        }
        arrayList.add(createGaussianPyramid.get(createGaussianPyramid.size() - 1));
        return arrayList;
    }

    private Bitmap collapseLaplacianPyramid(ScriptC_pyramid_blending scriptC_pyramid_blending, List<Allocation> list) {
        boolean z = true;
        Allocation allocation = (Allocation) list.get(list.size() - 1);
        int size = list.size() - 2;
        while (size >= 0) {
            Allocation expandBitmap = expandBitmap(scriptC_pyramid_blending, allocation);
            if (!z) {
                allocation.destroy();
            }
            addBitmap(scriptC_pyramid_blending, expandBitmap, (Allocation) list.get(size));
            z = false;
            size--;
            allocation = expandBitmap;
        }
        Bitmap createBitmap = Bitmap.createBitmap(allocation.getType().getX(), allocation.getType().getY(), Config.ARGB_8888);
        allocation.copyTo(createBitmap);
        if (!z) {
            allocation.destroy();
        }
        return createBitmap;
    }

    private void mergePyramids(ScriptC_pyramid_blending scriptC_pyramid_blending, List<Allocation> list, List<Allocation> list2, int[] iArr, int i) {
        int i2;
        int[] iArr2;
        float f;
        ScriptC_pyramid_blending scriptC_pyramid_blending2 = scriptC_pyramid_blending;
        List<Allocation> list3 = list;
        if (iArr == null) {
            i2 = 3;
            iArr2 = new int[]{1};
        } else {
            iArr2 = iArr;
            i2 = i;
        }
        int i3 = 0;
        for (int i4 = 0; i4 < list.size(); i4++) {
            i3 = Math.max(i3, ((Allocation) list3.get(i4)).getType().getY());
        }
        RenderScript renderScript = this.f16rs;
        Allocation createSized = Allocation.createSized(renderScript, Element.I32(renderScript), i3);
        scriptC_pyramid_blending2.bind_interpolated_best_path(createSized);
        int[] iArr3 = new int[i3];
        int i5 = 0;
        while (i5 < list.size()) {
            Allocation allocation = (Allocation) list3.get(i5);
            Allocation allocation2 = (Allocation) list2.get(i5);
            int x = allocation.getType().getX();
            int y = allocation.getType().getY();
            int x2 = allocation2.getType().getX();
            String str = TAG;
            if (x2 != x || allocation2.getType().getY() != y) {
                Log.e(str, "allocations of different dimensions");
                throw new RuntimeException();
            } else if (allocation.getType().getElement().getDataType() == allocation2.getType().getElement().getDataType()) {
                scriptC_pyramid_blending2.set_bitmap(allocation2);
                int i6 = x / 2;
                if (i5 != list.size() - 1) {
                    int i7 = 2;
                    for (int i8 = 0; i8 < i5; i8++) {
                        i7 *= 2;
                    }
                    i6 = Math.min(i7, i6);
                }
                float length = ((float) iArr2.length) / ((float) y);
                int i9 = 0;
                while (i9 < y) {
                    float f2 = (((float) i9) + 0.5f) * length;
                    if (f2 <= 0.5f) {
                        f = (float) iArr2[0];
                    } else if (f2 >= ((float) (iArr2.length - 1)) + 0.5f) {
                        f = (float) iArr2[iArr2.length - 1];
                    } else {
                        float f3 = f2 - 0.5f;
                        int i10 = (int) f3;
                        float f4 = f3 - ((float) i10);
                        float f5 = f4 < 0.1f ? 0.0f : f4 > 0.9f ? 1.0f : (f4 - 0.1f) / 0.8f;
                        f = ((1.0f - f5) * ((float) iArr2[i10])) + (f5 * ((float) iArr2[i10 + 1]));
                    }
                    float f6 = f / (((float) i2) - 1.0f);
                    iArr3[i9] = (int) (((((1.0f - f6) * 0.25f) + (f6 * 0.75f)) * ((float) x)) + 0.5f);
                    int i11 = i6 / 2;
                    String str2 = "    width: ";
                    int[] iArr4 = iArr2;
                    String str3 = "    blend_width: ";
                    int i12 = i2;
                    String str4 = "]: ";
                    float f7 = length;
                    String str5 = "    interpolated_best_path[";
                    if (iArr3[i9] - i11 < 0) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(str5);
                        sb.append(i9);
                        sb.append(str4);
                        sb.append(iArr3[i9]);
                        Log.e(str, sb.toString());
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(str3);
                        sb2.append(i6);
                        Log.e(str, sb2.toString());
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append(str2);
                        sb3.append(x);
                        Log.e(str, sb3.toString());
                        throw new RuntimeException("blend window runs off left hand size");
                    } else if (iArr3[i9] + i11 <= x) {
                        i9++;
                        List<Allocation> list4 = list;
                        List<Allocation> list5 = list2;
                        length = f7;
                        iArr2 = iArr4;
                        i2 = i12;
                    } else {
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append(str5);
                        sb4.append(i9);
                        sb4.append(str4);
                        sb4.append(iArr3[i9]);
                        Log.e(str, sb4.toString());
                        StringBuilder sb5 = new StringBuilder();
                        sb5.append(str3);
                        sb5.append(i6);
                        Log.e(str, sb5.toString());
                        StringBuilder sb6 = new StringBuilder();
                        sb6.append(str2);
                        sb6.append(x);
                        Log.e(str, sb6.toString());
                        throw new RuntimeException("blend window runs off right hand size");
                    }
                }
                int[] iArr5 = iArr2;
                int i13 = i2;
                createSized.copyFrom(iArr3);
                scriptC_pyramid_blending2.invoke_setBlendWidth(i6, x);
                if (allocation.getType().getElement().getDataType() == DataType.FLOAT_32) {
                    scriptC_pyramid_blending2.forEach_merge_f(allocation, allocation);
                } else {
                    scriptC_pyramid_blending2.forEach_merge(allocation, allocation);
                }
                i5++;
                list3 = list;
                iArr2 = iArr5;
                i2 = i13;
            } else {
                Log.e(str, "allocations of different data types");
                throw new RuntimeException();
            }
        }
        createSized.destroy();
    }

    private void saveBitmap(Bitmap bitmap, String str) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
            sb.append("/");
            sb.append(str);
            File file = new File(sb.toString());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            if (str.toLowerCase().endsWith(".png")) {
                bitmap.compress(CompressFormat.PNG, 100, fileOutputStream);
            } else {
                bitmap.compress(CompressFormat.JPEG, 90, fileOutputStream);
            }
            fileOutputStream.close();
            ((MainActivity) this.context).getStorageUtils().broadcastFile(file, true, false, true);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private void saveAllocation(String str, Allocation allocation) {
        Bitmap bitmap;
        int x = allocation.getType().getX();
        int y = allocation.getType().getY();
        StringBuilder sb = new StringBuilder();
        sb.append("count: ");
        sb.append(allocation.getType().getCount());
        String sb2 = sb.toString();
        String str2 = TAG;
        Log.d(str2, sb2);
        StringBuilder sb3 = new StringBuilder();
        sb3.append("byte size: ");
        sb3.append(allocation.getType().getElement().getBytesSize());
        Log.d(str2, sb3.toString());
        if (allocation.getType().getElement().getDataType() == DataType.FLOAT_32) {
            int i = x * y;
            float[] fArr = new float[(i * 4)];
            allocation.copyTo(fArr);
            int[] iArr = new int[i];
            for (int i2 = 0; i2 < i; i2++) {
                int i3 = i2 * 4;
                iArr[i2] = Color.argb(255, Math.max(Math.min((int) ((((fArr[i3] / 510.0f) + 0.5f) * 255.0f) + 0.5f), 255), 0), Math.max(Math.min((int) ((((fArr[i3 + 1] / 510.0f) + 0.5f) * 255.0f) + 0.5f), 255), 0), Math.max(Math.min((int) ((((fArr[i3 + 2] / 510.0f) + 0.5f) * 255.0f) + 0.5f), 255), 0));
            }
            bitmap = Bitmap.createBitmap(iArr, x, y, Config.ARGB_8888);
        } else if (allocation.getType().getElement().getDataType() == DataType.UNSIGNED_8) {
            int i4 = x * y;
            byte[] bArr = new byte[i4];
            allocation.copyTo(bArr);
            int[] iArr2 = new int[i4];
            for (int i5 = 0; i5 < i4; i5++) {
                byte b = bArr[i5];
                int i6 = b < 0 ? b + 255 : b;
                iArr2[i5] = Color.argb(255, i6, i6, i6);
            }
            bitmap = Bitmap.createBitmap(iArr2, x, y, Config.ARGB_8888);
        } else {
            Bitmap createBitmap = Bitmap.createBitmap(x, y, Config.ARGB_8888);
            allocation.copyTo(createBitmap);
            bitmap = createBitmap;
        }
        saveBitmap(bitmap, str);
        bitmap.recycle();
    }

    private void savePyramid(String str, List<Allocation> list) {
        for (int i = 0; i < list.size(); i++) {
            Allocation allocation = (Allocation) list.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("_");
            sb.append(i);
            sb.append(".jpg");
            saveAllocation(sb.toString(), allocation);
        }
    }

    private static int getBlendDimension() {
        return (int) (Math.pow(2.0d, 4.0d) + 0.5d);
    }

    private Bitmap blendPyramids(Bitmap bitmap, Bitmap bitmap2) {
        Bitmap bitmap3 = bitmap;
        Bitmap bitmap4 = bitmap2;
        if (this.pyramidBlendingScript == null) {
            this.pyramidBlendingScript = new ScriptC_pyramid_blending(this.f16rs);
        }
        int width = bitmap.getWidth();
        int width2 = bitmap2.getWidth();
        String str = TAG;
        if (width == width2 && bitmap.getHeight() == bitmap2.getHeight()) {
            int blendDimension = getBlendDimension();
            String str2 = " not a multiple of ";
            if (bitmap.getWidth() % blendDimension != 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("bitmap width ");
                sb.append(bitmap.getWidth());
                sb.append(str2);
                sb.append(blendDimension);
                Log.e(str, sb.toString());
                throw new RuntimeException();
            } else if (bitmap.getHeight() % blendDimension == 0) {
                int[] iArr = new int[8];
                Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bitmap3, bitmap.getWidth() / 4, bitmap.getHeight() / 4, true);
                Bitmap createScaledBitmap2 = Bitmap.createScaledBitmap(bitmap4, bitmap2.getWidth() / 4, bitmap2.getHeight() / 4, true);
                Allocation createFromBitmap = Allocation.createFromBitmap(this.f16rs, createScaledBitmap);
                Allocation createFromBitmap2 = Allocation.createFromBitmap(this.f16rs, createScaledBitmap2);
                int[] iArr2 = new int[1];
                RenderScript renderScript = this.f16rs;
                Allocation createSized = Allocation.createSized(renderScript, Element.I32(renderScript), 1);
                this.pyramidBlendingScript.bind_errors(createSized);
                LaunchOptions launchOptions = new LaunchOptions();
                this.pyramidBlendingScript.set_bitmap(createFromBitmap2);
                int max = Math.max(2, createScaledBitmap.getWidth() / 8);
                int i = 0;
                int i2 = 0;
                for (int i3 = 8; i < i3; i3 = 8) {
                    iArr[i] = -1;
                    int i4 = i + 1;
                    int height = (createScaledBitmap.getHeight() * i4) / 8;
                    launchOptions.setY(i2, height);
                    int i5 = height;
                    int i6 = -1;
                    int i7 = 0;
                    while (i7 < 7) {
                        float f = ((float) i7) / 6.0f;
                        int width3 = (int) (((((1.0f - f) * 0.25f) + (f * 0.75f)) * ((float) createScaledBitmap.getWidth())) + 0.5f);
                        int i8 = max / 2;
                        int i9 = max;
                        launchOptions.setX(width3 - i8, width3 + i8);
                        this.pyramidBlendingScript.invoke_init_errors();
                        this.pyramidBlendingScript.forEach_compute_error(createFromBitmap, launchOptions);
                        createSized.copyTo(iArr2);
                        int i10 = iArr2[0];
                        int[] iArr3 = iArr2;
                        if (iArr[i] == -1 || i10 < i6) {
                            iArr[i] = i7;
                            i6 = i10;
                        }
                        i7++;
                        iArr2 = iArr3;
                        max = i9;
                    }
                    i = i4;
                    i2 = i5;
                }
                createFromBitmap.destroy();
                createFromBitmap2.destroy();
                createSized.destroy();
                if (createScaledBitmap != bitmap3) {
                    createScaledBitmap.recycle();
                }
                if (createScaledBitmap2 != bitmap4) {
                    createScaledBitmap2.recycle();
                }
                List<Allocation> createLaplacianPyramid = createLaplacianPyramid(this.pyramidBlendingScript, bitmap3, 4, "lhs");
                List<Allocation> createLaplacianPyramid2 = createLaplacianPyramid(this.pyramidBlendingScript, bitmap4, 4, "rhs");
                mergePyramids(this.pyramidBlendingScript, createLaplacianPyramid, createLaplacianPyramid2, iArr, 7);
                Bitmap collapseLaplacianPyramid = collapseLaplacianPyramid(this.pyramidBlendingScript, createLaplacianPyramid);
                for (Allocation destroy : createLaplacianPyramid) {
                    destroy.destroy();
                }
                for (Allocation destroy2 : createLaplacianPyramid2) {
                    destroy2.destroy();
                }
                return collapseLaplacianPyramid;
            } else {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("bitmap height ");
                sb2.append(bitmap.getHeight());
                sb2.append(str2);
                sb2.append(blendDimension);
                Log.e(str, sb2.toString());
                throw new RuntimeException();
            }
        } else {
            Log.e(str, "lhs/rhs bitmaps of different dimensions");
            throw new RuntimeException();
        }
    }

    /* access modifiers changed from: private */
    public static void computeDistancesBetweenMatches(List<FeatureMatch> list, int i, int i2, int i3, List<Bitmap> list2, int[] iArr, int[] iArr2) {
        int i4 = i3;
        int i5 = (i4 * 2) + 1;
        int i6 = i5 * i5;
        int i7 = i;
        int i8 = i2;
        while (i7 < i8) {
            FeatureMatch featureMatch = (FeatureMatch) list.get(i7);
            int i9 = -i4;
            int access$000 = featureMatch.index0 * i6;
            int access$100 = featureMatch.index1 * i6;
            int i10 = i9;
            float f = 0.0f;
            float f2 = 0.0f;
            float f3 = 0.0f;
            float f4 = 0.0f;
            float f5 = 0.0f;
            while (i10 <= i4) {
                float f6 = f5;
                float f7 = f3;
                float f8 = f;
                int i11 = i9;
                while (i11 <= i4) {
                    int i12 = iArr[access$000];
                    int i13 = iArr2[access$100];
                    access$000++;
                    access$100++;
                    f2 += (float) i12;
                    f8 += (float) (i12 * i12);
                    f4 += (float) i13;
                    f7 += (float) (i13 * i13);
                    f6 += (float) (i12 * i13);
                    i11++;
                    int i14 = i2;
                    i4 = i3;
                }
                i10++;
                int i15 = i2;
                i4 = i3;
                f = f8;
                f3 = f7;
                f5 = f6;
            }
            float f9 = (float) i6;
            float f10 = (f * f9) - (f2 * f2);
            float f11 = 0.0f;
            float f12 = f10 == 0.0f ? 0.0f : 1.0f / f10;
            float f13 = (f3 * f9) - (f4 * f4);
            if (f13 != 0.0f) {
                f11 = 1.0f / f13;
            }
            float f14 = (f9 * f5) - (f2 * f4);
            featureMatch.distance = 1.0f - Math.abs(((f14 * f14) * f12) * f11);
            i7++;
            i8 = i2;
            i4 = i3;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:183:0x0765  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private net.sourceforge.opencamera.PanoramaProcessor.AutoAlignmentByFeatureResult autoAlignmentByFeature(int r41, int r42, java.util.List<android.graphics.Bitmap> r43, int r44) throws net.sourceforge.opencamera.PanoramaProcessorException {
        /*
            r40 = this;
            r1 = r40
            r2 = r41
            r3 = r42
            r0 = r43
            int r4 = r43.size()
            java.lang.String r12 = "PanoramaProcessor"
            r13 = 2
            if (r4 != r13) goto L_0x0a7c
            r40.initRenderscript()
            int r4 = r43.size()
            android.renderscript.Allocation[] r15 = new android.renderscript.Allocation[r4]
            r4 = 0
        L_0x001b:
            int r5 = r43.size()
            if (r4 >= r5) goto L_0x0032
            android.renderscript.RenderScript r5 = r1.f16rs
            java.lang.Object r6 = r0.get(r4)
            android.graphics.Bitmap r6 = (android.graphics.Bitmap) r6
            android.renderscript.Allocation r5 = android.renderscript.Allocation.createFromBitmap(r5, r6)
            r15[r4] = r5
            int r4 = r4 + 1
            goto L_0x001b
        L_0x0032:
            net.sourceforge.opencamera.ScriptC_feature_detector r4 = r1.featureDetectorScript
            if (r4 != 0) goto L_0x003f
            net.sourceforge.opencamera.ScriptC_feature_detector r4 = new net.sourceforge.opencamera.ScriptC_feature_detector
            android.renderscript.RenderScript r5 = r1.f16rs
            r4.<init>(r5)
            r1.featureDetectorScript = r4
        L_0x003f:
            android.graphics.Point[][] r11 = new android.graphics.Point[r13][]
            r4 = 0
        L_0x0042:
            int r5 = r43.size()
            if (r4 >= r5) goto L_0x01bd
            android.renderscript.RenderScript r5 = r1.f16rs
            android.renderscript.Element r8 = android.renderscript.Element.U8(r5)
            android.renderscript.Type r8 = android.renderscript.Type.createXY(r5, r8, r2, r3)
            android.renderscript.Allocation r5 = android.renderscript.Allocation.createTyped(r5, r8)
            net.sourceforge.opencamera.ScriptC_feature_detector r8 = r1.featureDetectorScript
            r9 = r15[r4]
            r8.forEach_create_greyscale(r9, r5)
            android.renderscript.RenderScript r8 = r1.f16rs
            android.renderscript.Element r9 = android.renderscript.Element.U8(r8)
            android.renderscript.Type r9 = android.renderscript.Type.createXY(r8, r9, r2, r3)
            android.renderscript.Allocation r8 = android.renderscript.Allocation.createTyped(r8, r9)
            android.renderscript.RenderScript r9 = r1.f16rs
            android.renderscript.Element r10 = android.renderscript.Element.U8(r9)
            android.renderscript.Type r10 = android.renderscript.Type.createXY(r9, r10, r2, r3)
            android.renderscript.Allocation r9 = android.renderscript.Allocation.createTyped(r9, r10)
            net.sourceforge.opencamera.ScriptC_feature_detector r10 = r1.featureDetectorScript
            r10.set_bitmap(r5)
            net.sourceforge.opencamera.ScriptC_feature_detector r10 = r1.featureDetectorScript
            r10.set_bitmap_Ix(r8)
            net.sourceforge.opencamera.ScriptC_feature_detector r10 = r1.featureDetectorScript
            r10.set_bitmap_Iy(r9)
            net.sourceforge.opencamera.ScriptC_feature_detector r10 = r1.featureDetectorScript
            r10.forEach_compute_derivatives(r5)
            android.renderscript.RenderScript r10 = r1.f16rs
            android.renderscript.Element r6 = android.renderscript.Element.F32(r10)
            android.renderscript.Type r6 = android.renderscript.Type.createXY(r10, r6, r2, r3)
            android.renderscript.Allocation r6 = android.renderscript.Allocation.createTyped(r10, r6)
            net.sourceforge.opencamera.ScriptC_feature_detector r10 = r1.featureDetectorScript
            r10.set_bitmap(r5)
            net.sourceforge.opencamera.ScriptC_feature_detector r10 = r1.featureDetectorScript
            r10.set_bitmap_Ix(r8)
            net.sourceforge.opencamera.ScriptC_feature_detector r10 = r1.featureDetectorScript
            r10.set_bitmap_Iy(r9)
            net.sourceforge.opencamera.ScriptC_feature_detector r10 = r1.featureDetectorScript
            r10.forEach_corner_detector(r5, r6)
            r8.destroy()
            r9.destroy()
            net.sourceforge.opencamera.ScriptC_feature_detector r8 = r1.featureDetectorScript
            r8.set_bitmap(r6)
            int r8 = r2 * r3
            byte[] r8 = new byte[r8]
            java.util.ArrayList r9 = new java.util.ArrayList
            r9.<init>()
            r10 = 0
        L_0x00c4:
            if (r10 >= r13) goto L_0x01a3
            r17 = 1251513984(0x4a989680, float:5000000.0)
            r18 = -1082130432(0xffffffffbf800000, float:-1.0)
            int r19 = r10 * r3
            int r7 = r19 / 2
            int r10 = r10 + 1
            int r19 = r10 * r3
            int r14 = r19 / 2
            r23 = r10
            r13 = 1251513984(0x4a989680, float:5000000.0)
            r17 = 0
            r22 = 0
        L_0x00de:
            net.sourceforge.opencamera.ScriptC_feature_detector r10 = r1.featureDetectorScript
            r10.set_corner_threshold(r13)
            android.renderscript.Script$LaunchOptions r10 = new android.renderscript.Script$LaunchOptions
            r10.<init>()
            r24 = r15
            r15 = 0
            r10.setX(r15, r2)
            r10.setY(r7, r14)
            net.sourceforge.opencamera.ScriptC_feature_detector r15 = r1.featureDetectorScript
            r15.forEach_local_maximum(r6, r5, r10)
            r5.copyTo(r8)
            java.util.ArrayList r10 = new java.util.ArrayList
            r10.<init>()
            r15 = 3
            int r25 = java.lang.Math.max(r7, r15)
            r15 = r25
        L_0x0105:
            int r1 = r3 + -3
            int r1 = java.lang.Math.min(r14, r1)
            if (r15 >= r1) goto L_0x012f
            r1 = 3
        L_0x010e:
            r25 = r7
            int r7 = r2 + -3
            if (r1 >= r7) goto L_0x0128
            int r7 = r15 * r2
            int r7 = r7 + r1
            byte r7 = r8[r7]
            if (r7 == 0) goto L_0x0123
            android.graphics.Point r7 = new android.graphics.Point
            r7.<init>(r1, r15)
            r10.add(r7)
        L_0x0123:
            int r1 = r1 + 1
            r7 = r25
            goto L_0x010e
        L_0x0128:
            int r15 = r15 + 1
            r1 = r40
            r7 = r25
            goto L_0x0105
        L_0x012f:
            r25 = r7
            int r1 = r10.size()
            r7 = 100
            r15 = 50
            if (r1 < r15) goto L_0x0145
            int r1 = r10.size()
            if (r1 > r7) goto L_0x0145
            r9.addAll(r10)
            goto L_0x017f
        L_0x0145:
            int r1 = r10.size()
            r26 = 1056964608(0x3f000000, float:0.5)
            if (r1 >= r15) goto L_0x016b
            r1 = 1234736768(0x49989680, float:1250000.0)
            int r1 = (r13 > r1 ? 1 : (r13 == r1 ? 0 : -1))
            if (r1 > 0) goto L_0x0158
            r9.addAll(r10)
            goto L_0x017f
        L_0x0158:
            int r1 = r17 + 1
            r15 = 10
            if (r1 != r15) goto L_0x0162
            r9.addAll(r10)
            goto L_0x017f
        L_0x0162:
            float r1 = r22 + r13
            float r1 = r1 * r26
            r18 = r13
            r13 = r1
            r1 = 0
            goto L_0x0199
        L_0x016b:
            r15 = 10
            int r1 = r17 + 1
            if (r1 != r15) goto L_0x0188
            int r1 = r10.size()
            java.util.List r1 = r10.subList(r7, r1)
            r1.clear()
            r9.addAll(r10)
        L_0x017f:
            r13 = 2
            r1 = r40
            r10 = r23
            r15 = r24
            goto L_0x00c4
        L_0x0188:
            r1 = 0
            int r7 = (r18 > r1 ? 1 : (r18 == r1 ? 0 : -1))
            if (r7 >= 0) goto L_0x0192
            r7 = 1092616192(0x41200000, float:10.0)
            float r7 = r7 * r13
            goto L_0x0196
        L_0x0192:
            float r7 = r13 + r18
            float r7 = r7 * r26
        L_0x0196:
            r22 = r13
            r13 = r7
        L_0x0199:
            int r17 = r17 + 1
            r1 = r40
            r15 = r24
            r7 = r25
            goto L_0x00de
        L_0x01a3:
            r24 = r15
            r7 = 0
            android.graphics.Point[] r1 = new android.graphics.Point[r7]
            java.lang.Object[] r1 = r9.toArray(r1)
            android.graphics.Point[] r1 = (android.graphics.Point[]) r1
            r11[r4] = r1
            r6.destroy()
            r5.destroy()
            int r4 = r4 + 1
            r13 = 2
            r1 = r40
            goto L_0x0042
        L_0x01bd:
            r24 = r15
            r1 = 0
            r7 = 0
            r4 = r11[r7]
            int r4 = r4.length
            r13 = 0
            r5 = 10
            if (r4 < r5) goto L_0x0a5b
            r15 = 1
            r4 = r11[r15]
            int r4 = r4.length
            if (r4 >= r5) goto L_0x01d1
            goto L_0x0a5b
        L_0x01d1:
            int r4 = r3 / 16
            int r5 = r2 * r2
            int r4 = r4 * r4
            int r5 = r5 + r4
            java.util.ArrayList r10 = new java.util.ArrayList
            r10.<init>()
            r4 = 0
        L_0x01de:
            r6 = 0
            r7 = r11[r6]
            int r7 = r7.length
            if (r4 >= r7) goto L_0x021b
            r7 = r11[r6]
            r7 = r7[r4]
            int r7 = r7.x
            r8 = r11[r6]
            r6 = r8[r4]
            int r6 = r6.y
            r8 = 0
        L_0x01f1:
            r9 = r11[r15]
            int r9 = r9.length
            if (r8 >= r9) goto L_0x0217
            r9 = r11[r15]
            r9 = r9[r8]
            int r9 = r9.x
            r16 = r11[r15]
            r1 = r16[r8]
            int r1 = r1.y
            int r9 = r9 - r7
            int r1 = r1 - r6
            int r9 = r9 * r9
            int r1 = r1 * r1
            int r9 = r9 + r1
            if (r9 >= r5) goto L_0x0213
            net.sourceforge.opencamera.PanoramaProcessor$FeatureMatch r1 = new net.sourceforge.opencamera.PanoramaProcessor$FeatureMatch
            r1.<init>(r4, r8)
            r10.add(r1)
        L_0x0213:
            int r8 = r8 + 1
            r1 = 0
            goto L_0x01f1
        L_0x0217:
            int r4 = r4 + 1
            r1 = 0
            goto L_0x01de
        L_0x021b:
            r1 = 0
            r4 = r11[r1]
            int r4 = r4.length
            int r4 = r4 * 49
            int[] r9 = new int[r4]
            r4 = r11[r15]
            int r4 = r4.length
            int r4 = r4 * 49
            int[] r8 = new int[r4]
            r4 = 0
        L_0x022b:
            r5 = r11[r1]
            int r5 = r5.length
            if (r4 >= r5) goto L_0x025a
            r5 = r11[r1]
            r5 = r5[r4]
            int r5 = r5.x
            r6 = r11[r1]
            r6 = r6[r4]
            int r6 = r6.y
            java.lang.Object r7 = r0.get(r1)
            r25 = r7
            android.graphics.Bitmap r25 = (android.graphics.Bitmap) r25
            int r27 = r4 * 49
            r28 = 7
            r1 = 3
            int r29 = r5 + -3
            int r30 = r6 + -3
            r31 = 7
            r32 = 7
            r26 = r9
            r25.getPixels(r26, r27, r28, r29, r30, r31, r32)
            int r4 = r4 + 1
            r1 = 0
            goto L_0x022b
        L_0x025a:
            r1 = 0
        L_0x025b:
            r4 = r11[r15]
            int r4 = r4.length
            if (r1 >= r4) goto L_0x0289
            r4 = r11[r15]
            r4 = r4[r1]
            int r4 = r4.x
            r5 = r11[r15]
            r5 = r5[r1]
            int r5 = r5.y
            java.lang.Object r6 = r0.get(r15)
            r25 = r6
            android.graphics.Bitmap r25 = (android.graphics.Bitmap) r25
            int r27 = r1 * 49
            r28 = 7
            r6 = 3
            int r29 = r4 + -3
            int r30 = r5 + -3
            r31 = 7
            r32 = 7
            r26 = r8
            r25.getPixels(r26, r27, r28, r29, r30, r31, r32)
            int r1 = r1 + 1
            goto L_0x025b
        L_0x0289:
            r1 = 0
        L_0x028a:
            int r4 = r9.length
            if (r1 >= r4) goto L_0x02c9
            r4 = r9[r1]
            r5 = 4599075939470750515(0x3fd3333333333333, double:0.3)
            int r7 = android.graphics.Color.red(r4)
            double r13 = (double) r7
            java.lang.Double.isNaN(r13)
            double r13 = r13 * r5
            r5 = 4603489467105573601(0x3fe2e147ae147ae1, double:0.59)
            int r7 = android.graphics.Color.green(r4)
            double r2 = (double) r7
            java.lang.Double.isNaN(r2)
            double r2 = r2 * r5
            double r13 = r13 + r2
            r2 = 4592590756007337001(0x3fbc28f5c28f5c29, double:0.11)
            int r4 = android.graphics.Color.blue(r4)
            double r4 = (double) r4
            java.lang.Double.isNaN(r4)
            double r4 = r4 * r2
            double r13 = r13 + r4
            int r2 = (int) r13
            r9[r1] = r2
            int r1 = r1 + 1
            r2 = r41
            r3 = r42
            r13 = 0
            goto L_0x028a
        L_0x02c9:
            r1 = 0
        L_0x02ca:
            int r2 = r8.length
            if (r1 >= r2) goto L_0x0304
            r2 = r8[r1]
            r3 = 4599075939470750515(0x3fd3333333333333, double:0.3)
            int r5 = android.graphics.Color.red(r2)
            double r5 = (double) r5
            java.lang.Double.isNaN(r5)
            double r5 = r5 * r3
            r3 = 4603489467105573601(0x3fe2e147ae147ae1, double:0.59)
            int r7 = android.graphics.Color.green(r2)
            double r13 = (double) r7
            java.lang.Double.isNaN(r13)
            double r13 = r13 * r3
            double r5 = r5 + r13
            r3 = 4592590756007337001(0x3fbc28f5c28f5c29, double:0.11)
            int r2 = android.graphics.Color.blue(r2)
            double r13 = (double) r2
            java.lang.Double.isNaN(r13)
            double r13 = r13 * r3
            double r5 = r5 + r13
            int r2 = (int) r5
            r8[r1] = r2
            int r1 = r1 + 1
            goto L_0x02ca
        L_0x0304:
            int r1 = r10.size()
            r2 = 2
            int r1 = java.lang.Math.min(r1, r2)
            net.sourceforge.opencamera.PanoramaProcessor$ComputeDistancesBetweenMatchesThread[] r2 = new net.sourceforge.opencamera.PanoramaProcessor.ComputeDistancesBetweenMatchesThread[r1]
            r3 = 0
            r6 = 0
        L_0x0311:
            if (r3 >= r1) goto L_0x0345
            int r13 = r3 + 1
            int r4 = r10.size()
            int r4 = r4 * r13
            int r14 = r4 / r1
            net.sourceforge.opencamera.PanoramaProcessor$ComputeDistancesBetweenMatchesThread r18 = new net.sourceforge.opencamera.PanoramaProcessor$ComputeDistancesBetweenMatchesThread
            r20 = 3
            r4 = r18
            r5 = r10
            r7 = r14
            r22 = r8
            r8 = r20
            r20 = r9
            r9 = r43
            r23 = r10
            r10 = r20
            r25 = r11
            r11 = r22
            r4.<init>(r5, r6, r7, r8, r9, r10, r11)
            r2[r3] = r18
            r3 = r13
            r6 = r14
            r9 = r20
            r8 = r22
            r10 = r23
            r11 = r25
            goto L_0x0311
        L_0x0345:
            r23 = r10
            r25 = r11
            r0 = 0
        L_0x034a:
            if (r0 >= r1) goto L_0x0354
            r3 = r2[r0]
            r3.start()
            int r0 = r0 + 1
            goto L_0x034a
        L_0x0354:
            r0 = 0
        L_0x0355:
            if (r0 >= r1) goto L_0x036f
            r3 = r2[r0]     // Catch:{ InterruptedException -> 0x035f }
            r3.join()     // Catch:{ InterruptedException -> 0x035f }
            int r0 = r0 + 1
            goto L_0x0355
        L_0x035f:
            r0 = move-exception
            java.lang.String r1 = "ComputeDistancesBetweenMatchesThread threads interrupted"
            android.util.Log.e(r12, r1)
            r0.printStackTrace()
            java.lang.Thread r0 = java.lang.Thread.currentThread()
            r0.interrupt()
        L_0x036f:
            java.util.Collections.sort(r23)
            r1 = 0
            r0 = r25[r1]
            int r0 = r0.length
            boolean[] r0 = new boolean[r0]
            r2 = r25[r1]
            int r1 = r2.length
            boolean[] r1 = new boolean[r1]
            r2 = r25[r15]
            int r2 = r2.length
            boolean[] r2 = new boolean[r2]
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            r4 = 0
        L_0x0388:
            int r5 = r23.size()
            r6 = 4532020583610935537(0x3ee4f8b588e368f1, double:1.0E-5)
            if (r4 >= r5) goto L_0x040e
            r5 = r23
            java.lang.Object r8 = r5.get(r4)
            net.sourceforge.opencamera.PanoramaProcessor$FeatureMatch r8 = (net.sourceforge.opencamera.PanoramaProcessor.FeatureMatch) r8
            int r9 = r8.index0
            boolean r9 = r1[r9]
            if (r9 != 0) goto L_0x0407
            int r9 = r8.index1
            boolean r9 = r2[r9]
            if (r9 == 0) goto L_0x03ac
            goto L_0x0407
        L_0x03ac:
            int r9 = r4 + 1
            r10 = 0
            r11 = 0
        L_0x03b0:
            int r13 = r5.size()
            if (r9 >= r13) goto L_0x03e7
            if (r10 != 0) goto L_0x03e7
            java.lang.Object r13 = r5.get(r9)
            net.sourceforge.opencamera.PanoramaProcessor$FeatureMatch r13 = (net.sourceforge.opencamera.PanoramaProcessor.FeatureMatch) r13
            int r14 = r8.index0
            int r15 = r13.index0
            if (r14 != r15) goto L_0x03e3
            float r10 = r8.distance
            float r13 = r13.distance
            float r10 = r10 / r13
            double r13 = (double) r10
            java.lang.Double.isNaN(r13)
            double r13 = r13 + r6
            r22 = 4605380979056443392(0x3fe99999a0000000, double:0.800000011920929)
            int r10 = (r13 > r22 ? 1 : (r13 == r22 ? 0 : -1))
            if (r10 <= 0) goto L_0x03e2
            r10 = 1
            r11 = 1
            goto L_0x03e3
        L_0x03e2:
            r10 = 1
        L_0x03e3:
            int r9 = r9 + 1
            r15 = 1
            goto L_0x03b0
        L_0x03e7:
            if (r11 == 0) goto L_0x03f7
            int r6 = r8.index0
            r7 = 1
            r1[r6] = r7
            int r6 = r8.index0
            r0[r6] = r7
            goto L_0x0407
        L_0x03f7:
            r7 = 1
            r3.add(r8)
            int r6 = r8.index0
            r1[r6] = r7
            int r6 = r8.index1
            r2[r6] = r7
        L_0x0407:
            int r4 = r4 + 1
            r23 = r5
            r15 = 1
            goto L_0x0388
        L_0x040e:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "### autoAlignmentByFeature: time after finding possible matches: "
            r0.append(r1)
            long r1 = java.lang.System.currentTimeMillis()
            r4 = 0
            long r1 = r1 - r4
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            android.util.Log.d(r12, r0)
            int r0 = r3.size()
            double r0 = (double) r0
            r4 = 4600877379321698714(0x3fd999999999999a, double:0.4)
            java.lang.Double.isNaN(r0)
            double r0 = r0 * r4
            int r0 = (int) r0
            r1 = 1
            int r0 = r0 + r1
            r1 = 5
            int r0 = java.lang.Math.max(r1, r0)
            int r1 = r3.size()
            if (r0 >= r1) goto L_0x0451
            int r1 = r3.size()
            java.util.List r0 = r3.subList(r0, r1)
            r0.clear()
        L_0x0451:
            r1 = 0
            r0 = r25[r1]
            int r0 = r0.length
            boolean[] r0 = new boolean[r0]
            r1 = 1
            r2 = r25[r1]
            int r2 = r2.length
            boolean[] r2 = new boolean[r2]
            java.util.Iterator r4 = r3.iterator()
        L_0x0461:
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L_0x047a
            java.lang.Object r5 = r4.next()
            net.sourceforge.opencamera.PanoramaProcessor$FeatureMatch r5 = (net.sourceforge.opencamera.PanoramaProcessor.FeatureMatch) r5
            int r8 = r5.index0
            r0[r8] = r1
            int r5 = r5.index1
            r2[r5] = r1
            goto L_0x0461
        L_0x047a:
            int r0 = r3.size()
            if (r0 != 0) goto L_0x049f
            r1 = r24
            r0 = 0
        L_0x0483:
            int r2 = r1.length
            if (r0 >= r2) goto L_0x0495
            r2 = r1[r0]
            if (r2 == 0) goto L_0x0492
            r2 = r1[r0]
            r2.destroy()
            r2 = 0
            r1[r0] = r2
        L_0x0492:
            int r0 = r0 + 1
            goto L_0x0483
        L_0x0495:
            net.sourceforge.opencamera.PanoramaProcessor$AutoAlignmentByFeatureResult r0 = new net.sourceforge.opencamera.PanoramaProcessor$AutoAlignmentByFeatureResult
            r1 = 1065353216(0x3f800000, float:1.0)
            r2 = 0
            r3 = 0
            r0.<init>(r3, r3, r2, r1)
            return r0
        L_0x049f:
            r1 = r24
            r2 = 0
            r0 = 1084227584(0x40a00000, float:5.0)
            int r4 = java.lang.Math.max(r41, r42)
            float r4 = (float) r4
            r5 = 1082130432(0x40800000, float:4.0)
            float r4 = r4 / r5
            float r0 = java.lang.Math.max(r0, r4)
            float r0 = r0 * r0
            java.util.ArrayList r4 = new java.util.ArrayList
            r4.<init>()
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            java.util.ArrayList r8 = new java.util.ArrayList
            r8.<init>()
            r9 = 1084248556(0x40a051ec, float:5.01)
            int r10 = java.lang.Math.max(r41, r42)
            float r10 = (float) r10
            r11 = 1120403456(0x42c80000, float:100.0)
            float r10 = r10 / r11
            float r9 = java.lang.Math.max(r9, r10)
            float r9 = r9 * r9
            r10 = 0
            r14 = 0
        L_0x04d4:
            int r11 = r3.size()
            if (r10 >= r11) goto L_0x0892
            java.lang.Object r11 = r3.get(r10)
            net.sourceforge.opencamera.PanoramaProcessor$FeatureMatch r11 = (net.sourceforge.opencamera.PanoramaProcessor.FeatureMatch) r11
            r15 = 1
            r18 = r25[r15]
            int r20 = r11.index1
            r2 = r18[r20]
            int r2 = r2.x
            r18 = 0
            r20 = r25[r18]
            int r21 = r11.index0
            r12 = r20[r21]
            int r12 = r12.x
            int r2 = r2 - r12
            r12 = r25[r15]
            int r13 = r11.index1
            r12 = r12[r13]
            int r12 = r12.y
            r13 = r25[r18]
            int r15 = r11.index0
            r13 = r13[r15]
            int r13 = r13.y
            int r12 = r12 - r13
            r8.clear()
            java.util.Iterator r13 = r3.iterator()
        L_0x0514:
            boolean r15 = r13.hasNext()
            if (r15 == 0) goto L_0x0577
            java.lang.Object r15 = r13.next()
            net.sourceforge.opencamera.PanoramaProcessor$FeatureMatch r15 = (net.sourceforge.opencamera.PanoramaProcessor.FeatureMatch) r15
            r18 = 0
            r20 = r25[r18]
            int r21 = r15.index0
            r6 = r20[r21]
            int r6 = r6.x
            r7 = r25[r18]
            int r18 = r15.index0
            r7 = r7[r18]
            int r7 = r7.y
            r18 = 1
            r20 = r25[r18]
            int r24 = r15.index1
            r41 = r13
            r13 = r20[r24]
            int r13 = r13.x
            r20 = r25[r18]
            int r18 = r15.index1
            r43 = r14
            r14 = r20[r18]
            int r14 = r14.y
            int r6 = r6 + r2
            int r7 = r7 + r12
            int r6 = r6 - r13
            float r6 = (float) r6
            int r7 = r7 - r14
            float r7 = (float) r7
            float r6 = r6 * r6
            float r7 = r7 * r7
            float r6 = r6 + r7
            double r6 = (double) r6
            java.lang.Double.isNaN(r6)
            r13 = 4532020583610935537(0x3ee4f8b588e368f1, double:1.0E-5)
            double r6 = r6 + r13
            double r13 = (double) r9
            int r18 = (r6 > r13 ? 1 : (r6 == r13 ? 0 : -1))
            if (r18 > 0) goto L_0x056d
            r8.add(r15)
        L_0x056d:
            r13 = r41
            r14 = r43
            r6 = 4532020583610935537(0x3ee4f8b588e368f1, double:1.0E-5)
            goto L_0x0514
        L_0x0577:
            r43 = r14
            int r2 = r8.size()
            int r6 = r5.size()
            if (r2 <= r6) goto L_0x05a2
            r4.clear()
            r4.add(r11)
            r5.clear()
            r5.addAll(r8)
            int r2 = r5.size()
            int r6 = r3.size()
            if (r2 != r6) goto L_0x05a0
            r24 = r1
            r1 = r5
            r2 = 2
            r14 = 0
            goto L_0x0898
        L_0x05a0:
            r2 = 0
            goto L_0x05a4
        L_0x05a2:
            r2 = r43
        L_0x05a4:
            r15 = r2
            r2 = 0
        L_0x05a6:
            if (r2 >= r10) goto L_0x0867
            java.lang.Object r6 = r3.get(r2)
            net.sourceforge.opencamera.PanoramaProcessor$FeatureMatch r6 = (net.sourceforge.opencamera.PanoramaProcessor.FeatureMatch) r6
            r7 = 0
            r12 = r25[r7]
            int r13 = r11.index0
            r12 = r12[r13]
            int r12 = r12.x
            r13 = r25[r7]
            int r14 = r6.index0
            r13 = r13[r14]
            int r13 = r13.x
            int r12 = r12 + r13
            r13 = 2
            int r12 = r12 / r13
            r14 = r25[r7]
            int r18 = r11.index0
            r14 = r14[r18]
            int r14 = r14.y
            r18 = r25[r7]
            int r7 = r6.index0
            r7 = r18[r7]
            int r7 = r7.y
            int r14 = r14 + r7
            int r14 = r14 / r13
            r7 = 1
            r18 = r25[r7]
            int r19 = r11.index1
            r13 = r18[r19]
            int r13 = r13.x
            r18 = r25[r7]
            int r19 = r6.index1
            r7 = r18[r19]
            int r7 = r7.x
            int r13 = r13 + r7
            r7 = 2
            int r13 = r13 / r7
            r18 = 1
            r19 = r25[r18]
            int r20 = r11.index1
            r7 = r19[r20]
            int r7 = r7.y
            r19 = r25[r18]
            int r18 = r6.index1
            r43 = r15
            r15 = r19[r18]
            int r15 = r15.y
            int r7 = r7 + r15
            r15 = 2
            int r7 = r7 / r15
            r15 = 0
            r18 = r25[r15]
            int r20 = r11.index0
            r24 = r1
            r1 = r18[r20]
            int r1 = r1.x
            r18 = r25[r15]
            int r20 = r6.index0
            r15 = r18[r20]
            int r15 = r15.x
            int r1 = r1 - r15
            float r1 = (float) r1
            r15 = 0
            r18 = r25[r15]
            int r20 = r11.index0
            r28 = r10
            r10 = r18[r20]
            int r10 = r10.y
            r18 = r25[r15]
            int r15 = r6.index0
            r15 = r18[r15]
            int r15 = r15.y
            int r10 = r10 - r15
            float r10 = (float) r10
            r15 = 1
            r18 = r25[r15]
            int r20 = r11.index1
            r29 = r2
            r2 = r18[r20]
            int r2 = r2.x
            r18 = r25[r15]
            int r20 = r6.index1
            r15 = r18[r20]
            int r15 = r15.x
            int r2 = r2 - r15
            float r2 = (float) r2
            r15 = 1
            r18 = r25[r15]
            int r20 = r11.index1
            r30 = r4
            r4 = r18[r20]
            int r4 = r4.y
            r18 = r25[r15]
            int r15 = r6.index1
            r15 = r18[r15]
            int r15 = r15.y
            int r4 = r4 - r15
            float r4 = (float) r4
            float r15 = r1 * r1
            float r18 = r10 * r10
            float r15 = r15 + r18
            float r18 = r2 * r2
            float r20 = r4 * r4
            float r18 = r18 + r20
            int r15 = (r15 > r0 ? 1 : (r15 == r0 ? 0 : -1))
            if (r15 < 0) goto L_0x0850
            int r15 = (r18 > r0 ? 1 : (r18 == r0 ? 0 : -1))
            if (r15 >= 0) goto L_0x0689
            goto L_0x0850
        L_0x0689:
            r15 = 1050253722(0x3e99999a, float:0.3)
            r20 = r0
            r18 = r5
            r5 = r42
            float r0 = (float) r5
            float r15 = r15 * r0
            r31 = 1060320051(0x3f333333, float:0.7)
            float r0 = r0 * r31
            r21 = 0
            r31 = r25[r21]
            int r32 = r11.index0
            r5 = r31[r32]
            int r5 = r5.y
            float r5 = (float) r5
            int r5 = (r5 > r15 ? 1 : (r5 == r15 ? 0 : -1))
            if (r5 < 0) goto L_0x084b
            r5 = r25[r21]
            int r31 = r11.index0
            r5 = r5[r31]
            int r5 = r5.y
            float r5 = (float) r5
            int r5 = (r5 > r0 ? 1 : (r5 == r0 ? 0 : -1))
            if (r5 > 0) goto L_0x084b
            r5 = 1
            r31 = r25[r5]
            int r32 = r11.index1
            r5 = r31[r32]
            int r5 = r5.y
            float r5 = (float) r5
            int r5 = (r5 > r15 ? 1 : (r5 == r15 ? 0 : -1))
            if (r5 < 0) goto L_0x084b
            r5 = 1
            r31 = r25[r5]
            int r5 = r11.index1
            r5 = r31[r5]
            int r5 = r5.y
            float r5 = (float) r5
            int r5 = (r5 > r0 ? 1 : (r5 == r0 ? 0 : -1))
            if (r5 > 0) goto L_0x084b
            r5 = 0
            r21 = r25[r5]
            int r31 = r6.index0
            r5 = r21[r31]
            int r5 = r5.y
            float r5 = (float) r5
            int r5 = (r5 > r15 ? 1 : (r5 == r15 ? 0 : -1))
            if (r5 < 0) goto L_0x084b
            r5 = 0
            r31 = r25[r5]
            int r5 = r6.index0
            r5 = r31[r5]
            int r5 = r5.y
            float r5 = (float) r5
            int r5 = (r5 > r0 ? 1 : (r5 == r0 ? 0 : -1))
            if (r5 > 0) goto L_0x084b
            r5 = 1
            r31 = r25[r5]
            int r32 = r6.index1
            r5 = r31[r32]
            int r5 = r5.y
            float r5 = (float) r5
            int r5 = (r5 > r15 ? 1 : (r5 == r15 ? 0 : -1))
            if (r5 < 0) goto L_0x084b
            r5 = 1
            r15 = r25[r5]
            int r5 = r6.index1
            r5 = r15[r5]
            int r5 = r5.y
            float r5 = (float) r5
            int r0 = (r5 > r0 ? 1 : (r5 == r0 ? 0 : -1))
            if (r0 <= 0) goto L_0x071c
            goto L_0x084b
        L_0x071c:
            double r4 = (double) r4
            r41 = r6
            r0 = r7
            double r6 = (double) r2
            double r4 = java.lang.Math.atan2(r4, r6)
            double r6 = (double) r10
            double r1 = (double) r1
            double r1 = java.lang.Math.atan2(r6, r1)
            double r4 = r4 - r1
            float r1 = (float) r4
            double r4 = (double) r1
            r6 = -4609115380302729960(0xc00921fb54442d18, double:-3.141592653589793)
            int r2 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r2 >= 0) goto L_0x0742
            java.lang.Double.isNaN(r4)
            r6 = 4618760256179416344(0x401921fb54442d18, double:6.283185307179586)
            double r4 = r4 + r6
        L_0x0740:
            float r1 = (float) r4
            goto L_0x0755
        L_0x0742:
            r6 = 4618760256179416344(0x401921fb54442d18, double:6.283185307179586)
            r22 = 4614256656552045848(0x400921fb54442d18, double:3.141592653589793)
            int r2 = (r4 > r22 ? 1 : (r4 == r22 ? 0 : -1))
            if (r2 <= 0) goto L_0x0755
            java.lang.Double.isNaN(r4)
            double r4 = r4 - r6
            goto L_0x0740
        L_0x0755:
            float r2 = java.lang.Math.abs(r1)
            double r4 = (double) r2
            r6 = 4602891378046628709(0x3fe0c152382d7365, double:0.5235987755982988)
            int r2 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r2 <= 0) goto L_0x0765
            goto L_0x084b
        L_0x0765:
            r8.clear()
            java.util.Iterator r2 = r3.iterator()
        L_0x076c:
            boolean r4 = r2.hasNext()
            if (r4 == 0) goto L_0x080a
            java.lang.Object r4 = r2.next()
            net.sourceforge.opencamera.PanoramaProcessor$FeatureMatch r4 = (net.sourceforge.opencamera.PanoramaProcessor.FeatureMatch) r4
            r5 = 0
            r6 = r25[r5]
            int r7 = r4.index0
            r6 = r6[r7]
            int r6 = r6.x
            r7 = r25[r5]
            int r5 = r4.index0
            r5 = r7[r5]
            int r5 = r5.y
            r7 = 1
            r10 = r25[r7]
            int r15 = r4.index1
            r10 = r10[r15]
            int r10 = r10.x
            r15 = r25[r7]
            int r7 = r4.index1
            r7 = r15[r7]
            int r7 = r7.y
            int r6 = r6 - r12
            int r5 = r5 - r14
            r31 = r14
            double r14 = (double) r6
            r32 = r2
            r6 = r3
            double r2 = (double) r1
            double r33 = java.lang.Math.cos(r2)
            java.lang.Double.isNaN(r14)
            double r33 = r33 * r14
            r35 = r6
            double r5 = (double) r5
            double r36 = java.lang.Math.sin(r2)
            java.lang.Double.isNaN(r5)
            double r36 = r36 * r5
            r38 = r11
            r39 = r12
            double r11 = r33 - r36
            int r11 = (int) r11
            double r33 = java.lang.Math.sin(r2)
            java.lang.Double.isNaN(r14)
            double r14 = r14 * r33
            double r2 = java.lang.Math.cos(r2)
            java.lang.Double.isNaN(r5)
            double r5 = r5 * r2
            double r14 = r14 + r5
            int r2 = (int) r14
            float r2 = (float) r2
            r3 = 1065353216(0x3f800000, float:1.0)
            float r2 = r2 * r3
            int r2 = (int) r2
            int r11 = r11 + r13
            int r2 = r2 + r0
            int r11 = r11 - r10
            float r3 = (float) r11
            int r2 = r2 - r7
            float r2 = (float) r2
            float r3 = r3 * r3
            float r2 = r2 * r2
            float r3 = r3 + r2
            double r2 = (double) r3
            java.lang.Double.isNaN(r2)
            r5 = 4532020583610935537(0x3ee4f8b588e368f1, double:1.0E-5)
            double r2 = r2 + r5
            double r5 = (double) r9
            int r7 = (r2 > r5 ? 1 : (r2 == r5 ? 0 : -1))
            if (r7 > 0) goto L_0x07fe
            r8.add(r4)
        L_0x07fe:
            r14 = r31
            r2 = r32
            r3 = r35
            r11 = r38
            r12 = r39
            goto L_0x076c
        L_0x080a:
            r35 = r3
            r38 = r11
            int r0 = r8.size()
            int r1 = r18.size()
            if (r0 <= r1) goto L_0x0844
            int r0 = r8.size()
            r1 = 5
            if (r0 < r1) goto L_0x0844
            r30.clear()
            r0 = r30
            r11 = r38
            r0.add(r11)
            r6 = r41
            r0.add(r6)
            r18.clear()
            r1 = r18
            r1.addAll(r8)
            int r2 = r1.size()
            int r3 = r35.size()
            if (r2 != r3) goto L_0x0842
            r14 = 1
            goto L_0x0875
        L_0x0842:
            r15 = 1
            goto L_0x0859
        L_0x0844:
            r1 = r18
            r0 = r30
            r11 = r38
            goto L_0x0857
        L_0x084b:
            r35 = r3
            r1 = r18
            goto L_0x0855
        L_0x0850:
            r20 = r0
            r35 = r3
            r1 = r5
        L_0x0855:
            r0 = r30
        L_0x0857:
            r15 = r43
        L_0x0859:
            int r2 = r29 + 1
            r4 = r0
            r5 = r1
            r0 = r20
            r1 = r24
            r10 = r28
            r3 = r35
            goto L_0x05a6
        L_0x0867:
            r20 = r0
            r24 = r1
            r35 = r3
            r0 = r4
            r1 = r5
            r28 = r10
            r43 = r15
            r14 = r43
        L_0x0875:
            int r2 = r1.size()
            int r3 = r35.size()
            if (r2 != r3) goto L_0x0880
            goto L_0x0897
        L_0x0880:
            int r10 = r28 + 1
            r4 = r0
            r5 = r1
            r0 = r20
            r1 = r24
            r3 = r35
            r2 = 0
            r6 = 4532020583610935537(0x3ee4f8b588e368f1, double:1.0E-5)
            goto L_0x04d4
        L_0x0892:
            r24 = r1
            r1 = r5
            r43 = r14
        L_0x0897:
            r2 = 2
        L_0x0898:
            android.graphics.Point[] r0 = new android.graphics.Point[r2]
            r3 = 0
        L_0x089b:
            if (r3 >= r2) goto L_0x08a8
            android.graphics.Point r2 = new android.graphics.Point
            r2.<init>()
            r0[r3] = r2
            int r3 = r3 + 1
            r2 = 2
            goto L_0x089b
        L_0x08a8:
            java.util.Iterator r2 = r1.iterator()
        L_0x08ac:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x08ff
            java.lang.Object r3 = r2.next()
            net.sourceforge.opencamera.PanoramaProcessor$FeatureMatch r3 = (net.sourceforge.opencamera.PanoramaProcessor.FeatureMatch) r3
            r4 = 0
            r5 = r0[r4]
            int r6 = r5.x
            r7 = r25[r4]
            int r8 = r3.index0
            r7 = r7[r8]
            int r7 = r7.x
            int r6 = r6 + r7
            r5.x = r6
            r5 = r0[r4]
            int r6 = r5.y
            r7 = r25[r4]
            int r4 = r3.index0
            r4 = r7[r4]
            int r4 = r4.y
            int r6 = r6 + r4
            r5.y = r6
            r4 = 1
            r5 = r0[r4]
            int r6 = r5.x
            r7 = r25[r4]
            int r8 = r3.index1
            r7 = r7[r8]
            int r7 = r7.x
            int r6 = r6 + r7
            r5.x = r6
            r5 = r0[r4]
            int r6 = r5.y
            r7 = r25[r4]
            int r3 = r3.index1
            r3 = r7[r3]
            int r3 = r3.y
            int r6 = r6 + r3
            r5.y = r6
            goto L_0x08ac
        L_0x08ff:
            r2 = 0
            r3 = 2
        L_0x0901:
            if (r2 >= r3) goto L_0x091c
            r4 = r0[r2]
            int r5 = r4.x
            int r6 = r1.size()
            int r5 = r5 / r6
            r4.x = r5
            r4 = r0[r2]
            int r5 = r4.y
            int r6 = r1.size()
            int r5 = r5 / r6
            r4.y = r5
            int r2 = r2 + 1
            goto L_0x0901
        L_0x091c:
            r2 = 1
            r3 = r0[r2]
            int r3 = r3.x
            r4 = 0
            r5 = r0[r4]
            int r5 = r5.x
            int r3 = r3 - r5
            r5 = r0[r2]
            int r2 = r5.y
            r5 = r0[r4]
            int r5 = r5.y
            int r2 = r2 - r5
            if (r14 == 0) goto L_0x0a3d
            java.util.Iterator r1 = r1.iterator()
            r10 = 0
            r14 = 0
        L_0x0938:
            boolean r5 = r1.hasNext()
            if (r5 == 0) goto L_0x09e1
            java.lang.Object r5 = r1.next()
            net.sourceforge.opencamera.PanoramaProcessor$FeatureMatch r5 = (net.sourceforge.opencamera.PanoramaProcessor.FeatureMatch) r5
            r6 = r25[r4]
            int r7 = r5.index0
            r6 = r6[r7]
            int r6 = r6.x
            r7 = r0[r4]
            int r7 = r7.x
            int r6 = r6 - r7
            float r6 = (float) r6
            r7 = r25[r4]
            int r8 = r5.index0
            r7 = r7[r8]
            int r7 = r7.y
            r8 = r0[r4]
            int r4 = r8.y
            int r7 = r7 - r4
            float r4 = (float) r7
            r7 = 1
            r8 = r25[r7]
            int r9 = r5.index1
            r8 = r8[r9]
            int r8 = r8.x
            r9 = r0[r7]
            int r9 = r9.x
            int r8 = r8 - r9
            float r8 = (float) r8
            r9 = r25[r7]
            int r5 = r5.index1
            r5 = r9[r5]
            int r5 = r5.y
            r9 = r0[r7]
            int r9 = r9.y
            int r5 = r5 - r9
            float r5 = (float) r5
            float r9 = r6 * r6
            float r11 = r4 * r4
            float r9 = r9 + r11
            float r11 = r8 * r8
            float r12 = r5 * r5
            float r11 = r11 + r12
            double r12 = (double) r9
            r18 = 4532020583610935537(0x3ee4f8b588e368f1, double:1.0E-5)
            int r9 = (r12 > r18 ? 1 : (r12 == r18 ? 0 : -1))
            if (r9 < 0) goto L_0x09d9
            double r11 = (double) r11
            int r9 = (r11 > r18 ? 1 : (r11 == r18 ? 0 : -1))
            if (r9 >= 0) goto L_0x099f
            goto L_0x09d9
        L_0x099f:
            double r11 = (double) r5
            double r8 = (double) r8
            double r8 = java.lang.Math.atan2(r11, r8)
            double r4 = (double) r4
            double r11 = (double) r6
            double r4 = java.lang.Math.atan2(r4, r11)
            double r8 = r8 - r4
            float r4 = (float) r8
            double r5 = (double) r4
            r8 = -4609115380302729960(0xc00921fb54442d18, double:-3.141592653589793)
            int r11 = (r5 > r8 ? 1 : (r5 == r8 ? 0 : -1))
            if (r11 >= 0) goto L_0x09c2
            java.lang.Double.isNaN(r5)
            r8 = 4618760256179416344(0x401921fb54442d18, double:6.283185307179586)
            double r5 = r5 + r8
        L_0x09c0:
            float r4 = (float) r5
            goto L_0x09d5
        L_0x09c2:
            r8 = 4618760256179416344(0x401921fb54442d18, double:6.283185307179586)
            r11 = 4614256656552045848(0x400921fb54442d18, double:3.141592653589793)
            int r13 = (r5 > r11 ? 1 : (r5 == r11 ? 0 : -1))
            if (r13 <= 0) goto L_0x09d5
            java.lang.Double.isNaN(r5)
            double r5 = r5 - r8
            goto L_0x09c0
        L_0x09d5:
            float r10 = r10 + r4
            int r14 = r14 + 1
            goto L_0x09de
        L_0x09d9:
            r8 = 4618760256179416344(0x401921fb54442d18, double:6.283185307179586)
        L_0x09de:
            r4 = 0
            goto L_0x0938
        L_0x09e1:
            if (r14 <= 0) goto L_0x09e7
            float r1 = (float) r14
            float r10 = r10 / r1
            r1 = 0
            goto L_0x09e9
        L_0x09e7:
            r1 = 0
            r10 = 0
        L_0x09e9:
            r4 = r0[r1]
            int r4 = r4.x
            double r4 = (double) r4
            double r6 = (double) r10
            double r8 = java.lang.Math.cos(r6)
            java.lang.Double.isNaN(r4)
            double r4 = r4 * r8
            r8 = r0[r1]
            int r8 = r8.y
            double r8 = (double) r8
            double r11 = java.lang.Math.sin(r6)
            java.lang.Double.isNaN(r8)
            double r8 = r8 * r11
            double r4 = r4 - r8
            float r4 = (float) r4
            r5 = r0[r1]
            int r5 = r5.x
            double r8 = (double) r5
            double r11 = java.lang.Math.sin(r6)
            java.lang.Double.isNaN(r8)
            double r8 = r8 * r11
            r5 = r0[r1]
            int r5 = r5.y
            double r11 = (double) r5
            double r5 = java.lang.Math.cos(r6)
            java.lang.Double.isNaN(r11)
            double r11 = r11 * r5
            double r8 = r8 + r11
            float r5 = (float) r8
            r6 = 1065353216(0x3f800000, float:1.0)
            float r5 = r5 * r6
            float r3 = (float) r3
            r6 = r0[r1]
            int r6 = r6.x
            float r6 = (float) r6
            float r6 = r6 - r4
            float r3 = r3 + r6
            int r3 = (int) r3
            float r2 = (float) r2
            r0 = r0[r1]
            int r0 = r0.y
            float r0 = (float) r0
            float r0 = r0 - r5
            float r2 = r2 + r0
            int r2 = (int) r2
            goto L_0x0a3e
        L_0x0a3d:
            r10 = 0
        L_0x0a3e:
            r1 = r24
            r0 = 0
        L_0x0a41:
            int r4 = r1.length
            if (r0 >= r4) goto L_0x0a53
            r4 = r1[r0]
            if (r4 == 0) goto L_0x0a50
            r4 = r1[r0]
            r4.destroy()
            r4 = 0
            r1[r0] = r4
        L_0x0a50:
            int r0 = r0 + 1
            goto L_0x0a41
        L_0x0a53:
            net.sourceforge.opencamera.PanoramaProcessor$AutoAlignmentByFeatureResult r0 = new net.sourceforge.opencamera.PanoramaProcessor$AutoAlignmentByFeatureResult
            r1 = 1065353216(0x3f800000, float:1.0)
            r0.<init>(r3, r2, r10, r1)
            return r0
        L_0x0a5b:
            r1 = r24
            r0 = 0
        L_0x0a5e:
            int r2 = r1.length
            if (r0 >= r2) goto L_0x0a72
            r2 = r1[r0]
            if (r2 == 0) goto L_0x0a6e
            r2 = r1[r0]
            r2.destroy()
            r2 = 0
            r1[r0] = r2
            goto L_0x0a6f
        L_0x0a6e:
            r2 = 0
        L_0x0a6f:
            int r0 = r0 + 1
            goto L_0x0a5e
        L_0x0a72:
            net.sourceforge.opencamera.PanoramaProcessor$AutoAlignmentByFeatureResult r0 = new net.sourceforge.opencamera.PanoramaProcessor$AutoAlignmentByFeatureResult
            r1 = 1065353216(0x3f800000, float:1.0)
            r2 = 0
            r3 = 0
            r0.<init>(r3, r3, r2, r1)
            return r0
        L_0x0a7c:
            r3 = 0
            java.lang.String r0 = "must have 2 bitmaps"
            android.util.Log.e(r12, r0)
            net.sourceforge.opencamera.PanoramaProcessorException r0 = new net.sourceforge.opencamera.PanoramaProcessorException
            r0.<init>(r3)
            goto L_0x0a89
        L_0x0a88:
            throw r0
        L_0x0a89:
            goto L_0x0a88
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.PanoramaProcessor.autoAlignmentByFeature(int, int, java.util.List, int):net.sourceforge.opencamera.PanoramaProcessor$AutoAlignmentByFeatureResult");
    }

    private Bitmap blend_panorama_alpha(Bitmap bitmap, Bitmap bitmap2) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int width2 = bitmap2.getWidth();
        String str = TAG;
        if (width != width2) {
            Log.e(str, "bitmaps have different widths");
            throw new RuntimeException();
        } else if (height == bitmap2.getHeight()) {
            Paint paint = new Paint();
            Rect rect = new Rect();
            Bitmap createBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            paint.setXfermode(new PorterDuffXfermode(Mode.ADD));
            int i = 0;
            while (i < width) {
                int i2 = i + 1;
                rect.set(i, 0, i2, height);
                float f = ((float) width) - 1.0f;
                float f2 = (float) i;
                paint.setAlpha((int) (((f - f2) / f) * 255.0f));
                canvas.drawBitmap(bitmap, rect, rect, paint);
                paint.setAlpha((int) ((f2 / f) * 255.0f));
                canvas.drawBitmap(bitmap2, rect, rect, paint);
                i = i2;
            }
            return createBitmap;
        } else {
            Log.e(str, "bitmaps have different heights");
            throw new RuntimeException();
        }
    }

    private static int nextMultiple(int i, int i2) {
        int i3 = i % i2;
        return i3 > 0 ? i + (i2 - i3) : i;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x007d A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.graphics.Bitmap createProjectedBitmap(android.graphics.Rect r18, android.graphics.Rect r19, android.graphics.Bitmap r20, android.graphics.Paint r21, int r22, int r23, double r24, int r26) {
        /*
            r17 = this;
            r0 = r18
            r1 = r19
            r2 = r20
            r3 = r21
            r4 = r22
            r5 = r23
            android.graphics.Bitmap$Config r6 = android.graphics.Bitmap.Config.ARGB_8888
            android.graphics.Bitmap r6 = android.graphics.Bitmap.createBitmap(r4, r5, r6)
            android.graphics.Canvas r7 = new android.graphics.Canvas
            r7.<init>(r6)
            r8 = -1
            r8 = 0
            r10 = -1
            r11 = -1
            r12 = 0
        L_0x001c:
            if (r8 >= r4) goto L_0x0082
            int r13 = r4 / 2
            int r13 = r13 + r26
            int r13 = r8 - r13
            float r13 = (float) r13
            double r13 = (double) r13
            java.lang.Double.isNaN(r13)
            double r13 = r13 * r24
            float r13 = (float) r13
            float r14 = (float) r4
            float r13 = r13 / r14
            float r14 = (float) r5
            r15 = r10
            double r9 = (double) r13
            double r9 = java.lang.Math.cos(r9)
            float r9 = (float) r9
            float r9 = r9 * r14
            float r10 = r14 - r9
            r13 = 1073741824(0x40000000, float:2.0)
            float r10 = r10 / r13
            r16 = 1056964608(0x3f000000, float:0.5)
            float r10 = r10 + r16
            int r10 = (int) r10
            float r14 = r14 + r9
            float r14 = r14 / r13
            float r14 = r14 + r16
            int r9 = (int) r14
            if (r8 != 0) goto L_0x004d
            r13 = r9
            r15 = r10
        L_0x004b:
            r11 = 0
            goto L_0x006e
        L_0x004d:
            int r13 = r10 - r15
            int r13 = java.lang.Math.abs(r13)
            r14 = 1
            if (r13 > r14) goto L_0x0061
            int r13 = r9 - r11
            int r13 = java.lang.Math.abs(r13)
            if (r13 <= r14) goto L_0x005f
            goto L_0x0061
        L_0x005f:
            r13 = r11
            goto L_0x004b
        L_0x0061:
            r11 = 0
            r0.set(r12, r11, r8, r5)
            r1.set(r12, r10, r8, r9)
            r7.drawBitmap(r2, r0, r1, r3)
            r12 = r8
            r13 = r9
            r15 = r10
        L_0x006e:
            int r14 = r4 + -1
            if (r8 != r14) goto L_0x007d
            int r14 = r8 + 1
            r0.set(r12, r11, r14, r5)
            r1.set(r12, r10, r14, r9)
            r7.drawBitmap(r2, r0, r1, r3)
        L_0x007d:
            int r8 = r8 + 1
            r11 = r13
            r10 = r15
            goto L_0x001c
        L_0x0082:
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.PanoramaProcessor.createProjectedBitmap(android.graphics.Rect, android.graphics.Rect, android.graphics.Bitmap, android.graphics.Paint, int, int, double, int):android.graphics.Bitmap");
    }

    private void renderPanoramaImage(int i, int i2, Rect rect, Rect rect2, Bitmap bitmap, Paint paint, int i3, int i4, int i5, int i6, int i7, Bitmap bitmap2, Canvas canvas, int i8, int i9, int i10, int i11, int i12, int i13, int i14, double d, long j) {
        int i15;
        Canvas canvas2;
        Paint paint2;
        int i16 = i;
        Rect rect3 = rect;
        Rect rect4 = rect2;
        Paint paint3 = paint;
        int i17 = i4;
        int i18 = i7;
        Canvas canvas3 = canvas;
        int i19 = i9;
        int i20 = i10;
        int i21 = i11;
        int i22 = i8;
        Bitmap createProjectedBitmap = createProjectedBitmap(rect, rect2, bitmap, paint, i3, i4, d, i14);
        if (i16 <= 0 || i5 <= 0) {
            paint2 = paint;
            canvas2 = canvas;
            i15 = i10;
        } else {
            int blendDimension = getBlendDimension();
            int nextMultiple = nextMultiple(i5 * 2, blendDimension);
            int nextMultiple2 = nextMultiple(i17, blendDimension);
            Bitmap createBitmap = Bitmap.createBitmap(nextMultiple, nextMultiple2, Config.ARGB_8888);
            Canvas canvas4 = new Canvas(createBitmap);
            int i23 = i18 + i12;
            int i24 = i23 - i5;
            rect3.set(i24, 0, i23 + i5, i17);
            rect3.offset(-i22, 0);
            rect4.set(0, 0, nextMultiple, nextMultiple2);
            paint2 = paint;
            canvas4.drawBitmap(bitmap2, rect3, rect4, paint2);
            Bitmap createBitmap2 = Bitmap.createBitmap(nextMultiple, nextMultiple2, Config.ARGB_8888);
            Canvas canvas5 = new Canvas(createBitmap2);
            rect3.set(i18 - i5, 0, i18 + i5, i17);
            i15 = i10;
            rect3.offset(i15, i11);
            int i25 = i9;
            rect4.set(0, -i25, nextMultiple, nextMultiple2 - i25);
            canvas5.drawBitmap(createProjectedBitmap, rect3, rect4, paint2);
            Bitmap blendPyramids = blendPyramids(createBitmap, createBitmap2);
            canvas2 = canvas;
            canvas2.drawBitmap(blendPyramids, (float) (i24 - i8), 0.0f, paint2);
            createBitmap.recycle();
            createBitmap2.recycle();
            blendPyramids.recycle();
        }
        int i26 = i6 + i5;
        int i27 = i16 == 0 ? -i18 : i5;
        if (i16 == i2 - 1) {
            i26 = (i6 + i18) - i15;
        }
        int i28 = i26 - i13;
        rect3.set(i18 + i27, 0, i18 + i28, i17);
        rect3.offset(i15, i11);
        int i29 = i18 + i12;
        int i30 = i9;
        rect4.set((i27 + i29) - i8, -i30, (i29 + i28) - i8, i17 - i30);
        canvas2.drawBitmap(createProjectedBitmap, rect3, rect4, paint2);
        createProjectedBitmap.recycle();
    }

    private float adjustExposuresLocal(List<Bitmap> list, int i, int i2, int i3, long j) {
        List<Bitmap> list2 = list;
        int i4 = i / 10;
        int i5 = (i - i3) / 2;
        ArrayList arrayList = new ArrayList();
        arrayList.add(Float.valueOf(1.0f));
        boolean z = false;
        int i6 = 0;
        float f = 1.0f;
        float f2 = 1.0f;
        float f3 = 1.0f;
        while (i6 < list.size() - 1) {
            Bitmap bitmap = (Bitmap) list2.get(i6);
            int i7 = i6 + 1;
            Bitmap bitmap2 = (Bitmap) list2.get(i7);
            Matrix matrix = new Matrix();
            matrix.postScale(0.5f, 0.5f);
            int i8 = i4 * 2;
            Matrix matrix2 = matrix;
            Bitmap createBitmap = Bitmap.createBitmap(bitmap, (i5 + i3) - i4, 0, i8, i2, matrix2, true);
            Bitmap createBitmap2 = Bitmap.createBitmap(bitmap2, i5 - i4, 0, i8, i2, matrix2, true);
            HistogramInfo histogramInfo = this.hdrProcessor.getHistogramInfo(this.hdrProcessor.computeHistogram(createBitmap, z));
            f3 *= ((float) Math.max(this.hdrProcessor.getHistogramInfo(this.hdrProcessor.computeHistogram(createBitmap2, z)).median_brightness, 1)) / ((float) Math.max(histogramInfo.median_brightness, 1));
            arrayList.add(Float.valueOf(f3));
            f2 = Math.min(f2, f3);
            f = Math.max(f, f3);
            if (createBitmap != list2.get(i6)) {
                createBitmap.recycle();
            }
            if (createBitmap2 != list2.get(i7)) {
                createBitmap2.recycle();
            }
            i6 = i7;
            z = false;
        }
        float f4 = f / f2;
        ArrayList arrayList2 = new ArrayList();
        float f5 = 0.0f;
        float f6 = 0.0f;
        float f7 = 0.0f;
        for (int i9 = 0; i9 < list.size(); i9++) {
            HistogramInfo histogramInfo2 = this.hdrProcessor.getHistogramInfo(this.hdrProcessor.computeHistogram((Bitmap) list2.get(i9), false));
            arrayList2.add(histogramInfo2);
            f6 += (float) histogramInfo2.median_brightness;
            f7 += ((float) histogramInfo2.median_brightness) / ((Float) arrayList.get(i9)).floatValue();
        }
        float size = (f6 / ((float) list.size())) / Math.max(f7 / ((float) list.size()), 1.0f);
        float f8 = 1000.0f;
        for (int i10 = 0; i10 < list.size(); i10++) {
            Bitmap bitmap3 = (Bitmap) list2.get(i10);
            HistogramInfo histogramInfo3 = (HistogramInfo) arrayList2.get(i10);
            int min = Math.min(255, (int) (((((float) histogramInfo3.median_brightness) * size) / ((Float) arrayList.get(i10)).floatValue()) + 0.1f));
            float f9 = (float) min;
            f8 = Math.min(f8, f9 / ((float) histogramInfo3.median_brightness));
            f5 = Math.max(f5, f9 / ((float) histogramInfo3.median_brightness));
            int i11 = (int) ((((float) histogramInfo3.median_brightness) * 2.0f) + 0.5f);
            this.hdrProcessor.brightenImage(bitmap3, histogramInfo3.median_brightness, histogramInfo3.max_brightness, Math.min(Math.max(min, (int) ((((float) histogramInfo3.median_brightness) * 0.5f) + 0.5f)), i11));
        }
        return f4;
    }

    private void adjustExposures(List<Bitmap> list, long j) {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        float f = 0.0f;
        float f2 = 0.0f;
        for (int i = 0; i < list.size(); i++) {
            HistogramInfo histogramInfo = this.hdrProcessor.getHistogramInfo(this.hdrProcessor.computeHistogram((Bitmap) list.get(i), false));
            arrayList.add(histogramInfo);
            f2 += (float) histogramInfo.median_brightness;
            arrayList2.add(Integer.valueOf(histogramInfo.median_brightness));
        }
        int size = (int) ((f2 / ((float) list.size())) + 0.1f);
        float f3 = 1000.0f;
        for (int i2 = 0; i2 < list.size(); i2++) {
            Bitmap bitmap = (Bitmap) list.get(i2);
            HistogramInfo histogramInfo2 = (HistogramInfo) arrayList.get(i2);
            float f4 = (float) size;
            f3 = Math.min(f3, f4 / ((float) histogramInfo2.median_brightness));
            f = Math.max(f, f4 / ((float) histogramInfo2.median_brightness));
            int i3 = (int) ((((float) histogramInfo2.median_brightness) * 1.5f) + 0.5f);
            this.hdrProcessor.brightenImage(bitmap, histogramInfo2.median_brightness, histogramInfo2.max_brightness, Math.min(Math.max(size, (int) (((((float) histogramInfo2.median_brightness) * 2.0f) / 3.0f) + 0.5f)), i3));
        }
    }

    private void computePanoramaTransforms(List<Matrix> list, List<Integer> list2, List<Integer> list3, List<Bitmap> list4, int i, int i2, int i3, int i4, int i5, long j) throws PanoramaProcessorException {
        int i6;
        int i7;
        int i8;
        int i9;
        List<Bitmap> list5 = list4;
        int i10 = i2;
        int i11 = i4;
        Matrix matrix = new Matrix();
        ArrayList arrayList = new ArrayList();
        int i12 = 0;
        int i13 = 0;
        while (i12 < list4.size()) {
            if (i12 > 0) {
                ArrayList<Bitmap> arrayList2 = new ArrayList<>();
                float f = (float) i10;
                float f2 = f / 520.0f;
                int i14 = 0;
                int i15 = 1;
                while (true) {
                    if (i14 > 4) {
                        i9 = i12;
                        break;
                    }
                    float f3 = (float) i15;
                    i9 = i12;
                    double d = (double) (f3 / f2);
                    if (d >= 0.949999988079071d && d <= 1.0499999523162842d) {
                        f2 = f3;
                        break;
                    }
                    i14++;
                    i15 *= 2;
                    i12 = i9;
                }
                int i16 = (i10 * 3) / 4;
                Matrix matrix2 = new Matrix();
                float f4 = 1.0f / f2;
                matrix2.postScale(f4, f4);
                i7 = i9;
                int i17 = 0 + i3;
                int i18 = i17 - i5;
                int i19 = (i10 - i16) / 2;
                int i20 = i5 * 2;
                int i21 = i16;
                Matrix matrix3 = matrix2;
                arrayList2.add(Bitmap.createBitmap((Bitmap) list5.get(i7), i18, i19, i20, i21, matrix3, true));
                arrayList2.add(Bitmap.createBitmap((Bitmap) list5.get(i7 - 1), (i17 + i11) - i5, i19, i20, i21, matrix3, true));
                AutoAlignmentByFeatureResult autoAlignmentByFeature = autoAlignmentByFeature(((Bitmap) arrayList2.get(0)).getWidth(), ((Bitmap) arrayList2.get(0)).getHeight(), arrayList2, i7);
                int i22 = autoAlignmentByFeature.offset_x;
                int i23 = autoAlignmentByFeature.offset_y;
                double d2 = (double) autoAlignmentByFeature.rotation;
                float f5 = autoAlignmentByFeature.y_scale;
                int i24 = (int) (((float) i22) * f2);
                int i25 = (int) (((float) i23) * f2);
                for (Bitmap recycle : arrayList2) {
                    recycle.recycle();
                }
                arrayList2.clear();
                Matrix matrix4 = new Matrix();
                matrix4.postRotate((float) Math.toDegrees(d2), (float) i18, 0.0f);
                matrix4.postScale(1.0f, f5);
                matrix4.postTranslate((float) i24, (float) i25);
                matrix.preTranslate((float) i11, 0.0f);
                matrix.postTranslate((float) (-i11), 0.0f);
                matrix.preConcat(matrix4);
                float f6 = ((float) i) / 2.0f;
                i6 = 0;
                float[] fArr = {f6, f / 2.0f};
                matrix.mapPoints(fArr);
                i8 = -((int) (fArr[0] - f6));
            } else {
                i6 = 0;
                int i26 = i;
                i7 = i12;
                i8 = 0;
            }
            list2.add(Integer.valueOf(i8));
            arrayList.add(Integer.valueOf(i6));
            list3.add(Integer.valueOf(i13));
            list.add(new Matrix(matrix));
            i13 += i11;
            i10 = i2;
            i12 = i7 + 1;
            list5 = list4;
        }
    }

    private void adjustPanoramaTransforms(List<Bitmap> list, List<Matrix> list2, int i, int i2, int i3, int i4) {
        List<Matrix> list3 = list2;
        float[] fArr = new float[9];
        float f = 1000.0f;
        float f2 = -1000.0f;
        for (int i5 = 0; i5 < list.size(); i5++) {
            ((Matrix) list2.get(i5)).getValues(fArr);
            float degrees = (float) Math.toDegrees(Math.atan2((double) fArr[1], (double) fArr[0]));
            f = Math.min(f, degrees);
            f2 = Math.max(f2, degrees);
        }
        float f3 = ((float) i4) / 2.0f;
        float[] fArr2 = {0.0f, f3};
        ((Matrix) list2.get(0)).mapPoints(fArr2);
        float f4 = fArr2[0];
        float f5 = fArr2[1];
        fArr2[0] = ((float) i3) - 1.0f;
        fArr2[1] = f3;
        ((Matrix) list2.get(list2.size() - 1)).mapPoints(fArr2);
        float min = Math.min(Math.max(-((float) Math.toDegrees(Math.atan2((double) (fArr2[1] - f5), (double) ((fArr2[0] + ((float) ((list2.size() - 1) * i2))) - f4)))), f), f2);
        for (int i6 = 0; i6 < list.size(); i6++) {
            ((Matrix) list2.get(i6)).postRotate(min, (((float) i) / 2.0f) - ((float) (i6 * i2)), f3);
            ((Matrix) list2.get(i6)).getValues(fArr);
            Math.toDegrees(Math.atan2((double) fArr[1], (double) fArr[0]));
        }
    }

    private void renderPanorama(List<Bitmap> list, int i, int i2, List<Matrix> list2, List<Integer> list3, List<Integer> list4, int i3, int i4, int i5, Bitmap bitmap, int i6, int i7, double d, long j) {
        int i8;
        int i9;
        int i10;
        int i11 = i;
        int i12 = i2;
        List<Matrix> list5 = list2;
        List<Integer> list6 = list3;
        Rect rect = new Rect();
        Rect rect2 = new Rect();
        Paint paint = new Paint(2);
        Canvas canvas = new Canvas(bitmap);
        int i13 = 0;
        while (i13 < list.size()) {
            Bitmap bitmap2 = (Bitmap) list.get(i13);
            int intValue = ((Integer) list6.get(i13)).intValue();
            int intValue2 = ((Integer) list4.get(i13)).intValue();
            int i14 = -intValue;
            if (i13 != 0) {
                int intValue3 = ((Integer) list6.get(i13 - 1)).intValue();
                i10 = -intValue3;
                i8 = intValue - intValue3;
                i9 = intValue2 - intValue3;
            } else {
                i8 = intValue;
                i9 = intValue2;
                i10 = 0;
            }
            if (i10 != 0) {
                float f = ((float) i11) / 2.0f;
                float[] fArr = {f, ((float) i12) / 2.0f};
                ((Matrix) list5.get(i13)).mapPoints(fArr);
                int i15 = (int) (fArr[0] - f);
                int i16 = -i10;
                if (i13 == list.size() - 1 && i15 < 0 && i16 + i15 > 0) {
                    i16 = -i15;
                }
                ((Matrix) list5.get(i13)).postTranslate((float) i16, 0.0f);
                i14 += i16;
                i10 += i16;
            }
            int i17 = i14;
            int i18 = i10;
            Bitmap createBitmap = Bitmap.createBitmap(i11, i12, Config.ARGB_8888);
            Bitmap bitmap3 = createBitmap;
            Canvas canvas2 = new Canvas(createBitmap);
            canvas2.save();
            canvas2.setMatrix((Matrix) list5.get(i13));
            canvas2.drawBitmap(bitmap2, 0.0f, 0.0f, paint);
            canvas2.restore();
            int i19 = i13;
            Canvas canvas3 = canvas;
            Bitmap bitmap4 = createBitmap;
            Paint paint2 = paint;
            renderPanoramaImage(i13, list.size(), rect, rect2, bitmap3, paint, i, i2, i3, i4, i5, bitmap, canvas3, i6, i7, i18, 0, i9, i8, i17, d, j);
            bitmap4.recycle();
            i13 = i19 + 1;
            i11 = i;
            i12 = i2;
            list5 = list2;
            list6 = list3;
            Bitmap bitmap5 = bitmap;
            canvas = canvas3;
            paint = paint2;
        }
    }

    public Bitmap panorama(List<Bitmap> list, float f, float f2, boolean z) throws PanoramaProcessorException {
        int i;
        int i2;
        int i3;
        ArrayList arrayList;
        int i4;
        Bitmap bitmap;
        List<Bitmap> list2 = list;
        char c = 0;
        int width = ((Bitmap) list2.get(0)).getWidth();
        int height = ((Bitmap) list2.get(0)).getHeight();
        int i5 = 1;
        while (i5 < list.size()) {
            Bitmap bitmap2 = (Bitmap) list2.get(i5);
            if (bitmap2.getWidth() == width && bitmap2.getHeight() == height) {
                i5++;
            } else {
                Log.e(TAG, "bitmaps not of equal sizes");
                throw new PanoramaProcessorException(1);
            }
        }
        float f3 = (float) width;
        int i6 = (int) (f3 / f);
        double radians = Math.toRadians((double) f2);
        int i7 = (width - i6) / 2;
        int nextMultiple = nextMultiple((int) ((f3 / 6.1f) + 0.5f), getBlendDimension() / 2);
        int i8 = width / 10;
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        ArrayList arrayList4 = new ArrayList();
        int i9 = width;
        ArrayList arrayList5 = arrayList2;
        int i10 = i6;
        float f4 = f3;
        int i11 = height;
        computePanoramaTransforms(arrayList2, arrayList3, arrayList4, list, i9, height, i7, i6, i8, 0);
        int size = (list.size() * i10) + (i7 * 2);
        List<Bitmap> list3 = list;
        int i12 = i10;
        adjustPanoramaTransforms(list3, arrayList5, size, i12, i9, i11);
        float adjustExposuresLocal = adjustExposuresLocal(list3, width, i11, i12, 0);
        if (z) {
            int i13 = width - 1;
            i4 = i11;
            int i14 = i4 - 1;
            int i15 = i13;
            int i16 = 0;
            int i17 = 0;
            int i18 = 0;
            while (i16 < list.size()) {
                float[] fArr = new float[8];
                fArr[c] = 0.0f;
                fArr[1] = 0.0f;
                float f5 = f4 - 1.0f;
                fArr[2] = f5;
                fArr[3] = 0.0f;
                fArr[4] = 0.0f;
                float f6 = ((float) i4) - 1.0f;
                fArr[5] = f6;
                fArr[6] = f5;
                fArr[7] = f6;
                ArrayList arrayList6 = arrayList5;
                ((Matrix) arrayList6.get(i16)).mapPoints(fArr);
                i18 = Math.max(Math.max(i18, (int) fArr[1]), (int) fArr[3]);
                i14 = Math.min(Math.min(i14, (int) fArr[5]), (int) fArr[7]);
                if (i16 == 0) {
                    i17 = Math.max(Math.max(i17, (int) fArr[0]), (int) fArr[4]);
                }
                if (i16 == list.size() - 1) {
                    i15 = Math.min(Math.min(i15, (int) fArr[2]), (int) fArr[6]);
                }
                i16++;
                arrayList5 = arrayList6;
                c = 0;
            }
            arrayList = arrayList5;
            size = (size - (i13 - i15)) - i17;
            double d = (double) (width / 2);
            Double.isNaN(d);
            float cos = (float) Math.cos((double) (((float) (d * radians)) / f4));
            float f7 = ((float) i4) / 2.0f;
            int i19 = (int) (((((float) i18) - f7) * cos) + f7 + 0.5f);
            i3 = (((int) ((f7 + (cos * (((float) i14) - f7))) + 0.5f)) - i19) + 1;
            i = i19;
            i2 = i17;
        } else {
            arrayList = arrayList5;
            i4 = i11;
            i3 = i4;
            i2 = 0;
            i = 0;
        }
        Bitmap createBitmap = Bitmap.createBitmap(size, i3, Config.ARGB_8888);
        List<Bitmap> list4 = list;
        Bitmap bitmap3 = createBitmap;
        renderPanorama(list4, width, i4, arrayList, arrayList3, arrayList4, nextMultiple, i10, i7, createBitmap, i2, i, radians, 0);
        for (Bitmap recycle : list) {
            recycle.recycle();
        }
        list.clear();
        if (adjustExposuresLocal >= 3.0f) {
            bitmap = bitmap3;
            Allocation createFromBitmap = Allocation.createFromBitmap(this.f16rs, bitmap);
            this.hdrProcessor.adjustHistogram(createFromBitmap, createFromBitmap, bitmap.getWidth(), bitmap.getHeight(), 0.25f, 1, true, 0);
            createFromBitmap.copyTo(bitmap);
            createFromBitmap.destroy();
        } else {
            bitmap = bitmap3;
        }
        freeScripts();
        return bitmap;
    }
}
