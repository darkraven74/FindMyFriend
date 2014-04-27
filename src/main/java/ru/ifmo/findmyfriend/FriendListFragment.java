package ru.ifmo.findmyfriend;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendListFragment extends ListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new FriendListAdapter(getActivity(), getFriends()));
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    private List<FriendData> getFriends() {
        SQLiteDatabase db = new DBHelper(getActivity()).getReadableDatabase();
        Cursor c = db.query(DBHelper.FRIENDS, null, null, null, null, null, null);
        if (c == null || !c.moveToFirst()) {
            return Collections.emptyList();
        }

        int id = c.getColumnIndex(DBHelper.ID);
        int name = c.getColumnIndex(DBHelper.NAME);
        int latitude = c.getColumnIndex(DBHelper.LATITUDE);
        int longitude = c.getColumnIndex(DBHelper.LONGITUDE);
        List<FriendData> res = new ArrayList<FriendData>();
        do {
            FriendData data = new FriendData(c.getLong(id), c.getString(name), c.getDouble(latitude), c.getDouble(longitude));
            res.add(data);
        } while (c.moveToNext());
        return res;
    }
}
