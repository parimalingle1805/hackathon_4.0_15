package net.sourceforge.opencamera;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.media.SoundPool.Builder;
import android.os.Build.VERSION;
import android.util.SparseIntArray;

class SoundPoolManager {
    private static final String TAG = "SoundPoolManager";
    private final Context context;
    private SparseIntArray sound_ids;
    private SoundPool sound_pool;

    SoundPoolManager(Context context2) {
        this.context = context2;
    }

    /* access modifiers changed from: 0000 */
    public void initSound() {
        if (this.sound_pool == null) {
            if (VERSION.SDK_INT >= 21) {
                this.sound_pool = new Builder().setMaxStreams(1).setAudioAttributes(new AudioAttributes.Builder().setLegacyStreamType(1).setContentType(4).build()).build();
            } else {
                this.sound_pool = new SoundPool(1, 1, 0);
            }
            this.sound_ids = new SparseIntArray();
        }
    }

    /* access modifiers changed from: 0000 */
    public void releaseSound() {
        SoundPool soundPool = this.sound_pool;
        if (soundPool != null) {
            soundPool.release();
            this.sound_pool = null;
            this.sound_ids = null;
        }
    }

    /* access modifiers changed from: 0000 */
    public void loadSound(int i) {
        SoundPool soundPool = this.sound_pool;
        if (soundPool != null) {
            this.sound_ids.put(i, soundPool.load(this.context, i, 1));
        }
    }

    /* access modifiers changed from: 0000 */
    public void playSound(int i) {
        if (this.sound_pool != null && this.sound_ids.indexOfKey(i) >= 0) {
            this.sound_pool.play(this.sound_ids.get(i), 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }
}
