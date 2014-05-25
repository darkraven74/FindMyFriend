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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.ifmo.findmyfriend.about.AboutFragment;
import ru.ifmo.findmyfriend.drawer.DrawerItem;
import ru.ifmo.findmyfriend.drawer.DrawerListAdapter;
import ru.ifmo.findmyfriend.friendlist.FriendListFragment;
import ru.ifmo.findmyfriend.map.MapFragment;
import ru.ifmo.findmyfriend.mylocation.MyLocationFragment;
import ru.ifmo.findmyfriend.utils.BitmapStorage;

public class MainActivity extends Activity implements BitmapStorage.BitmapLoadListener {
    public static final String PREFERENCES_NAME = MainActivity.class.getName();
    public static final String PREFERENCE_SHARING_END_TIME = "sharing_end_time";
    public static final String PREFERENCE_CURRENT_UID = "current_uid";
    public static final String PREFERENCE_CURRENT_NAME = "current_name";
    public static final String PREFERENCE_CURRENT_IMG_URL = "current_pic";

    public static final int DRAWER_MAP_POSITION = 1;
    public static final int DRAWER_FRIEND_LIST_POSITION = 2;
    public static final int DRAWER_MY_LOCATION_POSITION = 3;
    public static final int DRAWER_ABOUT_POSITION = 4;

    private static final String FRAGMENTS_TAG = "ru.ifmo.findmyfriend.MainActivity.FRAGMENTS";

    private static final String SAVED_DRAWER_SELECTED_POSITION = "drawer_selected_position";

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private DrawerListAdapter drawerListAdapter;
    private ActionBarDrawerToggle drawerToggle;

    private String[] menuTitles;
    private TypedArray menuIcons;

    private long userId;

    private UpdateReceiver updateReceiver = new UpdateReceiver();
    private Handler handler = new Handler();


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
        for (int i = 1; i <= 4; i++) {
            drawerItems.add(new DrawerItem(menuTitles[i], menuIcons.getResourceId(i, -1)));
        }

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerListAdapter = new DrawerListAdapter(this, drawerItems);
        drawerList.setAdapter(drawerListAdapter);
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FE9711")));

        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer_light,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {

        };
        drawerLayout.setDrawerListener(drawerToggle);

        BitmapStorage.createInstance(this);

        if (savedInstanceState == null) {
            selectItem(DRAWER_MAP_POSITION);
        } else {
            setTitle(menuTitles[savedInstanceState.getInt(SAVED_DRAWER_SELECTED_POSITION, DRAWER_MAP_POSITION)]);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(UpdateService.ACTION_DATA_CHANGE);
        registerReceiver(updateReceiver, filter);
        BitmapStorage.getInstance().addListener(this);

        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, UpdateService.class);
                intent.putExtra(UpdateService.EXTRA_TASK_ID, UpdateService.TASK_UPDATE_FRIENDS_STATUS);
                startService(intent);
                handler.postDelayed(this, TimeUnit.SECONDS.toMillis(30));
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);
        BitmapStorage.getInstance().removeListener(this);
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BitmapStorage.getInstance().clearAll(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int currentDrawerPosition;
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof MapFragment) {
            currentDrawerPosition = DRAWER_MAP_POSITION;
        } else if (currentFragment instanceof FriendListFragment) {
            currentDrawerPosition = DRAWER_FRIEND_LIST_POSITION;
        } else if (currentFragment instanceof MyLocationFragment) {
            currentDrawerPosition = DRAWER_MY_LOCATION_POSITION;
        } else if (currentFragment instanceof AboutFragment) {
            currentDrawerPosition = DRAWER_ABOUT_POSITION;
        } else {
            throw new AssertionError("Invalid currentFragment");
        }
        outState.putInt(SAVED_DRAWER_SELECTED_POSITION, currentDrawerPosition);
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
        switch (position) {
            case 0:
                Intent browseIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.odnoklassniki.ru/profile/" + userId));
                startActivity(browseIntent);
                return;
            case 1:
                switchToFragment(new MapFragment());
                break;
            case 2:
                switchToFragment(new FriendListFragment());
                break;
            case 3:
                switchToFragment(new MyLocationFragment());
                break;
            case 4:
                switchToFragment(new AboutFragment());
                break;
        }
        drawerList.setItemChecked(position, true);
        drawerList.setItemChecked(position, false);
        setTitle(menuTitles[position]);

        drawerLayout.closeDrawer(drawerList);
    }

    public void switchToFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, FRAGMENTS_TAG).commit();
    }

    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentByTag(FRAGMENTS_TAG);
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
            Fragment currentFragment = MainActivity.this.getCurrentFragment();
            if (currentFragment instanceof DataChangeListener) {
                ((DataChangeListener) currentFragment).onDataChange();
            }
        }
    }
}
