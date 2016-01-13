package com.mona.personalizedtwitter;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class TweetReply extends ActionBarActivity implements View.OnClickListener{
    private static Twitter twitter;
    private static RequestToken requestToken;
    private static SharedPreferences sharedPreferences;

    private String consumerKey = null;
    private String consumerSecret = null;
    private String callbackUrl = null;
    private String oAuthVerifier = null;



    private long tweetID = 0;
    private String tweetName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_reply);

    }

    @Override
    protected void onResume() {
        super.onResume();
        replyTweet();
    }
    private void replyTweet(){
        sharedPreferences = getSharedPreferences(StringTokens.PREF_NAME, 0);
        String userToken = sharedPreferences.getString(StringTokens.PREF_KEY_OAUTH_TOKEN, null);
        String userSecret = sharedPreferences.getString(StringTokens.PREF_KEY_OAUTH_SECRET, null);


        Configuration twitConf = new ConfigurationBuilder().setOAuthConsumerKey(StringTokens.consumerKey).setOAuthConsumerSecret(StringTokens.consumerSecret).setOAuthAccessToken(userToken)
                .setOAuthAccessTokenSecret(userSecret).build();


        twitter = new TwitterFactory(twitConf).getInstance();
        Bundle extras = getIntent().getExtras();
        tweetID = extras.getLong("tweetID");
        tweetName = extras.getString("tweetUser");
        Log.d(StringTokens.TAG,"ID"+tweetID);
        Log.d(StringTokens.TAG,"Name"+tweetName);
        EditText theReply = (EditText)findViewById(R.id.tweettext);
        theReply.setText("@"+tweetName+" ");
        theReply.setSelection(theReply.getText().length());
        Button tweetButton = (Button)findViewById(R.id.dotweet);
        tweetButton.setOnClickListener(this);

    }


    public void onClick(View v) {
            EditText tweetTxt = (EditText)findViewById(R.id.tweettext);
           switch(v.getId()) {
            case R.id.dotweet:
                //send tweet
                String toTweet = tweetTxt.getText().toString();
                try {
                    //handle replies

                    twitter.updateStatus(new StatusUpdate(toTweet).inReplyToStatusId(tweetID));

                    tweetTxt.setText("");

                }
                catch(TwitterException te) { Log.e(StringTokens.TAG, te.getMessage()); }
                break;
            default:
                break;

        }
        finish();
    }
}
