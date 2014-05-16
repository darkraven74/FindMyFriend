package ru.ifmo.findmyfriend.utils;

import android.content.Context;

import java.io.File;

/**
* Created by: avgarder
*/
public class ImageStorage {
    private static final String STORAGE_DIRECTORY_NAME = "storage";

    private static File getStorageDirectory(Context context) {
        File directory = new File(context.getExternalCacheDir(), STORAGE_DIRECTORY_NAME);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    public static File getImageFile(Context context, String imageUrl) {
        return new File(getStorageDirectory(context), getNameFromUrl(imageUrl));
    }

    public static boolean imageExists(Context context, String imageUrl) {
        return getImageFile(context, imageUrl).exists();
    }

    public static String getNameFromUrl(String imageUrl) {
        return Utils.md5Hash(imageUrl);
    }
}
