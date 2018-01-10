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

public class FeedAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<FeedInfo> feedList;

    public FeedAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setFeedList(ArrayList<FeedInfo> feedList) {
        this.feedList = feedList;
    }

    @Override
    public int getCount() {
        return feedList.size();
    }

    @Override
    public Object getItem(int position) {
        return feedList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return feedList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeedInfo info = feedList.get(position);
        if(info.getComment() != null) {
            if (info.getFeedId() == -1) {
                convertView = layoutInflater.inflate(R.layout.feed_read_more, parent, false);
                ((TextView) convertView.findViewById(R.id.feed_comment)).setText(String.valueOf(info.getComment()));
            }else {
                convertView = layoutInflater.inflate(R.layout.feed_item, parent, false);
                ((ImageView) convertView.findViewById(R.id.feed_icon)).setImageBitmap(info.getIconBitmap());
                ((TextView) convertView.findViewById(R.id.feed_name)).setText(String.valueOf(info.getUserName()));
                ((TextView) convertView.findViewById(R.id.feed_date)).setText(String.valueOf(info.getDate()));
                ((TextView) convertView.findViewById(R.id.feed_comment)).setText(String.valueOf(info.getComment()));
            }
        }else {
            convertView = layoutInflater.inflate(R.layout.feed_image_item, parent, false);
            ((ImageView) convertView.findViewById(R.id.feed_icon)).setImageBitmap(info.getIconBitmap());
            ((TextView) convertView.findViewById(R.id.feed_name)).setText(String.valueOf(info.getUserName()));
            ((TextView) convertView.findViewById(R.id.feed_date)).setText(String.valueOf(info.getDate()));
            ((ImageView) convertView.findViewById(R.id.feed_image)).setImageBitmap(info.getImageBitmap());
        }
        return convertView;
    }
}
