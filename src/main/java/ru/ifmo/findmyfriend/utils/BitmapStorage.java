package ru.ifmo.findmyfriend.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
* Created by: avgarder
*/
public class BitmapStorage {
    public static interface BitmapLoadListener {
        void onBitmapLoaded(String url);
    }

    public static final String PARAMETER_FILE_PATH = "image_url";
    private static final int COUNT_THREADS = 3;

    private BitmapStorage() {
    }

    private static BitmapStorage instance = null;

    public static BitmapStorage getInstance() {
        if (instance == null) {
            instance = new BitmapStorage();
        }
        return instance;
    }

    private final ExecutorService executor = Executors.newFixedThreadPool(COUNT_THREADS);

    private final Map<String, Bitmap> urlToBitmap = new HashMap<String, Bitmap>();
    private final Map<Uri, String> bsUriToUrl = new HashMap<Uri, String>();

    private final List<BitmapLoadListener> listeners = new LinkedList<BitmapLoadListener>();
    private final Handler handler = new Handler();

    private final ContentObserver observer = new ContentObserver(handler) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            String filePath = Uri.decode(uri.getQueryParameter(PARAMETER_FILE_PATH));
            uri = uri.buildUpon().clearQuery().build();
            executor.execute(new BitmapLoader(bsUriToUrl.get(uri), new File(filePath)));
        }
    };

    public Bitmap getBitmap(Context context, String url) {
        if (url == null) {
            return null;
        }
        Bitmap bitmap = urlToBitmap.get(url);
        if (bitmap != null) {
            return bitmap;
        }
        File imageFile = ImageStorage.getImageFile(context, url);
        if (!imageFile.exists()) {
            Uri bsUri = getBSUriFromUrl(url);
            if (!bsUriToUrl.containsKey(bsUri)) {
                bsUriToUrl.put(bsUri, url);
                context.getContentResolver().registerContentObserver(getBSUriFromUrl(url), false, observer);
            }
        }
        // double check because of concurrency
        if (imageFile.exists()) {
            executor.execute(new BitmapLoader(url, imageFile));
        }
        return null;
    }

    public void addListener(BitmapLoadListener listener) {
        listeners.add(listener);
    }

    public void removeListener(BitmapLoadListener listener) {
        listeners.remove(listener);
    }

    public void clearAll(Context context) {
        context.getContentResolver().unregisterContentObserver(observer);
        instance = null;
    }

    public static Uri getBSUriFromUrl(String url) {
        return Uri.parse("bitmapstorage://" + ImageStorage.getNameFromUrl(url));
    }

    private class BitmapLoader implements Runnable {
        private String imageUrl;
        private File imageFile;

        private BitmapLoader(String imageUrl, File imageFile) {
            this.imageUrl = imageUrl;
            this.imageFile = imageFile;
        }

        @Override
        public void run() {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            urlToBitmap.put(imageUrl, bitmap);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (listeners) {
                        for (BitmapLoadListener listener : listeners) {
                            listener.onBitmapLoaded(imageUrl);
                        }
                    }
                }
            });
        }
    }
}
