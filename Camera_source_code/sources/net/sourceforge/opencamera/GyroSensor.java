package net.sourceforge.opencamera;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.ArrayList;
import java.util.List;

public class GyroSensor implements SensorEventListener {
    private static final float NS2S = 1.0E-9f;
    private static final String TAG = "GyroSensor";
    private final float[] accelVector = new float[3];
    private final float[] currentRotationMatrix = new float[9];
    private final float[] currentRotationMatrixGyroOnly = new float[9];
    private final float[] deltaRotationMatrix = new float[9];
    private final float[] deltaRotationVector = new float[4];
    private final float[] gyroVector = new float[3];
    private boolean hasTarget;
    private boolean has_gyroVector;
    private boolean has_init_accel = false;
    private boolean has_lastTargetAngle;
    private boolean has_original_rotation_matrix;
    private boolean has_rotationVector;
    private final float[] inVector = new float[3];
    private final float[] initAccelVector = new float[3];
    private boolean is_recording;
    private int is_upright;
    private float lastTargetAngle;
    private final Sensor mSensor;
    private final Sensor mSensorAccel;
    private final SensorManager mSensorManager;
    private final float[] originalRotationMatrix = new float[9];
    private final float[] rotationVector = new float[3];
    private boolean targetAchieved;
    private float targetAngle;
    private TargetCallback targetCallback;
    private final List<float[]> targetVectors = new ArrayList();
    private final float[] temp2Matrix = new float[9];
    private final float[] tempMatrix = new float[9];
    private final float[] tempVector = new float[3];
    private long timestamp;
    private float tooFarAngle;
    private float uprightAngleTol;

    public interface TargetCallback {
        void onAchieved(int i);

        void onTooFar();
    }

    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    GyroSensor(Context context) {
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        this.mSensor = this.mSensorManager.getDefaultSensor(4);
        this.mSensorAccel = this.mSensorManager.getDefaultSensor(1);
        setToIdentity();
    }

    /* access modifiers changed from: 0000 */
    public boolean hasSensors() {
        return (this.mSensor == null || this.mSensorAccel == null) ? false : true;
    }

    private void setToIdentity() {
        for (int i = 0; i < 9; i++) {
            this.currentRotationMatrix[i] = 0.0f;
        }
        float[] fArr = this.currentRotationMatrix;
        fArr[0] = 1.0f;
        fArr[4] = 1.0f;
        fArr[8] = 1.0f;
        System.arraycopy(fArr, 0, this.currentRotationMatrixGyroOnly, 0, 9);
        for (int i2 = 0; i2 < 3; i2++) {
            this.initAccelVector[i2] = 0.0f;
        }
        this.has_init_accel = false;
        this.has_original_rotation_matrix = false;
    }

    static void setVector(float[] fArr, float f, float f2, float f3) {
        fArr[0] = f;
        fArr[1] = f2;
        fArr[2] = f3;
    }

    private static float getMatrixComponent(float[] fArr, int i, int i2) {
        return fArr[(i * 3) + i2];
    }

    private static void setMatrixComponent(float[] fArr, int i, int i2, float f) {
        fArr[(i * 3) + i2] = f;
    }

    public static void transformVector(float[] fArr, float[] fArr2, float[] fArr3) {
        for (int i = 0; i < 3; i++) {
            fArr[i] = 0.0f;
            for (int i2 = 0; i2 < 3; i2++) {
                fArr[i] = fArr[i] + (getMatrixComponent(fArr2, i, i2) * fArr3[i2]);
            }
        }
    }

