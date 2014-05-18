package ru.ifmo.findmyfriend.drawer;


import android.content.Context;
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
    private List<DrawerItem> data;
    private LayoutInflater inflater;

    public DrawerListAdapter(Context context, List<DrawerItem> data) {
        this.data = new ArrayList<DrawerItem>(data);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public DrawerItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView title = (TextView) view.findViewById(R.id.title);

        title.setText(data.get(position).getTitle());

        if (position == 0) {
            icon.setImageBitmap(BitmapStorage.getInstance().getBitmap(parent.getContext(),
                    data.get(position).getUrl()));
        } else {
            icon.setImageResource(data.get(position).getIconResource());
        }
        return view;
    }

}