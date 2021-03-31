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

public class ScriptC_process_hdr extends ScriptC {
    private static final String __rs_resource_name = "process_hdr";
    public static final float const_exposure = 1.2f;
    public static final float const_filmic_exposure_bias = 0.007843138f;
    public static final int const_tonemap_algorithm_aces_c = 4;
    public static final int const_tonemap_algorithm_clamp_c = 0;
    public static final int const_tonemap_algorithm_exponential_c = 1;
    public static final int const_tonemap_algorithm_filmic_c = 3;
    public static final int const_tonemap_algorithm_reinhard_c = 2;
    public static final float const_weight_scale_c = 0.0077816225f;
    private static final int mExportForEachIdx_hdr = 1;
    private static final int mExportForEachIdx_hdr_n = 2;
    private static final int mExportVarIdx_W = 45;
    private static final int mExportVarIdx_bitmap0 = 0;
    private static final int mExportVarIdx_bitmap1 = 1;
    private static final int mExportVarIdx_bitmap2 = 2;
    private static final int mExportVarIdx_bitmap3 = 3;
    private static final int mExportVarIdx_bitmap4 = 4;
    private static final int mExportVarIdx_bitmap5 = 5;
    private static final int mExportVarIdx_bitmap6 = 6;
    private static final int mExportVarIdx_exposure = 42;
    private static final int mExportVarIdx_filmic_exposure_bias = 44;
    private static final int mExportVarIdx_linear_scale = 46;
    private static final int mExportVarIdx_n_bitmaps_g = 47;
    private static final int mExportVarIdx_offset_x0 = 7;
    private static final int mExportVarIdx_offset_x1 = 9;
    private static final int mExportVarIdx_offset_x2 = 11;
    private static final int mExportVarIdx_offset_x3 = 13;
    private static final int mExportVarIdx_offset_x4 = 15;
    private static final int mExportVarIdx_offset_x5 = 17;
    private static final int mExportVarIdx_offset_x6 = 19;
    private static final int mExportVarIdx_offset_y0 = 8;
    private static final int mExportVarIdx_offset_y1 = 10;
    private static final int mExportVarIdx_offset_y2 = 12;
    private static final int mExportVarIdx_offset_y3 = 14;
    private static final int mExportVarIdx_offset_y4 = 16;
    private static final int mExportVarIdx_offset_y5 = 18;
    private static final int mExportVarIdx_offset_y6 = 20;
    private static final int mExportVarIdx_parameter_A0 = 21;
    private static final int mExportVarIdx_parameter_A1 = 23;
    private static final int mExportVarIdx_parameter_A2 = 25;
    private static final int mExportVarIdx_parameter_A3 = 27;
    private static final int mExportVarIdx_parameter_A4 = 29;
    private static final int mExportVarIdx_parameter_A5 = 31;
    private static final int mExportVarIdx_parameter_A6 = 33;
    private static final int mExportVarIdx_parameter_B0 = 22;
    private static final int mExportVarIdx_parameter_B1 = 24;
    private static final int mExportVarIdx_parameter_B2 = 26;
    private static final int mExportVarIdx_parameter_B3 = 28;
    private static final int mExportVarIdx_parameter_B4 = 30;
    private static final int mExportVarIdx_parameter_B5 = 32;
    private static final int mExportVarIdx_parameter_B6 = 34;
    private static final int mExportVarIdx_tonemap_algorithm = 41;
    private static final int mExportVarIdx_tonemap_algorithm_aces_c = 40;
    private static final int mExportVarIdx_tonemap_algorithm_clamp_c = 36;
    private static final int mExportVarIdx_tonemap_algorithm_exponential_c = 37;
    private static final int mExportVarIdx_tonemap_algorithm_filmic_c = 39;
    private static final int mExportVarIdx_tonemap_algorithm_reinhard_c = 38;
    private static final int mExportVarIdx_tonemap_scale = 43;
    private static final int mExportVarIdx_weight_scale_c = 35;
    private Element __ALLOCATION;
    private Element __F32;
    private Element __I32;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_F32;
    private FieldPacker __rs_fp_I32;
    private float mExportVar_W;
    private Allocation mExportVar_bitmap0;
    private Allocation mExportVar_bitmap1;
    private Allocation mExportVar_bitmap2;
    private Allocation mExportVar_bitmap3;
    private Allocation mExportVar_bitmap4;
    private Allocation mExportVar_bitmap5;
    private Allocation mExportVar_bitmap6;
    private float mExportVar_exposure;
    private float mExportVar_filmic_exposure_bias;
    private float mExportVar_linear_scale;
    private int mExportVar_n_bitmaps_g;
    private int mExportVar_offset_x0 = 0;
    private int mExportVar_offset_x1;
    private int mExportVar_offset_x2;
    private int mExportVar_offset_x3;
    private int mExportVar_offset_x4;
    private int mExportVar_offset_x5;
    private int mExportVar_offset_x6;
    private int mExportVar_offset_y0;
    private int mExportVar_offset_y1;
    private int mExportVar_offset_y2;
    private int mExportVar_offset_y3;
    private int mExportVar_offset_y4;
    private int mExportVar_offset_y5;
    private int mExportVar_offset_y6;
    private float mExportVar_parameter_A0;
    private float mExportVar_parameter_A1;
    private float mExportVar_parameter_A2;
    private float mExportVar_parameter_A3;
    private float mExportVar_parameter_A4;
    private float mExportVar_parameter_A5;
    private float mExportVar_parameter_A6;
    private float mExportVar_parameter_B0;
    private float mExportVar_parameter_B1;
    private float mExportVar_parameter_B2;
    private float mExportVar_parameter_B3;
    private float mExportVar_parameter_B4;
    private float mExportVar_parameter_B5;
    private float mExportVar_parameter_B6;
    private int mExportVar_tonemap_algorithm;
    private int mExportVar_tonemap_algorithm_aces_c;
    private int mExportVar_tonemap_algorithm_clamp_c;
    private int mExportVar_tonemap_algorithm_exponential_c;
    private int mExportVar_tonemap_algorithm_filmic_c;
    private int mExportVar_tonemap_algorithm_reinhard_c;
    private float mExportVar_tonemap_scale;
    private float mExportVar_weight_scale_c;

