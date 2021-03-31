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
import android.renderscript.Type;

public class ScriptC_histogram_compute extends ScriptC {
    private static final String __rs_resource_name = "histogram_compute";
    private static final int mExportForEachIdx_generate_focus_peaking = 9;
    private static final int mExportForEachIdx_generate_focus_peaking_filtered = 10;
    private static final int mExportForEachIdx_generate_zebra_stripes = 8;
    private static final int mExportForEachIdx_histogram_compute_by_intensity = 4;
    private static final int mExportForEachIdx_histogram_compute_by_intensity_f = 5;
    private static final int mExportForEachIdx_histogram_compute_by_lightness = 6;
    private static final int mExportForEachIdx_histogram_compute_by_luminance = 1;
    private static final int mExportForEachIdx_histogram_compute_by_value = 2;
    private static final int mExportForEachIdx_histogram_compute_by_value_f = 3;
    private static final int mExportForEachIdx_histogram_compute_rgb = 7;
    private static final int mExportFuncIdx_init_histogram = 0;
    private static final int mExportFuncIdx_init_histogram_rgb = 1;
    private static final int mExportVarIdx_bitmap = 6;
    private static final int mExportVarIdx_histogram = 0;
    private static final int mExportVarIdx_histogram_b = 3;
    private static final int mExportVarIdx_histogram_g = 2;
    private static final int mExportVarIdx_histogram_r = 1;
    private static final int mExportVarIdx_zebra_stripes_threshold = 4;
    private static final int mExportVarIdx_zebra_stripes_width = 5;
    private Element __ALLOCATION;
    private Element __F32_3;
    private Element __I32;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_I32;
    private Allocation mExportVar_bitmap;
    private Allocation mExportVar_histogram;
    private Allocation mExportVar_histogram_b;
    private Allocation mExportVar_histogram_g;
    private Allocation mExportVar_histogram_r;
    private int mExportVar_zebra_stripes_threshold = 255;
    private int mExportVar_zebra_stripes_width;

    public ScriptC_histogram_compute(RenderScript renderScript) {
        super(renderScript, __rs_resource_name, histogram_computeBitCode.getBitCode32(), histogram_computeBitCode.getBitCode64());
        this.__I32 = Element.I32(renderScript);
        this.mExportVar_zebra_stripes_width = 40;
        this.__ALLOCATION = Element.ALLOCATION(renderScript);
        this.__U8_4 = Element.U8_4(renderScript);
        this.__F32_3 = Element.F32_3(renderScript);
    }

    public void bind_histogram(Allocation allocation) {
        this.mExportVar_histogram = allocation;
        if (allocation == null) {
            bindAllocation(null, 0);
        } else {
            bindAllocation(allocation, 0);
        }
    }

    public Allocation get_histogram() {
        return this.mExportVar_histogram;
    }

    public void bind_histogram_r(Allocation allocation) {
        this.mExportVar_histogram_r = allocation;
        if (allocation == null) {
            bindAllocation(null, 1);
        } else {
            bindAllocation(allocation, 1);
        }
    }

    public Allocation get_histogram_r() {
        return this.mExportVar_histogram_r;
    }

    public void bind_histogram_g(Allocation allocation) {
        this.mExportVar_histogram_g = allocation;
        if (allocation == null) {
            bindAllocation(null, 2);
        } else {
            bindAllocation(allocation, 2);
        }
    }

    public Allocation get_histogram_g() {
        return this.mExportVar_histogram_g;
    }

    public void bind_histogram_b(Allocation allocation) {
        this.mExportVar_histogram_b = allocation;
        if (allocation == null) {
            bindAllocation(null, 3);
        } else {
            bindAllocation(allocation, 3);
        }
    }

    public Allocation get_histogram_b() {
        return this.mExportVar_histogram_b;
    }

    public synchronized void set_zebra_stripes_threshold(int i) {
        setVar(4, i);
        this.mExportVar_zebra_stripes_threshold = i;
    }

    public int get_zebra_stripes_threshold() {
        return this.mExportVar_zebra_stripes_threshold;
    }

    public FieldID getFieldID_zebra_stripes_threshold() {
        return createFieldID(4, null);
    }

    public synchronized void set_zebra_stripes_width(int i) {
        setVar(5, i);
        this.mExportVar_zebra_stripes_width = i;
    }

    public int get_zebra_stripes_width() {
        return this.mExportVar_zebra_stripes_width;
    }

    public FieldID getFieldID_zebra_stripes_width() {
        return createFieldID(5, null);
    }

    public synchronized void set_bitmap(Allocation allocation) {
        setVar(6, allocation);
        this.mExportVar_bitmap = allocation;
    }

    public Allocation get_bitmap() {
        return this.mExportVar_bitmap;
    }

