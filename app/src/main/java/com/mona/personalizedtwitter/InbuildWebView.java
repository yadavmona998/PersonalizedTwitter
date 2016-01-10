package com.mona.personalizedtwitter;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class InbuildWebView extends ActionBarActivity {

    private WebView webView;

    public static String AUTHENTICATE_URL_DATA = "athenticate_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbuild_web_view);

        final String url = this.getIntent().getStringExtra(AUTHENTICATE_URL_DATA);

        if(url == null) {
            finish();
        }

        webView = (WebView) findViewById(R.id.myWebView);
        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl(url);
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if(url.contains(getResources().getString(R.string.twitter_callback))) {

                Uri uri = Uri.parse(url);

                String verifier = uri.getQueryParameter(getString(R.string.twitter_oauth_verifier));
                Intent resultIntent = new Intent();
                resultIntent.putExtra(getString(R.string.twitter_oauth_verifier), verifier);
                setResult(RESULT_OK, resultIntent);

                finish();
                return true;
            }
            return false;
        }
    }
}
