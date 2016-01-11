package com.mona.personalizedtwitter;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mona.personalizedtwitter.asynctask.ImageDownloaderTask;

import java.util.ArrayList;

/**
 * Created by mona on 1/11/2016.
 */
public class CustomTweetAdapter extends ArrayAdapter<CustomTweetModal> {
    ArrayList<CustomTweetModal> tweets;
    LayoutInflater inflater;

    public CustomTweetAdapter(Activity activity, int resource, ArrayList<CustomTweetModal>arrayList) {
        super(activity, resource, arrayList);
        tweets=arrayList;
        inflater=activity.getWindow().getLayoutInflater();
    }

@Override
    public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
        convertView = inflater.inflate(R.layout.tweet_row, null);
        holder = new ViewHolder();
        holder.updateText = (TextView) convertView.findViewById(R.id.updateText);
        holder.updateTime= (TextView) convertView.findViewById(R.id.updateTime);
        holder.userScreen=(TextView)   convertView.findViewById(R.id.userScreen);
        holder.user_img = (ImageView) convertView.findViewById(R.id.userImg);
        convertView.setTag(holder);
    } else {
        holder = (ViewHolder) convertView.getTag();
    }

    CustomTweetModal t = tweets.get(position);
    holder.updateText.setText(t.getUpdate_text());
    long createdAt = t.getUpdate_time();
    holder.updateTime.setText(DateUtils.getRelativeTimeSpanString(createdAt)+" ");
    holder.userScreen.setText(t.getUser_screen());

    if (holder.user_img != null) {
        new ImageDownloaderTask(holder.user_img).execute(t.getUser_img());
    }

    return convertView;
}

    static class ViewHolder {
        TextView updateText;
        TextView userScreen;
        TextView updateTime;
        ImageView user_img;
    }
}
