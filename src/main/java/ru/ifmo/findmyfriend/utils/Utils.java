package ru.ifmo.findmyfriend.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;

import java.util.List;

import ru.ifmo.findmyfriend.MainActivity;

/**
 * Created by: avgarder
 */
public class Utils {
    public static Location getLastBestLocation(Context context) {
        LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        List<String> matchingProviders = locManager.getAllProviders();
        for (String provider : matchingProviders) {
            Location location = locManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if (accuracy < bestAccuracy) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if (bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }
        return bestResult;
    }

    public static long getCurrentUserId(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);
        return preferences.getLong(MainActivity.PREFERENCE_CURRENT_UID, -1);
    }
}
