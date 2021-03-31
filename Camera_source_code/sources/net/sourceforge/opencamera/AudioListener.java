package net.sourceforge.opencamera;

import android.media.AudioRecord;
import android.util.Log;

class AudioListener {
    private static final String TAG = "AudioListener";
    /* access modifiers changed from: private */

    /* renamed from: ar */
    public AudioRecord f10ar;
    /* access modifiers changed from: private */
    public int buffer_size = -1;
    /* access modifiers changed from: private */
    public volatile boolean is_running = true;
    private Thread thread;

    public interface AudioListenerCallback {
        void onAudio(int i);
    }

    AudioListener(final AudioListenerCallback audioListenerCallback) {
        try {
            this.buffer_size = AudioRecord.getMinBufferSize(8000, 16, 2);
            if (this.buffer_size > 0) {
                synchronized (this) {
                    AudioRecord audioRecord = new AudioRecord(1, 8000, 16, 2, this.buffer_size);
                    this.f10ar = audioRecord;
                    notifyAll();
                }
                synchronized (this) {
                    if (this.f10ar.getState() == 1) {
                        final short[] sArr = new short[this.buffer_size];
                        this.f10ar.startRecording();
                        this.thread = new Thread() {
                            public void run() {
                                while (AudioListener.this.is_running) {
                                    try {
                                        int read = AudioListener.this.f10ar.read(sArr, 0, AudioListener.this.buffer_size);
                                        if (read > 0) {
                                            int i = 0;
                                            int i2 = 0;
                                            for (int i3 = 0; i3 < read; i3++) {
                                                int abs = Math.abs(sArr[i3]);
                                                i += abs;
                                                i2 = Math.max(i2, abs);
                                            }
                                            audioListenerCallback.onAudio(i / read);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                synchronized (AudioListener.this) {
                                    AudioListener.this.f10ar.release();
                                    AudioListener.this.f10ar = null;
                                    AudioListener.this.notifyAll();
                                }
                            }
                        };
                        return;
                    }
                    Log.e(TAG, "audiorecord failed to initialise");
                    this.f10ar.release();
                    this.f10ar = null;
                    notifyAll();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "failed to create audiorecord");
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean status() {
        boolean z;
        synchronized (this) {
            z = this.f10ar != null;
        }
        return z;
    }

    /* access modifiers changed from: 0000 */
    public void start() {
        Thread thread2 = this.thread;
        if (thread2 != null) {
            thread2.start();
        }
    }

    /* access modifiers changed from: 0000 */
    public void release(boolean z) {
        this.is_running = false;
        this.thread = null;
        if (z) {
            synchronized (this) {
                while (this.f10ar != null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
