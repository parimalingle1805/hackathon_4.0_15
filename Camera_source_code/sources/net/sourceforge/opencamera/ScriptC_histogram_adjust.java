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

public class ScriptC_histogram_adjust extends ScriptC {
    private static final String __rs_resource_name = "histogram_adjust";
    private static final int mExportForEachIdx_histogram_adjust = 1;
    private static final int mExportVarIdx_c_histogram = 0;
    private static final int mExportVarIdx_hdr_alpha = 1;
    private static final int mExportVarIdx_height = 4;
    private static final int mExportVarIdx_n_tiles = 2;
    private static final int mExportVarIdx_width = 3;
    private Element __ALLOCATION;
    private Element __F32;
    private Element __I32;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_F32;
    private FieldPacker __rs_fp_I32;
    private Allocation mExportVar_c_histogram;
    private float mExportVar_hdr_alpha = 0.5f;
    private int mExportVar_height;
    private int mExportVar_n_tiles;
    private int mExportVar_width;

    public ScriptC_histogram_adjust(RenderScript renderScript) {
        super(renderScript, __rs_resource_name, histogram_adjustBitCode.getBitCode32(), histogram_adjustBitCode.getBitCode64());
        this.__ALLOCATION = Element.ALLOCATION(renderScript);
        this.__F32 = Element.F32(renderScript);
        this.mExportVar_n_tiles = 0;
        this.__I32 = Element.I32(renderScript);
        this.mExportVar_width = 0;
        this.mExportVar_height = 0;
        this.__U8_4 = Element.U8_4(renderScript);
    }

    public synchronized void set_c_histogram(Allocation allocation) {
        setVar(0, allocation);
        this.mExportVar_c_histogram = allocation;
    }

    public Allocation get_c_histogram() {
        return this.mExportVar_c_histogram;
    }

    public FieldID getFieldID_c_histogram() {
        return createFieldID(0, null);
    }

    public synchronized void set_hdr_alpha(float f) {
        setVar(1, f);
        this.mExportVar_hdr_alpha = f;
    }

    public float get_hdr_alpha() {
        return this.mExportVar_hdr_alpha;
    }

    public FieldID getFieldID_hdr_alpha() {
        return createFieldID(1, null);
    }

    public synchronized void set_n_tiles(int i) {
        setVar(2, i);
        this.mExportVar_n_tiles = i;
    }

    public int get_n_tiles() {
        return this.mExportVar_n_tiles;
    }

    public FieldID getFieldID_n_tiles() {
        return createFieldID(2, null);
    }

    public synchronized void set_width(int i) {
        setVar(3, i);
        this.mExportVar_width = i;
    }

    public int get_width() {
        return this.mExportVar_width;
    }

    public FieldID getFieldID_width() {
        return createFieldID(3, null);
    }

    public synchronized void set_height(int i) {
        setVar(4, i);
        this.mExportVar_height = i;
    }

    public int get_height() {
        return this.mExportVar_height;
    }

    public FieldID getFieldID_height() {
        return createFieldID(4, null);
    }

    public KernelID getKernelID_histogram_adjust() {
        return createKernelID(1, 59, null, null);
    }

    public void forEach_histogram_adjust(Allocation allocation, Allocation allocation2) {
        forEach_histogram_adjust(allocation, allocation2, null);
    }

    public void forEach_histogram_adjust(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
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
}