    public ScriptC_process_hdr(RenderScript renderScript) {
        super(renderScript, __rs_resource_name, process_hdrBitCode.getBitCode32(), process_hdrBitCode.getBitCode64());
        this.__ALLOCATION = Element.ALLOCATION(renderScript);
        this.__I32 = Element.I32(renderScript);
        this.mExportVar_offset_y0 = 0;
        this.mExportVar_offset_x1 = 0;
        this.mExportVar_offset_y1 = 0;
        this.mExportVar_offset_x2 = 0;
        this.mExportVar_offset_y2 = 0;
        this.mExportVar_offset_x3 = 0;
        this.mExportVar_offset_y3 = 0;
        this.mExportVar_offset_x4 = 0;
        this.mExportVar_offset_y4 = 0;
        this.mExportVar_offset_x5 = 0;
        this.mExportVar_offset_y5 = 0;
        this.mExportVar_offset_x6 = 0;
        this.mExportVar_offset_y6 = 0;
        this.mExportVar_parameter_A0 = 1.0f;
        this.__F32 = Element.F32(renderScript);
        this.mExportVar_parameter_B0 = 0.0f;
        this.mExportVar_parameter_A1 = 1.0f;
        this.mExportVar_parameter_B1 = 0.0f;
        this.mExportVar_parameter_A2 = 1.0f;
        this.mExportVar_parameter_B2 = 0.0f;
        this.mExportVar_parameter_A3 = 1.0f;
        this.mExportVar_parameter_B3 = 0.0f;
        this.mExportVar_parameter_A4 = 1.0f;
        this.mExportVar_parameter_B4 = 0.0f;
        this.mExportVar_parameter_A5 = 1.0f;
        this.mExportVar_parameter_B5 = 0.0f;
        this.mExportVar_parameter_A6 = 1.0f;
        this.mExportVar_parameter_B6 = 0.0f;
        this.mExportVar_weight_scale_c = 0.0077816225f;
        this.mExportVar_tonemap_algorithm_clamp_c = 0;
        this.mExportVar_tonemap_algorithm_exponential_c = 1;
        this.mExportVar_tonemap_algorithm_reinhard_c = 2;
        this.mExportVar_tonemap_algorithm_filmic_c = 3;
        this.mExportVar_tonemap_algorithm_aces_c = 4;
        this.mExportVar_tonemap_algorithm = 2;
        this.mExportVar_exposure = 1.2f;
        this.mExportVar_tonemap_scale = 1.0f;
        this.mExportVar_filmic_exposure_bias = 0.007843138f;
        this.mExportVar_W = 11.2f;
        this.mExportVar_linear_scale = 1.0f;
        this.mExportVar_n_bitmaps_g = 3;
        this.__U8_4 = Element.U8_4(renderScript);
    }

