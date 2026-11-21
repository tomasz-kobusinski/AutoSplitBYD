package com.byd.autosplit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String PKG_MAPS = "com.google.android.apps.maps";
    private static final String PKG_OBD = "OCTech.Mobile.Applications.OBDLink";
    private static final String PREFS_NAME = "autosplit_prefs";
    private static final String KEY_ALREADY_LAUNCHED = "already_launched";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean alreadyLaunched = prefs.getBoolean(KEY_ALREADY_LAUNCHED, false);

        if (!alreadyLaunched) {
            prefs.edit().putBoolean(KEY_ALREADY_LAUNCHED, true).apply();
            launchSplitScreen(true);
        } else {
            launchSplitScreen(false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        launchSplitScreen(false);
    }

    private void launchSplitScreen(boolean coldStart) {
        PackageManager pm = getPackageManager();
        Intent mapsIntent = pm.getLaunchIntentForPackage(PKG_MAPS);
        Intent obdIntent = pm.getLaunchIntentForPackage(PKG_OBD);

        if (mapsIntent == null || obdIntent == null) {
            Toast.makeText(this, "Brak Google Maps lub OBDLink!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (coldStart) {
            mapsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mapsIntent);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                obdIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
                );
                try {
                    startActivity(obdIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "Błąd split-screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                finish();
            }, 1200);
        } else {
            // Przy powrocie (np. ze Spotify) przywracamy okna bez tworzenia nowego zadania OBDLink
            mapsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            obdIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(mapsIntent);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    startActivity(obdIntent);
                } catch (Exception ignored) {
                }
                finish();
            }, 500);
        }
    }
}
