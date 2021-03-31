package net.sourceforge.opencamera.p004ui;

import android.widget.SeekBar;
import java.util.ArrayList;
import java.util.List;

/* renamed from: net.sourceforge.opencamera.ui.ManualSeekbars */
public class ManualSeekbars {
    private static final String TAG = "ManualSeekbars";
    private static final int manual_n = 1000;
    private List<Long> seekbar_values_iso;
    private List<Long> seekbar_values_shutter_speed;
    private List<Long> seekbar_values_white_balance;

    public static double seekbarScaling(double d) {
        return (Math.pow(100.0d, d) - 1.0d) / 99.0d;
    }

    private static double seekbarScalingInverse(double d) {
        return Math.log((d * 99.0d) + 1.0d) / Math.log(100.0d);
    }

    public static void setProgressSeekbarScaled(SeekBar seekBar, double d, double d2, double d3) {
        int i = 1000;
        seekBar.setMax(1000);
        int seekbarScalingInverse = (int) ((seekbarScalingInverse((d3 - d) / (d2 - d)) * 1000.0d) + 0.5d);
        if (seekbarScalingInverse < 0) {
            i = 0;
        } else if (seekbarScalingInverse <= 1000) {
            i = seekbarScalingInverse;
        }
        seekBar.setProgress(i);
    }

    public int getWhiteBalanceTemperature(int i) {
        return ((Long) this.seekbar_values_white_balance.get(i)).intValue();
    }

    public int getISO(int i) {
        return ((Long) this.seekbar_values_iso.get(i)).intValue();
    }

    public long getExposureTime(int i) {
        return ((Long) this.seekbar_values_shutter_speed.get(i)).longValue();
    }

    private void setProgressBarToClosest(SeekBar seekBar, List<Long> list, long j) {
        long j2 = 0;
        int i = -1;
        for (int i2 = 0; i2 < list.size(); i2++) {
            long abs = Math.abs(((Long) list.get(i2)).longValue() - j);
            if (i == -1 || abs < j2) {
                i = i2;
                j2 = abs;
            }
        }
        if (i != -1) {
            seekBar.setProgress(i);
        }
    }

    /* access modifiers changed from: 0000 */
    public void setISOProgressBarToClosest(SeekBar seekBar, long j) {
        setProgressBarToClosest(seekBar, this.seekbar_values_iso, j);
    }

    public void setProgressSeekbarWhiteBalance(SeekBar seekBar, long j, long j2, long j3) {
        this.seekbar_values_white_balance = new ArrayList();
        List<Long> list = this.seekbar_values_white_balance;
        while (j < j2) {
            list.add(Long.valueOf(j));
            j += 100;
        }
        list.add(Long.valueOf(j2));
        seekBar.setMax(list.size() - 1);
        setProgressBarToClosest(seekBar, list, j3);
    }

    public void setProgressSeekbarISO(SeekBar seekBar, long j, long j2, long j3) {
        long j4;
        long j5;
        long j6;
        this.seekbar_values_iso = new ArrayList();
        List<Long> list = this.seekbar_values_iso;
        list.add(Long.valueOf(j));
        for (long j7 = 1; j7 < 100; j7++) {
            if (j7 > j && j7 < j2) {
                list.add(Long.valueOf(j7));
            }
        }
        long j8 = 100;
        while (true) {
            j4 = 500;
            if (j8 >= 500) {
                break;
            }
            if (j8 > j && j8 < j2) {
                list.add(Long.valueOf(j8));
            }
            j8 += 5;
        }
        while (true) {
            j5 = 1000;
            if (j4 >= 1000) {
                break;
            }
            if (j4 > j && j4 < j2) {
                list.add(Long.valueOf(j4));
            }
            j4 += 10;
        }
        while (true) {
            if (j5 >= 5000) {
                break;
            }
            if (j5 > j && j5 < j2) {
                list.add(Long.valueOf(j5));
            }
            j5 += 50;
        }
        for (j6 = 5000; j6 < 10000; j6 += 100) {
            if (j6 > j && j6 < j2) {
                list.add(Long.valueOf(j6));
            }
        }
        list.add(Long.valueOf(j2));
        seekBar.setMax(list.size() - 1);
        setProgressBarToClosest(seekBar, list, j3);
    }

    public void setProgressSeekbarShutterSpeed(SeekBar seekBar, long j, long j2, long j3) {
        int i;
        SeekBar seekBar2 = seekBar;
        this.seekbar_values_shutter_speed = new ArrayList();
        List<Long> list = this.seekbar_values_shutter_speed;
        list.add(Long.valueOf(j));
        for (int i2 = 10; i2 >= 1; i2--) {
            long j4 = 1000000000 / ((long) (i2 * 1000));
            if (j4 > j && j4 < j2) {
                list.add(Long.valueOf(j4));
            }
        }
        for (int i3 = 9; i3 >= 1; i3--) {
            long j5 = 1000000000 / ((long) (i3 * 100));
            if (j5 > j && j5 < j2) {
                list.add(Long.valueOf(j5));
            }
        }
        for (int i4 = 9; i4 >= 6; i4--) {
            long j6 = 1000000000 / ((long) (i4 * 10));
            if (j6 > j && j6 < j2) {
                list.add(Long.valueOf(j6));
            }
        }
        for (int i5 = 50; i5 >= 10; i5 -= 5) {
            long j7 = 1000000000 / ((long) i5);
            if (j7 > j && j7 < j2) {
                list.add(Long.valueOf(j7));
            }
        }
        int i6 = 1;
        while (true) {
            if (i6 >= 20) {
                break;
            }
            long j8 = ((long) i6) * 100000000;
            if (j8 > j && j8 < j2) {
                list.add(Long.valueOf(j8));
            }
            i6++;
        }
        for (int i7 = 2; i7 < 20; i7++) {
            long j9 = ((long) i7) * 1000000000;
            if (j9 > j && j9 < j2) {
                list.add(Long.valueOf(j9));
            }
        }
        for (i = 20; i <= 60; i += 5) {
            long j10 = ((long) i) * 1000000000;
            if (j10 > j && j10 < j2) {
                list.add(Long.valueOf(j10));
            }
        }
        list.add(Long.valueOf(j2));
        seekBar.setMax(list.size() - 1);
        setProgressBarToClosest(seekBar, list, j3);
    }
}
