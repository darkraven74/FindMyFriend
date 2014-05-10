package ru.ifmo.findmyfriend;

import android.app.IntentService;
import android.content.Intent;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ru.ifmo.findmyfriend.friendlist.FriendData;
import ru.ifmo.findmyfriend.utils.DBHelper;
import ru.ifmo.findmyfriend.utils.Logger;
import ru.ifmo.findmyfriend.utils.OkFriends;
import ru.ok.android.sdk.Odnoklassniki;

public class UpdateService extends IntentService {
    private static final String TAG = UpdateService.class.getName();

    public static final String ACTION_DATA_UPDATED = "ru.ifmo.findmyfriend.ACTION_DATA_UPDATED";

    private static final String URL_GET_FRIENDS_COORDS = "https://localhost:8443/get/coordinats";
    private static final String URL_POST_OUT_COORDS = "https://localhost:8443/post/coordinats";

    public UpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sendOurCoordinates();
        updateFriendsCoordinates();
    }

    private void sendOurCoordinates() {
    }

    private void updateFriendsCoordinates() {
        long currentTime = System.currentTimeMillis();
        List<FriendData> friends = getUserFriends();
        if (friends == null) {
            return;
        }
        Map<Long, FriendData> friendById = new HashMap<Long, FriendData>();
        for (FriendData friend : friends) {
            friendById.put(friend.id, friend);
        }
        JSONObject idsJson = genIdsJson(friends);
        HttpEntity entity = sendJson(URL_GET_FRIENDS_COORDS, idsJson);
        try {
            JSONObject resJson = new JSONObject(entity.toString());
            JSONArray result = resJson.getJSONArray("result");
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
            e.printStackTrace();
        }
        DBHelper.mapToDb(this, friends);
        Intent intent = new Intent(ACTION_DATA_UPDATED);
        sendBroadcast(intent);
    }

    private List<FriendData> getUserFriends() {
        Odnoklassniki ok = Odnoklassniki.getInstance(this);
        try {
            List<FriendData> res = new OkFriends().execute(ok).get();
            return res;
        } catch (InterruptedException e) {
            Logger.d(TAG, "getUserFriends", e);
            return null;
        } catch (ExecutionException e) {
            Logger.d(TAG, "getUserFriends", e);
            return null;
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
            Logger.d(TAG, "genIdsJson", e);
            return null;
        }
        return resJson;
    }

    private HttpEntity sendJson(String url, JSONObject json) {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);
        StringEntity entity;
        try {
            entity = new StringEntity(json.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
        request.setEntity(entity);
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            Logger.d(TAG, "sendJson", e);
            return null;
        }
        int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            Logger.d(TAG, "sendJson; Http error " + status);
            return null;
        }
        return response.getEntity();
    }
}
