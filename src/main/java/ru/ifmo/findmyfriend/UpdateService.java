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
import java.util.concurrent.ExecutionException;

import ru.ifmo.findmyfriend.friendlist.FriendData;
import ru.ifmo.findmyfriend.utils.DBHelper;
import ru.ifmo.findmyfriend.utils.Logger;
import ru.ifmo.findmyfriend.utils.OkFriends;
import ru.ok.android.sdk.Odnoklassniki;

public class UpdateService extends IntentService {
    private static final String TAG = UpdateService.class.getName();

    public static final String ACTION_DATA_UPDATED = "ru.ifmo.findmyfriend.ACTION_DATA_UPDATED";

    private static final String URL_GET_FRIENDS_COORDS = "http://192.243.125.239:9031/get/coordinates";
    private static final String URL_POST_OUT_COORDS = "http://192.243.125.239:9031/post/coordinates";

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
        List<FriendData> updatedFriends = new ArrayList<FriendData>();
        Map<Long, FriendData> friendById = new HashMap<Long, FriendData>();
        for (FriendData friend : friends) {
            friendById.put(friend.id, friend);
        }
        JSONObject idsJson = genIdsJson(friends);
        HttpEntity entity = postJson(URL_GET_FRIENDS_COORDS, idsJson);
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
                updatedFriends.add(friend);
            }
        } catch (JSONException e) {
            Logger.d(TAG, "updateFriendsCoordinates", e);
            return;
        } catch (IOException e) {
            Logger.d(TAG, "updateFriendsCoordinates", e);
            return;
        }
        DBHelper.save(this, updatedFriends);
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
            Logger.d(TAG, "postJson", e);
            return null;
        }
        int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            Logger.d(TAG, "postJson; Http error " + status);
            return null;
        }
        return response.getEntity();
    }
}
