package com.af.pxl.custom;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.af.pxl.R;

/**
 * Created by Aefyr on 02.12.2017.
 */

public class ListAdapterWithIcons extends ArrayAdapter<ItemWithIcon> {
    private ItemWithIcon[] items;

    public ListAdapterWithIcons(@NonNull Context context, @NonNull ItemWithIcon[] objects) {
        super(context, R.layout.list_item_with_icon, objects);
        this.items = objects;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Nullable
    @Override
    public ItemWithIcon getItem(int position) {
        return items[position];
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v;
        if(convertView!=null)
            v = convertView;
        else
            v = LayoutInflater.from(getContext()).inflate(R.layout.list_item_with_icon, parent, false);

        ((TextView)v.findViewById(R.id.text)).setText(items[position].text);
        ((ImageView)v.findViewById(R.id.icon)).setImageResource(items[position].icon);

        return v;
    }
}
