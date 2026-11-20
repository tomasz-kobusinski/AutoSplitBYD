package com.byd.autosplit;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String PKG_MAPS = "com.google.android.apps.maps";
    private static final String PKG_OBD = "OCTech.Mobile.Applications.OBDLink";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchSplitScreen();
    }

    private void launchSplitScreen() {
        PackageManager pm = getPackageManager();
        Intent mapsIntent = pm.getLaunchIntentForPackage(PKG_MAPS);
        Intent obdIntent = pm.getLaunchIntentForPackage(PKG_OBD);

        if (mapsIntent == null || obdIntent == null) {
            Toast.makeText(this, "Brak Google Maps lub OBDLink!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 1. Uruchom Google Maps jako główne okno
        mapsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mapsIntent);

        // 2. Po 1.2 sekundy odpal OBDLink obok (FLAG_ACTIVITY_LAUNCH_ADJACENT)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            obdIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            );
            try {
                startActivity(obdIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Błąd split-screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finish();
        }, 1200);
    }
}
