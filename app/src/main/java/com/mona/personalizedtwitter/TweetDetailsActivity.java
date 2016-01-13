package com.mona.personalizedtwitter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mona.personalizedtwitter.CustomTweetManger.CustomTweetAdapter;
import com.mona.personalizedtwitter.CustomTweetManger.CustomTweetModal;
import com.mona.personalizedtwitter.CustomTweetManger.ReplyTweetDataModel;
import com.mona.personalizedtwitter.asynctask.ImageDownloaderTask;
import com.mona.personalizedtwitter.customUtilites.TwitterInstanceManager;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;


public class TweetDetailsActivity extends ActionBarActivity implements View.OnClickListener{
    private CustomTweetModal tweet;
    private CustomTweetAdapter adapter;
    private ArrayList<Status> replytweets;
    private Twitter twitter;
    private TwitterInstanceManager twitterInstanceManager;
    private ArrayList<CustomTweetModal>customreplies;
    private ListView replyls;
    private Context context;




    Button mainReplyBtn;
    ImageView favorite;
    TextView fav_count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);
        tweet = (CustomTweetModal) this.getIntent().getSerializableExtra("tweet");
        context=TweetDetailsActivity.this;

        if (null != tweet) {
            ImageView user_img = (ImageView) findViewById(R.id.userImg);
            new ImageDownloaderTask(user_img).execute(tweet.getUser_img());


            TextView user_screen = (TextView) findViewById(R.id.userScreen);
            TextView updateTime = (TextView) findViewById(R.id.updateTime);
            TextView updateText = (TextView) findViewById(R.id.updateText);
            replyls = (ListView) findViewById(R.id.replyListView);
            mainReplyBtn=(Button)findViewById(R.id.reply);
            favorite=(ImageView)findViewById(R.id.favorite);
            fav_count=(TextView)findViewById(R.id.fav_count);

            user_screen.setText(tweet.getUser_screen());
            long createdAt = tweet.getUpdate_time();
            updateTime.setText(DateUtils.getRelativeTimeSpanString(createdAt) + " ");
            updateText.setText(tweet.getUpdate_text());
            if(tweet.getIsfav()==1)
                favorite.setImageResource(R.drawable.like);
            else
                favorite.setImageResource(R.drawable.dislike);
            mainReplyBtn.setOnClickListener(this);
            ReplyTweetDataModel tweetdata=new ReplyTweetDataModel(tweet.getT_id(),tweet.getUser_screen(),tweet.getFav_count(),0);
            mainReplyBtn.setTag(tweetdata);
            favorite.setTag(tweetdata);
            favorite.setOnClickListener(this);
            new fetchReplyTweetsAsync().execute("startask");




        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.reply : {
                //create an intent for sending a new tweet
                Intent replyIntent = new Intent(v.getContext(), TweetReply.class);
                //get the data from the tag within the button view
                ReplyTweetDataModel theData = (ReplyTweetDataModel) v.getTag();
                //pass the status ID
                replyIntent.putExtra("tweetID", theData.getID());
                //pass the user name
                replyIntent.putExtra("tweetUser", theData.getUser());
                //go to the tweet screen
                v.getContext().startActivity(replyIntent);


                break;
            }
            case R.id.favorite: {
                TwitterInstanceManager twitterInstanceManager=new TwitterInstanceManager();
                Twitter t =twitterInstanceManager.getTwitterInstance(context);
                try
                {
                    if(tweet.getIsfav()==0)
                    {
                        t.createFavorite(tweet.getT_id());
                        favorite.setImageDrawable(v.getContext().getResources().getDrawable(R.drawable.like));
                    }

                    else
                    {
                        t.destroyFavorite(tweet.getT_id());
                        favorite.setImageDrawable(v.getContext().getResources().getDrawable(R.drawable.dislike));
                    }

                    Status temp=t.showStatus(tweet.getT_id());
                    int ret = temp.getFavoriteCount();
                    fav_count.setText("" + ret);

                }
                catch (Exception e)
                {
                    e.printStackTrace();

                }

            }




        break;
            }
            //implemented all views onClick functions here
        }



    class fetchReplyTweetsAsync extends AsyncTask<String, String, Void> {


        @Override
        protected Void doInBackground(String... params) {

            String check = params[0];


            twitterInstanceManager=new TwitterInstanceManager();
            twitter=twitterInstanceManager.getTwitterInstance(TweetDetailsActivity.this);
            Log.d(StringTokens.TAG, "mona got twiiter instance successfully");
            twitter4j.Status status;
            try{
                status= twitter.showStatus(tweet.getT_id());
                Log.d(StringTokens.TAG,"found reply tweetstatus"+status.getText());
                replytweets = new ArrayList<>();
                replytweets=getDiscussion(status,twitter);
                customreplies=new ArrayList<>();
                for (int i = 0; i < replytweets.size(); i++) {
                    CustomTweetModal t = new CustomTweetModal();
                    twitter4j.Status temp = replytweets.get(i);
                    Log.d(StringTokens.TAG,"mona found reply as"+temp.getText());
                    t.setT_id(temp.getId());
                    t.setUpdate_text(temp.getText());
                    t.setUser_screen(temp.getUser().getScreenName());
                    t.setUser_img(temp.getUser().getProfileImageURL().toString());
                    t.setUpdate_time(temp.getCreatedAt().getTime());
                    t.setFav_count(temp.getFavoriteCount());
                    if(temp.isFavorited())
                        t.setIsfav(1);
                    else
                        t.setIsfav(0);

                    customreplies.add(t);
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter = new CustomTweetAdapter(TweetDetailsActivity.this, R.layout.tweet_row, customreplies);
            replyls.setAdapter(adapter);

        }
    }


    public ArrayList<Status> getDiscussion(Status status, Twitter twitter) {
        ArrayList<Status> replies = new ArrayList<>();
        Log.d(StringTokens.TAG,"mona inside getdiscussions");
        ArrayList<Status> all = null;

        try {
            long id = status.getId();
            String screenname = status.getUser().getScreenName();

            Query query = new Query("@" + screenname + " since_id:" + id);

            System.out.println("query string: " + query.getQuery());

            try {
                query.setCount(100);
            } catch (Throwable e) {
                // enlarge buffer error?
                query.setCount(30);
            }

            QueryResult result = twitter.search(query);
            System.out.println("result: " + result.getTweets().size());

            all = new ArrayList<Status>();

            do {
                System.out.println("do loop repetition");

                List<Status> tweets = result.getTweets();

                for (Status tweet : tweets)
                    if (tweet.getInReplyToStatusId() == id)
                        all.add(tweet);

                if (all.size() > 0) {
                    for (int i = all.size() - 1; i >= 0; i--)
                        replies.add(all.get(i));
                    all.clear();
                }

                query = result.nextQuery();

                if (query != null)
                    result = twitter.search(query);

            } while (query != null);

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        Log.d(StringTokens.TAG,"mona returning repliesinstance successfully");
        return replies;

    }
}
