package ru.ifmo.findmyfriend;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import ru.ifmo.findmyfriend.friendlist.FriendListFragment;
import ru.ifmo.findmyfriend.map.MapFragment;

public class MainActivity extends Activity {
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private CharSequence title;
    private String[] menuTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        title = getTitle();
        menuTitles = getResources().getStringArray(R.array.drawer_array);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, menuTitles));
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
            selectItem(0);
        }
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

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments

        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                switchToFragment(new MapFragment());
                break;
            case 1:
                switchToFragment(new FriendListFragment());
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
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
        getActionBar().setTitle(this.title);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

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

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public static class TempFragment extends Fragment implements DataSetChangeable {
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

        @Override
        public void notifyDataSetChanged() {
        }
    }


}