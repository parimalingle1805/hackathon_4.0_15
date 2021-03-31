package net.sourceforge.opencamera.cameracontroller;

import android.hardware.camera2.DngCreator;
import android.media.Image;
import java.io.IOException;
import java.io.OutputStream;

public class RawImage {
    private static final String TAG = "RawImage";
    private final DngCreator dngCreator;
    private final Image image;

    public RawImage(DngCreator dngCreator2, Image image2) {
        this.dngCreator = dngCreator2;
        this.image = image2;
    }

    public void writeImage(OutputStream outputStream) throws IOException {
        try {
            this.dngCreator.writeImage(outputStream, this.image);
        } catch (AssertionError e) {
            e.printStackTrace();
            throw new IOException();
        } catch (IllegalStateException e2) {
            e2.printStackTrace();
            throw new IOException();
        }
    }

    public void close() {
        this.image.close();
        this.dngCreator.close();
    }
}
