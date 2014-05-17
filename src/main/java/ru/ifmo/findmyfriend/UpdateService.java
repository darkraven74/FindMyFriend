package ru.ifmo.findmyfriend;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ifmo.findmyfriend.friendlist.FriendData;
import ru.ifmo.findmyfriend.utils.DBHelper;
import ru.ifmo.findmyfriend.utils.ImageDownloader;
import ru.ifmo.findmyfriend.utils.ImageStorage;
import ru.ifmo.findmyfriend.utils.Utils;
import ru.ifmo.findmyfriend.utils.Logger;
import ru.ok.android.sdk.Odnoklassniki;

public class UpdateService extends IntentService {
    private static final String LOG_TAG = UpdateService.class.getName();

    public static final String ACTION_DATA_CHANGE = "ru.ifmo.findmyfriend.ACTION_DATA_CHANGE";

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_DURATION = "duration";

    public static final int TASK_SEND_OUR_COORDINATES = 1;
    public static final int TASK_UPDATE_FRIENDS_STATUS = 2;
    public static final int TASK_UPDATE_FRIENDS_INFO = 3;
    public static final int TASK_SEND_DURATION = 4;

    private static final String URL_GET_FRIENDS_COORDINATES = "http://192.243.125.239:9031/get/coordinates";
    private static final String URL_POST_OUR_COORDINATES = "http://192.243.125.239:9031/post/coordinates";
    private static final String URL_POST_DURATION = "http://192.243.125.239:9031/post/duration";

    public UpdateService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int taskType = intent.getIntExtra(EXTRA_TASK_ID, -1);
        switch (taskType) {
            case TASK_SEND_OUR_COORDINATES:
                sendOurCoordinates();
                break;
            case TASK_UPDATE_FRIENDS_STATUS:
                updateFriendsStatus();
                break;
            case TASK_UPDATE_FRIENDS_INFO:
                updateFriendsInfo();
                break;
            case TASK_SEND_DURATION:
                long duration = intent.getLongExtra(EXTRA_DURATION, 0);
                if (duration < 0) {
                    duration = 0;
                }
                sendDuration(duration);
                break;
            default:
                Logger.d(LOG_TAG, "Invalid task type: " + taskType);
        }
    }

    public static PendingIntent getSendCoordinatesIntent(Context context) {
        Intent intent = new Intent(context, UpdateService.class);
        intent.putExtra(EXTRA_TASK_ID, TASK_SEND_OUR_COORDINATES);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    private void sendOurCoordinates() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFERENCES_NAME, MODE_MULTI_PROCESS);
        long endTime = prefs.getLong(MainActivity.PREFERENCE_SHARING_END_TIME, 0);
        if (endTime < System.currentTimeMillis()) {
            PendingIntent intent = getSendCoordinatesIntent(this);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(intent);
            return;
        }

        Location location = Utils.getLastBestLocation(this);
        JSONObject dataJson = new JSONObject();
        long currentUid = Utils.getCurrentUserId(this);
        if (currentUid == -1) {
            return;
        }
        try {
            dataJson.put("id", currentUid);
            dataJson.put("latitude", location.getLatitude());
            dataJson.put("longitude", location.getLongitude());
        } catch (JSONException ignored) {
        }
        postJson(URL_POST_OUR_COORDINATES, dataJson);
    }

    private void sendDuration(long duration) {
        long currentUid = Utils.getCurrentUserId(this);
        if (currentUid == -1) {
            return;
        }
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("id", currentUid);
            dataJson.put("duration", duration);
        } catch (JSONException ignored) {
        }
        postJson(URL_POST_DURATION, dataJson);
    }

    private void updateFriendsStatus() {
        long currentTime = System.currentTimeMillis();
        List<FriendData> friends = DBHelper.getAllFriends(this);
        if (friends == null || friends.isEmpty()) {
            return;
        }
        Map<Long, FriendData> friendById = new HashMap<Long, FriendData>();
        for (FriendData friend : friends) {
            friendById.put(friend.id, friend);
        }
        JSONObject idsJson = genIdsJson(friends);
        HttpEntity entity = postJson(URL_GET_FRIENDS_COORDINATES, idsJson);
        try {
            JSONObject resJson = new JSONObject(EntityUtils.toString(entity));
            JSONArray result = resJson.getJSONArray("users");
            for (int i = 0; i < result.length(); i++) {
                JSONObject user = result.getJSONObject(i);
                long id = user.getLong("id");
                FriendData friend = friendById.get(id);
                friend.updateTime = currentTime;
                friend.isAlive = user.getBoolean("alive");
                if (friend.isAlive) {
                    friend.latitude = user.getDouble("latitude");
                    friend.longitude = user.getDouble("longitude");
                }
            }
        } catch (JSONException e) {
            Logger.d(LOG_TAG, "updateFriendsStatus", e);
            return;
        } catch (IOException e) {
            Logger.d(LOG_TAG, "updateFriendsStatus", e);
            return;
        }
        DBHelper.saveFriendsStatus(this, friends);
        sendBroadcast(new Intent(ACTION_DATA_CHANGE));
    }

    private void updateFriendsInfo() {
        Odnoklassniki ok = Odnoklassniki.getInstance(this);
        try {
            JSONArray friendsIdsArray = new JSONArray(ok.request("friends.get", null, "get"));
            StringBuilder friendsIds = new StringBuilder();
            for (int i = 0; i < friendsIdsArray.length(); i++) {
                friendsIds.append(',').append(friendsIdsArray.getString(i));
            }

            Map<String, String> requestParams = new HashMap<String, String>();
            requestParams.put("uids", friendsIds.substring(1));
            requestParams.put("fields", "uid, name, pic_5");
            String friendsInfo = ok.request("users.getInfo", requestParams, "get");

            List<FriendData> friends = new ArrayList<FriendData>();
            JSONArray friendsArray = new JSONArray(friendsInfo);
            for (int i = 0; i < friendsArray.length(); i++) {
                JSONObject friendJson = friendsArray.getJSONObject(i);
                FriendData friend = FriendData.fromInfo(Long.parseLong(friendJson.getString("uid")),
                        friendJson.getString("name"), friendJson.getString("pic_5"));
                friends.add(friend);

                if (!ImageStorage.imageExists(this, friend.imageUrl)) {
                    ImageDownloader.downloadImage(this, friend.imageUrl);
                }
            }
            DBHelper.saveFriendsInfo(this, friends);
            sendBroadcast(new Intent(ACTION_DATA_CHANGE));
        } catch (IOException e) {
            Logger.d(LOG_TAG, "updateFriendsInfo", e);
        } catch (JSONException e) {
            Logger.d(LOG_TAG, "updateFriendsInfo", e);
        }
    }

    private JSONObject genIdsJson(List<FriendData> friends) {
        JSONArray idsArray = new JSONArray();
        for (FriendData friend : friends) {
            idsArray.put(friend.id);
        }
        JSONObject resJson = new JSONObject();
        try {
            resJson.put("ids", idsArray);
        } catch (JSONException e) {
            Logger.d(LOG_TAG, "genIdsJson", e);
            return null;
        }
        return resJson;
    }

    private HttpEntity postJson(String url, JSONObject json) {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);
        StringEntity entity;
        try {
            entity = new StringEntity(json.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
        request.setEntity(entity);
        request.addHeader("Content-Type", "application/json");
        HttpResponse response;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            Logger.d(LOG_TAG, "postJson", e);
            return null;
        }
        int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            Logger.d(LOG_TAG, "postJson; Http error " + status);
            return null;
        }
        return response.getEntity();
    }
}
