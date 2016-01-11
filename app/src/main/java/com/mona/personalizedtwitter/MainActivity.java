package com.mona.personalizedtwitter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.provider.BaseColumns;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
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

        setContentView(R.layout.activity_main);
        connectionChecker=new ConnectionChecker(getApplicationContext());
        alert=new AlertDialogueGenerator();
        tweets=new ArrayList<>();
        loginLayout = (RelativeLayout) findViewById(R.id.login_layout);
        timeline_view=(LinearLayout)findViewById(R.id.timelineLayout);
        userName = (TextView) findViewById(R.id.user_name);
        ls=(ListView)findViewById(R.id.timelinelistView);
        if(!connectionChecker.checkConnection())
        {
            alert.showAlertDialog(MainActivity.this, "Internet Connection Error", "Please connect to working Internet connection", false);
            return;
        }
        Log.d(TAG,"checked successfully");
        findViewById(R.id.btn_login).setOnClickListener(this);
       

        if(TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
            Toast.makeText(this, "Twitter key or secret not configured",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        sharedPreferences = getSharedPreferences(StringTokens.PREF_NAME, 0);

        boolean isLoggedIn = sharedPreferences.getBoolean(StringTokens.PREF_KEY_TWITTER_LOGIN, false);

        if(isLoggedIn) {
            new fetchTimelineAsync().execute("startask");
            loginLayout.setVisibility(View.GONE);
            String username = sharedPreferences.getString(StringTokens.PREF_USER_NAME, "");
            userName.setText("hello " + username);


        } else {
            loginLayout.setVisibility(View.VISIBLE);
            userName.setVisibility(View.GONE);

            Uri uri = getIntent().getData();

            if(uri != null && uri.toString().startsWith(callbackUrl)) {

                String verifier = uri.getQueryParameter(oAuthVerifier);

                try {

                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                    long userId = accessToken.getUserId();
                    final User user = twitter.showUser(userId);
                    final String username = user.getName();

                    saveUserData(accessToken);
                    new fetchTimelineAsync().execute("startask");
                    loginLayout.setVisibility(View.GONE);
                    timeline_view.setVisibility(View.VISIBLE);
                    userName.setText("hello " + username);

                            } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
        boolean isLoggedIn = sharedPreferences.getBoolean(StringTokens.PREF_KEY_TWITTER_LOGIN, false);

        if(!isLoggedIn) {
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
        } else {new fetchTimelineAsync().execute("startask");
            loginLayout.setVisibility(View.GONE);
            timeline_view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "getting back from inbuildwebView");
        if(resultCode == Activity.RESULT_OK) {
            String verifier = data.getExtras().getString(oAuthVerifier);

            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

                long userId = accessToken.getUserId();
                final User user = twitter.showUser(userId);
                String username = user.getName();


                saveUserData(accessToken);
                new fetchTimelineAsync().execute("startask");
                loginLayout.setVisibility(View.GONE);
                timeline_view.setVisibility(View.VISIBLE);

                userName.setText("hello "+username);


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


    class fetchTimelineAsync extends AsyncTask<String, String, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Fetching Twiiter Timeline...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
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
                Log.d(TAG,"got twitter instance successfully");
                try{
                    List<twitter4j.Status>homeTimeline=timelineTwitter.getHomeTimeline();
                    Log.d(TAG,"got timeline");
                    for (twitter4j.Status statusUpdate : homeTimeline)
                    {String tst=statusUpdate.getText();
                     CustomTweetModal t=new CustomTweetModal();
                        Log.d(TAG,"checked data updates is"+tst);
                        t.setT_id(statusUpdate.getId());
                     t.setUpdate_text(tst);
                     t.setUser_screen(statusUpdate.getUser().getScreenName());
                     t.setUser_img(statusUpdate.getUser().getProfileImageURL().toString());

                     t.setUpdate_time(statusUpdate.getCreatedAt().getTime());
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

            pd.dismiss();

            Toast.makeText(MainActivity.this, "Posted to Twitter!", Toast.LENGTH_SHORT);

            CustomTweetAdapter myAdapter=new CustomTweetAdapter(MainActivity.this,R.layout.tweet_row,tweets);
            ls.setAdapter(myAdapter);
        }
    }
    


}

