package com.mona.personalizedtwitter.customUtilites;

import android.content.Context;
import android.content.SharedPreferences;

import com.mona.personalizedtwitter.StringTokens;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by mona on 1/13/2016.
 */
public class TwitterInstanceManager {
   public TwitterInstanceManager(){};
    public Twitter getTwitterInstance(Context c)
    {
        SharedPreferences sharedPreferences = c.getSharedPreferences(StringTokens.PREF_NAME, 0);
        String userToken = sharedPreferences.getString(StringTokens.PREF_KEY_OAUTH_TOKEN, null);
        String userSecret = sharedPreferences.getString(StringTokens.PREF_KEY_OAUTH_SECRET, null);


        Configuration twitConf = new ConfigurationBuilder().setOAuthConsumerKey(StringTokens.consumerKey).setOAuthConsumerSecret(StringTokens.consumerSecret).setOAuthAccessToken(userToken)
                .setOAuthAccessTokenSecret(userSecret).build();


        Twitter twitter = new TwitterFactory(twitConf).getInstance();
        return twitter;
    }

}
