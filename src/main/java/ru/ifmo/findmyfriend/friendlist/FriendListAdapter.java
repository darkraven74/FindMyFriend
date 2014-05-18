package ru.ifmo.findmyfriend.friendlist;

import android.content.Context;
import android.graphics.Color;
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
    private List<FriendData> data;
    private LayoutInflater inflater;

    public FriendListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        data = Collections.emptyList();
    }

    public void setData(List<FriendData> newData) {
        data = new ArrayList<FriendData>(newData);
        Collections.sort(data, new Comparator<FriendData>() {
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
        return data.size();
    }

    @Override
    public FriendData getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.friend_list_item, null);
        }

        ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
        TextView name = (TextView) view.findViewById(R.id.name);

        Context context = parent.getContext();
        FriendData friend = getItem(position);

        avatar.setImageBitmap(BitmapStorage.getInstance().getBitmap(context, friend.imageUrl));
        name.setText(friend.name);

        if (friend.isAlive) {
            view.setClickable(false);
            name.setTextColor(Color.BLACK);
        } else {
            view.setClickable(true);
            name.setTextColor(Color.rgb(0xAA, 0xAA, 0xAA));
        }

        TextView status = (TextView) view.findViewById(R.id.status);
        ImageView divider = (ImageView) view.findViewById(R.id.divider);

        if (position == 0) {
            status.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            if (friend.isAlive) {
                status.setText(context.getString(R.string.friends_online));
            } else {
                status.setText(context.getString(R.string.friends_offline));
            }
        } else if (!friend.isAlive && getItem(position - 1).isAlive) {
            status.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            status.setText(context.getString(R.string.friends_offline));
        } else {
            status.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }

        return view;
    }
}
