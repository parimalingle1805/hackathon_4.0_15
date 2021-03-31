package net.sourceforge.opencamera.p004ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.util.Locale;
import net.sourceforge.opencamera.BuildConfig;
import net.sourceforge.opencamera.C0316R;
import net.sourceforge.opencamera.StorageUtils;

/* renamed from: net.sourceforge.opencamera.ui.FolderChooserDialog */
public class FolderChooserDialog extends DialogFragment {
    private static final String TAG = "FolderChooserFragment";
    /* access modifiers changed from: private */
    public String chosen_file;
    private String chosen_folder;
    /* access modifiers changed from: private */
    public File current_folder;
    private String extension;
    /* access modifiers changed from: private */
    public AlertDialog folder_dialog;
    private ListView list;
    /* access modifiers changed from: private */
    public boolean mode_folder = true;
    private boolean show_dcim_shortcut = true;
    /* access modifiers changed from: private */
    public boolean show_new_folder_button = true;
    private File start_folder = new File(BuildConfig.FLAVOR);

    /* renamed from: net.sourceforge.opencamera.ui.FolderChooserDialog$FileWrapper */
    private static class FileWrapper implements Comparable<FileWrapper> {
        private final File file;
        private final String override_name;
        private final int sort_order;

        FileWrapper(File file2, String str, int i) {
            this.file = file2;
            this.override_name = str;
            this.sort_order = i;
        }

        public String toString() {
            String str = this.override_name;
            if (str != null) {
                return str;
            }
            if (!this.file.isDirectory()) {
                return this.file.getName();
            }
            StringBuilder sb = new StringBuilder();
            sb.append(this.file.getName());
            sb.append(File.separator);
            return sb.toString();
        }

