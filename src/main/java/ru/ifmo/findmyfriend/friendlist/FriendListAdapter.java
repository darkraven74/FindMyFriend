package ru.ifmo.findmyfriend.friendlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.ifmo.findmyfriend.R;
import ru.ifmo.findmyfriend.utils.BitmapStorage;

/**
 * Created by: avgarder
 */
public class FriendListAdapter extends BaseAdapter {
    private List<FriendData> mData;
    private LayoutInflater mInflater;

    private BitmapDrawable backgroundOffline;


    public FriendListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mData = Collections.emptyList();

        Bitmap background = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        background.setPixel(0, 0, Color.argb(40, 110, 110, 110));
        backgroundOffline = new BitmapDrawable(context.getResources(), background);
    }

    public void setData(List<FriendData> newData) {
        mData = new ArrayList<FriendData>(newData);
        Collections.sort(mData, new Comparator<FriendData>() {
            @Override
            public int compare(FriendData l, FriendData r) {
                if (l.isAlive && !r.isAlive) {
                    return -1;
                }
                if (!l.isAlive && r.isAlive) {
                    return 1;
                }
                return l.name.compareTo(r.name);
            }
        });
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public FriendData getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.friend_list_item, null);
        }

        ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
        TextView name = (TextView) view.findViewById(R.id.name);

        Context context = parent.getContext();
        FriendData friend = getItem(position);

        avatar.setImageBitmap(BitmapStorage.getInstance().getBitmap(context, friend.imageUrl));
        name.setText(friend.name);

        if (friend.isAlive) {
            view.setClickable(false);
            view.setBackgroundColor(Color.WHITE);
        } else {
            view.setClickable(true);
            view.setBackground(backgroundOffline);
        }
        return view;
    }
}
