package com.example.bootcampsns.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bootcampsns.R;

import java.util.ArrayList;

/**
 * Created by 01020410 on 2017/11/08.
 */

public class FriendAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<FriendInfo> friendList;

    public FriendAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setFriendList(ArrayList<FriendInfo> friendList) {
        this.friendList = friendList;
    }

    @Override
    public int getCount() {
        return friendList.size();
    }

    @Override
    public Object getItem(int position) {
        return friendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return friendList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.friend_item, parent, false);
        ((ImageView) convertView.findViewById(R.id.friend_icon)).setImageBitmap(friendList.get(position).getIconBitmap());
        ((TextView) convertView.findViewById(R.id.friend_name)).setText(String.valueOf(friendList.get(position).getUserName()));
        return convertView;
    }
}
