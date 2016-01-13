package com.mona.personalizedtwitter.CustomTweetManger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mona.personalizedtwitter.R;
import com.mona.personalizedtwitter.StringTokens;
import com.mona.personalizedtwitter.TweetReply;
import com.mona.personalizedtwitter.asynctask.ImageDownloaderTask;


import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by mona on 1/11/2016.
 */
public class CustomTweetAdapter extends ArrayAdapter<CustomTweetModal> {
    ArrayList<CustomTweetModal> tweets;
    LayoutInflater inflater;
    ListView ls;
    Activity activity;
    public CustomTweetAdapter(Activity activity, int resource, ArrayList<CustomTweetModal>arrayList) {
        super(activity, resource, arrayList);
        tweets=arrayList;
        inflater=activity.getWindow().getLayoutInflater();
        this.activity=activity;
    }

@Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    final ViewHolder holder;
    if (convertView == null) {
        convertView = inflater.inflate(R.layout.tweet_row, null);
        holder = new ViewHolder();
        holder.updateText = (TextView) convertView.findViewById(R.id.updateText);
        holder.updateTime= (TextView) convertView.findViewById(R.id.updateTime);
        holder.userScreen=(TextView)   convertView.findViewById(R.id.userScreen);
        holder.user_img = (ImageView) convertView.findViewById(R.id.userImg);
        holder.fav_count=(TextView)convertView.findViewById(R.id.fav_count);
        holder.favorite=(ImageView)convertView.findViewById(R.id.favorite);
        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SharedPreferences sharedPreferences =activity.getSharedPreferences(StringTokens.PREF_NAME, 0);
                String userToken = sharedPreferences.getString(StringTokens.PREF_KEY_OAUTH_TOKEN, null);
                String userSecret = sharedPreferences.getString(StringTokens.PREF_KEY_OAUTH_SECRET, null);

                Configuration twitConf = new ConfigurationBuilder().setOAuthConsumerKey(StringTokens.consumerKey).setOAuthConsumerSecret(StringTokens.consumerSecret).setOAuthAccessToken(userToken)
                        .setOAuthAccessTokenSecret(userSecret).build();

                Twitter t = new TwitterFactory(twitConf).getInstance();
                try
                {
                    Log.d(StringTokens.TAG, "h2 " + tweets.get(position).getT_id());
                    if(tweets.get(position).getIsfav()==0)
                    {t.createFavorite(tweets.get(position).getT_id());
                    holder.favorite.setImageDrawable(v.getContext().getResources().getDrawable(R.drawable.like));
                        Log.d(StringTokens.TAG, "h3 " + tweets.get(position).getT_id());
                    }

                    else
                    {
                        t.destroyFavorite(tweets.get(position).getT_id());
                        holder.favorite.setImageDrawable(v.getContext().getResources().getDrawable(R.drawable.dislike));
                    }

                    Status temp=t.showStatus(tweets.get(position).getT_id());
                    Log.d(StringTokens.TAG, "h4" + temp.getText() + temp.getFavoriteCount());
                    //setting the updated count
                    int ret = temp.getFavoriteCount();
                    holder.fav_count.setText("" + ret);
                    Log.d(StringTokens.TAG, "increasing like count" + temp);
                    Log.d(StringTokens.TAG, "h5");


                }
                catch (Exception e)
                {
                    e.printStackTrace();

                }

            }
        });



        holder.reply=(Button)convertView.findViewById(R.id.reply);
        holder.reply.setOnClickListener(tweetListener);

        convertView.setTag(holder);





    } else {
        holder = (ViewHolder) convertView.getTag();
    }

    CustomTweetModal t = tweets.get(position);
    holder.updateText.setText(t.getUpdate_text());
    holder.fav_count.setText(""+t.getFav_count());
    long createdAt = t.getUpdate_time();
    holder.updateTime.setText(DateUtils.getRelativeTimeSpanString(createdAt)+" ");
    holder.userScreen.setText(t.getUser_screen());
    if(t.getIsfav()==1)
        holder.favorite.setImageResource(R.drawable.like);
    else
        holder.favorite.setImageResource(R.drawable.dislike);
    Log.d(StringTokens.TAG,"mona setting tag for reply and like btn on tweet"+t.getUpdate_text());

    ReplyTweetDataModel tweetdata=new ReplyTweetDataModel(t.getT_id(),t.getUser_screen(),t.getFav_count(),position);
    holder.reply.setTag(tweetdata);
    holder.favorite.setTag(tweetdata);

    if (holder.user_img != null) {
        new ImageDownloaderTask(holder.user_img).execute(t.getUser_img());
    }
    Log.d(StringTokens.TAG,"adding view successfully"+holder.updateText.getText());
    return convertView;
}

    static class ViewHolder {
        TextView updateText;
        TextView userScreen;
        TextView updateTime;
        TextView fav_count;
        ImageView user_img;
        ImageView favorite;
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
