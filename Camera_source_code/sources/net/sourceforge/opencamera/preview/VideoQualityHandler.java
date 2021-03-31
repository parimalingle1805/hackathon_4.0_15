package net.sourceforge.opencamera.preview;

import android.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.sourceforge.opencamera.BuildConfig;
import net.sourceforge.opencamera.cameracontroller.CameraController.CameraFeatures;
import net.sourceforge.opencamera.cameracontroller.CameraController.Size;

public class VideoQualityHandler {
    private static final String TAG = "VideoQualityHandler";
    private int current_video_quality = -1;
    private List<String> video_quality;
    private List<Size> video_sizes;
    private List<Size> video_sizes_high_speed;

    public static class Dimension2D {
        final int height;
        final int width;

        public Dimension2D(int i, int i2) {
            this.width = i;
            this.height = i2;
        }
    }

    private static class SortVideoSizesComparator implements Comparator<Size>, Serializable {
        private static final long serialVersionUID = 5802214721033718212L;

        private SortVideoSizesComparator() {
        }

        public int compare(Size size, Size size2) {
            return (size2.width * size2.height) - (size.width * size.height);
        }
    }

    /* access modifiers changed from: 0000 */
    public void resetCurrentQuality() {
        this.video_quality = null;
        this.current_video_quality = -1;
    }

    public void initialiseVideoQualityFromProfiles(List<Integer> list, List<Dimension2D> list2) {
        boolean[] zArr;
        this.video_quality = new ArrayList();
        List<Size> list3 = this.video_sizes;
        if (list3 != null) {
            zArr = new boolean[list3.size()];
            for (int i = 0; i < this.video_sizes.size(); i++) {
                zArr[i] = false;
            }
        } else {
            zArr = null;
        }
        if (list.size() == list2.size()) {
            for (int i2 = 0; i2 < list.size(); i2++) {
                Dimension2D dimension2D = (Dimension2D) list2.get(i2);
                addVideoResolutions(zArr, ((Integer) list.get(i2)).intValue(), dimension2D.width, dimension2D.height);
            }
            return;
        }
        Log.e(TAG, "profiles and dimensions have unequal sizes");
        throw new RuntimeException();
    }

    public void sortVideoSizes() {
        Collections.sort(this.video_sizes, new SortVideoSizesComparator());
    }

    private void addVideoResolutions(boolean[] zArr, int i, int i2, int i3) {
        if (this.video_sizes != null) {
            for (int i4 = 0; i4 < this.video_sizes.size(); i4++) {
                if (!zArr[i4]) {
                    Size size = (Size) this.video_sizes.get(i4);
                    int i5 = size.width;
                    String str = BuildConfig.FLAVOR;
                    if (i5 == i2 && size.height == i3) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(str);
                        sb.append(i);
                        this.video_quality.add(sb.toString());
                        zArr[i4] = true;
                    } else if (i == 0 || size.width * size.height >= i2 * i3) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(str);
                        sb2.append(i);
                        sb2.append("_r");
                        sb2.append(size.width);
                        sb2.append("x");
                        sb2.append(size.height);
                        this.video_quality.add(sb2.toString());
                        zArr[i4] = true;
                    }
                }
            }
        }
    }

    public List<String> getSupportedVideoQuality() {
        return this.video_quality;
    }

    /* access modifiers changed from: 0000 */
    public int getCurrentVideoQualityIndex() {
        return this.current_video_quality;
    }

    /* access modifiers changed from: 0000 */
    public void setCurrentVideoQualityIndex(int i) {
        this.current_video_quality = i;
    }

    public String getCurrentVideoQuality() {
        int i = this.current_video_quality;
        if (i == -1) {
            return null;
        }
        return (String) this.video_quality.get(i);
    }

    public List<Size> getSupportedVideoSizes() {
        return this.video_sizes;
    }

    public List<Size> getSupportedVideoSizesHighSpeed() {
        return this.video_sizes_high_speed;
    }

    public boolean videoSupportsFrameRate(int i) {
        return CameraFeatures.supportsFrameRate(this.video_sizes, i);
    }

    public boolean videoSupportsFrameRateHighSpeed(int i) {
        return CameraFeatures.supportsFrameRate(this.video_sizes_high_speed, i);
    }

    /* access modifiers changed from: 0000 */
    public Size findVideoSizeForFrameRate(int i, int i2, double d) {
        Size size = new Size(i, i2);
        Size findSize = CameraFeatures.findSize(getSupportedVideoSizes(), size, d, false);
        return (findSize != null || getSupportedVideoSizesHighSpeed() == null) ? findSize : CameraFeatures.findSize(getSupportedVideoSizesHighSpeed(), size, d, false);
    }

    private static Size getMaxVideoSize(List<Size> list) {
        int i = -1;
        int i2 = -1;
        for (Size size : list) {
            if (i == -1 || size.width * size.height > i * i2) {
                i = size.width;
                i2 = size.height;
            }
        }
        return new Size(i, i2);
    }

    /* access modifiers changed from: 0000 */
    public Size getMaxSupportedVideoSize() {
        return getMaxVideoSize(this.video_sizes);
    }

    /* access modifiers changed from: 0000 */
    public Size getMaxSupportedVideoSizeHighSpeed() {
        return getMaxVideoSize(this.video_sizes_high_speed);
    }

    public void setVideoSizes(List<Size> list) {
        this.video_sizes = list;
        sortVideoSizes();
    }

    public void setVideoSizesHighSpeed(List<Size> list) {
        this.video_sizes_high_speed = list;
    }
}
