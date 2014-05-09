package ru.ifmo.findmyfriend.friendlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.findmyfriend.R;

/**
 * Created by: avgarder
 */
public class FriendListAdapter extends BaseAdapter {
    private List<FriendData> mData;
    private LayoutInflater mInflater;

    public FriendListAdapter(Context context, List<FriendData> data) {
        mData = new ArrayList<FriendData>(data);
        mInflater = LayoutInflater.from(context);
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

        avatar.setImageResource(parent.getContext().getResources().getIdentifier("ava" + (position + 1), "drawable", "ru.ifmo.findmyfriend"));
        name.setText(getItem(position).name);
        return view;
    }
}
