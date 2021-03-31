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

public class ScriptC_avg_brighten extends ScriptC {
    private static final String __rs_resource_name = "avg_brighten";
    private static final int mExportForEachIdx_avg_brighten_f = 1;
    private static final int mExportForEachIdx_dro_brighten = 2;
    private static final int mExportFuncIdx_setBlackLevel = 0;
    private static final int mExportFuncIdx_setBrightenParameters = 1;
    private static final int mExportVarIdx_bitmap = 0;
    private static final int mExportVarIdx_median_filter_strength = 1;
    private Element __ALLOCATION;
    private Element __F32;
    private Element __F32_3;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_F32;
    private Allocation mExportVar_bitmap;
    private float mExportVar_median_filter_strength = 1.0f;

    public ScriptC_avg_brighten(RenderScript renderScript) {
        super(renderScript, __rs_resource_name, avg_brightenBitCode.getBitCode32(), avg_brightenBitCode.getBitCode64());
        this.__ALLOCATION = Element.ALLOCATION(renderScript);
        this.__F32 = Element.F32(renderScript);
        this.__F32_3 = Element.F32_3(renderScript);
        this.__U8_4 = Element.U8_4(renderScript);
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

    public synchronized void set_median_filter_strength(float f) {
        setVar(1, f);
        this.mExportVar_median_filter_strength = f;
    }

    public float get_median_filter_strength() {
        return this.mExportVar_median_filter_strength;
    }

    public FieldID getFieldID_median_filter_strength() {
        return createFieldID(1, null);
    }

    public KernelID getKernelID_avg_brighten_f() {
        return createKernelID(1, 59, null, null);
    }

    public void forEach_avg_brighten_f(Allocation allocation, Allocation allocation2) {
        forEach_avg_brighten_f(allocation, allocation2, null);
    }

    public void forEach_avg_brighten_f(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        if (!allocation.getType().getElement().isCompatible(this.__F32_3)) {
            throw new RSRuntimeException("Type mismatch with F32_3!");
        } else if (allocation2.getType().getElement().isCompatible(this.__U8_4)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(1, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
    }

    public KernelID getKernelID_dro_brighten() {
        return createKernelID(2, 59, null, null);
    }

    public void forEach_dro_brighten(Allocation allocation, Allocation allocation2) {
        forEach_dro_brighten(allocation, allocation2, null);
    }

    public void forEach_dro_brighten(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
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

    public void invoke_setBlackLevel(float f) {
        FieldPacker fieldPacker = new FieldPacker(4);
        fieldPacker.addF32(f);
        invoke(0, fieldPacker);
    }

    public void invoke_setBrightenParameters(float f, float f2, float f3, float f4, float f5) {
        FieldPacker fieldPacker = new FieldPacker(20);
        fieldPacker.addF32(f);
        fieldPacker.addF32(f2);
        fieldPacker.addF32(f3);
        fieldPacker.addF32(f4);
        fieldPacker.addF32(f5);
        invoke(1, fieldPacker);
    }
}
