package ru.ifmo.findmyfriend;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapActivity extends Activity {
    LocationManager locManager;
    Location location;
    LatLng curLocation;

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private CharSequence title;
    private String[] menuTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

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

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        location = getLastBestLocation();
        curLocation = new LatLng(location.getLatitude(), location.getLongitude());

    }


    public Location getLastBestLocation() {
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        List<String> matchingProviders = locManager.getAllProviders();
        for (String provider : matchingProviders) {
            Location location = locManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if (accuracy < bestAccuracy) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if (bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }
        return bestResult;
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
        if (position == 0) {
            Fragment mapFragment = new BasicMapActivity();
            mapFragment.getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, mapFragment).commit();

        } else {
            Fragment tempFragment = new TempFragment();
            Bundle args = new Bundle();
            args.putInt(TempFragment.ARG_FRAGMENT_NUMBER, position);
            tempFragment.setArguments(args);
            fragmentManager.beginTransaction().replace(R.id.content_frame, tempFragment).commit();
        }

        drawerList.setItemChecked(position, true);
        drawerList.setItemChecked(position, false);

        setTitle(menuTitles[position]);
        drawerLayout.closeDrawer(drawerList);
    }

    public class BasicMapActivity extends Fragment {
        /**
         * Note that this may be null if the Google Play services APK is not available.
         */
        private MapView mMapView;
        private GoogleMap mMap;
        private Bundle mBundle;


        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View inflatedView = inflater.inflate(R.layout.map_fragment, container, false);
            MapsInitializer.initialize(getActivity());
            mMapView = (MapView) inflatedView.findViewById(R.id.mapView);
            mMapView.onCreate(mBundle);
            setUpMapIfNeeded(inflatedView);

            return inflatedView;
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mBundle = savedInstanceState;
        }

        private void setUpMapIfNeeded(View inflatedView) {
            if (mMap == null) {
                mMap = ((MapView) inflatedView.findViewById(R.id.mapView)).getMap();
                if (mMap != null) {
                    setUpMap();
                }
            }
        }

        private void setUpMap() {
            Marker me = mMap.addMarker(new MarkerOptions()
                    .position(curLocation)
                    .title("title").snippet("text")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 12));
        }

        @Override
        public void onResume() {
            super.onResume();
            mMapView.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
            mMapView.onPause();
        }

        @Override
        public void onDestroy() {
            mMapView.onDestroy();
            super.onDestroy();
        }
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