    public synchronized void set_bitmap0(Allocation allocation) {
        setVar(0, allocation);
        this.mExportVar_bitmap0 = allocation;
    }

    public Allocation get_bitmap0() {
        return this.mExportVar_bitmap0;
    }

    public FieldID getFieldID_bitmap0() {
        return createFieldID(0, null);
    }

    public synchronized void set_bitmap1(Allocation allocation) {
        setVar(1, allocation);
        this.mExportVar_bitmap1 = allocation;
    }

    public Allocation get_bitmap1() {
        return this.mExportVar_bitmap1;
    }

    public FieldID getFieldID_bitmap1() {
        return createFieldID(1, null);
    }

    public synchronized void set_bitmap2(Allocation allocation) {
        setVar(2, allocation);
        this.mExportVar_bitmap2 = allocation;
    }

    public Allocation get_bitmap2() {
        return this.mExportVar_bitmap2;
    }

    public FieldID getFieldID_bitmap2() {
        return createFieldID(2, null);
    }

    public synchronized void set_bitmap3(Allocation allocation) {
        setVar(3, allocation);
        this.mExportVar_bitmap3 = allocation;
    }

    public Allocation get_bitmap3() {
        return this.mExportVar_bitmap3;
    }

    public FieldID getFieldID_bitmap3() {
        return createFieldID(3, null);
    }

    public synchronized void set_bitmap4(Allocation allocation) {
        setVar(4, allocation);
        this.mExportVar_bitmap4 = allocation;
    }

    public Allocation get_bitmap4() {
        return this.mExportVar_bitmap4;
    }

    public FieldID getFieldID_bitmap4() {
        return createFieldID(4, null);
    }

    public synchronized void set_bitmap5(Allocation allocation) {
        setVar(5, allocation);
        this.mExportVar_bitmap5 = allocation;
    }

    public Allocation get_bitmap5() {
        return this.mExportVar_bitmap5;
    }

    public FieldID getFieldID_bitmap5() {
        return createFieldID(5, null);
    }

    public synchronized void set_bitmap6(Allocation allocation) {
        setVar(6, allocation);
        this.mExportVar_bitmap6 = allocation;
    }

    public Allocation get_bitmap6() {
        return this.mExportVar_bitmap6;
    }

    public FieldID getFieldID_bitmap6() {
        return createFieldID(6, null);
    }

    public synchronized void set_offset_x0(int i) {
        setVar(7, i);
        this.mExportVar_offset_x0 = i;
    }

    public int get_offset_x0() {
        return this.mExportVar_offset_x0;
    }

    public FieldID getFieldID_offset_x0() {
        return createFieldID(7, null);
    }

    public synchronized void set_offset_y0(int i) {
        setVar(8, i);
        this.mExportVar_offset_y0 = i;
    }

    public int get_offset_y0() {
        return this.mExportVar_offset_y0;
    }

    public FieldID getFieldID_offset_y0() {
        return createFieldID(8, null);
    }

    public synchronized void set_offset_x1(int i) {
        setVar(9, i);
        this.mExportVar_offset_x1 = i;
    }

