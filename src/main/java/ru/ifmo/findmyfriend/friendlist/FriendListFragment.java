package ru.ifmo.findmyfriend.friendlist;

import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import ru.ifmo.findmyfriend.MainActivity;
import ru.ifmo.findmyfriend.R;
import ru.ifmo.findmyfriend.map.MapFragment;
import ru.ifmo.findmyfriend.utils.DBHelper;

public class FriendListFragment extends ListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new FriendListAdapter(getActivity(), DBHelper.getAllFriends(getActivity())));
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        MapFragment mapFragment = new MapFragment();
        Bundle args = new Bundle();
        args.putLong(MapFragment.BUNDLE_KEY_ID, id);
        mapFragment.setArguments(args);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.switchToFragment(mapFragment);
        mainActivity.setTitle(getResources().getString(R.string.menu_map));
    }
}
