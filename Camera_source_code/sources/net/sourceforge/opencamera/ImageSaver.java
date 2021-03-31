package net.sourceforge.opencamera;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.ParcelFileDescriptor;
import android.renderscript.Allocation;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import net.sourceforge.opencamera.HDRProcessor.AvgData;
import net.sourceforge.opencamera.HDRProcessor.DROTonemappingAlgorithm;
import net.sourceforge.opencamera.HDRProcessor.TonemappingAlgorithm;
import net.sourceforge.opencamera.cameracontroller.RawImage;
import org.xmlpull.v1.XmlSerializer;

public class ImageSaver extends Thread {
    private static final String TAG = "ImageSaver";
    private static final String TAG_DATETIME_DIGITIZED = "DateTimeDigitized";
    private static final String TAG_DATETIME_ORIGINAL = "DateTimeOriginal";
    private static final String TAG_GPS_IMG_DIRECTION = "GPSImgDirection";
    private static final String TAG_GPS_IMG_DIRECTION_REF = "GPSImgDirectionRef";
    private static final String gyro_info_camera_view_angle_x_tag = "camera_view_angle_x";
    private static final String gyro_info_camera_view_angle_y_tag = "camera_view_angle_y";
    private static final String gyro_info_doc_tag = "open_camera_gyro_info";
    private static final String gyro_info_image_tag = "image";
    private static final String gyro_info_panorama_pics_per_screen_tag = "panorama_pics_per_screen";
    private static final String gyro_info_vector_right_type = "X";
    private static final String gyro_info_vector_screen_type = "Z";
    private static final String gyro_info_vector_tag = "vector";
    private static final String gyro_info_vector_up_type = "Y";
    private static final int queue_cost_dng_c = 6;
    private static final int queue_cost_jpeg_c = 1;
    public static volatile boolean test_small_queue_size;
    private final HDRProcessor hdrProcessor;
    /* access modifiers changed from: private */
    public final MainActivity main_activity;
    private int n_images_to_save = 0;
    private int n_real_images_to_save = 0;

    /* renamed from: p */
    private final Paint f13p = new Paint();
    private final PanoramaProcessor panoramaProcessor;
    private Request pending_image_average_request = null;
    private final BlockingQueue<Request> queue;
    private final int queue_capacity;
    public volatile boolean test_queue_blocked;
    public volatile boolean test_slow_saving;

