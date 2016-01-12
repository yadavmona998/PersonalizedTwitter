 package com.mona.personalizedtwitter.CustomTweetManger;

/**
 * Created by mona on 1/11/2016.
 */
public class ReplyTweetDataModel {
    private long tweetID;
    private String tweetUser;

    public ReplyTweetDataModel(long ID, String screenName) {
        //instantiate variables
        tweetID = ID;
        tweetUser = screenName;
    }

    public long getID() {
        return tweetID;
    }

    public String getUser() {
        return tweetUser;
    }
}