    public FieldID getFieldID_bitmap() {
        return createFieldID(6, null);
    }

    public KernelID getKernelID_histogram_compute_by_luminance() {
        return createKernelID(1, 57, null, null);
    }

    public void forEach_histogram_compute_by_luminance(Allocation allocation) {
        forEach_histogram_compute_by_luminance(allocation, null);
    }

    public void forEach_histogram_compute_by_luminance(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__U8_4)) {
            forEach(1, allocation, null, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with U8_4!");
    }

    public KernelID getKernelID_histogram_compute_by_value() {
        return createKernelID(2, 57, null, null);
    }

    public void forEach_histogram_compute_by_value(Allocation allocation) {
        forEach_histogram_compute_by_value(allocation, null);
    }

    public void forEach_histogram_compute_by_value(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__U8_4)) {
            forEach(2, allocation, null, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with U8_4!");
    }

    public KernelID getKernelID_histogram_compute_by_value_f() {
        return createKernelID(3, 57, null, null);
    }

    public void forEach_histogram_compute_by_value_f(Allocation allocation) {
        forEach_histogram_compute_by_value_f(allocation, null);
    }

    public void forEach_histogram_compute_by_value_f(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__F32_3)) {
            forEach(3, allocation, null, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with F32_3!");
    }

    public KernelID getKernelID_histogram_compute_by_intensity() {
        return createKernelID(4, 57, null, null);
    }

    public void forEach_histogram_compute_by_intensity(Allocation allocation) {
        forEach_histogram_compute_by_intensity(allocation, null);
    }

    public void forEach_histogram_compute_by_intensity(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__U8_4)) {
            forEach(4, allocation, null, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with U8_4!");
    }

    public KernelID getKernelID_histogram_compute_by_intensity_f() {
        return createKernelID(5, 57, null, null);
    }

    public void forEach_histogram_compute_by_intensity_f(Allocation allocation) {
        forEach_histogram_compute_by_intensity_f(allocation, null);
    }

    public void forEach_histogram_compute_by_intensity_f(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__F32_3)) {
            forEach(5, allocation, null, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with F32_3!");
    }

    public KernelID getKernelID_histogram_compute_by_lightness() {
        return createKernelID(6, 57, null, null);
    }

    public void forEach_histogram_compute_by_lightness(Allocation allocation) {
        forEach_histogram_compute_by_lightness(allocation, null);
    }

    public void forEach_histogram_compute_by_lightness(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__U8_4)) {
            forEach(6, allocation, null, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with U8_4!");
    }

    public KernelID getKernelID_histogram_compute_rgb() {
        return createKernelID(7, 57, null, null);
    }

    public void forEach_histogram_compute_rgb(Allocation allocation) {
        forEach_histogram_compute_rgb(allocation, null);
    }

    public void forEach_histogram_compute_rgb(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__U8_4)) {
            forEach(7, allocation, null, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with U8_4!");
    }

    public KernelID getKernelID_generate_zebra_stripes() {
        return createKernelID(8, 59, null, null);
    }

    public void forEach_generate_zebra_stripes(Allocation allocation, Allocation allocation2) {
        forEach_generate_zebra_stripes(allocation, allocation2, null);
    }

    public void forEach_generate_zebra_stripes(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        String str = "Type mismatch with U8_4!";
        if (!allocation.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException(str);
        } else if (allocation2.getType().getElement().isCompatible(this.__U8_4)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(8, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException(str);
        }
    }

    public KernelID getKernelID_generate_focus_peaking() {
        return createKernelID(9, 59, null, null);
    }

    public void forEach_generate_focus_peaking(Allocation allocation, Allocation allocation2) {
        forEach_generate_focus_peaking(allocation, allocation2, null);
    }

    public void forEach_generate_focus_peaking(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        String str = "Type mismatch with U8_4!";
        if (!allocation.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException(str);
        } else if (allocation2.getType().getElement().isCompatible(this.__U8_4)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(9, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException(str);
        }
    }

    public KernelID getKernelID_generate_focus_peaking_filtered() {
        return createKernelID(10, 59, null, null);
    }

    public void forEach_generate_focus_peaking_filtered(Allocation allocation, Allocation allocation2) {
        forEach_generate_focus_peaking_filtered(allocation, allocation2, null);
    }

    public void forEach_generate_focus_peaking_filtered(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        String str = "Type mismatch with U8_4!";
        if (!allocation.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException(str);
        } else if (allocation2.getType().getElement().isCompatible(this.__U8_4)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(10, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException(str);
        }
    }

    public void invoke_init_histogram() {
        invoke(0);
    }

    public void invoke_init_histogram_rgb() {
        invoke(1);
    }
}