    public int get_offset_x1() {
        return this.mExportVar_offset_x1;
    }

    public FieldID getFieldID_offset_x1() {
        return createFieldID(9, null);
    }

    public synchronized void set_offset_y1(int i) {
        setVar(10, i);
        this.mExportVar_offset_y1 = i;
    }

    public int get_offset_y1() {
        return this.mExportVar_offset_y1;
    }

    public FieldID getFieldID_offset_y1() {
        return createFieldID(10, null);
    }

    public synchronized void set_offset_x2(int i) {
        setVar(11, i);
        this.mExportVar_offset_x2 = i;
    }

    public int get_offset_x2() {
        return this.mExportVar_offset_x2;
    }

    public FieldID getFieldID_offset_x2() {
        return createFieldID(11, null);
    }

    public synchronized void set_offset_y2(int i) {
        setVar(12, i);
        this.mExportVar_offset_y2 = i;
    }

    public int get_offset_y2() {
        return this.mExportVar_offset_y2;
    }

    public FieldID getFieldID_offset_y2() {
        return createFieldID(12, null);
    }

    public synchronized void set_offset_x3(int i) {
        setVar(13, i);
        this.mExportVar_offset_x3 = i;
    }

    public int get_offset_x3() {
        return this.mExportVar_offset_x3;
    }

    public FieldID getFieldID_offset_x3() {
        return createFieldID(13, null);
    }

    public synchronized void set_offset_y3(int i) {
        setVar(14, i);
        this.mExportVar_offset_y3 = i;
    }

    public int get_offset_y3() {
        return this.mExportVar_offset_y3;
    }

    public FieldID getFieldID_offset_y3() {
        return createFieldID(14, null);
    }

    public synchronized void set_offset_x4(int i) {
        setVar(15, i);
        this.mExportVar_offset_x4 = i;
    }

    public int get_offset_x4() {
        return this.mExportVar_offset_x4;
    }

    public FieldID getFieldID_offset_x4() {
        return createFieldID(15, null);
    }

    public synchronized void set_offset_y4(int i) {
        setVar(16, i);
        this.mExportVar_offset_y4 = i;
    }

    public int get_offset_y4() {
        return this.mExportVar_offset_y4;
    }

    public FieldID getFieldID_offset_y4() {
        return createFieldID(16, null);
    }

    public synchronized void set_offset_x5(int i) {
        setVar(17, i);
        this.mExportVar_offset_x5 = i;
    }

    public int get_offset_x5() {
        return this.mExportVar_offset_x5;
    }

    public FieldID getFieldID_offset_x5() {
        return createFieldID(17, null);
    }

    public synchronized void set_offset_y5(int i) {
        setVar(18, i);
        this.mExportVar_offset_y5 = i;
    }

    public int get_offset_y5() {
        return this.mExportVar_offset_y5;
    }

    public FieldID getFieldID_offset_y5() {
        return createFieldID(18, null);
    }

    public synchronized void set_offset_x6(int i) {
        setVar(19, i);
        this.mExportVar_offset_x6 = i;
    }

    public int get_offset_x6() {
        return this.mExportVar_offset_x6;
    }

    public FieldID getFieldID_offset_x6() {
        return createFieldID(19, null);
    }

    public synchronized void set_offset_y6(int i) {
        setVar(20, i);
        this.mExportVar_offset_y6 = i;
    }

    public int get_offset_y6() {
        return this.mExportVar_offset_y6;
    }

    public FieldID getFieldID_offset_y6() {
        return createFieldID(20, null);
    }

    public synchronized void set_parameter_A0(float f) {
        setVar(21, f);
        this.mExportVar_parameter_A0 = f;
    }

    public float get_parameter_A0() {
        return this.mExportVar_parameter_A0;
    }

    public FieldID getFieldID_parameter_A0() {
        return createFieldID(21, null);
    }

    public synchronized void set_parameter_B0(float f) {
        setVar(22, f);
        this.mExportVar_parameter_B0 = f;
    }

    public float get_parameter_B0() {
        return this.mExportVar_parameter_B0;
    }

