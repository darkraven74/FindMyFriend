package ru.ifmo.findmyfriend;

import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class FriendListFragment extends ListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new FriendListAdapter(getActivity(), DBHelper.getAllFriends(getActivity())));
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }
}
