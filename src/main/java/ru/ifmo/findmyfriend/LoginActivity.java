package ru.ifmo.findmyfriend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ru.ifmo.findmyfriend.utils.ImageDownloader;
import ru.ifmo.findmyfriend.utils.ImageStorage;
import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import ru.ok.android.sdk.util.OkScope;

public class LoginActivity extends Activity implements OkTokenRequestListener {
    private String APP_ID = "927961344";
    private String APP_PUBLIC_KEY = "CBACJQEIDBABABABA";
    private String APP_SECRET_KEY = "BA4A8EBC7AF0F03551156B44";
    private Odnoklassniki mOdnoklassniki;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onResume();
        mOdnoklassniki = Odnoklassniki.createInstance(getApplicationContext(), APP_ID, APP_SECRET_KEY, APP_PUBLIC_KEY);
        mOdnoklassniki.setTokenRequestListener(this);
        mOdnoklassniki.requestAuthorization(this, false, OkScope.VALUABLE_ACCESS);
    }

    @Override
    public void onSuccess(String token) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> requestParams = new HashMap<String, String>();
                requestParams.put("fields", "uid, name, pic_5");

                Context context = LoginActivity.this;
                try {
                    JSONObject info = new JSONObject(mOdnoklassniki.request("users.getCurrentUser", requestParams, "get"));
                    SharedPreferences preferences = context.getSharedPreferences(MainActivity.PREFERENCES_NAME, MODE_MULTI_PROCESS);
                    preferences.edit()
                            .putLong(MainActivity.PREFERENCE_CURRENT_UID, Long.parseLong(info.getString("uid")))
                            .putString(MainActivity.PREFERENCE_CURRENT_NAME, info.getString("name"))
                            .putString(MainActivity.PREFERENCE_CURRENT_IMG_URL, info.getString("pic_5"))
                            .commit();
                    String imageUrl = info.getString("pic_5");
                    if (!ImageStorage.imageExists(context, imageUrl)) {
                        ImageDownloader.downloadImage(context, imageUrl);
                    }

                    Intent serviceIntent = new Intent(context, UpdateService.class);
                    serviceIntent.putExtra(UpdateService.EXTRA_TASK_ID, UpdateService.TASK_UPDATE_FRIENDS_INFO);
                    startService(serviceIntent);

                    startActivity(new Intent(context, MainActivity.class));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    LoginActivity.this.finish();
                }
            }
        }).start();
    }

    @Override
    public void onError() {
        finish();
    }

    @Override
    public void onCancel() {
        finish();
    }
}
