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

public class ScriptC_pyramid_blending extends ScriptC {
    private static final String __rs_resource_name = "pyramid_blending";
    public static final float const_g0 = 0.05f;
    public static final float const_g1 = 0.25f;
    public static final float const_g2 = 0.4f;
    private static final int mExportForEachIdx_add = 7;
    private static final int mExportForEachIdx_blur = 3;
    private static final int mExportForEachIdx_blur1dX = 4;
    private static final int mExportForEachIdx_blur1dY = 5;
    private static final int mExportForEachIdx_compute_error = 10;
    private static final int mExportForEachIdx_expand = 2;
    private static final int mExportForEachIdx_merge = 8;
    private static final int mExportForEachIdx_merge_f = 9;
    private static final int mExportForEachIdx_reduce = 1;
    private static final int mExportForEachIdx_subtract = 6;
    private static final int mExportFuncIdx_init_errors = 1;
    private static final int mExportFuncIdx_setBlendWidth = 0;
    private static final int mExportVarIdx_bitmap = 0;
    private static final int mExportVarIdx_errors = 5;
    private static final int mExportVarIdx_g0 = 1;
    private static final int mExportVarIdx_g1 = 2;
    private static final int mExportVarIdx_g2 = 3;
    private static final int mExportVarIdx_interpolated_best_path = 4;
    private Element __ALLOCATION;
    private Element __F32;
    private Element __F32_3;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_F32;
    private Allocation mExportVar_bitmap;
    private Allocation mExportVar_errors;
    private float mExportVar_g0 = 0.05f;
    private float mExportVar_g1;
    private float mExportVar_g2;
    private Allocation mExportVar_interpolated_best_path;

    public ScriptC_pyramid_blending(RenderScript renderScript) {
        super(renderScript, __rs_resource_name, pyramid_blendingBitCode.getBitCode32(), pyramid_blendingBitCode.getBitCode64());
        this.__ALLOCATION = Element.ALLOCATION(renderScript);
        this.__F32 = Element.F32(renderScript);
        this.mExportVar_g1 = 0.25f;
        this.mExportVar_g2 = 0.4f;
        this.__U8_4 = Element.U8_4(renderScript);
        this.__F32_3 = Element.F32_3(renderScript);
    }

    public synchronized void set_bitmap(Allocation allocation) {
        setVar(0, allocation);
        this.mExportVar_bitmap = allocation;
    }

    public Allocation get_bitmap() {
        return this.mExportVar_bitmap;
    }

    public FieldID getFieldID_bitmap() {
        return createFieldID(0, null);
    }

    public float get_g0() {
        return this.mExportVar_g0;
    }

    public FieldID getFieldID_g0() {
        return createFieldID(1, null);
    }

    public float get_g1() {
        return this.mExportVar_g1;
    }

    public FieldID getFieldID_g1() {
        return createFieldID(2, null);
    }

    public float get_g2() {
        return this.mExportVar_g2;
    }

    public FieldID getFieldID_g2() {
        return createFieldID(3, null);
    }

    public void bind_interpolated_best_path(Allocation allocation) {
        this.mExportVar_interpolated_best_path = allocation;
        if (allocation == null) {
            bindAllocation(null, 4);
        } else {
            bindAllocation(allocation, 4);
        }
    }

    public Allocation get_interpolated_best_path() {
        return this.mExportVar_interpolated_best_path;
    }

    public void bind_errors(Allocation allocation) {
        this.mExportVar_errors = allocation;
        if (allocation == null) {
            bindAllocation(null, 5);
        } else {
            bindAllocation(allocation, 5);
        }
    }

    public Allocation get_errors() {
        return this.mExportVar_errors;
    }

    public KernelID getKernelID_reduce() {
        return createKernelID(1, 59, null, null);
    }

    public void forEach_reduce(Allocation allocation, Allocation allocation2) {
        forEach_reduce(allocation, allocation2, null);
    }

