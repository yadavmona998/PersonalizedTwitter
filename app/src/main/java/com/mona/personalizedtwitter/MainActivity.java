package com.mona.personalizedtwitter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mona.personalizedtwitter.CustomTweetManger.CustomTweetAdapter;
import com.mona.personalizedtwitter.CustomTweetManger.CustomTweetModal;
import com.mona.personalizedtwitter.CustomTweetManger.ReplyTweetDataModel;
import com.mona.personalizedtwitter.customUtilites.AlertDialogueGenerator;
import com.mona.personalizedtwitter.customUtilites.ConnectionChecker;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterResponse;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class MainActivity extends Activity implements View.OnClickListener {


    public static final String TAG="tag";
    public static final int WEBVIEW_REQUEST_CODE = 100;


    private ConnectionChecker connectionChecker;
    private AlertDialogueGenerator alert;

    private static Twitter twitter;
    private static RequestToken requestToken;
    private static SharedPreferences sharedPreferences;


    private TextView userName;
    private View loginLayout;
    private View timeline_view;
    private ListView ls;
    private ProgressDialog pd;
    private SwipeRefreshLayout swipeContainer;
    private Integer flag=0;



    private String consumerKey = null;
    private String consumerSecret = null;
    private String callbackUrl = null;
    private String oAuthVerifier = null;
    private ArrayList<CustomTweetModal>tweets;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeStrings();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        tweets = new ArrayList<>();
        sharedPreferences = getSharedPreferences(StringTokens.PREF_NAME, 0);
        boolean isLoggedIn = sharedPreferences.getBoolean(StringTokens.PREF_KEY_TWITTER_LOGIN, false);
        if (!isLoggedIn) {
            setContentView(R.layout.activity_main);
            connectionChecker = new ConnectionChecker(getApplicationContext());
            alert = new AlertDialogueGenerator();
            loginLayout = (RelativeLayout) findViewById(R.id.login_layout);

            if (!connectionChecker.checkConnection()) {
                alert.showAlertDialog(MainActivity.this, "Internet Connection Error", "Please connect to working Internet connection", false);
                return;
            }
            Log.d(TAG, "checked successfully");
            findViewById(R.id.btn_login).setOnClickListener(this);


            if (TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
                Toast.makeText(this, "Twitter key or secret not configured",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri = getIntent().getData();

            if (uri != null && uri.toString().startsWith(callbackUrl)) {

                String verifier = uri.getQueryParameter(oAuthVerifier);

                try {

                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                    saveUserData(accessToken);
                    setUserTimeline();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else{Log.d(TAG,"user already logged in");
            setUserTimeline();
        }
    }


    private void initializeStrings() {
        consumerKey = StringTokens.consumerKey;
        consumerSecret =StringTokens.consumerSecret;
        callbackUrl = StringTokens.callbackUrl;
        oAuthVerifier = getString(R.string.twitter_oauth_verifier);
    }

    private void saveUserData(AccessToken accessToken) {
        long userId = accessToken.getUserId();
        User user;
        try {

            user = twitter.showUser(userId);
            String username = user.getName();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString(StringTokens.PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
            e.putString(StringTokens.PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
            e.putBoolean(StringTokens.PREF_KEY_TWITTER_LOGIN, true);
            e.putString(StringTokens.PREF_USER_NAME, username);
            e.commit();

        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    private void loginToTwitter() {
        Log.d(TAG, "login btn clicked");
            final ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(consumerKey);
            builder.setOAuthConsumerSecret(consumerSecret);
            final Configuration configuration = builder.build();
            final TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();
             try {
                requestToken = twitter.getOAuthRequestToken(callbackUrl);
                Log.d(TAG, "getting user verified");
                final Intent intent = new Intent(this, InbuildWebView.class);
                intent.putExtra(InbuildWebView.AUTHENTICATE_URL_DATA, requestToken.getAuthenticationURL());
                startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
            } catch (TwitterException e) {
                e.printStackTrace();
            }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "getting back from inbuildwebView");
        if(resultCode == Activity.RESULT_OK) {
            String verifier = data.getExtras().getString(oAuthVerifier);

            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                saveUserData(accessToken);
                setUserTimeline();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login :
                loginToTwitter();

                break;
              //implemented all views onClick functions here
        }
    }

    public void setUserTimeline()
    {
        setContentView(R.layout.timeline);
        connectionChecker = new ConnectionChecker(getApplicationContext());
        alert = new AlertDialogueGenerator();
        timeline_view=(LinearLayout)findViewById(R.id.timelineLayout);
        userName = (TextView) findViewById(R.id.user_name);
        ls=(ListView)findViewById(R.id.timelinelistView);
        swipeContainer=(SwipeRefreshLayout)findViewById(R.id.swipeContainer);
        //Log.d(TAG,"set view timeline "+ls);
          swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                flag=1;
                new fetchTimelineAsync().execute("startask");
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,android.R.color.holo_green_light
        ,android.R.color.holo_orange_light,android.R.color.holo_red_light);



        if (!connectionChecker.checkConnection()) {
            alert.showAlertDialog(MainActivity.this, "Internet Connection Error", "Please connect to working Internet connection", false);
            return;
        }
        new fetchTimelineAsync().execute("startask");
        String username = sharedPreferences.getString(StringTokens.PREF_USER_NAME, "");
        userName.setText("hello " + username);
    }


    class fetchTimelineAsync extends AsyncTask<String, String, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Fetching Twitter Timeline...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
           if(flag==0) pd.show();
        }

        @Override
        protected Void doInBackground(String... params) {

                String check = params[0];


                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(consumerKey);
                builder.setOAuthConsumerSecret(consumerSecret);

                String access_token = sharedPreferences.getString(StringTokens.PREF_KEY_OAUTH_TOKEN, "");
                String access_token_secret = sharedPreferences.getString(StringTokens.PREF_KEY_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);

                Twitter timelineTwitter = new TwitterFactory(builder.build()).getInstance(accessToken);
                Log.d(TAG, "got twitter instance successfully");
                try{
                    List<twitter4j.Status>homeTimeline=timelineTwitter.getHomeTimeline();
                    Log.d(TAG,"got timeline");
                    for (twitter4j.Status statusUpdate : homeTimeline) {
                        String text = statusUpdate.getText();
                        long statusID = statusUpdate.getId();
                        String statusName = statusUpdate.getUser().getScreenName();
                        CustomTweetModal t = new CustomTweetModal();

                        t.setT_id(statusID);
                        Log.d(TAG, "id" + text + "mona"+statusUpdate.getInReplyToStatusId());

                        t.setUpdate_text(text);
                        t.setUser_screen(statusName);
                        t.setUser_img(statusUpdate.getUser().getProfileImageURL().toString());
                        t.setUpdate_time(statusUpdate.getCreatedAt().getTime());
                        t.setFav_count(statusUpdate.getFavoriteCount());
                        if(statusUpdate.isFavorited())
                            t.setIsfav(1);
                        else
                           t.setIsfav(0);


                        tweets.add(t);
                        Log.d(TAG, "tweet added and fetch successfully");


                    }
                }
                catch (Exception e){Log.e(TAG,"Exception: " + e);
                }

                return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(flag==0)
            pd.dismiss();
            else
               swipeContainer.setRefreshing(false);
            flag=0;
            Toast.makeText(MainActivity.this, "Posted to Twitter!", Toast.LENGTH_SHORT);
            CustomTweetAdapter myAdapter=new CustomTweetAdapter(MainActivity.this,R.layout.tweet_row,tweets);

            Log.d(TAG,"before writing onitemclicklistener");
            ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Object o = ls.getItemAtPosition(position);

                    CustomTweetModal tweetData = (CustomTweetModal) o;

                    Intent intent = new Intent(MainActivity.this, TweetDetailsActivity.class);
                    intent.putExtra("tweet", tweetData);
                    Log.d(TAG, "called onitemclicklistener" + tweetData.getUpdate_text());
                    startActivity(intent);
                }
            });
            ls.setAdapter(myAdapter);

        }
    }



}
