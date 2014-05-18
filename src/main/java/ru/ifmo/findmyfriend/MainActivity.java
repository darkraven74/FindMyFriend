package ru.ifmo.findmyfriend;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.findmyfriend.drawer.DrawerItem;
import ru.ifmo.findmyfriend.drawer.DrawerListAdapter;
import ru.ifmo.findmyfriend.friendlist.FriendListFragment;
import ru.ifmo.findmyfriend.map.MapFragment;
import ru.ifmo.findmyfriend.settings.MyLocationFragment;
import ru.ifmo.findmyfriend.utils.BitmapStorage;

public class MainActivity extends Activity implements BitmapStorage.BitmapLoadListener {
    public static final String PREFERENCES_NAME = MainActivity.class.getName();
    public static final String PREFERENCE_SHARING_END_TIME = "sharing_end_time";
    public static final String PREFERENCE_CURRENT_UID = "current_uid";
    public static final String PREFERENCE_CURRENT_NAME = "current_name";
    public static final String PREFERENCE_CURRENT_IMG_URL = "current_pic";

    private static final String SAVED_DRAWER_SELECTED_POSITION = "drawer_selected_position";

    private int drawerSelectedPosition;

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private DrawerListAdapter drawerListAdapter;
    private ActionBarDrawerToggle drawerToggle;

    private String[] menuTitles;
    private TypedArray menuIcons;

    private long userId;

    private Fragment currentFragment;
    private UpdateReceiver updateReceiver = new UpdateReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        menuTitles = getResources().getStringArray(R.array.drawer_array);
        menuIcons = getResources().obtainTypedArray(R.array.drawer_icons);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        SharedPreferences prefs = getSharedPreferences(PREFERENCES_NAME, MODE_MULTI_PROCESS);
        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();

        userId = prefs.getLong(PREFERENCE_CURRENT_UID, 0);
        drawerItems.add(new DrawerItem(prefs.getString(PREFERENCE_CURRENT_NAME, ""),
                prefs.getString(PREFERENCE_CURRENT_IMG_URL, "")));
        for (int i = 1; i <= 5; i++) {
            drawerItems.add(new DrawerItem(menuTitles[i], menuIcons.getResourceId(i, -1)));
        }

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerListAdapter = new DrawerListAdapter(this, drawerItems);
        drawerList.setAdapter(drawerListAdapter);
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {

        };
        drawerLayout.setDrawerListener(drawerToggle);

        if (savedInstanceState == null) {
            selectItem(1);
        } else {
            selectItem(savedInstanceState.getInt(SAVED_DRAWER_SELECTED_POSITION, 1));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(UpdateService.ACTION_DATA_CHANGE);
        registerReceiver(updateReceiver, filter);
        BitmapStorage.getInstance().addListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);
        BitmapStorage.getInstance().removeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BitmapStorage.getInstance().clearAll(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_DRAWER_SELECTED_POSITION, drawerSelectedPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBitmapLoaded(String url) {
        drawerListAdapter.notifyDataSetChanged();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        drawerSelectedPosition = position;
        switch (position) {
            case 0:
                Intent browseIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.odnoklassniki.ru/profile/" + userId));
                startActivity(browseIntent);
                break;
            case 1:
                switchToFragment(new MapFragment());
                break;
            case 2:
                switchToFragment(new FriendListFragment());
                break;
            case 3:
                switchToFragment(new MyLocationFragment());
                break;
            default:
                Fragment tempFragment = new TempFragment();
                Bundle args = new Bundle();
                args.putInt(TempFragment.ARG_FRAGMENT_NUMBER, position);
                tempFragment.setArguments(args);
                switchToFragment(tempFragment);
        }

        drawerList.setItemChecked(position, true);
        drawerList.setItemChecked(position, false);
        setTitle(menuTitles[position]);

        drawerLayout.closeDrawer(drawerList);
    }

    public void switchToFragment(Fragment fragment) {
        currentFragment = fragment;
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public void setTitle(CharSequence title) {
        getActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentFragment instanceof DataChangeListener) {
                ((DataChangeListener) currentFragment).onDataChange();
            }
        }
    }

    public static class TempFragment extends Fragment {
        public static final String ARG_FRAGMENT_NUMBER = "fragment_number";

        public TempFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            int i = getArguments().getInt(ARG_FRAGMENT_NUMBER);
            View rootView = inflater.inflate(R.layout.test_fragment, container, false);
            String text = getResources().getStringArray(R.array.drawer_array)[i];
            ((TextView) rootView.findViewById(R.id.text)).setText(text);
            getActivity().setTitle(text);
            return rootView;
        }
    }
}