    private void transformTransposeVector(float[] fArr, float[] fArr2, float[] fArr3) {
        for (int i = 0; i < 3; i++) {
            fArr[i] = 0.0f;
            for (int i2 = 0; i2 < 3; i2++) {
                fArr[i] = fArr[i] + (getMatrixComponent(fArr2, i2, i) * fArr3[i2]);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void enableSensors() {
        this.has_rotationVector = false;
        this.has_gyroVector = false;
        for (int i = 0; i < 3; i++) {
            this.accelVector[i] = 0.0f;
            this.rotationVector[i] = 0.0f;
            this.gyroVector[i] = 0.0f;
        }
        Sensor sensor = this.mSensor;
        if (sensor != null) {
            this.mSensorManager.registerListener(this, sensor, 2);
        }
        Sensor sensor2 = this.mSensorAccel;
        if (sensor2 != null) {
            this.mSensorManager.registerListener(this, sensor2, 2);
        }
    }

    /* access modifiers changed from: 0000 */
    public void disableSensors() {
        this.mSensorManager.unregisterListener(this);
    }

    /* access modifiers changed from: 0000 */
    public void startRecording() {
        this.is_recording = true;
        this.timestamp = 0;
        setToIdentity();
    }

    /* access modifiers changed from: 0000 */
    public void stopRecording() {
        if (this.is_recording) {
            this.is_recording = false;
            this.timestamp = 0;
        }
    }

    public boolean isRecording() {
        return this.is_recording;
    }

    /* access modifiers changed from: 0000 */
    public void setTarget(float f, float f2, float f3, float f4, float f5, float f6, TargetCallback targetCallback2) {
        this.hasTarget = true;
        this.targetVectors.clear();
        addTarget(f, f2, f3);
        this.targetAngle = f4;
        this.uprightAngleTol = f5;
        this.tooFarAngle = f6;
        this.targetCallback = targetCallback2;
        this.has_lastTargetAngle = false;
        this.lastTargetAngle = 0.0f;
    }

    /* access modifiers changed from: 0000 */
    public void addTarget(float f, float f2, float f3) {
        this.targetVectors.add(new float[]{f, f2, f3});
    }

    /* access modifiers changed from: 0000 */
    public void clearTarget() {
        this.hasTarget = false;
        this.targetVectors.clear();
        this.targetCallback = null;
        this.has_lastTargetAngle = false;
        this.lastTargetAngle = 0.0f;
    }

    /* access modifiers changed from: 0000 */
    public void disableTargetCallback() {
        this.targetCallback = null;
    }

    /* access modifiers changed from: 0000 */
    public boolean hasTarget() {
        return this.hasTarget;
    }

    /* access modifiers changed from: 0000 */
    public boolean isTargetAchieved() {
        return this.hasTarget && this.targetAchieved;
    }

    public int isUpright() {
        return this.is_upright;
    }

    private void adjustGyroForAccel() {
        if (this.timestamp != 0 && this.has_init_accel) {
            transformVector(this.tempVector, this.currentRotationMatrix, this.accelVector);
            float[] fArr = this.tempVector;
            float f = fArr[0];
            float[] fArr2 = this.initAccelVector;
            double d = (double) ((f * fArr2[0]) + (fArr[1] * fArr2[1]) + (fArr[2] * fArr2[2]));
            if (d < 0.99999999995d) {
                double cos = Math.cos(Math.acos(d) * 0.019999999552965164d);
                float[] fArr3 = this.tempVector;
                float f2 = fArr3[1];
                float[] fArr4 = this.initAccelVector;
                double d2 = (double) ((f2 * fArr4[2]) - (fArr3[2] * fArr4[1]));
                double d3 = (double) ((fArr3[2] * fArr4[0]) - (fArr3[0] * fArr4[2]));
                double d4 = (double) ((fArr3[0] * fArr4[1]) - (fArr3[1] * fArr4[0]));
                Double.isNaN(d2);
                Double.isNaN(d2);
                double d5 = d2 * d2;
                Double.isNaN(d3);
                Double.isNaN(d3);
                double d6 = d5 + (d3 * d3);
                Double.isNaN(d4);
                Double.isNaN(d4);
                double sqrt = Math.sqrt(d6 + (d4 * d4));
                if (sqrt >= 1.0E-5d) {
                    Double.isNaN(d2);
                    double d7 = d2 / sqrt;
                    Double.isNaN(d3);
                    double d8 = d3 / sqrt;
                    Double.isNaN(d4);
                    double d9 = d4 / sqrt;
                    double sqrt2 = Math.sqrt(1.0d - (cos * cos));
                    double d10 = 1.0d - cos;
                    setMatrixComponent(this.tempMatrix, 0, 0, (float) ((d7 * d7 * d10) + cos));
                    double d11 = d7 * d8 * d10;
                    double d12 = sqrt2 * d9;
                    double d13 = cos;
                    setMatrixComponent(this.tempMatrix, 0, 1, (float) (d11 - d12));
                    double d14 = d7 * d9 * d10;
                    double d15 = sqrt2 * d8;
                    double d16 = d11;
                    setMatrixComponent(this.tempMatrix, 0, 2, (float) (d14 + d15));
                    setMatrixComponent(this.tempMatrix, 1, 0, (float) (d16 + d12));
                    setMatrixComponent(this.tempMatrix, 1, 1, (float) ((d8 * d8 * d10) + d13));
                    double d17 = d8 * d9 * d10;
                    double d18 = sqrt2 * d7;
                    setMatrixComponent(this.tempMatrix, 1, 2, (float) (d17 - d18));
                    setMatrixComponent(this.tempMatrix, 2, 0, (float) (d14 - d15));
                    setMatrixComponent(this.tempMatrix, 2, 1, (float) (d17 + d18));
                    setMatrixComponent(this.tempMatrix, 2, 2, (float) ((d9 * d9 * d10) + d13));
                    for (int i = 0; i < 3; i++) {
                        for (int i2 = 0; i2 < 3; i2++) {
                            float f3 = 0.0f;
                            for (int i3 = 0; i3 < 3; i3++) {
                                f3 += getMatrixComponent(this.tempMatrix, i, i3) * getMatrixComponent(this.currentRotationMatrix, i3, i2);
                            }
                            setMatrixComponent(this.temp2Matrix, i, i2, f3);
                        }
                    }
                    System.arraycopy(this.temp2Matrix, 0, this.currentRotationMatrix, 0, 9);
                }
            }
        }
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        SensorEvent sensorEvent2 = sensorEvent;
        if (sensorEvent2.sensor.getType() == 1) {
            for (int i = 0; i < 3; i++) {
                float[] fArr = this.accelVector;
                fArr[i] = (fArr[i] * 0.8f) + (sensorEvent2.values[i] * 0.19999999f);
            }
            float[] fArr2 = this.accelVector;
            double sqrt = Math.sqrt((double) ((fArr2[0] * fArr2[0]) + (fArr2[1] * fArr2[1]) + (fArr2[2] * fArr2[2])));
            if (sqrt > 1.0E-8d) {
                float[] fArr3 = this.accelVector;
                double d = (double) fArr3[0];
                Double.isNaN(d);
                fArr3[0] = (float) (d / sqrt);
                double d2 = (double) fArr3[1];
                Double.isNaN(d2);
                fArr3[1] = (float) (d2 / sqrt);
                double d3 = (double) fArr3[2];
                Double.isNaN(d3);
                fArr3[2] = (float) (d3 / sqrt);
            }
            if (!this.has_init_accel) {
                System.arraycopy(this.accelVector, 0, this.initAccelVector, 0, 3);
                this.has_init_accel = true;
            }
            adjustGyroForAccel();
        } else if (sensorEvent2.sensor.getType() == 4) {
            if (this.has_gyroVector) {
                for (int i2 = 0; i2 < 3; i2++) {
                    float[] fArr4 = this.gyroVector;
                    fArr4[i2] = (fArr4[i2] * 0.5f) + (sensorEvent2.values[i2] * 0.5f);
                }
            } else {
                System.arraycopy(sensorEvent2.values, 0, this.gyroVector, 0, 3);
                this.has_gyroVector = true;
            }
            if (this.timestamp != 0) {
                float f = ((float) (sensorEvent2.timestamp - this.timestamp)) * NS2S;
                float[] fArr5 = this.gyroVector;
                float f2 = fArr5[0];
                float f3 = fArr5[1];
                float f4 = fArr5[2];
                double sqrt2 = Math.sqrt((double) ((f2 * f2) + (f3 * f3) + (f4 * f4)));
                if (sqrt2 > 1.0E-5d) {
                    double d4 = (double) f2;
                    Double.isNaN(d4);
                    f2 = (float) (d4 / sqrt2);
                    double d5 = (double) f3;
                    Double.isNaN(d5);
                    f3 = (float) (d5 / sqrt2);
                    double d6 = (double) f4;
                    Double.isNaN(d6);
                    f4 = (float) (d6 / sqrt2);
                }
                double d7 = (double) f;
                Double.isNaN(d7);
                double d8 = (sqrt2 * d7) / 2.0d;
                float sin = (float) Math.sin(d8);
                float cos = (float) Math.cos(d8);
                float[] fArr6 = this.deltaRotationVector;
                fArr6[0] = f2 * sin;
                fArr6[1] = f3 * sin;
                fArr6[2] = sin * f4;
                fArr6[3] = cos;
                SensorManager.getRotationMatrixFromVector(this.deltaRotationMatrix, fArr6);
                for (int i3 = 0; i3 < 3; i3++) {
                    for (int i4 = 0; i4 < 3; i4++) {
                        float f5 = 0.0f;
                        for (int i5 = 0; i5 < 3; i5++) {
                            f5 += getMatrixComponent(this.currentRotationMatrix, i3, i5) * getMatrixComponent(this.deltaRotationMatrix, i5, i4);
                        }
                        setMatrixComponent(this.tempMatrix, i3, i4, f5);
                    }
                }
                System.arraycopy(this.tempMatrix, 0, this.currentRotationMatrix, 0, 9);
                for (int i6 = 0; i6 < 3; i6++) {
                    for (int i7 = 0; i7 < 3; i7++) {
                        float f6 = 0.0f;
                        for (int i8 = 0; i8 < 3; i8++) {
                            f6 += getMatrixComponent(this.currentRotationMatrixGyroOnly, i6, i8) * getMatrixComponent(this.deltaRotationMatrix, i8, i7);
                        }
                        setMatrixComponent(this.tempMatrix, i6, i7, f6);
                    }
                }
                System.arraycopy(this.tempMatrix, 0, this.currentRotationMatrixGyroOnly, 0, 9);
                adjustGyroForAccel();
            }
            this.timestamp = sensorEvent2.timestamp;
        } else if (sensorEvent2.sensor.getType() == 11 || sensorEvent2.sensor.getType() == 15) {
            if (this.has_rotationVector) {
                for (int i9 = 0; i9 < 3; i9++) {
                    float[] fArr7 = this.rotationVector;
                    fArr7[i9] = (fArr7[i9] * 0.8f) + (sensorEvent2.values[i9] * 0.19999999f);
                }
            } else {
                System.arraycopy(sensorEvent2.values, 0, this.rotationVector, 0, 3);
                this.has_rotationVector = true;
            }
            SensorManager.getRotationMatrixFromVector(this.tempMatrix, this.rotationVector);
            if (!this.has_original_rotation_matrix) {
                System.arraycopy(this.tempMatrix, 0, this.originalRotationMatrix, 0, 9);
                this.has_original_rotation_matrix = true;
                if (((double) sensorEvent2.values[3]) == 1.0d) {
                    this.has_original_rotation_matrix = false;
                }
            }
            for (int i10 = 0; i10 < 3; i10++) {
                for (int i11 = 0; i11 < 3; i11++) {
                    float f7 = 0.0f;
                    for (int i12 = 0; i12 < 3; i12++) {
                        f7 += getMatrixComponent(this.originalRotationMatrix, i12, i10) * getMatrixComponent(this.tempMatrix, i12, i11);
                    }
                    setMatrixComponent(this.currentRotationMatrix, i10, i11, f7);
                }
            }
        }
        if (this.hasTarget) {
            this.targetAchieved = false;
            int i13 = 0;
            for (int i14 = 0; i14 < this.targetVectors.size(); i14++) {
                float[] fArr8 = (float[]) this.targetVectors.get(i14);
                setVector(this.inVector, 0.0f, 1.0f, 0.0f);
                transformVector(this.tempVector, this.currentRotationMatrix, this.inVector);
                this.is_upright = 0;
                float[] fArr9 = this.tempVector;
                float f8 = fArr9[0];
                float f9 = fArr9[1];
                float f10 = fArr9[2];
                float f11 = (fArr8[0] * f8) + (fArr8[1] * f9) + (fArr8[2] * f10);
                float f12 = f8 - (fArr8[0] * f11);
                float f13 = f9 - (fArr8[1] * f11);
                float f14 = f10 - (f11 * fArr8[2]);
                double sqrt3 = Math.sqrt((double) ((f12 * f12) + (f13 * f13) + (f14 * f14)));
                if (sqrt3 > 1.0E-5d) {
                    double d9 = (double) f12;
                    Double.isNaN(d9);
                    float f15 = (float) (d9 / sqrt3);
                    double d10 = (double) f14;
                    Double.isNaN(d10);
                    float f16 = -((float) (d10 / sqrt3));
                    float asin = (float) Math.asin((double) ((float) Math.sqrt((double) ((f16 * f16) + 0.0f + (f15 * f15)))));
                    setVector(this.inVector, 0.0f, 0.0f, -1.0f);
                    transformVector(this.tempVector, this.currentRotationMatrix, this.inVector);
                    if (Math.abs(asin) > this.uprightAngleTol) {
                        float[] fArr10 = this.tempVector;
                        this.is_upright = ((f16 * fArr10[0]) + (fArr10[1] * 0.0f)) + (f15 * fArr10[2]) < 0.0f ? 1 : -1;
                    }
                }
                float[] fArr11 = this.tempVector;
                float acos = (float) Math.acos((double) ((fArr11[0] * fArr8[0]) + (fArr11[1] * fArr8[1]) + (fArr11[2] * fArr8[2])));
                if (this.is_upright == 0 && acos <= this.targetAngle) {
                    this.targetAchieved = true;
                    TargetCallback targetCallback2 = this.targetCallback;
                    if (targetCallback2 != null && this.has_lastTargetAngle && acos > this.lastTargetAngle) {
                        targetCallback2.onAchieved(i14);
                    }
                    this.has_lastTargetAngle = true;
                    this.lastTargetAngle = acos;
                }
                if (acos > this.tooFarAngle) {
                    i13++;
                }
            }
            if (i13 > 0 && i13 == this.targetVectors.size()) {
                TargetCallback targetCallback3 = this.targetCallback;
                if (targetCallback3 != null) {
                    targetCallback3.onTooFar();
                }
            }
        }
    }

    public void getRelativeInverseVector(float[] fArr, float[] fArr2) {
        transformTransposeVector(fArr, this.currentRotationMatrix, fArr2);
    }

    public void getRelativeInverseVectorGyroOnly(float[] fArr, float[] fArr2) {
        transformTransposeVector(fArr, this.currentRotationMatrixGyroOnly, fArr2);
    }

    public void getRotationMatrix(float[] fArr) {
        System.arraycopy(this.currentRotationMatrix, 0, fArr, 0, 9);
    }

    public void testForceTargetAchieved(int i) {
        TargetCallback targetCallback2 = this.targetCallback;
        if (targetCallback2 != null) {
            targetCallback2.onAchieved(i);
        }
    }
}
