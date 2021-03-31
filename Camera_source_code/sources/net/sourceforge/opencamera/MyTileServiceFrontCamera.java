package net.sourceforge.opencamera;

import android.content.Intent;
import android.service.quicksettings.TileService;

public class MyTileServiceFrontCamera extends TileService {
    private static final String TAG = "MyTileServiceFrontCam";
    public static final String TILE_ID = "net.sourceforge.opencamera.TILE_FRONT_CAMERA";

    public void onDestroy() {
        super.onDestroy();
    }

    public void onTileAdded() {
        super.onTileAdded();
    }

    public void onTileRemoved() {
        super.onTileRemoved();
    }

    public void onStartListening() {
        super.onStartListening();
    }

    public void onStopListening() {
        super.onStopListening();
    }

    public void onClick() {
        super.onClick();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(335544320);
        intent.setAction(TILE_ID);
        startActivity(intent);
    }
}
