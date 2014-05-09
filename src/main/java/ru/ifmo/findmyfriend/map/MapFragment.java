package ru.ifmo.findmyfriend.map;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
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
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import ru.ifmo.findmyfriend.R;
import ru.ifmo.findmyfriend.friendlist.FriendData;
import ru.ifmo.findmyfriend.utils.DBHelper;
import ru.ifmo.findmyfriend.utils.LocationUtils;

/**
 * Created by: avgarder
 */
public class MapFragment extends Fragment {
    public static final String BUNDLE_KEY_LONGITUDE = "bundle_key_longitude";
    public static final String BUNDLE_KEY_LATITUDE = "bundle_key_latitude";

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
        if (args != null && args.containsKey(BUNDLE_KEY_LONGITUDE)
                && args.containsKey(BUNDLE_KEY_LATITUDE)) {
            mCurLocation = new LatLng(args.getDouble(BUNDLE_KEY_LATITUDE),
                    args.getDouble(BUNDLE_KEY_LONGITUDE));
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

    private Bitmap getMarkerBitmap(int resourceId) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(90, 125, conf);
        Canvas canvas = new Canvas(bmp);
        Paint color = new Paint();
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.custom_marker), 0, 0, color);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
                resourceId), 10, 10, color);
        return bmp;
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
        mMap.setMyLocationEnabled(true);

        List<FriendData> allFriends = DBHelper.getOnlineFriends(getActivity());

        for (FriendData friendData : allFriends) {
            int resourceId = getActivity().getResources().getIdentifier("marker" + friendData.id,
                    "drawable", "ru.ifmo.findmyfriend");
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(friendData.latitude, friendData.longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmap(resourceId))))
                    .setAnchor(0.5f, 1);
        }


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