    public FieldID getFieldID_parameter_B0() {
        return createFieldID(22, null);
    }

    public synchronized void set_parameter_A1(float f) {
        setVar(23, f);
        this.mExportVar_parameter_A1 = f;
    }

    public float get_parameter_A1() {
        return this.mExportVar_parameter_A1;
    }

    public FieldID getFieldID_parameter_A1() {
        return createFieldID(23, null);
    }

    public synchronized void set_parameter_B1(float f) {
        setVar(24, f);
        this.mExportVar_parameter_B1 = f;
    }

    public float get_parameter_B1() {
        return this.mExportVar_parameter_B1;
    }

    public FieldID getFieldID_parameter_B1() {
        return createFieldID(24, null);
    }

    public synchronized void set_parameter_A2(float f) {
        setVar(25, f);
        this.mExportVar_parameter_A2 = f;
    }

    public float get_parameter_A2() {
        return this.mExportVar_parameter_A2;
    }

    public FieldID getFieldID_parameter_A2() {
        return createFieldID(25, null);
    }

    public synchronized void set_parameter_B2(float f) {
        setVar(26, f);
        this.mExportVar_parameter_B2 = f;
    }

    public float get_parameter_B2() {
        return this.mExportVar_parameter_B2;
    }

    public FieldID getFieldID_parameter_B2() {
        return createFieldID(26, null);
    }

    public synchronized void set_parameter_A3(float f) {
        setVar(27, f);
        this.mExportVar_parameter_A3 = f;
    }

    public float get_parameter_A3() {
        return this.mExportVar_parameter_A3;
    }

    public FieldID getFieldID_parameter_A3() {
        return createFieldID(27, null);
    }

    public synchronized void set_parameter_B3(float f) {
        setVar(28, f);
        this.mExportVar_parameter_B3 = f;
    }

    public float get_parameter_B3() {
        return this.mExportVar_parameter_B3;
    }

    public FieldID getFieldID_parameter_B3() {
        return createFieldID(28, null);
    }

    public synchronized void set_parameter_A4(float f) {
        setVar(mExportVarIdx_parameter_A4, f);
        this.mExportVar_parameter_A4 = f;
    }

    public float get_parameter_A4() {
        return this.mExportVar_parameter_A4;
    }

    public FieldID getFieldID_parameter_A4() {
        return createFieldID(mExportVarIdx_parameter_A4, null);
    }

    public synchronized void set_parameter_B4(float f) {
        setVar(mExportVarIdx_parameter_B4, f);
        this.mExportVar_parameter_B4 = f;
    }

    public float get_parameter_B4() {
        return this.mExportVar_parameter_B4;
    }

    public FieldID getFieldID_parameter_B4() {
        return createFieldID(mExportVarIdx_parameter_B4, null);
    }

    public synchronized void set_parameter_A5(float f) {
        setVar(mExportVarIdx_parameter_A5, f);
        this.mExportVar_parameter_A5 = f;
    }

    public float get_parameter_A5() {
        return this.mExportVar_parameter_A5;
    }

    public FieldID getFieldID_parameter_A5() {
        return createFieldID(mExportVarIdx_parameter_A5, null);
    }

    public synchronized void set_parameter_B5(float f) {
        setVar(32, f);
        this.mExportVar_parameter_B5 = f;
    }

    public float get_parameter_B5() {
        return this.mExportVar_parameter_B5;
    }

    public FieldID getFieldID_parameter_B5() {
        return createFieldID(32, null);
    }

    public synchronized void set_parameter_A6(float f) {
        setVar(33, f);
        this.mExportVar_parameter_A6 = f;
    }

    public float get_parameter_A6() {
        return this.mExportVar_parameter_A6;
    }

    public FieldID getFieldID_parameter_A6() {
        return createFieldID(33, null);
    }

    public synchronized void set_parameter_B6(float f) {
        setVar(34, f);
        this.mExportVar_parameter_B6 = f;
    }

    public float get_parameter_B6() {
        return this.mExportVar_parameter_B6;
    }

    public FieldID getFieldID_parameter_B6() {
        return createFieldID(34, null);
    }

