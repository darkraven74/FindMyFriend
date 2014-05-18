package ru.ifmo.findmyfriend.map;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
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

import ru.ifmo.findmyfriend.DataChangeListener;
import ru.ifmo.findmyfriend.R;
import ru.ifmo.findmyfriend.friendlist.FriendData;
import ru.ifmo.findmyfriend.utils.BitmapStorage;
import ru.ifmo.findmyfriend.utils.DBHelper;
import ru.ifmo.findmyfriend.utils.Utils;

/**
 * Created by: avgarder
 */
public class MapFragment extends Fragment implements DataChangeListener, BitmapStorage.BitmapLoadListener {
    public static final String BUNDLE_KEY_LONGITUDE = "bundle_key_longitude";
    public static final String BUNDLE_KEY_LATITUDE = "bundle_key_latitude";

    private MapView mapView;
    private GoogleMap map;
    private Map<String, Long> userIdFromMarkerId;
    private Map<Long, Marker> markerFromUserId;
    private Bitmap markerBackground;
    private LatLng curLocation;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Location location = Utils.getLastBestLocation(getActivity());
        curLocation = new LatLng(location.getLatitude(), location.getLongitude());

        Bundle args = getArguments();
        if (args != null && args.containsKey(BUNDLE_KEY_LONGITUDE)
                && args.containsKey(BUNDLE_KEY_LATITUDE)) {
            curLocation = new LatLng(args.getDouble(BUNDLE_KEY_LATITUDE),
                    args.getDouble(BUNDLE_KEY_LONGITUDE));
        }

        markerBackground = BitmapFactory.decodeResource(getResources(), R.drawable.custom_marker);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View inflatedView = inflater.inflate(R.layout.map_fragment, container, false);
        MapsInitializer.initialize(getActivity());
        mapView = (MapView) inflatedView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        setUpMapIfNeeded(inflatedView);
        return inflatedView;
    }

    private Bitmap getMarkerBitmap(Bitmap userImage) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap res = Bitmap.createBitmap(90, 125, conf);
        Canvas canvas = new Canvas(res);
        canvas.drawBitmap(markerBackground, 0, 0, null);
        if (userImage != null) {
            canvas.drawBitmap(Bitmap.createScaledBitmap(userImage, 70, 65, false), 10, 10, null);
        }
        return res;
    }

    private void setUpMapIfNeeded(View inflatedView) {
        if (map == null) {
            map = ((MapView) inflatedView.findViewById(R.id.mapView)).getMap();
            if (map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        userIdFromMarkerId = new HashMap<String, Long>();
        markerFromUserId = new HashMap<Long, Marker>();
        updateMarkers();
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 12));
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.odnoklassniki.ru/profile/"
                        + userIdFromMarkerId.get(marker.getId())));
                startActivity(browseIntent);
            }
        });
    }

    private void updateMarkers() {
        List<FriendData> allFriends = DBHelper.getOnlineFriends(getActivity());
        Set<Marker> outdatedMarkers = new HashSet<Marker>(markerFromUserId.values());
        for (FriendData friendData : allFriends) {
            Bitmap userImage = BitmapStorage.getInstance().getBitmap(getActivity(), friendData.imageUrl);
            if (userImage == null) {
                continue;
            }
            Marker marker = markerFromUserId.get(friendData.id);
            if (marker != null) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmap(userImage)));
                marker.setPosition(new LatLng(friendData.latitude, friendData.longitude));
                outdatedMarkers.remove(marker);
            } else {
                marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(friendData.latitude, friendData.longitude))
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmap(userImage)))
                        .title(friendData.name));
                marker.setAnchor(0.5f, 1);
                userIdFromMarkerId.put(marker.getId(), friendData.id);
                markerFromUserId.put(friendData.id, marker);
            }
        }
        for (Marker marker : outdatedMarkers) {
            String markerId = marker.getId();
            markerFromUserId.remove(userIdFromMarkerId.get(markerId));
            userIdFromMarkerId.remove(markerId);
            marker.remove();
        }
    }

    @Override
    public void onDataChange() {
        updateMarkers();
    }

    @Override
    public void onBitmapLoaded(String url) {
        updateMarkers();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        BitmapStorage.getInstance().addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        BitmapStorage.getInstance().removeListener(this);
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
}
