package android.support.p000v4.media;

import android.content.Context;
import android.os.Build.VERSION;
import android.util.Log;

/* renamed from: android.support.v4.media.MediaSessionManager */
public final class MediaSessionManager {
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    static final String TAG = "MediaSessionManager";
    private static final Object sLock = new Object();
    private static volatile MediaSessionManager sSessionManager;
    MediaSessionManagerImpl mImpl;

    /* renamed from: android.support.v4.media.MediaSessionManager$MediaSessionManagerImpl */
    interface MediaSessionManagerImpl {
        Context getContext();

        boolean isTrustedForMediaControl(RemoteUserInfoImpl remoteUserInfoImpl);
    }

    /* renamed from: android.support.v4.media.MediaSessionManager$RemoteUserInfo */
    public static final class RemoteUserInfo {
        public static final String LEGACY_CONTROLLER = "android.media.session.MediaController";
        RemoteUserInfoImpl mImpl;

        public RemoteUserInfo(String str, int i, int i2) {
            if (VERSION.SDK_INT >= 28) {
                this.mImpl = new RemoteUserInfoImplApi28(str, i, i2);
            } else {
                this.mImpl = new RemoteUserInfoImplBase(str, i, i2);
            }
        }

        public RemoteUserInfo(android.media.session.MediaSessionManager.RemoteUserInfo remoteUserInfo) {
            this.mImpl = new RemoteUserInfoImplApi28(remoteUserInfo);
        }

        public String getPackageName() {
            return this.mImpl.getPackageName();
        }

        public int getPid() {
            return this.mImpl.getPid();
        }

        public int getUid() {
            return this.mImpl.getUid();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof RemoteUserInfo)) {
                return false;
            }
            return this.mImpl.equals(((RemoteUserInfo) obj).mImpl);
        }

        public int hashCode() {
            return this.mImpl.hashCode();
        }
    }

    /* renamed from: android.support.v4.media.MediaSessionManager$RemoteUserInfoImpl */
    interface RemoteUserInfoImpl {
        String getPackageName();

        int getPid();

        int getUid();
    }

    public static MediaSessionManager getSessionManager(Context context) {
        MediaSessionManager mediaSessionManager = sSessionManager;
        if (mediaSessionManager == null) {
            synchronized (sLock) {
                mediaSessionManager = sSessionManager;
                if (mediaSessionManager == null) {
                    sSessionManager = new MediaSessionManager(context.getApplicationContext());
                    mediaSessionManager = sSessionManager;
                }
            }
        }
        return mediaSessionManager;
    }

    private MediaSessionManager(Context context) {
        if (VERSION.SDK_INT >= 28) {
            this.mImpl = new MediaSessionManagerImplApi28(context);
        } else if (VERSION.SDK_INT >= 21) {
            this.mImpl = new MediaSessionManagerImplApi21(context);
        } else {
            this.mImpl = new MediaSessionManagerImplBase(context);
        }
    }

    public boolean isTrustedForMediaControl(RemoteUserInfo remoteUserInfo) {
        if (remoteUserInfo != null) {
            return this.mImpl.isTrustedForMediaControl(remoteUserInfo.mImpl);
        }
        throw new IllegalArgumentException("userInfo should not be null");
    }

    /* access modifiers changed from: 0000 */
    public Context getContext() {
        return this.mImpl.getContext();
    }
}