    public void forEach_reduce(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        String str = "Type mismatch with U8_4!";
        if (!allocation.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException(str);
        } else if (allocation2.getType().getElement().isCompatible(this.__U8_4)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(1, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException(str);
        }
    }

    public KernelID getKernelID_expand() {
        return createKernelID(2, 59, null, null);
    }

    public void forEach_expand(Allocation allocation, Allocation allocation2) {
        forEach_expand(allocation, allocation2, null);
    }

    public void forEach_expand(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        String str = "Type mismatch with U8_4!";
        if (!allocation.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException(str);
        } else if (allocation2.getType().getElement().isCompatible(this.__U8_4)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(2, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException(str);
        }
    }

    public KernelID getKernelID_blur() {
        return createKernelID(3, 59, null, null);
    }

    public void forEach_blur(Allocation allocation, Allocation allocation2) {
        forEach_blur(allocation, allocation2, null);
    }

    public void forEach_blur(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        String str = "Type mismatch with U8_4!";
        if (!allocation.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException(str);
        } else if (allocation2.getType().getElement().isCompatible(this.__U8_4)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(3, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException(str);
        }
    }

    public KernelID getKernelID_blur1dX() {
        return createKernelID(4, 59, null, null);
    }

    public void forEach_blur1dX(Allocation allocation, Allocation allocation2) {
        forEach_blur1dX(allocation, allocation2, null);
    }

    public void forEach_blur1dX(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        String str = "Type mismatch with U8_4!";
        if (!allocation.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException(str);
        } else if (allocation2.getType().getElement().isCompatible(this.__U8_4)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(4, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException(str);
        }
    }

    public KernelID getKernelID_blur1dY() {
        return createKernelID(5, 59, null, null);
    }

    public void forEach_blur1dY(Allocation allocation, Allocation allocation2) {
        forEach_blur1dY(allocation, allocation2, null);
    }

    public void forEach_blur1dY(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        String str = "Type mismatch with U8_4!";
        if (!allocation.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException(str);
        } else if (allocation2.getType().getElement().isCompatible(this.__U8_4)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(5, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException(str);
        }
    }

    public KernelID getKernelID_subtract() {
        return createKernelID(6, 59, null, null);
    }

    public void forEach_subtract(Allocation allocation, Allocation allocation2) {
        forEach_subtract(allocation, allocation2, null);
    }

    public void forEach_subtract(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        if (!allocation.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        } else if (allocation2.getType().getElement().isCompatible(this.__F32_3)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(6, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException("Type mismatch with F32_3!");
        }
    }

    public KernelID getKernelID_add() {
        return createKernelID(7, 59, null, null);
    }

    public void forEach_add(Allocation allocation, Allocation allocation2) {
        forEach_add(allocation, allocation2, null);
    }

    public void forEach_add(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        String str = "Type mismatch with U8_4!";
        if (!allocation.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException(str);
        } else if (allocation2.getType().getElement().isCompatible(this.__U8_4)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(7, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException(str);
        }
    }

    public KernelID getKernelID_merge() {
        return createKernelID(8, 59, null, null);
    }

    public void forEach_merge(Allocation allocation, Allocation allocation2) {
        forEach_merge(allocation, allocation2, null);
    }

    public void forEach_merge(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
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

    public KernelID getKernelID_merge_f() {
        return createKernelID(9, 59, null, null);
    }

    public void forEach_merge_f(Allocation allocation, Allocation allocation2) {
        forEach_merge_f(allocation, allocation2, null);
    }

    public void forEach_merge_f(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        String str = "Type mismatch with F32_3!";
        if (!allocation.getType().getElement().isCompatible(this.__F32_3)) {
            throw new RSRuntimeException(str);
        } else if (allocation2.getType().getElement().isCompatible(this.__F32_3)) {
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

    public KernelID getKernelID_compute_error() {
        return createKernelID(10, 57, null, null);
    }

    public void forEach_compute_error(Allocation allocation) {
        forEach_compute_error(allocation, null);
    }

    public void forEach_compute_error(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__U8_4)) {
            forEach(10, allocation, null, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with U8_4!");
    }

    public void invoke_setBlendWidth(int i, int i2) {
        FieldPacker fieldPacker = new FieldPacker(8);
        fieldPacker.addI32(i);
        fieldPacker.addI32(i2);
        invoke(0, fieldPacker);
    }

    public void invoke_init_errors() {
        invoke(1);
    }
}
