package ru.ifmo.findmyfriend.drawer;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.findmyfriend.R;
import ru.ifmo.findmyfriend.utils.BitmapStorage;

public class DrawerListAdapter extends BaseAdapter {
    private static int[] scaledSizes = new int[]{48, 32, 48, 64, 96};
    private List<DrawerItem> mData;
    private LayoutInflater mInflater;

    public DrawerListAdapter(Context context, List<DrawerItem> data) {
        mData = new ArrayList<DrawerItem>(data);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public DrawerItem getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView title = (TextView) view.findViewById(R.id.title);

        icon.setImageResource(mData.get(position).getIcon());
        title.setText(mData.get(position).getTitle());

        if (position == 0) {
            int screenLayout = parent.getResources().getConfiguration().screenLayout
                    & Configuration.SCREENLAYOUT_SIZE_MASK;
            int scaledSize = scaledSizes[screenLayout];
            Bitmap avatar = BitmapStorage.getInstance().getBitmap(parent.getContext(),
                    mData.get(position).getUrl());
            if (avatar != null) {
                icon.setImageBitmap(Bitmap.createScaledBitmap(avatar, scaledSize, scaledSize, false));
            }
        }
        return view;
    }

}