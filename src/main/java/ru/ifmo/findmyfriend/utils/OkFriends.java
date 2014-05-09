package ru.ifmo.findmyfriend.utils;


import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ifmo.findmyfriend.friendlist.FriendData;
import ru.ok.android.sdk.Odnoklassniki;

public class OkFriends extends AsyncTask<Odnoklassniki, Void, List<FriendData>> {

    @Override
    protected List<FriendData> doInBackground(Odnoklassniki... odnoklassnikis) {
        List<FriendData> result = null;
        try {
            String friendsId = jsonArrayToString(odnoklassnikis[0].request("friends.get", null, "get"));
            Map<String, String> requestParams = new HashMap<String, String>();
            requestParams.put("uids", friendsId);
            requestParams.put("fields", "uid, last_name, first_name, pic_5");
            String friendsInfo = odnoklassnikis[0].request("users.getInfo", requestParams, "get");
            result = getFriendsInfoFromResponse(friendsInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String jsonArrayToString(String str) throws JSONException {
        JSONArray array = new JSONArray(str);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            builder.append(',').append(array.getString(i));
        }
        return builder.substring(1);
    }

    private List<FriendData> getFriendsInfoFromResponse(String response) throws JSONException {
        List<FriendData> friends = new ArrayList<FriendData>();
        JSONArray friendsJSON = new JSONArray(response);
        for (int i = 0; i < friendsJSON.length(); i++) {
            JSONObject friend = friendsJSON.getJSONObject(i);
            friends.add(new FriendData(Long.parseLong(friend.getString("uid")), friend.getString("first_name") + " " +
                    friend.getString("last_name"), friend.getString("pic_5")));
        }
        return friends;
    }
}
