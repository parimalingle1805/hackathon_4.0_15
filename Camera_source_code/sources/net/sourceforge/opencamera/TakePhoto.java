package net.sourceforge.opencamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TakePhoto extends Activity {
    private static final String TAG = "TakePhoto";
    public static final String TAKE_PHOTO = "net.sourceforge.opencamera.TAKE_PHOTO";

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(335544320);
        intent.putExtra(TAKE_PHOTO, true);
        startActivity(intent);
        finish();
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
    }
}
