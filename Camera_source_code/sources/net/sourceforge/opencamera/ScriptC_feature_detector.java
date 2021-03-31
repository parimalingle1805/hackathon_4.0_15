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

public class ScriptC_feature_detector extends ScriptC {
    private static final String __rs_resource_name = "feature_detector";
    private static final int mExportForEachIdx_compute_derivatives = 2;
    private static final int mExportForEachIdx_corner_detector = 3;
    private static final int mExportForEachIdx_create_greyscale = 1;
    private static final int mExportForEachIdx_local_maximum = 4;
    private static final int mExportVarIdx_bitmap = 1;
    private static final int mExportVarIdx_bitmap_Ix = 2;
    private static final int mExportVarIdx_bitmap_Iy = 3;
    private static final int mExportVarIdx_corner_threshold = 0;
    private Element __ALLOCATION;
    private Element __F32;
    private Element __U8;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_F32;
    private Allocation mExportVar_bitmap;
    private Allocation mExportVar_bitmap_Ix;
    private Allocation mExportVar_bitmap_Iy;
    private float mExportVar_corner_threshold;

    public ScriptC_feature_detector(RenderScript renderScript) {
        super(renderScript, __rs_resource_name, feature_detectorBitCode.getBitCode32(), feature_detectorBitCode.getBitCode64());
        this.__F32 = Element.F32(renderScript);
        this.__ALLOCATION = Element.ALLOCATION(renderScript);
        this.__U8_4 = Element.U8_4(renderScript);
        this.__U8 = Element.U8(renderScript);
    }

    public synchronized void set_corner_threshold(float f) {
        setVar(0, f);
        this.mExportVar_corner_threshold = f;
    }

    public float get_corner_threshold() {
        return this.mExportVar_corner_threshold;
    }

    public FieldID getFieldID_corner_threshold() {
        return createFieldID(0, null);
    }

    public synchronized void set_bitmap(Allocation allocation) {
        setVar(1, allocation);
        this.mExportVar_bitmap = allocation;
    }

    public Allocation get_bitmap() {
        return this.mExportVar_bitmap;
    }

    public FieldID getFieldID_bitmap() {
        return createFieldID(1, null);
    }

    public synchronized void set_bitmap_Ix(Allocation allocation) {
        setVar(2, allocation);
        this.mExportVar_bitmap_Ix = allocation;
    }

    public Allocation get_bitmap_Ix() {
        return this.mExportVar_bitmap_Ix;
    }

    public FieldID getFieldID_bitmap_Ix() {
        return createFieldID(2, null);
    }

    public synchronized void set_bitmap_Iy(Allocation allocation) {
        setVar(3, allocation);
        this.mExportVar_bitmap_Iy = allocation;
    }

    public Allocation get_bitmap_Iy() {
        return this.mExportVar_bitmap_Iy;
    }

    public FieldID getFieldID_bitmap_Iy() {
        return createFieldID(3, null);
    }

    public KernelID getKernelID_create_greyscale() {
        return createKernelID(1, 59, null, null);
    }

    public void forEach_create_greyscale(Allocation allocation, Allocation allocation2) {
        forEach_create_greyscale(allocation, allocation2, null);
    }

    public void forEach_create_greyscale(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        if (!allocation.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        } else if (allocation2.getType().getElement().isCompatible(this.__U8)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(1, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException("Type mismatch with U8!");
        }
    }

    public KernelID getKernelID_compute_derivatives() {
        return createKernelID(2, 57, null, null);
    }

    public void forEach_compute_derivatives(Allocation allocation) {
        forEach_compute_derivatives(allocation, null);
    }

    public void forEach_compute_derivatives(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__U8)) {
            forEach(2, allocation, null, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with U8!");
    }

    public KernelID getKernelID_corner_detector() {
        return createKernelID(3, 59, null, null);
    }

    public void forEach_corner_detector(Allocation allocation, Allocation allocation2) {
        forEach_corner_detector(allocation, allocation2, null);
    }

    public void forEach_corner_detector(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        if (!allocation.getType().getElement().isCompatible(this.__U8)) {
            throw new RSRuntimeException("Type mismatch with U8!");
        } else if (allocation2.getType().getElement().isCompatible(this.__F32)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(3, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException("Type mismatch with F32!");
        }
    }

    public KernelID getKernelID_local_maximum() {
        return createKernelID(4, 59, null, null);
    }

    public void forEach_local_maximum(Allocation allocation, Allocation allocation2) {
        forEach_local_maximum(allocation, allocation2, null);
    }

    public void forEach_local_maximum(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
        if (!allocation.getType().getElement().isCompatible(this.__F32)) {
            throw new RSRuntimeException("Type mismatch with F32!");
        } else if (allocation2.getType().getElement().isCompatible(this.__U8)) {
            Type type = allocation.getType();
            Type type2 = allocation2.getType();
            if (type.getCount() == type2.getCount() && type.getX() == type2.getX() && type.getY() == type2.getY() && type.getZ() == type2.getZ() && type.hasFaces() == type2.hasFaces() && type.hasMipmaps() == type2.hasMipmaps()) {
                forEach(4, allocation, allocation2, null, launchOptions);
                return;
            }
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        } else {
            throw new RSRuntimeException("Type mismatch with U8!");
        }
    }
}