    /* renamed from: net.sourceforge.opencamera.ImageSaver$4 */
    static /* synthetic */ class C02404 {

        /* renamed from: $SwitchMap$net$sourceforge$opencamera$ImageSaver$Request$ImageFormat */
        static final /* synthetic */ int[] f14x15fcad09 = new int[ImageFormat.values().length];
        static final /* synthetic */ int[] $SwitchMap$net$sourceforge$opencamera$ImageSaver$Request$Type = new int[Type.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|5|6|7|9|10|11|12|13|14|16) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x0032 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x003c */
        static {
            /*
                net.sourceforge.opencamera.ImageSaver$Request$ImageFormat[] r0 = net.sourceforge.opencamera.ImageSaver.Request.ImageFormat.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f14x15fcad09 = r0
                r0 = 1
                int[] r1 = f14x15fcad09     // Catch:{ NoSuchFieldError -> 0x0014 }
                net.sourceforge.opencamera.ImageSaver$Request$ImageFormat r2 = net.sourceforge.opencamera.ImageSaver.Request.ImageFormat.WEBP     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r1[r2] = r0     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                r1 = 2
                int[] r2 = f14x15fcad09     // Catch:{ NoSuchFieldError -> 0x001f }
                net.sourceforge.opencamera.ImageSaver$Request$ImageFormat r3 = net.sourceforge.opencamera.ImageSaver.Request.ImageFormat.PNG     // Catch:{ NoSuchFieldError -> 0x001f }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2[r3] = r1     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                net.sourceforge.opencamera.ImageSaver$Request$Type[] r2 = net.sourceforge.opencamera.ImageSaver.Request.Type.values()
                int r2 = r2.length
                int[] r2 = new int[r2]
                $SwitchMap$net$sourceforge$opencamera$ImageSaver$Request$Type = r2
                int[] r2 = $SwitchMap$net$sourceforge$opencamera$ImageSaver$Request$Type     // Catch:{ NoSuchFieldError -> 0x0032 }
                net.sourceforge.opencamera.ImageSaver$Request$Type r3 = net.sourceforge.opencamera.ImageSaver.Request.Type.RAW     // Catch:{ NoSuchFieldError -> 0x0032 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0032 }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x0032 }
            L_0x0032:
                int[] r0 = $SwitchMap$net$sourceforge$opencamera$ImageSaver$Request$Type     // Catch:{ NoSuchFieldError -> 0x003c }
                net.sourceforge.opencamera.ImageSaver$Request$Type r2 = net.sourceforge.opencamera.ImageSaver.Request.Type.JPEG     // Catch:{ NoSuchFieldError -> 0x003c }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x003c }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x003c }
            L_0x003c:
                int[] r0 = $SwitchMap$net$sourceforge$opencamera$ImageSaver$Request$Type     // Catch:{ NoSuchFieldError -> 0x0047 }
                net.sourceforge.opencamera.ImageSaver$Request$Type r1 = net.sourceforge.opencamera.ImageSaver.Request.Type.DUMMY     // Catch:{ NoSuchFieldError -> 0x0047 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0047 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0047 }
            L_0x0047:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.ImageSaver.C02404.<clinit>():void");
        }
    }

    public static class GyroDebugInfo {
        public final List<GyroImageDebugInfo> image_info = new ArrayList();

        public static class GyroImageDebugInfo {
            public float[] vectorRight;
            public float[] vectorScreen;
            public float[] vectorUp;
        }
    }

    private static class LoadBitmapThread extends Thread {
        Bitmap bitmap;
        final byte[] jpeg;
        final Options options;

        LoadBitmapThread(Options options2, byte[] bArr) {
            this.options = options2;
            this.jpeg = bArr;
        }

        public void run() {
            byte[] bArr = this.jpeg;
            this.bitmap = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, this.options);
        }
    }

    private static class PostProcessBitmapResult {
        final Bitmap bitmap;
        final File exifTempFile;

        PostProcessBitmapResult(Bitmap bitmap2, File file) {
            this.bitmap = bitmap2;
            this.exifTempFile = file;
        }
    }

    static class Request {
        float camera_view_angle_x;
        float camera_view_angle_y;
        final int color;
        final Date current_date;
        final String custom_tag_artist;
        final String custom_tag_copyright;
        boolean do_auto_stabilise;
        final long exposure_time;
        final int font_size;
        final boolean force_suffix;
        final double geo_direction;
        final List<float[]> gyro_rotation_matrix;
        final boolean image_capture_intent;
        final Uri image_capture_intent_uri;
        ImageFormat image_format;
        int image_quality;
        final boolean is_front_facing;
        final int iso;
        final List<byte[]> jpeg_images;
        final double level_angle;
        final Location location;
        boolean mirror;
        final boolean panorama_crop;
        boolean panorama_dir_left_to_right;
        final String pref_style;
        final String preference_hdr_contrast_enhancement;
        String preference_stamp;
        final String preference_stamp_dateformat;
        final String preference_stamp_geo_address;
        final String preference_stamp_gpsformat;
        final String preference_stamp_timeformat;
        String preference_textstamp;
        final String preference_units_distance;
        final ProcessType process_type;
        final RawImage raw_image;
        final int sample_factor;
        final SaveBase save_base;
        final boolean store_geo_direction;
        final boolean store_location;
        final int suffix_offset;
        final Type type;
        final boolean using_camera2;
        final float zoom_factor;

        enum ImageFormat {
            STD,
            WEBP,
            PNG
        }

        enum ProcessType {
            NORMAL,
            HDR,
            AVERAGE,
            PANORAMA
        }

        enum SaveBase {
            SAVEBASE_NONE,
            SAVEBASE_FIRST,
            SAVEBASE_ALL,
            SAVEBASE_ALL_PLUS_DEBUG
        }

        enum Type {
            JPEG,
            RAW,
            DUMMY
        }

        Request(Type type2, ProcessType processType, boolean z, int i, SaveBase saveBase, List<byte[]> list, RawImage rawImage, boolean z2, Uri uri, boolean z3, ImageFormat imageFormat, int i2, boolean z4, double d, List<float[]> list2, boolean z5, boolean z6, Date date, String str, int i3, long j, float f, String str2, String str3, int i4, int i5, String str4, String str5, String str6, String str7, String str8, String str9, boolean z7, boolean z8, Location location2, boolean z9, double d2, String str10, String str11, int i6) {
            this.type = type2;
            this.process_type = processType;
            this.force_suffix = z;
            this.suffix_offset = i;
            this.save_base = saveBase;
            this.jpeg_images = list;
            this.raw_image = rawImage;
            this.image_capture_intent = z2;
            this.image_capture_intent_uri = uri;
            this.using_camera2 = z3;
            this.image_format = imageFormat;
            this.image_quality = i2;
            this.do_auto_stabilise = z4;
            this.level_angle = d;
            this.gyro_rotation_matrix = list2;
            this.is_front_facing = z5;
            this.mirror = z6;
            this.current_date = date;
            this.preference_hdr_contrast_enhancement = str;
            this.iso = i3;
            this.exposure_time = j;
            this.zoom_factor = f;
            this.preference_stamp = str2;
            this.preference_textstamp = str3;
            this.font_size = i4;
            this.color = i5;
            this.pref_style = str4;
            this.preference_stamp_dateformat = str5;
            this.preference_stamp_timeformat = str6;
            this.preference_stamp_gpsformat = str7;
            this.preference_stamp_geo_address = str8;
            this.preference_units_distance = str9;
            this.panorama_crop = z7;
            this.store_location = z8;
            this.location = location2;
            this.store_geo_direction = z9;
            this.geo_direction = d2;
            this.custom_tag_artist = str10;
            this.custom_tag_copyright = str11;
            this.sample_factor = i6;
        }

        /* access modifiers changed from: 0000 */
        public Request copy() {
            Request request = new Request(this.type, this.process_type, this.force_suffix, this.suffix_offset, this.save_base, this.jpeg_images, this.raw_image, this.image_capture_intent, this.image_capture_intent_uri, this.using_camera2, this.image_format, this.image_quality, this.do_auto_stabilise, this.level_angle, this.gyro_rotation_matrix, this.is_front_facing, this.mirror, this.current_date, this.preference_hdr_contrast_enhancement, this.iso, this.exposure_time, this.zoom_factor, this.preference_stamp, this.preference_textstamp, this.font_size, this.color, this.pref_style, this.preference_stamp_dateformat, this.preference_stamp_timeformat, this.preference_stamp_gpsformat, this.preference_stamp_geo_address, this.preference_units_distance, this.panorama_crop, this.store_location, this.location, this.store_geo_direction, this.geo_direction, this.custom_tag_artist, this.custom_tag_copyright, this.sample_factor);
            return request;
        }
    }

    public static int computeRequestCost(boolean z, int i) {
        return z ? i * 6 : i * 1;
    }

    private boolean needGPSTimestampHack(boolean z, boolean z2, boolean z3) {
        if (!z || !z2) {
            return false;
        }
        return z3;
    }

    ImageSaver(MainActivity mainActivity) {
        this.main_activity = mainActivity;
        this.queue_capacity = computeQueueSize(((ActivityManager) mainActivity.getSystemService("activity")).getLargeMemoryClass());
        this.queue = new ArrayBlockingQueue(this.queue_capacity);
        this.hdrProcessor = new HDRProcessor(mainActivity, mainActivity.is_test);
        this.panoramaProcessor = new PanoramaProcessor(mainActivity, this.hdrProcessor);
        this.f13p.setAntiAlias(true);
    }

    public int getQueueSize() {
        return this.queue_capacity;
    }

    public static int computeQueueSize(int i) {
        if (test_small_queue_size) {
            i = 0;
        }
        if (i >= 512) {
            return 34;
        }
        if (i >= 256) {
            return 12;
        }
        return i >= 128 ? 8 : 6;
    }

    /* access modifiers changed from: 0000 */
    public int computePhotoCost(int i, int i2) {
        int computeRequestCost = i > 0 ? computeRequestCost(true, i) + 0 : 0;
        return i2 > 0 ? computeRequestCost + computeRequestCost(false, i2) : computeRequestCost;
    }

    /* access modifiers changed from: 0000 */
    public boolean queueWouldBlock(int i, int i2) {
        return queueWouldBlock(computePhotoCost(i, i2));
    }

    /* access modifiers changed from: 0000 */
    public synchronized boolean queueWouldBlock(int i) {
        if (this.n_images_to_save == 0) {
            return false;
        }
        if (this.n_images_to_save + i > this.queue_capacity + 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: 0000 */
    public int getMaxDNG() {
        return ((this.queue_capacity + 1) / 6) + 1;
    }

    public synchronized int getNImagesToSave() {
        return this.n_images_to_save;
    }

    public synchronized int getNRealImagesToSave() {
        return this.n_real_images_to_save;
    }

    /* access modifiers changed from: 0000 */
    public void onDestroy() {
        PanoramaProcessor panoramaProcessor2 = this.panoramaProcessor;
        if (panoramaProcessor2 != null) {
            panoramaProcessor2.onDestroy();
        }
        HDRProcessor hDRProcessor = this.hdrProcessor;
        if (hDRProcessor != null) {
            hDRProcessor.onDestroy();
        }
    }

    public void run() {
        while (true) {
            try {
                Request request = (Request) this.queue.take();
                int i = C02404.$SwitchMap$net$sourceforge$opencamera$ImageSaver$Request$Type[request.type.ordinal()];
                if (i == 1) {
                    saveImageNowRaw(request);
                } else if (i == 2) {
                    saveImageNow(request);
                }
                if (this.test_slow_saving) {
                    Thread.sleep(2000);
                }
                synchronized (this) {
                    this.n_images_to_save--;
                    if (request.type != Type.DUMMY) {
                        this.n_real_images_to_save--;
                    }
                    notifyAll();
                    this.main_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            ImageSaver.this.main_activity.imageQueueChanged();
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean saveImageJpeg(boolean z, boolean z2, boolean z3, int i, boolean z4, List<byte[]> list, boolean z5, Uri uri, boolean z6, ImageFormat imageFormat, int i2, boolean z7, double d, boolean z8, boolean z9, Date date, String str, int i3, long j, float f, String str2, String str3, int i4, int i5, String str4, String str5, String str6, String str7, String str8, String str9, boolean z10, boolean z11, Location location, boolean z12, double d2, String str10, String str11, int i6) {
        return saveImage(z, false, z2, z3, i, z4, list, null, z5, uri, z6, imageFormat, i2, z7, d, z8, z9, date, str, i3, j, f, str2, str3, i4, i5, str4, str5, str6, str7, str8, str9, z10, z11, location, z12, d2, str10, str11, i6);
    }

    /* access modifiers changed from: 0000 */
    public boolean saveImageRaw(boolean z, boolean z2, int i, RawImage rawImage, Date date) {
        return saveImage(z, true, false, z2, i, false, null, rawImage, false, null, false, ImageFormat.STD, 0, false, 0.0d, false, false, date, null, 0, 0, 1.0f, null, null, 0, 0, null, null, null, null, null, null, false, false, null, false, 0.0d, null, null, 1);
    }

    /* access modifiers changed from: 0000 */
    public void startImageBatch(boolean z, ProcessType processType, SaveBase saveBase, boolean z2, Uri uri, boolean z3, ImageFormat imageFormat, int i, boolean z4, double d, boolean z5, boolean z6, boolean z7, Date date, int i2, long j, float f, String str, String str2, int i3, int i4, String str3, String str4, String str5, String str6, String str7, String str8, boolean z8, boolean z9, Location location, boolean z10, double d2, String str9, String str10, int i5) {
        Request request = r0;
        Request request2 = new Request(Type.JPEG, processType, false, 0, saveBase, new ArrayList(), null, z2, uri, z3, imageFormat, i, z4, d, z5 ? new ArrayList() : null, z6, z7, date, null, i2, j, f, str, str2, i3, i4, str3, str4, str5, str6, str7, str8, z8, z9, location, z10, d2, str9, str10, i5);
        this.pending_image_average_request = request;
    }

    /* access modifiers changed from: 0000 */
    public void addImageBatch(byte[] bArr, float[] fArr) {
        Request request = this.pending_image_average_request;
        if (request == null) {
            Log.e(TAG, "addImageBatch called but no pending_image_average_request");
            return;
        }
        request.jpeg_images.add(bArr);
        if (fArr != null) {
            float[] fArr2 = new float[fArr.length];
            System.arraycopy(fArr, 0, fArr2, 0, fArr.length);
            this.pending_image_average_request.gyro_rotation_matrix.add(fArr2);
        }
    }

    /* access modifiers changed from: 0000 */
    public Request getImageBatchRequest() {
        return this.pending_image_average_request;
    }

    /* access modifiers changed from: 0000 */
    public void finishImageBatch(boolean z) {
        Request request = this.pending_image_average_request;
        if (request != null) {
            if (z) {
                addRequest(this.pending_image_average_request, computeRequestCost(false, request.jpeg_images.size()));
            } else {
                waitUntilDone();
                saveImageNow(this.pending_image_average_request);
            }
            this.pending_image_average_request = null;
        }
    }

    /* access modifiers changed from: 0000 */
    public void flushImageBatch() {
        this.pending_image_average_request = null;
    }

    private boolean saveImage(boolean z, boolean z2, boolean z3, boolean z4, int i, boolean z5, List<byte[]> list, RawImage rawImage, boolean z6, Uri uri, boolean z7, ImageFormat imageFormat, int i2, boolean z8, double d, boolean z9, boolean z10, Date date, String str, int i3, long j, float f, String str2, String str3, int i4, int i5, String str4, String str5, String str6, String str7, String str8, String str9, boolean z11, boolean z12, Location location, boolean z13, double d2, String str10, String str11, int i6) {
        boolean z14 = z2;
        Request request = r2;
        Request request2 = new Request(z14 ? Type.RAW : Type.JPEG, z3 ? ProcessType.HDR : ProcessType.NORMAL, z4, i, z5 ? SaveBase.SAVEBASE_ALL : SaveBase.SAVEBASE_NONE, list, rawImage, z6, uri, z7, imageFormat, i2, z8, d, null, z9, z10, date, str, i3, j, f, str2, str3, i4, i5, str4, str5, str6, str7, str8, str9, z11, z12, location, z13, d2, str10, str11, i6);
        if (z) {
            addRequest(request, computeRequestCost(z14, z14 ? 1 : request.jpeg_images.size()));
            return true;
        }
        Request request3 = request;
        waitUntilDone();
        if (z14) {
            return saveImageNowRaw(request3);
        }
        return saveImageNow(request3);
    }

    private void addRequest(Request request, int i) {
        if (VERSION.SDK_INT < 17 || !this.main_activity.isDestroyed()) {
            boolean z = false;
            while (!z) {
                try {
                    synchronized (this) {
                        this.n_images_to_save++;
                        if (request.type != Type.DUMMY) {
                            this.n_real_images_to_save++;
                        }
                        this.main_activity.runOnUiThread(new Runnable() {
                            public void run() {
                                ImageSaver.this.main_activity.imageQueueChanged();
                            }
                        });
                    }
                    if (this.queue.size() + 1 > this.queue_capacity) {
                        String str = TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("ImageSaver thread is going to block, queue already full: ");
                        sb.append(this.queue.size());
                        Log.e(str, sb.toString());
                        this.test_queue_blocked = true;
                    }
                    this.queue.put(request);
                    z = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (i > 0) {
                for (int i2 = 0; i2 < i - 1; i2++) {
                    addDummyRequest();
                }
            }
            return;
        }
        Log.e(TAG, "application is destroyed, image lost!");
    }

    private void addDummyRequest() {
        Request request = r0;
        Request request2 = new Request(Type.DUMMY, ProcessType.NORMAL, false, 0, SaveBase.SAVEBASE_NONE, null, null, false, null, false, ImageFormat.STD, 0, false, 0.0d, null, false, false, null, null, 0, 0, 1.0f, null, null, 0, 0, null, null, null, null, null, null, false, false, null, false, 0.0d, null, null, 1);
        addRequest(request, 1);
    }

    /* access modifiers changed from: 0000 */
    public void waitUntilDone() {
        synchronized (this) {
            while (this.n_images_to_save > 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setBitmapOptionsSampleSize(Options options, int i) {
        if (i > 1) {
            options.inDensity = i;
            options.inTargetDensity = 1;
        }
    }

    private Bitmap loadBitmap(byte[] bArr, boolean z, int i) {
        Options options = new Options();
        options.inMutable = z;
        setBitmapOptionsSampleSize(options, i);
        if (VERSION.SDK_INT <= 19) {
            options.inPurgeable = true;
        }
        Bitmap decodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
        if (decodeByteArray == null) {
            Log.e(TAG, "failed to decode bitmap");
        }
        return decodeByteArray;
    }

    private List<Bitmap> loadBitmaps(List<byte[]> list, int i, int i2) {
        Options options = new Options();
        boolean z = true;
        options.inMutable = true;
        setBitmapOptionsSampleSize(options, i2);
        Options options2 = new Options();
        options2.inMutable = false;
        setBitmapOptionsSampleSize(options2, i2);
        if (VERSION.SDK_INT <= 19) {
            options.inPurgeable = true;
            options2.inPurgeable = true;
        }
        LoadBitmapThread[] loadBitmapThreadArr = new LoadBitmapThread[list.size()];
        int i3 = 0;
        while (i3 < list.size()) {
            loadBitmapThreadArr[i3] = new LoadBitmapThread(i3 == i ? options : options2, (byte[]) list.get(i3));
            i3++;
        }
        for (int i4 = 0; i4 < list.size(); i4++) {
            loadBitmapThreadArr[i4].start();
        }
        int i5 = 0;
        while (i5 < list.size()) {
            try {
                loadBitmapThreadArr[i5].join();
                i5++;
            } catch (InterruptedException e) {
                e.printStackTrace();
                z = false;
            }
        }
        ArrayList arrayList = new ArrayList();
        for (int i6 = 0; i6 < list.size() && z; i6++) {
            Bitmap bitmap = loadBitmapThreadArr[i6].bitmap;
            if (bitmap == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("failed to decode bitmap in thread: ");
                sb.append(i6);
                Log.e(TAG, sb.toString());
                z = false;
            }
            arrayList.add(bitmap);
        }
        if (z) {
            return arrayList;
        }
        for (int i7 = 0; i7 < list.size(); i7++) {
            if (loadBitmapThreadArr[i7].bitmap != null) {
                loadBitmapThreadArr[i7].bitmap.recycle();
                loadBitmapThreadArr[i7].bitmap = null;
            }
        }
        arrayList.clear();
        System.gc();
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0042, code lost:
        if (r6 < 16949152) goto L_0x0004;
     */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static float getHDRAlpha(java.lang.String r5, long r6, int r8) {
        /*
            r0 = 0
            r1 = 1
            if (r8 != r1) goto L_0x0006
        L_0x0004:
            r0 = 1
            goto L_0x0045
        L_0x0006:
            r8 = -1
            int r2 = r5.hashCode()
            r3 = -643375150(0xffffffffd9a6dfd2, float:-5.8713674E15)
            r4 = 3
            if (r2 == r3) goto L_0x0030
            r3 = -60559732(0xfffffffffc63ee8c, float:-4.7339588E36)
            if (r2 == r3) goto L_0x0026
            r3 = 1935541158(0x735e03a6, float:1.7589781E31)
            if (r2 == r3) goto L_0x001c
            goto L_0x0039
        L_0x001c:
            java.lang.String r2 = "preference_hdr_contrast_enhancement_smart"
            boolean r5 = r5.equals(r2)
            if (r5 == 0) goto L_0x0039
            r8 = 1
            goto L_0x0039
        L_0x0026:
            java.lang.String r2 = "preference_hdr_contrast_enhancement_off"
            boolean r5 = r5.equals(r2)
            if (r5 == 0) goto L_0x0039
            r8 = 0
            goto L_0x0039
        L_0x0030:
            java.lang.String r2 = "preference_hdr_contrast_enhancement_always"
            boolean r5 = r5.equals(r2)
            if (r5 == 0) goto L_0x0039
            r8 = 3
        L_0x0039:
            if (r8 == 0) goto L_0x0045
            if (r8 == r4) goto L_0x0004
            r2 = 16949152(0x1029fa0, double:8.3739937E-317)
            int r5 = (r6 > r2 ? 1 : (r6 == r2 ? 0 : -1))
            if (r5 >= 0) goto L_0x0045
            goto L_0x0004
        L_0x0045:
            if (r0 == 0) goto L_0x004a
            r5 = 1056964608(0x3f000000, float:0.5)
            goto L_0x004b
        L_0x004a:
            r5 = 0
        L_0x004b:
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.ImageSaver.getHDRAlpha(java.lang.String, long, int):float");
    }

    private void writeGyroDebugXml(Writer writer, Request request) throws IOException {
        Request request2 = request;
        XmlSerializer newSerializer = Xml.newSerializer();
        newSerializer.setOutput(writer);
        char c = 1;
        newSerializer.startDocument("UTF-8", Boolean.valueOf(true));
        String str = gyro_info_doc_tag;
        String str2 = null;
        newSerializer.startTag(null, str);
        StringBuilder sb = new StringBuilder();
        String str3 = BuildConfig.FLAVOR;
        sb.append(str3);
        sb.append(MyApplicationInterface.getPanoramaPicsPerScreen());
        newSerializer.attribute(null, gyro_info_panorama_pics_per_screen_tag, sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append(str3);
        sb2.append(request2.camera_view_angle_x);
        newSerializer.attribute(null, gyro_info_camera_view_angle_x_tag, sb2.toString());
        StringBuilder sb3 = new StringBuilder();
        sb3.append(str3);
        sb3.append(request2.camera_view_angle_y);
        newSerializer.attribute(null, gyro_info_camera_view_angle_y_tag, sb3.toString());
        float[] fArr = new float[3];
        float[] fArr2 = new float[3];
        char c2 = 0;
        int i = 0;
        while (i < request2.gyro_rotation_matrix.size()) {
            String str4 = gyro_info_image_tag;
            newSerializer.startTag(str2, str4);
            StringBuilder sb4 = new StringBuilder();
            sb4.append(str3);
            sb4.append(i);
            newSerializer.attribute(str2, "index", sb4.toString());
            GyroSensor.setVector(fArr, 1.0f, 0.0f, 0.0f);
            GyroSensor.transformVector(fArr2, (float[]) request2.gyro_rotation_matrix.get(i), fArr);
            String str5 = gyro_info_vector_tag;
            newSerializer.startTag(str2, str5);
            String str6 = "type";
            newSerializer.attribute(str2, str6, gyro_info_vector_right_type);
            StringBuilder sb5 = new StringBuilder();
            sb5.append(str3);
            sb5.append(fArr2[c2]);
            String sb6 = sb5.toString();
            String str7 = "x";
            newSerializer.attribute(str2, str7, sb6);
            StringBuilder sb7 = new StringBuilder();
            sb7.append(str3);
            sb7.append(fArr2[c]);
            String sb8 = sb7.toString();
            String str8 = "y";
            newSerializer.attribute(str2, str8, sb8);
            StringBuilder sb9 = new StringBuilder();
            sb9.append(str3);
            sb9.append(fArr2[2]);
            String sb10 = sb9.toString();
            String str9 = "z";
            newSerializer.attribute(str2, str9, sb10);
            newSerializer.endTag(str2, str5);
            GyroSensor.setVector(fArr, 0.0f, 1.0f, 0.0f);
            GyroSensor.transformVector(fArr2, (float[]) request2.gyro_rotation_matrix.get(i), fArr);
            newSerializer.startTag(str2, str5);
            newSerializer.attribute(str2, str6, gyro_info_vector_up_type);
            StringBuilder sb11 = new StringBuilder();
            sb11.append(str3);
            sb11.append(fArr2[0]);
            newSerializer.attribute(str2, str7, sb11.toString());
            StringBuilder sb12 = new StringBuilder();
            sb12.append(str3);
            sb12.append(fArr2[1]);
            str2 = null;
            newSerializer.attribute(null, str8, sb12.toString());
            StringBuilder sb13 = new StringBuilder();
            sb13.append(str3);
            sb13.append(fArr2[2]);
            newSerializer.attribute(null, str9, sb13.toString());
            newSerializer.endTag(null, str5);
            GyroSensor.setVector(fArr, 0.0f, 0.0f, -1.0f);
            GyroSensor.transformVector(fArr2, (float[]) request2.gyro_rotation_matrix.get(i), fArr);
            newSerializer.startTag(null, str5);
            newSerializer.attribute(null, str6, gyro_info_vector_screen_type);
            StringBuilder sb14 = new StringBuilder();
            sb14.append(str3);
            sb14.append(fArr2[0]);
            newSerializer.attribute(null, str7, sb14.toString());
            StringBuilder sb15 = new StringBuilder();
            sb15.append(str3);
            sb15.append(fArr2[1]);
            newSerializer.attribute(null, str8, sb15.toString());
            StringBuilder sb16 = new StringBuilder();
            sb16.append(str3);
            sb16.append(fArr2[2]);
            newSerializer.attribute(null, str9, sb16.toString());
            newSerializer.endTag(null, str5);
            newSerializer.endTag(null, str4);
            i++;
            c = 1;
            c2 = 0;
        }
        newSerializer.endTag(str2, str);
        newSerializer.endDocument();
        newSerializer.flush();
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00fe A[Catch:{ Exception -> 0x0116, all -> 0x0114 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean readGyroDebugXml(java.io.InputStream r13, net.sourceforge.opencamera.ImageSaver.GyroDebugInfo r14) {
        /*
            r0 = 0
            org.xmlpull.v1.XmlPullParser r1 = android.util.Xml.newPullParser()     // Catch:{ Exception -> 0x0116 }
            java.lang.String r2 = "http://xmlpull.org/v1/doc/features.html#process-namespaces"
            r1.setFeature(r2, r0)     // Catch:{ Exception -> 0x0116 }
            r2 = 0
            r1.setInput(r13, r2)     // Catch:{ Exception -> 0x0116 }
            r1.nextTag()     // Catch:{ Exception -> 0x0116 }
            java.lang.String r3 = "open_camera_gyro_info"
            r4 = 2
            r1.require(r4, r2, r3)     // Catch:{ Exception -> 0x0116 }
        L_0x0017:
            r3 = r2
        L_0x0018:
            int r5 = r1.next()     // Catch:{ Exception -> 0x0116 }
            r6 = 1
            if (r5 == r6) goto L_0x010b
            int r5 = r1.getEventType()     // Catch:{ Exception -> 0x0116 }
            java.lang.String r7 = "image"
            r8 = 100313435(0x5faa95b, float:2.3572098E-35)
            r9 = 3
            r10 = -1
            if (r5 == r4) goto L_0x0044
            if (r5 == r9) goto L_0x002f
            goto L_0x0018
        L_0x002f:
            java.lang.String r5 = r1.getName()     // Catch:{ Exception -> 0x0116 }
            int r6 = r5.hashCode()     // Catch:{ Exception -> 0x0116 }
            if (r6 == r8) goto L_0x003a
            goto L_0x0041
        L_0x003a:
            boolean r5 = r5.equals(r7)     // Catch:{ Exception -> 0x0116 }
            if (r5 == 0) goto L_0x0041
            r10 = 0
        L_0x0041:
            if (r10 == 0) goto L_0x0017
            goto L_0x0018
        L_0x0044:
            java.lang.String r5 = r1.getName()     // Catch:{ Exception -> 0x0116 }
            int r11 = r5.hashCode()     // Catch:{ Exception -> 0x0116 }
            r12 = -820387517(0xffffffffcf19e143, float:-2.5816768E9)
            if (r11 == r12) goto L_0x005c
            if (r11 == r8) goto L_0x0054
            goto L_0x0066
        L_0x0054:
            boolean r5 = r5.equals(r7)     // Catch:{ Exception -> 0x0116 }
            if (r5 == 0) goto L_0x0066
            r5 = 0
            goto L_0x0067
        L_0x005c:
            java.lang.String r7 = "vector"
            boolean r5 = r5.equals(r7)     // Catch:{ Exception -> 0x0116 }
            if (r5 == 0) goto L_0x0066
            r5 = 1
            goto L_0x0067
        L_0x0066:
            r5 = -1
        L_0x0067:
            if (r5 == 0) goto L_0x00fe
            if (r5 == r6) goto L_0x006c
            goto L_0x0018
        L_0x006c:
            java.lang.String r5 = "ImageSaver"
            if (r3 != 0) goto L_0x007e
            java.lang.String r14 = "vector tag outside of image tag"
            android.util.Log.e(r5, r14)     // Catch:{ Exception -> 0x0116 }
            r13.close()     // Catch:{ IOException -> 0x0079 }
            goto L_0x007d
        L_0x0079:
            r13 = move-exception
            r13.printStackTrace()
        L_0x007d:
            return r0
        L_0x007e:
            java.lang.String r7 = "type"
            java.lang.String r7 = r1.getAttributeValue(r2, r7)     // Catch:{ Exception -> 0x0116 }
            java.lang.String r8 = "x"
            java.lang.String r8 = r1.getAttributeValue(r2, r8)     // Catch:{ Exception -> 0x0116 }
            java.lang.String r11 = "y"
            java.lang.String r11 = r1.getAttributeValue(r2, r11)     // Catch:{ Exception -> 0x0116 }
            java.lang.String r12 = "z"
            java.lang.String r12 = r1.getAttributeValue(r2, r12)     // Catch:{ Exception -> 0x0116 }
            float[] r9 = new float[r9]     // Catch:{ Exception -> 0x0116 }
            float r8 = java.lang.Float.parseFloat(r8)     // Catch:{ Exception -> 0x0116 }
            r9[r0] = r8     // Catch:{ Exception -> 0x0116 }
            float r8 = java.lang.Float.parseFloat(r11)     // Catch:{ Exception -> 0x0116 }
            r9[r6] = r8     // Catch:{ Exception -> 0x0116 }
            float r8 = java.lang.Float.parseFloat(r12)     // Catch:{ Exception -> 0x0116 }
            r9[r4] = r8     // Catch:{ Exception -> 0x0116 }
            int r8 = r7.hashCode()     // Catch:{ Exception -> 0x0116 }
            switch(r8) {
                case 88: goto L_0x00c6;
                case 89: goto L_0x00bc;
                case 90: goto L_0x00b2;
                default: goto L_0x00b1;
            }     // Catch:{ Exception -> 0x0116 }
        L_0x00b1:
            goto L_0x00cf
        L_0x00b2:
            java.lang.String r8 = "Z"
            boolean r8 = r7.equals(r8)     // Catch:{ Exception -> 0x0116 }
            if (r8 == 0) goto L_0x00cf
            r10 = 2
            goto L_0x00cf
        L_0x00bc:
            java.lang.String r8 = "Y"
            boolean r8 = r7.equals(r8)     // Catch:{ Exception -> 0x0116 }
            if (r8 == 0) goto L_0x00cf
            r10 = 1
            goto L_0x00cf
        L_0x00c6:
            java.lang.String r8 = "X"
            boolean r8 = r7.equals(r8)     // Catch:{ Exception -> 0x0116 }
            if (r8 == 0) goto L_0x00cf
            r10 = 0
        L_0x00cf:
            if (r10 == 0) goto L_0x00fa
            if (r10 == r6) goto L_0x00f6
            if (r10 == r4) goto L_0x00f2
            java.lang.StringBuilder r14 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0116 }
            r14.<init>()     // Catch:{ Exception -> 0x0116 }
            java.lang.String r1 = "unknown type in vector tag: "
            r14.append(r1)     // Catch:{ Exception -> 0x0116 }
            r14.append(r7)     // Catch:{ Exception -> 0x0116 }
            java.lang.String r14 = r14.toString()     // Catch:{ Exception -> 0x0116 }
            android.util.Log.e(r5, r14)     // Catch:{ Exception -> 0x0116 }
            r13.close()     // Catch:{ IOException -> 0x00ed }
            goto L_0x00f1
        L_0x00ed:
            r13 = move-exception
            r13.printStackTrace()
        L_0x00f1:
            return r0
        L_0x00f2:
            r3.vectorScreen = r9     // Catch:{ Exception -> 0x0116 }
            goto L_0x0018
        L_0x00f6:
            r3.vectorUp = r9     // Catch:{ Exception -> 0x0116 }
            goto L_0x0018
        L_0x00fa:
            r3.vectorRight = r9     // Catch:{ Exception -> 0x0116 }
            goto L_0x0018
        L_0x00fe:
            java.util.List<net.sourceforge.opencamera.ImageSaver$GyroDebugInfo$GyroImageDebugInfo> r3 = r14.image_info     // Catch:{ Exception -> 0x0116 }
            net.sourceforge.opencamera.ImageSaver$GyroDebugInfo$GyroImageDebugInfo r5 = new net.sourceforge.opencamera.ImageSaver$GyroDebugInfo$GyroImageDebugInfo     // Catch:{ Exception -> 0x0116 }
            r5.<init>()     // Catch:{ Exception -> 0x0116 }
            r3.add(r5)     // Catch:{ Exception -> 0x0116 }
            r3 = r5
            goto L_0x0018
        L_0x010b:
            r13.close()     // Catch:{ IOException -> 0x010f }
            goto L_0x0113
        L_0x010f:
            r13 = move-exception
            r13.printStackTrace()
        L_0x0113:
            return r6
        L_0x0114:
            r14 = move-exception
            goto L_0x0123
        L_0x0116:
            r14 = move-exception
            r14.printStackTrace()     // Catch:{ all -> 0x0114 }
            r13.close()     // Catch:{ IOException -> 0x011e }
            goto L_0x0122
        L_0x011e:
            r13 = move-exception
            r13.printStackTrace()
        L_0x0122:
            return r0
        L_0x0123:
            r13.close()     // Catch:{ IOException -> 0x0127 }
            goto L_0x012b
        L_0x0127:
            r13 = move-exception
            r13.printStackTrace()
        L_0x012b:
            goto L_0x012d
        L_0x012c:
            throw r14
        L_0x012d:
            goto L_0x012c
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.ImageSaver.readGyroDebugXml(java.io.InputStream, net.sourceforge.opencamera.ImageSaver$GyroDebugInfo):boolean");
    }

    private boolean saveImageNow(Request request) {
        boolean z;
        Uri uri;
        File file;
        OutputStream outputStream;
        Bitmap bitmap;
        Request request2 = request;
        if (request2.type != Type.JPEG) {
            throw new RuntimeException();
        } else if (request2.jpeg_images.size() != 0) {
            ProcessType processType = request2.process_type;
            ProcessType processType2 = ProcessType.AVERAGE;
            String str = "_";
            int i = 4;
            String str2 = TAG;
            if (processType == processType2) {
                saveBaseImages(request2, str);
                this.main_activity.savingImage(true);
                if (VERSION.SDK_INT >= 21) {
                    try {
                        System.currentTimeMillis();
                        int avgSampleSize = this.hdrProcessor.getAvgSampleSize(request2.iso);
                        System.currentTimeMillis();
                        int min = Math.min(4, request2.jpeg_images.size());
                        ArrayList arrayList = new ArrayList();
                        for (int i2 = 0; i2 < min; i2++) {
                            arrayList.add(request2.jpeg_images.get(i2));
                        }
                        List loadBitmaps = loadBitmaps(arrayList, -1, avgSampleSize);
                        Bitmap bitmap2 = (Bitmap) loadBitmaps.get(0);
                        Bitmap bitmap3 = (Bitmap) loadBitmaps.get(1);
                        int width = bitmap2.getWidth();
                        int height = bitmap2.getHeight();
                        System.currentTimeMillis();
                        AvgData processAvg = this.hdrProcessor.processAvg(bitmap2, bitmap3, 1.0f, request2.iso, request2.zoom_factor);
                        if (loadBitmaps != null) {
                            loadBitmaps.set(0, null);
                            loadBitmaps.set(1, null);
                        }
                        Allocation allocation = processAvg.allocation_out;
                        int i3 = 2;
                        while (i3 < request2.jpeg_images.size()) {
                            System.currentTimeMillis();
                            if (i3 < loadBitmaps.size()) {
                                bitmap = (Bitmap) loadBitmaps.get(i3);
                            } else {
                                int min2 = Math.min(i, request2.jpeg_images.size() - i3);
                                ArrayList arrayList2 = new ArrayList();
                                for (int i4 = i3; i4 < i3 + min2; i4++) {
                                    arrayList2.add(request2.jpeg_images.get(i4));
                                }
                                loadBitmaps.addAll(loadBitmaps(arrayList2, -1, avgSampleSize));
                                bitmap = (Bitmap) loadBitmaps.get(i3);
                            }
                            float f = (float) i3;
                            System.currentTimeMillis();
                            this.hdrProcessor.updateAvg(processAvg, width, height, bitmap, f, request2.iso, request2.zoom_factor);
                            if (loadBitmaps != null) {
                                loadBitmaps.set(i3, null);
                            }
                            i3++;
                            i = 4;
                        }
                        System.currentTimeMillis();
                        Bitmap avgBrighten = this.hdrProcessor.avgBrighten(allocation, width, height, request2.iso, request2.exposure_time);
                        processAvg.destroy();
                        System.gc();
                        this.main_activity.savingImage(false);
                        z = saveSingleImageNow(request, (byte[]) request2.jpeg_images.get(0), avgBrighten, "_NR", true, true, true, false);
                        avgBrighten.recycle();
                        System.gc();
                    } catch (HDRProcessorException e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    }
                } else {
                    Log.e(str2, "shouldn't have offered NoiseReduction as an option if not on Android 5");
                    throw new RuntimeException();
                }
            } else {
                String str3 = "UNEQUAL_SIZES";
                if (request2.process_type == ProcessType.HDR) {
                    if (request2.jpeg_images.size() == 1 || request2.jpeg_images.size() == 3) {
                        System.currentTimeMillis();
                        if (request2.jpeg_images.size() > 1) {
                            saveBaseImages(request2, str);
                        }
                        this.main_activity.savingImage(true);
                        List loadBitmaps2 = loadBitmaps(request2.jpeg_images, (request2.jpeg_images.size() - 1) / 2, 1);
                        if (loadBitmaps2 == null) {
                            this.main_activity.savingImage(false);
                            return false;
                        }
                        float hDRAlpha = getHDRAlpha(request2.preference_hdr_contrast_enhancement, request2.exposure_time, loadBitmaps2.size());
                        try {
                            if (VERSION.SDK_INT >= 21) {
                                this.hdrProcessor.processHDR(loadBitmaps2, true, null, true, null, hDRAlpha, 4, true, TonemappingAlgorithm.TONEMAPALGORITHM_REINHARD, DROTonemappingAlgorithm.DROALGORITHM_GAINGAMMA);
                                Bitmap bitmap4 = (Bitmap) loadBitmaps2.get(0);
                                loadBitmaps2.clear();
                                System.gc();
                                this.main_activity.savingImage(false);
                                int size = (request2.jpeg_images.size() - 1) / 2;
                                z = saveSingleImageNow(request, (byte[]) request2.jpeg_images.get(size), bitmap4, request2.jpeg_images.size() == 1 ? "_DRO" : "_HDR", true, true, true, false);
                                bitmap4.recycle();
                                System.gc();
                            } else {
                                Log.e(str2, "shouldn't have offered HDR as an option if not on Android 5");
                                throw new RuntimeException();
                            }
                        } catch (HDRProcessorException e2) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("HDRProcessorException from processHDR: ");
                            sb.append(e2.getCode());
                            Log.e(str2, sb.toString());
                            e2.printStackTrace();
                            if (e2.getCode() == 1) {
                                this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.failed_to_process_hdr);
                                Log.e(str2, str3);
                                loadBitmaps2.clear();
                                System.gc();
                                this.main_activity.savingImage(false);
                                return false;
                            }
                            throw new RuntimeException();
                        }
                    } else {
                        throw new RuntimeException();
                    }
                } else if (request2.process_type == ProcessType.PANORAMA) {
                    if (!request2.image_capture_intent && request2.save_base == SaveBase.SAVEBASE_ALL_PLUS_DEBUG) {
                        try {
                            StringWriter stringWriter = new StringWriter();
                            writeGyroDebugXml(stringWriter, request2);
                            StorageUtils storageUtils = this.main_activity.getStorageUtils();
                            boolean isUsingSAF = storageUtils.isUsingSAF();
                            String str4 = "xml";
                            String str5 = BuildConfig.FLAVOR;
                            if (isUsingSAF) {
                                uri = storageUtils.createOutputMediaFileSAF(4, str5, str4, request2.current_date);
                                file = null;
                            } else {
                                file = storageUtils.createOutputMediaFile(4, str5, str4, request2.current_date);
                                uri = null;
                            }
                            if (file != null) {
                                outputStream = new FileOutputStream(file);
                            } else {
                                outputStream = this.main_activity.getContentResolver().openOutputStream(uri);
                            }
                            outputStream.write(stringWriter.toString().getBytes(Charset.forName("UTF-8")));
                            outputStream.close();
                            if (file != null) {
                                storageUtils.broadcastFile(file, false, false, false);
                            } else {
                                broadcastSAFFile(uri, false);
                            }
                        } catch (IOException e3) {
                            Log.e(str2, "failed to write gyro text file");
                            e3.printStackTrace();
                        } catch (Throwable th) {
                            outputStream.close();
                            throw th;
                        }
                    }
                    saveBaseImages(request2, str);
                    this.main_activity.savingImage(true);
                    System.currentTimeMillis();
                    if (!request2.panorama_dir_left_to_right) {
                        Collections.reverse(request2.jpeg_images);
                        Collections.reverse(request2.gyro_rotation_matrix);
                    }
                    List loadBitmaps3 = loadBitmaps(request2.jpeg_images, -1, 1);
                    if (loadBitmaps3 == null) {
                        this.main_activity.savingImage(false);
                        return false;
                    }
                    File exifTempFile = getExifTempFile((byte[]) request2.jpeg_images.get(0));
                    for (int i5 = 0; i5 < loadBitmaps3.size(); i5++) {
                        loadBitmaps3.set(i5, rotateForExif((Bitmap) loadBitmaps3.get(i5), (byte[]) request2.jpeg_images.get(0), exifTempFile));
                    }
                    try {
                        if (VERSION.SDK_INT >= 21) {
                            Bitmap panorama = this.panoramaProcessor.panorama(loadBitmaps3, MyApplicationInterface.getPanoramaPicsPerScreen(), request2.camera_view_angle_y, request2.panorama_crop);
                            loadBitmaps3.clear();
                            System.gc();
                            this.main_activity.savingImage(false);
                            z = saveSingleImageNow(request, (byte[]) request2.jpeg_images.get(0), panorama, "_PANO", true, true, true, true);
                            panorama.recycle();
                            System.gc();
                            if (exifTempFile != null) {
                                exifTempFile.delete();
                            }
                        } else {
                            Log.e(str2, "shouldn't have offered panorama as an option if not on Android 5");
                            throw new RuntimeException();
                        }
                    } catch (PanoramaProcessorException e4) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("PanoramaProcessorException from panorama: ");
                        sb2.append(e4.getCode());
                        Log.e(str2, sb2.toString());
                        e4.printStackTrace();
                        if (e4.getCode() == 1) {
                            this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.failed_to_process_panorama);
                            Log.e(str2, str3);
                            loadBitmaps3.clear();
                            System.gc();
                            this.main_activity.savingImage(false);
                            return false;
                        }
                        throw new RuntimeException();
                    }
                } else {
                    z = saveImages(request, "_", false, true, true);
                }
            }
            return z;
        } else {
            throw new RuntimeException();
        }
    }

    private boolean saveImages(Request request, String str, boolean z, boolean z2, boolean z3) {
        String str2;
        Request request2 = request;
        int size = request2.jpeg_images.size() / 2;
        int i = 0;
        boolean z4 = true;
        while (i < request2.jpeg_images.size()) {
            byte[] bArr = (byte[]) request2.jpeg_images.get(i);
            if ((request2.jpeg_images.size() > 1 && !z) || request2.force_suffix) {
                StringBuilder sb = new StringBuilder();
                sb.append(str);
                sb.append(request2.suffix_offset + i);
                str2 = sb.toString();
            } else {
                str2 = BuildConfig.FLAVOR;
                String str3 = str;
            }
            if (!saveSingleImageNow(request, bArr, null, str2, z2, z3 && i == size, false, false)) {
                z4 = false;
            }
            if (z) {
                break;
            }
            i++;
        }
        return z4;
    }

    private void saveBaseImages(Request request, String str) {
        if (!request.image_capture_intent && request.save_base != SaveBase.SAVEBASE_NONE) {
            if (request.process_type == ProcessType.PANORAMA) {
                request = request.copy();
                request.image_format = ImageFormat.PNG;
                request.preference_stamp = "preference_stamp_no";
                request.preference_textstamp = BuildConfig.FLAVOR;
                request.do_auto_stabilise = false;
                request.mirror = false;
            } else if (request.process_type == ProcessType.AVERAGE) {
                request = request.copy();
                request.image_quality = 100;
            }
            Request request2 = request;
            saveImages(request2, str, request2.save_base == SaveBase.SAVEBASE_FIRST, false, false);
        }
    }

    private Bitmap autoStabilise(byte[] bArr, Bitmap bitmap, double d, boolean z, File file) {
        Bitmap bitmap2;
        Bitmap bitmap3;
        double d2 = d;
        while (d2 < -90.0d) {
            d2 += 180.0d;
        }
        while (d2 > 90.0d) {
            d2 -= 180.0d;
        }
        if (bitmap == null) {
            bitmap2 = loadBitmapWithRotation(bArr, false, file);
            if (bitmap2 == null) {
                this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.failed_to_auto_stabilise);
                System.gc();
            }
        } else {
            bitmap2 = bitmap;
        }
        if (bitmap2 == null) {
            return bitmap2;
        }
        int width = bitmap2.getWidth();
        int height = bitmap2.getHeight();
        Matrix matrix = new Matrix();
        double abs = Math.abs(Math.toRadians(d2));
        double d3 = (double) width;
        double cos = Math.cos(abs);
        Double.isNaN(d3);
        double d4 = cos * d3;
        double d5 = (double) height;
        double sin = Math.sin(abs);
        Double.isNaN(d5);
        double d6 = d4 + (sin * d5);
        double sin2 = Math.sin(abs);
        Double.isNaN(d3);
        double d7 = d3 * sin2;
        double cos2 = Math.cos(abs);
        Double.isNaN(d5);
        double d8 = d7 + (d5 * cos2);
        int i = width * height;
        double d9 = abs;
        float sqrt = (float) Math.sqrt((double) (((float) i) / ((float) (d6 * d8))));
        if (this.main_activity.test_low_memory) {
            sqrt *= i >= 7500 ? 1.5f : 2.0f;
        }
        matrix.postScale(sqrt, sqrt);
        double d10 = (double) sqrt;
        Double.isNaN(d10);
        double d11 = d6 * d10;
        Double.isNaN(d10);
        double d12 = d8 * d10;
        int i2 = (int) (((float) width) * sqrt);
        int i3 = (int) (((float) height) * sqrt);
        if (z) {
            matrix.postRotate((float) (-d2));
        } else {
            matrix.postRotate((float) d2);
        }
        Bitmap createBitmap = Bitmap.createBitmap(bitmap2, 0, 0, width, height, matrix, true);
        if (createBitmap != bitmap2) {
            bitmap2.recycle();
            bitmap2 = createBitmap;
        }
        System.gc();
        double tan = Math.tan(d9);
        double sin3 = Math.sin(d9);
        double d13 = (d12 / d11) + tan;
        double d14 = (d11 / d12) + tan;
        if (d13 == 0.0d || d13 < 1.0E-14d || d14 == 0.0d || d14 < 1.0E-14d) {
            return bitmap2;
        }
        Bitmap bitmap4 = bitmap2;
        double d15 = (double) i3;
        Double.isNaN(d15);
        int i4 = (int) ((((((d15 * 2.0d) * sin3) * tan) + d12) - (d11 * tan)) / d13);
        double d16 = (double) i4;
        Double.isNaN(d16);
        int i5 = (int) ((d16 * d12) / d11);
        double d17 = (double) i2;
        Double.isNaN(d17);
        int i6 = (int) ((((((d17 * 2.0d) * sin3) * tan) + d11) - (tan * d12)) / d14);
        double d18 = (double) i6;
        Double.isNaN(d18);
        int i7 = (int) ((d18 * d11) / d12);
        if (i7 >= i4) {
            i7 = i4;
            i6 = i5;
        }
        if (i7 <= 0) {
            i7 = 1;
        } else if (i7 >= bitmap4.getWidth()) {
            i7 = bitmap4.getWidth() - 1;
        }
        if (i6 <= 0) {
            i6 = 1;
        } else if (i6 >= bitmap4.getHeight()) {
            i6 = bitmap4.getHeight() - 1;
        }
        Bitmap bitmap5 = bitmap4;
        Bitmap createBitmap2 = Bitmap.createBitmap(bitmap5, (bitmap4.getWidth() - i7) / 2, (bitmap4.getHeight() - i6) / 2, i7, i6);
        if (createBitmap2 != bitmap5) {
            bitmap5.recycle();
            bitmap3 = createBitmap2;
        } else {
            bitmap3 = bitmap5;
        }
        System.gc();
        return bitmap3;
    }

    private Bitmap mirrorImage(byte[] bArr, Bitmap bitmap, File file) {
        if (bitmap == null) {
            bitmap = loadBitmapWithRotation(bArr, false, file);
            if (bitmap == null) {
                System.gc();
            }
        }
        if (bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.preScale(-1.0f, 1.0f);
            Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (createBitmap != bitmap) {
                bitmap.recycle();
                return createBitmap;
            }
        }
        return bitmap;
    }

    /* JADX WARNING: Removed duplicated region for block: B:101:0x028f  */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x029b  */
    /* JADX WARNING: Removed duplicated region for block: B:108:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00d6  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00e4  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00ea  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01fc  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x0261  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.graphics.Bitmap stampImage(net.sourceforge.opencamera.ImageSaver.Request r35, byte[] r36, android.graphics.Bitmap r37, java.io.File r38) {
        /*
            r34 = this;
            r1 = r34
            r2 = r35
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity
            net.sourceforge.opencamera.MyApplicationInterface r14 = r0.getApplicationInterface()
            java.lang.String r0 = r2.preference_stamp
            java.lang.String r3 = "preference_stamp_yes"
            boolean r0 = r0.equals(r3)
            java.lang.String r3 = r2.preference_textstamp
            int r3 = r3.length()
            r4 = 1
            if (r3 <= 0) goto L_0x001e
            r16 = 1
            goto L_0x0020
        L_0x001e:
            r16 = 0
        L_0x0020:
            if (r0 != 0) goto L_0x0029
            if (r16 == 0) goto L_0x0025
            goto L_0x0029
        L_0x0025:
            r30 = r37
            goto L_0x02b2
        L_0x0029:
            r13 = 0
            if (r37 != 0) goto L_0x0047
            r3 = r36
            r5 = r38
            android.graphics.Bitmap r3 = r1.loadBitmapWithRotation(r3, r4, r5)
            if (r3 != 0) goto L_0x0045
            net.sourceforge.opencamera.MainActivity r5 = r1.main_activity
            net.sourceforge.opencamera.preview.Preview r5 = r5.getPreview()
            r6 = 2131492980(0x7f0c0074, float:1.8609427E38)
            r5.showToast(r13, r6)
            java.lang.System.gc()
        L_0x0045:
            r12 = r3
            goto L_0x0049
        L_0x0047:
            r12 = r37
        L_0x0049:
            if (r12 == 0) goto L_0x02b0
            int r3 = r2.font_size
            int r11 = r2.color
            java.lang.String r5 = r2.pref_style
            java.lang.String r6 = r2.preference_stamp_dateformat
            java.lang.String r7 = r2.preference_stamp_timeformat
            java.lang.String r10 = r2.preference_stamp_gpsformat
            int r9 = r12.getWidth()
            int r8 = r12.getHeight()
            android.graphics.Canvas r15 = new android.graphics.Canvas
            r15.<init>(r12)
            android.graphics.Paint r13 = r1.f13p
            r4 = -1
            r13.setColor(r4)
            if (r9 >= r8) goto L_0x006e
            r13 = r9
            goto L_0x006f
        L_0x006e:
            r13 = r8
        L_0x006f:
            float r13 = (float) r13
            r19 = 1133510656(0x43900000, float:288.0)
            float r13 = r13 / r19
            float r4 = (float) r3
            float r4 = r4 * r13
            r19 = 1056964608(0x3f000000, float:0.5)
            float r4 = r4 + r19
            int r4 = (int) r4
            r20 = r10
            android.graphics.Paint r10 = r1.f13p
            float r4 = (float) r4
            r10.setTextSize(r4)
            r4 = 1090519040(0x41000000, float:8.0)
            float r4 = r4 * r13
            float r4 = r4 + r19
            int r10 = (int) r4
            int r3 = r3 + 4
            float r3 = (float) r3
            float r3 = r3 * r13
            float r3 = r3 + r19
            int r13 = (int) r3
            int r19 = r8 - r10
            android.graphics.Paint r3 = r1.f13p
            android.graphics.Paint$Align r4 = android.graphics.Paint.Align.RIGHT
            r3.setTextAlign(r4)
            net.sourceforge.opencamera.MyApplicationInterface$Shadow r3 = net.sourceforge.opencamera.MyApplicationInterface.Shadow.SHADOW_NONE
            int r4 = r5.hashCode()
            r8 = -980652836(0xffffffffc58c6cdc, float:-4493.6074)
            r37 = r3
            r3 = 2
            if (r4 == r8) goto L_0x00c9
            r8 = 233037148(0xde3dd5c, float:1.4043245E-30)
            if (r4 == r8) goto L_0x00bf
            r8 = 1412856173(0x5436796d, float:3.13488494E12)
            if (r4 == r8) goto L_0x00b5
            goto L_0x00d3
        L_0x00b5:
            java.lang.String r4 = "preference_stamp_style_shadowed"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L_0x00d3
            r4 = 0
            goto L_0x00d4
        L_0x00bf:
            java.lang.String r4 = "preference_stamp_style_background"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L_0x00d3
            r4 = 2
            goto L_0x00d4
        L_0x00c9:
            java.lang.String r4 = "preference_stamp_style_plain"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L_0x00d3
            r4 = 1
            goto L_0x00d4
        L_0x00d3:
            r4 = -1
        L_0x00d4:
            if (r4 == 0) goto L_0x00e4
            r5 = 1
            if (r4 == r5) goto L_0x00e1
            if (r4 == r3) goto L_0x00de
            r25 = r37
            goto L_0x00e8
        L_0x00de:
            net.sourceforge.opencamera.MyApplicationInterface$Shadow r3 = net.sourceforge.opencamera.MyApplicationInterface.Shadow.SHADOW_BACKGROUND
            goto L_0x00e6
        L_0x00e1:
            net.sourceforge.opencamera.MyApplicationInterface$Shadow r3 = net.sourceforge.opencamera.MyApplicationInterface.Shadow.SHADOW_NONE
            goto L_0x00e6
        L_0x00e4:
            net.sourceforge.opencamera.MyApplicationInterface$Shadow r3 = net.sourceforge.opencamera.MyApplicationInterface.Shadow.SHADOW_OUTLINE
        L_0x00e6:
            r25 = r3
        L_0x00e8:
            if (r0 == 0) goto L_0x028f
            java.util.Date r0 = r2.current_date
            java.lang.String r0 = net.sourceforge.opencamera.TextFormatter.getDateString(r6, r0)
            java.util.Date r3 = r2.current_date
            java.lang.String r3 = net.sourceforge.opencamera.TextFormatter.getTimeString(r7, r3)
            int r4 = r0.length()
            if (r4 > 0) goto L_0x0113
            int r4 = r3.length()
            if (r4 <= 0) goto L_0x0103
            goto L_0x0113
        L_0x0103:
            r26 = r9
            r28 = r10
            r29 = r11
            r30 = r12
            r31 = r13
            r27 = r20
            r32 = 0
            goto L_0x0180
        L_0x0113:
            java.lang.String r4 = ""
            int r5 = r0.length()
            if (r5 <= 0) goto L_0x012a
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            r5.append(r4)
            r5.append(r0)
            java.lang.String r4 = r5.toString()
        L_0x012a:
            int r0 = r3.length()
            if (r0 <= 0) goto L_0x0158
            int r0 = r4.length()
            if (r0 <= 0) goto L_0x0147
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r4)
            java.lang.String r4 = " "
            r0.append(r4)
            java.lang.String r4 = r0.toString()
        L_0x0147:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r4)
            r0.append(r3)
            java.lang.String r0 = r0.toString()
            r6 = r0
            goto L_0x0159
        L_0x0158:
            r6 = r4
        L_0x0159:
            android.graphics.Paint r5 = r1.f13p
            r8 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r0 = r9 - r10
            net.sourceforge.opencamera.MyApplicationInterface$Alignment r18 = net.sourceforge.opencamera.MyApplicationInterface.Alignment.ALIGNMENT_BOTTOM
            r21 = 0
            r3 = r14
            r4 = r15
            r7 = r11
            r26 = r9
            r9 = r0
            r28 = r10
            r27 = r20
            r10 = r19
            r29 = r11
            r11 = r18
            r30 = r12
            r12 = r21
            r31 = r13
            r32 = 0
            r13 = r25
            r3.drawTextWithBackground(r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
        L_0x0180:
            int r33 = r19 - r31
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity
            net.sourceforge.opencamera.TextFormatter r17 = r0.getTextFormatter()
            java.lang.String r0 = r2.preference_units_distance
            boolean r3 = r2.store_location
            android.location.Location r4 = r2.location
            boolean r5 = r2.store_geo_direction
            double r6 = r2.geo_direction
            r18 = r27
            r19 = r0
            r20 = r3
            r21 = r4
            r22 = r5
            r23 = r6
            java.lang.String r6 = r17.getGPSString(r18, r19, r20, r21, r22, r23)
            int r0 = r6.length()
            if (r0 <= 0) goto L_0x028c
            boolean r0 = r2.store_location
            if (r0 == 0) goto L_0x01f7
            java.lang.String r0 = r2.preference_stamp_geo_address
            java.lang.String r3 = "preference_stamp_geo_address_no"
            boolean r0 = r0.equals(r3)
            if (r0 != 0) goto L_0x01f7
            boolean r0 = android.location.Geocoder.isPresent()
            if (r0 == 0) goto L_0x01f7
            android.location.Geocoder r7 = new android.location.Geocoder
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity
            java.util.Locale r3 = java.util.Locale.getDefault()
            r7.<init>(r0, r3)
            android.location.Location r0 = r2.location     // Catch:{ Exception -> 0x01ea }
            double r8 = r0.getLatitude()     // Catch:{ Exception -> 0x01ea }
            android.location.Location r0 = r2.location     // Catch:{ Exception -> 0x01ea }
            double r10 = r0.getLongitude()     // Catch:{ Exception -> 0x01ea }
            r12 = 1
            java.util.List r0 = r7.getFromLocation(r8, r10, r12)     // Catch:{ Exception -> 0x01ea }
            if (r0 == 0) goto L_0x01f7
            int r3 = r0.size()     // Catch:{ Exception -> 0x01ea }
            if (r3 <= 0) goto L_0x01f7
            r13 = 0
            java.lang.Object r0 = r0.get(r13)     // Catch:{ Exception -> 0x01e8 }
            android.location.Address r0 = (android.location.Address) r0     // Catch:{ Exception -> 0x01e8 }
            goto L_0x01fa
        L_0x01e8:
            r0 = move-exception
            goto L_0x01ec
        L_0x01ea:
            r0 = move-exception
            r13 = 0
        L_0x01ec:
            java.lang.String r3 = "ImageSaver"
            java.lang.String r4 = "failed to read from geocoder"
            android.util.Log.e(r3, r4)
            r0.printStackTrace()
            goto L_0x01f8
        L_0x01f7:
            r13 = 0
        L_0x01f8:
            r0 = r32
        L_0x01fa:
            if (r0 == 0) goto L_0x0247
            java.lang.String r3 = r2.preference_stamp_geo_address
            java.lang.String r4 = "preference_stamp_geo_address_both"
            boolean r3 = r3.equals(r4)
            if (r3 == 0) goto L_0x0207
            goto L_0x0247
        L_0x0207:
            boolean r3 = r2.store_geo_direction
            if (r3 == 0) goto L_0x0244
            net.sourceforge.opencamera.MainActivity r3 = r1.main_activity
            net.sourceforge.opencamera.TextFormatter r17 = r3.getTextFormatter()
            java.lang.String r3 = r2.preference_units_distance
            r20 = 0
            r21 = 0
            boolean r4 = r2.store_geo_direction
            double r5 = r2.geo_direction
            r18 = r27
            r19 = r3
            r22 = r4
            r23 = r5
            java.lang.String r6 = r17.getGPSString(r18, r19, r20, r21, r22, r23)
            int r3 = r6.length()
            if (r3 <= 0) goto L_0x0244
            android.graphics.Paint r5 = r1.f13p
            r8 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r9 = r26 - r28
            net.sourceforge.opencamera.MyApplicationInterface$Alignment r11 = net.sourceforge.opencamera.MyApplicationInterface.Alignment.ALIGNMENT_BOTTOM
            r12 = 0
            r3 = r14
            r4 = r15
            r7 = r29
            r10 = r33
            r17 = 0
            r13 = r25
            r3.drawTextWithBackground(r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
            goto L_0x025d
        L_0x0244:
            r17 = 0
            goto L_0x025f
        L_0x0247:
            r17 = 0
            android.graphics.Paint r5 = r1.f13p
            r8 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r9 = r26 - r28
            net.sourceforge.opencamera.MyApplicationInterface$Alignment r11 = net.sourceforge.opencamera.MyApplicationInterface.Alignment.ALIGNMENT_BOTTOM
            r12 = 0
            r3 = r14
            r4 = r15
            r7 = r29
            r10 = r33
            r13 = r25
            r3.drawTextWithBackground(r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
        L_0x025d:
            int r33 = r33 - r31
        L_0x025f:
            if (r0 == 0) goto L_0x028c
            r13 = 0
        L_0x0262:
            int r3 = r0.getMaxAddressLineIndex()
            if (r13 > r3) goto L_0x028c
            int r3 = r0.getMaxAddressLineIndex()
            int r3 = r3 - r13
            java.lang.String r6 = r0.getAddressLine(r3)
            android.graphics.Paint r5 = r1.f13p
            r8 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r9 = r26 - r28
            net.sourceforge.opencamera.MyApplicationInterface$Alignment r11 = net.sourceforge.opencamera.MyApplicationInterface.Alignment.ALIGNMENT_BOTTOM
            r12 = 0
            r3 = r14
            r4 = r15
            r7 = r29
            r10 = r33
            r17 = r13
            r13 = r25
            r3.drawTextWithBackground(r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
            int r33 = r33 - r31
            int r13 = r17 + 1
            goto L_0x0262
        L_0x028c:
            r9 = r33
            goto L_0x0299
        L_0x028f:
            r26 = r9
            r28 = r10
            r29 = r11
            r30 = r12
            r9 = r19
        L_0x0299:
            if (r16 == 0) goto L_0x02b2
            android.graphics.Paint r4 = r1.f13p
            java.lang.String r5 = r2.preference_textstamp
            r7 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            int r8 = r26 - r28
            net.sourceforge.opencamera.MyApplicationInterface$Alignment r10 = net.sourceforge.opencamera.MyApplicationInterface.Alignment.ALIGNMENT_BOTTOM
            r11 = 0
            r2 = r14
            r3 = r15
            r6 = r29
            r12 = r25
            r2.drawTextWithBackground(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
            goto L_0x02b2
        L_0x02b0:
            r30 = r12
        L_0x02b2:
            return r30
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.ImageSaver.stampImage(net.sourceforge.opencamera.ImageSaver$Request, byte[], android.graphics.Bitmap, java.io.File):android.graphics.Bitmap");
    }

    private File getExifTempFile(byte[] bArr) {
        FileOutputStream fileOutputStream;
        File file = null;
        if (VERSION.SDK_INT < 24) {
            try {
                file = File.createTempFile("opencamera_exif", BuildConfig.FLAVOR);
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bArr);
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Throwable th) {
                fileOutputStream.close();
                throw th;
            }
        }
        return file;
    }

    private PostProcessBitmapResult postProcessBitmap(Request request, byte[] bArr, Bitmap bitmap, boolean z) throws IOException {
        System.currentTimeMillis();
        boolean equals = request.preference_stamp.equals("preference_stamp_yes");
        boolean z2 = request.preference_textstamp.length() > 0;
        File file = null;
        if ((bitmap != null || request.image_format != ImageFormat.STD || request.do_auto_stabilise || request.mirror || equals || z2) && !z) {
            file = getExifTempFile(bArr);
            if (bitmap != null) {
                bitmap = rotateForExif(bitmap, bArr, file);
            }
        }
        Bitmap bitmap2 = bitmap;
        if (request.do_auto_stabilise) {
            bitmap2 = autoStabilise(bArr, bitmap2, request.level_angle, request.is_front_facing, file);
        }
        if (request.mirror) {
            bitmap2 = mirrorImage(bArr, bitmap2, file);
        }
        if (request.image_format != ImageFormat.STD && bitmap2 == null) {
            bitmap2 = loadBitmapWithRotation(bArr, true, file);
            if (bitmap2 == null) {
                System.gc();
                if (file != null) {
                    file.delete();
                }
                throw new IOException();
            }
        }
        return new PostProcessBitmapResult(stampImage(request, bArr, bitmap2, file), file);
    }

    /* JADX WARNING: type inference failed for: r11v3, types: [boolean] */
    /* JADX WARNING: type inference failed for: r8v1, types: [java.io.File] */
    /* JADX WARNING: type inference failed for: r7v2 */
    /* JADX WARNING: type inference failed for: r0v19, types: [android.graphics.Bitmap] */
    /* JADX WARNING: type inference failed for: r0v24 */
    /* JADX WARNING: type inference failed for: r0v25, types: [android.graphics.Bitmap] */
    /* JADX WARNING: type inference failed for: r0v28, types: [android.graphics.Bitmap] */
    /* JADX WARNING: type inference failed for: r16v0 */
    /* JADX WARNING: type inference failed for: r11v4 */
    /* JADX WARNING: type inference failed for: r7v4 */
    /* JADX WARNING: type inference failed for: r8v3 */
    /* JADX WARNING: type inference failed for: r16v1 */
    /* JADX WARNING: type inference failed for: r11v5 */
    /* JADX WARNING: type inference failed for: r7v5, types: [net.sourceforge.opencamera.ToastBoxer] */
    /* JADX WARNING: type inference failed for: r16v2 */
    /* JADX WARNING: type inference failed for: r11v6 */
    /* JADX WARNING: type inference failed for: r7v6, types: [net.sourceforge.opencamera.ToastBoxer] */
    /* JADX WARNING: type inference failed for: r11v7 */
    /* JADX WARNING: type inference failed for: r7v7 */
    /* JADX WARNING: type inference failed for: r27v2 */
    /* JADX WARNING: type inference failed for: r7v8 */
    /* JADX WARNING: type inference failed for: r8v7 */
    /* JADX WARNING: type inference failed for: r11v8 */
    /* JADX WARNING: type inference failed for: r7v9 */
    /* JADX WARNING: type inference failed for: r28v2 */
    /* JADX WARNING: type inference failed for: r7v10, types: [java.io.File] */
    /* JADX WARNING: type inference failed for: r11v9 */
    /* JADX WARNING: type inference failed for: r8v9 */
    /* JADX WARNING: type inference failed for: r7v11 */
    /* JADX WARNING: type inference failed for: r11v10 */
    /* JADX WARNING: type inference failed for: r16v3 */
    /* JADX WARNING: type inference failed for: r11v11 */
    /* JADX WARNING: type inference failed for: r16v4 */
    /* JADX WARNING: type inference failed for: r28v3 */
    /* JADX WARNING: type inference failed for: r28v4 */
    /* JADX WARNING: type inference failed for: r28v5 */
    /* JADX WARNING: type inference failed for: r11v12 */
    /* JADX WARNING: type inference failed for: r11v13 */
    /* JADX WARNING: type inference failed for: r11v14 */
    /* JADX WARNING: type inference failed for: r11v15 */
    /* JADX WARNING: type inference failed for: r11v16 */
    /* JADX WARNING: type inference failed for: r11v17 */
    /* JADX WARNING: type inference failed for: r28v6 */
    /* JADX WARNING: type inference failed for: r11v18 */
    /* JADX WARNING: type inference failed for: r11v19 */
    /* JADX WARNING: type inference failed for: r11v20 */
    /* JADX WARNING: type inference failed for: r11v21 */
    /* JADX WARNING: type inference failed for: r16v5 */
    /* JADX WARNING: type inference failed for: r7v12 */
    /* JADX WARNING: type inference failed for: r11v22 */
    /* JADX WARNING: type inference failed for: r16v6 */
    /* JADX WARNING: type inference failed for: r7v13 */
    /* JADX WARNING: type inference failed for: r11v23 */
    /* JADX WARNING: type inference failed for: r11v24 */
    /* JADX WARNING: type inference failed for: r11v25 */
    /* JADX WARNING: type inference failed for: r11v26 */
    /* JADX WARNING: type inference failed for: r11v27 */
    /* JADX WARNING: type inference failed for: r28v7 */
    /* JADX WARNING: type inference failed for: r28v8 */
    /* JADX WARNING: type inference failed for: r11v28 */
    /* JADX WARNING: type inference failed for: r11v29 */
    /* JADX WARNING: type inference failed for: r11v30 */
    /* JADX WARNING: type inference failed for: r28v9 */
    /* JADX WARNING: type inference failed for: r28v10 */
    /* JADX WARNING: type inference failed for: r28v11 */
    /* JADX WARNING: type inference failed for: r28v12 */
    /* JADX WARNING: type inference failed for: r7v14 */
    /* JADX WARNING: type inference failed for: r28v13 */
    /* JADX WARNING: type inference failed for: r16v7 */
    /* JADX WARNING: type inference failed for: r11v36 */
    /* JADX WARNING: type inference failed for: r7v15 */
    /* JADX WARNING: type inference failed for: r16v8 */
    /* JADX WARNING: type inference failed for: r11v37 */
    /* JADX WARNING: type inference failed for: r7v16 */
    /* JADX WARNING: type inference failed for: r16v9 */
    /* JADX WARNING: type inference failed for: r11v38 */
    /* JADX WARNING: type inference failed for: r7v17 */
    /* JADX WARNING: type inference failed for: r16v10 */
    /* JADX WARNING: type inference failed for: r11v39 */
    /* JADX WARNING: type inference failed for: r7v18 */
    /* JADX WARNING: type inference failed for: r28v14 */
    /* JADX WARNING: type inference failed for: r16v11 */
    /* JADX WARNING: type inference failed for: r16v12 */
    /* JADX WARNING: type inference failed for: r7v19 */
    /* JADX WARNING: type inference failed for: r0v105, types: [java.io.File] */
    /* JADX WARNING: type inference failed for: r7v22 */
    /* JADX WARNING: type inference failed for: r7v23 */
    /* JADX WARNING: type inference failed for: r16v13 */
    /* JADX WARNING: type inference failed for: r11v40 */
    /* JADX WARNING: type inference failed for: r7v24 */
    /* JADX WARNING: type inference failed for: r16v14 */
    /* JADX WARNING: type inference failed for: r11v41 */
    /* JADX WARNING: type inference failed for: r7v25 */
    /* JADX WARNING: type inference failed for: r11v42 */
    /* JADX WARNING: type inference failed for: r7v26 */
    /* JADX WARNING: type inference failed for: r11v43 */
    /* JADX WARNING: type inference failed for: r7v27 */
    /* JADX WARNING: type inference failed for: r11v44 */
    /* JADX WARNING: type inference failed for: r0v111, types: [java.io.File] */
    /* JADX WARNING: type inference failed for: r7v29 */
    /* JADX WARNING: type inference failed for: r11v45 */
    /* JADX WARNING: type inference failed for: r7v31 */
    /* JADX WARNING: type inference failed for: r11v46 */
    /* JADX WARNING: type inference failed for: r7v32 */
    /* JADX WARNING: type inference failed for: r11v47 */
    /* JADX WARNING: type inference failed for: r11v53 */
    /* JADX WARNING: type inference failed for: r7v33 */
    /* JADX WARNING: type inference failed for: r7v34 */
    /* JADX WARNING: type inference failed for: r11v54 */
    /* JADX WARNING: type inference failed for: r11v55 */
    /* JADX WARNING: type inference failed for: r7v35 */
    /* JADX WARNING: type inference failed for: r16v15 */
    /* JADX WARNING: type inference failed for: r7v36 */
    /* JADX WARNING: type inference failed for: r11v56 */
    /* JADX WARNING: type inference failed for: r11v57 */
    /* JADX WARNING: type inference failed for: r7v37 */
    /* JADX WARNING: type inference failed for: r16v16 */
    /* JADX WARNING: type inference failed for: r7v38 */
    /* JADX WARNING: type inference failed for: r11v58 */
    /* JADX WARNING: type inference failed for: r11v59 */
    /* JADX WARNING: type inference failed for: r7v39 */
    /* JADX WARNING: type inference failed for: r7v40 */
    /* JADX WARNING: type inference failed for: r11v60 */
    /* JADX WARNING: type inference failed for: r0v127 */
    /* JADX WARNING: type inference failed for: r0v128 */
    /* JADX WARNING: type inference failed for: r11v64 */
    /* JADX WARNING: type inference failed for: r7v41 */
    /* JADX WARNING: type inference failed for: r16v17 */
    /* JADX WARNING: type inference failed for: r11v65 */
    /* JADX WARNING: type inference failed for: r7v42 */
    /* JADX WARNING: type inference failed for: r16v18 */
    /* JADX WARNING: type inference failed for: r11v66 */
    /* JADX WARNING: type inference failed for: r7v43 */
    /* JADX WARNING: type inference failed for: r11v67 */
    /* JADX WARNING: type inference failed for: r7v44 */
    /* JADX WARNING: type inference failed for: r11v68 */
    /* JADX WARNING: type inference failed for: r11v69 */
    /* JADX WARNING: type inference failed for: r11v70 */
    /* JADX WARNING: type inference failed for: r11v71 */
    /* JADX WARNING: type inference failed for: r11v72 */
    /* JADX WARNING: type inference failed for: r11v73 */
    /* JADX WARNING: type inference failed for: r11v74 */
    /* JADX WARNING: type inference failed for: r11v75 */
    /* JADX WARNING: type inference failed for: r11v76 */
    /* JADX WARNING: type inference failed for: r11v77 */
    /* JADX WARNING: type inference failed for: r11v78 */
    /* JADX WARNING: type inference failed for: r11v79 */
    /* JADX WARNING: type inference failed for: r11v80 */
    /* JADX WARNING: type inference failed for: r28v15 */
    /* JADX WARNING: type inference failed for: r7v45 */
    /* JADX WARNING: type inference failed for: r7v46 */
    /* JADX WARNING: type inference failed for: r7v47 */
    /* JADX WARNING: type inference failed for: r7v48 */
    /* JADX WARNING: type inference failed for: r7v49 */
    /* JADX WARNING: type inference failed for: r7v50 */
    /* JADX WARNING: type inference failed for: r11v81 */
    /* JADX WARNING: type inference failed for: r7v51 */
    /* JADX WARNING: type inference failed for: r11v82 */
    /* JADX WARNING: type inference failed for: r11v83 */
    /* JADX WARNING: type inference failed for: r11v84 */
    /* JADX WARNING: type inference failed for: r11v85 */
    /* JADX WARNING: type inference failed for: r11v86 */
    /* JADX WARNING: type inference failed for: r11v87 */
    /* JADX WARNING: type inference failed for: r11v88 */
    /* JADX WARNING: type inference failed for: r11v89 */
    /* JADX WARNING: type inference failed for: r11v90 */
    /* JADX WARNING: type inference failed for: r11v91 */
    /* JADX WARNING: type inference failed for: r11v92 */
    /* JADX WARNING: type inference failed for: r7v52 */
    /* JADX WARNING: type inference failed for: r11v93 */
    /* JADX WARNING: type inference failed for: r7v53 */
    /* JADX WARNING: type inference failed for: r11v94 */
    /* JADX WARNING: type inference failed for: r11v95 */
    /* JADX WARNING: type inference failed for: r11v96 */
    /* JADX WARNING: type inference failed for: r11v97 */
    /* JADX WARNING: type inference failed for: r28v16 */
    /* JADX WARNING: type inference failed for: r28v17 */
    /* JADX WARNING: type inference failed for: r28v18 */
    /* JADX WARNING: type inference failed for: r28v19 */
    /* JADX WARNING: type inference failed for: r16v19 */
    /* JADX WARNING: type inference failed for: r11v98 */
    /* JADX WARNING: type inference failed for: r7v54 */
    /* JADX WARNING: type inference failed for: r16v20 */
    /* JADX WARNING: type inference failed for: r11v99 */
    /* JADX WARNING: type inference failed for: r7v55 */
    /* JADX WARNING: type inference failed for: r16v21 */
    /* JADX WARNING: type inference failed for: r11v100 */
    /* JADX WARNING: type inference failed for: r7v56 */
    /* JADX WARNING: type inference failed for: r16v22 */
    /* JADX WARNING: type inference failed for: r11v101 */
    /* JADX WARNING: type inference failed for: r7v57 */
    /* JADX WARNING: type inference failed for: r7v58 */
    /* JADX WARNING: type inference failed for: r11v102 */
    /* JADX WARNING: type inference failed for: r7v59 */
    /* JADX WARNING: type inference failed for: r11v103 */
    /* JADX WARNING: type inference failed for: r7v60 */
    /* JADX WARNING: type inference failed for: r11v104 */
    /* JADX WARNING: type inference failed for: r7v61 */
    /* JADX WARNING: type inference failed for: r11v105 */
    /* JADX WARNING: type inference failed for: r7v62 */
    /* JADX WARNING: type inference failed for: r11v106 */
    /* JADX WARNING: type inference failed for: r11v107 */
    /* JADX WARNING: type inference failed for: r7v63 */
    /* JADX WARNING: type inference failed for: r11v108 */
    /* JADX WARNING: type inference failed for: r7v64 */
    /* JADX WARNING: type inference failed for: r11v109 */
    /* JADX WARNING: type inference failed for: r11v110 */
    /* JADX WARNING: type inference failed for: r7v65 */
    /* JADX WARNING: type inference failed for: r7v66 */
    /* JADX WARNING: type inference failed for: r11v111 */
    /* JADX WARNING: type inference failed for: r11v112 */
    /* JADX WARNING: type inference failed for: r7v67 */
    /* JADX WARNING: type inference failed for: r16v23 */
    /* JADX WARNING: type inference failed for: r7v68 */
    /* JADX WARNING: type inference failed for: r11v113 */
    /* JADX WARNING: type inference failed for: r11v114 */
    /* JADX WARNING: type inference failed for: r7v69 */
    /* JADX WARNING: type inference failed for: r16v24 */
    /* JADX WARNING: type inference failed for: r7v70 */
    /* JADX WARNING: type inference failed for: r11v115 */
    /* JADX WARNING: type inference failed for: r11v116 */
    /* JADX WARNING: type inference failed for: r7v71 */
    /* JADX WARNING: type inference failed for: r7v72 */
    /* JADX WARNING: type inference failed for: r11v117 */
    /* JADX WARNING: Code restructure failed: missing block: B:201:0x02b7, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:202:0x02b8, code lost:
        r8 = r15;
        r7 = 0;
        r11 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:204:0x02d3, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:205:0x02d4, code lost:
        r12 = net.sourceforge.opencamera.C0316R.string.failed_to_save_photo;
        r16 = 0;
        r8 = r15;
        r15 = null;
        r7 = 0;
        r11 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:207:0x02eb, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:208:0x02ec, code lost:
        r12 = net.sourceforge.opencamera.C0316R.string.failed_to_save_photo;
        r16 = 0;
        r8 = r15;
        r15 = null;
        r7 = 0;
        r11 = 0;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Multi-variable type inference failed. Error: jadx.core.utils.exceptions.JadxRuntimeException: No candidate types for var: r11v4
      assigns: []
      uses: []
      mth insns count: 525
    	at jadx.core.dex.visitors.typeinference.TypeSearch.fillTypeCandidates(TypeSearch.java:237)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    	at jadx.core.dex.visitors.typeinference.TypeSearch.run(TypeSearch.java:53)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runMultiVariableSearch(TypeInferenceVisitor.java:99)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:92)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
    	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
    	at jadx.core.ProcessClass.process(ProcessClass.java:30)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:311)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:217)
     */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x0192 A[Catch:{ all -> 0x018d, all -> 0x0274 }] */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x019c  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x019e  */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x01a6  */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x01f4 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x020c A[Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }] */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x021d A[Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }] */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x0222 A[Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }] */
    /* JADX WARNING: Removed duplicated region for block: B:201:0x02b7 A[ExcHandler: SecurityException (e java.lang.SecurityException), Splitter:B:33:0x008b] */
    /* JADX WARNING: Removed duplicated region for block: B:212:0x0307  */
    /* JADX WARNING: Removed duplicated region for block: B:214:0x030d  */
    /* JADX WARNING: Removed duplicated region for block: B:227:0x0366  */
    /* JADX WARNING: Removed duplicated region for block: B:231:0x0383  */
    /* JADX WARNING: Removed duplicated region for block: B:237:0x03bb  */
    /* JADX WARNING: Removed duplicated region for block: B:239:0x03c7  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0147 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x0150 A[SYNTHETIC, Splitter:B:87:0x0150] */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0160 A[SYNTHETIC, Splitter:B:93:0x0160] */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x016d A[SYNTHETIC, Splitter:B:96:0x016d] */
    /* JADX WARNING: Unknown variable types count: 87 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean saveSingleImageNow(net.sourceforge.opencamera.ImageSaver.Request r25, byte[] r26, android.graphics.Bitmap r27, java.lang.String r28, boolean r29, boolean r30, boolean r31, boolean r32) {
        /*
            r24 = this;
            r1 = r24
            r2 = r25
            r3 = r26
            r0 = r28
            r4 = r29
            r5 = r30
            net.sourceforge.opencamera.ImageSaver$Request$Type r6 = r2.type
            net.sourceforge.opencamera.ImageSaver$Request$Type r7 = net.sourceforge.opencamera.ImageSaver.Request.Type.JPEG
            if (r6 != r7) goto L_0x03e1
            if (r3 == 0) goto L_0x03db
            java.lang.System.currentTimeMillis()
            net.sourceforge.opencamera.MainActivity r6 = r1.main_activity
            net.sourceforge.opencamera.MyApplicationInterface r6 = r6.getApplicationInterface()
            r7 = 0
            r8 = 1
            if (r31 != 0) goto L_0x0029
            boolean r9 = r6.isRawOnly()
            if (r9 == 0) goto L_0x0029
            r9 = 1
            goto L_0x002a
        L_0x0029:
            r9 = 0
        L_0x002a:
            net.sourceforge.opencamera.MainActivity r10 = r1.main_activity
            net.sourceforge.opencamera.StorageUtils r10 = r10.getStorageUtils()
            int[] r11 = net.sourceforge.opencamera.ImageSaver.C02404.f14x15fcad09
            net.sourceforge.opencamera.ImageSaver$Request$ImageFormat r12 = r2.image_format
            int r12 = r12.ordinal()
            r11 = r11[r12]
            java.lang.String r12 = "jpg"
            r13 = 2
            if (r11 == r8) goto L_0x0046
            if (r11 == r13) goto L_0x0043
            r11 = r12
            goto L_0x0048
        L_0x0043:
            java.lang.String r11 = "png"
            goto L_0x0048
        L_0x0046:
            java.lang.String r11 = "webp"
        L_0x0048:
            net.sourceforge.opencamera.MainActivity r14 = r1.main_activity
            r14.savingImage(r8)
            java.lang.String r14 = "ImageSaver"
            if (r9 != 0) goto L_0x0080
            r15 = r27
            r13 = r32
            net.sourceforge.opencamera.ImageSaver$PostProcessBitmapResult r13 = r1.postProcessBitmap(r2, r3, r15, r13)     // Catch:{ FileNotFoundException -> 0x0073, IOException -> 0x0066, SecurityException -> 0x005e }
            android.graphics.Bitmap r15 = r13.bitmap     // Catch:{ FileNotFoundException -> 0x0073, IOException -> 0x0066, SecurityException -> 0x005e }
            java.io.File r13 = r13.exifTempFile     // Catch:{ FileNotFoundException -> 0x0073, IOException -> 0x0066, SecurityException -> 0x005e }
            goto L_0x0083
        L_0x005e:
            r0 = move-exception
            r8 = r15
            r7 = 0
            r11 = 0
            r13 = 0
        L_0x0063:
            r15 = 0
            goto L_0x02bd
        L_0x0066:
            r0 = move-exception
            r8 = r15
            r7 = 0
            r11 = 0
            r12 = 2131492977(0x7f0c0071, float:1.8609421E38)
            r13 = 0
        L_0x006e:
            r15 = 0
            r16 = 0
            goto L_0x02de
        L_0x0073:
            r0 = move-exception
            r8 = r15
            r7 = 0
            r11 = 0
            r12 = 2131492977(0x7f0c0071, float:1.8609421E38)
            r13 = 0
        L_0x007b:
            r15 = 0
            r16 = 0
            goto L_0x02f6
        L_0x0080:
            r15 = r27
            r13 = 0
        L_0x0083:
            if (r9 == 0) goto L_0x008b
            r8 = r15
            r7 = 0
            r11 = 1
        L_0x0088:
            r15 = 0
            goto L_0x0110
        L_0x008b:
            boolean r8 = r2.image_capture_intent     // Catch:{ FileNotFoundException -> 0x02eb, IOException -> 0x02d3, SecurityException -> 0x02b7 }
            if (r8 == 0) goto L_0x00f6
            android.net.Uri r0 = r2.image_capture_intent_uri     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            if (r0 == 0) goto L_0x009b
            android.net.Uri r0 = r2.image_capture_intent_uri     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
        L_0x0095:
            r8 = r15
            r7 = 0
            r11 = 0
            r15 = r0
            goto L_0x0110
        L_0x009b:
            if (r15 != 0) goto L_0x00a2
            android.graphics.Bitmap r0 = r1.loadBitmapWithRotation(r3, r7, r13)     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            r15 = r0
        L_0x00a2:
            if (r15 == 0) goto L_0x00d2
            int r0 = r15.getWidth()     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            int r21 = r15.getHeight()     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            r8 = 128(0x80, float:1.794E-43)
            if (r0 <= r8) goto L_0x00d2
            r8 = 1124073472(0x43000000, float:128.0)
            float r11 = (float) r0     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            float r8 = r8 / r11
            android.graphics.Matrix r11 = new android.graphics.Matrix     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            r11.<init>()     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            r11.postScale(r8, r8)     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            r18 = 0
            r19 = 0
            r23 = 1
            r17 = r15
            r20 = r0
            r22 = r11
            android.graphics.Bitmap r0 = android.graphics.Bitmap.createBitmap(r17, r18, r19, r20, r21, r22, r23)     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            if (r0 == r15) goto L_0x00d2
            r15.recycle()     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            r15 = r0
        L_0x00d2:
            if (r15 == 0) goto L_0x00e7
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            android.content.Intent r8 = new android.content.Intent     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            java.lang.String r11 = "inline-data"
            r8.<init>(r11)     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            java.lang.String r11 = "data"
            android.content.Intent r8 = r8.putExtra(r11, r15)     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            r11 = -1
            r0.setResult(r11, r8)     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
        L_0x00e7:
            if (r13 == 0) goto L_0x00ec
            r13.delete()     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
        L_0x00ec:
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity     // Catch:{ FileNotFoundException -> 0x0073, IOException -> 0x0066, SecurityException -> 0x005e }
            r0.finish()     // Catch:{ FileNotFoundException -> 0x0073, IOException -> 0x0066, SecurityException -> 0x005e }
            r8 = r15
            r7 = 0
            r11 = 0
            r13 = 0
            goto L_0x0088
        L_0x00f6:
            boolean r8 = r10.isUsingSAF()     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            if (r8 == 0) goto L_0x0104
            java.util.Date r8 = r2.current_date     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            r7 = 1
            android.net.Uri r0 = r10.createOutputMediaFileSAF(r7, r0, r11, r8)     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            goto L_0x0095
        L_0x0104:
            java.util.Date r7 = r2.current_date     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            r8 = 1
            java.io.File r0 = r10.createOutputMediaFile(r8, r0, r11, r7)     // Catch:{ FileNotFoundException -> 0x02ae, IOException -> 0x02a5, SecurityException -> 0x02b7 }
            r7 = r0
            r8 = r15
            r11 = 0
            goto L_0x0088
        L_0x0110:
            r0 = 24
            if (r15 == 0) goto L_0x0141
            if (r7 != 0) goto L_0x0141
            r28 = r7
            int r7 = android.os.Build.VERSION.SDK_INT     // Catch:{ FileNotFoundException -> 0x0138, IOException -> 0x012f, SecurityException -> 0x012a }
            if (r7 >= r0) goto L_0x0143
            java.lang.String r7 = "picFile"
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity     // Catch:{ FileNotFoundException -> 0x0138, IOException -> 0x012f, SecurityException -> 0x012a }
            java.io.File r0 = r0.getCacheDir()     // Catch:{ FileNotFoundException -> 0x0138, IOException -> 0x012f, SecurityException -> 0x012a }
            java.io.File r0 = java.io.File.createTempFile(r7, r12, r0)     // Catch:{ FileNotFoundException -> 0x0138, IOException -> 0x012f, SecurityException -> 0x012a }
            r7 = r0
            goto L_0x0145
        L_0x012a:
            r0 = move-exception
            r7 = r28
            goto L_0x02bd
        L_0x012f:
            r0 = move-exception
            r16 = r28
        L_0x0132:
            r7 = 0
        L_0x0133:
            r12 = 2131492977(0x7f0c0071, float:1.8609421E38)
            goto L_0x02de
        L_0x0138:
            r0 = move-exception
            r16 = r28
        L_0x013b:
            r7 = 0
        L_0x013c:
            r12 = 2131492977(0x7f0c0071, float:1.8609421E38)
            goto L_0x02f6
        L_0x0141:
            r28 = r7
        L_0x0143:
            r7 = r28
        L_0x0145:
            if (r7 != 0) goto L_0x014e
            if (r15 == 0) goto L_0x014a
            goto L_0x014e
        L_0x014a:
            r32 = r8
            goto L_0x0248
        L_0x014e:
            if (r7 == 0) goto L_0x0160
            java.io.FileOutputStream r0 = new java.io.FileOutputStream     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            r0.<init>(r7)     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
        L_0x0155:
            r12 = r0
            goto L_0x016b
        L_0x0157:
            r0 = move-exception
            goto L_0x02bd
        L_0x015a:
            r0 = move-exception
            goto L_0x0298
        L_0x015d:
            r0 = move-exception
            goto L_0x02a1
        L_0x0160:
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity     // Catch:{ FileNotFoundException -> 0x029c, IOException -> 0x0293, SecurityException -> 0x028d }
            android.content.ContentResolver r0 = r0.getContentResolver()     // Catch:{ FileNotFoundException -> 0x029c, IOException -> 0x0293, SecurityException -> 0x028d }
            java.io.OutputStream r0 = r0.openOutputStream(r15)     // Catch:{ FileNotFoundException -> 0x029c, IOException -> 0x0293, SecurityException -> 0x028d }
            goto L_0x0155
        L_0x016b:
            if (r8 == 0) goto L_0x0192
            int[] r0 = net.sourceforge.opencamera.ImageSaver.C02404.f14x15fcad09     // Catch:{ all -> 0x018d }
            r28 = r11
            net.sourceforge.opencamera.ImageSaver$Request$ImageFormat r11 = r2.image_format     // Catch:{ all -> 0x0274 }
            int r11 = r11.ordinal()     // Catch:{ all -> 0x0274 }
            r0 = r0[r11]     // Catch:{ all -> 0x0274 }
            r11 = 1
            if (r0 == r11) goto L_0x0185
            r11 = 2
            if (r0 == r11) goto L_0x0182
            android.graphics.Bitmap$CompressFormat r0 = android.graphics.Bitmap.CompressFormat.JPEG     // Catch:{ all -> 0x0274 }
            goto L_0x0187
        L_0x0182:
            android.graphics.Bitmap$CompressFormat r0 = android.graphics.Bitmap.CompressFormat.PNG     // Catch:{ all -> 0x0274 }
            goto L_0x0187
        L_0x0185:
            android.graphics.Bitmap$CompressFormat r0 = android.graphics.Bitmap.CompressFormat.WEBP     // Catch:{ all -> 0x0274 }
        L_0x0187:
            int r11 = r2.image_quality     // Catch:{ all -> 0x0274 }
            r8.compress(r0, r11, r12)     // Catch:{ all -> 0x0274 }
            goto L_0x0197
        L_0x018d:
            r0 = move-exception
            r28 = r11
            goto L_0x0275
        L_0x0192:
            r28 = r11
            r12.write(r3)     // Catch:{ all -> 0x0274 }
        L_0x0197:
            r12.close()     // Catch:{ FileNotFoundException -> 0x026e, IOException -> 0x0268, SecurityException -> 0x0261 }
            if (r15 != 0) goto L_0x019e
            r11 = 1
            goto L_0x01a0
        L_0x019e:
            r11 = r28
        L_0x01a0:
            net.sourceforge.opencamera.ImageSaver$Request$ImageFormat r0 = r2.image_format     // Catch:{ FileNotFoundException -> 0x025d, IOException -> 0x0259, SecurityException -> 0x0254 }
            net.sourceforge.opencamera.ImageSaver$Request$ImageFormat r12 = net.sourceforge.opencamera.ImageSaver.Request.ImageFormat.STD     // Catch:{ FileNotFoundException -> 0x025d, IOException -> 0x0259, SecurityException -> 0x0254 }
            if (r0 != r12) goto L_0x01f2
            if (r8 == 0) goto L_0x01ef
            int r0 = android.os.Build.VERSION.SDK_INT     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            r12 = 24
            if (r0 < r12) goto L_0x01df
            if (r7 == 0) goto L_0x01b4
            r1.setExifFromData(r2, r3, r7)     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            goto L_0x01f2
        L_0x01b4:
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            android.content.ContentResolver r0 = r0.getContentResolver()     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            java.lang.String r12 = "rw"
            android.os.ParcelFileDescriptor r0 = r0.openFileDescriptor(r15, r12)     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            if (r0 == 0) goto L_0x01ca
            java.io.FileDescriptor r0 = r0.getFileDescriptor()     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            r1.setExifFromData(r2, r3, r0)     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            goto L_0x01f2
        L_0x01ca:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            r0.<init>()     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            java.lang.String r12 = "failed to create ParcelFileDescriptor for saveUri: "
            r0.append(r12)     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            r0.append(r15)     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            java.lang.String r0 = r0.toString()     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            android.util.Log.e(r14, r0)     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            goto L_0x01f2
        L_0x01df:
            if (r7 == 0) goto L_0x01e7
            if (r13 == 0) goto L_0x01f2
            r1.setExifFromFile(r2, r13, r7)     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            goto L_0x01f2
        L_0x01e7:
            java.lang.RuntimeException r0 = new java.lang.RuntimeException     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            java.lang.String r12 = "should have set picFile on pre-Android 7!"
            r0.<init>(r12)     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
            throw r0     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
        L_0x01ef:
            r1.updateExif(r2, r7, r15)     // Catch:{ FileNotFoundException -> 0x015d, IOException -> 0x015a, SecurityException -> 0x0157 }
        L_0x01f2:
            if (r7 == 0) goto L_0x0206
            if (r15 != 0) goto L_0x0206
            r32 = r8
            r8 = 1
            r12 = 0
            r10.broadcastFile(r7, r8, r12, r4)     // Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity     // Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }
            java.lang.String r8 = r7.getAbsolutePath()     // Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }
            r0.test_last_saved_image = r8     // Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }
            goto L_0x0208
        L_0x0206:
            r32 = r8
        L_0x0208:
            boolean r0 = r2.image_capture_intent     // Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }
            if (r0 == 0) goto L_0x0217
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity     // Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }
            r8 = -1
            r0.setResult(r8)     // Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity     // Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }
            r0.finish()     // Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }
        L_0x0217:
            boolean r0 = r10.isUsingSAF()     // Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }
            if (r0 == 0) goto L_0x0220
            r10.clearLastMediaScanned()     // Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }
        L_0x0220:
            if (r15 == 0) goto L_0x0248
            if (r7 == 0) goto L_0x0229
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity     // Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }
            r1.copyFileToUri(r0, r15, r7)     // Catch:{ FileNotFoundException -> 0x0252, IOException -> 0x0250, SecurityException -> 0x024e }
        L_0x0229:
            boolean r0 = r2.image_capture_intent     // Catch:{ FileNotFoundException -> 0x023f, IOException -> 0x0236, SecurityException -> 0x0230 }
            r1.broadcastSAFFile(r15, r0)     // Catch:{ FileNotFoundException -> 0x023f, IOException -> 0x0236, SecurityException -> 0x0230 }
            r11 = 1
            goto L_0x0248
        L_0x0230:
            r0 = move-exception
            r8 = r32
            r11 = 1
            goto L_0x02bd
        L_0x0236:
            r0 = move-exception
            r8 = r32
            r16 = r7
            r7 = 0
            r11 = 1
            goto L_0x0133
        L_0x023f:
            r0 = move-exception
            r8 = r32
            r16 = r7
            r7 = 0
            r11 = 1
            goto L_0x013c
        L_0x0248:
            r12 = r32
            r8 = r7
            r7 = 0
            goto L_0x0305
        L_0x024e:
            r0 = move-exception
            goto L_0x027e
        L_0x0250:
            r0 = move-exception
            goto L_0x0284
        L_0x0252:
            r0 = move-exception
            goto L_0x028a
        L_0x0254:
            r0 = move-exception
            r32 = r8
            goto L_0x02bd
        L_0x0259:
            r0 = move-exception
            r32 = r8
            goto L_0x0298
        L_0x025d:
            r0 = move-exception
            r32 = r8
            goto L_0x02a1
        L_0x0261:
            r0 = move-exception
            r32 = r8
            r11 = r28
            goto L_0x02bd
        L_0x0268:
            r0 = move-exception
            r32 = r8
            r11 = r28
            goto L_0x0298
        L_0x026e:
            r0 = move-exception
            r32 = r8
            r11 = r28
            goto L_0x02a1
        L_0x0274:
            r0 = move-exception
        L_0x0275:
            r32 = r8
            r12.close()     // Catch:{ FileNotFoundException -> 0x0287, IOException -> 0x0281, SecurityException -> 0x027b }
            throw r0     // Catch:{ FileNotFoundException -> 0x0287, IOException -> 0x0281, SecurityException -> 0x027b }
        L_0x027b:
            r0 = move-exception
            r11 = r28
        L_0x027e:
            r8 = r32
            goto L_0x02bd
        L_0x0281:
            r0 = move-exception
            r11 = r28
        L_0x0284:
            r8 = r32
            goto L_0x0298
        L_0x0287:
            r0 = move-exception
            r11 = r28
        L_0x028a:
            r8 = r32
            goto L_0x02a1
        L_0x028d:
            r0 = move-exception
            r32 = r8
            r28 = r11
            goto L_0x02bd
        L_0x0293:
            r0 = move-exception
            r32 = r8
            r28 = r11
        L_0x0298:
            r16 = r7
            goto L_0x0132
        L_0x029c:
            r0 = move-exception
            r32 = r8
            r28 = r11
        L_0x02a1:
            r16 = r7
            goto L_0x013b
        L_0x02a5:
            r0 = move-exception
            r8 = r15
            r7 = 0
            r11 = 0
            r12 = 2131492977(0x7f0c0071, float:1.8609421E38)
            goto L_0x006e
        L_0x02ae:
            r0 = move-exception
            r8 = r15
            r7 = 0
            r11 = 0
            r12 = 2131492977(0x7f0c0071, float:1.8609421E38)
            goto L_0x007b
        L_0x02b7:
            r0 = move-exception
            r8 = r15
            r7 = 0
            r11 = 0
            goto L_0x0063
        L_0x02bd:
            r0.printStackTrace()
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity
            net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()
            r27 = r7
            r7 = 0
            r12 = 2131492977(0x7f0c0071, float:1.8609421E38)
            r0.showToast(r7, r12)
            r12 = r8
            r8 = r27
            goto L_0x0305
        L_0x02d3:
            r0 = move-exception
            r7 = 0
            r12 = 2131492977(0x7f0c0071, float:1.8609421E38)
            r16 = r7
            r8 = r15
            r11 = 0
            r15 = r16
        L_0x02de:
            r0.printStackTrace()
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity
            net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()
            r0.showToast(r7, r12)
            goto L_0x0302
        L_0x02eb:
            r0 = move-exception
            r7 = 0
            r12 = 2131492977(0x7f0c0071, float:1.8609421E38)
            r16 = r7
            r8 = r15
            r11 = 0
            r15 = r16
        L_0x02f6:
            r0.printStackTrace()
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity
            net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()
            r0.showToast(r7, r12)
        L_0x0302:
            r12 = r8
            r8 = r16
        L_0x0305:
            if (r13 == 0) goto L_0x030a
            r13.delete()
        L_0x030a:
            if (r9 == 0) goto L_0x030d
            goto L_0x0320
        L_0x030d:
            if (r11 == 0) goto L_0x0315
            if (r15 != 0) goto L_0x0315
            r6.addLastImage(r8, r5)
            goto L_0x0320
        L_0x0315:
            if (r11 == 0) goto L_0x0320
            boolean r0 = r10.isUsingSAF()
            if (r0 == 0) goto L_0x0320
            r6.addLastImageSAF(r15, r5)
        L_0x0320:
            if (r11 == 0) goto L_0x03c5
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity
            net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()
            net.sourceforge.opencamera.cameracontroller.CameraController r0 = r0.getCameraController()
            if (r0 == 0) goto L_0x03c5
            if (r4 == 0) goto L_0x03c5
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity
            net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()
            net.sourceforge.opencamera.cameracontroller.CameraController r0 = r0.getCameraController()
            net.sourceforge.opencamera.cameracontroller.CameraController$Size r0 = r0.getPictureSize()
            int r0 = r0.width
            double r4 = (double) r0
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity
            net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()
            android.view.View r0 = r0.getView()
            int r0 = r0.getWidth()
            double r9 = (double) r0
            java.lang.Double.isNaN(r4)
            java.lang.Double.isNaN(r9)
            double r4 = r4 / r9
            double r4 = java.lang.Math.ceil(r4)
            int r0 = (int) r4
            int r0 = java.lang.Integer.highestOneBit(r0)
            int r2 = r2.sample_factor
            int r0 = r0 * r2
            if (r12 != 0) goto L_0x0383
            android.graphics.BitmapFactory$Options r2 = new android.graphics.BitmapFactory$Options
            r2.<init>()
            r4 = 0
            r2.inMutable = r4
            int r5 = android.os.Build.VERSION.SDK_INT
            r7 = 19
            if (r5 > r7) goto L_0x0377
            r5 = 1
            r2.inPurgeable = r5
        L_0x0377:
            r2.inSampleSize = r0
            int r0 = r3.length
            android.graphics.Bitmap r0 = android.graphics.BitmapFactory.decodeByteArray(r3, r4, r0, r2)
            android.graphics.Bitmap r0 = r1.rotateForExif(r0, r3, r8)
            goto L_0x03b8
        L_0x0383:
            int r2 = r12.getWidth()
            int r3 = r12.getHeight()
            android.graphics.Matrix r4 = new android.graphics.Matrix
            r4.<init>()
            r5 = 1065353216(0x3f800000, float:1.0)
            float r0 = (float) r0
            float r5 = r5 / r0
            r4.postScale(r5, r5)
            r0 = 0
            r5 = 0
            r9 = 1
            r25 = r12
            r26 = r0
            r27 = r5
            r28 = r2
            r29 = r3
            r30 = r4
            r31 = r9
            android.graphics.Bitmap r0 = android.graphics.Bitmap.createBitmap(r25, r26, r27, r28, r29, r30, r31)     // Catch:{ IllegalArgumentException -> 0x03ad }
            goto L_0x03b8
        L_0x03ad:
            r0 = move-exception
            r2 = r0
            java.lang.String r0 = "can't create thumbnail bitmap due to IllegalArgumentException?!"
            android.util.Log.e(r14, r0)
            r2.printStackTrace()
            r0 = r7
        L_0x03b8:
            if (r0 != 0) goto L_0x03bb
            goto L_0x03c5
        L_0x03bb:
            net.sourceforge.opencamera.MainActivity r2 = r1.main_activity
            net.sourceforge.opencamera.ImageSaver$3 r3 = new net.sourceforge.opencamera.ImageSaver$3
            r3.<init>(r6, r0)
            r2.runOnUiThread(r3)
        L_0x03c5:
            if (r12 == 0) goto L_0x03ca
            r12.recycle()
        L_0x03ca:
            if (r8 == 0) goto L_0x03d1
            if (r15 == 0) goto L_0x03d1
            r8.delete()
        L_0x03d1:
            java.lang.System.gc()
            net.sourceforge.opencamera.MainActivity r0 = r1.main_activity
            r2 = 0
            r0.savingImage(r2)
            return r11
        L_0x03db:
            java.lang.RuntimeException r0 = new java.lang.RuntimeException
            r0.<init>()
            throw r0
        L_0x03e1:
            java.lang.RuntimeException r0 = new java.lang.RuntimeException
            r0.<init>()
            goto L_0x03e8
        L_0x03e7:
            throw r0
        L_0x03e8:
            goto L_0x03e7
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.ImageSaver.saveSingleImageNow(net.sourceforge.opencamera.ImageSaver$Request, byte[], android.graphics.Bitmap, java.lang.String, boolean, boolean, boolean, boolean):boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0021  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setExifFromData(net.sourceforge.opencamera.ImageSaver.Request r3, byte[] r4, java.io.File r5) throws java.io.IOException {
        /*
            r2 = this;
            r0 = 0
            java.io.ByteArrayInputStream r1 = new java.io.ByteArrayInputStream     // Catch:{ all -> 0x001d }
            r1.<init>(r4)     // Catch:{ all -> 0x001d }
            android.media.ExifInterface r4 = new android.media.ExifInterface     // Catch:{ all -> 0x001b }
            r4.<init>(r1)     // Catch:{ all -> 0x001b }
            android.media.ExifInterface r0 = new android.media.ExifInterface     // Catch:{ all -> 0x001b }
            java.lang.String r5 = r5.getAbsolutePath()     // Catch:{ all -> 0x001b }
            r0.<init>(r5)     // Catch:{ all -> 0x001b }
            r2.setExif(r3, r4, r0)     // Catch:{ all -> 0x001b }
            r1.close()
            return
        L_0x001b:
            r3 = move-exception
            goto L_0x001f
        L_0x001d:
            r3 = move-exception
            r1 = r0
        L_0x001f:
            if (r1 == 0) goto L_0x0024
            r1.close()
        L_0x0024:
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.ImageSaver.setExifFromData(net.sourceforge.opencamera.ImageSaver$Request, byte[], java.io.File):void");
    }

    private void broadcastSAFFile(Uri uri, boolean z) {
        StorageUtils storageUtils = this.main_activity.getStorageUtils();
        File fileFromDocumentUriSAF = storageUtils.getFileFromDocumentUriSAF(uri, false);
        if (fileFromDocumentUriSAF != null) {
            storageUtils.broadcastFile(fileFromDocumentUriSAF, true, false, true);
            this.main_activity.test_last_saved_image = fileFromDocumentUriSAF.getAbsolutePath();
        } else if (!z) {
            storageUtils.announceUri(uri, true, false);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x001d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setExifFromData(net.sourceforge.opencamera.ImageSaver.Request r3, byte[] r4, java.io.FileDescriptor r5) throws java.io.IOException {
        /*
            r2 = this;
            r0 = 0
            java.io.ByteArrayInputStream r1 = new java.io.ByteArrayInputStream     // Catch:{ all -> 0x0019 }
            r1.<init>(r4)     // Catch:{ all -> 0x0019 }
            android.media.ExifInterface r4 = new android.media.ExifInterface     // Catch:{ all -> 0x0017 }
            r4.<init>(r1)     // Catch:{ all -> 0x0017 }
            android.media.ExifInterface r0 = new android.media.ExifInterface     // Catch:{ all -> 0x0017 }
            r0.<init>(r5)     // Catch:{ all -> 0x0017 }
            r2.setExif(r3, r4, r0)     // Catch:{ all -> 0x0017 }
            r1.close()
            return
        L_0x0017:
            r3 = move-exception
            goto L_0x001b
        L_0x0019:
            r3 = move-exception
            r1 = r0
        L_0x001b:
            if (r1 == 0) goto L_0x0020
            r1.close()
        L_0x0020:
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.ImageSaver.setExifFromData(net.sourceforge.opencamera.ImageSaver$Request, byte[], java.io.FileDescriptor):void");
    }

    private void setExifFromFile(Request request, File file, File file2) throws IOException {
        try {
            setExif(request, new ExifInterface(file.getAbsolutePath()), new ExifInterface(file2.getAbsolutePath()));
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        }
    }

    private void setExif(Request request, ExifInterface exifInterface, ExifInterface exifInterface2) throws IOException {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        String str7;
        String str8;
        String str9;
        String str10;
        String str11;
        String str12;
        String str13;
        String str14;
        String str15;
        String str16;
        String str17;
        String str18;
        String str19;
        String str20;
        String str21;
        String str22;
        String str23;
        String str24;
        String str25;
        String str26;
        String str27;
        String str28;
        String str29;
        String str30;
        String str31;
        String str32;
        String str33;
        String str34;
        String str35;
        String str36;
        String str37;
        String str38;
        String str39;
        String str40;
        String str41;
        String str42;
        String str43;
        String str44;
        Request request2 = request;
        ExifInterface exifInterface3 = exifInterface;
        ExifInterface exifInterface4 = exifInterface2;
        String str45 = "FNumber";
        String attribute = exifInterface3.getAttribute(str45);
        String str46 = "DateTime";
        String attribute2 = exifInterface3.getAttribute(str46);
        String str47 = "ExposureTime";
        String attribute3 = exifInterface3.getAttribute(str47);
        String str48 = "Flash";
        String attribute4 = exifInterface3.getAttribute(str48);
        String str49 = "FocalLength";
        String attribute5 = exifInterface3.getAttribute(str49);
        String str50 = "GPSAltitude";
        String attribute6 = exifInterface3.getAttribute(str50);
        String str51 = "GPSAltitudeRef";
        String attribute7 = exifInterface3.getAttribute(str51);
        String str52 = str51;
        String str53 = "GPSDateStamp";
        String str54 = attribute7;
        String attribute8 = exifInterface3.getAttribute(str53);
        String str55 = str53;
        String str56 = "GPSLatitude";
        String str57 = attribute8;
        String attribute9 = exifInterface3.getAttribute(str56);
        String str58 = str56;
        String str59 = "GPSLatitudeRef";
        String str60 = attribute9;
        String attribute10 = exifInterface3.getAttribute(str59);
        String str61 = str59;
        String str62 = "GPSLongitude";
        String str63 = attribute10;
        String attribute11 = exifInterface3.getAttribute(str62);
        String str64 = str62;
        String str65 = "GPSLongitudeRef";
        String str66 = attribute11;
        String attribute12 = exifInterface3.getAttribute(str65);
        String str67 = str65;
        String str68 = "GPSProcessingMethod";
        String str69 = attribute12;
        String attribute13 = exifInterface3.getAttribute(str68);
        String str70 = str68;
        String str71 = "GPSTimeStamp";
        String str72 = attribute13;
        String attribute14 = exifInterface3.getAttribute(str71);
        String str73 = str71;
        String attribute15 = exifInterface3.getAttribute("ISOSpeedRatings");
        String attribute16 = exifInterface3.getAttribute("Make");
        String attribute17 = exifInterface3.getAttribute("Model");
        String attribute18 = exifInterface3.getAttribute("WhiteBalance");
        String str74 = attribute14;
        if (VERSION.SDK_INT >= 23) {
            String attribute19 = exifInterface3.getAttribute(TAG_DATETIME_DIGITIZED);
            String attribute20 = exifInterface3.getAttribute("SubSecTime");
            String str75 = attribute19;
            String attribute21 = exifInterface3.getAttribute("SubSecTimeDigitized");
            str = exifInterface3.getAttribute("SubSecTimeOriginal");
            str4 = str75;
            str2 = attribute21;
            str3 = attribute20;
        } else {
            str4 = null;
            str3 = null;
            str2 = null;
            str = null;
        }
        String str76 = str4;
        if (VERSION.SDK_INT >= 24) {
            String attribute22 = exifInterface3.getAttribute("ApertureValue");
            String attribute23 = exifInterface3.getAttribute("BrightnessValue");
            str42 = exifInterface3.getAttribute("CFAPattern");
            String str77 = attribute23;
            String attribute24 = exifInterface3.getAttribute("ColorSpace");
            String attribute25 = exifInterface3.getAttribute("ComponentsConfiguration");
            String attribute26 = exifInterface3.getAttribute("CompressedBitsPerPixel");
            String attribute27 = exifInterface3.getAttribute("Compression");
            String attribute28 = exifInterface3.getAttribute("Contrast");
            String attribute29 = exifInterface3.getAttribute(TAG_DATETIME_ORIGINAL);
            String attribute30 = exifInterface3.getAttribute("DeviceSettingDescription");
            String attribute31 = exifInterface3.getAttribute("DigitalZoomRatio");
            String attribute32 = exifInterface3.getAttribute("ExposureBiasValue");
            String attribute33 = exifInterface3.getAttribute("ExposureIndex");
            String attribute34 = exifInterface3.getAttribute("ExposureMode");
            String attribute35 = exifInterface3.getAttribute("ExposureProgram");
            String attribute36 = exifInterface3.getAttribute("FlashEnergy");
            String attribute37 = exifInterface3.getAttribute("FocalLengthIn35mmFilm");
            String attribute38 = exifInterface3.getAttribute("FocalPlaneResolutionUnit");
            String attribute39 = exifInterface3.getAttribute("FocalPlaneXResolution");
            String attribute40 = exifInterface3.getAttribute("FocalPlaneYResolution");
            String attribute41 = exifInterface3.getAttribute("GainControl");
            String attribute42 = exifInterface3.getAttribute("GPSAreaInformation");
            String attribute43 = exifInterface3.getAttribute("GPSDifferential");
            String attribute44 = exifInterface3.getAttribute("GPSDOP");
            String attribute45 = exifInterface3.getAttribute("GPSMeasureMode");
            String attribute46 = exifInterface3.getAttribute("ImageDescription");
            String attribute47 = exifInterface3.getAttribute("LightSource");
            String attribute48 = exifInterface3.getAttribute("MakerNote");
            String attribute49 = exifInterface3.getAttribute("MaxApertureValue");
            String attribute50 = exifInterface3.getAttribute("MeteringMode");
            String attribute51 = exifInterface3.getAttribute("OECF");
            String attribute52 = exifInterface3.getAttribute("PhotometricInterpretation");
            String attribute53 = exifInterface3.getAttribute("Saturation");
            String attribute54 = exifInterface3.getAttribute("SceneCaptureType");
            String attribute55 = exifInterface3.getAttribute("SceneType");
            String attribute56 = exifInterface3.getAttribute("SensingMethod");
            String attribute57 = exifInterface3.getAttribute("Sharpness");
            String attribute58 = exifInterface3.getAttribute("ShutterSpeedValue");
            String attribute59 = exifInterface3.getAttribute("Software");
            str5 = exifInterface3.getAttribute("UserComment");
            str44 = attribute22;
            str43 = str77;
            str41 = attribute24;
            str40 = attribute25;
            str39 = attribute26;
            str38 = attribute27;
            str37 = attribute28;
            str36 = attribute29;
            str35 = attribute30;
            str34 = attribute31;
            str33 = attribute32;
            str32 = attribute33;
            str31 = attribute34;
            str30 = attribute35;
            str29 = attribute36;
            str28 = attribute37;
            str27 = attribute38;
            str26 = attribute39;
            str25 = attribute40;
            str24 = attribute41;
            str23 = attribute42;
            str22 = attribute43;
            str21 = attribute44;
            str20 = attribute45;
            str19 = attribute46;
            str18 = attribute47;
            str17 = attribute48;
            str16 = attribute49;
            str15 = attribute50;
            str14 = attribute51;
            str13 = attribute52;
            str12 = attribute53;
            str11 = attribute54;
            str10 = attribute55;
            str9 = attribute56;
            str8 = attribute57;
            str7 = attribute58;
            str6 = attribute59;
        } else {
            str44 = null;
            str43 = null;
            str42 = null;
            str41 = null;
            str40 = null;
            str39 = null;
            str38 = null;
            str37 = null;
            str36 = null;
            str35 = null;
            str34 = null;
            str33 = null;
            str32 = null;
            str31 = null;
            str30 = null;
            str29 = null;
            str28 = null;
            str27 = null;
            str26 = null;
            str25 = null;
            str24 = null;
            str23 = null;
            str22 = null;
            str21 = null;
            str20 = null;
            str19 = null;
            str18 = null;
            str17 = null;
            str16 = null;
            str15 = null;
            str14 = null;
            str13 = null;
            str12 = null;
            str11 = null;
            str10 = null;
            str9 = null;
            str8 = null;
            str7 = null;
            str6 = null;
            str5 = null;
        }
        if (attribute != null) {
            exifInterface4.setAttribute(str45, attribute);
        }
        if (attribute2 != null) {
            exifInterface4.setAttribute(str46, attribute2);
        }
        if (attribute3 != null) {
            exifInterface4.setAttribute(str47, attribute3);
        }
        if (attribute4 != null) {
            exifInterface4.setAttribute(str48, attribute4);
        }
        if (attribute5 != null) {
            exifInterface4.setAttribute(str49, attribute5);
        }
        if (attribute6 != null) {
            exifInterface4.setAttribute(str50, attribute6);
        }
        if (str54 != null) {
            exifInterface4.setAttribute(str52, str54);
        }
        if (str57 != null) {
            exifInterface4.setAttribute(str55, str57);
        }
        if (str60 != null) {
            exifInterface4.setAttribute(str58, str60);
        }
        if (str63 != null) {
            exifInterface4.setAttribute(str61, str63);
        }
        if (str66 != null) {
            exifInterface4.setAttribute(str64, str66);
        }
        if (str69 != null) {
            exifInterface4.setAttribute(str67, str69);
        }
        if (str72 != null) {
            exifInterface4.setAttribute(str70, str72);
        }
        if (str74 != null) {
            exifInterface4.setAttribute(str73, str74);
        }
        if (attribute15 != null) {
            exifInterface4.setAttribute("ISOSpeedRatings", attribute15);
        }
        if (attribute16 != null) {
            exifInterface4.setAttribute("Make", attribute16);
        }
        if (attribute17 != null) {
            exifInterface4.setAttribute("Model", attribute17);
        }
        if (attribute18 != null) {
            exifInterface4.setAttribute("WhiteBalance", attribute18);
        }
        if (VERSION.SDK_INT >= 23) {
            if (str76 != null) {
                exifInterface4.setAttribute(TAG_DATETIME_DIGITIZED, str76);
            }
            if (str3 != null) {
                exifInterface4.setAttribute("SubSecTime", str3);
            }
            String str78 = str2;
            if (str78 != null) {
                exifInterface4.setAttribute("SubSecTimeDigitized", str78);
            }
            String str79 = str;
            if (str79 != null) {
                exifInterface4.setAttribute("SubSecTimeOriginal", str79);
            }
        }
        if (VERSION.SDK_INT >= 24) {
            if (str44 != null) {
                exifInterface4.setAttribute("ApertureValue", str44);
            }
            if (str43 != null) {
                exifInterface4.setAttribute("BrightnessValue", str43);
            }
            if (str42 != null) {
                exifInterface4.setAttribute("CFAPattern", str42);
            }
            String str80 = str41;
            if (str80 != null) {
                exifInterface4.setAttribute("ColorSpace", str80);
            }
            String str81 = str40;
            if (str81 != null) {
                exifInterface4.setAttribute("ComponentsConfiguration", str81);
            }
            String str82 = str39;
            if (str82 != null) {
                exifInterface4.setAttribute("CompressedBitsPerPixel", str82);
            }
            String str83 = str38;
            if (str83 != null) {
                exifInterface4.setAttribute("Compression", str83);
            }
            String str84 = str37;
            if (str84 != null) {
                exifInterface4.setAttribute("Contrast", str84);
            }
            String str85 = str36;
            if (str85 != null) {
                exifInterface4.setAttribute(TAG_DATETIME_ORIGINAL, str85);
            }
            String str86 = str35;
            if (str86 != null) {
                exifInterface4.setAttribute("DeviceSettingDescription", str86);
            }
            String str87 = str34;
            if (str87 != null) {
                exifInterface4.setAttribute("DigitalZoomRatio", str87);
            }
            String str88 = str33;
            if (str88 != null) {
                exifInterface4.setAttribute("ExposureBiasValue", str88);
            }
            String str89 = str32;
            if (str89 != null) {
                exifInterface4.setAttribute("ExposureIndex", str89);
            }
            String str90 = str31;
            if (str90 != null) {
                exifInterface4.setAttribute("ExposureMode", str90);
            }
            String str91 = str30;
            if (str91 != null) {
                exifInterface4.setAttribute("ExposureProgram", str91);
            }
            String str92 = str29;
            if (str92 != null) {
                exifInterface4.setAttribute("FlashEnergy", str92);
            }
            String str93 = str28;
            if (str93 != null) {
                exifInterface4.setAttribute("FocalLengthIn35mmFilm", str93);
            }
            String str94 = str27;
            if (str94 != null) {
                exifInterface4.setAttribute("FocalPlaneResolutionUnit", str94);
            }
            String str95 = str26;
            if (str95 != null) {
                exifInterface4.setAttribute("FocalPlaneXResolution", str95);
            }
            String str96 = str25;
            if (str96 != null) {
                exifInterface4.setAttribute("FocalPlaneYResolution", str96);
            }
            String str97 = str24;
            if (str97 != null) {
                exifInterface4.setAttribute("GainControl", str97);
            }
            String str98 = str23;
            if (str98 != null) {
                exifInterface4.setAttribute("GPSAreaInformation", str98);
            }
            String str99 = str22;
            if (str99 != null) {
                exifInterface4.setAttribute("GPSDifferential", str99);
            }
            String str100 = str21;
            if (str100 != null) {
                exifInterface4.setAttribute("GPSDOP", str100);
            }
            String str101 = str20;
            if (str101 != null) {
                exifInterface4.setAttribute("GPSMeasureMode", str101);
            }
            String str102 = str19;
            if (str102 != null) {
                exifInterface4.setAttribute("ImageDescription", str102);
            }
            String str103 = str18;
            if (str103 != null) {
                exifInterface4.setAttribute("LightSource", str103);
            }
            String str104 = str17;
            if (str104 != null) {
                exifInterface4.setAttribute("MakerNote", str104);
            }
            String str105 = str16;
            if (str105 != null) {
                exifInterface4.setAttribute("MaxApertureValue", str105);
            }
            String str106 = str15;
            if (str106 != null) {
                exifInterface4.setAttribute("MeteringMode", str106);
            }
            String str107 = str14;
            if (str107 != null) {
                exifInterface4.setAttribute("OECF", str107);
            }
            String str108 = str13;
            if (str108 != null) {
                exifInterface4.setAttribute("PhotometricInterpretation", str108);
            }
            String str109 = str12;
            if (str109 != null) {
                exifInterface4.setAttribute("Saturation", str109);
            }
            String str110 = str11;
            if (str110 != null) {
                exifInterface4.setAttribute("SceneCaptureType", str110);
            }
            String str111 = str10;
            if (str111 != null) {
                exifInterface4.setAttribute("SceneType", str111);
            }
            String str112 = str9;
            if (str112 != null) {
                exifInterface4.setAttribute("SensingMethod", str112);
            }
            String str113 = str8;
            if (str113 != null) {
                exifInterface4.setAttribute("Sharpness", str113);
            }
            String str114 = str7;
            if (str114 != null) {
                exifInterface4.setAttribute("ShutterSpeedValue", str114);
            }
            String str115 = str6;
            if (str115 != null) {
                exifInterface4.setAttribute("Software", str115);
            }
            String str116 = str5;
            if (str116 != null) {
                exifInterface4.setAttribute("UserComment", str116);
            }
        }
        Request request3 = request;
        boolean z = request3.type == Type.JPEG;
        boolean z2 = request3.using_camera2;
        Date date = request3.current_date;
        boolean z3 = request3.store_location;
        boolean z4 = request3.store_geo_direction;
        double d = request3.geo_direction;
        String str117 = request3.custom_tag_artist;
        String str118 = request3.custom_tag_copyright;
        modifyExif(exifInterface2, z, z2, date, z3, z4, d, str117, str118);
        setDateTimeExif(exifInterface4);
        exifInterface2.saveAttributes();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00c0, code lost:
        if (r2 != null) goto L_0x00c2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00c2, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00df, code lost:
        if (r2 != null) goto L_0x00c2;
     */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00b8 A[SYNTHETIC, Splitter:B:53:0x00b8] */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00d7 A[SYNTHETIC, Splitter:B:65:0x00d7] */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x00ef A[SYNTHETIC, Splitter:B:75:0x00ef] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x00f9  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:62:0x00c9=Splitter:B:62:0x00c9, B:50:0x00aa=Splitter:B:50:0x00aa} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean saveImageNowRaw(net.sourceforge.opencamera.ImageSaver.Request r10) {
        /*
            r9 = this;
            int r0 = android.os.Build.VERSION.SDK_INT
            r1 = 0
            r2 = 21
            if (r0 >= r2) goto L_0x0008
            return r1
        L_0x0008:
            net.sourceforge.opencamera.MainActivity r0 = r9.main_activity
            net.sourceforge.opencamera.StorageUtils r0 = r0.getStorageUtils()
            net.sourceforge.opencamera.MainActivity r2 = r9.main_activity
            r3 = 1
            r2.savingImage(r3)
            net.sourceforge.opencamera.cameracontroller.RawImage r2 = r10.raw_image
            r4 = 2131492978(0x7f0c0072, float:1.8609423E38)
            r5 = 0
            java.lang.String r6 = "_"
            boolean r7 = r10.force_suffix     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            if (r7 == 0) goto L_0x0032
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            r7.<init>()     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            r7.append(r6)     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            int r6 = r10.suffix_offset     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            r7.append(r6)     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            java.lang.String r6 = r7.toString()     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            goto L_0x0034
        L_0x0032:
            java.lang.String r6 = ""
        L_0x0034:
            boolean r7 = r0.isUsingSAF()     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            java.lang.String r8 = "dng"
            if (r7 == 0) goto L_0x0045
            java.util.Date r10 = r10.current_date     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            android.net.Uri r10 = r0.createOutputMediaFileSAF(r3, r6, r8, r10)     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            r6 = r10
            r10 = r5
            goto L_0x004c
        L_0x0045:
            java.util.Date r10 = r10.current_date     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            java.io.File r10 = r0.createOutputMediaFile(r3, r6, r8, r10)     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            r6 = r5
        L_0x004c:
            if (r10 == 0) goto L_0x0054
            java.io.FileOutputStream r7 = new java.io.FileOutputStream     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            r7.<init>(r10)     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            goto L_0x005e
        L_0x0054:
            net.sourceforge.opencamera.MainActivity r7 = r9.main_activity     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            android.content.ContentResolver r7 = r7.getContentResolver()     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
            java.io.OutputStream r7 = r7.openOutputStream(r6)     // Catch:{ FileNotFoundException -> 0x00c6, IOException -> 0x00a7, all -> 0x00a5 }
        L_0x005e:
            r2.writeImage(r7)     // Catch:{ FileNotFoundException -> 0x00a3, IOException -> 0x00a1 }
            r2.close()     // Catch:{ FileNotFoundException -> 0x00a3, IOException -> 0x00a1 }
            r7.close()     // Catch:{ FileNotFoundException -> 0x009e, IOException -> 0x009b, all -> 0x0098 }
            net.sourceforge.opencamera.MainActivity r2 = r9.main_activity     // Catch:{ FileNotFoundException -> 0x0094, IOException -> 0x0090, all -> 0x008c }
            net.sourceforge.opencamera.MyApplicationInterface r2 = r2.getApplicationInterface()     // Catch:{ FileNotFoundException -> 0x0094, IOException -> 0x0090, all -> 0x008c }
            boolean r7 = r2.isRawOnly()     // Catch:{ FileNotFoundException -> 0x0094, IOException -> 0x0090, all -> 0x008c }
            if (r6 != 0) goto L_0x0077
            r2.addLastImage(r10, r7)     // Catch:{ FileNotFoundException -> 0x0094, IOException -> 0x0090, all -> 0x008c }
            goto L_0x0080
        L_0x0077:
            boolean r8 = r0.isUsingSAF()     // Catch:{ FileNotFoundException -> 0x0094, IOException -> 0x0090, all -> 0x008c }
            if (r8 == 0) goto L_0x0080
            r2.addLastImageSAF(r6, r7)     // Catch:{ FileNotFoundException -> 0x0094, IOException -> 0x0090, all -> 0x008c }
        L_0x0080:
            if (r6 != 0) goto L_0x0087
            r0.broadcastFile(r10, r3, r1, r1)     // Catch:{ FileNotFoundException -> 0x0094, IOException -> 0x0090, all -> 0x008c }
            goto L_0x00e2
        L_0x0087:
            r0.broadcastUri(r6, r3, r1, r1)     // Catch:{ FileNotFoundException -> 0x0094, IOException -> 0x0090, all -> 0x008c }
            goto L_0x00e2
        L_0x008c:
            r10 = move-exception
            r2 = r5
            goto L_0x00ed
        L_0x0090:
            r10 = move-exception
            r2 = r5
            r7 = r2
            goto L_0x00aa
        L_0x0094:
            r10 = move-exception
            r2 = r5
            r7 = r2
            goto L_0x00c9
        L_0x0098:
            r10 = move-exception
            r2 = r5
            goto L_0x00ec
        L_0x009b:
            r10 = move-exception
            r2 = r5
            goto L_0x00a9
        L_0x009e:
            r10 = move-exception
            r2 = r5
            goto L_0x00c8
        L_0x00a1:
            r10 = move-exception
            goto L_0x00a9
        L_0x00a3:
            r10 = move-exception
            goto L_0x00c8
        L_0x00a5:
            r10 = move-exception
            goto L_0x00ed
        L_0x00a7:
            r10 = move-exception
            r7 = r5
        L_0x00a9:
            r3 = 0
        L_0x00aa:
            r10.printStackTrace()     // Catch:{ all -> 0x00eb }
            net.sourceforge.opencamera.MainActivity r10 = r9.main_activity     // Catch:{ all -> 0x00eb }
            net.sourceforge.opencamera.preview.Preview r10 = r10.getPreview()     // Catch:{ all -> 0x00eb }
            r10.showToast(r5, r4)     // Catch:{ all -> 0x00eb }
            if (r7 == 0) goto L_0x00c0
            r7.close()     // Catch:{ IOException -> 0x00bc }
            goto L_0x00c0
        L_0x00bc:
            r10 = move-exception
            r10.printStackTrace()
        L_0x00c0:
            if (r2 == 0) goto L_0x00e2
        L_0x00c2:
            r2.close()
            goto L_0x00e2
        L_0x00c6:
            r10 = move-exception
            r7 = r5
        L_0x00c8:
            r3 = 0
        L_0x00c9:
            r10.printStackTrace()     // Catch:{ all -> 0x00eb }
            net.sourceforge.opencamera.MainActivity r10 = r9.main_activity     // Catch:{ all -> 0x00eb }
            net.sourceforge.opencamera.preview.Preview r10 = r10.getPreview()     // Catch:{ all -> 0x00eb }
            r10.showToast(r5, r4)     // Catch:{ all -> 0x00eb }
            if (r7 == 0) goto L_0x00df
            r7.close()     // Catch:{ IOException -> 0x00db }
            goto L_0x00df
        L_0x00db:
            r10 = move-exception
            r10.printStackTrace()
        L_0x00df:
            if (r2 == 0) goto L_0x00e2
            goto L_0x00c2
        L_0x00e2:
            java.lang.System.gc()
            net.sourceforge.opencamera.MainActivity r10 = r9.main_activity
            r10.savingImage(r1)
            return r3
        L_0x00eb:
            r10 = move-exception
        L_0x00ec:
            r5 = r7
        L_0x00ed:
            if (r5 == 0) goto L_0x00f7
            r5.close()     // Catch:{ IOException -> 0x00f3 }
            goto L_0x00f7
        L_0x00f3:
            r0 = move-exception
            r0.printStackTrace()
        L_0x00f7:
            if (r2 == 0) goto L_0x00fc
            r2.close()
        L_0x00fc:
            goto L_0x00fe
        L_0x00fd:
            throw r10
        L_0x00fe:
            goto L_0x00fd
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.ImageSaver.saveImageNowRaw(net.sourceforge.opencamera.ImageSaver$Request):boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x004d A[Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x007e A[SYNTHETIC, Splitter:B:37:0x007e] */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0090 A[SYNTHETIC, Splitter:B:47:0x0090] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x009a A[SYNTHETIC, Splitter:B:53:0x009a] */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00a0 A[SYNTHETIC, Splitter:B:57:0x00a0] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:50:0x0095=Splitter:B:50:0x0095, B:44:0x008b=Splitter:B:44:0x008b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.graphics.Bitmap rotateForExif(android.graphics.Bitmap r10, byte[] r11, java.io.File r12) {
        /*
            r9 = this;
            r0 = 0
            int r1 = android.os.Build.VERSION.SDK_INT     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            r2 = 24
            if (r1 < r2) goto L_0x001f
            java.io.ByteArrayInputStream r12 = new java.io.ByteArrayInputStream     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            r12.<init>(r11)     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            android.media.ExifInterface r11 = new android.media.ExifInterface     // Catch:{ IOException -> 0x001b, NoClassDefFoundError -> 0x0017, all -> 0x0013 }
            r11.<init>(r12)     // Catch:{ IOException -> 0x001b, NoClassDefFoundError -> 0x0017, all -> 0x0013 }
            r0 = r12
            goto L_0x002a
        L_0x0013:
            r10 = move-exception
            r0 = r12
            goto L_0x009e
        L_0x0017:
            r11 = move-exception
            r0 = r12
            goto L_0x008b
        L_0x001b:
            r11 = move-exception
            r0 = r12
            goto L_0x0095
        L_0x001f:
            if (r12 == 0) goto L_0x0087
            android.media.ExifInterface r11 = new android.media.ExifInterface     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            java.lang.String r12 = r12.getAbsolutePath()     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            r11.<init>(r12)     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
        L_0x002a:
            java.lang.String r12 = "Orientation"
            r1 = 0
            int r11 = r11.getAttributeInt(r12, r1)     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            r12 = 1
            if (r11 == 0) goto L_0x004a
            if (r11 == r12) goto L_0x004a
            r2 = 3
            if (r11 == r2) goto L_0x0047
            r2 = 6
            if (r11 == r2) goto L_0x0044
            r2 = 8
            if (r11 == r2) goto L_0x0041
            goto L_0x004a
        L_0x0041:
            r1 = 270(0x10e, float:3.78E-43)
            goto L_0x004b
        L_0x0044:
            r1 = 90
            goto L_0x004b
        L_0x0047:
            r1 = 180(0xb4, float:2.52E-43)
            goto L_0x004b
        L_0x004a:
            r12 = 0
        L_0x004b:
            if (r12 == 0) goto L_0x007c
            android.graphics.Matrix r7 = new android.graphics.Matrix     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            r7.<init>()     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            float r11 = (float) r1     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            int r12 = r10.getWidth()     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            float r12 = (float) r12     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            r1 = 1056964608(0x3f000000, float:0.5)
            float r12 = r12 * r1
            int r2 = r10.getHeight()     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            float r2 = (float) r2     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            float r2 = r2 * r1
            r7.setRotate(r11, r12, r2)     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            r3 = 0
            r4 = 0
            int r5 = r10.getWidth()     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            int r6 = r10.getHeight()     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            r8 = 1
            r2 = r10
            android.graphics.Bitmap r11 = android.graphics.Bitmap.createBitmap(r2, r3, r4, r5, r6, r7, r8)     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            if (r11 == r10) goto L_0x007c
            r10.recycle()     // Catch:{ IOException -> 0x0094, NoClassDefFoundError -> 0x008a }
            r10 = r11
        L_0x007c:
            if (r0 == 0) goto L_0x009d
            r0.close()     // Catch:{ IOException -> 0x0082 }
            goto L_0x009d
        L_0x0082:
            r11 = move-exception
            r11.printStackTrace()
            goto L_0x009d
        L_0x0087:
            return r10
        L_0x0088:
            r10 = move-exception
            goto L_0x009e
        L_0x008a:
            r11 = move-exception
        L_0x008b:
            r11.printStackTrace()     // Catch:{ all -> 0x0088 }
            if (r0 == 0) goto L_0x009d
            r0.close()     // Catch:{ IOException -> 0x0082 }
            goto L_0x009d
        L_0x0094:
            r11 = move-exception
        L_0x0095:
            r11.printStackTrace()     // Catch:{ all -> 0x0088 }
            if (r0 == 0) goto L_0x009d
            r0.close()     // Catch:{ IOException -> 0x0082 }
        L_0x009d:
            return r10
        L_0x009e:
            if (r0 == 0) goto L_0x00a8
            r0.close()     // Catch:{ IOException -> 0x00a4 }
            goto L_0x00a8
        L_0x00a4:
            r11 = move-exception
            r11.printStackTrace()
        L_0x00a8:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.ImageSaver.rotateForExif(android.graphics.Bitmap, byte[], java.io.File):android.graphics.Bitmap");
    }

    private Bitmap loadBitmapWithRotation(byte[] bArr, boolean z, File file) {
        Bitmap loadBitmap = loadBitmap(bArr, z, 1);
        return loadBitmap != null ? rotateForExif(loadBitmap, bArr, file) : loadBitmap;
    }

    private ExifInterface createExifInterface(File file, Uri uri) throws IOException {
        if (file != null) {
            return new ExifInterface(file.getAbsolutePath());
        }
        if (VERSION.SDK_INT >= 24) {
            ParcelFileDescriptor openFileDescriptor = this.main_activity.getContentResolver().openFileDescriptor(uri, "rw");
            if (openFileDescriptor != null) {
                return new ExifInterface(openFileDescriptor.getFileDescriptor());
            }
            StringBuilder sb = new StringBuilder();
            sb.append("failed to create ParcelFileDescriptor for saveUri: ");
            sb.append(uri);
            Log.e(TAG, sb.toString());
            return null;
        }
        throw new RuntimeException("picFile==null but Android version is not 7 or later");
    }

    private void updateExif(Request request, File file, Uri uri) throws IOException {
        boolean z = true;
        boolean z2 = false;
        if (request.store_geo_direction || hasCustomExif(request.custom_tag_artist, request.custom_tag_copyright)) {
            System.currentTimeMillis();
            try {
                ExifInterface createExifInterface = createExifInterface(file, uri);
                if (createExifInterface != null) {
                    if (request.type == Type.JPEG) {
                        z2 = true;
                    }
                    modifyExif(createExifInterface, z2, request.using_camera2, request.current_date, request.store_location, request.store_geo_direction, request.geo_direction, request.custom_tag_artist, request.custom_tag_copyright);
                    createExifInterface.saveAttributes();
                }
            } catch (NoClassDefFoundError e) {
                e.printStackTrace();
            }
        } else {
            if (request.type != Type.JPEG) {
                z = false;
            }
            if (needGPSTimestampHack(z, request.using_camera2, request.store_location)) {
                try {
                    ExifInterface createExifInterface2 = createExifInterface(file, uri);
                    if (createExifInterface2 != null) {
                        fixGPSTimestamp(createExifInterface2, request.current_date);
                        createExifInterface2.saveAttributes();
                    }
                } catch (NoClassDefFoundError e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    private void modifyExif(ExifInterface exifInterface, boolean z, boolean z2, Date date, boolean z3, boolean z4, double d, String str, String str2) {
        setGPSDirectionExif(exifInterface, z4, d);
        setCustomExif(exifInterface, str, str2);
        if (needGPSTimestampHack(z, z2, z3)) {
            fixGPSTimestamp(exifInterface, date);
        }
    }

    private void setGPSDirectionExif(ExifInterface exifInterface, boolean z, double d) {
        if (z) {
            float degrees = (float) Math.toDegrees(d);
            if (degrees < 0.0f) {
                degrees += 360.0f;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(Math.round(degrees * 100.0f));
            sb.append("/100");
            exifInterface.setAttribute(TAG_GPS_IMG_DIRECTION, sb.toString());
            exifInterface.setAttribute(TAG_GPS_IMG_DIRECTION_REF, "M");
        }
    }

    private boolean hasCustomExif(String str, String str2) {
        if (VERSION.SDK_INT >= 24 && str != null && str.length() > 0) {
            return true;
        }
        if (VERSION.SDK_INT < 24 || str2 == null || str2.length() <= 0) {
            return false;
        }
        return true;
    }

    private void setCustomExif(ExifInterface exifInterface, String str, String str2) {
        if (VERSION.SDK_INT >= 24 && str != null && str.length() > 0) {
            exifInterface.setAttribute("Artist", str);
        }
        if (VERSION.SDK_INT >= 24 && str2 != null && str2.length() > 0) {
            exifInterface.setAttribute("Copyright", str2);
        }
    }

    private void setDateTimeExif(ExifInterface exifInterface) {
        String attribute = exifInterface.getAttribute("DateTime");
        if (attribute != null) {
            exifInterface.setAttribute(TAG_DATETIME_ORIGINAL, attribute);
            exifInterface.setAttribute(TAG_DATETIME_DIGITIZED, attribute);
        }
    }

    private void fixGPSTimestamp(ExifInterface exifInterface, Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd", Locale.US);
        String str = "UTC";
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(str));
        String format = simpleDateFormat.format(date);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH:mm:ss", Locale.US);
        simpleDateFormat2.setTimeZone(TimeZone.getTimeZone(str));
        String format2 = simpleDateFormat2.format(date);
        exifInterface.setAttribute("GPSDateStamp", format);
        exifInterface.setAttribute("GPSTimeStamp", format2);
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [java.io.OutputStream] */
    /* JADX WARNING: type inference failed for: r1v0, types: [java.io.InputStream] */
    /* JADX WARNING: type inference failed for: r1v1 */
    /* JADX WARNING: type inference failed for: r1v2, types: [java.io.FileInputStream, java.io.InputStream] */
    /* JADX WARNING: type inference failed for: r1v3 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0031  */
    /* JADX WARNING: Unknown variable types count: 3 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void copyFileToUri(android.content.Context r3, android.net.Uri r4, java.io.File r5) throws java.io.IOException {
        /*
            r2 = this;
            r0 = 0
            java.io.FileInputStream r1 = new java.io.FileInputStream     // Catch:{ all -> 0x0028 }
            r1.<init>(r5)     // Catch:{ all -> 0x0028 }
            android.content.ContentResolver r3 = r3.getContentResolver()     // Catch:{ all -> 0x0026 }
            java.io.OutputStream r0 = r3.openOutputStream(r4)     // Catch:{ all -> 0x0026 }
            r3 = 1024(0x400, float:1.435E-42)
            byte[] r3 = new byte[r3]     // Catch:{ all -> 0x0026 }
        L_0x0012:
            int r4 = r1.read(r3)     // Catch:{ all -> 0x0026 }
            if (r4 <= 0) goto L_0x001d
            r5 = 0
            r0.write(r3, r5, r4)     // Catch:{ all -> 0x0026 }
            goto L_0x0012
        L_0x001d:
            r1.close()
            if (r0 == 0) goto L_0x0025
            r0.close()
        L_0x0025:
            return
        L_0x0026:
            r3 = move-exception
            goto L_0x002a
        L_0x0028:
            r3 = move-exception
            r1 = r0
        L_0x002a:
            if (r1 == 0) goto L_0x002f
            r1.close()
        L_0x002f:
            if (r0 == 0) goto L_0x0034
            r0.close()
        L_0x0034:
            goto L_0x0036
        L_0x0035:
            throw r3
        L_0x0036:
            goto L_0x0035
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.ImageSaver.copyFileToUri(android.content.Context, android.net.Uri, java.io.File):void");
    }

    /* access modifiers changed from: 0000 */
    public HDRProcessor getHDRProcessor() {
        return this.hdrProcessor;
    }

    public PanoramaProcessor getPanoramaProcessor() {
        return this.panoramaProcessor;
    }
}
