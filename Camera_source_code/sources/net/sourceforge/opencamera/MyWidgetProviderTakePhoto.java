package net.sourceforge.opencamera;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class MyWidgetProviderTakePhoto extends AppWidgetProvider {
    private static final String TAG = "MyWidgetProviderTakePho";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] iArr) {
        for (int i : iArr) {
            PendingIntent activity = PendingIntent.getActivity(context, 0, new Intent(context, TakePhoto.class), 0);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), C0316R.layout.widget_layout_take_photo);
            remoteViews.setOnClickPendingIntent(C0316R.C0318id.widget_take_photo, activity);
            appWidgetManager.updateAppWidget(i, remoteViews);
        }
    }
}
