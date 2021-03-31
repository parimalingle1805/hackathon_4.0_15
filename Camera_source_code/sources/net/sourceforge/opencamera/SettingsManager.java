package net.sourceforge.opencamera;

import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SettingsManager {
    private static final String TAG = "SettingsManager";
    private static final String boolean_tag = "boolean";
    private static final String doc_tag = "open_camera_prefs";
    private static final String float_tag = "float";
    private static final String int_tag = "int";
    private static final String long_tag = "long";
    private static final String string_tag = "string";
    private final MainActivity main_activity;

    SettingsManager(MainActivity mainActivity) {
        this.main_activity = mainActivity;
    }

    public boolean loadSettings(String str) {
        try {
            return loadSettings((InputStream) new FileInputStream(str));
        } catch (FileNotFoundException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("failed to load: ");
            sb.append(str);
            Log.e(TAG, sb.toString());
            e.printStackTrace();
            this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.restore_settings_failed);
            return false;
        }
    }

    public boolean loadSettings(Uri uri) {
        try {
            return loadSettings(this.main_activity.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("failed to load: ");
            sb.append(uri);
            Log.e(TAG, sb.toString());
            e.printStackTrace();
            this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.restore_settings_failed);
            return false;
        }
    }

    private boolean loadSettings(InputStream inputStream) {
        try {
            XmlPullParser newPullParser = Xml.newPullParser();
            newPullParser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
            newPullParser.setInput(inputStream, null);
            newPullParser.nextTag();
            newPullParser.require(2, null, doc_tag);
            Editor edit = PreferenceManager.getDefaultSharedPreferences(this.main_activity).edit();
            edit.clear();
            while (newPullParser.next() != 3) {
                if (newPullParser.getEventType() == 2) {
                    String name = newPullParser.getName();
                    String attributeValue = newPullParser.getAttributeValue(null, "key");
                    char c = 65535;
                    switch (name.hashCode()) {
                        case -891985903:
                            if (name.equals(string_tag)) {
                                c = 4;
                                break;
                            }
                            break;
                        case 104431:
                            if (name.equals(int_tag)) {
                                c = 2;
                                break;
                            }
                            break;
                        case 3327612:
                            if (name.equals(long_tag)) {
                                c = 3;
                                break;
                            }
                            break;
                        case 64711720:
                            if (name.equals(boolean_tag)) {
                                c = 0;
                                break;
                            }
                            break;
                        case 97526364:
                            if (name.equals(float_tag)) {
                                c = 1;
                                break;
                            }
                            break;
                    }
                    String str = "value";
                    if (c == 0) {
                        edit.putBoolean(attributeValue, Boolean.valueOf(newPullParser.getAttributeValue(null, str)).booleanValue());
                    } else if (c == 1) {
                        edit.putFloat(attributeValue, Float.valueOf(newPullParser.getAttributeValue(null, str)).floatValue());
                    } else if (c == 2) {
                        edit.putInt(attributeValue, Integer.parseInt(newPullParser.getAttributeValue(null, str)));
                    } else if (c == 3) {
                        edit.putLong(attributeValue, Long.parseLong(newPullParser.getAttributeValue(null, str)));
                    } else if (c == 4) {
                        edit.putString(attributeValue, newPullParser.getAttributeValue(null, str));
                    }
                    skipXml(newPullParser);
                }
            }
            edit.putBoolean(PreferenceKeys.FirstTimePreferenceKey, true);
            try {
                edit.putInt(PreferenceKeys.LatestVersionPreferenceKey, this.main_activity.getPackageManager().getPackageInfo(this.main_activity.getPackageName(), 0).versionCode);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            edit.apply();
            if (!this.main_activity.is_test) {
                this.main_activity.restartOpenCamera();
            }
            try {
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return true;
        } catch (Exception e3) {
            e3.printStackTrace();
            this.main_activity.getPreview().showToast((ToastBoxer) null, (int) C0316R.string.restore_settings_failed);
            try {
            } catch (IOException e4) {
                e4.printStackTrace();
            }
            return false;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e5) {
                e5.printStackTrace();
            }
        }
    }

    private static void skipXml(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        if (xmlPullParser.getEventType() == 2) {
            int i = 1;
            while (i != 0) {
                int next = xmlPullParser.next();
                if (next == 2) {
                    i++;
                } else if (next == 3) {
                    i--;
                }
            }
            return;
        }
        throw new IllegalStateException();
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x0114 A[SYNTHETIC, Splitter:B:41:0x0114] */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0120 A[SYNTHETIC, Splitter:B:47:0x0120] */
    /* JADX WARNING: Removed duplicated region for block: B:58:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void saveSettings(java.lang.String r14) {
        /*
            r13 = this;
            java.lang.String r0 = "open_camera_prefs"
            java.lang.String r1 = "UTF-8"
            r2 = 0
            net.sourceforge.opencamera.MainActivity r3 = r13.main_activity     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            net.sourceforge.opencamera.StorageUtils r3 = r3.getStorageUtils()     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            java.io.File r4 = r3.getSettingsFolder()     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            r3.createFolderIfRequired(r4)     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            java.io.File r5 = new java.io.File     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            r6.<init>()     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            java.lang.String r4 = r4.getPath()     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            r6.append(r4)     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            java.lang.String r4 = java.io.File.separator     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            r6.append(r4)     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            r6.append(r14)     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            java.lang.String r14 = r6.toString()     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            r5.<init>(r14)     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            net.sourceforge.opencamera.MainActivity r14 = r13.main_activity     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            java.lang.String r4 = r5.getAbsolutePath()     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            r14.test_save_settings_file = r4     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            java.io.FileOutputStream r14 = new java.io.FileOutputStream     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            r14.<init>(r5)     // Catch:{ IOException -> 0x0101, all -> 0x00fe }
            org.xmlpull.v1.XmlSerializer r4 = android.util.Xml.newSerializer()     // Catch:{ IOException -> 0x00fc }
            java.io.StringWriter r6 = new java.io.StringWriter     // Catch:{ IOException -> 0x00fc }
            r6.<init>()     // Catch:{ IOException -> 0x00fc }
            r4.setOutput(r6)     // Catch:{ IOException -> 0x00fc }
            r7 = 1
            java.lang.Boolean r7 = java.lang.Boolean.valueOf(r7)     // Catch:{ IOException -> 0x00fc }
            r4.startDocument(r1, r7)     // Catch:{ IOException -> 0x00fc }
            r4.startTag(r2, r0)     // Catch:{ IOException -> 0x00fc }
            net.sourceforge.opencamera.MainActivity r7 = r13.main_activity     // Catch:{ IOException -> 0x00fc }
            android.content.SharedPreferences r7 = android.preference.PreferenceManager.getDefaultSharedPreferences(r7)     // Catch:{ IOException -> 0x00fc }
            java.util.Map r7 = r7.getAll()     // Catch:{ IOException -> 0x00fc }
            java.util.Set r7 = r7.entrySet()     // Catch:{ IOException -> 0x00fc }
            java.util.Iterator r7 = r7.iterator()     // Catch:{ IOException -> 0x00fc }
        L_0x0065:
            boolean r8 = r7.hasNext()     // Catch:{ IOException -> 0x00fc }
            if (r8 == 0) goto L_0x00d0
            java.lang.Object r8 = r7.next()     // Catch:{ IOException -> 0x00fc }
            java.util.Map$Entry r8 = (java.util.Map.Entry) r8     // Catch:{ IOException -> 0x00fc }
            java.lang.Object r9 = r8.getKey()     // Catch:{ IOException -> 0x00fc }
            java.lang.String r9 = (java.lang.String) r9     // Catch:{ IOException -> 0x00fc }
            java.lang.Object r8 = r8.getValue()     // Catch:{ IOException -> 0x00fc }
            if (r9 == 0) goto L_0x0065
            boolean r10 = r8 instanceof java.lang.Boolean     // Catch:{ IOException -> 0x00fc }
            if (r10 == 0) goto L_0x0084
            java.lang.String r10 = "boolean"
            goto L_0x00b7
        L_0x0084:
            boolean r10 = r8 instanceof java.lang.Float     // Catch:{ IOException -> 0x00fc }
            if (r10 == 0) goto L_0x008b
            java.lang.String r10 = "float"
            goto L_0x00b7
        L_0x008b:
            boolean r10 = r8 instanceof java.lang.Integer     // Catch:{ IOException -> 0x00fc }
            if (r10 == 0) goto L_0x0092
            java.lang.String r10 = "int"
            goto L_0x00b7
        L_0x0092:
            boolean r10 = r8 instanceof java.lang.Long     // Catch:{ IOException -> 0x00fc }
            if (r10 == 0) goto L_0x0099
            java.lang.String r10 = "long"
            goto L_0x00b7
        L_0x0099:
            boolean r10 = r8 instanceof java.lang.String     // Catch:{ IOException -> 0x00fc }
            if (r10 == 0) goto L_0x00a0
            java.lang.String r10 = "string"
            goto L_0x00b7
        L_0x00a0:
            java.lang.String r10 = "SettingsManager"
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x00fc }
            r11.<init>()     // Catch:{ IOException -> 0x00fc }
            java.lang.String r12 = "unknown value type: "
            r11.append(r12)     // Catch:{ IOException -> 0x00fc }
            r11.append(r8)     // Catch:{ IOException -> 0x00fc }
            java.lang.String r11 = r11.toString()     // Catch:{ IOException -> 0x00fc }
            android.util.Log.e(r10, r11)     // Catch:{ IOException -> 0x00fc }
            r10 = r2
        L_0x00b7:
            if (r10 == 0) goto L_0x0065
            r4.startTag(r2, r10)     // Catch:{ IOException -> 0x00fc }
            java.lang.String r11 = "key"
            r4.attribute(r2, r11, r9)     // Catch:{ IOException -> 0x00fc }
            if (r8 == 0) goto L_0x00cc
            java.lang.String r9 = "value"
            java.lang.String r8 = r8.toString()     // Catch:{ IOException -> 0x00fc }
            r4.attribute(r2, r9, r8)     // Catch:{ IOException -> 0x00fc }
        L_0x00cc:
            r4.endTag(r2, r10)     // Catch:{ IOException -> 0x00fc }
            goto L_0x0065
        L_0x00d0:
            r4.endTag(r2, r0)     // Catch:{ IOException -> 0x00fc }
            r4.endDocument()     // Catch:{ IOException -> 0x00fc }
            r4.flush()     // Catch:{ IOException -> 0x00fc }
            java.lang.String r0 = r6.toString()     // Catch:{ IOException -> 0x00fc }
            java.nio.charset.Charset r1 = java.nio.charset.Charset.forName(r1)     // Catch:{ IOException -> 0x00fc }
            byte[] r0 = r0.getBytes(r1)     // Catch:{ IOException -> 0x00fc }
            r14.write(r0)     // Catch:{ IOException -> 0x00fc }
            net.sourceforge.opencamera.MainActivity r0 = r13.main_activity     // Catch:{ IOException -> 0x00fc }
            net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()     // Catch:{ IOException -> 0x00fc }
            r1 = 2131493559(0x7f0c02b7, float:1.8610602E38)
            r0.showToast(r2, r1)     // Catch:{ IOException -> 0x00fc }
            r0 = 0
            r3.broadcastFile(r5, r0, r0, r0)     // Catch:{ IOException -> 0x00fc }
            r14.close()     // Catch:{ IOException -> 0x0118 }
            goto L_0x011c
        L_0x00fc:
            r0 = move-exception
            goto L_0x0103
        L_0x00fe:
            r0 = move-exception
            r14 = r2
            goto L_0x011e
        L_0x0101:
            r0 = move-exception
            r14 = r2
        L_0x0103:
            r0.printStackTrace()     // Catch:{ all -> 0x011d }
            net.sourceforge.opencamera.MainActivity r0 = r13.main_activity     // Catch:{ all -> 0x011d }
            net.sourceforge.opencamera.preview.Preview r0 = r0.getPreview()     // Catch:{ all -> 0x011d }
            r1 = 2131493558(0x7f0c02b6, float:1.86106E38)
            r0.showToast(r2, r1)     // Catch:{ all -> 0x011d }
            if (r14 == 0) goto L_0x011c
            r14.close()     // Catch:{ IOException -> 0x0118 }
            goto L_0x011c
        L_0x0118:
            r14 = move-exception
            r14.printStackTrace()
        L_0x011c:
            return
        L_0x011d:
            r0 = move-exception
        L_0x011e:
            if (r14 == 0) goto L_0x0128
            r14.close()     // Catch:{ IOException -> 0x0124 }
            goto L_0x0128
        L_0x0124:
            r14 = move-exception
            r14.printStackTrace()
        L_0x0128:
            goto L_0x012a
        L_0x0129:
            throw r0
        L_0x012a:
            goto L_0x0129
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.SettingsManager.saveSettings(java.lang.String):void");
    }
}
