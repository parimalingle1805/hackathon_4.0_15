package net.sourceforge.opencamera;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.FieldPacker;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.Script.FieldID;
import android.renderscript.Script.KernelID;
import android.renderscript.Script.LaunchOptions;
import android.renderscript.ScriptC;

public class ScriptC_create_mtb extends ScriptC {
    private static final String __rs_resource_name = "create_mtb";
    private static final int mExportForEachIdx_create_greyscale = 2;
    private static final int mExportForEachIdx_create_greyscale_f = 3;
    private static final int mExportForEachIdx_create_mtb = 1;
    private static final int mExportVarIdx_median_value = 1;
    private static final int mExportVarIdx_out_bitmap = 0;
    private static final int mExportVarIdx_start_x = 2;
    private static final int mExportVarIdx_start_y = 3;
    private Element __ALLOCATION;
    private Element __F32_3;
    private Element __I32;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_I32;
    private int mExportVar_median_value = 0;
    private Allocation mExportVar_out_bitmap;
    private int mExportVar_start_x;
    private int mExportVar_start_y;

    public ScriptC_create_mtb(RenderScript renderScript) {
        super(renderScript, __rs_resource_name, create_mtbBitCode.getBitCode32(), create_mtbBitCode.getBitCode64());
        this.__ALLOCATION = Element.ALLOCATION(renderScript);
        this.__I32 = Element.I32(renderScript);
        this.mExportVar_start_x = 0;
        this.mExportVar_start_y = 0;
        this.__U8_4 = Element.U8_4(renderScript);
        this.__F32_3 = Element.F32_3(renderScript);
    }

    public synchronized void set_out_bitmap(Allocation allocation) {
        setVar(0, allocation);
        this.mExportVar_out_bitmap = allocation;
    }

    public Allocation get_out_bitmap() {
        return this.mExportVar_out_bitmap;
    }

    public FieldID getFieldID_out_bitmap() {
        return createFieldID(0, null);
    }

    public synchronized void set_median_value(int i) {
        setVar(1, i);
        this.mExportVar_median_value = i;
    }

    public int get_median_value() {
        return this.mExportVar_median_value;
    }

    public FieldID getFieldID_median_value() {
        return createFieldID(1, null);
    }

    public synchronized void set_start_x(int i) {
        setVar(2, i);
        this.mExportVar_start_x = i;
    }

    public int get_start_x() {
        return this.mExportVar_start_x;
    }

    public FieldID getFieldID_start_x() {
        return createFieldID(2, null);
    }

    public synchronized void set_start_y(int i) {
        setVar(3, i);
        this.mExportVar_start_y = i;
    }

    public int get_start_y() {
        return this.mExportVar_start_y;
    }

    public FieldID getFieldID_start_y() {
        return createFieldID(3, null);
    }

    public KernelID getKernelID_create_mtb() {
        return createKernelID(1, 57, null, null);
    }

    public void forEach_create_mtb(Allocation allocation) {
        forEach_create_mtb(allocation, null);
    }

    public void forEach_create_mtb(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__U8_4)) {
            forEach(1, allocation, null, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with U8_4!");
    }

    public KernelID getKernelID_create_greyscale() {
        return createKernelID(2, 57, null, null);
    }

    public void forEach_create_greyscale(Allocation allocation) {
        forEach_create_greyscale(allocation, null);
    }

    public void forEach_create_greyscale(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__U8_4)) {
            forEach(2, allocation, null, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with U8_4!");
    }

    public KernelID getKernelID_create_greyscale_f() {
        return createKernelID(3, 57, null, null);
    }

    public void forEach_create_greyscale_f(Allocation allocation) {
        forEach_create_greyscale_f(allocation, null);
    }

    public void forEach_create_greyscale_f(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__F32_3)) {
            forEach(3, allocation, null, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with F32_3!");
    }
}
