package ru.ifmo.findmyfriend.map;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ifmo.findmyfriend.FriendsStatusListener;
import ru.ifmo.findmyfriend.R;
import ru.ifmo.findmyfriend.friendlist.FriendData;
import ru.ifmo.findmyfriend.utils.DBHelper;
import ru.ifmo.findmyfriend.utils.Utils;

/**
 * Created by: avgarder
 */
public class MapFragment extends Fragment implements FriendsStatusListener {
    public static final String BUNDLE_KEY_LONGITUDE = "bundle_key_longitude";
    public static final String BUNDLE_KEY_LATITUDE = "bundle_key_latitude";

    private MapView mMapView;
    private GoogleMap mMap;
    private Bundle mBundle;
    private Map<String, Long> mUserIdFromMarkerId;
    private Map<Long, Marker> mMarkerFromUserId;

    private LatLng mCurLocation;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        Location location = Utils.getLastBestLocation(getActivity());
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
        mUserIdFromMarkerId = new HashMap<String, Long>();
        mMarkerFromUserId = new HashMap<Long, Marker>();
        updateMarkers();
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurLocation, 12));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.odnoklassniki.ru/profile/"
                        + mUserIdFromMarkerId.get(marker.getId())));
                startActivity(browseIntent);
            }
        });
    }

    private void updateMarkers() {
        List<FriendData> allFriends = DBHelper.getOnlineFriends(getActivity());
        Set<Marker> outdatedMarkers = new HashSet<Marker>(mMarkerFromUserId.values());
        for (FriendData friendData : allFriends) {
            Marker marker = mMarkerFromUserId.get(friendData.id);
            //TODO: images
            int resourceId = getActivity().getResources().getIdentifier("marker" + friendData.id,
                    "drawable", "ru.ifmo.findmyfriend");
            if (marker != null) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmap(resourceId)));
                marker.setPosition(new LatLng(friendData.latitude, friendData.longitude));
                outdatedMarkers.remove(marker);
            } else {
                marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(friendData.latitude, friendData.longitude))
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmap(resourceId)))
                        .title(friendData.name));
                marker.setAnchor(0.5f, 1);
                mUserIdFromMarkerId.put(marker.getId(), friendData.id);
                mMarkerFromUserId.put(friendData.id, marker);
            }
        }
        for (Marker marker : outdatedMarkers) {
            String markerId = marker.getId();
            mMarkerFromUserId.remove(mUserIdFromMarkerId.get(markerId));
            mUserIdFromMarkerId.remove(markerId);
            marker.remove();
        }
    }

    @Override
    public void onFriendsStatusUpdated() {
        updateMarkers();
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
