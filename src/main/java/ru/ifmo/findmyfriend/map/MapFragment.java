package ru.ifmo.findmyfriend.map;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ru.ifmo.findmyfriend.R;
import ru.ifmo.findmyfriend.utils.LocationUtils;

/**
 * Created by: avgarder
 */
public class MapFragment extends Fragment {
    public static final String BUNDLE_KEY_ID = "bundle_key_id";

    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private MapView mMapView;
    private GoogleMap mMap;
    private Bundle mBundle;

    private LatLng mCurLocation;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        Location location = LocationUtils.getLastBestLocation(getActivity());
        mCurLocation = new LatLng(location.getLatitude(), location.getLongitude());

        Bundle args = getArguments();
        if (args != null && args.containsKey(BUNDLE_KEY_ID)) {
            long selectedUserId = args.getLong(BUNDLE_KEY_ID);
            //TODO: deal with it :)
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View inflatedView = inflater.inflate(R.layout.map_fragment, container, false);
        MapsInitializer.initialize(getActivity());
        mMapView = (MapView) inflatedView.findViewById(R.id.mapView);
        mMapView.onCreate(mBundle);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setUpMapIfNeeded(inflatedView);
            }
        }, 200);

        return inflatedView;
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
                .position(mCurLocation)
                .title("title").snippet("text")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurLocation, 12));
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
