package net.sourceforge.opencamera;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.support.p000v4.media.session.PlaybackStateCompat;
import android.system.Os;
import android.system.StructStatVfs;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class StorageUtils {
    static final int MEDIA_TYPE_GYRO_INFO = 4;
    static final int MEDIA_TYPE_IMAGE = 1;
    static final int MEDIA_TYPE_PREFS = 3;
    static final int MEDIA_TYPE_VIDEO = 2;
    private static final String TAG = "StorageUtils";
    private static final File base_folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    /* access modifiers changed from: private */
    public final MyApplicationInterface applicationInterface;
    /* access modifiers changed from: private */
    public final Context context;
    public volatile boolean failed_to_scan;
    /* access modifiers changed from: private */
    public Uri last_media_scanned;

    static class Media {
        final long date;

        /* renamed from: id */
        final long f18id;
        final int orientation;
        final String path;
        final Uri uri;
        final boolean video;

        Media(long j, boolean z, Uri uri2, long j2, int i, String str) {
            this.f18id = j;
            this.video = z;
            this.uri = uri2;
            this.date = j2;
            this.orientation = i;
            this.path = str;
        }
    }

    StorageUtils(Context context2, MyApplicationInterface myApplicationInterface) {
        this.context = context2;
        this.applicationInterface = myApplicationInterface;
    }

    /* access modifiers changed from: 0000 */
    public Uri getLastMediaScanned() {
        return this.last_media_scanned;
    }

    /* access modifiers changed from: 0000 */
    public void clearLastMediaScanned() {
        this.last_media_scanned = null;
    }

    /* access modifiers changed from: 0000 */
    public void announceUri(Uri uri, boolean z, boolean z2) {
        if (VERSION.SDK_INT < 24) {
            if (z) {
                this.context.sendBroadcast(new Intent("android.hardware.action.NEW_PICTURE", uri));
                this.context.sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", uri));
            } else if (z2) {
                this.context.sendBroadcast(new Intent("android.hardware.action.NEW_VIDEO", uri));
            }
        }
    }

    public void broadcastFile(File file, boolean z, boolean z2, boolean z3) {
        if (!file.isDirectory()) {
            this.failed_to_scan = true;
            Context context2 = this.context;
            String[] strArr = {file.getAbsolutePath()};
            final boolean z4 = z3;
            final boolean z5 = z;
            final boolean z6 = z2;
            final File file2 = file;
            C03211 r3 = new OnScanCompletedListener() {
                public void onScanCompleted(String str, Uri uri) {
                    StorageUtils storageUtils = StorageUtils.this;
                    storageUtils.failed_to_scan = false;
                    if (z4) {
                        storageUtils.last_media_scanned = uri;
                    }
                    StorageUtils.this.announceUri(uri, z5, z6);
                    StorageUtils.this.applicationInterface.scannedFile(file2, uri);
                    Activity activity = (Activity) StorageUtils.this.context;
                    if ("android.media.action.VIDEO_CAPTURE".equals(activity.getIntent().getAction())) {
                        Intent intent = new Intent();
                        intent.setData(uri);
                        activity.setResult(-1, intent);
                        activity.finish();
                    }
                }
            };
            MediaScannerConnection.scanFile(context2, strArr, null, r3);
        }
    }

    public File broadcastUri(Uri uri, boolean z, boolean z2, boolean z3) {
        File fileFromDocumentUriSAF = getFileFromDocumentUriSAF(uri, false);
        if (fileFromDocumentUriSAF != null) {
            broadcastFile(fileFromDocumentUriSAF, z, z2, z3);
            return fileFromDocumentUriSAF;
        }
        announceUri(uri, z, z2);
        return null;
    }

    /* access modifiers changed from: 0000 */
    public boolean isUsingSAF() {
        if (VERSION.SDK_INT < 21 || !PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(PreferenceKeys.getUsingSAFPreferenceKey(), false)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: 0000 */
    public String getSaveLocation() {
        return PreferenceManager.getDefaultSharedPreferences(this.context).getString(PreferenceKeys.getSaveLocationPreferenceKey(), "OpenCamera");
    }

    /* access modifiers changed from: 0000 */
    public String getSaveLocationSAF() {
        return PreferenceManager.getDefaultSharedPreferences(this.context).getString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), BuildConfig.FLAVOR);
    }

    private Uri getTreeUriSAF() {
        return Uri.parse(getSaveLocationSAF());
    }

    /* access modifiers changed from: 0000 */
    public File getSettingsFolder() {
        return new File(this.context.getExternalFilesDir(null), "backups");
    }

    /* access modifiers changed from: 0000 */
    public File getImageFolder() {
        if (isUsingSAF()) {
            return getFileFromDocumentUriSAF(getTreeUriSAF(), true);
        }
        return getImageFolder(getSaveLocation());
    }

    public static File getBaseFolder() {
        return base_folder;
    }

    private static File getImageFolder(String str) {
        if (str.length() > 0 && str.lastIndexOf(47) == str.length() - 1) {
            str = str.substring(0, str.length() - 1);
        }
        if (str.startsWith("/")) {
            return new File(str);
        }
        return new File(getBaseFolder(), str);
    }

    public File getFileFromDocumentUriSAF(Uri uri, boolean z) {
        Uri uri2;
        File file;
        String authority = uri.getAuthority();
        String str = ":";
        File file2 = null;
        int i = 0;
        if ("com.android.externalstorage.documents".equals(authority)) {
            String[] split = (z ? DocumentsContract.getTreeDocumentId(uri) : DocumentsContract.getDocumentId(uri)).split(str);
            if (split.length < 2) {
                return null;
            }
            String str2 = split[0];
            String str3 = split[1];
            File[] listFiles = new File("/storage").listFiles();
            if ("primary".equalsIgnoreCase(str2)) {
                file2 = new File(Environment.getExternalStorageDirectory(), str3);
            }
            while (listFiles != null && i < listFiles.length && file2 == null) {
                File file3 = new File(listFiles[i], str3);
                if (file3.exists()) {
                    file2 = file3;
                }
                i++;
            }
            if (file2 != null) {
                return file2;
            }
            file = new File(str3);
        } else if ("com.android.providers.downloads.documents".equals(authority)) {
            if (z) {
                return null;
            }
            String documentId = DocumentsContract.getDocumentId(uri);
            String str4 = "raw:";
            if (documentId.startsWith(str4)) {
                file = new File(documentId.replaceFirst(str4, BuildConfig.FLAVOR));
            } else {
                try {
                    String dataColumn = getDataColumn(ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(documentId)), null, null);
                    return dataColumn != null ? new File(dataColumn) : null;
                } catch (NumberFormatException e) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("failed to parse id: ");
                    sb.append(documentId);
                    Log.e(TAG, sb.toString());
                    e.printStackTrace();
                    return null;
                }
            }
        } else if (!"com.android.providers.media.documents".equals(authority)) {
            return null;
        } else {
            String[] split2 = DocumentsContract.getDocumentId(uri).split(str);
            String str5 = split2[0];
            char c = 65535;
            int hashCode = str5.hashCode();
            if (hashCode != 93166550) {
                if (hashCode != 100313435) {
                    if (hashCode == 112202875 && str5.equals("video")) {
                        c = 1;
                    }
                } else if (str5.equals("image")) {
                    c = 0;
                }
            } else if (str5.equals("audio")) {
                c = 2;
            }
            if (c == 0) {
                uri2 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if (c == 1) {
                uri2 = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if (c != 2) {
                uri2 = null;
            } else {
                uri2 = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            String dataColumn2 = getDataColumn(uri2, "_id=?", new String[]{split2[1]});
            if (dataColumn2 != null) {
                return new File(dataColumn2);
            }
            return null;
        }
        return file;
    }

    /* JADX WARNING: type inference failed for: r9v0, types: [android.net.Uri] */
    /* JADX WARNING: type inference failed for: r9v1, types: [android.database.Cursor] */
    /* JADX WARNING: type inference failed for: r9v2 */
    /* JADX WARNING: type inference failed for: r9v3, types: [android.database.Cursor] */
    /* JADX WARNING: type inference failed for: r9v4 */
    /* JADX WARNING: type inference failed for: r9v5 */
    /* JADX WARNING: type inference failed for: r9v6 */
    /* JADX WARNING: type inference failed for: r9v7 */
    /* JADX WARNING: type inference failed for: r9v8 */
    /* JADX WARNING: type inference failed for: r2v0, types: [android.net.Uri] */
    /* JADX WARNING: type inference failed for: r9v9, types: [android.database.Cursor] */
    /* JADX WARNING: type inference failed for: r9v10 */
    /* JADX WARNING: type inference failed for: r9v11 */
    /* JADX WARNING: type inference failed for: r9v12 */
    /* JADX WARNING: type inference failed for: r9v13 */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0031, code lost:
        if (r9 != 0) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0044, code lost:
        if (r9 != 0) goto L_0x0046;
     */
    /* JADX WARNING: Incorrect type for immutable var: ssa=android.net.Uri, code=null, for r9v0, types: [android.net.Uri] */
    /* JADX WARNING: Multi-variable type inference failed. Error: jadx.core.utils.exceptions.JadxRuntimeException: No candidate types for var: r9v2
      assigns: []
      uses: []
      mth insns count: 43
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:49)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:49)
    	at jadx.core.ProcessClass.process(ProcessClass.java:35)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:311)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:217)
     */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x003e A[Catch:{ IllegalArgumentException -> 0x003f, SecurityException -> 0x0037, all -> 0x0034, all -> 0x004a }] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x004d  */
    /* JADX WARNING: Unknown variable types count: 5 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String getDataColumn(android.net.Uri r9, java.lang.String r10, java.lang.String[] r11) {
        /*
            r8 = this;
            r0 = 1
            java.lang.String[] r3 = new java.lang.String[r0]
            java.lang.String r0 = "_data"
            r1 = 0
            r3[r1] = r0
            r7 = 0
            android.content.Context r1 = r8.context     // Catch:{ IllegalArgumentException -> 0x003f, SecurityException -> 0x0037, all -> 0x0034 }
            android.content.ContentResolver r1 = r1.getContentResolver()     // Catch:{ IllegalArgumentException -> 0x003f, SecurityException -> 0x0037, all -> 0x0034 }
            r6 = 0
            r2 = r9
            r4 = r10
            r5 = r11
            android.database.Cursor r9 = r1.query(r2, r3, r4, r5, r6)     // Catch:{ IllegalArgumentException -> 0x003f, SecurityException -> 0x0037, all -> 0x0034 }
            if (r9 == 0) goto L_0x0031
            boolean r10 = r9.moveToFirst()     // Catch:{ IllegalArgumentException -> 0x002f, SecurityException -> 0x002d }
            if (r10 == 0) goto L_0x0031
            int r10 = r9.getColumnIndexOrThrow(r0)     // Catch:{ IllegalArgumentException -> 0x002f, SecurityException -> 0x002d }
            java.lang.String r10 = r9.getString(r10)     // Catch:{ IllegalArgumentException -> 0x002f, SecurityException -> 0x002d }
            if (r9 == 0) goto L_0x002c
            r9.close()
        L_0x002c:
            return r10
        L_0x002d:
            r10 = move-exception
            goto L_0x0039
        L_0x002f:
            r10 = move-exception
            goto L_0x0041
        L_0x0031:
            if (r9 == 0) goto L_0x0049
            goto L_0x0046
        L_0x0034:
            r10 = move-exception
            r9 = r7
            goto L_0x004b
        L_0x0037:
            r10 = move-exception
            r9 = r7
        L_0x0039:
            r10.printStackTrace()     // Catch:{ all -> 0x004a }
            if (r9 == 0) goto L_0x0049
            goto L_0x0046
        L_0x003f:
            r10 = move-exception
            r9 = r7
        L_0x0041:
            r10.printStackTrace()     // Catch:{ all -> 0x004a }
            if (r9 == 0) goto L_0x0049
        L_0x0046:
            r9.close()
        L_0x0049:
            return r7
        L_0x004a:
            r10 = move-exception
        L_0x004b:
            if (r9 == 0) goto L_0x0050
            r9.close()
        L_0x0050:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.StorageUtils.getDataColumn(android.net.Uri, java.lang.String, java.lang.String[]):java.lang.String");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0037, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0038, code lost:
        if (r0 != null) goto L_0x003a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0042, code lost:
        throw r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getFileName(android.net.Uri r9) {
        /*
            r8 = this;
            java.lang.String r0 = r9.getScheme()
            r1 = 0
            if (r0 == 0) goto L_0x004d
            java.lang.String r0 = r9.getScheme()
            java.lang.String r2 = "content"
            boolean r0 = r0.equals(r2)
            if (r0 == 0) goto L_0x004d
            android.content.Context r0 = r8.context     // Catch:{ Exception -> 0x0049 }
            android.content.ContentResolver r2 = r0.getContentResolver()     // Catch:{ Exception -> 0x0049 }
            r4 = 0
            r5 = 0
            r6 = 0
            r7 = 0
            r3 = r9
            android.database.Cursor r0 = r2.query(r3, r4, r5, r6, r7)     // Catch:{ Exception -> 0x0049 }
            if (r0 == 0) goto L_0x0043
            boolean r2 = r0.moveToFirst()     // Catch:{ all -> 0x0035 }
            if (r2 == 0) goto L_0x0043
            java.lang.String r2 = "_display_name"
            int r2 = r0.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x0035 }
            java.lang.String r1 = r0.getString(r2)     // Catch:{ all -> 0x0035 }
            goto L_0x0043
        L_0x0035:
            r2 = move-exception
            throw r2     // Catch:{ all -> 0x0037 }
        L_0x0037:
            r3 = move-exception
            if (r0 == 0) goto L_0x0042
            r0.close()     // Catch:{ all -> 0x003e }
            goto L_0x0042
        L_0x003e:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ Exception -> 0x0049 }
        L_0x0042:
            throw r3     // Catch:{ Exception -> 0x0049 }
        L_0x0043:
            if (r0 == 0) goto L_0x004d
            r0.close()     // Catch:{ Exception -> 0x0049 }
            goto L_0x004d
        L_0x0049:
            r0 = move-exception
            r0.printStackTrace()
        L_0x004d:
            if (r1 != 0) goto L_0x0062
            java.lang.String r1 = r9.getPath()
            r9 = 47
            int r9 = r1.lastIndexOf(r9)
            r0 = -1
            if (r9 == r0) goto L_0x0062
            int r9 = r9 + 1
            java.lang.String r1 = r1.substring(r9)
        L_0x0062:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.StorageUtils.getFileName(android.net.Uri):java.lang.String");
    }

    private String createMediaFilename(int i, String str, int i2, String str2, Date date) {
        String str3;
        String str4;
        if (i2 > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("_");
            sb.append(i2);
            str3 = sb.toString();
        } else {
            str3 = BuildConfig.FLAVOR;
        }
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        if (defaultSharedPreferences.getString(PreferenceKeys.getSaveZuluTimePreferenceKey(), "local").equals("zulu")) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss'Z'", Locale.US);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            str4 = simpleDateFormat.format(date);
        } else {
            str4 = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(date);
        }
        if (i != 1) {
            if (i == 2) {
                String string = defaultSharedPreferences.getString(PreferenceKeys.getSaveVideoPrefixPreferenceKey(), "VID_");
                StringBuilder sb2 = new StringBuilder();
                sb2.append(string);
                sb2.append(str4);
                sb2.append(str);
                sb2.append(str3);
                sb2.append(str2);
                return sb2.toString();
            } else if (i == 3) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("BACKUP_OC_");
                sb3.append(str4);
                sb3.append(str);
                sb3.append(str3);
                sb3.append(str2);
                return sb3.toString();
            } else if (i != 4) {
                throw new RuntimeException();
            }
        }
        String string2 = defaultSharedPreferences.getString(PreferenceKeys.getSavePhotoPrefixPreferenceKey(), "IMG_");
        StringBuilder sb4 = new StringBuilder();
        sb4.append(string2);
        sb4.append(str4);
        sb4.append(str);
        sb4.append(str3);
        sb4.append(str2);
        return sb4.toString();
    }

    /* access modifiers changed from: 0000 */
    public File createOutputMediaFile(int i, String str, String str2, Date date) throws IOException {
        return createOutputMediaFile(getImageFolder(), i, str, str2, date);
    }

    /* access modifiers changed from: 0000 */
    public void createFolderIfRequired(File file) throws IOException {
        if (file.exists()) {
            return;
        }
        if (file.mkdirs()) {
            broadcastFile(file, false, false, false);
        } else {
            Log.e(TAG, "failed to create directory");
            throw new IOException();
        }
    }

    /* access modifiers changed from: 0000 */
    public File createOutputMediaFile(File file, int i, String str, String str2, Date date) throws IOException {
        createFolderIfRequired(file);
        File file2 = null;
        int i2 = 0;
        while (true) {
            if (i2 >= 100) {
                break;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(".");
            sb.append(str2);
            String createMediaFilename = createMediaFilename(i, str, i2, sb.toString(), date);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(file.getPath());
            sb2.append(File.separator);
            sb2.append(createMediaFilename);
            File file3 = new File(sb2.toString());
            if (!file3.exists()) {
                file2 = file3;
                break;
            }
            i2++;
            file2 = file3;
        }
        if (file2 != null) {
            return file2;
        }
        throw new IOException();
    }

    /* access modifiers changed from: 0000 */
    public Uri createOutputFileSAF(String str, String str2) throws IOException {
        try {
            Uri treeUriSAF = getTreeUriSAF();
            Uri createDocument = DocumentsContract.createDocument(this.context.getContentResolver(), DocumentsContract.buildDocumentUriUsingTree(treeUriSAF, DocumentsContract.getTreeDocumentId(treeUriSAF)), str2, str);
            if (createDocument != null) {
                return createDocument;
            }
            throw new IOException();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new IOException();
        } catch (IllegalStateException e2) {
            e2.printStackTrace();
            throw new IOException();
        } catch (SecurityException e3) {
            e3.printStackTrace();
            throw new IOException();
        }
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0037, code lost:
        if (r11.equals("3gp") != false) goto L_0x003b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0077, code lost:
        if (r11.equals("dng") != false) goto L_0x007b;
     */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x003d  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x008a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.net.Uri createOutputMediaFileSAF(int r9, java.lang.String r10, java.lang.String r11, java.util.Date r12) throws java.io.IOException {
        /*
            r8 = this;
            r0 = 0
            java.lang.String r1 = "text/xml"
            r2 = -1
            r3 = 2
            r4 = 1
            if (r9 == r4) goto L_0x0049
            if (r9 == r3) goto L_0x0018
            r0 = 3
            if (r9 == r0) goto L_0x008d
            r0 = 4
            if (r9 != r0) goto L_0x0012
            goto L_0x008d
        L_0x0012:
            java.lang.RuntimeException r9 = new java.lang.RuntimeException
            r9.<init>()
            throw r9
        L_0x0018:
            int r1 = r11.hashCode()
            r3 = 52316(0xcc5c, float:7.331E-41)
            if (r1 == r3) goto L_0x0031
            r0 = 3645337(0x379f99, float:5.108205E-39)
            if (r1 == r0) goto L_0x0027
            goto L_0x003a
        L_0x0027:
            java.lang.String r0 = "webm"
            boolean r0 = r11.equals(r0)
            if (r0 == 0) goto L_0x003a
            r0 = 1
            goto L_0x003b
        L_0x0031:
            java.lang.String r1 = "3gp"
            boolean r1 = r11.equals(r1)
            if (r1 == 0) goto L_0x003a
            goto L_0x003b
        L_0x003a:
            r0 = -1
        L_0x003b:
            if (r0 == 0) goto L_0x0046
            if (r0 == r4) goto L_0x0043
            java.lang.String r0 = "video/mp4"
        L_0x0041:
            r1 = r0
            goto L_0x008d
        L_0x0043:
            java.lang.String r0 = "video/webm"
            goto L_0x0041
        L_0x0046:
            java.lang.String r0 = "video/3gpp"
            goto L_0x0041
        L_0x0049:
            int r1 = r11.hashCode()
            r5 = 99613(0x1851d, float:1.39588E-40)
            if (r1 == r5) goto L_0x0071
            r0 = 111145(0x1b229, float:1.55747E-40)
            if (r1 == r0) goto L_0x0067
            r0 = 3645340(0x379f9c, float:5.10821E-39)
            if (r1 == r0) goto L_0x005d
            goto L_0x007a
        L_0x005d:
            java.lang.String r0 = "webp"
            boolean r0 = r11.equals(r0)
            if (r0 == 0) goto L_0x007a
            r0 = 1
            goto L_0x007b
        L_0x0067:
            java.lang.String r0 = "png"
            boolean r0 = r11.equals(r0)
            if (r0 == 0) goto L_0x007a
            r0 = 2
            goto L_0x007b
        L_0x0071:
            java.lang.String r1 = "dng"
            boolean r1 = r11.equals(r1)
            if (r1 == 0) goto L_0x007a
            goto L_0x007b
        L_0x007a:
            r0 = -1
        L_0x007b:
            if (r0 == 0) goto L_0x008a
            if (r0 == r4) goto L_0x0087
            if (r0 == r3) goto L_0x0084
            java.lang.String r0 = "image/jpeg"
            goto L_0x0041
        L_0x0084:
            java.lang.String r0 = "image/png"
            goto L_0x0041
        L_0x0087:
            java.lang.String r0 = "image/webp"
            goto L_0x0041
        L_0x008a:
            java.lang.String r0 = "image/dng"
            goto L_0x0041
        L_0x008d:
            r5 = 0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "."
            r0.append(r2)
            r0.append(r11)
            java.lang.String r6 = r0.toString()
            r2 = r8
            r3 = r9
            r4 = r10
            r7 = r12
            java.lang.String r9 = r2.createMediaFilename(r3, r4, r5, r6, r7)
            android.net.Uri r9 = r8.createOutputFileSAF(r9, r1)
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.StorageUtils.createOutputMediaFileSAF(int, java.lang.String, java.lang.String, java.util.Date):android.net.Uri");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0107, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0158, code lost:
        if (r14 != null) goto L_0x015a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x015a, code lost:
        r14.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0166, code lost:
        if (r14 != null) goto L_0x015a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0169, code lost:
        return r11;
     */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x016d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private net.sourceforge.opencamera.StorageUtils.Media getLatestMedia(boolean r18) {
        /*
            r17 = this;
            r1 = r17
            java.lang.String r7 = "datetaken DESC,_id DESC"
            java.lang.String r0 = "."
            int r2 = android.os.Build.VERSION.SDK_INT
            r11 = 0
            r3 = 23
            if (r2 < r3) goto L_0x0018
            android.content.Context r2 = r1.context
            java.lang.String r3 = "android.permission.READ_EXTERNAL_STORAGE"
            int r2 = android.support.p000v4.content.ContextCompat.checkSelfPermission(r2, r3)
            if (r2 == 0) goto L_0x0018
            return r11
        L_0x0018:
            if (r18 == 0) goto L_0x001d
            android.net.Uri r2 = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            goto L_0x001f
        L_0x001d:
            android.net.Uri r2 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        L_0x001f:
            r8 = r2
            java.lang.String r2 = "_data"
            java.lang.String r3 = "datetaken"
            java.lang.String r4 = "_id"
            r9 = 3
            r10 = 2
            r12 = 1
            r13 = 0
            if (r18 == 0) goto L_0x0035
            java.lang.String[] r5 = new java.lang.String[r9]
            r5[r13] = r4
            r5[r12] = r3
            r5[r10] = r2
            goto L_0x0042
        L_0x0035:
            r5 = 4
            java.lang.String[] r5 = new java.lang.String[r5]
            r5[r13] = r4
            r5[r12] = r3
            r5[r10] = r2
            java.lang.String r2 = "orientation"
            r5[r9] = r2
        L_0x0042:
            r4 = r5
            if (r18 == 0) goto L_0x0048
            java.lang.String r2 = ""
            goto L_0x004a
        L_0x0048:
            java.lang.String r2 = "mime_type='image/jpeg' OR mime_type='image/webp' OR mime_type='image/png' OR mime_type='image/x-adobe-dng'"
        L_0x004a:
            r5 = r2
            android.content.Context r2 = r1.context     // Catch:{ Exception -> 0x0161, all -> 0x015e }
            android.content.ContentResolver r2 = r2.getContentResolver()     // Catch:{ Exception -> 0x0161, all -> 0x015e }
            r6 = 0
            r3 = r8
            android.database.Cursor r14 = r2.query(r3, r4, r5, r6, r7)     // Catch:{ Exception -> 0x0161, all -> 0x015e }
            if (r14 == 0) goto L_0x0158
            boolean r2 = r14.moveToFirst()     // Catch:{ Exception -> 0x0156 }
            if (r2 == 0) goto L_0x0158
            java.io.File r2 = r17.getImageFolder()     // Catch:{ Exception -> 0x0156 }
            if (r2 != 0) goto L_0x0067
            r2 = r11
            goto L_0x007c
        L_0x0067:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0156 }
            r3.<init>()     // Catch:{ Exception -> 0x0156 }
            java.lang.String r2 = r2.getAbsolutePath()     // Catch:{ Exception -> 0x0156 }
            r3.append(r2)     // Catch:{ Exception -> 0x0156 }
            java.lang.String r2 = java.io.File.separator     // Catch:{ Exception -> 0x0156 }
            r3.append(r2)     // Catch:{ Exception -> 0x0156 }
            java.lang.String r2 = r3.toString()     // Catch:{ Exception -> 0x0156 }
        L_0x007c:
            java.lang.String r3 = r14.getString(r10)     // Catch:{ Exception -> 0x0156 }
            if (r2 == 0) goto L_0x008a
            if (r3 == 0) goto L_0x009a
            boolean r3 = r3.contains(r2)     // Catch:{ Exception -> 0x0156 }
            if (r3 == 0) goto L_0x009a
        L_0x008a:
            long r3 = r14.getLong(r12)     // Catch:{ Exception -> 0x0156 }
            long r5 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x0156 }
            r15 = 172800000(0xa4cb800, double:8.53745436E-316)
            long r5 = r5 + r15
            int r7 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
            if (r7 <= 0) goto L_0x00a2
        L_0x009a:
            boolean r3 = r14.moveToNext()     // Catch:{ Exception -> 0x0156 }
            if (r3 != 0) goto L_0x007c
            r2 = 0
            goto L_0x00a3
        L_0x00a2:
            r2 = 1
        L_0x00a3:
            if (r2 == 0) goto L_0x012d
            java.lang.String r3 = r14.getString(r10)     // Catch:{ Exception -> 0x0156 }
            if (r3 == 0) goto L_0x012d
            java.util.Locale r4 = java.util.Locale.US     // Catch:{ Exception -> 0x0156 }
            java.lang.String r4 = r3.toLowerCase(r4)     // Catch:{ Exception -> 0x0156 }
            java.lang.String r5 = ".dng"
            boolean r4 = r4.endsWith(r5)     // Catch:{ Exception -> 0x0156 }
            if (r4 == 0) goto L_0x012d
            int r4 = r14.getPosition()     // Catch:{ Exception -> 0x0156 }
            java.util.Locale r5 = java.util.Locale.US     // Catch:{ Exception -> 0x0156 }
            java.lang.String r3 = r3.toLowerCase(r5)     // Catch:{ Exception -> 0x0156 }
            int r5 = r3.indexOf(r0)     // Catch:{ Exception -> 0x0156 }
            if (r5 <= 0) goto L_0x00d1
            int r5 = r3.lastIndexOf(r0)     // Catch:{ Exception -> 0x0156 }
            java.lang.String r3 = r3.substring(r13, r5)     // Catch:{ Exception -> 0x0156 }
        L_0x00d1:
            boolean r5 = r14.moveToNext()     // Catch:{ Exception -> 0x0156 }
            if (r5 == 0) goto L_0x0127
            java.lang.String r5 = r14.getString(r10)     // Catch:{ Exception -> 0x0156 }
            if (r5 != 0) goto L_0x00de
            goto L_0x0127
        L_0x00de:
            java.util.Locale r6 = java.util.Locale.US     // Catch:{ Exception -> 0x0156 }
            java.lang.String r6 = r5.toLowerCase(r6)     // Catch:{ Exception -> 0x0156 }
            int r7 = r6.indexOf(r0)     // Catch:{ Exception -> 0x0156 }
            if (r7 <= 0) goto L_0x00f2
            int r7 = r6.lastIndexOf(r0)     // Catch:{ Exception -> 0x0156 }
            java.lang.String r6 = r6.substring(r13, r7)     // Catch:{ Exception -> 0x0156 }
        L_0x00f2:
            boolean r6 = r3.equals(r6)     // Catch:{ Exception -> 0x0156 }
            if (r6 != 0) goto L_0x00f9
            goto L_0x0127
        L_0x00f9:
            java.util.Locale r6 = java.util.Locale.US     // Catch:{ Exception -> 0x0156 }
            java.lang.String r6 = r5.toLowerCase(r6)     // Catch:{ Exception -> 0x0156 }
            java.lang.String r7 = ".jpg"
            boolean r6 = r6.endsWith(r7)     // Catch:{ Exception -> 0x0156 }
            if (r6 == 0) goto L_0x0109
        L_0x0107:
            r0 = 1
            goto L_0x0128
        L_0x0109:
            java.util.Locale r6 = java.util.Locale.US     // Catch:{ Exception -> 0x0156 }
            java.lang.String r6 = r5.toLowerCase(r6)     // Catch:{ Exception -> 0x0156 }
            java.lang.String r7 = ".webp"
            boolean r6 = r6.endsWith(r7)     // Catch:{ Exception -> 0x0156 }
            if (r6 == 0) goto L_0x0118
            goto L_0x0107
        L_0x0118:
            java.util.Locale r6 = java.util.Locale.US     // Catch:{ Exception -> 0x0156 }
            java.lang.String r5 = r5.toLowerCase(r6)     // Catch:{ Exception -> 0x0156 }
            java.lang.String r6 = ".png"
            boolean r5 = r5.endsWith(r6)     // Catch:{ Exception -> 0x0156 }
            if (r5 == 0) goto L_0x00d1
            goto L_0x0107
        L_0x0127:
            r0 = 0
        L_0x0128:
            if (r0 != 0) goto L_0x012d
            r14.moveToPosition(r4)     // Catch:{ Exception -> 0x0156 }
        L_0x012d:
            if (r2 != 0) goto L_0x0132
            r14.moveToFirst()     // Catch:{ Exception -> 0x0156 }
        L_0x0132:
            long r3 = r14.getLong(r13)     // Catch:{ Exception -> 0x0156 }
            long r15 = r14.getLong(r12)     // Catch:{ Exception -> 0x0156 }
            if (r18 == 0) goto L_0x013e
            r9 = 0
            goto L_0x0143
        L_0x013e:
            int r0 = r14.getInt(r9)     // Catch:{ Exception -> 0x0156 }
            r9 = r0
        L_0x0143:
            android.net.Uri r6 = android.content.ContentUris.withAppendedId(r8, r3)     // Catch:{ Exception -> 0x0156 }
            java.lang.String r10 = r14.getString(r10)     // Catch:{ Exception -> 0x0156 }
            net.sourceforge.opencamera.StorageUtils$Media r0 = new net.sourceforge.opencamera.StorageUtils$Media     // Catch:{ Exception -> 0x0156 }
            r2 = r0
            r5 = r18
            r7 = r15
            r2.<init>(r3, r5, r6, r7, r9, r10)     // Catch:{ Exception -> 0x0156 }
            r11 = r0
            goto L_0x0158
        L_0x0156:
            r0 = move-exception
            goto L_0x0163
        L_0x0158:
            if (r14 == 0) goto L_0x0169
        L_0x015a:
            r14.close()
            goto L_0x0169
        L_0x015e:
            r0 = move-exception
            r14 = r11
            goto L_0x016b
        L_0x0161:
            r0 = move-exception
            r14 = r11
        L_0x0163:
            r0.printStackTrace()     // Catch:{ all -> 0x016a }
            if (r14 == 0) goto L_0x0169
            goto L_0x015a
        L_0x0169:
            return r11
        L_0x016a:
            r0 = move-exception
        L_0x016b:
            if (r14 == 0) goto L_0x0170
            r14.close()
        L_0x0170:
            goto L_0x0172
        L_0x0171:
            throw r0
        L_0x0172:
            goto L_0x0171
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.StorageUtils.getLatestMedia(boolean):net.sourceforge.opencamera.StorageUtils$Media");
    }

    /* access modifiers changed from: 0000 */
    public Media getLatestMedia() {
        Media latestMedia = getLatestMedia(false);
        Media latestMedia2 = getLatestMedia(true);
        if (latestMedia != null && latestMedia2 == null) {
            return latestMedia;
        }
        if (latestMedia != null || latestMedia2 == null) {
            if (latestMedia == null || latestMedia2 == null) {
                return null;
            }
            if (latestMedia.date >= latestMedia2.date) {
                return latestMedia;
            }
        }
        return latestMedia2;
    }

    private long freeMemorySAF() {
        Uri treeUriSAF = this.applicationInterface.getStorageUtils().getTreeUriSAF();
        try {
            ParcelFileDescriptor openFileDescriptor = this.context.getContentResolver().openFileDescriptor(DocumentsContract.buildDocumentUriUsingTree(treeUriSAF, DocumentsContract.getTreeDocumentId(treeUriSAF)), "r");
            if (openFileDescriptor != null) {
                StructStatVfs fstatvfs = Os.fstatvfs(openFileDescriptor.getFileDescriptor());
                return (fstatvfs.f_bavail * fstatvfs.f_bsize) / PlaybackStateCompat.ACTION_SET_CAPTIONING_ENABLED;
            }
            Log.e(TAG, "pfd is null!");
            throw new FileNotFoundException();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return -1;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            return -1;
        } catch (Exception e3) {
            e3.printStackTrace();
            return -1;
        }
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(5:18|19|(4:23|(1:25)(1:26)|27|28)|29|31) */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005e, code lost:
        r4 = new android.os.StatFs(getBaseFolder().getAbsolutePath());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006d, code lost:
        if (android.os.Build.VERSION.SDK_INT >= 18) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006f, code lost:
        r2 = r4.getAvailableBlocksLong();
        r4 = r4.getBlockSizeLong();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0078, code lost:
        r2 = (long) r4.getAvailableBlocks();
        r4 = (long) r4.getBlockSize();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0085, code lost:
        return (r2 * r4) / android.support.p000v4.media.session.PlaybackStateCompat.ACTION_SET_CAPTIONING_ENABLED;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0088, code lost:
        return -1;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:18:0x004c */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long freeMemory() {
        /*
            r7 = this;
            net.sourceforge.opencamera.MyApplicationInterface r0 = r7.applicationInterface
            net.sourceforge.opencamera.StorageUtils r0 = r0.getStorageUtils()
            boolean r0 = r0.isUsingSAF()
            if (r0 == 0) goto L_0x0017
            int r0 = android.os.Build.VERSION.SDK_INT
            r1 = 21
            if (r0 < r1) goto L_0x0017
            long r0 = r7.freeMemorySAF()
            return r0
        L_0x0017:
            r0 = 1048576(0x100000, double:5.180654E-318)
            r2 = 18
            java.io.File r3 = r7.getImageFolder()     // Catch:{ IllegalArgumentException -> 0x004c }
            if (r3 == 0) goto L_0x0046
            android.os.StatFs r4 = new android.os.StatFs     // Catch:{ IllegalArgumentException -> 0x004c }
            java.lang.String r3 = r3.getAbsolutePath()     // Catch:{ IllegalArgumentException -> 0x004c }
            r4.<init>(r3)     // Catch:{ IllegalArgumentException -> 0x004c }
            int r3 = android.os.Build.VERSION.SDK_INT     // Catch:{ IllegalArgumentException -> 0x004c }
            if (r3 < r2) goto L_0x0038
            long r5 = r4.getAvailableBlocksLong()     // Catch:{ IllegalArgumentException -> 0x004c }
            long r3 = r4.getBlockSizeLong()     // Catch:{ IllegalArgumentException -> 0x004c }
            goto L_0x0042
        L_0x0038:
            int r3 = r4.getAvailableBlocks()     // Catch:{ IllegalArgumentException -> 0x004c }
            long r5 = (long) r3     // Catch:{ IllegalArgumentException -> 0x004c }
            int r3 = r4.getBlockSize()     // Catch:{ IllegalArgumentException -> 0x004c }
            long r3 = (long) r3     // Catch:{ IllegalArgumentException -> 0x004c }
        L_0x0042:
            long r5 = r5 * r3
            long r5 = r5 / r0
            return r5
        L_0x0046:
            java.lang.IllegalArgumentException r3 = new java.lang.IllegalArgumentException     // Catch:{ IllegalArgumentException -> 0x004c }
            r3.<init>()     // Catch:{ IllegalArgumentException -> 0x004c }
            throw r3     // Catch:{ IllegalArgumentException -> 0x004c }
        L_0x004c:
            boolean r3 = r7.isUsingSAF()     // Catch:{ IllegalArgumentException -> 0x0086 }
            if (r3 != 0) goto L_0x0086
            java.lang.String r3 = r7.getSaveLocation()     // Catch:{ IllegalArgumentException -> 0x0086 }
            java.lang.String r4 = "/"
            boolean r3 = r3.startsWith(r4)     // Catch:{ IllegalArgumentException -> 0x0086 }
            if (r3 != 0) goto L_0x0086
            java.io.File r3 = getBaseFolder()     // Catch:{ IllegalArgumentException -> 0x0086 }
            android.os.StatFs r4 = new android.os.StatFs     // Catch:{ IllegalArgumentException -> 0x0086 }
            java.lang.String r3 = r3.getAbsolutePath()     // Catch:{ IllegalArgumentException -> 0x0086 }
            r4.<init>(r3)     // Catch:{ IllegalArgumentException -> 0x0086 }
            int r3 = android.os.Build.VERSION.SDK_INT     // Catch:{ IllegalArgumentException -> 0x0086 }
            if (r3 < r2) goto L_0x0078
            long r2 = r4.getAvailableBlocksLong()     // Catch:{ IllegalArgumentException -> 0x0086 }
            long r4 = r4.getBlockSizeLong()     // Catch:{ IllegalArgumentException -> 0x0086 }
            goto L_0x0082
        L_0x0078:
            int r2 = r4.getAvailableBlocks()     // Catch:{ IllegalArgumentException -> 0x0086 }
            long r2 = (long) r2     // Catch:{ IllegalArgumentException -> 0x0086 }
            int r4 = r4.getBlockSize()     // Catch:{ IllegalArgumentException -> 0x0086 }
            long r4 = (long) r4     // Catch:{ IllegalArgumentException -> 0x0086 }
        L_0x0082:
            long r2 = r2 * r4
            long r2 = r2 / r0
            return r2
        L_0x0086:
            r0 = -1
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.StorageUtils.freeMemory():long");
    }
}
