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


       /**the update ID for this tweet if it is a reply*/
    private long tweetID = 0;
    /**the username for the tweet if it is a reply*/
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
        //get preferences for user twitter details
        sharedPreferences = getSharedPreferences(StringTokens.PREF_NAME, 0);

        //get user token and secret for authentication
        String userToken = sharedPreferences.getString(StringTokens.PREF_KEY_OAUTH_TOKEN, null);
        String userSecret = sharedPreferences.getString(StringTokens.PREF_KEY_OAUTH_SECRET, null);

        //create a new twitter configuration usign user details
        Configuration twitConf = new ConfigurationBuilder().setOAuthConsumerKey(StringTokens.consumerKey).setOAuthConsumerSecret(StringTokens.consumerSecret).setOAuthAccessToken(userToken)
                .setOAuthAccessTokenSecret(userSecret).build();

        //create a twitter instance
        twitter = new TwitterFactory(twitConf).getInstance();
        Bundle extras = getIntent().getExtras();

            //get the ID of the tweet we are replying to
            tweetID = extras.getLong("tweetID");
            //get the user screen name for the tweet we are replying to
            tweetName = extras.getString("tweetUser");
            Log.d(StringTokens.TAG,"ID"+tweetID);
            Log.d(StringTokens.TAG,"NAme"+tweetName);
            //use the passed information
            EditText theReply = (EditText)findViewById(R.id.tweettext);
            //start the tweet text for the reply @username
            theReply.setText("@"+tweetName+" ");
            //set the cursor to the end of the text for entry
            theReply.setSelection(theReply.getText().length());


        //set up listener for choosing home button to go to timeline
        //LinearLayout tweetClicker = (LinearLayout)findViewById(R.id.homebtn);
        //tweetClicker.setOnClickListener(this);

        //set up listener for send tweet button
        Button tweetButton = (Button)findViewById(R.id.dotweet);
        tweetButton.setOnClickListener(this);

    }
    public void onClick(View v) {
        //handle home and send button clicks
        EditText tweetTxt = (EditText)findViewById(R.id.tweettext);
        //find out which view has been clicked
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
            //case R.id.homebtn:
                //go to the home timeline

               // break;
            default:
                break;

        }
        finish();
    }
}
