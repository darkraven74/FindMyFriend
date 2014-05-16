package ru.ifmo.findmyfriend.utils;

import android.content.Context;
import android.net.Uri;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by: avgarder
 */
public class ImageDownloader {
    private static final String LOG_TAG = ImageDownloader.class.getName();

    private final static ExecutorService executor = Executors.newFixedThreadPool(1);

    public static void downloadImage(Context context, String imageUrl) {
        executor.execute(new DownloadTask(context, imageUrl));
    }

    private static class DownloadTask implements Runnable {
        private Context context;
        private String imageUrl;

        private DownloadTask(Context context, String imageUrl) {
            this.context = context;
            this.imageUrl = imageUrl;
        }

        @Override
        public void run() {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(imageUrl);
                HttpResponse response = client.execute(request);
                int status = response.getStatusLine().getStatusCode();
                if (status != HttpStatus.SC_OK) {
                    Logger.d(LOG_TAG, "Download image by url: " + imageUrl + "; HttpError " + status);
                    return;
                }
                HttpEntity entity = response.getEntity();

                File imageFile = ImageStorage.getImageFile(context, imageUrl);
                OutputStream output = new FileOutputStream(imageFile);
                entity.writeTo(output);
                output.close();

                Uri bsUri = BitmapStorage.getBSUriFromUrl(imageUrl);
                bsUri = bsUri.buildUpon().appendQueryParameter(BitmapStorage.PARAMETER_FILE_PATH, imageFile.getAbsolutePath()).build();
                context.getContentResolver().notifyChange(bsUri, null);
            } catch (IOException e) {
                Logger.d(LOG_TAG, "Download image by url: " + imageUrl, e);
            }
        }
    }
}
