package ru.ifmo.findmyfriend.friendlist;

/**
 * Created by: avgarder
 */
public class FriendData {
    public long id;
    public String name;
    public double latitude;
    public double longitude;
    public String imageUrl;
    public boolean isAlive;
    public long updateTime;

    public FriendData(long id, String name, double latitude, double longitude, String imageUrl, boolean isAlive, long updateTime) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.isAlive = isAlive;
        this.updateTime = updateTime;
    }

    public FriendData(long id, String name, String imageUrl) {
        this(id, name, 0, 0, imageUrl, false, 0);
    }

    public FriendData(long id, double latitude, double longitude, boolean isAlive, long updateTime) {
        this(id, null, latitude, longitude, null, isAlive, updateTime);
    }
}