        public int compareTo(FileWrapper fileWrapper) {
            int i = this.sort_order;
            int i2 = fileWrapper.sort_order;
            if (i < i2) {
                return -1;
            }
            if (i > i2) {
                return 1;
            }
            return this.file.getName().toLowerCase(Locale.US).compareTo(fileWrapper.getFile().getName().toLowerCase(Locale.US));
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof FileWrapper)) {
                return false;
            }
            FileWrapper fileWrapper = (FileWrapper) obj;
            if (this.sort_order != fileWrapper.sort_order) {
                return false;
            }
            return this.file.getName().toLowerCase(Locale.US).equals(fileWrapper.getFile().getName().toLowerCase(Locale.US));
        }

        public int hashCode() {
            return this.file.getName().toLowerCase(Locale.US).hashCode();
        }

        /* access modifiers changed from: 0000 */
        public File getFile() {
            return this.file;
        }
    }

    /* renamed from: net.sourceforge.opencamera.ui.FolderChooserDialog$NewFolderInputFilter */
    private static class NewFolderInputFilter implements InputFilter {
        private static final String disallowed = "|\\?*<\":>";

        private NewFolderInputFilter() {
        }

        public CharSequence filter(CharSequence charSequence, int i, int i2, Spanned spanned, int i3, int i4) {
            while (i < i2) {
                if (disallowed.indexOf(charSequence.charAt(i)) != -1) {
                    return BuildConfig.FLAVOR;
                }
                i++;
            }
            return null;
        }
    }

    public Dialog onCreateDialog(Bundle bundle) {
        this.list = new ListView(getActivity());
        this.list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                File file = ((FileWrapper) adapterView.getItemAtPosition(i)).getFile();
                if (file.isDirectory()) {
                    FolderChooserDialog.this.refreshList(file);
                } else if (!FolderChooserDialog.this.mode_folder && file.isFile()) {
                    FolderChooserDialog.this.chosen_file = file.getAbsolutePath();
                    FolderChooserDialog.this.folder_dialog.dismiss();
                }
            }
        });
        Builder view = new Builder(getActivity()).setView(this.list);
        if (this.mode_folder) {
            view.setPositiveButton(17039370, null);
        }
        if (this.show_new_folder_button) {
            view.setNeutralButton(C0316R.string.new_folder, null);
        }
        view.setNegativeButton(17039360, null);
        this.folder_dialog = view.create();
        this.folder_dialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialogInterface) {
                if (FolderChooserDialog.this.mode_folder) {
                    FolderChooserDialog.this.folder_dialog.getButton(-1).setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            if (FolderChooserDialog.this.useFolder()) {
                                FolderChooserDialog.this.folder_dialog.dismiss();
                            }
                        }
                    });
                }
                if (FolderChooserDialog.this.show_new_folder_button) {
                    FolderChooserDialog.this.folder_dialog.getButton(-3).setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            FolderChooserDialog.this.newFolder();
                        }
                    });
                }
            }
        });
        if (!this.start_folder.exists()) {
            this.start_folder.mkdirs();
        }
        refreshList(this.start_folder);
        if (!canWrite()) {
            refreshList(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
            if (this.current_folder == null) {
                refreshList(new File("/"));
            }
        }
        return this.folder_dialog;
    }

    public void setStartFolder(File file) {
        this.start_folder = file;
    }

    public void setShowNewFolderButton(boolean z) {
        this.show_new_folder_button = z;
    }

    public void setShowDCIMShortcut(boolean z) {
        this.show_dcim_shortcut = z;
    }

    public void setModeFolder(boolean z) {
        this.mode_folder = z;
    }

    public void setExtension(String str) {
        this.extension = str.toLowerCase();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x008d, code lost:
        if (r8.substring(r9).toLowerCase().equals(r11.extension) == false) goto L_0x008f;
     */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00a3 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void refreshList(java.io.File r12) {
        /*
            r11 = this;
            if (r12 != 0) goto L_0x0003
            return
        L_0x0003:
            r0 = 0
            java.io.File[] r1 = r12.listFiles()     // Catch:{ Exception -> 0x0009 }
            goto L_0x000e
        L_0x0009:
            r1 = move-exception
            r1.printStackTrace()
            r1 = r0
        L_0x000e:
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            java.io.File r3 = r12.getParentFile()
            r4 = 0
            if (r3 == 0) goto L_0x0031
            net.sourceforge.opencamera.ui.FolderChooserDialog$FileWrapper r3 = new net.sourceforge.opencamera.ui.FolderChooserDialog$FileWrapper
            java.io.File r5 = r12.getParentFile()
            android.content.res.Resources r6 = r11.getResources()
            r7 = 2131493038(0x7f0c00ae, float:1.8609545E38)
            java.lang.String r6 = r6.getString(r7)
            r3.<init>(r5, r6, r4)
            r2.add(r3)
        L_0x0031:
            boolean r3 = r11.show_dcim_shortcut
            r5 = 1
            if (r3 == 0) goto L_0x0054
            java.lang.String r3 = android.os.Environment.DIRECTORY_DCIM
            java.io.File r3 = android.os.Environment.getExternalStoragePublicDirectory(r3)
            boolean r6 = r3.equals(r12)
            if (r6 != 0) goto L_0x0054
            java.io.File r6 = r12.getParentFile()
            boolean r6 = r3.equals(r6)
            if (r6 != 0) goto L_0x0054
            net.sourceforge.opencamera.ui.FolderChooserDialog$FileWrapper r6 = new net.sourceforge.opencamera.ui.FolderChooserDialog$FileWrapper
            r6.<init>(r3, r0, r5)
            r2.add(r6)
        L_0x0054:
            if (r1 == 0) goto L_0x00a6
            int r3 = r1.length
            r6 = 0
        L_0x0058:
            if (r6 >= r3) goto L_0x00a6
            r7 = r1[r6]
            boolean r8 = r7.isDirectory()
            if (r8 == 0) goto L_0x0064
        L_0x0062:
            r8 = 1
            goto L_0x0090
        L_0x0064:
            boolean r8 = r11.mode_folder
            if (r8 != 0) goto L_0x008f
            boolean r8 = r7.isFile()
            if (r8 == 0) goto L_0x008f
            java.lang.String r8 = r11.extension
            if (r8 == 0) goto L_0x0062
            java.lang.String r8 = r7.getName()
            r9 = 46
            int r9 = r8.lastIndexOf(r9)
            r10 = -1
            if (r9 == r10) goto L_0x0062
            java.lang.String r8 = r8.substring(r9)
            java.lang.String r8 = r8.toLowerCase()
            java.lang.String r9 = r11.extension
            boolean r8 = r8.equals(r9)
            if (r8 != 0) goto L_0x0062
        L_0x008f:
            r8 = 0
        L_0x0090:
            if (r8 == 0) goto L_0x00a3
            boolean r8 = r7.isDirectory()
            if (r8 == 0) goto L_0x009a
            r8 = 2
            goto L_0x009b
        L_0x009a:
            r8 = 3
        L_0x009b:
            net.sourceforge.opencamera.ui.FolderChooserDialog$FileWrapper r9 = new net.sourceforge.opencamera.ui.FolderChooserDialog$FileWrapper
            r9.<init>(r7, r0, r8)
            r2.add(r9)
        L_0x00a3:
            int r6 = r6 + 1
            goto L_0x0058
        L_0x00a6:
            java.util.Collections.sort(r2)
            android.widget.ArrayAdapter r0 = new android.widget.ArrayAdapter
            android.app.Activity r1 = r11.getActivity()
            r3 = 17367043(0x1090003, float:2.5162934E-38)
            r0.<init>(r1, r3, r2)
            android.widget.ListView r1 = r11.list
            r1.setAdapter(r0)
            r11.current_folder = r12
            android.app.AlertDialog r12 = r11.folder_dialog
            java.io.File r0 = r11.current_folder
            java.lang.String r0 = r0.getAbsolutePath()
            r12.setTitle(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sourceforge.opencamera.p004ui.FolderChooserDialog.refreshList(java.io.File):void");
    }

    private boolean canWrite() {
        try {
            if (this.current_folder != null && this.current_folder.canWrite()) {
                return true;
            }
        } catch (Exception unused) {
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean useFolder() {
        if (this.current_folder == null) {
            return false;
        }
        if (canWrite()) {
            File baseFolder = StorageUtils.getBaseFolder();
            String absolutePath = this.current_folder.getAbsolutePath();
            if (this.current_folder.getParentFile() != null && this.current_folder.getParentFile().equals(baseFolder)) {
                absolutePath = this.current_folder.getName();
            }
            this.chosen_folder = absolutePath;
            return true;
        }
        Toast.makeText(getActivity(), C0316R.string.cant_write_folder, 0).show();
        return false;
    }

    public String getChosenFolder() {
        return this.chosen_folder;
    }

    public String getChosenFile() {
        return this.chosen_file;
    }

    /* access modifiers changed from: private */
    public void newFolder() {
        if (this.current_folder != null) {
            if (canWrite()) {
                final EditText editText = new EditText(getActivity());
                editText.setSingleLine();
                editText.setTextSize(1, 20.0f);
                editText.setContentDescription(getResources().getString(C0316R.string.enter_new_folder));
                editText.setFilters(new InputFilter[]{new NewFolderInputFilter()});
                new Builder(getActivity()).setTitle(C0316R.string.enter_new_folder).setView(editText).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editText.getText().length() != 0) {
                            try {
                                StringBuilder sb = new StringBuilder();
                                sb.append(FolderChooserDialog.this.current_folder.getAbsolutePath());
                                sb.append(File.separator);
                                sb.append(editText.getText().toString());
                                File file = new File(sb.toString());
                                if (file.exists()) {
                                    Toast.makeText(FolderChooserDialog.this.getActivity(), C0316R.string.folder_exists, 0).show();
                                } else if (file.mkdirs()) {
                                    FolderChooserDialog.this.refreshList(FolderChooserDialog.this.current_folder);
                                } else {
                                    Toast.makeText(FolderChooserDialog.this.getActivity(), C0316R.string.failed_create_folder, 0).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(FolderChooserDialog.this.getActivity(), C0316R.string.failed_create_folder, 0).show();
                            }
                        }
                    }
                }).setNegativeButton(17039360, null).create().show();
            } else {
                Toast.makeText(getActivity(), C0316R.string.cant_write_folder, 0).show();
            }
        }
    }

    public void onResume() {
        super.onResume();
        refreshList(this.current_folder);
    }

    public File getCurrentFolder() {
        return this.current_folder;
    }
}
