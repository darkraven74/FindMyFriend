package ru.ifmo.findmyfriend.friendlist;

/**
 * Created by: avgarder
 */
public class FriendData {
    private long id;
    private String name;
    private double latitude;
    private double longitude;
    private String imageUrl;

    public FriendData(long id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public FriendData(long id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
