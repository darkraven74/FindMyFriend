package ru.ifmo.findmyfriend.friendlist;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import ru.ifmo.findmyfriend.DataChangeListener;
import ru.ifmo.findmyfriend.MainActivity;
import ru.ifmo.findmyfriend.R;
import ru.ifmo.findmyfriend.map.MapFragment;
import ru.ifmo.findmyfriend.utils.BitmapStorage;
import ru.ifmo.findmyfriend.utils.DBHelper;

public class FriendListFragment extends ListFragment implements DataChangeListener, BitmapStorage.BitmapLoadListener {
    private FriendListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new FriendListAdapter(getActivity());
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        BitmapStorage.getInstance().addListener(this);
        updateAdapter();
    }

    @Override
    public void onPause() {
        super.onPause();
        BitmapStorage.getInstance().removeListener(this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        MapFragment mapFragment = new MapFragment();
        Bundle args = new Bundle();
        FriendData item = (FriendData) l.getItemAtPosition(position);
        args.putDouble(MapFragment.BUNDLE_KEY_LATITUDE, item.latitude);
        args.putDouble(MapFragment.BUNDLE_KEY_LONGITUDE, item.longitude);
        mapFragment.setArguments(args);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.switchToFragment(mapFragment);
        mainActivity.setTitle(getResources().getString(R.string.menu_map));
    }

    @Override
    public void onDataChange() {
        updateAdapter();
    }

    @Override
    public void onBitmapLoaded(String url) {
        adapter.notifyDataSetChanged();
    }

    private void updateAdapter() {
        adapter.setData(DBHelper.getAllFriends(getActivity()));
    }
}
