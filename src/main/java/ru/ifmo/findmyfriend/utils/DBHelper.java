package ru.ifmo.findmyfriend.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private static final int VERSION = 2;

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

        insertTempData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_FRIENDS + ";");
        onCreate(db);
    }

    public static void mapToDb(Context context, List<FriendData> friends) {
        String queryFormat = "INSERT OR REPLACE INTO " + TABLE_FRIENDS + "(" +
                ID + "," + NAME + "," + LATITUDE + "," + LONGITUDE + "," + IMAGE_URL + "," + IS_ALIVE + "," + UPDATE_TIME +
                ") VALUES (%d, %s, %f, %f, %s, %d, %d);";
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        db.beginTransaction();
        for (FriendData friend : friends) {
            db.execSQL(String.format(queryFormat, friend.id, friend.name, friend.latitude, friend.longitude,
                    friend.imageUrl, friend.isAlive ? 1 : 0, friend.updateTime));
        }
        db.endTransaction();
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
        updateTimeLimit = "0";

        Cursor c = db.query(DBHelper.TABLE_FRIENDS, null, IS_ALIVE + "=1 AND " + UPDATE_TIME + ">= $1",
                new String[]{updateTimeLimit}, null, null, null);
        List<FriendData> res = getFriendsFromCursor(c);
        db.close();
        return res;
    }

    private static List<FriendData> getFriendsFromCursor(Cursor c) {
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
    }

    private void insertTempData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO friends VALUES (1, \"Марина Васильева\", 59.9570072, 30.2729272, \"\", 1, 0);");
        db.execSQL("INSERT INTO friends VALUES (2, \"Артем Комаров\", 59.9676194, 30.384112, \"\", 1, 0);");
        db.execSQL("INSERT INTO friends VALUES (3, \"Николай Иванов\", 59.9411471, 30.3793913, \"\", 1, 0);");
        db.execSQL("INSERT INTO friends VALUES (4, \"Эдгар Кузнецов\", 59.9235158, 30.3332144, \"\", 1, -1);");
        db.execSQL("INSERT INTO friends VALUES (5, \"Наталья Костенева\", 59.9232577, 30.2954489, \"\", 0, 0);");
    }
}