    public float get_weight_scale_c() {
        return this.mExportVar_weight_scale_c;
    }

    public FieldID getFieldID_weight_scale_c() {
        return createFieldID(35, null);
    }

    public int get_tonemap_algorithm_clamp_c() {
        return this.mExportVar_tonemap_algorithm_clamp_c;
    }

    public FieldID getFieldID_tonemap_algorithm_clamp_c() {
        return createFieldID(36, null);
    }

    public int get_tonemap_algorithm_exponential_c() {
        return this.mExportVar_tonemap_algorithm_exponential_c;
    }

    public FieldID getFieldID_tonemap_algorithm_exponential_c() {
        return createFieldID(37, null);
    }

    public int get_tonemap_algorithm_reinhard_c() {
        return this.mExportVar_tonemap_algorithm_reinhard_c;
    }

    public FieldID getFieldID_tonemap_algorithm_reinhard_c() {
        return createFieldID(38, null);
    }

    public int get_tonemap_algorithm_filmic_c() {
        return this.mExportVar_tonemap_algorithm_filmic_c;
    }

    public FieldID getFieldID_tonemap_algorithm_filmic_c() {
        return createFieldID(39, null);
    }

    public int get_tonemap_algorithm_aces_c() {
        return this.mExportVar_tonemap_algorithm_aces_c;
    }

    public FieldID getFieldID_tonemap_algorithm_aces_c() {
        return createFieldID(40, null);
    }

    public synchronized void set_tonemap_algorithm(int i) {
        setVar(41, i);
        this.mExportVar_tonemap_algorithm = i;
    }

    public int get_tonemap_algorithm() {
        return this.mExportVar_tonemap_algorithm;
    }

    public FieldID getFieldID_tonemap_algorithm() {
        return createFieldID(41, null);
    }

    public float get_exposure() {
        return this.mExportVar_exposure;
    }

    public FieldID getFieldID_exposure() {
        return createFieldID(42, null);
    }

    public synchronized void set_tonemap_scale(float f) {
        setVar(43, f);
        this.mExportVar_tonemap_scale = f;
    }

    public float get_tonemap_scale() {
        return this.mExportVar_tonemap_scale;
    }

    public FieldID getFieldID_tonemap_scale() {
        return createFieldID(43, null);
    }

    public float get_filmic_exposure_bias() {
        return this.mExportVar_filmic_exposure_bias;
    }

    public FieldID getFieldID_filmic_exposure_bias() {
        return createFieldID(44, null);
    }

    public synchronized void set_W(float f) {
        setVar(45, f);
        this.mExportVar_W = f;
    }

    public float get_W() {
        return this.mExportVar_W;
    }

    public FieldID getFieldID_W() {
        return createFieldID(45, null);
    }

    public synchronized void set_linear_scale(float f) {
        setVar(46, f);
        this.mExportVar_linear_scale = f;
    }

    public float get_linear_scale() {
        return this.mExportVar_linear_scale;
    }

    public FieldID getFieldID_linear_scale() {
        return createFieldID(46, null);
    }

    public synchronized void set_n_bitmaps_g(int i) {
        setVar(47, i);
        this.mExportVar_n_bitmaps_g = i;
    }

    public int get_n_bitmaps_g() {
        return this.mExportVar_n_bitmaps_g;
    }

    public FieldID getFieldID_n_bitmaps_g() {
        return createFieldID(47, null);
    }

    public KernelID getKernelID_hdr() {
        return createKernelID(1, 59, null, null);
    }

    public void forEach_hdr(Allocation allocation, Allocation allocation2) {
        forEach_hdr(allocation, allocation2, null);
    }

    public void forEach_hdr(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
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

    public KernelID getKernelID_hdr_n() {
        return createKernelID(2, 59, null, null);
    }

    public void forEach_hdr_n(Allocation allocation, Allocation allocation2) {
        forEach_hdr_n(allocation, allocation2, null);
    }

    public void forEach_hdr_n(Allocation allocation, Allocation allocation2, LaunchOptions launchOptions) {
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
}
