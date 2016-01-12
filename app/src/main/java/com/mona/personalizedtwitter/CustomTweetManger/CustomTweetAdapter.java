package com.mona.personalizedtwitter.CustomTweetManger;

import android.app.Activity;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mona.personalizedtwitter.R;
import com.mona.personalizedtwitter.TweetReply;
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
        holder.reply=(Button)convertView.findViewById(R.id.reply);
        holder.reply.setOnClickListener(tweetListener);
        convertView.setTag(holder);
    } else {
        holder = (ViewHolder) convertView.getTag();
    }

    CustomTweetModal t = tweets.get(position);
    holder.updateText.setText(t.getUpdate_text());
    long createdAt = t.getUpdate_time();
    holder.updateTime.setText(DateUtils.getRelativeTimeSpanString(createdAt)+" ");
    holder.userScreen.setText(t.getUser_screen());
    ReplyTweetDataModel tweetdata=new ReplyTweetDataModel(t.getT_id(),t.getUser_screen());
    holder.reply.setTag(tweetdata);

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
        Button reply;
    }
    private View.OnClickListener tweetListener = new View.OnClickListener() {
        //onClick method
        public void onClick(View v) {
            //create an intent for sending a new tweet
            Intent replyIntent = new Intent(v.getContext(),TweetReply.class);
            //get the data from the tag within the button view
            ReplyTweetDataModel theData = (ReplyTweetDataModel)v.getTag();
            //pass the status ID
            replyIntent.putExtra("tweetID", theData.getID());
            //pass the user name
            replyIntent.putExtra("tweetUser", theData.getUser());
            //go to the tweet screen
            v.getContext().startActivity(replyIntent);
        }
    };
}
