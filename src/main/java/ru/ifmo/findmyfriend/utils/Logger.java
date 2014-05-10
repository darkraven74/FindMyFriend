package ru.ifmo.findmyfriend.utils;

import android.util.Log;

import ru.ifmo.findmyfriend.BuildConfig;

/**
 * Created by: avgarder
 */
public class Logger {
    public static void d(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg, tr);
        }
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }
}
