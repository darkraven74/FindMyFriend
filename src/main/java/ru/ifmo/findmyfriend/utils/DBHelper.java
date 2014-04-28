package ru.ifmo.findmyfriend.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ifmo.findmyfriend.friendlist.FriendData;

/**
 * Created by: avgarder
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String FRIENDS = "friends";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String UPDATE_TIME = "update_time";

    private static final int VERSION = 1;

    public DBHelper(Context context) {
        super(context, "mainDB", null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE friends (" +
                ID + " INTEGER PRIMARY KEY," +
                NAME + " TEXT," +
                LATITUDE + " REAL," +
                LONGITUDE + " REAL," +
                UPDATE_TIME + "INTEGER" +
                ");");

        insertTempData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE friends;");
        onCreate(db);
    }

    public static List<FriendData> getAllFriends(Context context) {
        SQLiteDatabase db = new DBHelper(context).getReadableDatabase();
        Cursor c = db.query(DBHelper.FRIENDS, null, null, null, null, null, null);
        if (c == null || !c.moveToFirst()) {
            return Collections.emptyList();
        }

        int id = c.getColumnIndex(DBHelper.ID);
        int name = c.getColumnIndex(DBHelper.NAME);
        int latitude = c.getColumnIndex(DBHelper.LATITUDE);
        int longitude = c.getColumnIndex(DBHelper.LONGITUDE);
        List<FriendData> res = new ArrayList<FriendData>();
        do {
            FriendData data = new FriendData(c.getLong(id), c.getString(name), c.getDouble(latitude), c.getDouble(longitude));
            res.add(data);
        } while (c.moveToNext());
        db.close();
        return res;
    }

    private void insertTempData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO friends VALUES (1, \"Марина Васильева\", 59.9570072, 30.2729272, 0);");
        db.execSQL("INSERT INTO friends VALUES (2, \"Артем Комаров\", 59.9676194, 30.384112, 0);");
        db.execSQL("INSERT INTO friends VALUES (3, \"Николай Иванов\", 59.9411471, 30.3793913, 0);");
        db.execSQL("INSERT INTO friends VALUES (4, \"Эдгар Кузнецов\", 59.9235158, 30.3332144, 0);");
        db.execSQL("INSERT INTO friends VALUES (5, \"Наталья Костенева\", 59.9232577, 30.2954489, 0);");
    }
}
