package net.sourceforge.opencamera;

public class HDRProcessorException extends Exception {
    public static final int INVALID_N_IMAGES = 0;
    public static final int UNEQUAL_SIZES = 1;
    private final int code;

    HDRProcessorException(int i) {
        this.code = i;
    }

    public int getCode() {
        return this.code;
    }
}
