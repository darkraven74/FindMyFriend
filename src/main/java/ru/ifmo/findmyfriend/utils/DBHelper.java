package ru.ifmo.findmyfriend.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ru.ifmo.findmyfriend.friendlist.FriendData;

/**
 * Created by: avgarder
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String TABLE_FRIENDS = "friends";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String IMAGE_URL = "image_url";
    public static final String IS_ALIVE = "is_alive";
    public static final String UPDATE_TIME = "update_time";

    public static final long ALIVE_INTERVAL = TimeUnit.MINUTES.toMillis(15);

    private static final Object LOCK = new Object();

    private static final int VERSION = 3;

    public DBHelper(Context context) {
        super(context, "mainDB", null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_FRIENDS + " (" +
                ID + " INTEGER PRIMARY KEY," +
                NAME + " TEXT," +
                LATITUDE + " REAL," +
                LONGITUDE + " REAL," +
                IMAGE_URL + " TEXT," +
                IS_ALIVE + " INTEGER," +
                UPDATE_TIME + " LONG" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_FRIENDS + ";");
        onCreate(db);
    }

    public static synchronized void saveFriends(Context context, List<FriendData> friends) {
        synchronized (LOCK) {
            String queryFormat = "INSERT OR REPLACE INTO " + TABLE_FRIENDS + "(" +
                    ID + "," + NAME + "," + LATITUDE + "," + LONGITUDE + "," + IMAGE_URL + "," + IS_ALIVE + "," + UPDATE_TIME +
                    ") VALUES (%d, \"%s\", %f, %f, \"%s\", %d, %d);";
            SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
            for (FriendData friend : friends) {
                String query = String.format(Locale.UK, queryFormat, friend.id, friend.name, friend.latitude, friend.longitude,
                        friend.imageUrl, friend.isAlive ? 1 : 0, friend.updateTime);
                db.execSQL(query);
            }
            db.close();
        }
    }

    public static void saveFriendsInfo(Context context, List<FriendData> friends) {
        synchronized (LOCK) {
            final String queryFormat = "INSERT OR REPLACE INTO " + TABLE_FRIENDS + "(" +
                    ID + "," + NAME + "," + IMAGE_URL + ") VALUES (%d, \"%s\", \"%s\");";
            SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
            for (FriendData friend : friends) {
                String query = String.format(Locale.UK, queryFormat, friend.id, friend.name, friend.imageUrl);
                db.execSQL(query);
            }
            db.close();
        }
    }

    public static void saveFriendsStatus(Context context, List<FriendData> friends) {
        final String queryFormat = "INSERT OR REPLACE INTO " + TABLE_FRIENDS + "(" +
                ID + "," + LATITUDE + "," + LONGITUDE + "," + IS_ALIVE + "," + UPDATE_TIME +
                ") VALUES (%d, %.7f, %.7f, %d, %d);";
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        for (FriendData friend : friends) {
            String query = String.format(queryFormat, friend.id, friend.latitude, friend.longitude, friend.isAlive ? 1 : 0, friend.updateTime);
            db.execSQL(query);
        }
        db.close();
    }

    public static List<FriendData> getAllFriends(Context context) {
        SQLiteDatabase db = new DBHelper(context).getReadableDatabase();
        Cursor c = db.query(DBHelper.TABLE_FRIENDS, null, null, null, null, null, null);
        List<FriendData> res = getFriendsFromCursor(c);
        db.close();
        return res;
    }

    public static List<FriendData> getOnlineFriends(Context context) {
        SQLiteDatabase db = new DBHelper(context).getReadableDatabase();
        String updateTimeLimit = String.valueOf(System.currentTimeMillis() - ALIVE_INTERVAL);

        Cursor c = db.query(DBHelper.TABLE_FRIENDS, null, IS_ALIVE + "=1 AND " + UPDATE_TIME + ">= $1",
                new String[]{updateTimeLimit}, null, null, null);
        List<FriendData> res = getFriendsFromCursor(c);
        db.close();
        return res;
    }

    private static List<FriendData> getFriendsFromCursor(Cursor c) {
        try {
            if (c == null || !c.moveToFirst()) {
                return Collections.emptyList();
            }

            int id = c.getColumnIndex(DBHelper.ID);
            int name = c.getColumnIndex(DBHelper.NAME);
            int latitude = c.getColumnIndex(DBHelper.LATITUDE);
            int longitude = c.getColumnIndex(DBHelper.LONGITUDE);
            int imageUrl = c.getColumnIndex(DBHelper.IMAGE_URL);
            int isAlive = c.getColumnIndex(DBHelper.IS_ALIVE);
            int updateTime = c.getColumnIndex(DBHelper.UPDATE_TIME);
            List<FriendData> res = new ArrayList<FriendData>();
            do {
                FriendData data = new FriendData(c.getLong(id), c.getString(name), c.getDouble(latitude),
                        c.getDouble(longitude), c.getString(imageUrl), c.getInt(isAlive) == 1, c.getLong(updateTime));
                res.add(data);
            } while (c.moveToNext());
            return res;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
