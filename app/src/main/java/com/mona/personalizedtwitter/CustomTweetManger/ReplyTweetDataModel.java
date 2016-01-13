 package com.mona.personalizedtwitter.CustomTweetManger;

 import android.util.Log;

 import com.mona.personalizedtwitter.StringTokens;

 /**
 * Created by mona on 1/11/2016.
 */
public class ReplyTweetDataModel {
    private long tweetID;
    private String tweetUser;
    private Integer fav_count,position;


    public ReplyTweetDataModel(long ID, String screenName,Integer fav_count,Integer position) {
        //instantiate variables

        tweetID = ID;
        tweetUser = screenName;
        this.fav_count=fav_count;
        this.position=position;

    }


    public long getID() {
        return tweetID;
    }

    public String getUser() {
        return tweetUser;
    }

    public Integer getPosition() {
        return position;
    }
}
