package android.support.p000v4.media;

import android.content.Context;
import android.media.browse.MediaBrowser.MediaItem;
import android.os.Parcel;
import android.service.media.MediaBrowserService.Result;

/* renamed from: android.support.v4.media.MediaBrowserServiceCompatApi23 */
class MediaBrowserServiceCompatApi23 {

    /* renamed from: android.support.v4.media.MediaBrowserServiceCompatApi23$MediaBrowserServiceAdaptor */
    static class MediaBrowserServiceAdaptor extends MediaBrowserServiceAdaptor {
        MediaBrowserServiceAdaptor(Context context, ServiceCompatProxy serviceCompatProxy) {
            super(context, serviceCompatProxy);
        }

        public void onLoadItem(String str, Result<MediaItem> result) {
            ((ServiceCompatProxy) this.mServiceProxy).onLoadItem(str, new ResultWrapper(result));
        }
    }

    /* renamed from: android.support.v4.media.MediaBrowserServiceCompatApi23$ServiceCompatProxy */
    public interface ServiceCompatProxy extends android.support.p000v4.media.MediaBrowserServiceCompatApi21.ServiceCompatProxy {
        void onLoadItem(String str, ResultWrapper<Parcel> resultWrapper);
    }

    public static Object createService(Context context, ServiceCompatProxy serviceCompatProxy) {
        return new MediaBrowserServiceAdaptor(context, serviceCompatProxy);
    }

    private MediaBrowserServiceCompatApi23() {
    }
}
